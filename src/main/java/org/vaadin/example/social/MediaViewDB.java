package org.vaadin.example.social;
import com.vaadin.flow.data.renderer.ComponentRenderer;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route("mediadb")
public class MediaViewDB extends VerticalLayout {

    private final PostService postService;
    private final UserService userService;           // <-- added UserService
    private List<PostEntity> dbPosts;                // replaces allPosts
    private VirtualList<PostEntity> dbPostList;      // replaces postList

    public MediaViewDB(PostService postService, UserService userService) { // <-- added param
        this.postService = postService;
        this.userService = userService;             // <-- assign field

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setSpacing(false);
        setMargin(false);
        setPadding(false);
        getStyle().set("background-color", "#1a1a1b");

        // === Input card ===
        TextField userField = new TextField();
        userField.setPlaceholder("Enter your username");  // ✅ real placeholder
        userField.getStyle().set("color", "#fff");  // white text
        userField.getElement().getStyle().set("--lumo-placeholder-text-color", "#ccc"); // light gray placeholder

        TextArea contentField = new TextArea();
        contentField.setPlaceholder("Write something...");
        contentField.getStyle().set("color", "#fff");

        contentField.getElement().executeJs(
                "this.inputElement.style.setProperty('color', '#fff', 'important');" +
                        "this.inputElement.style.setProperty('opacity', '1', 'important');" +
                        "this.inputElement.setAttribute('placeholder', this.getAttribute('placeholder'));"
        );
/
        Button saveButton = new Button("Post", e -> {
            PostEntity post = new PostEntity();
            post.setUserName(userField.getValue());
            post.setPostContent(contentField.getValue());
            post.setTimestamp(java.time.LocalDateTime.now().toString());
            post.setParentId(0L);
            post.setLikes(0);

            postService.save(post);       // save to DB
            contentField.clear();

            loadPosts();                  // refresh the post list
        });

        add(userField, contentField, saveButton);

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
                post.setUserName(userField.getValue()); // use same userField
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

        loadPosts();  // initial load
    }

    private void loadPosts() {
        dbPosts = postService.findAll();              // fetch from DB
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

