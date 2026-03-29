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

    private final ActivityResultLauncher<String[]> profileLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    profileUri = uri;
                    profilePreview.setImageURI(uri);
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            });

    private final ActivityResultLauncher<String[]> artLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    artUri = uri;
                    artImagePreview.setImageURI(uri);
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        profilePreview = findViewById(R.id.profilePreview);
        artImagePreview = findViewById(R.id.artImagePreview);
        etUserEmail = findViewById(R.id.etUserEmail);
        etUserPassword = findViewById(R.id.etUserPassword);
        etArtName = findViewById(R.id.etArtName);
        etDescription = findViewById(R.id.etDescription);
        btnSave = findViewById(R.id.btnSave);
        btnGoToProfile = findViewById(R.id.btnGoToProfile);

        profilePreview.setOnClickListener(v -> profileLauncher.launch(new String[]{"image/*"}));
        artImagePreview.setOnClickListener(v -> artLauncher.launch(new String[]{"image/*"}));

        btnSave.setOnClickListener(v -> {
            String email = etUserEmail.getText().toString().trim();
            String pass = etUserPassword.getText().toString().trim();
            String artName = etArtName.getText().toString().trim();
            String desc = etDescription.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty() || artName.isEmpty() || artUri == null) {
                Toast.makeText(this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            String pUri = (profileUri != null) ? profileUri.toString() : "no_profile";

            // Format: Email###Password###ArtName###Description###ArtUri###ProfileUri
            String newData = email + "###" + pass + "###" + artName + "###" + desc + "###" + artUri.toString() + "###" + pUri;

            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            String existingData = prefs.getString("all_art_data", "");

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("all_art_data", existingData.isEmpty() ? newData : existingData + "|||" + newData);
            editor.apply();

            Toast.makeText(this, "Saved Successfully!", Toast.LENGTH_SHORT).show();
        });

        btnGoToProfile.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Verification");
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 20, 50, 10);

            final EditText inEmail = new EditText(this); inEmail.setHint("Email");
            layout.addView(inEmail);
            final EditText inPass = new EditText(this); inPass.setHint("Password");
            inPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            layout.addView(inPass);

            builder.setView(layout);
            builder.setPositiveButton("Verify", (dialog, which) -> {
                Intent intent = new Intent(this, UserProfileActivity.class);
                intent.putExtra("LOGGED_EMAIL", inEmail.getText().toString());
                intent.putExtra("LOGGED_PASS", inPass.getText().toString());
                startActivity(intent);
            });
            builder.show();
        });
    }
}