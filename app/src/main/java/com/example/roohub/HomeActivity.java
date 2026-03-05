package com.example.roohub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    // Top bar
    ImageView notificationIcon, logoutIcon;

    // Bottom navigation
    ImageView btnHome, btnCourse, btnUploadBottom, btnProfile;

    // Category buttons
    Button btnColorArt, btnPenArt, btnPencilArt, btnAnimationArt, btnAnimalArt, btnNatureArt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // ===== Top Bar =====
        notificationIcon = findViewById(R.id.btnNotification);
        logoutIcon = findViewById(R.id.btnUpload);

        // ===== Bottom Navigation =====
        btnHome = findViewById(R.id.btnHome);
        btnCourse = findViewById(R.id.btnCourse);
        btnUploadBottom = findViewById(R.id.btnUploadBottom);
        btnProfile = findViewById(R.id.btnProfile);

        // ===== Category Buttons =====
        btnColorArt = findViewById(R.id.btnColorArt);
        btnPenArt = findViewById(R.id.btnPenArt);
        btnPencilArt = findViewById(R.id.btnPencilArt);
        btnAnimationArt = findViewById(R.id.btnAnimationArt);
        btnAnimalArt = findViewById(R.id.btnAnimalArt);
        btnNatureArt = findViewById(R.id.btnNatureArt);


        // ================= TOP BAR CLICK =================

        notificationIcon.setOnClickListener(v ->
                Toast.makeText(this,"Notifications",Toast.LENGTH_SHORT).show()
        );

        logoutIcon.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });


        // ================= BOTTOM NAVIGATION =================

        btnHome.setOnClickListener(v ->
                Toast.makeText(this,"Home",Toast.LENGTH_SHORT).show()
        );

        btnCourse.setOnClickListener(v ->
                Toast.makeText(this,"Courses",Toast.LENGTH_SHORT).show()
        );

        btnUploadBottom.setOnClickListener(v ->
                Toast.makeText(this,"Upload Art",Toast.LENGTH_SHORT).show()
        );

        btnProfile.setOnClickListener(v ->
                Toast.makeText(this,"Profile",Toast.LENGTH_SHORT).show()
        );


        // ================= CATEGORY BUTTONS =================

        btnColorArt.setOnClickListener(v ->
                Toast.makeText(this,"Color Art",Toast.LENGTH_SHORT).show()
        );

        btnPenArt.setOnClickListener(v ->
                Toast.makeText(this,"Pen Art",Toast.LENGTH_SHORT).show()
        );

        btnPencilArt.setOnClickListener(v ->
                Toast.makeText(this,"Pencil Art",Toast.LENGTH_SHORT).show()
        );

        btnAnimationArt.setOnClickListener(v ->
                Toast.makeText(this,"Animation Art",Toast.LENGTH_SHORT).show()
        );

        btnAnimalArt.setOnClickListener(v ->
                Toast.makeText(this,"Animal Art",Toast.LENGTH_SHORT).show()
        );

        btnNatureArt.setOnClickListener(v ->
                Toast.makeText(this,"Nature Art",Toast.LENGTH_SHORT).show()
        );
    }
}