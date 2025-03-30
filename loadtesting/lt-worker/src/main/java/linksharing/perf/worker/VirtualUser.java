package linksharing.perf.worker;

import io.github.serpro69.kfaker.Faker;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Random;

@Component
@Scope("prototype")
public class VirtualUser implements Runnable {

    private static final Faker faker = new Faker();
    private final RestTemplate timedRest;
    private final RestTemplate rest;
    private final Random random = new Random();
    @Setter
    private String targetBaseUrl;
    private String email;

    public VirtualUser(
            @Qualifier("timedRestTemplate") RestTemplate timedRest,
            @Qualifier("restTemplate") RestTemplate rest
    ) {
        this.timedRest = timedRest;
        this.rest = rest; // non timed
    }

    @PostConstruct
    public void init() {
        email = faker.getInternet().email();
    }

    // todo: what happens if an exception is thrown inside run()? (e.g. 404 not found)
    @Override
    public void run() {
        while (!isInterrupted()) {
            if (!isUserExists(email)) {
                registerUser(email);
            }

            if (isInterrupted()) break;
            var userDto = new UserDto(faker.getName().firstName(), faker.getName().lastName());
            updateUser(email, userDto);

            if (isInterrupted()) break;
            updateProfilePicture(email);

            if (isInterrupted()) break;
            for (int i = 0; i < rndNum(5, 8) && !isInterrupted(); i++) {
                addLink(email, new LinkDto(faker.getName().name(), faker.getTheITCrowd().quotes()));
            }

            if (isInterrupted()) break;
            for (int i = 0; i < rndNum(9, 11) && !isInterrupted(); i++) {
                fetchPublicProfile(email);
            }

        }
    }

    private boolean isInterrupted() {
        return Thread.currentThread().isInterrupted();
    }

    private boolean isUserExists(String email) {
        try {
            rest.exchange(
                    targetBaseUrl + "/api/user/" + email + "/exists",
                    HttpMethod.GET,
                    null,
                    String.class);

            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    private void registerUser(String email) {
        timedRest.postForObject(targetBaseUrl + "/api/user/register", email, String.class);
    }

    private void updateUser(String email, UserDto userDto) {
        timedRest.exchange(targetBaseUrl + "/api/user/" + email, HttpMethod.PUT, new HttpEntity<>(userDto), String.class);
    }

    private void updateProfilePicture(String email) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        byte[] imageData = new byte[130 * 1024];
        random.nextBytes(imageData);
        ByteArrayResource resource = new ByteArrayResource(imageData) {
            @Override
            public String getFilename() {
                return "img.jpg";
            }
        };
        body.add("file", resource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        timedRest.postForObject(
                targetBaseUrl + "/api/user/" + email + "/pic",
                requestEntity,
                String.class);
    }

    private byte[] fetchProfilePicture(String email) {
        return timedRest.getForObject(
                targetBaseUrl + "/api/user/" + email + "/pic",
                byte[].class);
    }

    private void addLink(String email, LinkDto linkDto) {
        timedRest.postForObject(
                targetBaseUrl + "/api/user/" + email + "/links",
                linkDto,
                String.class);
    }

    private void fetchPublicProfile(String email) {
        timedRest.getForObject(
                targetBaseUrl + "/api/public/" + email,
                InfoDto.class);
    }

    int rndNum(int min, int max) {
        return random.nextInt(max - min) + min;
    }

    public record UserDto(String firstName, String lastName) {
    }

    public record InfoDto(String email, String firstName, String lastName, String imageUrl, List<LinkDto> links) {
    }

    public record LinkDto(String title, String url) {
    }
}
