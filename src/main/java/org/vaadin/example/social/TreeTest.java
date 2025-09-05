package org.vaadin.example.social;

import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Route("treetest")
public class TreeTest extends VerticalLayout {

    public TreeTest() {
        // Create TreeGrid for displaying forum structure
        TreeGrid<Path> treeGrid = new TreeGrid<>();

        // Define columns
        treeGrid.addHierarchyColumn(path -> path.getFileName().toString())
                .setHeader("Forums");

        // Set items (root is Forum folder, children are subfolders)
        Path forumsPath = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/Forum");
        treeGrid.setItems(getChildren(forumsPath), this::getChildren);

        // Add to layout
        add(treeGrid);
    }

    // Get children folders, excluding "Descriptors"
    private List<Path> getChildren(Path path) {
        try {
            return Files.list(path)
                    .filter(Files::isDirectory)
                    .filter(p -> !p.getFileName().toString().equalsIgnoreCase("Descriptors"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return List.of();
        }
    }
}
