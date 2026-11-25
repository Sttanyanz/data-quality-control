package com.example.data_quality_control.domain.summary;

public record SeverityDto(
    String code,
    Integer current,
    Integer previous,
    Integer delta
) {}
