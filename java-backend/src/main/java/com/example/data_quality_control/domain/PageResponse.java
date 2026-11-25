package com.example.data_quality_control.domain;

import java.util.List;

public record PageResponse<T>(
    List<T> items,
    int limit,
    int offset,
    long total
) {}
