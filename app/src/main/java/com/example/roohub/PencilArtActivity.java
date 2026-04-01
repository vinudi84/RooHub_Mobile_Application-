package com.example.roohub;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class PencilArtActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TeacherAdapter adapter;
    private ArrayList<String> pencilVideoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pencil_art);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        recyclerView = findViewById(R.id.rvPencilVideos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        pencilVideoList = new ArrayList<>();

        loadVideoData();
    }

    private void loadVideoData() {
        SharedPreferences pref = getSharedPreferences("RooHubData", MODE_PRIVATE);
        // Fetch data specifically for "Pencil art"
        String allData = pref.getString("Pencil art", "");

        if (!allData.isEmpty()) {
            String[] records = allData.split("###");
            for (String record : records) {
                if (!record.isEmpty()) {
                    pencilVideoList.add(record);
                }
            }
        }

        adapter = new TeacherAdapter(pencilVideoList, this);
        recyclerView.setAdapter(adapter);
    }
}