package com.ahmetroid.popularmovies.adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ahmetroid.popularmovies.R;
import com.ahmetroid.popularmovies.databinding.ItemVideoBinding;
import com.ahmetroid.popularmovies.model.Video;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoAdapterViewHolder> {

    private final Context mContext;
    private List<Video> mList;

    public VideoAdapter(Context context) {
        this.mContext = context;
    }

    @NonNull
    @Override
    public VideoAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        ItemVideoBinding binding = ItemVideoBinding.inflate(layoutInflater, parent, false);
        return new VideoAdapterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoAdapterViewHolder holder, int position) {
        Video video = mList.get(position);
        holder.bind(video);
    }

    @Override
    public int getItemCount() {
        if (mList == null) {
            return 0;
        }
        return mList.size();
    }

    public void addVideosList(List<Video> videosList) {
        mList = videosList;
        notifyDataSetChanged();
    }

    public ArrayList<Video> getList() {
        return (ArrayList<Video>) mList;
    }

    public class VideoAdapterViewHolder extends RecyclerView.ViewHolder {

        public ItemVideoBinding binding;

        VideoAdapterViewHolder(ItemVideoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Video video) {
            binding.setVideo(video);
            binding.setPresenter(this);

            String photoUrl = String.format("https://img.youtube.com/vi/%s/0.jpg", video.getVideoUrl());
            Picasso.get()
                    .load(photoUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .into(binding.videoIv);
        }

        public void onClickVideo(String videoUrl) {
            Intent appIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("vnd.youtube:" + videoUrl));

            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/watch?v=" + videoUrl));
            try {
                mContext.startActivity(appIntent);
            } catch (ActivityNotFoundException ex) {
                mContext.startActivity(webIntent);
            }
        }
    }
}