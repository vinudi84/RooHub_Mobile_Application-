package com.example.roohub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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
    private Button btnBack;
    private LinearLayout artListContainer;

    private String loggedEmail, loggedPass;
    private Uri tempUpdateUri = null;
    private ImageView tempUpdateImageView = null;

    // පින්තූරය වෙනස් කිරීමට Launcher එකක්
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

        edtEmail = findViewById(R.id.edtEmail);
        edtPass = findViewById(R.id.edtPass);
        imgShowProfile = findViewById(R.id.imgShowProfile);
        btnBack = findViewById(R.id.btnBack);

        // Art පෙන්වන Container එක සකස් කිරීම
        artListContainer = new LinearLayout(this);
        artListContainer.setOrientation(LinearLayout.VERTICAL);
        ((LinearLayout) btnBack.getParent()).addView(artListContainer);

        loggedEmail = getIntent().getStringExtra("LOGGED_EMAIL");
        loggedPass = getIntent().getStringExtra("LOGGED_PASS");

        loadAndDisplayUserContent();

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadAndDisplayUserContent() {
        artListContainer.removeAllViews();
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String allData = prefs.getString("all_art_data", "");

        if (allData.isEmpty()) return;

        String[] records = allData.split("\\|\\|\\|");
        boolean isFirstMatch = true;

        for (String record : records) {
            String[] p = record.split("###");
            if (p.length >= 6 && p[0].equals(loggedEmail) && p[1].equals(loggedPass)) {

                if (isFirstMatch) {
                    edtEmail.setText(p[0]);
                    edtPass.setText(p[1]);
                    if (!p[5].equals("no_profile")) imgShowProfile.setImageURI(Uri.parse(p[5]));
                    isFirstMatch = false;
                }

                // Art එක Edit කිරීමට අවශ්‍ය UI එක සාදයි
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

        // පින්තූරය පෙන්වන තැන
        ImageView iv = new ImageView(this);
        iv.setLayoutParams(new LinearLayout.LayoutParams(-1, 500));
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setImageURI(Uri.parse(artUri));

        // පින්තූරය මත Click කළ විට එය වෙනස් කිරීමට ඉඩ ලබාදීම
        iv.setOnClickListener(v -> {
            tempUpdateImageView = iv;
            updateArtLauncher.launch(new String[]{"image/*"});
        });

        // නම වෙනස් කිරීමට EditText එකක්
        EditText etName = new EditText(this);
        etName.setText(name);
        etName.setHint("Art Name");

        // විස්තරය වෙනස් කිරීමට EditText එකක්
        EditText etDesc = new EditText(this);
        etDesc.setText(desc);
        etDesc.setHint("Description");

        // Update Button එක
        Button btnUpdateArt = new Button(this);
        btnUpdateArt.setText("Update This Art");
        btnUpdateArt.setBackgroundColor(Color.BLUE);
        btnUpdateArt.setTextColor(Color.WHITE);
        btnUpdateArt.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            String newDesc = etDesc.getText().toString().trim();
            String finalUri = (tempUpdateUri != null && tempUpdateImageView == iv) ? tempUpdateUri.toString() : artUri;

            updateSpecificArt(fullRecord, newName, newDesc, finalUri);
        });

        // Delete Button එක
        Button btnDelete = new Button(this);
        btnDelete.setText("Delete This Art");
        btnDelete.setBackgroundColor(Color.RED);
        btnDelete.setTextColor(Color.WHITE);
        btnDelete.setOnClickListener(v -> deleteRecord(fullRecord));

        itemLayout.addView(new TextView(this){{setText("Tap Image to Change");}});
        itemLayout.addView(iv);
        itemLayout.addView(etName);
        itemLayout.addView(etDesc);
        itemLayout.addView(btnUpdateArt);
        itemLayout.addView(btnDelete);

        artListContainer.addView(itemLayout);
    }

    private void updateSpecificArt(String oldRecord, String newName, String newDesc, String newUri) {
        if (newName.isEmpty() || newDesc.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String allData = prefs.getString("all_art_data", "");

        String[] p = oldRecord.split("###");
        String newRecord = p[0] + "###" + p[1] + "###" + newName + "###" + newDesc + "###" + newUri + "###" + p[5];

        String updatedAllData = allData.replace(oldRecord, newRecord);

        prefs.edit().putString("all_art_data", updatedAllData).apply();
        Toast.makeText(this, "Art Updated Successfully!", Toast.LENGTH_SHORT).show();

        tempUpdateUri = null; // Reset temp URI
        loadAndDisplayUserContent(); // UI  Refresh
    }

    private void deleteRecord(String recordToDelete) {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String allData = prefs.getString("all_art_data", "");
        String newData = allData.replace(recordToDelete, "").replace("||||||", "|||");

        if (newData.startsWith("|||")) newData = newData.substring(3);
        if (newData.endsWith("|||")) newData = newData.substring(0, newData.length() - 3);

        prefs.edit().putString("all_art_data", newData).apply();
        Toast.makeText(this, "Deleted!", Toast.LENGTH_SHORT).show();
        loadAndDisplayUserContent();
    }
}