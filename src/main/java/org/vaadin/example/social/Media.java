package org.vaadin.example.social;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.io.File;
import java.io.IOException;
import com.vaadin.flow.component.notification.Notification;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.UI;

import java.util.*;
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
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;


@Route("media")
public class Media extends VerticalLayout {

    private Button sortNewButton;
    private Button sortTopButton;
    private HorizontalLayout middleBar;
    private int sortMode = 0; // 0 = new, 1 = top
    private VirtualList<Object> postList;
    private List<Post> allPosts;
    private Button sortButton;
    private int notification = 0;



    public Media() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setSpacing(false);
        setMargin(false);
        setPadding(false);
        getStyle().set("background-color", "#1a1a1b");
        String username = getLoggedInUsername();
        String avatarUrl = "/avatar/" + username + "/" + getAvatarFilenameForUser(username);

        VerticalLayout popoverContent = new VerticalLayout();
        popoverContent.addClassName("popover-content");




        Avatar userAvatar2 = new Avatar();
        userAvatar2.setImage(avatarUrl);
        userAvatar2.getStyle()
                .set("background-color", "white")
                .set("border", "1px solid black");

        Span userpageLink = new Span("View Profile");
        userpageLink.getStyle()
                .set("font-weight", "bold")
                .set("font-size", "14px")
                .set("color", "white")
                .set("text-decoration", "none")
                .set("cursor", "pointer");

        userpageLink.getElement().addEventListener("click", event -> {
            try {
                Files.writeString(Paths.get("selecteduser.txt"), username);
            } catch (IOException e) {
                e.printStackTrace();
            }
            UI.getCurrent().navigate(UserPage.class);
        });

       //inside the userInfoLayout is View Profile at the top and the username at the bottom. This is the username
        Div usernameDiv = new Div();
        usernameDiv.setText(username);
        usernameDiv.getStyle()
                .set("font-size", "13px")
                .set("color", "#7e8f96");

        //contains View Profile and username. It is part of the popup on the left
        VerticalLayout userInfoLayout = new VerticalLayout(userpageLink, usernameDiv);
        userInfoLayout.setPadding(false);
        userInfoLayout.setSpacing(false);
        userInfoLayout.setMargin(false);


        Button logoutButton = new Button("Logout", event -> {
            // implement logout logic here
            getUI().ifPresent(ui -> ui.navigate("login"));
        });
        logoutButton.getStyle()
                .set("color", "white")
                .set("font-size", "14px");

        Button avatarCreatingButton = new Button("Upload your own Avatar", event -> {
            // implement logout logic here
            getUI().ifPresent(ui -> ui.navigate("avatarselection"));
        });
        avatarCreatingButton.getStyle().set("color", "white")
                .set("font-size", "14px");

        //popoverContent is the popup on the right when clicking on avatar. secondavatarlayout is View Profile and username (avatarLink, userInfoLayout)
        popoverContent.add(userInfoLayout, avatarCreatingButton, logoutButton);

        //popovercontent is inside popover
        Popover popover = new Popover();
        popover.setTarget(userAvatar2);
        popover.setPosition(PopoverPosition.BOTTOM);
        popover.addThemeVariants(PopoverVariant.ARROW, PopoverVariant.LUMO_NO_PADDING);
        popover.add(popoverContent);  // Add content to the popover only once

        //cCreate a variable to track if the popover is already opened
        boolean[] isOpened = {false};

        // handle the click event to toggle the popover visibility
        userAvatar2.getElement().addEventListener("click", event -> {
            if (!isOpened[0]) {
                // Open the popover if it's not opened
                popover.setOpened(true);
                isOpened[0] = true;

                // seet a timeout to keep the popover open for at least 1 second
                getUI().ifPresent(ui -> ui.access(() -> {
                    // after 1 second, allow the popover to be closed again
                    new Timer().schedule(new TimerTask() {
                        public void run() {

                            if (popover.isOpened()) {

                            }
                        }
                    }, 1000);
                }));
            } else {

                popover.setOpened(false);
                isOpened[0] = false;
            }
        });

        //notification bell icon
        Icon notificationBell = new Icon(VaadinIcon.BELL);
        notificationBell.setSize("30px");
        notificationBell.getElement().getStyle().set("color", "white");

        //overlay with notification count
        Span notificationCount = new Span();
        int notifNumber = 0;

