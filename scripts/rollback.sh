#!/usr/bin/env bash
set -e

# Загружаем переменные из .env.hide
if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs)
fi

echo "Откат миграций (U3 -> U2 -> U1)..."

# Подключаемся к базе внутри контейнера через psql
docker compose exec -T postgres psql -U "${DB_USERNAME:-kkd}" -d "${DB_NAME:-kkd}" < src/main/resources/db/migration/U3__seed_data.sql
docker compose exec -T postgres psql -U "${DB_USERNAME:-kkd}" -d "${DB_NAME:-kkd}" < src/main/resources/db/migration/U2__seed_rules.sql
docker compose exec -T postgres psql -U "${DB_USERNAME:-kkd}" -d "${DB_NAME:-kkd}" < src/main/resources/db/migration/U1__drop_schema.sql

echo "Очистка истории Flyway..."
docker compose exec postgres psql -U "${DB_USERNAME:-kkd}" -d "${DB_NAME:-kkd}" -c "DELETE FROM flyway_schema_history;"

echo "Откат успешно завершен!"