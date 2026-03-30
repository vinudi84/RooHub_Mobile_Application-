package com.example.roohub;

import java.util.ArrayList;
import java.util.List;

public class TeacherDataStore {
    public static class Teacher {
        public String name;
        public String email;        // Added to identify unique teachers
        public String qualify;      // Added to show teacher's qualifications
        public String artType;
        public String description;
        public String imageUri;

        // Updated constructor to accept all 6 parameters
        public Teacher(String name, String email, String qualify, String artType, String description, String imageUri) {
            this.name = name;
            this.email = email;
            this.qualify = qualify;
            this.artType = artType;
            this.description = description;
            this.imageUri = imageUri;
        }
    }

    // This static list will store all teachers globally across the app
    public static List<Teacher> allTeachers = new ArrayList<>();
}