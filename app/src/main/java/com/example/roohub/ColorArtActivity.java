package com.example.roohub;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class ColorArtActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TeacherAdapter adapter;
    private ArrayList<TeacherDataStore.Teacher> colorTeachers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_art);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        recyclerView = findViewById(R.id.rvColorVideos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        colorTeachers = new ArrayList<>();

        loadTeacherList();
    }

    private void loadTeacherList() {
        // 1. Fetch saved JSON data
        SharedPreferences pref = getSharedPreferences("RooHubData", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = pref.getString("teachers_list", null);

        if (json != null) {
            Type type = new TypeToken<ArrayList<TeacherDataStore.Teacher>>() {}.getType();
            ArrayList<TeacherDataStore.Teacher> allTeachers = gson.fromJson(json, type);

            // 2. Filter for "Coloring art"
            for (TeacherDataStore.Teacher t : allTeachers) {
                if (t.artType.equals("Coloring art")) {
                    colorTeachers.add(t);
                }
            }
        }

        adapter = new TeacherAdapter(colorTeachers, this);
        recyclerView.setAdapter(adapter);
    }
}