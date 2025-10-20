package org.vaadin.example;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Route;

@Route
public class MainView extends VerticalLayout {

    public MainView() {
        // Navigation buttons
        Button hangmanButton = new Button("Go to Hangman", e -> UI.getCurrent().navigate("hangman"));
        Button notebookButton = new Button("Go to Notebook", e -> UI.getCurrent().navigate("notebook"));
        Button loginButton = new Button("Go to Login", e -> UI.getCurrent().navigate("login"));
        Button mediaButton = new Button("Go to Media", e -> UI.getCurrent().navigate("media"));
        Button specialButton = new Button("Go to Special", e -> UI.getCurrent().navigate("special"));
        Button statisticsButton = new Button("Go to Statistics", e -> UI.getCurrent().navigate("statistics"));
        Button coinGameButton = new Button("Go to CoinGame", e -> UI.getCurrent().navigate("coin-game"));
        Button programmierAufgabenButton = new Button("Go to ProgrammierAufgaben", e -> UI.getCurrent().navigate("programmier-aufgaben"));
        Button mediaDB = new Button("Go to MediaDB", e -> UI.getCurrent().navigate("mediadb"));
        Button loginDB = new Button("Go to LoginDB", e -> UI.getCurrent().navigate("loginDB"));


        // Header layout for navigation buttons
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        headerLayout.add(specialButton, programmierAufgabenButton, coinGameButton, statisticsButton, hangmanButton, notebookButton, loginButton, mediaButton,mediaDB, loginDB);

        // Add the header layout to the main layout
        add(headerLayout);
    }
}
