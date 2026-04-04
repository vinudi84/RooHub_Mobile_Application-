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
import java.io.ByteArrayOutputStream;
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
    private Spinner spinnerCourseCategory;
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
                    // Get file name from URI
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

        // Initialize session data
        userId = SessionManager.getUserId(this);
        authToken = SessionManager.getToken(this);

        // Validate session data
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "ERROR: userId is null or empty");
            Toast.makeText(this, "Session error: User ID missing", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        if (authToken == null || authToken.isEmpty()) {
            Log.e(TAG, "ERROR: authToken is null or empty");
            Toast.makeText(this, "Session error: Auth token missing", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Log.d(TAG, "Session validated - userId: " + userId);
        Log.d(TAG, "Token length: " + authToken.length());

        // Initialize UI components
        initializeUI();
    }

    /**
     * Initialize all UI components and set click listeners
     */
    private void initializeUI() {
        spinnerCourseCategory = findViewById(R.id.spinnerCourseCategory);
        etCourseName = findViewById(R.id.etCourseName);
        etCourseDescription = findViewById(R.id.etCourseDescription);
        tvVideoFileName = findViewById(R.id.tvVideoFileName);
        btnSelectVideo = findViewById(R.id.btnSelectVideo);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnCancel = findViewById(R.id.btnCancel);

        // Setup category spinner
        setupCategorySpinner();

        // Video selection
        btnSelectVideo.setOnClickListener(v -> openVideoSelector());

        // Form submission
        btnSubmit.setOnClickListener(v -> submitCourseForm());

        // Cancel button
        btnCancel.setOnClickListener(v -> finish());
    }

    /**
     * Setup spinner with course categories
     */
    private void setupCategorySpinner() {
        List<String> categories = new ArrayList<>();
        categories.add("Select Category");
        categories.add("Pencil Art");
        categories.add("Coloring");
        categories.add("Assemblage Art");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourseCategory.setAdapter(adapter);
    }

    /**
     * Open video selector
     */
    private void openVideoSelector() {
        videoLauncher.launch(new String[]{"video/*"});
    }

    /**
     * Get video file name from URI
     */
    private void getVideoFileName(Uri uri) {
        try {
            // Try to get display name from cursor
            String[] projection = {android.provider.MediaStore.Video.VideoColumns.DISPLAY_NAME};
            android.database.Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                videoFileName = cursor.getString(0);
                cursor.close();
            } else {
                // Fallback: use last path segment
                videoFileName = uri.getLastPathSegment();
            }
            tvVideoFileName.setText("Selected: " + videoFileName);
            Log.d(TAG, "Video file name: " + videoFileName);
        } catch (Exception e) {
            Log.e(TAG, "Error getting video name: " + e.getMessage());
            videoFileName = "video_" + System.currentTimeMillis();
            tvVideoFileName.setText("Selected: " + videoFileName);
        }
    }

    /**
     * Validate and submit course form
     */
    private void submitCourseForm() {
        String courseName = etCourseName.getText().toString().trim();
        String courseDesc = etCourseDescription.getText().toString().trim();
        int selectedPosition = spinnerCourseCategory.getSelectedItemPosition();

        Log.d(TAG, "Form submission started");
        Log.d(TAG, "Course name: " + courseName);
        Log.d(TAG, "Selected position: " + selectedPosition);
        Log.d(TAG, "Video URI: " + (courseVideoUri != null ? courseVideoUri.toString() : "null"));

        // Validation
        if (courseName.isEmpty()) {
            Toast.makeText(this, "Please enter course name", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Validation failed: course name empty");
            return;
        }

        if (courseName.length() < 3) {
            Toast.makeText(this, "Course name must be at least 3 characters", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Validation failed: course name too short");
            return;
        }

        if (courseDesc.isEmpty()) {
            Toast.makeText(this, "Please enter course description", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Validation failed: description empty");
            return;
        }

        if (courseDesc.length() < 10) {
            Toast.makeText(this, "Description must be at least 10 characters", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Validation failed: description too short");
            return;
        }

        if (selectedPosition == 0 || selectedPosition < 0) {
            Toast.makeText(this, "Please select a course category", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Validation failed: no category selected");
            return;
        }

        if (courseVideoUri == null) {
            Toast.makeText(this, "Please select a course video", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Validation failed: video not selected");
            return;
        }

        // Validate video file size (max 500MB)
        try {
            long fileSize = getFileSize(courseVideoUri);
            Log.d(TAG, "Video file size: " + (fileSize / (1024 * 1024)) + " MB");
            
            if (fileSize > 500 * 1024 * 1024) {
                Toast.makeText(this, "Video file is too large (max 500MB)", Toast.LENGTH_LONG).show();
                Log.w(TAG, "Validation failed: file size exceeds limit");
                return;
            }
            
            if (fileSize == 0) {
                Toast.makeText(this, "Video file is empty", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Validation failed: file size is 0");
                return;
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not validate file size: " + e.getMessage());
        }

        // Get selected category
        selectedCategory = spinnerCourseCategory.getSelectedItem().toString();
        Log.d(TAG, "Selected category: " + selectedCategory);

        // Upload form data
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Uploading...");

        Toast.makeText(this, "Starting upload...", Toast.LENGTH_SHORT).show();
        uploadCourseData(courseName, courseDesc);
    }

    /**
     * Upload course video first, then submit form data to database
     */
    private void uploadCourseData(String courseName, String courseDesc) {
        new Thread(() -> {
            try {
                Log.d(TAG, "Starting upload thread");
                Log.d(TAG, "Auth token present: " + (authToken != null));
                Log.d(TAG, "User ID present: " + (userId != null));

                // Step 1: Upload video to Supabase Storage
                Log.d(TAG, "Step 1: Uploading video to storage");
                String videoUrl = uploadVideoToStorage(
                    courseVideoUri,
                    "course-videos",
                    authToken
                );
                Log.d(TAG, "Video uploaded successfully: " + videoUrl);

                // Step 2: Submit form data using REST API
                Log.d(TAG, "Step 2: Submitting form to database");
                submitFormToDatabase(courseName, courseDesc, videoUrl);

            } catch (Exception e) {
                Log.e(TAG, "Upload error: " + e.getMessage(), e);
                e.printStackTrace();
                runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Submit Course");
                    String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
                    Toast.makeText(CourseFormActivity.this,
                            "Upload error: " + errorMsg,
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    /**
     * Get file size from URI
     */
    private long getFileSize(Uri uri) throws Exception {
        try (InputStream is = getContentResolver().openInputStream(uri)) {
            if (is == null) return 0;
            long size = 0;
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                size += bytesRead;
            }
            return size;
        }
    }

    /**
     * Upload video to Supabase Storage
     */
    private String uploadVideoToStorage(Uri videoUri, String bucket, String token) throws Exception {

        InputStream is = getContentResolver().openInputStream(videoUri);
        if (is == null) throw new Exception("Cannot read video file");

        // Get MIME type
        String mimeType = getContentResolver().getType(videoUri);
        if (mimeType == null) mimeType = "video/mp4";

        // Generate filename
        String extension = getVideoExtension(mimeType);
        String fileName = UUID.randomUUID().toString() + extension;

        // ✅ IMPORTANT: Make sure bucket name is EXACT
        String uploadUrl = SupabaseClient.SUPABASE_URL
                + "/storage/v1/object/"
                + bucket
                + "/"
                + fileName;

        Log.e(TAG, "UPLOAD URL: " + uploadUrl);

        URL url = new URL(uploadUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("apikey", SupabaseClient.SUPABASE_ANON_KEY);
        conn.setRequestProperty("Content-Type", mimeType);
        conn.setRequestProperty("x-upsert", "true");

        conn.setDoOutput(true);
        conn.setChunkedStreamingMode(0); // 🔥 VERY IMPORTANT (no memory issue)

        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);

        // ✅ STREAM upload (no ByteArray)
        OutputStream os = conn.getOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;

        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }

        os.flush();
        os.close();
        is.close();

        int responseCode = conn.getResponseCode();

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                responseCode >= 200 && responseCode < 300
                        ? conn.getInputStream()
                        : conn.getErrorStream()
        ));

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        conn.disconnect();

        Log.e(TAG, "RESPONSE CODE: " + responseCode);
        Log.e(TAG, "RESPONSE: " + response.toString());

        if (responseCode < 200 || responseCode >= 300) {
            throw new Exception("Upload failed: " + responseCode + " - " + response);
        }

        // ✅ Return public URL
        return SupabaseClient.SUPABASE_URL
                + "/storage/v1/object/public/"
                + bucket
                + "/"
                + fileName;
    }

    /**
     * Get video file extension based on mime type
     */
    private String getVideoExtension(String mimeType) {
        if (mimeType == null) return ".mp4";
        
        switch (mimeType) {
            case "video/mp4":
                return ".mp4";
            case "video/quicktime":
                return ".mov";
            case "video/x-msvideo":
                return ".avi";
            case "video/x-matroska":
                return ".mkv";
            case "video/webm":
                return ".webm";
            case "video/3gpp":
                return ".3gp";
            default:
                return ".mp4";
        }
    }

    /**
     * Submit form data to database using REST API
     */
    private void submitFormToDatabase(String courseName, String courseDesc, String videoUrl) {
        new Thread(() -> {
            try {
                // Create JSON object for database submission
                JSONObject formData = new JSONObject();
                formData.put("user_id", userId);
                formData.put("course_name", courseName);
                formData.put("course_category", selectedCategory);
                formData.put("description", courseDesc);
                formData.put("video_url", videoUrl);
                formData.put("created_at", System.currentTimeMillis());

                // Submit to Supabase REST API
                String dbUrl = SupabaseClient.SUPABASE_URL + "/rest/v1/course_uploads";
                Log.d(TAG, "Submitting to database: " + dbUrl);
                Log.d(TAG, "Body: " + formData.toString());

                URL url = new URL(dbUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + authToken);
                conn.setRequestProperty("apikey", SupabaseClient.SUPABASE_ANON_KEY);
                conn.setRequestProperty("Prefer", "return=representation");
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(60000);
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(formData.toString().getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

                int responseCode = conn.getResponseCode();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        responseCode >= 200 && responseCode < 300 ?
                                conn.getInputStream() :
                                conn.getErrorStream()
                ));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();
                conn.disconnect();

                Log.d(TAG, "Database response code: " + responseCode);
                Log.d(TAG, "Database response: " + response.toString());

                if (responseCode >= 200 && responseCode < 300) {
                    Log.d(TAG, "Database submission successful");
                    runOnUiThread(() -> {
                        Toast.makeText(CourseFormActivity.this,
                                "Course submitted successfully!",
                                Toast.LENGTH_SHORT).show();

                        // Navigate to home
                        startActivity(new Intent(CourseFormActivity.this, HomeActivity.class));
                        finish();
                    });
                } else {
                    Log.e(TAG, "=== DATABASE SUBMISSION FAILED ===");
                    Log.e(TAG, "Response code: " + responseCode);
                    Log.e(TAG, "Response: " + response.toString());
                    
                    String errorMsg = response.toString();
                    if (errorMsg.isEmpty()) {
                        errorMsg = "HTTP " + responseCode;
                    }
                    
                    final String finalErrorMsg = errorMsg;
                    runOnUiThread(() -> {
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText("Submit Course");
                        Toast.makeText(CourseFormActivity.this,
                                "Submission error: " + finalErrorMsg,
                                Toast.LENGTH_LONG).show();
                    });
                }

            } catch (Exception e) {
                Log.e(TAG, "Database submission error: " + e.getMessage(), e);
                e.printStackTrace();
                runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Submit Course");
                    Toast.makeText(CourseFormActivity.this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}
