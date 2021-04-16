package model;

/**
 * Created by Mercer on 01.03.2018.
 */

public class ChatInUser {
    private String username;
    private String profilePhoto;
    private String lastMessage;
    private String timestamp;
    private String chatId;
    private String phoneNumber;
    private String token;
    public ChatInUser() {
    }

    public ChatInUser(String phoneNumber, String chatId, String username, String senderProfilePhoto, String lastMessage, String token) {
        this.phoneNumber = phoneNumber;
        this.token = token;
        this.chatId = chatId;
        this.username = username;
        this.profilePhoto = senderProfilePhoto;
        this.lastMessage = lastMessage;
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

    public void setProfilePhoto(String senderProfilePhoto) {
        this.profilePhoto = senderProfilePhoto;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

