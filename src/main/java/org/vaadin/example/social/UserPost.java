package org.vaadin.example.social;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Route("userpost")
public class UserPost {

    private static final String postsDirectory = "C:/Users/sdachs/IdeaProjects/vaadin-programmieraufgaben/posts";

    // Read posts from files and return a list of Post objects in Media
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

                                // Trim to first 8 fields if too many
                                if (parts.length > 8) {
                                    parts = Arrays.copyOf(parts, 8);
                                    line = String.join("#", parts);
                                }

                                if (parts.length == 8) {
                                    try {
                                        // Decode postContent here
                                        String decodedContent = Post.decodeContent(parts[4]);

                                        Post post = new Post(
                                                parts[0], // postId
                                                parts[1], // parentId
                                                Integer.parseInt(parts[2]), // likes
                                                parts[3], // commenters (parentUser)
                                                decodedContent, // decoded postContent
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

        // Sort posts by postId descending (latest first)
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


    // same as the above but for The userpage. It only gets one users posts
    public static List<Post> readPostsForUser(String username) {
        List<Post> posts = new ArrayList<>();
        Path userPostsDir = Paths.get("C:/Users/sdachs/IdeaProjects/vaadin-programmieraufgaben/users", username, "Posts");

        if (!Files.exists(userPostsDir)) {
            return posts; // Return empty if folder doesn't exist
        }

        try {
            Files.list(userPostsDir)
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
                                    // Decode content here!
                                    String decodedContent = Post.decodeContent(parts[4]);

                                    Post post = new Post(
                                            parts[0], // postId
                                            parts[1], // parentId
                                            Integer.parseInt(parts[2]), // likes
                                            parts[3], // parentUser
                                            decodedContent, // decoded content
                                            parts[5], // timestamp
                                            parts[6], // username
                                            parts[7]  // likedUsers
                                    );
                                    posts.add(post);
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("Error reading file: " + file);
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error listing files for user: " + username);
            e.printStackTrace();
        }

        // Sort by postId descending (latest first)
        posts.sort((p1, p2) -> Integer.compare(
                Integer.parseInt(p2.getPostId()),
                Integer.parseInt(p1.getPostId())
        ));

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




    //Aktualisiert einen bestehenden Post (z. B. bei Like oder Edit) und speichert alle Posts neu in den Dateien.
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

        Button replyButton = new Button("Reply", VaadinIcon.COMMENT_O.create());
        replyButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        replyButton.getStyle()
                .set("color", "#A0B3B6")
                .set("margin", "0")
                .set("padding", "0")
                .set("font-size", "14px");

        // Layout for icon + reply button
        HorizontalLayout replyButtonRow = new HorizontalLayout(replyButton);
        replyButtonRow.setSpacing(false);
        replyButtonRow.getStyle().set("gap", "5px"); // manually set small gap
        replyButtonRow.setAlignItems(Alignment.CENTER);

        // Reply input section
        HorizontalLayout inputRow = new HorizontalLayout();
        inputRow.setVisible(false);
        inputRow.setWidthFull();
        inputRow.setAlignItems(Alignment.CENTER);
        inputRow.getStyle().set("margin-top", "10px");

        TextArea replyField = new TextArea();
        replyField.setPlaceholder("Write your reply...");
        replyField.getElement().getStyle()
                .set("background-color", "#6c7a89")
                .set("color", "#A0B3B6");
        replyField.setWidthFull();
        replyField.setMaxLength(500);
        replyField.setHeight("80px");

        Button sendButton = new Button("Send");
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        inputRow.add(replyField, sendButton);

        replyButton.addClickListener(e -> inputRow.setVisible(!inputRow.isVisible()));

        sendButton.addClickListener(e -> {
            String replyContent = replyField.getValue();

            if (!replyContent.trim().isEmpty()) {
                createAndSaveReply(parentPost, replyContent);
            }

            replyField.clear();
            inputRow.setVisible(false);
        });

        replySection.add(replyButtonRow, inputRow);
        return replySection;
    }




    //Erstellt eine Antwort (Reply) auf einen bestehenden Post.
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



    //Speichert einen neuen Post oder Reply als Datei.
    public static void saveNewPost(Post newPost, boolean isReply) {
        try {
            // Save to main directory
            Files.write(Paths.get(postsDirectory, newPost.getPostId()), newPost.toString().getBytes());

            // Save to user-specific folder
            savePostToUserDirectory(newPost);

            // Only update post count if it's a top-level post
            if (!isReply) {
                updateUserPosts(newPost.getUserName());
            }

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

    //Erhöht den Post-Zähler eines Benutzers, wenn dieser einen neuen Post (kein Reply) erstellt.
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

    //Erstellt einen ganz neuen Post (kein Reply) und speichert ihn.
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

    private static void savePostToUserDirectory(Post post) {
        String username = post.getUserName();
        if (username == null || username.isEmpty()) {
            return; // safety check
        }

        Path userPostsDir = Paths.get("C:/Users/sdachs/IdeaProjects/vaadin-programmieraufgaben/users", username, "Posts");

        try {
            Files.createDirectories(userPostsDir); // create folder if not exists

            // Get the next available post number (based on number of existing files)
            long postNumber = Files.list(userPostsDir)
                    .filter(p -> !Files.isDirectory(p))
                    .count() + 1;

            Path userPostFile = userPostsDir.resolve(String.valueOf(postNumber));
            Files.write(userPostFile, post.toString().getBytes());

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to write user-specific post file for user: " + username);
        }
    }


}
