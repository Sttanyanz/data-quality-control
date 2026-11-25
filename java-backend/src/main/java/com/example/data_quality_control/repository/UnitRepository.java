package com.example.data_quality_control.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class UnitRepository {

    private final JdbcTemplate jdbcTemplate;

    public UnitRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<Long, Long> getUnitParentMap() {
        String sql = "SELECT id, parent_id FROM unit";
        return jdbcTemplate.query(sql, rs -> {
            Map<Long, Long> map = new HashMap<>();
            while (rs.next()) {
                Long id = rs.getLong("id");
                Long parentId = rs.getObject("parent_id", Long.class);
                map.put(id, parentId);
            }
            return map;
        });
    }

    public String findUnitName(Long id) {
        String sql = "SELECT name FROM unit WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, String.class, id);
    }
}
