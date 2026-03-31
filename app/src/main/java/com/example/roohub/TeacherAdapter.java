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

// The Adapter class to manage the list of teachers and display them in a RecyclerView
public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.ViewHolder> {

    private final List<TeacherDataStore.Teacher> teacherList;
    private final Context context;

    // Constructor to initialize the teacher list and the context of the calling Activity
    public TeacherAdapter(List<TeacherDataStore.Teacher> teacherList, Context context) {
        this.teacherList = teacherList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the teacher_item.xml layout for each individual row
        View view = LayoutInflater.from(context).inflate(R.layout.teacher_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the specific teacher object for the current position
        TeacherDataStore.Teacher teacher = teacherList.get(position);

        if (teacher != null) {
            // Set text data from the teacher object to the UI components
            holder.name.setText(teacher.name);
            holder.qual.setText(teacher.qualify);
            holder.email.setText(teacher.email);

            /* CRITICAL UPDATE: Using try-catch to prevent "SecurityException" crashes.
               Android often restricts permission to read image URIs across different activities.
            */
            try {
                if (teacher.imageUri != null && !teacher.imageUri.isEmpty()) {
                    // Try to load the image using the saved URI
                    holder.image.setImageURI(Uri.parse(teacher.imageUri));
                } else {
                    // If no URI exists, show a default background image
                    holder.image.setImageResource(R.drawable.ic_launcher_background);
                }
            } catch (SecurityException e) {
                // If permission to read the image is denied, show the default image instead of crashing
                holder.image.setImageResource(R.drawable.ic_launcher_background);
                e.printStackTrace();
            }

            // Click listener to navigate to the detailed view of the instructor
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, TeacherDetailActivity.class);
                // Pass the teacher's data to the Detail page using Extras
                intent.putExtra("teacher_name", teacher.name);
                intent.putExtra("teacher_email", teacher.email);
                intent.putExtra("teacher_image", teacher.imageUri);
                intent.putExtra("teacher_qual", teacher.qualify);
                intent.putExtra("art_category", teacher.artType);
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        // Return the total number of teachers in the list
        return teacherList.size();
    }

    // ViewHolder class to map and hold references to the views in teacher_item.xml
    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView image;
        TextView name, qual, email;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Matching the view IDs defined in the teacher_item.xml file
            image = itemView.findViewById(R.id.itemImage);
            name = itemView.findViewById(R.id.itemName);
            qual = itemView.findViewById(R.id.itemQual);
            email = itemView.findViewById(R.id.itemEmail);
        }
    }
}