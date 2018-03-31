package com.ahmetroid.popularmovies.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.ahmetroid.popularmovies.R;
import com.ahmetroid.popularmovies.databinding.ActivityReviewBinding;
import com.ahmetroid.popularmovies.model.Review;

public class ReviewActivity extends AppCompatActivity {

    public static final String REVIEW_INTENT_KEY = "com.ahmetroid.popularmovies.ui.review";
    public static final String MOVIE_TITLE_KEY = "com.ahmetroid.popularmovies.ui.movie_title";
    public static final String COLOR_ACTIONBAR_KEY = "com.ahmetroid.popularmovies.ui.color_actionbar";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityReviewBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_review);

        Intent intent = getIntent();
        Review review = intent.getParcelableExtra(REVIEW_INTENT_KEY);
        String movieTitle = intent.getStringExtra(MOVIE_TITLE_KEY);
        int color = intent.getIntExtra(COLOR_ACTIONBAR_KEY, ContextCompat.getColor(this, R.color.colorPrimary));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(String.format("%s - %s", getString(R.string.review), movieTitle));
            actionBar.setBackgroundDrawable(new ColorDrawable(color));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }

        binding.setReview(review);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
