package com.example.myapplication.map;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.example.myapplication.particles.Particle;

import java.util.LinkedList;
import java.util.List;

public class Layout {

    private List<Cell> cellList;
    private static int CELLNUM = 15;
    Paint paint = new Paint();

    public Layout(){
        cellList = new LinkedList<>();
        cellList.add(new Cell(1, 0f,0f, 3.63f, 3.15f));
        cellList.add(new Cell(2, 0f,3.15f, 3.63f, 6.30f));
        cellList.add(new Cell(3, 2.40f,6.30f, 3.63f, 10.32f));
        cellList.add(new Cell(4, 3.63f,2.03f, 8.44f, 4.25f));
        cellList.add(new Cell(5, 8.44f,2.03f, 13.25f, 4.25f));
        cellList.add(new Cell(6, 13.25f,2.03f, 18.06f, 4.25f));
        cellList.add(new Cell(7, 18.06f,2.03f, 22.87f, 4.25f));
        cellList.add(new Cell(8, 18.54f,4.25f, 22.87f, 6.30f));
        cellList.add(new Cell(9, 22.87f,0f, 26.50f, 3.15f));
        cellList.add(new Cell(10, 22.87f,3.15f, 26.50f, 6.30f));
        cellList.add(new Cell(11, 22.87f,6.30f, 24.10f, 10.32f));
        cellList.add(new Cell(12, 18.28f,10.32f, 24.1f, 11.90f));
        cellList.add(new Cell(13, 14.54f,4.25f, 16.83f, 8.58f));
        cellList.add(new Cell(14, 18.28f,10.32f, 24.1f, 11.90f));
        cellList.add(new Cell(15, 14.54f,4.25f, 16.83f, 8.58f));
//        cellList.add(new Cell(14, 0,0, 3.63f, 3.15f));
//        cellList.add(new Cell(15, 0,0, 3.63f, 3.15f));
    }



    /**
     *  detect if the particle is out of the scope of all cell i.e. should be dead
     * @param p
     */

    public boolean detectOutAllCell(Particle p){
        for(int i=0;i<CELLNUM;i++){
            if(cellList.get(i).detectInScope(p)){
                return false;
            }
        }
        return true;
    }

    public void drawLayout(Canvas canvas){
        for(int i=0;i<CELLNUM;i++){
            cellList.get(i).drawCell(canvas);
        }
    }

    public List<Cell> getCellList() {
        return cellList;
    }
}
