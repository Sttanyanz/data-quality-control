package com.example.data_quality_control.service;

import com.example.data_quality_control.domain.KkdIssue;
import com.example.data_quality_control.domain.rule.GoalForRule;
import com.example.data_quality_control.domain.rule.IssueProposal;
import com.example.data_quality_control.domain.rule.KeyResultForRule;
import com.example.data_quality_control.repository.GoalRepository;
import com.example.data_quality_control.repository.KkdIssueRepository;
import com.example.data_quality_control.repository.KkdRuleRepository;
import com.example.data_quality_control.repository.KkdRunRepository;
import com.example.data_quality_control.rule.RuleEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KkdRunServiceTest {

    @Mock
    private KkdRunRepository runRepository;

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private KkdRuleRepository kkdRuleRepository;

    @Mock
    private KkdIssueRepository kkdIssueRepository;

    @Mock
    private RuleEngine ruleEngine;

    @Mock
    private Clock clock;

    @InjectMocks
    private KkdRunService kkdRunService;

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(Instant.parse("2024-01-01T00:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
    }

    @Test
    void shouldExecuteRunSuccessfully() {
        Long comparedToRunId = 10L;
        Long newRunId = 20L;

        when(runRepository.findLatestCompletedRunId()).thenReturn(comparedToRunId);
        when(runRepository.createRun(eq(comparedToRunId), any(LocalDate.class))).thenReturn(newRunId);

        GoalForRule goal1 = new GoalForRule(1L, 100L, "ACTIVE");
        GoalForRule goal2 = new GoalForRule(2L, 100L, "ACTIVE");
        when(goalRepository.findActiveGoalsForRule()).thenReturn(List.of(goal1, goal2));

        KeyResultForRule kr1 = new KeyResultForRule(1L, 1L, BigDecimal.ONE, BigDecimal.TEN, Instant.now());
        KeyResultForRule kr2 = new KeyResultForRule(2L, 1L, BigDecimal.ONE, BigDecimal.TEN, Instant.now());
        KeyResultForRule kr3 = new KeyResultForRule(3L, 2L, BigDecimal.ONE, BigDecimal.TEN, Instant.now());
        when(goalRepository.findAllKrsForActiveGoals()).thenReturn(List.of(kr1, kr2, kr3));

        IssueProposal proposal1 = new IssueProposal("RULE_1", 1L, 1L, "Issue message 1");
        IssueProposal proposal2 = new IssueProposal("RULE_2", 2L, 3L, "Issue message 2");
        when(ruleEngine.analyze(any(), any())).thenReturn(List.of(proposal1, proposal2));

        when(kkdRuleRepository.getRuleSeverities()).thenReturn(Map.of(
            "RULE_1", "HIGH",
            "RULE_2", "MEDIUM"
        ));
        when(kkdRuleRepository.getRuleIds()).thenReturn(Map.of(
            "RULE_1", 1L,
            "RULE_2", 2L
        ));

        kkdRunService.executeRun();

        verify(runRepository).createRun(eq(comparedToRunId), any(LocalDate.class));
        verify(kkdIssueRepository).saveAll(eq(newRunId), any());

        ArgumentCaptor<List<KkdIssue>> issuesCaptor = ArgumentCaptor.forClass(List.class);
        verify(kkdIssueRepository).saveAll(eq(newRunId), issuesCaptor.capture());
        List<KkdIssue> savedIssues = issuesCaptor.getValue();
        assertThat(savedIssues).hasSize(2);
        assertThat(savedIssues.get(0).runId()).isEqualTo(newRunId);
        assertThat(savedIssues.get(0).ruleId()).isEqualTo(1L);
        assertThat(savedIssues.get(0).goalId()).isEqualTo(1L);
        assertThat(savedIssues.get(0).keyResultId()).isEqualTo(1L);
        assertThat(savedIssues.get(0).severity()).isEqualTo("HIGH");
        assertThat(savedIssues.get(0).message()).isEqualTo("Issue message 1");

        verify(runRepository).updateStatus(newRunId, "COMPLETED");
    }

    @Test
    void shouldFailRunWhenIssueRepositoryThrowsException() {
        Long comparedToRunId = 10L;
        Long newRunId = 20L;

        when(runRepository.findLatestCompletedRunId()).thenReturn(comparedToRunId);
        when(runRepository.createRun(eq(comparedToRunId), any(LocalDate.class))).thenReturn(newRunId);

        GoalForRule goal1 = new GoalForRule(1L, 100L, "ACTIVE");
        when(goalRepository.findActiveGoalsForRule()).thenReturn(List.of(goal1));

        KeyResultForRule kr1 = new KeyResultForRule(1L, 1L, BigDecimal.ONE, BigDecimal.TEN, Instant.now());
        when(goalRepository.findAllKrsForActiveGoals()).thenReturn(List.of(kr1));

        IssueProposal proposal1 = new IssueProposal("RULE_1", 1L, 1L, "Issue message 1");
        when(ruleEngine.analyze(any(), any())).thenReturn(List.of(proposal1));

        when(kkdRuleRepository.getRuleSeverities()).thenReturn(Map.of("RULE_1", "HIGH"));
        when(kkdRuleRepository.getRuleIds()).thenReturn(Map.of("RULE_1", 1L));

        doThrow(new RuntimeException("Database error")).when(kkdIssueRepository).saveAll(anyLong(), any());

        assertThatThrownBy(() -> kkdRunService.executeRun())
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Database error");

        verify(runRepository).updateStatus(newRunId, "FAILED");
    }
}
