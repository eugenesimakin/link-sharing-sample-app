package linksharing.perf.master;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppController {

    private final MasterService service;

    public AppController(MasterService service) {
        this.service = service;
    }

    @PostMapping("/api/start")
    public ResponseEntity<?> startJob(@RequestBody TestConfig config) {
        service.start(config);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/stop")
    public ResponseEntity<?> stopJob() {
        service.stop();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/progress")
    public ResponseEntity<?> getProgress() {
        return ResponseEntity.ok(service.getProgress());
    }

    @PostMapping("/api/reset")
    public ResponseEntity<?> resetJob() {
        service.reset();
        return ResponseEntity.ok().build();
    }
}
