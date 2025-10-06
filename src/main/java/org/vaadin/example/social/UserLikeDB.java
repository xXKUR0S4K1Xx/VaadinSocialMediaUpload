package org.vaadin.example.social;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_likes")
@IdClass(UserLikeKey.class)
public class UserLikeDB {

    @Id
    private Long userId;

    @Id
    private Long postId;

    public UserLikeDB() {}  // default constructor

    public UserLikeDB(Long userId, Long postId) {
        this.userId = userId;
        this.postId = postId;
    }

    // --- Getters & Setters ---
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
}
