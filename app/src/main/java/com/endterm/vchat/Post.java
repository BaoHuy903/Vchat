package com.endterm.vchat;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Post {
    private String postId;
    private String userId;
    private String description;
    private String imageUrl;

    @ServerTimestamp
    private Date timestamp;

    // Required empty public constructor for Firestore
    public Post() {}

    public Post(String postId, String userId, String description, String imageUrl) {
        this.postId = postId;
        this.userId = userId;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
