package org.vaadin.example;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.util.Random;
import java.util.Timer;

@Route("programmier-aufgaben")
public class ProgrammierAufgaben extends VerticalLayout {

    private int currentNumber = 1;
    private boolean isCounting = false;
    private Timer timer;
    private int[] randomArray;
    private int[] sortedArray;
    private Button generateSortButton;
    private Button generateArrayButton;
    private Div arrayDiv;
    private TextField sortedArrayField;
    private Button fibonacciButton;

    public ProgrammierAufgaben() {
        setSizeFull();  // Make the main layout fill the entire screen

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
        countingLayout.setAlignItems(Alignment.START);

        // Array UI
        arrayDiv = new Div();
        arrayDiv.setText("Array: None");

        generateArrayButton = new Button("Generate Array", e -> generateRandomArray());

        sortedArrayField = new TextField();
        sortedArrayField.setReadOnly(true);

        generateSortButton = new Button("Sort", e -> sortArray());

        VerticalLayout arrayLayout = new VerticalLayout(generateArrayButton, arrayDiv, generateSortButton, sortedArrayField);
        arrayLayout.setAlignItems(Alignment.CENTER);

        // Search UI
        TextField wordInput = new TextField("Enter a word");
        wordInput.setPlaceholder("Type a word");

        TextField letterInput = new TextField("Enter letters");
        letterInput.addClassName("placeholder-custom");
        letterInput.setPlaceholder("Type letters to search");

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

            Notification.show(result.length() > 10 ? result.toString() : "No matches found.");
        });

        VerticalLayout searchLayout = new VerticalLayout(wordInput, letterInput, findLettersButton);
        searchLayout.setAlignItems(Alignment.END);

        // Fibonacci UI
        TextField fibonacciInput = new TextField("Enter position for Fibonacci");
        fibonacciInput.setPlaceholder("Type a number");

        fibonacciButton = new Button("Fibonacci Number at place: 0");

        Button calculateFibonacciButton = new Button("Calculate Fibonacci", e -> {
            try {
                int position = Integer.parseInt(fibonacciInput.getValue());
                if (position < 0) {
                    Notification.show("Please enter a non-negative number.");
                    return;
                }
                int fibonacciNumber = calculateFibonacci(position);
                fibonacciButton.setText("Fibonacci Number at place: " + fibonacciNumber);
            } catch (NumberFormatException ex) {
                Notification.show("Please enter a valid number.");
            }
        });

        VerticalLayout fibonacciVerticalLayout = new VerticalLayout(fibonacciInput, calculateFibonacciButton, fibonacciButton);
        fibonacciVerticalLayout.setAlignItems(Alignment.CENTER); // Center the elements inside the vertical layout

        HorizontalLayout fibonacciLayout = new HorizontalLayout(fibonacciVerticalLayout);
        fibonacciLayout.setWidth("100%"); // Ensure it takes the full width of the screen
        fibonacciLayout.setAlignItems(Alignment.END); // This ensures the layout is centered
        fibonacciLayout.setJustifyContentMode(JustifyContentMode.START); // This centers the items in the layout horizontally
        fibonacciLayout.setWidth("100%"); // Ensure it takes the full width to be centered
        fibonacciLayout.setHeight("100%"); // Ensure it takes the full width to be centered

        // Create a button for the center of the screen
        Button backToMainButton = new Button("Back to Main View", e -> {
            UI.getCurrent().navigate(MainView.class);
        });

        // Create a layout to center the button horizontally and vertically on the screen
        VerticalLayout centerButtonLayout = new VerticalLayout(backToMainButton);
        centerButtonLayout.setAlignItems(Alignment.CENTER); // Center the button horizontally
        centerButtonLayout.setHeight("100%"); // Set the height to take up the full screen
        centerButtonLayout.setJustifyContentMode(JustifyContentMode.CENTER); // Center the button vertically

        // Main Layout
        HorizontalLayout mainLayout = new HorizontalLayout(countingLayout, arrayLayout, searchLayout);
        mainLayout.setWidth("100%");
        mainLayout.setAlignItems(Alignment.START);

        // Add the center button layout, main layout, and Fibonacci layout
        add(mainLayout, centerButtonLayout, fibonacciLayout);
    }

private void startCounting(int targetNumber, Div numberDiv) {
    isCounting = true;
    currentNumber = 1;

    UI ui = UI.getCurrent(); // Capture the UI reference once, for thread safety

    new Thread(() -> {
        while (currentNumber <= targetNumber) {
            int finalNumber = currentNumber;

            // Update the UI safely from this background thread
            ui.access(() -> numberDiv.setText("Current number: " + finalNumber));

            currentNumber++;

            try {
                Thread.sleep(1000); // Delay between updates (1 second)
            } catch (InterruptedException ignored) {
            }
        }

        // Optional: update flag/UI once counting is complete
        ui.access(() -> {
            isCounting = false;
            numberDiv.setText("Done!");
        });

    }).start();
}


    private void generateRandomArray() {
        randomArray = new int[5];
        sortedArray = new int[5];
        Random rand = new Random();
        for (int i = 0; i < randomArray.length; i++) {
            randomArray[i] = rand.nextInt(100);
            sortedArray[i] = randomArray[i];
        }

        updateArrayDisplay();
    }

    private void updateArrayDisplay() {
        StringBuilder arrayText = new StringBuilder("Array: ");
        for (int num : randomArray) {
            arrayText.append(num).append(" ");
        }
        arrayDiv.setText(arrayText.toString());
    }

    private void sortArray() {
        sortedArray = sortedArray.clone();
        for (int i = 0; i < sortedArray.length - 1; i++) {
            for (int j = 0; j < sortedArray.length - 1 - i; j++) {
                if (sortedArray[j] > sortedArray[j + 1]) {
                    int temp = sortedArray[j];
                    sortedArray[j] = sortedArray[j + 1];
                    sortedArray[j + 1] = temp;
                }
            }
        }

        updateSortedArrayDisplay();
    }

    private void updateSortedArrayDisplay() {
        StringBuilder arrayText = new StringBuilder();
        for (int num : sortedArray) {
            arrayText.append(num).append(" ");
        }
        sortedArrayField.setValue(arrayText.toString().trim());
    }

    private int calculateFibonacci(int n) {
        if (n <= 1) return n;
        int a = 0, b = 1, temp;
        for (int i = 2; i <= n; i++) {
            temp = a + b;
            a = b;
            b = temp;
        }
        return b;
    }
}
