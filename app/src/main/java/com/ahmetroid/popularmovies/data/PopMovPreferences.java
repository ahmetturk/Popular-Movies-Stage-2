package com.ahmetroid.popularmovies.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ahmetroid.popularmovies.R;

public class PopMovPreferences {
    public static int getSorting(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(context.getString(R.string.pref_sort_key), 0);
    }

    public static void setSorting(Context context, int selectedItem) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(context.getString(R.string.pref_sort_key), selectedItem);
        editor.apply();
    }

    public static int getChangedMovie(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(context.getString(R.string.pref_changed_movie), -1);
    }

    public static void setChangedMovie(Context context, int movieNumber) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(context.getString(R.string.pref_changed_movie), movieNumber);
        editor.apply();
    }
}