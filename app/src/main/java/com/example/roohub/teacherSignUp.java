package com.example.roohub;

import android.content.Intent; // Required for navigation
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button; // Required for the button
import android.widget.Spinner;
import android.widget.TextView;

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
    Button registerBtn; // Added variable for your button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teacher_sign_up);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize Views
        artSpinner = findViewById(R.id.artSpinner);
        profileImage = findViewById(R.id.profileImage);
        registerBtn = findViewById(R.id.registerBtn);

        //Initialize the Gallery Launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        // Set the selected image URI to the CircleImageView
                        profileImage.setImageURI(uri);
                    }
                }
        );

        // Set Click Listener on the Profile Image
        profileImage.setOnClickListener(v -> {
            // This opens the gallery to select an image
            galleryLauncher.launch("image/*");
        });


//        registerBtn.setOnClickListener(v -> {
//            Intent intent = new Intent(teacherSignUp.this, ViewUpload.class);
//            startActivity(intent);
//        });


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
                if (position == 0) {
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        artSpinner.setAdapter(adapter);
    }
}