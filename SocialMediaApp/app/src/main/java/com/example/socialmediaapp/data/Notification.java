package com.example.socialmediaapp.data;

public class Notification {
    private String id;
    private String type; // "follow", "like", "retweet", "reply", "mention"
    private String sourceUserId;
    private String sourceUserName;
    private String sourceUserImage;
    private String targetUserId;
    private String tweetId;
    private String tweetContent;
    private long timestamp;

    public Notification() {
        // Required empty constructor for Firebase
    }

    public Notification(String type, String sourceUserId, String sourceUserName,
                       String sourceUserImage, String targetUserId, String tweetId,
                       String tweetContent) {
        this.type = type;
        this.sourceUserId = sourceUserId;
        this.sourceUserName = sourceUserName;
        this.sourceUserImage = sourceUserImage;
        this.targetUserId = targetUserId;
        this.tweetId = tweetId;
        this.tweetContent = tweetContent;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSourceUserId() {
        return sourceUserId;
    }

    public void setSourceUserId(String sourceUserId) {
        this.sourceUserId = sourceUserId;
    }

    public String getSourceUserName() {
        return sourceUserName;
    }

    public void setSourceUserName(String sourceUserName) {
        this.sourceUserName = sourceUserName;
    }

    public String getSourceUserImage() {
        return sourceUserImage;
    }

    public void setSourceUserImage(String sourceUserImage) {
        this.sourceUserImage = sourceUserImage;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getTweetId() {
        return tweetId;
    }

    public void setTweetId(String tweetId) {
        this.tweetId = tweetId;
    }

    public String getTweetContent() {
        return tweetContent;
    }

    public void setTweetContent(String tweetContent) {
        this.tweetContent = tweetContent;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getNotificationText() {
        switch (type) {
            case "follow":
                return sourceUserName + " followed you";
            case "like":
                return sourceUserName + " liked your Tweet";
            case "retweet":
                return sourceUserName + " Retweeted your Tweet";
            case "reply":
                return sourceUserName + " replied to your Tweet";
            case "mention":
                return sourceUserName + " mentioned you in a Tweet";
            default:
                return "";
        }
    }
}
