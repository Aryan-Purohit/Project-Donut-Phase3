package application;

// Import necessary JavaFX and utility classes
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class UserInterface extends Application {

    private Stage window; // Primary stage for the application
    private Login loginInstance = Login.getInstance(); // Singleton instance of Login class
    private User currentUser; // Currently logged-in user
    private String currentGroup = "all"; // Current active group
    private String currentLevel = "All"; // Current content level

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setTitle("CSE 360 Help System");

        // Display the login screen when the application starts
        showLoginScreen();
    }

    // Method to display the login screen
    private void showLoginScreen() {
        VBox vbox = new VBox(10); // Vertical box layout with spacing of 10 pixels

        // Username input field
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        // Password input field
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        // Login button
        Button loginButton = new Button("Login");

        // Event handler for the login button
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            // If no users exist, register the first user as an Admin
            if (Login.getInstance().listUsers().isEmpty()) {
                User newUser = Login.getInstance().registerUser(username, password, "Admin", false, null);
                currentUser = newUser; // Set the current user
                showRegistrationScreen(newUser);
            } else {
                // Authenticate the user
                boolean isAuthenticated = Login.getInstance().authenticate(username, password);
                if (isAuthenticated) {
                    User user = Login.getInstance().findUser(username);
                    currentUser = user; // Set the current user
                    if (user != null && !user.isAccountSetupComplete()) {
                        // If account setup is incomplete, show the registration screen
                        showRegistrationScreen(user);
                    } else {
                        // Show role selection screen
                        showRoleSelectionScreen(user);
                    }
                } else {
                    System.out.println("Login failed. If this is a one-time password, it may have expired.");
                }
            }
        });

        // Add components to the layout
        vbox.getChildren().addAll(usernameField, passwordField, loginButton);

        // Set the scene and display the login screen
        Scene loginScene = new Scene(vbox, 400, 300);
        window.setScene(loginScene);
        window.show();
    }

    // Method to display the registration screen for account setup
    private void showRegistrationScreen(User user) {
        VBox vbox = new VBox(10);

        // Input fields for user details
        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");

        TextField middleNameField = new TextField();
        middleNameField.setPromptText("Middle Name");

        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");

        TextField preferredNameField = new TextField();
        preferredNameField.setPromptText("Preferred Name");

        // Map to store proficiency levels for topics
        Map<String, ComboBox<String>> topicComboBoxes = new HashMap<>();
        for (String topic : user.getTopics().keySet()) {
            // ComboBox for selecting proficiency level
            ComboBox<String> topicLevelBox = new ComboBox<>();
            topicLevelBox.getItems().addAll("Beginner", "Intermediate", "Advanced", "Expert");
            topicLevelBox.setValue(user.getTopicProficiency(topic)); // Set current proficiency level
            topicComboBoxes.put(topic, topicLevelBox);

            // Label and ComboBox for each topic
            Label topicLabel = new Label(topic);
            vbox.getChildren().addAll(topicLabel, topicLevelBox);
        }

        // Button to complete account setup
        Button registerButton = new Button("Complete Setup");

        // Event handler for the register button
        registerButton.setOnAction(e -> {
            // Set user details
            user.setEmail(emailField.getText());
            user.setFirstName(firstNameField.getText());
            user.setMiddleName(middleNameField.getText());
            user.setLastName(lastNameField.getText());
            user.setPreferredName(preferredNameField.getText());

            // Set proficiency levels for topics
            for (String topic : topicComboBoxes.keySet()) {
                user.setTopicProficiency(topic, topicComboBoxes.get(topic).getValue());
            }

            user.setAccountSetupComplete(true); // Mark account setup as complete
            System.out.println("Account setup completed.");
            showRoleSelectionScreen(user); // Proceed to role selection screen
        });

        // Add components to the layout
        vbox.getChildren().addAll(emailField, firstNameField, middleNameField, lastNameField, preferredNameField, registerButton);

        // Set the scene and display the registration screen
        Scene registerScene = new Scene(vbox, 400, 400);
        window.setScene(registerScene);
        window.show();
    }

    // Method to display the role selection screen
    private void showRoleSelectionScreen(User user) {
        VBox vbox = new VBox(10);

        // ComboBox to select the user's role
        ComboBox<String> roleDropdown = new ComboBox<>();
        roleDropdown.getItems().addAll(user.getRole()); // User's available roles
        roleDropdown.setPromptText("Select Role");

        // Button to confirm role selection
        Button selectButton = new Button("Select Role");

        // Event handler for the select button
        selectButton.setOnAction(e -> {
            String selectedRole = roleDropdown.getValue();
            System.out.println("Role Selected: " + selectedRole);
            if ("Admin".equalsIgnoreCase(selectedRole)) {
                showAdminDashboard(user); // Show admin dashboard
            } else if ("Instructor".equalsIgnoreCase(selectedRole)) {
                showInstructorDashboard(user); // Show instructor dashboard
            } else {
                showSimpleHomePage(user); // Show student home page
            }
        });

        // Add components to the layout
        vbox.getChildren().addAll(new Label("Select Role"), roleDropdown, selectButton);

        // Set the scene and display the role selection screen
        Scene roleScene = new Scene(vbox, 400, 200);
        window.setScene(roleScene);
        window.show();
    }

    // Method to display the student's home page
    private void showSimpleHomePage(User user) {
        currentUser = user; // Set the current user

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        Button logoutButton = new Button("Logout");

        // Search functionality components
        TextField searchField = new TextField();
        searchField.setPromptText("Enter keyword to search");

        // ComboBox for content level
        ComboBox<String> levelComboBox = new ComboBox<>();
        levelComboBox.getItems().addAll("All", "Beginner", "Intermediate", "Advanced", "Expert");
        levelComboBox.setValue("All");
        levelComboBox.setPromptText("Select Content Level");

        // ComboBox for group selection
        ComboBox<String> groupComboBox = new ComboBox<>();
        List<String> groupOptions = new ArrayList<>(currentUser.getGroupNames());
        groupOptions.add(0, "all");
        groupComboBox.getItems().addAll(groupOptions);
        groupComboBox.setValue("all");
        groupComboBox.setPromptText("Select Group");

        Button searchButton = new Button("Search Articles");

        // Labels to display active group and number of articles matching each level
        Label activeGroupLabel = new Label("Active Group: all");
        Label articleCountLabel = new Label("Articles Matching Levels:");

        ListView<String> articlesListView = new ListView<>(); // ListView to display article titles

        // Map to associate sequence numbers with articles
        Map<Integer, User.HelpArticle> sequenceToArticleMap = new HashMap<>();

        // Event handler for the search button
        searchButton.setOnAction(e -> {
            String keyword = searchField.getText();
            String selectedLevel = levelComboBox.getValue();
            String selectedGroup = groupComboBox.getValue();

            currentLevel = selectedLevel;
            currentGroup = selectedGroup;

            activeGroupLabel.setText("Active Group: " + currentGroup);

            List<User.HelpArticle> results;
            if (keyword != null && !keyword.isEmpty()) {
                results = currentUser.searchHelpArticles(keyword.trim());
            } else {
                results = currentUser.getAllHelpArticles();
            }

            // Filter articles by group and level
            List<User.HelpArticle> filteredArticles = new ArrayList<>();
            for (User.HelpArticle article : results) {
                if (article.userHasAccess(currentUser)) {
                    boolean groupMatch = currentGroup.equals("all") || article.getGroups().contains(currentGroup);
                    boolean levelMatch = currentLevel.equals("All") || article.getLevel().equalsIgnoreCase(currentLevel);
                    if (groupMatch && levelMatch) {
                        filteredArticles.add(article);
                    }
                }
            }

            // Count articles by level
            Map<String, Integer> levelCounts = new HashMap<>();
            for (String level : Arrays.asList("Beginner", "Intermediate", "Advanced", "Expert")) {
                levelCounts.put(level, 0);
            }
            for (User.HelpArticle article : filteredArticles) {
                String level = article.getLevel();
                levelCounts.put(level, levelCounts.getOrDefault(level, 0) + 1);
            }

            // Update article count label
            StringBuilder countText = new StringBuilder("Articles Matching Levels:\n");
            for (String level : Arrays.asList("Beginner", "Intermediate", "Advanced", "Expert")) {
                countText.append(level).append(": ").append(levelCounts.get(level)).append("\n");
            }
            articleCountLabel.setText(countText.toString());

            // Display articles in short form
            articlesListView.getItems().clear();
            sequenceToArticleMap.clear();
            int sequenceNumber = 1;
            for (User.HelpArticle article : filteredArticles) {
                String itemText = sequenceNumber + ". Title: " + article.getTitle() + ", Author: " + article.getAuthor() + ", Abstract: " + article.getDescription();
                articlesListView.getItems().add(itemText);
                sequenceToArticleMap.put(sequenceNumber, article);
                sequenceNumber++;
            }
        });

        // Event handler for selecting an article to view details using sequence number
        articlesListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Double-click
                String selectedItem = articlesListView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    // Extract sequence number from the selected item
                    int dotIndex = selectedItem.indexOf(".");
                    if (dotIndex != -1) {
                        String sequenceStr = selectedItem.substring(0, dotIndex);
                        try {
                            int sequenceNumber = Integer.parseInt(sequenceStr.trim());
                            User.HelpArticle article = sequenceToArticleMap.get(sequenceNumber);
                            if (article != null) {
                                showArticleDetail(article);
                            }
                        } catch (NumberFormatException ex) {
                            System.out.println("Invalid sequence number.");
                        }
                    }
                }
            }
        });

        // Message sending components
        TextArea genericMessageArea = new TextArea();
        genericMessageArea.setPromptText("Enter your generic message here...");
        genericMessageArea.setPrefRowCount(2);

        Button sendGenericMessageButton = new Button("Send Generic Message");
        sendGenericMessageButton.setOnAction(e -> {
            String messageContent = genericMessageArea.getText();
            if (messageContent != null && !messageContent.isEmpty()) {
                currentUser.sendMessage("Generic Message: " + messageContent);
                genericMessageArea.clear();
                System.out.println("Generic message sent.");
            } else {
                System.out.println("Please enter a message to send.");
            }
        });

        TextArea specificMessageArea = new TextArea();
        specificMessageArea.setPromptText("Enter your specific message here...");
        specificMessageArea.setPrefRowCount(2);

        Button sendSpecificMessageButton = new Button("Send Specific Message");
        sendSpecificMessageButton.setOnAction(e -> {
            String messageContent = specificMessageArea.getText();
            if (messageContent != null && !messageContent.isEmpty()) {
                currentUser.sendMessage("Specific Message: " + messageContent);
                specificMessageArea.clear();
                System.out.println("Specific message sent.");
            } else {
                System.out.println("Please enter a message to send.");
            }
        });

        // Event handler for the logout button
        logoutButton.setOnAction(e -> {
            System.out.println("Logging out.");
            showLoginScreen(); // Return to login screen
        });

        // Layout for search options
        HBox searchOptions = new HBox(10);
        searchOptions.getChildren().addAll(new Label("Content Level:"), levelComboBox, new Label("Group:"), groupComboBox);

        // Add components to the layout
        vbox.getChildren().addAll(
                new Label("Student Home Page"),
                new Separator(),
                searchField,
                searchOptions,
                searchButton,
                activeGroupLabel,
                articleCountLabel,
                new Label("Search Results:"),
                articlesListView,
                new Separator(),
                new Label("Send a Generic Message to Instructors/Admins:"),
                genericMessageArea, sendGenericMessageButton,
                new Separator(),
                new Label("Send a Specific Message (e.g., request for help articles):"),
                specificMessageArea, sendSpecificMessageButton,
                new Separator(),
                logoutButton);

        // Set the scene and display the home page
        Scene homeScene = new Scene(vbox, 600, 800);
        window.setScene(homeScene);
        window.show();
    }

    // Method to display the instructor dashboard
    private void showInstructorDashboard(User user) {
        currentUser = user; // Set the current user

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        Button logoutButton = new Button("Logout");

        // Search functionality components (same as student)
        TextField searchField = new TextField();
        searchField.setPromptText("Enter keyword to search");

        // ComboBox for content level
        ComboBox<String> levelComboBox = new ComboBox<>();
        levelComboBox.getItems().addAll("All", "Beginner", "Intermediate", "Advanced", "Expert");
        levelComboBox.setValue("All");
        levelComboBox.setPromptText("Select Content Level");

        // ComboBox for group selection
        ComboBox<String> groupComboBox = new ComboBox<>();
        Set<String> allGroups = new HashSet<>();
        allGroups.add("all");
        for (Group group : loginInstance.listGroups()) {
            allGroups.add(group.getGroupName());
        }
        groupComboBox.getItems().addAll(allGroups);
        groupComboBox.setValue("all");
        groupComboBox.setPromptText("Select Group");

        Button searchButton = new Button("Search Articles");

        // Labels to display active group and number of articles matching each level
        Label activeGroupLabel = new Label("Active Group: all");
        Label articleCountLabel = new Label("Articles Matching Levels:");

        ListView<String> articlesListView = new ListView<>(); // ListView to display article titles
        articlesListView.setPrefHeight(200); // Set preferred height

        // Map to associate sequence numbers with articles
        Map<Integer, User.HelpArticle> sequenceToArticleMap = new HashMap<>();

        // Event handler for the search button
        searchButton.setOnAction(e -> {
            String keyword = searchField.getText();
            String selectedLevel = levelComboBox.getValue();
            String selectedGroup = groupComboBox.getValue();

            currentLevel = selectedLevel;
            currentGroup = selectedGroup;

            activeGroupLabel.setText("Active Group: " + currentGroup);

            List<User.HelpArticle> results;
            if (keyword != null && !keyword.isEmpty()) {
                results = currentUser.searchHelpArticles(keyword.trim());
            } else {
                results = currentUser.getAllHelpArticles();
            }

            // Filter articles by group and level
            List<User.HelpArticle> filteredArticles = new ArrayList<>();
            for (User.HelpArticle article : results) {
                boolean groupMatch = currentGroup.equals("all") || article.getGroups().contains(currentGroup);
                boolean levelMatch = currentLevel.equals("All") || article.getLevel().equalsIgnoreCase(currentLevel);
                if (groupMatch && levelMatch) {
                    filteredArticles.add(article);
                }
            }

            // Count articles by level
            Map<String, Integer> levelCounts = new HashMap<>();
            for (String level : Arrays.asList("Beginner", "Intermediate", "Advanced", "Expert")) {
                levelCounts.put(level, 0);
            }
            for (User.HelpArticle article : filteredArticles) {
                String level = article.getLevel();
                levelCounts.put(level, levelCounts.getOrDefault(level, 0) + 1);
            }

            // Update article count label
            StringBuilder countText = new StringBuilder("Articles Matching Levels:\n");
            for (String level : Arrays.asList("Beginner", "Intermediate", "Advanced", "Expert")) {
                countText.append(level).append(": ").append(levelCounts.get(level)).append("\n");
            }
            articleCountLabel.setText(countText.toString());

            // Display articles in short form
            articlesListView.getItems().clear();
            sequenceToArticleMap.clear();
            int sequenceNumber = 1;
            for (User.HelpArticle article : filteredArticles) {
                String itemText = sequenceNumber + ". Title: " + article.getTitle() + ", Author: " + article.getAuthor() + ", Abstract: " + article.getDescription();
                articlesListView.getItems().add(itemText);
                sequenceToArticleMap.put(sequenceNumber, article);
                sequenceNumber++;
            }
        });

        // Event handler for selecting an article to view details using sequence number
        articlesListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Double-click
                String selectedItem = articlesListView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    // Extract sequence number from the selected item
                    int dotIndex = selectedItem.indexOf(".");
                    if (dotIndex != -1) {
                        String sequenceStr = selectedItem.substring(0, dotIndex);
                        try {
                            int sequenceNumber = Integer.parseInt(sequenceStr.trim());
                            User.HelpArticle article = sequenceToArticleMap.get(sequenceNumber);
                            if (article != null) {
                                showArticleDetail(article);
                            }
                        } catch (NumberFormatException ex) {
                            System.out.println("Invalid sequence number.");
                        }
                    }
                }
            }
        });

        // Input fields for article details (for adding/editing articles)
        TextField titleField = new TextField();
        titleField.setPromptText("Title");

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");

        TextField keywordsField = new TextField();
        keywordsField.setPromptText("Keywords (comma-separated)");

        TextArea bodyArea = new TextArea();
        bodyArea.setPromptText("Body of the article");

        TextField groupsField = new TextField();
        groupsField.setPromptText("Groups (comma-separated)");

        TextField levelField = new TextField();
        levelField.setPromptText("Level (Beginner, Intermediate, Advanced, Expert)");

        // Buttons for article management
        Button addArticleButton = new Button("Add Article");
        Button editArticleButton = new Button("Edit Selected Article");
        Button deleteArticleButton = new Button("Delete Selected Article");

        // Event handler for adding a new article
        addArticleButton.setOnAction(e -> {
            addArticle(
                    titleField.getText(), descriptionField.getText(),
                    Arrays.asList(keywordsField.getText().split(",")),
                    bodyArea.getText(), new ArrayList<>(),
                    Arrays.asList(groupsField.getText().split(",")), levelField.getText());
            clearArticleInputFields(titleField, descriptionField, keywordsField, bodyArea, groupsField, levelField);
        });

        // Event handler for editing an article
        editArticleButton.setOnAction(e -> {
            String selectedItem = articlesListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                int dotIndex = selectedItem.indexOf(".");
                if (dotIndex != -1) {
                    String sequenceStr = selectedItem.substring(0, dotIndex);
                    try {
                        int sequenceNumber = Integer.parseInt(sequenceStr.trim());
                        User.HelpArticle article = sequenceToArticleMap.get(sequenceNumber);
                        if (article != null) {
                            // Update the article with new details
                            currentUser.updateHelpArticle(
                                    article.getId(),
                                    titleField.getText(),
                                    descriptionField.getText(),
                                    Arrays.asList(keywordsField.getText().split(",")),
                                    bodyArea.getText(),
                                    new ArrayList<>(),
                                    Arrays.asList(groupsField.getText().split(",")),
                                    levelField.getText());
                            System.out.println("Article updated.");
                            clearArticleInputFields(titleField, descriptionField, keywordsField, bodyArea, groupsField, levelField);
                        }
                    } catch (NumberFormatException ex) {
                        System.out.println("Invalid sequence number.");
                    }
                }
            } else {
                System.out.println("No article selected for editing.");
            }
        });

        // Event handler for deleting an article
        deleteArticleButton.setOnAction(e -> {
            String selectedItem = articlesListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                int dotIndex = selectedItem.indexOf(".");
                if (dotIndex != -1) {
                    String sequenceStr = selectedItem.substring(0, dotIndex);
                    try {
                        int sequenceNumber = Integer.parseInt(sequenceStr.trim());
                        User.HelpArticle article = sequenceToArticleMap.get(sequenceNumber);
                        if (article != null) {
                            currentUser.removeHelpArticle(article.getId());
                            System.out.println("Article deleted.");
                            articlesListView.getItems().remove(selectedItem);
                        }
                    } catch (NumberFormatException ex) {
                        System.out.println("Invalid sequence number.");
                    }
                }
            } else {
                System.out.println("No article selected for deletion.");
            }
        });

        // Group management components
        TextField groupNameField = new TextField();
        groupNameField.setPromptText("Enter Group Name");

        CheckBox specialAccessCheckBox = new CheckBox("Special Access Group");

        Button createGroupButton = new Button("Create Group");
        Button addUserToGroupButton = new Button("Add User to Group");
        Button removeUserFromGroupButton = new Button("Remove User from Group");
        Button listGroupsButton = new Button("List Groups");

        // Event handler for creating a group
        createGroupButton.setOnAction(e -> {
            String groupName = groupNameField.getText();
            boolean isSpecialAccess = specialAccessCheckBox.isSelected();
            if (!groupName.isEmpty()) {
                Group group = loginInstance.createGroup(groupName, isSpecialAccess);
                if (group != null) {
                    System.out.println("Group created successfully.");
                    groupComboBox.getItems().add(groupName); // Update group selection ComboBox
                } else {
                    System.out.println("Failed to create group.");
                }
            } else {
                System.out.println("Please enter a group name.");
            }
        });

        // Event handler for adding a user to a group
        addUserToGroupButton.setOnAction(e -> {
            String groupName = groupNameField.getText();
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add User to Group");
            dialog.setHeaderText("Enter the username to add to the group:");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(usernameToAdd -> {
                User userToAdd = loginInstance.findUser(usernameToAdd);
                if (userToAdd != null) {
                    boolean added = loginInstance.addUserToGroup(groupName, userToAdd);
                    if (added) {
                        System.out.println("User added to group successfully.");
                    } else {
                        System.out.println("Failed to add user to group.");
                    }
                } else {
                    System.out.println("User not found.");
                }
            });
        });

        // Event handler for removing a user from a group
        removeUserFromGroupButton.setOnAction(e -> {
            String groupName = groupNameField.getText();
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Remove User from Group");
            dialog.setHeaderText("Enter the username to remove from the group:");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(usernameToRemove -> {
                User userToRemove = loginInstance.findUser(usernameToRemove);
                if (userToRemove != null) {
                    boolean removed = loginInstance.removeUserFromGroup(groupName, userToRemove);
                    if (removed) {
                        System.out.println("User removed from group successfully.");
                    } else {
                        System.out.println("Failed to remove user from group.");
                    }
                } else {
                    System.out.println("User not found.");
                }
            });
        });

        // Event handler for listing all groups
        listGroupsButton.setOnAction(e -> {
            System.out.println("Listing all groups:");
            for (Group g : loginInstance.listGroups()) {
                System.out.println("Group: " + g.getGroupName() + ", Special Access: " + g.isSpecialAccess());
            }
        });

        // Backup and Restore buttons
        Button backupButton = new Button("Backup Articles and Groups");
        Button restoreButton = new Button("Restore Articles and Groups");

        backupButton.setOnAction(e -> backupArticlesAndGroups());
        restoreButton.setOnAction(e -> restoreArticlesAndGroups());

        // Event handler for the logout button
        logoutButton.setOnAction(e -> {
            System.out.println("Logging out.");
            showLoginScreen(); // Return to login screen
        });

        // Layouts for organizing components
        HBox searchOptions = new HBox(10);
        searchOptions.getChildren().addAll(new Label("Content Level:"), levelComboBox, new Label("Group:"), groupComboBox);

        HBox articleButtons = new HBox(10);
        articleButtons.getChildren().addAll(addArticleButton, editArticleButton, deleteArticleButton);

        HBox groupButtons = new HBox(10);
        groupButtons.getChildren().addAll(createGroupButton, addUserToGroupButton, removeUserFromGroupButton, listGroupsButton);

        // Add components to the layout
        vbox.getChildren().addAll(
                new Label("Instructor Dashboard"),
                new Separator(),
                searchField,
                searchOptions,
                searchButton,
                activeGroupLabel,
                articleCountLabel,
                new Label("Search Results:"),
                articlesListView,
                new Separator(),
                new Label("Article Management:"),
                titleField, descriptionField, keywordsField, bodyArea, groupsField, levelField,
                articleButtons,
                new Separator(),
                new Label("Group Management:"),
                groupNameField, specialAccessCheckBox,
                groupButtons,
                new Separator(),
                backupButton, restoreButton,
                new Separator(),
                logoutButton);

        // Set the scene and display the instructor dashboard
        Scene instructorScene = new Scene(vbox, 800, 1000);
        window.setScene(instructorScene);
        window.show();
    }

    // Method to clear article input fields
    private void clearArticleInputFields(TextField titleField, TextField descriptionField, TextField keywordsField,
                                         TextArea bodyArea, TextField groupsField, TextField levelField) {
        titleField.clear();
        descriptionField.clear();
        keywordsField.clear();
        bodyArea.clear();
        groupsField.clear();
        levelField.clear();
    }

    // Method to show article details
    private void showArticleDetail(User.HelpArticle article) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Article Detail");
        alert.setHeaderText(article.getTitle());
        alert.setContentText("Author: " + article.getAuthor() + "\n\n" + article.getBody(currentUser));
        alert.showAndWait();
    }

    // Method to add a new article to the current user's list
    private void addArticle(String title, String description, List<String> keywords, String body, List<String> links, List<String> groups, String level) {
        if (currentUser != null) {
            User.HelpArticle newArticle = new User.HelpArticle(
                    System.currentTimeMillis(), // Unique ID
                    title, description, keywords, body, links, groups, level, currentUser.getUsername()
            );
            currentUser.addHelpArticle(newArticle); // Add the new article
            System.out.println("Article Added: " + title);
        } else {
            System.out.println("No user is currently logged in.");
        }
    }

    // Method to backup articles and groups to a file
    private void backupArticlesAndGroups() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Backup");
        File file = fileChooser.showSaveDialog(window); // Show save dialog
        if (file != null) {
            loginInstance.backupHelpArticles(file.getAbsolutePath(), currentUser); // Backup articles
            // Implement backup of groups if needed
            System.out.println("Backup completed.");
        }
    }

    // Method to restore articles and groups from a file
    private void restoreArticlesAndGroups() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Backup File");
        File file = fileChooser.showOpenDialog(window); // Show open dialog
        if (file != null) {
            // Confirmation dialog for merge option
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to merge with existing articles?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(response -> {
                boolean merge = response == ButtonType.YES;
                loginInstance.restoreHelpArticles(file.getAbsolutePath(), merge, currentUser); // Restore articles
                // Implement restore of groups if needed
                System.out.println("Restore completed.");
            });
        }
    }


    // Method to display the admin dashboard
    private void showAdminDashboard(User user) {
        currentUser = user; // Set the current user
        VBox vbox = new VBox(10);

        // Input fields for user management
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter Username");

        TextField newUserPasswordField = new TextField();
        newUserPasswordField.setPromptText("Enter Password");

        // ComboBox to select user role
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Student", "Instructor");
        roleComboBox.setValue("Student"); // Default role

        // Group management fields
        TextField groupNameField = new TextField();
        groupNameField.setPromptText("Enter Group Name");

        CheckBox specialAccessCheckBox = new CheckBox("Special Access Group");

        // Buttons for user management
        Button addUserButton = new Button("Add User");
        Button deleteUserButton = new Button("Delete User");
        Button resetPasswordButton = new Button("Reset Password");
        Button listUsersButton = new Button("List Users");
        Button addArticleButton = new Button("Manage Articles");
        Button createGroupButton = new Button("Create Group");
        Button addUserToGroupButton = new Button("Add User to Group");
        Button removeUserFromGroupButton = new Button("Remove User from Group");
        Button listGroupsButton = new Button("List Groups");
        Button logoutButton = new Button("Logout");

        // Event handler for adding a user
        addUserButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = newUserPasswordField.getText();
            String role = roleComboBox.getValue();
            boolean isOneTimePassword = false;

            // Register the new user
            if (!username.isEmpty() && !password.isEmpty()) {
                User newUser = Login.getInstance().registerUser(username, password, role, isOneTimePassword, null);
                if (newUser != null) {
                    System.out.println("User added successfully.");
                } else {
                    System.out.println("Failed to add user.");
                }
            } else {
                System.out.println("Please enter a username and password.");
            }
        });

        // Event handler for deleting a user
        deleteUserButton.setOnAction(e -> {
            String usernameToDelete = usernameField.getText();
            if (!usernameToDelete.isEmpty()) {
                boolean isDeleted = Login.getInstance().deleteUser(usernameToDelete);
                if (isDeleted) {
                    System.out.println("User deleted successfully.");
                } else {
                    System.out.println("Failed to delete user.");
                }
            } else {
                System.out.println("Please enter a username.");
            }
        });

        // Event handler for resetting a user's password
        resetPasswordButton.setOnAction(e -> {
            String usernameToReset = usernameField.getText();
            if (!usernameToReset.isEmpty()) {
                String newPassword = newUserPasswordField.getText();
                if (!newPassword.isEmpty()) {
                    boolean isReset = Login.getInstance().resetPassword(usernameToReset, newPassword);
                    if (isReset) {
                        System.out.println("Password reset successfully.");
                    } else {
                        System.out.println("Failed to reset password. User may not exist.");
                    }
                } else {
                    System.out.println("Please enter a new password.");
                }
            } else {
                System.out.println("Please enter a username.");
            }
        });

        // Event handler for creating a group
        createGroupButton.setOnAction(e -> {
            String groupName = groupNameField.getText();
            boolean isSpecialAccess = specialAccessCheckBox.isSelected();
            if (!groupName.isEmpty()) {
                Group group = loginInstance.createGroup(groupName, isSpecialAccess);
                if (group != null) {
                    System.out.println("Group created successfully.");
                } else {
                    System.out.println("Failed to create group.");
                }
            } else {
                System.out.println("Please enter a group name.");
            }
        });

        // Event handler for adding a user to a group
        addUserToGroupButton.setOnAction(e -> {
            String usernameToAdd = usernameField.getText();
            String groupName = groupNameField.getText();
            if (!usernameToAdd.isEmpty() && !groupName.isEmpty()) {
                User userToAdd = loginInstance.findUser(usernameToAdd);
                if (userToAdd != null) {
                    boolean added = loginInstance.addUserToGroup(groupName, userToAdd);
                    if (added) {
                        System.out.println("User added to group successfully.");
                    } else {
                        System.out.println("Failed to add user to group.");
                    }
                } else {
                    System.out.println("User not found.");
                }
            } else {
                System.out.println("Please enter a username and group name.");
            }
        });

        // Event handler for removing a user from a group
        removeUserFromGroupButton.setOnAction(e -> {
            String usernameToRemove = usernameField.getText();
            String groupName = groupNameField.getText();
            if (!usernameToRemove.isEmpty() && !groupName.isEmpty()) {
                User userToRemove = loginInstance.findUser(usernameToRemove);
                if (userToRemove != null) {
                    boolean removed = loginInstance.removeUserFromGroup(groupName, userToRemove);
                    if (removed) {
                        System.out.println("User removed from group successfully.");
                    } else {
                        System.out.println("Failed to remove user from group.");
                    }
                } else {
                    System.out.println("User not found.");
                }
            } else {
                System.out.println("Please enter a username and group name.");
            }
        });

        // Event handler for listing all users
        listUsersButton.setOnAction(e -> {
            System.out.println("Listing all users:");
            for (User u : loginInstance.listUsers()) {
                System.out.println("Username: " + u.getUsername() + ", Role: " + u.getRole());
            }
        });

        // Event handler for listing all groups
        listGroupsButton.setOnAction(e -> {
            System.out.println("Listing all groups:");
            for (Group g : loginInstance.listGroups()) {
                System.out.println("Group: " + g.getGroupName() + ", Special Access: " + g.isSpecialAccess());
            }
        });

        // Event handler to manage articles
        addArticleButton.setOnAction(e -> {
            showArticleDashboard(user); // Show article management dashboard
        });

        // Components to view messages
        Button viewMessagesButton = new Button("View Messages");
        ListView<String> messagesListView = new ListView<>();
        viewMessagesButton.setOnAction(e -> {
            List<Login.Message> messages = loginInstance.getMessages();
            messagesListView.getItems().clear();
            for (Login.Message message : messages) {
                messagesListView.getItems().add("From: " + message.getUsername() + " | " + message.getTimestamp() + "\n" + message.getContent());
            }
        });

        // Components to view search history
        Button viewSearchHistoryButton = new Button("View Search History");
        ListView<String> searchHistoryListView = new ListView<>();
        viewSearchHistoryButton.setOnAction(e -> {
            List<Login.SearchQuery> searchQueries = loginInstance.getSearchQueries();
            searchHistoryListView.getItems().clear();
            for (Login.SearchQuery query : searchQueries) {
                searchHistoryListView.getItems().add("User: " + query.getUsername() + " | " + query.getTimestamp() + "\nQuery: " + query.getQuery());
            }
        });

        // Event handler for the logout button
        logoutButton.setOnAction(e -> {
            System.out.println("Logging out.");
            showLoginScreen(); // Return to login screen
        });

        // Add components to the layout
        vbox.getChildren().addAll(
                new Label("Admin Dashboard"),
                usernameField,
                newUserPasswordField,
                roleComboBox,
                addUserButton,
                deleteUserButton,
                resetPasswordButton,
                listUsersButton,
                new Separator(),
                groupNameField,
                specialAccessCheckBox,
                createGroupButton,
                addUserToGroupButton,
                removeUserFromGroupButton,
                listGroupsButton,
                new Separator(),
                addArticleButton,
                new Separator(),
                viewMessagesButton, messagesListView,
                new Separator(),
                viewSearchHistoryButton, searchHistoryListView,
                new Separator(),
                logoutButton
        );

        // Set the scene and display the admin dashboard
        Scene adminScene = new Scene(vbox, 600, 800);
        window.setScene(adminScene);
        window.show();
    }
    
    // Method to display the admin's article management dashboard
    private void showArticleDashboard(User user) {
        VBox vbox = new VBox(10);

        // Input fields for article details
        TextField titleField = new TextField();
        titleField.setPromptText("Title");

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");

        TextField keywordsField = new TextField();
        keywordsField.setPromptText("Keywords (comma-separated)");

        TextArea bodyArea = new TextArea();
        bodyArea.setPromptText("Body of the article");

        TextField groupsField = new TextField();
        groupsField.setPromptText("Groups (comma-separated)");

        TextField levelField = new TextField();
        levelField.setPromptText("Level (Beginner, Intermediate, Advanced, Expert)");

        // Button to add a new article
        Button addArticleButton = new Button("Add Article");
        addArticleButton.setOnAction(e -> addArticle(
                titleField.getText(), descriptionField.getText(),
                Arrays.asList(keywordsField.getText().split(",")),
                bodyArea.getText(), new ArrayList<>(),
                Arrays.asList(groupsField.getText().split(",")), levelField.getText()));

        // Search functionality components
        TextField searchField = new TextField();
        searchField.setPromptText("Enter keyword to search");

        Button searchButton = new Button("Search Articles");
        Button listArticlesButton = new Button("List All Articles");

        ListView<String> articlesListView = new ListView<>(); // ListView to display article titles

        // Map to associate article titles with their IDs for deletion
        Map<String, Long> articleTitleToIdMap = new HashMap<>();

        // Event handler for the search button
        searchButton.setOnAction(e -> {
            String keyword = searchField.getText();
            if (keyword != null && !keyword.isEmpty()) {
                List<User.HelpArticle> results = currentUser.searchHelpArticles(keyword.trim());
                articlesListView.getItems().clear();
                articleTitleToIdMap.clear();
                for (User.HelpArticle article : results) {
                    articlesListView.getItems().add(article.getTitle());
                    articleTitleToIdMap.put(article.getTitle(), article.getId());
                }
            }
        });

        // Event handler for listing all articles
        listArticlesButton.setOnAction(e -> {
            List<User.HelpArticle> articles = currentUser.getAllHelpArticles();
            articlesListView.getItems().clear();
            articleTitleToIdMap.clear();
            for (User.HelpArticle article : articles) {
                articlesListView.getItems().add(article.getTitle());
                articleTitleToIdMap.put(article.getTitle(), article.getId());
            }
        });

        // Button to delete the selected article
        Button deleteArticleButton = new Button("Delete Selected Article");
        deleteArticleButton.setOnAction(e -> {
            String selectedTitle = articlesListView.getSelectionModel().getSelectedItem();
            if (selectedTitle != null) {
                Long articleId = articleTitleToIdMap.get(selectedTitle);
                if (articleId != null) {
                    currentUser.removeHelpArticle(articleId); // Remove article from user's list
                    System.out.println("Article Deleted: " + selectedTitle);
                    articlesListView.getItems().remove(selectedTitle); // Remove from ListView
                }
            } else {
                System.out.println("No article selected for deletion.");
            }
        });

        // Buttons for backup and restore functionality
        Button backupButton = new Button("Backup Articles");
        backupButton.setOnAction(e -> backupArticlesAndGroups());

        Button restoreButton = new Button("Restore Articles");
        restoreButton.setOnAction(e -> restoreArticlesAndGroups());

        // Logout button
        Button logoutButton = new Button("Back");
        logoutButton.setOnAction(e -> {
            System.out.println("Going Back.");
            showAdminDashboard(user); // Return to admin screen
        });

        // Add components to the layout
        vbox.getChildren().addAll(
                new Label("Article Dashboard"),
                titleField, descriptionField, keywordsField, bodyArea, groupsField, levelField,
                addArticleButton,
                new Separator(),
                searchField, searchButton, listArticlesButton, articlesListView, deleteArticleButton,
                new Separator(),
                backupButton, restoreButton, logoutButton
        );

        // Set the scene and display the article dashboard
        Scene articleScene = new Scene(vbox, 600, 800);
        window.setScene(articleScene);
        window.show();
    }

    public static void main(String[] args) {
        launch(args); // Launch the JavaFX application
    }
}
