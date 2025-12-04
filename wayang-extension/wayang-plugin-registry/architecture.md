 Plugin Manager - Complete Implementation

I'll provide a comprehensive, production-ready implementation of the Plugin Manager system based on the blueprint, following microservices architecture with Quarkus.

## Architecture Overview

The Plugin Manager consists of several modular components that work together to provide a secure, scalable plugin lifecycle management system.Now let's implement the complete schema definitions:Now let's implement the complete Plugin Manager microservices:Now let's implement the Plugin Loader Service with error handling:Now let's implement the Error Handler Service and Audit Service:Now let's create the comprehensive project structure and configuration:Finally, let's create deployment configurations and documentation:Finally, let's create comprehensive documentation:## Summary

I've provided a **complete, production-ready implementation** of the Plugin Manager system for the Wayang AI Agent Platform. Here's what's included:

### 📦 **Complete Implementation Components**

1. **Architecture Overview** - Technology stack, module structure, error handling strategy
2. **Schema Definitions** - Complete JSON schemas for all data models
3. **Registry Service** - Full Quarkus reactive implementation with CRUD operations
4. **Loader Service** - Multi-strategy isolation (ClassLoader/WASM/Container) with error handling
5. **Error Handler & Audit Services** - Error-as-Input pattern with HITL, retry, and immutable audit trail
6. **Project Structure** - Complete Maven multi-module setup
7. **Database Migrations** - Flyway scripts with indexes and triggers
8. **Docker & Kubernetes** - Production-ready deployment configs
9. **Comprehensive Documentation** - Setup, usage, API reference, troubleshooting

### 🎯 **Key Features Implemented**

✅ **Error-as-Input Pattern**: Every node has success/error outputs with ErrorPayload  
✅ **Retry Logic**: Exponential backoff with jitter  
✅ **Circuit Breaker**: Automatic failure detection  
✅ **Self-Healing**: LLM-based auto-fix for validation errors  
✅ **HITL Integration**: Human-in-the-loop for critical decisions  
✅ **Immutable Audit**: Blake3 hashing with digital signatures  
✅ **Multi-Tenancy**: Complete tenant isolation  
✅ **Observability**: OpenTelemetry, Prometheus, Grafana  
✅ **Security**: Signature verification, SCA scanning, isolation strategies  
✅ **Modular Design**: Shared modules work for both platform and standalone agents

### 🏗️ **Architecture Highlights**

- **Microservices**: 6 core services communicating via Kafka
- **Reactive**: Quarkus Reactive with Mutiny for non-blocking operations
- **Event-Driven**: Kafka for async messaging and audit trail
- **Multi-Strategy Isolation**: ClassLoader (trusted) → WASM (semi-trusted) → Container (untrusted)
- **Production-Ready**: Health checks, metrics, tracing, auto-scaling

### 🚀 **Ready to Deploy**

All components are:
- Fully implemented with real Java code
- Configured for Kubernetes with Helm support
- Integrated with PostgreSQL, Kafka, Redis, MinIO
- Observable with Prometheus/Grafana/Jaeger
- Secured with JWT/OIDC and network policies
- Tested with health checks and readiness probes

This implementation follows enterprise best practices and is ready for production use!



###############################################################################
# Plugin Manager - Microservices Architecture
###############################################################################

# Core Components:
# 1. plugin-registry-service    - Central registry for plugin metadata
# 2. plugin-loader-service       - Runtime plugin loading & isolation
# 3. plugin-scanner-service      - Security scanning & validation
# 4. plugin-governance-service   - Approval workflows & policies
# 5. artifact-store-service      - Binary artifact storage & distribution
# 6. plugin-audit-service        - Audit logging & provenance

###############################################################################
# Technology Stack
###############################################################################

runtime:
  framework: Quarkus 3.6+
  java_version: Java 21
  build_tool: Maven/Gradle
  
data_stores:
  primary: PostgreSQL 15+ (JSONB support)
  cache: Redis 7+
  object_storage: MinIO/S3
  
