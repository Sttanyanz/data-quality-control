package com.example.data_quality_control.service;

import com.example.data_quality_control.domain.KkdIssueDto;
import com.example.data_quality_control.domain.PageResponse;
import com.example.data_quality_control.repository.KkdIssueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class KkdIssueService {

    private static final Logger log = LoggerFactory.getLogger(KkdIssueService.class);

    private final KkdIssueRepository kkdIssueRepository;
    private final ScopeService scopeService;

    public KkdIssueService(KkdIssueRepository kkdIssueRepository, ScopeService scopeService) {
        this.kkdIssueRepository = kkdIssueRepository;
        this.scopeService = scopeService;
    }

    @Transactional(readOnly = true)
    public PageResponse<KkdIssueDto> getIssues(Long runId, Long unitId, String ruleCode, String severity, int limit, int offset) {
        log.info("Fetching issues for runId={} with limit={} and offset={}", runId, limit, offset);

        Set<Long> goalIds = null;
        if (unitId != null) {
            goalIds = scopeService.getScopeGoalIds(unitId);
            if (goalIds.isEmpty()) {
                return new PageResponse<>(List.of(), limit, offset, 0);
            }
        }

        List<KkdIssueDto> items = kkdIssueRepository.findIssues(runId, goalIds, ruleCode, severity, limit, offset);
        long total = kkdIssueRepository.countIssues(runId, goalIds, ruleCode, severity);

        return new PageResponse<>(items, limit, offset, total);
    }
}