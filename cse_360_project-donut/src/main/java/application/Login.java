package application;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The Login class manages user authentication, registration, deletion,
 * password reset, and backup/restore of help articles. It follows the Singleton
 * design pattern to ensure only one instance exists throughout the application.
 */
public class Login {

    private List<User> users = new ArrayList<>(); // List to store users
    private List<Group> groups = new ArrayList<>(); // List to store groups
    private static Login instance = null; // Singleton instance

    // Encryption key for user passwords
    private byte[] passwordEncryptionKey;

    // List to store messages
    private List<Message> messages = new ArrayList<>();

    // List to store search queries
    private List<SearchQuery> searchQueries = new ArrayList<>();

    // Method to get the singleton instance
    public static Login getInstance() {
        if (instance == null) {
            instance = new Login();
        }
        return instance;
    }

    private Login() {
        // Load or generate the encryption key
        try {
            passwordEncryptionKey = EncryptionUtil.getPasswordKey();
        } catch (IOException e) {
            System.out.println("Error loading encryption key: " + e.getMessage());
            // Handle error appropriately
        }
    }

    /**
     * Authenticates a user based on username and password.
     *
     * @param username The username input.
     * @param password The password input.
     * @return True if authentication is successful, else false.
     */
    public boolean authenticate(String username, String password) {
        if (passwordEncryptionKey == null) {
            System.out.println("Encryption key not available. Cannot authenticate users.");
            return false;
        }

        for (User user : users) {
            if (user.getUsername().equals(username)) {
                // Check for one-time password expiry
                if (user.isOneTimePassword()) {
                    if (LocalDateTime.now().isAfter(user.getOtpExpiry())) {
                        System.out.println("One-time password has expired. Please reset your password.");
                        return false;
                    }
                }
                // Decrypt the stored password
                String decryptedPassword;
                try {
                    decryptedPassword = EncryptionUtil.decrypt(user.getPassword(), passwordEncryptionKey);
                } catch (Exception e) {
                    System.out.println("Error decrypting password: " + e.getMessage());
                    return false;
                }
                // Check if the password matches
                if (decryptedPassword.equals(password)) {
                    return true; // Authentication successful
                }
            }
        }
        return false; // Authentication failed
    }

    /**
     * Registers a new user with the provided details.
     *
     * @param username          The desired username.
     * @param password          The desired password.
     * @param role              The role of the user (Admin, Instructor, Student).
     * @param isOneTimePassword Flag indicating if the password is one-time.
     * @param otpExpiry         Expiry time for one-time password.
     * @return The newly registered User object.
     */
    public User registerUser(String username, String password, String role, boolean isOneTimePassword, LocalDateTime otpExpiry) {
        if (passwordEncryptionKey == null) {
            System.out.println("Encryption key not available. Cannot register users.");
            return null;
        }

        // Check if username already exists
        if (findUser(username) != null) {
            System.out.println("Username already exists. Please choose a different username.");
            return null;
        }

        User newUser;
        try {
            // Encrypt the password before storing
            byte[] encryptedPassword = EncryptionUtil.encrypt(password, passwordEncryptionKey);
            newUser = new User(username, encryptedPassword, role);
            newUser.setOneTimePassword(isOneTimePassword);
            newUser.setOtpExpiry(otpExpiry);
            users.add(newUser); // Add the new user to the list
            return newUser;
        } catch (Exception e) {
            System.out.println("Error encrypting password: " + e.getMessage());
            return null;
        }
    }

    /**
     * Deletes a user based on the provided username.
     *
     * @param usernameToDelete The username of the user to delete.
     * @return True if deletion is successful, else false.
     */
    public boolean deleteUser(String usernameToDelete) {
        return users.removeIf(user -> user.getUsername().equals(usernameToDelete));
    }

    /**
     * Resets a user's password.
     *
     * @param usernameToReset The username of the user whose password is to be reset.
     * @param newPassword     The new password.
     * @return True if password reset is successful, else false.
     */
    public boolean resetPassword(String usernameToReset, String newPassword) {
        if (passwordEncryptionKey == null) {
            System.out.println("Encryption key not available. Cannot reset passwords.");
            return false;
        }

        for (User user : users) {
            if (user.getUsername().equals(usernameToReset)) {
                try {
                    // Encrypt the new password before storing
                    byte[] encryptedPassword = EncryptionUtil.encrypt(newPassword, passwordEncryptionKey);
                    user.setPassword(encryptedPassword);
                    user.setOneTimePassword(false); // Reset OTP flag
                    user.setOtpExpiry(null); // Clear OTP expiry
                    return true;
                } catch (Exception e) {
                    System.out.println("Error encrypting new password: " + e.getMessage());
                    return false;
                }
            }
        }
        return false;
    }
    
    

    /**
     * Lists all registered users.
     *
     * @return A copy of the list of users.
     */
    public List<User> listUsers() {
        return new ArrayList<>(users); // Return a copy of the users list
    }

