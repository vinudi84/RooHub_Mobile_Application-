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

    TextView LoginLink;

    EditText etName, etEmail, etQualifications, etPassword;
    Uri selectedImageUri;

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
        LoginLink = findViewById(R.id.LoginLink);

        // Match IDs with XML
        etName = findViewById(R.id.name);
        etEmail = findViewById(R.id.email);
        etQualifications = findViewById(R.id.qualification);
        etPassword = findViewById(R.id.password);

        LoginLink.setOnClickListener(v -> {
            Intent loginIntent = new Intent(teacherSignUp.this, LoginActivity.class);
            startActivity(loginIntent);
        });

        // Initialize the Gallery Launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        profileImage.setImageURI(uri);
                        selectedImageUri = uri;
                    }
                }
        );

        profileImage.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        //register button
        registerBtn.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String qualifications = etQualifications.getText().toString().trim();
            String artType = artSpinner.getSelectedItem().toString();

            // Validation
            if (name.isEmpty() || email.isEmpty() || artType.equals("Art type")) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
                return;
            }


            String imageStr = (selectedImageUri != null) ? selectedImageUri.toString() : null;


            TeacherDataStore.allTeachers.add(new TeacherDataStore.Teacher(
                    name,
                    artType,
                    qualifications,
                    imageStr
            ));


            Intent intent = new Intent(teacherSignUp.this, ViewUploadActivity.class);
            intent.putExtra("teacher_name", name);
            intent.putExtra("teacher_email", email);
            intent.putExtra("art_type", artType);
            intent.putExtra("qualifications", qualifications);
            if (selectedImageUri != null) {
                intent.putExtra("teacher_image", selectedImageUri.toString());
            }
            startActivity(intent);
        });

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