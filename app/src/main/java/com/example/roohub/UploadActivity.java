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

public class UploadActivity extends AppCompatActivity {

    ImageView imagePreview;
    Spinner spinnerType;
    EditText etArtName, etDescription;
    Button btnEdit, btnSave, btnCancel;

    Uri selectedImageUri;

    // Image picker launcher
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

        // Find views
        imagePreview = findViewById(R.id.imagePreview);
        spinnerType = findViewById(R.id.spinnerType);
        etArtName = findViewById(R.id.etArtName);
        etDescription = findViewById(R.id.etDescription);
        btnEdit = findViewById(R.id.btnEdit);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Populate spinner with Teacher / Artist
        String[] types = {"Teacher", "Artist"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        // Select image on click
        imagePreview.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // Save button click
        btnSave.setOnClickListener(v -> {
            String artName = etArtName.getText().toString().trim();
            String artDescription = etDescription.getText().toString().trim();
            String artType = spinnerType.getSelectedItem().toString();

            if (selectedImageUri != null && !artName.isEmpty()) {
                // Save data to SharedPreferences
                SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("last_uploaded_image", selectedImageUri.toString());
                editor.putString("last_art_name", artName);
                editor.putString("last_art_description", artDescription);
                editor.putString("last_art_type", artType);
                editor.apply();

                // Go back to HomeActivity
                startActivity(new Intent(UploadActivity.this, HomeActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Please select an image and enter art name", Toast.LENGTH_SHORT).show();
            }
        });

        // Edit button: load previously saved art
        btnEdit.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            String name = prefs.getString("last_art_name", "");
            String desc = prefs.getString("last_art_description", "");
            String type = prefs.getString("last_art_type", "");
            String uri = prefs.getString("last_uploaded_image", null);

            etArtName.setText(name);
            etDescription.setText(desc);

            if (!type.isEmpty()) {
                for (int i = 0; i < spinnerType.getCount(); i++) {
                    if (spinnerType.getItemAtPosition(i).toString().equals(type)) {
                        spinnerType.setSelection(i);
                        break;
                    }
                }
            }

            if (uri != null) {
                selectedImageUri = Uri.parse(uri);
                imagePreview.setImageURI(selectedImageUri);
            }
        });

        // Cancel button: close activity
        btnCancel.setOnClickListener(v -> finish());
    }
}