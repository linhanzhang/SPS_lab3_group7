//package com.example.myapplication;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.os.Bundle;
//
//public class MainActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//    }
//}
package com.example.myapplication.Activities;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.map.Layout;
import com.example.myapplication.particles.ParticleCollection;

import  java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {
    private static final String TAG = "DEBUG";


    private float stepDistance= (float) 0.66;
    private float refDirection=-110;

    /**
     * The sensor manager object.
     */
    private SensorManager sensorManager;
    private Button calibration;
    private Button localization;
    private TextView textReminder;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("7777777");
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        calibration = (Button) findViewById(R.id.buttonCalibration);
        localization = (Button) findViewById(R.id.buttonLocalization);
        textReminder = (TextView) findViewById(R.id.textViewReminder);

        textReminder.setText("step length = "+stepDistance+
                "ref direction = "+refDirection );

        // get step distance from Calibration Activity
//        Bundle bundle = getIntent().getExtras();
//        if(bundle!=null){
//            stepDistance = bundle.getFloat("stepDistance");
//            refDirection = bundle.getFloat("refDirection");
//            textReminder.setText("step length = "+stepDistance+
//                                 "ref direction = "+refDirection );
//        }
//        else{
//            System.out.println("No step!!!!");
//        }

        calibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentCalib=new Intent(MainActivity.this, CalibrationActivity.class);
                startActivityForResult(intentCalib,1);
            }
        });

        localization.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(stepDistance ==0){
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "you haven't done calibration yet, please calibrate your step distance first",
                            Toast.LENGTH_LONG);

                    toast.show();

                }else {
                    Intent intentLoc = new Intent(MainActivity.this, LocalizationActivity.class);
                    intentLoc.putExtra("stepDistance",stepDistance);
                    intentLoc.putExtra("refDirection",refDirection);
                    startActivity(intentLoc);
                }
            }
        });





    }

    @SuppressLint("SetTextI18n")
    public void onSensorChanged(SensorEvent event)  // 监听数据变化
    {

    }

    public void onAccuracyChanged(Sensor sensor,int accuracy)
    {//不用处理，空着就行
        return ;
    }


    public void onClick(View v)  //监听函数
    {

    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            stepDistance = data.getFloatExtra("stepDistance",stepDistance);
            refDirection = data.getFloatExtra("refDirection",refDirection);

            System.out.println("finish and return to main, step distance is"+stepDistance);
            System.out.println("finish and return to main, refDirection is"+refDirection);

            textReminder.setText("step length = "+stepDistance+
                    "ref direction = "+refDirection );

        }
    }
}