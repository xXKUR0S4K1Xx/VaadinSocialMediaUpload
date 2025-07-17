package org.vaadin.example.social;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.io.IOException;
import com.vaadin.flow.component.notification.Notification;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import com.vaadin.flow.component.select.Select;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.UI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.popover.PopoverVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


@Route("media")  // Defines the route for this view. When navigating to '/media', this view is displayed.
public class Media extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(Media.class);  // Main layout of the Media view. It extends VerticalLayout for vertical stacking of components.

    private Button sortNewButton;
    private Button sortTopButton;
    private HorizontalLayout middleBar;
    private int sortMode = 0; // 0 = new, 1 = top
    private VirtualList<Object> postList;
    private List<Post> allPosts;
    private Button sortButton;



    public Media() {  // Constructor that initializes the Media view.
        setSizeFull();  // Set the layout to take up the entire available space.
        setAlignItems(Alignment.CENTER);  // Align child components (like cards) to the center horizontally.
        setSpacing(false);  // Disable spacing between the components.
        setMargin(false);
        setPadding(false);
        getStyle().set("background-color", "#1a1a1b");  // Baby blue background
        String username = getLoggedInUsername();
        String avatarUrl = "/avatar/" + username + "/" + getAvatarFilenameForUser(username);


        VerticalLayout popoverContent = new VerticalLayout();
        popoverContent.getStyle().set("background-color", "#282b30")  // Apply the color you need
                .set("border-radius", "16px")  // Adjust the value as needed to round the corners
                .set("overflow", "hidden"); // This clips the content to the rounded corners


        // Get the avatar image URL for this user

// Create avatar with image (not initials)
        Avatar userAvatar2 = new Avatar();
        userAvatar2.setImage(avatarUrl);
        userAvatar2.getStyle()
                .set("background-color", "white")
                .set("border", "1px solid black");


// Wrap avatar in RouterLink (optional, can keep this if you want avatar clickable)
        RouterLink avatarLink = new RouterLink();
        avatarLink.setRoute(UserPage.class);
        avatarLink.add(userAvatar2);
        avatarLink.getStyle().set("text-decoration", "none");

// Instead of RouterLink for "View Profile", use a clickable Span
        Span userpageLink = new Span("View Profile");
        userpageLink.getStyle()
                .set("font-weight", "bold")
                .set("font-size", "14px")
                .set("color", "white")
                .set("text-decoration", "none")
                .set("cursor", "pointer");

// Add click listener that writes username and navigates
        userpageLink.getElement().addEventListener("click", event -> {
            try {
                Files.writeString(Paths.get("selecteduser.txt"), username);
            } catch (IOException e) {
                e.printStackTrace();
            }
            UI.getCurrent().navigate(UserPage.class);
        });

        Div usernameDiv = new Div();
        usernameDiv.setText(username); // Use the username of the profile shown
        usernameDiv.getStyle()
                .set("font-size", "13px")
                .set("color", "#7e8f96");

        VerticalLayout userInfoLayout = new VerticalLayout(userpageLink, usernameDiv);
        userInfoLayout.setPadding(false);
        userInfoLayout.setSpacing(false);
        userInfoLayout.setMargin(false);

        HorizontalLayout userRow = new HorizontalLayout(userInfoLayout);
        userRow.setAlignItems(FlexComponent.Alignment.CENTER);
        userRow.setSpacing(true);

// === Add the Second Avatar to a Different Layout or Row ===


// Now create the layout with avatar and user info
        HorizontalLayout secondAvatarLayout = new HorizontalLayout(avatarLink, userInfoLayout);
        secondAvatarLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        secondAvatarLayout.setSpacing(true);



        Button logoutButton = new Button("Logout", event -> {
            // Implement logout logic here
            getUI().ifPresent(ui -> ui.navigate("login"));
        });
        logoutButton.getStyle()
                .set("color", "white")  // Set text color to white
                .set("font-size", "14px");

        Button avatarCreatingButton = new Button("Upload your own Avatar", event -> {
            // Implement logout logic here
            getUI().ifPresent(ui -> ui.navigate("avatarselection"));
        });
        avatarCreatingButton.getStyle().set("color", "white")
                .set("font-size", "14px");
// Set text color to white
        popoverContent.add(secondAvatarLayout, avatarCreatingButton, logoutButton);

        Avatar userAvatar = new Avatar(username);
        userAvatar.setImage(avatarUrl);  // Set the user's avatar image

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
        userAvatar2.getElement().addEventListener("click", event -> {
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
System.out.println("Hello World");

        Icon notificationBell = new Icon(VaadinIcon.BELL);
        notificationBell.getElement().getStyle()
                .set("color", "#fff")  // Make the bell white for contrast
                .set("font-size", "24px");  // Adjust the size of the bell if needed

        // Create the search bar (TextField)
        TextField searchField = new TextField();
        searchField.setPlaceholder("Search Semaino");  // Placeholder text
        searchField.addClassName("media-textfield");
        searchField.getElement().getStyle().set("color", "#D7DADC");


// Apply styles to the outer TextField container
        searchField.getElement().getStyle()
                .set("background-color", "#6C7A89")  // Sky Blue for the outer container
                .set("color", "#FFFFFF ")               // Text color inside the field (dark for contrast)
                .set("border-radius", "20px")        // Rounded corners for the container
                .set("width", "300px")               // Width of the search bar
                .set("border", "none")               // Remove the default border
                .set("padding", "0 15px")            // Padding inside the field
                .set("font-size", "12px");           // Set font size to 14px (adjust as needed)

// Apply styles to the inner input element to ensure it matches the outer container
        searchField.getElement().getChildren()
                .filter(child -> child.getTag().equals("input"))  // Find the <input> element
                .forEach(input -> input.getStyle()
                        .set("background-color", "#FFFFFF   ")  // Sky Blue for the inner input field (to match the container)
                        .set("border-radius", "20px")        // Rounded corners for the input field (to match container)
                        .set("border", "none")               // Remove default border from the input field
                        .set("color", "#FFFFFF ")               // Text color in the input (dark for contrast)
                        .set("padding", "0 15px")            // Padding inside the input field
                );


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

        HorizontalLayout centerLayout = new HorizontalLayout(searchField);
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

        // Create the input card ONCE. This is the first element of the list.
        Component inputCard = createPostInputCard();  // Create the input card for adding posts or replies.
        inputCard.getElement().getStyle().set("width", "800px");  // Set the width of the input card to 800px.
        inputCard.getElement().getStyle().set("margin-left", "auto");  // Center the card horizontally.
        inputCard.getElement().getStyle().set("margin-right", "auto");  // Center the card horizontally.
        inputCard.getElement().getStyle().set("margin-top", "15px");

        // === Sort Button (Trigger) ===
        sortButton = new Button("New");
        sortButton.addClassName("glow-hover");
        sortButton.getStyle()
                .set("margin", "0")
                .set("padding", "0")
                .set("height", "20px")
                .set("color", "#686b6e") // Set text color
                .set("font-size", "13px");       // Make text smaller (half of typical 20px)

        Div sortButtonWrapper = new Div();
        sortButtonWrapper.addClassName("elliptical-glow-wrapper");

// Add the sortButton inside the wrapper
        sortButtonWrapper.add(sortButton);
// === Popover Setup ===
        Popover sortPopoverPopup = new Popover();
        sortPopoverPopup.addClassName("glow-hover");
        sortPopoverPopup.setTarget(sortButton); // This is the trigger
        sortPopoverPopup.setPosition(PopoverPosition.BOTTOM);
        sortPopoverPopup.addThemeVariants(PopoverVariant.ARROW, PopoverVariant.LUMO_NO_PADDING);

// === Popover Content ===
        VerticalLayout sortPopoverContent = new VerticalLayout();
        sortPopoverContent.setWidthFull(); // Ensure popup content fills available width
        sortPopoverContent.setPadding(false);
        sortPopoverContent.setSpacing(false);
        sortPopoverContent.getStyle()
                .set("background-color", "#282b30")
                .set("color", "#ffffff")
                .set("min-width", "120px")
                .set("text-align", "left")
                .set("border-radius", "8px");  // 👈 Rounded corners


        Div sortHeader = new Div();
        sortHeader.setText("Sort by:");
        sortHeader.getStyle()
                .set("font-weight", "bold")
                .set("color", "#686b6e")
                .set("text-align", "center")
                .set("width", "100%"); // Ensure it spans enough space to allow centering

// === Sort Options ===
        sortNewButton = new Button("New");
        sortNewButton.setWidthFull(); // Ensures the button spans full width
        sortNewButton.addClassName("popup-hover-item");
        sortNewButton.getStyle().set("width", "100%")
                .set("color", "#D7DADC");


        sortTopButton = new Button("Top");
        sortTopButton.setWidthFull();
        sortTopButton.addClassName("popup-hover-item");
        sortTopButton.getStyle().set("width", "100%")
                .set("color", "#D7DADC");

// === Assemble Popover ===
        sortPopoverContent.add(sortHeader, sortNewButton, sortTopButton);
        sortPopoverPopup.add(sortPopoverContent);

// === Track Popover Open State ===
        boolean[] isSortPopoverOpen = {false};

// === Toggle with Delay to Prevent Vaadin Sync Issues ===
        sortButton.getElement().addEventListener("click", e -> {
            if (!isSortPopoverOpen[0]) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        getUI().ifPresent(ui -> ui.access(() -> {
                            sortPopoverPopup.setOpened(true);
                            isSortPopoverOpen[0] = true;
                        }));
                    }
                }, 100); // 100ms delay
            } else {
                sortPopoverPopup.setOpened(false);
                isSortPopoverOpen[0] = false;
            }
        });

