package org.vaadin.example.social;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import com.vaadin.flow.router.Route;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

@Route("login")
public class Login extends VerticalLayout {

    public Login() {
        addClassName("login-rich-content");
        setSizeFull(); // Fill screen
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Tabs
        Tab loginTab = new Tab("Login");
        Tab registerTab = new Tab("Register");
        Tabs tabs = new Tabs(loginTab, registerTab);
        tabs.getElement().getThemeList().add("dark");

        tabs.setWidth("100%");
        tabs.setFlexGrowForEnclosedTabs(1);

        // Initial content
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setPadding(false);
        contentLayout.setSpacing(false);
        contentLayout.setAlignItems(Alignment.CENTER);
        contentLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        contentLayout.setWidthFull();

        // Add initial form (login)
        Component initialForm = createLoginForm();
        initialForm.getElement().getThemeList().add("dark");
        contentLayout.add(initialForm);

        // Wrapper layout
        VerticalLayout tabContentWrapper = new VerticalLayout();
        tabContentWrapper.setAlignItems(Alignment.CENTER);
        tabContentWrapper.setJustifyContentMode(JustifyContentMode.CENTER);
        tabContentWrapper.setPadding(false);
        tabContentWrapper.setSpacing(false);
        tabContentWrapper.setWidth("350px");
        tabContentWrapper.add(tabs, contentLayout);

        // Tab switching
        tabs.addSelectedChangeListener(event -> {
            contentLayout.removeAll();
            Component newForm;

            if (event.getSelectedTab() == loginTab) {
                newForm = createLoginForm();
            } else {
                newForm = createRegisterForm();
            }

            newForm.getElement().getThemeList().add("dark");
            contentLayout.add(newForm);
        });

        // Final add
        add(tabContentWrapper);
    }

    // Login form
    private String loggedInUsername;

    private Component createLoginForm() {
        TextField usernameField = new TextField("Username");
        PasswordField passwordField = new PasswordField("Password");

        Button loginButton = new Button("Login", event -> {
            String username = usernameField.getValue();
            String password = passwordField.getValue();

            File userFile = new File("Users/" + username + ".txt");

            if (userFile.exists()) {
                try {
                    Scanner scanner = new Scanner(userFile);
                    if (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        String[] parts = line.split("#");

                        String savedPassword = parts[0]; // First part is the saved password

                        if (savedPassword.equals(password)) {

                            // Store username without the '.txt' part
                            loggedInUsername = username;
                            try {
                                Files.write(Paths.get("loggedinuser.txt"), username.getBytes());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            // Navigate to media view
                            getUI().ifPresent(ui -> ui.navigate("media"));
                            return;
                        }
                    }
                    scanner.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // If file not found or password mismatch
            Notification notification = new Notification("Sorry, wrong User or Password!", 3000);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.open();
        });

        VerticalLayout loginLayout = new VerticalLayout(usernameField, passwordField, loginButton);
        loginLayout.setAlignItems(Alignment.CENTER);
        return loginLayout;
    }


    // Register form
    // Register form
    private Component createRegisterForm() {
        TextField usernameField = new TextField("Username");
        PasswordField passwordField = new PasswordField("Password");

        Button registerButton = new Button("Register", event -> {
            String username = usernameField.getValue();
            String password = passwordField.getValue();

            if (!username.isEmpty() && !password.isEmpty()) {
                User newUser = new User(username, password);
                newUser.saveToFile();

                try {
                    Path userDir = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users", username);

                    // Create main user directory if it doesn't exist
                    if (!Files.exists(userDir)) {
                        Files.createDirectories(userDir);
                    }

                    // Create Forum file with default content "all"
                    Path forumFile = userDir.resolve("Forum");
                    if (!Files.exists(forumFile)) {
                        Files.writeString(forumFile, "all", StandardCharsets.UTF_8);
                    }

                    // Create Followed Forums directory and default file
                    Path followedForumsDir = userDir.resolve("Followed Forums");
                    if (!Files.exists(followedForumsDir)) {
                        Files.createDirectories(followedForumsDir);
                    }
                    Path allFile = followedForumsDir.resolve("all");
                    if (!Files.exists(allFile)) {
                        Files.writeString(allFile, "all", StandardCharsets.UTF_8);
                    }

                    // Create Notifications directory
                    Path notificationsDir = userDir.resolve("Notifications");
                    if (!Files.exists(notificationsDir)) {
                        Files.createDirectories(notificationsDir);
                    }

                    // **Create Administrator and Moderator folders**
                    Path adminDir = userDir.resolve("Administrator");
                    if (!Files.exists(adminDir)) {
                        Files.createDirectories(adminDir);
                    }

                    Path moderatorDir = userDir.resolve("Moderator");
                    if (!Files.exists(moderatorDir)) {
                        Files.createDirectories(moderatorDir);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Notification error = new Notification("Error creating user folders/files!", 3000);
                    error.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    error.open();
                    return;
                }

                Notification success = new Notification("User successfully registered!", 3000);
                success.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                success.open();
            } else {
                Notification error = new Notification("Please fill in both fields!", 3000);
                error.addThemeVariants(NotificationVariant.LUMO_ERROR);
                error.open();
            }
        });




        VerticalLayout registerLayout = new VerticalLayout(usernameField, passwordField, registerButton);
        registerLayout.setAlignItems(Alignment.CENTER);
        return registerLayout;
    }
}
