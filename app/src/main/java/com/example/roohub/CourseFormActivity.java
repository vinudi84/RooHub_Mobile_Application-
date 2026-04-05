package com.example.roohub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * CourseFormActivity - Displays upload form for course-specific content
 * Handles video upload and REST API database submissions
 */
public class CourseFormActivity extends AppCompatActivity {

    private static final String TAG = "CourseFormActivity";

    // UI Components
    private Spinner spinnerCourseCategory, spinnerCourseStatus; // Status spinner added
    private EditText etCourseName, etCourseDescription;
    private TextView tvVideoFileName;
    private Button btnSelectVideo, btnSubmit, btnCancel;

    // Data
    private Uri courseVideoUri = null;
    private String selectedCategory = "";
    private String userId = "";
    private String authToken = "";

    // Video file name
    private String videoFileName = "";

    // Video Launcher
    private final ActivityResultLauncher<String[]> videoLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    courseVideoUri = uri;
                    getVideoFileName(uri);
                    getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Log.d(TAG, "Video selected: " + uri.toString());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_form);

        // Check authentication
        if (!SessionManager.isLoggedIn(this)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        userId = SessionManager.getUserId(this);
        authToken = SessionManager.getToken(this);

        if (userId == null || userId.isEmpty() || authToken == null || authToken.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initializeUI();
    }

    private void initializeUI() {
        spinnerCourseCategory = findViewById(R.id.spinnerCourseCategory);
        spinnerCourseStatus = findViewById(R.id.spinnerCourseStatus); // Status Spinner Reference
        etCourseName = findViewById(R.id.etCourseName);
        etCourseDescription = findViewById(R.id.etCourseDescription);
        tvVideoFileName = findViewById(R.id.tvVideoFileName);
        btnSelectVideo = findViewById(R.id.btnSelectVideo);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnCancel = findViewById(R.id.btnCancel);

        setupCategorySpinner();
        setupStatusSpinner(); // Setup the new Status Spinner

        btnSelectVideo.setOnClickListener(v -> openVideoSelector());
        btnSubmit.setOnClickListener(v -> submitCourseForm());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void setupCategorySpinner() {
        List<String> categories = new ArrayList<>();
        categories.add("Select Category");
        categories.add("Pencil Art");
        categories.add("Coloring");
        categories.add("Assemblage Art");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourseCategory.setAdapter(adapter);
    }

    /**
     * NEW: Setup the Activate/Deactivate Spinner
     */
    private void setupStatusSpinner() {
        List<String> statusOptions = new ArrayList<>();
        statusOptions.add("Activate");
        statusOptions.add("Deactivate");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourseStatus.setAdapter(adapter);
    }

    private void openVideoSelector() {
        videoLauncher.launch(new String[]{"video/*"});
    }

    private void getVideoFileName(Uri uri) {
        try {
            String[] projection = {android.provider.MediaStore.Video.VideoColumns.DISPLAY_NAME};
            android.database.Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                videoFileName = cursor.getString(0);
                cursor.close();
            } else {
                videoFileName = uri.getLastPathSegment();
            }
            tvVideoFileName.setText("Selected: " + videoFileName);
        } catch (Exception e) {
            videoFileName = "video_" + System.currentTimeMillis();
            tvVideoFileName.setText("Selected: " + videoFileName);
        }
    }

    private void submitCourseForm() {
        String courseName = etCourseName.getText().toString().trim();
        String courseDesc = etCourseDescription.getText().toString().trim();
        int selectedPosition = spinnerCourseCategory.getSelectedItemPosition();

        if (courseName.isEmpty() || courseName.length() < 3) {
            Toast.makeText(this, "Valid course name required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (courseDesc.isEmpty() || courseDesc.length() < 10) {
            Toast.makeText(this, "Valid description required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedPosition <= 0) {
            Toast.makeText(this, "Select a category", Toast.LENGTH_SHORT).show();
            return;
        }
        if (courseVideoUri == null) {
            Toast.makeText(this, "Select a video", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedCategory = spinnerCourseCategory.getSelectedItem().toString();
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Uploading...");
        uploadCourseData(courseName, courseDesc);
    }

    private void uploadCourseData(String courseName, String courseDesc) {
        new Thread(() -> {
            try {
                String videoUrl = uploadVideoToStorage(courseVideoUri, "course-videos", authToken);
                submitFormToDatabase(courseName, courseDesc, videoUrl);
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Submit Course");
                    Toast.makeText(CourseFormActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private String uploadVideoToStorage(Uri videoUri, String bucket, String token) throws Exception {
        InputStream is = getContentResolver().openInputStream(videoUri);
        String mimeType = getContentResolver().getType(videoUri);
        if (mimeType == null) mimeType = "video/mp4";
        String fileName = UUID.randomUUID().toString() + getVideoExtension(mimeType);

        String uploadUrl = SupabaseClient.SUPABASE_URL + "/storage/v1/object/" + bucket + "/" + fileName;
        URL url = new URL(uploadUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("apikey", SupabaseClient.SUPABASE_ANON_KEY);
        conn.setRequestProperty("Content-Type", mimeType);
        conn.setDoOutput(true);
        conn.setChunkedStreamingMode(0);

        OutputStream os = conn.getOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) os.write(buffer, 0, bytesRead);
        os.flush(); os.close(); is.close();

        if (conn.getResponseCode() < 200 || conn.getResponseCode() >= 300) throw new Exception("Upload failed");

        return SupabaseClient.SUPABASE_URL + "/storage/v1/object/public/" + bucket + "/" + fileName;
    }

    private String getVideoExtension(String mimeType) {
        if (mimeType.equals("video/quicktime")) return ".mov";
        return ".mp4";
    }

    private void submitFormToDatabase(String courseName, String courseDesc, String videoUrl) {
        new Thread(() -> {
            try {
                // Get the status value to pass it to the next screen
                final String selectedStatus = spinnerCourseStatus.getSelectedItem().toString();

                JSONObject formData = new JSONObject();
                formData.put("user_id", userId);
                formData.put("course_name", courseName);
                formData.put("course_category", selectedCategory);
                formData.put("description", courseDesc);
                formData.put("video_url", videoUrl);
                formData.put("created_at", System.currentTimeMillis());

                URL url = new URL(SupabaseClient.SUPABASE_URL + "/rest/v1/course_uploads");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + authToken);
                conn.setRequestProperty("apikey", SupabaseClient.SUPABASE_ANON_KEY);
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(formData.toString().getBytes(StandardCharsets.UTF_8));
                }

                if (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300) {
                    runOnUiThread(() -> {
                        Toast.makeText(CourseFormActivity.this, "Course submitted successfully!", Toast.LENGTH_SHORT).show();

                        // Pass status to HomeActivity/Display Page
                        Intent intent = new Intent(CourseFormActivity.this, HomeActivity.class);
                        intent.putExtra("COURSE_STATUS", selectedStatus);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    runOnUiThread(() -> {
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText("Submit Course");
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Submit Course");
                });
            }
        }).start();
    }
}