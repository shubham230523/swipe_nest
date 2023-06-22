package com.shubham.swipenest.model;

public class Viewers {
    public int profileImg;
    public String userName;

    public int getProfileImg() {
        return profileImg;
    }

    public Viewers(int profileImg, String userName) {
        this.profileImg = profileImg;
        this.userName = userName;
    }

    public void setProfileImg(int profileImg) {
        this.profileImg = profileImg;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
