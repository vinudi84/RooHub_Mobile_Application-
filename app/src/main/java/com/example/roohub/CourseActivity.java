package com.example.roohub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class CourseActivity extends AppCompatActivity {

    // Button variables ප්‍රකාශ කිරීම
    private Button colorArt, penArt, pencilArt, animation, animal, natural;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        // Action Bar එක hide කිරීම
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // XML එකේ ඇති ID සමඟ සම්බන්ධ කිරීම
        colorArt = findViewById(R.id.btnColorArt);
        penArt = findViewById(R.id.btnPenArt);
        pencilArt = findViewById(R.id.btnPencilArt);
        animation = findViewById(R.id.btnAnimation);
        animal = findViewById(R.id.btnAnimal);
        natural = findViewById(R.id.btnNatural);

        // --- Click Listeners ---

        // Color Art පිටුවට යාම
        colorArt.setOnClickListener(v -> {
            Intent intent = new Intent(CourseActivity.this, ColorArtActivity.class);
            startActivity(intent);
        });

        // Pen Art පිටුවට යාම
        penArt.setOnClickListener(v -> {
            Intent intent = new Intent(CourseActivity.this, PenArtActivity.class);
            startActivity(intent);
        });

        // Pencil Art පිටුවට යාම
        pencilArt.setOnClickListener(v -> {
            Intent intent = new Intent(CourseActivity.this, PencilArtActivity.class);
            startActivity(intent);
        });

        // Animation පිටුවට යාම
        animation.setOnClickListener(v -> {
            Intent intent = new Intent(CourseActivity.this, AnimationActivity.class);
            startActivity(intent);
        });

        // Animal Art පිටුවට යාම
        animal.setOnClickListener(v -> {
            Intent intent = new Intent(CourseActivity.this, AnimalArtActivity.class);
            startActivity(intent);
        });

        // Natural Art පිටුවට යාම
        natural.setOnClickListener(v -> {
            Intent intent = new Intent(CourseActivity.this, NaturalArtActivity.class);
            startActivity(intent);
        });
    }
}