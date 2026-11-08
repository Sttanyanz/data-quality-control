package com.example.data_quality_control.controller;

import com.example.data_quality_control.domain.KkdIssueDto;
import com.example.data_quality_control.domain.PageResponse;
import com.example.data_quality_control.exception.ValidationException;
import com.example.data_quality_control.service.KkdIssueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kkd")
public class KkdIssueController {

    private static final Logger log = LoggerFactory.getLogger(KkdIssueController.class);

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
        log.info("Received request for issues: runId={}, unitId={}, rule={}, severity={}, limit={}, offset={}", run_id, unit_id, rule, severity, limit, offset);
        if (limit < 0 || offset < 0) {
            throw new ValidationException("Limit and offset must be non-negative");
        }
        if (limit > 100) {
            throw new ValidationException("Limit must be less than or equal to 100");
        }
        return kkdIssueService.getIssues(run_id, unit_id, rule, severity, limit, offset);
    }
}
