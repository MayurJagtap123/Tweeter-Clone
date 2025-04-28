package com.example.socialmediaapp.data;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String userId;
    private String email;
    private String username;
    private String handle;
    private String profileImageUrl;
    private String coverImageUrl;
    private String bio;
    private String location;
    private String website;
    private long joinDate;
    private int followersCount;
    private int followingCount;
    private int tweetsCount;
    private Map<String, Boolean> followers;
    private Map<String, Boolean> following;

    public User() {
        // Required empty constructor for Firebase
        followers = new HashMap<>();
        following = new HashMap<>();
    }

    public User(String userId, String email, String username, String handle) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.handle = handle;
        this.joinDate = System.currentTimeMillis();
        this.followersCount = 0;
        this.followingCount = 0;
        this.tweetsCount = 0;
        this.followers = new HashMap<>();
        this.following = new HashMap<>();
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getHandle() { return handle; }
    public void setHandle(String handle) { this.handle = handle; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public long getJoinDate() { return joinDate; }
    public void setJoinDate(long joinDate) { this.joinDate = joinDate; }

    public int getFollowersCount() { return followersCount; }
    public void setFollowersCount(int followersCount) { this.followersCount = followersCount; }

    public int getFollowingCount() { return followingCount; }
    public void setFollowingCount(int followingCount) { this.followingCount = followingCount; }

    public int getTweetsCount() { return tweetsCount; }
    public void setTweetsCount(int tweetsCount) { this.tweetsCount = tweetsCount; }

    public Map<String, Boolean> getFollowers() { return followers; }
    public void setFollowers(Map<String, Boolean> followers) { this.followers = followers; }

    public Map<String, Boolean> getFollowing() { return following; }
    public void setFollowing(Map<String, Boolean> following) { this.following = following; }
}
