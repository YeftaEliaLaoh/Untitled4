package com.example.myapplication8.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myapplication8.models.Cell;
import com.example.myapplication8.models.MeasuredLocationAndCell;
import com.example.myapplication8.utilities.Config;

import java.util.List;

@Dao
public interface CellDao
{
    @Query("SELECT * FROM Cell WHERE cell_sessionId = :sessionId;")
    List<Cell> getAllBySessionId( long sessionId );

    @Query("Select count(distinct cellReference)  from Cell where cell_sessionId =  :id")
    int getCellCountBySessionId( long id );

    @Query("SELECT cell_id, cell_sessionId,cellReference,mcc,mnc,lac,cellId,locationId,radioType,psc FROM Cell WHERE cell_sessionId = :sessionId GROUP BY cellReference ORDER BY cell_id ASC LIMIT :startRecord , " + Config.RESULTS_LOADER_LIMIT)
    List<Cell> getUniqueCellBySessionIdLimited( long sessionId, int startRecord );

    @Query("SELECT Cell.*, MeasuredLocation.* FROM Cell INNER JOIN MeasuredLocation ON Cell.locationId = MeasuredLocation.measuredLocation_id AND Cell.cell_sessionId = MeasuredLocation.measuredLocation_sessionId  WHERE Cell.cell_sessionId = :sessionId AND Cell.cellReference = :cellRef;")
    List<MeasuredLocationAndCell> getByCellRefAndSessionId( long sessionId, String cellRef );

    @Query("DELETE FROM Cell WHERE cell_sessionId = :sessionId;")
    void deleteBySessionId( long sessionId );

    @Insert
    long insertNewEntry( Cell cell );

}
