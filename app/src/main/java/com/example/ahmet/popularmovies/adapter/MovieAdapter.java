package com.example.ahmet.popularmovies.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ahmet.popularmovies.DetailActivity;
import com.example.ahmet.popularmovies.PopMovPreferences;
import com.example.ahmet.popularmovies.R;
import com.example.ahmet.popularmovies.data.MovieContract;
import com.example.ahmet.popularmovies.models.Movie;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private final Context mContext;
    private List<Movie> mList;


    public MovieAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    @NonNull
    public MovieAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.item_movie, parent, false);
        return new MovieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieAdapterViewHolder holder, int position) {
        Movie movie = mList.get(position);

        holder.movieTitleTv.setText(movie.getMovieTitle());
        Picasso.with(mContext)
                .load(movie.getPosterPath())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(holder.movieItemIv);

        Cursor cursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.buildMovieUriWithId(movie.getMovieId()),
                new String[]{MovieContract.MovieEntry.COLUMN_MOVIE_ID},
                null,
                null,
                null);

        if (cursor != null && cursor.moveToNext()) {
            holder.favoriteIv.setImageResource(R.drawable.ic_star_white_24px);
            holder.isFavorite = true;
        } else {
            holder.favoriteIv.setImageResource(R.drawable.ic_star_border_white_24px);
            holder.isFavorite = false;
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public int getItemCount() {
        if (mList == null) {
            return 0;
        }
        return mList.size();
    }

    public void clearMoviesList() {
        if (mList == null) {
            mList = new ArrayList<>();
        } else {
            int itemCount = mList.size();
            mList.clear();
            notifyItemRangeRemoved(0, itemCount);
        }
    }

    public void addMoviesList(List<Movie> moviesList) {
        if (moviesList != null) {
            int positionStart = mList.size();
            mList.addAll(moviesList);
            notifyItemRangeInserted(positionStart, moviesList.size());
        }
    }

    public void addMovie(Movie movie) {
        if (movie != null) {
            int positionStart = mList.size();
            mList.add(movie);
            notifyItemInserted(positionStart);
        }
    }

    public ArrayList<Movie> getList() {
        return (ArrayList<Movie>) mList;
    }

    class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.movie_title_tv)
        TextView movieTitleTv;
        @BindView(R.id.movie_item_iv)
        ImageView movieItemIv;
        @BindView(R.id.favorite_iv)
        ImageView favoriteIv;
        boolean isFavorite;

        MovieAdapterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        /**
         * starts detail activity for this movie
         * <p>
         * setChangedMovie is called to refresh the favorite star icon of this movie
         * when returning back to main activity
         */
        @Override
        public void onClick(View v) {
            int movieNumber = getAdapterPosition();
            PopMovPreferences.setChangedMovie(mContext, movieNumber);

            Movie movie = mList.get(movieNumber);
            Intent intent = new Intent(mContext, DetailActivity.class);
            intent.putExtra(DetailActivity.DETAIL_INTENT_KEY, movie);
            mContext.startActivity(intent);
        }

        /**
         * adds the movie to favorite or remove it if it already exists
         * adding favorite means adds it to sql database
         */
        @OnClick(R.id.favorite_iv)
        public void onClickFavoriteButton(View view) {
            String snackBarText;
            Movie movie = mList.get(getAdapterPosition());
            if (isFavorite) {
                mContext.getContentResolver().delete(
                        MovieContract.MovieEntry.buildMovieUriWithId(movie.getMovieId()),
                        null,
                        null);
                isFavorite = false;
                favoriteIv.setImageResource(R.drawable.ic_star_border_white_24px);
                snackBarText = mContext.getString(R.string.remove_favorite);
            } else {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movie.getMovieId());
                contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, movie.getMovieTitle());
                contentValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
                contentValues.put(MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS, movie.getPlotSynopsis());
                contentValues.put(MovieContract.MovieEntry.COLUMN_USER_RATING, movie.getUserRating());
                contentValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
                contentValues.put(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH, movie.getBackdropPath());

                mContext.getContentResolver().insert(
                        MovieContract.MovieEntry.CONTENT_URI,
                        contentValues);
                isFavorite = true;
                favoriteIv.setImageResource(R.drawable.ic_star_white_24px);
                snackBarText = mContext.getString(R.string.add_favorite);
            }
            Snackbar.make(view, snackBarText, Snackbar.LENGTH_SHORT).show();
        }
    }
}

