package com.example.roohub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import de.hdodenhof.circleimageview.CircleImageView;

public class TeacherDetailActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VideoAdapter adapter;
    private ArrayList<VideoModel> filteredVideos;
    private String teacherEmail, artCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_detail);

        // Hide action bar for full screen experience
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // --- 1. GET DATA FROM INTENT (Passed from TeacherAdapter) ---
        String teacherName = getIntent().getStringExtra("teacher_name");
        teacherEmail = getIntent().getStringExtra("teacher_email");
        artCategory = getIntent().getStringExtra("art_category");
        String teacherImage = getIntent().getStringExtra("teacher_image");
        String teacherQual = getIntent().getStringExtra("teacher_qual");

        // --- 2. SETUP UI ELEMENTS ---
        TextView tvName = findViewById(R.id.detailTeacherName);
        TextView tvQual = findViewById(R.id.detailTeacherQual);
        CircleImageView ivProfile = findViewById(R.id.detailTeacherImage);

        // Setting the teacher's profile information
        tvName.setText(teacherName != null ? teacherName : "Teacher");
        tvQual.setText(teacherQual != null ? teacherQual : "No Qualifications");

        if (teacherImage != null && !teacherImage.isEmpty()) {
            ivProfile.setImageURI(Uri.parse(teacherImage));
        }

        // --- 3. RECYCLERVIEW SETUP ---
        recyclerView = findViewById(R.id.rvTeacherVideos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        filteredVideos = new ArrayList<>();

        // Start loading videos belonging to this specific teacher
        loadTeacherVideos();
    }

    private void loadTeacherVideos() {
        SharedPreferences sharedPreferences = getSharedPreferences("RooHubData", MODE_PRIVATE);

        // Use the artCategory to find the correct list of videos
        if (artCategory != null) {
            String savedData = sharedPreferences.getString(artCategory, "");

            if (!savedData.isEmpty()) {
                String[] entries = savedData.split("###");
                for (String entry : entries) {
                    if (!entry.isEmpty()) {
                        String[] parts = entry.split("\\|");

                        // Ensure we have all necessary parts (Title, Uri, Email etc.)
                        if (parts.length >= 5) {
                            String videoTitle = parts[1];
                            String videoUri = parts[3];
                            String videoOwnerEmail = parts[4];

                            // FILTER: Only show videos where the owner email matches the selected teacher
                            if (videoOwnerEmail != null && videoOwnerEmail.equals(teacherEmail)) {
                                filteredVideos.add(new VideoModel(videoTitle, videoUri, teacherEmail));
                            }
                        }
                    }
                }
            }
        }

        // --- 4. ADAPTER SETUP ---
        adapter = new VideoAdapter(filteredVideos, this, model -> {
            playVideo(model.getVideoUri());
        });
        recyclerView.setAdapter(adapter);
    }

    private void playVideo(String uriString) {
        if (uriString == null || uriString.isEmpty()) {
            Toast.makeText(this, "Video link is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri videoUri = Uri.parse(uriString);

            // Gaining persistable URI permission to fix "Permission Denied" errors
            // Note: This only works if the URI was picked via SAF (Storage Access Framework)
            try {
                getContentResolver().takePersistableUriPermission(videoUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException se) {
                // Ignore if we can't get persistable permission, just try to play
            }

            // Move to VideoPlayerActivity
            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra("video_uri", uriString);
            startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}