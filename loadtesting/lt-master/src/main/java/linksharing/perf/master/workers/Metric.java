package linksharing.perf.master.workers;

import lombok.Data;

@Data
public class Metric {
    private String requestPath;
    private long responseTime;
    private int errorCode;
}
