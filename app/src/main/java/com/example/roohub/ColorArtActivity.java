package com.example.roohub;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ColorArtActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TeacherAdapter adapter;
    List<TeacherDataStore.Teacher> filteredList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_art);

        recyclerView = findViewById(R.id.recyclerViewColor);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        for (TeacherDataStore.Teacher teacher : TeacherDataStore.allTeachers) {
            if (teacher.artType.equals("Coloring art")) {
                filteredList.add(teacher);
            }
        }


        adapter = new TeacherAdapter(filteredList);
        recyclerView.setAdapter(adapter);
    }
}