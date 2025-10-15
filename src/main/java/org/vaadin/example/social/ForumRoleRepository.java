package org.vaadin.example.social;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ForumRoleRepository extends JpaRepository<ForumRoleEntity, Long> {
    List<ForumRoleEntity> findByForumId(Long forumId);
    List<ForumRoleEntity> findByUserId(Long userId);
}
