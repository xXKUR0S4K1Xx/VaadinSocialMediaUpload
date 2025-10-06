package org.vaadin.example.social;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@Route("loginDB")
public class LoginDB extends VerticalLayout {

    private final UserService userService;

    public LoginDB(UserService userService) {
        this.userService = userService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);

        H1 title = new H1("Login / Register");

        TextField usernameField = new TextField("Username");
        PasswordField passwordField = new PasswordField("Password");

        Button loginButton = new Button("Login", e -> login(usernameField.getValue(), passwordField.getValue()));
        Button registerButton = new Button("Register", e -> register(usernameField.getValue(), passwordField.getValue()));

        add(title, usernameField, passwordField, loginButton, registerButton);
    }

    private void login(String username, String password) {
        userService.findByUsername(username).ifPresentOrElse(user -> {
            if (user.getPassword().equals(password)) {
                VaadinSession.getCurrent().setAttribute(UserEntity.class, user);
                Notification.show("Login successful!");
                UI.getCurrent().navigate("mediadb");
            } else {
                Notification.show("Wrong password");
            }
        }, () -> Notification.show("User not found"));
    }

    private void register(String username, String password) {
        if (userService.findByUsername(username).isPresent()) {
            Notification.show("Username already exists");
            return;
        }

        UserEntity newUser = new UserEntity();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setAvatarUrl(""); // can be empty initially

        userService.save(newUser);
        Notification.show("Registration successful!");
    }
}
