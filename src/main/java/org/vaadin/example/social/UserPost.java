package org.vaadin.example.social;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Route("userpost")
public class UserPost {

    private static final String postsDirectory = "C:/Users/sdachs/IdeaProjects/vaadin-programmieraufgaben/posts";

    // Read posts from files and return a list of Post objects
    public static List<Post> readPostsFromFiles() {
        List<Post> posts = new ArrayList<>();

        try {
            Files.list(Paths.get(postsDirectory))
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            List<String> lines = Files.readAllLines(file);
                            for (String line : lines) {
                                String[] parts = line.split("#", -1);

                                // ✅ Trim to first 8 fields if too many
                                if (parts.length > 8) {
                                    parts = Arrays.copyOf(parts, 8);
                                    line = String.join("#", parts);
                                }

                                if (parts.length == 8) {
                                    try {
                                        Post post = new Post(
                                                parts[0], // postId
                                                parts[1], // parentId
                                                Integer.parseInt(parts[2]), // likes
                                                parts[3], // commenters
                                                parts[4], // postContent
                                                parts[5], // timestamp
                                                parts[6], // userName
                                                parts[7]  // likedUsers
                                        );
                                        posts.add(post);
                                    } catch (Exception e) {
                                        System.err.println("Invalid post data: " + line);
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("Error reading file: " + file);
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error listing files in directory: " + postsDirectory);
            e.printStackTrace();
        }

        // ✅ Sort posts by postId descending (latest first)
        posts.sort((p1, p2) -> {
            try {
                return Integer.compare(
                        Integer.parseInt(p2.getPostId()),
                        Integer.parseInt(p1.getPostId())
                );
            } catch (NumberFormatException e) {
                return 0;
            }
        });

        return posts;
    }

    public static List<Post> readPostsSortedByLikes() {
        List<Post> posts = new ArrayList<>();

        try {
            Files.list(Paths.get(postsDirectory))
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            List<String> lines = Files.readAllLines(file);
                            for (String line : lines) {
                                String[] parts = line.split("#", -1);

                                if (parts.length > 8) {
                                    parts = Arrays.copyOf(parts, 8);
                                    line = String.join("#", parts);
                                }

                                if (parts.length == 8) {
                                    try {
                                        Post post = new Post(
                                                parts[0],
                                                parts[1],
                                                Integer.parseInt(parts[2]),
                                                parts[3],
                                                parts[4],
                                                parts[5],
                                                parts[6],
                                                parts[7]
                                        );
                                        posts.add(post);
                                    } catch (Exception e) {
                                        System.err.println("Invalid post data: " + line);
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("Error reading file: " + file);
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error listing files in directory: " + postsDirectory);
            e.printStackTrace();
        }

        // ✅ Sort by number of likes descending
        posts.sort((p1, p2) -> Integer.compare(p2.getLikes(), p1.getLikes()));

        return posts;
    }




    // Save an updated post back to the file system
    public static void savePost(Post updatedPost) {
        List<Post> posts = readPostsFromFiles();

        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).getPostId().equals(updatedPost.getPostId())) {
                posts.set(i, updatedPost); // Replace the old one
                break;
            }
        }

        // Save all posts back to files
        for (Post post : posts) {
            try {
                Files.write(Paths.get(postsDirectory, post.getPostId()), post.toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Create the UI component for replying to a post
    public Component createReplyInputSection(Post parentPost) {
        VerticalLayout replySection = new VerticalLayout();
        replySection.setSpacing(false);
        replySection.setPadding(false);
        replySection.setMargin(false);
        replySection.setWidthFull();

        Button toggleReplyButton = new Button("Reply");
        toggleReplyButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        toggleReplyButton.getStyle().set("margin-top", "10px");

        HorizontalLayout inputRow = new HorizontalLayout();
        inputRow.setVisible(false); // Start hidden
        inputRow.setWidthFull();
        inputRow.setAlignItems(Alignment.CENTER);
        inputRow.getStyle().set("margin-top", "10px");

        TextArea replyField = new TextArea();
        replyField.setPlaceholder("Write your reply...");
        replyField.setWidthFull();
        replyField.setMaxLength(500);
        replyField.setHeight("80px");

        Button sendButton = new Button("Send");
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        inputRow.add(replyField, sendButton);

        toggleReplyButton.addClickListener(e -> inputRow.setVisible(!inputRow.isVisible()));

        sendButton.addClickListener(e -> {
            String replyContent = replyField.getValue();

            if (!replyContent.trim().isEmpty()) {
                createAndSaveReply(parentPost, replyContent);
            }

            replyField.clear();
            inputRow.setVisible(false);
        });

        replySection.add(toggleReplyButton, inputRow);
        return replySection;
    }

    // Method to create and save a new reply post with a unique ID
    public static void createAndSaveReply(Post parentPost, String replyContent) {
        if (replyContent == null || replyContent.trim().isEmpty()) {
            return; // Don't create a post if the content is empty
        }

        // Get current user from file
        String currentUser = "Guest";
        try {
            currentUser = Files.readString(Paths.get("loggedinuser.txt")).trim();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get current time in seconds
        long currentTimeInSeconds = System.currentTimeMillis() / 1000;
        String formattedTimestamp = Post.formatTimestamp(String.valueOf(currentTimeInSeconds));

        // Find the maximum postId in the existing posts and create a new unique ID for the reply
        File folder = new File(postsDirectory);
        File[] files = folder.listFiles((dir, name) -> !name.contains("."));
        int nextId = files != null && files.length > 0 ? Arrays.stream(files)
                .mapToInt(file -> Integer.parseInt(file.getName()))
                .max()
                .orElse(0) + 1 : 1;

        // Create a new reply post with a unique ID
        Post newReply = new Post(
                String.valueOf(nextId),             // 1. postId
                parentPost.getPostId(),             // 2. parentId
                0,                                  // 3. likes (default value)
                parentPost.getUserName(),           // 4. parentUser
                replyContent,                       // 5. postContent
                formattedTimestamp,                 // 6. timestamp
                currentUser,                        // 7. userName
                ""                                   // 8. likedUsers
        );




        // Save the new reply post
        saveNewPost(newReply, false); // False means this is not a reply, so post count should be updated for the replying user
    }



    // Save a new post (modified for reply handling)
    // Speichert einen bereits fertig erstellten Post
    public static void saveNewPost(Post newPost, boolean isReply) {
        try {
            // Save the post without the .txt extension
            Files.write(Paths.get(postsDirectory, newPost.getPostId()), newPost.toString().getBytes());

            // Only update the post count if it's a normal post (not a reply)
                updateUserPosts(newPost.getUserName()); // Update post count for the user who created the post
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    // Find a post by its ID
    public static Post findPostById(String parentId) {
        List<Post> allPosts = readPostsFromFiles();
        for (Post post : allPosts) {
            if (post.getPostId().equals(parentId)) {
                return post;
            }
        }
        return null;
    }
    private static void updateUserPosts(String username) {
        File userDirectory = new File("C:/Users/sdachs/IdeaProjects/vaadin-programmieraufgaben/users");
        File[] userFiles = userDirectory.listFiles((dir, name) -> name.endsWith(".txt"));

        if (userFiles != null) {
            for (File userFile : userFiles) {
                if (userFile.getName().equals(username + ".txt")) {
                    // Read the file, update the posts, and save it back
                    try {
                        List<String> lines = Files.readAllLines(userFile.toPath());
                        if (!lines.isEmpty()) {
                            String[] userParts = lines.get(0).split("#");

                            // Assuming the format: username#numberOfPosts#numberOfLikes
                            if (userParts.length == 3) {
                                // Get the current number of posts (index 1 in userParts)
                                int currentPosts = Integer.parseInt(userParts[1]);
                                userParts[1] = String.valueOf(currentPosts + 1); // Increment posts by 1

                                // Rebuild the user data line
                                String updatedUserData = String.join("#", userParts);

                                // Write the updated data back to the file
                                Files.write(userFile.toPath(), updatedUserData.getBytes());
                                System.out.println("Updated posts for user: " + username);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Error updating user posts for " + username);
                    }
                    break;
                }
            }
        } else {
            System.out.println("User directory not found.");
        }
    }

    //Erstellt einen neuen Post (mit neuer ID, Username, Zeitstempel).
    public static void createAndSaveNewPost(String postContent) {
        // Get all posts and find the maximum existing post ID
        File folder = new File(postsDirectory);
        File[] files = folder.listFiles((dir, name) -> !name.contains("."));
        int nextId = files != null && files.length > 0 ? Arrays.stream(files)
                .mapToInt(file -> Integer.parseInt(file.getName()))
                .max()
                .orElse(0) + 1 : 1;

        // Get current user from file
        String currentUser = "Guest";
        try {
            currentUser = Files.readString(Paths.get("loggedinuser.txt")).trim();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get current time in seconds (flat, no milliseconds)
        long currentTimeInSeconds = System.currentTimeMillis() / 1000;

        // Convert the timestamp to the desired format before saving
        String formattedTimestamp = Post.formatTimestamp(String.valueOf(currentTimeInSeconds));

        // Create the new post
        Post newPost = new Post(
                String.valueOf(nextId),             // 1. postId
                "0",                                // 2. parentId (not a reply)
                0,                                  // 3. likes (default value)
                "",                                 // 4. parentUser (not used unless it's a reply)
                postContent,                        // 5. post content
                formattedTimestamp,                 // 6. timestamp
                currentUser,                        // 7. username of the author
                ""                                  // 8. likedUsers (empty string for new post)
        );



        // Save the new post to file with the unique ID
        saveNewPost(newPost, false);  // False means this is not a reply, so the post count should be updated for the user
    }


}
