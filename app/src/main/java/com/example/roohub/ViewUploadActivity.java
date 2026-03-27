package com.example.roohub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ViewUploadActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvArtType, tvQualifications;
    private EditText etArtName, etVideoDetails;
    private ImageView ivTeacherProfile;
    private String teacherArtCategory = "";
    private static final int VIDEO_PICK_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_upload);

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvArtType = findViewById(R.id.tvArtType);
        tvQualifications = findViewById(R.id.tvQualifications);
        ivTeacherProfile = findViewById(R.id.ivTeacherProfile);
        etArtName = findViewById(R.id.etArtName);
        etVideoDetails = findViewById(R.id.etVideoDetails);
        Button btnSelectVideo = findViewById(R.id.btnSelectVideo);
        Button btnUpload = findViewById(R.id.btnUpload);

        Intent intent = getIntent();
        if (intent != null) {
            String name = intent.getStringExtra("teacher_name");
            String email = intent.getStringExtra("teacher_email");
            teacherArtCategory = intent.getStringExtra("art_type");
            String qualifications = intent.getStringExtra("qualifications");
            String imageUriString = intent.getStringExtra("teacher_image");

            if (name != null) tvName.setText("Name: " + name);
            if (email != null) tvEmail.setText("Email: " + email);
            if (teacherArtCategory != null) tvArtType.setText("Art Type: " + teacherArtCategory);
            if (qualifications != null) tvQualifications.setText("Qualifications: " + qualifications);



// show Profile Image

            if (imageUriString != null) {

                ivTeacherProfile.setImageURI(Uri.parse(imageUriString));

            }

        }



// select a video using gallery

        btnSelectVideo.setOnClickListener(v -> {

            Intent videoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);

            startActivityForResult(videoIntent, VIDEO_PICK_REQUEST);

        });



// Logic of Upload Button

        btnUpload.setOnClickListener(v -> {
            String artName = etArtName.getText().toString().trim();
            String artDesc = etVideoDetails.getText().toString().trim();

            if (artName.isEmpty() || artDesc.isEmpty()) {
                Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            } else {

// take decision to select Category

                String folder = (teacherArtCategory != null) ? teacherArtCategory : "General Art";
                String msg = "Art '" + artName + "' is uploading to " + folder + " category.";
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIDEO_PICK_REQUEST && resultCode == RESULT_OK && data != null) {
            Toast.makeText(this, "Video Selection Successful!", Toast.LENGTH_SHORT).show();
        }
    }
}