package com.example.data_quality_control.controller;

import com.example.data_quality_control.domain.summary.SummaryResponse;
import com.example.data_quality_control.service.KkdSummaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kkd")
public class KkdSummaryController {

    private final KkdSummaryService kkdSummaryService;

    public KkdSummaryController(KkdSummaryService kkdSummaryService) {
        this.kkdSummaryService = kkdSummaryService;
    }

    @GetMapping("/summary")
    public SummaryResponse getSummary(
        @RequestParam("run_id") Long runId,
        @RequestParam("unit_id") Long unitId
    ) {
        return kkdSummaryService.getSummary(runId, unitId);
    }
}
