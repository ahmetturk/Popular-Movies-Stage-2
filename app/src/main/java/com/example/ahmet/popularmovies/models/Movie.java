package com.example.ahmet.popularmovies.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable {
    public static final Parcelable.Creator<Movie> CREATOR
            = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    private final String movieId;
    private final String movieTitle;
    private final String posterPath;
    private final String plotSynopsis;
    private final String userRating;
    private final String releaseDate;
    private final String backdropPath;

    public Movie(String movieId, String movieTitle, String posterPath, String plotSynopsis, String userRating, String releaseDate, String backdropPath) {
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.posterPath = posterPath;
        this.plotSynopsis = plotSynopsis;
        this.userRating = userRating;
        this.releaseDate = releaseDate;
        this.backdropPath = backdropPath;
    }

    private Movie(Parcel in) {
        this.movieId = in.readString();
        this.movieTitle = in.readString();
        this.posterPath = in.readString();
        this.plotSynopsis = in.readString();
        this.userRating = in.readString();
        this.releaseDate = in.readString();
        this.backdropPath = in.readString();
    }

    public String getMovieId() {
        return movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getPlotSynopsis() {
        return plotSynopsis;
    }

    public String getUserRating() {
        return userRating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(movieId);
        parcel.writeString(movieTitle);
        parcel.writeString(posterPath);
        parcel.writeString(plotSynopsis);
        parcel.writeString(userRating);
        parcel.writeString(releaseDate);
        parcel.writeString(backdropPath);
    }
}
