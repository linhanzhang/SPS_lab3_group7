package com.example.myapplication.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.map.Cell;
import com.example.myapplication.motion.StepCounter;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;


public class CalibrationActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private Sensor accSensor;
    private TextView textInstruction;
    private TextView textInstruction2;
    private TextView textInstruction3;
    private TextView textStep;
    private TextView textStep2;
    private TextView textOrientation;
    private TextView textViewThreshold;
    private Button startStep;
    private Button startCountingStep;
    private Button endStep;
    private Button endCountingStep;
    private Button startAngle;
    private Button endAngle;
    private Button exit;
    private Button buttonPlus1;
    private Button buttonMinus1;

    private int step;
    private float stepDistance=-1; // default value
    private float averageDirection=-1; // default value
    private List<Float> directionList;
   // private final float MAXSTEPLEN = (float) 0.6; // to be changed
    private SeekBar seek;
    private float threshold=3.5f;
    // Gravity for accelerometer data
    private float[] gravity = new float[3];
    // smoothed values
    private float[] smoothed = new float[3];

    private boolean inStep;
    /**
     *  2 x distance from the left most edge to the right most edge of the room
     */
    private final float distance = 26.5F;


    boolean startCountStep = false;
    boolean startMeasureAngle = false;

    private long lastdateOne;
    private int stepOnCountingType = 0; // 1 means step length calibration, 2 means threshold calibration

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(CalibrationActivity.this, rotationSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(CalibrationActivity.this, accSensor, SensorManager.SENSOR_DELAY_GAME);

        textInstruction = (TextView) findViewById(R.id.textViewInstruction);
        textInstruction2 = (TextView) findViewById(R.id.textViewInstruction2);
        textInstruction3 = (TextView) findViewById(R.id.textViewInstruction3);
        textStep = (TextView) findViewById(R.id.textViewStep);
        textStep2 = (TextView) findViewById(R.id.textViewStep2);
        textOrientation = (TextView) findViewById(R.id.textViewOrientation);
        textViewThreshold = (TextView) findViewById(R.id.textViewThreshold);

        startStep = (Button) findViewById(R.id.buttonStartCalibStep);
        startCountingStep = (Button) findViewById(R.id.buttonStartStep);
        endStep = (Button) findViewById(R.id.buttonEndCalibStep);
        endCountingStep = (Button) findViewById(R.id.buttonEndStep);
        startAngle = (Button) findViewById(R.id.buttonStartCalibAngle);
        endAngle = (Button) findViewById(R.id.buttonEndCalibAngle);
        buttonPlus1 = (Button) findViewById(R.id.buttonPlus1);
        buttonMinus1 = (Button) findViewById(R.id.buttonMinus1);


        exit = (Button) findViewById(R.id.buttonReturn);
      //  textInstruction.setText("Please stand at the left side of the room \n and click START button to start calibration");
        textStep.setText("step count:"+step);

        step = 0;
        directionList = new LinkedList<>();

        startStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                textStep.setText("initial step:"+step);
                startCountStep = true;
                stepOnCountingType = 1;
                textInstruction.setText("calibration starts, please walk to the other end of the room");

                Date date = new Date();
                lastdateOne = date.getTime();

            }
        });

        startCountingStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                textStep.setText("initial step:"+step);
                startCountStep = true;
                stepOnCountingType = 2;
                textInstruction3.setText("calibration starts, please walk and adjust the threshold");

                Date date = new Date();
                lastdateOne = date.getTime();

            }
        });



        endStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stepDistance = distance/step;
                step = 0;
                stepOnCountingType = 0;
                startCountStep = false;
                textInstruction.setText("calibration ends, your average step distance is \n"+ stepDistance+" m");
            }
        });

        endCountingStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                step = 0;
                stepOnCountingType = 0;
                startCountStep = false;
                textInstruction3.setText("the threshold you choose is"+threshold);
            }
        });

        startAngle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                directionList = new LinkedList<>();
                startMeasureAngle = true;
                textInstruction2.setText("calibration of angle starts, please hold your phone stil");
            }
        });

        endAngle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float mean = 0;
                for(int i=0; i<directionList.size();i++){
                    mean += directionList.get(i);
                }
                mean/=directionList.size();

                // overwrite the old average direction value
                averageDirection = mean;
                startMeasureAngle = false;
                textInstruction2.setText("calibration ends, ref direction is \n"+ averageDirection);
            }
        });

        buttonPlus1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                threshold+=0.1;
                textViewThreshold.setText("threshold:  "+threshold);
            }
        });

        buttonMinus1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                threshold-=0.1;
                textViewThreshold.setText("threshold: "+threshold);
            }
        });


        /**
         *  Go back to main menu and send the calibrated value to main activity (if not calibrated then send default value)
         */

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sensorManager.unregisterListener(CalibrationActivity.this, accSensor);
                sensorManager.unregisterListener(CalibrationActivity.this, rotationSensor);
                //Intent intentMain = new Intent(CalibrationActivity.this, MainActivity.class);
                Intent resultIntent = new Intent();

                if(stepDistance!=-1) {
                    resultIntent.putExtra("stepDistance", stepDistance);
                }
                if(averageDirection!=-1) {
                    resultIntent.putExtra("refDirection", averageDirection);
                }
                resultIntent.putExtra("threshold", threshold);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();

                System.out.println("calibrated step distance is "+stepDistance);

//                startActivity(intentMain);

            }

        });
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        System.out.println("destroyed!!!!!");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(startMeasureAngle &&  event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
            float[] rotationMatrix = new float[16];
            float[] orientations = new float[3];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            SensorManager.getOrientation(rotationMatrix, orientations);
            // we only need one orientation
            float orientation = orientations[0];
            float direction = (float) Math.toDegrees(orientation);
            directionList.add(direction);
            textOrientation.setText(Float.toString(direction));
        }

        // get accelerometer data
        if ( startCountStep && event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // we need to use a low pass filter to make data smoothed
            smoothed = lowPassFilter(event.values, gravity);
            gravity[0] = smoothed[0];
            gravity[1] = smoothed[1];
            gravity[2] = smoothed[2];

            float currentvectorSum = StepCounter.getAccelRes(smoothed);

            Date date = new Date();

            System.out.println(currentvectorSum);
            if (currentvectorSum > threshold && date.getTime() - lastdateOne > 600) {   //para 10.5 //para= 4

                step++;
                switch (stepOnCountingType){
                    case 1:
                        textStep.setText("Step: " + step);
                        break;
                    case 2:
                        textStep2.setText("Step: " + step);
                        break;
                    default:
                }


                lastdateOne = date.getTime();
            }

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    protected float[] lowPassFilter( float[] input, float[] output ) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + 0.5f * (input[i] - output[i]);
        }
        return output;
    }

}