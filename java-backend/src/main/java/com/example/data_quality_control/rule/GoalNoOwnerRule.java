package com.example.data_quality_control.rule;

import com.example.data_quality_control.domain.rule.GoalForRule;
import com.example.data_quality_control.domain.rule.IssueProposal;
import com.example.data_quality_control.domain.rule.KeyResultForRule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GoalNoOwnerRule implements KkdRule {

    @Override
    public String getCode() {
        return "GOAL_NO_OWNER";
    }

    @Override
    public List<IssueProposal> evaluate(List<GoalForRule> goals, Map<Long, List<KeyResultForRule>> krsByGoal) {
        return goals.stream()
                .filter(goal -> goal.ownerId() == null)
                .map(goal -> new IssueProposal(getCode(), goal.id(), null, "У цели отсутствует ответственный"))
                .collect(Collectors.toList());
    }
}
