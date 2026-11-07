-- Очищаем таблицы и сбрасываем счетчики ID обратно в начальное состояние.
-- CASCADE автоматически удалит зависимые данные, не нарушая внешние ключи.
-- RESTART IDENTITY сбрасывает sequence (id) на 1.

TRUNCATE TABLE kkd_issue, kkd_run, key_result, goal, unit RESTART IDENTITY CASCADE;