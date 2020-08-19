package com.example.myapplication8.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myapplication8.models.Cell;
import com.example.myapplication8.models.MeasuredLocation;
import com.example.myapplication8.models.Session;

import java.util.List;

@Dao
public interface LocationDao
{
    @Query("SELECT * FROM MeasuredLocation WHERE sessionId = :sessionId;")
    List<MeasuredLocation> getAllBySessionId( int sessionId );

    @Query("SELECT * FROM MeasuredLocation WHERE id = :locationId;")
    MeasuredLocation getSingleLocation( long locationId );

    @Query("SELECT * FROM MeasuredLocation  WHERE latitude = :lat  AND longitude  = :ltg  AND sessionId = :sessionId LIMIT 1")
    MeasuredLocation validateMeasuredLocationIfExisted( double lat, double ltg, long sessionId );

    @Query("DELETE FROM MeasuredLocation WHERE sessionId = :sessionId;")
    void deleteBySessionId( long sessionId );

    @Insert
    long insertNewEntry( MeasuredLocation MeasuredLocation );

}
