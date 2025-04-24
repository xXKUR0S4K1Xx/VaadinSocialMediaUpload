package org.vaadin.example;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Route("coin-game")
public class CoinGame extends VerticalLayout {

    String playerName;
    String playerConfirm;
    private int choice;
    private Button throwCoin;
    int coin;
    int totalGames;
    int wins;
    int losses;

    public CoinGame() {
        TextField playerNameField = new TextField("Enter your name");

        Button submitButton = new Button("Submit", event -> {
            playerName = playerNameField.getValue(); // Get the entered name
            playerConfirm = playerName;
            Notification.show("Hello, " + playerName + "!");
            readPlayerStats("player_stats.txt", playerName);
        });

        // Back button in the top-right corner
        Button backButton = new Button("Back", event -> UI.getCurrent().navigate(MainView.class));
        HorizontalLayout topRightLayout = new HorizontalLayout(backButton);
        topRightLayout.setWidthFull();
        topRightLayout.setJustifyContentMode(JustifyContentMode.END);

        Div totalGamesDiv = new Div();
        totalGamesDiv.setText(" Total Games: " + totalGames);

        Div winsDiv = new Div();
        winsDiv.setText(" Wins: " + wins);

        Div lossesDiv = new Div();
        lossesDiv.setText(" Losses: " + losses);

        // "Choose heads or tails" text
        Div headstailsDiv = new Div();
        headstailsDiv.setText("Choose heads or tails");

        // Heads and Tails buttons
        Button headsButton = new Button("Heads", event -> {
            choice = 0;
            Notification.show("You chose Heads!");
        });

        Button tailsButton = new Button("Tails", event -> {
            choice = 1;
            Notification.show("You chose Tails!");
        });

        Button throwButton = new Button("Toss", event -> {
            coinToss();
            if (coin == choice) {
                Notification.show("You Won!");
                totalGames++;
                wins++;
                winsDiv.setText(playerName + "'s Wins: " + wins);
                totalGamesDiv.setText(playerName + "'s Total Games: " + totalGames);
            } else {
                Notification.show("You Lost!");
                totalGames++;
                losses++;
                lossesDiv.setText(playerName + "'s Losses: " + losses);
                totalGamesDiv.setText(playerName + "'s Total Games: " + totalGames);
            }
            updatePlayerStats(playerName, totalGames, wins, losses);
        });

        Button resetButton = new Button("Reset Name", event -> {
            playerName = null;
            totalGames = 0;
            wins = 0;
            losses = 0;
            totalGamesDiv.setText("Total Games: " + totalGames);
            winsDiv.setText("Wins: " + wins);
            lossesDiv.setText("Losses: " + losses);
        });

        VerticalLayout playerLayout = new VerticalLayout(playerNameField, submitButton, resetButton);
        playerLayout.setAlignItems(Alignment.CENTER); // Pushes buttonslayout items to the left

        // Layout for buttons
        HorizontalLayout mainLayout = new HorizontalLayout(headsButton, tailsButton);

        // Vertical layout containing text and buttons
        VerticalLayout buttonsLayout = new VerticalLayout(headstailsDiv, mainLayout, throwButton);
        buttonsLayout.setAlignItems(Alignment.START); // Pushes buttonslayout items to the left

        VerticalLayout statisticsLayout = new VerticalLayout(playerLayout, totalGamesDiv, winsDiv, lossesDiv);
        statisticsLayout.setAlignItems(Alignment.CENTER); // Pushes buttonslayout items to the left

        // Top layout containing both back button and game buttons
        HorizontalLayout topLayout = new HorizontalLayout(buttonsLayout, statisticsLayout, topRightLayout);
        topLayout.setWidthFull();
        topLayout.setAlignItems(Alignment.START); // Keep everything at the top
        topLayout.setJustifyContentMode(JustifyContentMode.END); // Pushes back button right

        // Add everything to the view
        add(topLayout);
    }

    private void coinToss() {
        Random rand = new Random();
        coin = rand.nextInt(2);
    }

    // Read the player stats from the file
    public void readPlayerStats(String fileName, String playerName) {
        boolean playerFound = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) { // Read each line
                if (line.startsWith(playerName + ":")) { // Check if the line starts with the player's name
                    String[] stats = line.split(":");
                    String[] gameStats = stats[1].split(",");
                    totalGames = Integer.parseInt(gameStats[0].split("=")[1].trim());
                    wins = Integer.parseInt(gameStats[1].split("=")[1].trim());
                    losses = Integer.parseInt(gameStats[2].split("=")[1].trim());
                    playerFound = true;
                    break; // Stop searching after finding the player
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!playerFound) {
            newPlayerStats(playerName, 0, 0, 0);
        }
    }

    // Update the player stats (this should be a non-static method)

    public void updatePlayerStats(String playerName, int totalGames, int wins, int losses) {
        File file = new File("player_stats.txt");
        List<String> lines = new ArrayList<>();
        boolean playerFound = false;

        // Read the file and check if player exists
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(playerName + ":")) {
                    // Update player's stats
                    line = playerName + ": Total Games=" + totalGames + ", Wins=" + wins + ", Losses=" + losses;
                    playerFound = true;
                }
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // If player was not found, add new stats at the end
        if (!playerFound) {
            lines.add(playerName + ": Total Games=" + totalGames + ", Wins=" + wins + ", Losses=" + losses);
        }

        // Overwrite the file with updated lines
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, false))) {
            for (String line : lines) {
                writer.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Create new player stats if the player doesn't exist
    public static void newPlayerStats(String playerName, int totalGames, int wins, int losses) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("player_stats.txt", true))) { // "true" enables appending
            writer.println(playerName + ": Total Games=" + totalGames + ", Wins=" + wins + ", Losses=" + losses);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
