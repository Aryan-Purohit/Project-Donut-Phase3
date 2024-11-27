package application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

public class TestUser {

	private User user;

    @BeforeEach
    void setUp() {
        user = new User("testUser", new byte[]{}, "Student");
    }

    @Test
    void testAddAndRemoveHelpArticle() {
        // Create a help article
        User.HelpArticle article = new User.HelpArticle(
                1L,
                "Sample Article",
                "Description",
                List.of("keyword"),
                "Body",
                List.of(),
                List.of("group1"),
                "Beginner",
                user.getUsername()
        );

        // Add article
        user.addHelpArticle(article);
        assertEquals(1, user.getAllHelpArticles().size(), "Article should be added.");

        // Remove article
        user.removeHelpArticle(1L);
        assertEquals(0, user.getAllHelpArticles().size(), "Article should be removed.");
    }

    @Test
    void testSetAndGetTopics() {
        user.setTopicProficiency("Java", "Advanced");
        assertEquals("Advanced", user.getTopicProficiency("Java"), "Proficiency should be set to Advanced.");
    }
}
