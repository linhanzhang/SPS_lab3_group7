package com.example.myapplication.particles;

import android.graphics.Canvas;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.myapplication.map.Cell;
import com.example.myapplication.map.Layout;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class ParticleCollection {

    public int CELLNUM=15;
    public Layout layout;
    public List<Particle> particleList;
    /**
     *  RATIO = 1/(num of particles / pixel)
     */
    public int RATIO = 100;

    /**
     * Direction: is the direction in layout graph
     */
    public static final int LEFT = 180;
    public static final int UP = 270;
    public static final int RIGHT = 0; // when we calibrate we walk from the left to right in the layout graph, ref direction
                                       // will be RIGHT
    public static final int DOWN = 90;

    public ParticleCollection(Layout layout){
        this.layout = layout;
        particleList = new LinkedList<>();
    }

    public void initializeParticleSet(){
        for(int i=0; i<CELLNUM;i++){
            Cell cell = layout.getCellList().get(i);
            // the num of particles is decided by prior probability, namely the area of cell
            int numOfParticle = cell.area / RATIO;

            System.out.println("num of particle:"+numOfParticle);

            initializeParticleinCell(cell.id,cell.left,cell.top,cell.right,cell.bottom,numOfParticle);

        }
    }

    public void initializeParticleinCell(int cellId, int left, int top, int right, int bottom, int numParticle){
        Random r = new Random();
        //randomly generate n particles in cell
        for(int i=0; i<numParticle;i++){
            int x = r.nextInt(right-left)+left;
            int y = r.nextInt(bottom-top)+top;
            Particle p = new Particle(x,y,cellId);
            particleList.add(p);
        }
    }

    /**
     *  should be called to UPDATE the view
     * @param canvas
     */

    public void drawParticleCollection(Canvas canvas){
        for(int i=0;i<particleList.size();i++){   //DEBUG
            particleList.get(i).drawParticle(canvas);
        }

    }

    public void clearParticleCollection(Canvas canvas){
//        for(int i=0;i<particleList.size();i++){
//            particleList.get(i).clearParticle(canvas);
//        }
        particleList.clear();
        drawParticleCollection(canvas);
    }

    /**
     *
     * @param distance  unit: pixel
     * @param direction
     */
    public void moveParticles(Canvas canvas, int distance, int direction){
        Log.d("size of set", String.valueOf(particleList.size()));
        int count =0;
       // int pixelDistance = Cell.mapMeterToPixel(distance);
        int pixelDistance = distance;
        Log.d("move distance", String.valueOf(pixelDistance));
        if(direction == LEFT){
            for(int i=0;i<particleList.size();i++){
                Particle p = particleList.get(i);
                p.x-=pixelDistance;
                if(layout.detectOutAllCell(p)){
                    p.alive = false;
                    Log.d("die out count ", String.valueOf(count++));
                }
            }
        }
        else if(direction == UP){
            for(int i=0;i<particleList.size();i++) {
                Particle p = particleList.get(i);
                p.y -= pixelDistance;
                if(layout.detectOutAllCell(p)){
                    p.alive = false;
                    Log.d("die out count ", String.valueOf(count++));
                }
            }
        }
        else if(direction == RIGHT){
            for(int i=0;i<particleList.size();i++) {
                Particle p = particleList.get(i);
                p.x += pixelDistance;
                //Log.d("move right","p.x: "+p.x);
                if(layout.detectOutAllCell(p)){
                    p.alive = false;
                   // Log.d("die out","p.x="+p.x+" p.y="+p.y);
                    Log.d("die out count ", String.valueOf(count++));
                }
            }
        }
        else if(direction == DOWN){
            for(int i=0;i<particleList.size();i++) {
                Particle p = particleList.get(i);
                p.y += pixelDistance;
                if(layout.detectOutAllCell(p)){
                    p.alive = false;
                    Log.d("die out count ", String.valueOf(count++));
                }
            }
        }

        updateParticleSet();

        Log.d("redraw","redraw");
        drawParticleCollection(canvas);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateParticleSet(){
//        for(int i=0; i<particleList.size();i++){
//            Particle p = particleList.get(i);
//            if(!p.alive){
//                particleList.remove(i);
//            }
//        }
        int countAlive = 0;
        for(int i =0 ;i< particleList.size();i++){
            Particle p = particleList.get(i);
            if(p.alive) countAlive++;
        }

        Log.d("count alive ", String.valueOf(countAlive));
        particleList.removeIf(particle -> !particle.alive);
    }

    public void resampleParticles(){

    }


}
