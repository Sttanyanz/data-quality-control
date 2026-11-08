package com.example.data_quality_control.repository;

import com.example.data_quality_control.domain.KkdIssue;
import com.example.data_quality_control.domain.KkdIssueDto;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class KkdIssueRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;

    public KkdIssueRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate, JdbcTemplate jdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<KkdIssueDto> kkdIssueDtoRowMapper = (rs, rowNum) -> {
        Long id = rs.getLong("id");
        String ruleCode = rs.getString("rule_code");
        String severity = rs.getString("severity");
        String targetType = rs.getString("target_type");
        Long targetId = rs.getObject("target_id", Long.class);
        String message = rs.getString("message");
        return new KkdIssueDto(id, ruleCode, severity, targetType, targetId, message);
    };

    public List<KkdIssueDto> findIssues(Long runId, Long unitId, String ruleCode, String severity, int limit, int offset) {
        String sql = """
            SELECT i.id, r.code AS rule_code, i.severity,
                   CASE WHEN i.goal_id IS NOT NULL THEN 'GOAL' ELSE 'KR' END AS target_type,
                   COALESCE(i.goal_id, i.key_result_id) AS target_id,
                   i.message
            FROM kkd_issue i
            JOIN kkd_rule r ON i.rule_id = r.id
            LEFT JOIN goal g ON i.goal_id = g.id
            LEFT JOIN key_result kr ON i.key_result_id = kr.id
            LEFT JOIN goal kr_goal ON kr.goal_id = kr_goal.id
            WHERE i.run_id = :runId
              AND (CAST(:unitId AS BIGINT) IS NULL OR COALESCE(g.unit_id, kr_goal.unit_id) = CAST(:unitId AS BIGINT))
              AND (CAST(:ruleCode AS VARCHAR) IS NULL OR r.code = CAST(:ruleCode AS VARCHAR))
              AND (CAST(:severity AS VARCHAR) IS NULL OR i.severity::text = CAST(:severity AS VARCHAR))
            ORDER BY i.id
            LIMIT :limit OFFSET :offset
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("runId", runId)
            .addValue("unitId", unitId)
            .addValue("ruleCode", ruleCode)
            .addValue("severity", severity)
            .addValue("limit", limit)
            .addValue("offset", offset);

        return namedParameterJdbcTemplate.query(sql, params, kkdIssueDtoRowMapper);
    }

    public long countIssues(Long runId, Long unitId, String ruleCode, String severity) {
        String sql = """
            SELECT COUNT(*)
            FROM kkd_issue i
            JOIN kkd_rule r ON i.rule_id = r.id
            LEFT JOIN goal g ON i.goal_id = g.id
            LEFT JOIN key_result kr ON i.key_result_id = kr.id
            LEFT JOIN goal kr_goal ON kr.goal_id = kr_goal.id
            WHERE i.run_id = :runId
              AND (CAST(:unitId AS BIGINT) IS NULL OR COALESCE(g.unit_id, kr_goal.unit_id) = CAST(:unitId AS BIGINT))
              AND (CAST(:ruleCode AS VARCHAR) IS NULL OR r.code = CAST(:ruleCode AS VARCHAR))
              AND (CAST(:severity AS VARCHAR) IS NULL OR i.severity::text = CAST(:severity AS VARCHAR))
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("runId", runId)
            .addValue("unitId", unitId)
            .addValue("ruleCode", ruleCode)
            .addValue("severity", severity);

        return namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
    }

    public void saveAll(Long runId, List<KkdIssue> issues) {
        String sql = """
                INSERT INTO kkd_issue (run_id, rule_id, goal_id, key_result_id, severity, message)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                KkdIssue issue = issues.get(i);
                ps.setLong(1, runId);
                ps.setLong(2, issue.ruleId());
                ps.setObject(3, issue.goalId());
                ps.setObject(4, issue.keyResultId());
                ps.setString(5, issue.severity());
                ps.setString(6, issue.message());
            }

            @Override
            public int getBatchSize() {
                return issues.size();
            }
        });
    }
}
