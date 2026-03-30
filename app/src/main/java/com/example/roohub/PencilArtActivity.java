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
    private TeacherAdapter adapter; // Using TeacherAdapter to show teachers
    private ArrayList<TeacherDataStore.Teacher> pencilTeachers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pencil_art);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        recyclerView = findViewById(R.id.rvPencilVideos); // Ensure this ID exists in your XML
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        pencilTeachers = new ArrayList<>();

        loadTeacherList();
    }

    private void loadTeacherList() {
        // 1. Get the full list of teachers from SharedPreferences
        SharedPreferences pref = getSharedPreferences("RooHubData", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = pref.getString("teachers_list", null);

        if (json != null) {
            Type type = new TypeToken<ArrayList<TeacherDataStore.Teacher>>() {}.getType();
            ArrayList<TeacherDataStore.Teacher> allTeachers = gson.fromJson(json, type);

            // 2. Filter teachers who belong to "Pencil art" category
            for (TeacherDataStore.Teacher t : allTeachers) {
                if (t.artType.equals("Pencil art")) {
                    pencilTeachers.add(t);
                }
            }
        }

        // 3. Set the filtered list to the Adapter
        adapter = new TeacherAdapter(pencilTeachers, this);
        recyclerView.setAdapter(adapter);
    }
}