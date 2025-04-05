package linksharing.perf.master;

import linksharing.perf.master.workers.Metric;
import linksharing.perf.master.workers.Worker;
import lombok.Data;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class MasterService {

    private final Map<String, Worker> workers = new ConcurrentHashMap<>();
    private final RestTemplate rest = new RestTemplate();

    private final Object o = new Object();

    private final ConcurrentLinkedQueue<Metric> queue = new ConcurrentLinkedQueue<>();

    private final StatsPart publicStats = new StatsPart();
    private final StatsPart backofficeStats = new StatsPart();

    public void addWorker(Worker worker) {
        workers.put(worker.getId(), worker);
    }

    public void start(TestConfig config) {
        workers.values().forEach(worker -> {
            try {
                rest.postForEntity(worker.getControlUrl() + "/start", config, String.class);
            } catch (Exception ignored) {
            }
        });
    }

    public Map<String, StatsPart> getProgress() {
        synchronized (o) {
            return Map.of(
                    "public", publicStats.clone(),
                    "backoffice", backofficeStats.clone()
            );
        }
    }

    @Scheduled(fixedDelay = 1000)
    private void updateProgress() {
        if (queue.isEmpty()) {
            return;
        }

        var receivedMetrics = new ArrayList<Metric>();
        while (!queue.isEmpty()) {
            var m = queue.poll();
            if (m != null) {
                receivedMetrics.add(m);
            }
        }

        var backofficeMetrics = receivedMetrics.stream()
                .filter(m -> !m.getRequestPath().contains("public"))
                .toList();
        var publicMetrics = receivedMetrics.stream()
                .filter(m -> m.getRequestPath().contains("public"))
                .toList();

        synchronized (o) {
            publicStats.updateWithMetrics(publicMetrics);
            backofficeStats.updateWithMetrics(backofficeMetrics);
        }
    }

    public void onUpdateFromWorker(List<Metric> metrics) {
        if (metrics.isEmpty()) {
            return;
        }
        queue.addAll(metrics);
    }

    public void stop() {
        workers.values().forEach(worker -> {
            try {
                rest.postForEntity(worker.getControlUrl() + "/stop", null, String.class);
            } catch (Exception ignored) {
            }
        });
    }

    public void reset() {
        synchronized (o) {
            publicStats.reset();
            backofficeStats.reset();
        }
        workers.values().forEach(worker -> {
            try {
                rest.postForEntity(worker.getControlUrl() + "/reset", null, String.class);
            } catch (Exception ignored) {
            }
        });
    }

    @Data
    public static class StatsPart {
        private Integer averageResponseTime = null;
        private long requestsSent = 0;
        private long requestsFailed = 0;

        public void updateWithMetrics(List<Metric> metrics) {
            if (metrics.isEmpty()) {
                return;
            }
            requestsSent += metrics.size();
            requestsFailed += metrics.stream().filter(metric -> metric.getErrorCode() > 0).count();
            double nextAverageResponseTime = metrics.stream()
                    .mapToLong(Metric::getResponseTime)
                    .filter(time -> time > 0)
                    .average()
                    .orElse(0);
            if (nextAverageResponseTime == 0) {
                return;
            }
            if (averageResponseTime != null) {
                nextAverageResponseTime = ((double) averageResponseTime + nextAverageResponseTime) / 2;
            }
            averageResponseTime = (int) nextAverageResponseTime;
        }

        public StatsPart clone() {
            var stats = new StatsPart();
            stats.averageResponseTime = averageResponseTime;
            stats.requestsSent = requestsSent;
            stats.requestsFailed = requestsFailed;
            return stats;
        }

        public void reset() {
            averageResponseTime = null;
            requestsSent = 0;
            requestsFailed = 0;
        }
    }
}
