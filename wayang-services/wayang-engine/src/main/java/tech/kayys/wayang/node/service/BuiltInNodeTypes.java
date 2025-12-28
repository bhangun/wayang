package tech.kayys.wayang.node.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.node.dto.CustomNodeTypeDescriptor;
import tech.kayys.wayang.node.dto.NodeConfigRequest;
import tech.kayys.wayang.node.dto.NodeSuggestionRequest;
import tech.kayys.wayang.node.dto.NodeTypeCatalogResponse;
import tech.kayys.wayang.node.dto.NodeTypeDescriptor;
import tech.kayys.wayang.node.model.ValidationResult;
import tech.kayys.wayang.node.dto.NodeTypeStats;

import jakarta.ws.rs.NotFoundException;

import java.util.*;

/**
 * Complete Built-in Node Types Catalog Implementation.
 * 
 * This provides the complete set of 50+ built-in nodes across 7 categories:
 * 1. Agent Nodes (10 nodes) - AI/LLM powered operations
 * 2. Integration Nodes (12 nodes) - System connectors
 * 3. Control Flow Nodes (8 nodes) - Workflow logic
 * 4. Data Nodes (10 nodes) - Data manipulation
 * 5. Human Nodes (5 nodes) - HITL operations
 * 6. Utility Nodes (8 nodes) - Common utilities
 * 7. Error Handling Nodes (5 nodes) - Error management
 * 
 * Each node includes:
 * - Complete input/output port schemas
 * - Configuration properties with validation
 * - Error handling specifications
 * - UI metadata for designer
 * - Documentation and examples
 * 
 * @since 1.0.0
 */
@ApplicationScoped
public class BuiltInNodeTypes implements NodeTypeService {

        @Override
        public Uni<NodeTypeCatalogResponse> getNodeTypeCatalog(String tenantId) {
                NodeTypeCatalogResponse catalog = new NodeTypeCatalogResponse();
                catalog.setVersion("1.0.0");
                catalog.setLastUpdated(java.time.Instant.now().toString());

                // Build all categories
                catalog.addCategory("Agent", "AI-powered reasoning and decision making",
                                buildAgentNodes());
                catalog.addCategory("Integration", "System connectors and data flow",
                                buildIntegrationNodes());
                catalog.addCategory("Control Flow", "Workflow logic and routing",
                                buildControlFlowNodes());
                catalog.addCategory("Data", "Data manipulation and validation",
                                buildDataNodes());
                catalog.addCategory("Human", "Human-in-the-loop operations",
                                buildHumanNodes());
                catalog.addCategory("Utility", "Common utility operations",
                                buildUtilityNodes());
                catalog.addCategory("Error", "Error management and recovery",
                                buildErrorNodes());

                catalog.setTotalNodes(catalog.calculateTotalNodes());

                return Uni.createFrom().item(catalog);
        }

        // ========================================================================
        // AGENT NODES (10 nodes)
        // ========================================================================

        private List<NodeTypeDescriptor> buildAgentNodes() {
                List<NodeTypeDescriptor> nodes = new ArrayList<>();

                // 1. LLM Completion
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.agent.llm-completion")
                                .name("LLM Completion")
                                .description("Generate text completion using language model")
                                .category("Agent")
                                .icon("brain")
                                .color("#8B5CF6")
                                .addInput("prompt", "string", true, "The prompt to send to the LLM")
                                .addInput("systemPrompt", "string", false, "System prompt to set context")
                                .addInput("context", "object", false, "Additional context data")
                                .addOutput("success", "completion", "string", "Generated text completion")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("model", "string", "claude-sonnet-4-5", true,
                                                "LLM model to use")
                                .addProperty("temperature", "number", 0.7, false,
                                                "Sampling temperature (0-1)")
                                .addProperty("maxTokens", "integer", 1024, false,
                                                "Maximum tokens to generate")
                                .addProperty("topP", "number", 1.0, false, "Top-p sampling")
                                .addProperty("stopSequences", "array", List.of(), false, "Stop sequences")
                                .errorHandling(3, "exponential", true)
                                .documentation("Generate text completions using Claude or other LLMs. " +
                                                "Supports system prompts, context injection, and streaming.")
                                .build());

