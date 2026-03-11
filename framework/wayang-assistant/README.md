# Wayang Internal Assistant

An AI-powered internal assistant for the Wayang low-code agentic AI platform. The assistant helps users understand Wayang capabilities, automatically generates projects based on intent, and provides troubleshooting guidance for errors.

## Features

### 1. **Ask Questions About Wayang**
Get answers about Wayang platform capabilities, features, architecture, and usage. The assistant searches the official documentation (wayang.github.io) to provide accurate, up-to-date information.

**Capabilities covered:**
- Agent execution with skill-based architecture
- RAG (Retrieval-Augmented Generation)
- Web search integration
- Tool execution and MCP
- Workflow orchestration (GAMELAN engine)
- Multi-agent collaboration
- Human-in-the-loop (HITL)
- Memory and conversation context
- Guardrails and safety filters
- Embedding generation

### 2. **Generate Wayang Projects**
Describe what you want to build in natural language, and the assistant will generate a complete Wayang project structure with:
- Appropriate workflows
- Configured nodes (agents, RAG, web-search, tools, etc.)
- Proper connections and data flow
- Best-practice configurations

**Example intents:**
- "Create a customer support bot with RAG"
- "Build a multi-agent system for code review"
- "Create an agent with web search capabilities"
- "Build a workflow with human-in-the-loop approval"

### 3. **Troubleshoot Errors**
Get step-by-step troubleshooting guidance for Wayang error messages. The assistant:
- Searches documentation for relevant information
- Identifies common error patterns
- Provides specific remediation steps
- Links to relevant documentation

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Wayang Assistant                        │
├─────────────────────────────────────────────────────────────┤
│  REST API (WayangAssistantApi)                             │
│  /api/v1/assistant/{ask|generate-project|troubleshoot}     │
├─────────────────────────────────────────────────────────────┤
│  Service Layer (WayangAssistantService)                    │
│  - Documentation Search                                     │
│  - Project Generation                                       │
│  - Error Troubleshooting                                    │
│  - Web Search Integration                                   │
├─────────────────────────────────────────────────────────────┤
│  Tool Layer (SPI)                                           │
│  - wayang-doc-search                                        │
│  - wayang-project-generator                                 │
│  - wayang-error-help                                        │
│  - web-search                                               │
├─────────────────────────────────────────────────────────────┤
│  External Resources                                         │
│  - wayang.github.io (documentation)                         │
│  - Web Search Providers (Google, Bing, DuckDuckGo)          │
│  - wayang-project-api                                       │
│  - wayang-schema-api                                        │
└─────────────────────────────────────────────────────────────┘
```

## REST API Endpoints

### Ask a Question
```http
POST /api/v1/assistant/ask
Content-Type: application/json

{
  "question": "How do I use RAG in Wayang?",
  "useWebSearch": false
}
```

### Generate a Project
```http
POST /api/v1/assistant/generate-project
Content-Type: application/json

{
  "intent": "Create a customer support bot with RAG and web search",
  "name": "Customer Support Bot",
  "description": "Automated customer support assistant"
}
```

### Troubleshoot an Error
```http
POST /api/v1/assistant/troubleshoot
Content-Type: application/json

{
  "errorMessage": "NullPointerException at WorkflowEngine.start()",
  "context": "Starting the application after adding a new agent node"
}
```

### Get Capabilities
```http
GET /api/v1/assistant/capabilities
```

## Usage Examples

### Example 1: Asking About Capabilities

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/assistant/ask \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What is the GAMELAN workflow engine?"
  }'
```

**Response:**
```json
{
  "question": "What is the GAMELAN workflow engine?",
  "documentationResults": [
    {
      "title": "Workflow Orchestration",
      "snippet": "GAMELAN is the workflow orchestration engine in Wayang...",
      "url": "workflow-orchestration",
      "score": 15
    }
  ],
  "suggestion": "Found 1 relevant documentation result..."
}
```

### Example 2: Generating a Project

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/assistant/generate-project \
  -H "Content-Type: application/json" \
  -d '{
    "intent": "Build a RAG-enabled document Q&A system"
  }'