    /**
     * Finds a user by their username.
     *
     * @param username The username to search for.
     * @return The User object if found, else null.
     */
    public User findUser(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user; // User found
            }
        }
        return null; // User not found
    }
    
    /**
     * Adds a message to the system.
     *
     * @param username      The username of the sender.
     * @param messageContent The content of the message.
     */
    public void addMessage(String username, String messageContent) {
        Message message = new Message(username, messageContent, LocalDateTime.now());
        messages.add(message);
    }

    /**
     * Retrieves all messages.
     *
     * @return A list of all messages.
     */
    public List<Message> getMessages() {
        return new ArrayList<>(messages);
    }

    // Search Query Handling Methods

    /**
     * Adds a search query to the system.
     *
     * @param username The username of the user who made the search.
     * @param query    The search query.
     */
    public void addSearchQuery(String username, String query) {
        SearchQuery searchQuery = new SearchQuery(username, query, LocalDateTime.now());
        searchQueries.add(searchQuery);
    }

    /**
     * Retrieves all search queries.
     *
     * @return A list of all search queries.
     */
    public List<SearchQuery> getSearchQueries() {
        return new ArrayList<>(searchQueries);
    }

    // Inner class for Message
    public static class Message {
        private String username;
        private String content;
        private LocalDateTime timestamp;

        public Message(String username, String content, LocalDateTime timestamp) {
            this.username = username;
            this.content = content;
            this.timestamp = timestamp;
        }

        public String getUsername() { return username; }
        public String getContent() { return content; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    // Inner class for SearchQuery
    public static class SearchQuery {
        private String username;
        private String query;
        private LocalDateTime timestamp;

        public SearchQuery(String username, String query, LocalDateTime timestamp) {
            this.username = username;
            this.query = query;
            this.timestamp = timestamp;
        }

        public String getUsername() { return username; }
        public String getQuery() { return query; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
    
    
    // Group Management Methods

    /**
     * Creates a new group.
     *
     * @param groupName       The name of the group.
     * @param isSpecialAccess True if it's a special access group.
     * @return The newly created Group object.
     */
    public Group createGroup(String groupName, boolean isSpecialAccess) {
        // Check if group already exists
        for (Group group : groups) {
            if (group.getGroupName().equals(groupName)) {
                System.out.println("Group already exists.");
                return null;
            }
        }
        Group newGroup = new Group(groupName, isSpecialAccess);
        groups.add(newGroup);
        System.out.println("Group created: " + groupName);
        return newGroup;
    }
    
    /**
     * Adds a user to a group.
     *
     * @param groupName The name of the group.
     * @param user      The user to add.
     * @return True if the user was added successfully.
     */
    public boolean addUserToGroup(String groupName, User user) {
        Group group = getGroup(groupName);
        if (group != null) {
            return group.addUser(user);
        } else {
            System.out.println("Group not found.");
            return false;
        }
    }
    
    /**
     * Removes a user from a group.
     *
     * @param groupName The name of the group.
     * @param user      The user to remove.
     * @return True if the user was removed successfully.
     */
    public boolean removeUserFromGroup(String groupName, User user) {
        Group group = getGroup(groupName);
        if (group != null) {
            return group.removeUser(user);
        } else {
            System.out.println("Group not found.");
            return false;
        }
    }

    /**
     * Retrieves a group by its name.
     *
     * @param groupName The name of the group.
     * @return The Group object if found, else null.
     */
    public Group getGroup(String groupName) {
        for (Group group : groups) {
            if (group.getGroupName().equals(groupName)) {
                return group;
            }
        }
        return null;
    }

    /**
     * Lists all groups.
     *
     * @return A list of all groups.
     */
    public List<Group> listGroups() {
        return new ArrayList<>(groups);
    }
    

    /**
     * Backs up help articles that the current user has access to.
     *
     * @param filename    The name of the file to back up to.
     * @param currentUser The user performing the backup.
     */
    public void backupHelpArticles(String filename, User currentUser) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            List<User.HelpArticle> accessibleArticles = new ArrayList<>();
            // Collect help articles the user has access to
            for (User user : users) {
                for (User.HelpArticle article : user.getAllHelpArticles()) {
                    if (article.userHasAccess(currentUser)) {
                        accessibleArticles.add(article);
                    }
                }
            }
            oos.writeObject(accessibleArticles); // Serialize the articles list
            System.out.println("Backup completed successfully.");
        } catch (IOException e) {
            System.out.println("Error during backup: " + e.getMessage());
        }
    }

    /**
     * Restores help articles from a specified file, considering access rights.
     *
     * @param filename    The name of the file to restore from.
     * @param merge       If true, merge with existing articles; else, replace existing articles.
     * @param currentUser The user performing the restore.
     */
    public void restoreHelpArticles(String filename, boolean merge, User currentUser) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            List<User.HelpArticle> restoredArticles = (List<User.HelpArticle>) ois.readObject();
            if (currentUser == null) {
                System.out.println("No user is currently logged in.");
                return;
            }

            if (!currentUser.isAdmin() && !currentUser.isInstructor()) {
                System.out.println("You do not have permission to restore articles.");
                return;
            }

            if (merge) {
                // Merge articles without duplicates
                for (User.HelpArticle article : restoredArticles) {
                    boolean exists = currentUser.getAllHelpArticles().stream()
                            .anyMatch(existingArticle -> existingArticle.getId() == article.getId());
                    if (!exists && article.userHasAccess(currentUser)) {
                        currentUser.addHelpArticle(article);
                    }
                }
            } else {
                // Replace existing articles with restored ones
                currentUser.getAllHelpArticles().clear();
                for (User.HelpArticle article : restoredArticles) {
                    if (article.userHasAccess(currentUser)) {
                        currentUser.addHelpArticle(article);
                    }
                }
            }
            System.out.println("Restore completed successfully.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error during restore: " + e.getMessage());
        }
    }
    
 // In Login.java
    public boolean deleteGroup(String groupName) {
        Iterator<Group> iterator = groups.iterator();
        while (iterator.hasNext()) {
            Group group = iterator.next();
            if (group.getGroupName().equals(groupName)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

}