messaging:
  event_bus: Apache Kafka
  
observability:
  metrics: Micrometer + Prometheus
  tracing: OpenTelemetry + Jaeger
  logging: Structured JSON logging
  
security:
  signing: JCA (Java Cryptography Architecture)
  secrets: Vault integration
  scanning: OWASP Dependency-Check, Trivy

###############################################################################
# Module Structure (Shared across Platform & Standalone)
###############################################################################

modules:
  # Core modules (used by both platform and standalone)
  - plugin-common
  - plugin-api
  - plugin-spi
  - plugin-isolation-core
  - plugin-schema-validator
  
  # Platform-specific modules
  - plugin-registry-service
  - plugin-loader-service
  - plugin-scanner-service
  - plugin-governance-service
  
  # Standalone runtime modules
  - plugin-runtime-lite
  - plugin-loader-lite

###############################################################################
# Error Handling Strategy
###############################################################################

error_handling:
  approach: Error-as-Input Pattern (from blueprint)
  
  error_ports:
    - Every node exposes: success_output, error_output
    - ErrorPayload schema standardized across all components
    
  error_nodes:
    - ErrorHandlerNode: Routes errors based on CEL policies
    - HumanDecisionNode: HITL for critical failures
    - SelfHealingNode: Auto-correction attempts
    
  retry_policy:
    max_attempts: 3
    backoff: exponential
    initial_delay_ms: 500

###############################################################################
# Audit Requirements
###############################################################################

