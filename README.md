# Модуль контроля качества данных (ККД)

Сервис автоматической проверки корректности целей и ключевых результатов (КР).
Находит ошибки в данных целеполагания, считает метрики качества и показывает
динамику между проверками.

## Что умеет

- Проверять цели и КР по четырём правилам: цель без ответственного, сумма весов
  КР ≠ 100%, КР без целевого значения, устаревший факт (> 45 дней).
- Считать три метрики качества с учётом иерархии подразделений и каскадирования
  целей.
- Показывать дельту между прогонами: новые, сохраняющиеся и исправленные
  проблемы.
- Отдавать данные через REST API с фильтрами и пагинацией.
- Запускать новый прогон по требованию.

## Стек

Java 21, Spring Boot 4 (Web, JDBC), PostgreSQL 15, Flyway, Docker Compose, JUnit
5, Testcontainers.

## Запуск

    cp .env.example .env
    docker compose up -d --build

Приложение: http://localhost:8080 (порт настраивается в `.env`).

## API

### GET /api/runs

Список прогонов с числом проблем.

    [
      {
        "id": 2,
        "snapshotDate": "2026-10-26",
        "comparedToRunId": 1,
        "status": "COMPLETED",
        "issuesCount": 10
      }
    ]

### POST /api/runs

Запускает новый прогон. Ответ: `201 Created`.

### GET /api/kkd/issues

Список проблем прогона.

| Параметр | Обяз. | Описание |
|---|---|---|
| `run_id` | да | ID прогона |
| `unit_id` | нет | Подразделение (с учётом дочерних и каскада) |
| `rule` | нет | Код правила |
| `severity` | нет | `critical` / `major` / `minor` |
| `limit` | нет | По умолчанию 20, максимум 100 |
| `offset` | нет | По умолчанию 0 |

    {
      "items": [
        {
          "id": 1,
          "ruleCode": "GOAL_NO_OWNER",
          "severity": "critical",
          "targetType": "GOAL",
          "targetId": 3,
          "message": "У цели отсутствует ответственный"
        }
      ],
      "limit": 5,
      "offset": 0,
      "total": 4
    }

### GET /api/kkd/summary

Метрики, разбивка по важности и дельта между прогонами. Параметры: `run_id`,
`unit_id`.

    {
      "run": { "id": 2, "snapshotDate": "2026-10-26" },
      "comparedTo": { "id": 1, "snapshotDate": "2026-10-19" },
      "unit": { "id": 1, "name": "HQ" },
      "metrics": [
        {
          "key": "clean_kr_share",
          "title": "Доля КР без замечаний",
          "unit": "%",
          "value": 80.6,
          "previous": 61.1,
          "delta": 19.5,
          "trend": "better"
        }
      ],
      "issuesBySeverity": [
        { "code": "critical", "current": 2, "previous": 4, "delta": -2 }
      ],
      "delta": { "newIssues": 4, "persisting": 6, "resolved": 14 }
    }

### Ошибки

    {
      "error": {
        "code": "NOT_FOUND",
        "message": "Run with id 999 not found"
      }
    }

Коды: `404` `NOT_FOUND`, `422` `VALIDATION_ERROR` / `INVALID_PARAMETER`, `500`
`INTERNAL_ERROR`.

## Метрики

| key | Формула | Trend |
|---|---|---|
| `clean_kr_share` | КР без проблем / все КР активных целей × 100 | больше — лучше |
| `avg_issues_per_goal` | Проблемы в скоупе / активные цели в скоупе | меньше — лучше |
| `critical_goals_share` | Цели с critical-проблемой / активные цели × 100 | меньше — лучше |

Скоуп — подразделение и потомки, плюс каскадированные цели. Первый прогон:
`previous`, `delta`, `trend` = `null`.

## Тесты

    cd java-backend
    ./mvnw test

Для интеграционных тестов должен быть запущен Docker — Testcontainers поднимает
PostgreSQL автоматически.

## Миграции

    ./scripts/migrate.sh
    ./scripts/rollback.sh

## Ограничения

Нет авторизации. CI не настроен.