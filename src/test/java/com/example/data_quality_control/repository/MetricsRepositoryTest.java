package com.example.data_quality_control.repository;

import com.example.data_quality_control.domain.summary.MetricCountsDto;
import com.example.data_quality_control.service.ScopeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MetricsRepositoryTest {

    @Autowired
    private MetricsRepository metricsRepository;

    @Autowired
    private ScopeService scopeService;

    @Test
    void shouldCalculateCorrectMetricsForFirstRun() {
        // Берем скоуп всего холдинга (unit_id = 1) для первого прогона
        Set<Long> scope = scopeService.getScopeGoalIds(1L);
        MetricCountsDto counts = metricsRepository.getCounts(1L, scope);

        // Проверяем цифры, которые мы заложили в V3__seed_data.sql
        assertThat(counts.totalActiveGoals()).isEqualTo(19);
        assertThat(counts.totalActiveKrs()).isEqualTo(36);
        assertThat(counts.totalIssues()).isEqualTo(20);
        assertThat(counts.criticalGoals()).isEqualTo(4);

        // У 14 КР есть проблемы (KR_NO_TARGET + KR_STALE_PROGRESS)
        assertThat(counts.krsWithIssues()).isEqualTo(14);
    }
}