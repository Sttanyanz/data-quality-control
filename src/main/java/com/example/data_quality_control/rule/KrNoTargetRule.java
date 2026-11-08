package com.example.data_quality_control.rule;

import com.example.data_quality_control.domain.rule.GoalForRule;
import com.example.data_quality_control.domain.rule.IssueProposal;
import com.example.data_quality_control.domain.rule.KeyResultForRule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class KrNoTargetRule implements KkdRule {

    @Override
    public String getCode() {
        return "KR_NO_TARGET";
    }

    @Override
    public List<IssueProposal> evaluate(List<GoalForRule> goals, Map<Long, List<KeyResultForRule>> krsByGoal) {
        return krsByGoal.values().stream()
                .flatMap(List::stream)
                .filter(kr -> kr.targetValue() == null)
                .map(kr -> new IssueProposal(getCode(), null, kr.id(), "У ключевого результата не заполнено целевое значение"))
                .collect(Collectors.toList());
    }
}
