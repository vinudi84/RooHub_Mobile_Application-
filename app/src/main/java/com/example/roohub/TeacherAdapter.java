package com.example.roohub;

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

    public TeacherAdapter(List<TeacherDataStore.Teacher> teacherList) {
        this.teacherList = teacherList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.teacher_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TeacherDataStore.Teacher teacher = teacherList.get(position);
        holder.name.setText(teacher.name);
        holder.qual.setText(teacher.description);

        if (teacher.imageUri != null) {
            holder.image.setImageURI(Uri.parse(teacher.imageUri));
        }
    }

    @Override
    public int getItemCount() {
        return teacherList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView image;
        TextView name, qual;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.itemImage);
            name = itemView.findViewById(R.id.itemName);
            qual = itemView.findViewById(R.id.itemQual);
        }
    }
}