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

public class PencilArtActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TeacherAdapter adapter;
    private ArrayList<TeacherDataStore.Teacher> pencilTeachers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pencil_art);

        // Hide the top ActionBar for a cleaner look
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // --- STEP 1: INITIALIZE RECYCLERVIEW ---
        // Ensure this ID matches the one in your activity_pencil_art.xml
        recyclerView = findViewById(R.id.rvPencilVideos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        pencilTeachers = new ArrayList<>();

        // --- STEP 2: LOAD FILTERED TEACHER LIST ---
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

            // --- STEP 3: FILTER FOR "Pencil art" CATEGORY ---
            if (allTeachers != null) {
                for (TeacherDataStore.Teacher t : allTeachers) {
                    // Check if artType matches "Pencil art" (Case-insensitive check)
                    if (t.artType != null && t.artType.equalsIgnoreCase("Pencil art")) {
                        pencilTeachers.add(t);
                    }
                }
            }
        }

        // --- STEP 4: SET THE ADAPTER TO SHOW THE LIST ---
        // Passing 'this' as the activity context
        adapter = new TeacherAdapter(pencilTeachers, this);
        recyclerView.setAdapter(adapter);
    }
}