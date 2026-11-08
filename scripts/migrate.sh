#!/usr/bin/env bash
set -e

# Загружаем переменные из .env
if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs)
else
    echo "Файл .env не найден. Будут использованы переменные окружения
    системы или значения по умолчанию."
fi

# Собираем JDBC URL из переменных
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5433}
DB_NAME=${DB_NAME:-kkd}
JDBC_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}"

echo "Запуск миграции Flyway для ${JDBC_URL}..."

# Передаем собранный URL и креды напрямую в Maven
./mvnw flyway:migrate \
  -Dflyway.url="${JDBC_URL}" \
  -Dflyway.user="${DB_USERNAME:-kkd}" \
  -Dflyway.password="${DB_PASSWORD:-kkd}" \
  "$@"