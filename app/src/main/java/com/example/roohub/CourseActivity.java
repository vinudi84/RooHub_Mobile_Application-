package com.example.roohub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

/**
 * CourseActivity - Displays available courses
 * When a course is clicked, opens CourseFormActivity for upload
 */
public class CourseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        Button btnPencilArt = findViewById(R.id.btnPencilArt);
        Button btnColorArt = findViewById(R.id.btnColorArt);
        Button btnAssemblageArt = findViewById(R.id.btnAssemblageArt);

        // Pencil Art Button Click - Open form to upload Pencil Art course
        btnPencilArt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CourseActivity.this, PencilArtActivity.class);
                startActivity(intent);
            }
        });

        // Coloring Button Click - Open form to upload Coloring course
        btnColorArt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CourseActivity.this, ColorArtActivity.class);
                startActivity(intent);
            }
        });

        // Assemblage Art Button Click - Open form to upload Assemblage Art course
        btnAssemblageArt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CourseActivity.this, AssemblageArtActivity.class);
                startActivity(intent);
            }
        });
    }
}