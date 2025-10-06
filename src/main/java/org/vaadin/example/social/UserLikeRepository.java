package org.vaadin.example.social;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLikeRepository extends JpaRepository<UserLikeDB, UserLikeKey> {
    boolean existsByUserIdAndPostId(Long userId, Long postId);
}
