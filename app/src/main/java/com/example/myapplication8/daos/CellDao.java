package com.example.myapplication8.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myapplication8.models.Cell;
import com.example.myapplication8.models.Session;
import com.example.myapplication8.utilities.Config;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface CellDao
{
    @Query("SELECT * FROM Cell WHERE sessionId = :sessionId;")
    List<Cell> getAllBySessionId( long sessionId );

    @Query("Select count(distinct cellReference)  from Cell where sessionId =  :id")
    int getCellCountBySessionId( long id );

    @Query("SELECT id, sessionId,cellReference,mcc,mnc,lac,cellId,locationId,radioType,psc FROM Cell WHERE sessionId = :sessionId GROUP BY cellReference ORDER BY id ASC LIMIT :startRecord , " + Config.RESULTS_LOADER_LIMIT)
    List<Cell> getUniqueCellBySessionIdLimited( long sessionId, int startRecord );

    @Query("DELETE FROM Cell WHERE sessionId = :sessionId;")
    void deleteBySessionId( long sessionId );

    @Insert
    long insertNewEntry( Cell cell );

}
