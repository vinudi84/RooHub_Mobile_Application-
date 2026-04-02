package com.example.roohub;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ColorArtActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TeacherAdapter adapter;
    private ArrayList<String> colorVideoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_art);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // --- STEP 1: INITIALIZE UI ---
        recyclerView = findViewById(R.id.rvColorVideos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        colorVideoList = new ArrayList<>();

        // --- STEP 2: LOAD DATA FROM CATEGORY KEY ---
        loadVideoData();
    }

    private void loadVideoData() {
        SharedPreferences pref = getSharedPreferences("RooHubData", MODE_PRIVATE);
        // Directly fetch the string saved for this specific category
        String allData = pref.getString("Coloring art", "");

        if (!allData.isEmpty()) {
            // Split the large string into individual records using "###"
            String[] records = allData.split("###");
            for (String record : records) {
                if (!record.isEmpty()) {
                    colorVideoList.add(record);
                }
            }
        }

        // --- STEP 3: SETUP ADAPTER ---
        adapter = new TeacherAdapter(colorVideoList, this);
        recyclerView.setAdapter(adapter);
    }
}