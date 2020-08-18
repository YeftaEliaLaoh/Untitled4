package com.example.myapplication8.daos;

import androidx.room.Dao;
import androidx.room.Query;

import com.example.myapplication8.models.Session;

import java.util.List;

@Dao
public interface SessionDao
{
    @Query("SELECT * FROM Session")
    List<Session> getAll();

    @Query("DELETE FROM Session WHERE id = :id;")
    void deleteById( long id );
}
