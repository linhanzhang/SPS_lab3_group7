package com.example.myapplication.particles;

import android.graphics.Canvas;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.myapplication.map.Cell;
import com.example.myapplication.map.Layout;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class ParticleCollection {

    public int CELLNUM=15;
    public Layout layout;
    public List<Particle> particleList;
    public int totalNumParticles;
    /**
     *  RATIO = 1/(num of particles / pixel)
     */
    public static final int RATIO = 20;

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
        // don't initialize at Cell 13 14 15 cuz we won't start there
        for(int i=0; i<CELLNUM-3;i++){
            Cell cell = layout.getCellList().get(i);
            // the num of particles is decided by prior probability, namely the area of cell
            int numOfParticle = cell.area / RATIO;
            initializeParticleinCell(cell.id,cell.left,cell.top,cell.right,cell.bottom,numOfParticle);
            totalNumParticles = particleList.size();

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


    /**
     *
     * @param distance  unit: pixel
     * @param direction
     */
    public void moveParticles(Canvas canvas, int distance, int direction){
        Log.d("size of set", String.valueOf(particleList.size()));
        int count =0;
        int pixelDistance = distance;
        Log.d("move distance", String.valueOf(pixelDistance));
        if(direction == LEFT){
            for(int i=0;i<particleList.size();i++){
                Particle p = particleList.get(i);
                p.x-=pixelDistance;
                if(layout.detectOutAllCell(p)){
                    p.alive = false;
                }
                else{
                    p.cell = layout.getCellfromCoordination(p.x,p.y);
                }
            }
        }
        else if(direction == UP){
            for(int i=0;i<particleList.size();i++) {
                Particle p = particleList.get(i);
                p.y -= pixelDistance;
                if(layout.detectOutAllCell(p)){
                    p.alive = false;
                }
                else{
                    p.cell = layout.getCellfromCoordination(p.x,p.y);
                }
            }
        }
        else if(direction == RIGHT){
            for(int i=0;i<particleList.size();i++) {
                Particle p = particleList.get(i);
                p.x += pixelDistance;
                if(layout.detectOutAllCell(p)){
                    p.alive = false;
                }
                else{
                    p.cell = layout.getCellfromCoordination(p.x,p.y);
                }
            }
        }
        else if(direction == DOWN){
            for(int i=0;i<particleList.size();i++) {
                Particle p = particleList.get(i);
                p.y += pixelDistance;
                if(layout.detectOutAllCell(p)){
                    p.alive = false;
                }
                else{
                    p.cell = layout.getCellfromCoordination(p.x,p.y);
                }
            }
        }

        updateParticleSet();

        Log.d("redraw","redraw");
        drawParticleCollection(canvas);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateParticleSet(){
        int countAlive = 0;
        for(int i =0 ;i< particleList.size();i++){
            Particle p = particleList.get(i);
            if(p.alive) {
                p.weight++;
                countAlive++;
            }
        }

        // if all particles are out of bound unfortunately, randomly select one and leave it in
        // particleList to avoid application crash
        if(countAlive == 0){
            Random random = new Random();
            int index = random.nextInt(totalNumParticles);
            particleList.get(index).alive = true;
        }

        particleList.removeIf(particle -> !particle.alive && particleList.size() != 0);
        // new particles will be added back to the particle list, total num of particle remains unchanged
        if(particleList.size()<totalNumParticles) {
            resampleParticles((totalNumParticles - countAlive));
        }
    }

    /**
     *
     * @param numResample   num of particles that needs to be resampled
     */

    public void resampleParticles(int numResample){

        // select the particle with biggest weight
        // TODO: select top 10%, select the particle inside bound
        //      resample only when under threshold
        int maxWeight = -1;
//        Particle biggestParticle = particleList.get(0);
//
//        // print the weight of current alive particles
//        for(int i=0; i < particleList.size(); i++){
//            Particle p = particleList.get(i);
//            //Log.d("Weight", String.valueOf(p.weight));
//            if(p.weight > maxWeight ){
//                maxWeight = p.weight;
//                biggestParticle = p;
//            }
//        }
        Collections.sort(particleList);
        List <Particle> maxWeightParticleList = new LinkedList<>();
        for(int i=0;i<particleList.size()/10;i++){
            maxWeightParticleList.add(particleList.get(i));
        }

        // resample if less than 70%
        if(particleList.size()<totalNumParticles*0.7)
            resampleAroundParticleSet(maxWeightParticleList, numResample);



    }

    /**
     *  Generate
     * @param
     * @param numResample
     */

//    public void resampleAroundParticle(Particle p, int numResample){
//        for(int i=0; i<numResample; i++){
//            // std might need CHANGE later
//
//            // make sure that the resampled particle is inside scope?
//            int x = 0;
//            int y = 0;
//            x = (int) getRandomFromGaussian(p.x, 2);
//            y = (int) getRandomFromGaussian(p.y, 2);
//            // might need CHANGE later
//            Particle newParticle = new Particle(x,y,layout.getCellfromCoordination(x,y));
//            particleList.add(newParticle);
//        }
//    }

    public void resampleAroundParticleSet(List<Particle> maxWeightList, int numResample){

       // int unResampled = numResample;
        // walk through the top 10% list
        for(int i=0; i<maxWeightList.size(); i++){
            // std might need CHANGE later
            Particle p = maxWeightList.get(i);
            for(int j=0; j<numResample/maxWeightList.size();j++){
                // make sure that the resampled particle is inside scope?
                int x = 0;
                int y = 0;
                x = (int) getRandomFromGaussian(p.x, 2);
                y = (int) getRandomFromGaussian(p.y, 2);

                // if x, y not in scope, then use particle over the old one instead
                if(layout.detectOutAllCell(x,y)){
                    x = p.x;
                    y = p.y;
                }


                // might need CHANGE later
                Particle newParticle = new Particle(x,y,layout.getCellfromCoordination(x,y));
                particleList.add(newParticle);
            }



        }
    }


    public float getRandomFromGaussian(float mean, float sd) {
        Random r = new Random();
        return (float) ((r.nextGaussian() * sd) + mean);
    }

    public int getCellwithMaxWeight(){
        List <Integer> weightList = new LinkedList<>();
        for(int i=0 ;i<CELLNUM; i++){
            weightList.add(0);
        }

        for(int i=0; i<particleList.size();i++){
            Particle p = particleList.get(i);
            if(p.cell != -1) {
                int newWeightSum = weightList.get(p.cell - 1) + p.weight;
                weightList.set(p.cell - 1, newWeightSum);
            }
        }

        int maxWeightSum = -1;
        int maxWeightCell = -1;
        for(int i=0;i<CELLNUM;i++){
            if(weightList.get(i)>maxWeightSum){
                maxWeightSum = weightList.get(i);
                maxWeightCell = i+1;
            }
        }
        return maxWeightCell;
    }

}
