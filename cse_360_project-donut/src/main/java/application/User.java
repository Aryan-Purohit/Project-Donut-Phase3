package application;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

/**
 * The User class represents a user in the system and includes methods for managing
 * user details, help articles, messages, and access control.
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L; // Serialization ID

    private String username;
    private byte[] password; // Encrypted password stored as bytes
    private String role;
    private String email;
    private String firstName;
    private String middleName;
    private String lastName;
    private String preferredName;
    private boolean isOneTimePassword;
    private LocalDateTime otpExpiry;
    private boolean isAccountSetupComplete = false; // Flag for account setup completion

    private Map<String, String> topics = new HashMap<>(); // Map of topics and proficiency levels

    private List<HelpArticle> helpArticles = new ArrayList<>(); // List of help articles

    // Group memberships
    private Set<String> groupNames = new HashSet<>(); // Names of groups the user belongs to

    // Messages sent by the student
    private List<String> messages = new ArrayList<>();

    // Encryption key for help articles
    private static byte[] articleEncryptionKey;

    // Constructor to create a new user
    public User(String username, byte[] password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.isOneTimePassword = false; // Default value
        this.otpExpiry = null; // No expiry by default

        // Initialize default topics with "Intermediate" level
        topics.put("Topic 1", "Intermediate");
        topics.put("Topic 2", "Intermediate");
        topics.put("Topic 3", "Intermediate");

        // Load or generate the article encryption key
        if (articleEncryptionKey == null) {
            try {
                articleEncryptionKey = EncryptionUtil.getArticleKey();
            } catch (IOException e) {
                System.out.println("Error loading article encryption key: " + e.getMessage());
                // Handle error appropriately
            }
        }
    }

    // Getters and Setters for user attributes

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public byte[] getPassword() { return password; }

    public void setPassword(byte[] password) { this.password = password; }

    public String getRole() { return role; }

    public void setRole(String role) { this.role = role; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }

    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getMiddleName() { return middleName; }

    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getLastName() { return lastName; }

    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPreferredName() { return preferredName; }

    public void setPreferredName(String preferredName) { this.preferredName = preferredName; }

    // Methods for group memberships
    public Set<String> getGroupNames() {
        return groupNames;
    }

    public void addGroupName(String groupName) {
        groupNames.add(groupName);
    }

    public void removeGroupName(String groupName) {
        groupNames.remove(groupName);
    }

    // Methods related to one-time password

    public boolean isOneTimePassword() { return isOneTimePassword; }

    public void setOneTimePassword(boolean isOneTimePassword) { this.isOneTimePassword = isOneTimePassword; }

    public LocalDateTime getOtpExpiry() { return otpExpiry; }

    public void setOtpExpiry(LocalDateTime otpExpiry) { this.otpExpiry = otpExpiry; }

    // Methods for account setup completion

    public boolean isAccountSetupComplete() { return isAccountSetupComplete; }

    public void setAccountSetupComplete(boolean isAccountSetupComplete) { this.isAccountSetupComplete = isAccountSetupComplete; }

    // Methods for topic proficiency

    public Map<String, String> getTopics() { return topics; }

    public String getTopicProficiency(String topic) { return topics.getOrDefault(topic, "Intermediate"); }

    public void setTopicProficiency(String topic, String level) { topics.put(topic, level); }

    // Nested class representing a help article
    public static class HelpArticle implements Serializable {
        private static final long serialVersionUID = 1L;

        private long id; // Unique identifier
        private String title;
        private String description; // This serves as the abstract
        private List<String> keywords;
        private byte[] encryptedBody; // Encrypted body
        private List<String> links;
        private List<String> groups; // Groups that have access to this article
        private String level;
        private String author;
        private boolean isSpecialAccess;

        // Encryption key for articles
        private static byte[] articleEncryptionKey;

        // Constructor to create a new help article
        public HelpArticle(long id, String title, String description, List<String> keywords, String body,
                           List<String> links, List<String> groups, String level, String author) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.keywords = keywords;
            this.links = links;
            this.groups = groups;
            this.level = level;
            this.author = author;

            // Load or generate the article encryption key
            if (articleEncryptionKey == null) {
                try {
                    articleEncryptionKey = EncryptionUtil.getArticleKey();
                } catch (IOException e) {
                    System.out.println("Error loading article encryption key: " + e.getMessage());
                    // Handle error appropriately
                }
            }

            // Determine if the article is in a special access group
            this.isSpecialAccess = isInSpecialAccessGroup(groups);

            // Encrypt the body
            setBody(body); // Use the setBody method to handle encryption
        }

        // Getters and Setters for help article attributes

        public long getId() { return id; }

        public String getTitle() { return title; }

        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }

        public void setDescription(String description) { this.description = description; }

        public List<String> getKeywords() { return keywords; }

        public void setKeywords(List<String> keywords) { this.keywords = keywords; }

        public List<String> getLinks() { return links; }

        public void setLinks(List<String> links) { this.links = links; }

        public List<String> getGroups() { return groups; }

        public void setGroups(List<String> groups) {
            this.groups = groups;
            this.isSpecialAccess = isInSpecialAccessGroup(groups); // Update isSpecialAccess flag
        }

        public String getLevel() { return level; }

        public void setLevel(String level) { this.level = level; }

        public String getAuthor() { return author; }

        public void setAuthor(String author) { this.author = author; }

        // Method to determine if the article is in a special access group
        private boolean isInSpecialAccessGroup(List<String> groups) {
            for (String group : groups) {
                if (group.startsWith("special_")) {
                    return true;
                }
            }
            return false;
        }

        // Method to check if the article belongs to a special access group
        public boolean isSpecialAccessGroup() {
            return isSpecialAccess;
        }

     // In User.HelpArticle class
        public boolean userHasAccess(User user) {
            // Check if the user is an admin or instructor
            if (user.isAdmin() || user.isInstructor()) {
                return true;
            }
            // Check if the user is a member of any group associated with the article
            for (String groupName : this.getGroups()) {
                if (user.getGroupNames().contains(groupName)) {
                    return true;
                }
            }
            return false;
        }
        
        
        // Method to get the body of the article, decrypting if necessary
        public String getBody(User user) {
            if (!userHasAccess(user)) {
                return "You do not have access to view this article.";
            }
            if (isSpecialAccessGroup()) {
                try {
                    if (articleEncryptionKey == null) {
                        return "Encryption key not available. Cannot decrypt article body.";
                    }
                    return EncryptionUtil.decrypt(encryptedBody, articleEncryptionKey);
                } catch (Exception e) {
                    System.out.println("Error decrypting article body: " + e.getMessage());
                    return "Error decrypting article body.";
                }
            } else {
                try {
                    return new String(encryptedBody, "UTF-8");
                } catch (Exception e) {
                    System.out.println("Error reading article body: " + e.getMessage());
                    return "Error reading article body.";
                }
            }
        }

        // Method to set the body of the article, encrypting if necessary
        public void setBody(String body) {
            if (isSpecialAccessGroup()) {
                try {
                    if (articleEncryptionKey == null) {
                        System.out.println("Encryption key not available. Cannot encrypt article body.");
                        this.encryptedBody = null;
                    } else {
                        this.encryptedBody = EncryptionUtil.encrypt(body, articleEncryptionKey);
                    }
                } catch (Exception e) {
                    System.out.println("Error encrypting article body: " + e.getMessage());
                    this.encryptedBody = null;
                }
            } else {
                try {
                    this.encryptedBody = body.getBytes("UTF-8");
                } catch (Exception e) {
                    System.out.println("Error setting article body: " + e.getMessage());
                    this.encryptedBody = null;
                }
            }
        }
    }

    // Methods for managing help articles

    // Add a help article to the user's list
    public void addHelpArticle(HelpArticle article) {
        helpArticles.add(article);
    }

    // Remove a help article by its ID
    public void removeHelpArticle(long id) {
        helpArticles.removeIf(article -> article.getId() == id);
    }

    // Update an existing help article
    public void updateHelpArticle(long id, String title, String description, List<String> keywords, String body,
                                  List<String> links, List<String> groups, String level) {
        for (HelpArticle article : helpArticles) {
            if (article.getId() == id) {
                article.setTitle(title);
                article.setDescription(description);
                article.setKeywords(keywords);
                article.setBody(body);
                article.setLinks(links);
                article.setGroups(groups);
                article.setLevel(level);
            }
        }
    }

    // Get help articles by group
    public List<HelpArticle> getHelpArticlesByGroup(String group) {
        if ("all".equalsIgnoreCase(group)) {
            return new ArrayList<>(helpArticles);
        }
        List<HelpArticle> filteredArticles = new ArrayList<>();
        for (HelpArticle article : helpArticles) {
            if (article.getGroups().contains(group)) {
                filteredArticles.add(article);
            }
        }
        return filteredArticles;
    }

    // Get all help articles
    public List<HelpArticle> getAllHelpArticles() {
        return new ArrayList<>(helpArticles);
    }

    // Search help articles by keyword in title or keywords
    public List<HelpArticle> searchHelpArticles(String keyword) {
        // Record the search query
        Login.getInstance().addSearchQuery(this.username, keyword);

        List<HelpArticle> results = new ArrayList<>();
        for (HelpArticle article : helpArticles) {
            if (article.getKeywords().contains(keyword) || article.getTitle().contains(keyword)) {
                results.add(article);
            }
        }
        return results;
    }

    // Method to determine if the user is an admin
    public boolean isAdmin() {
        return "Admin".equalsIgnoreCase(this.role);
    }

    public boolean isInstructor() {
        return "Instructor".equalsIgnoreCase(this.role);
    }

    // Message Handling

    // Method for student to send a message
    public void sendMessage(String messageContent) {
        Login.getInstance().addMessage(this.username, messageContent);
        System.out.println("Message sent.");
    }
}
