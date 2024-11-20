package application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

public class TestLogin {

    private Login login;

    @BeforeEach
    public void setup() {
        login = Login.getInstance();
        login.listUsers().clear(); // Clear users before each test
    }

    //@Test
    //**public void testRegisterUser() {
    //    User user = login.registerUser("admin", "adminPass", false, null);
     //   assertNotNull(user);
     //   assertEquals("admin", user.getUsername());
      //  assertEquals("Admin", user.getRole());
    //}

    @Test
    public void testAuthenticateUser() {
        login.registerUser("admin", "adminPass", false, null);
        assertTrue(login.authenticate("admin", "adminPass"));
        assertFalse(login.authenticate("admin", "wrongPass"));
    }

    @Test
    public void testOneTimePasswordExpiry() {
        LocalDateTime expiryTime = LocalDateTime.now().minusDays(1); // Expired OTP
        User user = login.registerUser("user1", "otpPass", true, expiryTime);
        assertFalse(login.authenticate("user1", "otpPass")); // Should fail due to OTP expiry
    }

    @Test
    public void testPasswordReset() {
        login.registerUser("user1", "initialPass", false, null);
        assertTrue(login.authenticate("user1", "initialPass"));

        login.resetPassword("user1", "newPass");
        assertTrue(login.authenticate("user1", "newPass"));
        assertFalse(login.authenticate("user1", "initialPass"));
    }

    @Test
    public void testDeleteUser() {
        login.registerUser("user1", "password123", false, null);
        assertTrue(login.deleteUser("user1"));
        assertFalse(login.deleteUser("nonexistentUser"));
    }

    /**@Test
    public void testListUsers() {
        login.registerUser("user1", "password1", false, null);
        login.registerUser("user2", "password2", false, null);

        assertEquals(2, login.listUsers().size());
    }**/
}
