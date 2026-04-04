package com.example.roohub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtFullName, txtBio;
    private ImageView displayProfileImage;
    private Button btnEditProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // ── Redirect if not logged in ────────────────────────────────────────
        if (!SessionManager.isLoggedIn(this)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        txtFullName          = findViewById(R.id.txtFullName);
        txtBio               = findViewById(R.id.txtBio);
        displayProfileImage  = findViewById(R.id.displayProfileImage);
        btnEditProfile       = findViewById(R.id.btnEditProfile);

        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class)));

        loadProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
    }

    // ── Fetch profile from Supabase ──────────────────────────────────────────
    private void loadProfile() {
        String token  = SessionManager.getToken(this);
        String userId = SessionManager.getUserId(this);

        new Thread(() -> {
            try {
                // ── Query profiles table for logged-in user ──────────────────
                String profileUrl = SupabaseClient.SUPABASE_URL
                        + "/rest/v1/profiles"
                        + "?id=eq." + userId
                        + "&select=full_name,bio,profile_image_url";

                android.util.Log.d("PROFILE", "Fetching: " + profileUrl);

                URL url = new URL(profileUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("apikey",        SupabaseClient.SUPABASE_ANON_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Content-Type",  "application/json");

                int responseCode = conn.getResponseCode();
                android.util.Log.d("PROFILE", "Response code: " + responseCode);

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

                android.util.Log.d("PROFILE", "Response: " + response);

                if (responseCode < 200 || responseCode >= 300) {
                    throw new Exception("Fetch failed (" + responseCode + "): " + response);
                }

                // ── Parse response ───────────────────────────────────────────
                JSONArray array = new JSONArray(response.toString());

                if (array.length() == 0) {
                    throw new Exception("Profile not found");
                }

                JSONObject profileData = array.getJSONObject(0);

                String fullName  = profileData.optString("full_name",         "Your Name Here");
                String bio       = profileData.optString("bio",                "No bio added yet.");
                String imageUrl  = profileData.optString("profile_image_url",  "");

                runOnUiThread(() -> {
                    txtFullName.setText(fullName);
                    txtBio.setText(bio);

                    // ── Load profile image if available ──────────────────────
                    if (!imageUrl.isEmpty()) {
                        Glide.with(this)
                                .load(imageUrl)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_menu_gallery)
                                .circleCrop()
                                .into(displayProfileImage);
                    }
                });

            } catch (Exception e) {
                android.util.Log.e("PROFILE", "Error: " + e.getMessage());
                runOnUiThread(() -> {
                    txtFullName.setText("Could not load profile");
                    txtBio.setText(e.getMessage());
                });
            }
        }).start();
    }
}