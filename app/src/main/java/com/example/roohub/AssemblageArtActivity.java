package com.example.roohub;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class AssemblageArtActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TeacherAdapter adapter;
    private ArrayList<String> assVideoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assemblage_art);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        recyclerView = findViewById(R.id.rvAssVideos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        assVideoList = new ArrayList<>();

        loadVideoData();
    }

    private void loadVideoData() {
        SharedPreferences pref = getSharedPreferences("RooHubData", MODE_PRIVATE);
        // Fetch data specifically for "Assemblage art"
        String allData = pref.getString("Assemblage art", "");

        if (!allData.isEmpty()) {
            String[] records = allData.split("###");
            for (String record : records) {
                if (!record.isEmpty()) {
                    assVideoList.add(record);
                }
            }
        }

        adapter = new TeacherAdapter(assVideoList, this);
        recyclerView.setAdapter(adapter);
    }
}