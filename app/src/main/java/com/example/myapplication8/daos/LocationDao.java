package com.example.myapplication8.daos;

import androidx.room.Dao;
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

    @Query("DELETE FROM MeasuredLocation WHERE sessionId = :sessionId;")
    void deleteBySessionId( long sessionId );
}
