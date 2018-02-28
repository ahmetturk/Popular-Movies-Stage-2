package com.example.ahmet.popularmovies.task;

import android.net.Uri;
import android.os.AsyncTask;

import com.example.ahmet.popularmovies.models.Review;
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

public class FetchReviewsTask extends AsyncTask<String, Void, List<Review>> {

    // JSON Keys
    private static final String REVIEW_AUTHOR_KEY = "author";
    private static final String REVIEW_CONTENT_KEY = "content";

    private final AsyncTaskCompleteListener<List<Review>> listener;

    public FetchReviewsTask(AsyncTaskCompleteListener<List<Review>> listener) {
        this.listener = listener;
    }

    @Override
    protected List<Review> doInBackground(String... params) {
        try {
            Uri uri = Uri.parse("https://api.themoviedb.org/3/movie/").buildUpon()
                    .appendPath(params[0])
                    .appendPath("reviews")
                    .appendQueryParameter("api_key", API_KEY)
                    .build();
            URL url = new URL(uri.toString());

            String jsonReviewsResponse = getResponseFromHttpUrl(url);

            return getReviewsDataFromJson(jsonReviewsResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Review> reviews) {
        super.onPostExecute(reviews);
        listener.onTaskComplete(reviews);
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

    private List<Review> getReviewsDataFromJson(String reviewsJsonStr) throws JSONException {
        JSONObject jsonObject = new JSONObject(reviewsJsonStr);
        JSONArray reviews = jsonObject.getJSONArray("results");

        List<Review> reviewsList = new ArrayList<>(reviews.length());

        for (int i = 0; i < reviews.length(); i++) {
            JSONObject reviewDetail = reviews.getJSONObject(i);

            Review review = new Review(
                    reviewDetail.getString(REVIEW_AUTHOR_KEY),
                    reviewDetail.getString(REVIEW_CONTENT_KEY));

            reviewsList.add(review);
        }
        return reviewsList;
    }
}
