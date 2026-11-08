package com.example.data_quality_control.rule;

import com.example.data_quality_control.domain.rule.GoalForRule;
import com.example.data_quality_control.domain.rule.IssueProposal;
import com.example.data_quality_control.domain.rule.KeyResultForRule;

import java.util.List;
import java.util.Map;

public interface KkdRule {
    String getCode();
    List<IssueProposal> evaluate(List<GoalForRule> goals, Map<Long, List<KeyResultForRule>> krsByGoal);
}
