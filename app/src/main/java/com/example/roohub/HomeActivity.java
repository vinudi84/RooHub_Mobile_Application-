package com.example.roohub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

       
        notification = findViewById(R.id.btnNotification);
        logout = findViewById(R.id.btnLogout);
        upload = findViewById(R.id.btnUpload);
        profile = findViewById(R.id.btnProfile);
        course = findViewById(R.id.btnCourse);

        homeArtImage = findViewById(R.id.home_art_image);
        homeArtName = findViewById(R.id.home_art_name);
        homeArtType = findViewById(R.id.home_art_type);
        homeArtDescription = findViewById(R.id.home_art_description);

        
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String imageUriString = prefs.getString("last_uploaded_image", null);
        String name = prefs.getString("last_art_name", "No Name");
        String type = prefs.getString("last_art_type", "No Type");
        String desc = prefs.getString("last_art_description", "No Description");

       
        homeArtName.setText(name);
        homeArtType.setText(type);
        homeArtDescription.setText(desc);

       
        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);

            ැ
            Glide.with(this)
                    .load(imageUri)
                    .placeholder(android.R.drawable.ic_menu_gallery) // පින්තූරය load වෙනකම් පෙන්වන එකක්
                    .error(android.R.drawable.stat_notify_error)    // load වුණේ නැත්නම් පෙන්වන එකක්
                    .into(homeArtImage);
        }

        
        notification.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, NotificationActivity.class)));

        upload.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, UploadActivity.class);
            startActivity(intent);
        });

        profile.setOnClickListener(v -> {
            
            startActivity(new Intent(HomeActivity.this, EditProfileActivity.class));
        });

        course.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, CourseActivity.class)));

        logout.setOnClickListener(v -> {
           
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
