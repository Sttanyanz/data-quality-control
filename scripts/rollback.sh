#!/usr/bin/env bash

# Прерывать выполнение при любой ошибке
set -e

echo "Откат миграций (U3 -> U2 -> U1)..."

# Применяем скрипты отката в обратном порядке
docker compose exec -T postgres psql -U kkd -d kkd < src/main/resources/db/migration/U3__seed_data.sql
docker compose exec -T postgres psql -U kkd -d kkd < src/main/resources/db/migration/U2__seed_rules.sql
docker compose exec -T postgres psql -U kkd -d kkd < src/main/resources/db/migration/U1__drop_schema.sql

echo "Очистка истории Flyway..."
docker compose exec postgres psql -U kkd -d kkd -c "DELETE FROM flyway_schema_history;"

echo "Откат успешно завершен!"