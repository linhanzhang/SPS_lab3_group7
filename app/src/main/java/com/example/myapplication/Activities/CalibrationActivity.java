package com.example.myapplication.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
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

import java.util.LinkedList;
import java.util.List;


public class CalibrationActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private Sensor accSensor;
    private TextView textInstruction;
    private TextView textStep;
    private TextView textOrientation;
    private TextView textThreshold;
    private Button start;
    private Button end;
    private Button exit;
    private int step;
    private float stepDistance;
    private float averageDirection;
    private List<Float> directionList;
    private final float MAXSTEPLEN = (float) 0.736; // to be changed
    private SeekBar seek;
    private double threshold;
    // Gravity for accelerometer data
    private float[] gravity = new float[3];
    // smoothed values
    private float[] smoothed = new float[3];
    // sensor manager
    private boolean ignore;
    private int countdown;
    private double prevY;
    /**
     *  2 x distance from the left most edge to the right most edge of the room
     */
    private final float distance = 53;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(CalibrationActivity.this, rotationSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(CalibrationActivity.this, accSensor, SensorManager.SENSOR_DELAY_GAME);

        textInstruction = (TextView) findViewById(R.id.textViewInstruction);
        textStep = (TextView) findViewById(R.id.textViewStep);
        textOrientation = (TextView) findViewById(R.id.textViewOrientation);
        textThreshold = (TextView) findViewById(R.id.textViewThreshold);
        start = (Button) findViewById(R.id.buttonStart);
        end = (Button) findViewById(R.id.buttonEnd);
        exit = (Button) findViewById(R.id.buttonReturn);
      //  textInstruction.setText("Please stand at the left side of the room \n and click START button to start calibration");
        textStep.setText("step count:"+step);
        textOrientation = (TextView)findViewById(R.id.textViewOrientation);

        seek = (SeekBar) findViewById(R.id.seek);
        step = 0;
        seek.setProgress(19);
        directionList = new LinkedList<>();



        Log.d("Calib debug", "I am calib");


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                directionList = new LinkedList<>();

                textStep.setText("initial step:"+step);


                //textInstruction.setText("calibration starts, please walk to the other end of the room \n and walk back to the start point");
            }
        });

        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stepDistance = distance/step;
               // textInstruction.setText("calibration ends, your average step distance is \n"+ stepDistance+" m");
            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(stepDistance == 0){
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "you haven't finished calibration, please do calibration first",
                            Toast.LENGTH_LONG);

                    toast.show();
                }
                else {
                    float mean = 0;

                    for(int i=0; i<directionList.size();i++){
                        mean += directionList.get(i);
                    }
                    mean/=directionList.size();

                    if(stepDistance> MAXSTEPLEN){
                        stepDistance = MAXSTEPLEN;
                    }

                    sensorManager.unregisterListener(CalibrationActivity.this, accSensor);
                    sensorManager.unregisterListener(CalibrationActivity.this, rotationSensor);
                    Intent intentMain = new Intent(CalibrationActivity.this, MainActivity.class);

                    intentMain.putExtra("stepDistance", stepDistance);
                    intentMain.putExtra("refDirection", mean);
                    intentMain.putExtra("threshold", threshold);

                    startActivity(intentMain);

                }
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
                //textOrientation.setText(String.valueOf(threshold));
                textThreshold.setText("Threshold: "+ threshold);
            }
        });

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
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
                countdown = 10; //
            //countdown = 10; // my phone
            if((Math.abs(prevY - gravity[1]) > threshold) && !ignore){
                step++;
                textStep.setText("Step: " + step );
                ignore = true;
            }
            prevY = gravity[1];
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    protected float[] lowPassFilter( float[] input, float[] output ) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + 0.5f * (input[i] - output[i]);
            //output[i] = output[i] + 0.5f * (input[i] - output[i]); // my phone
        }
        return output;
    }

}