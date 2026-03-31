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

public class AssemblageArtActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TeacherAdapter adapter;
    private ArrayList<TeacherDataStore.Teacher> assTeachers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assemblage_art);

        // Hide the top action bar for a better look
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // --- STEP 1: INITIALIZE UI COMPONENTS ---
        recyclerView = findViewById(R.id.rvAssVideos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        assTeachers = new ArrayList<>();

        // --- STEP 2: LOAD FILTERED TEACHER DATA ---
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

            // --- STEP 3: FILTER FOR "Assemblage art" CATEGORY ---
            if (allTeachers != null) {
                for (TeacherDataStore.Teacher t : allTeachers) {
                    // Check if artType matches "Assemblage art" (Case-insensitive check)
                    if (t.artType != null && t.artType.equalsIgnoreCase("Assemblage art")) {
                        assTeachers.add(t);
                    }
                }
            }
        }

        // --- STEP 4: SETUP RECYCLERVIEW ADAPTER ---
        // Pass 'this' as context to the adapter
        adapter = new TeacherAdapter(assTeachers, this);
        recyclerView.setAdapter(adapter);
    }
}