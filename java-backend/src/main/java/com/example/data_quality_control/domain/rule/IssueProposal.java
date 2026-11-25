package com.example.data_quality_control.domain.rule;

public record IssueProposal(String ruleCode, Long goalId, Long keyResultId, String message) {
}
