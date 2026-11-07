package com.example.data_quality_control.domain;

import java.time.LocalDate;

public record KkdRun(
    Long id,
    String status,
    LocalDate snapshotDate,
    Long comparedToRunId,
    Integer issuesCount
) {}
