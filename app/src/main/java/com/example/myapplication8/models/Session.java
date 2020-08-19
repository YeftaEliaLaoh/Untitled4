package com.example.myapplication8.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Session implements Parcelable
{
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "cellCount")
    private int cellCount;
    @ColumnInfo(name = "partFile")
    private String partFile;
    @ColumnInfo(name = "dateTime")
    private long dateTime;
    @ColumnInfo(name = "exportId")
    private long exportId;

    public static final int IDLE = 0;
    public static final int SENT = 1;
    public static final int EXPORTED = 2;

    public Session()
    {
        //cellList = new ArrayList<>();
    }

    public Session( Parcel parcel )
    {
        readFromParcel(parcel);
    }

    public long getId()
    {
        return id;
    }

    public void setId( long id )
    {
        this.id = id;
    }

    public void setDateTime( long dateTime )
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

    public void setExportId( long exportId )
    {
        this.exportId = exportId;
    }

/*    public ArrayList<Cell> getCellList()
    {
        return cellList;
    }

    public void setCellList(ArrayList<Cell> cellList)
    {
        this.cellList = cellList;
    }*/

    public String getPartFile()
    {
        return partFile;
    }

    public void setPartFile( String partFile )
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
    public void writeToParcel( Parcel parcel, int flags )
    {
        parcel.writeLong(id);
        parcel.writeLong(dateTime);
        parcel.writeInt(cellCount);
    }

    protected void readFromParcel( Parcel parcel )
    {
        id = parcel.readInt();
        dateTime = parcel.readLong();
        cellCount = parcel.readInt();
    }

    public int getCellCount()
    {
        return cellCount;
    }

    public void setCellCount( int cellCount )
    {
        this.cellCount = cellCount;
    }

    public static final Creator<Session> CREATOR = new Creator<Session>()
    {

        public Session createFromParcel( Parcel source )
        {
            return new Session(source);
        }

        public Session[] newArray( int size )
        {
            return new Session[size];
        }

    };
}
