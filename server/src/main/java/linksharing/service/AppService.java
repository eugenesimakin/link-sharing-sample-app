package linksharing.service;

import linksharing.db.Link;
import linksharing.db.User;
import linksharing.db.UserRepository;
import linksharing.dto.InfoDto;
import linksharing.dto.LinkDto;
import linksharing.dto.UserDto;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
public class AppService {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(AppService.class);

    private final UserRepository userRepo;
    private final String picsDir;

    public AppService(UserRepository userRepo, @Value("${pics.directory}") String picsDir) {
        this.userRepo = userRepo;
        this.picsDir = picsDir;
    }

    public boolean userExists(String email) {
        return userRepo.existsById(email);
    }

    public void registerUser(String email) {
        log.info("Registering user: {}", email);
        var user = new User();
        user.setEmail(email);
        userRepo.save(user);
    }

    public Optional<User> updateUser(String email, UserDto dto) {
        log.info("Updating user: {}", email);
        var userOpt = userRepo.findById(email);
        if (userOpt.isPresent()) {
            var user = userOpt.get();
            user.setFirstName(dto.firstName());
            user.setLastName(dto.lastName());
            userRepo.save(user);
        }
        return userOpt;
    }

    public Optional<User> updateProfilePicture(String email, MultipartFile file) throws IOException {
        log.info("Updating profile picture for user: {}", email);
        var userOpt = userRepo.findById(email);
        if (userOpt.isPresent()) {
            var user = userOpt.get();
            user.setImageUrl("/api/user/" + email + "/pic");
            userRepo.save(user);

            Path uploadDir = Paths.get(picsDir);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path filePath = uploadDir.resolve(email + ".jpg");
            Files.copy(file.getInputStream(), filePath, REPLACE_EXISTING);
        }
        return userOpt;
    }

    public byte[] getProfilePicture(String email) throws IOException {
        Path imagePath = Paths.get(picsDir, email + ".jpg");
        if (!Files.exists(imagePath)) {
            return null;
        }
        return Files.readAllBytes(imagePath);
    }

    public Optional<User> addLink(String email, LinkDto dto) {
        log.info("Adding link for user: {}", email);
        var userOpt = userRepo.findById(email);
        if (userOpt.isPresent()) {
            var user = userOpt.get();
            user.getLinks().add(new Link(null, dto.title(), dto.url(), user));
            userRepo.save(user);
        }
        return userOpt;
    }

    public Optional<InfoDto> getPublicProfile(String email) {
        var userOpt = userRepo.findById(email);
        return userOpt.map(user -> new InfoDto(user.getEmail(), user.getFirstName(), user.getLastName(), user.getImageUrl(), user.getLinks().stream().map(l -> new LinkDto(l.getTitle(), l.getUrl())).toList()));
    }
}
