package org.vaadin.example.social;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ForumRepository extends JpaRepository<ForumEntity, Long> {
    Optional<ForumEntity> findByName(String name);
}
