package com.example.roohub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class UploadActivity extends AppCompatActivity {

    private ImageView imagePreview;
    private EditText etArtName, etDescription;
    private Button btnEdit, btnSave, btnCancel;
    private Uri selectedImageUri;

    // Image picker launcher
    private final ActivityResultLauncher<String> imagePickerLauncher =
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

        // Initialize views
        imagePreview = findViewById(R.id.imagePreview);
        etArtName = findViewById(R.id.etArtName);
        etDescription = findViewById(R.id.etDescription);
        btnEdit = findViewById(R.id.btnEdit);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Click image to open gallery
        imagePreview.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // SAVE button
        btnSave.setOnClickListener(v -> {
            String artName = etArtName.getText().toString().trim();
            String artDescription = etDescription.getText().toString().trim();

            // Validation
            if (selectedImageUri == null) {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
                return;
            }

            if (artName.isEmpty()) {
                Toast.makeText(this, "Please enter art name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save data
            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            String allArt = prefs.getString("all_art_data", "");

            // Default type (spinner removed)
            String artType = "Art";

            String newData = artName + "###" + artType + "###" + artDescription + "###" + selectedImageUri.toString();

            if (!allArt.isEmpty()) {
                allArt = newData + "|||" + allArt;
            } else {
                allArt = newData;
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("all_art_data", allArt);
            editor.apply();

            Toast.makeText(this, "Art uploaded successfully!", Toast.LENGTH_SHORT).show();

            // Go to HomeActivity
            Intent intent = new Intent(UploadActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        // CANCEL button
        btnCancel.setOnClickListener(v -> finish());

        // EDIT button (optional – reselect image)
        btnEdit.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
    }
}