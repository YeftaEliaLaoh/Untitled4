package com.example.myapplication8.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ClusteredCenterMarker implements ClusterItem
{
    private String name;
    private LatLng position;
    private double latitude;
    private double longitude;
    private int type;

    public ClusteredCenterMarker( LatLng latlng, String name, int type )
    {
        this.position = latlng;
        this.latitude = latlng.latitude;
        this.longitude = latlng.longitude;
        this.name = name;
        this.type = type;
    }

    @Override
    public LatLng getPosition()
    {
        return position;
    }

    @Override
    public String getTitle()
    {
        return null;
    }

    @Override
    public String getSnippet()
    {
        return null;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public int getType()
    {
        return type;
    }

    public String getName()
    {
        return name;
    }

}
