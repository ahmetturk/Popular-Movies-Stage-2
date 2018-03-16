package com.ahmetroid.popularmovies.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.ahmetroid.popularmovies.model.Movie;

@Database(entities = {Movie.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract MovieDao movieDao();
}