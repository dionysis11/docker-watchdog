package gr.aueb.dmst.dockerWatchdog.controller;

import gr.aueb.dmst.dockerWatchdog.model.Metric;
import gr.aueb.dmst.dockerWatchdog.service.DockerService;
import gr.aueb.dmst.dockerWatchdog.model.Instance;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.List;

@RestController
@RequestMapping("/api/containers")
public class DockerController {

    private final DockerService dockerService;

    public DockerController(DockerService dockerService) {
        this.dockerService = dockerService;
    }

    @PostMapping("/{containerId}/start")
    public ResponseEntity<String> startContainer(@PathVariable("containerId") String containerId) {
        dockerService.startContainer(containerId);
        return ResponseEntity.ok("Container " + containerId + " started");
    }

    @PostMapping("/{containerId}/stop")
    public ResponseEntity<String> stopContainer(@PathVariable("containerId") String containerId) {
        dockerService.stopContainer(containerId);
        return ResponseEntity.ok("Container " + containerId + " stopped");
    }

    @GetMapping("/instances")
    public List<Instance> getAllInstances() {
        return dockerService.getAllInstancesMaxId();
    }

    @GetMapping("/metrics")
    public List<Metric> getMetrics(@RequestParam("startDate") String startDateString, @RequestParam("endDate") String endDateString) {
        Timestamp startDate = Timestamp.valueOf(startDateString);
        Timestamp endDate = Timestamp.valueOf(endDateString);
        return dockerService.getMetrics(startDate, endDate);
    }
}
