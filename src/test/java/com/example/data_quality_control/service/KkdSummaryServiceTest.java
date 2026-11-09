package com.example.data_quality_control.service;

import com.example.data_quality_control.domain.KkdRun;
import com.example.data_quality_control.domain.summary.DeltaDto;
import com.example.data_quality_control.domain.summary.MetricCountsDto;
import com.example.data_quality_control.domain.summary.MetricDto;
import com.example.data_quality_control.domain.summary.SeverityDto;
import com.example.data_quality_control.domain.summary.SummaryResponse;
import com.example.data_quality_control.repository.KkdRunRepository;
import com.example.data_quality_control.repository.MetricsRepository;
import com.example.data_quality_control.repository.SummaryDeltaRepository;
import com.example.data_quality_control.repository.UnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KkdSummaryServiceTest {

    @Mock
    private KkdRunRepository kkdRunRepository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private ScopeService scopeService;

    @Mock
    private MetricsRepository metricsRepository;

    @Mock
    private SummaryDeltaRepository summaryDeltaRepository;

    @InjectMocks
    private KkdSummaryService kkdSummaryService;

    @BeforeEach
    void setUp() {
        when(summaryDeltaRepository.getDelta(anyLong(), any(), any())).thenReturn(new DeltaDto(0, 0, 0));
        when(summaryDeltaRepository.getSeverityStats(anyLong(), any(), any())).thenReturn(List.of());
    }

    @Test
    void shouldCalculateMetricsWithPreviousRunId() {
        Long runId = 1L;
        Long unitId = 1L;
        Long comparedToRunId = 2L;

        KkdRun run = new KkdRun(runId, "COMPLETED", LocalDate.of(2024, 1, 1), comparedToRunId, 0);
        KkdRun comparedRun = new KkdRun(comparedToRunId, "COMPLETED", LocalDate.of(2023, 12, 1), null, 0);

        when(kkdRunRepository.findById(runId)).thenReturn(run);
        when(kkdRunRepository.findById(comparedToRunId)).thenReturn(comparedRun);
        when(unitRepository.findUnitName(unitId)).thenReturn("Test Unit");
        when(scopeService.getScopeGoalIds(unitId)).thenReturn(Set.of(1L, 2L));

        MetricCountsDto currentCounts = new MetricCountsDto(10, 20, 5, 15, 3);
        MetricCountsDto previousCounts = new MetricCountsDto(10, 20, 8, 20, 5);

        when(metricsRepository.getCounts(runId, Set.of(1L, 2L))).thenReturn(currentCounts);
        when(metricsRepository.getCounts(comparedToRunId, Set.of(1L, 2L))).thenReturn(previousCounts);

        SummaryResponse response = kkdSummaryService.getSummary(runId, unitId);

        assertThat(response.metrics()).hasSize(3);

        MetricDto cleanKrShare = response.metrics().get(0);
        assertThat(cleanKrShare.key()).isEqualTo("clean_kr_share");
        assertThat(cleanKrShare.value()).isEqualTo(75.0);
        assertThat(cleanKrShare.previous()).isEqualTo(60.0);
        assertThat(cleanKrShare.delta()).isEqualTo(15.0);
        assertThat(cleanKrShare.trend()).isEqualTo("better");

        MetricDto avgIssuesPerGoal = response.metrics().get(1);
        assertThat(avgIssuesPerGoal.key()).isEqualTo("avg_issues_per_goal");
        assertThat(avgIssuesPerGoal.value()).isEqualTo(1.5);
        assertThat(avgIssuesPerGoal.previous()).isEqualTo(2.0);
        assertThat(avgIssuesPerGoal.delta()).isEqualTo(-0.5);
        assertThat(avgIssuesPerGoal.trend()).isEqualTo("better");

        MetricDto criticalGoalsShare = response.metrics().get(2);
        assertThat(criticalGoalsShare.key()).isEqualTo("critical_goals_share");
        assertThat(criticalGoalsShare.value()).isEqualTo(30.0);
        assertThat(criticalGoalsShare.previous()).isEqualTo(50.0);
        assertThat(criticalGoalsShare.delta()).isEqualTo(-20.0);
        assertThat(criticalGoalsShare.trend()).isEqualTo("better");
    }

    @Test
    void shouldReturnNullValueWhenTotalActiveGoalsIsZero() {
        Long runId = 1L;
        Long unitId = 1L;

        KkdRun run = new KkdRun(runId, "COMPLETED", LocalDate.of(2024, 1, 1), null, 0);

        when(kkdRunRepository.findById(runId)).thenReturn(run);
        when(unitRepository.findUnitName(unitId)).thenReturn("Test Unit");
        when(scopeService.getScopeGoalIds(unitId)).thenReturn(Set.of(1L, 2L));

        MetricCountsDto currentCounts = new MetricCountsDto(0, 20, 5, 15, 3);

        when(metricsRepository.getCounts(runId, Set.of(1L, 2L))).thenReturn(currentCounts);

        SummaryResponse response = kkdSummaryService.getSummary(runId, unitId);

        assertThat(response.metrics()).hasSize(3);

        MetricDto cleanKrShare = response.metrics().get(0);
        assertThat(cleanKrShare.value()).isEqualTo(75.0);

        MetricDto avgIssuesPerGoal = response.metrics().get(1);
        assertThat(avgIssuesPerGoal.value()).isNull();

        MetricDto criticalGoalsShare = response.metrics().get(2);
        assertThat(criticalGoalsShare.value()).isNull();
    }

    @Test
    void shouldReturnNullPreviousAndDeltaWhenComparedToRunIdIsNull() {
        Long runId = 1L;
        Long unitId = 1L;

        KkdRun run = new KkdRun(runId, "COMPLETED", LocalDate.of(2024, 1, 1), null, 0);

        when(kkdRunRepository.findById(runId)).thenReturn(run);
        when(unitRepository.findUnitName(unitId)).thenReturn("Test Unit");
        when(scopeService.getScopeGoalIds(unitId)).thenReturn(Set.of(1L, 2L));

        MetricCountsDto currentCounts = new MetricCountsDto(10, 20, 5, 15, 3);

        when(metricsRepository.getCounts(runId, Set.of(1L, 2L))).thenReturn(currentCounts);

        SummaryResponse response = kkdSummaryService.getSummary(runId, unitId);

        assertThat(response.metrics()).hasSize(3);

        for (MetricDto metric : response.metrics()) {
            assertThat(metric.previous()).isNull();
            assertThat(metric.delta()).isNull();
            assertThat(metric.trend()).isNull();
        }
    }
}
