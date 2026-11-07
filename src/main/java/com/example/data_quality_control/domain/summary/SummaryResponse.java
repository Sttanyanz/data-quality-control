package com.example.data_quality_control.domain.summary;

import java.util.List;

public record SummaryResponse(
    RunInfo run,
    RunInfo comparedTo,
    UnitInfo unit,
    List<MetricDto> metrics,
    List<SeverityDto> issuesBySeverity,
    DeltaDto delta
) {}
