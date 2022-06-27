package com.example.myapplication.motion;

public class StepCounter {
    private float[] oldData;    // Old accel data (0 is x, 1 is y and 2 is z)
    final  private  float  STEP_MIN_ACCEL_RES = 1.2f ;     //0.5
    final private int MIN_STRIDE_DURATION = 80;  // 7 Readings at 20ms each -> min 140 ms     40
    final private int MAX_STRIDE_DURATION = 160; // Stride can be of max 500ms      70
    private int highDuration;   // Duration for which accel readings have been higher than thresh

    public StepCounter()
    {
        highDuration = 0;
        oldData = null;
    }

    /**
     *  This function is not used
     * @param curretACCEL
     * @return
     */

    public boolean isStepDetected (float[] curretACCEL)
    {

        boolean stepDetected = false;

        if (oldData == null) {
            oldData = new  float [ 3 ];
            for (int i = 0; i < 3; i ++)
                oldData [ i ] = curretACCEL [ i ];
        }

        float res = getAccelRes(curretACCEL);

        if (res > STEP_MIN_ACCEL_RES) {

            highDuration ++;
            System.out.println(highDuration);
            //return stepDetected;
        }
        if((highDuration>MIN_STRIDE_DURATION)&&(highDuration<MAX_STRIDE_DURATION)){
            stepDetected=true;
            highDuration=0;
            return stepDetected;
        }else{
            return stepDetected;
        }


    }

    public static float getAccelRes (float[] curretACCEL)
    {
//        float res = (float) Math.sqrt((Math.pow(curretACCEL[0], 2))
//                + (Math.pow(curretACCEL[1], 2))
//                + (Math.pow(curretACCEL[2], 2)));

        float res = (float) Math.pow((curretACCEL[2]-9.8),2);

       // System.out.println("Res is -------> "+res);

        return res;
    }
}
