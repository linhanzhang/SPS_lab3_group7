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
import java.util.Date;
import java.util.Calendar;

public class LocalizationActivity extends AppCompatActivity implements SensorEventListener {
    private FusedLocationProviderClient fusedLocationClient;
    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private Sensor accSensor;
    private Button start;
    private Button end;
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
    private final float tolerateWindow = 45;
    // Gravity for accelerometer data
    private float[] gravity = new float[3];
    // smoothed values
    private float[] smoothed = new float[3];

    private int calibratedDirection = 0;

    private StepCounter sc;

    private boolean inStep;

    private TextView degreeTV;
    private Boolean isLocationRetrieved = true;

    private static final int REQUEST_PERMISSION_FINE_LOCATION = 1;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    OnSuccessListener<Location> altitudeUpdate = null;

    // Create a constant to convert nanoseconds to seconds.
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;
    float orientations[] = new float[3];
    boolean executed = false;
    float[] currentRotationMatrix = new float[16];

    public float height;

    private int counter = 0;
    private int countdown = 0;
    private Calendar c1;
    private long lastdateOne;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_localization);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(LocalizationActivity.this, rotationSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(LocalizationActivity.this, accSensor, SensorManager.SENSOR_DELAY_GAME);

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

        /**
         *  Default value
         */
        stepDistance = (float) 0.6;
        refDirection = -110;

        pc = null;
        sc = new StepCounter();

//        // creating a Calendar object
//        c1 = Calendar.getInstance();
//        // set Month
//        // MONTH starts with 0 i.e. ( 0 - Jan)
//        c1.set(Calendar.MONTH, 6);
//
//        // set Date
//        c1.set(Calendar.DATE, 24);
//
//        // set Year
//        c1.set(Calendar.YEAR, 2022);

        locationRequest = locationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    System.out.println("location result is empty");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    height = (float) location.getAltitude();
                    degreeTV.setText("Height: " +String.format(".%1f",height));

                }
            }
        };

        OnSuccessListener<Location> altitudeUpdate = new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    isLocationRetrieved = true;
                }
            }
        };


        new CountDownTimer(3000000, 5000) {

            public void onTick(long millisUntilFinished) {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(LocalizationActivity.this);
                getLocation();


            }

            public void onFinish() {
            }
        }.start();


        // get step distance from Calibration Activity
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            stepDistance = bundle.getFloat("stepDistance");
            refDirection = bundle.getFloat("refDirection");

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

                Date date = new Date();
                lastdateOne = date.getTime();
            }
        });

        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

                countdown ++;

                float currentvectorSum = sc.getAccelRes(smoothed);

//                if (currentvectorSum < 8.5 && inStep == false) {  //para 9.0
//                    inStep = true;
//                }
//                if (currentvectorSum > 0.6 && inStep == true && countdown >50) {   //para 10.5
//                    inStep = false;
//                    step++;
//                    countdown = 0;

                // creating a Calendar object
//                c1 = Calendar.getInstance();
//                // set Month
//                // MONTH starts with 0 i.e. ( 0 - Jan)
//                c1.set(Calendar.MONTH, 6);
//
//                // set Date
//                c1.set(Calendar.DATE, 24);
//
//                // set Year
//                c1.set(Calendar.YEAR, 2022);

                Date date = new Date();

//                System.out.println(date.getTime());
                if (currentvectorSum > 3.5 && date.getTime() - lastdateOne > 600) {   //para 10.5 //para= 4
                    inStep = false;
                    step++;
                    lastdateOne = date.getTime();
                    System.out.println(lastdateOne);

//                    if(c1.getTime().getTime() - lastdateOne > 500){
//
//                    }
                  //  countdown = 0;

                    // restart rotation sensor every 10 steps
                    if(step % 10 == 0){
                        reStartRotationSensor();
                    }

                    textStep.setText("Step: " + step);
                    getLayoutCanvas();

                    pc.moveParticles(canvas, stepDistance, calibratedDirection,height);  //ATTENTION: to be changed
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
        ImageView canvasView = (ImageView) findViewById(R.id.canvas); // w 340 h 200
        Bitmap blankBitmap = Bitmap.createBitmap(360, 200, Bitmap.Config.ARGB_8888);
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
       // calibratedDirection = (int) (direction-refDirection);

        if (direction > refDirection + 360 - tolerateWindow || direction < refDirection + tolerateWindow) {
            calibratedDirection = 0;
        } else if (direction > refDirection + 90 - tolerateWindow && direction < refDirection + 90 + tolerateWindow) {
            calibratedDirection = 90;
        } else if (direction > refDirection + 180 - tolerateWindow && direction < refDirection + 180 + tolerateWindow) {
            calibratedDirection = 180;
        } else if (direction > refDirection + 270 - tolerateWindow && direction < refDirection + 270 + tolerateWindow) {
            calibratedDirection = 270;
        }
        //return calibratedDirection;
        counter ++;
        if(counter == 100) {
            System.out.println("direction: " + direction + "ref: " + refDirection + "calibrated: " + calibratedDirection);
            counter = 0;
        }
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
        System.out.println("start location updates");
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
