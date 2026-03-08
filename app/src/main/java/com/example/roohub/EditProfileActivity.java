package com.example.roohub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView editImagePreview;
    private EditText inputName, inputBio;
    private Button btnPickImage, btnSave;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    Glide.with(this).load(selectedImageUri).into(editImagePreview);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editImagePreview = findViewById(R.id.editImagePreview);
        inputName = findViewById(R.id.inputName);
        inputBio = findViewById(R.id.inputBio);
        btnPickImage = findViewById(R.id.btnPickImage);
        btnSave = findViewById(R.id.btnSave);

        btnPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> {
            String name = inputName.getText().toString();
            String bio = inputBio.getText().toString();

            // Save data to SharedPreferences
            SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
            editor.putString("NAME", name);
            editor.putString("BIO", bio);
            if (selectedImageUri != null) {
                editor.putString("IMAGE_URI", selectedImageUri.toString());
            }
            editor.apply();

            // ProfileActivity එකට යාම
            finish(); // Edit එක වැසීම පමණක් සෑහේ, Profile එක දැනටමත් stack එකේ තියෙන්න ඕනේ
        });
    }
}


