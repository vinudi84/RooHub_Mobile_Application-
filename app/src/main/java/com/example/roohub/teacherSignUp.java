package com.example.roohub;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class teacherSignUp extends AppCompatActivity {

    Spinner artSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teacher_sign_up);

        // Initialize Spinner
        artSpinner = findViewById(R.id.artSpinner);

        // Spinner Data
        String[] artTypes = {"Pencil", "Coloring", "Assemblage art"};

        // Adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                artTypes
        );

        // Set Adapter
        artSpinner.setAdapter(adapter);
    }
}