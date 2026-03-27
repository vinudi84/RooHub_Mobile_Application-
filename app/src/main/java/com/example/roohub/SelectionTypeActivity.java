package com.example.roohub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SelectionTypeActivity extends AppCompatActivity {

    Button btnTeacher, btnArtist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_selection_type);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Buttons
        btnTeacher = findViewById(R.id.btnTeacher);
        btnArtist = findViewById(R.id.btnArtist);

        // Teacher Button Click Logic
        if (btnTeacher != null) {
            btnTeacher.setOnClickListener(v -> {
                Intent intent = new Intent(SelectionTypeActivity.this, teacherSignUp.class);
                startActivity(intent);
            });
        }

        // Artist Button Click Logic
        if (btnArtist != null) {
            btnArtist.setOnClickListener(v -> {
                Intent intent = new Intent(SelectionTypeActivity.this, UploadActivity.class);
                startActivity(intent);
            });
        }
    }
}