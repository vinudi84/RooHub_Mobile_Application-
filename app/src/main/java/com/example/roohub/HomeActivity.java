package com.example.roohub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class HomeActivity extends AppCompatActivity {

    ImageView notification, logout;
    ImageButton upload;
    LinearLayout profile, course;
    ImageView homeArtImage;
    TextView homeArtName, homeArtType, homeArtDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // UI Elements
        notification = findViewById(R.id.btnNotification);
        logout = findViewById(R.id.btnLogout);
        upload = findViewById(R.id.btnUpload);
        profile = findViewById(R.id.btnProfile);
        course = findViewById(R.id.btnCourse);
        homeArtImage = findViewById(R.id.home_art_image);
        homeArtName = findViewById(R.id.home_art_name);
        homeArtType = findViewById(R.id.home_art_type);
        homeArtDescription = findViewById(R.id.home_art_description);

        // Load Data
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String imageUriString = prefs.getString("last_uploaded_image", null);
        homeArtName.setText(prefs.getString("last_art_name", "No Art"));
        homeArtType.setText(prefs.getString("last_art_type", "Unknown"));
        homeArtDescription.setText(prefs.getString("last_art_description", "No description"));

        if (imageUriString != null) {
            Glide.with(this).load(Uri.parse(imageUriString)).into(homeArtImage);
        }


        profile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        upload.setOnClickListener(v -> startActivity(new Intent(this, UploadActivity.class)));
        course.setOnClickListener(v -> startActivity(new Intent(this, CourseActivity.class)));
        notification.setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));

        logout.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
