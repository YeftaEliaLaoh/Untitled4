package com.example.myapplication8.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Cell extends Radio
{
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "sessionId")
    private int sessionId;
    @ColumnInfo(name = "cellReference")
    private String cellReference;
    @ColumnInfo(name = "psc")
    private int psc;
    @ColumnInfo(name = "cellId")
    private int cellId;
    @ColumnInfo(name = "mnc")
    private int mnc;
    @ColumnInfo(name = "mcc")
    private int mcc;
    @ColumnInfo(name = "lac")
    private int lac;
    @ColumnInfo(name = "radioType")
    private String radioType;
    @ColumnInfo(name = "locationId")
    private long locationId;

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
        return sessionId;
    }

    public void setSessionId( int sessionId )
    {
        this.sessionId = sessionId;
    }

    public String getCellReference()
    {
        return cellReference;
    }

    public void setCellReference( String cellReference )
    {
        this.cellReference = cellReference;
    }

    public int getPsc()
    {
        return psc;
    }

    public void setPsc( int psc )
    {
        this.psc = psc;
    }

    public int getCellId()
    {
        return cellId;
    }

    public void setCellId( int cellId )
    {
        this.cellId = cellId;
    }

    public int getMnc()
    {
        return mnc;
    }

    public void setMnc( int mnc )
    {
        this.mnc = mnc;
    }

    public int getMcc()
    {
        return mcc;
    }

    public void setMcc( int mcc )
    {
        this.mcc = mcc;
    }

    public int getLac()
    {
        return lac;
    }

    public void setLac( int lac )
    {
        this.lac = lac;
    }

    public String getRadioType()
    {
        return radioType;
    }

    public void setRadioType( String radioType )
    {
        this.radioType = radioType;
    }

    public long getLocationId()
    {
        return locationId;
    }

    public void setLocationId( long locationId )
    {
        this.locationId = locationId;
    }
}
