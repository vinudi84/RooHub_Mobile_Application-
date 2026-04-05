package com.example.roohub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class EditProfileActivity extends AppCompatActivity {

    // Variable for the age input field
    private EditText etFullName, etBio, etAge;
    private ImageView imgProfile;
    private Button btnSave, btnManageArt, btnManageCourses;

    private Uri newProfileUri = null;

    private final ActivityResultLauncher<String[]> imageLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    newProfileUri = uri;
                    imgProfile.setImageURI(uri);
                    getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        etFullName      = findViewById(R.id.etFullName);
        etAge           = findViewById(R.id.etAge); // Connect age field to XML
        etBio           = findViewById(R.id.etBio);
        imgProfile      = findViewById(R.id.imgEditProfile);
        btnSave         = findViewById(R.id.btnSaveProfile);
        btnManageArt    = findViewById(R.id.btnManageArt);
        btnManageCourses = findViewById(R.id.btnManageCourses);

        loadDisplayData();

        imgProfile.setOnClickListener(v ->
                imageLauncher.launch(new String[]{"image/*"}));

        btnSave.setOnClickListener(v -> {
            String name = etFullName.getText().toString().trim();
            String age  = etAge.getText().toString().trim(); // Get age value from UI
            String bio  = etBio.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Display update information including age typed by user
            String updateInfo = "Updated Profile -> Name: " + name + " | Age: " + age;
            Toast.makeText(this, updateInfo, Toast.LENGTH_LONG).show();
        });

        btnManageArt.setOnClickListener(v ->
                startActivity(new Intent(this, ManageArtActivity.class)));

        btnManageCourses.setOnClickListener(v ->
                startActivity(new Intent(this, ManageCoursesActivity.class)));
    }

    private void loadDisplayData() {
        etFullName.setText("RooHub User");
        etAge.setText(""); // Age field is empty for user to type
        etBio.setText("Professional Artist and Teacher.");

        Glide.with(this)
                .load(android.R.drawable.ic_menu_gallery)
                .circleCrop()
                .into(imgProfile);
    }
}