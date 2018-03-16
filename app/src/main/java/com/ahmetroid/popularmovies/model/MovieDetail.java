package com.ahmetroid.popularmovies.model;

import com.google.gson.annotations.SerializedName;

public class MovieDetail {

    @SerializedName("overview")
    private String plotSynopsis;

    public MovieDetail() {
    }

    public String getPlotSynopsis() {
        return plotSynopsis;
    }

    public void setPlotSynopsis(String plotSynopsis) {
        this.plotSynopsis = plotSynopsis;
    }
}
