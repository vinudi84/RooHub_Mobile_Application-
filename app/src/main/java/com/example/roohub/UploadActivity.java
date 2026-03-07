package com.example.roohub;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class UploadActivity extends AppCompatActivity {

    ImageView imagePreview;
    Button btnUploadImage, btnEdit, btnSave, btnCancel;
    EditText etArtName, etDescription;
    Spinner spinnerType;

    Uri imageUri;

    private static final int PICK_IMAGE = 1;

    // For modern activity result API
    private ActivityResultLauncher<Intent> imagePickerLauncher;

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

        // Activity result launcher for gallery picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == RESULT_OK && result.getData() != null){
                        imageUri = result.getData().getData();
                        imagePreview.setImageURI(imageUri);
                    }
                }
        );

        // Upload Image Button
        btnUploadImage.setOnClickListener(v -> {
            // Check permission
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                        != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 100);
                } else {
                    openGallery();
                }
            } else {
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
                } else {
                    openGallery();
                }
            }
        });

        // Save Button
        btnSave.setOnClickListener(v -> {
            String name = etArtName.getText().toString();
            String description = etDescription.getText().toString();
            String type = spinnerType.getSelectedItem().toString();

            if(name.isEmpty() || description.isEmpty() || type.equals("Select Type")){
                Toast.makeText(this,"Please fill all fields and select type",Toast.LENGTH_SHORT).show();
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

    // Open gallery method
    private void openGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            openGallery();
        } else {
            Toast.makeText(this,"Permission Denied! Cannot select image.", Toast.LENGTH_SHORT).show();
        }
    }
}