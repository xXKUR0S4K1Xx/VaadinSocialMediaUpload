package org.vaadin.example.social;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Route("media")  // Defines the route for this view. When navigating to '/media', this view is displayed.
public class Media extends VerticalLayout {  // Main layout of the Media view. It extends VerticalLayout for vertical stacking of components.

    public Media() {  // Constructor that initializes the Media view.
        setSizeFull();  // Set the layout to take up the entire available space.
        setAlignItems(Alignment.CENTER);  // Align child components (like cards) to the center horizontally.
        setSpacing(false);  // Disable spacing between the components.
        setMargin(false);
        setPadding(false);

        VerticalLayout popoverContent = new VerticalLayout();
        Button logoutButton = new Button("Logout", event -> {
            // Implement logout logic here
            getUI().ifPresent(ui -> ui.navigate("login"));
        });
        popoverContent.add(logoutButton);

        String username = getLoggedInUsername();
        Avatar userAvatar = new Avatar(username);
        userAvatar.getElement().getStyle().set("background-color", "#fff");  // white background

        // Create the popover once outside the click event
        Popover popover = new Popover();
        popover.setTarget(userAvatar);
        popover.setPosition(PopoverPosition.BOTTOM);
        popover.addThemeVariants(PopoverVariant.ARROW, PopoverVariant.LUMO_NO_PADDING);
        popover.add(popoverContent);  // Add content to the popover only once

        // Create a variable to track if the popover is already opened
        boolean[] isOpened = {false};

        // Handle the click event to toggle the popover visibility
        userAvatar.getElement().addEventListener("click", event -> {
            if (!isOpened[0]) {
                // Open the popover if it's not opened
                popover.setOpened(true);
                isOpened[0] = true;

                // Set a timeout to keep the popover open for at least 1 second
                getUI().ifPresent(ui -> ui.access(() -> {
                    // After 1 second, allow the popover to be closed again
                    new Timer().schedule(new TimerTask() {
                        public void run() {
                            // Check if the popover is still open, then allow closing
                            if (popover.isOpened()) {
                                // Do nothing if it's already open, just wait for the close action
                            }
                        }
                    }, 1000);  // Delay for 1 second (1000 milliseconds)
                }));
            } else {
                // If the popover is already opened, close it when clicked again
                popover.setOpened(false);
                isOpened[0] = false;
            }
        });

        Icon notificationBell = new Icon(VaadinIcon.BELL);
        notificationBell.getElement().getStyle()
                .set("color", "#fff")  // Make the bell white for contrast
                .set("font-size", "24px");  // Adjust the size of the bell if needed

        // Create the search bar (TextField)
        TextField searchField = new TextField();
        searchField.setPlaceholder("Search Communo");  // Placeholder text
        searchField.addClassName("media-textfield");

// Apply styles to the outer TextField container
        searchField.getElement().getStyle()
                .set("background-color", "#87CEEB")  // Sky Blue for the outer container
                .set("color", "black")               // Text color inside the field (dark for contrast)
                .set("border-radius", "20px")        // Rounded corners for the container
                .set("width", "300px")               // Width of the search bar
                .set("border", "none")               // Remove the default border
                .set("padding", "0 15px")            // Padding inside the field
                .set("font-size", "12px");           // Set font size to 14px (adjust as needed)

// Apply styles to the inner input element to ensure it matches the outer container
        searchField.getElement().getChildren()
                .filter(child -> child.getTag().equals("input"))  // Find the <input> element
                .forEach(input -> input.getStyle()
                        .set("background-color", "#87CEEB")  // Sky Blue for the inner input field (to match the container)
                        .set("border-radius", "20px")        // Rounded corners for the input field (to match container)
                        .set("border", "none")               // Remove default border from the input field
                        .set("color", "black")               // Text color in the input (dark for contrast)
                        .set("padding", "0 15px")            // Padding inside the input field
                );



        // Fancy "Communo" title
        Span title = new Span("Communo");
        title.getStyle()
                .set("font-family", "'Segoe Script', cursive")
                .set("font-size", "28px")
                .set("font-weight", "bold")
                .set("color", "#FFFFFF");  // Set headline color to white

        // Create root layout
        HorizontalLayout rootLayout = new HorizontalLayout();
        rootLayout.setWidthFull();
        rootLayout.setPadding(false);
        rootLayout.setMargin(false);


        // Apply baby blue background without affecting text color
        rootLayout.getStyle()
                .set("background-color", "#ADD8E6")  // Baby blue background
                .set("color", "#333");  // Text remains dark grey, unaffected by the background

        // Sub-layouts for left, center, and right
        HorizontalLayout leftLayout = new HorizontalLayout(title);
        leftLayout.setJustifyContentMode(JustifyContentMode.START);
        leftLayout.setWidthFull();
        leftLayout.getElement().getStyle().set("margin-left", "20px");
        leftLayout.getStyle().set("color", "#333");  // Text color stays dark grey

        HorizontalLayout centerLayout = new HorizontalLayout(searchField);
        centerLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        centerLayout.setAlignItems(Alignment.CENTER);
        centerLayout.setWidthFull();
        centerLayout.getStyle().set("color", "#333");  // Text color stays dark grey

        HorizontalLayout rightLayout = new HorizontalLayout(notificationBell, userAvatar);
        rightLayout.setJustifyContentMode(JustifyContentMode.END);
        rightLayout.setAlignItems(Alignment.CENTER);
        rightLayout.setWidthFull();
        rightLayout.getElement().getStyle().set("margin-right", "20px");
        rightLayout.getStyle().set("color", "#333");  // Text color stays dark grey

        // Add all sub-layouts to root
        rootLayout.add(leftLayout, centerLayout, rightLayout);
        rootLayout.setFlexGrow(1, leftLayout, centerLayout, rightLayout);

        // Create the input card ONCE. This is the first element of the list.
        Component inputCard = createPostInputCard();  // Create the input card for adding posts or replies.
        inputCard.getElement().getStyle().set("width", "800px");  // Set the width of the input card to 800px.
        inputCard.getElement().getStyle().set("margin-left", "auto");  // Center the card horizontally.
        inputCard.getElement().getStyle().set("margin-right", "auto");  // Center the card horizontally.
        inputCard.getElement().getStyle().set("margin-top", "15px");

        // Virtual List of posts
        VirtualList<Object> postList = new VirtualList<>();  // A VirtualList for rendering posts (efficient large data handling).
        postList.getElement().getStyle().set("scrollbar-gutter", "stable both-edges");  // Ensures the scrollbar appears on both edges.
        postList.getElement().getStyle()
                .set("padding", "0")
                .set("margin", "0");
        // Create list: first the post input card, then the posts
        List<Object> items = new ArrayList<>();  // Create a list to store the input card and posts.
        items.add(inputCard);  // Add the input card to the list.
        items.addAll(UserPost.readPostsFromFiles());  // Add posts read from files.

        postList.setItems(items);  // Set the list of items (input card + posts) to the VirtualList.
        postList.getElement().getStyle().set("overflow", "hidden");


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
                // Set styles for the card (no background color interference).
                card.getElement().getStyle().set("margin", "0 auto");
                card.getElement().getStyle().set("width", "800px");
                card.getElement().getStyle().set("margin-top", "10px");
                return card;
            } else {
                return new Span("Unknown item");  // Default message if item is unknown.
            }
        }));

        postList.setWidthFull();  // Make the list take up full width.
        postList.setHeightFull();  // Make the list take up full height.

        // Wrapper layout for holding the content
        VerticalLayout content = new VerticalLayout();  // A container to hold the post list.
        content.setWidthFull();  // Make the container take up full width.
        content.setHeight("100%");  // Make the container take up the remaining height (95%).
        content.setPadding(false);  // Remove padding from the container.
        content.setSpacing(true);  // Add spacing between components inside the container.
        content.setDefaultHorizontalComponentAlignment(Alignment.CENTER);  // Center-align the components horizontally.


        content.add(postList);  // Add the post list to the content layout.
        content.setFlexGrow(1, postList);  // Make the post list grow to fill available space.

        HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeFull();  // Make the layout stretch to fill the available width
        layout.setAlignItems(Alignment.CENTER);  // Center the content vertically within the layout


