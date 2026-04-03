package com.example.roohub;

import android.content.Intent;
import android.graphics.Paint;
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

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private Button btnRegister;
    private TextView tvLoginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        etName      = findViewById(R.id.etName);
        etEmail     = findViewById(R.id.etEmail);
        etPassword  = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        tvLoginLink.setPaintFlags(tvLoginLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        btnRegister.setOnClickListener(v -> {
            String name     = etName.getText().toString().trim();
            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                handleRegistration(name, email, password);
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });

        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void handleRegistration(String name, String email, String password) {
        new Thread(() -> {
            try {
                // ── Step 1: Sign up via Supabase Auth ───────────────────────────
                String signUpUrl = SupabaseClient.SUPABASE_URL + "/auth/v1/signup";

                JSONObject signUpBody = new JSONObject();
                signUpBody.put("email",    email);
                signUpBody.put("password", password);

                JSONObject signUpResponse = makePostRequest(signUpUrl, signUpBody, null);

                // ── Step 2: Get access token and user ID ─────────────────────────
                String accessToken;
                String userId;

                if (signUpResponse.has("access_token") && !signUpResponse.isNull("access_token")) {
                    // Email confirmation OFF — token available immediately
                    accessToken = signUpResponse.getString("access_token");
                    userId      = signUpResponse.getJSONObject("user").getString("id");

                } else {
                    // Email confirmation ON — sign in to get token
                    String signInUrl = SupabaseClient.SUPABASE_URL + "/auth/v1/token?grant_type=password";

                    JSONObject signInBody = new JSONObject();
                    signInBody.put("email",    email);
                    signInBody.put("password", password);

                    JSONObject signInResponse = makePostRequest(signInUrl, signInBody, null);
                    accessToken = signInResponse.getString("access_token");
                    userId      = signInResponse.getJSONObject("user").getString("id");
                }

                // ── Step 3: Insert into profiles table ───────────────────────────
                String profilesUrl = SupabaseClient.SUPABASE_URL + "/rest/v1/profiles";

                JSONObject profileBody = new JSONObject();
                profileBody.put("id",        userId);
                profileBody.put("full_name", name);
                profileBody.put("email",     email);

                makePostRequest(profilesUrl, profileBody, accessToken);

                // ── Step 4: Go to HomeActivity ───────────────────────────────────
                runOnUiThread(() -> {
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                    finish();
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this,
                                "Error: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"),
                                Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    private JSONObject makePostRequest(String urlString, JSONObject body, String accessToken) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type",  "application/json");
        conn.setRequestProperty("apikey",        SupabaseClient.SUPABASE_ANON_KEY);
        conn.setRequestProperty("Authorization", "Bearer " +
                (accessToken != null ? accessToken : SupabaseClient.SUPABASE_ANON_KEY));
        conn.setRequestProperty("Prefer",        "return=minimal");
        conn.setDoOutput(true);

        // Write request body
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        // Read response
        int responseCode = conn.getResponseCode();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                responseCode >= 200 && responseCode < 300
                        ? conn.getInputStream()
                        : conn.getErrorStream()
        ));

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        conn.disconnect();

        // Handle errors
        if (responseCode < 200 || responseCode >= 300) {
            String rawResponse = response.toString();
            try {
                JSONObject errorJson = new JSONObject(rawResponse);
                String errorMsg = errorJson.optString("message",
                        errorJson.optString("error_description",
                                errorJson.optString("error", "Request failed: " + responseCode)));
                throw new Exception(errorMsg);
            } catch (Exception parseEx) {
                throw new Exception("Request failed (" + responseCode + "): " + rawResponse);
            }
        }

        // Return empty object if response body is empty (e.g. profiles insert with return=minimal)
        String rawResponse = response.toString().trim();
        return rawResponse.isEmpty() ? new JSONObject() : new JSONObject(rawResponse);
    }
}