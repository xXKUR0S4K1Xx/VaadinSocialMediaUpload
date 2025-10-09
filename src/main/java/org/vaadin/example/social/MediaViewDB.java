package org.vaadin.example.social;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
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

    private final PostService postService;
    private final UserService userService;
    private final LikeService likeService;

    private List<PostEntity> dbPosts;
    private VirtualList<Object> dbPostList; // use Object to allow input card + posts

    private Long replyToPostId = 0L;
    private String replyToUser = "";
    private TextArea contentField;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public MediaViewDB(PostService postService, UserService userService, LikeService likeService) {
        this.postService = postService;
        this.userService = userService;
        this.likeService = likeService;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

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
        dbPostList.setRenderer(new ComponentRenderer<>(item -> {
            if (item instanceof Component c) {
                c.getStyle().set("margin", "0 auto");
                c.getStyle().set("width", "800px");
                return c;
            } else if (item instanceof PostEntity p) {
                Component card = (p.getParentId() == 0L)
                        ? createCommentCardDB(p)
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
        H2 logo = new H2("Semaino");
        logo.getStyle()
                .set("color", "#fff")
                .set("margin", "0")
                .set("font-weight", "bold");

// --- Center: Search field ---
        TextField searchField = new TextField();
        searchField.setPlaceholder("Search...");
        searchField.setWidth("20%"); // can adjust as needed
        searchField.setHeight("95%");
        searchField.getElement().getStyle().set("display", "flex");
        searchField.getElement().getStyle().set("align-items", "center");
        searchField.getStyle()
                .set("background-color", "#666")
                .set("color", "#fff")
                .set("border-radius", "20px")
                .set("border", "1px solid #555")
                .set("padding", "0 15px");

// --- Right: Bell + User avatar ---
// --- Right: Bell + User avatar ---
        HorizontalLayout rightLayout = new HorizontalLayout();
        rightLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        rightLayout.setSpacing(true);

// Bell icon
        Icon bell = VaadinIcon.BELL.create();
        bell.setSize("24px");
        bell.getStyle().set("color", "#fff").set("cursor", "pointer");

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
        topBar.add(logo, searchField, rightLayout);
        topBar.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, logo, searchField, rightLayout);

// --- Search functionality ---
        searchField.addValueChangeListener(event -> {
            String searchText = event.getValue().trim();
            if (searchText.isEmpty()) {
                loadPosts();
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
        forumLayout.getStyle()
                .set("border-right", "1px solid #333");
        forumLayout.setPadding(true);
        forumLayout.setSpacing(true);

        Select<String> forumSelect = new Select<>();
        forumSelect.setPlaceholder("Choose Forum");
        forumSelect.setWidthFull();
        forumSelect.getStyle().set("border", "1px solid white");

        TextField nonWritableBox = new TextField();
        nonWritableBox.setReadOnly(true);
        nonWritableBox.setWidthFull();
        nonWritableBox.getStyle().set("border", "1px solid white");

        Div createLabel = new Div();
        createLabel.setText("Create a new Forum");
        createLabel.getStyle()
                .set("font-weight", "bold")
                .set("margin-top", "10px");

        TextField writableBox = new TextField();
        writableBox.setWidthFull();
        writableBox.getStyle().set("border", "1px solid white");

        forumLayout.add(forumSelect, nonWritableBox, createLabel, writableBox);


// right layout for user info
        VerticalLayout userLayout = new VerticalLayout();
        userLayout.setWidth("10%");
        userLayout.setHeightFull();
        userLayout.getStyle().set("border-left", "1px solid #333");


// add both layouts to main layout
        mainLayout.add(forumLayout, listWithInput, userLayout);
        mainLayout.setFlexGrow(0, forumLayout);
        mainLayout.setFlexGrow(0, userLayout);
        mainLayout.setFlexGrow(1, listWithInput);


// use mainLayout as root conten

        add(topBar, mainLayout);

// --- Load posts initially ---
        loadPosts();
    }

    private void loadPosts() {
        if (dbPostList == null) return; // safety check
        dbPosts = postService.findAll();
        dbPosts.sort((a, b) -> b.getId().compareTo(a.getId())); // newest first

        List<Object> allItems = new ArrayList<>();
        allItems.add(createPostInputCardDB()); // input card at top
        allItems.addAll(dbPosts);

        dbPostList.setItems(allItems);
    }



    /** Create styled card for each post */
    private VerticalLayout createCommentCardDB(PostEntity postData) {
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
                () -> avatarImg.setSrc("/images/default-avatar.png") // fallback
        );

        // Avatar click navigates to the user's posts
        avatarImg.getElement().addEventListener("click", e -> {
            List<PostEntity> userPosts = postService.findPostsByUser(username);
            userPosts.sort(Comparator.comparing(PostEntity::getId).reversed());

            List<Object> items = new ArrayList<>();
            items.add(createPostInputCardDB());
            items.addAll(userPosts);
            dbPostList.setItems(items);
        });

        // Username span
        Span userName = new Span(username);
        userName.getStyle()
                .set("color", "#ffffff")
                .set("text-decoration", "underline")
                .set("cursor", "pointer");

        // Username click navigates to user's posts (same as avatar click)
        userName.getElement().addEventListener("click", e -> {
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

                    postService.save(replyPost);
                    replyField.clear();
                    loadPosts();
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

        // --- Post button ---
        Button postButton = new Button("Post", e -> {
            if (!postArea.isEmpty()) {
                PostEntity newPost = new PostEntity();
                newPost.setUserName(currentUser.getUsername());
                newPost.setPostContent(postArea.getValue());
                newPost.setTimestamp(LocalDateTime.now().format(formatter));
                newPost.setParentId(0L);
                newPost.setLikes(0);
                newPost.setLikedUsers("");

                postService.save(newPost);

                // Update user's post count
                currentUser.setPostCount(currentUser.getPostCount() + 1);
                userService.save(currentUser);

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

            // Click on parent avatar to filter posts by user
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

        replyCardLayout.add(replyMeta, replyContent, replyLikesRow);

        // --- Reply input section ---
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

                    postService.save(replyPost);
                    replyField.clear();
                    loadPosts(); // reload the list
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



