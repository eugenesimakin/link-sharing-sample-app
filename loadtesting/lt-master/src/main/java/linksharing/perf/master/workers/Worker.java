package linksharing.perf.master.workers;

import lombok.Data;

import java.net.URI;

@Data
public class Worker {
    private String id; // combination of host and port (maybe hashed)
    private String statusUrl; // url to get status of worker
    private String controlUrl; // url to start or stop load testing job
    private String status = "ready"; // 'ready', 'running', 'completed', 'offline'

    public void setStatusUrl(String statusUrl) {
        this.statusUrl = URI.create(statusUrl).toString();
    }

    public void setControlUrl(String controlUrl) {
        this.controlUrl = URI.create(controlUrl).toString();
    }
}
