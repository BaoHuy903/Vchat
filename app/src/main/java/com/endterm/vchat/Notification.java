package com.endterm.vchat;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Notification {
    private String userid;
    private String text;
    private String postid;
    private boolean ispost;
    
    @ServerTimestamp
    private Date timestamp;

    public Notification(String userid, String text, String postid, boolean ispost) {
        this.userid = userid;
        this.text = text;
        this.postid = postid;
        this.ispost = ispost;
    }

    public Notification() {
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public boolean isIspost() {
        return ispost;
    }

    public void setIspost(boolean ispost) {
        this.ispost = ispost;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
