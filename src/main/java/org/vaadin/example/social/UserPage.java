package org.vaadin.example.social;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Route("userpage")  // Defines the route for this view. When navigating to '/media', this view is displayed.
public class UserPage extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(Media.class);

    private String selectedUsername;
    private User selectedUser;
    private int likes;
    private List<String> followers = new ArrayList<>();
    private List<String> subscriptions = new ArrayList<>();



    private Button sortNewButton;
    private Button sortTopButton;
    private HorizontalLayout middleBar;
    private int sortMode = 0; // 0 = new, 1 = top
    private VirtualList<Object> postList;
    private List<Post> allPosts;
    private Button sortButton;

    public UserPage() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setSpacing(false);
        setMargin(false);
        setPadding(false);
        getStyle().set("background-color", "#1a1a1b");

        String username = getLoggedInUsername();

        Media avatarService = new Media();
        String avatarUrl = "/avatar/" + username + "/" + avatarService.getAvatarFilenameForUser(username);

        try {
            selectedUsername = Files.readString(Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/selecteduser.txt")).trim();
         //   selectedUsername = Files.readString(Paths.get("C:\\Users\\0\\IdeaProjects\\VaadinSocialMediaUpload\\selecteduser.txt")).trim();

            if (!selectedUsername.isEmpty()) {
                selectedUser = User.loadFromFile(selectedUsername);
                if (selectedUser != null) {
                    username = selectedUser.getUsername();

                    Path followBasePath = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users", username, "Follow");
                   // Path followBasePath = Paths.get("C:\\Users\\0\\IdeaProjects\\VaadinSocialMediaUpload\\users", username, "Follow");


                    Path followingPath = followBasePath.resolve("Following");
                    Path followedByPath = followBasePath.resolve("FollowedBy");

                    if (Files.exists(followingPath)) {
                        try (DirectoryStream<Path> stream = Files.newDirectoryStream(followingPath)) {
                            for (Path file : stream) {
                                subscriptions.add(file.getFileName().toString().replace(".txt", ""));
                            }
                        }
                    }

                    if (Files.exists(followedByPath)) {
                        try (DirectoryStream<Path> stream = Files.newDirectoryStream(followedByPath)) {
                            for (Path file : stream) {
                                followers.add(file.getFileName().toString().replace(".txt", ""));
                            }
                        }
                    }

                    System.out.println("Followers: " + followers);
                    System.out.println("Subscriptions: " + subscriptions);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        VerticalLayout popoverContent = new VerticalLayout();
        popoverContent.getStyle()
                .set("background-color", "#282b30")
                .set("border-radius", "16px")
                .set("overflow", "hidden");

        Avatar userAvatar2 = new Avatar();
        userAvatar2.setImage(avatarUrl);
        userAvatar2.getStyle()
                .set("background-color", "white")
                .set("border", "1px solid black");

        RouterLink avatarLink = new RouterLink();
        avatarLink.setRoute(UserPage.class);
        avatarLink.add(userAvatar2);
        avatarLink.getStyle().set("text-decoration", "none");

        RouterLink userpageLink = new RouterLink();
        userpageLink.setText("View Profile");
        userpageLink.setRoute(UserPage.class);
        userpageLink.getStyle()
                .set("font-weight", "bold")
                .set("font-size", "14px")
                .set("color", "white")
                .set("text-decoration", "none");

        Div usernameDiv = new Div();
        usernameDiv.setText(username);
        usernameDiv.getStyle()
                .set("font-size", "13px")
                .set("color", "#7e8f96");

        VerticalLayout userInfoLayout = new VerticalLayout(userpageLink, usernameDiv);
        userInfoLayout.setPadding(false);
        userInfoLayout.setSpacing(false);
        userInfoLayout.setMargin(false);

        HorizontalLayout secondAvatarLayout = new HorizontalLayout(avatarLink, userInfoLayout);
        secondAvatarLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        secondAvatarLayout.setSpacing(true);

        Button logoutButton = new Button("Logout", event -> getUI().ifPresent(ui -> ui.navigate("login")));
        logoutButton.getStyle().set("color", "white").set("font-size", "14px");

        Button backToMediaButton = new Button("Go to the Homepage", event -> getUI().ifPresent(ui -> ui.navigate("media")));
        backToMediaButton.getStyle().set("color", "white").set("font-size", "14px");

        Button avatarCreatingButton = new Button("Upload your own Avatar", event -> getUI().ifPresent(ui -> ui.navigate("avatarselection")));
        avatarCreatingButton.getStyle().set("color", "white").set("font-size", "14px");

        popoverContent.add(secondAvatarLayout, avatarCreatingButton, backToMediaButton, logoutButton);

        Avatar userAvatar = new Avatar(username);
        userAvatar.setImage(avatarUrl);
        userAvatar.getStyle()
                .set("background-color", "white")
                .set("color", "black")
                .set("border", "1px solid black");

        Popover popover = new Popover();
        popover.setTarget(userAvatar2);
        popover.setPosition(PopoverPosition.BOTTOM);
        popover.addThemeVariants(PopoverVariant.ARROW, PopoverVariant.LUMO_NO_PADDING);
        popover.add(popoverContent);

        boolean[] isOpened = {false};
        userAvatar2.getElement().addEventListener("click", event -> {
            if (!isOpened[0]) {
                popover.setOpened(true);
                isOpened[0] = true;
                getUI().ifPresent(ui -> ui.access(() ->
                        new Timer().schedule(new TimerTask() {
                            public void run() {
                                // Do nothing; placeholder for potential future logic
                            }
                        }, 1000)
                ));
            } else {
                popover.setOpened(false);
                isOpened[0] = false;
            }
        });

        Icon notificationBell = new Icon(VaadinIcon.BELL);
        notificationBell.getElement().getStyle()
                .set("color", "#fff")
                .set("font-size", "24px");

        TextField searchField = new TextField();
        searchField.setPlaceholder("Search Semaino");
        searchField.addClassName("media-textfield");
        searchField.getElement().getStyle()
                .set("background-color", "#6C7A89")
                .set("color", "#FFFFFF")
                .set("border-radius", "20px")
                .set("width", "300px")
                .set("border", "none")
                .set("padding", "0 15px")
                .set("font-size", "12px");

        searchField.getElement().getChildren()
                .filter(child -> child.getTag().equals("input"))
                .forEach(input -> input.getStyle()
                        .set("background-color", "#FFFFFF")
                        .set("border-radius", "20px")
                        .set("border", "none")
                        .set("color", "#FFFFFF")
                        .set("padding", "0 15px")
                );

        // You can add layout arrangements here as needed, like a top bar layout, etc.
// Fancy "Communo" title (RouterLink navigates to Media view)
        RouterLink clickableTitle = new RouterLink("Semaino", Media.class);
        clickableTitle.getStyle()
                .set("font-family", "'Segoe Script', cursive")
                .set("font-size", "28px")
                .set("font-weight", "bold")
                .set("color", "#FFFFFF")
                .set("text-decoration", "none")
                .set("cursor", "pointer")
                .set("width", "179px");

// Root horizontal layout for header bar
        HorizontalLayout rootLayout = new HorizontalLayout();
        rootLayout.setWidthFull();
        rootLayout.setPadding(false);
        rootLayout.setMargin(false);

// Background color is dark (you said "baby blue" but color is #1a1a1b, which is dark)
// Text color is white for the entire root
        rootLayout.getStyle()
                .set("border-bottom", "1px solid #666")
                .set("background-color", "#1a1a1b")  // Dark background color
                .set("color", "#ffffff");             // White text color

// Left layout with clickable title
        HorizontalLayout leftLayout = new HorizontalLayout(clickableTitle);
        leftLayout.setJustifyContentMode(JustifyContentMode.START);
        leftLayout.setWidthFull();
        leftLayout.getElement().getStyle().set("margin-left", "20px");
// Keep text white to match root, not dark gray (#333) which won't be visible
        leftLayout.getStyle().set("color", "#ffffff");

// Center layout with searchField (make sure searchField is defined)
        HorizontalLayout centerLayout = new HorizontalLayout(searchField);
        centerLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        centerLayout.setAlignItems(Alignment.CENTER);
        centerLayout.setWidthFull();
        centerLayout.getStyle().set("color", "#ffffff");

// Right layout with notification bell and avatar (make sure they are defined)
        HorizontalLayout rightLayout = new HorizontalLayout(notificationBell, userAvatar2);
        rightLayout.setJustifyContentMode(JustifyContentMode.END);
        rightLayout.setAlignItems(Alignment.CENTER);
        rightLayout.setWidthFull();
        rightLayout.getElement().getStyle().set("margin-right", "20px");
        rightLayout.getStyle().set("color", "#ffffff");

// Add sub-layouts to root layout
        rootLayout.add(leftLayout, centerLayout, rightLayout);
        rootLayout.setFlexGrow(1, leftLayout, centerLayout, rightLayout);

// Create the post input card once and style it centered
        Component inputCard = createPostInputCard();
        inputCard.getElement().getStyle()
                .set("width", "800px")
                .set("margin-left", "auto")
                .set("margin-right", "auto")
                .set("margin-top", "15px");

// Sort button and wrapper
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
        sortButtonWrapper.add(sortButton);

// Popover setup
        Popover sortPopoverPopup = new Popover();
        sortPopoverPopup.addClassName("glow-hover");
        sortPopoverPopup.setTarget(sortButton);
        sortPopoverPopup.setPosition(PopoverPosition.BOTTOM);
        sortPopoverPopup.addThemeVariants(PopoverVariant.ARROW, PopoverVariant.LUMO_NO_PADDING);

// Popover content
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

        sortNewButton = new Button("New");
        sortNewButton.setWidthFull();
        sortNewButton.addClassName("popup-hover-item");
        sortNewButton.getStyle().set("color", "#D7DADC");

        sortTopButton = new Button("Top");
        sortTopButton.setWidthFull();
        sortTopButton.addClassName("popup-hover-item");
        sortTopButton.getStyle().set("color", "#D7DADC");

        sortPopoverContent.add(sortHeader, sortNewButton, sortTopButton);
        sortPopoverPopup.add(sortPopoverContent);

        boolean[] isSortPopoverOpen = {false};

// Toggle popover with a delay to prevent UI sync issues
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
                }, 100);
            } else {
                sortPopoverPopup.setOpened(false);
                isSortPopoverOpen[0] = false;
            }
        });

