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

import com.bumptech.glide.Glide; // මේක අනිවාර්යයෙන් තියෙන්න ඕනේ

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

        // ActionBar එක hide කරන්න (අවශ්‍ය නම් පමණක්)
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

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
        String uriString = prefs.getString("last_uploaded_image", null);
        String name = prefs.getString("last_art_name", "No Name");
        String type = prefs.getString("last_art_type", "No Type");
        String desc = prefs.getString("last_art_description", "No Description");

        // මෙතනදී Glide පාවිච්චි කිරීමෙන් Crash වීම නවතී
        if (uriString != null) {
            Uri imageUri = Uri.parse(uriString);
            Glide.with(this)
                    .load(imageUri)
                    .placeholder(android.R.drawable.ic_menu_gallery) // load වෙනකන් පෙන්වන රූපය
                    .into(homeArtImage);
        }

        homeArtName.setText(name);
        homeArtType.setText(type);
        homeArtDescription.setText(desc);

        // Navigation
        notification.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, NotificationActivity.class)));

        upload.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, UploadActivity.class);
            startActivity(intent);
        });

        profile.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
        course.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, CourseActivity.class)));

        logout.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Login එකට ගියාම Home එක close කරන්න
        });
    }
}