```

**Response:**
```json
{
  "success": true,
  "project": {
    "name": "Rag Document Q&A",
    "description": "Auto-generated Wayang project: Build a RAG-enabled document Q&A system",
    "capabilities": ["rag", "agent"],
    "workflows": [
      {
        "name": "main-workflow",
        "nodes": [
          {"id": "rag-1", "type": "rag-task", "executor": "rag"},
          {"id": "agent-1", "type": "agent-task", "executor": "agent"}
        ],
        "connections": [{"from": "rag-1", "to": "agent-1"}]
      }
    ]
  },
  "summary": "Generated project: Rag Document Q&A\nCapabilities: rag, agent\nWorkflows: 1",
  "nextSteps": [...]
}
```

### Example 3: Troubleshooting an Error

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/assistant/troubleshoot \
  -H "Content-Type: application/json" \
  -d '{
    "errorMessage": "Connection timeout when connecting to PostgreSQL"
  }'
```

**Response:**
```json
{
  "errorMessage": "Connection timeout when connecting to PostgreSQL",
  "advice": "## Troubleshooting Steps:\n\n### Relevant Documentation:\n1. **Database Configuration**: Ensure PostgreSQL is running...\n\n### General Steps:\n1. Check your Wayang configuration files...\n2. Verify that all required dependencies...\n\n### Connection-Related Error:\n- Verify that dependent services (PostgreSQL, Kafka) are running\n- Check network connectivity and firewall rules...",
  "documentationResults": [...],
  "additionalHelp": [...]
}
```

## Skill Definition

The assistant is defined as a skill in `src/main/resources/skills/wayang-assistant.json`:

```json
{
  "id": "wayang-assistant",
  "name": "Wayang Internal Assistant",
  "description": "An internal chatbot that answers questions about the Wayang platform...",
  "tools": [
    "wayang-doc-search",
    "wayang-project-generator",
    "wayang-error-help",
    "web-search"
  ]
}
```

## Tools

### wayang-doc-search
Searches the official Wayang documentation for relevant information.

**Input Schema:**
```json
{
  "query": "string (required)",
  "maxResults": "integer (default: 10)"
}
```

### wayang-project-generator
Generates a Wayang project from high-level intent.

**Input Schema:**
```json
{
  "intent": "string (required)",
  "name": "string (optional)",
  "description": "string (optional)"
}
```

### wayang-error-help
Provides troubleshooting guidance for error messages.

**Input Schema:**
```json
{
  "error": "string (required)",
  "context": "string (optional)"
}
```

### web-search
Performs web searches for real-time information.

**Input Schema:**
```json
{
  "query": "string (required)",
  "maxResults": "integer (default: 5)"
}
```

## Configuration

Configure the assistant via `application.properties`:

```properties
# Documentation path
wayang.docs.path=website/wayang.github.io

# Maximum results
wayang.assistant.max-doc-results=10
wayang.assistant.max-web-results=5

# Inference provider
wayang.assistant.default-provider=tech.kayys/anthropic-provider
wayang.assistant.fallback-provider=tech.kayys/openai-provider
```

## Building

```bash
cd wayang/framework/wayang-assistant
mvn clean install
```

## Running

```bash
# Run in development mode
mvn quarkus:dev

# Run tests
mvn test
```

## Integration with Wayang Platform

The assistant integrates with:

1. **wayang-project-api**: For project generation and validation
2. **wayang-schema-api**: For schema validation
3. **websearch-api**: For web search capabilities
4. **wayang-rag-embedding**: For potential RAG-based documentation search
5. **wayang-tool-core**: For tool SPI implementation

## Future Enhancements

- [ ] Vector-based RAG search for documentation
- [ ] Interactive project customization wizard
- [ ] Error pattern recognition with ML
- [ ] Integration with Wayang UI for visual project editing
- [ ] Support for generating complete runnable projects
- [ ] Multi-turn conversation for complex project requirements
- [ ] Integration with GitHub for project templates
- [ ] Support for generating tests alongside projects

## Contributing

To contribute new capabilities:

1. Add new tool implementations implementing the `Tool` SPI
2. Update the skill definition JSON
3. Add corresponding API endpoints if needed
4. Update documentation

## License

Apache 2.0 - See LICENSE file for details.
