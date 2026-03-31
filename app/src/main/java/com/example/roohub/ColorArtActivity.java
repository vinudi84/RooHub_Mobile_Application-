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

        // Hide action bar for a cleaner UI
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // --- STEP 1: INITIALIZE UI COMPONENTS ---
        recyclerView = findViewById(R.id.rvColorVideos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        colorTeachers = new ArrayList<>();

        // --- STEP 2: LOAD DATA ---
        loadTeacherList();
    }

    private void loadTeacherList() {
        // Fetch saved JSON data from SharedPreferences
        SharedPreferences pref = getSharedPreferences("RooHubData", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = pref.getString("teachers_list", null);

        if (json != null) {
            Type type = new TypeToken<ArrayList<TeacherDataStore.Teacher>>() {}.getType();
            ArrayList<TeacherDataStore.Teacher> allTeachers = gson.fromJson(json, type);

            // --- STEP 3: FILTER FOR "Coloring art" CATEGORY ---
            if (allTeachers != null) {
                for (TeacherDataStore.Teacher t : allTeachers) {
                    // Using .equalsIgnoreCase to avoid minor spelling issues
                    if (t.artType != null && t.artType.equalsIgnoreCase("Coloring art")) {
                        colorTeachers.add(t);
                    }
                }
            }
        }

        // --- STEP 4: SETUP RECYCLERVIEW ADAPTER ---
        // Passing 'this' context so the adapter knows which activity to use
        adapter = new TeacherAdapter(colorTeachers, this);
        recyclerView.setAdapter(adapter);
    }
}