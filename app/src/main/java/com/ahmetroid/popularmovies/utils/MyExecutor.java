package com.ahmetroid.popularmovies.utils;

import android.support.annotation.NonNull;

import java.util.concurrent.Executor;

public class MyExecutor implements Executor {
    @Override
    public void execute(@NonNull Runnable runnable) {
        new Thread(runnable).start();
    }
}