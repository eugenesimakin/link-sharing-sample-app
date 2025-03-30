package linksharing.perf.worker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class RegisterWorkerTask implements ApplicationRunner {

    private final RestTemplate rest;
    @Value("${server.host}")
    private String host;
    @Value("${server.port}")
    private int port;

    public RegisterWorkerTask(RestTemplate rest) {
        this.rest = rest;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var body = Map.of(
                "status", WorkerController.STATUS_PATH,
                "control", WorkerController.CONTROL_PATH,
                "base", "http://" + host + ":" + port
        );
        var request = new HttpEntity<>(body, headers);
        rest.postForEntity("http://master.internal:8080/api/workers", request, String.class);
    }
}
