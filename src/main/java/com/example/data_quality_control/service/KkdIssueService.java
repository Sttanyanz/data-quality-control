package com.example.data_quality_control.service;

import com.example.data_quality_control.domain.KkdIssueDto;
import com.example.data_quality_control.domain.PageResponse;
import com.example.data_quality_control.repository.KkdIssueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KkdIssueService {

    private static final Logger log = LoggerFactory.getLogger(KkdIssueService.class);

    private final KkdIssueRepository kkdIssueRepository;

    public KkdIssueService(KkdIssueRepository kkdIssueRepository) {
        this.kkdIssueRepository = kkdIssueRepository;
    }

    @Transactional(readOnly =true)
    public PageResponse<KkdIssueDto> getIssues(Long runId, Long unitId, String ruleCode, String severity, int limit, int offset) {
        log.info("Fetching issues for runId={} with limit={} and offset={}", runId, limit, offset);
        List<KkdIssueDto> items = kkdIssueRepository.findIssues(runId, unitId, ruleCode, severity, limit, offset);
        long total = kkdIssueRepository.countIssues(runId, unitId, ruleCode, severity);
        return new PageResponse<>(items, limit, offset, total);
    }
}
