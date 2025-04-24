package org.vaadin.example.social;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Post {
    private String postId;
    private String parentId;
    private int likes;
    private String parentUser;
    private String postContent;
    private String timestamp;
    private String userName;
    private String likedUsers;  // this will remain a string to keep it as per your structure

    // Constructor
    public Post(String postId, String parentId, int likes, String parentUser, String postContent,
                String timestamp, String userName, String likedUsers) {
        this.postId = postId;
        this.parentId = parentId;
        this.likes = likes;
        this.parentUser = parentUser;
        this.postContent = postContent;
        this.timestamp = timestamp;
        this.userName = userName;
        this.likedUsers = likedUsers;
    }

    // Getters
    public String getPostId() { return postId; }
    public String getParentId() { return parentId; }
    public int getLikes() { return likes; }
    public String getParentUser() { return parentUser; }
    public String getPostContent() { return postContent; }
    public String getUserName() { return userName; }
    public String getLikedUsers() { return likedUsers; }

    // Setter for likedUsers
    public void setLikedUsers(String likedUsers) {
        this.likedUsers = likedUsers;
    }

    // Add a like to the post
    public void addLike(String username) {
        if (likedUsers == null || likedUsers.isEmpty()) {
            likedUsers = username;  // If there are no liked users, set the first one
        } else {
            if (!likedUsers.contains(username)) {
                likedUsers += "," + username;  // Add the new user to the list (comma-separated)
            }
        }
        likes++;  // Increase the like count
    }

    // Update the likes count (if needed in the future)
    public void setLikes(int likes) {
        this.likes = likes;
    }

    public static String formatTimestamp(String timestamp) {
        // Check if the timestamp is a Unix timestamp (numeric string)
        try {
            long timestampInSeconds = Long.parseLong(timestamp);
            Instant instant = Instant.ofEpochSecond(timestampInSeconds);
            LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy/HH:mm");
            return dateTime.format(formatter);
        } catch (NumberFormatException e) {
            // If it's not a Unix timestamp, try parsing as ISO 8601 format
            try {
                LocalDateTime dateTime = LocalDateTime.parse(timestamp);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy/HH:mm");
                return dateTime.format(formatter);
            } catch (Exception ex) {
                // If both parsing attempts fail, return the original string
                return timestamp;
            }
        }

    }
    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.join("#",
                postId,
                parentId,
                String.valueOf(likes),
                parentUser, // commenters (in your app, this is used for "parentUser")
                postContent,
                timestamp,
                userName,
                likedUsers
        );
    }
}
