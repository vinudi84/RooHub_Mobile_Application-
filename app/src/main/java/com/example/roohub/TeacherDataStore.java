package com.example.roohub;

import java.util.ArrayList;
import java.util.List;

public class TeacherDataStore {
    public static class Teacher {
        public String name;
        public String artType;
        public String description;
        public String imageUri; // Aluthin ekathu kala

        // Constructor eka dan arguments 4k gannawa
        public Teacher(String name, String artType, String description, String imageUri) {
            this.name = name;
            this.artType = artType;
            this.description = description;
            this.imageUri = imageUri;
        }
    }
    public static List<Teacher> allTeachers = new ArrayList<>();
}