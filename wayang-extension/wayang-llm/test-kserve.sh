#!/bin/bash

# Test KServe protocol
curl -X POST http://localhost:8080/v2/models/llama3-8b/infer \
  -H "Content-Type: application/json" \
  -d '{
    "id": "kserve-test-001",
    "inputs": [{
      "name": "prompt",
      "shape": [1],
      "datatype": "BYTES",
      "data": ["Explain quantum computing in simple terms."]
    }],
    "parameters": {
      "max_tokens": 200,
      "temperature": 0.7,
      "tenant_id": "demo"
    }
  }' | jq