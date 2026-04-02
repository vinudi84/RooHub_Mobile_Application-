package com.example.roohub;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ManageVideosActivity extends AppCompatActivity {

    private RecyclerView rvManageVideos;
    private ManageAdapter adapter;
    private List<String> myVideosList = new ArrayList<>();
    private String teacherEmail;
    private final String[] categories = {"Pencil art", "Coloring art", "Assemblage art"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_videos);

        // Get teacher email passed from Profile Activity
        teacherEmail = getIntent().getStringExtra("teacher_email");
        rvManageVideos = findViewById(R.id.rvManageVideos);
        rvManageVideos.setLayoutManager(new LinearLayoutManager(this));

        loadMyVideos();
    }

    private void loadMyVideos() {
        myVideosList.clear();
        SharedPreferences sharedPreferences = getSharedPreferences("RooHubData", MODE_PRIVATE);

        for (String category : categories) {
            String savedData = sharedPreferences.getString(category, "");
            if (!savedData.isEmpty()) {
                String[] records = savedData.split("###");
                for (String record : records) {
                    if (record.contains(teacherEmail)) {
                        // Temporarily append category name to identify the list later
                        myVideosList.add(record + "|" + category);
                    }
                }
            }
        }

        adapter = new ManageAdapter(myVideosList, this, new ManageAdapter.OnActionClickListener() {
            @Override
            public void onDelete(int position) { performDelete(position); }

            @Override
            public void onEdit(int position, String fullRecord) {
                // Split the temporary full string to get original record and its category
                int lastPipeIndex = fullRecord.lastIndexOf("|");
                String actualData = fullRecord.substring(0, lastPipeIndex);
                String category = fullRecord.substring(lastPipeIndex + 1);

                showEditDialog(actualData, category);
            }
        });
        rvManageVideos.setAdapter(adapter);
    }

    // Displays a popup dialog to update both Title and Description
    private void showEditDialog(String oldRecord, String category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Video Details");

        // Create a layout to hold multiple EditTexts
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText etTitle = new EditText(this);
        final EditText etDesc = new EditText(this);

        // Extract current data from the record string
        String[] fields = oldRecord.split("\\|");
        if (fields.length > 2) {
            etTitle.setText(fields[1]); // Pre-fill with current title
            etDesc.setText(fields[2]);  // Pre-fill with current description
        }

        etTitle.setHint("Art Title");
        etDesc.setHint("Art Description");

        layout.addView(etTitle);
        layout.addView(etDesc);
        builder.setView(layout);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newTitle = etTitle.getText().toString().trim();
            String newDesc = etDesc.getText().toString().trim();

            if (!newTitle.isEmpty() && !newDesc.isEmpty()) {
                performUpdate(oldRecord, newTitle, newDesc, category);
            } else {
                Toast.makeText(this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // Logic to replace the old string with the new updated values
    private void performUpdate(String oldRecord, String newTitle, String newDesc, String category) {
        String[] fields = oldRecord.split("\\|");
        if (fields.length > 2) {
            fields[1] = newTitle; // Update the Title field
            fields[2] = newDesc;  // Update the Description field

            // Re-construct the pipe-separated string
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < fields.length; i++) {
                sb.append(fields[i]).append(i == fields.length - 1 ? "" : "|");
            }
            String newRecord = sb.toString();

            SharedPreferences sp = getSharedPreferences("RooHubData", MODE_PRIVATE);
            String currentData = sp.getString(category, "");

            // Replace the old record string with the new one in SharedPreferences
            String updatedData = currentData.replace(oldRecord + "###", newRecord + "###");

            sp.edit().putString(category, updatedData).apply();
            Toast.makeText(this, "Video details updated!", Toast.LENGTH_SHORT).show();
            loadMyVideos(); // Refresh RecyclerView
        }
    }

    private void performDelete(int position) {
        String fullRecord = myVideosList.get(position);
        int lastPipeIndex = fullRecord.lastIndexOf("|");
        String categoryName = fullRecord.substring(lastPipeIndex + 1);
        String actualRecord = fullRecord.substring(0, lastPipeIndex);

        SharedPreferences sharedPreferences = getSharedPreferences("RooHubData", MODE_PRIVATE);
        String currentCategoryData = sharedPreferences.getString(categoryName, "");
        String updatedData = currentCategoryData.replace(actualRecord + "###", "");

        sharedPreferences.edit().putString(categoryName, updatedData).apply();
        Toast.makeText(this, "Video deleted successfully!", Toast.LENGTH_SHORT).show();
        loadMyVideos();
    }
}