package com.example.data_quality_control.domain.summary;

public record MetricCountsDto(
    int totalActiveGoals,
    int totalActiveKrs,
    int krsWithIssues,
    int totalIssues,
    int criticalGoals
) {}
