package org.vaadin.example.social;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "forum_roles", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"forum_id", "user_id", "role"})
})
public class ForumRoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "forum_id", nullable = false)
    private Long forumId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(length = 16, nullable = false)
    private String role; // ADMIN or MODERATOR

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt = LocalDateTime.now();

    // --- Getters / Setters ---
    public Long getId() { return id; }

    public Long getForumId() { return forumId; }
    public void setForumId(Long forumId) { this.forumId = forumId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
}
