package com.example.roohub;

import android.content.Intent;
import android.content.SharedPreferences;
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

// --- NEW IMPORTS FOR GSON ---
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;

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
        etName = findViewById(R.id.name);
        etEmail = findViewById(R.id.email);
        etQualifications = findViewById(R.id.qualification);
        etPassword = findViewById(R.id.password);

        LoginLink.setOnClickListener(v -> {
            Intent loginIntent = new Intent(teacherSignUp.this, LoginActivity.class);
            startActivity(loginIntent);
        });

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

        // Register button logic
        registerBtn.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String qualifications = etQualifications.getText().toString().trim();
            String artType = artSpinner.getSelectedItem().toString();

            if (name.isEmpty() || email.isEmpty() || artType.equals("Art type")) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String imageStr = (selectedImageUri != null) ? selectedImageUri.toString() : "";

            // --- STEP 1: LOAD EXISTING TEACHERS LIST USING GSON ---
            SharedPreferences pref = getSharedPreferences("RooHubData", MODE_PRIVATE);
            Gson gson = new Gson();
            String json = pref.getString("teachers_list", null);

            ArrayList<TeacherDataStore.Teacher> teacherList;
            if (json == null) {
                teacherList = new ArrayList<>(); // Create new list if empty
            } else {
                Type type = new TypeToken<ArrayList<TeacherDataStore.Teacher>>() {}.getType();
                teacherList = gson.fromJson(json, type); // Convert JSON string back to List
            }

            // --- STEP 2: ADD NEW TEACHER TO THE LIST ---
            teacherList.add(new TeacherDataStore.Teacher(
                    name, email, qualifications, artType, qualifications, imageStr
            ));

            // --- STEP 3: SAVE UPDATED LIST BACK TO SHAREDPREFERENCES ---
            String updatedJson = gson.toJson(teacherList);
            pref.edit().putString("teachers_list", updatedJson).apply();

            // Also update the current login profile for ViewUploadActivity
            SharedPreferences profilePref = getSharedPreferences("TeacherProfile", MODE_PRIVATE);
            SharedPreferences.Editor editor = profilePref.edit();
            editor.putString("t_name", name);
            editor.putString("t_email", email);
            editor.putString("t_art_type", artType);
            editor.putString("t_qualify", qualifications);
            editor.putString("t_image", imageStr);
            editor.apply();

            // Navigate to ViewUploadActivity
            Intent intent = new Intent(teacherSignUp.this, ViewUploadActivity.class);
            intent.putExtra("teacher_name", name);
            intent.putExtra("teacher_email", email);
            intent.putExtra("art_type", artType);
            intent.putExtra("qualifications", qualifications);
            if (!imageStr.isEmpty()) {
                intent.putExtra("teacher_image", imageStr);
            }
            startActivity(intent);

            Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();
        });

        // Setup Spinner for Art Types
        String[] artTypes = {"Art type", "Pencil art", "Coloring art", "Assemblage art"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, artTypes) {
            @Override
            public boolean isEnabled(int position) { return position != 0; }
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