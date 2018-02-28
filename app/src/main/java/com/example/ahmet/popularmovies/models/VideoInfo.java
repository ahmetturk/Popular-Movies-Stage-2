package com.example.ahmet.popularmovies.models;

public class VideoInfo {
    private final String videoUrl;
    private final String videoName;

    public VideoInfo(String videoUrl, String videoName) {
        this.videoUrl = videoUrl;
        this.videoName = videoName;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getVideoName() {
        return videoName;
    }
}

