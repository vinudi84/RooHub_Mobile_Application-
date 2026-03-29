package com.example.roohub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class UserProfileActivity extends AppCompatActivity {

    private EditText edtEmail, edtPass;
    private ImageView imgShowProfile;
    private Button btnUpdate, btnBack;
    private TextView txtTitle;
    private LinearLayout artListLayout;

    private String loggedEmail, mode;
    private Uri tempUpdateUri = null;
    private ImageView tempUpdateImageView = null;

    // Image picker for updating artwork photos
    private final ActivityResultLauncher<String[]> updateArtLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null && tempUpdateImageView != null) {
                    tempUpdateUri = uri;
                    tempUpdateImageView.setImageURI(uri);
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Link UI elements with XML IDs
        edtEmail = findViewById(R.id.edtEmail);
        edtPass = findViewById(R.id.edtPass);
        imgShowProfile = findViewById(R.id.imgShowProfile);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnBack = findViewById(R.id.btnBack);
        txtTitle = findViewById(R.id.txtTitle);
        artListLayout = findViewById(R.id.artListLayout);

        // Get info passed from HomeActivity
        loggedEmail = getIntent().getStringExtra("LOGGED_EMAIL");
        mode = getIntent().getStringExtra("MODE");

        // Check if the user is just a visitor (VIEW_ONLY)
        if ("VIEW_ONLY".equals(mode)) {
            txtTitle.setText("Artist Profile"); // Change title
            edtPass.setVisibility(View.GONE);    // Hide password

            // HIDE THE BUTTONS YOU MENTIONED
            btnUpdate.setVisibility(View.GONE);
            btnBack.setVisibility(View.GONE);

            edtEmail.setEnabled(false);         // Disable editing
            edtEmail.setTextColor(Color.BLACK);
        }

        // Load content from storage
        loadAndDisplayUserContent();

        // If not hidden, back button just closes the screen
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadAndDisplayUserContent() {
        artListLayout.removeAllViews();
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String allData = prefs.getString("all_art_data", "");

        if (allData.isEmpty()) return;

        String[] records = allData.split("\\|\\|\\|");
        boolean isFirstMatch = true;

        for (String record : records) {
            String[] p = record.split("###");

            // Match artworks belonging to the artist's email
            if (p.length >= 6 && p[0].equals(loggedEmail)) {
                if (isFirstMatch) {
                    edtEmail.setText(p[0]);
                    if (!p[5].equals("no_profile")) imgShowProfile.setImageURI(Uri.parse(p[5]));
                    isFirstMatch = false;
                }
                // Generate UI for each artwork
                createArtItemUI(p[2], p[3], p[4], record);
            }
        }
    }

    private void createArtItemUI(String name, String desc, String artUri, String fullRecord) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.VERTICAL);
        itemLayout.setPadding(30, 30, 30, 30);
        itemLayout.setBackgroundColor(Color.parseColor("#FAD1E0"));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 20, 0, 20);
        itemLayout.setLayoutParams(params);

        ImageView iv = new ImageView(this);
        iv.setLayoutParams(new LinearLayout.LayoutParams(-1, 500));
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setImageURI(Uri.parse(artUri));

        EditText etName = new EditText(this);
        etName.setText(name);
        EditText etDesc = new EditText(this);
        etDesc.setText(desc);

        if ("VIEW_ONLY".equals(mode)) {
            // Visitor mode: No editing, no action buttons
            etName.setEnabled(false);
            etDesc.setEnabled(false);
            etName.setTextColor(Color.BLACK);
            etDesc.setTextColor(Color.DKGRAY);

            itemLayout.addView(iv);
            itemLayout.addView(etName);
            itemLayout.addView(etDesc);
        } else {
            // Owner mode: Show Update/Delete buttons for each art
            iv.setOnClickListener(v -> {
                tempUpdateImageView = iv;
                updateArtLauncher.launch(new String[]{"image/*"});
            });

            Button bUpdate = new Button(this);
            bUpdate.setText("Update Art");
            bUpdate.setBackgroundColor(Color.BLUE);
            bUpdate.setOnClickListener(v -> {
                String finalUri = (tempUpdateUri != null && tempUpdateImageView == iv) ? tempUpdateUri.toString() : artUri;
                updateSpecificArt(fullRecord, etName.getText().toString(), etDesc.getText().toString(), finalUri);
            });

            Button bDelete = new Button(this);
            bDelete.setText("Delete Art");
            bDelete.setBackgroundColor(Color.RED);
            bDelete.setOnClickListener(v -> deleteRecord(fullRecord));

            itemLayout.addView(iv);
            itemLayout.addView(etName);
            itemLayout.addView(etDesc);
            itemLayout.addView(bUpdate);
            itemLayout.addView(bDelete);
        }

        artListLayout.addView(itemLayout);
    }

    private void updateSpecificArt(String oldRecord, String newName, String newDesc, String newUri) {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String allData = prefs.getString("all_art_data", "");
        String[] p = oldRecord.split("###");

        String newRecord = p[0] + "###" + p[1] + "###" + newName + "###" + newDesc + "###" + newUri + "###" + p[5];
        String updatedAllData = allData.replace(oldRecord, newRecord);

        prefs.edit().putString("all_art_data", updatedAllData).apply();
        Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show();
        loadAndDisplayUserContent();
    }

    private void deleteRecord(String recordToDelete) {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String allData = prefs.getString("all_art_data", "");
        String newData = allData.replace(recordToDelete, "").replace("||||||", "|||");
        prefs.edit().putString("all_art_data", newData).apply();
        loadAndDisplayUserContent();
    }
}