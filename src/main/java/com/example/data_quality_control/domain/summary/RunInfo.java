package com.example.data_quality_control.domain.summary;

import java.time.LocalDate;

public record RunInfo(
    Long id,
    LocalDate snapshotDate
) {}
