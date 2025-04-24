package org.vaadin.example.social;

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

    public static List<Post> readPostsFromFiles() {
        List<Post> posts = new ArrayList<>();

        try {
            Files.list(Paths.get(postsDirectory))
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            List<String> lines = Files.readAllLines(file);
                            for (String line : lines) {
                                String[] parts = line.split("#");

                                if (parts.length == 8) {
                                    try {
                                        Post post = new Post(
                                                parts[0], // postId
                                                parts[1], // parentId
                                                Integer.parseInt(parts[2]), // likes
                                                parts[3], // parentUser
                                                parts[4], // postContent
                                                parts[5], // timestamp
                                                parts[6], // userName
                                                parts[7]  // likedUsers
                                        );
                                        posts.add(post);
                                    } catch (NumberFormatException e) {
                                        System.out.println("Invalid number in post: " + line);
                                    }
                                } else {
                                    System.out.println("Malformed line: " + line);
                                }
                            }
                        } catch (IOException e) {
                            System.out.println("Error reading file: " + file);
                            e.printStackTrace();
                        }
                    });

            // Sort posts numerically by postId
            posts.sort((p1, p2) -> {
                try {
                    return Integer.compare(
                            Integer.parseInt(p1.getPostId()),
                            Integer.parseInt(p2.getPostId())
                    );
                } catch (NumberFormatException e) {
                    return 0;
                }
            });

        } catch (IOException e) {
            System.out.println("Error accessing directory: " + postsDirectory);
            e.printStackTrace();
        }

        return posts;
    }

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

    public static Post findPostById(String parentId) {
        // Get all posts
        List<Post> allPosts = readPostsFromFiles();
        for (Post post : allPosts) {
            if (post.getPostId().equals(parentId)) {
                return post;
            }
        }
        return null;  // Return null if no parent post is found
    }

    public static void saveNewPost(String postContent) {
        File folder = new File(postsDirectory);
        File[] files = folder.listFiles((dir, name) -> !name.contains("."));
        int nextId = files != null && files.length > 0 ? Arrays.stream(files)
                .mapToInt(file -> Integer.parseInt(file.getName()))
                .max()
                .orElse(0) + 1 : 1;

        // Get current user from file
        String currentUser = "unknown";
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
                String.valueOf(nextId),  // postId
                "0",                     // parentId (not a reply)
                0,                       // likes
                currentUser,             // parentUser (not really used unless it's a reply)
                postContent,             // content
                formattedTimestamp,      // formatted timestamp
                currentUser,             // author
                ""                       // likedUsers
        );

        // Save post to file
        try {
            Files.write(Paths.get(postsDirectory, String.valueOf(nextId)), newPost.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
