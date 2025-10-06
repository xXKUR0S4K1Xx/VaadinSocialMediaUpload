package org.vaadin.example.social;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.textfield.TextArea;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.StreamResource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.popover.PopoverVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.StreamResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

@Route("admin")  // Defines the route for this view. When navigating to '/media', this view is displayed.
public class AdminView extends VerticalLayout {
    private static final Logger log = LoggerFactory.getLogger(Media.class);  // Main layout of the Media view. It extends VerticalLayout for vertical stacking of components.

    private Button sortNewButton;
    private Button sortTopButton;
    private HorizontalLayout middleBar;
    private int sortMode = 0; // 0 = new, 1 = top
    private VirtualList<Object> postList;
    private List<Post> allPosts;
    private Button sortButton;


    public AdminView() {  // Constructor that initializes the Media view.
        setSizeFull();  // Set the layout to take up the entire available space.
        setAlignItems(Alignment.CENTER);  // Align child components (like cards) to the center horizontally.
        setSpacing(false);  // Disable spacing between the components.
        setMargin(false);
        setPadding(false);
        getStyle().set("background-color", "#1a1a1b");  // Baby blue background
        String username = getLoggedInUsername();
        Media avatarService = new Media();
        String avatarUrl = "/avatar/" + username + "/" + avatarService.getAvatarFilenameForUser(username);


        VerticalLayout popoverContent = new VerticalLayout();
        popoverContent.getStyle().set("background-color", "#282b30")  // Apply the color you need
                .set("border-radius", "16px")  // Adjust the value as needed to round the corners
                .set("overflow", "hidden"); // This clips the content to the rounded corners


        Avatar userAvatar2 = new Avatar(username);
        userAvatar2.setImage(avatarUrl);
        userAvatar2.getStyle()
                .set("background-color", "white")
                .set("color", "black")
                .set("border", "1px solid black");
// Wrap avatar in RouterLink
        RouterLink avatarLink = new RouterLink();
        avatarLink.setRoute(UserPage.class); // replace with actual class
        avatarLink.add(userAvatar2);

// Optional: remove link styling
        avatarLink.getStyle().set("text-decoration", "none");
        RouterLink userpageLink = new RouterLink();
        userpageLink.setText("View Profile");
        userpageLink.setRoute(UserPage.class); // Replace with your actual target view class
        userpageLink.getStyle()
                .set("font-weight", "bold")
                .set("font-size", "14px")
                .set("color", "white")
                .set("text-decoration", "none"); // optional: remove underline


        Div usernameDiv = new Div();
        usernameDiv.setText(username); // Use the logged-in username
        usernameDiv.getStyle()
                .set("font-size", "13px")
                .set("color", "#7e8f96");

        VerticalLayout userInfoLayout = new VerticalLayout(userpageLink, usernameDiv);
        userInfoLayout.setPadding(false);
        userInfoLayout.setSpacing(false);
        userInfoLayout.setMargin(false);

        HorizontalLayout userRow = new HorizontalLayout(userInfoLayout);
        userRow.setAlignItems(FlexComponent.Alignment.CENTER);  // Center vertically
        userRow.setSpacing(true);  // Add some space between Avatar and the user info

// === Add the Second Avatar to a Different Layout or Row ===
        HorizontalLayout secondAvatarLayout = new HorizontalLayout(avatarLink, userInfoLayout);
        secondAvatarLayout.setAlignItems(FlexComponent.Alignment.CENTER);  // Center the second avatar
        secondAvatarLayout.setSpacing(true);


        Button logoutButton = new Button("Logout", event -> {
            // Implement logout logic here
            getUI().ifPresent(ui -> ui.navigate("login"));
        });
        logoutButton.getStyle().set("color", "white")
                .set("font-size", "14px");

        Button backToMediaButton = new Button("Upload your own Avatar", event -> {
            // Implement logout logic here
            getUI().ifPresent(ui -> ui.navigate("avatarselection"));
        });
        backToMediaButton.getStyle().set("color", "white")
                .set("font-size", "14px");
        popoverContent.add(secondAvatarLayout, backToMediaButton, logoutButton);

        Avatar userAvatar = new Avatar(username);

        userAvatar.getStyle()
                .set("background-color", "white")  //White background
                .set("color", "black")  // black text
                .set("border", "1px solid, black");  // white border
        // Create the popover once outside the click event
        Popover popover = new Popover();
        popover.setTarget(userAvatar2);
        popover.setPosition(PopoverPosition.BOTTOM);
        popover.addThemeVariants(PopoverVariant.ARROW, PopoverVariant.LUMO_NO_PADDING);
        popover.add(popoverContent);  // Add content to the popover only once

        // Create a variable to track if the popover is already opened
        boolean[] isOpened = {false};

        // Handle the click event to toggle the popover visibility
        userAvatar.getElement().addEventListener("click", event -> {
            if (!isOpened[0]) {
                // Open the popover if it's not opened
                popover.setOpened(true);
                isOpened[0] = true;

                // Set a timeout to keep the popover open for at least 1 second
                getUI().ifPresent(ui -> ui.access(() -> {
                    // After 1 second, allow the popover to be closed again
                    new Timer().schedule(new TimerTask() {
                        public void run() {
                            // Check if the popover is still open, then allow closing
                            if (popover.isOpened()) {
                                // Do nothing if it's already open, just wait for the close action
                            }
                        }
                    }, 1000);  // Delay for 1 second (1000 milliseconds)
                }));
            } else {
                // If the popover is already opened, close it when clicked again
                popover.setOpened(false);
                isOpened[0] = false;
            }
        });

        Icon notificationBell = new Icon(VaadinIcon.BELL);
        notificationBell.getElement().getStyle()
                .set("color", "#fff")  // Make the bell white for contrast
                .set("font-size", "24px");  // Adjust the size of the bell if needed

        // Create the search bar (TextField)
// === Search TextField ===
        Div wrapper = new Div();
        wrapper.getStyle()
                .set("position", "relative")
                .set("display", "inline-block"); // Needed to align the dropdown under the field

// Path to the users folder
        Path usersDir = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users");

// List to store usernames (folder names)
        List<String> suggestions = new ArrayList<>();

        try (Stream<Path> paths = Files.list(usersDir)) {
            // Filter: only directories, and add their names to the list (don't reassign the list!)
            paths.filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .forEach(suggestions::add);
        } catch (IOException e) {
            e.printStackTrace(); // or handle error appropriately
        }

// Create styled TextField for search
        TextField searchField = new TextField();
        searchField.setPlaceholder("Search Semaino");
        searchField.addClassName("media-textfield");
        searchField.getStyle()
                .set("color", "#D7DADC")
                .set("background-color", "#6C7A89")
                .set("border-radius", "20px")
                .set("width", "300px")
                .set("border", "none")
                .set("padding", "0 15px")
                .set("font-size", "12px")
                .set("z-index", "2")
                .set("position", "relative"); // ensure it's above dropdown

// Add value change listener to simulate autocomplete
        searchField.addValueChangeListener(event -> {
            String typed = event.getValue();
            if (typed == null || typed.isEmpty()) {
                return;
            }

            // Find first match that starts with typed value (case-insensitive)
            Optional<String> match = suggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(typed.toLowerCase()))
                    .findFirst();

            match.ifPresent(firstMatch -> {
                if (!typed.equalsIgnoreCase(firstMatch)) {
                    searchField.setValue(firstMatch);

                    // JavaScript fallback: highlight the autocompleted part
                    searchField.getElement().executeJs(
                            "this.setSelectionRange($0, $1);",
                            typed.length(),
                            firstMatch.length()
                    );
                }
            });


        });

// === Dropdown Container ===
        ListBox<String> dropdown = new ListBox<>();
        dropdown.setVisible(false);
        dropdown.setWidthFull(); // take 100% of parent
        dropdown.getStyle()
                .set("position", "absolute")
                .set("top", "100%") // place right below TextField
                .set("left", "0")
                .set("background-color", "#2c2f33")
                .set("color", "white")
                .set("border-radius", "10px")
                .set("z-index", "1")
                .set("box-shadow", "0 4px 8px rgba(0,0,0,0.3)");

// === Load usernames ===
        File usersDir2 = new File("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users");
        List<String> usernames = Optional.ofNullable(usersDir2.listFiles(File::isDirectory))
                .map(files -> Arrays.stream(files)
                        .map(File::getName)
                        .sorted()
                        .toList())
                .orElse(List.of());

// === Filter Logic ===
        searchField.addValueChangeListener(event -> {
            String input = event.getValue().toLowerCase();
            if (input.isEmpty()) {
                dropdown.setVisible(false);
                return;
            }

            List<String> filtered = usernames.stream()
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .toList();

            if (!filtered.isEmpty()) {
                dropdown.setItems(filtered);
                dropdown.setVisible(true);
            } else {
                dropdown.setVisible(false);
            }
        });

