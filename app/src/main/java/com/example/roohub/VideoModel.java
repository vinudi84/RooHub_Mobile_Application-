package com.example.roohub;

// This class acts as a container for our video data
public class VideoModel {
    private String title;
    private String description;
    private String videoUri;

    // Constructor to initialize the data
    public VideoModel(String title, String description, String videoUri) {
        this.title = title;
        this.description = description;
        this.videoUri = videoUri;
    }

    // Getters to retrieve the data
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getVideoUri() { return videoUri; }
}