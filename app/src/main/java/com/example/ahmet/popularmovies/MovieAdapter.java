package com.example.ahmet.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ahmet.popularmovies.models.Movie;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private final Context mContext;
    private List<Movie> mMoviesList;


    MovieAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    @NonNull
    public MovieAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_movie, parent, false);
        return new MovieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieAdapterViewHolder holder, int position) {
        Movie movie = mMoviesList.get(position);

        holder.movieTitleTv.setText(movie.getMovieTitle());
        Picasso.with(mContext)
                .load(movie.getPosterPath())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(holder.movieItemIv);
    }

    @Override
    public int getItemCount() {
        if (mMoviesList == null) {
            return 0;
        }
        return mMoviesList.size();
    }

    void clearMoviesList() {
        if (mMoviesList == null) {
            mMoviesList = new ArrayList<>();
        } else {
            int itemCount = mMoviesList.size();
            mMoviesList.clear();
            notifyItemRangeRemoved(0, itemCount);
        }
    }

    void addMoviesList(List<Movie> moviesList) {
        int positionStart = mMoviesList.size();
        mMoviesList.addAll(moviesList);
        notifyItemRangeInserted(positionStart, moviesList.size());
    }

    class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.movie_title_tv)
        TextView movieTitleTv;
        @BindView(R.id.movie_item_iv)
        ImageView movieItemIv;

        MovieAdapterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Movie movie = mMoviesList.get(getAdapterPosition());
            Intent intent = new Intent(mContext, DetailActivity.class);
            intent.putExtra(DetailActivity.DETAIL_INTENT_KEY, movie);
            mContext.startActivity(intent);
        }
    }
}

