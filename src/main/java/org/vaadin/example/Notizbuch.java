package org.vaadin.example;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Route("notebook")
public class Notizbuch extends VerticalLayout {

    private static final String TRASHBIN_DIR_PATH = "C:\\Users\\sdachs\\IdeaProjects\\vaadin-programmieraufgaben\\Trashbin";
    private File trashbinDir = new File(TRASHBIN_DIR_PATH);
    private static final String INHALT_FILE = "Inhaltsverzeichnis.txt";
    private static final String TEMPORARY_FILE = "temporaryName.txt";
    private static final String TRASHBIN_DIR = "Trashbin";
    private final TextField[] textFields = new TextField[9];
    private int currentPage = 1;

    private TextField pageInput;

    public Notizbuch() {
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.START);
        setAlignItems(Alignment.CENTER);

        pageInput = new TextField("Page Number");
        pageInput.setWidth("160px");
        pageInput.setValue(String.valueOf(currentPage));  // Set initial value to currentPage
        pageInput.setLabel("Page Number: " + currentPage);  // Set label to indicate current page number

        // Add pageInput to the layout
        add(pageInput);


        Button pageButton = new Button("Go to Page", e -> {
            try {
                int newPage = Integer.parseInt(pageInput.getValue());
                if (newPage > 0) {
                    currentPage = newPage;
                    loadTitles();
                    changePage(currentPage);

                }
            } catch (NumberFormatException ignored) {}
        });
        pageButton.setWidth("160px");

        Button backButton = new Button("<----", e -> {
            if (currentPage > 1) {
                currentPage--;
                loadTitles();
                changePage(currentPage);

            }
        });

        Button forwardButton = new Button("---->", e -> {
            currentPage++;
            loadTitles();
            changePage(currentPage);
        });

        VerticalLayout goSection = new VerticalLayout(pageInput, pageButton);
        goSection.setAlignItems(Alignment.CENTER);
        goSection.setPadding(false);
        goSection.setSpacing(false);

        HorizontalLayout topLayout = new HorizontalLayout(backButton, goSection, forwardButton);
        topLayout.setWidth("40%");
        topLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        topLayout.setAlignItems(Alignment.CENTER);

        VerticalLayout topWrapper = new VerticalLayout(topLayout);
        topWrapper.setWidthFull();
        topWrapper.setJustifyContentMode(JustifyContentMode.START);
        topWrapper.setAlignItems(Alignment.CENTER);


        VerticalLayout noticeLayout = new VerticalLayout();
        noticeLayout.setWidth("60%");

        HorizontalLayout row1 = new HorizontalLayout();
        HorizontalLayout row2 = new HorizontalLayout();
        HorizontalLayout row3 = new HorizontalLayout();

