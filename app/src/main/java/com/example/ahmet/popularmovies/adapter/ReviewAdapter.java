package com.example.ahmet.popularmovies.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ahmet.popularmovies.R;
import com.example.ahmet.popularmovies.models.Review;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewAdapterViewHolder> {

    private final Context mContext;
    private List<Review> mList;

    public ReviewAdapter(Context context) {
        this.mContext = context;
    }

    @NonNull
    @Override
    public ReviewAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_review, parent, false);
        return new ReviewAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewAdapterViewHolder holder, int position) {
        Review review = mList.get(position);

        holder.authorTv.setText(review.getAuthor());
        holder.contentTv.setText(review.getContent());
    }

    @Override
    public int getItemCount() {
        if (mList == null) {
            return 0;
        }
        return mList.size();
    }

    public void addReviewsList(List<Review> reviewsList) {
        mList = reviewsList;
        notifyItemRangeInserted(0, reviewsList.size());
    }

    class ReviewAdapterViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.author_tv)
        TextView authorTv;
        @BindView(R.id.content_tv)
        TextView contentTv;

        ReviewAdapterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }
}
