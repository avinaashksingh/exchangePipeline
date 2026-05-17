# exchangePipeline

## Local development (app on host, infra in Docker)

Start Kafka, Prometheus, and Grafana:

```bash
docker compose up -d
```

Run the Spring Boot app on the host (IDE debug or Maven):

```bash
./mvnw spring-boot:run
# or: ./scripts/run-app.sh
```

- App: `http://localhost:8080`
- Prometheus: `http://localhost:9090` (scrapes app at `host.docker.internal:8080`)
- Grafana: `http://localhost:3000` (login: `admin` / `admin`)

Run everything (infra must be up; starts app, simulator, and UI):

```bash
./scripts/run-all.sh
```

Or separately:

```bash
cd client && npm run dev
./scripts/run-simulator.sh
```

## Optional: run app in Docker

```bash
docker compose -f docker-compose.yml -f docker-compose.app.yml up -d --build
```

Uses profile `docker` (`application-docker.properties`) so Kafka connects to the `kafka` service.

## Architecture Diagram

![Architecture Diagram](./arch.png)
