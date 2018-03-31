package com.ahmetroid.popularmovies.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.ahmetroid.popularmovies.R;
import com.ahmetroid.popularmovies.adapter.MovieAdapter;
import com.ahmetroid.popularmovies.data.AppPreferences;
import com.ahmetroid.popularmovies.databinding.ActivityMainBinding;
import com.ahmetroid.popularmovies.model.Movie;
import com.ahmetroid.popularmovies.utils.GridItemDecoration;
import com.ahmetroid.popularmovies.utils.RecyclerViewScrollListener;
import com.ahmetroid.popularmovies.viewmodel.MainViewModel;

import java.util.List;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

public class MainActivity extends AppCompatActivity {

    // popular = 0, highest rated = 1, favorites = 2
    public static final int FAVORITES = 2;
    public static final int NO_INTERNET = 1;

    // keys for savedInstanceState bundle
    private static final String BUNDLE_PAGE = "page";
    private static final String BUNDLE_COUNT = "count";
    private static final String BUNDLE_RECYCLER = "recycler";
    private static final String BUNDLE_PREF = "pref";

    private ActivityMainBinding mBinding;
    private MovieAdapter mMoviesAdapter;
    private RecyclerViewScrollListener mScrollListener;
    private Bundle mSavedInstanceState;
    private GridLayoutManager mGridLayoutManager;
    private MainViewModel mViewModel;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mBinding.setPresenter(this);

        mGridLayoutManager = new GridLayoutManager(this, numberOfColumns());
        mMoviesAdapter = new MovieAdapter(this);

        mBinding.moviesList.setLayoutManager(mGridLayoutManager);
        mBinding.moviesList.addItemDecoration(new GridItemDecoration(this));
        mBinding.moviesList.setAdapter(mMoviesAdapter);

        mBinding.swipeRefreshLayout.setEnabled(false);

        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        showInternetStatus();

        mScrollListener = new RecyclerViewScrollListener(mGridLayoutManager) {
            @Override
            public void onLoadMore(int page) {
                int sorting = AppPreferences.getSorting(MainActivity.this);
                mViewModel.loadMovies(sorting, page);
            }
        };

        mBinding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mViewModel.getStatus().setValue(0);
                int sorting = AppPreferences.getSorting(MainActivity.this);
                populateUI(sorting);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(BUNDLE_PAGE, mScrollListener.getPage());
        outState.putInt(BUNDLE_COUNT, mScrollListener.getCount());
        outState.putInt(BUNDLE_PREF, AppPreferences.getSorting(this));
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
        mMoviesAdapter.refreshFavorite();
    }

    private void populateUI(int selected) {
        mViewModel.getPopularMovies().removeObservers(MainActivity.this);
        mViewModel.getHighestMovies().removeObservers(MainActivity.this);
        mViewModel.getFavoriteMovies().removeObservers(MainActivity.this);

        mMoviesAdapter.clearList();
        hideStatus();

        switch (selected) {
            case 0:
                mViewModel.getPopularMovies().observe(MainActivity.this,
                        new Observer<List<Movie>>() {
                            @Override
                            public void onChanged(@Nullable List<Movie> movies) {
                                mMoviesAdapter.addMoviesList(movies);
                            }
                        });
                break;
            case 1:
                mViewModel.getHighestMovies().observe(MainActivity.this,
                        new Observer<List<Movie>>() {
                            @Override
                            public void onChanged(@Nullable List<Movie> movies) {
                                mMoviesAdapter.addMoviesList(movies);
                            }
                        });
                break;
            default:
                mBinding.swipeRefreshLayout.setEnabled(false);
                mViewModel.getFavoriteMovies().observe(MainActivity.this,
                        new Observer<List<Movie>>() {
                            @Override
                            public void onChanged(@Nullable List<Movie> movies) {
                                if (mMoviesAdapter.getItemCount() < movies.size()) {
                                    hideStatus();
                            mMoviesAdapter.addMoviesList(movies);
                                } else if (movies.size() == 0) {
                                    showNoFavoriteStatus();
                                }
                            }
                        });
        }

        if (mSavedInstanceState != null && selected == mSavedInstanceState.getInt(BUNDLE_PREF)) {
            if (selected == FAVORITES) {
                mBinding.moviesList.clearOnScrollListeners();
            } else {
                mScrollListener.setState(
                        mSavedInstanceState.getInt(BUNDLE_PAGE),
                        mSavedInstanceState.getInt(BUNDLE_COUNT));
                mBinding.moviesList.addOnScrollListener(mScrollListener);
            }
            mGridLayoutManager
                    .onRestoreInstanceState(mSavedInstanceState.getParcelable(BUNDLE_RECYCLER));
        } else {
            if (selected == FAVORITES) {
                mBinding.moviesList.clearOnScrollListeners();
            } else {
                mScrollListener.resetState();
                mBinding.moviesList.addOnScrollListener(mScrollListener);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem item = menu.findItem(R.id.sort_spinner);
        Spinner spinner = (Spinner) item.getActionView();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_spinner_list, R.layout.sort_spinner_item);
        adapter.setDropDownViewResource(R.layout.sort_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setSelection(AppPreferences.getSorting(MainActivity.this));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, final int selected, long l) {
                AppPreferences.setSorting(MainActivity.this, selected);
                populateUI(selected);
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
     * @return the number of columns in the grid layout of main ui
     */
    private int numberOfColumns() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        if (getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT) {
            if (width > 1000) {
                return 3;
            } else {
                return 2;
            }
        } else {
            if (width > 1700) {
                return 5;
            } else if (width > 1200) {
                return 4;
            } else {
                return 3;
            }
        }
    }

    /**
     * shows no internet text view and enables swipe refresh
     */
    private void showInternetStatus() {
        mViewModel.getStatus().observe(MainActivity.this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer status) {
                int sorting = AppPreferences.getSorting(MainActivity.this);
                if (sorting != FAVORITES) {
                    mBinding.swipeRefreshLayout.setRefreshing(false);
                    if (status == NO_INTERNET) {
                        mBinding.statusImage.setImageResource(R.drawable.ic_signal_wifi_off_white_24px);
                        mBinding.statusImage.setVisibility(View.VISIBLE);
                        mBinding.statusText.setText(R.string.no_internet);
                        mBinding.statusText.setVisibility(View.VISIBLE);
                        mBinding.swipeRefreshLayout.setEnabled(true);
                    } else {
                        mBinding.swipeRefreshLayout.setEnabled(false);
                        hideStatus();
                    }
                }
            }
        });

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

    public void notThisStar() {
        Snackbar.make(mBinding.mainLayout, getString(R.string.not_this_star), Snackbar.LENGTH_LONG).show();
    }
}