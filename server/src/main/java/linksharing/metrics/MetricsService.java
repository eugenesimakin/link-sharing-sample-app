package linksharing.metrics;

import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class MetricsService {

    private final MetricRepository repo;

    public MetricsService(MetricRepository repo) {
        this.repo = repo;
    }

    public void linkClicked(String userEmail, String linkUrl, String userAgent, String clientIp) {
        Metric metric = new Metric();
        metric.setUserEmail(userEmail);
        metric.setLinkUrl(linkUrl);
        metric.setUserAgent(userAgent);
        metric.setClientIp(clientIp);
        metric.setClickedAt(new Timestamp(System.currentTimeMillis()));
        repo.save(metric);
    }
}
