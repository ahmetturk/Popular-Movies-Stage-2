package com.ahmetroid.popularmovies.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ahmetroid.popularmovies.R;
import com.ahmetroid.popularmovies.data.AppDatabase;
import com.ahmetroid.popularmovies.data.AppPreferences;
import com.ahmetroid.popularmovies.databinding.ItemMovieBinding;
import com.ahmetroid.popularmovies.model.MiniMovie;
import com.ahmetroid.popularmovies.model.Movie;
import com.ahmetroid.popularmovies.ui.DetailActivity;
import com.ahmetroid.popularmovies.ui.MainActivity;
import com.ahmetroid.popularmovies.utils.MyExecutor;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private Activity mActivity;
    private AppDatabase mDatabase;
    private List<Movie> mList;
    private Executor executor;

    public MovieAdapter(Activity activity) {
        this.mActivity = activity;
        this.mDatabase = AppDatabase.getDatabase(activity);
        this.executor = new MyExecutor();
    }

    @Override
    @NonNull
    public MovieAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mActivity);
        ItemMovieBinding binding = ItemMovieBinding.inflate(layoutInflater, parent, false);
        return new MovieAdapterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieAdapterViewHolder holder, int position) {
        Movie movie = mList.get(position);
        holder.bind(movie);
    }

    @Override
    public int getItemCount() {
        if (mList == null) {
            return 0;
        }
        return mList.size();
    }

    public void clearList() {
        if (mList == null) {
            mList = new ArrayList<>();
        } else {
            int itemCount = mList.size();
            mList.clear();
            notifyItemRangeRemoved(0, itemCount);
        }
    }

    public void addMoviesList(List<Movie> moviesList) {
        int positionStart = mList.size();
        mList.clear();

        mList.addAll(moviesList);
        notifyItemRangeInserted(positionStart, moviesList.size() - positionStart);
    }

    // using for refreshing favorite star when back from detail ui
    public void refreshFavorite() {
        int movieNumber = AppPreferences.getChangedMovie(mActivity);
        if (movieNumber != -1) {
            notifyItemChanged(movieNumber);
            AppPreferences.setChangedMovie(mActivity, -1);
        }
    }

    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder {
        ItemMovieBinding binding;
        boolean isFavorite;

        MovieAdapterViewHolder(ItemMovieBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final Movie movie) {
            binding.setMovie(movie);
            binding.setPresenter(this);

            Picasso.get()
                    .load("http://image.tmdb.org/t/p/w342" + movie.posterPath)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .into(binding.movieItemIv);

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    final MiniMovie miniMovie = mDatabase.movieDao().getMovieById(movie.movieId);

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (miniMovie != null) {
                                binding.favoriteIv.setImageResource(R.drawable.ic_star_white_24px);
                                isFavorite = true;
                            } else {
                                binding.favoriteIv.setImageResource(R.drawable.ic_star_border_white_24px);
                                isFavorite = false;
                            }
                        }
                    });
                }
            });
        }

        /**
         * starts detail ui for this movie
         * setChangedMovie is called to refresh the favorite star icon of this movie
         * when returning back to main ui
         */
        public void openMovieDetail(Movie movie) {
            int movieNumber = getAdapterPosition();

            Intent intent = new Intent(mActivity, DetailActivity.class);
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(mActivity,
                            binding.movieItemIv,
                            ViewCompat.getTransitionName(binding.movieItemIv));
            intent.putExtra(DetailActivity.DETAIL_INTENT_KEY, movie);
            intent.putExtra(DetailActivity.MOVIE_NUMBER_KEY, movieNumber);
            mActivity.startActivity(intent, options.toBundle());
        }

        /**
         * adds the movie to favorite or remove it if it already exists
         * adding favorite means adds it to sql database
         */
        public void onClickFavorite(View view) {
            String snackBarText;
            int position = getAdapterPosition();
            final Movie movie = mList.get(position);

            if (isFavorite) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        mDatabase.movieDao().delete(movie);
                    }
                });
                isFavorite = false;
                binding.favoriteIv.setImageResource(R.drawable.ic_star_border_white_24px);
                snackBarText = mActivity.getString(R.string.remove_favorite);

                if (AppPreferences.getSorting(mActivity) == MainActivity.FAVORITES) {
                    mList.remove(position);
                    notifyItemRemoved(position);
                }

            } else {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        mDatabase.movieDao().insert(movie);
                    }
                });
                isFavorite = true;
                binding.favoriteIv.setImageResource(R.drawable.ic_star_white_24px);
                snackBarText = mActivity.getString(R.string.add_favorite);
            }
            Snackbar.make(view, snackBarText, Snackbar.LENGTH_SHORT).show();
        }
    }
}