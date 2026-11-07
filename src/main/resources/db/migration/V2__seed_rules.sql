INSERT INTO kkd_rule (code, target_type, severity, title)
VALUES ('GOAL_NO_OWNER', 'GOAL', 'critical', 'У цели нет ответственного'),
       ('KR_WEIGHTS_SUM', 'GOAL', 'major', 'Сумма весов КР цели не равна 100%'),
       ('KR_NO_TARGET', 'KR', 'major', 'У КР не заполнено целевое значение'),
       ('KR_STALE_PROGRESS', 'KR', 'minor',
        'Факт КР не обновлялся более 45 дней');