package org.vaadin.example.social;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import java.util.Arrays;

public class LikeButtonDB extends Div {

    private PostEntity post;
    private final PostService postService;
    private final UserService userService;

    public LikeButtonDB(PostEntity post, PostService postService, UserService userService) {
        this.post = post;
        this.postService = postService;
        this.userService = userService;

        Icon likeIcon = VaadinIcon.THUMBS_UP_O.create();
        likeIcon.addClassName("no-border-icon");
        this.addClassName("no-border-wrapper");
        likeIcon.setSize("10px");
        likeIcon.getStyle()
                .set("color", "#A0B3B6")
                .set("cursor", "pointer")
                .set("background-color", "#1a1a1b")
                .set("padding", "0")
                .set("margin", "0")
                .set("width", "24px")
                .set("height", "24px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");

        likeIcon.addClickListener(event -> likePost());

        add(likeIcon);
    }

    private void likePost() {
        UserEntity currentUser = UserUtils.getCurrentUser(); // utility method
        if (currentUser == null) {
            System.out.println("No user logged in!");
            return;
        }

        String likedUsers = post.getLikedUsers();
        boolean hasLiked = false;

        if (likedUsers == null || likedUsers.isEmpty()) {
            likedUsers = currentUser.getUsername();
            post.setLikes(post.getLikes() + 1);
            hasLiked = false; // user was not previously in the list
        } else {
            String[] usernames = likedUsers.split(",");
            hasLiked = Arrays.stream(usernames)
                    .anyMatch(u -> u.equals(currentUser.getUsername()));

            if (hasLiked) {
                likedUsers = String.join(",", Arrays.stream(usernames)
                        .filter(u -> !u.equals(currentUser.getUsername()))
                        .toArray(String[]::new));
                post.setLikes(post.getLikes() - 1);
            } else {
                likedUsers += "," + currentUser.getUsername();
                post.setLikes(post.getLikes() + 1);
            }
        }

        post.setLikedUsers(likedUsers);
        postService.save(post);

        // Update user's total likes properly
        if (hasLiked) {
            currentUser.setLikeCount(currentUser.getLikeCount() - 1);
        } else {
            currentUser.setLikeCount(currentUser.getLikeCount() + 1);
        }
        userService.save(currentUser);
    }

    }

