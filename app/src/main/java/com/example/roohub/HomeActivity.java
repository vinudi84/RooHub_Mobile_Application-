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

    // වැදගත්: මෙන්න මේ variable එක මෙතන තියෙන්නම ඕනේ
    LinearLayout artContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // UI Elements සම්බන්ධ කිරීම
        notification = findViewById(R.id.btnNotification);
        logout = findViewById(R.id.btnLogout);
        upload = findViewById(R.id.btnUpload);
        profile = findViewById(R.id.btnProfile);
        course = findViewById(R.id.btnCourse);

        // Error එක එන තැන: XML එකේ id එකට සමාන විය යුතුයි
        artContainer = findViewById(R.id.art_list_container);

        // කලින් සේව් කරපු දත්ත පෙන්වීමට function එක call කිරීම
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
            // "|||" මගින් පෝස්ට් වෙන් කර ගැනීම
            String[] artItems = allArt.split("\\|\\|\\|");

            for (String item : artItems) {
                // "###" මගින් දත්ත වෙන් කර ගැනීම (Name, Type, Desc, Uri)
                String[] parts = item.split("###");
                if (parts.length == 4) {
                    addArtToUI(parts[0], parts[1], parts[2], parts[3]);
                }
            }
        }
    }

    private void addArtToUI(String name, String type, String desc, String uriString) {
        // item_art_card.xml එක load කරගැනීම
        View artView = LayoutInflater.from(this).inflate(R.layout.item_art_card, artContainer, false);

        ImageView img = artView.findViewById(R.id.home_art_image);
        TextView txtName = artView.findViewById(R.id.home_art_name);
        TextView txtType = artView.findViewById(R.id.home_art_type);
        TextView txtDesc = artView.findViewById(R.id.home_art_description);

        // දත්ත ඇතුළත් කිරීම
        txtName.setText(name);
        txtType.setText(type);
        txtDesc.setText(desc);

        // Glide භාවිතයෙන් image එක පෙන්වීම
        Glide.with(this).load(Uri.parse(uriString)).into(img);

        // අවසානයේ artContainer එකට මෙම card එක එකතු කිරීම
        artContainer.addView(artView);
    }
}