package com.example.roohub;

import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // --- 1. GET DATA FROM INTENT ---
        String teacherName = getIntent().getStringExtra("teacher_name");
        String teacherEmail = getIntent().getStringExtra("teacher_email");
        String artName = getIntent().getStringExtra("art_name");
        String description = getIntent().getStringExtra("description");
        String videoUri = getIntent().getStringExtra("video_uri");
        String teacherImage = getIntent().getStringExtra("teacher_image"); // New Image URI

        // --- 2. SETUP UI ELEMENTS ---
        TextView tvName = findViewById(R.id.detailTeacherName);
        TextView tvQual = findViewById(R.id.detailTeacherQual);
        CircleImageView ivProfile = findViewById(R.id.detailTeacherImage);

        tvName.setText(teacherName != null ? teacherName : "Unknown Instructor");
        tvQual.setText(teacherEmail != null ? teacherEmail : "No Contact Info");

        // --- LOAD PROFILE IMAGE ---
        if (teacherImage != null && !teacherImage.isEmpty()) {
            ivProfile.setImageURI(Uri.parse(teacherImage));
        } else {
            ivProfile.setImageResource(R.drawable.ic_launcher_background);
        }

        recyclerView = findViewById(R.id.rvTeacherVideos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        filteredVideos = new ArrayList<>();

        if (videoUri != null && !videoUri.isEmpty()) {
            filteredVideos.add(new VideoModel(artName, videoUri, teacherEmail));
        }

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
            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra("video_uri", uriString);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error playing video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}