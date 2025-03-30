package linksharing.perf.master;

import lombok.Data;

@Data
public class TestConfig {
    private String targetUrl;
    private long duration; // in seconds
    private int numOfUsers; // for each worker node
    private long rampUpTime; // in seconds
}
