package org.vaadin.example.social;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

// Represents a single post or reply in the system
public class Post {
    private String postId;          // Unique identifier for this post
    private String parentId;        // ID of the parent post (0 if this is not a reply)
    private int likes;              // Number of likes this post has received
    private String parentUser;      // Username of the author of the parent post (if any)
    private String postContent;     // The text content of the post
    private String timestamp;       // When the post was created (in string format)
    private String userName;        // The username of the author of this post
    private String likedUsers;      // Comma-separated usernames of people who liked the post

    // Constructor to initialize a new Post object with all fields
    public Post(String postId, String parentId, int likes, String parentUser,
                String postContent, String timestamp, String userName, String likedUsers) {
        this.postId = postId;
        this.parentId = parentId;
        this.likes = likes;             // Set likes as an integer
        this.parentUser = parentUser;
        this.postContent = postContent;
        this.timestamp = timestamp;
        this.userName = userName;
        this.likedUsers = likedUsers;
    }

    // Getters and setters if needed
    public String getPostId() {
        return postId;
    }

    public String getParentId() {
        return parentId;
    }

    public int getLikes() {
        return likes;
    }

    public String getParentUser() {
        return parentUser;
    }

    public String getPostContent() {
        return postContent;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getUserName() {
        return userName;
    }

    public String getLikedUsers() {
        return likedUsers;
    }



    // Setter for liked users (replaces the whole string)
    public void setLikedUsers(String likedUsers) {
        this.likedUsers = likedUsers;
    }

    // Adds a like from the given username if they haven't already liked the post
    public void addLike(String username) {
        if (likedUsers == null || likedUsers.isEmpty()) {
            likedUsers = username; // If no likes yet, start the list
        } else {
            if (!likedUsers.contains(username)) {
                likedUsers += "," + username; // Append new user if not already in the list
            }
        }
        likes++; // Increment like count regardless
    }

    public static String encodeContent(String content) {
        if (content == null) return "";
        return content.replace("\\", "\\\\")  // Escape backslashes first
                .replace("\n", "\\n")  // Escape line breaks
                .replace("\r", "");    // Optionally remove carriage returns
    }

    public static String decodeContent(String content) {
        if (content == null) return "";
        return content.replace("\\n", "\n")
                .replace("\\\\", "\\");  // Unescape backslashes last
    }

    // Setter for the likes field (manually override the count)
    public void setLikes(int likes) {
        this.likes = likes;
    }

    // Format a timestamp string into a human-readable format (dd-MM-yyyy/HH:mm)
    public static String formatTimestamp(String timestamp) {
        try {
            // Try parsing it as a Unix timestamp
            long timestampInSeconds = Long.parseLong(timestamp);
            Instant instant = Instant.ofEpochSecond(timestampInSeconds);
            LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy/HH:mm");
            return dateTime.format(formatter);
        } catch (NumberFormatException e) {
            // If not a Unix timestamp, try ISO-8601 format
            try {
                LocalDateTime dateTime = LocalDateTime.parse(timestamp);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy/HH:mm");
                return dateTime.format(formatter);
            } catch (Exception ex) {
                // Fallback: return the original value if parsing fails
                return timestamp;
            }
        }
    }


    // Converts the Post object into a single string for file storage (fields joined with "#")
    @Override
    public String toString() {
        return String.join("#",
                postId,
                parentId,
                String.valueOf(likes),
                parentUser == null ? "" : parentUser,
                postContent == null ? "" : encodeContent(postContent),
                timestamp == null ? "" : timestamp,
                userName == null ? "" : userName,
                likedUsers == null ? "" : likedUsers
        );
    }
    public static Post fromString(String line) {
        String[] parts = line.split("#", -1);  // -1 to preserve trailing empty strings

        if (parts.length < 8) {
            throw new IllegalArgumentException("Invalid post format: " + line);
        }

        String postId = parts[0];
        String parentId = parts[1];
        int likes = Integer.parseInt(parts[2]);
        String parentUser = parts[3];
        String postContent = decodeContent(parts[4]);
        String timestamp = parts[5];
        String userName = parts[6];
        String likedUsers = parts[7];

        return new Post(postId, parentId, likes, parentUser, postContent, timestamp, userName, likedUsers);
    }


}