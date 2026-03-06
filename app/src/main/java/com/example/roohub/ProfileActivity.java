package com.example.roohub;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    Spinner spinnerRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        spinnerRole = findViewById(R.id.spinnerRole);

        String[] roles = {"Learner", "Art Enthusiast", "Teacher"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                roles
        );

        spinnerRole.setAdapter(adapter);
    }
}