package com.example.socialmediaapp.data;

import java.util.HashMap;
import java.util.Map;

public class Tweet {
    private String tweetId;
    private String userId;
    private String userName;
    private String userHandle;
    private String userProfileImage;
    private String content;
    private String mediaUrl;
    private long timestamp;
    private int likesCount;
    private int retweetsCount;
    private int repliesCount;
    private Map<String, Boolean> likes;
    private Map<String, Boolean> retweets;
    private String replyToTweetId;
    private boolean isRetweet;
    private String originalTweetId;

    public Tweet() {
        // Required empty constructor for Firebase
        likes = new HashMap<>();
        retweets = new HashMap<>();
    }

    public Tweet(String userId, String userName, String userHandle, String userProfileImage, 
                String content, String mediaUrl) {
        this.userId = userId;
        this.userName = userName;
        this.userHandle = userHandle;
        this.userProfileImage = userProfileImage;
        this.content = content;
        this.mediaUrl = mediaUrl;
        this.timestamp = System.currentTimeMillis();
        this.likes = new HashMap<>();
        this.retweets = new HashMap<>();
        this.likesCount = 0;
        this.retweetsCount = 0;
        this.repliesCount = 0;
        this.isRetweet = false;
    }

    // Getters and Setters
    public String getTweetId() { return tweetId; }
    public void setTweetId(String tweetId) { this.tweetId = tweetId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserHandle() { return userHandle; }
    public void setUserHandle(String userHandle) { this.userHandle = userHandle; }

    public String getUserProfileImage() { return userProfileImage; }
    public void setUserProfileImage(String userProfileImage) { this.userProfileImage = userProfileImage; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getLikesCount() { return likesCount; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }

    public int getRetweetsCount() { return retweetsCount; }
    public void setRetweetsCount(int retweetsCount) { this.retweetsCount = retweetsCount; }

    public int getRepliesCount() { return repliesCount; }
    public void setRepliesCount(int repliesCount) { this.repliesCount = repliesCount; }

    public Map<String, Boolean> getLikes() { return likes; }
    public void setLikes(Map<String, Boolean> likes) { this.likes = likes; }

    public Map<String, Boolean> getRetweets() { return retweets; }
    public void setRetweets(Map<String, Boolean> retweets) { this.retweets = retweets; }

    public String getReplyToTweetId() { return replyToTweetId; }
    public void setReplyToTweetId(String replyToTweetId) { this.replyToTweetId = replyToTweetId; }

    public boolean isRetweet() { return isRetweet; }
    public void setRetweet(boolean retweet) { isRetweet = retweet; }

    public String getOriginalTweetId() { return originalTweetId; }
    public void setOriginalTweetId(String originalTweetId) { this.originalTweetId = originalTweetId; }
}
