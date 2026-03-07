package com.example.roohub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtFullName, txtBio;
    private ImageView displayProfileImage;
    private Button btnEditProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        txtFullName = findViewById(R.id.txtFullName);
        txtBio = findViewById(R.id.txtBio);
        displayProfileImage = findViewById(R.id.displayProfileImage);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String name = extras.getString("NAME");
            String bio = extras.getString("BIO");
            String imageUriString = extras.getString("IMAGE_URI");

            if (name != null) txtFullName.setText(name);
            if (bio != null) txtBio.setText(bio);
            if (imageUriString != null) {
                displayProfileImage.setImageURI(Uri.parse(imageUriString));
            }
        }

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });
    }
}