package com.example.data_quality_control.service;

import com.example.data_quality_control.domain.KkdIssue;
import com.example.data_quality_control.domain.KkdRun;
import com.example.data_quality_control.domain.rule.GoalForRule;
import com.example.data_quality_control.domain.rule.IssueProposal;
import com.example.data_quality_control.domain.rule.KeyResultForRule;
import com.example.data_quality_control.repository.GoalRepository;
import com.example.data_quality_control.repository.KkdIssueRepository;
import com.example.data_quality_control.repository.KkdRuleRepository;
import com.example.data_quality_control.repository.KkdRunRepository;
import com.example.data_quality_control.rule.RuleEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KkdRunService {

    private static final Logger log = LoggerFactory.getLogger(KkdRunService.class);

    private final KkdRunRepository runRepository;
    private final GoalRepository goalRepository;
    private final KkdRuleRepository kkdRuleRepository;
    private final KkdIssueRepository kkdIssueRepository;
    private final RuleEngine ruleEngine;
    private final Clock clock;

    public KkdRunService(
            KkdRunRepository runRepository,
            GoalRepository goalRepository,
            KkdRuleRepository kkdRuleRepository,
            KkdIssueRepository kkdIssueRepository,
            RuleEngine ruleEngine,
            Clock clock
    ) {
        this.runRepository = runRepository;
        this.goalRepository = goalRepository;
        this.kkdRuleRepository = kkdRuleRepository;
        this.kkdIssueRepository = kkdIssueRepository;
        this.ruleEngine = ruleEngine;
        this.clock = clock;
    }

    public List<KkdRun> getAllRuns() {
        return runRepository.findAll();
    }

    @Transactional
    public void executeRun() {
        Long newRunId = null;
        try {
            log.info("Starting new run execution");
            Long comparedToRunId = runRepository.findLatestCompletedRunId();
            log.info("Found comparedToRunId: {}", comparedToRunId);

            newRunId = runRepository.createRun(comparedToRunId, LocalDate.now(clock));
            log.info("Created new run with id: {}", newRunId);

            List<GoalForRule> goals = goalRepository.findActiveGoalsForRule();
            List<KeyResultForRule> krs = goalRepository.findAllKrsForActiveGoals();
            log.info("Found {} active goals and {} key results", goals.size(), krs.size());

            Map<Long, List<KeyResultForRule>> krsMap = krs.stream()
                    .collect(Collectors.groupingBy(KeyResultForRule::goalId));

            List<IssueProposal> proposals = ruleEngine.analyze(goals, krsMap);
            log.info("Rule engine generated {} issue proposals", proposals.size());

            Map<String, String> severities = kkdRuleRepository.getRuleSeverities();
            Map<String, Long> ruleIds = kkdRuleRepository.getRuleIds();

            final Long finalRunId = newRunId;
            List<KkdIssue> issues = proposals.stream()
                    .map(proposal -> {
                        Long ruleId = ruleIds.get(proposal.ruleCode());
                        String severity = severities.get(proposal.ruleCode());
                        return new KkdIssue(
                                finalRunId,
                                ruleId,
                                proposal.goalId(),
                                proposal.keyResultId(),
                                severity,
                                proposal.message()
                        );
                    })
                    .toList();

            if (!issues.isEmpty()) {
                kkdIssueRepository.saveAll(newRunId, issues);
                log.info("Saved {} issues to database", issues.size());
            }

            runRepository.updateStatus(newRunId, "COMPLETED");
            log.info("Run {} completed successfully", newRunId);

        } catch (Exception e) {
            log.error("Run {} failed, updating status to FAILED", newRunId, e);
            if (newRunId != null) {
                runRepository.updateStatus(newRunId, "FAILED");
            }
            throw e;
        }
    }
}
