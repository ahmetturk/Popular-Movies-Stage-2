package com.ahmetroid.popularmovies.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ApiResponse<T> {
    @SerializedName("results")
    private List<T> results;

    public ApiResponse(List<T> results) {
        this.results = results;
    }

    public List<T> getResults() {
        return results;
    }
}