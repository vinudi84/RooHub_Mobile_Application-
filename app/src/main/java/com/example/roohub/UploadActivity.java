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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class UploadActivity extends AppCompatActivity {

    private ImageView profilePreview, artImagePreview;
    private EditText etArtName, etDescription;
    private Button btnSave, btnGoToProfile;

    private Uri artUri     = null;
    private Uri profileUri = null;

    private final ActivityResultLauncher<String[]> artLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    artUri = uri;
                    artImagePreview.setImageURI(uri);
                    getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            });

    private final ActivityResultLauncher<String[]> profileLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    profileUri = uri;
                    profilePreview.setImageURI(uri);
                    getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // ── Redirect if not logged in ────────────────────────────────────────
        if (!SessionManager.isLoggedIn(this)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        profilePreview  = findViewById(R.id.profilePreview);
        artImagePreview = findViewById(R.id.artImagePreview);
        etArtName       = findViewById(R.id.etArtName);
        etDescription   = findViewById(R.id.etDescription);
        btnSave         = findViewById(R.id.btnSave);
        btnGoToProfile  = findViewById(R.id.btnGoToProfile);

        profilePreview.setOnClickListener(v ->
                profileLauncher.launch(new String[]{"image/*"}));

        artImagePreview.setOnClickListener(v ->
                artLauncher.launch(new String[]{"image/*"}));

        btnGoToProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        btnSave.setOnClickListener(v -> {
            String artName = etArtName.getText().toString().trim();
            String desc    = etDescription.getText().toString().trim();

            if (artName.isEmpty() || desc.isEmpty() || artUri == null) {
                Toast.makeText(this,
                        "Please fill all fields and select an image",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            btnSave.setEnabled(false);
            btnSave.setText("Uploading...");
            handleUpload(artName, desc);
        });
    }

    // ── Main upload handler ──────────────────────────────────────────────────
    private void handleUpload(String artName, String desc) {
        new Thread(() -> {
            try {
                String token  = SessionManager.getToken(this);
                String userId = SessionManager.getUserId(this);

                // ── DEBUG: Session info ──────────────────────────────────────
                android.util.Log.d("UPLOAD", "=== SESSION INFO ===");
                android.util.Log.d("UPLOAD", "userId: " + userId);
                android.util.Log.d("UPLOAD", "token null? " + (token == null));
                if (token != null && token.length() > 30) {
                    android.util.Log.d("UPLOAD", "token preview: " + token.substring(0, 30));
                }

                // ── DEBUG: Test bucket accessibility ─────────────────────────
                testBucketAccess("art-images", token);

                // ── Step 1: Upload art image ─────────────────────────────────
                String artImageUrl = uploadImageToStorage(artUri, "art-images", token);

                // ── Step 2: Upload profile image if selected ─────────────────
                String profileImageUrl = null;
                if (profileUri != null) {
                    profileImageUrl = uploadImageToStorage(profileUri, "profile-images", token);
                }

                // ── Step 3: Insert into artworks table ───────────────────────
                String artworksUrl = SupabaseClient.SUPABASE_URL + "/rest/v1/artworks";

                JSONObject artBody = new JSONObject();
                artBody.put("user_id",     userId);
                artBody.put("name",        artName);
                artBody.put("description", desc);
                artBody.put("art_url",     artImageUrl);
                if (profileImageUrl != null) {
                    artBody.put("profile_url", profileImageUrl);
                }

                makePostRequest(artworksUrl, artBody, token);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Uploaded successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                });

            } catch (Exception e) {
                android.util.Log.e("UPLOAD", "Upload error: " + e.getMessage());
                runOnUiThread(() -> {
                    btnSave.setEnabled(true);
                    btnSave.setText("Save & Go to Home");
                    Toast.makeText(this,
                            "Error: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    // ── Test if bucket is accessible ─────────────────────────────────────────
    private void testBucketAccess(String bucket, String token) {
        try {
            String testUrl = SupabaseClient.SUPABASE_URL + "/storage/v1/bucket/" + bucket;
            URL url = new URL(testUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("apikey",        SupabaseClient.SUPABASE_ANON_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + token);

            int code = conn.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    code >= 200 && code < 300
                            ? conn.getInputStream()
                            : conn.getErrorStream()));
            StringBuilder resp = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) resp.append(line);
            reader.close();
            conn.disconnect();

            android.util.Log.d("UPLOAD", "=== BUCKET TEST [" + bucket + "] ===");
            android.util.Log.d("UPLOAD", "Response code: " + code);
            android.util.Log.d("UPLOAD", "Response body: " + resp);

        } catch (Exception e) {
            android.util.Log.e("UPLOAD", "Bucket test error: " + e.getMessage());
        }
    }

    // ── Upload image to Supabase Storage ─────────────────────────────────────
    private String uploadImageToStorage(Uri imageUri, String bucket, String token) throws Exception {
        // Read image bytes
        InputStream is = getContentResolver().openInputStream(imageUri);
        if (is == null) throw new Exception("Cannot read image file");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(chunk)) != -1) buffer.write(chunk, 0, bytesRead);
        is.close();
        byte[] imageBytes = buffer.toByteArray();

        android.util.Log.d("UPLOAD", "Image size: " + imageBytes.length + " bytes");

        // Get mime type
        String mimeType = getContentResolver().getType(imageUri);
        if (mimeType == null) mimeType = "image/jpeg";

        // Generate unique filename
        String extension = mimeType.equals("image/png") ? ".png" : ".jpg";
        String fileName  = UUID.randomUUID().toString() + extension;

        // Build upload URL
        String uploadUrl = SupabaseClient.SUPABASE_URL
                + "/storage/v1/object/"
                + bucket
                + "/"
                + fileName;

        android.util.Log.d("UPLOAD", "=== UPLOADING IMAGE ===");
        android.util.Log.d("UPLOAD", "URL: " + uploadUrl);
        android.util.Log.d("UPLOAD", "Mime: " + mimeType);

        URL url = new URL(uploadUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("apikey",        SupabaseClient.SUPABASE_ANON_KEY);
        conn.setRequestProperty("Content-Type",  mimeType);
        conn.setRequestProperty("x-upsert",      "true");
        conn.setFixedLengthStreamingMode(imageBytes.length);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(imageBytes);
        }

        int responseCode = conn.getResponseCode();

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                responseCode >= 200 && responseCode < 300
                        ? conn.getInputStream()
                        : conn.getErrorStream()
        ));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) response.append(line);
        reader.close();
        conn.disconnect();

        android.util.Log.d("UPLOAD", "Response code: " + responseCode);
        android.util.Log.d("UPLOAD", "Response body: " + response);

        if (responseCode < 200 || responseCode >= 300) {
            throw new Exception("Image upload failed (" + responseCode + "): " + response);
        }

        // Return public URL
        return SupabaseClient.SUPABASE_URL
                + "/storage/v1/object/public/"
                + bucket
                + "/"
                + fileName;
    }

    // ── Generic POST request ─────────────────────────────────────────────────
    private void makePostRequest(String urlString, JSONObject body, String token) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type",  "application/json");
        conn.setRequestProperty("apikey",        SupabaseClient.SUPABASE_ANON_KEY);
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Prefer",        "return=minimal");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();

        android.util.Log.d("UPLOAD", "=== INSERT ARTWORK ===");
        android.util.Log.d("UPLOAD", "Response code: " + responseCode);

        if (responseCode < 200 || responseCode >= 300) {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();
            conn.disconnect();
            android.util.Log.e("UPLOAD", "Insert error: " + response);
            throw new Exception("Insert failed (" + responseCode + "): " + response);
        }

        conn.disconnect();
    }
}