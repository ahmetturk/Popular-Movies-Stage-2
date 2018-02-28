package com.example.ahmet.popularmovies.utils;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.ahmet.popularmovies.R;

public class VideoItemDecoration extends RecyclerView.ItemDecoration {

    private final Context mContext;

    public VideoItemDecoration(Context context) {
        this.mContext = context;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
        if (position == RecyclerView.NO_POSITION) {
            outRect.set(0, 0, 0, 0);
            return;
        }

        int itemDivider = mContext.getResources().getDimensionPixelSize(R.dimen.video_item_divider);
        int itemDividerSmall = mContext.getResources().getDimensionPixelSize(R.dimen.video_item_divider_small);

        outRect.top = itemDividerSmall;
        outRect.bottom = itemDividerSmall;

        if (position == 0) {
            outRect.left = itemDivider;
            outRect.right = itemDividerSmall;
        } else if (position == state.getItemCount() - 1) {
            outRect.left = itemDividerSmall;
            outRect.right = itemDivider;
        } else {
            outRect.left = itemDividerSmall;
            outRect.right = itemDividerSmall;
        }
    }
}
