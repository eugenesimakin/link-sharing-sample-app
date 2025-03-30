package linksharing.perf.worker;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Metric {
    private String requestPath;
    private long responseTime;
    private int errorCode;
}
