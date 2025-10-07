package org.vaadin.example.social;
import com.vaadin.flow.component.html.Image;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.listbox.ListBox;
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

// 👇 make it effectively final for use in lambdas later
        final UserEntity currentUser = sessionUser;

        // === Input card ===
        contentField = new TextArea();
        contentField.setPlaceholder("Write something...");
        contentField.getStyle().set("color", "#fff");

        Button sendButton = new Button("Send", e -> {
            String content = contentField.getValue().trim();
            if (!content.isEmpty()) {
                PostEntity post = new PostEntity();
                post.setUserName(currentUser.getUsername());
                post.setPostContent(content);
                post.setTimestamp(LocalDateTime.now().format(formatter));
                post.setLikes(0);

                if (replyToPostId != 0L) {
                    post.setParentId(replyToPostId);
                    post.setParentUser(replyToUser);
                } else {
                    post.setParentId(0L);
                    post.setParentUser(null);
                }

                postService.save(post);
                contentField.clear();
                replyToPostId = 0L;
                replyToUser = "";
                contentField.setPlaceholder("Write your post...");

                loadPosts(); // refresh list after posting
            }
        });

        // --- Create input card ---
        Component inputCard = createPostInputCardDB(); // your input card

// --- VirtualList for posts + input card ---
        dbPostList = new VirtualList<>();
        dbPostList.setWidthFull();
        dbPostList.setHeightFull();

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

// --- Layout ---
        VerticalLayout listWithInput = new VerticalLayout(dbPostList);
        listWithInput.setWidthFull();
        listWithInput.setHeightFull();
        listWithInput.setPadding(false);
        listWithInput.setSpacing(false);
        listWithInput.setAlignItems(FlexComponent.Alignment.CENTER);

        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setWidthFull();
        mainLayout.setHeightFull();
        mainLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        mainLayout.add(listWithInput);

        add(mainLayout);

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
        commentCardLayout.setAlignItems(FlexComponent.Alignment.CENTER);
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
// --- Top row ---
        HorizontalLayout topRow = new HorizontalLayout();
        String username = postData.getUserName();

        Image avatarImg = new Image();
        userService.getAvatarData(username).ifPresentOrElse(
                data -> {
                    StreamResource resource = new StreamResource("avatar.png", () -> new ByteArrayInputStream(data));
                    avatarImg.setSrc(resource);
                },
                () -> avatarImg.setSrc("/images/default-avatar.png") // fallback
        );

        avatarImg.setWidth("50px");
        avatarImg.setHeight("50px");
        avatarImg.getStyle()
                .set("border-radius", "50%")
                .set("border", "1px solid #ffffff")
                .set("background-color", "white");

        Span userName = new Span(username);
        userName.getStyle()
                .set("color", "#ffffff")
                .set("text-decoration", "underline")
                .set("cursor", "pointer");

        userName.getElement().addEventListener("click", event -> {
            VaadinSession.getCurrent().setAttribute("selectedUser", username);
            UI.getCurrent().navigate("userpage");
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

            Avatar parentAvatar = new Avatar();
            String parentAvatarUrl = userService.getAvatarUrl(parentUsername);
            parentAvatar.setImage(parentAvatarUrl);
            parentAvatar.getStyle().set("border", "1px solid #fff");

            Span parentUserSpan = new Span(parentUsername);
            parentUserSpan.getStyle()
                    .set("color", "#fff")
                    .set("text-decoration", "underline")
                    .set("cursor", "pointer");
            parentUserSpan.getElement().addEventListener("click", e -> userService.navigateToUser(parentUsername));

            Span parentTime = new Span("Posted on: " + Post.formatTimestamp(parentPost.getTimestamp()));
            parentTime.getStyle().set("color", "#ffffff");

            topRow.add(parentAvatar, parentUserSpan, parentTime);
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

            // Parent likes
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
        Avatar replyAvatar = new Avatar();
        replyAvatar.setImage(userService.getAvatarUrl(replyUsername));
        replyAvatar.getStyle().set("border", "1px solid #fff");

        Span replyUserSpan = new Span(replyUsername);
        replyUserSpan.getStyle()
                .set("color", "#fff")
                .set("text-decoration", "underline")
                .set("cursor", "pointer");
        replyUserSpan.getElement().addEventListener("click", e -> userService.navigateToUser(replyUsername));

        Span replyTime = new Span("Posted on: " + Post.formatTimestamp(postData.getTimestamp()));
        replyTime.getStyle().set("color", "#fff");

        HorizontalLayout replyMeta = new HorizontalLayout(replyAvatar, replyUserSpan, replyTime);
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
        new LikeButtonDB(postData, postService, userService);

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