// === Button Logic ===
        sortNewButton.addClickListener(e -> {
            sortMode = 0;
            loadPosts();
            updateSortButtonHighlight();
            sortPopoverPopup.setOpened(false);
            isSortPopoverOpen[0] = false;
        });

        sortTopButton.addClickListener(e -> {
            sortMode = 1;
            loadPosts();
            updateSortButtonHighlight();
            sortPopoverPopup.setOpened(false);
            isSortPopoverOpen[0] = false;
        });
        updateSortButtonHighlight();


// Create the horizontal layout wrapper and center the button
        middleBar = new HorizontalLayout(sortButtonWrapper);        middleBar.setWidth("800px"); // Fixed width
        middleBar.setHeight("20px");
        middleBar.getElement().getStyle().set("margin-left", "auto");  // Center the card horizontally.
        middleBar.getElement().getStyle().set("margin-right", "auto");  // Center the card horizontally.
        middleBar.getElement().getStyle().set("margin-top", "10px");  // Center the card horizontally.
        middleBar.setJustifyContentMode(FlexComponent.JustifyContentMode.START); // Center the button
        middleBar.setPadding(false);
        middleBar.getStyle()
                .set("background-color", "#1a1a1b"); // Optional: match card background



        postList = new VirtualList<>();
        postList.getElement().getStyle().set("scrollbar-gutter", "stable both-edges");
        postList.getElement().getStyle()
                .set("padding", "0")
                .set("margin", "0");

        List<Object> items = new ArrayList<>();
        items.add(middleBar);
        items.add(inputCard);
        items.addAll(UserPost.readPostsFromFiles());

        postList.setItems(items);
        postList.getElement().getStyle().set("overflow", "hidden");

        postList.setRenderer(new ComponentRenderer<>(item -> {  // Define how each item should be rendered in the list.
            if (item instanceof Component) {
                return (Component) item;  // If the item is the input card, return it as a component.
            } else if (item instanceof Post post) {  // If the item is a post, render it as a post card.
                Component card;
                if (post.getParentId().equals("0")) {  // If the post is a top-level comment (not a reply).
                    card = createCommentCard(post);  // Create a comment card.
                } else {
                    card = createReplyCard(post);  // Otherwise, create a reply card.
                }
                // Set styles for the card (no background color interference).
                card.getElement().getStyle().set("margin", "0 auto");
                card.getElement().getStyle().set("width", "800px");
                card.getElement().getStyle().set("margin-top", "10px");
                return card;
            } else {
                return new Span("Unknown item");  // Default message if item is unknown.
            }
        }));

        postList.setWidthFull();  // Make the list take up full width.
        postList.setHeightFull();  // Make the list take up full height.

        // Wrapper layout for holding the content
        VerticalLayout content = new VerticalLayout();  // A container to hold the post list.
        content.setWidthFull();  // Make the container take up full width.
        content.setHeight("100%");  // Make the container take up the remaining height (95%).
        content.setPadding(false);  // Remove padding from the container.
        content.setSpacing(true);  // Add spacing between components inside the container.
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

