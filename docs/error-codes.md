# Wayang Error Codes

Generated from `ErrorCode` at build time.

## CORE

| Code | HTTP | Retryable | Message |
| --- | --- | --- | --- |
| CORE_001 | 400 | false | Invalid request |
| CORE_002 | 404 | false | Resource not found |
| CORE_003 | 400 | false | Unsupported operation |
| CORE_004 | 409 | false | Conflict detected |

## ORCH

| Code | HTTP | Retryable | Message |
| --- | --- | --- | --- |
| ORCH_001 | 404 | false | Workflow not found |
| ORCH_002 | 409 | false | Invalid orchestration state |
| ORCH_003 | 500 | true | Failed to schedule workflow |

## EXEC

| Code | HTTP | Retryable | Message |
| --- | --- | --- | --- |
| EXEC_001 | 404 | false | Task execution not found |
| EXEC_002 | 502 | true | Task dispatch failed |
| EXEC_003 | 504 | true | Task execution timed out |
| EXEC_004 | 500 | true | Task execution failed |

## PLUGIN

| Code | HTTP | Retryable | Message |
| --- | --- | --- | --- |
| PLUGIN_001 | 404 | false | Plugin not found |
| PLUGIN_002 | 500 | true | Plugin load failed |
| PLUGIN_003 | 500 | true | Plugin execution failed |
| PLUGIN_004 | 400 | false | Unsupported plugin |

## MCP

| Code | HTTP | Retryable | Message |
| --- | --- | --- | --- |
| MCP_001 | 404 | false | MCP tool not found |
| MCP_002 | 400 | false | Invalid MCP request |
| MCP_003 | 429 | true | MCP rate limit exceeded |

## TOOL

| Code | HTTP | Retryable | Message |
| --- | --- | --- | --- |
| TOOL_001 | 404 | false | Tool not found |
| TOOL_002 | 502 | true | Tool execution failed |

## AGENT

| Code | HTTP | Retryable | Message |
| --- | --- | --- | --- |
| AGENT_001 | 404 | false | Agent not found |
| AGENT_002 | 409 | false | Invalid agent state |
| AGENT_003 | 500 | true | Agent planning failed |

## INF

| Code | HTTP | Retryable | Message |
| --- | --- | --- | --- |
| INF_001 | 503 | true | Inference provider unavailable |
| INF_002 | 502 | true | Inference request failed |
| INF_003 | 502 | true | Inference response invalid |
| INF_004 | 429 | true | Inference rate limit exceeded |
| INF_005 | 401 | false | Inference authentication failed |

## MEM

| Code | HTTP | Retryable | Message |
| --- | --- | --- | --- |
| MEM_001 | 404 | false | Memory entry not found |
| MEM_002 | 500 | true | Memory store failed |

## VEC

| Code | HTTP | Retryable | Message |
| --- | --- | --- | --- |
| VEC_001 | 503 | true | Vector store unavailable |
| VEC_002 | 502 | true | Vector query failed |

## RAG

| Code | HTTP | Retryable | Message |
| --- | --- | --- | --- |
| RAG_001 | 404 | false | RAG index not found |
| RAG_002 | 502 | true | RAG retrieval failed |

## GRD

| Code | HTTP | Retryable | Message |
| --- | --- | --- | --- |
| GRD_001 | 403 | false | Guardrail policy violated |
| GRD_002 | 500 | false | Guardrail configuration invalid |

## HITL

| Code | HTTP | Retryable | Message |
| --- | --- | --- | --- |
| HITL_001 | 404 | false | HITL task not found |
| HITL_002 | 403 | false | HITL access denied |

## EIP

| Code | HTTP | Retryable | Message |
| --- | --- | --- | --- |
| EIP_001 | 404 | false | EIP route not found |
| EIP_002 | 502 | true | EIP route failed |

## INTEGRATION

| Code | HTTP | Retryable | Message |
| --- | --- | --- | --- |
| INTEGRATION_001 | 504 | true | Integration timed out |
| INTEGRATION_002 | 502 | true | Integration failed |

## SEC

| Code | HTTP | Retryable | Message |
| --- | --- | --- | --- |
| SEC_001 | 401 | false | Unauthorized |
| SEC_002 | 403 | false | Forbidden |
| SEC_003 | 404 | false | Secret not found |
| SEC_004 | 503 | true | Secret backend unavailable |
| SEC_005 | 500 | false | Secret encryption failed |
| SEC_006 | 500 | false | Secret decryption failed |
| SEC_007 | 400 | false | Secret path invalid |
| SEC_008 | 429 | true | Secret quota exceeded |
| SEC_009 | 404 | false | Secret version not found |
| SEC_010 | 410 | false | Secret expired |
| SEC_011 | 500 | true | Secret rotation failed |

## TENANT

| Code | HTTP | Retryable | Message |
| --- | --- | --- | --- |
| TENANT_001 | 404 | false | Tenant not found |
| TENANT_002 | 400 | false | Invalid tenant |

## CONFIG

| Code | HTTP | Retryable | Message |
| --- | --- | --- | --- |
| CONFIG_001 | 500 | false | Configuration missing |
| CONFIG_002 | 500 | false | Configuration invalid |

## VAL

| Code | HTTP | Retryable | Message |
| --- | --- | --- | --- |
| VAL_001 | 400 | false | Validation failed |
| VAL_002 | 400 | false | Missing required field |

## STORAGE

| Code | HTTP | Retryable | Message |
| --- | --- | --- | --- |
| STORAGE_001 | 500 | true | Storage read failed |
| STORAGE_002 | 500 | true | Storage write failed |
| STORAGE_003 | 409 | false | Storage conflict |

## NET

| Code | HTTP | Retryable | Message |
| --- | --- | --- | --- |
| NET_001 | 502 | true | Network error |
| NET_002 | 503 | true | Network unavailable |

## TIMEOUT

| Code | HTTP | Retryable | Message |
| --- | --- | --- | --- |
| TIMEOUT_001 | 504 | true | Operation timed out |

## RATE

| Code | HTTP | Retryable | Message |
| --- | --- | --- | --- |
| RATE_001 | 429 | true | Rate limit exceeded |

## CONC

| Code | HTTP | Retryable | Message |
| --- | --- | --- | --- |
| CONC_001 | 409 | true | Concurrency conflict |
| CONC_002 | 429 | true | Concurrency limit exceeded |

## RUNTIME

| Code | HTTP | Retryable | Message |
| --- | --- | --- | --- |
| RUNTIME_001 | 500 | true | Runtime error |

## INTERNAL

| Code | HTTP | Retryable | Message |
| --- | --- | --- | --- |
| INTERNAL_001 | 500 | true | Internal server error |

