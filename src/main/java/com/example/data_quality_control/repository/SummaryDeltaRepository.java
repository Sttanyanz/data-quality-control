package com.example.data_quality_control.repository;

import com.example.data_quality_control.domain.summary.DeltaDto;
import com.example.data_quality_control.domain.summary.SeverityDto;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class SummaryDeltaRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public SummaryDeltaRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public DeltaDto getDelta(Long currentRunId, Long previousRunId, Set<Long> goalIds) {
        if (previousRunId == null) {
            return new DeltaDto(null, null, null);
        }

        if (goalIds == null || goalIds.isEmpty()) {
            return new DeltaDto(0, 0, 0);
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("currentRunId", currentRunId)
            .addValue("previousRunId", previousRunId)
            .addValue("goalIds", goalIds);

        String sql = """
            WITH current_issues AS (
                SELECT i.rule_id, i.goal_id, i.key_result_id
                FROM kkd_issue i
                LEFT JOIN goal g1 ON i.goal_id = g1.id
                LEFT JOIN key_result kr ON i.key_result_id = kr.id
                WHERE i.run_id = :currentRunId AND COALESCE(g1.id, kr.goal_id) IN (:goalIds)
            ),
            previous_issues AS (
                SELECT i.rule_id, i.goal_id, i.key_result_id
                FROM kkd_issue i
                LEFT JOIN goal g1 ON i.goal_id = g1.id
                LEFT JOIN key_result kr ON i.key_result_id = kr.id
                WHERE i.run_id = :previousRunId AND COALESCE(g1.id, kr.goal_id) IN (:goalIds)
            )
            SELECT
                COUNT(*) FILTER (WHERE p.rule_id IS NULL) AS new_issues,
                COUNT(*) FILTER (WHERE p.rule_id IS NOT NULL) AS persisting_issues,
                (SELECT COUNT(*) FROM previous_issues p2
                 LEFT JOIN current_issues c2 ON c2.rule_id = p2.rule_id
                   AND c2.goal_id IS NOT DISTINCT FROM p2.goal_id
                   AND c2.key_result_id IS NOT DISTINCT FROM p2.key_result_id
                 WHERE c2.rule_id IS NULL) AS resolved_issues
            FROM current_issues c
            LEFT JOIN previous_issues p ON p.rule_id = c.rule_id
              AND p.goal_id IS NOT DISTINCT FROM c.goal_id
              AND p.key_result_id IS NOT DISTINCT FROM c.key_result_id
            """;

        return namedParameterJdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> {
            Integer newIssues = rs.getInt("new_issues");
            Integer persisting = rs.getInt("persisting_issues");
            Integer resolved = rs.getInt("resolved_issues");
            return new DeltaDto(newIssues, persisting, resolved);
        });
    }

    public List<SeverityDto> getSeverityStats(Long currentRunId, Long previousRunId, Set<Long> goalIds) {
        if (goalIds == null || goalIds.isEmpty()) {
            return List.of();
        }

        Long effectivePreviousRunId = previousRunId != null ? previousRunId : -1L;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("currentRunId", currentRunId)
                .addValue("previousRunId", effectivePreviousRunId)
                .addValue("goalIds", goalIds);

        String sql = """
                SELECT
                    i.severity::text AS code,
                    COUNT(*) FILTER (WHERE i.run_id = :currentRunId) AS current_count,
                    COUNT(*) FILTER (WHERE i.run_id = :previousRunId) AS previous_count
                FROM kkd_issue i
                LEFT JOIN key_result kr ON i.key_result_id = kr.id
                WHERE i.run_id IN (:currentRunId, :previousRunId)
                  AND COALESCE(i.goal_id, kr.goal_id) IN (:goalIds)
                GROUP BY i.severity::text
                """;

        Map<String, SeverityDto> severityMap = new HashMap<>();
        for (String code : List.of("critical", "major", "minor")) {
            severityMap.put(code, previousRunId == null
                    ? new SeverityDto(code, 0, null, null)
                    : new SeverityDto(code, 0, 0, 0));
        }

        namedParameterJdbcTemplate.query(sql, params, (rs) -> {
            String code = rs.getString("code");
            Integer current = rs.getInt("current_count");
            if (previousRunId == null) {
                severityMap.put(code, new SeverityDto(code, current, null, null));
            } else {
                Integer previous = rs.getInt("previous_count");
                severityMap.put(code, new SeverityDto(code, current, previous, current - previous));
            }
        });

        return List.of(
                severityMap.get("critical"),
                severityMap.get("major"),
                severityMap.get("minor")
        );
    }
}
