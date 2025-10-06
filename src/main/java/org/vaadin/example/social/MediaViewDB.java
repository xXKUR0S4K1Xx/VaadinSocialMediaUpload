package org.vaadin.example.social;

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
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.server.VaadinSession;
import java.util.List;

@Route("mediadb")
public class MediaViewDB extends VerticalLayout {

    private final PostService postService;
    private final UserService userService;
    private final LikeService likeService;

    private List<PostEntity> dbPosts;
    private VirtualList<PostEntity> dbPostList;

    // Fields for reply tracking
    private Long replyToPostId = 0L;
    private String replyToUser = "";
    private TextArea contentField; // 👈 make this a field, not shadowed

    public MediaViewDB(PostService postService, UserService userService, LikeService likeService) {
        this.postService = postService;
        this.userService = userService;
        this.likeService = likeService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setSpacing(false);
        setMargin(false);
        setPadding(false);
        getStyle().set("background-color", "#1a1a1b");

        // === Get logged-in user from session ===
        UserEntity currentUser = VaadinSession.getCurrent().getAttribute(UserEntity.class);
        if (currentUser == null) {
            Notification.show("No logged-in user found");
            return;
        }

        // === Input card ===
        contentField = new TextArea(); // 👈 assign to field, not new local var
        contentField.setPlaceholder("Write something...");
        contentField.getStyle().set("color", "#fff");

        Button sendButton = new Button("Send", e -> {
            String content = contentField.getValue().trim();
            if (!content.isEmpty()) {
                PostEntity post = new PostEntity();
                post.setUserName(currentUser.getUsername());
                post.setPostContent(content);
                post.setTimestamp(java.time.LocalDateTime.now().toString());
                post.setLikes(0);

                // If replying to a post, set parent info
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
                contentField.setPlaceholder("Write something...");
            }
        });

        add(contentField, sendButton);

        // === Optional test like button ===
        Button testLikeButton = new Button("Test Like Post 1", e -> {
            PostEntity post = postService.findById(1L).orElse(null);
            if (post == null) {
                Notification.show("Post not found");
                return;
            }
            likeService.likePost(currentUser, post);
            Notification.show("Post liked! Current likes: " + post.getLikes());
            loadPosts();
        });
        add(testLikeButton);


        // === Post list ===
        // === Post list ===
        dbPostList = new VirtualList<>();
        dbPostList.setWidthFull();   // list takes full width
        dbPostList.setHeightFull();
        dbPostList.setRenderer(new ComponentRenderer<>(post -> {
            Component card = createCommentCardDB(post);
            card.getStyle().set("margin-bottom", "15px");
            card.getStyle().set("max-width", "850px");
            card.getStyle().set("width", "100%");

            HorizontalLayout wrapper = new HorizontalLayout();
            wrapper.setWidthFull();
            wrapper.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
            wrapper.add(card);

            return wrapper;
        }));

        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setWidthFull();
        mainLayout.setHeightFull();
        mainLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        mainLayout.add(dbPostList);

        add(mainLayout);

// Populate the list initially
        loadPosts();


    }


    /** Load posts from DB and refresh the virtual list */
    private void loadPosts() {
        dbPosts = postService.findAll();
        dbPosts.sort((a, b) -> b.getId().compareTo(a.getId())); // newest first
        dbPostList.setItems(dbPosts);
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
        HorizontalLayout topRow = new HorizontalLayout();
        String username = postData.getUserName();

        final String avatarUrl = userService.findByUsername(username)
                .map(user -> {
                    String url = user.getAvatarUrl();
                    return (url != null && !url.isEmpty()) ? url : "";
                })
                .orElse("");

        Avatar userAvatar = new Avatar();
        if (!avatarUrl.isEmpty()) {
            userAvatar.setImage(avatarUrl);
        }
        userAvatar.getStyle()
                .set("background-color", "white")
                .set("border", "1px solid #ffffff");

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

        topRow.add(userAvatar, userName, commentTime);
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
}



