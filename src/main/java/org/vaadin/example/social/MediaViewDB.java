package org.vaadin.example.social;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;
import java.util.function.Consumer;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.listbox.ListBox;
import java.util.Comparator;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Route("mediadb")
public class MediaViewDB extends VerticalLayout {
    private String selectedUser;

    private final PostService postService;
    private final UserService userService;
    private final LikeService likeService;
    private String activeUsername;

    private List<PostEntity> dbPosts;
    private VirtualList<Object> dbPostList; // use Object to allow input card + posts
private final ForumService forumService;
    private Long replyToPostId = 0L;
    private String replyToUser = "";
    private TextArea contentField;
    private ForumEntity currentForum;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public MediaViewDB(ForumService forumService, PostService postService, UserService userService, LikeService likeService) {
        this.forumService = forumService;
        this.postService = postService;
        this.userService = userService;
        this.likeService = likeService;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        currentForum = forumService.findByName("all").orElseThrow();


        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setSpacing(false);
        setMargin(false);
        setPadding(false);
        getStyle().set("background-color", "#1a1a1b");

        // === Get logged-in user ===
        UserEntity sessionUser = VaadinSession.getCurrent().getAttribute(UserEntity.class);
        if (sessionUser == null) {
            sessionUser = userService.findByUsername("A").orElse(null);
            VaadinSession.getCurrent().setAttribute(UserEntity.class, sessionUser);
        }

        final UserEntity currentUser = sessionUser;

        // === Input card ===
        contentField = new TextArea();
        contentField.setPlaceholder("Write something...");
        contentField.getStyle().set("color", "#fff");

        // --- Create input card ---
        Component inputCard = createPostInputCardDB(); // your input card

// --- VirtualList for posts + input card ---
        dbPostList = new VirtualList<>();
        dbPostList.setWidthFull();
        dbPostList.setHeightFull();
        dbPostList.getStyle()
                .set("overflow", "hidden !important");

// --- Combine input card + posts into one list ---
        List<Object> allItems = new ArrayList<>();
        allItems.add(inputCard);             // input card at top
        allItems.addAll(postService.findAll()); // all posts from DB
        dbPostList.setItems(allItems);

// --- Renderer ---
// --- User layout ---
        VerticalLayout userLayout = new VerticalLayout();
        userLayout.setWidth("10%");
        userLayout.setHeightFull();
        userLayout.getStyle().set("border-left", "1px solid #333");
        userLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        userLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

        String defaultUsername = currentUser != null ? currentUser.getUsername() : "Anonymous";

// Avatar
        Image userAvatar = new Image();
        userAvatar.setWidth("80px");
        userAvatar.setHeight("80px");
        userAvatar.getStyle()
                .set("border-radius", "50%")
                .set("border", "1px solid #333")
                .set("object-fit", "cover");

        userService.getAvatarData(defaultUsername).ifPresentOrElse(
                data -> userAvatar.setSrc(new StreamResource(defaultUsername + "-avatar.png",
                        () -> new ByteArrayInputStream(data))),
                () -> userAvatar.setSrc("images/default-avatar.png")
        );

// Username div
        Div userNameDiv = new Div();
        userNameDiv.setText(defaultUsername);
        userNameDiv.getStyle()
                .set("color", "white")
                .set("margin-top", "8px")
                .set("text-align", "center");
// --- Status field ---
        TextField statusField = new TextField("Status");
        statusField.setWidthFull();
        statusField.getStyle().set("color", "white");
        statusField.getStyle().set("margin-top", "8px");
        statusField.getStyle().set("text-align", "center");

// Load initial status for default user
        userService.findByUsername(defaultUsername).ifPresent(user -> {
            statusField.setValue(user.getStatus() != null ? user.getStatus() : "");
        });

// Editable only if default user is the logged-in user
        statusField.setReadOnly(!defaultUsername.equals(currentUser.getUsername()));

// Persist edits
        statusField.addValueChangeListener(e -> {
            if (defaultUsername.equals(currentUser.getUsername())) {
                userService.findByUsername(currentUser.getUsername()).ifPresent(user -> {
                    user.setStatus(e.getValue());
                    userService.save(user);
                });
            }
        });

// Add it to userLayout
        userLayout.add(userAvatar, userNameDiv, statusField);

        userLayout.add(userAvatar, userNameDiv);

// --- Now set up dbPostList and renderer ---
        dbPostList.setRenderer(new ComponentRenderer<>(item -> {
            if (item instanceof Component c) {
                c.getStyle().set("margin", "0 auto");
                c.getStyle().set("width", "800px");
                return c;
            } else if (item instanceof PostEntity p) {
                Component card = (p.getParentId() == 0L)
                        ? createCommentCardDB(p, userLayout, userAvatar, userNameDiv, statusField)
                        : createReplyCardDB(p);

                card.getStyle().set("margin", "0 auto");
                card.getStyle().set("width", "800px");
                card.getStyle().set("margin-top", "10px");
                return card;
            } else return new Span("Unknown item");
        }));


// --- Top bar layout ---
        HorizontalLayout topBar = new HorizontalLayout();
        topBar.setWidthFull();
        topBar.setHeight("5vh"); // 5% of viewport height
        topBar.getStyle()
                .set("background-color", "#2a2a2b")
                .set("border-bottom", "1px solid #555");
        topBar.setPadding(true);
        topBar.setAlignItems(FlexComponent.Alignment.CENTER);
        topBar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN); // space between left, center, right
        topBar.setSpacing(true);

// --- Left: Logo ---
        // --- Logo ---


// --- Center: Search field ---
        TextField searchField = new TextField();
        searchField.setPlaceholder("Search...");
        searchField.setWidth("20%"); // can adjust as needed
        searchField.getElement().getStyle().set("align-items", "center");
        searchField.getStyle()
                .set("background-color", "#666")
                .set("color", "#fff")
                .set("border-radius", "20px")
                .set("border", "1px solid #555")
                .set("padding", "0 15px");

// --- Right: Bell + User avatar ---
        HorizontalLayout rightLayout = new HorizontalLayout();
        rightLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        rightLayout.setSpacing(true);

// Bell icon
        Icon bell = VaadinIcon.BELL.create();
        bell.setSize("24px");
        bell.getStyle()
                .set("color", "#fff")
                .set("cursor", "pointer");

