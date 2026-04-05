package com.example.roohub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class UploadActivity extends AppCompatActivity {

    private ImageView profilePreview, artImagePreview;
    private EditText etArtName, etDescription;
    private Button btnSave, btnGoToProfile;
    private Spinner spinnerArtistType; //  NEW

    private Uri artUri = null;
    private Uri profileUri = null;

    private final ActivityResultLauncher<String[]> artLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    artUri = uri;
                    artImagePreview.setImageURI(uri);
                }
            });

    private final ActivityResultLauncher<String[]> profileLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    profileUri = uri;
                    profilePreview.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        profilePreview = findViewById(R.id.profilePreview);
        artImagePreview = findViewById(R.id.artImagePreview);
        etArtName = findViewById(R.id.etArtName);
        etDescription = findViewById(R.id.etDescription);
        btnSave = findViewById(R.id.btnSave);
        btnGoToProfile = findViewById(R.id.btnGoToProfile);
        spinnerArtistType = findViewById(R.id.spinnerArtistType); //  NEW

        // Spinner values
        String[] types = {"Freelancer", "Contractor", "Full-time Artist", "Student", "Other"};
           //set values for spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                types
        );

        spinnerArtistType.setAdapter(adapter);

        profilePreview.setOnClickListener(v ->
                profileLauncher.launch(new String[]{"image/*"}));

        artImagePreview.setOnClickListener(v ->
                artLauncher.launch(new String[]{"image/*"}));

        btnGoToProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        btnSave.setOnClickListener(v -> {

            String artName = etArtName.getText().toString().trim();
            String desc = etDescription.getText().toString().trim();
            String artistType = spinnerArtistType.getSelectedItem().toString(); // 🔥 NEW

            if (artName.isEmpty() || desc.isEmpty() || artUri == null) {
                Toast.makeText(this,
                        "Please fill all fields",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            handleUpload(artName, desc, artistType); //  pass value
        });
    }

    private void handleUpload(String artName, String desc, String artistType) {
        new Thread(() -> {
            try {
                String token = SessionManager.getToken(this);
                String userId = SessionManager.getUserId(this);

                String artImageUrl = uploadImageToStorage(artUri, "art-images", token);

                JSONObject artBody = new JSONObject();
                artBody.put("user_id", userId);
                artBody.put("name", artName);
                artBody.put("description", desc);
                artBody.put("art_url", artImageUrl);
                artBody.put("artist_type", artistType); //  NEW

                makePostRequest(
                        SupabaseClient.SUPABASE_URL + "/rest/v1/artworks",
                        artBody,
                        token
                );

                runOnUiThread(() -> {
                    Toast.makeText(this, "Uploaded!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private String uploadImageToStorage(Uri imageUri, String bucket, String token) throws Exception {
        InputStream is = getContentResolver().openInputStream(imageUri);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        byte[] chunk = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(chunk)) != -1) {
            buffer.write(chunk, 0, bytesRead);
        }

        byte[] imageBytes = buffer.toByteArray();

        String fileName = UUID.randomUUID().toString() + ".jpg";

        URL url = new URL(SupabaseClient.SUPABASE_URL +
                "/storage/v1/object/art-images/" + fileName);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("apikey", SupabaseClient.SUPABASE_ANON_KEY);
        conn.setDoOutput(true);

        OutputStream os = conn.getOutputStream();
        os.write(imageBytes);
        os.close();

        if (conn.getResponseCode() != 200) {
            throw new Exception("Upload failed");
        }

        return SupabaseClient.SUPABASE_URL +
                "/storage/v1/object/public/art-images/" + fileName;
    }

    private void makePostRequest(String urlString, JSONObject body, String token) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("apikey", SupabaseClient.SUPABASE_ANON_KEY);
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setDoOutput(true);

        OutputStream os = conn.getOutputStream();
        os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        os.close();

        if (conn.getResponseCode() != 201) {
            throw new Exception("Insert failed");
        }
    }
}