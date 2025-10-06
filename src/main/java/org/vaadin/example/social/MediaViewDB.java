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
    private List<PostEntity> dbPosts;
    private VirtualList<PostEntity> dbPostList;

    public MediaViewDB(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;

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
        TextArea contentField = new TextArea();
        contentField.setPlaceholder("Write something...");
        contentField.getStyle().set("color", "#fff");

        Button saveButton = new Button("Post", e -> {
            String content = contentField.getValue().trim();
            if (!content.isEmpty()) {
                PostEntity post = new PostEntity();
                post.setUserName(currentUser.getUsername()); // use logged-in user
                post.setPostContent(content);
                post.setTimestamp(java.time.LocalDateTime.now().toString());
                post.setParentId(0L);
                post.setLikes(0);

                postService.save(post); // save to DB
                contentField.clear();

                loadPosts();
            }
        });

        add(contentField, saveButton);

        // === Post list ===
        dbPostList = new VirtualList<>();
        dbPostList.setWidthFull();
        add(dbPostList);

        // === Message input field ===
        TextField messageField = new TextField();
        messageField.setPlaceholder("Type your message...");
        messageField.setWidthFull();

        Button sendButton = new Button("Send", e -> {
            String content = messageField.getValue().trim();
            if (!content.isEmpty()) {
                PostEntity post = new PostEntity();
                post.setUserName(currentUser.getUsername()); // use logged-in user
                post.setPostContent(content);
                post.setTimestamp(java.time.LocalDateTime.now().toString());
                post.setParentId(0L);
                post.setLikes(0);

                postService.save(post);
                messageField.clear();
                loadPosts();
            }
        });

        HorizontalLayout inputLayout = new HorizontalLayout(messageField, sendButton);
        inputLayout.setWidthFull();
        add(inputLayout);

        loadPosts(); // initial load
    }

    private void loadPosts() {
        dbPosts = postService.findAll();
        dbPosts.sort((a, b) -> b.getId().compareTo(a.getId())); // newest first

        dbPostList.setItems(dbPosts);
        dbPostList.setRenderer(new ComponentRenderer<>(post -> {
            VerticalLayout card = new VerticalLayout();
            card.getStyle().set("background-color", "#2a2a2b")
                    .set("color", "#fff")
                    .set("padding", "10px")
                    .set("border-radius", "8px");
            card.add(new Span(post.getUserName()));
            card.add(new Span(post.getPostContent()));

            LikeButtonDB likeButton = new LikeButtonDB(post, postService, userService);
            card.add(likeButton);

            return card;
        }));
    }
}

