package com.example.ahmet.popularmovies;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ahmet.popularmovies.models.Movie;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import butterknife.BindView;
import butterknife.ButterKnife;


public class DetailActivity extends AppCompatActivity {

    public static final String DETAIL_INTENT_KEY = "com.example.ahmet.popularmovies.detail";

    @BindView(R.id.movieTitleTv)
    private TextView movieTitleTv;
    @BindView(R.id.releaseDateTv)
    private TextView releaseDateTv;
    @BindView(R.id.userRatingTv)
    private TextView userRatingTv;
    @BindView(R.id.plotSynopsisTv)
    private TextView plotSynopsisTv;
    @BindView(R.id.backdrop)
    private ImageView backdropIv;
    @BindView(R.id.poster)
    private ImageView posterIv;

    private Target targetBackdrop;

    private CollapsingToolbarLayout collapsingToolbar;

    private Movie movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        movie = getIntent().getParcelableExtra(DETAIL_INTENT_KEY);

        collapsingToolbar = findViewById(R.id.collapsing_toolbar);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(movie.getMovieTitle());
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        populateUI();
    }

    private void populateUI() {
        movieTitleTv.setText(movie.getMovieTitle());
        releaseDateTv.setText(movie.getReleaseDate());
        userRatingTv.setText(getString(R.string.user_rating_text, movie.getUserRating()));
        plotSynopsisTv.setText(movie.getPlotSynopsis());

        targetBackdrop = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                backdropIv.setImageBitmap(bitmap);
                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(@NonNull Palette palette) {
                        int color = palette.getVibrantColor(R.attr.colorPrimary);
                        collapsingToolbar.setContentScrimColor(color);
                        collapsingToolbar.setStatusBarScrimColor(color);
                    }
                });
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
        Picasso.with(this)
                .load(movie.getBackdropPath())
                .into(targetBackdrop);

        Picasso.with(this)
                .load(movie.getPosterPath())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(posterIv);
    }
}
