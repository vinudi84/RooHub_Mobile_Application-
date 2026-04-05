package com.example.roohub;

import android.os.Bundle;
import android.widget.Toast;
import android.util.Log;

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

public class PencilArtActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TeacherAdapter adapter;
    private ArrayList<String> pencilVideoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pencil_art);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        recyclerView = findViewById(R.id.rvPencilVideos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        pencilVideoList = new ArrayList<>();

        loadVideoData();
    }

    private void loadVideoData() {
        new Thread(() -> {
            try {
                // 1. Added 'is_active' column to the selection query
                String urlString = SupabaseClient.SUPABASE_URL
                        + "/rest/v1/course_uploads"
                        + "?course_category=eq.Pencil Art"
                        + "&select=teacher_name,art_name,description,video_url,teacher_email,profile_image_url,is_active";

                Log.d("PENCIL", "Fetching: " + urlString);

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("apikey",        SupabaseClient.SUPABASE_ANON_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + SupabaseClient.SUPABASE_ANON_KEY);
                conn.setRequestProperty("Content-Type",  "application/json");

                int responseCode = conn.getResponseCode();
                Log.d("PENCIL", "Response code: " + responseCode);

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

                if (responseCode >= 200 && responseCode < 300) {
                    JSONArray jsonArray = new JSONArray(response.toString());
                    pencilVideoList.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);

                        // 2. Retrieve the 'is_active' value (Defaults to true if missing)
                        boolean isActive = obj.optBoolean("is_active", true);

                        // 3. Add to the list ONLY if the record is ACTIVE
                        if (isActive) {
                            String teacherName   = obj.optString("teacher_name",      "Unknown");
                            String artName       = obj.optString("art_name",          "Pencil Art");
                            String desc          = obj.optString("description",       "");
                            String videoUrl      = obj.optString("video_url",         "");
                            String teacherEmail  = obj.optString("teacher_email",     "");
                            String profileImgUrl = obj.optString("profile_image_url", "");

                            Log.d("PENCIL", "Video URL: " + videoUrl);

                            // Format: Name|ArtName|Desc|VideoUri|Email|ProfileImageUri
                            String record = teacherName  + "|"
                                    + artName       + "|"
                                    + desc          + "|"
                                    + videoUrl      + "|"
                                    + teacherEmail  + "|"
                                    + profileImgUrl;

                            if (!videoUrl.isEmpty()) {
                                pencilVideoList.add(record);
                            }
                        }
                    }

                    runOnUiThread(() -> {
                        if (pencilVideoList.isEmpty()) {
                            Toast.makeText(this, "No active Pencil Art videos available.", Toast.LENGTH_SHORT).show();
                        }
                        adapter = new TeacherAdapter(pencilVideoList, PencilArtActivity.this);
                        recyclerView.setAdapter(adapter);
                    });

                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Error loading: " + responseCode, Toast.LENGTH_SHORT).show()
                    );
                }

            } catch (Exception e) {
                Log.e("PENCIL", "Error: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }
}