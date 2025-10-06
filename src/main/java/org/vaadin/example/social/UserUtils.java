package org.vaadin.example.social;

import java.util.Optional;

public class UserUtils {

    // Example: find the currently logged in user (you can integrate with Vaadin session later)
    public static UserEntity getCurrentUser(UserService userService) {
        String username = "MyLoggedInUser"; // placeholder; replace with real session logic
        Optional<UserEntity> user = userService.findByUsername(username);
        return user.orElse(null);
    }
}