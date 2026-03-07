package com.example.roohub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class UploadActivity extends AppCompatActivity {

    ImageView imagePreview;
    Button btnUploadImage, btnEdit, btnSave, btnCancel;
    EditText etArtName, etDescription;
    Spinner spinnerType;

    Uri imageUri;

    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Connect UI components
        imagePreview = findViewById(R.id.imagePreview);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnEdit = findViewById(R.id.btnEdit);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        etArtName = findViewById(R.id.etArtName);
        etDescription = findViewById(R.id.etDescription);
        spinnerType = findViewById(R.id.spinnerType);

        // Spinner values
        String[] types = {"Select Type", "Teacher", "Artist"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                types
        );

        spinnerType.setAdapter(adapter);

        // Upload Image Button
        btnUploadImage.setOnClickListener(v -> {

            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            startActivityForResult(intent, PICK_IMAGE);

        });

        // Save Button
        btnSave.setOnClickListener(v -> {

            String name = etArtName.getText().toString();
            String description = etDescription.getText().toString();
            String type = spinnerType.getSelectedItem().toString();

            if(name.isEmpty() || description.isEmpty()){
                Toast.makeText(this,"Please fill all fields",Toast.LENGTH_SHORT).show();
            }
            else{

                Toast.makeText(this,"Art Saved Successfully",Toast.LENGTH_SHORT).show();

                // Go to HomeActivity
                Intent intent = new Intent(UploadActivity.this, HomeActivity.class);
                startActivity(intent);

                finish();
            }

        });

        // Edit Button
        btnEdit.setOnClickListener(v -> {

            Toast.makeText(this,"Edit Mode Enabled",Toast.LENGTH_SHORT).show();

            etArtName.setEnabled(true);
            etDescription.setEnabled(true);
            spinnerType.setEnabled(true);

        });

        // Cancel Button
        btnCancel.setOnClickListener(v -> {

            Toast.makeText(this,"Upload Cancelled",Toast.LENGTH_SHORT).show();

            finish();

        });

    }

    // Image Picker Result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null){

            imageUri = data.getData();
            imagePreview.setImageURI(imageUri);

        }

    }
}