        Image userAvatarImg = new Image();
        userAvatarImg.setWidth("32px");
        userAvatarImg.setHeight("32px");
        userAvatarImg.getStyle()
                .set("border-radius", "50%")
                .set("object-fit", "cover");

        userService.getAvatarData(currentUser.getUsername()).ifPresentOrElse(
                data -> {
                    StreamResource resource = new StreamResource("avatar.png", () -> new ByteArrayInputStream(data));
                    userAvatarImg.setSrc(resource);
                },
                () -> userAvatarImg.setSrc("/images/default-avatar.png") // fallback
        );

// Add bell and avatar to right layout
        rightLayout.add(bell, userAvatarImg);
        // Add click listener to avatar
        userAvatarImg.getElement().addEventListener("click", e -> {
            // Check if clicked avatar is current user's own avatar
            String clickedUsername = currentUser.getUsername(); // for now, it's the currentUser
            List<PostEntity> postsToShow;

            if (clickedUsername.equals(currentUser.getUsername())) {
                // Show own posts
                postsToShow = postService.findPostsByUser(currentUser.getUsername());
            } else {
                // Show clicked user's posts
                postsToShow = postService.findPostsByUser(clickedUsername);
            }

            // Sort newest first
            postsToShow.sort(Comparator.comparing(PostEntity::getId).reversed());

            // Prepare items for VirtualList
            List<Object> items = new ArrayList<>();
            items.add(createPostInputCardDB()); // keep input card
            items.addAll(postsToShow);

            dbPostList.setItems(items);
        });




// --- Add all components to top bar ---


// --- Search functionality ---
        searchField.addValueChangeListener(event -> {
            String searchText = event.getValue().trim();
            if (searchText.isEmpty()) {
                loadPostsByForum(currentForum.getId());
                return;
            }

            Optional<UserEntity> user = userService.findByUsername(searchText);
            if (user.isPresent()) {
                List<PostEntity> userPosts = postService.findPostsByUser(searchText);
                userPosts.sort(Comparator.comparing(PostEntity::getId).reversed()); // newest first

                List<Object> items = new ArrayList<>();
                items.add(createPostInputCardDB());
                items.addAll(userPosts);

                dbPostList.setItems(items);
            } else {
                dbPostList.setItems(List.of(createPostInputCardDB()));
            }
        });


// --- Layout ---
        VerticalLayout listWithInput = new VerticalLayout(dbPostList);
        listWithInput.setWidthFull();
        listWithInput.setHeightFull();
        listWithInput.setPadding(false);
        listWithInput.setSpacing(false);
        listWithInput.setAlignItems(FlexComponent.Alignment.CENTER);

        HorizontalLayout mainLayout = new HorizontalLayout(); // main container
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);


        VerticalLayout forumLayout = new VerticalLayout();
        forumLayout.setWidth("10%");
        forumLayout.setHeightFull();
        forumLayout.getStyle().set("border-right", "1px solid #333");
        forumLayout.setPadding(true);
        forumLayout.setSpacing(true);

// Forum dropdown
        Select<ForumEntity> forumSelect = new Select<>();
        forumSelect.setItems(forumService.findAllForums());
        forumSelect.setItemLabelGenerator(ForumEntity::getName);
        forumSelect.setValue(currentForum); // default "all"
        forumSelect.addValueChangeListener(e -> {
            currentForum = e.getValue();
            System.out.println("Selected forum changed to: " + currentForum.getName() + " (ID=" + currentForum.getId() + ")");
            e.getSource().getUI().ifPresent(ui -> ui.getPage().reload());
            loadPostsByForum(currentForum.getId());
        });
        forumSelect.setPlaceholder("Choose Forum");
        forumSelect.setWidth("90%");
        forumSelect.getStyle().set("border", "1px solid white");

// Read-only box
        TextField nonWritableBox = new TextField();
        nonWritableBox.setReadOnly(true);
        nonWritableBox.setWidth("90%");
        nonWritableBox.getStyle().set("border", "1px solid white");

