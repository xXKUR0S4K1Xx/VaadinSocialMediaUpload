package org.vaadin.example.social;

import com.vaadin.flow.server.VaadinSession;

public class UserUtils {

    // Get the currently logged-in user from the Vaadin session
    public static UserEntity getCurrentUser() {
        if (VaadinSession.getCurrent() == null) {
            return null;
        }
        return VaadinSession.getCurrent().getAttribute(UserEntity.class);
    }
    public static void setClickedUser(String username) {
        if (VaadinSession.getCurrent() != null) {
            VaadinSession.getCurrent().setAttribute("clickedUser", username);
        }
    }

    public static String getClickedUser() {
        if (VaadinSession.getCurrent() != null) {
            return (String) VaadinSession.getCurrent().getAttribute("clickedUser");
        }
        return null;
    }
}
