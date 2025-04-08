package linksharing.service;

import linksharing.db.Link;
import linksharing.db.LinkRepository;
import linksharing.db.User;
import linksharing.db.UserRepository;
import linksharing.dto.LinkDto;
import linksharing.dto.UserDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class AppServiceTest {

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_FIRST_NAME = "John";
    private static final String TEST_LAST_NAME = "Doe";
    private static final String TEST_IMAGE_URL = "/api/user/test@example.com/pic";

    @TempDir
    Path tempDir;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LinkRepository linkRepo;

    private AppService appService;

    @BeforeEach
    void setUp() {
        System.setProperty("pics.directory", tempDir.toString());
        appService = new AppService(userRepository, linkRepo, tempDir.toString());
    }

    @AfterEach
    void cleanup() throws IOException {
        userRepository.deleteAll();
    }

    @Test
    void userExists_WhenUserDoesNotExist_ReturnsFalse() {
        assertFalse(appService.userExists(TEST_EMAIL));
    }

    @Test
    void userExists_WhenUserExists_ReturnsTrue() {
        createTestUser();
        assertTrue(appService.userExists(TEST_EMAIL));
    }

    @Test
    void registerUser_CreatesNewUser() {
        appService.registerUser(TEST_EMAIL);

        Optional<User> user = userRepository.findById(TEST_EMAIL);
        assertTrue(user.isPresent());
        assertEquals(TEST_EMAIL, user.get().getEmail());
    }

    @Test
    void updateUser_WhenUserExists_UpdatesUserDetails() {
        createTestUser();
        UserDto dto = new UserDto("Jane", "Smith");

        Optional<User> result = appService.updateUser(TEST_EMAIL, dto);

        assertTrue(result.isPresent());
        User updatedUser = result.get();
        assertEquals("Jane", updatedUser.getFirstName());
        assertEquals("Smith", updatedUser.getLastName());
    }

    @Test
    void updateUser_WhenUserDoesNotExist_ReturnsEmpty() {
        UserDto dto = new UserDto("Jane", "Smith");
        Optional<User> result = appService.updateUser(TEST_EMAIL, dto);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateProfilePicture_WhenUserExists_SavesPictureAndUpdatesUrl() throws IOException {
        createTestUser();
        MultipartFile file = new MockMultipartFile(
                "pic.jpg",
                "pic.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        Optional<User> result = appService.updateProfilePicture(TEST_EMAIL, file);

        assertTrue(result.isPresent());
        User updatedUser = result.get();
        assertEquals("/api/user/" + TEST_EMAIL + "/pic", updatedUser.getImageUrl());
        assertTrue(Files.exists(tempDir.resolve(TEST_EMAIL + ".jpg")));
    }

    @Test
    void updateProfilePicture_WhenUserDoesNotExist_ReturnsEmpty() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "pic.jpg",
                "pic.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        Optional<User> result = appService.updateProfilePicture(TEST_EMAIL, file);
        assertTrue(result.isEmpty());
    }

    @Test
    void getProfilePicture_WhenPictureExists_ReturnsPictureBytes() throws IOException {
        // Create test picture file
        Path picPath = tempDir.resolve(TEST_EMAIL + ".jpg");
        byte[] testContent = "test image content".getBytes();
        Files.write(picPath, testContent);

        byte[] result = appService.getProfilePicture(TEST_EMAIL);
        assertArrayEquals(testContent, result);
    }

    @Test
    void getProfilePicture_WhenPictureDoesNotExist_ReturnsNull() throws IOException {
        byte[] result = appService.getProfilePicture(TEST_EMAIL);
        assertNull(result);
    }

    @Test
    @Disabled
    void addLink_WhenUserExists_AddsNewLink() {
        createTestUser();
        LinkDto dto = new LinkDto("Test Link", "https://example.com");

        Optional<User> result = appService.addLink(TEST_EMAIL, dto);

        assertTrue(result.isPresent());
        User updatedUser = result.get();
        assertEquals(1, updatedUser.getLinks().size());
        Link addedLink = updatedUser.getLinks().iterator().next();
        assertEquals("Test Link", addedLink.getTitle());
        assertEquals("https://example.com", addedLink.getUrl());
    }

    @Test
    void addLink_WhenUserDoesNotExist_ReturnsEmpty() {
        LinkDto dto = new LinkDto("Test Link", "https://example.com");
        Optional<User> result = appService.addLink(TEST_EMAIL, dto);
        assertTrue(result.isEmpty());
    }

    @Test
    @Disabled
    void getPublicProfile_WhenUserExists_ReturnsProfile() {
        User user = createTestUser();
        user.getLinks().add(new Link(null, "Test Link", "https://example.com", user));
        userRepository.save(user);

        var result = appService.getPublicProfile(TEST_EMAIL);

        assertTrue(result.isPresent());
        var profile = result.get();
        assertEquals(TEST_EMAIL, profile.email());
        assertEquals(TEST_FIRST_NAME, profile.firstName());
        assertEquals(TEST_LAST_NAME, profile.lastName());
        assertEquals(1, profile.links().size());
        assertEquals("Test Link", profile.links().get(0).title());
        assertEquals("https://example.com", profile.links().get(0).url());
    }

    @Test
    void getPublicProfile_WhenUserDoesNotExist_ReturnsEmpty() {
        var result = appService.getPublicProfile(TEST_EMAIL);
        assertTrue(result.isEmpty());
    }

    private User createTestUser() {
        User user = new User();
        user.setEmail(TEST_EMAIL);
        user.setFirstName(TEST_FIRST_NAME);
        user.setLastName(TEST_LAST_NAME);
        user.setImageUrl(TEST_IMAGE_URL);
        user.setLinks(new ArrayList<>());
        return userRepository.save(user);
    }
}