        for (int i = 0; i < 9; i++) {
            final int index = i;
            TextField titleField = new TextField("Title " + (i + 1));
            titleField.setWidth("240px");
            textFields[i] = titleField;

            Button saveButton = new Button("Save", e -> {
                saveTitle(index, titleField.getValue());
                UI.getCurrent().navigate(Writing.class);
            });
            saveButton.setEnabled(false);

            titleField.addValueChangeListener(event -> {
                String value = event.getValue();
                saveButton.setEnabled(value != null && !value.trim().isEmpty());
            });

            Button deleteButton = new Button("Delete", e -> {
                int globalPosition = (currentPage - 1) * 9 + index + 1;
                String title = titleField.getValue();
                String filename = globalPosition + "_" + title + ".txt";
                File originalFile = new File(filename);

                if (originalFile.exists()) {
                    try {
                        File trashFile = new File(TRASHBIN_DIR, filename);
                        Files.copy(originalFile.toPath(), trashFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        originalFile.delete();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                updateInhaltsverzeichnis(globalPosition, null);
                updatePageFile(globalPosition, null);
                clearTemporaryIfMatch(filename);

                titleField.clear();
                titleField.setLabel("Title " + (index + 1));
            });

            VerticalLayout pair = new VerticalLayout(saveButton, titleField, deleteButton);
            pair.setAlignItems(Alignment.CENTER);

            if (i < 3) row1.add(pair);
            else if (i < 6) row2.add(pair);
            else row3.add(pair);
        }

        noticeLayout.add(row1, row2, row3);
        noticeLayout.setAlignItems(Alignment.CENTER);

        ComboBox<String> trashbinComboBox = new ComboBox<>("Select Title from Trashbin");
        trashbinComboBox.setItems(loadTitlesFromTrashbin());
        trashbinComboBox.addValueChangeListener(event -> {
            String selectedTitle = event.getValue();
            if (selectedTitle != null) {
                insertTitleFromTrashbin(selectedTitle);
            }
        });


        ComboBox<TitleEntry> titleComboBox = new ComboBox<>("Search All Titles");
        titleComboBox.setItems(loadAllTitlesFromInhaltsverzeichnis());
        titleComboBox.setItemLabelGenerator(TitleEntry::getTitle);
        titleComboBox.addValueChangeListener(event -> {
            TitleEntry selected = event.getValue();
            if (selected != null) {
                currentPage = selected.getPageNumber();
                loadTitles();
                try (FileWriter writer = new FileWriter(TEMPORARY_FILE, false)) {
                    writer.write(selected.getPageNumber() + "_" + selected.getTitle() + ".txt");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                UI.getCurrent().navigate(Writing.class);
            }
        });

        VerticalLayout leftLayout = new VerticalLayout(trashbinComboBox, titleComboBox);
        leftLayout.setWidth("200px");
        leftLayout.getStyle().set("position", "absolute");
        leftLayout.getStyle().set("left", "140px");
        leftLayout.getStyle().set("top", "331px");
        leftLayout.getStyle().set("transform", "translateY(-50%)");



        Button mainViewButton = new Button("Back to Main Menu", e -> UI.getCurrent().navigate(MainView.class));
        Button programmierButton = new Button("Go to Programmieraufgaben", e -> UI.getCurrent().navigate(ProgrammierAufgaben.class));
        Button coinButton = new Button("Play CoinToss", e -> UI.getCurrent().navigate(CoinGame.class));
        Button hangmanButton = new Button("Play Hangman", e -> UI.getCurrent().navigate(Hangman.class));

        VerticalLayout rightLayout = new VerticalLayout(mainViewButton, programmierButton, coinButton, hangmanButton);
        rightLayout.setWidth("200px");
        rightLayout.setAlignItems(Alignment.END);
        rightLayout.getStyle().set("position", "absolute");
        rightLayout.getStyle().set("right", "20px");
        rightLayout.getStyle().set("top", "50%");
        rightLayout.getStyle().set("transform", "translateY(-50%)");

        VerticalLayout mainContent = new VerticalLayout(noticeLayout);
        mainContent.setSizeFull();
        mainContent.setJustifyContentMode(JustifyContentMode.START);
        mainContent.setAlignItems(Alignment.CENTER);

        add(topWrapper, mainContent, leftLayout, rightLayout);
        loadTitles();
    }

    private void clearTemporaryIfMatch(String filename) {
        File tempFile = new File(TEMPORARY_FILE);
        if (tempFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
                String line = reader.readLine();
                if (line != null && line.trim().equals(filename)) {
                    try (FileWriter writer = new FileWriter(tempFile, false)) {
                        writer.write(""); // Clear file
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updatePageFile(int position, String title) {
        String pageFileName = currentPage + "_page";
        File pageFile = new File(pageFileName);
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(pageFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith(position + " ")) {
                    lines.add(line);
                } else if (title != null) {
                    lines.add(position + " " + title);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (FileWriter writer = new FileWriter(pageFile, false)) {
            for (String l : lines) {
                writer.write(l + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changePage(int newPageNumber) {
        // Update the current page number
        currentPage = newPageNumber;
        pageInput.setLabel("Page Number: " + currentPage);  // Update label
        pageInput.setValue(String.valueOf(currentPage));  // Update value (text) to the current page number
    }


    private List<TitleEntry> loadAllTitlesFromInhaltsverzeichnis() {
        List<TitleEntry> titles = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(INHALT_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ", 2);
                if (parts.length == 2) {
                    int position = Integer.parseInt(parts[0]);
                    String title = parts[1];
                    int pageNumber = (position - 1) / 9 + 1;
                    titles.add(new TitleEntry(title, pageNumber, position));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return titles;
    }

    private List<String> loadTitlesFromTrashbin() {
        List<String> titles = new ArrayList<>();
        // Ensure the Trashbin directory exists and is valid
        if (trashbinDir.exists() && trashbinDir.isDirectory()) {
            // List all files in the Trashbin directory that end with .txt
            File[] files = trashbinDir.listFiles((dir, name) -> name.endsWith(".txt"));

            if (files != null) {
                for (File file : files) {
                    // Extract the title from the filename (after the first underscore and before the .txt)
                    String fileName = file.getName();
                    String title = fileName.substring(fileName.indexOf('_') + 1, fileName.lastIndexOf('.'));
                    titles.add(title); // Add the extracted title to the list
                }
            }
        }
        return titles; // Return the list of titles found in Trashbin
    }

    private void loadTitles() {
        String pageFileName = currentPage + "_page";
        File pageFile = new File(pageFileName);

        for (int i = 0; i < 9; i++) {
            textFields[i].setValue("");
            textFields[i].setLabel("Title " + (i + 1));
        }

        if (pageFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(pageFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(" ", 2);
                    if (parts.length == 2) {
                        int position = Integer.parseInt(parts[0]);
                        int indexInPage = (position - 1) % 9;
                        textFields[indexInPage].setValue(parts[1]);
                        textFields[indexInPage].setLabel(parts[1]);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveTitle(int index, String title) {
        int globalPosition = (currentPage - 1) * 9 + index + 1;
        String fullFilename = globalPosition + "_" + title + ".txt";

        // Check for any existing file with the same global position
        File currentDir = new File(".");
        File[] matchingFiles = currentDir.listFiles((dir, name) -> name.matches(globalPosition + "_.*\\.txt"));

        if (matchingFiles != null) {
            for (File oldFile : matchingFiles) {
                try {
                    // Move old file to Trashbin before deleting
                    File trashFile = new File("Trashbin", oldFile.getName());
                    Files.copy(oldFile.toPath(), trashFile.toPath(), StandardCopyOption.REPLACE_EXISTING);  // Copy to Trashbin
                    oldFile.delete();  // Delete original file
                    trashFile.delete();

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }


        // Create the new file for the title
        File writingFile = new File(fullFilename);
        try {
            if (!writingFile.exists()) {
                writingFile.createNewFile();
            }

            // Write the filename to temporary file for Writing.java
            try (FileWriter writer = new FileWriter(TEMPORARY_FILE, false)) {
                writer.write(fullFilename + "\n");
            }

            // Update Inhaltsverzeichnis.txt
            updateInhaltsverzeichnis(globalPosition, title);

            // Save the new title to the correct page file
            saveToPageFile(globalPosition, title);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void insertTitleFromTrashbin(String titleFromTrashbin) {
        int maxTitles = 1000;
        int foundPosition = -1;

        // Step 1: Find the first empty position
        for (int globalPosition = 1; globalPosition <= maxTitles; globalPosition++) {
            String expectedFilenamePattern = globalPosition + "_.*\\.txt";
            File currentDir = new File(".");
            File[] matchingFiles = currentDir.listFiles((dir, name) -> name.matches(expectedFilenamePattern));

            if (matchingFiles == null || matchingFiles.length == 0) {
                foundPosition = globalPosition;
                break;
            }
        }

        if (foundPosition != -1) {
            int targetPage = (foundPosition - 1) / 9 + 1;
            int indexInPage = (foundPosition - 1) % 9;

            // Step 2: Locate the correct file in Trashbin
            File trashDir = new File("Trashbin");
            File[] trashFiles = trashDir.listFiles((dir, name) -> name.contains("_" + titleFromTrashbin + ".txt"));

            if (trashFiles != null && trashFiles.length > 0) {
                File trashFile = trashFiles[0];

                // Step 3: Read the content from the trashbin file
                String fileContent = "";
                try {
                    fileContent = Files.readString(trashFile.toPath(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                // Step 4: Create the restored file in the root directory
                File restoredFile = new File(foundPosition + "_" + titleFromTrashbin + ".txt");
                try {
                    Files.writeString(restoredFile.toPath(), fileContent, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                // Step 5: Delete the trashbin file
                boolean deleted = trashFile.delete();
                if (!deleted) {
                    System.err.println("⚠️ Could not delete trash file: " + trashFile.getName());
                }

                // Step 6: Update UI and data
                updateInhaltsverzeichnis(foundPosition, titleFromTrashbin);
                try {
                    saveToPageFile(foundPosition, titleFromTrashbin);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (currentPage == targetPage) {
                    textFields[indexInPage].setValue(titleFromTrashbin);
                    textFields[indexInPage].setLabel(titleFromTrashbin);
                }

                // Step 7: Clear temporaryName.txt
                try (FileWriter writer = new FileWriter(TEMPORARY_FILE, false)) {
                    writer.write("");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Notification.show("Title and content restored to position " + foundPosition);
            } else {
                Notification.show("Title not found in Trashbin");
            }
        } else {
            Notification.show("No available position found");
        }
    }




    private void saveToPageFile(int globalPosition, String title) throws IOException {
        String pageFileName = currentPage + "_page";
        File pageFile = new File(pageFileName);
        List<String> lines = new ArrayList<>();
        boolean updated = false;

        if (pageFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(pageFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(globalPosition + " ")) {
                        lines.add(globalPosition + " " + title);
                        updated = true;
                    } else {
                        lines.add(line);
                    }
                }
            }
        }

        if (!updated) {
            lines.add(globalPosition + " " + title);
        }

        try (FileWriter writer = new FileWriter(pageFile, false)) {
            for (String line : lines) {
                writer.write(line + "\n");
            }
        }
    }

    private void updateInhaltsverzeichnis(int position, String title) {
        File inhaltFile = new File(INHALT_FILE);
        List<String> lines = new ArrayList<>();
        String newLine = position + " " + title;
        boolean updated = false;

        try {
            if (!inhaltFile.exists()) inhaltFile.createNewFile();

            try (BufferedReader reader = new BufferedReader(new FileReader(inhaltFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(position + " ")) {
                        if (title != null) {
                            lines.add(newLine);
                        }
                        updated = true;
                    } else {
                        lines.add(line);
                    }
                }
            }

            if (!updated && title != null) {
                lines.add(newLine);
            }

            try (FileWriter writer = new FileWriter(inhaltFile, false)) {
                for (String l : lines) {
                    writer.write(l + "\n");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class TitleEntry {
        private final String title;
        private final int pageNumber;
        private final int position;

        public TitleEntry(String title, int pageNumber, int position) {
            this.title = title;
            this.pageNumber = pageNumber;
            this.position = position;
        }

        public String getTitle() {
            return title;
        }

        public int getPageNumber() {
            return pageNumber;
        }

        public int getPosition() {
            return position;
        }
    }
}
