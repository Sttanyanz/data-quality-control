package com.example.data_quality_control.repository;

import com.example.data_quality_control.domain.summary.MetricCountsDto;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Set;

@Repository
public class MetricsRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public MetricsRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public MetricCountsDto getCounts(Long runId, Set<Long> goalIds) {
        if (goalIds == null || goalIds.isEmpty()) {
            return new MetricCountsDto(0, 0, 0, 0, 0);
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("runId", runId)
            .addValue("goalIds", goalIds);

        // Query 1: totalActiveGoals
        String sql1 = "SELECT COUNT(id) FROM goal WHERE id IN (:goalIds) AND status = 'ACTIVE'";
        Integer totalActiveGoals = namedParameterJdbcTemplate.queryForObject(sql1, params, Integer.class);
        totalActiveGoals = Objects.requireNonNullElse(totalActiveGoals, 0);

        // Query 2: totalActiveKrs
        String sql2 = """
            SELECT COUNT(kr.id) FROM key_result kr
            JOIN goal g ON kr.goal_id = g.id
            WHERE g.id IN (:goalIds) AND g.status = 'ACTIVE'
            """;
        Integer totalActiveKrs = namedParameterJdbcTemplate.queryForObject(sql2, params, Integer.class);
        totalActiveKrs = Objects.requireNonNullElse(totalActiveKrs, 0);

        // Query 3: krsWithIssues
        String sql3 = """
            SELECT COUNT(DISTINCT i.key_result_id) FROM kkd_issue i
            JOIN key_result kr ON i.key_result_id = kr.id
            JOIN goal g ON kr.goal_id = g.id
            WHERE i.run_id = :runId AND g.id IN (:goalIds) AND g.status = 'ACTIVE'
            """;
        Integer krsWithIssues = namedParameterJdbcTemplate.queryForObject(sql3, params, Integer.class);
        krsWithIssues = Objects.requireNonNullElse(krsWithIssues, 0);

        // Query 4: totalIssues
        String sql4 = """
            SELECT COUNT(i.id) FROM kkd_issue i
            LEFT JOIN goal g1 ON i.goal_id = g1.id
            LEFT JOIN key_result kr ON i.key_result_id = kr.id
            WHERE i.run_id = :runId AND COALESCE(g1.id, kr.goal_id) IN (:goalIds)
            """;
        Integer totalIssues = namedParameterJdbcTemplate.queryForObject(sql4, params, Integer.class);
        totalIssues = Objects.requireNonNullElse(totalIssues, 0);

        // Query 5: criticalGoals
        String sql5 = """
            SELECT COUNT(DISTINCT COALESCE(i.goal_id, kr.goal_id)) FROM kkd_issue i
            LEFT JOIN goal g1 ON i.goal_id = g1.id
            LEFT JOIN key_result kr ON i.key_result_id = kr.id
            WHERE i.run_id = :runId AND i.severity::text = 'critical'
            AND COALESCE(g1.id, kr.goal_id) IN (:goalIds)
            """;
        Integer criticalGoals = namedParameterJdbcTemplate.queryForObject(sql5, params, Integer.class);
        criticalGoals = Objects.requireNonNullElse(criticalGoals, 0);

        return new MetricCountsDto(totalActiveGoals, totalActiveKrs, krsWithIssues, totalIssues, criticalGoals);
    }
}
