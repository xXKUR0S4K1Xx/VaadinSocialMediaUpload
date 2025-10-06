package org.vaadin.example.social;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // auto-increment primary key

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // consider storing hashed passwords

    @Column(name = "avatar_url")
    private String avatarUrl; // avatar URL

    private int postCount;
    private int likeCount;

    @ElementCollection
    @CollectionTable(name = "user_posts", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "post_name", length = 500)
    private List<String> postNames;

    // Default constructor required by JPA
    public UserEntity() {}

    public UserEntity(String username, String password, String avatarUrl, int postCount, int likeCount, List<String> postNames) {
        this.username = username;
        this.password = password;
        this.avatarUrl = avatarUrl;
        this.postCount = postCount;
        this.likeCount = likeCount;
        this.postNames = postNames;
    }

    // --- Getters & Setters ---
    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public int getPostCount() { return postCount; }
    public void setPostCount(int postCount) { this.postCount = postCount; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public List<String> getPostNames() { return postNames; }
    public void setPostNames(List<String> postNames) { this.postNames = postNames; }
}
