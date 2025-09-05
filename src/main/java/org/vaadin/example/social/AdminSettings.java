package org.vaadin.example.social;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.Route;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
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
import com.vaadin.flow.server.VaadinSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Route("admin")
public class AdminSettings extends VerticalLayout {
    private static final Logger log = LoggerFactory.getLogger(Media.class);  // Main layout of the Media view. It extends VerticalLayout for vertical stacking of components.

    private Button sortNewButton;
    private Button sortTopButton;
    private HorizontalLayout middleBar;
    private int sortMode = 0; // 0 = new, 1 = top
    private VirtualList<Object> postList;
    private List<Post> allPosts;
    private Button sortButton;


    public AdminSettings() {  // Constructor that initializes the Media view.
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



        // Virtual List of posts
        postList = new VirtualList<>();
        postList.getElement().getStyle().set("scrollbar-gutter", "stable both-edges");  // Ensures the scrollbar appears on both edges.
        postList.getElement().getStyle()
                .set("padding", "0")
                .set("margin", "0");

        // Create list: first the post input card, then the posts
        VirtualList<Component> postList = new VirtualList<>();
        postList.setItems(List.of(createForumTreeComponent()));
        postList.setRenderer(new ComponentRenderer<>(component -> component));  // Single renderer

        postList.setWidthFull();   // Take full available width
        postList.setHeight("800px");  // Fixed height or use setHeightFull() if you want full height

        postList.getElement().getStyle().set("overflow", "hidden");  // Hide scrollbars if desired

        // Wrapper layout for holding the content
        VerticalLayout content = new VerticalLayout();  // A container to hold the post list.
        content.setWidthFull();  // Make the container take up full width.
        content.setHeight("100%");  // Make the container take up the remaining height (95%).
        content.setPadding(false);  // Remove padding from the container.
        content.setSpacing(false);  // Add spacing between components inside the container.
        content.setDefaultHorizontalComponentAlignment(Alignment.CENTER);  // Center-align the components horizontally.


        content.add(postList);  // Add the post list to the content layout.
        content.setFlexGrow(1, postList);  // Make the post list grow to fill available space.

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
    private Component createForumTreeComponent() {
        Path forumsPath = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/Forum");
        System.out.println("Building forum structure from path: " + forumsPath);

        // Create TreeGrid
        TreeGrid<TreeTest.Node> treeGrid = new TreeGrid<>();
        treeGrid.addHierarchyColumn(TreeTest.Node::getName).setHeader("Forum Structure");
        treeGrid.setWidthFull();
        treeGrid.setHeight("700px");

        // Build forum structure
        List<TreeTest.Node> forumNodes = new ArrayList<>();
        try {
            forumNodes = Files.list(forumsPath)
                    .filter(Files::isDirectory)
                    .map(forumFolder -> {
                        System.out.println("\nProcessing forum folder: " + forumFolder.getFileName());
                        TreeTest.Node forumNode = new TreeTest.Node(forumFolder.getFileName().toString());

                        // Process Admin folder (second level)
                        Path adminPath = forumFolder.resolve("Admin");
                        if (Files.exists(adminPath) && Files.isDirectory(adminPath)) {
                            TreeTest.Node adminNode = new TreeTest.Node("Admin");

                            // Process subfolders in Admin
                            processSubfolders(adminPath, adminNode);

                            // Process Moderator folder (third level under Admin)
                            Path moderatorPath = adminPath.resolve("Moderator");
                            if (Files.exists(moderatorPath) && Files.isDirectory(moderatorPath)) {
                                TreeTest.Node moderatorNode = new TreeTest.Node("Moderator");

                                // Process subfolders in Moderator
                                processSubfolders(moderatorPath, moderatorNode);

                                adminNode.getChildren().add(moderatorNode);
                            }

                            forumNode.getChildren().add(adminNode);
                        }

                        System.out.println("Final forum node: " + forumNode.getName() +
                                " has " + forumNode.getChildren().size() + " children");
                        return forumNode;
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error reading forums directory: " + e.getMessage());
            e.printStackTrace();
        }

        // Debug the complete node structure
        System.out.println("\nFinal node structure:");
        forumNodes.forEach(this::printNodeStructure);

        // Set items and expand
        treeGrid.setItems(forumNodes, TreeTest.Node::getChildren);
        expandAllNodes(treeGrid, forumNodes);

        return treeGrid;
    }

    // Unified method to process subfolders
    private void processSubfolders(Path parentPath, TreeTest.Node parentNode) {
        try {
            Files.list(parentPath)
                    .filter(Files::isDirectory)
                    .forEach(subfolder -> {
                        TreeTest.Node subfolderNode = new TreeTest.Node(subfolder.getFileName().toString());
                        System.out.println("  Processing subfolder: " + subfolderNode.getName());

                        // Recursively process any nested subfolders
                        processSubfolders(subfolder, subfolderNode);

                        parentNode.getChildren().add(subfolderNode);
                    });
        } catch (IOException e) {
            System.err.println("Error reading subfolders: " + e.getMessage());
        }
    }

    private void expandAllNodes(TreeGrid<TreeTest.Node> treeGrid, List<TreeTest.Node> nodes) {
        nodes.forEach(node -> {
            treeGrid.expand(node);
            if (!node.getChildren().isEmpty()) {
                expandAllNodes(treeGrid, node.getChildren());
            }
        });
    }
    private String getLoggedInUsername() {
        try {
            return java.nio.file.Files.readString(java.nio.file.Paths.get("loggedinuser.txt")).trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown";
        }
    }
    // Represents either a Forum (with children) or an Admin user (no children)
    private void processFiles(Path folderPath, TreeTest.Node parentNode) {
        try {
            Files.list(folderPath)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        TreeTest.Node fileNode = new TreeTest.Node(file.getFileName().toString());
                        parentNode.getChildren().add(fileNode);
                        System.out.println("  Added file: " + fileNode.getName());
                    });
        } catch (IOException e) {
            System.err.println("Error reading files: " + e.getMessage());
        }
    }

    private void processModeratorSubfolders(Path moderatorPath, TreeTest.Node moderatorNode) {
        try {
            Files.list(moderatorPath)
                    .filter(Files::isDirectory)
                    .forEach(subfolder -> {
                        TreeTest.Node subfolderNode = new TreeTest.Node(subfolder.getFileName().toString());
                        System.out.println("  Processing Moderator subfolder: " + subfolderNode.getName());

                        // Add files from subfolder
                        processFiles(subfolder, subfolderNode);

                        moderatorNode.getChildren().add(subfolderNode);
                    });
        } catch (IOException e) {
            System.err.println("Error reading Moderator subfolders: " + e.getMessage());
        }
    }

    private void printNodeStructure(TreeTest.Node node, String indent) {
        System.out.println(indent + node.getName() +
                " (children: " + node.getChildren().size() + ")");
        node.getChildren().forEach(child -> printNodeStructure(child, indent + "  "));
    }

    private void printNodeStructure(TreeTest.Node node) {
        printNodeStructure(node, "");
    }

    public static class Node {
        private String name;
        private List<TreeTest.Node> children;

        public Node(String name) {
            this.name = name;
            this.children = new ArrayList<>();
            System.out.println("Created node: " + name);
        }

        public String getName() {
            return name;
        }

        public List<TreeTest.Node> getChildren() {
            return children;
        }
    }
}
