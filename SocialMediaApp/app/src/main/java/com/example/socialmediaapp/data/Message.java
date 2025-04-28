package com.example.socialmediaapp.data;

public class Message {
    private String messageId;
    private String senderId;
    private String content;
    private long timestamp;
    private boolean isRead;

    public Message() {
        // Required empty constructor for Firebase
    }

    public Message(String senderId, String content) {
        this.senderId = senderId;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
