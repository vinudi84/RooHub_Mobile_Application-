package com.example.roohub;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class teacherSignUp extends AppCompatActivity {

    Spinner artSpinner;
    CircleImageView profileImage;
    ActivityResultLauncher<String> galleryLauncher;
    Button registerBtn;

    // Declare variables for the input fields
    EditText etName, etQualification, etEmail;
    Uri selectedImageUri; // To store the profile image URI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teacher_sign_up);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize Views using IDs from your XML
        artSpinner = findViewById(R.id.artSpinner);
        profileImage = findViewById(R.id.profileImage);
        registerBtn = findViewById(R.id.registerBtn);
        etName = findViewById(R.id.name);
        etQualification = findViewById(R.id.qualification);
        etEmail = findViewById(R.id.email);

        // Initialize the Gallery Launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        // This line "locks" the permission so you can use it in the next activity
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        getContentResolver().takePersistableUriPermission(uri, takeFlags);

                        profileImage.setImageURI(uri);
                        selectedImageUri = uri;
                    }
                }
        );

        profileImage.setOnClickListener(v -> {
            galleryLauncher.launch("image/*");
        });

        registerBtn.setOnClickListener(v -> {
            // Get data from fields
            String teacherName = etName.getText().toString().trim();
            String teacherQual = etQualification.getText().toString().trim();
            String teacherEmail = etEmail.getText().toString().trim();
            String artType = artSpinner.getSelectedItem().toString();

            // Check if user selected a valid art type
            if (artType.equals("Art type")) {
                Toast.makeText(this, "Please select an Art Type", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create Intent to move to ViewUploadActivity
            Intent intent = new Intent(teacherSignUp.this, ViewUploadActivity.class);

            // Put data into the intent
            intent.putExtra("teacher_name", teacherName);
            intent.putExtra("teacher_email", teacherEmail);
            intent.putExtra("art_type", artType);
            intent.putExtra("qualifications", teacherQual);

            // Pass the image URI as a string if it exists
            if (selectedImageUri != null) {
                intent.putExtra("teacher_image", selectedImageUri.toString());
            }

            startActivity(intent);
        });

        // Spinner Logic
        String[] artTypes = {"Art type", "Pencil art", "Coloring art", "Assemblage art"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                artTypes
        ) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(position == 0 ? Color.GRAY : Color.BLACK);
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        artSpinner.setAdapter(adapter);
    }
}