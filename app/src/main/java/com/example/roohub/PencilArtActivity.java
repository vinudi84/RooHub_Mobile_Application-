package com.example.roohub;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class PencilArtActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TeacherAdapter adapter;
    List<TeacherDataStore.Teacher> filteredList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pencil_art);

        recyclerView = findViewById(R.id.recyclerViewPencil);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Filter Logic: "Pencil art" අයට අදාළ data විතරක් වෙන් කරගන්නවා
        for (TeacherDataStore.Teacher teacher : TeacherDataStore.allTeachers) {
            if (teacher.artType.equals("Pencil art")) {
                filteredList.add(teacher);
            }
        }

        // Adapter එකට filter කරපු list එක දෙනවා
        adapter = new TeacherAdapter(filteredList);
        recyclerView.setAdapter(adapter);
    }
}