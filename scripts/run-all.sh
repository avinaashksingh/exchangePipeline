#!/usr/bin/env bash
# Start infra, Spring Boot app, order simulator, and React UI.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if ! docker info >/dev/null 2>&1; then
  echo "ERROR: Docker is not running. Start Docker Desktop, then re-run this script."
  exit 1
fi

echo "==> Starting Docker infra (Kafka, Prometheus, Grafana)..."
docker compose up -d

echo "==> Waiting for Kafka on localhost:9092..."
for i in {1..30}; do
  if (echo >/dev/tcp/localhost/9092) 2>/dev/null; then
    break
  fi
  sleep 1
done

echo "==> Starting Spring Boot app..."
./mvnw -q spring-boot:run &
APP_PID=$!

cleanup() {
  echo ""
  echo "==> Shutting down..."
  kill "$APP_PID" 2>/dev/null || true
  kill "$SIM_PID" 2>/dev/null || true
  kill "$UI_PID" 2>/dev/null || true
}
trap cleanup EXIT INT TERM

echo "==> Waiting for app on localhost:8080..."
for i in {1..60}; do
  if curl -sf http://localhost:8080/actuator/health >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

echo "==> Starting order simulator..."
./mvnw -q exec:java -Dexec.mainClass=com.scetzhbook.exchangePipeline.simulator.OrderLoadSimulator &
SIM_PID=$!

echo "==> Starting React UI..."
(cd client && npm run dev) &
UI_PID=$!

echo ""
echo "Stack running:"
echo "  App:        http://localhost:8080"
echo "  UI:         http://localhost:5173"
echo "  Prometheus: http://localhost:9090"
echo "  Grafana:    http://localhost:3000"
echo ""
echo "Press Ctrl+C to stop app, simulator, and UI (Docker keeps running)."
wait
