package com.example.data_quality_control.controller;

import com.example.data_quality_control.domain.KkdIssueDto;
import com.example.data_quality_control.domain.PageResponse;
import com.example.data_quality_control.service.KkdIssueService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kkd")
public class KkdIssueController {

    private final KkdIssueService kkdIssueService;

    public KkdIssueController(KkdIssueService kkdIssueService) {
        this.kkdIssueService = kkdIssueService;
    }

    @GetMapping("/issues")
    public PageResponse<KkdIssueDto> getIssues(
        @RequestParam Long run_id,
        @RequestParam(required = false) Long unit_id,
        @RequestParam(required = false) String rule,
        @RequestParam(required = false) String severity,
        @RequestParam(defaultValue = "20") int limit,
        @RequestParam(defaultValue = "0") int offset
    ) {
        return kkdIssueService.getIssues(run_id, unit_id, rule, severity, limit, offset);
    }
}