// Container div above popup
        Div containerDiv = new Div();
        containerDiv.setText("All Pages:");
        containerDiv.getStyle()
                .set("margin-top", "20px")
                .set("font-weight", "bold")
                .set("color", "white");

        Div containerDiv2 = new Div();
        containerDiv2.setText("Current Page:");
        containerDiv2.getStyle()
                .set("margin-top", "20px")
                .set("font-weight", "bold")
                .set("color", "white");
// Popup Div
        Div pageSelectionPopup = new Div();
        pageSelectionPopup.getStyle()
                .set("background-color", "#1a1a1b")
                .set("color", "white")
                .set("border", "1px solid #444")
                .set("border-radius", "4px")
                .set("padding", "10px")
                .set("margin-top", "4px")
                .set("width", "100%")
                .set("box-sizing", "border-box") // ← fixes horizontal overflow
                .set("display", "none")
                .set("z-index", "10");

// Title for selecting forum
        H4 dialogTitle = new H4("Select a forum");
        dialogTitle.getStyle().set("color", "white");
        pageSelectionPopup.add(dialogTitle);

// Vertical layout for forum buttons
        VerticalLayout pageListLayout = new VerticalLayout();
        pageListLayout.setPadding(false);
        pageListLayout.setSpacing(false);
        pageListLayout.setMargin(false);
        pageSelectionPopup.add(pageListLayout);


// Title for forum creation
        H4 createTitle = new H4("Create new forum");
        createTitle.getStyle()
                .set("color", "white")
                .set("font-size", "14px")
                .set("margin", "16px 0 4px 0");

// TextField with dark background and compact height
// TextField with dark background and rounded corners
        TextField forumNameField = new TextField();
        forumNameField.setPlaceholder("");
        forumNameField.setWidthFull();
        forumNameField.getElement().getStyle()
                .set("--lumo-text-field-size", "25px") // Controls the full component height
                .set("font-size", "12px")              // Adjusts text size
                .set("line-height", "25px")            // Aligns text vertically
                .set("border", "1px solid #444")
                .set("border-radius", "4px")
                .set("background-color", "#1a1a1b")
                .set("color", "white")
                .set("box-sizing", "border-box");

// Button to open popup
        String forumname = readCurrentForum(username);
        Button openPopupButton = new Button(forumname);
        openPopupButton.getStyle()
                .set("background-color", "#1a1a1b")
                .set("color", "white")
                .set("border", "1px solid #444")
                .set("border-radius", "4px")
                .set("width", "100%");

