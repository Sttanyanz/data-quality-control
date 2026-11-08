package com.example.data_quality_control.repository;

import com.example.data_quality_control.domain.rule.GoalForRule;
import com.example.data_quality_control.domain.rule.KeyResultForRule;
import com.example.data_quality_control.domain.summary.GoalData;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public class GoalRepository {

    private final JdbcTemplate jdbcTemplate;

    public GoalRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<GoalData> goalDataRowMapper = (rs, rowNum) -> {
        Long id = rs.getLong("id");
        Long unitId = rs.getObject("unit_id", Long.class);
        Long parentGoalId = rs.getObject("parent_goal_id", Long.class);
        String status = rs.getString("status");
        return new GoalData(id, unitId, parentGoalId, status);
    };

    private final RowMapper<GoalForRule> goalForRuleRowMapper = (rs, rowNum) -> {
        Long id = rs.getLong("id");
        Long ownerId = rs.getObject("owner_id", Long.class);
        String status = rs.getString("status");
        return new GoalForRule(id, ownerId, status);
    };

    private final RowMapper<KeyResultForRule> keyResultForRuleRowMapper = (rs, rowNum) -> {
        Long id = rs.getLong("id");
        Long goalId = rs.getLong("goal_id");
        java.math.BigDecimal weight = rs.getObject("weight", java.math.BigDecimal.class);
        java.math.BigDecimal targetValue = rs.getObject("target_value", java.math.BigDecimal.class);
        java.sql.Timestamp ts = rs.getTimestamp("fact_updated_at");
        Instant factUpdatedAt = ts != null ? ts.toInstant() : null;
        return new KeyResultForRule(id, goalId, weight, targetValue, factUpdatedAt);
    };

    public List<GoalData> findAllGoals() {
        String sql = "SELECT id, unit_id, parent_goal_id, status FROM goal";
        return jdbcTemplate.query(sql, goalDataRowMapper);
    }

    public List<GoalForRule> findActiveGoalsForRule() {
        String sql = "SELECT id, owner_id, status FROM goal WHERE status = 'ACTIVE'";
        return jdbcTemplate.query(sql, goalForRuleRowMapper);
    }

    public List<KeyResultForRule> findAllKrsForActiveGoals() {
        String sql = """
                SELECT kr.id, kr.goal_id, kr.weight, kr.target_value, kr.fact_updated_at
                FROM key_result kr
                JOIN goal g ON kr.goal_id = g.id
                WHERE g.status = 'ACTIVE'
                """;
        return jdbcTemplate.query(sql, keyResultForRuleRowMapper);
    }
}
