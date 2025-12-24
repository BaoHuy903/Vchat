package com.endterm.vchat;

public class User {
    private String id;
    private String username;
    private String imageurl;
    private String country;
    private String genre;
    private String workPosition;
    private String phone;

    // Required empty public constructor for Firestore
    public User() {
    }

    public User(String id, String username, String imageurl, String country, String genre, String workPosition, String phone) {
        this.id = id;
        this.username = username;
        this.imageurl = imageurl;
        this.country = country;
        this.genre = genre;
        this.workPosition = workPosition;
        this.phone = phone;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getWorkPosition() {
        return workPosition;
    }

    public void setWorkPosition(String workPosition) {
        this.workPosition = workPosition;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
