package org.vaadin.example.social;
import java.util.Optional;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ForumService {

    private final ForumRepository forumRepo;
    private final ForumRoleRepository roleRepo;

    public ForumService(ForumRepository forumRepo, ForumRoleRepository roleRepo) {
        this.forumRepo = forumRepo;
        this.roleRepo = roleRepo;
        ensureDefaultForumExists();
    }

    public ForumEntity createForum(String name, Long creatorUserId, String description) {
        ForumEntity forum = new ForumEntity();
        forum.setName(name);
        forum.setCreatedBy(creatorUserId);
        forum.setDescription(description);
        forum = forumRepo.save(forum);

        // assign creator as ADMIN
        ForumRoleEntity role = new ForumRoleEntity();
        role.setForumId(forum.getId());
        role.setUserId(creatorUserId);
        role.setRole("ADMIN");
        roleRepo.save(role);

        return forum;
    }
    private void ensureDefaultForumExists() {
        forumRepo.findByName("all").orElseGet(() -> {
            ForumEntity forum = new ForumEntity();
            forum.setName("all");
            forum.setDescription("Default global forum");
            forum.setCreatedBy(0L); // system
            return forumRepo.save(forum);
        });
    }

    public List<ForumEntity> findAllForums() {
        return forumRepo.findAll();
    }
    public Optional<ForumEntity> findByName(String name) {
        return forumRepo.findByName(name);
    }
    public List<ForumRoleEntity> getRolesForForum(Long forumId) {
        return roleRepo.findByForumId(forumId);
    }
}
