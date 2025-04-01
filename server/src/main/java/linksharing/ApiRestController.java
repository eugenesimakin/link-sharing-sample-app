package linksharing;

import linksharing.db.Link;
import linksharing.db.User;
import linksharing.db.UserRepository;
import linksharing.dto.InfoDto;
import linksharing.dto.LinkDto;
import linksharing.dto.UserDto;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
public class ApiRestController {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ApiRestController.class);

    private final UserRepository userRepo;

    public ApiRestController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/api/check")
    ResponseEntity<?> healthCheck() {
        // todo: db health check
        return ResponseEntity.ok("Ok");
    }

    @GetMapping("/api/user/{email}/exists")
    ResponseEntity<?> userExists(@PathVariable String email) {
        boolean exists = userRepo.existsById(email);
        if (!exists) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/user/register")
    ResponseEntity<?> registerUser(@RequestBody String email) {
        log.info("Registering user: {}", email);
        var user = new User();
        user.setEmail(email);
        userRepo.save(user);
        return ResponseEntity.ok(email);
    }

    @PutMapping("/api/user/{email}")
    ResponseEntity<?> updateUser(@PathVariable String email, @RequestBody UserDto dto) {
        log.info("Updating user: {}", email);
        var userOpt = userRepo.findById(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var user = userOpt.get();
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        userRepo.save(user);
        return ResponseEntity.ok(email);
    }

    @PostMapping("/api/user/{email}/pic")
    ResponseEntity<?> updateProfilePicture(@PathVariable String email, @RequestParam("file") MultipartFile file) {
        log.info("Updating profile picture for user: {}", email);
        var userOpt = userRepo.findById(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var user = userOpt.get();
        user.setImageUrl("/api/user{" + email + "}/pic");
        userRepo.save(user);

        if (!"image/jpeg".equals(file.getContentType())) {
            return new ResponseEntity<>("Only .jpg or .jpeg files supported.", HttpStatus.BAD_REQUEST);
        }

        try {
            Path uploadDir = Paths.get("pics");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path filePath = uploadDir.resolve(email + ".jpg");
            Files.copy(file.getInputStream(), filePath, REPLACE_EXISTING);
        } catch (IOException e) {
            return new ResponseEntity<>("Failed to save profile picture: " + e.getMessage(), INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok(email);
    }

    @GetMapping("/api/user/{email}/pic")
    ResponseEntity<?> getProfilePicture(@PathVariable String email) {
        Path imagePath = Paths.get("pics", email + ".jpg");
        if (!Files.exists(imagePath)) {
            return ResponseEntity.notFound().build();
        }

        try {
            byte[] imageBytes = Files.readAllBytes(imagePath);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(imageBytes);
        } catch (IOException e) {
            return new ResponseEntity<>("Failed to read profile picture: " + e.getMessage(), INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/user/{email}/links")
    ResponseEntity<?> addLink(@PathVariable String email, @RequestBody LinkDto dto) {
        log.info("Adding link for user: {}", email);
        var userOpt = userRepo.findById(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var user = userOpt.get();
        user.getLinks().add(new Link(null, dto.title(), dto.url(), user));
        userRepo.save(user);

        return ResponseEntity.ok(email);
    }

    @GetMapping("/api/public/{email}")
    ResponseEntity<?> getPublicProfile(@PathVariable String email) {
        var userOpt = userRepo.findById(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var user = userOpt.get();
        var dto = new InfoDto(
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getImageUrl(),
                user.getLinks().stream().map(l -> new LinkDto(l.getTitle(), l.getUrl())).toList()
        );
        return ResponseEntity.ok(dto);
    }
}
