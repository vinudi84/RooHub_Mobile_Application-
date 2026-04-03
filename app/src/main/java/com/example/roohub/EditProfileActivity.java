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

import org.json.JSONArray;
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

public class EditProfileActivity extends AppCompatActivity {

    private EditText etFullName, etBio;
    private ImageView imgProfile;
    private Button btnSave, btnManageArt, btnManageCourses;

    private Uri newProfileUri = null;
    private String currentImageUrl = "";

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

        if (!SessionManager.isLoggedIn(this)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        etFullName      = findViewById(R.id.etFullName);
        etBio           = findViewById(R.id.etBio);
        imgProfile      = findViewById(R.id.imgEditProfile);
        btnSave         = findViewById(R.id.btnSaveProfile);
        btnManageArt    = findViewById(R.id.btnManageArt);
        btnManageCourses = findViewById(R.id.btnManageCourses);

        // ── Load current profile data ────────────────────────────────────────
        loadCurrentProfile();

        // ── Pick new profile image ───────────────────────────────────────────
        imgProfile.setOnClickListener(v ->
                imageLauncher.launch(new String[]{"image/*"}));

        // ── Save profile changes ─────────────────────────────────────────────
        btnSave.setOnClickListener(v -> {
            String name = etFullName.getText().toString().trim();
            String bio  = etBio.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSave.setEnabled(false);
            btnSave.setText("Saving...");
            saveProfile(name, bio);
        });

        // ── Go to manage artworks ────────────────────────────────────────────
        btnManageArt.setOnClickListener(v ->
                startActivity(new Intent(this, ManageArtActivity.class)));

        // ── Go to manage courses ─────────────────────────────────────────────
        btnManageCourses.setOnClickListener(v ->
                startActivity(new Intent(this, ManageCoursesActivity.class)));
    }

    // ── Load existing profile into fields ────────────────────────────────────
    private void loadCurrentProfile() {
        String token  = SessionManager.getToken(this);
        String userId = SessionManager.getUserId(this);

        new Thread(() -> {
            try {
                String profileUrl = SupabaseClient.SUPABASE_URL
                        + "/rest/v1/profiles"
                        + "?id=eq." + userId
                        + "&select=full_name,bio,profile_image_url";

                URL url = new URL(profileUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("apikey",        SupabaseClient.SUPABASE_ANON_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Content-Type",  "application/json");

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

                JSONArray array = new JSONArray(response.toString());
                if (array.length() == 0) return;

                JSONObject profile = array.getJSONObject(0);
                String fullName    = profile.optString("full_name",        "");
                String bio         = profile.optString("bio",               "");
                currentImageUrl    = profile.optString("profile_image_url", "");

                runOnUiThread(() -> {
                    etFullName.setText(fullName);
                    etBio.setText(bio);

                    if (!currentImageUrl.isEmpty()) {
                        Glide.with(this)
                                .load(currentImageUrl)
                                .circleCrop()
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .into(imgProfile);
                    }
                });

            } catch (Exception e) {
                android.util.Log.e("EDIT_PROFILE", "Load error: " + e.getMessage());
            }
        }).start();
    }

    // ── Save profile to Supabase ─────────────────────────────────────────────
    private void saveProfile(String name, String bio) {
        String token  = SessionManager.getToken(this);
        String userId = SessionManager.getUserId(this);

        new Thread(() -> {
            try {
                String finalImageUrl = currentImageUrl;

                // ── Upload new profile image if selected ─────────────────────
                if (newProfileUri != null) {
                    finalImageUrl = uploadImage(newProfileUri, "profile-images", token);
                }

                // ── Update profiles table ────────────────────────────────────
                String updateUrl = SupabaseClient.SUPABASE_URL
                        + "/rest/v1/profiles"
                        + "?id=eq." + userId;

                JSONObject body = new JSONObject();
                body.put("full_name",         name);
                body.put("bio",               bio);
                body.put("profile_image_url", finalImageUrl);

                makePatchRequest(updateUrl, body, token);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                    finish();
                });

            } catch (Exception e) {
                android.util.Log.e("EDIT_PROFILE", "Save error: " + e.getMessage());
                runOnUiThread(() -> {
                    btnSave.setEnabled(true);
                    btnSave.setText("Save");
                    Toast.makeText(this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    // ── Upload image to Supabase Storage ─────────────────────────────────────
    private String uploadImage(Uri imageUri, String bucket, String token) throws Exception {
        InputStream is = getContentResolver().openInputStream(imageUri);
        if (is == null) throw new Exception("Cannot read image");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(chunk)) != -1) buffer.write(chunk, 0, bytesRead);
        is.close();
        byte[] imageBytes = buffer.toByteArray();

        String mimeType  = getContentResolver().getType(imageUri);
        if (mimeType == null) mimeType = "image/jpeg";
        String extension = mimeType.equals("image/png") ? ".png" : ".jpg";
        String fileName  = UUID.randomUUID().toString() + extension;

        String uploadUrl = SupabaseClient.SUPABASE_URL
                + "/storage/v1/object/" + bucket + "/" + fileName;

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
        if (responseCode < 200 || responseCode >= 300) {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream()));
            StringBuilder resp = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) resp.append(line);
            throw new Exception("Image upload failed (" + responseCode + "): " + resp);
        }
        conn.disconnect();

        return SupabaseClient.SUPABASE_URL
                + "/storage/v1/object/public/" + bucket + "/" + fileName;
    }

    // ── PATCH request to update record ───────────────────────────────────────
    private void makePatchRequest(String urlString, JSONObject body, String token) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PATCH");
        conn.setRequestProperty("Content-Type",  "application/json");
        conn.setRequestProperty("apikey",        SupabaseClient.SUPABASE_ANON_KEY);
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Prefer",        "return=minimal");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream()));
            StringBuilder resp = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) resp.append(line);
            conn.disconnect();
            throw new Exception("Update failed (" + responseCode + "): " + resp);
        }
        conn.disconnect();
    }
}