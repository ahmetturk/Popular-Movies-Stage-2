package com.ahmetroid.popularmovies.utils;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ahmetroid.popularmovies.R;

public class HorizontalItemDecoration extends RecyclerView.ItemDecoration {

    private final Context mContext;

    public HorizontalItemDecoration(Context context) {
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