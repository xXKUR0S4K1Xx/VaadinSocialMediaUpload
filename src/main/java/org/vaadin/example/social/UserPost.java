package org.vaadin.example.social;
import java.util.function.Supplier;
import java.util.function.Consumer;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.HasMenuItems;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Route("userpost")
public class UserPost {

    private static final String postsDirectory = "C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/posts";


    public static List<Post> readPostsFromFiles() {
        User currentUser = User.getCurrentUser();
        if (currentUser == null) {
            System.err.println("No logged-in user found.");
            return Collections.emptyList();
        }
        String username = currentUser.getUsername();
        return readPostsFromCurrentForum(username);
    }

    // This is kept for compatibility but not used in current logic
    public static List<Post> readPostsFromFiles(Path directory) {
        List<Post> posts = new ArrayList<>();

        try {
            Files.list(directory)
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
                                        String decodedContent = Post.decodeContent(parts[4]);
                                        Post post = new Post(
                                                parts[0],
                                                parts[1],
                                                Integer.parseInt(parts[2]),
                                                parts[3],
                                                decodedContent,
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
            System.err.println("Error listing files in directory: " + directory);
            e.printStackTrace();
        }

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

    // Same method name, but now reads posts from the forum folder indicated in user's Forum file
    public static List<Post> readPostsForUser(String username) {
        List<Post> posts = new ArrayList<>();

        // Read the forum name from user's Forum file
        Path forumFilePath = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users", username, "Forum");
        String forumName;
        try {
            List<String> lines = Files.readAllLines(forumFilePath);
            if (lines.isEmpty()) {
                System.err.println("Forum file is empty for user " + username);
                return posts;
            }
            forumName = lines.get(0).trim();
        } catch (IOException e) {
            System.err.println("Could not read Forum file for user " + username);
            e.printStackTrace();
            return posts;
        }

        Path forumPostsDir = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/Forum", forumName);

        // Create forum directory if missing
        if (!Files.exists(forumPostsDir)) {
            try {
                Files.createDirectories(forumPostsDir);
            } catch (IOException e) {
                System.err.println("Could not create forum posts directory: " + forumPostsDir);
                e.printStackTrace();
                return posts;
            }
        }

        // Read posts from forum posts directory
        try {
            Files.list(forumPostsDir)
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
                                    String decodedContent = Post.decodeContent(parts[4]);
                                    Post post = new Post(
                                            parts[0],
                                            parts[1],
                                            Integer.parseInt(parts[2]),
                                            parts[3],
                                            decodedContent,
                                            parts[5],
                                            parts[6],
                                            parts[7]
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
            System.err.println("Error listing files for forum directory: " + forumPostsDir);
            e.printStackTrace();
        }

        posts.sort((p1, p2) -> Integer.compare(
                Integer.parseInt(p2.getPostId()),
                Integer.parseInt(p1.getPostId())
        ));

        return posts;
    }

    // Helper method used by readPostsFromFiles()
    private static List<Post> readPostsFromCurrentForum(String username) {
        return readPostsForUser(username);
    }
    public static List<Post> readPostsSortedByLikes() {
        return readPostsSortedByLikes(Paths.get(postsDirectory));
    }

    // Overloaded version with Path argument
    public static List<Post> readPostsSortedByLikes(Path directory) {
        List<Post> posts = new ArrayList<>();

        try {
            Files.list(directory)
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
                                                parts[0],    // postId
                                                parts[1],    // parentId
                                                Integer.parseInt(parts[2]),  // likes
                                                parts[3],    // commenters (parentUser)
                                                parts[4],    // postContent (not decoded here, decode if needed)
                                                parts[5],    // timestamp
                                                parts[6],    // userName
                                                parts[7]     // likedUsers
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
            System.err.println("Error listing files in directory: " + directory);
            e.printStackTrace();
        }

        // Sort by likes descending
        posts.sort((p1, p2) -> Integer.compare(p2.getLikes(), p1.getLikes()));

        return posts;
    }

    public static List<Post> readPostsForUserSortedByLikes(String username) {
        List<Post> posts = new ArrayList<>();
        Path userPostsDir = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users", username, "Posts");
     //   Path userPostsDir = Paths.get("C:\\Users\\0\\IdeaProjects\\VaadinSocialMediaUpload\\users", username, "Posts");


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

        //  Sort by likes descending
        posts.sort((p1, p2) -> Integer.compare(p2.getLikes(), p1.getLikes()));

        return posts;
    }



    //Aktualisiert einen bestehenden Post und speichert alle Posts neu in den Dateien.
    public static void savePost(Post updatedPost) {
        // Get current user
        User currentUser = User.getCurrentUser();
        if (currentUser == null) {
            System.err.println("No logged-in user found.");
            return;
        }
        String username = currentUser.getUsername();

        // Read posts from current user's forum folder
        List<Post> posts = readPostsForUser(username);

        // Update the post in the list
        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).getPostId().equals(updatedPost.getPostId())) {
                posts.set(i, updatedPost); // Replace the old one
                break;
            }
        }

        // Save all posts back to the current forum posts directory
        // Get forum name from user's Forum file
        Path forumFilePath = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users", username, "Forum");
        String forumName;
        try {
            List<String> lines = Files.readAllLines(forumFilePath);
            if (lines.isEmpty()) {
                System.err.println("Forum file is empty for user " + username);
                return;
            }
            forumName = lines.get(0).trim();
        } catch (IOException e) {
            System.err.println("Could not read Forum file for user " + username);
            e.printStackTrace();
            return;
        }

        Path forumPostsDir = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/Forum", forumName);
        if (!Files.exists(forumPostsDir)) {
            try {
                Files.createDirectories(forumPostsDir);
            } catch (IOException e) {
                System.err.println("Could not create forum posts directory: " + forumPostsDir);
                e.printStackTrace();
                return;
            }
        }

        // Save updated posts to forum posts directory
        for (Post post : posts) {
            try {
                Path postFile = forumPostsDir.resolve(post.getPostId());
                Files.write(postFile, post.toString().getBytes());
            } catch (IOException e) {
                System.err.println("Failed to save post: " + post.getPostId());
                e.printStackTrace();
            }
        }

        // Also save the post in the user's personal directory (your existing method)
        savePostToUserDirectory(updatedPost, true);
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
        replyField.getElement().getStyle()
                .set("background-color", "#1a1a1b")
                .set("color", "#d7dadc");

        simulatePlaceholder(replyField, "Write your reply...");

        replyField.setWidthFull();
        replyField.setMaxLength(500);
        replyField.setHeight("80px");

        Button sendButton = new Button("Send");
        sendButton.getStyle()
                .set("background-color", "#E0E0E0")
                .set("color", "#333333")
                .set("border", "none")
                .set("border-radius", "4px")
                .set("font-weight", "bold")
                .set("box-shadow", "none");
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




    // Erstellt eine Antwort (Reply) auf einen bestehenden Post.
    public static void createAndSaveReply(Post parentPost, String replyContent) {
        if (replyContent == null || replyContent.trim().isEmpty()) {
            return; // Don't create a post if the content is empty
        }

        // Get current user
        User currentUser = User.getCurrentUser();
        if (currentUser == null) {
            System.err.println("No logged-in user found.");
            return;
        }
        String currentUserName = currentUser.getUsername();

        // Get current time in seconds
        long currentTimeInSeconds = System.currentTimeMillis() / 1000;
        String formattedTimestamp = Post.formatTimestamp(String.valueOf(currentTimeInSeconds));

        // Find the maximum postId in the user's forum folder to generate a new unique ID
        String forumName = getUserForumName(currentUserName);
        if (forumName == null) {
            System.err.println("Could not find forum name for user " + currentUserName);
            return;
        }
        Path forumPostsDir = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/Forum", forumName);

        int nextId = getNextPostId(forumPostsDir);

        // Create a new reply post with a unique ID
        Post newReply = new Post(
                String.valueOf(nextId),          // postId
                parentPost.getPostId(),          // parentId
                0,                              // likes
                parentPost.getUserName(),        // parentUser
                replyContent,                   // postContent
                formattedTimestamp,             // timestamp
                currentUserName,                // userName
                ""                             // likedUsers
        );

        // Save the new reply post
        saveNewPost(newReply, true); // true means it is a reply, so no post count increment

        // --- ADD NOTIFICATION HERE ---
        // Notify the user who owns the parent post about the new reply, if different from current user
        if (!currentUserName.equals(parentPost.getUserName())) {
            UserPost.createNotificationForUser(parentPost.getUserName(), currentUserName);
        }
    }


    // Speichert einen neuen Post oder Reply als Datei.
    public static void saveNewPost(Post newPost, boolean isReply) {
        // Get forum folder for user
        User currentUser = User.getCurrentUser();
        if (currentUser == null) {
            System.err.println("No logged-in user found.");
            return;
        }
        String username = currentUser.getUsername();

        String forumName = getUserForumName(username);
        if (forumName == null) {
            System.err.println("Could not find forum name for user " + username);
            return;
        }
        Path forumPostsDir = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/Forum", forumName);

        try {
            // Create forum folder if not exist
            if (!Files.exists(forumPostsDir)) {
                Files.createDirectories(forumPostsDir);
            }
            // Save to forum directory
            Files.write(forumPostsDir.resolve(newPost.getPostId()), newPost.toString().getBytes());

            // Save to user-specific folder
            savePostToUserDirectory(newPost, false); // false because it's a new post, not overwrite

            // Only update post count if it's a top-level post (not reply)
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

    // Erhöht den Post-Zähler eines Benutzers, wenn dieser einen neuen Post (kein Reply) erstellt.
    private static void updateUserPosts(String username) {
        File userDirectory = new File("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users");

        File[] userFiles = userDirectory.listFiles((dir, name) -> name.endsWith(".txt"));

        if (userFiles != null) {
            for (File userFile : userFiles) {
                if (userFile.getName().equals(username + ".txt")) {
                    try {
                        List<String> lines = Files.readAllLines(userFile.toPath());
                        if (!lines.isEmpty()) {
                            String[] userParts = lines.get(0).split("#");

                            if (userParts.length == 3) {
                                int currentPosts = Integer.parseInt(userParts[1]);
                                userParts[1] = String.valueOf(currentPosts + 1);

                                String updatedUserData = String.join("#", userParts);

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

    // Erstellt einen ganz neuen Post (kein Reply) und speichert ihn.
    public static Post createAndSaveNewPost(String postContent) {
        // Get current user
        User currentUser = User.getCurrentUser();
        if (currentUser == null) {
            System.err.println("No logged-in user found.");
            return null;
        }
        String currentUserName = currentUser.getUsername();

        // Get forum name and posts directory
        String forumName = getUserForumName(currentUserName);
        if (forumName == null) {
            System.err.println("Could not find forum name for user " + currentUserName);
            return null;
        }
        Path forumPostsDir = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/Forum", forumName);

        int nextId = getNextPostId(forumPostsDir);

        // Get current time in seconds
        long currentTimeInSeconds = System.currentTimeMillis() / 1000;
        String formattedTimestamp = Post.formatTimestamp(String.valueOf(currentTimeInSeconds));

        // Create the new post
        Post newPost = new Post(
                String.valueOf(nextId),   // postId
                "0",                     // parentId (top-level)
                0,                       // likes
                "",                      // parentUser
                postContent,             // postContent
                formattedTimestamp,      // timestamp
                currentUserName,         // userName
                ""                       // likedUsers
        );

        // Save to forum and user directories and update stats
        saveNewPost(newPost, false);

        return newPost;
    }

    // Speichert einen Post in den Benutzerordner (Benutzer-Posts).
    public static void savePostToUserDirectory(Post post, boolean overwrite) {
        String username = post.getUserName();
        if (username == null || username.isEmpty()) return;

        Path userPostsDir = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users", username, "Posts");

        try {
            Files.createDirectories(userPostsDir);

            // Check if a file already contains this postId
            Path existingFile = Files.list(userPostsDir)
                    .filter(p -> {
                        try {
                            return Files.readString(p).startsWith(post.getPostId() + "#");
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .findFirst()
                    .orElse(null);

            if (existingFile != null && overwrite) {
                // Overwrite the matching file
                Files.write(existingFile, post.toString().getBytes());
            } else if (existingFile == null) {
                // Create a new post file if it doesn't already exist
                long postNumber = Files.list(userPostsDir)
                        .filter(p -> !Files.isDirectory(p))
                        .count() + 1;

                Path newPostFile = userPostsDir.resolve(String.valueOf(postNumber));
                Files.write(newPostFile, post.toString().getBytes());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper: get user's forum name from their Forum file
    private static String getUserForumName(String username) {
        Path forumFilePath = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users", username, "Forum");
        try {
            List<String> lines = Files.readAllLines(forumFilePath);
            if (lines.isEmpty()) return null;
            return lines.get(0).trim();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Helper: get next post ID in a forum posts directory
    private static int getNextPostId(Path forumPostsDir) {
        int maxId = 0;
        if (Files.exists(forumPostsDir)) {
            try {
                maxId = Files.list(forumPostsDir)
                        .filter(Files::isRegularFile)
                        .mapToInt(path -> {
                            try {
                                return Integer.parseInt(path.getFileName().toString());
                            } catch (NumberFormatException e) {
                                return 0;
                            }
                        })
                        .max()
                        .orElse(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return maxId + 1;
    }


    private void simulatePlaceholder(TextArea area, String placeholderText) {
        // Track whether we’re showing the fake placeholder
        final boolean[] showingPlaceholder = {true};

        area.setValue(placeholderText);
        area.getStyle()
                .set("color", "#6c7a89")
                .set("font-style", "normal")
                .set("font-size", "0.875rem")
                .set("line-height", "1.2");

        area.addFocusListener(e -> {
            if (showingPlaceholder[0]) {
                // Clear placeholder text and prepare for typing
                area.clear();
                showingPlaceholder[0] = false;
                area.getStyle()
                        .set("color", "#d7dadc") // normal input color
                        .set("font-style", "normal")
                        .set("font-size", "1rem")
                        .set("line-height", "normal");
            }
        });

        area.addValueChangeListener(event -> {
            String value = event.getValue();

            if (!showingPlaceholder[0] && value.trim().isEmpty()) {
                // User deleted all text manually → show placeholder again
                area.setValue(placeholderText);
                showingPlaceholder[0] = true;
                area.getStyle()
                        .set("color", "#6c7a89")
                        .set("font-style", "normal")
                        .set("font-size", "0.875rem")
                        .set("line-height", "1.2");
            } else if (showingPlaceholder[0] && !value.equals(placeholderText)) {
                // User started typing while placeholder was still visible
                showingPlaceholder[0] = false;
                area.getStyle()
                        .set("color", "#d7dadc")
                        .set("font-style", "normal")
                        .set("font-size", "1rem")
                        .set("line-height", "normal");
            }
        });

        area.addBlurListener(e -> {
            if (area.getValue().trim().isEmpty()) {
                area.setValue(placeholderText);
                showingPlaceholder[0] = true;
                area.getStyle()
                        .set("color", "#6c7a89")
                        .set("font-style", "normal")
                        .set("font-size", "0.875rem")
                        .set("line-height", "1.2");
            }
        });
    }


    public void applySimulatedPlaceholder(TextArea area, String placeholderText, String inputColor) {
        simulatePlaceholder(area, placeholderText);
    }

    public void updateNotificationNumber() {
        try {
            String username = User.getCurrentUser().getUsername();
            Path notifDir = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users", username, "notifications");

            long count = Files.exists(notifDir)
                    ? Files.list(notifDir).filter(p -> p.getFileName().toString().endsWith(".txt")).count()
                    : 0;

            Path notifCountFile = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users", username, "NotificationNumber");
            Files.writeString(notifCountFile, String.valueOf(count), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> filenames = new ArrayList<>();

    public static List<String> getNotificationPreviews(String username) {
        List<String> previews = new ArrayList<>();
        filenames.clear();

        Path notifDir = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users", username, "Notifications");

        if (!Files.exists(notifDir)) {
            return previews;
        }

        try (Stream<Path> paths = Files.list(notifDir)) {
            List<Path> notifFiles = paths
                    .filter(p -> p.getFileName().toString().endsWith(".txt"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString().replace(".txt", "")))
                    .toList();

            for (Path notifFile : notifFiles) {
                filenames.add(notifFile.getFileName().toString()); // Store for deletion

                String preview = "No content";
                try {
                    List<String> lines = Files.readAllLines(notifFile);
                    if (!lines.isEmpty()) {
                        String fullPostLine = lines.get(0).trim();
                        String[] parts = fullPostLine.split("#");
                        String content = parts.length > 4 ? parts[4] : "No content";

                        String[] words = content.split("\\s+");
                        preview = String.join(" ", Arrays.copyOfRange(words, 0, Math.min(40, words.length)));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                previews.add(preview);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return previews;
    }


    /**
     * Deletes notification file by filename for the current user.
     */
    public void deleteNotificationByFilename(String filename) {
        try {
            String username = User.getCurrentUser().getUsername();
            Path notifDir = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users", username, "notifications");

            Path notifFile = notifDir.resolve(filename);
            if (Files.exists(notifFile)) {
                Files.delete(notifFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static List<String> getNotificationFilenames() {
        return filenames;
    }
    /**
     * Renumbers all notification files sequentially (1.txt, 2.txt, ...).
     */
    public void renumberNotifications() {
        try {
            String username = User.getCurrentUser().getUsername();
            Path notifDir = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users", username, "notifications");

            List<Path> files = Files.list(notifDir)
                    .filter(p -> p.getFileName().toString().endsWith(".txt"))
                    .sorted()
                    .toList();

            int index = 1;
            for (Path file : files) {
                Path newPath = notifDir.resolve(index + ".txt");
                Files.move(file, newPath, StandardCopyOption.REPLACE_EXISTING);
                index++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a notification for recipientUsername by writing a new text file
     * containing the path to the sender’s last post.
     *
     /**
     * Creates a notification for recipientUsername by reading the full post line from senderUsername's post with given postId,
     * then writing a notification file containing that full post line.
     *
     * @param recipientUsername the user receiving the notification
     * @param senderUsername the user who wrote the post
     */
    //MADE WITH CHATGPT. DONT DELETE UNTIL I UNDERSTAND IT
    public static void createNotificationForUser(String recipientUsername, String senderUsername) {
        try {
            System.out.println("=== DEBUG: Starting createNotificationForUser ===");
            System.out.println("Recipient: " + recipientUsername);
            System.out.println("Sender: " + senderUsername);

            Path senderPostsDir = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users",
                    senderUsername, "Posts");

            if (!Files.exists(senderPostsDir)) {
                System.err.println("Sender posts directory does not exist: " + senderPostsDir);
                return;
            }

            // Find latest post file from sender
            Optional<Path> lastPostFile = Files.list(senderPostsDir)
                    .filter(p -> p.getFileName().toString().matches("\\d+"))
                    .sorted(Comparator.comparingInt(p -> Integer.parseInt(p.getFileName().toString())))
                    .reduce((first, second) -> second); // get last file

            if (lastPostFile.isEmpty()) {
                System.err.println("No posts found for sender: " + senderUsername);
                return;
            }

            String fullPostLine = Files.readString(lastPostFile.get()).trim();
            System.out.println("Found sender's last post: " + fullPostLine);

            // Get sender's post number (from file name)
            int senderPostNumber = Integer.parseInt(lastPostFile.get().getFileName().toString());
            System.out.println("Sender's latest post number: " + senderPostNumber);

            // Write notification
            Path recipientNotifDir = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users",
                    recipientUsername, "Notifications");
            if (!Files.exists(recipientNotifDir)) {
                Files.createDirectories(recipientNotifDir);
            }

            int notifFileNumber = (int) Files.list(recipientNotifDir)
                    .filter(p -> p.getFileName().toString().endsWith(".txt"))
                    .count() + 1;

            Path notifFile = recipientNotifDir.resolve(notifFileNumber + ".txt");
            List<String> lines = List.of(fullPostLine, String.valueOf(senderPostNumber));
            Files.write(notifFile, lines);

            // Update NotificationNumber
            Path notifCountFile = recipientNotifDir.resolveSibling("NotificationNumber");
            Files.writeString(notifCountFile, String.valueOf(notifFileNumber),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("Notification file created: " + notifFile);
            System.out.println("=== DEBUG: End createNotificationForUser ===");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void buildNotificationMenu(ContextMenu notificationMenu, Media mediaInstance) {
        String username = mediaInstance.getLoggedInUsername();
        List<String> previews = getNotificationPreviews(username);
        List<String> filenames = getNotificationFilenames();

        for (int i = 0; i < previews.size(); i++) {
            String preview = previews.get(i);
            String filename = filenames.get(i);

            notificationMenu.addItem(preview, click -> {
                Path notifPath = Paths.get("users", username, "Notifications", filename);

                mediaInstance.loadPostFromNotification(notifPath);

                deleteNotificationByFilename(filename);
                renumberNotifications();
                updateNotificationNumber();

                UI.getCurrent().navigate("media");
            });
        }
    }
    public void showNotificationCount(Span notificationCountSpan) {
        try {
            String username = User.getCurrentUser().getUsername();
            Path notifCountFile = Paths.get(
                    "C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users",
                    username,
                    "NotificationNumber"
            );

            String count = "0";
            if (Files.exists(notifCountFile)) {
                count = Files.readString(notifCountFile);
            }

            notificationCountSpan.setText(count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
