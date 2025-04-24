package org.vaadin.example.social;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route("media")
public class Media extends VerticalLayout {

    public Media() {
        setSizeFull(); // Make the whole Media view stretch full screen
        setAlignItems(Alignment.CENTER);
        setSpacing(false); // Optional: depends if you want spacing between UserCard and List

        // UserCard
        Component userCard = createPostInputCard();
        userCard.getElement().getStyle().set("width", "800px");
        userCard.getElement().getStyle().set("margin-left", "auto");
        userCard.getElement().getStyle().set("margin-right", "auto");

        // Virtual List of posts
        VirtualList<Post> postList = new VirtualList<>();
        postList.getElement().getStyle().set("scrollbar-gutter", "stable both-edges");

        List<Post> posts = UserPost.readPostsFromFiles();
        postList.setItems(posts);

        postList.setRenderer(new ComponentRenderer<>(post -> {
            Component card;
            if (post.getParentId().equals("0")) {
                card = createCommentCard(post);
            } else {
                card = createReplyCard(post);
            }
            card.getElement().getStyle().set("margin", "0 auto");
            card.getElement().getStyle().set("width", "800px");
            card.getElement().getStyle().set("margin-top", "10px");
            return card;
        }));

        postList.setWidthFull();
        postList.setHeightFull(); // ⬅️ important: list stretches vertically

        // Wrapper layout to control vertical growing
        VerticalLayout content = new VerticalLayout();
        content.setWidthFull();
        content.setHeightFull();
        content.setPadding(false);
        content.setSpacing(true);
        content.setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        content.add(userCard, postList);
        content.setFlexGrow(1, postList); // ⬅️ important: let postList grow vertically

        add(content);
    }



    // Define the Comment Card
    public VerticalLayout createCommentCard(Post postData) {
        // Create the container for the card layout
        VerticalLayout commentCardLayout = new VerticalLayout();

        commentCardLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        commentCardLayout.setSpacing(true);
        commentCardLayout.setPadding(true);
        commentCardLayout.getStyle().set("border", "1px solid #ccc");
        commentCardLayout.getStyle().set("padding", "10px");

        commentCardLayout.setWidth("800px");
        commentCardLayout.getStyle().set("background-color", "#f8f8f8");

        // Top: Avatar + User Name + Posted on: Date
        HorizontalLayout topRow = new HorizontalLayout();
        Avatar userAvatar = new Avatar(postData.getUserName());
        Span userName = new Span(postData.getUserName());
        Span commentTime = new Span("Posted on: " + Post.formatTimestamp(postData.getTimestamp()));
        topRow.add(userAvatar, userName, commentTime);
        topRow.setWidthFull(); // Make the row take full width
        topRow.setJustifyContentMode(JustifyContentMode.START); // Align content to start (left)
        topRow.setAlignItems(Alignment.CENTER); // Vertically center inside the row

        // Comment content (Textfield for the comment)
        Div commentContent = new Div();
        commentContent.setWidthFull(); // Make the row take full width
        commentContent.getStyle().set("text-align", "left");
        commentContent.getStyle()
                .set("white-space", "pre-wrap")
                .set("overflow-wrap", "break-word")
                .set("word-break", "break-word")
                .set("max-width", "750px"); // optional
        commentContent.setText(postData.getPostContent());

        // Display number of likes
        HorizontalLayout likesRow = new HorizontalLayout();
        likesRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
        likesRow.setAlignItems(Alignment.CENTER);
        likesRow.getStyle()
                .set("margin-top", "10px")
                .set("width", "700px"); // ✅ 700px for comment card

// Likes count
        Span likesCount = new Span("Liked: " + postData.getLikes());
        likesCount.getStyle()
                .set("font-size", "14px")
                .set("color", "#888");

// Like button avatar
        LikeButton likeButton = new LikeButton(postData);
        likesRow.add(likesCount, likeButton);
        // Add everything to the layout
        commentCardLayout.add(topRow, commentContent, likesRow);

        return commentCardLayout;
    }

