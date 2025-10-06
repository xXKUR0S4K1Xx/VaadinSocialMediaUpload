package org.vaadin.example.social;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
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
            System.out.println("MediaViewDB session user: " + (currentUser != null ? currentUser.getUsername() : "null"));
            Notification.show("No logged-in user found");
            return;
        }

        // === Input card ===
        TextArea contentField = new TextArea();
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
                loadPosts(contentField);
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
            loadPosts(contentField);
        });
        add(testLikeButton);

        // === Post list ===
        dbPostList = new VirtualList<>();
        dbPostList.setWidthFull();
        add(dbPostList);

        loadPosts(contentField); // pass contentField so reply buttons can update it
    }

    private void loadPosts(TextArea contentField) {
        dbPosts = postService.findAll();
        dbPosts.sort((a, b) -> b.getId().compareTo(a.getId())); // newest first

        dbPostList.setItems(dbPosts);
        dbPostList.setRenderer(new ComponentRenderer<>(post -> {
            VerticalLayout card = new VerticalLayout();
            card.getStyle().set("background-color", "#2a2a2b")
                    .set("color", "#fff")
                    .set("padding", "10px")
                    .set("border-radius", "8px");

            // Indent replies
            if (post.getParentId() != 0L) {
                card.getStyle().set("margin-left", "20px");
                card.add(new Span("Reply to " + post.getParentUser()));
            }

            card.add(new Span(post.getUserName()));
            card.add(new Span(post.getPostContent()));

            LikeButtonDB likeButton = new LikeButtonDB(post, postService, userService);
            card.add(likeButton);

            // --- Reply button ---
            Button replyButton = new Button("Reply", e -> {
                replyToPostId = post.getId();
                replyToUser = post.getUserName();
                contentField.setPlaceholder("Replying to " + replyToUser + "...");
                contentField.focus();
            });
            card.add(replyButton);

            return card;
        }));
    }
}

