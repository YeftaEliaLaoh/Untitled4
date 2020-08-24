package com.example.myapplication8.models;

import androidx.room.Embedded;

public class MeasuredLocationAndCell
{

    @Embedded
    MeasuredLocation measuredLocation;

    @Embedded
    Cell cell;

    public MeasuredLocation getMeasuredLocation()
    {
        return measuredLocation;
    }

    public void setMeasuredLocation( MeasuredLocation measuredLocation )
    {
        this.measuredLocation = measuredLocation;
    }

    public Cell getCell()
    {
        return cell;
    }

    public void setCell( Cell cell )
    {
        this.cell = cell;
    }
}
