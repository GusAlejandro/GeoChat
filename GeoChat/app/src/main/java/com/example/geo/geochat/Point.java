package com.example.geo.geochat;

/**
 * Created by gustavo on 5/2/17.
 */

public class Point {
    private final double mX;
    private final double mY;

    public Point(double x, double y){
        this.mX = x;
        this.mY = y;
    }

    public double getX(){
        return this.mX;
    }

    public double getY(){
        return this.mY;
    }
}
