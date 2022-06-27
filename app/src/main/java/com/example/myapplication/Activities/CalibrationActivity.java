package com.example.myapplication.Activities;

import androidx.appcompat.app.AppCompatActivity;

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

import java.util.LinkedList;
import java.util.List;


public class CalibrationActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private Sensor accSensor;
    private TextView textInstruction;
    private TextView textInstruction2;
    private TextView textStep;
    private TextView textOrientation;
    private TextView textThreshold;
    private Button startStep;
    private Button endStep;
    private Button startAngle;
    private Button endAngle;
    private Button exit;
    private int step;
    private float stepDistance = (float) 0.6; // default value
    private float averageDirection = -110; // default value
    private List<Float> directionList;
    private final float MAXSTEPLEN = (float) 0.6; // to be changed
    private SeekBar seek;
    private double threshold;
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

    private int countdown = 0;

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
        textStep = (TextView) findViewById(R.id.textViewStep);

        textOrientation = (TextView) findViewById(R.id.textViewOrientation);

        startStep = (Button) findViewById(R.id.buttonStartCalibStep);
        endStep = (Button) findViewById(R.id.buttonEndCalibStep);
        startAngle = (Button) findViewById(R.id.buttonStartCalibAngle);
        endAngle = (Button) findViewById(R.id.buttonEndCalibAngle);
        exit = (Button) findViewById(R.id.buttonReturn);
      //  textInstruction.setText("Please stand at the left side of the room \n and click START button to start calibration");
        textStep.setText("step count:"+step);

        step = 0;
        directionList = new LinkedList<>();

        startStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                directionList = new LinkedList<>();
                textStep.setText("initial step:"+step);
                startCountStep = true;
                textInstruction.setText("calibration starts, please walk to the other end of the room");
            }
        });

        endStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stepDistance = distance/step;
                step = 0;
                startCountStep = false;
                textInstruction.setText("calibration ends, your average step distance is \n"+ stepDistance+" m");
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

        /**
         *  Go back to main menu and send the calibrated value to main activity (if not calibrated then send default value)
         */

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sensorManager.unregisterListener(CalibrationActivity.this, accSensor);
                sensorManager.unregisterListener(CalibrationActivity.this, rotationSensor);
                Intent intentMain = new Intent(CalibrationActivity.this, MainActivity.class);

                intentMain.putExtra("stepDistance", stepDistance);
                intentMain.putExtra("refDirection", averageDirection);

                System.out.println("calibrated step distance is "+stepDistance);

                startActivity(intentMain);

            }

        });
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

            countdown ++;
            float currentvectorSum = StepCounter.getAccelRes(smoothed);

            if (currentvectorSum < 9.0 && inStep == false) {  //para
                inStep = true;
            }
            if (currentvectorSum > 10.5 && inStep == true && countdown >50) {   //para
                countdown = 0;
                inStep = false;
                step++;

                //Log.d("TAG_ACCELEROMETER", "\t" + numSteps);
                textStep.setText("Step: " + step);
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