package com.example.data_quality_control.rule;

import com.example.data_quality_control.domain.rule.GoalForRule;
import com.example.data_quality_control.domain.rule.IssueProposal;
import com.example.data_quality_control.domain.rule.KeyResultForRule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RuleTest {

    @Test
    void goalNoOwnerRule_shouldDetectGoalsWithoutOwner() {
        // Arrange
        GoalForRule goal1 = new GoalForRule(1L, 10L, "active");
        GoalForRule goal2 = new GoalForRule(2L, null, "active");
        List<GoalForRule> goals = List.of(goal1, goal2);
        Map<Long, List<KeyResultForRule>> krsByGoal = Map.of();

        GoalNoOwnerRule rule = new GoalNoOwnerRule();

        // Act
        List<IssueProposal> issues = rule.evaluate(goals, krsByGoal);

        // Assert
        assertThat(issues).hasSize(1);
        assertThat(issues.getFirst().goalId()).isEqualTo(2L);
        assertThat(issues.getFirst().message()).contains("У цели отсутствует ответственный");
    }

    @Test
    void krWeightsSumRule_validWeights_shouldReturnNoIssues() {
        // Arrange
        GoalForRule goal = new GoalForRule(1L, 10L, "active");
        List<GoalForRule> goals = List.of(goal);

        KeyResultForRule kr1 = new KeyResultForRule(1L, 1L, new BigDecimal("50.00"), BigDecimal.TEN, Instant.now());
        KeyResultForRule kr2 = new KeyResultForRule(2L, 1L, new BigDecimal("50.00"), BigDecimal.TEN, Instant.now());
        Map<Long, List<KeyResultForRule>> krsByGoal = Map.of(1L, List.of(kr1, kr2));

        KrWeightsSumRule rule = new KrWeightsSumRule();

        // Act
        List<IssueProposal> issues = rule.evaluate(goals, krsByGoal);

        // Assert
        assertThat(issues).isEmpty();
    }

    @Test
    void krWeightsSumRule_invalidWeights_shouldReturnIssue() {
        // Arrange
        GoalForRule goal = new GoalForRule(1L, 10L, "active");
        List<GoalForRule> goals = List.of(goal);

        KeyResultForRule kr1 = new KeyResultForRule(1L, 1L, new BigDecimal("99.99"), BigDecimal.TEN, Instant.now());
        KeyResultForRule kr2 = new KeyResultForRule(2L, 1L, new BigDecimal("0.00"), BigDecimal.TEN, Instant.now());
        Map<Long, List<KeyResultForRule>> krsByGoal = Map.of(1L, List.of(kr1, kr2));

        KrWeightsSumRule rule = new KrWeightsSumRule();

        // Act
        List<IssueProposal> issues = rule.evaluate(goals, krsByGoal);

        // Assert
        assertThat(issues).hasSize(1);
        assertThat(issues.getFirst().goalId()).isEqualTo(1L);
        assertThat(issues.getFirst().message()).contains("Сумма весов ключевых результатов не равна 100%");
    }

    @Test
    void krStaleProgressRule_exactly45Days_shouldReturnNoIssue() {
        // Arrange
        Instant now = Instant.parse("2026-01-01T12:00:00Z");
        Clock fixedClock = Clock.fixed(now, ZoneOffset.UTC);

        KeyResultForRule kr = new KeyResultForRule(1L, 1L, BigDecimal.TEN, BigDecimal.TEN, now.minus(45, ChronoUnit.DAYS));
        Map<Long, List<KeyResultForRule>> krsByGoal = Map.of(1L, List.of(kr));

        KrStaleProgressRule rule = new KrStaleProgressRule(fixedClock);

        // Act
        List<IssueProposal> issues = rule.evaluate(List.of(), krsByGoal);

        // Assert
        assertThat(issues).isEmpty();
    }

    @Test
    void krStaleProgressRule_46Days_shouldReturnIssue() {
        // Arrange
        Instant now = Instant.parse("2026-01-01T12:00:00Z");
        Clock fixedClock = Clock.fixed(now, ZoneOffset.UTC);

        KeyResultForRule kr = new KeyResultForRule(1L, 1L, BigDecimal.TEN, BigDecimal.TEN, now.minus(46, ChronoUnit.DAYS));
        Map<Long, List<KeyResultForRule>> krsByGoal = Map.of(1L, List.of(kr));

        KrStaleProgressRule rule = new KrStaleProgressRule(fixedClock);

        // Act
        List<IssueProposal> issues = rule.evaluate(List.of(), krsByGoal);

        // Assert
        assertThat(issues).hasSize(1);
        assertThat(issues.getFirst().keyResultId()).isEqualTo(1L);
        assertThat(issues.getFirst().message()).contains("Факт ключевого результата не обновлялся более 45 дней");
    }

    @Test
    void krStaleProgressRule_nullFactUpdatedAt_shouldReturnNoIssue() {
        // Arrange
        Instant now = Instant.parse("2026-01-01T12:00:00Z");
        Clock fixedClock = Clock.fixed(now, ZoneOffset.UTC);

        KeyResultForRule kr = new KeyResultForRule(1L, 1L, BigDecimal.TEN, BigDecimal.TEN, null);
        Map<Long, List<KeyResultForRule>> krsByGoal = Map.of(1L, List.of(kr));

        KrStaleProgressRule rule = new KrStaleProgressRule(fixedClock);

        // Act
        List<IssueProposal> issues = rule.evaluate(List.of(), krsByGoal);

        // Assert
        assertThat(issues).isEmpty();
    }
}
