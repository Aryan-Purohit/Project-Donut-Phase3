package application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

public class TestUser {

    private User user;

    @BeforeEach
    public void setup() {
        user = new User("testUser", "password123", "Student");
    }

    @Test
    public void testUsername() {
        assertEquals("testUser", user.getUsername());
    }

    @Test
    public void testPassword() {
        assertArrayEquals("password123".getBytes(), user.getPassword());
        user.setPassword("newPassword456".getBytes());
        assertArrayEquals("newPassword456".getBytes(), user.getPassword());
    }

    @Test
    public void testRole() {
        assertEquals("Student", user.getRole());
    }

    @Test
    public void testEmail() {
        user.setEmail("user@example.com");
        assertEquals("user@example.com", user.getEmail());
    }

    @Test
    public void testOneTimePassword() {
        user.setOneTimePassword(true);
        assertTrue(user.isOneTimePassword());
        user.setOneTimePassword(false);
        assertFalse(user.isOneTimePassword());
    }

    @Test
    public void testOtpExpiry() {
        LocalDateTime expiry = LocalDateTime.now().plusDays(1);
        user.setOtpExpiry(expiry);
        assertEquals(expiry, user.getOtpExpiry());
    }

    @Test
    public void testTopicProficiency() {
        assertEquals("Intermediate", user.getTopicProficiency("Topic 1"));
        user.setTopicProficiency("Topic 1", "Advanced");
        assertEquals("Advanced", user.getTopicProficiency("Topic 1"));
    }

    @Test
    public void testAccountSetupComplete() {
        assertFalse(user.isAccountSetupComplete());
        user.setAccountSetupComplete(true);
        assertTrue(user.isAccountSetupComplete());
    }
}
