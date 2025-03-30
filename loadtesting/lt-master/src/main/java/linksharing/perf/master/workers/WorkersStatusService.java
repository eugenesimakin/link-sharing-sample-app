package linksharing.perf.master.workers;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WorkersStatusService {

    private final Map<String, String> statuses = new ConcurrentHashMap<>();

    private final RestTemplate restTmpl;

    public WorkersStatusService(RestTemplate restTmpl) {
        this.restTmpl = restTmpl;
    }

    public void addWorker(String callbackUrl) {
        statuses.put(callbackUrl, "ready");
    }

    public List<String> getStatuses() {
        return List.copyOf(statuses.values());
    }

    @Scheduled(fixedRate = 1000)
    private void checkWorkers() {
        statuses.forEach((callbackUrl, currStatus) -> {
            try {
                var newStatus = restTmpl.getForObject(callbackUrl, String.class);
                statuses.put(callbackUrl, newStatus);
            } catch (ResourceAccessException e) {
                statuses.put(callbackUrl, "offline");
            } catch (RestClientException e) {
                statuses.put(callbackUrl, "error");
            }
        });
    }
}
