package com.example.myapplication.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.map.Cell;
import com.example.myapplication.map.Layout;
import com.example.myapplication.motion.StepCounter;
import com.example.myapplication.particles.Particle;
import com.example.myapplication.particles.ParticleCollection;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class LocalizationActivity extends AppCompatActivity implements SensorEventListener {

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
    private final float tolerateWindow = 40;
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



    CSVWriter csvWriter = null;
    String csv = "/data/data/com.example.myapplication/cache/walk_sqrt.csv";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_localization);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
//                    rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_D);

        sensorManager.registerListener(LocalizationActivity.this, stepCounter, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(LocalizationActivity.this, rotationSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(LocalizationActivity.this, accSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(LocalizationActivity.this, magSensor, SensorManager.SENSOR_DELAY_NORMAL);

        start = (Button) findViewById(R.id.buttonStart);
        end = (Button) findViewById(R.id.buttonEnd);
        stepAdd = (Button) findViewById(R.id.buttonStepAdd);
        textStep = (TextView)findViewById(R.id.textViewStep);
        textOrientation = (TextView)findViewById(R.id.textViewOrientation);
        textCell = (TextView)findViewById(R.id.textViewCell);


        layout = new Layout();
        getLayoutCanvas();

        step = 0;
        threshold = 0.18; // get from experiments
        stepDistance = (float) 0.736;
        textStep.setText("step:"+step);
        pc = null;
        refDirection = -53;

        sc = new StepCounter();
        if(pc == null){
            Log.d("SET NULL","pc set to null");
        }
        else{

        }

        try {
            csvWriter = new CSVWriter(new FileWriter(csv));
        } catch (IOException e) {
            e.printStackTrace();
        }


        // get step distance from Calibration Activity
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            //stepDistance = bundle.getFloat("stepDistance");
            refDirection = bundle.getFloat("refDirection");
            //threshold = bundle.getDouble("threshold");
            threshold = 0.38;

            for (String key : bundle.keySet())
            {
                Log.d("Bundle Debug locaa", key + " = \"" + bundle.get(key) + "\"");
            }


     //       System.out.println("step distance is "+stepDistance);

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


                if(pc == null) {
                    pc = new ParticleCollection(layout);
                    pc.initializeParticleSet();
                    pc.drawParticleCollection(canvas);
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
                try {
                    csvWriter.writeAll(data);
                    csvWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

//                step++;
//                textStep.setText("step:"+step+"\n\t"
//                                + String.valueOf(threshold));
//
//                getLayoutCanvas();
//
//                pc.moveParticles(canvas, (int)Cell.mapMeterToPixel(stepDistance), calibratedDirection);  //ATTENTION: to be changed

               calibratedDirection = (calibratedDirection+90)%360;
              //  textOrientation.setText(String.valueOf(calibratedDirection));

            }
        });

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(pc != null) {

//            if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
//                float[] rotationMatrix = new float[16];
//                float[] orientations = new float[3];
//                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
//                SensorManager.getOrientation(rotationMatrix, orientations);
//                // we only need one orientation
//                float orientation = orientations[0];
//                float direction = (float) Math.toDegrees(orientation);
//                getCalibratedDirection(direction);
//                textOrientation.setText(String.valueOf(calibratedDirection)+" "+direction);
//            }
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    System.arraycopy(event.values, 0, accelerometerReading, 0, 3);
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    System.arraycopy(event.values, 0, magnetometerReading, 0, 3);
                    break;
//                case Sensor.TYPE_ORIENTATION:
//                    System.arraycopy(event.values, 0, mOldOreintation, 0, 3);
//                    break;

                default:
                    return;
            }

            if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
//                float[] rotationMatrix = new float[9];
//                float[] mGravs = new float[3];
//                float[] mGeoMags = new float[3];
//                float[] orientationAngles = new float[3];
//                float[] accelerometerReading = new float[3];
                boolean succeed = SensorManager.getRotationMatrix(rotationMatrix,null,accelerometerReading,magnetometerReading);
                if(succeed){
                    Log.d("succeed","succeed");
                }
                else{
                    Log.d("fail","fail");
                }

                SensorManager.getOrientation(rotationMatrix, orientationAngles);
                float direction = (float) Math.toDegrees(orientationAngles[0]);
                // double dir = (orientationAngles[0]*100) / 1.722 ;
//                Log.d(,"")
                Log.d("====","====");
                textOrientation.setText(String.valueOf(direction));
            }



            if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
//                float[] rotationMatrix = new float[9];
//                float[] mGravs = new float[3];
//                float[] mGeoMags = new float[3];
//                float[] orientationAngles = new float[3];
//                float[] accelerometerReading = new float[3];
                boolean succeed = SensorManager.getRotationMatrix(rotationMatrix,null,accelerometerReading,magnetometerReading);
                if(succeed){
                    Log.d("succeed","succeed");
                }
                else{
                    Log.d("fail","fail");
                }
                SensorManager.getOrientation(rotationMatrix, orientationAngles);
                float direction = (float) Math.toDegrees(orientationAngles[0]);
               // double dir = (orientationAngles[0]*100) / 1.722 ;
                Log.d("====","====");
                textOrientation.setText(String.valueOf(direction)+" "+String.valueOf(calibratedDirection));
            }

            // get accelerometer data
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                // we need to use a low pass filter to make data smoothed
                smoothed = lowPassFilter(event.values, gravity);
                gravity[0] = smoothed[0];
                gravity[1] = smoothed[1];
                gravity[2] = smoothed[2];

                float currentvectorSum = sc.getAccelRes(smoothed);

              //  data.add(new String[]{String.valueOf(currentvectorSum)});
               // csvWriter.writeNext(new String[]{String.valueOf(currentvectorSum)});
               // acc.setText("x: "+gravity[0] + " y: " + gravity[1] + " z: " + gravity[2]+ "ignore: "+ ignore + "countdown: "+ countdown);
