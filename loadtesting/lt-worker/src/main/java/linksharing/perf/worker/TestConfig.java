package linksharing.perf.worker;

import lombok.Data;

@Data
public class TestConfig {
    private String targetUrl;
    private int duration; // in seconds
    private int numOfUsers; // for each worker node
    private int rampUpTime; // in seconds
}
