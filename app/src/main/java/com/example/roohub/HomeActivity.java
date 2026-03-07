package com.example.roohub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {


    ImageView notification, logout;
    ImageButton upload;
    LinearLayout profile, course;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);


        notification = findViewById(R.id.btnNotification);
        logout = findViewById(R.id.btnLogout);
        upload = findViewById(R.id.btnUpload);
        profile = findViewById(R.id.btnProfile);
        course = findViewById(R.id.btnCourse);

        // Click Listeners
        notification.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, NotificationActivity.class)));

        upload.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, UploadActivity.class)));

        profile.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));

        course.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, CourseActivity.class)));

        logout.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, LoginActivity.class)));
    }
}