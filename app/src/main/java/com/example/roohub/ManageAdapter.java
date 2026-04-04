package com.example.roohub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ManageAdapter extends RecyclerView.Adapter<ManageAdapter.ViewHolder> {

    private List<String> myVideos;
    private Context context;
    private OnActionClickListener listener;

    // Interface to handle clicks in the Activity
    public interface OnActionClickListener {
        void onDelete(int position);
        // Updated: Pass the full record string to the edit method
        void onEdit(int position, String fullRecord);
    }

    public ManageAdapter(List<String> myVideos, Context context, OnActionClickListener listener) {
        this.myVideos = myVideos;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.manage_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String record = myVideos.get(position);
        // Format: Name|ArtName|Desc|VideoUri|Email|Image|Category
        String[] d = record.split("\\|");

        if (d.length >= 2) {
            holder.artName.setText(d[1]); // Display Art Title
            // Display category name (stored at index 6)
            holder.category.setText("In: " + (d.length > 6 ? d[6] : "Art"));
        }

        // Delete button logic
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(position));

        // Edit button logic - passing the full record string
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(position, record));
    }

    @Override
    public int getItemCount() { return myVideos.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView artName, category;
        ImageButton btnEdit, btnDelete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            artName = itemView.findViewById(R.id.manageArtName);
            category = itemView.findViewById(R.id.manageCategory);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}