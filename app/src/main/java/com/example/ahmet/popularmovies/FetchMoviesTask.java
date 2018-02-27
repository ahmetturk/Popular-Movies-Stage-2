package com.example.ahmet.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;

import com.example.ahmet.popularmovies.models.Movie;
import com.example.ahmet.popularmovies.utils.AsyncTaskCompleteListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.example.ahmet.popularmovies.BuildConfig.API_KEY;

class FetchMoviesTask extends AsyncTask<String, Void, List<Movie>> {

    // JSON Keys
    private static final String MOVIE_TITLE_KEY = "title";
    private static final String POSTER_PATH_KEY = "poster_path";
    private static final String PLOT_SYNOPSIS_KEY = "overview";
    private static final String USER_RATING_KEY = "vote_average";
    private static final String RELEASE_DATE_KEY = "release_date";
    private static final String BACKDROP_PATH_KEY = "backdrop_path";

    private final String language;
    private final AsyncTaskCompleteListener<List<Movie>> listener;

    FetchMoviesTask(String language, AsyncTaskCompleteListener<List<Movie>> listener) {
        this.language = language;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        listener.onTaskStart();
    }

    @Override
    protected List<Movie> doInBackground(String... params) {
        try {
            Uri uri = Uri.parse("https://api.themoviedb.org/3/movie/").buildUpon()
                    .appendPath(params[0])
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("language", language)
                    .appendQueryParameter("page", params[1])
                    .build();
            URL url = new URL(uri.toString());

            String jsonMoviesResponse = getResponseFromHttpUrl(url);

            return getMoviesDataFromJson(jsonMoviesResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Movie> moviesList) {
        super.onPostExecute(moviesList);
        listener.onTaskComplete(moviesList);
    }

    private String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream == null) {
                return null;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }

            if (builder.length() == 0) {
                return null;
            }

            return builder.toString();
        } finally {
            urlConnection.disconnect();
        }
    }

    private List<Movie> getMoviesDataFromJson(String moviesJsonStr) throws JSONException {
        List<Movie> moviesList = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(moviesJsonStr);
        JSONArray movies = jsonObject.getJSONArray("results");
        for (int i = 0; i < movies.length(); i++) {
            JSONObject movieDetail = movies.getJSONObject(i);

                /* movieName is original name of movie
                 * posterPath is image url of the poster of movie
                 * plotSynopsis is plot synopsis of movie
                 * userRating is user rating of movie
                 * releaseDate is release date of movie
                 * backdropPath is image url of the backdrop of movie*/

            String movieName = movieDetail.getString(MOVIE_TITLE_KEY);
            String posterPath = "http://image.tmdb.org/t/p/w500/" + movieDetail.getString(POSTER_PATH_KEY);
            String plotSynopsis = movieDetail.getString(PLOT_SYNOPSIS_KEY);
            String userRating = movieDetail.getString(USER_RATING_KEY);
            String releaseDate = movieDetail.getString(RELEASE_DATE_KEY);
            String backdropPath = "http://image.tmdb.org/t/p/w1280/" + movieDetail.getString(BACKDROP_PATH_KEY);

            moviesList.add(new Movie(movieName, posterPath, plotSynopsis, userRating, releaseDate, backdropPath));
        }
        return moviesList;
    }
}
