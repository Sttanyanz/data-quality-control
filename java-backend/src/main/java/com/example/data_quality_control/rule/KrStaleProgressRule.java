package com.example.data_quality_control.rule;

import com.example.data_quality_control.domain.rule.GoalForRule;
import com.example.data_quality_control.domain.rule.IssueProposal;
import com.example.data_quality_control.domain.rule.KeyResultForRule;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class KrStaleProgressRule implements KkdRule {

    private final Clock clock;

    public KrStaleProgressRule(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String getCode() {
        return "KR_STALE_PROGRESS";
    }

    @Override
    public List<IssueProposal> evaluate(List<GoalForRule> goals, Map<Long, List<KeyResultForRule>> krsByGoal) {
        Instant threshold = Instant.now(clock).minus(45, ChronoUnit.DAYS);
        return krsByGoal.values().stream()
                .flatMap(List::stream)
                .filter(kr -> kr.factUpdatedAt() != null)
                .filter(kr -> kr.factUpdatedAt().isBefore(threshold))
                .map(kr -> new IssueProposal(getCode(), null, kr.id(), "Факт ключевого результата не обновлялся более 45 дней"))
                .collect(Collectors.toList());
    }
}
