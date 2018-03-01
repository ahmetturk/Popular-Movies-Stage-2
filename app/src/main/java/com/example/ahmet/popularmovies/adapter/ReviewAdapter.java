package com.example.ahmet.popularmovies.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ahmet.popularmovies.R;
import com.example.ahmet.popularmovies.models.Review;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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

    public ArrayList<Review> getList() {
        return (ArrayList<Review>) mList;
    }

    class ReviewAdapterViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.author_tv)
        TextView authorTv;
        @BindView(R.id.content_tv)
        TextView contentTv;
        @BindView(R.id.view_more_tv)
        TextView viewMoreTv;
        @BindView(R.id.less_iv)
        ImageView lessIv;
        @BindView(R.id.expand_iv)
        ImageView expandIv;
        boolean expanded = false;

        ReviewAdapterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @OnClick({R.id.view_more_tv, R.id.expand_iv, R.id.less_iv})
        public void expandCard() {
            int lineCount = contentTv.getLineCount();

            if (lineCount < 10) {
                return;
            }

            if (expanded) {
                expanded = false;
                contentTv.setMaxLines(10);
                lessIv.setVisibility(GONE);
                expandIv.setImageResource(R.drawable.ic_expand_more_black_24px);
                viewMoreTv.setText(R.string.view_more);
            } else {
                expanded = true;
                contentTv.setMaxLines(Integer.MAX_VALUE);
                lessIv.setVisibility(VISIBLE);
                expandIv.setImageResource(R.drawable.ic_expand_less_black_24px);
                viewMoreTv.setText(R.string.view_less);
            }
        }

    }
}
