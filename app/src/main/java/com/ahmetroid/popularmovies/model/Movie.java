package com.ahmetroid.popularmovies.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

@Entity
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

    @PrimaryKey
    @NonNull
    @SerializedName("id")
    public String movieId;
    @SerializedName("title")
    public String movieTitle;
    @SerializedName("original_title")
    public String originalTitle;
    @SerializedName("poster_path")
    public String posterPath;
    @SerializedName("overview")
    public String plotSynopsis;
    @SerializedName("vote_average")
    public String userRating;
    @SerializedName("release_date")
    public String releaseDate;
    @SerializedName("backdrop_path")
    public String backdropPath;

    public Movie() {
    }

    private Movie(Parcel in) {
        this.movieId = in.readString();
        this.movieTitle = in.readString();
        this.originalTitle = in.readString();
        this.posterPath = in.readString();
        this.plotSynopsis = in.readString();
        this.userRating = in.readString();
        this.releaseDate = in.readString();
        this.backdropPath = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(movieId);
        parcel.writeString(movieTitle);
        parcel.writeString(originalTitle);
        parcel.writeString(posterPath);
        parcel.writeString(plotSynopsis);
        parcel.writeString(userRating);
        parcel.writeString(releaseDate);
        parcel.writeString(backdropPath);
    }
}