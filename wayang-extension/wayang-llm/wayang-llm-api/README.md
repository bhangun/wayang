# Wayang Models - LLM Runtime Abstraction

Unified LLM runtime abstraction providing provider-agnostic interface for AI model inference with routing, caching, safety, and observability.

## Features

- **Unified API**: Single interface for all model providers
- **Multi-Provider**: Ollama, OpenAI, vLLM, Triton support
- **Smart Routing**: Policy-based model selection
- **Caching**: Response caching for cost optimization
- **Safety**: Built-in content filtering and PII detection
- **Observability**: Comprehensive metrics and tracing
- **Streaming**: Real-time streaming responses
- **Function Calling**: Tool/function execution support

## Quick Start

### Prerequisites

- Java 21+
- Docker & Docker Compose
- Maven 3.9+

### Running Locally

```bash
# Start infrastructure
cd wayang-models-deployment
docker-compose up -d postgres redis ollama

# Build
mvn clean install

# Run
mvn quarkus:dev
```

### API Access

- **Swagger UI**: http://localhost:8080/swagger-ui
- **Health**: http://localhost:8080/health
- **Metrics**: http://localhost:8080/metrics

## Usage

### Inference Request

```bash
curl -X POST http://localhost:8080/api/v1/models/infer \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "req-123",
    "tenantId": "tenant-1",
    "type": "chat",
    "prompt": "Hello!",
    "maxTokens": 100,
    "temperature": 0.7
  }'
```

### Register Model

```bash
curl -X POST http://localhost:8080/api/v1/models/registry \
  -H "Content-Type: application/json" \
  -d '{
    "modelId": "llama3-8b",
    "name": "Llama 3 8B",
    "version": "1.0",
    "provider": "ollama",
    "type": "llm",
    "capabilities": ["chat", "streaming"],
    "maxTokens": 8192
  }'
```

## Configuration

See `application.properties` for all configuration options.

Key environment variables:
- `DB_URL`: PostgreSQL connection string
- `REDIS_HOSTS`: Redis connection string
- `OLLAMA_URL`: Ollama API endpoint
- `OPENAI_API_KEY`: OpenAI API key

## Architecture

```
wayang-models/
├── wayang-models-api/          # API definitions
├── wayang-models-core/         # Core implementation
├── wayang-models-router/       # Routing logic
├── wayang-models-adapters/     # Provider adapters
│   ├── ollama/
│   ├── openai/
│   ├── vllm/
│   └── triton/
├── wayang-models-cache/        # Caching layer
├── wayang-models-safety/       # Safety filtering
├── wayang-models-metrics/      # Observability
└── wayang-models-deployment/   # REST API
```

## License

Copyright © 2025 kayys.tech