        try {
            Path notifPath = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users", username, "NotificationNumber");

            if (Files.exists(notifPath)) {
                String countStr = Files.readString(notifPath).trim();
                notifNumber = countStr.isEmpty() ? 0 : Integer.parseInt(countStr);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        notificationCount.setText(String.valueOf(notifNumber));
        notificationCount.getElement().getStyle()
                .set("position", "absolute")
                .set("top", "1px")
                .set("right", "-13px")
                .set("background-color", "red")
                .set("color", "white")
                .set("border-radius", "45%")
                .set("padding", "2px 6px")
                .set("font-size", "10px")
                .set("min-width", "18px")
                .set("height", "18px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");

        // Turn the bell into a button
        Button notificationButton = new Button(notificationBell);
        notificationButton.addClassName("notification-bell");
        notificationButton.getStyle()
                .set("position", "relative")
                .set("padding", "0")
                .set("border", "none")
                .set("background", "none");

        //Wrapper around notificationbellbell and notificationcount
        Div bellWrapper = new Div();
        bellWrapper.add(notificationButton);
        if (notifNumber > 0) {
            bellWrapper.add(notificationCount); // only add count if > 0
        }
        bellWrapper.getStyle().set("position", "relative").set("width", "fit-content");

// dropdown on notification bell
        ContextMenu notificationMenu = new ContextMenu(notificationButton);
        notificationMenu.getElement().setAttribute("theme", "notification");
        notificationMenu.getElement().getStyle().set("background-color", "#000000");
        notificationMenu.setOpenOnClick(true);

// build menu items from notification previews
        UserPost userPost = new UserPost();
        userPost.buildNotificationMenu(notificationMenu, this);
        userPost.showNotificationCount(notificationCount);


// path to the users folder
        Path usersDir = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users");

// list to store usernames (folder names)
        List<String> suggestions = new ArrayList<>();

        try (Stream<Path> paths = Files.list(usersDir)) {
            paths.filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .forEach(suggestions::add);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
                .set("position", "relative");

// add value change listener to simulate autocomplete
        searchField.addValueChangeListener(event -> {
            String typed = event.getValue();
            if (typed == null || typed.isEmpty()) {
                return;
            }

            // find first match that starts with typed value (case-insensitive)
            Optional<String> match = suggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(typed.toLowerCase()))
                    .findFirst();

            match.ifPresent(firstMatch -> {
                if (!typed.equalsIgnoreCase(firstMatch)) {
                    searchField.setValue(firstMatch);

                    searchField.getElement().executeJs(
                            "this.setSelectionRange($0, $1);",
                            typed.length(),
                            firstMatch.length()
                    );
                }
        });


        });


        ListBox<String> dropdown = new ListBox<>();
        dropdown.setVisible(false);
        dropdown.setWidthFull();
        dropdown.getStyle()
                .set("position", "absolute")
                .set("top", "100%")
                .set("left", "0")
                .set("background-color", "#2c2f33")
                .set("color", "white")
                .set("border-radius", "10px")
                .set("z-index", "1")
                .set("box-shadow", "0 4px 8px rgba(0,0,0,0.3)");

        Div wrapper = new Div();
        wrapper.getStyle()
                .set("position", "relative")
                .set("display", "inline-block");

        wrapper.add(searchField, dropdown);

// load username
        File usersDir2 = new File("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users");
        List<String> usernames = Optional.ofNullable(usersDir2.listFiles(File::isDirectory))
                .map(files -> Arrays.stream(files)
                        .map(File::getName)
                        .sorted()
                        .toList())
                .orElse(List.of());

// filter
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

                try {
                    Path filePath = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/selecteduser.txt");
                    Files.writeString(filePath, selectedUser, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }


                UI.getCurrent().navigate("userpage");

                dropdown.setVisible(false);
            }
        });

        // semaino title
        Span clickableTitle = new Span("Semaino");
        clickableTitle.addClickListener(e -> {
            loadPosts();
            UI.getCurrent().navigate(Media.class);
        });        clickableTitle.getStyle()
                .set("font-family", "'Segoe Script', cursive")
                .set("font-size", "28px")
                .set("font-weight", "bold")
                .set("color", "#FFFFFF")
                .set("text-decoration", "none")
                .set("cursor", "pointer")
                .setWidth("179px");



        // create root layout
        HorizontalLayout rootLayout = new HorizontalLayout();
        rootLayout.setWidthFull();
        rootLayout.setPadding(false);
        rootLayout.setMargin(false);


        // apply baby blue background without affecting text color
        rootLayout.getStyle()
                .set("border-bottom", "1px solid #666")
                .set("background-color", "#1a1a1b")
                .set("color", "#ffffff");


        // sub-layouts for left, center, and right
        HorizontalLayout leftLayout = new HorizontalLayout(clickableTitle);
        leftLayout.setJustifyContentMode(JustifyContentMode.START);
        leftLayout.setWidthFull();
        leftLayout.getElement().getStyle().set("margin-left", "20px");
        leftLayout.getStyle().set("color", "#333");


        HorizontalLayout centerLayout = new HorizontalLayout(wrapper);
        centerLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        centerLayout.setAlignItems(Alignment.CENTER);
        centerLayout.setWidthFull();
        centerLayout.getStyle().set("color", "#333");
        HorizontalLayout rightLayout = new HorizontalLayout(bellWrapper, userAvatar2);
        rightLayout.setJustifyContentMode(JustifyContentMode.END);
        rightLayout.setAlignItems(Alignment.CENTER);
        rightLayout.setWidthFull();
        rightLayout.getElement().getStyle().set("margin-right", "20px");
        rightLayout.getStyle().set("color", "#333");