                // 2. Agent Executor
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.agent.executor")
                                .name("Agent Executor")
                                .description("Execute autonomous agent with tool access")
                                .category("Agent")
                                .icon("robot")
                                .color("#8B5CF6")
                                .addInput("goal", "string", true, "Agent's objective")
                                .addInput("context", "object", false, "Additional context")
                                .addOutput("success", "result", "object", "Agent execution result")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("agentId", "string", null, true, "Agent definition ID")
                                .addProperty("maxIterations", "integer", 10, false,
                                                "Maximum reasoning iterations")
                                .addProperty("enableRAG", "boolean", false, false,
                                                "Enable RAG for context retrieval")
                                .addProperty("tools", "array", List.of(), false,
                                                "List of tool IDs to enable")
                                .errorHandling(2, "exponential", true)
                                .build());

                // 3. RAG Query
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.agent.rag-query")
                                .name("RAG Query")
                                .description("Retrieval-augmented generation query")
                                .category("Agent")
                                .icon("database-search")
                                .color("#8B5CF6")
                                .addInput("query", "string", true, "Search query")
                                .addInput("filters", "object", false, "Metadata filters")
                                .addOutput("success", "results", "array", "Retrieved documents")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("vectorStore", "string", "default", true, "Vector store name")
                                .addProperty("topK", "integer", 5, false, "Number of results")
                                .addProperty("threshold", "number", 0.7, false, "Similarity threshold")
                                .addProperty("rerank", "boolean", false, false, "Enable reranking")
                                .build());

                // 4. Agent Orchestrator
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.agent.orchestrator")
                                .name("Agent Orchestrator")
                                .description("Orchestrate multiple agents dynamically")
                                .category("Agent")
                                .icon("network")
                                .color("#8B5CF6")
                                .addInput("task", "string", true, "High-level task description")
                                .addInput("agents", "array", false, "Available agents")
                                .addOutput("success", "plan", "object", "Execution plan")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("strategy", "string", "dynamic", false,
                                                "Orchestration strategy")
                                .addProperty("maxConcurrency", "integer", 4, false,
                                                "Max parallel agents")
                                .build());

                // 5. Embedding Generator
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.agent.embedding")
                                .name("Generate Embedding")
                                .description("Generate vector embeddings for text")
                                .category("Agent")
                                .icon("vector")
                                .color("#8B5CF6")
                                .addInput("text", "string", true, "Text to embed")
                                .addOutput("success", "embedding", "vector", "Generated embedding")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("model", "string", "text-embedding-3", true,
                                                "Embedding model")
                                .addProperty("dimensions", "integer", 1536, false, "Vector dimensions")
                                .build());

                // 6. Semantic Search
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.agent.semantic-search")
                                .name("Semantic Search")
                                .description("Search using semantic similarity")
                                .category("Agent")
                                .icon("search")
                                .color("#8B5CF6")
                                .addInput("query", "string", true, "Search query")
                                .addInput("documents", "array", true, "Documents to search")
                                .addOutput("success", "matches", "array", "Matching documents")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("threshold", "number", 0.8, false, "Match threshold")
                                .addProperty("topK", "integer", 10, false, "Results limit")
                                .build());

                // 7. Classifier
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.agent.classifier")
                                .name("Text Classifier")
                                .description("Classify text into categories")
                                .category("Agent")
                                .icon("tag")
                                .color("#8B5CF6")
                                .addInput("text", "string", true, "Text to classify")
                                .addInput("categories", "array", true, "Possible categories")
                                .addOutput("success", "classification", "object", "Classification result")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("model", "string", "claude-sonnet-4-5", false, "Model to use")
                                .addProperty("includeConfidence", "boolean", true, false,
                                                "Include confidence scores")
                                .build());

                // 8. Sentiment Analysis
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.agent.sentiment")
                                .name("Sentiment Analysis")
                                .description("Analyze sentiment of text")
                                .category("Agent")
                                .icon("smile")
                                .color("#8B5CF6")
                                .addInput("text", "string", true, "Text to analyze")
                                .addOutput("success", "sentiment", "object", "Sentiment analysis result")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("includeEmotions", "boolean", false, false,
                                                "Include emotion breakdown")
                                .build());

                // 9. Entity Extraction
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.agent.entity-extraction")
                                .name("Entity Extraction")
                                .description("Extract named entities from text")
                                .category("Agent")
                                .icon("bookmark")
                                .color("#8B5CF6")
                                .addInput("text", "string", true, "Text to analyze")
                                .addOutput("success", "entities", "array", "Extracted entities")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("entityTypes", "array",
                                                List.of("PERSON", "ORG", "LOCATION", "DATE"),
                                                false, "Entity types to extract")
                                .build());

                // 10. Summarization
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.agent.summarize")
                                .name("Summarize Text")
                                .description("Generate summary of long text")
                                .category("Agent")
                                .icon("file-text")
                                .color("#8B5CF6")
                                .addInput("text", "string", true, "Text to summarize")
                                .addOutput("success", "summary", "string", "Generated summary")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("maxLength", "integer", 200, false, "Max summary length")
                                .addProperty("style", "string", "concise", false,
                                                "Summary style (concise/detailed/bullet)")
                                .build());

                return nodes;
        }

        // ========================================================================
        // INTEGRATION NODES (12 nodes)
        // ========================================================================

        private List<NodeTypeDescriptor> buildIntegrationNodes() {
                List<NodeTypeDescriptor> nodes = new ArrayList<>();

                // 1. HTTP Request
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.integration.http-request")
                                .name("HTTP Request")
                                .description("Make HTTP/REST API calls")
                                .category("Integration")
                                .icon("globe")
                                .color("#10B981")
                                .addInput("url", "string", true, "Target URL")
                                .addInput("body", "object", false, "Request body")
                                .addInput("headers", "object", false, "HTTP headers")
                                .addOutput("success", "response", "object", "HTTP response")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("method", "string", "GET", true,
                                                "HTTP method (GET/POST/PUT/DELETE/PATCH)")
                                .addProperty("timeout", "integer", 30000, false, "Request timeout (ms)")
                                .addProperty("retryOn5xx", "boolean", true, false, "Retry on 5xx errors")
                                .addProperty("followRedirects", "boolean", true, false, "Follow redirects")
                                .errorHandling(3, "exponential", true)
                                .build());

                // 2. GraphQL Query
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.integration.graphql")
                                .name("GraphQL Query")
                                .description("Execute GraphQL queries and mutations")
                                .category("Integration")
                                .icon("graph")
                                .color("#10B981")
                                .addInput("query", "string", true, "GraphQL query/mutation")
                                .addInput("variables", "object", false, "Query variables")
                                .addOutput("success", "data", "object", "Query result")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("endpoint", "string", null, true, "GraphQL endpoint URL")
                                .addProperty("headers", "object", Map.of(), false, "HTTP headers")
                                .build());

                // 3. Database Query
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.integration.database")
                                .name("Database Query")
                                .description("Execute SQL queries")
                                .category("Integration")
                                .icon("database")
                                .color("#10B981")
                                .addInput("query", "string", true, "SQL query")
                                .addInput("parameters", "object", false, "Query parameters")
                                .addOutput("success", "results", "array", "Query results")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("datasource", "string", "default", true, "Datasource name")
                                .addProperty("transactional", "boolean", false, false,
                                                "Run in transaction")
                                .addProperty("fetchSize", "integer", 100, false, "Result fetch size")
                                .build());

                // 4. Message Queue Send
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.integration.mq-send")
                                .name("Send Message")
                                .description("Send message to queue/topic")
                                .category("Integration")
                                .icon("mail")
                                .color("#10B981")
                                .addInput("message", "object", true, "Message payload")
                                .addInput("headers", "object", false, "Message headers")
                                .addOutput("success", "messageId", "string", "Sent message ID")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("destination", "string", null, true, "Queue/topic name")
                                .addProperty("destinationType", "string", "queue", true,
                                                "Type (queue/topic)")
                                .addProperty("persistent", "boolean", true, false, "Persistent delivery")
                                .build());

                // 5. Message Queue Receive
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.integration.mq-receive")
                                .name("Receive Message")
                                .description("Receive message from queue/topic")
                                .category("Integration")
                                .icon("mail-open")
                                .color("#10B981")
                                .addInput("trigger", "object", false, "Trigger signal")
                                .addOutput("success", "message", "object", "Received message")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("source", "string", null, true, "Queue/topic name")
                                .addProperty("timeout", "integer", 5000, false, "Receive timeout (ms)")
                                .addProperty("autoAck", "boolean", true, false, "Auto acknowledge")
                                .build());

                // 6. File Read
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.integration.file-read")
                                .name("Read File")
                                .description("Read file from storage")
                                .category("Integration")
                                .icon("file")
                                .color("#10B981")
                                .addInput("path", "string", true, "File path")
                                .addOutput("success", "content", "string", "File content")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("encoding", "string", "UTF-8", false, "File encoding")
                                .addProperty("storageType", "string", "local", true,
                                                "Storage type (local/s3/gcs)")
                                .build());

                // 7. File Write
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.integration.file-write")
                                .name("Write File")
                                .description("Write data to file")
                                .category("Integration")
                                .icon("file-plus")
                                .color("#10B981")
                                .addInput("path", "string", true, "File path")
                                .addInput("content", "string", true, "Content to write")
                                .addOutput("success", "path", "string", "Written file path")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("encoding", "string", "UTF-8", false, "File encoding")
                                .addProperty("storageType", "string", "local", true, "Storage type")
                                .addProperty("overwrite", "boolean", false, false, "Overwrite existing")
                                .build());

                // 8. Email Send
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.integration.email-send")
                                .name("Send Email")
                                .description("Send email notification")
                                .category("Integration")
                                .icon("mail-send")
                                .color("#10B981")
                                .addInput("to", "string", true, "Recipient email")
                                .addInput("subject", "string", true, "Email subject")
                                .addInput("body", "string", true, "Email body (HTML supported)")
                                .addOutput("success", "messageId", "string", "Sent email ID")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("from", "string", "noreply@wayang.tech", false,
                                                "Sender email")
                                .addProperty("cc", "string", null, false, "CC recipients")
                                .addProperty("bcc", "string", null, false, "BCC recipients")
                                .addProperty("attachments", "array", List.of(), false, "File attachments")
                                .build());

                // 9. Webhook Trigger
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.integration.webhook")
                                .name("Webhook")
                                .description("Receive webhook POST requests")
                                .category("Integration")
                                .icon("webhook")
                                .color("#10B981")
                                .addInput("trigger", "object", false, "Trigger signal")
                                .addOutput("success", "payload", "object", "Webhook payload")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("path", "string", null, true, "Webhook URL path")
                                .addProperty("secret", "string", null, false, "Webhook secret")
                                .addProperty("validateSignature", "boolean", true, false,
                                                "Validate HMAC signature")
                                .build());

                // 10. SOAP Call
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.integration.soap")
                                .name("SOAP Call")
                                .description("Call SOAP web services")
                                .category("Integration")
                                .icon("server")
                                .color("#10B981")
                                .addInput("payload", "string", true, "SOAP XML payload")
                                .addOutput("success", "response", "string", "SOAP response")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("endpoint", "string", null, true, "SOAP endpoint URL")
                                .addProperty("soapAction", "string", null, false, "SOAPAction header")
                                .build());

                // 11. FTP Transfer
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.integration.ftp")
                                .name("FTP Transfer")
                                .description("Transfer files via FTP/SFTP")
                                .category("Integration")
                                .icon("upload")
                                .color("#10B981")
                                .addInput("localPath", "string", true, "Local file path")
                                .addInput("remotePath", "string", true, "Remote file path")
                                .addOutput("success", "result", "object", "Transfer result")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("host", "string", null, true, "FTP host")
                                .addProperty("port", "integer", 21, false, "FTP port")
                                .addProperty("secure", "boolean", false, false, "Use SFTP")
                                .addProperty("operation", "string", "upload", true,
                                                "Operation (upload/download)")
                                .build());

                // 12. Cloud Storage
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.integration.cloud-storage")
                                .name("Cloud Storage")
                                .description("Interact with cloud storage (S3, GCS, Azure)")
                                .category("Integration")
                                .icon("cloud")
                                .color("#10B981")
                                .addInput("operation", "string", true, "Operation type")
                                .addInput("path", "string", true, "Object path/key")
                                .addInput("data", "string", false, "Data for write operations")
                                .addOutput("success", "result", "object", "Operation result")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("provider", "string", "s3", true,
                                                "Provider (s3/gcs/azure)")
                                .addProperty("bucket", "string", null, true, "Bucket/container name")
                                .build());

                return nodes;
        }

        // ========================================================================
        // CONTROL FLOW NODES (8 nodes)
        // ========================================================================

        private List<NodeTypeDescriptor> buildControlFlowNodes() {
                List<NodeTypeDescriptor> nodes = new ArrayList<>();

                // 1. Conditional
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.control.conditional")
                                .name("Conditional")
                                .description("Route based on condition")
                                .category("Control Flow")
                                .icon("git-branch")
                                .color("#F59E0B")
                                .addInput("input", "object", true, "Data to evaluate")
                                .addOutput("true", "true", "object", "Condition is true")
                                .addOutput("false", "false", "object", "Condition is false")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("condition", "string", null, true,
                                                "Boolean expression (CEL)")
                                .documentation("Evaluate condition and route to true or false branch. " +
                                                "Supports CEL expressions for complex conditions.")
                                .build());

                // 2. Switch
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.control.switch")
                                .name("Switch")
                                .description("Multi-way conditional routing")
                                .category("Control Flow")
                                .icon("split")
                                .color("#F59E0B")
                                .addInput("input", "object", true, "Data to evaluate")
                                .addOutput("case1", "case1", "object", "First case")
                                .addOutput("case2", "case2", "object", "Second case")
                                .addOutput("case3", "case3", "object", "Third case")
                                .addOutput("default", "default", "object", "Default case")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("cases", "array", List.of(), true,
                                                "Array of case conditions")
                                .build());

                // 3. Loop/Iterate
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.control.loop")
                                .name("Loop")
                                .description("Iterate over collection")
                                .category("Control Flow")
                                .icon("repeat")
                                .color("#F59E0B")
                                .addInput("collection", "array", true, "Items to iterate")
                                .addOutput("item", "item", "object", "Current item")
                                .addOutput("complete", "complete", "array", "All processed items")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("parallel", "boolean", false, false, "Parallel execution")
                                .addProperty("maxConcurrency", "integer", 1, false, "Max parallel items")
                                .addProperty("continueOnError", "boolean", false, false,
                                                "Continue if item fails")
                                .build());

                // 4. Merge
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.control.merge")
                                .name("Merge")
                                .description("Merge multiple inputs")
                                .category("Control Flow")
                                .icon("merge")
                                .color("#F59E0B")
                                .addInput("input1", "object", true, "First input")
                                .addInput("input2", "object", false, "Second input")
                                .addInput("input3", "object", false, "Third input")
                                .addOutput("success", "merged", "object", "Merged output")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("strategy", "string", "all", true,
                                                "Merge strategy (all/any/first)")
                                .addProperty("timeout", "integer", 30000, false,
                                                "Wait timeout (ms) for all strategy")
                                .build());

                // 5. Parallel
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.control.parallel")
                                .name("Parallel")
                                .description("Execute branches in parallel")
                                .category("Control Flow")
                                .icon("layers")
                                .color("#F59E0B")
                                .addInput("input", "object", true, "Data for all branches")
                                .addOutput("branch1", "branch1", "object", "First branch")
                                .addOutput("branch2", "branch2", "object", "Second branch")
                                .addOutput("branch3", "branch3", "object", "Third branch")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("maxConcurrency", "integer", 4, false,
                                                "Max parallel branches")
                                .addProperty("failFast", "boolean", true, false,
                                                "Fail all if one branch fails")
                                .build());

                // 6. Subworkflow
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.control.subworkflow")
                                .name("Subworkflow")
                                .description("Execute another workflow")
                                .category("Control Flow")
                                .icon("workflow")
                                .color("#F59E0B")
                                .addInput("input", "object", true, "Subworkflow input")
                                .addOutput("success", "output", "object", "Subworkflow output")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("workflowId", "string", null, true, "Workflow to execute")
                                .addProperty("async", "boolean", false, false, "Asynchronous execution")
                                .build());

                // 7. Wait/Delay
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.control.wait")
                                .name("Wait")
                                .description("Wait for duration or event")
                                .category("Control Flow")
                                .icon("clock")
                                .color("#F59E0B")
                                .addInput("input", "object", true, "Data to pass through")
                                .addOutput("success", "output", "object", "Passed through data")
                                .addOutput("timeout", "timeout", "object", "Timeout occurred")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("duration", "integer", 1000, false,
                                                "Wait duration (ms)")
                                .addProperty("waitFor", "string", null, false,
                                                "Event name to wait for")
                                .addProperty("timeout", "integer", 300000, false,
                                                "Event wait timeout (ms)")
                                .build());

                // 8. Batch Processor
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.control.batch")
                                .name("Batch Processor")
                                .description("Process items in batches")
                                .category("Control Flow")
                                .icon("package")
                                .color("#F59E0B")
                                .addInput("items", "array", true, "Items to batch process")
                                .addOutput("batch", "batch", "array", "Current batch")
                                .addOutput("complete", "complete", "array", "All results")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("batchSize", "integer", 10, true, "Items per batch")
                                .addProperty("parallel", "boolean", false, false, "Parallel batches")
                                .build());

                return nodes;
        }

        // Continued in next message due to length...
        // Continuation of CompleteBuiltInNodeTypes.java

        // ========================================================================
        // DATA NODES (10 nodes)
        // ========================================================================

        private List<NodeTypeDescriptor> buildDataNodes() {
                List<NodeTypeDescriptor> nodes = new ArrayList<>();

                // 1. JSON Transform
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.data.json-transform")
                                .name("Transform JSON")
                                .description("Transform JSON using JSONata or JMESPath")
                                .category("Data")
                                .icon("transform")
                                .color("#3B82F6")
                                .addInput("data", "object", true, "Input JSON data")
                                .addOutput("success", "result", "object", "Transformed data")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("expression", "string", null, true,
                                                "Transform expression")
                                .addProperty("language", "string", "jsonata", false,
                                                "Expression language (jsonata/jmespath)")
                                .build());

                // 2. Data Validation
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.data.validate")
                                .name("Validate Data")
                                .description("Validate against JSON schema")
                                .category("Data")
                                .icon("check-circle")
                                .color("#3B82F6")
                                .addInput("data", "object", true, "Data to validate")
                                .addOutput("valid", "valid", "object", "Valid data")
                                .addOutput("invalid", "invalid", "object", "Invalid data with errors")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("schema", "object", null, true, "JSON Schema")
                                .addProperty("strict", "boolean", true, false,
                                                "Strict validation mode")
                                .build());

                // 3. Filter
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.data.filter")
                                .name("Filter Data")
                                .description("Filter array by condition")
                                .category("Data")
                                .icon("filter")
                                .color("#3B82F6")
                                .addInput("data", "array", true, "Array to filter")
                                .addOutput("success", "filtered", "array", "Filtered data")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("condition", "string", null, true,
                                                "Filter condition (CEL)")
                                .addProperty("limit", "integer", null, false, "Result limit")
                                .build());

                // 4. Map/Transform
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.data.map")
                                .name("Map Data")
                                .description("Transform each item in array")
                                .category("Data")
                                .icon("list")
                                .color("#3B82F6")
                                .addInput("data", "array", true, "Array to map")
                                .addOutput("success", "mapped", "array", "Mapped data")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("expression", "string", null, true,
                                                "Map expression (CEL)")
                                .build());

                // 5. Aggregate
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.data.aggregate")
                                .name("Aggregate Data")
                                .description("Perform aggregation operations")
                                .category("Data")
                                .icon("calculator")
                                .color("#3B82F6")
                                .addInput("data", "array", true, "Data to aggregate")
                                .addOutput("success", "result", "object", "Aggregation result")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("operation", "string", "sum", true,
                                                "Operation (sum/avg/min/max/count)")
                                .addProperty("field", "string", null, false, "Field to aggregate")
                                .addProperty("groupBy", "string", null, false, "Group by field")
                                .build());

                // 6. Sort
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.data.sort")
                                .name("Sort Data")
                                .description("Sort array by field")
                                .category("Data")
                                .icon("sort")
                                .color("#3B82F6")
                                .addInput("data", "array", true, "Array to sort")
                                .addOutput("success", "sorted", "array", "Sorted data")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("field", "string", null, true, "Field to sort by")
                                .addProperty("order", "string", "asc", false,
                                                "Sort order (asc/desc)")
                                .build());

                // 7. Deduplicate
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.data.dedupe")
                                .name("Deduplicate")
                                .description("Remove duplicate items")
                                .category("Data")
                                .icon("copy")
                                .color("#3B82F6")
                                .addInput("data", "array", true, "Array with duplicates")
                                .addOutput("success", "unique", "array", "Unique items")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("field", "string", null, false,
                                                "Field to check uniqueness")
                                .build());

                // 8. Join
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.data.join")
                                .name("Join Data")
                                .description("Join two arrays")
                                .category("Data")
                                .icon("link")
                                .color("#3B82F6")
                                .addInput("left", "array", true, "Left array")
                                .addInput("right", "array", true, "Right array")
                                .addOutput("success", "joined", "array", "Joined data")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("leftKey", "string", null, true, "Left join key")
                                .addProperty("rightKey", "string", null, true, "Right join key")
                                .addProperty("type", "string", "inner", false,
                                                "Join type (inner/left/right/full)")
                                .build());

                // 9. Parse CSV
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.data.parse-csv")
                                .name("Parse CSV")
                                .description("Parse CSV string to array")
                                .category("Data")
                                .icon("file-spreadsheet")
                                .color("#3B82F6")
                                .addInput("csv", "string", true, "CSV string")
                                .addOutput("success", "data", "array", "Parsed data")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("hasHeader", "boolean", true, false, "First row is header")
                                .addProperty("delimiter", "string", ",", false, "Field delimiter")
                                .build());

                // 10. Convert Format
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.data.convert")
                                .name("Convert Format")
                                .description("Convert between data formats")
                                .category("Data")
                                .icon("exchange")
                                .color("#3B82F6")
                                .addInput("data", "string", true, "Input data")
                                .addOutput("success", "result", "string", "Converted data")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("fromFormat", "string", "json", true,
                                                "Source format (json/xml/yaml/csv)")
                                .addProperty("toFormat", "string", "xml", true,
                                                "Target format (json/xml/yaml/csv)")
                                .build());

                return nodes;
        }

        // ========================================================================
        // HUMAN NODES (5 nodes)
        // ========================================================================

        private List<NodeTypeDescriptor> buildHumanNodes() {
                List<NodeTypeDescriptor> nodes = new ArrayList<>();

                // 1. Approval
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.human.approval")
                                .name("Approval")
                                .description("Request human approval")
                                .category("Human")
                                .icon("user-check")
                                .color("#EC4899")
                                .addInput("data", "object", true, "Data to review")
                                .addOutput("approved", "approved", "object", "Approved data")
                                .addOutput("rejected", "rejected", "object", "Rejected data")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("assignTo", "string", null, true,
                                                "User/role to assign")
                                .addProperty("slaHours", "integer", 24, false, "SLA in hours")
                                .addProperty("instructions", "string", null, false,
                                                "Instructions for approver")
                                .addProperty("form", "string", null, false, "Form definition ID")
                                .errorHandling(0, "none", false)
                                .build());

                // 2. Form Input
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.human.form-input")
                                .name("Form Input")
                                .description("Collect data via form")
                                .category("Human")
                                .icon("form")
                                .color("#EC4899")
                                .addInput("context", "object", false, "Initial form data")
                                .addOutput("success", "formData", "object", "Submitted form data")
                                .addOutput("cancelled", "cancelled", "object", "Form cancelled")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("formSchema", "object", null, true,
                                                "JSON Schema for form")
                                .addProperty("assignTo", "string", null, true, "User/role")
                                .addProperty("validationRules", "array", List.of(), false,
                                                "Custom validation rules")
                                .build());

                // 3. Manual Review
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.human.review")
                                .name("Manual Review")
                                .description("Manual data review and correction")
                                .category("Human")
                                .icon("eye")
                                .color("#EC4899")
                                .addInput("data", "object", true, "Data to review")
                                .addOutput("success", "reviewed", "object", "Reviewed/corrected data")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("instructions", "string", null, false,
                                                "Review instructions")
                                .addProperty("assignTo", "string", null, true, "User/role")
                                .addProperty("allowEdit", "boolean", true, false,
                                                "Allow data editing")
                                .build());

                // 4. Task Assignment
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.human.task")
                                .name("Assign Task")
                                .description("Create human task with instructions")
                                .category("Human")
                                .icon("clipboard-check")
                                .color("#EC4899")
                                .addInput("taskData", "object", true, "Task details")
                                .addOutput("success", "result", "object", "Task completion result")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("title", "string", null, true, "Task title")
                                .addProperty("description", "string", null, true, "Task description")
                                .addProperty("assignTo", "string", null, true, "User/role")
                                .addProperty("priority", "string", "normal", false,
                                                "Priority (low/normal/high/urgent)")
                                .addProperty("dueDate", "string", null, false, "Due date (ISO-8601)")
                                .build());

                // 5. Decision Point
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.human.decision")
                                .name("Decision Point")
                                .description("Request human decision with options")
                                .category("Human")
                                .icon("help-circle")
                                .color("#EC4899")
                                .addInput("data", "object", true, "Decision context")
                                .addOutput("option1", "option1", "object", "First option selected")
                                .addOutput("option2", "option2", "object", "Second option selected")
                                .addOutput("option3", "option3", "object", "Third option selected")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("question", "string", null, true, "Decision question")
                                .addProperty("options", "array", null, true, "Available options")
                                .addProperty("assignTo", "string", null, true, "User/role")
                                .addProperty("defaultOption", "integer", null, false,
                                                "Default option index")
                                .build());

                return nodes;
        }

        // ========================================================================
        // UTILITY NODES (8 nodes)
        // ========================================================================

        private List<NodeTypeDescriptor> buildUtilityNodes() {
                List<NodeTypeDescriptor> nodes = new ArrayList<>();

                // 1. Delay
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.utility.delay")
                                .name("Delay")
                                .description("Wait for specified duration")
                                .category("Utility")
                                .icon("clock")
                                .color("#6B7280")
                                .addInput("input", "object", true, "Data to pass through")
                                .addOutput("success", "output", "object", "Passed through data")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("duration", "integer", 1000, true,
                                                "Delay duration (ms)")
                                .build());

                // 2. Log
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.utility.log")
                                .name("Log")
                                .description("Log data to console/file")
                                .category("Utility")
                                .icon("file-text")
                                .color("#6B7280")
                                .addInput("data", "object", true, "Data to log")
                                .addOutput("success", "output", "object", "Same as input")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("level", "string", "INFO", false,
                                                "Log level (DEBUG/INFO/WARN/ERROR)")
                                .addProperty("message", "string", null, false, "Log message prefix")
                                .addProperty("includeTimestamp", "boolean", true, false,
                                                "Include timestamp")
                                .build());

                // 3. Variable Set
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.utility.set-variable")
                                .name("Set Variable")
                                .description("Set workflow variable")
                                .category("Utility")
                                .icon("variable")
                                .color("#6B7280")
                                .addInput("value", "object", true, "Value to store")
                                .addOutput("success", "output", "object", "Stored value")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("variableName", "string", null, true,
                                                "Variable name")
                                .addProperty("scope", "string", "workflow", false,
                                                "Scope (workflow/global)")
                                .build());

                // 4. Variable Get
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.utility.get-variable")
                                .name("Get Variable")
                                .description("Get workflow variable")
                                .category("Utility")
                                .icon("variable")
                                .color("#6B7280")
                                .addInput("trigger", "object", false, "Trigger signal")
                                .addOutput("success", "value", "object", "Variable value")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("variableName", "string", null, true, "Variable name")
                                .addProperty("defaultValue", "object", null, false,
                                                "Default if not found")
                                .build());

                // 5. Script Execute
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.utility.script")
                                .name("Execute Script")
                                .description("Execute JavaScript/Python code")
                                .category("Utility")
                                .icon("code")
                                .color("#6B7280")
                                .addInput("input", "object", true, "Script input data")
                                .addOutput("success", "output", "object", "Script output")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("language", "string", "javascript", true,
                                                "Language (javascript/python)")
                                .addProperty("script", "string", null, true, "Script code")
                                .addProperty("timeout", "integer", 30000, false,
                                                "Execution timeout (ms)")
                                .build());

                // 6. Template Render
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.utility.template")
                                .name("Render Template")
                                .description("Render template with data")
                                .category("Utility")
                                .icon("template")
                                .color("#6B7280")
                                .addInput("data", "object", true, "Template data")
                                .addOutput("success", "rendered", "string", "Rendered output")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("template", "string", null, true,
                                                "Template string (Mustache/Handlebars)")
                                .addProperty("engine", "string", "mustache", false,
                                                "Template engine")
                                .build());

                // 7. Generate UUID
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.utility.uuid")
                                .name("Generate UUID")
                                .description("Generate unique identifier")
                                .category("Utility")
                                .icon("hash")
                                .color("#6B7280")
                                .addInput("trigger", "object", false, "Trigger signal")
                                .addOutput("success", "uuid", "string", "Generated UUID")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("version", "string", "v4", false,
                                                "UUID version (v1/v4)")
                                .build());

                // 8. Timestamp
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.utility.timestamp")
                                .name("Get Timestamp")
                                .description("Get current timestamp")
                                .category("Utility")
                                .icon("calendar")
                                .color("#6B7280")
                                .addInput("trigger", "object", false, "Trigger signal")
                                .addOutput("success", "timestamp", "string", "Current timestamp")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("format", "string", "iso8601", false,
                                                "Format (iso8601/unix/custom)")
                                .addProperty("timezone", "string", "UTC", false, "Timezone")
                                .build());

                return nodes;
        }

        // ========================================================================
        // ERROR HANDLING NODES (5 nodes)
        // ========================================================================

        private List<NodeTypeDescriptor> buildErrorNodes() {
                List<NodeTypeDescriptor> nodes = new ArrayList<>();

                // 1. Error Handler
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.error.handler")
                                .name("Error Handler")
                                .description("Handle and route errors")
                                .category("Error")
                                .icon("alert-triangle")
                                .color("#EF4444")
                                .addInput("error", "error_payload", true, "Error to handle")
                                .addOutput("retry", "retry", "object", "Retry operation")
                                .addOutput("fallback", "fallback", "object", "Fallback path")
                                .addOutput("escalate", "escalate", "error_payload", "Escalate to human")
                                .addOutput("abort", "abort", "error_payload", "Abort workflow")
                                .addProperty("maxRetries", "integer", 3, false, "Max retry attempts")
                                .addProperty("retryDelay", "integer", 1000, false,
                                                "Delay between retries (ms)")
                                .addProperty("errorTypes", "array", List.of(), false,
                                                "Error types to handle")
                                .build());

                // 2. Self-Healing
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.error.self-heal")
                                .name("Self-Healing")
                                .description("Auto-fix errors using LLM")
                                .category("Error")
                                .icon("wrench")
                                .color("#EF4444")
                                .addInput("error", "error_payload", true, "Error to fix")
                                .addInput("context", "object", false, "Additional context")
                                .addOutput("success", "fixed", "object", "Fixed input")
                                .addOutput("failed", "failed", "error_payload", "Cannot fix")
                                .addProperty("model", "string", "claude-sonnet-4-5", false,
                                                "LLM model")
                                .addProperty("maxAttempts", "integer", 2, false,
                                                "Max fix attempts")
                                .build());

                // 3. Fallback
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.error.fallback")
                                .name("Fallback")
                                .description("Provide fallback value on error")
                                .category("Error")
                                .icon("shield")
                                .color("#EF4444")
                                .addInput("input", "object", true, "Primary input")
                                .addOutput("success", "output", "object", "Primary or fallback value")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("fallbackValue", "object", null, true,
                                                "Fallback value")
                                .addProperty("fallbackSource", "string", null, false,
                                                "Alternative data source")
                                .build());

                // 4. Circuit Breaker
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.error.circuit-breaker")
                                .name("Circuit Breaker")
                                .description("Prevent cascading failures")
                                .category("Error")
                                .icon("zap-off")
                                .color("#EF4444")
                                .addInput("input", "object", true, "Operation input")
                                .addOutput("success", "output", "object", "Operation output")
                                .addOutput("open", "open", "error_payload", "Circuit open")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("failureThreshold", "integer", 5, false,
                                                "Failures to open circuit")
                                .addProperty("timeout", "integer", 60000, false,
                                                "Circuit open duration (ms)")
                                .addProperty("successThreshold", "integer", 2, false,
                                                "Successes to close circuit")
                                .build());

                // 5. Dead Letter Queue
                nodes.add(NodeTypeDescriptor.builder()
                                .id("builtin.error.dlq")
                                .name("Dead Letter Queue")
                                .description("Send failed items to DLQ")
                                .category("Error")
                                .icon("archive")
                                .color("#EF4444")
                                .addInput("error", "error_payload", true, "Failed item")
                                .addOutput("success", "queued", "object", "DLQ reference")
                                .addOutput("error", "error", "error_payload", "Error details")
                                .addProperty("queueName", "string", "default-dlq", true,
                                                "DLQ name")
                                .addProperty("includeStackTrace", "boolean", true, false,
                                                "Include stack trace")
                                .addProperty("retention", "integer", 7, false,
                                                "Retention days")
                                .build());

                return nodes;
        }

        // ========================================================================
        // SERVICE IMPLEMENTATION METHODS
        // ========================================================================

        @Override
        public Uni<NodeTypeDescriptor> getNodeTypeDescriptor(
                        String nodeTypeId, String tenantId) {
                return getNodeTypeCatalog(tenantId)
                                .map(catalog -> catalog.findNodeType(nodeTypeId)
                                                .orElseThrow(() -> new NotFoundException(
                                                                "Node type not found: " + nodeTypeId)));
        }

        @Override
        public Uni<List<NodeTypeDescriptor>> searchNodeTypes(
                        String query, String category, String tenantId) {
                return getNodeTypeCatalog(tenantId)
                                .map(catalog -> catalog.search(query, category));
        }

        @Override
        public Uni<List<NodeTypeDescriptor>> getNodeTypesByCategory(
                        String category, String tenantId) {
                return getNodeTypeCatalog(tenantId)
                                .map(catalog -> catalog.getNodesByCategory(category));
        }

        @Override
        public Uni<ValidationResult> validateNodeConfig(
                        String nodeTypeId, NodeConfigRequest config, String tenantId) {
                return getNodeTypeDescriptor(nodeTypeId, tenantId)
                                .map(descriptor -> descriptor.validateConfig(config));
        }

        @Override
        public Uni<List<NodeTypeDescriptor>> getNodeSuggestions(
                        NodeSuggestionRequest context, String tenantId) {
                return getNodeTypeCatalog(tenantId)
                                .map(catalog -> catalog.getSuggestions(context));
        }

        @Override
        public Uni<CustomNodeTypeDescriptor> registerCustomNodeType(
                        CustomNodeTypeDescriptor descriptor, String tenantId, String userId) {
                // Implementation for custom node registration
                return Uni.createFrom().failure(
                                new UnsupportedOperationException("Custom node registration not yet implemented"));
        }

        @Override
        public Uni<CustomNodeTypeDescriptor> updateCustomNodeType(
                        String nodeTypeId, CustomNodeTypeDescriptor descriptor,
                        String tenantId, String userId) {
                return Uni.createFrom().failure(
                                new UnsupportedOperationException("Custom node update not yet implemented"));
        }

        @Override
        public Uni<Void> deleteCustomNodeType(
                        String nodeTypeId, String tenantId, String userId) {
                return Uni.createFrom().failure(
                                new UnsupportedOperationException("Custom node deletion not yet implemented"));
        }

        @Override
        public Uni<NodeTypeStats> getNodeTypeStats(
                        String nodeTypeId, String tenantId, String fromDate, String toDate) {
                return Uni.createFrom().failure(
                                new UnsupportedOperationException("Node type stats not yet implemented"));
        }

        @Override
        public Uni<String> getNodeTypeDocumentation(
                        String nodeTypeId, String tenantId) {
                return getNodeTypeDescriptor(nodeTypeId, tenantId)
                                .map(descriptor -> descriptor.getDocumentation());
        }
}