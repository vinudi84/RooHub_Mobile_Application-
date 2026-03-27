package com.example.roohub;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class UserProfileActivity extends AppCompatActivity {

    private EditText edtEmail, edtPass;
    private ImageView imgShowProfile;
    private Button btnUpdate, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        edtEmail = findViewById(R.id.edtEmail);
        edtPass = findViewById(R.id.edtPass);
        imgShowProfile = findViewById(R.id.imgShowProfile);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnBack = findViewById(R.id.btnBack);

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

        // Load existing data
        edtEmail.setText(prefs.getString("user_email", ""));
        edtPass.setText(prefs.getString("user_password", ""));
        String uriStr = prefs.getString("profile_pic", "");
        if (!uriStr.isEmpty()) {
            imgShowProfile.setImageURI(Uri.parse(uriStr));
        }

        // Update Button
        btnUpdate.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("user_email", edtEmail.getText().toString());
            editor.putString("user_password", edtPass.getText().toString());
            editor.apply();

            Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnBack.setOnClickListener(v -> finish());
    }
}