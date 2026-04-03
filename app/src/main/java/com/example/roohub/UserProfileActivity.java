package com.example.roohub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView imgShowProfile;
    private TextView txtTitle, txtArtistName, txtArtistBio, txtArtistEmail;
    private Button btnBack;
    private LinearLayout artListLayout;

    private String ownerEmail, userId, mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        imgShowProfile  = findViewById(R.id.imgShowProfile);
        txtTitle        = findViewById(R.id.txtTitle);
        txtArtistName   = findViewById(R.id.txtArtistName);
        txtArtistBio    = findViewById(R.id.txtArtistBio);
        txtArtistEmail  = findViewById(R.id.txtArtistEmail);
        btnBack         = findViewById(R.id.btnBack);
        artListLayout   = findViewById(R.id.artListLayout);

        // ── Get data passed from HomeActivity ────────────────────────────────
        ownerEmail = getIntent().getStringExtra("LOGGED_EMAIL");
        userId     = getIntent().getStringExtra("USER_ID");
        mode       = getIntent().getStringExtra("MODE");

        if ("VIEW_ONLY".equals(mode)) {
            txtTitle.setText("Artist Profile");
        }

        btnBack.setOnClickListener(v -> finish());

        // ── Load artist profile and their artworks ───────────────────────────
        loadArtistProfile();
        loadArtistArtworks();
    }

    // ── Fetch artist profile from profiles table ─────────────────────────────
    private void loadArtistProfile() {
        String token = SessionManager.getToken(this);

        new Thread(() -> {
            try {
                String profileUrl = SupabaseClient.SUPABASE_URL
                        + "/rest/v1/profiles"
                        + "?id=eq." + userId
                        + "&select=full_name,bio,email,profile_image_url";

                android.util.Log.d("USER_PROFILE", "Fetching profile: " + profileUrl);

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

                android.util.Log.d("USER_PROFILE", "Profile response: " + response);

                JSONArray array = new JSONArray(response.toString());
                if (array.length() == 0) throw new Exception("Profile not found");

                JSONObject profile  = array.getJSONObject(0);
                String fullName     = profile.optString("full_name",        "Unknown Artist");
                String bio          = profile.optString("bio",               "No bio yet.");
                String email        = profile.optString("email",             "");
                String profileImage = profile.optString("profile_image_url", "");

                runOnUiThread(() -> {
                    txtArtistName.setText(fullName);
                    txtArtistBio.setText(bio);
                    txtArtistEmail.setText(email);

                    if (!profileImage.isEmpty()) {
                        Glide.with(this)
                                .load(profileImage)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .circleCrop()
                                .into(imgShowProfile);
                    }
                });

            } catch (Exception e) {
                android.util.Log.e("USER_PROFILE", "Profile error: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(this, "Could not load profile", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    // ── Fetch all artworks by this artist ────────────────────────────────────
    private void loadArtistArtworks() {
        String token = SessionManager.getToken(this);

        new Thread(() -> {
            try {
                String artUrl = SupabaseClient.SUPABASE_URL
                        + "/rest/v1/artworks"
                        + "?user_id=eq." + userId
                        + "&select=id,name,description,art_url"
                        + "&order=created_at.desc";

                android.util.Log.d("USER_PROFILE", "Fetching artworks: " + artUrl);

                URL url = new URL(artUrl);
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

                android.util.Log.d("USER_PROFILE", "Artworks response: " + response);

                JSONArray artArray = new JSONArray(response.toString());

                runOnUiThread(() -> {
                    artListLayout.removeAllViews();

                    if (artArray.length() == 0) {
                        TextView empty = new TextView(this);
                        empty.setText("No artworks uploaded yet.");
                        empty.setTextSize(14);
                        empty.setPadding(16, 32, 16, 32);
                        artListLayout.addView(empty);
                        return;
                    }

                    for (int i = 0; i < artArray.length(); i++) {
                        try {
                            JSONObject art = artArray.getJSONObject(i);
                            String name    = art.optString("name",        "Untitled");
                            String desc    = art.optString("description",  "");
                            String artImg  = art.optString("art_url",      "");

                            addArtCard(name, desc, artImg);
                        } catch (Exception e) {
                            android.util.Log.e("USER_PROFILE", "Art parse error: " + e.getMessage());
                        }
                    }
                });

            } catch (Exception e) {
                android.util.Log.e("USER_PROFILE", "Artworks error: " + e.getMessage());
            }
        }).start();
    }

    // ── Create artwork card UI ───────────────────────────────────────────────
    private void addArtCard(String name, String desc, String artUrl) {
        View card = getLayoutInflater().inflate(R.layout.item_art_card, artListLayout, false);

        ImageView img    = card.findViewById(R.id.home_art_image);
        TextView txtName = card.findViewById(R.id.home_art_name);
        TextView txtDesc = card.findViewById(R.id.home_art_description);
        TextView txtType = card.findViewById(R.id.home_art_type);

        txtName.setText(name);
        txtDesc.setText(desc);
        txtType.setVisibility(View.GONE); // hide "Artist:" label on artist's own profile page

        if (!artUrl.isEmpty()) {
            Glide.with(this)
                    .load(artUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(img);
        }

        artListLayout.addView(card);
    }
}