package com.example.roohub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class ManageArtActivity extends AppCompatActivity {

    private LinearLayout artContainer;
    private String token, userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_art);

        if (!SessionManager.isLoggedIn(this)) {
            finish();
            return;
        }

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        artContainer = findViewById(R.id.artContainer);
        token        = SessionManager.getToken(this);
        userId       = SessionManager.getUserId(this);

        loadMyArtworks();
    }

    private void loadMyArtworks() {
        new Thread(() -> {
            try {
                String artUrl = SupabaseClient.SUPABASE_URL
                        + "/rest/v1/artworks"
                        + "?user_id=eq." + userId
                        + "&select=id,name,description,art_url"
                        + "&order=created_at.desc";

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

                JSONArray array = new JSONArray(response.toString());

                runOnUiThread(() -> {
                    artContainer.removeAllViews();

                    if (array.length() == 0) {
                        TextView empty = new TextView(this);
                        empty.setText("You have no artworks yet.");
                        empty.setPadding(16, 32, 16, 32);
                        artContainer.addView(empty);
                        return;
                    }

                    for (int i = 0; i < array.length(); i++) {
                        try {
                            JSONObject art = array.getJSONObject(i);
                            String artId   = art.optString("id",          "");
                            String name    = art.optString("name",        "");
                            String desc    = art.optString("description", "");
                            String artImg  = art.optString("art_url",     "");
                            addArtEditCard(artId, name, desc, artImg);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                android.util.Log.e("MANAGE_ART", "Error: " + e.getMessage());
            }
        }).start();
    }

    private void addArtEditCard(String artId, String name, String desc, String artImg) {
        View card = LayoutInflater.from(this)
                .inflate(R.layout.item_manage_art, artContainer, false);

        ImageView img      = card.findViewById(R.id.manageArtImage);
        EditText etName    = card.findViewById(R.id.manageArtName);
        EditText etDesc    = card.findViewById(R.id.manageArtDesc);
        Button btnUpdate   = card.findViewById(R.id.btnUpdateArt);
        Button btnDelete   = card.findViewById(R.id.btnDeleteArt);

        etName.setText(name);
        etDesc.setText(desc);

        if (!artImg.isEmpty()) {
            Glide.with(this)
                    .load(artImg)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(img);
        }

        // ── Update artwork ───────────────────────────────────────────────────
        btnUpdate.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            String newDesc = etDesc.getText().toString().trim();

            if (newName.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            updateArtwork(artId, newName, newDesc, card);
        });

        // ── Delete artwork ───────────────────────────────────────────────────
        btnDelete.setOnClickListener(v ->
                deleteArtwork(artId, card));

        artContainer.addView(card);
    }

    private void updateArtwork(String artId, String name, String desc, View card) {
        new Thread(() -> {
            try {
                String updateUrl = SupabaseClient.SUPABASE_URL
                        + "/rest/v1/artworks"
                        + "?id=eq." + artId;

                JSONObject body = new JSONObject();
                body.put("name",        name);
                body.put("description", desc);

                makePatchRequest(updateUrl, body);

                runOnUiThread(() ->
                        Toast.makeText(this, "Artwork updated!", Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void deleteArtwork(String artId, View card) {
        new Thread(() -> {
            try {
                String deleteUrl = SupabaseClient.SUPABASE_URL
                        + "/rest/v1/artworks"
                        + "?id=eq." + artId;

                URL url = new URL(deleteUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setRequestProperty("apikey",        SupabaseClient.SUPABASE_ANON_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Prefer",        "return=minimal");

                int responseCode = conn.getResponseCode();
                conn.disconnect();

                if (responseCode >= 200 && responseCode < 300) {
                    runOnUiThread(() -> {
                        artContainer.removeView(card);
                        Toast.makeText(this, "Artwork deleted!", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    throw new Exception("Delete failed: " + responseCode);
                }

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void makePatchRequest(String urlString, JSONObject body) throws Exception {
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
            throw new Exception("Patch failed (" + responseCode + "): " + resp);
        }
        conn.disconnect();
    }
}