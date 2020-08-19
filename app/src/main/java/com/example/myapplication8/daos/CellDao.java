package com.example.myapplication8.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myapplication8.models.Cell;
import com.example.myapplication8.models.Session;

import java.util.List;

@Dao
public interface CellDao
{
    @Query("SELECT * FROM Cell WHERE sessionId = :sessionId;")
    List<Cell> getAllBySessionId( long sessionId );

    @Query("DELETE FROM Cell WHERE sessionId = :sessionId;")
    void deleteBySessionId( long sessionId );

    @Query("Select count(distinct cellReference)  from Cell where sessionId =  :id")
    int getCellCountBySessionId( long id );

    @Insert
    long insertNewEntry( Cell cell );

}
