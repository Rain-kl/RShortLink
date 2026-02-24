#!/bin/sh

set -eu

wait_for_host() {
    name="$1"
    host="$2"
    port="$3"
    retries="${4:-60}"
    attempt=1

    until nc -z "$host" "$port" >/dev/null 2>&1; do
        if [ "$attempt" -ge "$retries" ]; then
            echo "Timed out waiting for $name at $host:$port" >&2
            exit 1
        fi

        echo "Waiting for $name at $host:$port... ($attempt/$retries)"
        attempt=$((attempt + 1))
        sleep 2
    done
}

wait_for_host "MySQL" "${MYSQL_HOST:-db}" "${MYSQL_PORT:-3306}"
wait_for_host "Redis" "${REDIS_HOST:-redis}" "${REDIS_PORT:-6379}"
wait_for_host "Nacos" "${NACOS_HOST:-nacos}" "${NACOS_PORT:-8848}"