// Label
        Div createLabel = new Div();
        createLabel.setText("Create a new Forum");
        createLabel.getStyle()
                .set("font-weight", "bold")
                .set("margin-top", "10px")
                .set("color", "white")
                .set("border", "1px solid white")
                .set("padding", "1px 5px")
                .set("border-radius", "6px")
                .set("width", "90%");

// TextField for new forum name
        TextField writableBox = new TextField();
        writableBox.getStyle()
                .set("border", "1px solid white")
                .set("border-radius", "6px");

// Create button
        Button createButton = new Button("Create", e -> {
            String forumName = writableBox.getValue().trim();
            if (forumName.isEmpty()) {
                Notification.show("Forum name cannot be empty").setPosition(Notification.Position.TOP_CENTER);
                return;
            }
            Long currentUserId = currentUser.getId();

            forumService.findByName(forumName).ifPresentOrElse(
                    f -> Notification.show("Forum already exists").setPosition(Notification.Position.TOP_CENTER),
                    () -> {
                        ForumEntity forum = forumService.createForum(forumName, currentUserId, "");
                        Notification.show("Forum created: " + forum.getName()).setPosition(Notification.Position.TOP_CENTER);
                    }
            );
            writableBox.clear();
        });
        createButton.setWidth("90%");
        createButton.getStyle()
                .set("color", "white")
                .set("border", "1px solid white")
                .set("border-radius", "6px");

// Add components once after creating them
        forumLayout.add(createLabel, forumSelect, writableBox, createButton);

        refreshForumList(forumLayout, createLabel, writableBox, createButton);

// --- Check user roles and show admin/moderator controls ---
        if (currentUser != null && currentForum != null) {
            Optional<ForumRoleEntity> userRoleOpt = forumService.getForumRoleForUser(currentUser.getId(), currentForum.getId());

            userRoleOpt.ifPresent(role -> {
                // Administrator section
                if ("ADMIN".equalsIgnoreCase(role.getRole())) {
                    Div adminLabel = new Div();
                    adminLabel.setText("Administrator");
                    adminLabel.getStyle()
                            .set("font-weight", "bold")
                            .set("color", "white")
                            .set("margin-top", "20px")
                            .set("text-align", "center");

                    Button editForumButton = new Button("Edit Forum");
                    editForumButton.setWidth("90%");
                    editForumButton.getStyle()
                            .set("color", "white")
                            .set("border", "1px solid white")
                            .set("border-radius", "6px");
                    editForumButton.addClickListener(ev -> {
                        Notification.show("Forum edit opened for Admin: " + currentForum.getName())
                                .setPosition(Notification.Position.TOP_CENTER);
                        // TODO: open edit dialog or navigation later
                    });

                    forumLayout.add(adminLabel, editForumButton);
                }

                // Moderator section
                if ("MODERATOR".equalsIgnoreCase(role.getRole())) {
                    Div modLabel = new Div();
                    modLabel.setText("Moderator");
                    modLabel.getStyle()
                            .set("font-weight", "bold")
                            .set("color", "white")
                            .set("margin-top", "20px")
                            .set("text-align", "center");

                    Button editButton = new Button("Edit");
                    editButton.setWidth("90%");
                    editButton.getStyle()
                            .set("color", "white")
                            .set("border", "1px solid white")
                            .set("border-radius", "6px");
                    editButton.addClickListener(ev -> {
                        Notification.show("Moderator edit opened for forum: " + currentForum.getName())
                                .setPosition(Notification.Position.TOP_CENTER);
                        // TODO: open moderator edit dialog later
                    });

                    forumLayout.add(modLabel, editButton);
                }
            });
        }
///  /

        userLayout.setWidth("10%");
        userLayout.setHeightFull();
        userLayout.getStyle().set("border-left", "1px solid #333");
        userLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        userLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

// --- Avatar ---
        userAvatar.setWidth("80px");
        userAvatar.setHeight("80px");
        userAvatar.getStyle()
                .set("border-radius", "50%")
                .set("border", "1px solid #333")
                .set("object-fit", "cover");

// --- Username ---
        userNameDiv.setText(defaultUsername);
        userNameDiv.getStyle()
                .set("color", "white")
                .set("margin-top", "8px")
                .set("text-align", "center");

// --- Status field ---
        // --- Status field ---
        statusField.setWidthFull();
        statusField.getStyle().set("color", "white");
        statusField.getStyle().set("margin-top", "8px");
        statusField.getStyle().set("text-align", "center");

// --- Save button ---
        Button saveStatusButton = new Button("Save", e -> {
            if (currentUser != null && activeUsername.equals(currentUser.getUsername())) {
                userService.findByUsername(currentUser.getUsername()).ifPresent(user -> {
                    user.setStatus(statusField.getValue());
                    userService.save(user);
                    Notification.show("Status saved").setPosition(Notification.Position.TOP_CENTER);
                });
            }
        });
        saveStatusButton.setVisible(defaultUsername.equals(currentUser.getUsername())); // only visible for current user

