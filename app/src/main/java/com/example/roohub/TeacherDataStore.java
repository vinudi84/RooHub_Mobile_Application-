package com.example.roohub;

import java.util.ArrayList;
import java.util.List;

public class TeacherDataStore {
    public static class Teacher {
        public String name;
        public String email;
        public String qualify;
        public String description; // This field stores the Password
        public String imageUri;

        // Constructor: Removed artType to allow teacher to teach multiple subjects
        public Teacher(String name, String email, String qualify, String description, String imageUri) {
            this.name = name;
            this.email = email;
            this.qualify = qualify;
            this.description = description; // Password stored here
            this.imageUri = imageUri;
        }

        // --- GETTER METHODS ---
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getQualify() { return qualify; }
        public String getImageUri() { return imageUri; }
        public String getPassword() { return description; }
    }

    public static List<Teacher> allTeachers = new ArrayList<>();
}