package org.vaadin.example.social;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.Route;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Route("tree")
public class TreeTest extends VerticalLayout {

    public TreeTest() {
        Path forumsPath = Paths.get("C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/Forum");
        System.out.println("Starting tree construction from path: " + forumsPath);

        List<Node> forumNodes = new ArrayList<>();
        try {
            forumNodes = Files.list(forumsPath)
                    .filter(Files::isDirectory)
                    .map(forumFolder -> {
                        System.out.println("\nProcessing forum folder: " + forumFolder.getFileName());
                        Node forumNode = new Node(forumFolder.getFileName().toString());

                        // Process Admin folder (second level)
                        Path adminPath = forumFolder.resolve("Admin");
                        if (Files.exists(adminPath) && Files.isDirectory(adminPath)) {
                            Node adminNode = new Node("Admin");

                            // Process subfolders in Admin
                            processSubfolders(adminPath, adminNode);

                            // Process Moderator folder (third level under Admin)
                            Path moderatorPath = adminPath.resolve("Moderator");
                            if (Files.exists(moderatorPath) && Files.isDirectory(moderatorPath)) {
                                Node moderatorNode = new Node("Moderator");

                                // Process subfolders in Moderator
                                processSubfolders(moderatorPath, moderatorNode);

                                adminNode.getChildren().add(moderatorNode);
                            }

                            forumNode.getChildren().add(adminNode);
                        }

                        System.out.println("Final forum node: " + forumNode.getName() +
                                " has " + forumNode.getChildren().size() + " children");
                        return forumNode;
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error reading forums directory: " + e.getMessage());
            e.printStackTrace();
        }

        // Debug the complete node structure
        System.out.println("\nFinal node structure:");
        forumNodes.forEach(this::printNodeStructure);

        // Create TreeGrid
        TreeGrid<Node> treeGrid = new TreeGrid<>();
        treeGrid.addHierarchyColumn(Node::getName).setHeader("Forum Structure");

        // Set the items with hierarchy
        treeGrid.setItems(forumNodes, Node::getChildren);

        // Expand all nodes by default for testing
        expandAllNodes(treeGrid, forumNodes);

        add(treeGrid);
        setSizeFull();
    }

    private void processSubfolders(Path parentPath, Node parentNode) {
        try {
            Files.list(parentPath)
                    .filter(Files::isDirectory)
                    .forEach(subfolder -> {
                        Node subfolderNode = new Node(subfolder.getFileName().toString());
                        System.out.println("  Processing subfolder: " + subfolderNode.getName());

                        // Recursively process any nested subfolders
                        processSubfolders(subfolder, subfolderNode);

                        parentNode.getChildren().add(subfolderNode);
                    });
        } catch (IOException e) {
            System.err.println("Error reading subfolders: " + e.getMessage());
        }
    }

    private void expandAllNodes(TreeGrid<Node> treeGrid, List<Node> nodes) {
        nodes.forEach(node -> {
            treeGrid.expand(node);
            if (!node.getChildren().isEmpty()) {
                expandAllNodes(treeGrid, node.getChildren());
            }
        });
    }

    private void printNodeStructure(Node node, String indent) {
        System.out.println(indent + node.getName() +
                " (children: " + node.getChildren().size() + ")");
        node.getChildren().forEach(child -> printNodeStructure(child, indent + "  "));
    }

    private void printNodeStructure(Node node) {
        printNodeStructure(node, "");
    }

    public static class Node {
        private String name;
        private List<Node> children;

        public Node(String name) {
            this.name = name;
            this.children = new ArrayList<>();
            System.out.println("Created node: " + name);
        }

        public String getName() {
            return name;
        }

        public List<Node> getChildren() {
            return children;
        }
    }
}