// Load initial status for default user
        userService.findByUsername(defaultUsername).ifPresent(user -> {
            statusField.setValue(user.getStatus() != null ? user.getStatus() : "");
        });
        H2 logo = new H2("Semaino");
        logo.getStyle()
                .set("color", "#fff")
                .set("margin", "0")
                .set("font-weight", "bold")
                .set("cursor", "pointer"); // show it's clickable

        logo.addClickListener(e -> {
            // Only reset if current user is logged in
            if (currentUser != null) {
                // Reset active user to current logged-in user
                activeUsername = currentUser.getUsername();

                // Reset layout to show current user’s avatar + status
                userService.getAvatarData(activeUsername).ifPresentOrElse(
                        data -> {
                            StreamResource resource = new StreamResource(activeUsername + "-avatar.png",
                                    () -> new ByteArrayInputStream(data));
                            userAvatar.setSrc(resource);
                        },
                        () -> userAvatar.setSrc("images/default-avatar.png")
                );

                // Reload status
                userService.findByUsername(activeUsername).ifPresent(user -> {
                    statusField.setValue(user.getStatus() != null ? user.getStatus() : "");
                });

                // Update name label
                userNameDiv.setText(activeUsername);

                // Make editable & show save button again
                statusField.setReadOnly(false);
                saveStatusButton.setVisible(true);
            }
        });

// Editable only if default user is the logged-in user
        statusField.setReadOnly(!defaultUsername.equals(currentUser.getUsername()));

// --- Refresh logic ---
        Consumer<String> refreshUserLayout = clickedUsername -> {
            activeUsername = clickedUsername;
            userNameDiv.setText(clickedUsername);

            // Update avatar
            userService.getAvatarData(clickedUsername).ifPresentOrElse(
                    data -> {
                        StreamResource resource = new StreamResource(clickedUsername + "-avatar.png",
                                () -> new ByteArrayInputStream(data));
                        userAvatar.setSrc(resource);
                    },
                    () -> userAvatar.setSrc("images/default-avatar.png")
            );

            // Update status field but do NOT save automatically
            userService.findByUsername(clickedUsername).ifPresent(user -> {
                statusField.setValue(user.getStatus() != null ? user.getStatus() : "");
            });

            // Editable only if active user is current user
            boolean isActiveUserCurrent = clickedUsername.equals(currentUser.getUsername());
            statusField.setReadOnly(!isActiveUserCurrent);
            saveStatusButton.setVisible(isActiveUserCurrent);
        };

// --- Add all components to layout ---
        userLayout.add(userAvatar, userNameDiv, statusField, saveStatusButton);
        topBar.add(logo, searchField, rightLayout);
        topBar.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, logo, searchField, rightLayout);



// add both layouts to main layout
        mainLayout.add(forumLayout, listWithInput, userLayout);
        mainLayout.setFlexGrow(0, forumLayout);
        mainLayout.setFlexGrow(0, userLayout);
        mainLayout.setFlexGrow(1, listWithInput);


// use mainLayout as root conten

        add(topBar, mainLayout);

