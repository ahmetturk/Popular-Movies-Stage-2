package com.example.ahmet.popularmovies.utils;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

abstract public class RecyclerViewScrollListener extends RecyclerView.OnScrollListener {

    private final GridLayoutManager mLayoutManager;
    private int previousTotalItemCount = 10;
    private int page = 2;
    private boolean loading = true;

    protected RecyclerViewScrollListener(GridLayoutManager layoutManager) {
        mLayoutManager = layoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

        int totalItemCount = mLayoutManager.getItemCount();
        int lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition();


        if (loading && (totalItemCount > previousTotalItemCount)) {
            loading = false;
            previousTotalItemCount = totalItemCount;
        }

        int visibleThreshold = 2;
        if (!loading && (lastVisibleItemPosition + visibleThreshold) > totalItemCount) {
            onLoadMore(page);
            page++;
            loading = true;
        }
    }

    public void resetState() {
        this.page = 2;
        this.previousTotalItemCount = 10;
        this.loading = false;
    }

    public abstract void onLoadMore(int page);
}
