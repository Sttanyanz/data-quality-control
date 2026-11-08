package com.example.data_quality_control.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class KkdRuleRepository {

    private final JdbcTemplate jdbcTemplate;

    public KkdRuleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, String> getRuleSeverities() {
        String sql = "SELECT code, severity FROM kkd_rule";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        return rows.stream()
                .collect(Collectors.toMap(
                        row -> (String) row.get("code"),
                        row -> (String) row.get("severity")
                ));
    }

    public Map<String, Long> getRuleIds() {
        String sql = "SELECT code, id FROM kkd_rule";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        return rows.stream()
                .collect(Collectors.toMap(
                        row -> (String) row.get("code"),
                        row -> ((Number) row.get("id")).longValue()
                ));
    }
}
