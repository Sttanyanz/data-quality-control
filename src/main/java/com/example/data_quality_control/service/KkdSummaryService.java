package com.example.data_quality_control.service;

import com.example.data_quality_control.domain.KkdRun;
import com.example.data_quality_control.domain.summary.*;
import com.example.data_quality_control.repository.KkdRunRepository;
import com.example.data_quality_control.repository.MetricsRepository;
import com.example.data_quality_control.repository.SummaryDeltaRepository;
import com.example.data_quality_control.repository.UnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class KkdSummaryService {

    private final KkdRunRepository kkdRunRepository;
    private final UnitRepository unitRepository;
    private final ScopeService scopeService;
    private final MetricsRepository metricsRepository;
    private final SummaryDeltaRepository summaryDeltaRepository;

    public KkdSummaryService(
        KkdRunRepository kkdRunRepository,
        UnitRepository unitRepository,
        ScopeService scopeService,
        MetricsRepository metricsRepository,
        SummaryDeltaRepository summaryDeltaRepository
    ) {
        this.kkdRunRepository = kkdRunRepository;
        this.unitRepository = unitRepository;
        this.scopeService = scopeService;
        this.metricsRepository = metricsRepository;
        this.summaryDeltaRepository = summaryDeltaRepository;
    }

    public SummaryResponse getSummary(Long runId, Long unitId) {
        // Get current run
        KkdRun run = kkdRunRepository.findById(runId);
        if (run == null) {
            throw new RuntimeException("Run not found");
        }

        // Get unit name
        String unitName = unitRepository.findUnitName(unitId);

        // Get scope goal IDs
        Set<Long> goalIds = scopeService.getScopeGoalIds(unitId);

        // Get current metrics
        MetricCountsDto currentCounts = metricsRepository.getCounts(runId, goalIds);

        // Get previous metrics if comparedToRunId exists
        MetricCountsDto previousCounts = null;
        if (run.comparedToRunId() != null) {
            previousCounts = metricsRepository.getCounts(run.comparedToRunId(), goalIds);
        }

        // Calculate metrics
        List<MetricDto> metrics = new ArrayList<>();

        // Metric 1: clean_kr_share (higher is better)
        MetricDto cleanKrShare = calculateMetric(
            "clean_kr_share",
            "Доля КР без замечаний",
            "%",
            currentCounts.totalActiveKrs() > 0
                ? (double) (currentCounts.totalActiveKrs() - currentCounts.krsWithIssues()) / currentCounts.totalActiveKrs() * 100
                : null,
            previousCounts != null && previousCounts.totalActiveKrs() > 0
                ? (double) (previousCounts.totalActiveKrs() - previousCounts.krsWithIssues()) / previousCounts.totalActiveKrs() * 100
                : null,
            true
        );
        metrics.add(cleanKrShare);

        // Metric 2: avg_issues_per_goal (lower is better)
        MetricDto avgIssuesPerGoal = calculateMetric(
            "avg_issues_per_goal",
            "Среднее число проблем на цель",
            "шт",
            currentCounts.totalActiveGoals() > 0
                ? (double) currentCounts.totalIssues() / currentCounts.totalActiveGoals()
                : null,
            previousCounts != null && previousCounts.totalActiveGoals() > 0
                ? (double) previousCounts.totalIssues() / previousCounts.totalActiveGoals()
                : null,
            false
        );
        metrics.add(avgIssuesPerGoal);

        // Metric 3: critical_goals_share (lower is better)
        MetricDto criticalGoalsShare = calculateMetric(
            "critical_goals_share",
            "Доля целей с критическими проблемами",
            "%",
            currentCounts.totalActiveGoals() > 0
                ? (double) currentCounts.criticalGoals() / currentCounts.totalActiveGoals() * 100
                : null,
            previousCounts != null && previousCounts.totalActiveGoals() > 0
                ? (double) previousCounts.criticalGoals() / previousCounts.totalActiveGoals() * 100
                : null,
            false
        );
        metrics.add(criticalGoalsShare);

        // Get delta and severity stats
        DeltaDto delta = summaryDeltaRepository.getDelta(runId, run.comparedToRunId(), goalIds);
        List<SeverityDto> severityStats = summaryDeltaRepository.getSeverityStats(runId, run.comparedToRunId(), goalIds);

        // Get compared to run info
        RunInfo comparedTo = null;
        if (run.comparedToRunId() != null) {
            KkdRun comparedRun = kkdRunRepository.findById(run.comparedToRunId());
            if (comparedRun != null) {
                comparedTo = new RunInfo(comparedRun.id(), comparedRun.snapshotDate());
            }
        }

        // Build response
        RunInfo runInfo = new RunInfo(run.id(), run.snapshotDate());
        UnitInfo unitInfo = new UnitInfo(unitId, unitName);

        return new SummaryResponse(runInfo, comparedTo, unitInfo, metrics, severityStats, delta);
    }

    private MetricDto calculateMetric(String key, String title, String unit, Double value, Double previous, boolean higherIsBetter) {
        Double roundedValue = value != null ? Math.round(value * 10.0) / 10.0 : null;
        Double roundedPrevious = previous != null ? Math.round(previous * 10.0) / 10.0 : null;

        Double delta = null;
        String trend = null;

        if (roundedValue != null && roundedPrevious != null) {
            delta = Math.round((roundedValue - roundedPrevious) * 10.0) / 10.0;

            if (Math.abs(delta) < 0.01) {
                trend = "stable";
            } else if (higherIsBetter) {
                trend = delta > 0 ? "better" : "worse";
            } else {
                trend = delta > 0 ? "worse" : "better";
            }
        }

        return new MetricDto(key, title, unit, roundedValue, roundedPrevious, delta, trend);
    }
}
