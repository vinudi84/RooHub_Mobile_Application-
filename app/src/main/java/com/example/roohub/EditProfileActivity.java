package com.example.roohub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView editImagePreview;
    private EditText inputName, inputBio;
    private Button btnPickImage, btnSave;
    private Uri selectedImageUri;

    // Gallery එකෙන් පින්තූරයක් තෝරා ගැනීම
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    // පින්තූරය පෙන්වීමට Glide පාවිච්චි කරන්න
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

        // කලින් තිබුණු දත්ත තියෙනවා නම් ඒවා පෙන්වන්න (Optional)
        loadCurrentData();

        btnPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> {
            saveProfileData();
        });
    }

    private void saveProfileData() {
        String name = inputName.getText().toString().trim();
        String bio = inputBio.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        // SharedPreferences වල දත්ත ස්ථිරවම සේව් කිරීම
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("NAME", name);
        editor.putString("BIO", bio);
        if (selectedImageUri != null) {
            editor.putString("IMAGE_URI", selectedImageUri.toString());
        }
        editor.apply(); // දත්ත සේව් වුණා

        Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();

        // කෙලින්ම ProfileActivity එකට යනවා
        startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class));
        finish();
    }

    private void loadCurrentData() {
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        inputName.setText(prefs.getString("NAME", ""));
        inputBio.setText(prefs.getString("BIO", ""));
        String img = prefs.getString("IMAGE_URI", null);
        if (img != null) {
            Glide.with(this).load(Uri.parse(img)).into(editImagePreview);
        }
    }
}