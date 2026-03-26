package com.example.roohub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class CourseActivity extends AppCompatActivity {


    private Button colorArt, penArt, pencilArt, animation, animal, natural;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }


        colorArt = findViewById(R.id.btnColorArt);
        penArt = findViewById(R.id.btnPenArt);
        pencilArt = findViewById(R.id.btnPencilArt);
        animation = findViewById(R.id.btnAnimation);
        animal = findViewById(R.id.btnAnimal);
        natural = findViewById(R.id.btnNatural);

        // --- Click Listeners ---


        colorArt.setOnClickListener(v -> {
            Intent intent = new Intent(CourseActivity.this, ColorArtActivity.class);
            startActivity(intent);
        });


        penArt.setOnClickListener(v -> {
            Intent intent = new Intent(CourseActivity.this, PenArtActivity.class);
            startActivity(intent);
        });


        pencilArt.setOnClickListener(v -> {
            Intent intent = new Intent(CourseActivity.this, PencilArtActivity.class);
            startActivity(intent);
        });


        animation.setOnClickListener(v -> {
            Intent intent = new Intent(CourseActivity.this, AnimationActivity.class);
            startActivity(intent);
        });

        animal.setOnClickListener(v -> {
            Intent intent = new Intent(CourseActivity.this, AnimalArtActivity.class);
            startActivity(intent);
        });

        natural.setOnClickListener(v -> {
            Intent intent = new Intent(CourseActivity.this, NaturalArtActivity.class);
            startActivity(intent);
        });
    }
}