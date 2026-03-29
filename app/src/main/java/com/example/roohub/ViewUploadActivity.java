package com.example.roohub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ViewUploadActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvArtType, tvQualifications;
    private EditText etArtName, etVideoDetails;
    private ImageView ivTeacherProfile;
    private String teacherArtCategory = "";
    private Uri videoUri = null;
    private static final int VIDEO_PICK_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_upload);

        // UI Initialization
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvArtType = findViewById(R.id.tvArtType);
        tvQualifications = findViewById(R.id.tvQualifications);
        ivTeacherProfile = findViewById(R.id.ivTeacherProfile);
        etArtName = findViewById(R.id.etArtName);
        etVideoDetails = findViewById(R.id.etVideoDetails);
        Button btnSelectVideo = findViewById(R.id.btnSelectVideo);
        Button btnUpload = findViewById(R.id.btnUpload);

        // Receive teacher profile data via Intent
        Intent intent = getIntent();
        if (intent != null) {
            tvName.setText("Name: " + intent.getStringExtra("teacher_name"));
            tvEmail.setText("Email: " + intent.getStringExtra("teacher_email"));
            teacherArtCategory = intent.getStringExtra("art_type");
            tvArtType.setText("Art Type: " + teacherArtCategory);
            tvQualifications.setText("Qualifications: " + intent.getStringExtra("qualifications"));

            String imageUriString = intent.getStringExtra("teacher_image");
            if (imageUriString != null) {
                ivTeacherProfile.setImageURI(Uri.parse(imageUriString));
            }
        }

        // Open gallery to select a video (Using OPEN_DOCUMENT for permanent access)
        btnSelectVideo.setOnClickListener(v -> {
            Intent videoIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            videoIntent.addCategory(Intent.CATEGORY_OPENABLE);
            videoIntent.setType("video/*");
            startActivityForResult(videoIntent, VIDEO_PICK_REQUEST);
        });

        // Save data and navigate
        btnUpload.setOnClickListener(v -> {
            String artName = etArtName.getText().toString().trim();
            String artDesc = etVideoDetails.getText().toString().trim();

            if (artName.isEmpty() || artDesc.isEmpty() || videoUri == null) {
                Toast.makeText(this, "Please fill all fields and select a video!", Toast.LENGTH_SHORT).show();
            } else {
                SharedPreferences sharedPreferences = getSharedPreferences("RooHubData", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                String categoryKey = (teacherArtCategory != null) ? teacherArtCategory.trim() : "General Art";

                try {
                    // 1. PERSISTENT PERMISSION: Request long-term access so video plays even after app restarts
                    final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                    getContentResolver().takePersistableUriPermission(videoUri, takeFlags);

                    // 2. FETCH EXISTING DATA: Read current videos to avoid overwriting (Append logic)
                    String existingData = sharedPreferences.getString(categoryKey, "");

                    // 3. APPEND NEW DATA: Format: Title|Description|VideoUri###
                    String newData = artName + "|" + artDesc + "|" + videoUri.toString() + "###";
                    String finalData = existingData + newData;

                    // 4. SAVE TO SHARED PREFERENCES
                    editor.putString(categoryKey, finalData);
                    editor.apply();

                    Toast.makeText(this, "Video uploaded successfully!", Toast.LENGTH_SHORT).show();

                    // Navigation Logic based on Category
                    Intent nextIntent;
                    if (categoryKey.equalsIgnoreCase("Coloring art")) {
                        nextIntent = new Intent(ViewUploadActivity.this, ColorArtActivity.class);
                    } else if (categoryKey.equalsIgnoreCase("Pencil art")) {
                        nextIntent = new Intent(ViewUploadActivity.this, PencilArtActivity.class);
                    } else {
                        nextIntent = new Intent(ViewUploadActivity.this, AssemblageArtActivity.class);
                    }
                    startActivity(nextIntent);
                    finish();

                } catch (Exception e) {
                    Toast.makeText(this, "Permission Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // --- TOOLBAR MENU METHODS ---

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds the "View & Edit" button to the toolbar
        getMenuInflater().inflate(R.menu.upload_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle toolbar item clicks
        if (item.getItemId() == R.id.action_view_edit) {
            // Open the friend's teacherSignUp activity
            Intent intent = new Intent(ViewUploadActivity.this, teacherSignUp.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIDEO_PICK_REQUEST && resultCode == RESULT_OK && data != null) {
            videoUri = data.getData();
            Toast.makeText(this, "Video selected!", Toast.LENGTH_SHORT).show();
        }
    }
}