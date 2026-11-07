package com.example.data_quality_control.domain.summary;

public record MetricDto(
    String key,
    String title,
    String unit,
    Double value,
    Double previous,
    Double delta,
    String trend
) {}
