package com.example.myapplication8.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.myapplication8.utilities.Utility;

@Entity
public class Cell extends Radio implements Parcelable
{
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "cell_id")
    private int id;
    @ColumnInfo(name = "cell_sessionId")
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

    public Cell( Parcel parcel )
    {
        readFromParcel(parcel);
    }

    public Cell()
    {
    }

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

    public void generateCellRef()
    {
        cellId = Utility.getCid(Utility.convertByteArrayP(cellId));
        cellReference = Utility.generateCellReference(String.valueOf(mcc).concat(String.valueOf(mnc)), lac, cellId, false);
    }

    public void setCellRefWithRNC( String cellRefWithRNC )
    {
    }

    public String getCellRefWithRNC()
    {
        cellId = Utility.getCid(Utility.convertByteArrayP(cellId));
        return Utility.generateCellReference(String.valueOf(mcc).concat(String.valueOf(mnc)), lac, cellId, true);
    }

    @Override
    public int describeContents()
    {
        // TODO Auto-generated method stub
        return 0;
    }


    public void writeToParcel( Parcel parcel, int flags )
    {
        parcel.writeInt(id);
        parcel.writeInt(sessionId);
        parcel.writeString(cellReference);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        //parcel.writeDouble(elevation);
        //parcel.writeDouble(accuracy);
        parcel.writeInt(mcc);
        parcel.writeInt(mnc);
        parcel.writeInt(lac);
        parcel.writeInt(cellId);
        parcel.writeInt(psc);
        parcel.writeString(datetime);
        parcel.writeString(radioType);
        parcel.writeLong(locationId);

    }

    protected void readFromParcel( Parcel parcel )
    {
        id = parcel.readInt();
        session_id = parcel.readInt();
        cellReference = parcel.readString();
        latitude = parcel.readDouble();
        longitude = parcel.readDouble();
        elevation = parcel.readDouble();
        accuracy = parcel.readDouble();
        mcc = parcel.readInt();
        mnc = parcel.readInt();
        lac = parcel.readInt();
        cellId = parcel.readInt();
        psc = parcel.readInt();
        datetime = parcel.readString();
        radioType = parcel.readString();
        locationId = parcel.readLong();

    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public Cell createFromParcel( Parcel in )
        {
            return new Cell(in);
        }

        public Cell[] newArray( int size )
        {
            return new Cell[size];
        }
    };
}
