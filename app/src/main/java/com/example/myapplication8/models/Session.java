package com.example.myapplication8.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;

@Entity
public class Session implements Parcelable
{
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "status")
    private int status;
    @ColumnInfo(name = "cellCount")
    private int cellCount;
    @ColumnInfo(name = "partFile")
    private String partFile;
    @ColumnInfo(name = "startTime")
    private long startTime;
    @ColumnInfo(name = "stopTime")
    private long stopTime;
    @ColumnInfo(name = "dateTime")
    private long dateTime;
    @ColumnInfo(name = "exportId")
    private long exportId;

    private ArrayList<Cell> cellList;

    public static final int IDLE = 0;
    public static final int SENT = 1;
    public static final int EXPORTED = 2;

    public Session()
    {
        setStartTime(System.currentTimeMillis());

        cellList = new ArrayList<>();
    }

    public Session(Parcel parcel)
    {
        readFromParcel(parcel);
    }

    public int getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = (int) id;
    }

    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }

    public long getStartTime()
    {
        return startTime;
    }

    public void setStartTime(long startTime)
    {
        this.startTime = startTime;
    }

    public long getStopTime()
    {
        return stopTime;
    }

    public void setStopTime(long stopTime)
    {
        this.stopTime = stopTime;
    }

    public void setDateTime(long dateTime)
    {
        this.dateTime = dateTime;
    }

    public long getDateTime()
    {
        return this.dateTime;
    }

    //Export ID setter and getter
    public long getExportId()
    {
        return exportId;
    }

    public void setExportId(long exportId)
    {
        this.exportId = exportId;
    }

    public ArrayList<Cell> getCellList()
    {
        return cellList;
    }

    public void setCellList(ArrayList<Cell> cellList)
    {
        this.cellList = cellList;
    }

    public String getPartFile()
    {
        return partFile;
    }

    public void setPartFile(String partFile)
    {
        this.partFile = partFile;
    }

    @Override
    public int describeContents()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags)
    {
        parcel.writeInt(id);
        parcel.writeInt(status);
        parcel.writeLong(startTime);
        parcel.writeLong(stopTime);
        parcel.writeLong(dateTime);
        parcel.writeInt(cellCount);
    }

    protected void readFromParcel(Parcel parcel)
    {
        id = parcel.readInt();
        status = parcel.readInt();
        startTime = parcel.readLong();
        stopTime = parcel.readLong();
        dateTime = parcel.readLong();
        cellCount = parcel.readInt();
    }

    public int getCellCount()
    {
        return cellCount;
    }

    public void setCellCount(int cellCount)
    {
        this.cellCount = cellCount;
    }

    public static final Creator<Session> CREATOR = new Creator<Session>()
    {

        public Session createFromParcel(Parcel source)
        {
            return new Session(source);
        }

        public Session[] newArray(int size)
        {
            return new Session[size];
        }

    };
}
