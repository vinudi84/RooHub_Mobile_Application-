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
                // API Request for Coloring category
                String urlString = SupabaseClient.SUPABASE_URL
                        + "/rest/v1/course_uploads?course_category=eq.Coloring&select=*";

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("apikey", SupabaseClient.SUPABASE_ANON_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + SupabaseClient.SUPABASE_ANON_KEY);

                int responseCode = conn.getResponseCode();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        responseCode == 200 ? conn.getInputStream() : conn.getErrorStream()));

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                if (responseCode == 200) {
                    JSONArray jsonArray = new JSONArray(response.toString());
                    colorVideoList.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);

                        // Null Check for Teacher Name
                        String tName = obj.optString("teacher_name", "Anonymous");
                        if (tName.equalsIgnoreCase("null") || tName.trim().isEmpty()) tName = "Anonymous";

                        // Null Check for Art Name
                        String aName = obj.optString("art_name", "Coloring Art");
                        if (aName.equalsIgnoreCase("null") || aName.trim().isEmpty()) aName = "Coloring Art";

                        String desc = obj.optString("description", "");
                        String vUrl = obj.optString("video_url", "");
                        String email = obj.optString("teacher_email", "");
                        String pImg = obj.optString("profile_image_url", "");

                        String record = tName + "|" + aName + "|" + desc + "|" + vUrl + "|" + email + "|" + pImg;
                        colorVideoList.add(record);
                    }

                    runOnUiThread(() -> {
                        adapter = new TeacherAdapter(colorVideoList, ColorArtActivity.this);
                        recyclerView.setAdapter(adapter);
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}