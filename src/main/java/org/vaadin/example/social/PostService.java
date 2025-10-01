package org.vaadin.example.social;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository repository;

    public PostService(PostRepository repository) {
        this.repository = repository;
    }

    public PostEntity save(PostEntity post) {
        return repository.save(post);
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
}
