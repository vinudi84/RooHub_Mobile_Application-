package com.example.roohub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HomeActivity extends AppCompatActivity {

    ImageView signIn, logout;
    ImageButton upload;
    LinearLayout profile, course;
    LinearLayout artContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // ── Redirect if not logged in ────────────────────────────────────────
        if (!SessionManager.isLoggedIn(this)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        signIn       = findViewById(R.id.btnSignIn);
        logout       = findViewById(R.id.btnLogout);
        upload       = findViewById(R.id.btnUpload);
        profile      = findViewById(R.id.btnProfile);
        course       = findViewById(R.id.btnCourse);
        artContainer = findViewById(R.id.art_list_container);

        profile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        upload.setOnClickListener(v ->
                startActivity(new Intent(this, UploadActivity.class)));

        course.setOnClickListener(v ->
                startActivity(new Intent(this, CourseActivity.class)));

        signIn.setOnClickListener(v ->
                startActivity(new Intent(this, SelectionTypeActivity.class)));

        logout.setOnClickListener(v -> {
            SessionManager.clearSession(this);
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUploadedArt();
    }

    // ── Fetch artworks from Supabase — newest first ──────────────────────────
    private void loadUploadedArt() {
        String token = SessionManager.getToken(this);

        new Thread(() -> {
            try {
                // ── Join artworks with profiles to get artist email ──────────
                // order=created_at.desc puts newest on top
                String artUrl = SupabaseClient.SUPABASE_URL
                        + "/rest/v1/artworks"
                        + "?select=id,name,description,art_url,user_id,profiles(email,full_name)"
                        + "&order=created_at.desc";

                android.util.Log.d("HOME", "Fetching: " + artUrl);

                URL url = new URL(artUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("apikey",        SupabaseClient.SUPABASE_ANON_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Content-Type",  "application/json");

                int responseCode = conn.getResponseCode();
                android.util.Log.d("HOME", "Response code: " + responseCode);

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

                android.util.Log.d("HOME", "Response: " + response);

                if (responseCode < 200 || responseCode >= 300) {
                    throw new Exception("Fetch failed (" + responseCode + "): " + response);
                }

                JSONArray artArray = new JSONArray(response.toString());
                android.util.Log.d("HOME", "Art count: " + artArray.length());

                runOnUiThread(() -> {
                    artContainer.removeAllViews();

                    if (artArray.length() == 0) {
                        // Show empty state
                        TextView empty = new TextView(this);
                        empty.setText("No artworks yet. Be the first to upload!");
                        empty.setTextSize(16);
                        empty.setPadding(32, 64, 32, 32);
                        artContainer.addView(empty);
                        return;
                    }

                    for (int i = 0; i < artArray.length(); i++) {
                        try {
                            JSONObject art = artArray.getJSONObject(i);

                            String artId   = art.optString("id", "");
                            String name    = art.optString("name", "Untitled");
                            String desc    = art.optString("description", "");
                            String artUrl2 = art.optString("art_url", "");
                            String userId  = art.optString("user_id", "");

                            // ── Get artist info from joined profiles ─────────
                            JSONObject profileObj = art.optJSONObject("profiles");
                            String ownerEmail     = "Unknown";
                            String ownerName      = "Unknown Artist";

                            if (profileObj != null) {
                                ownerEmail = profileObj.optString("email",     "Unknown");
                                ownerName  = profileObj.optString("full_name", "Unknown Artist");
                            }

                            addArtToUI(artId, name, ownerEmail, ownerName, desc, artUrl2, userId);

                        } catch (Exception e) {
                            android.util.Log.e("HOME", "Error parsing art item: " + e.getMessage());
                        }
                    }
                });

            } catch (Exception e) {
                android.util.Log.e("HOME", "Load art error: " + e.getMessage());
                runOnUiThread(() -> {
                    artContainer.removeAllViews();
                    TextView error = new TextView(this);
                    error.setText("Could not load artworks. Check your connection.");
                    error.setTextSize(14);
                    error.setPadding(32, 64, 32, 32);
                    artContainer.addView(error);
                });
            }
        }).start();
    }

    // ── Create and add art card to screen ────────────────────────────────────
    private void addArtToUI(String artId, String name, String ownerEmail,
                            String ownerName, String desc, String artUrl, String userId) {

        View artView = LayoutInflater.from(this)
                .inflate(R.layout.item_art_card, artContainer, false);

        ImageView img    = artView.findViewById(R.id.home_art_image);
        TextView txtName = artView.findViewById(R.id.home_art_name);
        TextView txtType = artView.findViewById(R.id.home_art_type);
        TextView txtDesc = artView.findViewById(R.id.home_art_description);

        txtName.setText(name);
        txtType.setText("Artist: " + ownerName);
        txtDesc.setText(desc);

        // ── Load image with Glide ────────────────────────────────────────────
        if (!artUrl.isEmpty()) {
            Glide.with(this)
                    .load(artUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(img);
        }

        // ── Click to view artist profile ─────────────────────────────────────
        artView.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, UserProfileActivity.class);
            intent.putExtra("LOGGED_EMAIL", ownerEmail);
            intent.putExtra("USER_ID",      userId);
            intent.putExtra("MODE",         "VIEW_ONLY");
            startActivity(intent);
        });

        artContainer.addView(artView);
    }
}