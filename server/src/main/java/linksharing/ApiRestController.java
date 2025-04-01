package linksharing;

import linksharing.dto.LinkDto;
import linksharing.dto.UserDto;
import linksharing.service.AppService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
public class ApiRestController {

    private final AppService appService;

    public ApiRestController(AppService appService) {
        this.appService = appService;
    }

    @GetMapping("/api/check")
    ResponseEntity<?> healthCheck() {
        // todo: db health check
        return ResponseEntity.ok("Ok");
    }

    @GetMapping("/api/user/{email}/exists")
    ResponseEntity<?> userExists(@PathVariable String email) {
        if (!appService.userExists(email)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/user/register")
    ResponseEntity<?> registerUser(@RequestBody String email) {
        appService.registerUser(email);
        return ResponseEntity.ok(email);
    }

    @PutMapping("/api/user/{email}")
    ResponseEntity<?> updateUser(@PathVariable String email, @RequestBody UserDto dto) {
        return appService.updateUser(email, dto)
                .map(user -> ResponseEntity.ok(email))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/api/user/{email}/pic")
    ResponseEntity<?> updateProfilePicture(@PathVariable String email, @RequestParam("file") MultipartFile file) {
        if (!"image/jpeg".equals(file.getContentType())) {
            return new ResponseEntity<>("Only .jpg or .jpeg files supported.", HttpStatus.BAD_REQUEST);
        }

        try {
            return appService.updateProfilePicture(email, file)
                    .map(user -> ResponseEntity.ok(email))
                    .orElse(ResponseEntity.notFound().build());
        } catch (IOException e) {
            return new ResponseEntity<>("Failed to save profile picture: " + e.getMessage(), INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/api/user/{email}/pic")
    ResponseEntity<?> getProfilePicture(@PathVariable String email) {
        try {
            byte[] imageBytes = appService.getProfilePicture(email);
            if (imageBytes == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(imageBytes);
        } catch (IOException e) {
            return new ResponseEntity<>("Failed to read profile picture: " + e.getMessage(), INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/user/{email}/links")
    ResponseEntity<?> addLink(@PathVariable String email, @RequestBody LinkDto dto) {
        return appService.addLink(email, dto)
                .map(user -> ResponseEntity.ok(email))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/public/{email}")
    ResponseEntity<?> getPublicProfile(@PathVariable String email) {
        return appService.getPublicProfile(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
