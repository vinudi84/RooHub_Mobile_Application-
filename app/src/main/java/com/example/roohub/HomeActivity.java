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

        notification = findViewById(R.id.btnNotification);
        logout = findViewById(R.id.btnLogout);
        upload = findViewById(R.id.btnUpload);
        profile = findViewById(R.id.btnProfile);
        course = findViewById(R.id.btnCourse);

        homeArtImage = findViewById(R.id.home_art_image);
        homeArtName = findViewById(R.id.home_art_name);
        homeArtType = findViewById(R.id.home_art_type);
        homeArtDescription = findViewById(R.id.home_art_description);

        // Load saved art info
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String uri = prefs.getString("last_uploaded_image", null);
        String name = prefs.getString("last_art_name", "");
        String type = prefs.getString("last_art_type", "");
        String desc = prefs.getString("last_art_description", "");

        if (uri != null) homeArtImage.setImageURI(Uri.parse(uri));
        homeArtName.setText(name);
        homeArtType.setText(type);
        homeArtDescription.setText(desc);

        // Navigation
        notification.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, NotificationActivity.class)));
        upload.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, UploadActivity.class)));
        profile.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
        course.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, CourseActivity.class)));
        logout.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, LoginActivity.class)));
    }
}