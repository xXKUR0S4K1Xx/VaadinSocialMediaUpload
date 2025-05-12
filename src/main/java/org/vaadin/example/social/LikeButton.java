package org.vaadin.example.social;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Div;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

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
        likeIcon.getStyle().set("cursor", "pointer");  // Change cursor to pointer on hover

        // Create a Div to hold the Avatar (for click ability)
        Div wrapper = new Div();
        wrapper.add(likeIcon);
        wrapper.setWidth("24px");
        wrapper.setHeight("24px");
        wrapper.getStyle()
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

        // If the likedUsers string is empty, initialize it with the current user
        if (likedUsers == null || likedUsers.isEmpty()) {
            likedUsers = currentUser.getUsername(); // Initialize with current user
            post.setLikes(post.getLikes() + 1);  // Increment likes when the post is liked
            System.out.println("Liked post " + post.getPostId() + " by user " + currentUser.getUsername());
        } else {
            // Split the likedUsers string into a list of usernames
            String[] usernames = likedUsers.split(",");

            // Check if the current user is already in the list
            boolean hasLiked = false;
            for (String username : usernames) {
                if (username.equals(currentUser.getUsername())) {
                    hasLiked = true; // User already liked the post
                    break;
                }
            }

            if (hasLiked) {
                // Remove the current user from the liked list (unlike)
                likedUsers = String.join(",", Arrays.stream(usernames)
                        .filter(username -> !username.equals(currentUser.getUsername()))  // Remove current user from list
                        .toArray(String[]::new));

                // Decrease the like count when the post is unliked
                post.setLikes(post.getLikes() - 1);
                System.out.println("Removed like from post " + post.getPostId() + " by user " + currentUser.getUsername());
            } else {
                // If the user has not liked the post yet, add them to the list
                likedUsers += "," + currentUser.getUsername();
                // Increase the like count when the post is liked
                post.setLikes(post.getLikes() + 1);
                System.out.println("Liked post " + post.getPostId() + " by user " + currentUser.getUsername());
            }
        }

        // Set the updated likedUsers string in the post
        post.setLikedUsers(likedUsers);

        // Save the updated post
        UserPost.savePost(post);

        // Now, we need to update the author's number of likes in their user file
        updateUserLikes(post.getUserName());
    }

    private void updateUserLikes(String username) {
        File userDirectory = new File("C:/Users/sdachs/IdeaProjects/vaadin-programmieraufgaben/users");
        File[] userFiles = userDirectory.listFiles((dir, name) -> name.endsWith(".txt"));

        if (userFiles != null) {
            for (File userFile : userFiles) {
                if (userFile.getName().equals(username + ".txt")) {
                    // Read the file, update the likes, and save it back
                    try {
                        List<String> lines = Files.readAllLines(userFile.toPath());
                        if (!lines.isEmpty()) {
                            String[] userParts = lines.get(0).split("#");

                            // Assuming the format: username#email#likesCount...
                            if (userParts.length > 2) {
                                int currentLikes = Integer.parseInt(userParts[2]); // Get current likes count
                                // Update the likes count based on whether a like was added or removed
                                userParts[2] = String.valueOf(currentLikes + (post.getLikes() > currentLikes ? 1 : -1));

                                // Rebuild the user data line
                                String updatedUserData = String.join("#", userParts);

                                // Write the updated data back to the file
                                Files.write(userFile.toPath(), updatedUserData.getBytes());
                                System.out.println("Updated likes for user: " + username);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Error updating user likes for " + username);
                    }
                    break;
                }
            }
        } else {
            System.out.println("User directory not found.");
        }
    }
}
