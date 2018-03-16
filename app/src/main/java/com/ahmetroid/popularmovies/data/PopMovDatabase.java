package com.ahmetroid.popularmovies.data;

import android.arch.persistence.room.Room;
import android.content.Context;

public class PopMovDatabase {

    private static AppDatabase db;

    public static AppDatabase getInstance(Context context) {
        if (db == null) {
            db = Room.databaseBuilder(context,
                    AppDatabase.class, "popularmovies").build();
        }
        return db;
    }

}