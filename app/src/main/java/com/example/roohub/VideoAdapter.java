package com.example.roohub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private ArrayList<VideoModel> videoList;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(VideoModel model);
    }

    public VideoAdapter(ArrayList<VideoModel> videoList, Context context, OnItemClickListener listener) {
        this.videoList = videoList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // --- UPDATED: Using our custom card layout instead of simple_list_item_2 ---
        View view = LayoutInflater.from(context).inflate(R.layout.item_video_card, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoModel video = videoList.get(position);

        // --- UPDATED: Binding data to our new XML views ---
        holder.textViewTitle.setText(video.getTitle());
        holder.textViewPlayHint.setText("Tap to play video"); // Better UX

        // Click listener for the entire card
        holder.itemView.setOnClickListener(v -> listener.onItemClick(video));
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        // --- UPDATED: Views match the IDs in item_video_card.xml ---
        TextView textViewTitle, textViewPlayHint;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewPlayHint = itemView.findViewById(R.id.textViewPlayHint);
        }
    }
}