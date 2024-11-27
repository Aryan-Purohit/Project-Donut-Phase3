package application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

public class TestLogin {

	private Login loginInstance;

    @BeforeEach
    void setUp() {
        loginInstance = Login.getInstance();

   
    }

    @Test
    void testRegisterUser() {
        // Test successful registration
        User user = loginInstance.registerUser("testUser", "password123", "Student", false, null);
        assertNotNull(user);
        assertEquals("testUser", user.getUsername());
        assertEquals("Student", user.getRole());

        // Test registration with existing username
        User duplicateUser = loginInstance.registerUser("testUser", "newPassword", "Student", false, null);
        assertNull(duplicateUser, "Duplicate username should not be allowed.");
    }

    @Test
    void testAuthenticate() {
        // Register a user
        loginInstance.registerUser("authUser", "securePass", "Instructor", false, null);

        // Test successful authentication
        boolean isAuthenticated = loginInstance.authenticate("authUser", "securePass");
        assertTrue(isAuthenticated, "User should be authenticated with correct credentials.");

        // Test authentication with wrong password
        boolean isNotAuthenticated = loginInstance.authenticate("authUser", "wrongPass");
        assertFalse(isNotAuthenticated, "User should not be authenticated with incorrect password.");

        // Test authentication with non-existent user
        boolean nonExistentUser = loginInstance.authenticate("nonUser", "password");
        assertFalse(nonExistentUser, "Non-existent user should not be authenticated.");
    }

    @Test
    void testResetPassword() {
        // Register a user
        loginInstance.registerUser("resetUser", "oldPass", "Student", false, null);

        // Reset password
        boolean isReset = loginInstance.resetPassword("resetUser", "newPass");
        assertTrue(isReset, "Password should be reset successfully.");

        // Authenticate with new password
        boolean isAuthenticated = loginInstance.authenticate("resetUser", "newPass");
        assertTrue(isAuthenticated, "User should be authenticated with new password.");

        // Authenticate with old password
        boolean isOldPasswordValid = loginInstance.authenticate("resetUser", "oldPass");
        assertFalse(isOldPasswordValid, "Old password should no longer be valid.");
    }

    

    @Test
    void testCreateAndDeleteGroup() {
        // Create a group
        Group group = loginInstance.createGroup("TestGroup", false);
        assertNotNull(group, "Group should be created.");
        assertEquals("TestGroup", group.getGroupName());

        // Verify group exists
        Group fetchedGroup = loginInstance.getGroup("TestGroup");
        assertNotNull(fetchedGroup, "Group should be retrievable.");

        // Delete the group (assuming deleteGroup method exists)
        boolean isDeleted = loginInstance.deleteGroup("TestGroup");
        assertTrue(isDeleted, "Group should be deleted.");

        // Verify group no longer exists
        Group deletedGroup = loginInstance.getGroup("TestGroup");
        assertNull(deletedGroup, "Group should no longer exist.");
    }

    @Test
    void testAddAndRemoveUserFromGroup() {
        // Create a group and a user
        Group group = loginInstance.createGroup("MembersGroup", false);
        User user = loginInstance.registerUser("groupUser", "password", "Student", false, null);

        // Add user to group
        boolean isAdded = loginInstance.addUserToGroup("MembersGroup", user);
        assertTrue(isAdded, "User should be added to the group.");

        // Verify user is in group
        assertTrue(group.getStudents().contains(user), "User should be in the group's student list.");

        // Remove user from group
        boolean isRemoved = loginInstance.removeUserFromGroup("MembersGroup", user);
        assertTrue(isRemoved, "User should be removed from the group.");

        // Verify user is no longer in group
        assertFalse(group.getStudents().contains(user), "User should not be in the group's student list.");
    }

    @Test
    void testMessages() {
        // Add messages
        loginInstance.addMessage("user1", "This is a test message.");
        loginInstance.addMessage("user2", "Another test message.");

        // Retrieve messages
        List<Login.Message> messages = loginInstance.getMessages();
        assertEquals(2, messages.size(), "There should be two messages.");

        // Verify message content
        Login.Message message1 = messages.get(0);
        assertEquals("user1", message1.getUsername());
        assertEquals("This is a test message.", message1.getContent());
    }

    @Test
    void testSearchQueries() {
        // Add search queries
        loginInstance.addSearchQuery("user1", "search term 1");
        loginInstance.addSearchQuery("user2", "search term 2");

        // Retrieve search queries
        List<Login.SearchQuery> queries = loginInstance.getSearchQueries();
        assertEquals(2, queries.size(), "There should be two search queries.");

        // Verify query content
        Login.SearchQuery query1 = queries.get(0);
        assertEquals("user1", query1.getUsername());
        assertEquals("search term 1", query1.getQuery());
    }
}
