package org.vaadin.example.social;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Div;

public class LikeButton extends Div {

    private Post post;

    public LikeButton(Post post) {
        this.post = post;

        // Create Avatar for the like icon
        Avatar likeIcon = new Avatar();
        likeIcon.setImage("https://cdn-icons-png.flaticon.com/512/126/126473.png"); // Like icon
        likeIcon.setName("Like");
        likeIcon.setWidth("24px");
        likeIcon.setHeight("24px");

        // Create a Div to hold the Avatar (for clickability)
        Div wrapper = new Div();
        wrapper.add(likeIcon);
        wrapper.setWidth("24px");
        wrapper.setHeight("24px");
        wrapper.getStyle()
                .set("cursor", "pointer")
                .set("border", "none")
                .set("box-shadow", "none");

        // Add click listener to the wrapper Div
        wrapper.addClickListener(event -> likePost());

        // Add wrapper to the main LikeButton container
        add(wrapper);
    }

    private void likePost() {
        User currentUser = User.getCurrentUser();
        if (currentUser == null) {
            System.out.println("No user logged in!");
            return;
        }

        String likedUsers = post.getLikedUsers(); // Get the current liked users (as a string)

        // If the current user has already liked the post, do nothing
        if (likedUsers == null || likedUsers.isEmpty()) {
            likedUsers = currentUser.getUsername(); // Initialize with current user
        } else {
            // Split the likedUsers string into a list of usernames
            String[] usernames = likedUsers.split(",");

            // Check if the current user is already in the list
            for (String username : usernames) {
                if (username.equals(currentUser.getUsername())) {
                    System.out.println("User already liked this post.");
                    return; // If the user already liked the post, do nothing
                }
            }

            // If the user has not liked the post yet, add them to the list
            likedUsers += "," + currentUser.getUsername();
        }

        // Set the updated likedUsers string in the post
        post.setLikedUsers(likedUsers);

        // Increment the like count
        post.setLikes(post.getLikes() + 1);

        // Save the updated post
        UserPost.savePost(post);

        System.out.println("Liked post " + post.getPostId() + " by user " + currentUser.getUsername());
    }
}
