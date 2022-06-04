package com.example.myapplication.Activities;

import static android.util.Half.EPSILON;
import static java.lang.Math.sqrt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.example.myapplication.map.Cell;
import com.example.myapplication.map.Layout;
import com.example.myapplication.motion.StepCounter;
import com.example.myapplication.particles.Particle;
import com.example.myapplication.particles.ParticleCollection;
import com.google.android.gms.common.logging.Logger;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;


//import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class LocalizationActivity extends AppCompatActivity implements SensorEventListener {
    private FusedLocationProviderClient fusedLocationClient;
    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private Sensor stepCounter;
    private Sensor accSensor;
    private Sensor magSensor;
    private Sensor gyroSensor;
    private Button start;
    private Button end;
    private Button stepAdd;
    private TextView textStep;
    private TextView textOrientation;
    private TextView textCell;
    private final String TAG = "DEBUG";
    public int step;
    private Layout layout;
    private Canvas canvas;
    private ParticleCollection pc;
    private float stepDistance;
    private float refDirection;
    private final float tolerateWindow = 35;
    // Gravity for accelerometer data
    private float[] gravity = new float[3];
    // smoothed values
    private float[] smoothed = new float[3];
    // sensor manager
    private boolean ignore;
    private int countdown;
    private double prevY;
    private double threshold = 0;

    private int calibratedDirection = 0;

    private StepCounter sc;

    private boolean inStep;

    private List<String[]> data;

    float[] rotationMatrix = new float[9];
    float[] magnetometerReading = new float[3];
    //    float[] mGeoMags = new float[3];
    float[] orientationAngles = new float[3];
    float[] accelerometerReading = new float[3];

    private TextView degreeTV;
    private Boolean isLocationRetrieved = true;
    private Double latitude;
    private Double longitude;
    private Double altitude;
    private static final int REQUEST_PERMISSION_FINE_LOCATION = 1;
    int total = 0;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    //  OnSuccessListener<Location> altitudeUpdate = null;

    // Create a constant to convert nanoseconds to seconds.
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;
    float orientations[] = new float[3];
    boolean executed = false;
    float[] currentRotationMatrix = new float[16];

    public float height;


    // CSVWriter csvWriter = null;
    String csv = "/data/data/com.example.myapplication/cache/walk_sqrt.csv";


    OnSuccessListener<Location> altitudeUpdate = new OnSuccessListener<Location>() {
        @Override
        public void onSuccess(Location location) {
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                isLocationRetrieved = true;
                total += 1;
                latitude = Double.valueOf(location.getLatitude());
                longitude = Double.valueOf(location.getLongitude());
                altitude = Double.valueOf(location.getAltitude());
              //  degreeTV.setText(total + ":" + Double.toString(altitude));
//                System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
//                System.out.println(total + ":" + Double.toString(altitude));
//                            magneticDeclination = CompassHelper.calculateMagneticDeclination(latitude, longitude, altitude);
//                            textViewMagneticDeclination.setText(getString(R.string.magnetic_declination, magneticDeclination));
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_localization);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
//        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
//                    rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_D);

        sensorManager.registerListener(LocalizationActivity.this, rotationSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(LocalizationActivity.this, accSensor, SensorManager.SENSOR_DELAY_GAME);
//        sensorManager.registerListener(LocalizationActivity.this, magSensor, SensorManager.SENSOR_DELAY_NORMAL);
//        sensorManager.registerListener(LocalizationActivity.this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);

        degreeTV = (TextView) findViewById(R.id.DegreeTV);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(LocalizationActivity.this);
        //check if we have permission to access location
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            //fine location permission already granted
            getLocation();

        } else {
            //if permission is not granted, request location permissions from user
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSION_FINE_LOCATION);
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


        start = (Button) findViewById(R.id.buttonStart);
        end = (Button) findViewById(R.id.buttonEnd);
        textStep = (TextView) findViewById(R.id.textViewStep);
        textOrientation = (TextView) findViewById(R.id.textViewOrientation);
        textCell = (TextView) findViewById(R.id.textViewCell);


        layout = new Layout();
        getLayoutCanvas();

        step = 0;
        threshold = 0.18; // get from experiments
        stepDistance = (float) 0.736;
        textStep.setText("step:" + step);
        pc = null;
        refDirection = -53;

        sc = new StepCounter();
        if (pc == null) {
            Log.d("SET NULL", "pc set to null");
        } else {

        }

        locationRequest = locationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {

                    return;
                }
//                System.out.println("********");
                for (Location location : locationResult.getLocations()) {
//                    System.out.println("0000000000");
                    height = (float) location.getAltitude();
                    degreeTV.setText("Height: " +String.format(".%1f",height));

                }
            }
        };


        new CountDownTimer(3000000, 5000) {

            public void onTick(long millisUntilFinished) {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(LocalizationActivity.this);
                //  mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
//                System.out.println("bbbbbbbbbbbbb");
                getLocation();


            }

            public void onFinish() {
                //  mTextField.setText("done!");
            }
        }.start();


        // get step distance from Calibration Activity
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            //stepDistance = bundle.getFloat("stepDistance");
            refDirection = bundle.getFloat("refDirection");
            //threshold = bundle.getDouble("threshold");
            threshold = 0.38;

            for (String key : bundle.keySet()) {
                Log.d("Bundle Debug locaa", key + " = \"" + bundle.get(key) + "\"");
            }


            //       System.out.println("step distance is "+stepDistance);

        } else {
            System.out.println("No step!!!!");
        }

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pc == null) {
                    Log.d("pc is NULL", "pc is");
                } else {
                    Log.d("pc not NULL", "pc not");
                }


                if (pc == null) {
                    pc = new ParticleCollection(layout);
                    pc.initializeParticleSet();
                    pc.drawParticleCollection(canvas);
                } else {
                    Toast t = Toast.makeText(getApplicationContext(),
                            "You have already finished initialization",
                            Toast.LENGTH_LONG);

                    t.show();
                    Log.e(TAG, "here not null");

                }
            }
        });

        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                try {
//                    csvWriter.writeAll(data);
//                    csvWriter.close();
//                } catch (IOException e) {
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                sensorManager.unregisterListener(LocalizationActivity.this, rotationSensor);
                sensorManager.unregisterListener(LocalizationActivity.this, accSensor);

                Intent intentMain = new Intent(LocalizationActivity.this, MainActivity.class);
                startActivity(intentMain);
            }
        });


    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (pc != null) {
//            switch (event.sensor.getType()) {
//                case Sensor.TYPE_ACCELEROMETER:
//                    System.arraycopy(event.values, 0, accelerometerReading, 0, 3);
//                    break;
//                case Sensor.TYPE_MAGNETIC_FIELD:
//                    System.arraycopy(event.values, 0, magnetometerReading, 0, 3);
//                    break;
////                case Sensor.TYPE_ORIENTATION:
////                    System.arraycopy(event.values, 0, mOldOreintation, 0, 3);
////                    break;
//
//                default:
//                    return;
//            }

//            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
//                // This timestep's delta rotation to be multiplied by the current rotation
//                // after computing it from the gyro sample data.
//                System.out.println("!!!!!!");
//                if (timestamp != 0) {
//                    final float dT = (event.timestamp - timestamp) * NS2S;
//                    // Axis of the rotation sample, not normalized yet.
//                    float axisX = event.values[0];
//                    float axisY = event.values[1];
//                    float axisZ = event.values[2];
//
//                    // Calculate the angular speed of the sample
//                    float omegaMagnitude = (float) Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);
//
//                    // Normalize the rotation vector if it's big enough to get the axis
//                    // (that is, EPSILON should represent your maximum allowable margin of error)
//                    if (omegaMagnitude > EPSILON) {
//                        axisX /= omegaMagnitude;
//                        axisY /= omegaMagnitude;
//                        axisZ /= omegaMagnitude;
//                    }
//
//                    // Integrate around this axis with the angular speed by the timestep
//                    // in order to get a delta rotation from this sample over the timestep
//                    // We will convert this axis-angle representation of the delta rotation
//                    // into a quaternion before turning it into the rotation matrix.
//                    float thetaOverTwo = omegaMagnitude * dT / 2.0f;
//                    float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
//                    float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);
//                    deltaRotationVector[0] = sinThetaOverTwo * axisX;
//                    deltaRotationVector[1] = sinThetaOverTwo * axisY;
//                    deltaRotationVector[2] = sinThetaOverTwo * axisZ;
//                    deltaRotationVector[3] = cosThetaOverTwo;
//                }
//                timestamp = event.timestamp;
//                float[] deltaRotationMatrix = new float[9];
//                float[] gyroscopeOrientation = new float[3];
//                SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
//                // User code should concatenate the delta rotation we computed with the current rotation
//                // in order to get the updated rotation.
//                // rotationCurrent = rotationCurrent * deltaRotationMatrix;
//
//                currentRotationMatrix = matrixMultiplication(
//                        currentRotationMatrix,
//                        deltaRotationMatrix);
//
//                SensorManager.getOrientation(currentRotationMatrix,
//                        gyroscopeOrientation);
//
//                float direction = (float) Math.toDegrees(gyroscopeOrientation[0]);
//
//              //  textOrientation.setText(String.valueOf(direction)+String.valueOf(gyroscopeOrientation));
//            }


//                if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
////                float[] rotationMatrix = new float[9];
////                float[] mGravs = new float[3];
////                float[] mGeoMags = new float[3];
////                float[] orientationAngles = new float[3];
////                float[] accelerometerReading = new float[3];
//                boolean succeed = SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
//                if (succeed) {
//                    //   Log.d("succeed","succeed");
//                } else {
//                    Log.d("fail", "fail");
//                }
//
//                SensorManager.getOrientation(rotationMatrix, orientationAngles);
//                float direction = (float) Math.toDegrees(orientationAngles[0]);
//             //   textOrientation.setText(String.valueOf(direction));
//            }
//
//
//            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
//                boolean succeed = SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
//                SensorManager.getOrientation(rotationMatrix, orientationAngles);
//                float direction = (float) Math.toDegrees(orientationAngles[0]);
//                // double dir = (orientationAngles[0]*100) / 1.722 ;
//                //  Log.d("====","====");
//           //     textOrientation.setText(String.valueOf(direction) + " " + String.valueOf(calibratedDirection));
//            }

//            if(!executed && event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
//
//               // float[] orientations = new float[3];
//                SensorManager.getRotationMatrixFromVector(currentRotationMatrix, event.values);
//                executed = true;
//            }

            if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
                float[] rotationMatrix = new float[16];
                float[] orientations = new float[3];
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
                SensorManager.getOrientation(rotationMatrix, orientations);
                // we only need one orientation
                float orientation = orientations[0];
                float direction = (float) Math.toDegrees(orientation);
                getCalibratedDirection(direction);
                textOrientation.setText("Magnetic angle:\n"+String.format(".%1f",direction)+ "\n"+
                                    "Move: "+Float.toString(calibratedDirection));
            }

            // get accelerometer data
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                // we need to use a low pass filter to make data smoothed
                smoothed = lowPassFilter(event.values, gravity);
                gravity[0] = smoothed[0];
                gravity[1] = smoothed[1];
                gravity[2] = smoothed[2];

                float currentvectorSum = sc.getAccelRes(smoothed);

                if (currentvectorSum < 9.0 && inStep == false) {  //para
                    inStep = true;
                }
                if (currentvectorSum > 10.5 && inStep == true) {   //para
                    inStep = false;
                    step++;

                    // restart rotation sensor every 10 steps
                    if(step % 10 == 0){
                        reStartRotationSensor();
                    }
                    //Log.d("TAG_ACCELEROMETER", "\t" + numSteps);
                    textStep.setText("Step: " + step);
                    getLayoutCanvas();

                    pc.moveParticles(canvas, (int) Cell.mapMeterToPixel(stepDistance), calibratedDirection,height);  //ATTENTION: to be changed
                    int predictCell = pc.getCellwithMaxWeight();
                    textCell.setText("cell " + String.valueOf(predictCell));
                }


            }

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     *  get canvas with initial layout and without particles
     */

    private void getLayoutCanvas() {
        ImageView canvasView = (ImageView) findViewById(R.id.canvas);
        Bitmap blankBitmap = Bitmap.createBitmap(340, 200, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(blankBitmap);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        canvasView.setImageBitmap(blankBitmap);
        canvas.drawColor(Color.rgb(204,204,204));

        layout.drawLayout(canvas);
    }

    /**
     *   The output is only 0, 90, 180, 270
     */
    private void getCalibratedDirection(float direction) {

        // compensate the direction to let the ref direction have the biggest degree value
        // ref direction(forward): 0 degree
//         right: 90 degree
//         back: 180 degree
//         left: 270 degree

        if (direction < refDirection) {
            direction += 360;
        }

        if (direction > refDirection - tolerateWindow && direction < refDirection + tolerateWindow) {
            calibratedDirection = 0;
        } else if (direction > refDirection + 90 - tolerateWindow && direction < refDirection + 90 + tolerateWindow) {
            calibratedDirection = 90;
        } else if (direction > refDirection + 180 - tolerateWindow && direction < refDirection + 180 + tolerateWindow) {
            calibratedDirection = 180;
        } else if (direction > refDirection + 270 - tolerateWindow && direction < refDirection + 270 + tolerateWindow) {
            calibratedDirection = 270;
        }
        //return calibratedDirection;
    }

    protected float[] lowPassFilter(float[] input, float[] output) {
        if (output == null) return input;
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + 0.5f * (input[i] - output[i]);
            //output[i] = output[i] + 0.5f * (input[i] - output[i]); // my phone
        }
        return output;
    }


    @SuppressLint("MissingPermission")
    //suppress warning since we have already checked for permissions before calling the function
    private void getLocation() {
        fusedLocationClient.getCurrentLocation(100, null).addOnSuccessListener(altitudeUpdate);
    }

    public static float map180to360(float angle) {
        return (angle + 360) % 360;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_FINE_LOCATION) {
            //if request is cancelled, the result arrays are empty.4
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission is granted
                getLocation();
            } else {
                //display Toast with error message
                Toast.makeText(this, "R.string.location_error_msg", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (true) {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        System.out.println("))))))))");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    private float[] matrixMultiplication(float a[], float b[]){
        // transfer a b, to 2d array
        float a2d[][] = matrixTo2D(a);
        float b2d[][] = matrixTo2D(b);

        float[][] result = new float[3][3];

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                result[row][col] = multiplyMatricesCell(a2d, b2d, row, col);
            }
        }


        return matrixTo1D(result);

    }

    float multiplyMatricesCell(float[][] firstMatrix, float[][] secondMatrix, int row, int col) {
        float cell = 0;
        for (int i = 0; i < secondMatrix.length; i++) {
            cell += firstMatrix[row][i] * secondMatrix[i][col];
        }
        return cell;
    }

    private float[][] matrixTo2D(float array1d[]){
        float array2d[][] = new float [3][3];
        for(int i=0; i<3;i++) {
            for (int j = 0; j < 3; j++)
                array2d[i][j] = array1d[(j * 3) + i];
        }
        return array2d;

    }

    private float[] matrixTo1D(float array2d[][]){
        float array1d[] = new float [9];
        for(int i=0; i<3;i++) {
            for (int j = 0; j < 3; j++)
                array1d[(j * 3) + i] = array2d[i][j];
        }
        return array1d;

    }

    private void reStartRotationSensor(){
        sensorManager.unregisterListener(LocalizationActivity.this, rotationSensor);
        sensorManager.registerListener(LocalizationActivity.this, rotationSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }






}
