package com.example.ahmet.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class MovieProvider extends ContentProvider {

    private static final int CODE_MOVIES = 100;
    private static final int CODE_MOVIE_WITH_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    private static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MovieContract.PATH_MOVIES, CODE_MOVIES);

        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/*", CODE_MOVIE_WITH_ID);

        return matcher;
    }


    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;

        switch (sUriMatcher.match(uri)) {

            case CODE_MOVIE_WITH_ID:
                String IdString = uri.getLastPathSegment();
                String[] selectionArguments = new String[]{IdString};

                cursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                break;

            case CODE_MOVIES:
                cursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Query unknown uri: " + uri);

        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }


    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new RuntimeException("I am not implementing getType in Popular Movies.");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIES:
                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME,
                        null, contentValues);

                if (_id != -1) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return ContentUris.withAppendedId(MovieContract.MovieEntry.CONTENT_URI, _id);
                }
                break;

            default:
                throw new UnsupportedOperationException("Insert unknown uri: " + uri);
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int numRowsDeleted;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIE_WITH_ID:
                String IdString = uri.getLastPathSegment();
                String[] selectionArguments = new String[]{IdString};
                numRowsDeleted = db.delete(MovieContract.MovieEntry.TABLE_NAME,
                        MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ",
                        selectionArguments);
                break;

            case CODE_MOVIES:
                numRowsDeleted = db.delete(MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Delete unknown uri: " + uri);
        }

        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues,
                      @Nullable String selection, @Nullable String[] selectionArgs) {

        int numRowsUpdated;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIE_WITH_ID:
                String IdString = uri.getLastPathSegment();
                String[] selectionArguments = new String[]{IdString};
                numRowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME,
                        contentValues,
                        MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ",
                        selectionArguments);
                break;

            case CODE_MOVIES:
                numRowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME,
                        contentValues,
                        selection,
                        selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Delete unknown uri: " + uri);
        }

        if (numRowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsUpdated;
    }
}
