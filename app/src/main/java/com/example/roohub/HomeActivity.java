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


    ImageView signIn, logout;
    ImageButton upload;
    LinearLayout profile, course;
    LinearLayout artContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // UI Elements connect

        signIn = findViewById(R.id.btnSignIn);
        logout = findViewById(R.id.btnLogout);
        upload = findViewById(R.id.btnUpload);
        profile = findViewById(R.id.btnProfile);
        course = findViewById(R.id.btnCourse);

        artContainer = findViewById(R.id.art_list_container);

        // call function
        loadUploadedArt();

        // Navigation Buttons
        profile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        upload.setOnClickListener(v -> startActivity(new Intent(this, UploadActivity.class)));
        course.setOnClickListener(v -> startActivity(new Intent(this, CourseActivity.class)));

        // when click sing_in go to selection_type
        signIn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SelectionTypeActivity.class);
            startActivity(intent);
        });


        // Logout logic
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
            // "|||" identify tha post
            String[] artItems = allArt.split("\\|\\|\\|");

            for (String item : artItems) {
                // "###"  (Name, Type, Desc, Uri) are identify
                String[] parts = item.split("###");
                if (parts.length == 4) {
                    addArtToUI(parts[0], parts[1], parts[2], parts[3]);
                }
            }
        }
    }

    private void addArtToUI(String name, String type, String desc, String uriString) {
        // item_art_card.xml  = inflate
        View artView = LayoutInflater.from(this).inflate(R.layout.item_art_card, artContainer, false);

        ImageView img = artView.findViewById(R.id.home_art_image);
        TextView txtName = artView.findViewById(R.id.home_art_name);
        TextView txtType = artView.findViewById(R.id.home_art_type);
        TextView txtDesc = artView.findViewById(R.id.home_art_description);

        // include data
        txtName.setText(name);
        txtType.setText(type);
        txtDesc.setText(desc);

        // Glide use for pic
        Glide.with(this).load(Uri.parse(uriString)).into(img);

        // lastly artContainer is card added
        artContainer.addView(artView);
    }
}