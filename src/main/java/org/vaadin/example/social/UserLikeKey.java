package org.vaadin.example.social;

import java.io.Serializable;
import java.util.Objects;

public class UserLikeKey implements Serializable {

    private Long userId;
    private Long postId;

    public UserLikeKey() {}  // default constructor

    public UserLikeKey(Long userId, Long postId) {
        this.userId = userId;
        this.postId = postId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserLikeKey)) return false;
        UserLikeKey that = (UserLikeKey) o;
        return Objects.equals(userId, that.userId) && Objects.equals(postId, that.postId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, postId);
    }
}
