.PHONY: help build test run dev clean docker-build docker-up docker-down install-ollama-models

help:
	@echo "Wayang Models - Makefile Commands"
	@echo "=================================="
	@echo "  make build                 - Build all modules"
	@echo "  make test                  - Run all tests"
	@echo "  make run                   - Run in dev mode"
	@echo "  make dev                   - Run with live reload"
	@echo "  make clean                 - Clean build artifacts"
	@echo "  make docker-build          - Build Docker image"
	@echo "  make docker-up             - Start all services"
	@echo "  make docker-down           - Stop all services"
	@echo "  make install-ollama-models - Pull required Ollama models"
	@echo "  make logs                  - View service logs"
	@echo "  make db-migrate            - Run database migrations"
	@echo "  make api-test              - Run API tests"

build:
	@echo "Building Wayang Models..."
	mvn clean install -DskipTests

test:
	@echo "Running tests..."
	mvn clean test

run:
	@echo "Running Wayang Models in dev mode..."
	cd wayang-models-deployment && mvn quarkus:dev

dev: run

clean:
	@echo "Cleaning build artifacts..."
	mvn clean
	docker-compose -f wayang-models-deployment/docker-compose.yml down -v

docker-build:
	@echo "Building Docker image..."
	cd wayang-models-deployment && mvn clean package -DskipTests
	docker build -t kayys/wayang-models:latest -f wayang-models-deployment/src/main/docker/Dockerfile.jvm wayang-models-deployment

docker-up:
	@echo "Starting all services..."
	cd wayang-models-deployment && docker-compose up -d
	@echo "Waiting for services to be healthy..."
	@sleep 10
	@echo "Services started! Access:"
	@echo "  - API: http://localhost:8080"
	@echo "  - Swagger UI: http://localhost:8080/swagger-ui"
	@echo "  - Health: http://localhost:8080/health"
	@echo "  - Metrics: http://localhost:8080/metrics"

docker-down:
	@echo "Stopping all services..."
	cd wayang-models-deployment && docker-compose down

docker-logs:
	cd wayang-models-deployment && docker-compose logs -f wayang-models

install-ollama-models:
	@echo "Pulling Ollama models..."
	docker exec wayang-ollama ollama pull llama3:8b
	docker exec wayang-ollama ollama pull llama3:70b
	docker exec wayang-ollama ollama pull mistral:7b
	@echo "Models installed successfully!"

db-migrate:
	@echo "Running database migrations..."
	cd wayang-models-deployment && mvn flyway:migrate

api-test:
	@echo "Testing API endpoints..."
	@echo "\n=== Server Health ==="
	curl -s http://localhost:8080/health/ready | jq
	@echo "\n=== KServe Server Metadata ==="
	curl -s http://localhost:8080/v2 | jq
	@echo "\n=== List Models ==="
	curl -s http://localhost:8080/api/v1/models/registry | jq
	@echo "\nAPI tests completed!"

format:
	@echo "Formatting code..."
	mvn spotless:apply

check:
	@echo "Checking code style..."
	mvn spotless:check