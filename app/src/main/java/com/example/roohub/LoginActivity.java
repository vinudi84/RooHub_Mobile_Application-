package com.example.roohub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private Button btnLogin;
    private TextView txtRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // ── If already logged in skip login screen ───────────────────────────
        if (SessionManager.isLoggedIn(this)) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        editEmail    = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin     = findViewById(R.id.btnLogin);
        txtRegister  = findViewById(R.id.txtRegister);

        btnLogin.setOnClickListener(v -> {
            String email    = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                btnLogin.setEnabled(false);
                btnLogin.setText("Logging in...");
                handleLogin(email, password);
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });

        txtRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void handleLogin(String email, String password) {
        new Thread(() -> {
            try {
                // ── Step 1: Clear any existing session first ─────────────────
                SessionManager.clearSession(this);
                android.util.Log.d("LOGIN", "Old session cleared");

                // ── Step 2: Sign in via Supabase Auth REST API ───────────────
                String signInUrl = SupabaseClient.SUPABASE_URL
                        + "/auth/v1/token?grant_type=password";

                JSONObject signInBody = new JSONObject();
                signInBody.put("email",    email);
                signInBody.put("password", password);

                JSONObject signInResponse = makePostRequest(signInUrl, signInBody);

                // ── Step 3: Get access token and user ID ─────────────────────
                String accessToken = signInResponse.getString("access_token");
                String userId      = signInResponse.getJSONObject("user").getString("id");

                android.util.Log.d("LOGIN", "Logged in userId: " + userId);
                android.util.Log.d("LOGIN", "Email: " + email);

                // ── Step 4: Save new session ─────────────────────────────────
                SessionManager.saveSession(this, accessToken, userId, email);

                // ── Step 5: Go to HomeActivity ───────────────────────────────
                runOnUiThread(() -> {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });

            } catch (Exception e) {
                android.util.Log.e("LOGIN", "Login error: " + e.getMessage());
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");
                    Toast.makeText(this,
                            "Error: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private JSONObject makePostRequest(String urlString, JSONObject body) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type",  "application/json");
        conn.setRequestProperty("apikey",        SupabaseClient.SUPABASE_ANON_KEY);
        conn.setRequestProperty("Authorization", "Bearer " + SupabaseClient.SUPABASE_ANON_KEY);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                responseCode >= 200 && responseCode < 300
                        ? conn.getInputStream()
                        : conn.getErrorStream()
        ));

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) response.append(line);
        reader.close();
        conn.disconnect();

        if (responseCode < 200 || responseCode >= 300) {
            String rawResponse = response.toString();
            try {
                JSONObject errorJson  = new JSONObject(rawResponse);
                String errorMsg = errorJson.optString("message",
                        errorJson.optString("error_description",
                                errorJson.optString("error", "Login failed: " + responseCode)));
                throw new Exception(errorMsg);
            } catch (Exception parseEx) {
                throw new Exception("Login failed (" + responseCode + "): " + rawResponse);
            }
        }

        return new JSONObject(response.toString());
    }
}