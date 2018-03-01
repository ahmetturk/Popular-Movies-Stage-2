package com.example.ahmet.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ahmet.popularmovies.adapter.ReviewAdapter;
import com.example.ahmet.popularmovies.adapter.VideoAdapter;
import com.example.ahmet.popularmovies.data.MovieContract;
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
import butterknife.OnClick;


public class DetailActivity extends AppCompatActivity {
    public static final String DETAIL_INTENT_KEY = "com.example.ahmet.popularmovies.detail";

    private static final String BUNDLE_VIDEOS = "videos";
    private static final String BUNDLE_REVIEWS = "reviews";

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
    @BindView(R.id.favorite_button)
    FloatingActionButton favoriteButton;

    private boolean isFavorite;
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
        populateVideos(savedInstanceState);
        populateReviews(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(BUNDLE_VIDEOS, mVideoAdapter.getList());
        outState.putParcelableArrayList(BUNDLE_REVIEWS, mReviewAdapter.getList());
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

        Cursor cursor = getContentResolver().query(
                MovieContract.MovieEntry.buildMovieUriWithId(movie.getMovieId()),
                new String[]{MovieContract.MovieEntry.COLUMN_MOVIE_ID},
                null,
                null,
                null);

        isFavorite = false;
        if (cursor != null && cursor.moveToNext()) {
            isFavorite = true;
            favoriteButton.setImageResource(R.drawable.ic_star_white_24px);
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    private void populateVideos(Bundle savedInstanceState) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        videosRecyclerView.setLayoutManager(layoutManager);

        RecyclerView.ItemDecoration itemDecoration = new HorizontalItemDecoration(this);
        videosRecyclerView.addItemDecoration(itemDecoration);

        mVideoAdapter = new VideoAdapter(this);
        videosRecyclerView.setAdapter(mVideoAdapter);

        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_VIDEOS)) {
            mVideoAdapter.addVideosList(savedInstanceState.<Video>getParcelableArrayList(BUNDLE_VIDEOS));
        } else {

            FetchVideosTask fetchVideosTask = new FetchVideosTask(new AsyncTaskCompleteListener<List<Video>>() {
                @Override
                public void onTaskStart() {
                }

                @Override
                public void onTaskComplete(List<Video> result) {
                    if (result != null && !result.isEmpty()) {

                        mVideoAdapter.addVideosList(result);
                    }
                }
            });
            fetchVideosTask.execute(movie.getMovieId());
        }
    }

    private void populateReviews(Bundle savedInstanceState) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        reviewsRecyclerView.setLayoutManager(layoutManager);

        RecyclerView.ItemDecoration itemDecoration = new HorizontalItemDecoration(this);
        reviewsRecyclerView.addItemDecoration(itemDecoration);

        mReviewAdapter = new ReviewAdapter(this);
        reviewsRecyclerView.setAdapter(mReviewAdapter);

        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_REVIEWS)) {
            mReviewAdapter.addReviewsList(savedInstanceState.<Review>getParcelableArrayList(BUNDLE_REVIEWS));
        } else {

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

    @OnClick(R.id.favorite_button)
    public void onClickFavoriteButton() {
        if (isFavorite) {
            getContentResolver().delete(
                    MovieContract.MovieEntry.buildMovieUriWithId(movie.getMovieId()),
                    null,
                    null);
            isFavorite = false;
            favoriteButton.setImageResource(R.drawable.ic_star_border_white_24px);

        } else {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movie.getMovieId());
            contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, movie.getMovieTitle());
            contentValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
            contentValues.put(MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS, movie.getPlotSynopsis());
            contentValues.put(MovieContract.MovieEntry.COLUMN_USER_RATING, movie.getUserRating());
            contentValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
            contentValues.put(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH, movie.getBackdropPath());

            getContentResolver().insert(
                    MovieContract.MovieEntry.CONTENT_URI,
                    contentValues);
            isFavorite = true;
            favoriteButton.setImageResource(R.drawable.ic_star_white_24px);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.share) {
            String shareText = mVideoAdapter.getShareUrl();
            if (shareText != null && !shareText.isEmpty()) {
                ShareCompat.IntentBuilder intentBuilder = ShareCompat.IntentBuilder.from(this)
                        .setText(shareText)
                        .setType("text/plain");
                try {
                    intentBuilder.startChooser();
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, R.string.no_app, Toast.LENGTH_LONG).show();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
