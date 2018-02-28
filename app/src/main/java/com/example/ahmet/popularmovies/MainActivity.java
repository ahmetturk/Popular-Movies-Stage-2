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
import android.view.MenuInflater;
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

    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.internet_status)
    TextView internetStatusTv;
    @BindView(R.id.movies_list)
    RecyclerView recyclerView;
    private MovieAdapter mMoviesAdapter;
    private RecyclerViewScrollListener mScrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Stetho.initializeWithDefaults(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, numberOfColumns());
        recyclerView.setLayoutManager(gridLayoutManager);

        recyclerView.addItemDecoration(new GridItemDecoration(this));

        mMoviesAdapter = new MovieAdapter(this);
        recyclerView.setAdapter(mMoviesAdapter);

        mScrollListener = new RecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page) {
                fetchNewMovies(page);
            }
        };

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateUI();
            }
        });
    }

    private void populateUI() {
        mScrollListener.resetState();
        mMoviesAdapter.clearMoviesList();
        fetchNewMovies(1);
    }

    private void fetchNewMovies(int page) {
        int sorting = Preferences.getSorting(this);

        if (sorting == FAVORITES) {
            recyclerView.clearOnScrollListeners();
            mSwipeRefreshLayout.setEnabled(false);
            getSupportLoaderManager().restartLoader(ID_FAVORITES_LOADER, null, this);
        } else {
            recyclerView.addOnScrollListener(mScrollListener);
            mSwipeRefreshLayout.setEnabled(true);
            getSupportLoaderManager().destroyLoader(ID_FAVORITES_LOADER);

            String sortMethod = getResources().getStringArray(R.array.sort_pref_list)[sorting];
            FetchMoviesTask moviesTask = new FetchMoviesTask(getString(R.string.language), this);
            moviesTask.execute(sortMethod, String.valueOf(page));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.popular_movies, menu);

        MenuItem item = menu.findItem(R.id.sort_spinner);
        Spinner spinner = (Spinner) item.getActionView();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_spinner_list, R.layout.sort_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setSelection(Preferences.getSorting(MainActivity.this));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Preferences.setSorting(MainActivity.this, i);
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
        int widthDivider = 500;
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
            internetStatusTv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTaskComplete(List<Movie> result) {
        mSwipeRefreshLayout.setRefreshing(false);
        if (result != null) {
            internetStatusTv.setVisibility(View.GONE);
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
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mMoviesAdapter.clearMoviesList();
    }
}
