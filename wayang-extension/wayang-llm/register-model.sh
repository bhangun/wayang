#!/bin/bash

# Register a new model
curl -X POST http://localhost:8080/api/v1/models/registry \
  -H "Content-Type: application/json" \
  -d '{
    "modelId": "custom-model-1",
    "name": "My Custom Model",
    "version": "1.0.0",
    "provider": "ollama",
    "type": "llm",
    "capabilities": ["chat", "completion", "streaming"],
    "maxTokens": 4096,
    "maxOutputTokens": 2048,
    "latencyProfile": {
      "p50Ms": 300,
      "p95Ms": 1000,
      "avgMs": 500
    },
    "tags": ["custom", "test"],
    "status": "ACTIVE",
    "owner": "admin"
  }' | jq