package com.example.ahmet.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class MovieContract {
    static final String CONTENT_AUTHORITY = "com.example.ahmet.popularmovies";
    static final String PATH_MOVIES = "movies";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIES)
                .build();
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_MOVIE_TITLE = "movie_title";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_PLOT_SYNOPSIS = "plot_synopsis";
        public static final String COLUMN_USER_RATING = "user_rating";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";
        static final String TABLE_NAME = "movies";

        public static Uri buildMovieUriWithId(String id) {
            return CONTENT_URI.buildUpon()
                    .appendPath(id)
                    .build();
        }

    }
}
