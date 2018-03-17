package com.ahmetroid.popularmovies.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ahmetroid.popularmovies.activity.DetailActivity;
import com.ahmetroid.popularmovies.databinding.ItemReviewBinding;
import com.ahmetroid.popularmovies.model.Review;

import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewAdapterViewHolder> {

    private final Activity mActivity;
    private List<Review> mList;

    public ReviewAdapter(Activity activity) {
        this.mActivity = activity;
    }

    @NonNull
    @Override
    public ReviewAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mActivity);
        ItemReviewBinding binding = ItemReviewBinding.inflate(layoutInflater, parent, false);
        return new ReviewAdapterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewAdapterViewHolder holder, int position) {
        Review review = mList.get(position);
        holder.bind(review);
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
        notifyDataSetChanged();
    }

    public ArrayList<Review> getList() {
        return (ArrayList<Review>) mList;
    }

    public class ReviewAdapterViewHolder extends RecyclerView.ViewHolder {

        ItemReviewBinding binding;

        public ReviewAdapterViewHolder(ItemReviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Review review) {
            binding.setReview(review);
            binding.setPresenter((DetailActivity) mActivity);
        }
    }
}