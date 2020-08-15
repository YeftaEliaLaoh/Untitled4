package com.example.myapplication8.models;

import android.location.Location;

public class MapSingleton
{
    private static MapSingleton mapSingleton;
    private int selectedMap = -1;
    //user interaction sign
    private boolean touchStatus = false;
    //last location
    private Location lastLocation;

    public static MapSingleton getInstance()
    {
        if( null == mapSingleton )
        {
            mapSingleton = new MapSingleton();
        }
        return mapSingleton;
    }

    public int getSelectedMap()
    {
        return selectedMap;
    }

    public void setSelectedMap( int selectedMap )
    {
        this.selectedMap = selectedMap;
    }

    //set touch status
    public void setTouchStatus( Boolean touchStatus )
    {
        this.touchStatus = touchStatus;
    }

    //get touch status
    public boolean getTouchStatus()
    {
        return touchStatus;
    }

    //set last location
    public void setLastLocation( Location lastLocation )
    {
        this.lastLocation = lastLocation;
    }

    //get last location
    public Location getLastLocation()
    {
        return lastLocation;
    }

}