// --- Load posts initially ---
        loadPostsByForum(currentForum.getId());
    }
    public void setSelectedUser(String username) {
        this.selectedUser = username;
    }

    // optional getter
    public String getSelectedUser() {
        return selectedUser;
    }

    private void loadPostsByForum(Long forumId) {
        List<PostEntity> posts = postService.findPostsByForum(forumId);
        posts.sort(Comparator.comparing(PostEntity::getId).reversed());

        List<Object> items = new ArrayList<>();
        items.add(createPostInputCardDB()); // always fresh, reads currentForum
        items.addAll(posts);
        dbPostList.setItems(items);
    }


    // --- inside your class, after loadPosts() ---
    private void refreshForumList(VerticalLayout forumLayout, Div createLabel, TextField writableBox, Button createButton) {
        forumLayout.removeAll();

        // Get all forums
        List<ForumEntity> forums = forumService.findAllForums();

        // Dropdown for forum selection
        Select<ForumEntity> forumDropdown = new Select<>();
        forumDropdown.setItems(forums);
        forumDropdown.setItemLabelGenerator(ForumEntity::getName);
        forumDropdown.setWidthFull();
        forumDropdown.setPlaceholder("Select a forum");
        forumDropdown.getStyle()
                .set("font-size", "9px")
                .set("color", "white")
                .set("border-radius", "6px")
                .set("border", "1px solid white")
                .set("padding", "4px")
                .set("margin-bottom", "8px")
                .set("width", "100%");


        // Style label
        createLabel.getStyle()
                .set("font-weight", "bold")
                .set("color", "white")
                .set("border", "1px solid white")
                .set("padding", "2px 5px")
                .set("border-radius", "6px")
                .set("margin-bottom", "8px")
                .set("width", "100%");

        // Style writableBox
        writableBox.setWidthFull();
        writableBox.getStyle()
                .set("border", "1px solid white")
                .set("border-radius", "6px")
                .set("margin-bottom", "8px")
                .set("width", "100%");


        // Style createButton
        createButton.setWidthFull();
        createButton.getStyle()
                .set("color", "white")
                .set("border", "1px solid white")
                .set("border-radius", "6px")
                .set("width", "100%");


        // Set currently selected forum
        forumDropdown.setValue(currentForum);

        // Handle selection
        forumDropdown.addValueChangeListener(e -> {
            ForumEntity selected = e.getValue();
            if (selected != null) {
                currentForum = selected; // update currentForum
                Notification.show("Selected forum: " + selected.getName());
                loadPostsByForum(currentForum.getId()); // immediately load posts
            }
        });

        // Add dropdown and create controls
        forumLayout.add(forumDropdown, createLabel, writableBox, createButton);
    }

    /** Create styled card for each post */
    private VerticalLayout createCommentCardDB(PostEntity postData,
                                               VerticalLayout userLayout,
                                               Image userAvatar,
                                               Div userNameDiv,
                                               TextField statusField) { // pass statusField
        // --- Card container ---
        VerticalLayout commentCardLayout = new VerticalLayout();
        commentCardLayout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        commentCardLayout.setAlignItems(Alignment.CENTER);
        commentCardLayout.addClassName("hover-card");
        commentCardLayout.setSpacing(true);
        commentCardLayout.setPadding(true);
        commentCardLayout.getStyle()
                .set("border", "1px solid #ccc")
                .set("border-radius", "10px")
                .set("padding", "10px")
                .set("margin-bottom", "15px")
                .set("background-color", "#1a1a1b")
                .set("color", "#ffffff");

        if (postData.getParentId() != 0L) {
            commentCardLayout.getStyle()
                    .set("margin-left", "40px")
                    .set("margin-top", "10px");
        }

        commentCardLayout.setWidth("800px");

        // --- Top row ---
        HorizontalLayout topRow = new HorizontalLayout();
        String username = postData.getUserName();

        // Avatar image
        Image avatarImg = new Image();
        avatarImg.setWidth("50px");
        avatarImg.setHeight("50px");
        avatarImg.getStyle()
                .set("border-radius", "50%")
                .set("border", "1px solid #ffffff")
                .set("object-fit", "cover");

        userService.getAvatarData(username).ifPresentOrElse(
                data -> {
                    StreamResource resource = new StreamResource("avatar.png", () -> new ByteArrayInputStream(data));
                    avatarImg.setSrc(resource);
                },
                () -> avatarImg.setSrc("/images/default-avatar.png")
        );

        // --- Refresh logic for right-hand userLayout ---
        Consumer<String> refreshUserLayout = clickedUsername -> {
            activeUsername = clickedUsername;
            userNameDiv.setText(clickedUsername);

            userService.getAvatarData(clickedUsername).ifPresentOrElse(
                    data -> {
                        StreamResource resource = new StreamResource(clickedUsername + "-avatar.png",
                                () -> new ByteArrayInputStream(data));
                        userAvatar.setSrc(resource);
                    },
                    () -> userAvatar.setSrc("images/default-avatar.png")
            );

            // Update status field
            userService.findByUsername(clickedUsername).ifPresent(user -> {
                statusField.setValue(user.getStatus() != null ? user.getStatus() : "");
            });

            // Editable only if active user is the logged-in user
            UserEntity currentUser = VaadinSession.getCurrent().getAttribute(UserEntity.class);
            statusField.setReadOnly(!clickedUsername.equals(currentUser.getUsername()));
        };

        // Avatar click: refresh userLayout and load posts for that user
        avatarImg.getElement().addEventListener("click", e -> {
            refreshUserLayout.accept(username);

            List<PostEntity> userPosts = postService.findPostsByUser(username);
            userPosts.sort(Comparator.comparing(PostEntity::getId).reversed());

            List<Object> items = new ArrayList<>();
            items.add(createPostInputCardDB()); // keep input at top
            items.addAll(userPosts);
            dbPostList.setItems(items);
        });

        // Username span
        Span userName = new Span(username);
        userName.getStyle()
                .set("color", "#ffffff")
                .set("text-decoration", "underline")
                .set("cursor", "pointer");

        // Username click: same behavior as avatar
        userName.getElement().addEventListener("click", e -> {
            refreshUserLayout.accept(username);

            List<PostEntity> userPosts = postService.findPostsByUser(username);
            userPosts.sort(Comparator.comparing(PostEntity::getId).reversed());

            List<Object> items = new ArrayList<>();
            items.add(createPostInputCardDB());
            items.addAll(userPosts);
            dbPostList.setItems(items);
        });

        Span commentTime = new Span("Posted on: " + postData.getTimestamp());
        commentTime.getStyle().set("color", "#ffffff");

        topRow.add(avatarImg, userName, commentTime);
        topRow.setWidthFull();
        topRow.setJustifyContentMode(JustifyContentMode.START);
        topRow.setAlignItems(Alignment.CENTER);

        // --- Content ---
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

        // --- Likes row ---
        HorizontalLayout likesRow = new HorizontalLayout();
        likesRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
        likesRow.setAlignItems(Alignment.CENTER);
        likesRow.getStyle().set("width", "725px");

        Span likesCount = new Span("Liked: " + postData.getLikes());
        likesCount.getStyle()
                .set("font-size", "14px")
                .set("color", "#ffffff")
                .set("white-space", "nowrap");

        LikeButtonDB likeButton = new LikeButtonDB(postData, postService, userService);
        likesRow.add(likesCount, likeButton);

        // --- Reply input ---
        TextField replyField = new TextField();
        replyField.setPlaceholder("Reply to " + postData.getUserName() + "...");
        replyField.setWidthFull();

        Button replyButton = new Button("Reply", e -> {
            String replyContent = replyField.getValue().trim();
            if (!replyContent.isEmpty()) {
                UserEntity currentUser = VaadinSession.getCurrent().getAttribute(UserEntity.class);
                if (currentUser != null) {
                    PostEntity replyPost = new PostEntity();
                    replyPost.setUserName(currentUser.getUsername());
                    replyPost.setPostContent(replyContent);
                    replyPost.setTimestamp(java.time.LocalDateTime.now().toString());
                    replyPost.setParentId(postData.getId());
                    replyPost.setParentUser(postData.getUserName());
                    replyPost.setLikes(0);
                    replyPost.setForumId(currentForum.getId());

                    postService.save(replyPost);
                    replyField.clear();
                    loadPostsByForum(currentForum.getId());
                } else {
                    Notification.show("No logged-in user to post reply");
                }
            }
        });

        HorizontalLayout replyLayout = new HorizontalLayout(replyField, replyButton);
        replyLayout.setWidthFull();

        // --- Assemble ---
        commentCardLayout.add(topRow, commentContent, likesRow, replyLayout);

        return commentCardLayout;
    }

    private Component createPostInputCardDB() {
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

        // --- Get current user ---
        UserEntity currentUser = UserUtils.getCurrentUser();
        if (currentUser == null) {
            Span error = new Span("No user logged in");
            postLayout.add(error);
            return postLayout;
        }

// --- Avatar display using Image ---
        // --- Avatar display using Image ---
        Image avatarImg;
        if (currentUser.getAvatarData() != null) {
            StreamResource resource = new StreamResource("avatar.png",
                    () -> new ByteArrayInputStream(currentUser.getAvatarData()));
            avatarImg = new Image(resource, "User avatar");
        } else {
            avatarImg = new Image("/images/default-avatar.png", "Default avatar");
        }

        avatarImg.setWidth("50px");
        avatarImg.setHeight("50px");
        avatarImg.getStyle()
                .set("border-radius", "50%")
                .set("border", "1px solid #ffffff")
                .set("background-color", "white");

// --- Upload component ---
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/png", "image/jpeg");
        upload.addSucceededListener(event -> {
            try (InputStream input = buffer.getInputStream()) {
                byte[] imageBytes = input.readAllBytes();
                currentUser.setAvatarData(imageBytes);
                userService.save(currentUser);

                // Update UI immediately
                StreamResource resource = new StreamResource("avatar.png",
                        () -> new ByteArrayInputStream(imageBytes));
                avatarImg.setSrc(resource);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });


        // --- Top row: Avatar + username ---
        HorizontalLayout topRow = new HorizontalLayout();
        Span name = new Span(currentUser.getUsername());
        name.getStyle().set("color", "#ffffff").set("font-weight", "bold");
        topRow.add(avatarImg, name);

        // Optionally, add the upload button next to avatar
        topRow.add(upload);

        // --- Stats row: posts + likes ---
        HorizontalLayout statsRow = new HorizontalLayout();
        Span postCount = new Span("Posts: " + currentUser.getPostCount());
        postCount.getStyle().set("color", "#ffffff");
        Span likeCount = new Span("Likes: " + currentUser.getLikeCount());
        likeCount.getStyle().set("color", "#ffffff");
        statsRow.add(postCount, likeCount);

        // --- TextArea for post content ---
        TextArea postArea = new TextArea();
        postArea.getElement().getStyle()
                .set("background-color", "#1a1a1b")
                .set("color", "#A0B3B6")
                .set("caret-color", "#d3e3fd")
                .set("border", "1px solid #6c7a89")
                .set("border-radius", "8px")
                .set("padding", "8px");

        UserPost userPost = new UserPost();
        userPost.applySimulatedPlaceholder(postArea, "What's on your mind?", "#A0B3B6");
        postArea.setWidthFull();
        postArea.setHeight("120px");

        Button postButton = new Button("Post", ev -> {
            if (currentUser != null && !postArea.isEmpty()) {
                LocalDateTime now = LocalDateTime.now();
                String content = postArea.getValue();

                // Save post to the selected forum
                PostEntity newPost = new PostEntity();
                newPost.setUserName(currentUser.getUsername());
                newPost.setPostContent(content);
                newPost.setTimestamp(now.format(formatter));
                newPost.setParentId(0L);
                newPost.setLikes(0);
                newPost.setLikedUsers("");

                // Ensure currentForum is not null
                if (currentForum == null) {
                    System.out.println("WARNING: currentForum is null, defaulting to 'All'");
                    currentForum = forumService.findByName("all").orElse(null);
                }
                newPost.setForumId(currentForum.getId());

                System.out.println("Posting to forum: " + currentForum.getName() + " (ID=" + currentForum.getId() + ")");
                postService.save(newPost);

                // Also post a copy to "all" forum (ID 4) if it's not already "all"
                if (!currentForum.getId().equals(4L)) {
                    PostEntity allPost = new PostEntity();
                    allPost.setUserName(currentUser.getUsername());
                    allPost.setPostContent(content);
                    allPost.setTimestamp(now.format(formatter));
                    allPost.setParentId(0L);
                    allPost.setLikes(0);
                    allPost.setLikedUsers("");
                    allPost.setForumId(4L);
                    postService.save(allPost);
                }

                // Update user's post count
                currentUser.setPostCount(currentUser.getPostCount() + 1);
                userService.save(currentUser);

                postArea.clear();
                loadPostsByForum(currentForum.getId()); // refresh posts
            }
        });




        postButton.getStyle()
                .set("background-color", "#E0E0E0")
                .set("color", "#333333")
                .set("border", "none")
                .set("border-radius", "4px")
                .set("font-weight", "bold")
                .set("box-shadow", "none");

        // --- Add components to layout ---
        postLayout.add(topRow, statsRow, postArea, postButton);
        return postLayout;
    }
    public VerticalLayout createReplyCardDB(PostEntity postData) {
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

        UserEntity currentUser = VaadinSession.getCurrent().getAttribute(UserEntity.class);

        // --- Parent post info ---
        Optional<PostEntity> parentPostOpt = postService.findById(postData.getParentId());
        if (parentPostOpt.isPresent()) {
            PostEntity parentPost = parentPostOpt.get();

            HorizontalLayout topRow = new HorizontalLayout();
            String parentUsername = parentPost.getUserName();

            // Parent avatar
            Image parentAvatarImg = new Image();
            parentAvatarImg.setWidth("50px");
            parentAvatarImg.setHeight("50px");
            parentAvatarImg.getStyle()
                    .set("border-radius", "50%")
                    .set("border", "1px solid #fff")
                    .set("object-fit", "cover");

            userService.getAvatarData(parentUsername).ifPresentOrElse(
                    data -> {
                        StreamResource resource = new StreamResource("avatar.png", () -> new ByteArrayInputStream(data));
                        parentAvatarImg.setSrc(resource);
                    },
                    () -> parentAvatarImg.setSrc("/images/default-avatar.png")
            );

            // Avatar click filters posts
            parentAvatarImg.getElement().addEventListener("click", e -> {
                List<PostEntity> userPosts = postService.findPostsByUser(parentUsername);
                userPosts.sort(Comparator.comparing(PostEntity::getId).reversed());
                List<Object> items = new ArrayList<>();
                items.add(createPostInputCardDB());
                items.addAll(userPosts);
                dbPostList.setItems(items);
            });

            // Parent username span
            Span parentUserSpan = new Span(parentUsername);
            parentUserSpan.getStyle()
                    .set("color", "#fff")
                    .set("text-decoration", "underline")
                    .set("cursor", "pointer");
            parentUserSpan.getElement().addEventListener("click", e -> {
                List<PostEntity> userPosts = postService.findPostsByUser(parentUsername);
                userPosts.sort(Comparator.comparing(PostEntity::getId).reversed());
                List<Object> items = new ArrayList<>();
                items.add(createPostInputCardDB());
                items.addAll(userPosts);
                dbPostList.setItems(items);
            });

            Span parentTime = new Span("Posted on: " + Post.formatTimestamp(parentPost.getTimestamp()));
            parentTime.getStyle().set("color", "#ffffff");

            topRow.add(parentAvatarImg, parentUserSpan, parentTime);
            topRow.setWidthFull();
            topRow.setAlignItems(Alignment.CENTER);


            Div parentContent = new Div();
            parentContent.setText(parentPost.getPostContent());
            parentContent.getStyle()
                    .set("white-space", "pre-wrap")
                    .set("overflow-wrap", "break-word")
                    .set("word-break", "break-word")
                    .set("max-width", "750px")
                    .set("border", "1px solid #ccc")
                    .set("border-radius", "5px")
                    .set("padding", "10px")
                    .set("margin-bottom", "10px")
                    .set("color", "#fff");

            HorizontalLayout parentLikesRow = new HorizontalLayout();
            parentLikesRow.setWidth("750px");
            parentLikesRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
            parentLikesRow.setAlignItems(Alignment.CENTER);

            Span parentLikesCount = new Span("Liked: " + parentPost.getLikes());
            parentLikesCount.getStyle().set("font-size", "14px").set("color", "#fff");

            parentLikesRow.add(parentLikesCount, new LikeButtonDB(parentPost, postService, userService));

            replyCardLayout.add(topRow, parentContent, parentLikesRow);
        }

        // --- Reply user info ---
        String replyUsername = postData.getUserName();

        Image replyAvatarImg = new Image();
        replyAvatarImg.setWidth("50px");
        replyAvatarImg.setHeight("50px");
        replyAvatarImg.getStyle()
                .set("border-radius", "50%")
                .set("border", "1px solid #fff")
                .set("object-fit", "cover");

        userService.getAvatarData(replyUsername).ifPresentOrElse(
                data -> {
                    StreamResource resource = new StreamResource("avatar.png", () -> new ByteArrayInputStream(data));
                    replyAvatarImg.setSrc(resource);
                },
                () -> replyAvatarImg.setSrc("/images/default-avatar.png")
        );

        // Avatar click filters posts
        replyAvatarImg.getElement().addEventListener("click", e -> {
            List<PostEntity> userPosts = postService.findPostsByUser(replyUsername);
            userPosts.sort(Comparator.comparing(PostEntity::getId).reversed());
            List<Object> items = new ArrayList<>();
            items.add(createPostInputCardDB());
            items.addAll(userPosts);
            dbPostList.setItems(items);
        });

        Span replyUserSpan = new Span(replyUsername);
        replyUserSpan.getStyle()
                .set("color", "#fff")
                .set("text-decoration", "underline")
                .set("cursor", "pointer");
        replyUserSpan.getElement().addEventListener("click", e -> {
            List<PostEntity> userPosts = postService.findPostsByUser(replyUsername);
            userPosts.sort(Comparator.comparing(PostEntity::getId).reversed());
            List<Object> items = new ArrayList<>();
            items.add(createPostInputCardDB());
            items.addAll(userPosts);
            dbPostList.setItems(items);
        });

        Span replyTime = new Span("Posted on: " + Post.formatTimestamp(postData.getTimestamp()));
        replyTime.getStyle().set("color", "#fff");

        HorizontalLayout replyMeta = new HorizontalLayout(replyAvatarImg, replyUserSpan, replyTime);
        replyMeta.setWidthFull();
        replyMeta.setAlignItems(Alignment.CENTER);
        replyMeta.getStyle().set("margin-left", "50px");

        // --- Reply status field + save button ---
        TextField replyStatusField = new TextField();
        replyStatusField.setWidth("200px");
        replyStatusField.getStyle()
                .set("color", "white")
                .set("margin-left", "50px")
                .set("margin-top", "4px")
                .set("text-align", "center");

        Button saveStatusButton = new Button("Save", ev -> {
            if (currentUser != null && replyUsername.equals(currentUser.getUsername())) {
                userService.findByUsername(currentUser.getUsername()).ifPresent(user -> {
                    user.setStatus(replyStatusField.getValue());
                    userService.save(user);
                    Notification.show("Status saved").setPosition(Notification.Position.TOP_CENTER);
                });
            }
        });
        saveStatusButton.getStyle().set("margin-left", "50px");
        saveStatusButton.setVisible(replyUsername.equals(currentUser.getUsername()));

        userService.findByUsername(replyUsername).ifPresent(user -> {
            replyStatusField.setValue(user.getStatus() != null ? user.getStatus() : "");
        });

        replyStatusField.setReadOnly(!replyUsername.equals(currentUser.getUsername()));

        // --- Reply content ---
        Div replyContent = new Div();
        replyContent.setText(postData.getPostContent());
        replyContent.getStyle()
                .set("white-space", "pre-wrap")
                .set("overflow-wrap", "break-word")
                .set("word-break", "break-word")
                .set("max-width", "750px")
                .set("margin-left", "50px")
                .set("color", "#fff");

        // --- Likes row for reply ---
        HorizontalLayout replyLikesRow = new HorizontalLayout();
        replyLikesRow.setWidth("700px");
        replyLikesRow.setAlignItems(Alignment.CENTER);
        replyLikesRow.getStyle().set("margin-left", "50px");

        Span likesCount = new Span("Liked: " + postData.getLikes());
        likesCount.getStyle().set("font-size", "14px").set("color", "#fff");

        replyLikesRow.add(likesCount, new LikeButtonDB(postData, postService, userService));

        // --- Assemble ---
        replyCardLayout.add(replyMeta, replyContent, replyLikesRow);
        replyCardLayout.add(createReplyInputSectionDB(postData));

        return replyCardLayout;
    }

    private HorizontalLayout createReplyInputSectionDB(PostEntity parentPost) {
        TextField replyField = new TextField();
        replyField.setPlaceholder("Reply to " + parentPost.getUserName() + "...");

        Button replyButton = new Button("Reply", e -> {
            String replyContent = replyField.getValue().trim();
            if (!replyContent.isEmpty()) {
                UserEntity currentUser = VaadinSession.getCurrent().getAttribute(UserEntity.class);
                if (currentUser != null) {
                    PostEntity replyPost = new PostEntity();
                    replyPost.setUserName(currentUser.getUsername());
                    replyPost.setPostContent(replyContent);
                    replyPost.setTimestamp(LocalDateTime.now().format(formatter));
                    replyPost.setParentId(parentPost.getId());
                    replyPost.setParentUser(parentPost.getUserName());
                    replyPost.setLikes(0);

                    replyPost.setForumId(currentForum.getId());
                    postService.save(replyPost);
                    replyField.clear();
                    loadPostsByForum(currentForum.getId());
                } else {
                    Notification.show("No logged-in user to post reply");
                }
            }
        });

        HorizontalLayout replyLayout = new HorizontalLayout(replyField, replyButton);
        replyLayout.setWidthFull();
        return replyLayout;
    }




}



