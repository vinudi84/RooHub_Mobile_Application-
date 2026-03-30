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

    private final List<TeacherDataStore.Teacher> teacherList;
    private final Context context; // Added Context to handle the intent and layout inflation

    // --- UPDATED CONSTRUCTOR: Now accepts both List and Context ---
    public TeacherAdapter(List<TeacherDataStore.Teacher> teacherList, Context context) {
        this.teacherList = teacherList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the teacher_item.xml layout using the provided context
        View view = LayoutInflater.from(context).inflate(R.layout.teacher_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TeacherDataStore.Teacher teacher = teacherList.get(position);

        // Binding data to UI elements from the teacher object
        holder.name.setText(teacher.name);
        holder.qual.setText(teacher.qualify);
        holder.email.setText(teacher.email);

        // Handle profile image loading with basic URI parsing
        if (teacher.imageUri != null && !teacher.imageUri.isEmpty()) {
            holder.image.setImageURI(Uri.parse(teacher.imageUri));
        } else {
            // Placeholder image if profile photo is missing
            holder.image.setImageResource(R.drawable.ic_launcher_background);
        }

        // Set up click listener to navigate to TeacherDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TeacherDetailActivity.class);

            // Pass teacher details to the detail activity for filtering videos later
            intent.putExtra("teacher_name", teacher.name);
            intent.putExtra("teacher_email", teacher.email);
            intent.putExtra("teacher_image", teacher.imageUri);
            intent.putExtra("teacher_qual", teacher.qualify);
            intent.putExtra("art_category", teacher.artType);

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return teacherList.size();
    }

    // ViewHolder class to hold references to the views in teacher_item.xml
    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView image;
        TextView name, qual, email;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Linking XML IDs with Java objects
            image = itemView.findViewById(R.id.itemImage);
            name = itemView.findViewById(R.id.itemName);
            qual = itemView.findViewById(R.id.itemQual);
            email = itemView.findViewById(R.id.itemEmail);
        }
    }
}