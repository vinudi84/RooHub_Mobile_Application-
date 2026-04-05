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

public class PencilArtActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TeacherAdapter adapter;
    private ArrayList<String> pencilVideoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pencil_art);

        // Hide action bar for a cleaner look
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Initialize UI components
        recyclerView = findViewById(R.id.rvPencilVideos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        pencilVideoList = new ArrayList<>();

        // Fetch data from database
        loadVideoData();
    }

    private void loadVideoData() {
        new Thread(() -> {
            try {
                // Supabase API URL filtered by 'Pencil Art' category
                String urlString = SupabaseClient.SUPABASE_URL
                        + "/rest/v1/course_uploads?course_category=eq.Pencil Art&select=*";

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
                    pencilVideoList.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);

                        // Validate Teacher Name
                        String tName = obj.optString("teacher_name", "Anonymous");
                        if (tName.equalsIgnoreCase("null") || tName.trim().isEmpty()) tName = "Anonymous";

                        // Validate Art Name (Handle null case)
                        String aName = obj.optString("art_name", "Pencil Art");
                        if (aName.equalsIgnoreCase("null") || aName.trim().isEmpty()) aName = "Pencil Art";

                        String desc = obj.optString("description", "");
                        String vUrl = obj.optString("video_url", "");
                        String email = obj.optString("teacher_email", "");
                        String pImg = obj.optString("profile_image_url", "");

                        // Join data using pipes for the adapter to split later
                        String record = tName + "|" + aName + "|" + desc + "|" + vUrl + "|" + email + "|" + pImg;
                        pencilVideoList.add(record);
                    }

                    // Refresh RecyclerView on Main Thread
                    runOnUiThread(() -> {
                        adapter = new TeacherAdapter(pencilVideoList, PencilArtActivity.this);
                        recyclerView.setAdapter(adapter);
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}