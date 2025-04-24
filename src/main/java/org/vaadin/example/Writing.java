package org.vaadin.example;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.UI;

import java.io.*;

// Define a new Vaadin view that is accessible via the "writing" route (URL)
@Route("writing")
public class Writing extends VerticalLayout {

    // Constructor: called when the Writing view is created
    public Writing() {
        // Make the view take up the full available space
        setSizeFull();

        // Align components at the top vertically
        setJustifyContentMode(JustifyContentMode.START);

        // Center components horizontally
        setAlignItems(Alignment.CENTER);

        // ----- Top Navigation Layout -----

        // Button to go back to the Notizbuch view
        Button backButton = new Button("Back to Notizbuch", e -> UI.getCurrent().navigate(Notizbuch.class));

        // Button to go back to the main menu view
        Button backMenutButton = new Button("Back to the Main Menu", event -> UI.getCurrent().navigate(MainView.class));

        // Placeholder for the title/headline shown above the text area
        H2 headline = new H2("Überschrift muss überarbeitet werden");

        // Create a horizontal layout for navigation buttons and the headline
        HorizontalLayout navigationLayout = new HorizontalLayout(backButton, headline, backMenutButton);

        // Make navigation layout span the full width
        navigationLayout.setWidthFull();

        // Distribute elements (left, center, right)
        navigationLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // Add padding around the navigation bar
        navigationLayout.getStyle().set("padding", "10px");

        // Align items at the top of the horizontal layout
        navigationLayout.setAlignItems(Alignment.START);

        // ----- Text Area for Writing -----

        // Create a text area for writing notes
        TextArea textArea = new TextArea("Write your text here");

        // Set width to 80% of the container
        textArea.setWidth("80%");

        // Set height to 300 pixels
        textArea.setHeight("300px");

        // Show placeholder text when the field is empty
        textArea.setPlaceholder("Start typing...");

        // Limit the maximum number of characters to 1000
        textArea.setMaxLength(1000);

        // ----- Save Button Layout -----

        // Create a Save button
        Button saveButton = new Button("Save");

        // Wrap the Save button in a centered horizontal layout
        HorizontalLayout saveLayout = new HorizontalLayout(saveButton);
        saveLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        saveLayout.setWidthFull();

        // ----- Main Layout Configuration -----

        // Main layout holds the text area and save button
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull(); // Fill the available space
        mainLayout.setJustifyContentMode(JustifyContentMode.CENTER); // Center vertically
        mainLayout.setAlignItems(Alignment.CENTER); // Center horizontally

        // Add the text area and save layout to the main layout
        mainLayout.add(textArea, saveLayout);

        // Make the text area grow and fill the space
        mainLayout.setFlexGrow(1, textArea);

        // Add the navigation and main layout to the view
        add(navigationLayout, mainLayout);

        // ----- Load Filename from Temporary File -----

        // The temporary file stores the last selected title's filename
        File tempFile = new File("temporaryName.txt");

        // Initialize the variable for the filename
        String filename = null;

        // Check if the temporary file exists
        if (tempFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
                // Read the filename from the first line
                filename = reader.readLine();

                // Make sure the filename is not null or empty
                if (filename != null && !filename.isEmpty()) {

                    // Clean the filename to get only the title (remove number, .txt, underscores)
                    String cleanTitle = filename.substring(filename.indexOf("_") + 1).replace(".txt", "").replace("_", " ");

                    // Update the headline text with the cleaned title
                    headline.setText(cleanTitle);

                    // ----- Load Content from File -----

                    // Create a File object for the actual text file
                    File textFile = new File(filename);

                    // Check if the file exists before reading
                    if (textFile.exists()) {
                        // Read the file content line by line and build the text string
                        BufferedReader fileReader = new BufferedReader(new FileReader(textFile));
                        StringBuilder content = new StringBuilder();
                        String line;
                        while ((line = fileReader.readLine()) != null) {
                            content.append(line).append("\n");
                        }
                        fileReader.close(); // Close the reader after reading

                        // Set the loaded content in the text area, trim to remove trailing newlines
                        textArea.setValue(content.toString().trim());
                    }
                }
            } catch (IOException e) {
                // Print any exceptions during file reading
                e.printStackTrace();
            }
        }

        // Save the filename as final so it can be used in the lambda below
        String finalFilename = filename;

        // Save button action: write the text area content into the file
        saveButton.addClickListener(e -> {
            // Check if the filename is valid
            if (finalFilename != null && !finalFilename.isEmpty()) {
                File fileToSave = new File(finalFilename);

                // Write the current content of the text area to the file
                try (FileWriter writer = new FileWriter(fileToSave, false)) {
                    writer.write(textArea.getValue());
                } catch (IOException ex) {
                    // Print any exceptions during file writing
                    ex.printStackTrace();
                }
            }
        });

    } // End of constructor
} // End of Writing class

