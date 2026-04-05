package com.example.roohub;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ColorArtActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TeacherAdapter adapter;
    private ArrayList<String> colorVideoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_art);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        recyclerView = findViewById(R.id.rvColorVideos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        colorVideoList = new ArrayList<>();

        loadVideoData();
    }

    private void loadVideoData() {
        new Thread(() -> {
            try {
                // ── Added 'is_active' column to the URL query ──────────────────────
                String urlString = SupabaseClient.SUPABASE_URL
                        + "/rest/v1/course_uploads"
                        + "?course_category=eq.Coloring"
                        + "&select=teacher_name,art_name,description,video_url,teacher_email,profile_image_url,is_active";

                android.util.Log.d("COLOR", "Fetching: " + urlString);

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("apikey",        SupabaseClient.SUPABASE_ANON_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + SupabaseClient.SUPABASE_ANON_KEY);
                conn.setRequestProperty("Content-Type",  "application/json");

                int responseCode = conn.getResponseCode();
                android.util.Log.d("COLOR", "Response code: " + responseCode);

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

                android.util.Log.d("COLOR", "Response: " + response);

                if (responseCode >= 200 && responseCode < 300) {
                    JSONArray jsonArray = new JSONArray(response.toString());

                    colorVideoList.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);

                        // 1. Check the status (Defaults to true if column is missing)
                        boolean isActive = obj.optBoolean("is_active", true);

                        // 2. Add data only if the course is Active
                        if (isActive) {
                            String teacherName    = obj.optString("teacher_name",      "Unknown");
                            String artName        = obj.optString("art_name",          "Coloring");
                            String desc           = obj.optString("description",       "");
                            String videoUrl       = obj.optString("video_url",         "");
                            String teacherEmail   = obj.optString("teacher_email",     "");
                            String profileImgUrl  = obj.optString("profile_image_url", "");

                            android.util.Log.d("COLOR", "Video URL: " + videoUrl);

                            // Format: Name|ArtName|Desc|VideoUri|Email|ProfileImageUri
                            String record = teacherName + "|"
                                    + artName       + "|"
                                    + desc          + "|"
                                    + videoUrl      + "|"
                                    + teacherEmail  + "|"
                                    + profileImgUrl;

                            if (!videoUrl.isEmpty()) {
                                colorVideoList.add(record);
                            }
                        }
                    }

                    runOnUiThread(() -> {
                        if (colorVideoList.isEmpty()) {
                            Toast.makeText(this,
                                    "No active Coloring Art videos available yet.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        adapter = new TeacherAdapter(colorVideoList, ColorArtActivity.this);
                        recyclerView.setAdapter(adapter);
                    });

                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this,
                                    "Error loading videos: " + responseCode,
                                    Toast.LENGTH_SHORT).show()
                    );
                }

            } catch (Exception e) {
                android.util.Log.e("COLOR", "Error: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }
}