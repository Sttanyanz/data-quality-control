package com.example.data_quality_control.controller;

import com.example.data_quality_control.domain.KkdRun;
import com.example.data_quality_control.service.KkdRunService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class KkdRunController {

    private final KkdRunService kkdRunService;

    public KkdRunController(KkdRunService kkdRunService) {
        this.kkdRunService = kkdRunService;
    }

    @GetMapping("/runs")
    public List<KkdRun> getRuns() {
        return kkdRunService.getAllRuns();
    }
}
