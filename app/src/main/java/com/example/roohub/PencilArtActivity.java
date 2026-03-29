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

public class PencilArtActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VideoAdapter adapter;
    private ArrayList<VideoModel> videoList;
    private VideoView videoPlayer;
    private ImageView ivPlayButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pencil_art);

        videoPlayer = findViewById(R.id.videoPlayerPencil);
        ivPlayButton = findViewById(R.id.ivPlayButtonPencil);
        recyclerView = findViewById(R.id.rvPencilVideos);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        videoList = new ArrayList<>();

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoPlayer);
        videoPlayer.setMediaController(mediaController);

        // Fetch Data using "Pencil art" key
        SharedPreferences sharedPreferences = getSharedPreferences("RooHubData", MODE_PRIVATE);
        String savedData = sharedPreferences.getString("Pencil art", "");

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

        if (!videoList.isEmpty()) {
            loadVideoPreview(videoList.get(0).getVideoUri());
        }

        ivPlayButton.setOnClickListener(v -> {
            videoPlayer.start();
            ivPlayButton.setVisibility(View.GONE);
        });

        adapter = new VideoAdapter(videoList, this, model -> {
            loadVideoPreview(model.getVideoUri());
        });

        recyclerView.setAdapter(adapter);
        videoPlayer.setOnCompletionListener(mp -> ivPlayButton.setVisibility(View.VISIBLE));
    }

    private void loadVideoPreview(String uriString) {
        Uri videoUri = Uri.parse(uriString);
        videoPlayer.setVideoURI(videoUri);
        videoPlayer.seekTo(1);
        ivPlayButton.setVisibility(View.VISIBLE);
    }
}