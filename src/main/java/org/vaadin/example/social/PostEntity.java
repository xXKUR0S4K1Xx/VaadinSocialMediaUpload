package org.vaadin.example.social;

import jakarta.persistence.*;

@Entity
@Table(name = "posts")
public class PostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // DB primary key

    private String postId;        // optional old postId for compatibility
    private Long parentId;        // 0 if no parent
    private int likes;
    private String parentUser;

    @Column(length = 5000)
    private String postContent;

    private String timestamp;
    private String userName;

    @Column(length = 1000)
    private String likedUsers;

    // --- Constructors ---
    public PostEntity() {} // JPA requires default constructor

    public PostEntity(String postId, String parentId, int likes, String parentUser,
                      String postContent, String timestamp, String userName, String likedUsers) {
        this.postId = postId;
        this.parentId = parentId == null ? 0L : Long.parseLong(parentId);
        this.likes = likes;
        this.parentUser = parentUser;
        this.postContent = postContent;
        this.timestamp = timestamp;
        this.userName = userName;
        this.likedUsers = likedUsers;
    }

    // --- Getters/Setters ---
    public Long getId() { return id; }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public String getParentUser() { return parentUser; }
    public void setParentUser(String parentUser) { this.parentUser = parentUser; }

    public String getPostContent() { return postContent; }
    public void setPostContent(String postContent) { this.postContent = postContent; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getLikedUsers() { return likedUsers; }
    public void setLikedUsers(String likedUsers) { this.likedUsers = likedUsers; }

    // --- Like handling ---
    public void addLike(String username) {
        if (likedUsers == null || likedUsers.isEmpty()) {
            likedUsers = username;
        } else if (!likedUsers.contains(username)) {
            likedUsers += "," + username;
        }
        likes++;
    }

    // --- Migration helper ---
    public static PostEntity fromOldPost(Post oldPost) {
        return new PostEntity(
                oldPost.getPostId(),
                oldPost.getParentId(),
                oldPost.getLikes(),
                oldPost.getParentUser(),
                oldPost.getPostContent(),
                oldPost.getTimestamp(),
                oldPost.getUserName(),
                oldPost.getLikedUsers()
        );
    }
}
