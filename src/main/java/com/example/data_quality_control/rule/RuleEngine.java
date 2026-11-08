package com.example.data_quality_control.rule;

import com.example.data_quality_control.domain.rule.GoalForRule;
import com.example.data_quality_control.domain.rule.IssueProposal;
import com.example.data_quality_control.domain.rule.KeyResultForRule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RuleEngine {

    private final List<KkdRule> rules;

    public RuleEngine(List<KkdRule> rules) {
        this.rules = rules;
    }

    public List<IssueProposal> analyze(List<GoalForRule> activeGoals, Map<Long, List<KeyResultForRule>> krsByGoal) {
        return rules.stream()
                .flatMap(rule -> rule.evaluate(activeGoals, krsByGoal).stream())
                .collect(Collectors.toList());
    }
}
