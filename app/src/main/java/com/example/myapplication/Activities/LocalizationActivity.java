package com.example.myapplication.Activities;

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
import com.example.myapplication.particles.Particle;
import com.example.myapplication.particles.ParticleCollection;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocalizationActivity extends AppCompatActivity implements SensorEventListener {
    private FusedLocationProviderClient fusedLocationClient;
    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private Sensor stepCounter;
    private Sensor accSensor;
    private Button start;
    private Button end;
    private Button stepAdd;
    private TextView textStep;
    private TextView textOrientation;
    private final String TAG = "DEBUG";
    public int step;
    private Layout layout;
    private Canvas canvas;
    private ParticleCollection pc;
    private float stepDistance;
    private float refDirection;
    private final float tolerateWindow = 45;
    private int currentDirection;
    // Gravity for accelerometer data
    private float[] gravity = new float[3];
    // smoothed values
    private float[] smoothed = new float[3];
    // sensor manager
    private boolean ignore;
    private int countdown;
    private double prevY;
    private double threshold = 0;
    private SeekBar seek;

    private TextView degreeTV;
    private Boolean isLocationRetrieved = true;
    private Double latitude ;
    private Double longitude ;
    private Double altitude;
    private static final int REQUEST_PERMISSION_FINE_LOCATION =1;
//    private SensorEventListener compassListener = new SensorEventListener(){
//        @Override
//        public void onAccuracyChanged(Sensor sensor, int accuracy) {
//            // not in use;
//        }
//        @Override
//        public void onSensorChanged(SensorEvent event){
//            // get angle around the z-axis rotated
//            float degree = Math.round(event.values[0]);
//            DegreeTV.setText("Heading: " + Float.toString(degree) + " degrees");
//            // rotation animation - reverse turn degree degrees
////            RotateAnimation ra = new RotateAnimation(
////                    DegreeStart,
////                    -degree,
////                    Animation.RELATIVE_TO_SELF, 0.5f,
////                    Animation.RELATIVE_TO_SELF, 0.5f);
////            // set the compass animation after the end of the reservation status
////            ra.setFillAfter(true);
////            // set how long the animation for the compass image will take place
////            ra.setDuration(210);
////            // Start animation of compass image
////            compassimage.startAnimation(ra);
////            DegreeStart = -degree;
//        }
//
//    };

    OnSuccessListener<Location> altitudeUpdate = new OnSuccessListener<Location>(){
        @Override
        public void onSuccess(Location location) {
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                isLocationRetrieved = true;
                latitude = Double.valueOf(location.getLatitude());
                longitude = Double.valueOf(location.getLongitude());
                altitude = Double.valueOf(location.getAltitude());
                degreeTV.setText(Double.toString(altitude));
                System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
                System.out.println(Double.toString(altitude));
//                            magneticDeclination = CompassHelper.calculateMagneticDeclination(latitude, longitude, altitude);
//                            textViewMagneticDeclination.setText(getString(R.string.magnetic_declination, magneticDeclination));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localization);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        sensorManager.registerListener(compassListener, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
//                SensorManager.SENSOR_DELAY_GAME);

        degreeTV = (TextView) findViewById(R.id.DegreeTV );
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
        stepAdd = (Button) findViewById(R.id.buttonStepAdd);
        textStep = (TextView)findViewById(R.id.textViewStep);
        textOrientation = (TextView)findViewById(R.id.textViewOrientation);
        seek = (SeekBar) findViewById(R.id.seek);

        layout = new Layout();
        getLayoutCanvas();

        step = 0;
        textStep.setText("step:"+step);
        pc = null;
        if(pc == null){
            Log.d("SET NULL","pc set to null");
        }
        else{

        }


        // get step distance from Calibration Activity
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            stepDistance = bundle.getFloat("stepDistance");
            refDirection = bundle.getFloat("refDirection");

            for (String key : bundle.keySet())
            {
                Log.d("Bundle Debug locaa", key + " = \"" + bundle.get(key) + "\"");
            }


            System.out.println("step distance is "+stepDistance);

        }
        else{
            System.out.println("No step!!!!");
        }

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pc == null){
                    Log.d("pc is NULL","pc is");
                }
                else{
                    Log.d("pc not NULL","pc not");
                }

//                Particle p = new Particle(0,0,1);
//                p.drawParticle(canvas);
//
//                Particle p1 = new Particle(132,42,5);
//                p1.drawParticle(canvas);

                if(pc == null) {
                    pc = new ParticleCollection(layout);
                    pc.initializeParticleSet();
                    pc.drawParticleCollection(canvas);
                    Log.e(TAG, "here");

                    stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
                    rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
                    accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//                    rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_D);

                    sensorManager.registerListener(LocalizationActivity.this, stepCounter, SensorManager.SENSOR_DELAY_GAME);
                    sensorManager.registerListener(LocalizationActivity.this, rotationSensor, SensorManager.SENSOR_DELAY_GAME);
                    sensorManager.registerListener(LocalizationActivity.this, accSensor, SensorManager.SENSOR_DELAY_GAME);

                    Log.e(TAG, "there");
                    //canvas.drawColor(Color.TRANSPARENT);
                }
                else{
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

                Log.e(TAG, "seeee");
                Log.d("Locate debug", "I am locate");
                sensorManager.unregisterListener(LocalizationActivity.this,stepCounter);
                sensorManager.unregisterListener(LocalizationActivity.this,rotationSensor);
                sensorManager.unregisterListener(LocalizationActivity.this, accSensor);

                Intent intentMain=new Intent(LocalizationActivity.this, MainActivity.class);
                startActivity(intentMain);
            }
        });

        stepAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                step++;
                textStep.setText("step:"+step);

                getLayoutCanvas();

                pc.moveParticles(canvas, (int)Cell.mapMeterToPixel(stepDistance), currentDirection);  //ATTENTION: to be changed


            }
        });

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                threshold = ((double)seek.getProgress()) * 0.02;
                //thresholdView.setText("Threshold: "+ threshold);
            }
        });
        //degreeTV.setText(Double.toString(altitude));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(pc != null) {