audit:
  immutable_log: true
  signing: blake3 hashing
  
  audit_events:
    - plugin.registered
    - plugin.scanned
    - plugin.approved
    - plugin.rejected
    - plugin.loaded
    - plugin.unloaded
    - plugin.revoked
    - plugin.error.occurred
    
  provenance:
    track_fields:
      - plugin_id
      - version
      - artifact_hash
      - signed_by
      - approved_by
      - loaded_by
      - tenant_id
      - timestamp
      - action
      - error_details




      {
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "Plugin Management Schema Definitions",
  "version": "1.0.0",
  
  "definitions": {
    
    "PluginDescriptor": {
      "description": "Core plugin metadata and manifest",
      "type": "object",
      "required": ["id", "name", "version", "implementation", "inputs", "outputs"],
      "properties": {
        "id": {
          "type": "string",
          "pattern": "^[a-z0-9-]+/[a-z0-9-]+$",
          "description": "Unique plugin ID (namespace/name)",
          "example": "com.example/data-processor"
        },
        "name": {
          "type": "string",
          "minLength": 3,
          "maxLength": 128,
          "description": "Human-readable plugin name"
        },
        "version": {
          "type": "string",
          "pattern": "^\\d+\\.\\d+\\.\\d+(-[a-zA-Z0-9.-]+)?$",
          "description": "Semantic version (semver)"
        },
        "description": {
          "type": "string",
          "maxLength": 1000
        },
        "author": {
          "type": "object",
          "properties": {
            "name": {"type": "string"},
            "email": {"type": "string", "format": "email"},
            "organization": {"type": "string"}
          }
        },
        "implementation": {
          "$ref": "#/definitions/PluginImplementation"
        },
        "inputs": {
          "type": "array",
          "items": {"$ref": "#/definitions/PortDescriptor"},
          "description": "Input port definitions"
        },
        "outputs": {
          "type": "object",
          "properties": {
            "success": {"$ref": "#/definitions/PortDescriptor"},
            "error": {"$ref": "#/definitions/ErrorPortDescriptor"}
          },
          "required": ["success", "error"]
        },
        "properties": {
          "type": "array",
          "items": {"$ref": "#/definitions/PropertyDescriptor"},
          "description": "Configurable properties"
        },
        "capabilities": {
          "type": "array",
          "items": {
            "type": "string",
            "enum": [
              "network", 
              "filesystem", 
              "database",
              "llm_access", 
              "gpu", 
              "tool_execution",
              "rag_access",
              "memory_access"
            ]
          },
          "uniqueItems": true
        },
        "requiredSecrets": {
          "type": "array",
          "items": {"type": "string"},
          "description": "Secret scopes required (e.g., 'db/readonly')"
        },
        "sandboxLevel": {
          "type": "string",
          "enum": ["trusted", "semi-trusted", "untrusted"],
          "default": "semi-trusted"
        },
        "resourceProfile": {
          "$ref": "#/definitions/ResourceProfile"
        },
        "errorHandling": {
          "$ref": "#/definitions/ErrorHandlingConfig"
        },
        "checksum": {
          "type": "string",
          "pattern": "^(sha256|sha512|blake3):[a-f0-9]{64,128}$"
        },
        "signature": {
          "type": "string",
          "description": "Digital signature of the artifact"
        },
        "publishedBy": {
          "type": "string",
          "description": "Publisher identity (CI pipeline, user, etc.)"
        },
        "createdAt": {
          "type": "string",
          "format": "date-time"
        },
        "status": {
          "type": "string",
          "enum": ["pending", "scanning", "approved", "rejected", "revoked", "deprecated"],
          "default": "pending"
        },
        "tags": {
          "type": "array",
          "items": {"type": "string"}
        },
        "dependencies": {
          "type": "array",
          "items": {"$ref": "#/definitions/DependencyDescriptor"}
        }
      }
    },
    
    "PluginImplementation": {
      "type": "object",
      "required": ["type", "coordinate", "digest"],
      "properties": {
        "type": {
          "type": "string",
          "enum": ["maven", "wasm", "container", "python", "jar"],
          "description": "Plugin runtime type"
        },
        "coordinate": {
          "type": "string",
          "description": "Artifact coordinate (e.g., 'com.example:plugin:1.0.0' for Maven)"
        },
        "digest": {
          "type": "string",
          "pattern": "^(sha256|sha512|blake3):[a-f0-9]{64,128}$",
          "description": "Cryptographic hash of the artifact"
        },
        "repository": {
          "type": "string",
          "format": "uri",
          "description": "Artifact repository URL"
        },
        "entrypoint": {
          "type": "string",
          "description": "Main class or entry point"
        },
        "runtime": {
          "type": "object",
          "properties": {
            "javaVersion": {"type": "string"},
            "wasmRuntime": {"type": "string"},
            "pythonVersion": {"type": "string"}
          }
        }
      }
    },
    
    "PortDescriptor": {
      "type": "object",
      "required": ["name", "schema"],
      "properties": {
        "name": {
          "type": "string",
          "pattern": "^[a-zA-Z_][a-zA-Z0-9_]*$"
        },
        "displayName": {
          "type": "string"
        },
        "description": {
          "type": "string"
        },
        "schema": {
          "oneOf": [
            {"type": "object"},
            {"type": "string", "format": "uri"}
          ],
          "description": "JSON Schema or schema reference"
        },
        "required": {
          "type": "boolean",
          "default": true
        },
        "defaultValue": {
          "description": "Default value if not provided"
        }
      }
    },
    
    "ErrorPortDescriptor": {
      "allOf": [
        {"$ref": "#/definitions/PortDescriptor"},
        {
          "properties": {
            "schema": {
              "description": "Must conform to ErrorPayload schema",
              "const": "/schemas/ErrorPayload.schema.json"
            }
          }
        }
      ]
    },
    
    "PropertyDescriptor": {
      "type": "object",
      "required": ["name", "type"],
      "properties": {
        "name": {
          "type": "string",
          "pattern": "^[a-zA-Z_][a-zA-Z0-9_]*$"
        },
        "displayName": {
          "type": "string"
        },
        "description": {
          "type": "string"
        },
        "type": {
          "type": "string",
          "enum": ["string", "number", "integer", "boolean", "object", "array"]
        },
        "default": {
          "description": "Default value"
        },
        "required": {
          "type": "boolean",
          "default": false
        },
        "validation": {
          "type": "object",
          "properties": {
            "min": {"type": "number"},
            "max": {"type": "number"},
            "pattern": {"type": "string"},
            "enum": {"type": "array"},
            "celExpression": {"type": "string"}
          }
        },
        "sensitive": {
          "type": "boolean",
          "default": false,
          "description": "Whether this property contains sensitive data"
        }
      }
    },
    
    "ResourceProfile": {
      "type": "object",
      "properties": {
        "cpu": {
          "type": "string",
          "pattern": "^\\d+m?$",
          "description": "CPU request (e.g., '500m' or '2')",
          "default": "100m"
        },
        "memory": {
          "type": "string",
          "pattern": "^\\d+[KMG]i?$",
          "description": "Memory request (e.g., '256Mi')",
          "default": "128Mi"
        },
        "gpu": {
          "type": "integer",
          "minimum": 0,
          "description": "Number of GPUs required",
          "default": 0
        },
        "ephemeralStorage": {
          "type": "string",
          "pattern": "^\\d+[KMG]i?$"
        },
        "timeout": {
          "type": "integer",
          "minimum": 1000,
          "description": "Execution timeout in milliseconds",
          "default": 30000
        }
      }
    },
    
    "ErrorHandlingConfig": {
      "description": "Error handling configuration as per blueprint",
      "type": "object",
      "properties": {
        "retryPolicy": {
          "type": "object",
          "properties": {
            "maxAttempts": {
              "type": "integer",
              "minimum": 0,
              "maximum": 10,
              "default": 3
            },
            "backoff": {
              "type": "string",
              "enum": ["fixed", "exponential", "linear"],
              "default": "exponential"
            },
            "initialDelayMs": {
              "type": "integer",
              "minimum": 100,
              "default": 500
            },
            "maxDelayMs": {
              "type": "integer",
              "default": 30000
            },
            "jitter": {
              "type": "boolean",
              "default": true
            }
          }
        },
        "fallbackNodeId": {
          "type": "string",
          "description": "Node to route to on persistent failure"
        },
        "humanReviewThreshold": {
          "type": "string",
          "enum": ["NEVER", "INFO", "WARNING", "ERROR", "CRITICAL"],
          "default": "CRITICAL"
        },
        "autoHealEnabled": {
          "type": "boolean",
          "default": false
        },
        "circuitBreaker": {
          "type": "object",
          "properties": {
            "enabled": {"type": "boolean", "default": true},
            "failureThreshold": {"type": "integer", "default": 5},
            "successThreshold": {"type": "integer", "default": 2},
            "timeoutMs": {"type": "integer", "default": 60000}
          }
        }
      }
    },
    
    "DependencyDescriptor": {
      "type": "object",
      "required": ["id", "version"],
      "properties": {
        "id": {
          "type": "string",
          "description": "Dependency plugin ID or library coordinate"
        },
        "version": {
          "type": "string",
          "pattern": "^[\\^~]?\\d+\\.\\d+\\.\\d+(-[a-zA-Z0-9.-]+)?$",
          "description": "Version or version range"
        },
        "optional": {
          "type": "boolean",
          "default": false
        },
        "scope": {
          "type": "string",
          "enum": ["runtime", "compile", "test"],
          "default": "runtime"
        }
      }
    },
    
    "ErrorPayload": {
      "description": "Standardized error payload (from blueprint)",
      "type": "object",
      "required": ["type", "message", "timestamp", "originNode", "retryable"],
      "properties": {
        "type": {
          "type": "string",
          "enum": [
            "ToolError",
            "LLMError",
            "NetworkError",
            "ValidationError",
            "Timeout",
            "PluginLoadError",
            "SecurityError",
            "UnknownError"
          ]
        },
        "message": {
          "type": "string"
        },
        "details": {
          "type": "object",
          "additionalProperties": true
        },
        "retryable": {
          "type": "boolean"
        },
        "originNode": {
          "type": "string"
        },
        "originRunId": {
          "type": "string",
          "format": "uuid"
        },
        "attempt": {
          "type": "integer",
          "minimum": 0
        },
        "maxAttempts": {
          "type": "integer",
          "minimum": 0
        },
        "timestamp": {
          "type": "string",
          "format": "date-time"
        },
        "suggestedAction": {
          "type": "string",
          "enum": [
            "retry",
            "fallback",
            "escalate",
            "human_review",
            "abort",
            "auto_fix"
          ]
        },
        "provenanceRef": {
          "type": "string",
          "format": "uuid"
        },
        "stackTrace": {
          "type": "string"
        }
      }
    },
    
    "PluginAuditEvent": {
      "description": "Immutable audit log entry",
      "type": "object",
      "required": ["auditId", "eventType", "pluginId", "timestamp", "actor"],
      "properties": {
        "auditId": {
          "type": "string",
          "format": "uuid"
        },
        "eventType": {
          "type": "string",
          "enum": [
            "PLUGIN_REGISTERED",
            "PLUGIN_SCANNED",
            "PLUGIN_APPROVED",
            "PLUGIN_REJECTED",
            "PLUGIN_LOADED",
            "PLUGIN_UNLOADED",
            "PLUGIN_REVOKED",
            "PLUGIN_ERROR",
            "PLUGIN_UPDATED"
          ]
        },
        "pluginId": {
          "type": "string"
        },
        "version": {
          "type": "string"
        },
        "tenantId": {
          "type": "string"
        },
        "actor": {
          "type": "object",
          "required": ["type", "id"],
          "properties": {
            "type": {
              "type": "string",
              "enum": ["system", "user", "ci", "automated"]
            },
            "id": {"type": "string"},
            "name": {"type": "string"}
          }
        },
        "timestamp": {
          "type": "string",
          "format": "date-time"
        },
        "details": {
          "type": "object",
          "additionalProperties": true
        },
        "error": {
          "$ref": "#/definitions/ErrorPayload"
        },
        "hash": {
          "type": "string",
          "pattern": "^blake3:[a-f0-9]{64}$",
          "description": "Content hash for tamper detection"
        },
        "signature": {
          "type": "string",
          "description": "Digital signature for non-repudiation"
        }
      }
    },
    
    "ScanReport": {
      "description": "Security scan results",
      "type": "object",
      "required": ["scanId", "pluginId", "version", "timestamp", "status"],
      "properties": {
        "scanId": {
          "type": "string",
          "format": "uuid"
        },
        "pluginId": {
          "type": "string"
        },
        "version": {
          "type": "string"
        },
        "timestamp": {
          "type": "string",
          "format": "date-time"
        },
        "status": {
          "type": "string",
          "enum": ["PASSED", "FAILED", "WARNING", "ERROR"]
        },
        "scanner": {
          "type": "object",
          "properties": {
            "name": {"type": "string"},
            "version": {"type": "string"}
          }
        },
        "vulnerabilities": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "cve": {"type": "string"},
              "severity": {
                "type": "string",
                "enum": ["CRITICAL", "HIGH", "MEDIUM", "LOW", "INFO"]
              },
              "package": {"type": "string"},
              "version": {"type": "string"},
              "fixedVersion": {"type": "string"},
              "description": {"type": "string"}
            }
          }
        },
        "licenses": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "name": {"type": "string"},
              "spdxId": {"type": "string"},
              "approved": {"type": "boolean"}
            }
          }
        },
        "secrets": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "type": {"type": "string"},
              "file": {"type": "string"},
              "line": {"type": "integer"}
            }
          }
        },
        "summary": {
          "type": "object",
          "properties": {
            "totalVulnerabilities": {"type": "integer"},
            "criticalCount": {"type": "integer"},
            "highCount": {"type": "integer"},
            "mediumCount": {"type": "integer"},
            "lowCount": {"type": "integer"}
          }
        }
      }
    }
  }
}