// Create the sidebar layout
        VerticalLayout sideBar = new VerticalLayout();
        sideBar.setHeightFull();
        sideBar.setWidth("25px"); // Sidebar width

// Create individual horizontal layouts with avatars and text
        HorizontalLayout avatarRow1 = new HorizontalLayout();
        Avatar avatar1 = new Avatar("User 1");
        Div text1 = new Div();
        text1.setText("Home");  // Setting the text inside the Div
        avatarRow1.add(avatar1, text1);
        avatarRow1.setWidthFull();
        avatarRow1.setPadding(false);
        avatarRow1.setSpacing(true);

        HorizontalLayout avatarRow2 = new HorizontalLayout();
        Avatar avatar2 = new Avatar("User 2");
        Div text2 = new Div();
        text2.setText("Popular");
        avatarRow2.add(avatar2, text2);
        avatarRow2.setWidthFull();
        avatarRow2.setPadding(false);
        avatarRow2.setSpacing(true);

        HorizontalLayout avatarRow3 = new HorizontalLayout();
        Avatar avatar3 = new Avatar("User 3");
        Div text3 = new Div();
        text3.setText("For you");
        avatarRow3.add(avatar3, text3);
        avatarRow3.setWidthFull();
        avatarRow3.setPadding(false);
        avatarRow3.setSpacing(true);

        HorizontalLayout avatarRow4 = new HorizontalLayout();
        Avatar avatar4 = new Avatar("User 4");
        Div text4 = new Div();
        text4.setText("All");
        avatarRow4.add(avatar4, text4);
        avatarRow4.setWidthFull();
        avatarRow4.setPadding(false);
        avatarRow4.setSpacing(true);

