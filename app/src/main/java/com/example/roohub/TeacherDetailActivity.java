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

        // 1. GET DATA FROM INTENT
        String teacherName = getIntent().getStringExtra("teacher_name");
        teacherEmail = getIntent().getStringExtra("teacher_email");
        artCategory = getIntent().getStringExtra("art_category");
        String teacherImage = getIntent().getStringExtra("teacher_image");
        String teacherQual = getIntent().getStringExtra("teacher_qual");

        // 2. SETUP UI ELEMENTS
        TextView tvName = findViewById(R.id.detailTeacherName);
        TextView tvQual = findViewById(R.id.detailTeacherQual);
        CircleImageView ivProfile = findViewById(R.id.detailTeacherImage);

        tvName.setText(teacherName);
        tvQual.setText(teacherQual);

        if (teacherImage != null && !teacherImage.isEmpty()) {
            ivProfile.setImageURI(Uri.parse(teacherImage));
        }

        // 3. RECYCLERVIEW SETUP
        recyclerView = findViewById(R.id.rvTeacherVideos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        filteredVideos = new ArrayList<>();

        loadTeacherVideos();
    }

    private void loadTeacherVideos() {
        SharedPreferences sharedPreferences = getSharedPreferences("RooHubData", MODE_PRIVATE);
        String savedData = sharedPreferences.getString(artCategory, "");

        if (!savedData.isEmpty()) {
            String[] entries = savedData.split("###");
            for (String entry : entries) {
                if (!entry.isEmpty()) {
                    String[] parts = entry.split("\\|");

                    // MATCHING INDICES WITH VIEWUPLOADACTIVITY
                    if (parts.length >= 5) {
                        String videoTitle = parts[1];      // Video Title
                        String videoUri = parts[3];        // Video URI string
                        String videoOwnerEmail = parts[4]; // Unique Email of the teacher

                        // FILTERING LOGIC
                        if (videoOwnerEmail.equals(teacherEmail)) {
                            // Ensure VideoModel constructor matches: (Title, URI, Email)
                            filteredVideos.add(new VideoModel(videoTitle, videoUri, teacherEmail));
                        }
                    }
                }
            }
        }

        // 4. ADAPTER SETUP
        adapter = new VideoAdapter(filteredVideos, this, model -> {
            playVideo(model.getVideoUri()); // Passing URI string to playVideo method
        });
        recyclerView.setAdapter(adapter);
    }

    private void playVideo(String uriString) {
        try {
            Uri videoUri = Uri.parse(uriString);

            // CRITICAL: Gain persistable URI permission to fix "Permission Denied"
            getContentResolver().takePersistableUriPermission(videoUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Start Player Activity
            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra("video_uri", uriString);
            startActivity(intent);

        } catch (SecurityException e) {
            // This happens if the URI wasn't selected using the ACTION_OPEN_DOCUMENT picker
            Toast.makeText(this, "Permission denied for this video", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}