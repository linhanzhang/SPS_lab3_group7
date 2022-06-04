package com.example.myapplication.particles;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.util.Log;

import androidx.versionedparcelable.ParcelImpl;

import java.util.Collection;
import java.util.Collections;

public class Particle implements Comparable<Particle> {

    public int x;
    public int y;
    public int weight;
    public boolean alive;
    public ShapeDrawable drawable;
    /**
     *  which cell it is in
     */
    public int cell;

    public Particle(int x, int y, int cell) {
        this.x = x;
        this.y = y;
        this.cell = cell;
        this.alive = true;
        this.weight = 0;
        drawable = new ShapeDrawable(new OvalShape());
    }


    public void drawParticle(Canvas canvas){
       // ShapeDrawable drawable =  new ShapeDrawable(new OvalShape());
        drawable.getPaint().setColor(Color.RED);
        drawable.getPaint().setAntiAlias(true);
        drawable.setBounds(x,y,x+3,y+3);
        //Log.d("x axis: ", String.valueOf(this.x));
        drawable.draw(canvas);
    }

    public void clearParticle(Canvas canvas){
        Log.d("clear","clear");
        ShapeDrawable drawable =  new ShapeDrawable(new OvalShape());
        drawable.getPaint().setColor(Color.WHITE);
        drawable.getPaint().setAntiAlias(true);
        drawable.setBounds(x,y,x+3,y+3);
        drawable.draw(canvas);

    }

    @Override
    public int compareTo(Particle p) {
        return Integer.compare(this.weight, p.weight);
    }



}
