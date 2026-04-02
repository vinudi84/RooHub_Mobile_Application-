package com.example.roohub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class teacherSignUp extends AppCompatActivity {

    CircleImageView profileImage;
    ActivityResultLauncher<Intent> galleryLauncher;
    Button registerBtn;
    TextView LoginLink;
    EditText etName, etEmail, etQualifications, etPassword;
    Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teacher_sign_up);

        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
            getSupportActionBar().setTitle("RooHub Teacher");
        }

        profileImage = findViewById(R.id.profileImage);
        registerBtn = findViewById(R.id.registerBtn);
        LoginLink = findViewById(R.id.LoginLink);
        etName = findViewById(R.id.name);
        etEmail = findViewById(R.id.email);
        etQualifications = findViewById(R.id.qualification);
        etPassword = findViewById(R.id.password);

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            profileImage.setImageURI(uri);
                            selectedImageUri = uri;
                        }
                    }
                }
        );

        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        });

        registerBtn.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String qualify = etQualifications.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            String img = (selectedImageUri != null) ? selectedImageUri.toString() : "";

            SharedPreferences pref = getSharedPreferences("RooHubData", MODE_PRIVATE);
            Gson gson = new Gson();
            String json = pref.getString("teachers_list", null);
            ArrayList<TeacherDataStore.Teacher> list = (json == null) ? new ArrayList<>() : gson.fromJson(json, new TypeToken<ArrayList<TeacherDataStore.Teacher>>(){}.getType());

            // Added without artType
            list.add(new TeacherDataStore.Teacher(name, email, qualify, password, img));
            pref.edit().putString("teachers_list", gson.toJson(list)).apply();

            getSharedPreferences("TeacherProfile", MODE_PRIVATE).edit()
                    .putString("t_name", name).putString("t_email", email)
                    .putString("t_qualify", qualify).putString("t_image", img).apply();

            startActivity(new Intent(this, ViewUploadActivity.class));
            finish();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Upload New Video").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) { showVerificationPopup(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void showVerificationPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verify Teacher");
        LinearLayout lay = new LinearLayout(this); lay.setOrientation(LinearLayout.VERTICAL); lay.setPadding(50, 20, 50, 20);
        final EditText em = new EditText(this); em.setHint("Email"); lay.addView(em);
        final EditText pw = new EditText(this); pw.setHint("Password"); pw.setInputType(129); lay.addView(pw);
        builder.setView(lay);

        builder.setPositiveButton("Verify", (d, w) -> {
            String uEm = em.getText().toString().trim();
            String uPw = pw.getText().toString().trim();

            SharedPreferences pref = getSharedPreferences("RooHubData", MODE_PRIVATE);
            String json = pref.getString("teachers_list", null);
            if (json != null) {
                ArrayList<TeacherDataStore.Teacher> list = new Gson().fromJson(json, new TypeToken<ArrayList<TeacherDataStore.Teacher>>(){}.getType());
                boolean found = false;
                for (TeacherDataStore.Teacher t : list) {
                    if (t.getEmail().equalsIgnoreCase(uEm) && t.getPassword().equals(uPw)) {
                        found = true;
                        getSharedPreferences("TeacherProfile", MODE_PRIVATE).edit()
                                .putString("t_name", t.getName()).putString("t_email", t.getEmail())
                                .putString("t_qualify", t.getQualify()).putString("t_image", t.getImageUri()).apply();
                        break;
                    }
                }
                if (found) startActivity(new Intent(this, ViewUploadActivity.class));
                else Toast.makeText(this, "Invalid credentials!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }
}