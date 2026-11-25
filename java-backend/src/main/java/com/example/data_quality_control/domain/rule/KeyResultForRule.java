package com.example.data_quality_control.domain.rule;

import java.math.BigDecimal;
import java.time.Instant;

public record KeyResultForRule(Long id, Long goalId, BigDecimal weight, BigDecimal targetValue, Instant factUpdatedAt) {
}
