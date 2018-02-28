package com.example.ahmet.popularmovies;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ahmet.popularmovies.adapter.ReviewAdapter;
import com.example.ahmet.popularmovies.adapter.VideoAdapter;
import com.example.ahmet.popularmovies.models.Movie;
import com.example.ahmet.popularmovies.models.Review;
import com.example.ahmet.popularmovies.models.Video;
import com.example.ahmet.popularmovies.task.FetchReviewsTask;
import com.example.ahmet.popularmovies.task.FetchVideosTask;
import com.example.ahmet.popularmovies.utils.AsyncTaskCompleteListener;
import com.example.ahmet.popularmovies.utils.HorizontalItemDecoration;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class DetailActivity extends AppCompatActivity {

    public static final String DETAIL_INTENT_KEY = "com.example.ahmet.popularmovies.detail";

    @BindView(R.id.movieTitleTv)
    TextView movieTitleTv;
    @BindView(R.id.releaseDateTv)
    TextView releaseDateTv;
    @BindView(R.id.userRatingTv)
    TextView userRatingTv;
    @BindView(R.id.plotSynopsisTv)
    TextView plotSynopsisTv;
    @BindView(R.id.backdrop)
    ImageView backdropIv;
    @BindView(R.id.poster)
    ImageView posterIv;
    @BindView(R.id.videos_list)
    RecyclerView videosRecyclerView;
    @BindView(R.id.reviews_list)
    RecyclerView reviewsRecyclerView;

    private VideoAdapter mVideoAdapter;
    private ReviewAdapter mReviewAdapter;
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
        populateVideos();
        populateReviews();
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

    private void populateVideos() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        videosRecyclerView.setLayoutManager(layoutManager);

        RecyclerView.ItemDecoration itemDecoration = new HorizontalItemDecoration(this);
        videosRecyclerView.addItemDecoration(itemDecoration);

        mVideoAdapter = new VideoAdapter(this);
        videosRecyclerView.setAdapter(mVideoAdapter);

        FetchVideosTask fetchVideosTask = new FetchVideosTask(new AsyncTaskCompleteListener<List<Video>>() {
            @Override
            public void onTaskStart() {
            }

            @Override
            public void onTaskComplete(List<Video> result) {
                if (result != null) {
                    mVideoAdapter.addVideosList(result);
                }
            }
        });
        fetchVideosTask.execute(movie.getMovieId());
    }

    private void populateReviews() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        reviewsRecyclerView.setLayoutManager(layoutManager);

        RecyclerView.ItemDecoration itemDecoration = new HorizontalItemDecoration(this);
        reviewsRecyclerView.addItemDecoration(itemDecoration);

        mReviewAdapter = new ReviewAdapter(this);
        reviewsRecyclerView.setAdapter(mReviewAdapter);

        FetchReviewsTask fetchReviewsTask = new FetchReviewsTask(new AsyncTaskCompleteListener<List<Review>>() {
            @Override
            public void onTaskStart() {
            }

            @Override
            public void onTaskComplete(List<Review> result) {
                if (result != null) {
                    mReviewAdapter.addReviewsList(result);
                }
            }
        });
        fetchReviewsTask.execute(movie.getMovieId());
    }
}