// Create button
        Button createForumButton = new Button("Create");
        createForumButton.getStyle()
                .set("background-color", "#333")
                .set("color", "white")
                .set("border", "1px solid #444")
                .set("border-radius", "4px")
                .set("width", "100%")
                .set("font-size", "26px")
                .set("height", "25px")
                .set("margin-top", "4px");

        createForumButton.addClickListener(event -> {
            String forumName = forumNameField.getValue().trim();
            UI ui = UI.getCurrent();

            java.util.function.Consumer<String> showPopup = msg -> {
                Div popup = new Div();
                popup.setText(msg);
                popup.getStyle()
                        .set("position", "fixed")
                        .set("top", "50%")
                        .set("left", "50%")
                        .set("transform", "translate(-50%, -50%)")
                        .set("background-color", "#1a1a1b")
                        .set("color", "#d3d3d3")
                        .set("padding", "15px 30px")
                        .set("border-radius", "8px")
                        .set("box-shadow", "0 4px 10px rgba(0,0,0,0.5)")
                        .set("z-index", "10000")
                        .set("font-size", "14px")
                        .set("text-align", "center");

                ui.add(popup);
                new java.util.Timer().schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        ui.access(() -> popup.removeFromParent());
                    }
                }, 3000);
            };

            if (forumName.isEmpty()) {
                showPopup.accept("Please enter a forum name");
                return;
            }

            Path baseForumDir = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/Forum");
            Path userFollowedForumsDir = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users", username, "Followed Forums");
            Path userForumFile = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users", username, "Forum");

            try {
                Path newForumPath = baseForumDir.resolve(forumName);
                if (Files.exists(newForumPath)) {
                    showPopup.accept("Forum already exists!");
                    return;
                }

                Files.createDirectories(newForumPath);
                Files.writeString(userForumFile, forumName, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                Files.createDirectories(userFollowedForumsDir);
                Path forumFollowFile = userFollowedForumsDir.resolve(forumName + ".txt");
                Files.writeString(forumFollowFile, "", StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                showPopup.accept("Forum '" + forumName + "' created and selected!");
                forumNameField.clear();

                // ✅ Update the button label here
                openPopupButton.setText(forumName);

            } catch (IOException e) {
                e.printStackTrace();
                showPopup.accept("Error creating forum: " + e.getMessage());
            }
        });


// Vertical layout wrapper for alignment
        VerticalLayout createForumLayout = new VerticalLayout(forumNameField, createForumButton);
        createForumLayout.setPadding(false);
        createForumLayout.setSpacing(false);
        createForumLayout.setMargin(false);
        createForumLayout.setWidthFull();
        createForumLayout.getStyle()
                .set("background-color", "#1a1a1b")
                .set("padding", "0");

        // Create dropdown for forum selection
        Select<String> forumDropdown = new Select<>();
        forumDropdown.setItems(getAllForumNames());
        forumDropdown.setPlaceholder("Select a forum");
        forumDropdown.setWidthFull();
        forumDropdown.getStyle()
                .set("margin", "10px 0")
                .set("--lumo-size", "2px")  // Or even 28px for tighter height
                .setWidth("165px")
                .setHeight("38px")//
                .set("font-size", "13px")
                .set("line-height", "32px")
                .set("border", "1px solid #444")
                .set("border-radius", "4px")
                .set("background-color", "#1a1a1b")
                .set("color", "white")
                .set("box-sizing", "border-box")
                .set("text-align", "center");

        Select<String> select = new Select<>();
        select.setItems("Option 1", "Option 2", "Option 3");
        select.setPlaceholder("Select a forum");

// Set placeholder text color to dull white (gray)
        select.getElement().getStyle().set("--lumo-secondary-text-color", "#ffffff");