// Sort button click listeners
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

// Middle bar holds the sort button wrapper and centers it
        middleBar = new HorizontalLayout(sortButtonWrapper);
        middleBar.setWidth("800px");
        middleBar.setHeight("20px");
        middleBar.getElement().getStyle()
                .set("margin-left", "auto")
                .set("margin-right", "auto")
                .set("margin-top", "10px");
        middleBar.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        middleBar.setPadding(false);
        middleBar.getStyle()
                .set("background-color", "#1a1a1b");

// Virtual list of posts (mixed Components and Posts)
        postList = new VirtualList<>();
        postList.getElement().getStyle()
                .set("scrollbar-gutter", "stable both-edges")
                .set("padding", "0")
                .set("margin", "0")
                .set("overflow", "hidden");

// Items: middleBar, inputCard, then posts
        List<Object> items = new ArrayList<>();
        items.add(middleBar);
        items.add(inputCard);
        items.addAll(UserPost.readPostsForUser(username));
        postList.setItems(items);

// Renderer for posts and components
        postList.setRenderer(new ComponentRenderer<>(item -> {
            if (item instanceof Component) {
                return (Component) item;
            } else if (item instanceof Post post) {
                Component card = post.getParentId().equals("0")
                        ? createCommentCard(post)
                        : createReplyCard(post);
                card.getElement().getStyle()
                        .set("margin", "0 auto")
                        .set("width", "800px")
                        .set("margin-top", "10px");
                return card;
            } else {
                return new Span("Unknown item");
            }
        }));

        postList.setWidthFull();
        postList.setHeightFull();

