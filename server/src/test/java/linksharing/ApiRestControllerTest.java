package linksharing;

import linksharing.dto.LinkDto;
import linksharing.dto.UserDto;
import linksharing.dto.InfoDto;
import linksharing.db.User;
import linksharing.service.AppService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ApiRestControllerTest {

    @Mock
    private AppService appService;

    @InjectMocks
    private ApiRestController controller;

    private static final String TEST_EMAIL = "test@example.com";

    @Test
    void healthCheck_ShouldReturnOk() {
        ResponseEntity<?> response = controller.healthCheck();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Ok", response.getBody());
    }

    @Test
    void userExists_WhenUserExists_ShouldReturnOk() {
        when(appService.userExists(TEST_EMAIL)).thenReturn(true);

        ResponseEntity<?> response = controller.userExists(TEST_EMAIL);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(appService).userExists(TEST_EMAIL);
    }

    @Test
    void userExists_WhenUserDoesNotExist_ShouldReturnNotFound() {
        when(appService.userExists(TEST_EMAIL)).thenReturn(false);

        ResponseEntity<?> response = controller.userExists(TEST_EMAIL);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(appService).userExists(TEST_EMAIL);
    }

    @Test
    void registerUser_ShouldRegisterAndReturnOk() {
        ResponseEntity<?> response = controller.registerUser(TEST_EMAIL);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(TEST_EMAIL, response.getBody());
        verify(appService).registerUser(TEST_EMAIL);
    }

    @Test
    void registerUser_WhenUserAlreadyExists_ShouldReturnOk() {
//        when(appService.userExists(TEST_EMAIL)).thenReturn(true);

        ResponseEntity<?> response = controller.registerUser(TEST_EMAIL);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(TEST_EMAIL, response.getBody());
        verify(appService).registerUser(TEST_EMAIL);
    }

    @Test
    void updateUser_WhenUserExists_ShouldUpdateAndReturnOk() {
        UserDto dto = new UserDto("John", "Doe");
        User user = new User();
        when(appService.updateUser(TEST_EMAIL, dto)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = controller.updateUser(TEST_EMAIL, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(TEST_EMAIL, response.getBody());
        verify(appService).updateUser(TEST_EMAIL, dto);
    }

    @Test
    void updateUser_WhenUserDoesNotExist_ShouldReturnNotFound() {
        UserDto dto = new UserDto("John", "Doe");
        when(appService.updateUser(TEST_EMAIL, dto)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.updateUser(TEST_EMAIL, dto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(appService).updateUser(TEST_EMAIL, dto);
    }

    @Test
    void updateProfilePicture_WithValidJpeg_ShouldUpdateAndReturnOk() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", "test image content".getBytes()
        );
        User user = new User();
        when(appService.updateProfilePicture(eq(TEST_EMAIL), any())).thenReturn(Optional.of(user));

        ResponseEntity<?> response = controller.updateProfilePicture(TEST_EMAIL, file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(TEST_EMAIL, response.getBody());
        verify(appService).updateProfilePicture(eq(TEST_EMAIL), any());
    }

    @Test
    void updateProfilePicture_WithInvalidFileType_ShouldReturnBadRequest() {
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.png", "image/png", "test image content".getBytes()
        );

        ResponseEntity<?> response = controller.updateProfilePicture(TEST_EMAIL, file);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Only .jpg or .jpeg files supported"));
    }

    @Test
    void getProfilePicture_WhenExists_ShouldReturnImage() throws IOException {
        byte[] imageBytes = "test image content".getBytes();
        when(appService.getProfilePicture(TEST_EMAIL)).thenReturn(imageBytes);

        ResponseEntity<?> response = controller.getProfilePicture(TEST_EMAIL);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.IMAGE_JPEG, response.getHeaders().getContentType());
        assertArrayEquals(imageBytes, (byte[]) response.getBody());
    }

    @Test
    void getProfilePicture_WhenDoesNotExist_ShouldReturnNotFound() throws IOException {
        when(appService.getProfilePicture(TEST_EMAIL)).thenReturn(null);

        ResponseEntity<?> response = controller.getProfilePicture(TEST_EMAIL);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void addLink_WhenUserExists_ShouldAddLinkAndReturnOk() {
        LinkDto dto = new LinkDto("Test Link", "https://example.com");
        User user = new User();
        when(appService.addLink(TEST_EMAIL, dto)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = controller.addLink(TEST_EMAIL, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(TEST_EMAIL, response.getBody());
        verify(appService).addLink(TEST_EMAIL, dto);
    }

    @Test
    void addLink_WhenUserDoesNotExist_ShouldReturnNotFound() {
        LinkDto dto = new LinkDto("Test Link", "https://example.com");
        when(appService.addLink(TEST_EMAIL, dto)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.addLink(TEST_EMAIL, dto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(appService).addLink(TEST_EMAIL, dto);
    }

    @Test
    void getPublicProfile_WhenExists_ShouldReturnProfile() {
        InfoDto infoDto = new InfoDto(TEST_EMAIL, "John", "Doe", "/api/user/test@example.com/pic", List.of());
        when(appService.getPublicProfile(TEST_EMAIL)).thenReturn(Optional.of(infoDto));

        ResponseEntity<?> response = controller.getPublicProfile(TEST_EMAIL);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(infoDto, response.getBody());
        verify(appService).getPublicProfile(TEST_EMAIL);
    }

    @Test
    void getPublicProfile_WhenDoesNotExist_ShouldReturnNotFound() {
        when(appService.getPublicProfile(TEST_EMAIL)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getPublicProfile(TEST_EMAIL);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(appService).getPublicProfile(TEST_EMAIL);
    }
}
