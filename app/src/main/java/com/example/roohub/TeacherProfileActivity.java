package com.example.roohub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class TeacherProfileActivity extends AppCompatActivity {

    private CircleImageView ivProfile;
    private TextView tvName, tvEmail;
    private String email, currentImageUri;
    private static final int PICK_IMAGE_REQUEST = 150;

    // Categories where video records are stored
    private final String[] categories = {"Pencil art", "Coloring art", "Assemblage art"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_profile);

        ivProfile = findViewById(R.id.profileImage);
        tvName = findViewById(R.id.profileName);
        tvEmail = findViewById(R.id.profileEmail);
        Button btnManage = findViewById(R.id.btnManageVideos);
        Button btnEditProfile = findViewById(R.id.btnEditProfile);

        // Initial loading of teacher data from SharedPreferences
        loadProfileData();

        // Open the edit profile dialog
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        // Go to Manage Videos page and pass current teacher email
        btnManage.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherProfileActivity.this, ManageVideosActivity.class);
            intent.putExtra("teacher_email", email);
            startActivity(intent);
        });
    }

    private void loadProfileData() {
        SharedPreferences pref = getSharedPreferences("TeacherProfile", MODE_PRIVATE);
        String name = pref.getString("t_name", "No Name");
        email = pref.getString("t_email", "No Email");
        currentImageUri = pref.getString("t_image", "");

        tvName.setText(name);
        tvEmail.setText(email);
        if (!currentImageUri.isEmpty()) {
            try {
                ivProfile.setImageURI(Uri.parse(currentImageUri));
            } catch (Exception e) {
                ivProfile.setImageResource(R.drawable.ic_launcher_background);
            }
        }
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Profile Info");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText etNewName = new EditText(this);
        etNewName.setText(tvName.getText().toString());
        etNewName.setHint("Enter New Name");

        Button btnChangeImage = new Button(this);
        btnChangeImage.setText("Change Profile Picture");
        btnChangeImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        layout.addView(etNewName);
        layout.addView(btnChangeImage);
        builder.setView(layout);

        builder.setPositiveButton("Save Changes", (dialog, which) -> {
            String updatedName = etNewName.getText().toString().trim();
            if (!updatedName.isEmpty()) {
                saveProfileUpdate(updatedName);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // This method saves data to both 'TeacherProfile' and 'RooHubData' files
    private void saveProfileUpdate(String newName) {
        // 1. Update main profile file
        SharedPreferences pref = getSharedPreferences("TeacherProfile", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("t_name", newName);
        editor.putString("t_image", currentImageUri);
        editor.apply();

        // 2. Sync this change to all existing video records in other categories
        updateTeacherInfoInAllCategories(newName, currentImageUri);

        tvName.setText(newName);
        Toast.makeText(this, "Profile and all video records updated!", Toast.LENGTH_SHORT).show();
    }

    // Helper logic to find and replace teacher name/image in category strings
    private void updateTeacherInfoInAllCategories(String newName, String newImage) {
        SharedPreferences videoSp = getSharedPreferences("RooHubData", MODE_PRIVATE);
        SharedPreferences.Editor videoEditor = videoSp.edit();

        for (String cat : categories) {
            String catData = videoSp.getString(cat, "");
            if (!catData.isEmpty()) {
                // Split long data into individual video records
                String[] records = catData.split("###");
                StringBuilder updatedCatData = new StringBuilder();

                for (String record : records) {
                    if (record.isEmpty()) continue;

                    // If the record belongs to THIS teacher (via email)
                    if (record.contains(email)) {
                        String[] fields = record.split("\\|");
                        if (fields.length > 5) {
                            fields[0] = newName;  // Update name at Index 0
                            fields[5] = newImage; // Update image at Index 5
                        }

                        // Re-build the record string with updated info
                        StringBuilder newRecord = new StringBuilder();
                        for (int i = 0; i < fields.length; i++) {
                            newRecord.append(fields[i]).append(i == fields.length - 1 ? "" : "|");
                        }
                        updatedCatData.append(newRecord.toString()).append("###");
                    } else {
                        // Keep other records unchanged
                        updatedCatData.append(record).append("###");
                    }
                }
                // Save updated category string back to SharedPreferences
                videoEditor.putString(cat, updatedCatData.toString());
            }
        }
        videoEditor.apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                currentImageUri = selectedImageUri.toString();
                ivProfile.setImageURI(selectedImageUri);

                try {
                    getContentResolver().takePersistableUriPermission(selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException e) { e.printStackTrace(); }
            }
        }
    }
}