// Update selected forum on change
        forumDropdown.addValueChangeListener(event -> {
            String selectedForum = event.getValue();
            if (selectedForum != null && !selectedForum.isEmpty()) {
                try {
                    UI.getCurrent().getPage().reload();

                    Path userForumFile = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users", username, "Forum");
                    Files.writeString(userForumFile, selectedForum, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    openPopupButton.setText(selectedForum);
                    loadPosts();  // if you want to reload posts for the selected forum
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Div dropdownWrapper = new Div(forumDropdown);
        dropdownWrapper.getStyle()
                .set("position", "relative")
                .set("z-index", "10001");  // Raise stacking context

        Span buttonText = new Span();
        buttonText.getElement().setProperty("innerHTML", "<div style='padding:8px 16px; line-height:1.3;'>Subscribe to<br>" + forumname + "</div>");

        readCurrentForum(username);
        Button subscribeButton = new Button(buttonText);
        subscribeButton.getStyle()
                .set("margin", "12px")
                .set("background-color", "#333")
                .set("color", "white")
                .set("border", "1px solid #444")
                .set("border-radius", "4px")
                .set("height", "60px")            // ⬅️ increases vertical size
                .set("white-space", "normal") // allow wrapping
                .set("word-break", "break-word") // break long words
                .set("max-width", "200px") // still a max width to prevent layout breakage
                .set("padding", "10px 16px");

        subscribeButton.addClickListener(event -> {
            try {

                Path followedForumsDir = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users", username, "Followed Forums");
                Files.createDirectories(followedForumsDir);  // Make sure the folder exists

                Path forumFile = followedForumsDir.resolve(forumname + ".txt");
                if (Files.exists(forumFile)) {
                    Notification.show("Already subscribed to " + forumname);
                } else {
                    Files.writeString(forumFile, "", StandardOpenOption.CREATE_NEW);
                    Notification.show("Subscribed to " + forumname);
                }

            } catch (IOException e) {
                e.printStackTrace();
                Notification.show("Error subscribing to forum: " + e.getMessage());
            }
        });

        Div spacer = new Div();
        spacer.setHeightFull();  // takes remaining vertical space
        sideBar.setFlexGrow(1, spacer);

        Div spacer2 = new Div();
        spacer.setHeightFull();  // takes remaining vertical space
        sideBar.setFlexGrow(1, spacer);

        Div spacer3 = new Div();
        spacer.setHeightFull();  // takes remaining vertical space
        sideBar.setFlexGrow(1, spacer);
// Add all to sidebar
        sideBar.add(
                homeLayout,
                popularLayout,
                forYouLayout,
                allLayout,
                containerDiv,
                dropdownWrapper,
                containerDiv2,
                openPopupButton,
                pageSelectionPopup,
                createTitle,
                createForumLayout,
                spacer,
                subscribeButton,
                spacer2,
                spacer3
        );

// Toggle popup visibility on button click
        openPopupButton.addClickListener(event -> {
            boolean isVisible = "block".equals(pageSelectionPopup.getStyle().get("display"));
            pageSelectionPopup.getStyle().set("display", isVisible ? "none" : "block");
        });

// Load followed forums for current user (same as before), but add buttons to pageListLayout:
        User currentUser = User.getCurrentUser();
        if (currentUser != null) {

            Path followedForumsFolder = Paths.get(
                    "C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users",
                    username,
                    "Followed Forums"
            );
            // C:\\Users\\sdachs\\IdeaProjects\\VaadinSocialMediaUpload\\users\\a\Followed Forums
            try (Stream<Path> stream = Files.list(followedForumsFolder)) {
                List<String> followedForumNames = stream
                        .filter(Files::isRegularFile)
                        .map(path -> {
                            String filename = path.getFileName().toString();
                            int dotIndex = filename.lastIndexOf('.');
                            return (dotIndex != -1) ? filename.substring(0, dotIndex) : filename;
                        })
                        .toList();

                pageListLayout.removeAll();
                pageListLayout.add(dialogTitle);

                if (followedForumNames.isEmpty()) {
                    pageListLayout.add(new Span("No followed forums found."));
                } else {
                    for (String forumName : followedForumNames) {
                        Button forumButton = new Button(forumName);
                        forumButton.getStyle()
                                .set("width", "100%")
                                .set("text-align", "left")
                                .set("background-color", "#1a1a1b")
                                .set("color", "white")
                                .set("border", "none")
                                .set("padding", "8px 12px");
                        forumButton.addClickListener(ev -> {
                            Path forumFilePath = Paths.get(
                                    "C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users",
                                    username,
                                    "Forum"
                            );
                            try {
                                Files.writeString(forumFilePath, forumName);
                                System.out.println("Set current forum to: " + forumName);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            sortMode = 0;
                            loadPosts();

                            openPopupButton.setText(forumName);
                            pageSelectionPopup.getStyle().set("display", "none");  // close popup
                        });
                        pageListLayout.add(forumButton);
                    }
                }

                Path forumFilePath = Paths.get(
                        "C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users",
                        username,
                        "Forum"
                );


            } catch (IOException e) {
                e.printStackTrace();
                pageListLayout.removeAll();
                pageListLayout.add(dialogTitle, new Span("Failed to load forums."));
            }
        } else {
            pageListLayout.removeAll();
            pageListLayout.add(dialogTitle, new Span("User not logged in."));
        }




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
        layout.setFlexGrow(1, content);       // Only content should grow

// Add overlays after layout
        add(rootLayout, layout);  // Add overlays separately

    }

     String getAvatarFilenameForUser(String username) {
        Path avatarDir = Paths.get("users", username, "Avatar");

        if (Files.exists(avatarDir) && Files.isDirectory(avatarDir)) {
            try (Stream<Path> files = Files.list(avatarDir)) {
                return files
                        .filter(Files::isRegularFile)
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .findFirst()
                        .orElse("default.png"); // fallback if no file found
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "default.png"; // fallback if folder missing or error
    }
    // Define the Comment Card
    public VerticalLayout createCommentCard(Post postData) {
        // Create the container for the card layout
        VerticalLayout commentCardLayout = new VerticalLayout();
        commentCardLayout.addClassName("hover-card");

        commentCardLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        commentCardLayout.setSpacing(true);
        commentCardLayout.setPadding(true);
        commentCardLayout.getStyle()
                .set("border", "1px solid #ccc")
                .set("border-radius", "10px")  // ✅ add smooth edges
                .set("padding", "10px")
                .set("margin", "0")
                .set("background-color", "#1a1a1b")  // Dark background
                .set("color", "#ffffff");  // White text

        commentCardLayout.setWidth("800px");

        // Top: Avatar + User Name + Posted on: Date
        HorizontalLayout topRow = new HorizontalLayout();
        // Get the username from post data
        String username = postData.getUserName();

// Get the avatar image URL for this user (implement this method based on your setup)
        String avatarUrl = "/avatar/" + username + "/" + getAvatarFilenameForUser(username);

// Create avatar without initials but with the user image
        Avatar userAvatar = new Avatar();
        userAvatar.setImage(avatarUrl);  // Set the user's uploaded image

// Style the avatar (white background, black border)
        userAvatar.getStyle()
                .set("background-color", "white")
                .set("border", "1px solid #ffffff");

// Username label with white text
        Span userName = new Span(username);
        userName.getStyle()
                .set("color", "#ffffff")
                .set("text-decoration", "underline")
                .set("cursor", "pointer");

        userName.getElement().addEventListener("click", event -> {
            try {
                // Username that was clicked
                String clickedUsername = username;

                // Read logged-in user
                String currentUsername = Files.readString(Paths.get("loggedinuser.txt")).trim();

                if (clickedUsername.equals(currentUsername)) {
                    // If clicking own username, write logged-in user
                    Files.writeString(Paths.get("selecteduser.txt"), currentUsername);
                } else {
                    // If clicking other user, write their username
                    Files.writeString(Paths.get("selecteduser.txt"), clickedUsername);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            UI.getCurrent().navigate("userpage");
        });


// Post time label with white text
        Span commentTime = new Span("Posted on: " + Post.formatTimestamp(postData.getTimestamp()));
        commentTime.getStyle().set("color", "#ffffff");

// Add components to the horizontal layout row
        topRow.add(userAvatar, userName, commentTime);
        topRow.setWidthFull();
        topRow.setJustifyContentMode(JustifyContentMode.START);
        topRow.setAlignItems(Alignment.CENTER);


        // Comment content (Textfield for the comment)
        Div commentContent = new Div();
        commentContent.setWidthFull(); // Make the row take full width

        commentContent.getStyle().set("text-align", "left");
        commentContent.getStyle()
                .set("white-space", "pre-wrap")
                .set("overflow-wrap", "break-word")
                .set("word-break", "break-word")
                .set("max-width", "750px")
                .set("color", "#ffffff");  // White text
        commentContent.setText(postData.getPostContent());

        // Display number of likes
        HorizontalLayout likesRow = new HorizontalLayout();
        likesRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
        likesRow.setAlignItems(Alignment.CENTER);
        likesRow.getStyle()
                .set("width", "725px"); // ✅ 700px for comment card

        // Likes count
        Span likesCount = new Span("Liked: " + postData.getLikes());
        likesCount.getStyle()
                .set("font-size", "14px")
                .set("color", "#ffffff")  // White text
                .set("white-space", "nowrap");

        // Like button avatar
        LikeButton likeButton = new LikeButton(postData);
        likeButton.getStyle()
                .set("background-color", "white")  // Avoid dark background affecting the avatar
                .set("color", "black")  // Ensure the text/initials stay white
                .set("border", "1px solid #ffffff");  // Optional: add a border to the avatar
        likesRow.add(likesCount, likeButton);

        // Add everything to the layout
        commentCardLayout.add(topRow, commentContent, likesRow);

        UserPost userPostInstance = new UserPost();
        commentCardLayout.add(userPostInstance.createReplyInputSection(postData));

        return commentCardLayout;
    }

    // Define the Reply Card
    public VerticalLayout createReplyCard(Post postData) {
        // Reply card layout container
        VerticalLayout replyCardLayout = new VerticalLayout();
        replyCardLayout.addClassName("hover-card");

        replyCardLayout.setAlignItems(Alignment.START);
        replyCardLayout.setSpacing(true);
        replyCardLayout.setPadding(true);
        replyCardLayout.setWidth("800px");
        replyCardLayout.getStyle()
                .set("border", "1px solid #ccc")
                .set("border-radius", "10px")  // ✅ add smooth edges
                .set("padding", "10px")
                .set("background-color", "#1a1a1b")  // Dark background
                .set("color", "#ffffff");  // White text

        Post parentPost = UserPost.findPostById(postData.getParentId());

        // 🔝 Parent post info
        HorizontalLayout topRow = new HorizontalLayout();
        String originalUsername = parentPost != null ? parentPost.getUserName() : "Unknown";
        String avatarFilename = getAvatarFilenameForUser(originalUsername); // Same helper method
        String avatarUrl = "/avatar/" + originalUsername + "/" + avatarFilename;

        Avatar originalAvatar = new Avatar();
        originalAvatar.setImage(avatarUrl);  // Show the uploaded image

        originalAvatar.getStyle()
                .set("background-color", "white")
                .set("border", "1px solid #ffffff");

        String originalPosterName = parentPost != null ? parentPost.getUserName() : "Unknown";
        Span originalPoster = new Span(originalPosterName);
        originalPoster.getStyle()
                .set("color", "#ffffff")
                .set("text-decoration", "underline")
                .set("cursor", "pointer");

        originalPoster.getElement().addEventListener("click", e -> {
            try {
                String clickedUsername = originalPosterName;
                String currentUsername = Files.readString(Paths.get("loggedinuser.txt")).trim();

                if (clickedUsername.equals(currentUsername)) {
                    Files.writeString(Paths.get("selecteduser.txt"), currentUsername);
                } else {
                    Files.writeString(Paths.get("selecteduser.txt"), clickedUsername);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            UI.getCurrent().navigate("userpage");
        });

        Span originalTime = new Span("Posted on: " + Post.formatTimestamp(parentPost != null ? parentPost.getTimestamp() : postData.getTimestamp()));
        originalTime.getStyle().set("color", "#ffffff");  // White text
        topRow.add(originalAvatar, originalPoster, originalTime);
        topRow.setWidthFull();
        topRow.setJustifyContentMode(JustifyContentMode.START);
        topRow.setAlignItems(Alignment.CENTER);

        // 💬 Parent post content
        Div originalPostContent = new Div();
        originalPostContent.getStyle()
                .set("white-space", "pre-wrap")
                .set("overflow-wrap", "break-word")
                .set("word-break", "break-word")
                .set("max-width", "750px")
                .set("border", "1px solid #ccc")
                .set("border-radius", "5px")
                .set("padding", "10px")
                .set("margin-bottom", "10px")
                .set("color", "#ffffff");  // White text
        originalPostContent.setText(parentPost != null ? parentPost.getPostContent() : "Original post not found");

        // ❤️ Likes row for parent post
        HorizontalLayout parentLikesRow = new HorizontalLayout();
        if (parentPost != null) {
            parentLikesRow.setWidth("750px");
            parentLikesRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
            parentLikesRow.setAlignItems(Alignment.CENTER);

            Span parentLikesCount = new Span("Liked: " + parentPost.getLikes());
            parentLikesCount.getStyle()
                    .set("font-size", "14px")
                    .set("color", "#ffffff")  // White text
                    .set("white-space", "nowrap");

            HorizontalLayout parentLikeButtonWrapper = new HorizontalLayout();
            parentLikeButtonWrapper.setWidthFull();
            parentLikeButtonWrapper.setJustifyContentMode(JustifyContentMode.END);
            parentLikeButtonWrapper.add(new LikeButton(parentPost));

            parentLikesRow.add(parentLikesCount, parentLikeButtonWrapper);
        }

        // 🧱 Add parent post section
        replyCardLayout.add(topRow, originalPostContent);
        if (parentPost != null) replyCardLayout.add(parentLikesRow);

        // 🔽 Reply input section to reply to the *parent*
        if (parentPost != null) {
            replyCardLayout.add(new UserPost().createReplyInputSection(parentPost));
        }

        // 🔁 Reply meta info
        HorizontalLayout replyMeta = new HorizontalLayout();

        String replyUsername = postData.getUserName();
        String replyAvatarFilename = getAvatarFilenameForUser(replyUsername);
        String replyAvatarUrl = "/avatar/" + replyUsername + "/" + replyAvatarFilename;

        Avatar replyAvatar = new Avatar();
        replyAvatar.setImage(replyAvatarUrl);  // ✅ Correctly use the reply user's image
        replyAvatar.getStyle()
                .set("background-color", "white")
                .set("border", "1px solid #ffffff");

        Span replyUser = new Span(replyUsername);
        replyUser.getStyle()
                .set("color", "#ffffff")
                .set("text-decoration", "underline")
                .set("cursor", "pointer");

        replyUser.getElement().addEventListener("click", e -> {
            try {
                String clickedUsername = replyUsername;
                String currentUsername = Files.readString(Paths.get("loggedinuser.txt")).trim();

                if (clickedUsername.equals(currentUsername)) {
                    Files.writeString(Paths.get("selecteduser.txt"), currentUsername);
                } else {
                    Files.writeString(Paths.get("selecteduser.txt"), clickedUsername);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            UI.getCurrent().navigate("userpage");
        });

        Span replyTime = new Span("Posted on: " + Post.formatTimestamp(postData.getTimestamp()));
        replyTime.getStyle().set("color", "#ffffff");

        replyMeta.add(replyAvatar, replyUser, replyTime);
        replyMeta.setWidthFull();
        replyMeta.setJustifyContentMode(JustifyContentMode.START);
        replyMeta.setAlignItems(Alignment.CENTER);
        replyMeta.getStyle().set("margin-left", "50px");

        // 📝 Reply content
        Div replyContent = new Div();
        replyContent.getStyle()
                .set("white-space", "pre-wrap")
                .set("overflow-wrap", "break-word")
                .set("word-break", "break-word")
                .set("max-width", "750px")
                .set("margin-left", "50px")
                .set("color", "#ffffff");  // White text
        replyContent.setText(postData.getPostContent());

        // 👍 Likes row for reply post
        HorizontalLayout likesRow = new HorizontalLayout();
        likesRow.setWidth("700px");
        likesRow.setAlignItems(Alignment.CENTER);
        likesRow.getStyle().set("margin-left", "50px");

        Span likesCount = new Span("Liked: " + postData.getLikes());
        likesCount.getStyle()
                .set("font-size", "14px")
                .set("color", "#ffffff")  // White text
                .set("white-space", "nowrap");

        HorizontalLayout buttonWrapper = new HorizontalLayout();
        buttonWrapper.setWidthFull();
        buttonWrapper.setJustifyContentMode(JustifyContentMode.END);
        buttonWrapper.add(new LikeButton(postData));

        likesRow.add(likesCount, buttonWrapper);

        // Add the reply content
        replyCardLayout.add(replyMeta, replyContent, likesRow);

        // 🔽 Reply input section to reply to this *reply*
        replyCardLayout.add(new UserPost().createReplyInputSection(postData));

        return replyCardLayout;
    }



    private Component createPostInputCard() {
        VerticalLayout postLayout = new VerticalLayout();
        postLayout.addClassName("hover-card");
        postLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        postLayout.setSpacing(true);
        postLayout.setPadding(true);
        postLayout.getStyle().set("border", "1px solid #ccc");
        postLayout.getStyle().set("border-radius", "10px");
        postLayout.getStyle().set("padding", "20px");
        postLayout.setWidth("800px");
        postLayout.getStyle().set("background-color", "#1a1a1b")  // Dark background
                .set("color", "#ffffff");  // White text
        postLayout.getStyle().set("padding-top", "10px");

        // Get current user
        String currentUsername = getLoggedInUsername();
        User user = User.loadFromFile(currentUsername);

        HorizontalLayout topRow = new HorizontalLayout();

// Build avatar image path
        String avatarFilename = getAvatarFilenameForUser(currentUsername); // same helper method you already use
        String avatarUrl = "/avatar/" + currentUsername + "/" + avatarFilename;

        Avatar avatar = new Avatar();
        avatar.setImage(avatarUrl); // Use image instead of initials

        avatar.getStyle()
                .set("background-color", "white")
                .set("border", "1px solid #ffffff");  // Optional styling



        Span name = new Span(currentUsername);
        name.getStyle().set("color", "#ffffff");  // White text
        name.getStyle().set("font-weight", "bold");
        topRow.add(avatar, name);

        // Second Row: Number of Posts + Likes
        HorizontalLayout statsRow = new HorizontalLayout();
        Span postCount = new Span("Posts: " + user.getPostCount());
        postCount.getStyle().set("color", "#ffffff");  // White text
        Span likeCount = new Span("Likes: " + user.getLikeCount());
        likeCount.getStyle().set("color", "#ffffff");  // White text
        statsRow.add(postCount, likeCount);

        // Third Row: Text Field
        TextArea postArea = new TextArea();
        UserPost userPost = new UserPost();
        postArea.getElement().getStyle()
                .set("background-color", "#1a1a1b")
                .set("color", "#A0B3B6")             // Soft text color
                .set("caret-color", "#d3e3fd")       // Slightly bright caret
                .set("border", "1px solid #6c7a89")  // Rounded border with specified color
                .set("border-radius", "8px")         // Rounded corners (adjust as needed)
                .set("padding", "8px");              // Optional: more breathing room

//#d3e3fd
        userPost.applySimulatedPlaceholder(postArea, "What's on your mind?", "#A0B3B6");

        postArea.setWidthFull();
        postArea.setHeight("120px");
//#6c7a89
        // Fourth Row: Post Button
        Button postButton = new Button("Post", e -> {
            if (!postArea.isEmpty()) {
                UserPost.createAndSaveNewPost(postArea.getValue());
                postArea.clear();
                getUI().ifPresent(ui -> ui.getPage().reload()); // simple reload to refresh posts
            }
        });

        postButton.getStyle()
                .set("background-color", "#E0E0E0")  // light grayish-white
                .set("color", "#333333")             // dark text for contrast
                .set("border", "none")
                .set("border-radius", "4px")
                .set("font-weight", "bold")
                .set("box-shadow", "none");          // keep it flat (dull look)


        postLayout.add(topRow, statsRow, postArea, postButton);
        return postLayout;
    }
    private void styleInputCard(Component card) {
        card.getElement().getStyle()
                .set("width", "800px")
                .set("margin-left", "auto")
                .set("margin-right", "auto")
                .set("margin-top", "15px");
    }

    private String getLoggedInUsername() {
        try {
            return java.nio.file.Files.readString(java.nio.file.Paths.get("loggedinuser.txt")).trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown";
        }
    }
    private void loadPosts() {
        System.out.println("=== loadPosts() START ===");

        User currentUser = User.getCurrentUser();
        if (currentUser == null) {
            System.out.println("No logged-in user found (currentUser == null)");
            return;
        }

        String username = currentUser.getUsername();
        if (username == null || username.isEmpty()) {
            System.out.println("Invalid username.");
            return;
        }
        System.out.println("Current user: " + username);

        // This is the file named "Forum" inside the user folder
        Path forumFilePath = Paths.get(
                "C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users",
                username, "Forum"
        );
        System.out.println("Forum file path: " + forumFilePath);

        String forumName = "";
        try {
            forumName = Files.readString(forumFilePath).trim();
            System.out.println("Forum name read from file content: '" + forumName + "'");
        } catch (IOException e) {
            System.out.println("Failed to read forum file: " + forumFilePath);
            e.printStackTrace();
            return;
        }

        // This is the folder inside /Forum/ that matches the forum name
        Path forumFolder = Paths.get(
                "C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/Forum",
                forumName
        );
        System.out.println("Resolved forum folder path: " + forumFolder);

        if (!Files.isDirectory(forumFolder)) {
            System.out.println("Forum folder not found or is not a directory: " + forumFolder);
            return;
        }

        // Load posts from that forum folder
        if (sortMode == 0) {
            allPosts = UserPost.readPostsFromFiles(forumFolder);
            sortButton.setText("New");
        } else {
            allPosts = UserPost.readPostsSortedByLikes(forumFolder);
            sortButton.setText("Top");
        }

        System.out.println("Loaded " + allPosts.size() + " posts.");

        Component inputCard = createPostInputCard();
        styleInputCard(inputCard);

        List<Object> items = new ArrayList<>();
        items.add(middleBar);
        items.add(inputCard);
        items.addAll(allPosts);

        postList.setItems(items);

        System.out.println("=== loadPosts() END ===");
    }



    private void updateSortButtonHighlight() {
        // Remove existing highlight
        sortNewButton.removeClassName("popup-hover-item-active");
        sortTopButton.removeClassName("popup-hover-item-active");

        // Apply highlight depending on sortMode
        if (sortMode == 0) {
            sortNewButton.addClassName("popup-hover-item-active");
        } else {
            sortTopButton.addClassName("popup-hover-item-active");
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
    public static List<String> getAllForumNames() {
        List<String> forumNames = new ArrayList<>();
        Path forumDirectory = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/Forum");

        try (Stream<Path> paths = Files.list(forumDirectory)) {
            forumNames = paths
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace(); // Or handle as needed
        }

        return forumNames;
    }


}