package org.vaadin.example;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Route("statistics")
public class Statistics extends VerticalLayout {

    public Statistics() {
        // Set the main layout to center everything
        setAlignItems(Alignment.CENTER); // Center everything horizontally

        // Load player stats from file
        List<String> playerNames = loadPlayerStats();

        // Create a VerticalLayout for holding the player buttons and delete buttons
        VerticalLayout playerButtonsLayout = new VerticalLayout();
        playerButtonsLayout.setAlignItems(Alignment.CENTER);  // Center buttons horizontally within this layout

        // Create a horizontal layout for each player
        for (String playerName : playerNames) {
            // Create a HorizontalLayout to contain the player's name and delete button
            HorizontalLayout playerLayout = new HorizontalLayout();
            playerLayout.setAlignItems(Alignment.CENTER); // Center the player button and delete button horizontally

            // Create a button for the player name
            Button playerButton = new Button(playerName, event -> showPlayerStats(playerName));
            playerButton.setWidth("200px"); // Set fixed width for player button

            // Create a delete button for the player
            Button deleteButton = new Button("Delete", event -> deletePlayerStats(playerName));
            deleteButton.setWidth("100px"); // Set fixed width for delete button

            // Add both buttons to the player's HorizontalLayout
            playerLayout.add(playerButton, deleteButton);

            // Add the HorizontalLayout (with both buttons) to the VerticalLayout for player buttons
            playerButtonsLayout.add(playerLayout);
        }

        // Add the player buttons layout to the main view layout
        add(playerButtonsLayout);

        // Create the back button
        Button backButton = new Button("Back", event -> UI.getCurrent().navigate(MainView.class));

        // Center the back button horizontally within a HorizontalLayout
        HorizontalLayout backButtonLayout = new HorizontalLayout(backButton);
        backButtonLayout.setAlignItems(Alignment.CENTER);  // Center the back button horizontally
        backButtonLayout.setJustifyContentMode(JustifyContentMode.CENTER); // Align it to the top

        // Add the back button layout below the player buttons
        add(backButtonLayout);
    }

    private List<String> loadPlayerStats() {
        List<String> playerNames = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("player_stats.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Assuming the player's name is at the start of the line (before ":")
                String playerName = line.split(":")[0].trim();
                playerNames.add(playerName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return playerNames;
    }

    private void showPlayerStats(String playerName) {
        // Read the player stats
        String stats = getPlayerStats(playerName);
        // Display the stats (for simplicity, you could use a Notification or a Div here)
        UI.getCurrent().access(() -> {
            Notification.show(playerName + " Stats: " + stats);
        });
    }

    private String getPlayerStats(String playerName) {
        try (BufferedReader reader = new BufferedReader(new FileReader("player_stats.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(playerName + ":")) {
                    return line; // Return the full line containing the stats
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Stats not found!";
    }

    private void deletePlayerStats(String playerName) {
        File file = new File("player_stats.txt");

        try {
            // Read all lines from the file into a List
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }

            // Remove the player's stats from the list
            lines.removeIf(line -> line.startsWith(playerName + ":"));

            // Write the updated content back into the file
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, false))) {
                for (String line : lines) {
                    writer.println(line);
                }
            }

            UI.getCurrent().access(() -> Notification.show(playerName + "'s stats deleted."));
        } catch (IOException e) {
            e.printStackTrace();
            UI.getCurrent().access(() -> Notification.show("Error deleting stats for " + playerName));
        }
    }
}
