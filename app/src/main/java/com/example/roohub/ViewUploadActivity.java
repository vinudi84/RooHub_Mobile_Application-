package com.example.roohub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
    private ImageButton btnTeacherProfile; // New Profile Button
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
        btnTeacherProfile = findViewById(R.id.btnTeacherProfile); // Link the new button
        etArtName = findViewById(R.id.etArtName);
        etVideoDetails = findViewById(R.id.etVideoDetails);
        spnArtType = findViewById(R.id.spnArtType);
        Button btnSelectVideo = findViewById(R.id.btnSelectVideo);
        Button btnUpload = findViewById(R.id.btnUpload);

        // Fetch saved profile data
        SharedPreferences teacherPref = getSharedPreferences("TeacherProfile", MODE_PRIVATE);
        String savedName = teacherPref.getString("t_name", "Unknown");
        String savedEmail = teacherPref.getString("t_email", "");
        String savedQual = teacherPref.getString("t_qualify", "");
        String savedImage = teacherPref.getString("t_image", "");

        // Set profile data to top card
        tvName.setText("Name: " + savedName);
        tvEmail.setText("Email: " + savedEmail);
        tvQualifications.setText("Qualifications: " + savedQual);

        if (!savedImage.isEmpty()) {
            try { ivTeacherProfile.setImageURI(Uri.parse(savedImage)); }
            catch (Exception e) { ivTeacherProfile.setImageResource(R.drawable.ic_launcher_background); }
        }

        // Setup Spinner categories
        String[] categories = {"Pencil art", "Coloring art", "Assemblage art"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnArtType.setAdapter(adapter);

        // Profile Button Click Listener (Navigates to Teacher Profile)
        btnTeacherProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ViewUploadActivity.this, TeacherProfileActivity.class);
            startActivity(intent);
        });

        // Video selection logic
        btnSelectVideo.setOnClickListener(v -> {
            Intent videoIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            videoIntent.addCategory(Intent.CATEGORY_OPENABLE);
            videoIntent.setType("video/*");
            videoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(videoIntent, VIDEO_PICK_REQUEST);
        });

        // Upload logic
        btnUpload.setOnClickListener(v -> {
            String artName = etArtName.getText().toString().trim();
            String artDesc = etVideoDetails.getText().toString().trim();
            String selectedCategory = spnArtType.getSelectedItem().toString();

            if (artName.isEmpty() || artDesc.isEmpty() || videoUri == null) {
                Toast.makeText(this, "Fill all fields and select video!", Toast.LENGTH_SHORT).show();
            } else {
                SharedPreferences sharedPreferences = getSharedPreferences("RooHubData", MODE_PRIVATE);
                String existingData = sharedPreferences.getString(selectedCategory, "");

                // Format: Name|ArtName|Desc|VideoUri|Email|ProfileImageUri###
                String newData = savedName + "|" + artName + "|" + artDesc + "|" + videoUri.toString() + "|" + savedEmail + "|" + savedImage + "###";

                sharedPreferences.edit().putString(selectedCategory, existingData + newData).apply();
                Toast.makeText(this, "Success! Uploaded to " + selectedCategory, Toast.LENGTH_SHORT).show();

                // Clear fields after upload for next use
                etArtName.setText("");
                etVideoDetails.setText("");
                videoUri = null;

                // Optional: Navigate to a success screen or stay on same page
                Toast.makeText(this, "Upload complete!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIDEO_PICK_REQUEST && resultCode == RESULT_OK && data != null) {
            videoUri = data.getData();
            if (videoUri != null) {
                try {
                    getContentResolver().takePersistableUriPermission(videoUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Toast.makeText(this, "Video Selected!", Toast.LENGTH_SHORT).show();
                }
                catch (SecurityException e) { e.printStackTrace(); }
            }
        }
    }
}