        // Add all sub-layouts to root
        rootLayout.add(leftLayout, centerLayout, rightLayout);
        rootLayout.setFlexGrow(1, leftLayout, centerLayout, rightLayout);

        // create the input card ONCE. this is the first element of the list.
        Component inputCard = createPostInputCard();  // Create the input card for adding posts or replies.
        inputCard.getElement().getStyle().set("width", "800px");
        inputCard.getElement().getStyle().set("margin-left", "auto");
        inputCard.getElement().getStyle().set("margin-right", "auto");
        inputCard.getElement().getStyle().set("margin-top", "15px");

        // sort button
        sortButton = new Button("New");
        sortButton.addClassName("glow-hover");
        sortButton.getStyle()
                .set("margin", "0")
                .set("padding", "0")
                .set("height", "20px")
                .set("color", "#686b6e")
                .set("font-size", "13px");

        Div sortButtonWrapper = new Div();
        sortButtonWrapper.addClassName("elliptical-glow-wrapper");

// add the sortButton inside the wrapper
        sortButtonWrapper.add(sortButton);
//sort popover
        Popover sortPopoverPopup = new Popover();
        sortPopoverPopup.addClassName("glow-hover");
        sortPopoverPopup.setTarget(sortButton);
        sortPopoverPopup.setPosition(PopoverPosition.BOTTOM);
        sortPopoverPopup.addThemeVariants(PopoverVariant.ARROW, PopoverVariant.LUMO_NO_PADDING);

// content popover
        VerticalLayout sortPopoverContent = new VerticalLayout();
        sortPopoverContent.setWidthFull();
        sortPopoverContent.setPadding(false);
        sortPopoverContent.setSpacing(false);
        sortPopoverContent.getStyle()
                .set("background-color", "#282b30")
                .set("color", "#ffffff")
                .set("min-width", "120px")
                .set("text-align", "left")
                .set("border-radius", "8px");


        Div sortHeader = new Div();
        sortHeader.setText("Sort by:");
        sortHeader.getStyle()
                .set("font-weight", "bold")
                .set("color", "#686b6e")
                .set("text-align", "center")
                .set("width", "100%");

// sort options
        sortNewButton = new Button("New");
        sortNewButton.setWidthFull();
        sortNewButton.addClassName("popup-hover-item");
        sortNewButton.getStyle().set("width", "100%")
                .set("color", "#D7DADC");


        sortTopButton = new Button("Top");
        sortTopButton.setWidthFull();
        sortTopButton.addClassName("popup-hover-item");
        sortTopButton.getStyle().set("width", "100%")
                .set("color", "#D7DADC");

// popover
        sortPopoverContent.add(sortHeader, sortNewButton, sortTopButton);
        sortPopoverPopup.add(sortPopoverContent);

        boolean[] isSortPopoverOpen = {false};

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


// create the horizontal layout wrapper and center the button
        middleBar = new HorizontalLayout(sortButtonWrapper);        middleBar.setWidth("800px");
        middleBar.setHeight("20px");
        middleBar.getElement().getStyle().set("margin-left", "auto");
        middleBar.getElement().getStyle().set("margin-right", "auto");
        middleBar.getElement().getStyle().set("margin-top", "10px");
        middleBar.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        middleBar.setPadding(false);
        middleBar.getStyle()
                .set("background-color", "#1a1a1b");



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

        //I made this with part with chatgpt. Dont delete comment until I've went over everything
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

                card.getElement().getStyle().set("margin", "0 auto");
                card.getElement().getStyle().set("width", "800px");
                card.getElement().getStyle().set("margin-top", "10px");
                return card;
            } else {
                return new Span("Unknown item");
            }
        }));

        postList.setWidthFull();
        postList.setHeightFull();

        // wrapper layout for holding the content
        VerticalLayout content = new VerticalLayout();
        content.setWidthFull();
        content.setHeight("100%");
        content.setPadding(false);
        content.setSpacing(true);
        content.setDefaultHorizontalComponentAlignment(Alignment.CENTER);


        content.add(postList);
        content.setFlexGrow(1, postList);

        HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeFull();
        layout.setAlignItems(Alignment.CENTER);
        layout.getStyle().set("position", "relative");

// sidebar
        VerticalLayout sideBar = new VerticalLayout();
        sideBar.setHeightFull();
        sideBar.setWidth("200px");
        sideBar.getStyle()
                .set("bottom", "0")
                .set("left", "0")
                .set("border-right", "1px solid #666")
                .set("background-color", "#1a1a1b")
                .set("color", "#FFFFFF");

