package com.example.roohub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ViewUploadActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvQualifications;
    private EditText etArtName, etVideoDetails;
    private ImageView ivTeacherProfile;
    private Spinner spnArtType;
    private Uri videoUri = null;
    private static final int VIDEO_PICK_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_upload);

        // Initialize UI components
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvQualifications = findViewById(R.id.tvQualifications);
        ivTeacherProfile = findViewById(R.id.ivTeacherProfile);
        etArtName = findViewById(R.id.etArtName);
        etVideoDetails = findViewById(R.id.etVideoDetails);
        spnArtType = findViewById(R.id.spnArtType);
        Button btnSelectVideo = findViewById(R.id.btnSelectVideo);
        Button btnUpload = findViewById(R.id.btnUpload);

        // --- STEP 1: FETCH SAVED TEACHER DATA FROM SHAREDPREFERENCES ---
        SharedPreferences teacherPref = getSharedPreferences("TeacherProfile", MODE_PRIVATE);
        String savedName = teacherPref.getString("t_name", "Unknown Teacher");
        String savedEmail = teacherPref.getString("t_email", "No Email");
        String savedQual = teacherPref.getString("t_qualify", "");
        String savedImage = teacherPref.getString("t_image", null);
        String savedArtType = teacherPref.getString("t_art_type", "Pencil art");

        tvName.setText("Name: " + savedName);
        tvEmail.setText("Email: " + savedEmail);
        tvQualifications.setText("Qualifications: " + savedQual);

        // Safety check for loading profile image with SecurityException handling
        if (savedImage != null) {
            try {
                ivTeacherProfile.setImageURI(Uri.parse(savedImage));
            } catch (Exception e) {
                // Fallback to default icon if URI access is denied or null
                ivTeacherProfile.setImageResource(R.drawable.ic_launcher_background);
            }
        }

        // --- STEP 2: CONFIGURE SPINNER FOR ART CATEGORIES ---
        String[] categories = {"Pencil art", "Coloring art", "Assemblage art"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnArtType.setAdapter(adapter);

        // Auto-select the teacher's registered art type in the spinner
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(savedArtType)) {
                spnArtType.setSelection(i);
                break;
            }
        }

        // Open Document intent to pick video from storage safely
        btnSelectVideo.setOnClickListener(v -> {
            Intent videoIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            videoIntent.addCategory(Intent.CATEGORY_OPENABLE);
            videoIntent.setType("video/*");
            // Request long-term access permission flags
            videoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(videoIntent, VIDEO_PICK_REQUEST);
        });

        // --- STEP 3: UPLOAD LOGIC ---
        btnUpload.setOnClickListener(v -> {
            String artName = etArtName.getText().toString().trim();
            String artDesc = etVideoDetails.getText().toString().trim();
            String selectedCategory = spnArtType.getSelectedItem().toString();

            if (artName.isEmpty() || artDesc.isEmpty() || videoUri == null) {
                Toast.makeText(this, "Please fill all fields and select a video!", Toast.LENGTH_SHORT).show();
            } else {
                SharedPreferences sharedPreferences = getSharedPreferences("RooHubData", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                try {
                    // Update the global data store with the new video entry
                    String existingData = sharedPreferences.getString(selectedCategory, "");
                    String newData = savedName + "|" + artName + "|" + artDesc + "|" + videoUri.toString() + "|" + savedEmail + "###";
                    String finalData = existingData + newData;

                    editor.putString(selectedCategory, finalData);
                    editor.apply();

                    Toast.makeText(this, "Video successfully added!", Toast.LENGTH_SHORT).show();

                    // Navigation logic based on the selected art category
                    Intent nextIntent;
                    if (selectedCategory.equalsIgnoreCase("Coloring art")) {
                        nextIntent = new Intent(ViewUploadActivity.this, ColorArtActivity.class);
                    } else if (selectedCategory.equalsIgnoreCase("Pencil art")) {
                        nextIntent = new Intent(ViewUploadActivity.this, PencilArtActivity.class);
                    } else {
                        nextIntent = new Intent(ViewUploadActivity.this, AssemblageArtActivity.class);
                    }
                    startActivity(nextIntent);
                    finish(); // Close current activity after successful upload

                } catch (Exception e) {
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // REMOVED: onCreateOptionsMenu and onOptionsItemSelected to hide "VIEW & EDIT" button

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIDEO_PICK_REQUEST && resultCode == RESULT_OK && data != null) {
            videoUri = data.getData();

            if (videoUri != null) {
                /* CRITICAL: Requesting permanent read permission for the selected video URI.
                   This is essential to keep the video playable even after app restarts.
                */
                final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                try {
                    getContentResolver().takePersistableUriPermission(videoUri, takeFlags);
                    Toast.makeText(this, "Video selected and permission granted!", Toast.LENGTH_SHORT).show();
                } catch (SecurityException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Permission Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}