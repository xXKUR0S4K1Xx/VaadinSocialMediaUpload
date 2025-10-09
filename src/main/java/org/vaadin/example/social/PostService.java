package org.vaadin.example.social;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository repository;
    private final UserService userService;  // <-- added UserService

    public PostService(PostRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    // Save post and associate with logged-in user
    public PostEntity save(PostEntity post) {
        // Find the user by username
        userService.findByUsername(post.getUserName()).ifPresent(user -> {
            // Increment user's post count
            user.setPostCount(user.getPostCount() + 1);
            userService.save(user);
        });

        return repository.save(post);
    }
    public List<PostEntity> findPostsByUser(String username) {
        return repository.findByUserName(username);
    }

    public List<PostEntity> findAll() {
        return repository.findAll();
    }

    public Optional<PostEntity> findById(Long id) {
        return repository.findById(id);
    }

    public List<PostEntity> findReplies(Long parentId) {
        return repository.findByParentId(parentId);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    // Create a post directly from a user entity and content
    public PostEntity createPost(UserEntity user, String content) {
        PostEntity post = new PostEntity();
        post.setUserName(user.getUsername());
        post.setPostContent(content);
        post.setTimestamp(java.time.LocalDateTime.now().toString());
        post.setParentId(0L);
        post.setLikes(0);

        // Save post and increment user's post count
        save(post);

        return post;
    }
}
