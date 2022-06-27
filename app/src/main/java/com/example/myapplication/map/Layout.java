package com.example.myapplication.map;

import android.graphics.Rect;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;

import com.example.myapplication.particles.Particle;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Layout {

    private List<Cell> cellList;
    private static int CELLNUM = 15;
    Paint paint = new Paint();
    /**
     * 柜子，cell8的墙
     */
    public List<Wall> wallList;

    public Layout(){
        cellList = new LinkedList<>();
        wallList = new LinkedList<>();
       // cellList.add(new Cell(1, 0f,0f, 3.63f, 3.15f));
        cellList.add(new Cell(1, 2.40f,6.30f, 3.63f, 10.32f));
        cellList.add(new Cell(2, 0f,3.15f, 3.63f, 6.30f));
        cellList.add(new Cell(3, 0f,0f, 3.63f, 3.15f));
        cellList.add(new Cell(4, 3.63f,2.03f, 8.44f, 4.25f));
        cellList.add(new Cell(5, 8.44f,2.03f, 13.25f, 4.25f));
        cellList.add(new Cell(6, 13.25f,2.03f, 18.06f, 4.25f)); //
        cellList.add(new Cell(7, 18.06f,2.03f, 22.87f, 4.25f));
        cellList.add(new Cell(8, 18.54f,4.25f, 22.87f, 6.30f));
        cellList.add(new Cell(9, 22.87f,0f, 26.50f, 3.15f));
        cellList.add(new Cell(10, 22.87f,3.15f, 26.50f, 6.30f));
        cellList.add(new Cell(11, 22.87f,6.30f, 24.10f, 10.32f));
        cellList.add(new Cell(12, 18.28f,10.32f, 24.1f, 11.90f));
        cellList.add(new Cell(13, 14.5f,4.25f, 16.79f, 8.58f));  //original bottom 8,58f
        cellList.add(new Cell(14, 14.5f,4.25f, 16.79f, 8.58f));  //original bottom 8,58f
        cellList.add(new Cell(15, 14.5f,4.25f, 16.79f, 8.58f));  //original bottom 8,58f

        cellList.add(new Cell(16, 14.5f,5.58f, 16.79f, 8.58f));

        // if(x< && x>14.5f && y<5.58f && y>7.55f)

        // left wall to the door of cell 8
//        ShapeDrawable cell8left = new ShapeDrawable(new RectShape()); //门左边 20.09
//        cell8left.setBounds(Cell.mapMeterToPixel(18.34f), Cell.mapMeterToPixel(4.05f), Cell.mapMeterToPixel(20.09f), Cell.mapMeterToPixel(4.65f) );
//        wallList.add(cell8left);

        // wall of cell 8
        wallList.add(new Wall(18.34f,4.05f, 20.09f, 4.65f ));
        wallList.add(new Wall(20.99f,4.05f, 22.87f, 4.65f ));
        wallList.add(new Wall(22.47f,4.05f, 23.07f, 6.5f ));

        // 柜子 in cell 9
        //wallList.add(new Wall(20.87f,1.43f, 25f, 2.03f ));
        // 柜子 in cell 1
        //wallList.add(new Wall(1.5f,1.43f, 5.63f, 2.03f ));

        // left wall of staircase
        wallList.add(new Wall(16.34f,4.05f, 18.06f, 4.45f ));

        // right wall of staircase
        wallList.add(new Wall(13.25f,4.05f, 15.33f, 4.45f ));

        // right wall to the door of cell 8
//        ShapeDrawable cell8right = new ShapeDrawable(new RectShape()); // 门右边 20.99
//        cell8right.setBounds(Cell.mapMeterToPixel(20.99f), Cell.mapMeterToPixel(4.05f), Cell.mapMeterToPixel(22.87f), Cell.mapMeterToPixel(4.65f) );
//        wallList.add(cell8right);
//
//        // right vertical complete wall of cell 8
//        ShapeDrawable cell8rightComplete = new ShapeDrawable(new RectShape()); //original bottom  6.3f
//        cell8rightComplete.setBounds(Cell.mapMeterToPixel(22.47f), Cell.mapMeterToPixel(4.05f), Cell.mapMeterToPixel(23.07f), Cell.mapMeterToPixel(6.5f) );
//        wallList.add(cell8rightComplete);
//
//        // 柜子 in cell9
//        ShapeDrawable cabinetCell9 = new ShapeDrawable(new RectShape()); // left original 22.87
//        cabinetCell9.setBounds(Cell.mapMeterToPixel(20.87f), Cell.mapMeterToPixel(1.43f), Cell.mapMeterToPixel(25f), Cell.mapMeterToPixel(2.03f) );
//        wallList.add(cabinetCell9);
//
//        // 柜子 in cell1
//        ShapeDrawable cabinetCell1 = new ShapeDrawable(new RectShape()); // right original 3.63f
//        cabinetCell1.setBounds(Cell.mapMeterToPixel(1.5f), Cell.mapMeterToPixel(1.43f), Cell.mapMeterToPixel(5.63f), Cell.mapMeterToPixel(2.03f) );
//        wallList.add(cabinetCell1);
//
//        // left wall of staircase
//        ShapeDrawable cell14left = new ShapeDrawable(new RectShape()); // right original 3.63f
//        cell14left.setBounds(Cell.mapMeterToPixel(16.34f), Cell.mapMeterToPixel(4.05f), Cell.mapMeterToPixel(18.06f), Cell.mapMeterToPixel(4.45f) );
//        wallList.add(cell14left);
//
//        // right wall of staircase
//        ShapeDrawable cell14right = new ShapeDrawable(new RectShape()); // right original 3.63f
//        cell14right.setBounds(Cell.mapMeterToPixel(13.25f), Cell.mapMeterToPixel(4.05f), Cell.mapMeterToPixel(15.33f), Cell.mapMeterToPixel(4.45f) );
//        wallList.add(cell14right);




//        cellList.add(new Cell(13, 14.54f,7.25f, 16.83f, 9.58f));
//        cellList.add(new Cell(14, 14.54f,4.25f, 16.83f, 6.58f));
//        cellList.add(new Cell(15, 14.54f,10.25f, 16.83f, 12.58f));
      //  cellList.add(new Cell(13, 14f,4.25f, 17.31f, 8.58f));  中等大小框
            //    bottom 12 f 延长框
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

    public boolean detectOutAllCell(float x, float y){
        for(int i=0;i<CELLNUM;i++){
            if(cellList.get(i).detectCoordinationInScope(x,y)){
                return false;
            }
        }
        return true;
    }

    public boolean collide(Particle p, float lastX, float lastY, float newX, float newY){
        // if direction == 0 or 180 may collide with vertical wall
            for (int i = 0; i < wallList.size(); i++) {
                Wall wall = wallList.get(i);
                if(collideWithWall(p,lastX, lastY, newX, newY, wall)) {
                    System.out.println("Collision detected!");
                    return true;
                }
            }

            return false;
    }

    public boolean collideWithWall(Particle p, float lastX, float lastY, float newX, float newY, Wall wall){


//            if(p.drawable.getBounds().intersect(wall)){
//                System.out.println("[Detect intersection!]");
//                return true;
//            }

//             collide with vertical wall left-> right
            if(lastX < wall.left && wall.left < newX )
                if(wall.top < lastY && lastY < wall.bottom)
                    return true;
            if(lastX < wall.right && wall.right < newX )
                if(wall.top < lastY && lastY < wall.bottom)
                    return true;


            // collide with vertical wall right-> left
            if(newX < wall.left && wall.left < lastX)
                if(wall.top < lastY && lastY < wall.bottom)
                    return true;
            if(newX < wall.right && wall.right < lastX)
                if(wall.top < lastY && lastY < wall.bottom)
                    return true;


            // collide with horizontal wall top-> bottom
            if(lastY < wall.top && wall.top < newY)
                if(wall.left < lastX && lastX < wall.right)
                    return true;
            if(lastY < wall.bottom && wall.bottom < newY)
                if(wall.left < lastX && lastX < wall.right)
                    return true;



            // collide with horizontal wall right-> left
            if(newY < wall.top && wall.top < lastY)
                if(wall.left < lastX && lastX < wall.right)
                    return true;
            if(newY < wall.bottom && wall.bottom< lastY)
                if(wall.left < lastX && lastX < wall.right)
                    return true;

            return false;
    }


//
//    public boolean

    public void drawLayout(Canvas canvas){
        for(int i=0;i<CELLNUM;i++){
            cellList.get(i).drawCell(canvas);
        }

        for(int i=0; i< wallList.size();i++){
            wallList.get(i).drawWall(canvas);
        }

    }

    public List<Cell> getCellList() {
        return cellList;
    }

    /**
     *
     * @param x
     * @param y
     * @return the cell id
     */
    public int getCellfromCoordination(float x, float y){


        // 16 is stairs
//        if(x< && x>14.5f && y<5.58f && y>7.55f){
//            System.out.println("Stair Detected~~~~");
//            return 16;
//        }
        if(cellList.get(15).detectCoordinationInScope(x,y)) {
            //System.out.println("Stair Detected~~~~");
            return 16;
        }
//        14.5f,4.25f, 16.79f, 8.58f


        for(int i=0;i<CELLNUM;i++){
            if(cellList.get(i).detectCoordinationInScope(x,y)){
                return (i+1);
            }
        }
        // if not in any cell
        return -1;
    }

    public List<Integer> getNeighborCells(int cellId){
        switch (cellId){
            case 1:
                return Arrays.asList(new Integer[]{1,2,4});
            case 2:
                return Arrays.asList(new Integer[]{1,2,3,4});
            case 3:
                return Arrays.asList(new Integer[]{2,3});
            case 4:
                return Arrays.asList(new Integer[]{1,2,4,5});
            case 5:
                return Arrays.asList(new Integer[]{4,5,6});
            case 6:
                return Arrays.asList(new Integer[]{5,6,7,14});
            case 7:
                return Arrays.asList(new Integer[]{6,7,8});
            case 8:
                return Arrays.asList(new Integer[]{7,8,10});
            case 9:
                return Arrays.asList(new Integer[]{7,9,10});
            case 10:
                return Arrays.asList(new Integer[]{10,11,9});
            case 11:
                return Arrays.asList(new Integer[]{10,11,12});
            case 12:
                return Arrays.asList(new Integer[]{11,12});
            case 14:
                return Arrays.asList(new Integer[]{6,14});
            default:
                break;

        }
        return null;
    }





}
