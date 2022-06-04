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

    private float height;
    private int floor;

    private int detectCollision;
    private int currentCell;
    private final int thresholdCOLLISION = 20;  // TODO: need change

    private boolean keepStil = false;


    public ParticleCollection(Layout layout){
        this.layout = layout;
        particleList = new LinkedList<>();
        height = 51; // the height in floor 2
        floor = 2;

        detectCollision = 0;
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
    public void moveParticles(Canvas canvas, int distance, int direction, float newHeight){
        Log.d("size of set", String.valueOf(particleList.size()));
        int count =0;
        int pixelDistance = distance;
        Log.d("move distance", String.valueOf(pixelDistance));
        int countDead = 0;

        if((int)newHeight != (int)height){
            moveBetweenFloors(newHeight);
        }

        // if in floor 1 or 3, don't move particle anymore
        if(!keepStil) {

            for (int i = 0; i < particleList.size(); i++) {
                Particle p = particleList.get(i);
                // when less than 10% remains, don't do move particle anymore
                if (countDead < particleList.size() * 9 / 10) {

                    switch (direction) {
                        case LEFT:
                            p.x -= pixelDistance;
                            break;
                        case UP:
                            p.y -= pixelDistance;
                            break;
                        case RIGHT:
                            p.x += pixelDistance;
                            break;
                        case DOWN:
                            p.y += pixelDistance;
                            break;

                    }

                    if (layout.detectOutAllCell(p)) {
                        p.alive = false;
                        countDead++;
                    } else {
                        // get new cell id
                        p.cell = layout.getCellfromCoordination(p.x, p.y);
                    }
                } else {
                    detectCollision++;
                    break;
                }

            }

            // TODO: if collision happens continuously, means that the localization may be wrong and need correlation
            // thus we spread the particle in two neighboring cells and ask the teacher to move around and
            // let the particles converge again
            if (detectCollision > thresholdCOLLISION) {
                particleList.clear();
                spreadinNeighborCells();
                detectCollision = 0;
                System.out.println("current cell is " + currentCell + " detect collision !!!!");
            }


            //    System.out.println("count Dead "+Integer.toString(countDead)+"total "+particleList.size());
        }
        updateParticleSet();
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

        System.out.println("count Alive "+Integer.toString(countAlive));

        particleList.removeIf(particle -> !particle.alive && particleList.size() != 0);
        // new particles will be added back to the particle list, total num of particle remains unchanged
        // if less than 70% left, need resample
        if(particleList.size()<totalNumParticles*0.7) {
            resampleParticles((totalNumParticles - countAlive));
        }
    }

    /**
     *
     * @param numResample   num of particles that needs to be resampled
     */

    public void resampleParticles(int numResample){

        // select top 10%, select the particle inside bound
        //      resample only when under threshold
        Collections.sort(particleList);
        List <Particle> maxWeightParticleList = new LinkedList<>();
        for(int i=0;i<particleList.size()/10;i++){
            maxWeightParticleList.add(particleList.get(i));
        }

        // resample if less than 70%
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
        int numToResample = numResample;
        for(int i=0; i<maxWeightList.size(); i++){
            // std might need CHANGE later
            Particle p = maxWeightList.get(i);
            for(int j=0; j<numResample/maxWeightList.size();j++){
                // make sure that the resampled particle is inside scope?
                int x = 0;
                int y = 0;
                x = (int) getRandomFromGaussian(p.x, 1);
                y = (int) getRandomFromGaussian(p.y, 1);

                // if x, y not in scope, then use particle over the old one instead
                if(layout.detectOutAllCell(x,y)){
                    x = p.x;
                    y = p.y;
                }


                // might need CHANGE later
                Particle newParticle = new Particle(x,y,layout.getCellfromCoordination(x,y));
                particleList.add(newParticle);
                numToResample --;
            }
        }
        if(numToResample > 0 ){
            Random ran = new Random();
            int index = ran.nextInt(maxWeightList.size());
            Particle p = maxWeightList.get(index);
            for(int i=0; i<numToResample; i++){
                // make sure that the resampled particle is inside scope?
                int x = 0;
                int y = 0;
                x = (int) getRandomFromGaussian(p.x, 1);
                y = (int) getRandomFromGaussian(p.y, 1);

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

        // if in floor 1 or floor 3 , directly know its in cell 13 / 15
        System.out.println("floor in "+floor);


        if(floor == 1) {
            currentCell = 13;
            return 13;
        }
        else if (floor == 3) {
            currentCell = 15;
            return 15;
        }


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

        if(maxWeightCell == 13 || maxWeightCell == 15){
            maxWeightCell = 14;
        }

        // walk from cell 6 to cell 14
        if(currentCell == 6 && maxWeightCell == 14){
            keepStil = true;
        }
        currentCell = maxWeightCell;
        return maxWeightCell;
    }

    public void moveBetweenFloors(float newHeight){
        // Path:
        // 14->13 13->14
        // 14->15 15->14

        // move from cell 14 to cell 15
        if(floor == 2 && inRange(newHeight, 51, 52.5F)){
//            for(int i=0;i<particleList.size();i++){
//                Particle p = particleList.get(i);
//                p.y += 60;
//                p.cell = layout.getCellfromCoordination(p.x, p.y);
//            }
            floor = 3;
            height = newHeight;
        }
        // move from cell 14 to cell 13
        if(floor == 2 && inRange(newHeight, 44,45.5F)){
//            for(int i=0;i<particleList.size();i++){
//                Particle p = particleList.get(i);
//                p.y += 30;
//                p.cell = layout.getCellfromCoordination(p.x, p.y);
//            }
            floor = 1;
            height = newHeight;
        }
        // move from cell 13 to cell 14
        if(floor ==1 && inRange(newHeight, 47, 49)){
//            for(int i=0;i<particleList.size();i++){
//                Particle p = particleList.get(i);
//                p.y -= 30;
//                p.cell = layout.getCellfromCoordination(p.x, p.y);
//            }
            floor = 2;
            height = newHeight;
        }
        // move from cell 15 to cell 14
        if(floor ==3 && inRange(newHeight, 47,49)){
//            for(int i=0;i<particleList.size();i++){
//                Particle p = particleList.get(i);
//                p.y -= 60;
//                p.cell = layout.getCellfromCoordination(p.x, p.y);
//            }
            floor = 2;
            keepStil = false;
            height = newHeight;
        }
    }

    boolean inRange( float height, float min, float max){
        if(height > min && height < max)
            return true;
        else
            return false;
    }

    void spreadinNeighborCells(){
        List<Integer> neighborList = layout.getNeighborCells(currentCell);
        for(int i=0; i<neighborList.size();i++){
            int id = neighborList.get(i);
            Cell cell = layout.getCellList().get(id-1);
            // the num of particles is decided by prior probability, namely the area of cell
            int numOfParticle = totalNumParticles/neighborList.size();
            initializeParticleinCell(cell.id,cell.left,cell.top,cell.right,cell.bottom,numOfParticle);

        }
    }


}
