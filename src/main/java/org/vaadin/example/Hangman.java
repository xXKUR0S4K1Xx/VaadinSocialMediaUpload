package org.vaadin.example;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("hangman")
public class Hangman extends VerticalLayout {

    private String wordToGuess;
    private char[] hiddenWord;
    private int remainingAttempts = 10;

    private Div hangman; // Using Div to allow multi-line formatting
    private Paragraph wordDisplay;
    private Paragraph attemptsLabel;
    private TextField guessInput;
    private Button guessButton;

    public Hangman() {
        // UI Components
        TextField wordInput = new TextField("Enter a word to guess:");
        Button startGameButton = new Button("Start Game", e -> {
            startGame(wordInput.getValue());
            wordInput.clear();
        });

        wordDisplay = new Paragraph("Hidden Word: ");
        attemptsLabel = new Paragraph("Attempts left: " + remainingAttempts);

        guessInput = new TextField("Enter a letter");
        guessInput.setMaxLength(1);

        guessButton = new Button("Guess", e -> processGuess(guessInput.getValue()));
        guessButton.setEnabled(false); // Disabled until the game starts

        Button backButton = new Button("Back to Main Menu", e -> UI.getCurrent().navigate(MainView.class));

        // Hangman Figure (Initial empty state)
        hangman = new Div();
        hangman.getElement().setProperty("innerHTML", "<pre>________</pre>");

        // Layout
        add(wordInput, startGameButton, wordDisplay, attemptsLabel, guessInput, guessButton, hangman, backButton);

        // Align items in the top-center of the view
        setAlignItems(Alignment.CENTER);
        setWidth("100%");
    }

    private void startGame(String word) {
        if (word.isEmpty()) {
            Notification.show("Please enter a word to start the game.");
            return;
        }

        wordToGuess = word.toLowerCase();
        hiddenWord = new char[wordToGuess.length() * 2 - 1];

        for (int i = 0; i < wordToGuess.length(); i++) {
            hiddenWord[i * 2] = '_';
            if (i < wordToGuess.length() - 1) {
                hiddenWord[i * 2 + 1] = ' ';
            }
        }

        wordDisplay.setText("Hidden Word: " + String.valueOf(hiddenWord));
        remainingAttempts = 10;
        attemptsLabel.setText("Attempts left: " + remainingAttempts);

        HangManFigure(); // Initialize the drawing
        guessButton.setEnabled(true);
    }

    private void HangManFigure() {
        String figure;
        switch (remainingAttempts) {
            case 10:
                figure = " \n \n \n \n \n \n________";
                break;
            case 9:
                figure = " |\n |\n |\n |\n |\n |\n________";
                break;
            case 8:
                figure = "  ____\n |    |\n |\n |\n |\n |\n________";
                break;
            case 7:
                figure = "  ____\n |    |\n |    O\n |\n |\n |\n________";
                break;
            case 6:
                figure = "  ____\n |    |\n |    O\n |    |\n |\n |\n________";
                break;
            case 5:
                figure = "  ____\n |    |\n |    O\n |   /|\n |\n |\n________";
                break;
            case 4:
                figure = "  ____\n |    |\n |    O\n |   /|\\\n |\n |\n________";
                break;
            case 3:
                figure = "  ____\n |    |\n |    O\n |   /|\\\n |   /\n |\n________";
                break;
            case 2:
                figure = "  ____\n |    |\n |    O\n |   /|\\\n |   / \\\n |\n________";
                break;
            case 1:
                figure = "  ____\n |    |\n |    O\n |   /|\\\n |   / \\\n |  Last Chance\n________";
                break;
            case 0:
                figure = "  ____\n |    |\n |    X\n |   /|\\\n |   / \\\n |  GAME OVER!\n________";
                break;
            default:
                figure = "________"; // Default empty gallows
        }

        // Update the Hangman figure
        hangman.getElement().setProperty("innerHTML", "<pre>" + figure + "</pre>");
    }

    private void processGuess(String input) {
        if (input.isEmpty()) {
            Notification.show("Please enter a letter.");
            return;
        }

        char guessedLetter = Character.toLowerCase(input.charAt(0));
        boolean found = false;

        // Check if the guessed letter is in the word
        for (int i = 0; i < wordToGuess.length(); i++) {
            if (wordToGuess.charAt(i) == guessedLetter) {
                hiddenWord[i * 2] = guessedLetter;  // Replace the underscore with the guessed letter
                found = true;
            }
        }

        // If the letter was not found, decrease attempts
        if (!found) {
            remainingAttempts--;
        }

        wordDisplay.setText("Hidden Word: " + String.valueOf(hiddenWord));
        attemptsLabel.setText("Attempts left: " + remainingAttempts);

        HangManFigure(); // Update the drawing

        // Check if the game is over
        if (remainingAttempts == 0) {
            Notification.show("Game Over! The word was: " + wordToGuess);
            guessButton.setEnabled(false);
        } else if (!new String(hiddenWord).contains("_")) {
            Notification.show("Congratulations! You guessed the word!");
            guessButton.setEnabled(false);
        }
    }
}
