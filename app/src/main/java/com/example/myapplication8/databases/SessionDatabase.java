package com.example.myapplication8.databases;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.myapplication8.daos.SessionDao;
import com.example.myapplication8.models.Session;

@Database(entities = {Session.class}, version = 1, exportSchema = false)
public abstract class SessionDatabase extends RoomDatabase
{
    public abstract SessionDao sessionDao();
}