// Content container holding postList
        VerticalLayout content = new VerticalLayout();
        content.setWidthFull();
        content.setHeight("100%");
        content.setPadding(false);
        content.setSpacing(true);
        content.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        content.add(postList);
        content.setFlexGrow(1, postList);

// Main layout holds sidebar, content, and filler
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeFull();
        layout.setAlignItems(Alignment.CENTER);
        layout.getStyle().set("position", "relative");

// Sidebar overlay on the left
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

// Sidebar menu items
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

        sideBar.add(homeLayout, popularLayout, forYouLayout, allLayout);

// Filler overlay on the right
        VerticalLayout filler = new VerticalLayout();
        filler.setHeightFull();
        filler.setWidth("200px");
        filler.setAlignItems(Alignment.END);
        filler.getStyle()
                .set("bottom", "0")
                .set("right", "0")
                .set("background-color", "#1a1a1b")
                .set("border-left", "1px solid #444");

// Prepare user card data (followers list must be defined)
        User user = selectedUser; // reuse the already loaded one

        VerticalLayout userCard;
        if (user != null) {
            System.out.println("Followers: " + followers);
            System.out.println("Subscriptions: " + subscriptions);
            userCard = createUserCard(username, user.getLikeCount(), followers, subscriptions);
        } else {
            userCard = createUserCard(username, 0, new ArrayList<>(), new ArrayList<>());
        }

        filler.add(userCard);



