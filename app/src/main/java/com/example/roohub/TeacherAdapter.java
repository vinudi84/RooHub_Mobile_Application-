package com.example.roohub;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.ViewHolder> {

    private final List<String> videoDataList;
    private final Context context;

    public TeacherAdapter(List<String> videoDataList, Context context) {
        this.videoDataList = videoDataList;
        this.context       = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the teacher_item layout for each row
        View view = LayoutInflater.from(context).inflate(R.layout.teacher_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String record = videoDataList.get(position);

        if (record == null || record.isEmpty()) return;

        // Split data by pipe symbol: Name|ArtName|Desc|VideoUri|Email|ProfileImageUri
        String[] details = record.split("\\|", -1);

        // --- 1. Handle Teacher Name Logic ---
        String rawName = details.length > 0 ? details[0] : "";
        String teacherName;
        if (rawName == null || rawName.trim().isEmpty() || rawName.equalsIgnoreCase("null")) {
            teacherName = "Anonymous";
        } else {
            teacherName = rawName;
        }

        // Handle Art Name Logic (NEW)
        // If the art name is null, empty, or literally "null", set it to "General Art"
        String rawArtName = details.length > 1 ? details[1] : "";
        String artName;
        if (rawArtName == null || rawArtName.trim().isEmpty() || rawArtName.equalsIgnoreCase("null")) {
            artName = "General Art";
        } else {
            artName = rawArtName;
        }

        String description   = details.length > 2 ? details[2] : "";
        String videoUri      = details.length > 3 ? details[3] : "";
        String teacherEmail  = details.length > 4 ? details[4] : "";
        String profileImgUri = details.length > 5 ? details[5] : "";

        // Set the cleaned text data to the views
        holder.name.setText("Teacher: " + teacherName);
        holder.qual.setText("Art: "     + artName);
        holder.email.setText("Desc: "   + description);

        // Load profile image using Glide (for URLs) or Uri (for local files)
        if (!profileImgUri.isEmpty()) {
            try {
                if (profileImgUri.startsWith("http")) {
                    com.bumptech.glide.Glide.with(context)
                            .load(profileImgUri)
                            .placeholder(R.drawable.ic_launcher_background)
                            .circleCrop()
                            .into(holder.image);
                } else {
                    holder.image.setImageURI(Uri.parse(profileImgUri));
                }
            } catch (Exception e) {
                holder.image.setImageResource(R.drawable.ic_launcher_background);
            }
        } else {
            holder.image.setImageResource(R.drawable.ic_launcher_background);
        }

        // Release any existing player to free up memory before creating a new one
        if (holder.player != null) {
            holder.player.release();
            holder.player = null;
        }

        // Setup ExoPlayer for video content
        if (!videoUri.isEmpty()) {
            try {
                ExoPlayer player = new ExoPlayer.Builder(context).build();
                holder.player = player;
                holder.playerView.setPlayer(player);

                MediaItem mediaItem = MediaItem.fromUri(videoUri);
                player.setMediaItem(mediaItem);
                player.prepare();
                // We keep autoplay off (false) so it doesn't slow down the list scrolling
                player.setPlayWhenReady(false);

            } catch (Exception e) {
                android.util.Log.e("ADAPTER", "Player error: " + e.getMessage());
            }
        } else {
            // Hide player view if there is no video link
            holder.playerView.setVisibility(View.GONE);
        }

        // Open Detail Activity when the whole item is clicked
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TeacherDetailActivity.class);
            intent.putExtra("teacher_name",  teacherName);
            intent.putExtra("art_name",      artName);
            intent.putExtra("description",   description);
            intent.putExtra("video_uri",     videoUri);
            intent.putExtra("teacher_email", teacherEmail);
            intent.putExtra("teacher_image", profileImgUri);
            context.startActivity(intent);
        });
    }

    // Release the player when the view is scrolled away to prevent memory leaks
    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.player != null) {
            holder.player.release();
            holder.player = null;
        }
    }

    @Override
    public int getItemCount() {
        return videoDataList.size();
    }

    // ViewHolder class to map UI elements from the XML layout
    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView image;
        TextView name, qual, email;
        PlayerView playerView;
        ExoPlayer player;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image      = itemView.findViewById(R.id.itemImage);
            name       = itemView.findViewById(R.id.itemName);
            qual       = itemView.findViewById(R.id.itemQual);
            email      = itemView.findViewById(R.id.itemEmail);
            playerView = itemView.findViewById(R.id.playerView);
        }
    }
}