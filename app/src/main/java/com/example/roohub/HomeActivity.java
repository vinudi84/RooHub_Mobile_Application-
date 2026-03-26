package com.example.roohub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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


    LinearLayout artContainer;

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

        // Error  XML = id
        artContainer = findViewById(R.id.art_list_container);

        // display old deta  function call
        loadUploadedArt();

        // Navigation Buttons
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

    private void loadUploadedArt() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String allArt = prefs.getString("all_art_data", "");

        if (!allArt.isEmpty()) {
            // "|||" post haduna ganna
            String[] artItems = allArt.split("\\|\\|\\|");

            for (String item : artItems) {
                // "###" datta wenkara haduna genima (Name, Type, Desc, Uri)
                String[] parts = item.split("###");
                if (parts.length == 4) {
                    addArtToUI(parts[0], parts[1], parts[2], parts[3]);
                }
            }
        }
    }

    private void addArtToUI(String name, String type, String desc, String uriString) {
        // item_art_card.xml
        View artView = LayoutInflater.from(this).inflate(R.layout.item_art_card, artContainer, false);

        ImageView img = artView.findViewById(R.id.home_art_image);
        TextView txtName = artView.findViewById(R.id.home_art_name);
        TextView txtType = artView.findViewById(R.id.home_art_type);
        TextView txtDesc = artView.findViewById(R.id.home_art_description);

        // insert data
        txtName.setText(name);
        txtType.setText(type);
        txtDesc.setText(desc);

        // Glide use image penwanna
        Glide.with(this).load(Uri.parse(uriString)).into(img);

        // last artContainer add card
        artContainer.addView(artView);
    }
}