        dropdown.addValueChangeListener(event -> {
            String selectedUser = event.getValue();
            if (selectedUser != null) {
                // Step 1: Write to file
                try {
                    Path filePath = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/selecteduser.txt");
                    Files.writeString(filePath, selectedUser, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace(); // Replace with proper error handling in production
                    return;
                }

                // Step 2: Navigate to /userpage
                UI.getCurrent().navigate("userpage");

                // Step 3: Optional cleanup
                dropdown.setVisible(false);
            }
        });
        wrapper.add(searchField, dropdown);

        // Fancy "Communo" title
        RouterLink clickableTitle = new RouterLink("Semaino", Media.class);
        clickableTitle.getStyle()
                .set("font-family", "'Segoe Script', cursive")
                .set("font-size", "28px")
                .set("font-weight", "bold")
                .set("color", "#FFFFFF")
                .set("text-decoration", "none")
                .set("cursor", "pointer")
                .setWidth("179px");


// Add this `clickableTitle` to your layout instead of the Span directly

        // Create root layout
        HorizontalLayout rootLayout = new HorizontalLayout();
        rootLayout.setWidthFull();
        rootLayout.setPadding(false);
        rootLayout.setMargin(false);


        // Apply baby blue background without affecting text color
        rootLayout.getStyle()
                .set("border-bottom", "1px solid #666")
                .set("background-color", "#1a1a1b")  // Baby blue background
                .set("color", "#ffffff");  // Text remains dark grey, unaffected by the background


        // Sub-layouts for left, center, and right
        HorizontalLayout leftLayout = new HorizontalLayout(clickableTitle);
        leftLayout.setJustifyContentMode(JustifyContentMode.START);
        leftLayout.setWidthFull();
        leftLayout.getElement().getStyle().set("margin-left", "20px");
        leftLayout.getStyle().set("color", "#333");  // Text color stays dark grey

        HorizontalLayout centerLayout = new HorizontalLayout(wrapper);
        centerLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        centerLayout.setAlignItems(Alignment.CENTER);
        centerLayout.setWidthFull();
        centerLayout.getStyle().set("color", "#333");  // Text color stays dark grey

        HorizontalLayout rightLayout = new HorizontalLayout(notificationBell, userAvatar2);
        rightLayout.setJustifyContentMode(JustifyContentMode.END);
        rightLayout.setAlignItems(Alignment.CENTER);
        rightLayout.setWidthFull();
        rightLayout.getElement().getStyle().set("margin-right", "20px");
        rightLayout.getStyle().set("color", "#333");  // Text color stays dark grey

        // Add all sub-layouts to root
        rootLayout.add(leftLayout, centerLayout, rightLayout);
        rootLayout.setFlexGrow(1, leftLayout, centerLayout, rightLayout);


// Create the TreeGrid
        TreeGrid<Path> treeGrid = new TreeGrid<>();
        treeGrid.addClassName("admin-treegrid"); // custom class for CSS

// Show folder names only
        treeGrid.addHierarchyColumn(path -> path.getFileName().toString())
                .setHeader("Forums");

// Base forum folder
        Path forumsRoot = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/Forum");

// Recursive lambda for children
        java.util.function.Function<Path, List<Path>> childrenProvider = path -> {
            try {
                if (Files.isDirectory(path)) {
                    // Root level: show Admin/Moderator folders only
                    if (path.getParent().equals(forumsRoot)) {
                        return Files.list(path)
                                .filter(Files::isDirectory)
                                .filter(p -> p.getFileName().toString().equalsIgnoreCase("Admin")
                                        || p.getFileName().toString().equalsIgnoreCase("Moderator"))
                                .toList();
                    }
                    // Level 2: show usernames (folders inside Admin/Moderator)
                    else {
                        return Files.list(path)
                                .filter(Files::isDirectory)
                                .toList();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return List.of();
        };

// Set TreeGrid items
        try {
            List<Path> rootForums = Files.list(forumsRoot)
                    .filter(Files::isDirectory)
                    .filter(p -> !p.getFileName().toString().equalsIgnoreCase("Descriptors"))
                    .toList();

            treeGrid.setItems(rootForums, childrenProvider::apply);
        } catch (Exception e) {
            e.printStackTrace();
        }

// Set size
        treeGrid.setWidthFull();
        treeGrid.setHeight("400px"); // smaller height to fit below with grid
        treeGrid.getStyle().set("overflow", "hidden");
        treeGrid.getElement().getThemeList().add("no-scrollbar"); // tag grid host
        treeGrid.getElement().executeJs(
                "const t=this.shadowRoot && this.shadowRoot.querySelector('#table'); if(t) t.style.overflow='hidden';");

// --- Content layout ---
        VerticalLayout content = new VerticalLayout();
        content.setWidthFull();
        content.setHeight("100%");
        content.setPadding(false);
        content.setSpacing(false);
        content.setDefaultHorizontalComponentAlignment(Alignment.CENTER);

// Add TreeGrid to content
        content.add(treeGrid);
        content.setFlexGrow(1, treeGrid);

// --- Users Grid ---
        Grid<User> usersGrid = new Grid<>(User.class, false); // don't auto-generate columns
        usersGrid.addClassName("dark-grid");
        usersGrid.getElement().executeJs(
                "const t=this.shadowRoot && this.shadowRoot.querySelector('#table'); if(t) t.style.overflow='hidden';");
// Base path: dynamic, relative to the current system user
        String basePath = System.getProperty("user.home")
                + "/IdeaProjects/VaadinSocialMediaUpload/users";

// Column: Username
        usersGrid.addColumn(User::getUsername).setHeader("User");

// Column: Avatar (second column)
        usersGrid.addComponentColumn(user -> {
            File avatarFolder = new File(basePath + "/" + user.getUsername() + "/Avatar");
            if (avatarFolder.exists() && avatarFolder.isDirectory()) {
                File[] avatars = avatarFolder.listFiles();
                if (avatars != null && avatars.length > 0) {
                    // pick the newest avatar file
                    File avatarFile = Arrays.stream(avatars)
                            .max((f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()))
                            .orElse(avatars[0]);
                    StreamResource resource = new StreamResource(
                            avatarFile.getName(),
                            () -> {
                                try {
                                    return new FileInputStream(avatarFile);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                    return null;
                                }
                            }
                    );
                    Image avatar = new Image(resource, "avatar");
                    avatar.setWidth("50px");
                    avatar.setHeight("50px");
                    avatar.getStyle().set("border-radius", "50%"); // round avatar
                    return avatar;
                }
            }
            // Fallback if no avatar found
            Image placeholder = new Image();
            placeholder.setWidth("50px");
            placeholder.setHeight("50px");
            return placeholder;
        }).setHeader("Avatar");

// Column: Admin? Yes/No
        usersGrid.addColumn(user -> {
            File adminFolder = new File(basePath + "/" + user.getUsername() + "/Administrator");
            return (adminFolder.exists() && adminFolder.isDirectory()
                    && Objects.requireNonNull(adminFolder.listFiles()).length > 0) ? "Yes" : "No";
        }).setHeader("is Admin?");

// Column: Last post number
        usersGrid.addColumn(user -> {
            File postsFolder = new File(basePath + "/" + user.getUsername() + "/Posts");
            int lastPostNumber = 0;
            if (postsFolder.exists() && postsFolder.isDirectory()) {
                File[] posts = postsFolder.listFiles(File::isFile);
                if (posts != null && posts.length > 0) {
                    lastPostNumber = Arrays.stream(posts)
                            .map(File::getName)
                            .mapToInt(name -> {
                                try { return Integer.parseInt(name); }
                                catch (NumberFormatException e) { return 0; }
                            })
                            .max()
                            .orElse(0);
                }
            }
            return lastPostNumber;
        }).setHeader("Posts");

// Load all users from folder
        File usersFolder = new File(basePath);
        List<User> allUsers = new ArrayList<>();
        for (File userDir : Objects.requireNonNull(usersFolder.listFiles(File::isDirectory))) {
            User user = User.loadFromFile(userDir.getName());
            if (user != null) {
                allUsers.add(user);
            }
        }
        usersGrid.setItems(allUsers);

// Optional: size
        usersGrid.setWidthFull();
        usersGrid.setHeight("400px");
        usersGrid.getStyle().set("margin-top", "20px"); // spacing below TreeGrid
        usersGrid.getStyle().set("margin-bottom", "20px");
        usersGrid.getStyle().set("overflow", "hidden");

// Add Users Grid to content below TreeGrid
        content.add(usersGrid);
        content.setFlexGrow(1, usersGrid);

// Add content to AdminView layout
        add(content);


        HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeFull();
        layout.setAlignItems(Alignment.CENTER);
        layout.getStyle().set("position", "relative"); // Make it a positioning context

// ===== Sidebar (Overlay) =====
        VerticalLayout sideBar = new VerticalLayout();
        sideBar.setHeightFull();
        sideBar.setWidth("200px");
        sideBar.getStyle()
                .set("bottom", "0")
                .set("left", "0")
                .set("z-index", "1000")
                .set("border-right", "1px solid #666")
                .set("background-color", "#1a1a1b")
                .set("color", "#FFFFFF");

// Icons and Labels
        Icon homeIcon = VaadinIcon.BUILDING.create();
        Span homeText = new Span("Home");
        HorizontalLayout homeLayout = new HorizontalLayout(homeIcon, homeText);

        Icon popularIcon = VaadinIcon.LINE_CHART.create();
        Span popularText = new Span("Popular");
        HorizontalLayout popularLayout = new HorizontalLayout(popularIcon, popularText);

        Icon forYouIcon = VaadinIcon.SEARCH.create();
        Span forYouText = new Span("Recommended");
        HorizontalLayout forYouLayout = new HorizontalLayout(forYouIcon, forYouText);

        Icon allIcon = VaadinIcon.GLOBE.create();
        Span allText = new Span("All");
        HorizontalLayout allLayout = new HorizontalLayout(allIcon, allText);

// Add all to sidebar
        sideBar.add(homeLayout, popularLayout, forYouLayout, allLayout);

// ===== Filler (Overlay) =====
        VerticalLayout filler = new VerticalLayout();
        filler.setHeightFull();
        filler.setWidth("200px");
        filler.setAlignItems(Alignment.END);
        filler.getStyle()
                .set("bottom", "0")
                .set("right", "0")
                .set("background-color", "#1a1a1b")
                .set("border-left", "1px solid #444"); // Border on the left side

        // Vertical layout for description + TextArea
        VerticalLayout descriptionLayout = new VerticalLayout();
        descriptionLayout.setPadding(false);
        descriptionLayout.setSpacing(false);
        descriptionLayout.setWidthFull();

        // Div above the TextArea
        Div descriptionDiv = new Div();
        descriptionDiv.setText("Forum description");
        descriptionDiv.getStyle()
                .set("color", "white")
                .set("font-weight", "bold")
                .set("margin-bottom", "5px"); // spacing between div and TextArea

// TextArea for editing
        TextArea summaryArea = new TextArea();
        summaryArea.setWidthFull();
        summaryArea.setHeight("150px");
        summaryArea.setPlaceholder("Write summary here...");

// White text and outline for TextArea
        summaryArea.getStyle()
                .set("color", "white")
                .set("border", "1px solid white")
                .set("background-color", "#1a1a1b")
                .set("outline", "none");

// Load existing content from file
        String forumName = readCurrentForum(username); // get current forum
        if (forumName != null && !forumName.isEmpty()) {
            Path summaryFile = Paths.get(
                    "C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/Forum",
                    forumName,
                    "Descriptors",
                    "Summary"
            );
            if (Files.exists(summaryFile)) {
                try {
                    String descriptorText = Files.readString(summaryFile);
                    summaryArea.setValue(descriptorText);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

// Button to finish editing
        Button finishButton = new Button("Finish Editing", event -> {
            if (forumName == null || forumName.isEmpty()) return;

            Path summaryFile = Paths.get(
                    "C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/Forum",
                    forumName,
                    "Descriptors",
                    "Summary"
            );

            try {
                // Ensure parent folder exists
                Files.createDirectories(summaryFile.getParent());

                // Overwrite file with TextArea content
                Files.writeString(summaryFile, summaryArea.getValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

// White text and white outline for Button
        finishButton.getStyle()
                .set("color", "white")
                .set("border", "1px solid white")
                .set("background-color", "#1a1a1b");

        descriptionLayout.add(descriptionDiv, summaryArea, finishButton);

// Add TextArea and Button to filler layout
        filler.add(descriptionLayout);

// ===== Content =====
        layout.add(sideBar, content, filler); // Only content is part of layout flow
        layout.setFlexGrow(1, content);      // Only content should grow

// Add overlays after layout
        add(rootLayout, layout);  // Add overlays separately

    }

    private Component createAvatarSelectionCard() {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("600px");
        card.getStyle()
                .set("background-color", "#1a1a1b")
                .set("border", "1px solid #343536")
                .set("border-radius", "8px")
                .set("padding", "16px")
                .set("color", "#d7dadc")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.3)")
                .set("margin", "0 auto"); // center horizontally if possible

        // Title
        H4 title = new H4("Choose Your Avatar");
        title.getStyle().set("color", "#d7dadc").set("margin", "0 0 12px 0");

        // Upload component
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        String username = getLoggedInUsername();  // Or however you're retrieving it

        int maxSizeInBytes = 5 * 1024 * 1024;
        // String avatarDirPath = "C:/Users/sdachs/IdeaProjects/vaadin-programmieraufgaben/users/" + username + "/Avatar";
        String avatarDirPath = "C:/Users//sdachs/IdeaProjects/VaadinSocialMediaUpload/users/" + username + "/Avatar";


        upload.addFileRejectedListener(event -> {
            Notification.show("File is too large. Max size is 5 MB.");
        });

        upload.addSucceededListener(event -> {
            if (event.getContentLength() > maxSizeInBytes) {
                Notification.show("File exceeds 5 MB limit");
                upload.getElement().callJsFunction("clearFileList");
                return;
            }

            InputStream fileData = buffer.getInputStream();
            String fileName = event.getFileName();
            Path avatarDir = Paths.get(avatarDirPath);

            try {
                // Ensure the avatar directory exists
                Files.createDirectories(avatarDir);

                // Delete any existing files in the avatar directory
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(avatarDir)) {
                    for (Path file : stream) {
                        Files.deleteIfExists(file);
                    }
                }

                // Save the new avatar file
                Path targetPath = avatarDir.resolve(fileName);
                Files.copy(fileData, targetPath, StandardCopyOption.REPLACE_EXISTING);

                Notification.show("Avatar uploaded successfully!");

            } catch (IOException e) {
                e.printStackTrace();
                Notification.show("Failed to upload avatar.");
            }
        });

        upload.setMaxFiles(1);
        upload.setDropAllowed(true);
        upload.setAutoUpload(true);
        upload.getStyle()
                .set("border", "2px dashed #555")
                .set("padding", "20px")
                .set("background-color", "#2c2f33")
                .set("border-radius", "10px")
                .set("color", "#ccc")
                .set("cursor", "pointer");


        Span helper = new Span("Drag and drop or click to upload an image");
        helper.getStyle().set("font-size", "12px").set("color", "#999").set("margin-top", "8px");

        card.add(title, upload, helper);

        return card;
    }

    private String getLoggedInUsername() {
        try {
            return java.nio.file.Files.readString(java.nio.file.Paths.get("loggedinuser.txt")).trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown";
        }
    }
    public static String readCurrentForum(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }

        Path forumFilePath = Paths.get(
                "C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users",
                username,
                "Forum"
        );

        if (Files.exists(forumFilePath)) {
            try {
                String forum = Files.readString(forumFilePath).trim();
                if (!forum.isEmpty()) {
                    return forum;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}