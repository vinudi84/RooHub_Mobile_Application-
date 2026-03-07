package com.example.roohub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;

public class UploadActivity extends AppCompatActivity {

    ImageView imagePreview;
    Spinner spinnerType;
    EditText etArtName, etDescription;
    Button btnEdit, btnSave, btnCancel;
    Uri selectedImageUri;

    ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imagePreview.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        imagePreview = findViewById(R.id.imagePreview);
        spinnerType = findViewById(R.id.spinnerType);
        etArtName = findViewById(R.id.etArtName);
        etDescription = findViewById(R.id.etDescription);
        btnEdit = findViewById(R.id.btnEdit);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        String[] types = {"Teacher", "Artist"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        imagePreview.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        btnSave.setOnClickListener(v -> {
            String artName = etArtName.getText().toString().trim();
            String artDescription = etDescription.getText().toString().trim();
            String artType = spinnerType.getSelectedItem().toString();

            if (selectedImageUri != null && !artName.isEmpty()) {
                SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

                // දැනට තියෙන ලිස්ට් එක ගන්න
                String allArt = prefs.getString("all_art_data", "");

                // අලුත් දත්ත ටික එක දිග string එකක් විදිහට හදාගන්න (වෙන් කරන්නේ | සංකේතයෙන්)
                String newData = artName + "###" + artType + "###" + artDescription + "###" + selectedImageUri.toString();

                // අලුත් දත්ත පරණ ඒවට එකතු කරන්න
                if (!allArt.isEmpty()) {
                    allArt = newData + "|||" + allArt; // අලුත් ඒවා උඩට එන්න
                } else {
                    allArt = newData;
                }

                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("all_art_data", allArt);
                editor.apply();

                startActivity(new Intent(UploadActivity.this, HomeActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Please select an image and enter art name", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> finish());
    }
}