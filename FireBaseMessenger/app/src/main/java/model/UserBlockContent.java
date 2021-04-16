package model;

/**
 * Created by Mercer on 27.02.2018.
 */

public class UserBlockContent {

    private String Uid;
    private String phoneNumber;
    private String username;
    private String profilePhoto;
    private String token;

    public UserBlockContent(String profilePhoto, String phoneNumber, String username, String token) {
        this.setPhoneNumber(phoneNumber);
        this.setUsername(username);
        this.setProfilePhoto(profilePhoto);
        this.token = token;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
