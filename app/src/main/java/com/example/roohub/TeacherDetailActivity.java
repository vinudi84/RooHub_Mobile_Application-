package com.example.roohub;

import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class TeacherDetailActivity extends AppCompatActivity {

    private PlayerView playerView;
    private ExoPlayer player;
    private CircleImageView teacherImage;
    private TextView teacherName, artName, description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize views
        playerView = findViewById(R.id.playerView);
        teacherImage = findViewById(R.id.teacherImage);
        teacherName = findViewById(R.id.teacherName);
        artName = findViewById(R.id.artName);
        description = findViewById(R.id.description);

        // Get data from Intent
        String name = getIntent().getStringExtra("teacher_name");
        String art = getIntent().getStringExtra("art_name");
        String desc = getIntent().getStringExtra("description");
        String videoUri = getIntent().getStringExtra("video_uri");
        String imageUri = getIntent().getStringExtra("teacher_image");

        // Set teacher info
        teacherName.setText("Teacher: " + name);
        artName.setText("Art: " + art);
        description.setText("Description: " + desc);

        if (imageUri != null && !imageUri.isEmpty()) {
            teacherImage.setImageURI(Uri.parse(imageUri));
        } else {
            teacherImage.setImageResource(R.drawable.ic_launcher_background);
        }

        // Initialize ExoPlayer
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        MediaItem mediaItem = MediaItem.fromUri(videoUri);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}