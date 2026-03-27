package com.example.roohub;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class UploadActivity extends AppCompatActivity {

    private ImageView profilePreview, artImagePreview;
    private EditText etUserEmail, etUserPassword, etArtName, etDescription;
    private Button btnSave, btnGoToProfile;

    private Uri profileUri = null;
    private Uri artUri = null;

    // Profile Image Launcher - මෙහිදී OpenDocument පාවිච්චි කර ඇත (Permission ස්ථිර කිරීමට)
    private final ActivityResultLauncher<String[]> profileLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    profileUri = uri;
                    profilePreview.setImageURI(uri);

                    // වැදගත්: Permission ස්ථිර කරගැනීම (Crash එක වැළැක්වීමට)
                    getContentResolver().takePersistableUriPermission(uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            });

    // Art Image Launcher
    private final ActivityResultLauncher<String[]> artLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    artUri = uri;
                    artImagePreview.setImageURI(uri);

                    getContentResolver().takePersistableUriPermission(uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Initialize Views
        profilePreview = findViewById(R.id.profilePreview);
        artImagePreview = findViewById(R.id.artImagePreview);
        etUserEmail = findViewById(R.id.etUserEmail);
        etUserPassword = findViewById(R.id.etUserPassword);
        etArtName = findViewById(R.id.etArtName);
        etDescription = findViewById(R.id.etDescription);
        btnSave = findViewById(R.id.btnSave);
        btnGoToProfile = findViewById(R.id.btnGoToProfile);

        // Selection listeners - මෙහිදී String array එකක් යැවිය යුතුය
        profilePreview.setOnClickListener(v -> profileLauncher.launch(new String[]{"image/*"}));
        artImagePreview.setOnClickListener(v -> artLauncher.launch(new String[]{"image/*"}));

        // Save Logic (ඔයාගේ කලින් තිබුණු logic එකමයි)
        btnSave.setOnClickListener(v -> {
            String email = etUserEmail.getText().toString().trim();
            String password = etUserPassword.getText().toString().trim();
            String artName = etArtName.getText().toString().trim();
            String desc = etDescription.getText().toString().trim();

            if (artUri == null || artName.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill required details!", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putString("user_email", email);
            editor.putString("user_password", password);
            if (profileUri != null) editor.putString("profile_pic", profileUri.toString());

            // කලින් තිබූ format එක (### සහ |||)
            String newData = artName + "###Art###" + desc + "###" + artUri.toString();
            String existingArt = prefs.getString("all_art_data", "");
            editor.putString("all_art_data", existingArt.isEmpty() ? newData : newData + "|||" + existingArt);

            editor.apply();

            Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        // Popup Verification and Navigation
        btnGoToProfile.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            String savedEmail = prefs.getString("user_email", "");
            String savedPass = prefs.getString("user_password", "");

            if (savedEmail.isEmpty()) {
                Toast.makeText(this, "No profile found!", Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Verify to Edit Profile");

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 20, 50, 10);

            final EditText inputEmail = new EditText(this);
            inputEmail.setHint("Email");
            layout.addView(inputEmail);

            final EditText inputPass = new EditText(this);
            inputPass.setHint("Password");
            inputPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            layout.addView(inputPass);

            builder.setView(layout);

            builder.setPositiveButton("Next", (dialog, which) -> {
                if (inputEmail.getText().toString().equals(savedEmail) &&
                        inputPass.getText().toString().equals(savedPass)) {
                    startActivity(new Intent(UploadActivity.this, UserProfileActivity.class));
                } else {
                    Toast.makeText(this, "Wrong Credentials!", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        });
    }
}