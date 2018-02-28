package com.example.ahmet.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ahmet.popularmovies.models.VideoInfo;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoAdapterViewHolder> {

    private final Context mContext;
    private List<VideoInfo> mList;

    VideoAdapter(Context context) {
        this.mContext = context;
    }

    @NonNull
    @Override
    public VideoAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_video, parent, false);
        return new VideoAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoAdapterViewHolder holder, int position) {
        VideoInfo videoInfo = mList.get(position);

        holder.nameTv.setText(videoInfo.getVideoName());

        String photoUrl = String.format("https://img.youtube.com/vi/%s/0.jpg", videoInfo.getVideoUrl());
        Picasso.with(mContext)
                .load(photoUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(holder.videoIv);
    }

    @Override
    public int getItemCount() {
        if (mList == null) {
            return 0;
        }
        return mList.size();
    }

    void addVideosList(List<VideoInfo> videosList) {
        mList = videosList;
        notifyItemRangeInserted(0, videosList.size());
    }

    class VideoAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.video_iv)
        ImageView videoIv;
        @BindView(R.id.name_tv)
        TextView nameTv;

        VideoAdapterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            VideoInfo videoInfo = mList.get(getAdapterPosition());

            Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoInfo.getVideoUrl()));
            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/watch?v=" + videoInfo.getVideoUrl()));
            try {
                mContext.startActivity(appIntent);
            } catch (ActivityNotFoundException ex) {
                mContext.startActivity(webIntent);
            }
        }
    }
}
