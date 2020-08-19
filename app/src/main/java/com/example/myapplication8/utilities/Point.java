package com.example.myapplication8.utilities;

public class Point implements Comparable<Point>
{
    public double x, y;

    public int compareTo(Point p){
        if(this.x == p.x)
        {
            return 0;
        }
        else if(this.x < p.x)
        {
            return -1;
        }
        else
            return 1;
    }

    public String toString(){
        return "(" + x + "," + y + ")";
    }

}
