package com.example.ahmet.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.ahmet.popularmovies.adapter.MovieAdapter;
import com.example.ahmet.popularmovies.data.MovieContract;
import com.example.ahmet.popularmovies.models.Movie;
import com.example.ahmet.popularmovies.task.FetchMoviesTask;
import com.example.ahmet.popularmovies.utils.AsyncTaskCompleteListener;
import com.example.ahmet.popularmovies.utils.GridItemDecoration;
import com.example.ahmet.popularmovies.utils.RecyclerViewScrollListener;
import com.facebook.stetho.Stetho;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements AsyncTaskCompleteListener<List<Movie>>, LoaderManager.LoaderCallbacks<Cursor> {

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

    private static final int FAVORITES = 2;
    private static final int ID_FAVORITES_LOADER = 1;

    private static final String BUNDLE_MOVIES = "movies";
    private static final String BUNDLE_PAGE = "page";
    private static final String BUNDLE_COUNT = "count";
    private static final String BUNDLE_PREF = "pref";
    private static final String BUNDLE_RECYCLER = "recycler";

    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.status)
    TextView statusTv;
    @BindView(R.id.movies_list)
    RecyclerView recyclerView;
    private MovieAdapter mMoviesAdapter;
    private RecyclerViewScrollListener mScrollListener;
    private Bundle mSavedInstanceState;
    private GridLayoutManager gridLayoutManager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Stetho.initializeWithDefaults(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mSavedInstanceState = savedInstanceState;

        gridLayoutManager = new GridLayoutManager(this, numberOfColumns());
        recyclerView.setLayoutManager(gridLayoutManager);

        recyclerView.addItemDecoration(new GridItemDecoration(this));

        mMoviesAdapter = new MovieAdapter(this);
        recyclerView.setAdapter(mMoviesAdapter);

        mScrollListener = new RecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page) {
                int sorting = PopMovPreferences.getSorting(MainActivity.this);
                fetchNewMovies(page, sorting);
            }
        };

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
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
        outState.putParcelable(BUNDLE_RECYCLER, gridLayoutManager.onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int movieNumber = PopMovPreferences.getChangedMovie(this);
        if (movieNumber != -1) {
            mMoviesAdapter.notifyItemChanged(movieNumber);
            PopMovPreferences.setChangedMovie(this, -1);
        }
    }

    private void populateUI() {
        int sorting = PopMovPreferences.getSorting(this);
        mMoviesAdapter.clearMoviesList();
        if (sorting == FAVORITES) {
            // FAVORITES SELECTED
            recyclerView.clearOnScrollListeners();
            mSwipeRefreshLayout.setEnabled(false);
            getSupportLoaderManager().restartLoader(ID_FAVORITES_LOADER, null, this);
        } else {
            if (mSavedInstanceState != null && mSavedInstanceState.getInt(BUNDLE_PREF) == sorting) {
                // NOT FAVORITES SELECTED BUT THERE IS SAVED DATA
                mScrollListener.setState(
                        mSavedInstanceState.getInt(BUNDLE_PAGE),
                        mSavedInstanceState.getInt(BUNDLE_COUNT));

                ArrayList<Movie> list = mSavedInstanceState.getParcelableArrayList(BUNDLE_MOVIES);
                if ((list == null || list.isEmpty()) && !isOnline()) {
                    statusTv.setText(R.string.no_internet);
                    statusTv.setVisibility(View.VISIBLE);
                }
                mMoviesAdapter.addMoviesList(list);
                gridLayoutManager.onRestoreInstanceState(mSavedInstanceState.getParcelable(BUNDLE_RECYCLER));
            } else {
                // NOT FAVORITES SELECTED AND THERE IS NOT SAVED DATA
                mScrollListener.resetState();
                fetchNewMovies(1, sorting);
            }
            recyclerView.addOnScrollListener(mScrollListener);
            mSwipeRefreshLayout.setEnabled(true);
        }
    }

    private void fetchNewMovies(int page, int sorting) {
        getSupportLoaderManager().destroyLoader(ID_FAVORITES_LOADER);
        String sortingMethod = getResources().getStringArray(R.array.sort_pref_list)[sorting];

        FetchMoviesTask moviesTask = new FetchMoviesTask(getString(R.string.language), this);
        moviesTask.execute(sortingMethod, String.valueOf(page));
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
                PopMovPreferences.setSorting(MainActivity.this, i);
                populateUI();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        return true;
    }

    private int numberOfColumns() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // You can change this divider to adjust the size of the poster
        int widthDivider = 400;
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;
        if (nColumns < 2) return 2;
        return nColumns;
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }
        return false;
    }

    @Override
    public void onTaskStart() {
        if (!isOnline()) {
            statusTv.setText(R.string.no_internet);
            statusTv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTaskComplete(List<Movie> result) {
        mSwipeRefreshLayout.setRefreshing(false);
        if (result != null) {
            statusTv.setVisibility(View.GONE);
            mMoviesAdapter.addMoviesList(result);
        }
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
        mMoviesAdapter.clearMoviesList();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                mMoviesAdapter.addMovie(new Movie(
                        cursor.getString(INDEX_MOVIE_ID),
                        cursor.getString(INDEX_MOVIE_TITLE),
                        cursor.getString(INDEX_POSTER_PATH),
                        cursor.getString(INDEX_PLOT_SYNOPSIS),
                        cursor.getString(INDEX_USER_RATING),
                        cursor.getString(INDEX_RELEASE_DATE),
                        cursor.getString(INDEX_BACKDROP_PATH)));
            } while (cursor.moveToNext());
        }

        if (mMoviesAdapter.getItemCount() == 0) {
            statusTv.setText(R.string.no_favorite);
            statusTv.setVisibility(View.VISIBLE);
        } else {
            statusTv.setVisibility(View.GONE);
        }

        if (mSavedInstanceState != null) {
            gridLayoutManager.onRestoreInstanceState(mSavedInstanceState.getParcelable(BUNDLE_RECYCLER));
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    }
}
