package com.example.roohub;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private TextView tvLoginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        tvLoginLink = findViewById(R.id.tvLoginLink);

        // Optional: Make "Login" text underlined like a hyperlink
        tvLoginLink.setPaintFlags(tvLoginLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvLoginLink.setTextColor(Color.parseColor("#6200EE")); // Ensure color matches XML

        // Click listener to open LoginActivity
        tvLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // optional: prevent back to register
            }
        });
    }
}