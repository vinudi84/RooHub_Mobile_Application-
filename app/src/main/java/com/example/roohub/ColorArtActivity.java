package com.example.roohub;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ColorArtActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VideoAdapter adapter;
    private ArrayList<VideoModel> videoList;
    private VideoView videoPlayer;
    private ImageView ivPlayButton; // Reference for the Play Button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_art);

        // 1. Initialize UI Elements
        videoPlayer = findViewById(R.id.videoPlayer);
        ivPlayButton = findViewById(R.id.ivPlayButton);
        recyclerView = findViewById(R.id.rvColorVideos);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        videoList = new ArrayList<>();

        // 2. Setup MediaController (Controls like Seekbar, Pause/Play)
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoPlayer);
        videoPlayer.setMediaController(mediaController);

        // 3. Fetch Data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("RooHubData", MODE_PRIVATE);
        String savedData = sharedPreferences.getString("Coloring art", "");

        if (!savedData.isEmpty()) {
            String[] entries = savedData.split("###");
            for (String entry : entries) {
                if (!entry.isEmpty()) {
                    String[] parts = entry.split("\\|");
                    if (parts.length >= 3) {
                        videoList.add(new VideoModel(parts[0], parts[1], parts[2]));
                    }
                }
            }
        }

        // 4. Load the first video preview if list is not empty
        if (!videoList.isEmpty()) {
            loadVideoPreview(videoList.get(0).getVideoUri());
        }

        // 5. Play Button Click Logic
        ivPlayButton.setOnClickListener(v -> {
            videoPlayer.start();
            ivPlayButton.setVisibility(View.GONE); // Hide play button when video starts
        });

        // 6. Initialize Adapter with item click listener
        adapter = new VideoAdapter(videoList, this, model -> {
            // When a new video is selected from the list
            loadVideoPreview(model.getVideoUri());
        });

        recyclerView.setAdapter(adapter);

        // Show play button again when video ends
        videoPlayer.setOnCompletionListener(mp -> ivPlayButton.setVisibility(View.VISIBLE));
    }

    // Helper method to load a video without starting it immediately
    private void loadVideoPreview(String uriString) {
        Uri videoUri = Uri.parse(uriString);
        videoPlayer.setVideoURI(videoUri);
        videoPlayer.seekTo(1); // Shows the first frame (Thumbnail effect)
        ivPlayButton.setVisibility(View.VISIBLE); // Show play button for the new video
    }
}