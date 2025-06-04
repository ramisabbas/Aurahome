package com.example.homeautomationsystem;

public class Notification {
    private String title;
    private String message;
    private Long timestamp;

    // Required empty constructor for Firebase
    public Notification() {
    }

    public Notification(String title, String message, Long timestamp) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}