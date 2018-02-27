package com.example.ahmet.popularmovies.utils;

public interface AsyncTaskCompleteListener<T> {
    void onTaskStart();

    void onTaskComplete(T result);
}
