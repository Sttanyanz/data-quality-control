DELETE
FROM kkd_rule
WHERE code IN (
               'GOAL_NO_OWNER',
               'KR_WEIGHTS_SUM',
               'KR_NO_TARGET',
               'KR_STALE_PROGRESS'
    );