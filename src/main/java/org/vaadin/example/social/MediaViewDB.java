package org.vaadin.example.social;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.io.File;
import java.io.IOException;
import com.vaadin.flow.component.notification.Notification;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.UI;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.popover.PopoverVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("mediadb")
public class MediaViewDB extends VerticalLayout {

    public MediaViewDB(PostService postService) {

        TextField userField = new TextField("User");
        TextArea contentField = new TextArea("Post Content");

        Button saveButton = new Button("Post", e -> {
            // 📝 Neues Entity erzeugen
            PostEntity post = new PostEntity();
            post.setUserName(userField.getValue());
            post.setPostContent(contentField.getValue());
            post.setTimestamp(java.time.LocalDateTime.now().toString());
            post.setParentId(0L);  // kein Parent, also Top-Level Post
            post.setLikes(0);

            // 💾 In DB speichern
            postService.save(post);

            // 🧹 Felder zurücksetzen
            contentField.clear();
        });

        add(userField, contentField, saveButton);
    }
}
