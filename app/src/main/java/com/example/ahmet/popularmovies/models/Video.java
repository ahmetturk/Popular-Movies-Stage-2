package com.example.ahmet.popularmovies.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Video implements Parcelable {
    public static final Parcelable.Creator<Video> CREATOR
            = new Parcelable.Creator<Video>() {
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

    private final String videoUrl;
    private final String videoName;

    public Video(String videoUrl, String videoName) {
        this.videoUrl = videoUrl;
        this.videoName = videoName;
    }

    private Video(Parcel in) {
        videoUrl = in.readString();
        videoName = in.readString();
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getVideoName() {
        return videoName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(videoUrl);
        parcel.writeString(videoName);
    }
}

