package com.ahmetroid.popularmovies.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.ahmetroid.popularmovies.BuildConfig;
import com.ahmetroid.popularmovies.R;
import com.ahmetroid.popularmovies.adapter.MovieAdapter;
import com.ahmetroid.popularmovies.data.MovieContract;
import com.ahmetroid.popularmovies.data.PopMovPreferences;
import com.ahmetroid.popularmovies.databinding.ActivityMainBinding;
import com.ahmetroid.popularmovies.model.ApiResponse;
import com.ahmetroid.popularmovies.model.Movie;
import com.ahmetroid.popularmovies.rest.ApiClient;
import com.ahmetroid.popularmovies.rest.ServiceGenerator;
import com.ahmetroid.popularmovies.utils.GridItemDecoration;
import com.ahmetroid.popularmovies.utils.RecyclerViewScrollListener;
import com.facebook.stetho.Stetho;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, MovieAdapter.ListenerMovieAdapter {

    // popular = 0, highest rated = 1, favorites = 2
    public static final int FAVORITES = 2;
    private static final int INDEX_MOVIE_ID = 0;
    private static final int INDEX_MOVIE_TITLE = 1;
    private static final int INDEX_POSTER_PATH = 2;
    private static final int INDEX_PLOT_SYNOPSIS = 3;
    private static final int INDEX_USER_RATING = 4;
    private static final int INDEX_RELEASE_DATE = 5;
    private static final int INDEX_BACKDROP_PATH = 6;
    private static final String[] MOVIE_PROJECTION = {
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS,
            MovieContract.MovieEntry.COLUMN_USER_RATING,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_BACKDROP_PATH};
    // Cursor Loader ID
    private static final int ID_FAVORITES_LOADER = 1;

    // keys for savedInstanceState bundle
    private static final String BUNDLE_MOVIES = "movies";
    private static final String BUNDLE_PAGE = "page";
    private static final String BUNDLE_COUNT = "count";
    private static final String BUNDLE_PREF = "pref";
    private static final String BUNDLE_RECYCLER = "recycler";

    private ActivityMainBinding mBinding;
    private MovieAdapter mMoviesAdapter;
    private RecyclerViewScrollListener mScrollListener;
    private Bundle mSavedInstanceState;
    private GridLayoutManager mGridLayoutManager;
    private ApiClient mApiClient;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Movies");

        mApiClient = ServiceGenerator.createService(ApiClient.class);

        mGridLayoutManager = new GridLayoutManager(this, numberOfColumns());
        mBinding.moviesList.setLayoutManager(mGridLayoutManager);

        mBinding.moviesList.addItemDecoration(new GridItemDecoration(this));

        mMoviesAdapter = new MovieAdapter(this, this);
        mBinding.moviesList.setAdapter(mMoviesAdapter);

        mScrollListener = new RecyclerViewScrollListener(mGridLayoutManager) {
            @Override
            public void onLoadMore(int page) {
                int sorting = PopMovPreferences.getSorting(MainActivity.this);
                fetchNewMovies(page, sorting);
            }
        };

        mBinding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSavedInstanceState = null;
                populateUI();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(BUNDLE_MOVIES, mMoviesAdapter.getList());
        outState.putInt(BUNDLE_PAGE, mScrollListener.getPage());
        outState.putInt(BUNDLE_COUNT, mScrollListener.getCount());
        outState.putInt(BUNDLE_PREF, PopMovPreferences.getSorting(this));
        outState.putParcelable(BUNDLE_RECYCLER, mGridLayoutManager.onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSavedInstanceState = savedInstanceState;
    }

    /**
     * Main reason is refresh the movie's favorite icon if it is selected in Detail Activity
     */
    @Override
    protected void onResume() {
        super.onResume();
        int movieNumber = PopMovPreferences.getChangedMovie(this);
        if (movieNumber != -1) {
            mMoviesAdapter.notifyItemChanged(movieNumber);
            PopMovPreferences.setChangedMovie(this, -1);
        }
    }

    /**
     * populates UI,
     * first,
     * it gets the selected sorting method from PopMovPreferences
     * if it is favorites,
     * it starts Cursor Loader
     * else,
     * it gets saved data or calls fetchNewMovies
     */
    private void populateUI() {
        int sorting = PopMovPreferences.getSorting(this);
        mMoviesAdapter.clearMoviesList();
        if (sorting == FAVORITES) {
            // FAVORITES SELECTED
            mBinding.moviesList.clearOnScrollListeners();
            mBinding.swipeRefreshLayout.setEnabled(false);
            getSupportLoaderManager().initLoader(ID_FAVORITES_LOADER, null, this);
        } else {
            if (mSavedInstanceState != null && mSavedInstanceState.getInt(BUNDLE_PREF) == sorting) {
                // NOT FAVORITES SELECTED BUT THERE IS SAVED DATA
                mScrollListener.setState(
                        mSavedInstanceState.getInt(BUNDLE_PAGE),
                        mSavedInstanceState.getInt(BUNDLE_COUNT));

                ArrayList<Movie> list = mSavedInstanceState.getParcelableArrayList(BUNDLE_MOVIES);
                if ((list == null || list.isEmpty()) && !isOnline()) {
                    showNoInternetStatus();
                }
                mMoviesAdapter.addMoviesList(list);
                mGridLayoutManager
                        .onRestoreInstanceState(mSavedInstanceState.getParcelable(BUNDLE_RECYCLER));
            } else {
                // NOT FAVORITES SELECTED AND THERE IS NOT SAVED DATA
                mScrollListener.resetState();
                fetchNewMovies(1, sorting);
            }
            mBinding.moviesList.addOnScrollListener(mScrollListener);
        }
    }

    /**
     * fetch movies from the TheMovieDB API
     *
     * @param page    the number of the page that will be fetched from API
     * @param sorting selected sorting method by user
     *                0 = most popular
     *                1 = highest rated
     */
    private void fetchNewMovies(int page, int sorting) {
        Call<ApiResponse<Movie>> call;

        switch (sorting) {
            case 0:
                call = mApiClient.getPopularMovies(getString(R.string.language),
                        String.valueOf(page));
                break;

            case 1:
            default:
                call = mApiClient.getTopRatedMovies(getString(R.string.language),
                        String.valueOf(page));
        }

        if (!isOnline()) {
            showNoInternetStatus();
        }

        call.enqueue(new Callback<ApiResponse<Movie>>() {
            @Override
            public void onResponse(Call<ApiResponse<Movie>> call, Response<ApiResponse<Movie>> response) {
                try {
                    List<Movie> result = response.body().getResults();
                    if (result != null) {
                        hideStatus();
                        mBinding.swipeRefreshLayout.setEnabled(false);
                        mMoviesAdapter.addMoviesList(result);
                    }
                } catch (NullPointerException e) {
                    Toast.makeText(MainActivity.this,
                            getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                } finally {
                    mBinding.swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Movie>> call, Throwable t) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.connection_error), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem item = menu.findItem(R.id.sort_spinner);
        Spinner spinner = (Spinner) item.getActionView();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_spinner_list, R.layout.sort_spinner_item);
        adapter.setDropDownViewResource(R.layout.sort_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setSelection(PopMovPreferences.getSorting(MainActivity.this));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i < 2) {
                    getSupportLoaderManager().destroyLoader(ID_FAVORITES_LOADER);
                }
                PopMovPreferences.setSorting(MainActivity.this, i);
                populateUI();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * calculates number of columns
     *
     * @return the number of columns in the grid layout of main activity
     */
    private int numberOfColumns() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int widthDivider = 400;
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;
        if (nColumns < 2) return 2;
        return nColumns;
    }

    /**
     * get the internet connection status
     *
     * @return true if the device has internet connection, false otherwise
     */
    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }
        return false;
    }

    /**
     * shows no internet text view and enables swipe refresh
     */
    private void showNoInternetStatus() {
        mBinding.statusImage.setImageResource(R.drawable.ic_signal_wifi_off_white_24px);
        mBinding.statusImage.setVisibility(View.VISIBLE);
        mBinding.statusText.setText(R.string.no_internet);
        mBinding.statusText.setVisibility(View.VISIBLE);
        mBinding.swipeRefreshLayout.setEnabled(true);
    }

    /**
     * shows no favorite text view
     */
    private void showNoFavoriteStatus() {
        mBinding.statusImage.setImageResource(R.drawable.ic_star_border_white_24px);
        mBinding.statusImage.setVisibility(View.VISIBLE);
        mBinding.statusText.setText(R.string.no_favorite);
        mBinding.statusText.setVisibility(View.VISIBLE);
    }

    /**
     * hides status text view
     */
    private void hideStatus() {
        mBinding.statusImage.setVisibility(View.INVISIBLE);
        mBinding.statusText.setVisibility(View.INVISIBLE);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case ID_FAVORITES_LOADER:
                return new CursorLoader(this,
                        MovieContract.MovieEntry.CONTENT_URI,
                        MOVIE_PROJECTION,
                        null,
                        null,
                        null);

            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null) {
            while (cursor.moveToNext()) {
                mMoviesAdapter.addMovie(new Movie(
                        cursor.getString(INDEX_MOVIE_ID),
                        cursor.getString(INDEX_MOVIE_TITLE),
                        cursor.getString(INDEX_POSTER_PATH),
                        cursor.getString(INDEX_PLOT_SYNOPSIS),
                        cursor.getString(INDEX_USER_RATING),
                        cursor.getString(INDEX_RELEASE_DATE),
                        cursor.getString(INDEX_BACKDROP_PATH)));
            }
        }

        if (mMoviesAdapter.getItemCount() == 0 && mSavedInstanceState != null
                && mSavedInstanceState.getInt(BUNDLE_PREF) == FAVORITES) {
            ArrayList<Movie> list = mSavedInstanceState.getParcelableArrayList(BUNDLE_MOVIES);
            mMoviesAdapter.addMoviesList(list);
            mGridLayoutManager
                    .onRestoreInstanceState(mSavedInstanceState.getParcelable(BUNDLE_RECYCLER));
        }

        if (mMoviesAdapter.getItemCount() == 0) {
            showNoFavoriteStatus();
        } else {
            hideStatus();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    }

    @Override
    public void onEmpty() {
        showNoFavoriteStatus();
    }
}
