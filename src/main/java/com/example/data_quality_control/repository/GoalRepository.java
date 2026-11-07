package com.example.data_quality_control.repository;

import com.example.data_quality_control.domain.summary.GoalData;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

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

    public List<GoalData> findAllGoals() {
        String sql = "SELECT id, unit_id, parent_goal_id, status FROM goal";
        return jdbcTemplate.query(sql, goalDataRowMapper);
    }
}
