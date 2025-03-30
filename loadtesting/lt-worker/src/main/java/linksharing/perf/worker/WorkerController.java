package linksharing.perf.worker;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkerController {

    public static final String STATUS_PATH = "/api/status";
    public static final String CONTROL_PATH = "/api/cmd";

    private final WorkerService service;

    public WorkerController(WorkerService service) {
        this.service = service;
    }

    @GetMapping(STATUS_PATH)
    public String getStatus() {
        return service.getStatus();
    }

    @PostMapping(CONTROL_PATH + "/start")
    public String start(@RequestBody TestConfig config) {
        service.start(config);
        return "ok";
    }

    @PostMapping(CONTROL_PATH + "/stop")
    public String stop() {
        service.stop();
        return "ok";
    }

    @PostMapping(CONTROL_PATH + "/reset")
    public String reset() {
        service.reset();
        return "ok";
    }
}
