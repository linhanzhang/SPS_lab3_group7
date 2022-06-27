package com.example.myapplication.map;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;

import com.example.myapplication.particles.Particle;

import java.util.LinkedList;
import java.util.List;

public class Cell {

    /**
        axis in canvas
     */
    public int id;
//    public int left;
//    public int top;
//    public int right;
//    public int bottom;
    public float left;
    public float top;
    public float right;
    public float bottom;
    public float area;
    public List<ShapeDrawable> walls;


    /**
     *  initialize a cell with id and coordination of each wall
     * @param id
     * @param leftMeasure the x-axis of left wall.  unit (Meter)
     * @param topMeasure
     * @param rightMeasure
     * @param bottomMeasure
     */
    public Cell(int id, float leftMeasure, float topMeasure, float rightMeasure, float bottomMeasure){
        this.id = id;
//        this.left = mapMeterToPixel(leftMeasure);
//        this.top = mapMeterToPixel(topMeasure);
//        this.right = mapMeterToPixel(rightMeasure);
//        this.bottom = mapMeterToPixel(bottomMeasure);
        this.left = leftMeasure;
        this.top = topMeasure;
        this.right = rightMeasure;
        this.bottom = bottomMeasure;
        this.area = (right-left)*(bottom-top);
        this.walls = new LinkedList<>();
//        System.out.println("left is "+ left+"\n");
//        System.out.println("right is "+ right+"\n");
    }



    /**
     *  detect if the particle is in the scope of a certain cell
     */

    public boolean detectInScope(Particle p){
        if(p.x<right && p.x>left && p.y<bottom && p.y>top)
            return true;
        return false;
    }

    public boolean detectCoordinationInScope(float x, float y){
        if(x<right && x>left && y<bottom && y>top)
            return true;
        return false;
    }

    public void drawCell(Canvas canvas){
        ShapeDrawable drawable =  new ShapeDrawable(new RectShape());
        drawable.setBounds(Cell.mapMeterToPixel(left),Cell.mapMeterToPixel(top),Cell.mapMeterToPixel(right),Cell.mapMeterToPixel(bottom));
        drawable.getPaint().setStyle(Paint.Style.STROKE);
        drawable.getPaint().setColor(Color.BLACK);
        drawable.draw(canvas);

        drawable.getPaint().setStyle(Paint.Style.FILL);
        drawable.getPaint().setColor(Color.WHITE);
        drawable.draw(canvas);
        Paint paint = new Paint();
        paint.setTextSize(8);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("C"+id,Cell.mapMeterToPixel((left+right)/2)-6,Cell.mapMeterToPixel((top+bottom)/2)+6,paint);
    }



    /**
     *  map measured size to axis in canvas
     */

    public static int mapMeterToPixel(float measure){
//        System.out.println("measure is "+ measure +"\n");
//        System.out.println("int is "+ (int)measure +"\n");
        return (int)(measure*13);
    }




}
