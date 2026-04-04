package com.example.roohub;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    ImageView signIn, logout;
    ImageButton upload;
    LinearLayout profile, course;
    LinearLayout artContainer;

    EditText searchBar;
    JSONArray fullArtList = new JSONArray();
    private Set<String> likedArtIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

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
        searchBar    = findViewById(R.id.searchArt);

        profile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        upload.setOnClickListener(v -> startActivity(new Intent(this, UploadActivity.class)));
        course.setOnClickListener(v -> startActivity(new Intent(this, CourseActivity.class)));
        signIn.setOnClickListener(v -> startActivity(new Intent(this, SelectionTypeActivity.class)));

        logout.setOnClickListener(v -> {
            SessionManager.clearSession(this);
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterArt(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        artContainer.removeAllViews();
        loadDataFromSupabase();
    }

    private void loadDataFromSupabase() {
        String token = SessionManager.getToken(this);
        String userId = SessionManager.getUserId(this);

        new Thread(() -> {
            try {
                // 1. Fetch User's Likes
                URL likesUrl = new URL(SupabaseClient.SUPABASE_URL + "/rest/v1/likes?select=artwork_id&user_id=eq." + userId);
                HttpURLConnection likesConn = (HttpURLConnection) likesUrl.openConnection();
                likesConn.setRequestMethod("GET");
                likesConn.setRequestProperty("apikey", SupabaseClient.SUPABASE_ANON_KEY);
                likesConn.setRequestProperty("Authorization", "Bearer " + token);

                if (likesConn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(likesConn.getInputStream()));
                    StringBuilder res = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) res.append(line);
                    reader.close();

                    JSONArray likesArray = new JSONArray(res.toString());
                    likedArtIds.clear();
                    for (int i = 0; i < likesArray.length(); i++) {
                        likedArtIds.add(likesArray.getJSONObject(i).getString("artwork_id"));
                    }
                }
                likesConn.disconnect();

                // 2. Fetch Artworks
                loadUploadedArt();

            } catch (Exception e) {
                Log.e("FETCH_ERROR", "Error: " + e.getMessage());
            }
        }).start();
    }

    private void loadUploadedArt() {
        String token = SessionManager.getToken(this);
        try {
            // Join with profiles table to get artist details
            String artUrl = SupabaseClient.SUPABASE_URL + "/rest/v1/artworks?select=id,name,description,art_url,user_id,likes_count,profiles(email,full_name)&order=created_at.desc";
            URL url = new URL(artUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("apikey", SupabaseClient.SUPABASE_ANON_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + token);

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                fullArtList = new JSONArray(response.toString());
                runOnUiThread(() -> displayArt(fullArtList));
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayArt(JSONArray artArray) {
        artContainer.removeAllViews();
        for (int i = 0; i < artArray.length(); i++) {
            try {
                JSONObject art = artArray.getJSONObject(i);
                JSONObject profileObj = art.optJSONObject("profiles");

                addArtToUI(
                        art.optString("id", ""),
                        art.optString("name", "Untitled"),
                        profileObj != null ? profileObj.optString("email", "") : "",
                        profileObj != null ? profileObj.optString("full_name", "Artist") : "Artist",
                        art.optString("description", ""),
                        art.optString("art_url", ""),
                        art.optString("user_id", ""),
                        art.optInt("likes_count", 0)
                );
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void addArtToUI(String artId, String name, String ownerEmail, String ownerName,
                            String desc, String artUrl, String userId, int likes) {

        View artView = LayoutInflater.from(this).inflate(R.layout.item_art_card, artContainer, false);

        ImageView img = artView.findViewById(R.id.home_art_image);
        TextView txtName = artView.findViewById(R.id.home_art_name);
        TextView txtType = artView.findViewById(R.id.home_art_type);
        TextView txtDesc = artView.findViewById(R.id.home_art_description);
        ImageView btnLike = artView.findViewById(R.id.btnLike);
        TextView txtLikeCount = artView.findViewById(R.id.txtLikeCount);

        txtName.setText(name);
        txtType.setText("Artist: " + ownerName);
        txtDesc.setText(desc);
        txtLikeCount.setText(String.valueOf(likes));

        // Like Image status
        if (likedArtIds.contains(artId)) {
            btnLike.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            btnLike.setImageResource(android.R.drawable.btn_star_big_off);
        }

        // --- Like Click Logic ---
        btnLike.setOnClickListener(v -> {
            int currentCount = Integer.parseInt(txtLikeCount.getText().toString());
            boolean isLikedNow = likedArtIds.contains(artId);

            if (!isLikedNow) {
                int newCount = currentCount + 1;
                txtLikeCount.setText(String.valueOf(newCount));
                btnLike.setImageResource(android.R.drawable.btn_star_big_on);
                likedArtIds.add(artId);
                toggleLikeInSupabase(artId, true, newCount);
            } else {
                int newCount = Math.max(0, currentCount - 1);
                txtLikeCount.setText(String.valueOf(newCount));
                btnLike.setImageResource(android.R.drawable.btn_star_big_off);
                likedArtIds.remove(artId);
                toggleLikeInSupabase(artId, false, newCount);
            }
        });

        // --- Card Click Logic (To View Artist Profile) ---
        artView.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, UserProfileActivity.class);
            intent.putExtra("LOGGED_EMAIL", ownerEmail);
            intent.putExtra("USER_ID",      userId);
            intent.putExtra("MODE",         "VIEW_ONLY");
            startActivity(intent);
        });

        if (!artUrl.isEmpty()) {
            Glide.with(this).load(artUrl).placeholder(android.R.drawable.ic_menu_gallery).into(img);
        }

        artContainer.addView(artView);
    }

    private void toggleLikeInSupabase(String artId, boolean isLikeAction, int newCount) {
        String token = SessionManager.getToken(this);
        String userId = SessionManager.getUserId(this);

        new Thread(() -> {
            try {
                // 1. PATCH artwork likes_count
                URL updateUrl = new URL(SupabaseClient.SUPABASE_URL + "/rest/v1/artworks?id=eq." + artId);
                HttpURLConnection updateConn = (HttpURLConnection) updateUrl.openConnection();
                updateConn.setRequestMethod("PATCH");
                updateConn.setRequestProperty("apikey", SupabaseClient.SUPABASE_ANON_KEY);
                updateConn.setRequestProperty("Authorization", "Bearer " + token);
                updateConn.setRequestProperty("Content-Type", "application/json");
                updateConn.setRequestProperty("Prefer", "return=minimal");
                updateConn.setDoOutput(true);

                String jsonCount = "{\"likes_count\": " + newCount + "}";
                try (OutputStream os = updateConn.getOutputStream()) {
                    os.write(jsonCount.getBytes(StandardCharsets.UTF_8));
                }
                updateConn.getResponseCode();
                updateConn.disconnect();

                // 2. Add/Remove from likes table
                if (isLikeAction) {
                    URL postUrl = new URL(SupabaseClient.SUPABASE_URL + "/rest/v1/likes");
                    HttpURLConnection postConn = (HttpURLConnection) postUrl.openConnection();
                    postConn.setRequestMethod("POST");
                    postConn.setRequestProperty("apikey", SupabaseClient.SUPABASE_ANON_KEY);
                    postConn.setRequestProperty("Authorization", "Bearer " + token);
                    postConn.setRequestProperty("Content-Type", "application/json");
                    postConn.setDoOutput(true);

                    String jsonLike = "{\"user_id\": \"" + userId + "\", \"artwork_id\": \"" + artId + "\"}";
                    try (OutputStream os = postConn.getOutputStream()) {
                        os.write(jsonLike.getBytes(StandardCharsets.UTF_8));
                    }
                    postConn.disconnect();
                } else {
                    URL deleteUrl = new URL(SupabaseClient.SUPABASE_URL + "/rest/v1/likes?user_id=eq." + userId + "&artwork_id=eq." + artId);
                    HttpURLConnection deleteConn = (HttpURLConnection) deleteUrl.openConnection();
                    deleteConn.setRequestMethod("DELETE");
                    deleteConn.setRequestProperty("apikey", SupabaseClient.SUPABASE_ANON_KEY);
                    deleteConn.setRequestProperty("Authorization", "Bearer " + token);
                    deleteConn.disconnect();
                }
            } catch (Exception e) {
                Log.e("SUPABASE_DEBUG", "Error: " + e.getMessage());
            }
        }).start();
    }

    private void filterArt(String query) {
        JSONArray filteredList = new JSONArray();
        for (int i = 0; i < fullArtList.length(); i++) {
            try {
                JSONObject art = fullArtList.getJSONObject(i);
                if (art.optString("name", "").toLowerCase().contains(query.toLowerCase())) {
                    filteredList.put(art);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
        displayArt(filteredList);
    }
}