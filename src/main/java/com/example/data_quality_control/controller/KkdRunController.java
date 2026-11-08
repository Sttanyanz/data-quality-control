package com.example.data_quality_control.controller;

import com.example.data_quality_control.domain.KkdRun;
import com.example.data_quality_control.service.KkdRunService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/runs")
public class KkdRunController {

    private static final Logger log = LoggerFactory.getLogger(KkdRunController.class);

    private final KkdRunService kkdRunService;

    public KkdRunController(KkdRunService kkdRunService) {
        this.kkdRunService = kkdRunService;
    }

    @GetMapping
    public List<KkdRun> getRuns() {
        return kkdRunService.getAllRuns();
    }

    @PostMapping
    public ResponseEntity<Void> triggerRun() {
        log.info("Received request to trigger new run");
        kkdRunService.executeRun();
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
