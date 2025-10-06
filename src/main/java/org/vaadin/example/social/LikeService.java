package org.vaadin.example.social;

import org.springframework.stereotype.Service;

@Service
public class LikeService {

    private final UserLikeRepository userLikeRepository;
    private final PostService postService;

    public LikeService(UserLikeRepository userLikeRepository, PostService postService) {
        this.userLikeRepository = userLikeRepository;
        this.postService = postService;
    }

    public void likePost(UserEntity user, PostEntity post) {
        // Check if user already liked this post
        if (!userLikeRepository.existsByUserIdAndPostId(user.getId(), post.getId())) {
            // Increment likes
            post.setLikes(post.getLikes() + 1);
            postService.save(post);

            // Save the user-post like relation
            UserLikeDB like = new UserLikeDB(); // or UserLike if you rename
            like.setUserId(user.getId());
            like.setPostId(post.getId());
            userLikeRepository.save(like);
        } else {
            // Optional: show notification "You've already liked this post"
        }
    }
}
