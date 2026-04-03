package com.example.roohub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ManageCoursesActivity extends AppCompatActivity {

    private LinearLayout coursesContainer;
    private String token, userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_courses);

        if (!SessionManager.isLoggedIn(this)) {
            finish();
            return;
        }

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        coursesContainer = findViewById(R.id.coursesContainer);
        token            = SessionManager.getToken(this);
        userId           = SessionManager.getUserId(this);

        loadMyCourses();
    }

    private void loadMyCourses() {
        new Thread(() -> {
            try {
                String coursesUrl = SupabaseClient.SUPABASE_URL
                        + "/rest/v1/course_uploads"
                        + "?user_id=eq." + userId
                        + "&select=id,teacher_name,art_name,description,course_category,video_url"
                        + "&order=created_at.desc";

                URL url = new URL(coursesUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("apikey",        SupabaseClient.SUPABASE_ANON_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Content-Type",  "application/json");

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

                JSONArray array = new JSONArray(response.toString());

                runOnUiThread(() -> {
                    coursesContainer.removeAllViews();

                    if (array.length() == 0) {
                        TextView empty = new TextView(this);
                        empty.setText("You have no course uploads yet.");
                        empty.setPadding(16, 32, 16, 32);
                        coursesContainer.addView(empty);
                        return;
                    }

                    for (int i = 0; i < array.length(); i++) {
                        try {
                            JSONObject course   = array.getJSONObject(i);
                            String courseId     = course.optString("id",               "");
                            String teacherName  = course.optString("teacher_name",     "");
                            String artName      = course.optString("art_name",         "");
                            String desc         = course.optString("description",      "");
                            String category     = course.optString("course_category",  "");
                            String videoUrl     = course.optString("video_url",        "");
                            addCourseEditCard(courseId, teacherName, artName, desc, category, videoUrl);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                android.util.Log.e("MANAGE_COURSES", "Error: " + e.getMessage());
            }
        }).start();
    }

    private void addCourseEditCard(String courseId, String teacherName,
                                   String artName, String desc,
                                   String category, String videoUrl) {
        View card = LayoutInflater.from(this)
                .inflate(R.layout.item_manage_course, coursesContainer, false);

        EditText etTeacherName = card.findViewById(R.id.manageTeacherName);
        EditText etArtName     = card.findViewById(R.id.manageArtName);
        EditText etDesc        = card.findViewById(R.id.manageDesc);
        TextView tvCategory    = card.findViewById(R.id.manageCategory);
        TextView tvVideoUrl    = card.findViewById(R.id.manageVideoUrl);
        Button btnUpdate       = card.findViewById(R.id.btnUpdateCourse);
        Button btnDelete       = card.findViewById(R.id.btnDeleteCourse);

        etTeacherName.setText(teacherName);
        etArtName.setText(artName);
        etDesc.setText(desc);
        tvCategory.setText("Category: " + category);
        tvVideoUrl.setText("Video: " + videoUrl);

        // ── Update course ────────────────────────────────────────────────────
        btnUpdate.setOnClickListener(v -> {
            String newTeacherName = etTeacherName.getText().toString().trim();
            String newArtName     = etArtName.getText().toString().trim();
            String newDesc        = etDesc.getText().toString().trim();
            updateCourse(courseId, newTeacherName, newArtName, newDesc);
        });

        // ── Delete course ────────────────────────────────────────────────────
        btnDelete.setOnClickListener(v ->
                deleteCourse(courseId, card));

        coursesContainer.addView(card);
    }

    private void updateCourse(String courseId, String teacherName,
                              String artName, String desc) {
        new Thread(() -> {
            try {
                String updateUrl = SupabaseClient.SUPABASE_URL
                        + "/rest/v1/course_uploads"
                        + "?id=eq." + courseId;

                JSONObject body = new JSONObject();
                body.put("teacher_name", teacherName);
                body.put("art_name",     artName);
                body.put("description",  desc);

                makePatchRequest(updateUrl, body);

                runOnUiThread(() ->
                        Toast.makeText(this, "Course updated!", Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void deleteCourse(String courseId, View card) {
        new Thread(() -> {
            try {
                String deleteUrl = SupabaseClient.SUPABASE_URL
                        + "/rest/v1/course_uploads"
                        + "?id=eq." + courseId;

                URL url = new URL(deleteUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setRequestProperty("apikey",        SupabaseClient.SUPABASE_ANON_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Prefer",        "return=minimal");

                int responseCode = conn.getResponseCode();
                conn.disconnect();

                if (responseCode >= 200 && responseCode < 300) {
                    runOnUiThread(() -> {
                        coursesContainer.removeView(card);
                        Toast.makeText(this, "Course deleted!", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    throw new Exception("Delete failed: " + responseCode);
                }

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void makePatchRequest(String urlString, JSONObject body) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PATCH");
        conn.setRequestProperty("Content-Type",  "application/json");
        conn.setRequestProperty("apikey",        SupabaseClient.SUPABASE_ANON_KEY);
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Prefer",        "return=minimal");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream()));
            StringBuilder resp = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) resp.append(line);
            conn.disconnect();
            throw new Exception("Patch failed (" + responseCode + "): " + resp);
        }
        conn.disconnect();
    }
}