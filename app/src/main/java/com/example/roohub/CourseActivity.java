package com.example.roohub;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CourseActivity extends AppCompatActivity {

    Button colorArt, penArt, pencilArt, animation, animal, natural;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        colorArt = findViewById(R.id.btnColorArt);
        penArt = findViewById(R.id.btnPenArt);
        pencilArt = findViewById(R.id.btnPencilArt);
        animation = findViewById(R.id.btnAnimation);
        animal = findViewById(R.id.btnAnimal);
        natural = findViewById(R.id.btnNatural);

        colorArt.setOnClickListener(v ->
                Toast.makeText(this,"Color Art Course",Toast.LENGTH_SHORT).show());

        penArt.setOnClickListener(v ->
                Toast.makeText(this,"Pen Art Course",Toast.LENGTH_SHORT).show());

        pencilArt.setOnClickListener(v ->
                Toast.makeText(this,"Pencil Art Course",Toast.LENGTH_SHORT).show());

        animation.setOnClickListener(v ->
                Toast.makeText(this,"Animation Course",Toast.LENGTH_SHORT).show());

        animal.setOnClickListener(v ->
                Toast.makeText(this,"Animal Art Course",Toast.LENGTH_SHORT).show());

        natural.setOnClickListener(v ->
                Toast.makeText(this,"Natural Art Course",Toast.LENGTH_SHORT).show());
    }
}