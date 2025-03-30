package linksharing.perf.master.workers;

import linksharing.perf.master.MasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class WorkerController {

    private final WorkersStatusService statusService;
    private final MasterService service;

    public WorkerController(WorkersStatusService statusService, MasterService service) {
        this.statusService = statusService;
        this.service = service;
    }

    // called by Vue.js frontend
    @GetMapping("/api/workers/status")
    public ResponseEntity<?> getStatusOfWorkers() {
        return ResponseEntity.ok(statusService.getStatuses());
    }

    // called by workers themselves
    @PostMapping("/api/workers")
    public ResponseEntity<?> addWorker(@RequestBody Map<String, String> paths) {
//        String remoteAddr = request.getRemoteAddr();
//        int remotePort = request.getRemotePort();
        var info = paths.get("base");

        var worker = new Worker();
        worker.setId(DigestUtils.md5DigestAsHex(info.getBytes()));
        worker.setControlUrl(info + paths.get("control"));
        worker.setStatusUrl(info + paths.get("status"));
        service.addWorker(worker);

        statusService.addWorker(worker.getStatusUrl());

        return ResponseEntity.ok("ok");
    }

    // called by workers themselves
    @PostMapping("/api/workers/metrics")
    public ResponseEntity<?> addMetrics(@RequestBody List<Metric> metrics) {
        service.onUpdateFromWorker(metrics);
        return ResponseEntity.ok("ok");
    }
}
