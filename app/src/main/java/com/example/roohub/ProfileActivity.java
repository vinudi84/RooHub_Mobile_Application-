package com.example.roohub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

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

        // සේව් කරපු දත්ත මෙතනදී Load කරනවා
        displayData();

        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
        });
    }

    // දත්ත පෙන්වන function එක වෙනම ලිව්වා (onResume එකේදීත් වැඩ කරන්න)
    private void displayData() {
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);

        String name = prefs.getString("NAME", "Your Name Here");
        String bio = prefs.getString("BIO", "No bio added yet.");
        String imageUriStr = prefs.getString("IMAGE_URI", null);

        txtFullName.setText(name);
        txtBio.setText(bio);

        if (imageUriStr != null) {
            // Glide පාවිච්චි කිරීමෙන් Permission Error එන්නේ නැහැ
            Glide.with(this)
                    .load(Uri.parse(imageUriStr))
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(displayProfileImage);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayData(); // Edit කරලා ආපහු එද්දී දත්ත Update වෙන්න ඕනේ නිසා
    }
}