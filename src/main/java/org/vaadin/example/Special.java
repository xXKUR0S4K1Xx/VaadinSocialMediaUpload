package org.vaadin.example;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;

@Route("special")
public class Special extends VerticalLayout {

    // Base forum directory path
    private static final String FORUM_PATH = "C:\\Users\\sdachs\\IdeaProjects\\VaadinSocialMediaUpload\\Forum";

    public Special() {
        // Button 1: Add Admin and Moderator folders in each Forum subfolder
        Button forumButton = new Button("Add Admin and Moderator", e -> {
            File forumDir = new File(FORUM_PATH);

            if (forumDir.exists() && forumDir.isDirectory()) {
                File[] subfolders = forumDir.listFiles(File::isDirectory);

                if (subfolders != null) {
                    for (File subfolder : subfolders) {
                        File adminFolder = new File(subfolder, "Admin");
                        File modFolder = new File(subfolder, "Moderator");

                        if (!adminFolder.exists()) {
                            boolean created = adminFolder.mkdir();
                            System.out.println("Admin folder " + (created ? "created" : "failed") + " in " + subfolder.getName());
                        }

                        if (!modFolder.exists()) {
                            boolean created = modFolder.mkdir();
                            System.out.println("Moderator folder " + (created ? "created" : "failed") + " in " + subfolder.getName());
                        }
                    }
                }
            } else {
                System.out.println("Forum path does not exist or is not a directory.");
            }
        });

        // Button 2: Add Administrator and Moderator folders under each user folder
        Button usersButton = new Button("Add Admin & Moderator Folders to Users", e -> {
            Path usersBasePath = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users");

            if (!Files.exists(usersBasePath) || !Files.isDirectory(usersBasePath)) {
                System.out.println("Users folder not found!");
                return;
            }

            try (Stream<Path> userFolders = Files.list(usersBasePath)) {
                userFolders
                        .filter(Files::isDirectory)
                        .forEach(userPath -> {
                            try {
                                Path adminDir = userPath.resolve("Administrator");
                                Path modDir = userPath.resolve("Moderator");

                                if (Files.notExists(adminDir)) {
                                    Files.createDirectory(adminDir);
                                    System.out.println("Created Administrator folder in " + userPath.getFileName());
                                }

                                if (Files.notExists(modDir)) {
                                    Files.createDirectory(modDir);
                                    System.out.println("Created Moderator folder in " + userPath.getFileName());
                                }

                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        });

            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("Failed to scan users directory.");
            }
        });

        // Button 3: Add a file named "Descriptors" to every Forum/forumName folder
        // Button 3: Add a folder named "Descriptors" to every Forum/forumName folder
        Button descriptorsButton = new Button("Add 'Descriptors' Folder to Forums", e -> {
            File forumDir = new File(FORUM_PATH);

            if (forumDir.exists() && forumDir.isDirectory()) {
                File[] subfolders = forumDir.listFiles(File::isDirectory);

                if (subfolders != null) {
                    for (File subfolder : subfolders) {
                        Path descriptorFolder = subfolder.toPath().resolve("Descriptors");
                        if (!Files.exists(descriptorFolder)) {
                            try {
                                Files.createDirectories(descriptorFolder);
                                System.out.println("Created Descriptors folder in " + subfolder.getName());
                            } catch (IOException ex) {
                                System.err.println("Failed to create Descriptors folder in " + subfolder.getName());
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            } else {
                System.out.println("Forum path does not exist or is not a directory.");
            }
        });
// Button 4: Delete all files named "Descriptors" in every Forum/forumName folder
        Button deleteDescriptorsFilesButton = new Button("Delete 'Descriptors' Files from Forums", e -> {
            File forumDir = new File(FORUM_PATH);

            if (forumDir.exists() && forumDir.isDirectory()) {
                File[] subfolders = forumDir.listFiles(File::isDirectory);

                if (subfolders != null) {
                    for (File subfolder : subfolders) {
                        File descriptorFile = new File(subfolder, "Descriptors");
                        if (descriptorFile.exists() && descriptorFile.isFile()) {
                            boolean deleted = descriptorFile.delete();
                            System.out.println("Deleted Descriptors file in " + subfolder.getName() + ": " + deleted);
                        }
                    }
                }
            } else {
                System.out.println("Forum path does not exist or is not a directory.");
            }
        });
// Button 5: Add a file named "Summary" inside each Forum/forumName/Descriptors folder
        Button addSummaryFileButton = new Button("Add 'Summary' File in Descriptors Folders", e -> {
            File forumDir = new File(FORUM_PATH);

            if (forumDir.exists() && forumDir.isDirectory()) {
                File[] subfolders = forumDir.listFiles(File::isDirectory);

                if (subfolders != null) {
                    for (File subfolder : subfolders) {
                        Path descriptorsFolder = subfolder.toPath().resolve("Descriptors");
                        if (Files.exists(descriptorsFolder) && Files.isDirectory(descriptorsFolder)) {
                            Path summaryFile = descriptorsFolder.resolve("Summary");
                            if (!Files.exists(summaryFile)) {
                                try {
                                    Files.createFile(summaryFile);
                                    System.out.println("Created Summary file in " + descriptorsFolder);
                                } catch (IOException ex) {
                                    System.err.println("Failed to create Summary file in " + descriptorsFolder);
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }
                }
            } else {
                System.out.println("Forum path does not exist or is not a directory.");
            }
        });


        // Add all three buttons to the UI
        add(forumButton, usersButton, descriptorsButton, deleteDescriptorsFilesButton, addSummaryFileButton);
    }
}
