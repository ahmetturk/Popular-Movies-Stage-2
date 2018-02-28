package com.example.ahmet.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;

import com.example.ahmet.popularmovies.models.VideoInfo;
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

class FetchVideosTask extends AsyncTask<String, Void, List<VideoInfo>> {

    // JSON Keys
    private static final String VIDEO_URL_KEY = "key";
    private static final String VIDEO_NAME_KEY = "name";

    private final AsyncTaskCompleteListener<List<VideoInfo>> listener;

    FetchVideosTask(AsyncTaskCompleteListener<List<VideoInfo>> listener) {
        this.listener = listener;
    }

    @Override
    protected List<VideoInfo> doInBackground(String... params) {
        try {
            Uri uri = Uri.parse("https://api.themoviedb.org/3/movie/").buildUpon()
                    .appendPath(params[0])
                    .appendPath("videos")
                    .appendQueryParameter("api_key", API_KEY)
                    .build();
            URL url = new URL(uri.toString());

            String jsonVideosResponse = getResponseFromHttpUrl(url);

            return getVideosDataFromJson(jsonVideosResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<VideoInfo> videosList) {
        super.onPostExecute(videosList);
        listener.onTaskComplete(videosList);
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

    private List<VideoInfo> getVideosDataFromJson(String videosJsonStr) throws JSONException {
        JSONObject jsonObject = new JSONObject(videosJsonStr);
        JSONArray videos = jsonObject.getJSONArray("results");

        List<VideoInfo> videosList = new ArrayList<>(videos.length());

        for (int i = 0; i < videos.length(); i++) {
            JSONObject videoDetail = videos.getJSONObject(i);

            VideoInfo video = new VideoInfo(
                    videoDetail.getString(VIDEO_URL_KEY),
                    videoDetail.getString(VIDEO_NAME_KEY));

            videosList.add(video);
        }
        return videosList;
    }
}
