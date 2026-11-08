package com.example.data_quality_control.domain;

public record KkdIssue(Long runId, Long ruleId, Long goalId, Long keyResultId, String severity, String message) {
}
