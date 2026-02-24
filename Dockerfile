FROM node:20-bookworm-slim AS frontend-builder

WORKDIR /workspace/frontend

RUN apt-get update \
    && apt-get install -y --no-install-recommends python3 make g++ \
    && rm -rf /var/lib/apt/lists/*

COPY frontend/package.json frontend/pnpm-lock.yaml ./
RUN corepack enable && corepack prepare pnpm@9.15.9 --activate && pnpm install --frozen-lockfile

COPY frontend/ ./
RUN pnpm build

FROM maven:3.9.9-eclipse-temurin-17 AS backend-builder

WORKDIR /workspace

COPY . .
RUN mvn -pl gateway,aggregation -am -DskipTests --no-transfer-progress package

FROM eclipse-temurin:17-jre-jammy AS runtime

RUN apt-get update \
    && apt-get install -y --no-install-recommends nginx supervisor netcat-openbsd \
    && rm -rf /var/lib/apt/lists/* \
    && rm -f /etc/nginx/sites-enabled/default

WORKDIR /opt/rshortlink

COPY --from=backend-builder /workspace/gateway/target/rlink-gateway.jar /opt/rshortlink/gateway.jar
COPY --from=backend-builder /workspace/aggregation/target/rlink-aggregation.jar /opt/rshortlink/aggregation.jar
COPY --from=frontend-builder /workspace/frontend/dist /usr/share/nginx/html
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf
COPY docker/supervisord.conf /etc/supervisor/conf.d/supervisord.conf
COPY docker/wait-for-dependencies.sh /opt/rshortlink/wait-for-dependencies.sh
COPY docker/start-gateway.sh /opt/rshortlink/start-gateway.sh
COPY docker/start-aggregation.sh /opt/rshortlink/start-aggregation.sh

RUN chmod +x /opt/rshortlink/wait-for-dependencies.sh /opt/rshortlink/start-gateway.sh /opt/rshortlink/start-aggregation.sh \
    && mkdir -p /var/log/supervisor /var/cache/nginx /var/run

EXPOSE 80 8000 8003

CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]
