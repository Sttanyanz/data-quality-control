package com.example.data_quality_control.domain.summary;

public record GoalData(
    Long id,
    Long unitId,
    Long parentGoalId,
    String status
) {}
