package com.ahmetroid.popularmovies.activity;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmetroid.popularmovies.R;
import com.ahmetroid.popularmovies.adapter.ReviewAdapter;
import com.ahmetroid.popularmovies.adapter.VideoAdapter;
import com.ahmetroid.popularmovies.data.MovieContract;
import com.ahmetroid.popularmovies.model.ApiResponse;
import com.ahmetroid.popularmovies.model.Movie;
import com.ahmetroid.popularmovies.model.Review;
import com.ahmetroid.popularmovies.model.Video;
import com.ahmetroid.popularmovies.rest.ApiClient;
import com.ahmetroid.popularmovies.rest.ServiceGenerator;
import com.ahmetroid.popularmovies.utils.HorizontalItemDecoration;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


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
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    private boolean isFavorite;
    private VideoAdapter mVideoAdapter;
    private ReviewAdapter mReviewAdapter;
    private Target targetBackdrop;
    private Movie movie;
    private ApiClient mApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        mApiClient = ServiceGenerator.createService(ApiClient.class);

        movie = getIntent().getParcelableExtra(DETAIL_INTENT_KEY);

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

    /**
     * populates UI of Detail Activity except Videos and Reviews
     */
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
                        int color = palette.getMutedColor(R.attr.colorPrimary) | 0xFF000000;
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
                .load("http://image.tmdb.org/t/p/w780" + movie.getBackdropPath())
                .into(targetBackdrop);

        Picasso.with(this)
                .load("http://image.tmdb.org/t/p/w342" + movie.getPosterPath())
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

    /**
     * fetch videos and populate their views
     */
    private void populateVideos(Bundle savedInstanceState) {
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        videosRecyclerView.setLayoutManager(layoutManager);
        videosRecyclerView.setHasFixedSize(true);
        videosRecyclerView.setNestedScrollingEnabled(false);

        RecyclerView.ItemDecoration itemDecoration = new HorizontalItemDecoration(this);
        videosRecyclerView.addItemDecoration(itemDecoration);

        mVideoAdapter = new VideoAdapter(this);
        videosRecyclerView.setAdapter(mVideoAdapter);

        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_VIDEOS)) {
            mVideoAdapter.addVideosList(savedInstanceState.
                    <Video>getParcelableArrayList(BUNDLE_VIDEOS));
        } else {
            Call<ApiResponse<Video>> call = mApiClient.getVideos(movie.getMovieId());

            call.enqueue(new Callback<ApiResponse<Video>>() {
                @Override
                public void onResponse(Call<ApiResponse<Video>> call,
                                       Response<ApiResponse<Video>> response) {
                    try {
                        List<Video> result = response.body().getResults();
                        if (result != null) {
                            mVideoAdapter.addVideosList(result);
                        }
                    } catch (NullPointerException e) {
                        Toast.makeText(DetailActivity.this,
                                getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Video>> call, Throwable t) {
                    Toast.makeText(DetailActivity.this,
                            getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * fetch reviews and populate their views
     */
    private void populateReviews(Bundle savedInstanceState) {
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        reviewsRecyclerView.setLayoutManager(layoutManager);
        reviewsRecyclerView.setHasFixedSize(true);
        reviewsRecyclerView.setNestedScrollingEnabled(false);

        RecyclerView.ItemDecoration itemDecoration = new HorizontalItemDecoration(this);
        reviewsRecyclerView.addItemDecoration(itemDecoration);

        mReviewAdapter = new ReviewAdapter(this);
        reviewsRecyclerView.setAdapter(mReviewAdapter);

        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_REVIEWS)) {
            mReviewAdapter.addReviewsList(savedInstanceState.<Review>getParcelableArrayList(BUNDLE_REVIEWS));
        } else {
            Call<ApiResponse<Review>> call = mApiClient.getReviews(movie.getMovieId());

            call.enqueue(new Callback<ApiResponse<Review>>() {
                @Override
                public void onResponse(Call<ApiResponse<Review>> call,
                                       Response<ApiResponse<Review>> response) {
                    try {
                        List<Review> result = response.body().getResults();
                        if (result != null) {
                            mReviewAdapter.addReviewsList(result);
                        }
                    } catch (NullPointerException e) {
                        Toast.makeText(DetailActivity.this,
                                getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Review>> call, Throwable t) {
                    Toast.makeText(DetailActivity.this,
                            getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * adds the movie to favorite or remove it if it already exists
     * adding favorite means adds it to sql database
     */
    public void onClickFavoriteButton(View view) {
        String snackBarText;
        if (isFavorite) {
            getContentResolver().delete(
                    MovieContract.MovieEntry.buildMovieUriWithId(movie.getMovieId()),
                    null,
                    null);
            isFavorite = false;
            favoriteButton.setImageResource(R.drawable.ic_star_border_white_24px);
            snackBarText = getString(R.string.remove_favorite);
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
            snackBarText = getString(R.string.add_favorite);
        }
        Snackbar.make(coordinatorLayout, snackBarText, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.share) {
            String shareText = "https://www.themoviedb.org/movie/" + movie.getMovieId();
            ShareCompat.IntentBuilder intentBuilder = ShareCompat.IntentBuilder.from(this)
                    .setText(shareText)
                    .setType("text/plain");
            try {
                intentBuilder.startChooser();
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, R.string.no_app, Toast.LENGTH_LONG).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
