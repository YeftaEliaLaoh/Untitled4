package com.example.myapplication8.databases;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.myapplication8.daos.CellDao;
import com.example.myapplication8.daos.LocationDao;
import com.example.myapplication8.daos.SessionDao;
import com.example.myapplication8.models.Cell;
import com.example.myapplication8.models.MeasuredLocation;
import com.example.myapplication8.models.Session;

@Database(entities = {Session.class, Cell.class, MeasuredLocation.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase
{
    public abstract SessionDao sessionDao();

    public abstract CellDao cellDao();

    public abstract LocationDao locationDao();

}
