package org.vaadin.example;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

@Route("programmier-aufgaben")
public class ProgrammierAufgaben extends VerticalLayout {

    private int currentNumber = 1;
    private boolean isCounting = false;
    private Timer timer;
    private int[] randomArray;
    private Button generateSortButton;
    private Div arrayDiv;

    public ProgrammierAufgaben() {
        // Counting UI
        TextField numberInput = new TextField("Enter a number");
        numberInput.setPlaceholder("Type a number");

        Div numberDiv = new Div();
        numberDiv.setText("Current number: 0");

        Button loopButton = new Button("Start Counting", e -> {
            try {
                int targetNumber = Integer.parseInt(numberInput.getValue());
                if (targetNumber <= 0) {
                    Notification.show("Please enter a number greater than 0");
                    return;
                }
                if (!isCounting) {
                    startCounting(targetNumber, numberDiv);
                } else {
                    Notification.show("Counting is already in progress");
                }
            } catch (NumberFormatException ex) {
                Notification.show("Please enter a valid number");
            }
        });

        VerticalLayout countingLayout = new VerticalLayout(numberInput, loopButton, numberDiv);
        countingLayout.setWidth("0");
        countingLayout.setAlignItems(Alignment.START);
        countingLayout.setSpacing(false);
        countingLayout.setMargin(false);
        countingLayout.setPadding(false);
        countingLayout.setHeight("100%");

        // Array UI
        arrayDiv = new Div();
        arrayDiv.setText("Array: None");

        generateSortButton = new Button("Generate Array", e -> generateRandomArray());

        VerticalLayout arrayLayout = new VerticalLayout(generateSortButton, arrayDiv);
        arrayLayout.setAlignItems(Alignment.CENTER);
        arrayLayout.setSpacing(false);
        arrayLayout.setMargin(false);

        // Search UI
        TextField searchField = new TextField();
        searchField.setPlaceholder("Enter a word");
        searchField.setVisible(false);

        Button searchButton = new Button("Search", e -> searchField.setVisible(true));

        VerticalLayout searchLayout = new VerticalLayout(searchButton, searchField);
        searchLayout.setWidth("0");
        searchLayout.setAlignItems(Alignment.END);
        searchLayout.setSpacing(false);
        searchLayout.setMargin(false);
        searchLayout.setPadding(false);

        // Letter Search UI (Aligned to END)
        TextField wordInput = new TextField("Enter a word");
        wordInput.setPlaceholder("Type a word");

        TextField letterInput = new TextField("Enter letters");
        letterInput.setPlaceholder("Type letters to search");

        Div resultDiv = new Div();
        resultDiv.setText("Result: ");

        Button findLettersButton = new Button("Find Letters", e -> {
            String word = wordInput.getValue();
            String letters = letterInput.getValue();

            if (word.isEmpty() || letters.isEmpty()) {
                Notification.show("Please enter both a word and letters.");
                return;
            }

            StringBuilder result = new StringBuilder("Positions: ");
            for (char letter : letters.toCharArray()) {
                int index = word.indexOf(letter);
                while (index != -1) {
                    result.append(letter).append(" at ").append(index).append(", ");
                    index = word.indexOf(letter, index + 1);
                }
            }

            resultDiv.setText(result.length() > 10 ? result.toString() : "No matches found.");
        });

        VerticalLayout searchLetterLayout = new VerticalLayout(wordInput, letterInput, findLettersButton, resultDiv);
        searchLetterLayout.setAlignItems(Alignment.END); // Align to the right
        searchLetterLayout.setSpacing(false);
        searchLetterLayout.setMargin(false);
        searchLetterLayout.setPadding(false);

        // Main Layout
        HorizontalLayout mainLayout = new HorizontalLayout(countingLayout, arrayLayout, searchLayout);
        mainLayout.setWidth("100%");
        mainLayout.setAlignItems(Alignment.START);
        mainLayout.setSpacing(true);

        add(mainLayout, searchLetterLayout);
    }

    private void startCounting(int targetNumber, Div numberDiv) {
        isCounting = true;
        currentNumber = 1;

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getUI().ifPresent(ui -> ui.access(() -> {
                    if (currentNumber <= targetNumber) {
                        numberDiv.setText("Current number: " + currentNumber);
                    } else {
                        timer.cancel();
                        isCounting = false;
                    }
                }));

                currentNumber++;
            }
        }, 0, 1000);
    }

    private void generateRandomArray() {
        randomArray = new int[5];
        Random rand = new Random();
        for (int i = 0; i < randomArray.length; i++) {
            randomArray[i] = rand.nextInt(100);
        }

        updateArrayDisplay();
        generateSortButton.setText("Sort");
        generateSortButton.addClickListener(e -> sortArray());
    }

    private void updateArrayDisplay() {
        StringBuilder arrayText = new StringBuilder("Array: ");
        for (int num : randomArray) {
            arrayText.append(num).append(" ");
        }
        arrayDiv.setText(arrayText.toString());
    }

    private void sortArray() {
        for (int i = 0; i < randomArray.length - 1; i++) {
            for (int j = 0; j < randomArray.length - 1 - i; j++) {
                if (randomArray[j] > randomArray[j + 1]) {
                    int temp = randomArray[j];
                    randomArray[j] = randomArray[j + 1];
                    randomArray[j + 1] = temp;
                }
            }
        }

        updateArrayDisplay();
    }
}
