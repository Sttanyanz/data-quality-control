package com.example.data_quality_control.repository;

import com.example.data_quality_control.domain.summary.DeltaDto;
import com.example.data_quality_control.domain.summary.SeverityDto;
import com.example.data_quality_control.service.ScopeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SummaryDeltaRepositoryTest {

    @Autowired
    private SummaryDeltaRepository deltaRepository;

    @Autowired
    private ScopeService scopeService;

    @Test
    void shouldCalculateDeltaBetweenTwoRuns() {
        // Берем скоуп всего холдинга (unit_id = 1)
        Set<Long> scope = scopeService.getScopeGoalIds(1L);

        // Сравниваем второй прогон (2) с первым (1)
        DeltaDto delta = deltaRepository.getDelta(2L, 1L, scope);

        // Проверяем цифры по нашей логике из V3
        assertThat(delta.newIssues()).isEqualTo(4);
        assertThat(delta.persisting()).isEqualTo(6);
        assertThat(delta.resolved()).isEqualTo(14);
    }

    @Test
    void shouldReturnNullDeltaForFirstRun() {
        Set<Long> scope = scopeService.getScopeGoalIds(1L);

        // Для первого прогона previousRunId = null
        DeltaDto delta = deltaRepository.getDelta(1L, null, scope);

        assertThat(delta.newIssues()).isNull();
        assertThat(delta.persisting()).isNull();
        assertThat(delta.resolved()).isNull();
    }

    @Test
    void shouldCalculateSeverityStats() {
        Set<Long> scope = scopeService.getScopeGoalIds(1L);

        // Сравниваем второй прогон (2) с первым (1)
        List<SeverityDto> stats = deltaRepository.getSeverityStats(2L, 1L, scope);

        assertThat(stats).hasSize(3);

        // Проверяем critical
        SeverityDto critical = stats.stream().filter(s -> s.code().equals("critical")).findFirst().orElseThrow();
        assertThat(critical.current()).isEqualTo(2);   // Во 2-м прогоне 2 critical
        assertThat(critical.previous()).isEqualTo(4);  // В 1-м прогоне было 4 critical
        assertThat(critical.delta()).isEqualTo(-2);

        // Проверяем major
        SeverityDto major = stats.stream().filter(s -> s.code().equals("major")).findFirst().orElseThrow();
        assertThat(major.current()).isEqualTo(2);
        assertThat(major.previous()).isEqualTo(6);     // 2 веса + 4 таргета
        assertThat(major.delta()).isEqualTo(-4);

        // Проверяем minor
        SeverityDto minor = stats.stream().filter(s -> s.code().equals("minor")).findFirst().orElseThrow();
        assertThat(minor.current()).isEqualTo(6);
        assertThat(minor.previous()).isEqualTo(10);
        assertThat(minor.delta()).isEqualTo(-4);
    }

    @Test
    void shouldHandleFirstRunForSeverityStats() {
        Set<Long> scope = scopeService.getScopeGoalIds(1L);

        // Первый прогон (previousRunId = null)
        List<SeverityDto> stats = deltaRepository.getSeverityStats(1L, null, scope);

        // Все previous должны быть 0, а delta равна current
        SeverityDto critical = stats.stream().filter(s -> s.code().equals("critical")).findFirst().orElseThrow();
        assertThat(critical.current()).isEqualTo(4);
        assertThat(critical.previous()).isEqualTo(0);
        assertThat(critical.delta()).isEqualTo(4);
    }
}