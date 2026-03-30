package com.example.roohub;

// This class acts as a container for our video data
public class VideoModel {
    private String title;
    private String videoUri;   // Swapped order to match TeacherDetailActivity logic
    private String teacherEmail;

    // Constructor to initialize the data
    public VideoModel(String title, String videoUri, String teacherEmail) {
        this.title = title;
        this.videoUri = videoUri;
        this.teacherEmail = teacherEmail;
    }

    // Getters to retrieve the data
    public String getTitle() { return title; }
    public String getVideoUri() { return videoUri; }
    public String getTeacherEmail() { return teacherEmail; }
}