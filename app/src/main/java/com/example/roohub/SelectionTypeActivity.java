package com.example.roohub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SelectionTypeActivity extends AppCompatActivity {


    private Button btnTeacher, btnArtist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_selection_type);


        btnTeacher = findViewById(R.id.btnTeacher);
        btnArtist = findViewById(R.id.btnArtist);


        if (btnTeacher != null) {
            btnTeacher.setOnClickListener(v -> {


                Intent intent = new Intent(SelectionTypeActivity.this, RegisterActivity.class);
                startActivity(intent);
            });
        }


        if (btnArtist != null) {
            btnArtist.setOnClickListener(v -> {
                // UploadActivity ව
                Intent intent = new Intent(SelectionTypeActivity.this, UploadActivity.class);
                startActivity(intent);
            });
        }
    }
}