// Add sidebar, content, and filler to main layout
        layout.add(sideBar, content, filler);
        layout.setFlexGrow(1, content); // Only content expands

// Add the header and main layout to the view
        add(rootLayout, layout);
    }


    private String getAvatarFilenameForUser(String username) {
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

    public VerticalLayout createUserCard(String username, int likes, List<String> followers, List<String> subscriptions) {
        VerticalLayout userCardLayout = new VerticalLayout();
        userCardLayout.addClassName("hover-card");
        userCardLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        userCardLayout.setSpacing(false);
        userCardLayout.setPadding(true);
        userCardLayout.getStyle()
                .set("border", "1px solid #ccc")
                .set("border-radius", "10px")
                .set("padding", "10px")
                .set("margin", "0")
                .set("background-color", "#1a1a1b")
                .set("color", "#ffffff");
        userCardLayout.setWidthFull();

        // Avatar
        Avatar avatar = new Avatar();
        String avatarFileName = getAvatarFilenameForUser(username);
        avatar.setImage("/avatar/" + username + "/" + avatarFileName);
        avatar.setWidth("50%");
        avatar.setHeight("auto");
        avatar.getStyle()
                .set("background-color", "white")
                .set("border", "1px solid #ffffff");
        userCardLayout.add(avatar);

        // Username
        Span name = new Span(username);
        name.getStyle().set("font-weight", "bold").set("font-size", "1rem");
        userCardLayout.add(name);

        // --- VirtualLists BEFORE the follow button ---
        VirtualList<String> followsList = new VirtualList<>();
        followsList.setItems(subscriptions);
        followsList.setRenderer(new ComponentRenderer<>(followedUser -> {
            Span userSpan = new Span(followedUser);
            userSpan.getStyle()
                    .set("color", "#ffffff")
                    .set("background-color", "#1a1a1b")
                    .set("padding", "6px 10px")
                    .set("border-radius", "4px")
                    .set("display", "block");
            return userSpan;
        }));

        VirtualList<String> followedByList = new VirtualList<>();
        followedByList.setItems(followers);
        followedByList.setRenderer(new ComponentRenderer<>(follower -> {
            Span userSpan = new Span(follower);
            userSpan.getStyle()
                    .set("color", "#ffffff")
                    .set("background-color", "#1a1a1b")
                    .set("padding", "6px 10px")
                    .set("border-radius", "4px")
                    .set("display", "block");
            return userSpan;
        }));

        // FOLLOW BUTTON
        Button followButton = new Button("Follow");
        followButton.getStyle()
                .set("background-color", "#3a3a3c")
                .set("color", "#ffffff")
                .set("border", "1px solid #555")
                .set("border-radius", "5px")
                .set("margin-top", "5px")
                .set("margin-bottom", "10px");

        followButton.addClickListener(click -> {
            try {
                String currentUsername = Files.readString(Paths.get("loggedinuser.txt")).trim();

                if (currentUsername.equals(username)) {
                    Notification.show("You can't follow yourself.", 3000, Notification.Position.TOP_CENTER);
                    return;
                }

                Path basePath = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users");
            //    Path basePath = Paths.get("C:\\Users\\0\\IdeaProjects\\VaadinSocialMediaUpload\\users");


                Path currentUserFollowingDir = basePath.resolve(currentUsername).resolve("Follow").resolve("Following");
                Path targetUserFollowedByDir = basePath.resolve(username).resolve("Follow").resolve("FollowedBy");

                Files.createDirectories(currentUserFollowingDir);
                Files.createDirectories(targetUserFollowedByDir);

                Path followingFile = currentUserFollowingDir.resolve(username + ".txt");
                Path followedByFile = targetUserFollowedByDir.resolve(currentUsername + ".txt");

                boolean updated = false;

                if (!Files.exists(followingFile)) {
                    Files.writeString(followingFile, username, StandardCharsets.UTF_8);
                    subscriptions.add(username);
                    followsList.getDataProvider().refreshAll();
                    updated = true;
                }

                if (!Files.exists(followedByFile)) {
                    Files.writeString(followedByFile, currentUsername, StandardCharsets.UTF_8);
                    followers.add(currentUsername);
                    followedByList.getDataProvider().refreshAll();
                    updated = true;
                }

                if (updated) {
                    Notification notification = new Notification("Now following " + username, 3000, Notification.Position.TOP_CENTER);
                    notification.getElement().getStyle()
                            .set("background-color", "#1a1a1b")
                            .set("color", "#ffffff")
                            .set("border", "1px solid #00ff99")
                            .set("border-radius", "8px");
                    notification.open();
                }

            } catch (IOException e) {
                e.printStackTrace();
                Notification.show("Failed to follow user.", 3000, Notification.Position.TOP_CENTER);
            }
        });

        userCardLayout.add(followButton);

        // Status Section
        Span statusLabel = new Span("Status:");
        statusLabel.getStyle().set("font-weight", "bold").set("margin-bottom", "5px");

        TextArea statusArea = new TextArea();
        statusArea.setPlaceholder("Write your status here...");
        statusArea.setWidth("100%");
        statusArea.getStyle()
                .set("background-color", "#2a2a2b")
                .set("color", "#ffffff")
                .set("border", "1px solid #555")
                .set("border-radius", "5px")
                .set("font-weight", "normal");


        Path statusFile = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users", username, "Status.txt");
        //Path statusFile = Paths.get("C:\\Users\\0\\IdeaProjects\\VaadinSocialMediaUpload\\users", username, "Status.txt");
        if (Files.exists(statusFile)) {
            try {
                String existingStatus = Files.readString(statusFile, StandardCharsets.UTF_8);
                statusArea.setValue(existingStatus);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Button saveButton = new Button("Save");
        saveButton.getStyle()
                .set("background-color", "#3a3a3c")
                .set("color", "#ffffff")
                .set("margin-top", "5px");

        saveButton.addClickListener(event -> {
            String statusText = statusArea.getValue();
            Path userDir = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users", username);
            //Path userDir = Paths.get("C:\\Users\\0\\IdeaProjects\\VaadinSocialMediaUpload\\users", username);
            try {
                Files.createDirectories(userDir);
                Files.writeString(userDir.resolve("Status.txt"), statusText, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                Notification notification = new Notification();
                notification.setDuration(3000);
                notification.setPosition(Notification.Position.TOP_CENTER);
                Span message = new Span("Status saved successfully!");
                message.getStyle()
                        .set("color", "#FF4500")
                        .set("font-weight", "bold");

                notification.add(message);
                notification.getElement().getStyle()
                        .set("background-color", "#1a1a1b")
                        .set("border", "1px solid #FF4500")
                        .set("border-radius", "8px")
                        .set("box-shadow", "0 0 10px #FF4500");

                notification.open();
            } catch (IOException e) {
                e.printStackTrace();
                Notification.show("Error saving status: " + e.getMessage(), 5000, Notification.Position.TOP_CENTER);
            }
        });

        VerticalLayout statusLayout = new VerticalLayout(statusLabel, statusArea, saveButton);
        statusLayout.setPadding(false);
        statusLayout.setSpacing(false);
        statusLayout.setWidthFull();
        userCardLayout.add(statusLayout);

        userCardLayout.add(new Hr());

        // Likes
        Span likesSpan = new Span("Likes: " + likes);
        likesSpan.getStyle().set("font-size", "0.9rem");
        userCardLayout.add(likesSpan);

        userCardLayout.add(new Hr());

        // Follows
        Span followsLabel = new Span("Follows:");
        followsLabel.getStyle().set("font-weight", "bold").set("font-size", "1rem");
        userCardLayout.add(followsLabel);

        followsList.setHeight("120px");
        followsList.getStyle()
                .set("background-color", "#1a1a1b")
                .set("padding", "10px")
                .set("border-radius", "5px");
        userCardLayout.add(followsList);

        userCardLayout.add(new Hr());

        // Followed By
        Span followedByLabel = new Span("Followed By:");
        followedByLabel.getStyle().set("font-weight", "bold").set("font-size", "1rem");
        userCardLayout.add(followedByLabel);

        followedByList.setHeight("120px");
        followedByList.getStyle()
                .set("background-color", "#1a1a1b")
                .set("padding", "10px")
                .set("border-radius", "5px");
        userCardLayout.add(followedByList);

        return userCardLayout;
    }


    // Create a card layout for a top-level comment post
    public VerticalLayout createCommentCard(Post postData) {
        VerticalLayout commentCardLayout = new VerticalLayout();
        commentCardLayout.addClassName("hover-card");
        commentCardLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        commentCardLayout.setSpacing(true);
        commentCardLayout.setPadding(true);
        commentCardLayout.setWidth("800px");
        commentCardLayout.getStyle()
                .set("border", "1px solid #ccc")
                .set("border-radius", "10px")
                .set("padding", "10px")
                .set("margin", "0")
                .set("background-color", "#1a1a1b")
                .set("color", "#ffffff");

        // Top row: avatar + username + post time
        HorizontalLayout topRow = new HorizontalLayout();
        String username = postData.getUserName();
        String avatarUrl = "/avatar/" + username + "/" + getAvatarFilenameForUser(username);
        Avatar userAvatar = new Avatar();
        userAvatar.setImage(avatarUrl);
        userAvatar.getStyle()
                .set("background-color", "white")
                .set("border", "1px solid #ffffff");

        Span userName = new Span(username);
        userName.getStyle().set("color", "#ffffff");

        Span commentTime = new Span("Posted on: " + Post.formatTimestamp(postData.getTimestamp()));
        commentTime.getStyle().set("color", "#ffffff");

        topRow.add(userAvatar, userName, commentTime);
        topRow.setWidthFull();
        topRow.setJustifyContentMode(JustifyContentMode.START);
        topRow.setAlignItems(Alignment.CENTER);

        // Comment content text
        Div commentContent = new Div();
        commentContent.setWidthFull();
        commentContent.getStyle()
                .set("text-align", "left")
                .set("white-space", "pre-wrap")
                .set("overflow-wrap", "break-word")
                .set("word-break", "break-word")
                .set("max-width", "750px")
                .set("color", "#ffffff");
        commentContent.setText(postData.getPostContent());

        // Likes row
        HorizontalLayout likesRow = new HorizontalLayout();
        likesRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
        likesRow.setAlignItems(Alignment.CENTER);
        likesRow.getStyle().set("width", "725px");

        Span likesCount = new Span("Likes: " + postData.getLikes());
        likesCount.getStyle()
                .set("font-size", "14px")
                .set("color", "#ffffff")
                .set("white-space", "nowrap");

        LikeButton likeButton = new LikeButton(postData);
        likeButton.getStyle()
                .set("background-color", "white")
                .set("color", "black")
                .set("border", "1px solid #ffffff");

        likesRow.add(likesCount, likeButton);

        // Add components to the comment card
        commentCardLayout.add(topRow, commentContent, likesRow);

        // Add reply input section below the comment card
        UserPost userPostInstance = new UserPost();
        commentCardLayout.add(userPostInstance.createReplyInputSection(postData));

        return commentCardLayout;
    }

    // Create a card layout for a reply post (child post)
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

        // Parent post info at the top of reply card
        HorizontalLayout topRow = new HorizontalLayout();
        String originalUsername = (parentPost != null) ? parentPost.getUserName() : "Unknown";
        String avatarFilename = getAvatarFilenameForUser(originalUsername);
        String avatarUrl = "/avatar/" + originalUsername + "/" + avatarFilename;

        Avatar originalAvatar = new Avatar();
        originalAvatar.setImage(avatarUrl);
        originalAvatar.getStyle()
                .set("background-color", "white")
                .set("border", "1px solid #ffffff");

        Span originalPoster = new Span(originalUsername);
        originalPoster.getStyle().set("color", "#ffffff");

        Span originalTime = new Span("Posted on: " +
                Post.formatTimestamp((parentPost != null) ? parentPost.getTimestamp() : postData.getTimestamp()));
        originalTime.getStyle().set("color", "#ffffff");

        topRow.add(originalAvatar, originalPoster, originalTime);
        topRow.setWidthFull();
        topRow.setJustifyContentMode(JustifyContentMode.START);
        topRow.setAlignItems(Alignment.CENTER);

        // Parent post content display
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
        originalPostContent.setText((parentPost != null) ? parentPost.getPostContent() : "Original post not found");

        // Parent post likes row
        HorizontalLayout parentLikesRow = new HorizontalLayout();
        if (parentPost != null) {
            parentLikesRow.setWidth("750px");
            parentLikesRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
            parentLikesRow.setAlignItems(Alignment.CENTER);

            Span parentLikesCount = new Span("Likes: " + parentPost.getLikes());
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

        // Add parent post info to the reply card
        replyCardLayout.add(topRow, originalPostContent);
        if (parentPost != null) {
            replyCardLayout.add(parentLikesRow);
        }

        // Reply input section to reply to the parent post
        if (parentPost != null) {
            replyCardLayout.add(new UserPost().createReplyInputSection(parentPost));
        }

        // Reply meta info: avatar, username, timestamp of the reply itself
        HorizontalLayout replyMeta = new HorizontalLayout();
        String replyUsername = postData.getUserName();
        String replyAvatarFilename = getAvatarFilenameForUser(replyUsername);
        String replyAvatarUrl = "/avatar/" + replyUsername + "/" + replyAvatarFilename;

        Avatar replyAvatar = new Avatar();
        replyAvatar.setImage(replyAvatarUrl);
        replyAvatar.getStyle()
                .set("background-color", "white")
                .set("border", "1px solid #ffffff");

        Span replyUser = new Span(replyUsername);
        replyUser.getStyle().set("color", "#ffffff");

        Span replyTime = new Span("Posted on: " + Post.formatTimestamp(postData.getTimestamp()));
        replyTime.getStyle().set("color", "#ffffff");

        replyMeta.add(replyAvatar, replyUser, replyTime);
        replyMeta.setWidthFull();
        replyMeta.setJustifyContentMode(JustifyContentMode.START);
        replyMeta.setAlignItems(Alignment.CENTER);
        replyMeta.getStyle().set("margin-left", "50px");

        // Reply content display
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

        Span likesCount = new Span("Likes: " + postData.getLikes());
        likesCount.getStyle()
                .set("font-size", "14px")
                .set("color", "#ffffff")
                .set("white-space", "nowrap");

        HorizontalLayout buttonWrapper = new HorizontalLayout();
        buttonWrapper.setWidthFull();
        buttonWrapper.setJustifyContentMode(JustifyContentMode.END);
        buttonWrapper.add(new LikeButton(postData));

        likesRow.add(likesCount, buttonWrapper);

        // Add reply meta, content, and likes row to reply card
        replyCardLayout.add(replyMeta, replyContent, likesRow);

        // Reply input section to reply to this reply
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
        if (sortMode == 0) {
            allPosts = UserPost.readPostsFromFiles();
            sortButton.setText("New");  // Update button label
        } else {
            allPosts = UserPost.readPostsSortedByLikes();
            sortButton.setText("Top");
        }

        Component inputCard = createPostInputCard();
        styleInputCard(inputCard);

        List<Object> items = new ArrayList<>();
        items.add(middleBar);
        items.add(inputCard);
        items.addAll(allPosts);

        postList.setItems(items);
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
    private void loadUserProfile(String username) {
        // Your logic to load and display user data from
        // C:/Users/sdachs/IdeaProjects/vaadin-programmieraufgaben2/users/<username>
        // For example:
        add(new Span("User profile for: " + username));
    }
    private List<String> loadList(String username, String listName) throws IOException {
        Path dir = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users",
                username, "Follow", listName);
        //Path dir = Paths.get("C:\\Users\\0\\IdeaProjects\\VaadinSocialMediaUpload\\users",
          //      username, "Follow", listName);
        if (!Files.exists(dir)) return Collections.emptyList();
        try (Stream<Path> files = Files.list(dir)) {
            return files
                    .filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString().replace(".txt", ""))
                    .collect(Collectors.toList());
        }
    }

    private List<String> loadSubscribers(String username) throws IOException {
        return loadList(username, "Following");
    }
    private List<String> loadFollowers(String username) throws IOException {
        return loadList(username, "FollowedBy");
    }

}