package com.example.roohub;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.ViewHolder> {

    private final List<String> videoDataList;
    private final Context context;

    public TeacherAdapter(List<String> videoDataList, Context context) {
        this.videoDataList = videoDataList;
        this.context = context;
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

        if (record != null && !record.isEmpty()) {
            // New Format: Name|ArtName|Desc|VideoUri|Email|ProfileImageUri
            String[] details = record.split("\\|");

            if (details.length >= 6) {
                String teacherName = details[0];
                String artName = details[1];
                String description = details[2];
                String videoUri = details[3];
                String teacherEmail = details[4];
                String profileImageUri = details[5]; // Extracting image URI

                holder.name.setText("Teacher: " + teacherName);
                holder.qual.setText("Art: " + artName);
                holder.email.setText("Desc: " + description);

                // --- SHOW TEACHER PROFILE IMAGE ---
                if (profileImageUri != null && !profileImageUri.isEmpty()) {
                    holder.image.setImageURI(Uri.parse(profileImageUri));
                } else {
                    holder.image.setImageResource(R.drawable.ic_launcher_background);
                }

                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(context, TeacherDetailActivity.class);
                    intent.putExtra("teacher_name", teacherName);
                    intent.putExtra("art_name", artName);
                    intent.putExtra("description", description);
                    intent.putExtra("video_uri", videoUri);
                    intent.putExtra("teacher_email", teacherEmail);
                    intent.putExtra("teacher_image", profileImageUri); // Passing image URI
                    context.startActivity(intent);
                });
            }
        }
    }

    @Override
    public int getItemCount() { return videoDataList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView image;
        TextView name, qual, email;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.itemImage);
            name = itemView.findViewById(R.id.itemName);
            qual = itemView.findViewById(R.id.itemQual);
            email = itemView.findViewById(R.id.itemEmail);
        }
    }
}