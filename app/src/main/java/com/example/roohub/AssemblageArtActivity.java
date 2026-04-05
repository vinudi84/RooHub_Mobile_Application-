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

public class AssemblageArtActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TeacherAdapter adapter;
    private ArrayList<String> assVideoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assemblage_art);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        recyclerView = findViewById(R.id.rvAssVideos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        assVideoList = new ArrayList<>();

        loadVideoData();
    }

    private void loadVideoData() {
        new Thread(() -> {
            try {
                // API Request for Assemblage Art category
                String urlString = SupabaseClient.SUPABASE_URL
                        + "/rest/v1/course_uploads?course_category=eq.Assemblage Art&select=*";

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
                    assVideoList.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);

                        // Replace null Teacher Name with Anonymous
                        String tName = obj.optString("teacher_name", "Anonymous");
                        if (tName.equalsIgnoreCase("null") || tName.trim().isEmpty()) tName = "Anonymous";

                        // Replace null Art Name with Category Name
                        String aName = obj.optString("art_name", "Assemblage Art");
                        if (aName.equalsIgnoreCase("null") || aName.trim().isEmpty()) aName = "Assemblage Art";

                        String desc = obj.optString("description", "");
                        String vUrl = obj.optString("video_url", "");
                        String email = obj.optString("teacher_email", "");
                        String pImg = obj.optString("profile_image_url", "");

                        String record = tName + "|" + aName + "|" + desc + "|" + vUrl + "|" + email + "|" + pImg;
                        assVideoList.add(record);
                    }

                    runOnUiThread(() -> {
                        adapter = new TeacherAdapter(assVideoList, AssemblageArtActivity.this);
                        recyclerView.setAdapter(adapter);
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}