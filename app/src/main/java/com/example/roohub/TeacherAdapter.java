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

import com.google.android.material.imageview.ShapeableImageView;

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
        View view = LayoutInflater.from(context).inflate(R.layout.teacher_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String record = videoDataList.get(position);

        if (record == null || record.isEmpty()) return;

        // ── Format: Name|ArtName|Desc|VideoUri|Email|ProfileImageUri ─────────
        String[] details = record.split("\\|", -1); // -1 keeps empty trailing fields

        String teacherName   = details.length > 0 ? details[0] : "Unknown";
        String artName       = details.length > 1 ? details[1] : "";
        String description   = details.length > 2 ? details[2] : "";
        String videoUri      = details.length > 3 ? details[3] : "";
        String teacherEmail  = details.length > 4 ? details[4] : "";
        String profileImgUri = details.length > 5 ? details[5] : "";

        android.util.Log.d("ADAPTER", "Binding position " + position);
        android.util.Log.d("ADAPTER", "Teacher: " + teacherName);
        android.util.Log.d("ADAPTER", "Video: "   + videoUri);

        holder.name.setText("Teacher: " + teacherName);
        holder.qual.setText("Art: "     + artName);
        holder.email.setText("Desc: "   + description);

        // ── Load profile image ───────────────────────────────────────────────
        if (!profileImgUri.isEmpty()) {
            try {
                // Try as URL first (Supabase storage URL)
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

        // ── Release old player if any ────────────────────────────────────────
        if (holder.player != null) {
            holder.player.release();
            holder.player = null;
        }

        // ── Setup ExoPlayer for video ────────────────────────────────────────
        if (!videoUri.isEmpty()) {
            try {
                ExoPlayer player = new ExoPlayer.Builder(context).build();
                holder.player = player;
                holder.playerView.setPlayer(player);

                MediaItem mediaItem = MediaItem.fromUri(videoUri);
                player.setMediaItem(mediaItem);
                player.prepare();
                // ✅ Don't autoplay — let user press play
                player.setPlayWhenReady(false);

                android.util.Log.d("ADAPTER", "Player set up for: " + videoUri);
            } catch (Exception e) {
                android.util.Log.e("ADAPTER", "Player error: " + e.getMessage());
            }
        } else {
            holder.playerView.setVisibility(View.GONE);
            android.util.Log.w("ADAPTER", "Empty video URI at position " + position);
        }

        // ── Click to open detail screen ──────────────────────────────────────
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

    // ── Release player when view is recycled ─────────────────────────────────
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView image;
        TextView name, qual, email;
        PlayerView playerView;
        ExoPlayer player; // ✅ keep reference for proper release

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