!/bin/bash

# Test basic inference
curl -X POST http://localhost:8080/api/v1/models/infer \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "test-001",
    "tenantId": "demo",
    "type": "chat",
    "prompt": "What is the capital of France?",
    "maxTokens": 100,
    "temperature": 0.7
  }' | jq