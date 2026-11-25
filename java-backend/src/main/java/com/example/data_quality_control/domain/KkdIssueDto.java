package com.example.data_quality_control.domain;

public record KkdIssueDto(
    Long id,
    String ruleCode,
    String severity,
    String targetType,
    Long targetId,
    String message
) {}
