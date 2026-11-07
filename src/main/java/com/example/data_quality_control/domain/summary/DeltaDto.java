package com.example.data_quality_control.domain.summary;

public record DeltaDto(
    Integer newIssues,
    Integer persisting,
    Integer resolved
) {}
