package com.example.myapplication8.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class MeasuredLocation implements Parcelable
{
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "measuredLocation_id")
    private int id;
    @ColumnInfo(name = "latitude")
    private double latitude;
    @ColumnInfo(name = "longitude")
    private double longitude;
    @ColumnInfo(name = "accuracy")
    private double accuracy;
    @ColumnInfo(name = "time")
    private long time;
    @ColumnInfo(name = "elevation")
    private double elevation;
    @ColumnInfo(name = "bearing")
    private double bearing;
    @ColumnInfo(name = "measuredLocation_sessionId")
    private int sessionId;
    @ColumnInfo(name = "numCell")
    private int numCell;
    @ColumnInfo(name = "numWifi")
    private int numWifi;
    @ColumnInfo(name = "totalRadio")
    private int totalRadio;

    public MeasuredLocation()
    {

    }

    public MeasuredLocation( Parcel parcel )
    {
        readFromParcel(parcel);
    }

    public double getBearing()
    {
        return bearing;
    }

    public void setBearing( double bearing )
    {
        this.bearing = bearing;
    }

    public double getElevation()
    {
        return elevation;
    }

    public void setElevation( double elevation )
    {
        this.elevation = elevation;
    }

    public long getTime()
    {
        return time;
    }

    public void setTime( long time )
    {
        this.time = time;
    }

    public double getAccuracy()
    {
        return accuracy;
    }

    public void setAccuracy( double accuracy )
    {
        this.accuracy = accuracy;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public void setLongitude( double longitude )
    {
        this.longitude = longitude;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public void setLatitude( double latitude )
    {
        this.latitude = latitude;
    }

    public int getSessionId()
    {
        return sessionId;
    }

    public void setSessionId( int session_id )
    {
        this.sessionId = session_id;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel( Parcel dest, int flags )
    {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeDouble(accuracy);
        dest.writeDouble(elevation);
        dest.writeDouble(bearing);
        dest.writeLong(time);
        dest.writeInt(sessionId);
        dest.writeInt(numCell);
        dest.writeInt(numWifi);
        dest.writeInt(totalRadio);
    }

    public void readFromParcel( Parcel parcel )
    {
        latitude = parcel.readDouble();
        longitude = parcel.readDouble();
        accuracy = parcel.readDouble();
        elevation = parcel.readDouble();
        bearing = parcel.readDouble();
        time = parcel.readLong();
        sessionId = parcel.readInt();
        numCell = parcel.readInt();
        numWifi = parcel.readInt();
        totalRadio = parcel.readInt();
    }

    public static final Creator CREATOR = new Creator()
    {

        public MeasuredLocation createFromParcel( Parcel in )
        {
            return new MeasuredLocation(in);
        }

        public MeasuredLocation[] newArray( int size )
        {
            return new MeasuredLocation[size];
        }
    };

    public int getId()
    {
        return id;
    }

    public void setId( int id )
    {
        this.id = id;
    }

    public int getNumCell()
    {
        return numCell;
    }

    public void setNumCell( int numCell )
    {
        this.numCell = numCell;
    }

    public int getNumWifi()
    {
        return numWifi;
    }

    public void setNumWifi( int numWifi )
    {
        this.numWifi = numWifi;
    }

    public int getTotalRadio()
    {
        return totalRadio;
    }

    public void setTotalRadio( int totalRadio )
    {
        this.totalRadio = totalRadio;
    }
}