// Add the horizontal layouts to the sidebar
        sideBar.add(avatarRow1, avatarRow2, avatarRow3, avatarRow4);


        VerticalLayout filler = new VerticalLayout();
        filler.setHeightFull();
        filler.setWidth("25");
        Button button2 = new Button("test");
        filler.add(button2);

        layout.add(sideBar, content, filler);

        add(rootLayout, layout);  // Add root layout and content to the view.


    }



    // Define the Comment Card
    public VerticalLayout createCommentCard(Post postData) {
        // Create the container for the card layout
        VerticalLayout commentCardLayout = new VerticalLayout();

        commentCardLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        commentCardLayout.setSpacing(true);
        commentCardLayout.setPadding(true);
        commentCardLayout.getStyle()
                .set("border", "1px solid #ccc")
                .set("border-radius", "10px")  // ✅ add smooth edges
                .set("padding", "10px")
                .set("margin", "0");


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
                .set("width", "725px"); // ✅ 700px for comment card

// Likes count
        Span likesCount = new Span("Liked: " + postData.getLikes());
        likesCount.getStyle()
                .set("font-size", "14px")
                .set("color", "#888")
                .set("white-space", "nowrap");


// Like button avatar

        LikeButton likeButton = new LikeButton(postData);
        likesRow.add(likesCount, likeButton);
        // Add everything to the layout
        commentCardLayout.add(topRow, commentContent, likesRow);

        UserPost userPostInstance = new UserPost();
        commentCardLayout.add(userPostInstance.createReplyInputSection(postData));

        return commentCardLayout;
    }

    // Define the Reply Card
    public VerticalLayout createReplyCard(Post postData) {
// Reply card layout container
        VerticalLayout replyCardLayout = new VerticalLayout();
        replyCardLayout.setAlignItems(Alignment.START);
        replyCardLayout.setSpacing(true);
        replyCardLayout.setPadding(true);
        replyCardLayout.setWidth("800px");
        replyCardLayout.getStyle()
                .set("border", "1px solid #ccc")
                .set("border-radius", "10px")  // ✅ add smooth edges
                .set("padding", "10px")
                .set("background-color", "#f8f8f8");


        Post parentPost = UserPost.findPostById(postData.getParentId());

// 🔝 Parent post info
        HorizontalLayout topRow = new HorizontalLayout();
        Avatar originalAvatar = new Avatar(parentPost != null ? parentPost.getUserName() : "Unknown");
        Span originalPoster = new Span(parentPost != null ? parentPost.getUserName() : "Unknown");
        Span originalTime = new Span("Posted on: " + Post.formatTimestamp(parentPost != null ? parentPost.getTimestamp() : postData.getTimestamp()));
        topRow.add(originalAvatar, originalPoster, originalTime);
        topRow.setWidthFull();
        topRow.setJustifyContentMode(JustifyContentMode.START);
        topRow.setAlignItems(Alignment.CENTER);

// 💬 Parent post content
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

// ❤️ Likes row for parent post
        HorizontalLayout parentLikesRow = new HorizontalLayout();
        if (parentPost != null) {
            parentLikesRow.setWidth("750px");
            parentLikesRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
            parentLikesRow.setAlignItems(Alignment.CENTER);

            Span parentLikesCount = new Span("Liked: " + parentPost.getLikes());
            parentLikesCount.getStyle()
                    .set("font-size", "14px")
                    .set("color", "#888")
                    .set("white-space", "nowrap");

            HorizontalLayout parentLikeButtonWrapper = new HorizontalLayout();
            parentLikeButtonWrapper.setWidthFull();
            parentLikeButtonWrapper.setJustifyContentMode(JustifyContentMode.END);
            parentLikeButtonWrapper.add(new LikeButton(parentPost));

            parentLikesRow.add(parentLikesCount, parentLikeButtonWrapper);
        }

// 🧱 Add parent post section
        replyCardLayout.add(topRow, originalPostContent);
        if (parentPost != null) replyCardLayout.add(parentLikesRow);

// 🔽 Reply input section to reply to the *parent*
        if (parentPost != null) {
            replyCardLayout.add(new UserPost().createReplyInputSection(parentPost));
        }

// 🔁 Reply meta info
        HorizontalLayout replyMeta = new HorizontalLayout();
        Avatar replyAvatar = new Avatar(postData.getUserName());
        Span replyUser = new Span(postData.getUserName());
        Span replyTime = new Span("Posted on: " + Post.formatTimestamp(postData.getTimestamp()));
        replyMeta.add(replyAvatar, replyUser, replyTime);
        replyMeta.setWidthFull();
        replyMeta.setJustifyContentMode(JustifyContentMode.START);
        replyMeta.setAlignItems(Alignment.CENTER);
        replyMeta.getStyle().set("margin-left", "50px");

// 📝 Reply content
        Div replyContent = new Div();
        replyContent.getStyle()
                .set("white-space", "pre-wrap")
                .set("overflow-wrap", "break-word")
                .set("word-break", "break-word")
                .set("max-width", "750px")
                .set("margin-left", "50px");
        replyContent.setText(postData.getPostContent());

// 👍 Likes row for reply post
        HorizontalLayout likesRow = new HorizontalLayout();
        likesRow.setWidth("700px");
        likesRow.setAlignItems(Alignment.CENTER);
        likesRow.getStyle().set("margin-left", "50px");

        Span likesCount = new Span("Liked: " + postData.getLikes());
        likesCount.getStyle()
                .set("font-size", "14px")
                .set("color", "#888")
                .set("white-space", "nowrap");

        HorizontalLayout buttonWrapper = new HorizontalLayout();
        buttonWrapper.setWidthFull();
        buttonWrapper.setJustifyContentMode(JustifyContentMode.END);
        buttonWrapper.add(new LikeButton(postData));

        likesRow.add(likesCount, buttonWrapper);

// Add the reply content
        replyCardLayout.add(replyMeta, replyContent, likesRow);

// 🔽 Reply input section to reply to this *reply*
        replyCardLayout.add(new UserPost().createReplyInputSection(postData));
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
        postLayout.getStyle().set("padding-top", "10px");


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
                UserPost.createAndSaveNewPost(postArea.getValue());
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