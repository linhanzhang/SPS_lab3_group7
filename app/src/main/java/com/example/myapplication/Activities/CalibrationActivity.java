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
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;

import java.util.LinkedList;
import java.util.List;


public class CalibrationActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private Sensor stepCounter;
    private TextView textInstruction;
    private TextView textStep;
    private TextView textOrientation;
    private Button start;
    private Button end;
    private Button exit;
    private int step;
    private float stepDistance;
    private float averageDirection;
    private List<Float> directionList;
    private final float MAXSTEPLEN = (float) 0.7; // to be changed
    /**
     *  2 x distance from the left most edge to the right most edge of the room
     */
    private final float distance = 53;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        textInstruction = (TextView) findViewById(R.id.textViewInstruction);
        textStep = (TextView) findViewById(R.id.textViewStep);
        textOrientation = (TextView) findViewById(R.id.textViewOrientation);
        start = (Button) findViewById(R.id.buttonStart);
        end = (Button) findViewById(R.id.buttonEnd);
        exit = (Button) findViewById(R.id.buttonReturn);
        textInstruction.setText("Please stand at the left side of the room \n and click START button to start calibration");
        textStep.setText("step count:"+step);
        textOrientation = (TextView)findViewById(R.id.textViewOrientation);
        step = 0;

        Log.d("Calib debug", "I am calib");


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                directionList = new LinkedList<>();
                sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
                rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

                textStep.setText("initial step:"+step);

                sensorManager.registerListener(CalibrationActivity.this, stepCounter, SensorManager.SENSOR_DELAY_GAME);
                sensorManager.registerListener(CalibrationActivity.this, rotationSensor, SensorManager.SENSOR_DELAY_GAME);

                textInstruction.setText("calibration starts, please walk to the other end of the room \n and walk back to the start point");
            }
        });

        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stepDistance = distance/step;
                textInstruction.setText("calibration ends, your average step distance is \n"+ stepDistance+" m");
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

                    sensorManager.unregisterListener(CalibrationActivity.this, stepCounter);
                    sensorManager.unregisterListener(CalibrationActivity.this, rotationSensor);
                    Intent intentMain = new Intent(CalibrationActivity.this, MainActivity.class);

                    intentMain.putExtra("stepDistance", stepDistance);
                    intentMain.putExtra("refDirection", mean);

                    startActivity(intentMain);

                }
            }
        });

    }

    @Override
    public void onSensorChanged(SensorEvent event) {


        if(event.sensor.getType() == Sensor.TYPE_STEP_COUNTER){

            step ++;
            textStep.setText("step count:"+step);
        }

        if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
            float[] rotationMatrix = new float[16];
            float[] orientations = new float[3];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            SensorManager.getOrientation(rotationMatrix, orientations);
            // we only need one orientation
            float orientation = orientations[0];
            float direction = (float) Math.toDegrees(orientation);
            directionList.add(direction);
            textOrientation.setText("Direction:"+direction);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}