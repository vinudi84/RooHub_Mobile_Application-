package com.example.roohub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.VideoView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class VideoPlayerActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        videoView = findViewById(R.id.fullVideoView);
        ImageButton btnBack = findViewById(R.id.btnBack);

        String videoUriStr = getIntent().getStringExtra("video_uri");

        if (videoUriStr != null) {
            try {
                Uri videoUri = Uri.parse(videoUriStr);

                // IMPORTANT: Re-verify persistable permission just before playing
                try {
                    getContentResolver().takePersistableUriPermission(videoUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException e) {
                    // Log permission issue but try to play anyway
                }

                MediaController mediaController = new MediaController(this);
                mediaController.setAnchorView(videoView);
                videoView.setMediaController(mediaController);

                videoView.setVideoURI(videoUri);
                videoView.requestFocus();
                videoView.start(); // Start playback

            } catch (Exception e) {
                Toast.makeText(this, "Failed to load video: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        btnBack.setOnClickListener(v -> finish());
    }
}