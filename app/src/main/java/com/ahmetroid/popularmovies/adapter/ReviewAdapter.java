package com.ahmetroid.popularmovies.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ahmetroid.popularmovies.R;
import com.ahmetroid.popularmovies.databinding.ItemReviewBinding;
import com.ahmetroid.popularmovies.model.Review;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewAdapterViewHolder> {

    private final Context mContext;
    private List<Review> mList;

    public ReviewAdapter(Context context) {
        this.mContext = context;
    }

    @NonNull
    @Override
    public ReviewAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
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
        boolean expanded;

        public ReviewAdapterViewHolder(ItemReviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.expanded = false;
        }

        public void bind(Review review) {
            binding.setReview(review);
            binding.setPresenter(this);
        }

        public void onClickExpand() {
            int lineCount = binding.contentTv.getLineCount();

            if (lineCount < 10) {
                return;
            }

            if (expanded) {
                expanded = false;
                binding.contentTv.setMaxLines(10);
                binding.lessIv.setVisibility(GONE);
                binding.expandIv.setImageResource(R.drawable.ic_expand_more_black_24px);
                binding.viewMoreTv.setText(R.string.view_more);
            } else {
                expanded = true;
                binding.contentTv.setMaxLines(Integer.MAX_VALUE);
                binding.lessIv.setVisibility(VISIBLE);
                binding.expandIv.setImageResource(R.drawable.ic_expand_less_black_24px);
                binding.viewMoreTv.setText(R.string.view_less);
            }
        }
    }
}
