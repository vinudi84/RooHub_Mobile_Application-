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

    // Define UI elements
    ImageView signIn, logout;
    ImageButton upload;
    LinearLayout profile, course;
    LinearLayout artContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize UI elements from XML
        signIn = findViewById(R.id.btnSignIn);
        logout = findViewById(R.id.btnLogout);
        upload = findViewById(R.id.btnUpload);
        profile = findViewById(R.id.btnProfile);
        course = findViewById(R.id.btnCourse);
        artContainer = findViewById(R.id.art_list_container);

        // Go to User's own Profile (Normal Mode)
        profile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        // Go to Upload Screen
        upload.setOnClickListener(v -> startActivity(new Intent(this, UploadActivity.class)));

        // Go to Courses Screen
        course.setOnClickListener(v -> startActivity(new Intent(this, CourseActivity.class)));

        // Go to Selection screen for Sign In
        signIn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SelectionTypeActivity.class);
            startActivity(intent);
        });

        // Logout and clear activity stack
        logout.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the art list every time the user comes back to Home
        loadUploadedArt();
    }

    // Load all uploaded art data from SharedPreferences
    private void loadUploadedArt() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String allArt = prefs.getString("all_art_data", "");

        artContainer.removeAllViews(); // Clear existing views before loading

        if (!allArt.isEmpty()) {
            // Split data by the separator
            String[] artItems = allArt.split("\\|\\|\\|");

            for (String item : artItems) {
                String[] parts = item.split("###");

                // Data format: Email###Pass###Name###Desc###ArtUri###ProfileUri
                if (parts.length >= 5) {
                    String uEmail = parts[0];   // Artist's Email
                    String aName = parts[2];    // Art Name
                    String aDesc = parts[3];    // Art Description
                    String aUri = parts[4];     // Art Image URI

                    addArtToUI(aName, uEmail, aDesc, aUri);
                }
            }
        }
    }

    // Create a card for each art and add it to the Home screen
    private void addArtToUI(String name, String ownerEmail, String desc, String uriString) {
        // Inflate the custom card layout
        View artView = LayoutInflater.from(this).inflate(R.layout.item_art_card, artContainer, false);

        ImageView img = artView.findViewById(R.id.home_art_image);
        TextView txtName = artView.findViewById(R.id.home_art_name);
        TextView txtType = artView.findViewById(R.id.home_art_type);
        TextView txtDesc = artView.findViewById(R.id.home_art_description);

        txtName.setText(name);
        txtType.setText("Artist: " + ownerEmail);
        txtDesc.setText(desc);

        // Load image using Glide library
        Glide.with(this)
                .load(Uri.parse(uriString))
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(img);

        // IMPORTANT: When an image is clicked, open Profile in VIEW_ONLY mode
        artView.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, UserProfileActivity.class);
            intent.putExtra("LOGGED_EMAIL", ownerEmail); // Pass artist email
            intent.putExtra("MODE", "VIEW_ONLY");        // Tell Profile screen this is a visitor
            startActivity(intent);
        });

        artContainer.addView(artView); // Add the card to the container
    }
}