//            if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
//
//                Toast t = Toast.makeText(getApplicationContext(),
//                        "This is a message displayed in a Toast",
//                        Toast.LENGTH_SHORT);
//
//                t.show();
//                step++;
//                textStep.setText("step:" + step);
//                if(step>=2) {   // to be changed. should remove if condition
//                    getLayoutCanvas();
//                    Log.d("step>2","step>2");
//                    //canvas.drawColor(Color.WHITE);
//                    //layout.drawLayout(canvas);
//                    getLayoutCanvas();
//
//                    pc.moveParticles(canvas, (int)Cell.mapMeterToPixel(stepDistance), currentDirection);  //ATTENTION: to be changed
//                    //pc.drawParticleCollection(canvas);
//                }
//////
//
//            }

            if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
                float[] rotationMatrix = new float[16];
                float[] orientations = new float[3];
                sensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
                sensorManager.getOrientation(rotationMatrix, orientations);
                // we only need one orientation
                float orientation = orientations[0];
                float direction = (float) Math.toDegrees(orientation);
                direction = this.map180to360(direction);
                currentDirection = getCalibratedDirection(direction);
                textOrientation.setText(String.valueOf(currentDirection)+" "+direction);
            }

            // get accelerometer data
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                // we need to use a low pass filter to make data smoothed
                smoothed = lowPassFilter(event.values, gravity);
                gravity[0] = smoothed[0];
                gravity[1] = smoothed[1];
                gravity[2] = smoothed[2];
                //acc.setText("x: "+gravity[0] + " y: " + gravity[1] + " z: " + gravity[2]+ "ignore: "+ ignore + "countdown: "+ countdown);
                if(ignore) {
                    countdown--;
                    ignore = (countdown < 0)? false : ignore;
                }
                else
                    countdown = 22;
                if((Math.abs(prevY - gravity[1]) > threshold) && !ignore){
                    step++;
                    textStep.setText("Step: " + step);
                    ignore = true;

                    getLayoutCanvas();
                    Log.d("step>2","step>2");
                    //canvas.drawColor(Color.WHITE);
                    //layout.drawLayout(canvas);
                    getLayoutCanvas();

                    pc.moveParticles(canvas, (int)Cell.mapMeterToPixel(stepDistance), currentDirection);  //ATTENTION: to be changed
                    //pc.drawParticleCollection(canvas);
                }
                prevY = gravity[1];
            }

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     *  get canvas with initial layout and without particles
     */

    private void getLayoutCanvas(){
        ImageView canvasView = (ImageView) findViewById(R.id.canvas);
        Bitmap blankBitmap = Bitmap.createBitmap(300, 200, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(blankBitmap);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        canvasView.setImageBitmap(blankBitmap);

        layout.drawLayout(canvas);
    }

    /**
     *   The output is only 0, 90, 180, 270
     */
    private int getCalibratedDirection(float direction){

        // compensate the direction to let the ref direction have the biggest degree value
        // ref direction(forward): 0 degree
//         right: 90 degree
//         back: 180 degree
//         left: 270 degree
        int calibratedDirection = 0;
        if(direction<refDirection){
            direction+=360;
        }

        if(direction>refDirection-tolerateWindow && direction<refDirection+tolerateWindow){
            calibratedDirection = 0;
        }
        else if(direction>refDirection+90-tolerateWindow && direction<refDirection+90+tolerateWindow){
            calibratedDirection = 90;
        }
        else if(direction>refDirection+180-tolerateWindow && direction<refDirection+180+tolerateWindow){
            calibratedDirection = 180;
        }
        else if(direction>refDirection+270-tolerateWindow && direction<refDirection+270+tolerateWindow){
            calibratedDirection =270;
        }
        return calibratedDirection;
    }

    protected float[] lowPassFilter( float[] input, float[] output ) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + 1.0f * (input[i] - output[i]);
        }
        return output;
    }

    @SuppressLint("MissingPermission") //suppress warning since we have already checked for permissions before calling the function
    private void getLocation() {
        fusedLocationClient.getCurrentLocation(100,null).addOnSuccessListener(altitudeUpdate);
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
}
