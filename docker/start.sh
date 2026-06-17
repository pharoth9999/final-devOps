#!/bin/bash
set -e

DB_HOST="${DB_HOST:-mysql}"
DB_PORT="${DB_PORT:-3306}"

echo "Starting SSH server..."
/usr/sbin/sshd

echo "Waiting for MySQL at ${DB_HOST}:${DB_PORT}..."
until bash -c "echo > /dev/tcp/${DB_HOST}/${DB_PORT}" 2>/dev/null; do
    sleep 2
done
echo "MySQL is up."

echo "Starting Spring Boot application on port 8081..."
java -jar /app.jar --server.port=8081 &

echo "Starting NGINX on port 8080..."
exec nginx -g 'daemon off;'
