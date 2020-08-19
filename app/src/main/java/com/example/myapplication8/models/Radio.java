package com.example.myapplication8.models;


import androidx.room.Ignore;

public class Radio
{
    @Ignore
    int id;
    @Ignore
    int session_id;
    @Ignore
    long location_id;
    @Ignore
    double latitude;
    @Ignore
    double longitude;
    @Ignore
    double accuracy;
    @Ignore
    int signal_strength;
    @Ignore
    double elevation;
    @Ignore
    String datetime;
    @Ignore
    MeasuredLocation measuredLocation;

    public int getId()
    {
        return id;
    }

    public void setId( int id )
    {
        this.id = id;
    }

    public int getSessionId()
    {
        return session_id;
    }

    public void setSessionId( int session_id )
    {
        this.session_id = session_id;
    }

    public long getLocationId()
    {
        return location_id;
    }

    public void setLocationId( long location_id )
    {
        this.location_id = location_id;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public void setLatitude( double latitude )
    {
        this.latitude = latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public void setLongitude( double longitude )
    {
        this.longitude = longitude;
    }

    public double getAccuracy()
    {
        return accuracy;
    }

    public void setAccuracy( double accuracy )
    {
        this.accuracy = accuracy;
    }

    public int getSignalStrength()
    {
        return signal_strength;
    }

    public void setSignalStrength( int signal_strength )
    {
        this.signal_strength = signal_strength;
    }

    public double getElevation()
    {
        return elevation;
    }

    public void setElevation( double elevation )
    {
        this.elevation = elevation;
    }

    public String getDatetime()
    {
        return datetime;
    }

    public void setDatetime( String datetime )
    {
        this.datetime = datetime;
    }

    public void setMeasuredLocation( MeasuredLocation measuredLocation )
    {
        this.measuredLocation = measuredLocation;
    }

    public MeasuredLocation getMeasuredLocation()
    {
        return measuredLocation;
    }
}
