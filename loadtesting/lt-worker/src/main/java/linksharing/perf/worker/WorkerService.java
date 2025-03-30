package linksharing.perf.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class WorkerService {

    private static final Logger log = LoggerFactory.getLogger(WorkerService.class);

    private final RestTemplate rest;

    private final AtomicReference<Status> statusRef = new AtomicReference<>(Status.READY);
    private final Queue<Metric> metricsQueue;
    private final ApplicationContext context;
    //    private ExecutorService executor = Executors.newCachedThreadPool();
    private ExecutorService executor;
    private Timer rumpUpTimer;
    private Timer stopTimer;
    private Timer sendMetricsTimer;

    public WorkerService(RestTemplate rest, ApplicationContext context, @Qualifier("metricsQueue") Queue<Metric> metricsQueue) {
        this.rest = rest;
        this.context = context;
        this.metricsQueue = metricsQueue;
    }

    public void start(TestConfig config) {
        log.info("Staring load testing job");
        statusRef.set(Status.RAMPING_UP);

        executor = Executors.newFixedThreadPool(config.getNumOfUsers() + 1);
        rumpUpTimer = new Timer();
        stopTimer = new Timer();
        sendMetricsTimer = new Timer();


        int rampUpRate; // virtual users per second
        if (config.getNumOfUsers() < config.getRampUpTime()) {
            rampUpRate = 1;
        } else {
            rampUpRate = config.getNumOfUsers() / config.getRampUpTime();
        }

        long startTime = System.currentTimeMillis();

        long stopRampUpAfter = TimeUnit.SECONDS.toMillis(config.getRampUpTime());
        var rampUpTask = new TimerTask() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed >= stopRampUpAfter) {
                    log.info("Ramp up is finished");
                    if (statusRef.get() == Status.RAMPING_UP) {
                        statusRef.set(Status.RUNNING);
                        rumpUpTimer.cancel();
                    }
                    return;
                }
                for (int i = 0; i < rampUpRate && !Thread.interrupted(); i++) {
                    var vUser = context.getBean(VirtualUser.class);
                    vUser.setTargetBaseUrl(config.getTargetUrl());
                    executor.submit(vUser);
                }
            }
        };

        log.info("Starting rumping up task...");
        rumpUpTimer.scheduleAtFixedRate(rampUpTask, 500, 1000);

        long stopTaskDelay = stopRampUpAfter + TimeUnit.SECONDS.toMillis(config.getDuration());
        var stopTask = new TimerTask() {
            @Override
            public void run() {
                log.info("Stopping load testing job");
                if (statusRef.get() == Status.RUNNING) {
                    statusRef.set(Status.COMPLETED);
                }
                executor.shutdownNow();
                sendMetricsTimer.cancel();
            }
        };
        stopTimer.schedule(stopTask, stopTaskDelay);

        var sendMetricsTask = new TimerTask() {
            @Override
            public void run() {
                var metricsToSend = new ArrayList<>();
                while (!metricsQueue.isEmpty()) {
                    var m = metricsQueue.poll();
                    if (m == null) continue;
                    metricsToSend.add(m);
                }
                if (metricsToSend.isEmpty()) return;

                var headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                var request = new HttpEntity<>(metricsToSend, headers);
                rest.postForEntity("http://master.internal:8080/api/workers/metrics", request, String.class);
            }
        };
        sendMetricsTimer.scheduleAtFixedRate(sendMetricsTask, 1000, 1000);
    }

    public void stop() {
        statusRef.set(Status.READY);
        executor.shutdownNow();
        if (rumpUpTimer != null) {
            rumpUpTimer.cancel();
        }
        if (stopTimer != null) {
            stopTimer.cancel();
        }
        if (sendMetricsTimer != null) {
            sendMetricsTimer.cancel();
        }
    }

    public void reset() {
        statusRef.set(Status.READY);
        if (executor != null) executor.shutdownNow();
        metricsQueue.clear();
    }

    public String getStatus() {
        return statusRef.get().toString().toLowerCase();
    }

    enum Status {
        READY,
        RAMPING_UP,
        RUNNING,
        COMPLETED,
        ERROR
    }
}
