package com.example.myapplication8.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myapplication8.models.MeasuredLocation;

import java.util.List;

@Dao
public interface LocationDao
{
    @Query("SELECT * FROM MeasuredLocation WHERE measuredLocation_sessionId = :sessionId;")
    List<MeasuredLocation> getAllBySessionId( int sessionId );

    @Query("SELECT * FROM MeasuredLocation WHERE measuredLocation_id = :locationId;")
    MeasuredLocation getSingleLocation( long locationId );

    @Query("SELECT * FROM MeasuredLocation  WHERE latitude = :lat  AND longitude  = :ltg  AND measuredLocation_sessionId = :sessionId LIMIT 1")
    MeasuredLocation validateMeasuredLocationIfExisted( double lat, double ltg, long sessionId );

    @Query("DELETE FROM MeasuredLocation WHERE measuredLocation_sessionId = :sessionId;")
    void deleteBySessionId( long sessionId );

    @Insert
    long insertNewEntry( MeasuredLocation MeasuredLocation );

}