//                if(ignore) {
//                    countdown--;
//                    ignore = (countdown < 0)? false : ignore;
//                }
//                else
//                    countdown = 10; //
//                    //countdown = 10; // my phone
//                if((Math.abs(prevY - gravity[1]) > threshold) && !ignore){
//                    step++;
//                    textStep.setText("Step: " + step);
//                    ignore = true;
//
//                    getLayoutCanvas();
//                    getLayoutCanvas();
//
//                    pc.moveParticles(canvas, (int)Cell.mapMeterToPixel(stepDistance), calibratedDirection);  //ATTENTION: to be changed
//                    //int predictCell = pc.getCellwithMaxWeight();
//                   // textCell.setText("cell "+String.valueOf(predictCell));
//                }
////                prevY = gravity[1];
//                boolean stepDetected = sc.isStepDetected(event.values.clone());
//                if(stepDetected){
//                    step++;
//                    textStep.setText("Step: " + step);
//                }

              //  textStep.setText(String.valueOf(currentvectorSum));

                if(currentvectorSum < 9.0 && inStep==false){  //para
                    inStep = true;
                }
                if(currentvectorSum > 10.5 && inStep==true){   //para
                    inStep = false;
                    step++;
                    //Log.d("TAG_ACCELEROMETER", "\t" + numSteps);
                    textStep.setText("Step: " + step);
                    getLayoutCanvas();

                    pc.moveParticles(canvas, (int)Cell.mapMeterToPixel(stepDistance), calibratedDirection);  //ATTENTION: to be changed
                    int predictCell = pc.getCellwithMaxWeight();
                    textCell.setText("cell "+String.valueOf(predictCell));

                    //textStep.setText("Step: " + step);
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
    private void getCalibratedDirection(float direction){

        // compensate the direction to let the ref direction have the biggest degree value
        // ref direction(forward): 0 degree
//         right: 90 degree
//         back: 180 degree
//         left: 270 degree

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
        //return calibratedDirection;
    }

    protected float[] lowPassFilter( float[] input, float[] output ) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + 0.5f * (input[i] - output[i]);
            //output[i] = output[i] + 0.5f * (input[i] - output[i]); // my phone
        }
        return output;
    }

//    protected float[] kalmanFilter( float [] input, float[] output){
//
//    }






}