// home icon
        Icon homeIcon = VaadinIcon.BUILDING.create();
        Span homeText = new Span("Home");
        HorizontalLayout homeLayout = new HorizontalLayout(homeIcon, homeText);
        homeLayout.getStyle().set("cursor", "pointer");
        homeLayout.addClickListener(event -> UI.getCurrent().navigate("media"));

        Icon popularIcon = VaadinIcon.LINE_CHART.create();
        Span popularText = new Span("Popular");
        HorizontalLayout popularLayout = new HorizontalLayout(popularIcon, popularText);

        Icon forYouIcon = VaadinIcon.SEARCH.create();
        Span forYouText = new Span("Recommended");
        HorizontalLayout forYouLayout = new HorizontalLayout(forYouIcon, forYouText);

        Icon allIcon = VaadinIcon.GLOBE.create();
        Span allText = new Span("All");
        HorizontalLayout allLayout = new HorizontalLayout(allIcon, allText);

        allLayout.getStyle().set("cursor", "pointer");

// navigates to forum "all"
        allLayout.addClickListener(event -> {
            try {
                // save "all" as selected forum
                Path userForumFile = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users", username, "Forum");
                Files.writeString(userForumFile, "all", StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);


                UI.getCurrent().getPage().reload();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });


// a cintainer for the div that says All Pages
        Div containerDiv = new Div();
        containerDiv.setText("All Pages:");
        containerDiv.getStyle()
                .set("margin-top", "20px")
                .set("font-weight", "bold")
                .set("color", "white");

        //a container for the Div that says Current Page:
        Div containerDiv2 = new Div();
        containerDiv2.setText("Current Page:");
        containerDiv2.getStyle()
                .set("margin-top", "20px")
                .set("font-weight", "bold")
                .set("color", "white");


        //this is the dropdown menu for Current Page: When you click on somethin it opens a dropdown of all subscribed pages
        Div pageSelectionPopup = new Div();
        pageSelectionPopup.getStyle()
                .set("background-color", "#282b30")
                .set("color", "#e0e0e0")
                .set("border", "1px solid #444")
                .set("border-radius", "4px")
                .set("padding", "10px")
                .set("margin-top", "4px")
                .set("width", "100%")
                .set("box-sizing", "border-box")
                .set("display", "none")
                .set("z-index", "11")
                .set("max-height", "300px")
                .set("overflow-y", "auto");

        Div popupWrapper = new Div();
        popupWrapper.getStyle()
                .set("position", "relative")
                .set("width", "100%");
        popupWrapper.add(pageSelectionPopup);

// title for selecting forum
        H4 dialogTitle = new H4("Select a forum");
        dialogTitle.getStyle().set("color", "white");
        pageSelectionPopup.add(dialogTitle);

// vertical layout for forum buttons
        VerticalLayout pageListLayout = new VerticalLayout();
        pageListLayout.setPadding(false);
        pageListLayout.setSpacing(false);
        pageListLayout.setMargin(false);
        pageSelectionPopup.add(pageListLayout);


// title for forum creation
        H4 createTitle = new H4("Create new forum");
        createTitle.getStyle()
                .set("color", "white")
                .set("font-size", "14px")
                .set("margin", "16px 0 4px 0");



        TextField forumNameField = new TextField();
        forumNameField.setPlaceholder("");
        forumNameField.setWidthFull();
        forumNameField.getElement().getStyle()
                .set("--lumo-text-field-size", "25px")
                .set("font-size", "12px")
                .set("line-height", "25px")
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

                //  Update the button label here
                openPopupButton.setText(forumName);

                //  Write username as file inside Forum/<forumName>/Admin/<username>
                Path forumAdminDir = newForumPath.resolve("Admin");
                Files.createDirectories(forumAdminDir);
                Path userAdminFile = forumAdminDir.resolve(username);
                Files.writeString(userAdminFile, "", StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);

                // Also add the forum name as a text file to every user's Administrator folder
                try (Stream<Path> userFolders = Files.list(usersDir)) {
                    userFolders.filter(Files::isDirectory).forEach(userPath -> {
                        Path adminDir = userPath.resolve("Administrator");
                        if (Files.exists(adminDir) && Files.isDirectory(adminDir)) {
                            Path forumFile = adminDir.resolve(forumName);
                            try {
                                Files.writeString(forumFile, "", StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
                            } catch (IOException ex) {
                                System.err.println("Failed to create forum file for user: " + userPath.getFileName());
                                ex.printStackTrace();
                            }
                        }
                    });
                } catch (IOException ex) {
                    System.err.println("Error accessing users directory:");
                    ex.printStackTrace();
                }

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
        forumDropdown.setWidth("165px");
        forumDropdown.getStyle()
                .set("height", "42px")
                .set("--lumo-size", "42px")
                .set("font-size", "12px")
                .set("line-height", "36px")
                .set("padding", "0 8px")
                .set("border", "1px solid #444")
                .set("border-radius", "4px")
                .set("background-color", "#1a1a1b")
                .set("color", "white")
                .set("box-sizing", "border-box")
                .set("text-align", "center")
                .set("margin", "10px 0");


        Select<String> select = new Select<>();
        select.setItems("Option 1", "Option 2", "Option 3");
        select.setPlaceholder("Select a forum");
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
                    loadPosts();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Div dropdownWrapper = new Div(forumDropdown);
        dropdownWrapper.getStyle()
                .set("position", "relative")
                .set("z-index", "10001");

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
                .set("height", "60px")
                .set("white-space", "normal")
                .set("word-break", "break-word")
                .set("max-width", "200px")
                .set("padding", "10px 16px")
                .set("outline", "none")
                .set("box-shadow", "none");


        subscribeButton.addClickListener(event -> {
            try {

                Path followedForumsDir = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users", username, "Followed Forums");
                Files.createDirectories(followedForumsDir);

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
        spacer.setHeightFull();
        sideBar.setFlexGrow(1, spacer);

        Div spacer2 = new Div();
        spacer.setHeightFull();
        sideBar.setFlexGrow(1, spacer);

        Div spacer3 = new Div();
        spacer.setHeightFull();
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
                popupWrapper,
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
                            pageSelectionPopup.getStyle().set("display", "none");
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




// filler
        VerticalLayout filler = new VerticalLayout();
        filler.setHeightFull();
        filler.setWidth("200px");
        filler.setAlignItems(Alignment.END);

        filler.getStyle()
                .set("bottom", "0")
                .set("right", "0")
                .set("background-color", "#1a1a1b")
                .set("border-left", "1px solid #444");

        VerticalLayout innerLayout = new VerticalLayout();
        innerLayout.setWidthFull();
        innerLayout.setPadding(false);
        innerLayout.setSpacing(true);
        innerLayout.setAlignItems(Alignment.CENTER);

// 1. Div to show forumName (using a simple Label with styling)
        Div forumNameDiv = new Div();
        forumNameDiv.setText(forumname);
        forumNameDiv.getStyle()
                .set("font-weight", "bold")
                .set("font-size", "1.2em")
                .set("color", "white");

// 2. Read-only TextField to display forum summary from Summary file
        TextArea forumSummaryField = new TextArea();
        forumSummaryField.setWidthFull();
        forumSummaryField.setReadOnly(true);
        forumSummaryField.getStyle()
                .set("border", "1px solid white")
                .set("color", "white")
                .setHeight("150px");


        Path summaryFile = Paths.get(
                "C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/Forum",
                forumname,
                "Descriptors",
                "Summary"
        );

        if (Files.exists(summaryFile)) {
            try {
                String summaryContent = Files.readString(summaryFile);
                forumSummaryField.setValue(summaryContent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

// 3. Check if user is admin by verifying if the folder exists
        boolean isAdmin = false;
        try {
            Path adminPath = Paths.get(
                    "C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users",
                    username, "Administrator", forumname
            );
            System.out.println("Checking admin folder exists: " + adminPath.toString());
            System.out.println("Exists: " + Files.exists(adminPath));
            System.out.println("Is directory: " + Files.isDirectory(adminPath));
            isAdmin = Files.exists(adminPath);
        } catch (Exception e) {
            e.printStackTrace();
        }

// 4. If admin, add Edit button below
        Button editButton = new Button("Edit", event -> {
            getUI().ifPresent(ui -> ui.navigate("admin"));
        });
        editButton.setVisible(isAdmin);
        editButton.getStyle().set("background-color", "white");
        editButton.getStyle().set("color", "black");
        editButton.getStyle().set("border", "1px solid black");

// Add components to inner layout
        innerLayout.add(forumNameDiv, forumSummaryField);
        if (isAdmin) {
            innerLayout.add(editButton);
        }

// Finally, add the inner layout to your filler layout
        filler.add(innerLayout);

// content
        layout.add(sideBar, content, filler);
        layout.setFlexGrow(1, content);

// Add overlays after layout
        add(rootLayout, layout);

    }

    //Helper method. The parameter name doesnt matter. It could be banana for all i care
     String getAvatarFilenameForUser(String username) {
        Path avatarDir = Paths.get("users", username, "Avatar");

        if (Files.exists(avatarDir) && Files.isDirectory(avatarDir)) {
            try (Stream<Path> files = Files.list(avatarDir)) {
                return files
                        .filter(Files::isRegularFile)
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .findFirst()
                        .orElse("default.png");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "default.png";
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
                .set("border-radius", "10px")
                .set("padding", "10px")
                .set("margin", "0")
                .set("background-color", "#1a1a1b")
                .set("color", "#ffffff");

        commentCardLayout.setWidth("800px");

        // Top: Avatar + User Name + Posted on: Date
        HorizontalLayout topRow = new HorizontalLayout();
        // Get the username from post data
        String username = postData.getUserName();

        String avatarUrl = "/avatar/" + username + "/" + getAvatarFilenameForUser(username);

        Avatar userAvatar = new Avatar();
        userAvatar.setImage(avatarUrl);

        userAvatar.getStyle()
                .set("background-color", "white")
                .set("border", "1px solid #ffffff");

        Span userName = new Span(username);
        userName.getStyle()
                .set("color", "#ffffff")
                .set("text-decoration", "underline")
                .set("cursor", "pointer");

        userName.getElement().addEventListener("click", event -> {
            try {
                String clickedUsername = username;

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


        Span commentTime = new Span("Posted on: " + Post.formatTimestamp(postData.getTimestamp()));
        commentTime.getStyle().set("color", "#ffffff");

        topRow.add(userAvatar, userName, commentTime);
        topRow.setWidthFull();
        topRow.setJustifyContentMode(JustifyContentMode.START);
        topRow.setAlignItems(Alignment.CENTER);


        Div commentContent = new Div();
        commentContent.setWidthFull();

        commentContent.getStyle().set("text-align", "left");
        commentContent.getStyle()
                .set("white-space", "pre-wrap")
                .set("overflow-wrap", "break-word")
                .set("word-break", "break-word")
                .set("max-width", "750px")
                .set("color", "#ffffff");
        commentContent.setText(postData.getPostContent());

        HorizontalLayout likesRow = new HorizontalLayout();
        likesRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
        likesRow.setAlignItems(Alignment.CENTER);
        likesRow.getStyle()
                .set("width", "725px"); //

        Span likesCount = new Span("Liked: " + postData.getLikes());
        likesCount.getStyle()
                .set("font-size", "14px")
                .set("color", "#ffffff")  // White text
                .set("white-space", "nowrap");

        LikeButton likeButton = new LikeButton(postData);
        likeButton.getStyle()
                .set("background-color", "white")
                .set("color", "black")
                .set("border", "1px solid #ffffff");
        likesRow.add(likesCount, likeButton);

        commentCardLayout.add(topRow, commentContent, likesRow);

        UserPost userPostInstance = new UserPost();
        commentCardLayout.add(userPostInstance.createReplyInputSection(postData));

        return commentCardLayout;
    }

    // Define the Reply Card
    public VerticalLayout createReplyCard(Post postData) {
        VerticalLayout replyCardLayout = new VerticalLayout();
        replyCardLayout.addClassName("hover-card");

        replyCardLayout.setAlignItems(Alignment.START);
        replyCardLayout.setSpacing(true);
        replyCardLayout.setPadding(true);
        replyCardLayout.setWidth("800px");
        replyCardLayout.getStyle()
                .set("border", "1px solid #ccc")
                .set("border-radius", "10px")
                .set("padding", "10px")
                .set("background-color", "#1a1a1b")
                .set("color", "#ffffff");

        Post parentPost = UserPost.findPostById(postData.getParentId());

        HorizontalLayout topRow = new HorizontalLayout();
        String originalUsername = parentPost != null ? parentPost.getUserName() : "Unknown";
        String avatarFilename = getAvatarFilenameForUser(originalUsername);
        String avatarUrl = "/avatar/" + originalUsername + "/" + avatarFilename;

        Avatar originalAvatar = new Avatar();
        originalAvatar.setImage(avatarUrl);
        originalAvatar.getStyle()
                .set("background-color", "white")
                .set("border", "1px solid #ffffff");

        Span originalPoster = new Span(originalUsername);
        originalPoster.getStyle()
                .set("color", "#ffffff")
                .set("text-decoration", "underline")
                .set("cursor", "pointer");

        originalPoster.getElement().addEventListener("click", e -> {
            try {
                String clickedUsername = originalUsername;
                String currentUsername = Files.readString(Paths.get("loggedinuser.txt")).trim();
                Files.writeString(Paths.get("selecteduser.txt"), clickedUsername.equals(currentUsername) ? currentUsername : clickedUsername);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            UI.getCurrent().navigate("userpage");
        });

        Span originalTime = new Span("Posted on: " + Post.formatTimestamp(parentPost != null ? parentPost.getTimestamp() : postData.getTimestamp()));
        originalTime.getStyle().set("color", "#ffffff");

        topRow.add(originalAvatar, originalPoster, originalTime);
        topRow.setWidthFull();
        topRow.setJustifyContentMode(JustifyContentMode.START);
        topRow.setAlignItems(Alignment.CENTER);

        //  Parent post content
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
                .set("color", "#ffffff");
        originalPostContent.setText(parentPost != null ? parentPost.getPostContent() : "Original post not found");

        // Likes row for parent post
        HorizontalLayout parentLikesRow = new HorizontalLayout();
        if (parentPost != null) {
            parentLikesRow.setWidth("750px");
            parentLikesRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
            parentLikesRow.setAlignItems(Alignment.CENTER);

            Span parentLikesCount = new Span("Liked: " + parentPost.getLikes());
            parentLikesCount.getStyle()
                    .set("font-size", "14px")
                    .set("color", "#ffffff")
                    .set("white-space", "nowrap");

            HorizontalLayout parentLikeButtonWrapper = new HorizontalLayout();
            parentLikeButtonWrapper.setWidthFull();
            parentLikeButtonWrapper.setJustifyContentMode(JustifyContentMode.END);
            parentLikeButtonWrapper.add(new LikeButton(parentPost));

            parentLikesRow.add(parentLikesCount, parentLikeButtonWrapper);
        }

        replyCardLayout.add(topRow, originalPostContent);
        if (parentPost != null) replyCardLayout.add(parentLikesRow);
        if (parentPost != null) {
            replyCardLayout.add(new UserPost().createReplyInputSection(parentPost));
        }

        // Reply meta info
        HorizontalLayout replyMeta = new HorizontalLayout();
        String replyUsername = postData.getUserName();
        String replyAvatarFilename = getAvatarFilenameForUser(replyUsername);
        String replyAvatarUrl = "/avatar/" + replyUsername + "/" + replyAvatarFilename;

        Avatar replyAvatar = new Avatar();
        replyAvatar.setImage(replyAvatarUrl);
        replyAvatar.getStyle()
                .set("background-color", "white")
                .set("border", "1px solid #ffffff");

        // Small status dot (always online for now)
        Span statusDot = new Span();
        statusDot.getStyle()
                .set("background-color", "limegreen")
                .set("border-radius", "50%")
                .set("width", "10px")
                .set("height", "10px")
                .set("display", "inline-block")
                .set("margin-left", "5px");

        Span replyUser = new Span(replyUsername);
        replyUser.getStyle()
                .set("color", "#ffffff")
                .set("text-decoration", "underline")
                .set("cursor", "pointer");

        Div userWrapper = new Div(replyUser, statusDot);
        userWrapper.getElement().setProperty("title", "Online");

        replyUser.getElement().addEventListener("click", e -> {
            try {
                String clickedUsername = replyUsername;
                String currentUsername = Files.readString(Paths.get("loggedinuser.txt")).trim();
                Files.writeString(Paths.get("selecteduser.txt"), clickedUsername.equals(currentUsername) ? currentUsername : clickedUsername);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            UI.getCurrent().navigate("userpage");
        });

        Span replyTime = new Span("Posted on: " + Post.formatTimestamp(postData.getTimestamp()));
        replyTime.getStyle().set("color", "#ffffff");

        replyMeta.add(replyAvatar, userWrapper, replyTime);
        replyMeta.setWidthFull();
        replyMeta.setJustifyContentMode(JustifyContentMode.START);
        replyMeta.setAlignItems(Alignment.CENTER);
        replyMeta.getStyle().set("margin-left", "50px");

        // Reply content
        Div replyContent = new Div();
        replyContent.getStyle()
                .set("white-space", "pre-wrap")
                .set("overflow-wrap", "break-word")
                .set("word-break", "break-word")
                .set("max-width", "750px")
                .set("margin-left", "50px")
                .set("color", "#ffffff");
        replyContent.setText(postData.getPostContent());

        // Likes row for reply post
        HorizontalLayout likesRow = new HorizontalLayout();
        likesRow.setWidth("700px");
        likesRow.setAlignItems(Alignment.CENTER);
        likesRow.getStyle().set("margin-left", "50px");

        Span likesCount = new Span("Liked: " + postData.getLikes());
        likesCount.getStyle()
                .set("font-size", "14px")
                .set("color", "#ffffff")
                .set("white-space", "nowrap");

        HorizontalLayout buttonWrapper = new HorizontalLayout();
        buttonWrapper.setWidthFull();
        buttonWrapper.setJustifyContentMode(JustifyContentMode.END);
        buttonWrapper.add(new LikeButton(postData));

        likesRow.add(likesCount, buttonWrapper);

        // Add the reply content
        replyCardLayout.add(replyMeta, replyContent, likesRow);

        // Reply input section to reply to this *reply*
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
        postLayout.getStyle().set("background-color", "#1a1a1b")
                .set("color", "#ffffff");
        postLayout.getStyle().set("padding-top", "10px");

        // Get current user
        String currentUsername = getLoggedInUsername();
        User user = User.loadFromFile(currentUsername);

        HorizontalLayout topRow = new HorizontalLayout();

// Build avatar image path
        String avatarFilename = getAvatarFilenameForUser(currentUsername);
        String avatarUrl = "/avatar/" + currentUsername + "/" + avatarFilename;

        Avatar avatar = new Avatar();
        avatar.setImage(avatarUrl);

        avatar.getStyle()
                .set("background-color", "white")
                .set("border", "1px solid #ffffff");



        Span name = new Span(currentUsername);
        name.getStyle().set("color", "#ffffff");
        name.getStyle().set("font-weight", "bold");
        topRow.add(avatar, name);

        // Second Row: Number of Posts + Likes
        HorizontalLayout statsRow = new HorizontalLayout();
        Span postCount = new Span("Posts: " + user.getPostCount());
        postCount.getStyle().set("color", "#ffffff");
        Span likeCount = new Span("Likes: " + user.getLikeCount());
        likeCount.getStyle().set("color", "#ffffff");
        statsRow.add(postCount, likeCount);

        // Third Row: Text Field
        TextArea postArea = new TextArea();
        UserPost userPost = new UserPost();
        postArea.getElement().getStyle()
                .set("background-color", "#1a1a1b")
                .set("color", "#A0B3B6")
                .set("caret-color", "#d3e3fd")
                .set("border", "1px solid #6c7a89")
                .set("border-radius", "8px")
                .set("padding", "8px");

        userPost.applySimulatedPlaceholder(postArea, "What's on your mind?", "#A0B3B6");

        postArea.setWidthFull();
        postArea.setHeight("120px");
        // Fourth Row: Post Button
        Button postButton = new Button("Post", e -> {
            if (!postArea.isEmpty()) {
                UserPost.createAndSaveNewPost(postArea.getValue());
                postArea.clear();
                getUI().ifPresent(ui -> ui.getPage().reload());
            }
        });

        postButton.getStyle()
                .set("background-color", "#E0E0E0")
                .set("color", "#333333")
                .set("border", "none")
                .set("border-radius", "4px")
                .set("font-weight", "bold")
                .set("box-shadow", "none");


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
    private Component createUsernameWithHoverStatus(String username) {
        // Username display
        Span usernameSpan = new Span(username);
        usernameSpan.getElement().setAttribute("data-username", username);

        // Tooltip container
        Div statusTooltip = new Div();
        statusTooltip.getStyle()
                .set("position", "absolute")
                .set("background-color", "#333")
                .set("color", "#fff")
                .set("padding", "5px 10px")
                .set("border-radius", "4px")
                .set("font-size", "12px")
                .set("display", "none")
                .set("z-index", "1000");

        // Show tooltip on hover
        usernameSpan.getElement().addEventListener("mouseenter", e -> {
            String hoveredUsername = usernameSpan.getElement().getAttribute("data-username");
            Path statusPath = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users/" + hoveredUsername + "/Status.txt");

            String statusText;
            try {
                statusText = Files.readString(statusPath);
            } catch (IOException ex) {
                statusText = "No status available";
            }

            statusTooltip.setText(statusText);
            statusTooltip.getStyle().set("display", "block");
        });

        // Hide tooltip on mouse leave
        usernameSpan.getElement().addEventListener("mouseleave", e -> {
            statusTooltip.getStyle().set("display", "none");
        });

        // Wrap both elements
        HorizontalLayout wrapper = new HorizontalLayout(usernameSpan, statusTooltip);
        wrapper.setSpacing(true);
        wrapper.getStyle().set("position", "relative");

        return wrapper;
    }


    public String getLoggedInUsername() {
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

        Path forumFolder = Paths.get(
                "C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/Forum",
                forumName
        );
        System.out.println("Resolved forum folder path: " + forumFolder);

        if (!Files.isDirectory(forumFolder)) {
            System.out.println("Forum folder not found or is not a directory: " + forumFolder);
            return;
        }

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

        // Here is the list of all forums (folder names) under Forum
        Path baseForumPath = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/Forum");
        try (Stream<Path> forumFolders = Files.list(baseForumPath)) {
            System.out.println("All available forums:");
            forumFolders
                    .filter(Files::isDirectory)
                    .forEach(path -> System.out.println(" - " + path.getFileName()));
        } catch (IOException e) {
            System.out.println("Failed to list forums in base forum folder.");
            e.printStackTrace();
        }

        System.out.println("=== loadPosts() END ===");
    }

    private void updateSortButtonHighlight() {
        sortNewButton.removeClassName("popup-hover-item-active");
        sortTopButton.removeClassName("popup-hover-item-active");

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
            e.printStackTrace();
        }

        return forumNames;
    }
    public void loadPostFromNotification(Path notificationFilePath) {
        try {
            List<String> lines = Files.readAllLines(notificationFilePath);
            if (lines.size() < 2) {
                System.err.println("Invalid notification file format.");
                return;
            }

            String postDataLine = lines.get(0).trim();
            Post referencedPost = Post.fromString(postDataLine);

            String senderUsername = referencedPost.getUserName();

            int postNumber = Integer.parseInt(lines.get(1).trim());

            Path postFilePath = Paths.get("users", senderUsername, "Posts", String.valueOf(postNumber));

            if (!Files.exists(postFilePath)) {
                System.err.println("Referenced post not found: " + postFilePath);
                return;
            }

            String actualPostLine = Files.readString(postFilePath).trim();
            Post actualPost = Post.fromString(actualPostLine);

            Component replyCard = createReplyCard(actualPost);
            replyCard.getElement().getStyle()
                    .set("margin", "0 auto")
                    .set("width", "800px")
                    .set("margin-top", "20px");

            Component inputCard = createPostInputCard();
            styleInputCard(inputCard);
            List<Object> items = new ArrayList<>();
            items.add(middleBar);
            items.add(inputCard);
            items.add(replyCard);

            postList.setItems(items);

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }


}