    // Define the Reply Card
    // Define the Reply Card
    public VerticalLayout createReplyCard(Post postData) {
        VerticalLayout replyCardLayout = new VerticalLayout();
        replyCardLayout.setAlignItems(Alignment.START);
        replyCardLayout.setSpacing(true);
        replyCardLayout.setPadding(true);
        replyCardLayout.getStyle()
                .set("border", "1px solid #ccc")
                .set("padding", "10px")
                .set("background-color", "#f8f8f8");
        replyCardLayout.setWidth("800px");

        // 🔍 Find the parent post
        Post parentPost = UserPost.findPostById(postData.getParentId());

        // 🧍 Top: Parent Avatar + Username + Timestamp
        HorizontalLayout topRow = new HorizontalLayout();
        Avatar originalAvatar = new Avatar(parentPost != null ? parentPost.getUserName() : "Unknown");
        Span originalPoster = new Span(parentPost != null ? parentPost.getUserName() : "Unknown");
        Span originalTime = new Span("Posted on: " +
                Post.formatTimestamp(parentPost != null ? parentPost.getTimestamp() : postData.getTimestamp()));
        topRow.add(originalAvatar, originalPoster, originalTime);
        topRow.setWidthFull();
        topRow.setJustifyContentMode(JustifyContentMode.START);
        topRow.setAlignItems(Alignment.CENTER);

        // 💬 Original comment content in a box
        Div originalPostContent = new Div();
        originalPostContent.getStyle()
                .set("white-space", "pre-wrap")
                .set("overflow-wrap", "break-word")
                .set("word-break", "break-word")
                .set("max-width", "750px")
                .set("border", "1px solid #ccc")
                .set("border-radius", "5px")
                .set("padding", "10px")
                .set("margin-bottom", "10px");
        originalPostContent.setText(parentPost != null ? parentPost.getPostContent() : "Original post not found");

        // 🧑‍💬 Reply Meta: Avatar + Username + Time
        HorizontalLayout replyMeta = new HorizontalLayout();
        Avatar replyAvatar = new Avatar(postData.getUserName());
        Span replyUser = new Span(postData.getUserName());
        Span replyTime = new Span("Posted on: " + Post.formatTimestamp(postData.getTimestamp()));
        replyMeta.add(replyAvatar, replyUser, replyTime);
        replyMeta.setWidthFull();
        replyMeta.setJustifyContentMode(JustifyContentMode.START);
        replyMeta.setAlignItems(Alignment.CENTER);
        replyMeta.getStyle().set("margin-left", "50px");

        // 📝 Reply Content
        Div replyContent = new Div();
        replyContent.getStyle()
                .set("white-space", "pre-wrap")
                .set("overflow-wrap", "break-word")
                .set("word-break", "break-word")
                .set("max-width", "750px")
                .set("margin-left", "50px");
        replyContent.setText(postData.getPostContent());

        // ➡️ New: Likes + Like Button Layout for reply
        // ➡️ Likes + Like Button Layout for reply
        HorizontalLayout likesRow = new HorizontalLayout();
// ❌ likesRow.setWidthFull(); // REMOVE THIS LINE
        likesRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
        likesRow.setAlignItems(Alignment.CENTER);
        likesRow.getStyle()
                .set("margin-left", "50px")
                .set("width", "700px"); // ✅ set it manually to match reply content

// Likes count
        Span likesCount = new Span("Liked: " + postData.getLikes());
        likesCount.getStyle()
                .set("font-size", "14px")
                .set("color", "#888");

// Like button avatar
        LikeButton likeButton = new LikeButton(postData);

        likesRow.add(likesCount, likeButton);


        // ➕ Assemble everything
        replyCardLayout.add(topRow, originalPostContent, replyMeta, replyContent, likesRow);

        return replyCardLayout;
    }




    private Component createPostInputCard() {
        VerticalLayout postLayout = new VerticalLayout();
        postLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        postLayout.setSpacing(true);
        postLayout.setPadding(true);
        postLayout.getStyle().set("border", "1px solid #ccc");
        postLayout.getStyle().set("border-radius", "10px");
        postLayout.getStyle().set("padding", "20px");
        postLayout.setWidth("800px");
        postLayout.getStyle().set("background-color", "#f8f8f8");

        // Get current user
        String currentUsername = getLoggedInUsername();
        User user = User.loadFromFile(currentUsername);

        // Top: Avatar + Name
        HorizontalLayout topRow = new HorizontalLayout();
        Avatar avatar = new Avatar(currentUsername);
        Span name = new Span(currentUsername);
        name.getStyle().set("font-weight", "bold");
        topRow.add(avatar, name);

        // Second Row: Number of Posts + Likes
        HorizontalLayout statsRow = new HorizontalLayout();
        Span postCount = new Span("Posts: " + user.getPostCount());
        Span likeCount = new Span("Likes: " + user.getLikeCount());
        statsRow.add(postCount, likeCount);

        // Third Row: Text Field
        TextArea postArea = new TextArea();
        postArea.setWidthFull();
        postArea.setPlaceholder("What's on your mind?");
        postArea.setHeight("120px");

        // Fourth Row: Post Button
        Button postButton = new Button("Post", e -> {
            if (!postArea.isEmpty()) {
                UserPost.saveNewPost(postArea.getValue());
                postArea.clear();
                getUI().ifPresent(ui -> ui.getPage().reload()); // simple reload to refresh posts
            }
        });

        postLayout.add(topRow, statsRow, postArea, postButton);
        return postLayout;
    }

    private String getLoggedInUsername() {
        try {
            return java.nio.file.Files.readString(java.nio.file.Paths.get("loggedinuser.txt")).trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown";
        }
    }
}
