package model;

/**
 * Created by Mercer on 02.03.2018.
 * структура даних у вигляді повідомлення
 */

public class Message {
    private String value = "";
    private String type = "";
    private String timestamp = "";
    private String profilePhoto = "";
    private String sender = "";
    private String chatId;


    public Message(String value, String type, String timestamp, String profilePhoto, String sender, String chatId) {
        this.value = value;
        this.type = type;
        this.timestamp = timestamp;
        this.profilePhoto= profilePhoto;
        this.sender = sender;
        this.setChatId(chatId);
    }


    public Message(Message message){
        this.value = message.getValue();
        this.type = message.getType();
        this.timestamp = message.getTimestamp();
        this.profilePhoto= message.getProfilePhoto();
        this.sender = message.getSender();
    }

    public Message() {

    }

    @Override
    public String toString() {
        return getValue();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }
}
