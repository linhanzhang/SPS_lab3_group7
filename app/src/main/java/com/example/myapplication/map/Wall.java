package com.example.myapplication.map;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;

public class Wall {

    public float left;
    public float top;
    public float right;
    public float bottom;


    public Wall(float left, float top, float right, float bottom){
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public void drawWall(Canvas canvas){
        ShapeDrawable drawable =  new ShapeDrawable(new RectShape());
        drawable.setBounds(Cell.mapMeterToPixel(left),Cell.mapMeterToPixel(top),Cell.mapMeterToPixel(right),Cell.mapMeterToPixel(bottom));
        drawable.getPaint().setStyle(Paint.Style.STROKE);
        drawable.getPaint().setColor(Color.BLACK);
        drawable.draw(canvas);

    }
}
