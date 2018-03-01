package com.example.ahmet.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

class PopMovPreferences {
    static int getSorting(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(context.getString(R.string.pref_sort_key), 0);
    }

    static void setSorting(Context context, int selectedItem) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(context.getString(R.string.pref_sort_key), selectedItem);
        editor.apply();
    }
}
