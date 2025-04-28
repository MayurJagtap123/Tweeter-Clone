package com.example.socialmediaapp.data;

public class MessageThread {
    private String threadId;
    private String otherUserId;
    private String otherUserName;
    private String otherUserImage;
    private String lastMessage;
    private String lastMessageSenderId;
    private long lastMessageTime;
    private boolean isRead;
    
    public MessageThread() {
        // Required empty constructor for Firebase
    }
    
    public MessageThread(String otherUserId, String otherUserName, String otherUserImage) {
        this.otherUserId = otherUserId;
        this.otherUserName = otherUserName;
        this.otherUserImage = otherUserImage;
        this.lastMessageTime = System.currentTimeMillis();
        this.isRead = true;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(String otherUserId) {
        this.otherUserId = otherUserId;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public String getOtherUserImage() {
        return otherUserImage;
    }

    public void setOtherUserImage(String otherUserImage) {
        this.otherUserImage = otherUserImage;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public void updateLastMessage(String message, String senderId) {
        this.lastMessage = message;
        this.lastMessageSenderId = senderId;
        this.lastMessageTime = System.currentTimeMillis();
        this.isRead = false;
    }
}
