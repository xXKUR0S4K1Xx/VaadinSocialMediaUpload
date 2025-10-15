package org.vaadin.example.social;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    List<PostEntity> findByParentId(Long parentId);
    List<PostEntity> findByUserName(String userName);
    List<PostEntity> findByForumId(Long forumId);

}

