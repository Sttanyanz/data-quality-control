package com.example.data_quality_control.repository;

import com.example.data_quality_control.domain.KkdRun;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;

@Repository
public class KkdRunRepository {

    private final JdbcTemplate jdbcTemplate;

    public KkdRunRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<KkdRun> kkdRunRowMapper = (rs, rowNum) -> {
        Long id = rs.getLong("id");
        LocalDate snapshotDate = rs.getDate("snapshot_date").toLocalDate();
        Long comparedToRunId = rs.getObject("compared_to_run_id", Long.class);
        String status = rs.getString("status");
        Integer issuesCount = rs.getInt("issues_count");
        return new KkdRun(id, status, snapshotDate, comparedToRunId, issuesCount);
    };

    public List<KkdRun> findAll() {
        String sql = """
                SELECT r.id, r.snapshot_date, r.compared_to_run_id, r.status, COUNT(i.id) AS issues_count
                FROM kkd_run r
                LEFT JOIN kkd_issue i ON r.id = i.run_id
                GROUP BY r.id, r.snapshot_date, r.compared_to_run_id, r.status
                ORDER BY r.snapshot_date DESC
                """;
        return jdbcTemplate.query(sql, kkdRunRowMapper);
    }

    public KkdRun findById(Long id) {
        String sql = """
                SELECT r.id, r.snapshot_date, r.compared_to_run_id, r.status, COUNT(i.id) AS issues_count
                FROM kkd_run r
                LEFT JOIN kkd_issue i ON r.id = i.run_id
                WHERE r.id = ?
                GROUP BY r.id, r.snapshot_date, r.compared_to_run_id, r.status
                """;
        return jdbcTemplate.query(sql, kkdRunRowMapper, id).stream().findFirst().orElse(null);
    }

    public Long createRun(Long comparedToRunId, LocalDate snapshotDate) {
        String sql = """
                INSERT INTO kkd_run (status, compared_to_run_id, snapshot_date)
                VALUES ('RUNNING', ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setObject(1, comparedToRunId);
            ps.setDate(2, java.sql.Date.valueOf(snapshotDate));
            return ps;
        }, keyHolder);
        return keyHolder.getKeyAs(Long.class);
    }

    public void updateStatus(Long runId, String status) {
        String sql = "UPDATE kkd_run SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, runId);
    }

    public Long findLatestCompletedRunId() {
        String sql = """
                SELECT id FROM kkd_run
                WHERE status = 'COMPLETED'
                ORDER BY snapshot_date DESC
                LIMIT 1
                """;
        List<Long> results = jdbcTemplate.queryForList(sql, Long.class);
        return results.isEmpty() ? null : results.getFirst();
    }
}
