# Quickstart Guide

Get the Wayang Workflow Engine up and running in minutes.

## Prerequisites
- **Java 21** or later
- **Docker** & **Docker Compose**
- **Maven** (optional, wrapper provided)

## Installation

1. **Clone the repository**:
   ```bash
   git clone <repo-url>
   cd wayang-workflow-engine
   ```

2. **Start Infrastructure**:
   The engine requires PostgreSQL and Redis. We provide a Docker Compose setup for this.
   ```bash
   docker-compose up -d
   ```
   *Note: This starts Postgres on port `5433` and Redis on `6380` to avoid conflicts.*

3. **Run the Application**:
   Use the provided script to start Quarkus in dev mode.
   ```bash
   ./run-dev.sh
   ```
   The application will start on `localhost:7001` (HTTP) and `localhost:9090` (gRPC).

## Verification

### Check Health
```bash
curl http://localhost:7001/q/health
```

### List Workflows (REST)
Use Basic Auth for quick access:
```bash
curl -u admin:admin http://localhost:7001/api/v1/workflows
```

### List gRPC Services
If you have `grpcurl` installed (using `-authority` or `-rpc-header` for auth if needed, but `grpcurl` usually doesn't show auth errors on `list` unless the server enforces it for reflection):
```bash
grpcurl -plaintext localhost:9090 list
```

To call a secured method:
```bash
# Using Basic Auth header for gRPC (Value is base64 of admin:admin)
grpcurl -plaintext -rpc-header "Authorization: Basic YWRtaW46YWRtaW4=" localhost:9090 tech.kayys.wayang.workflow.v1.WorkflowRunService/GetActiveCount
```

## Next Steps
- Read [USAGE.md](USAGE.md) for detailed configuration and testing.
- Read [API.md](API.md) for API reference.
