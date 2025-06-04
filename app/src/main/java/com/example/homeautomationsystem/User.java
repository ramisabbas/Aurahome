package com.example.homeautomationsystem;

public class User {
    private String name;
    private String email;
    private String purpose;
    private String organization;
    private String status;
    private String uid;

    // Default constructor required for Firebase
    public User() {
    }

    public User(String name, String email, String purpose, String organization, String status, String uid) {
        this.name = name;
        this.email = email;
        this.purpose = purpose;
        this.organization = organization;
        this.status = status;
        this.uid = uid;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}