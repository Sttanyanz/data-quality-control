package com.example.data_quality_control.rule;

import com.example.data_quality_control.domain.rule.GoalForRule;
import com.example.data_quality_control.domain.rule.IssueProposal;
import com.example.data_quality_control.domain.rule.KeyResultForRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class KrWeightsSumRule implements KkdRule {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    @Override
    public String getCode() {
        return "KR_WEIGHTS_SUM";
    }

    @Override
    public List<IssueProposal> evaluate(List<GoalForRule> goals, Map<Long, List<KeyResultForRule>> krsByGoal) {
        return goals.stream()
                .filter(goal -> krsByGoal.containsKey(goal.id()))
                .filter(goal -> {
                    List<KeyResultForRule> krs = krsByGoal.get(goal.id());
                    BigDecimal sum = krs.stream()
                            .map(KeyResultForRule::weight)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return sum.compareTo(HUNDRED) != 0;
                })
                .map(goal -> new IssueProposal(getCode(), goal.id(), null, "Сумма весов ключевых результатов не равна 100%"))
                .collect(Collectors.toList());
    }
}
