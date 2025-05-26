package org.vaadin.example.social;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

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
        // Create the Vaadin thumbs-up icon
        Icon likeIcon = VaadinIcon.THUMBS_UP.create();
        likeIcon.setSize("20px");
        likeIcon.getStyle()
                .set("color", "#A0B3B6")
                .set("cursor", "pointer")
                .set("background", "transparent")       // Ensures icon itself has no background
                .set("box-shadow", "none")              // Removes any drop shadow
                .set("border", "none")                  // Removes border
                .set("padding", "0")
                .set("margin", "0");

// Wrap the icon in a Div to make it clickable
        Div wrapper = new Div(likeIcon);
        wrapper.setWidth("24px");
        wrapper.setHeight("24px");
        wrapper.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("background-color", "transparent") // Fully transparent background
                .set("border", "none")
                .set("box-shadow", "none")
                .set("padding", "0")
                .set("margin", "0");


// Add click listener to the wrapper
        wrapper.addClickListener(event -> likePost());

// Add wrapper to the layout/container
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
