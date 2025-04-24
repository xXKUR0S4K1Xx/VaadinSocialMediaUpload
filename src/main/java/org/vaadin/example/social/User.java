package org.vaadin.example.social;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class User {

    private String username;
    private String password;
    private int postCount;
    private int likeCount;
    private List<String> postNames;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.postCount = 0;
        this.likeCount = 0;
        this.postNames = new ArrayList<>();
    }

    // --- Getters and Setters ---

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    // Consider storing hashed passwords only
    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getPostNames() {
        return postNames;
    }

    public void addPostName(String postName) {
        this.postNames.add(postName);
        this.postCount++;
    }

    public int getPostCount() {
        return postCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void addLike() {
        this.likeCount++;
    }

    public static User getCurrentUser() {
        try {
            String username = Files.readString(Paths.get("loggedinuser.txt")).trim();
            return User.loadFromFile(username);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static User loadFromFile(String username) {
        String userDirectoryPath = "C:/Users/sdachs/IdeaProjects/vaadin-programmieraufgaben/users";
        String filePath = userDirectoryPath + "/" + username + ".txt";
        File userFile = new File(filePath);

        if (!userFile.exists()) {
            System.out.println("User file not found for: " + username);
            return null;
        }

        try {
            String content = java.nio.file.Files.readString(userFile.toPath()).trim();
            String[] parts = content.split("#");

            if (parts.length >= 3) {
                String password = parts[0];
                int postCount = Integer.parseInt(parts[1]);
                int likeCount = Integer.parseInt(parts[2]);
                List<String> postNames = new ArrayList<>();

                for (int i = 3; i < parts.length; i++) {
                    postNames.add(parts[i]);
                }

                User user = new User(username, password);
                user.postCount = postCount;
                user.likeCount = likeCount;
                user.postNames = postNames;

                return user;
            } else {
                System.out.println("Corrupted data in file for user: " + username);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Save the user data to a file in the correct directory (Users folder)
    public void saveToFile() {
        // Define the directory path for users
        String userDirectoryPath = "C:/Users/sdachs/IdeaProjects/vaadin-programmieraufgaben/users";

        // Create the directory if it doesn't exist
        File userDirectory = new File(userDirectoryPath);
        if (!userDirectory.exists()) {
            userDirectory.mkdirs();
        }

        // Create the file path for the user's data (username.txt)
        String filePath = userDirectoryPath + "/" + this.username + ".txt";
        File userFile = new File(filePath);

        try {
            if (!userFile.exists()) {
                userFile.createNewFile();
            }

            try (FileWriter writer = new FileWriter(userFile)) {
                // Write the user data to the file: password#postCount#likeCount#postNames
                writer.write(this.password + "#");
                writer.write(this.postCount + "#");
                writer.write(this.likeCount + "#");
                writer.write(String.join("#", this.postNames)); // Join post names with "#"
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
