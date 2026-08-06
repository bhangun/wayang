package tech.kayys.wayang.error;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;



import java.util.*;

/**
 * Wayang Error Code System - Comprehensive error classification.
 * 
 * <p>This system provides a standardized way to represent errors across all
 * Wayang components. Each error code follows the pattern:</p>
 * 
 * <pre>
 * WYN-{CATEGORY}-{SUBSYSTEM}-{CODE}
 * 
 * Example: WYN-CORE-PLUGIN-001
 * </pre>
 * 
 * <h2>Error Code Categories</h2>
 * <ul>
 *   <li><b>CORE</b> - Core runtime errors (0000-0999)</li>
 *   <li><b>PLUGIN</b> - Plugin system errors (1000-1999)</li>
 *   <li><b>CONFIG</b> - Configuration errors (2000-2999)</li>
 *   <li><b>DATABASE</b> - Database errors (3000-3999)</li>
 *   <li><b>SECURITY</b> - Security errors (4000-4999)</li>
 *   <li><b>EXECUTION</b> - Execution errors (5000-5999)</li>
 *   <li><b>MESSAGING</b> - Messaging errors (6000-6999)</li>
 *   <li><b>STORAGE</b> - Storage errors (7000-7999)</li>
 *   <li><b>NETWORK</b> - Network errors (8000-8999)</li>
 *   <li><b>API</b> - API errors (9000-9999)</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * <pre>
 * {@code
 * try {
 *     // Some operation
 * } catch (Exception e) {
 *     throw new WayangException(
 *         ErrorCode.PLUGIN_LOAD_FAILED,
 *         "Failed to load plugin: " + pluginId,
 *         e
 *     );
 * }
 * }
 * </pre>
 */
public final class ErrorCode {
    
    // ============================================================================
    // Core Errors (0000-0999)
    // ============================================================================
    
    // General Core (0000-0099)
    public static final ErrorCode CORE_INITIALIZATION_FAILED = 
        new ErrorCode("WYN-CORE-0001", "Core initialization failed", Severity.CRITICAL);
    public static final ErrorCode CORE_SHUTDOWN_FAILED = 
        new ErrorCode("WYN-CORE-0002", "Core shutdown failed", Severity.ERROR);
    public static final ErrorCode CORE_RESOURCE_NOT_FOUND = 
        new ErrorCode("WYN-CORE-0003", "Resource not found", Severity.WARNING);
    public static final ErrorCode CORE_RESOURCE_ALREADY_EXISTS = 
        new ErrorCode("WYN-CORE-0004", "Resource already exists", Severity.WARNING);
    public static final ErrorCode CORE_INVALID_STATE = 
        new ErrorCode("WYN-CORE-0005", "Invalid state transition", Severity.ERROR);
    public static final ErrorCode CORE_UNSUPPORTED_OPERATION = 
        new ErrorCode("WYN-CORE-0006", "Unsupported operation", Severity.WARNING);
    public static final ErrorCode CORE_TIMEOUT = 
        new ErrorCode("WYN-CORE-0007", "Operation timed out", Severity.ERROR);
    public static final ErrorCode CORE_INTERRUPTED = 
        new ErrorCode("WYN-CORE-0008", "Operation was interrupted", Severity.ERROR);
    public static final ErrorCode CORE_DEADLOCK_DETECTED = 
        new ErrorCode("WYN-CORE-0009", "Deadlock detected", Severity.CRITICAL);
    public static final ErrorCode CORE_OUT_OF_MEMORY = 
        new ErrorCode("WYN-CORE-0010", "Out of memory", Severity.CRITICAL);
    
    // Resource Management (0100-0199)
    public static final ErrorCode CORE_RESOURCE_CREATION_FAILED = 
        new ErrorCode("WYN-CORE-0101", "Resource creation failed", Severity.ERROR);
    public static final ErrorCode CORE_RESOURCE_LOADING_FAILED = 
        new ErrorCode("WYN-CORE-0102", "Resource loading failed", Severity.ERROR);
    public static final ErrorCode CORE_RESOURCE_SAVING_FAILED = 
        new ErrorCode("WYN-CORE-0103", "Resource saving failed", Severity.ERROR);
    public static final ErrorCode CORE_RESOURCE_DELETION_FAILED = 
        new ErrorCode("WYN-CORE-0104", "Resource deletion failed", Severity.ERROR);
    public static final ErrorCode CORE_RESOURCE_VERSION_MISMATCH = 
        new ErrorCode("WYN-CORE-0105", "Resource version mismatch", Severity.ERROR);
    public static final ErrorCode CORE_RESOURCE_CORRUPTED = 
        new ErrorCode("WYN-CORE-0106", "Resource is corrupted", Severity.ERROR);
    
    // Identity (0200-0299)
    public static final ErrorCode CORE_INVALID_ID = 
        new ErrorCode("WYN-CORE-0201", "Invalid ID format", Severity.WARNING);
    public static final ErrorCode CORE_ID_GENERATION_FAILED = 
        new ErrorCode("WYN-CORE-0202", "ID generation failed", Severity.ERROR);
    public static final ErrorCode CORE_ID_ALREADY_EXISTS = 
        new ErrorCode("WYN-CORE-0203", "ID already exists", Severity.WARNING);
    public static final ErrorCode CORE_ID_NOT_FOUND = 
        new ErrorCode("WYN-CORE-0204", "ID not found", Severity.WARNING);
    
    // Version (0300-0399)
    public static final ErrorCode CORE_INVALID_VERSION = 
        new ErrorCode("WYN-CORE-0301", "Invalid version format", Severity.WARNING);
    public static final ErrorCode CORE_VERSION_MISMATCH = 
        new ErrorCode("WYN-CORE-0302", "Version mismatch", Severity.ERROR);
    public static final ErrorCode CORE_VERSION_UNSUPPORTED = 
        new ErrorCode("WYN-CORE-0303", "Unsupported version", Severity.ERROR);
    
    // ============================================================================
    // Plugin Errors (1000-1999)
    // ============================================================================
    
    // Plugin Loading (1000-1099)
    public static final ErrorCode PLUGIN_LOAD_FAILED = 
        new ErrorCode("WYN-PLUGIN-1001", "Failed to load plugin", Severity.ERROR);
    public static final ErrorCode PLUGIN_NOT_FOUND = 
        new ErrorCode("WYN-PLUGIN-1002", "Plugin not found", Severity.WARNING);
    public static final ErrorCode PLUGIN_ALREADY_LOADED = 
        new ErrorCode("WYN-PLUGIN-1003", "Plugin already loaded", Severity.WARNING);
    public static final ErrorCode PLUGIN_INVALID_MANIFEST = 
        new ErrorCode("WYN-PLUGIN-1004", "Invalid plugin manifest", Severity.ERROR);
    public static final ErrorCode PLUGIN_CORRUPTED_JAR = 
        new ErrorCode("WYN-PLUGIN-1005", "Plugin JAR is corrupted", Severity.ERROR);
    public static final ErrorCode PLUGIN_UNSUPPORTED_FORMAT = 
        new ErrorCode("WYN-PLUGIN-1006", "Unsupported plugin format", Severity.ERROR);
    public static final ErrorCode PLUGIN_VERSION_INCOMPATIBLE = 
        new ErrorCode("WYN-PLUGIN-1007", "Plugin version incompatible", Severity.ERROR);
    public static final ErrorCode PLUGIN_DEPENDENCY_MISSING = 
        new ErrorCode("WYN-PLUGIN-1008", "Plugin dependency missing", Severity.ERROR);
    public static final ErrorCode PLUGIN_DEPENDENCY_CYCLE = 
        new ErrorCode("WYN-PLUGIN-1009", "Plugin dependency cycle detected", Severity.ERROR);
    public static final ErrorCode PLUGIN_DEPENDENCY_VERSION_MISMATCH = 
        new ErrorCode("WYN-PLUGIN-1010", "Plugin dependency version mismatch", Severity.ERROR);
    
    // Plugin Class Loading (1100-1199)
    public static final ErrorCode PLUGIN_CLASS_NOT_FOUND = 
        new ErrorCode("WYN-PLUGIN-1101", "Plugin class not found", Severity.ERROR);
    public static final ErrorCode PLUGIN_CLASS_LOAD_ERROR = 
        new ErrorCode("WYN-PLUGIN-1102", "Plugin class load error", Severity.ERROR);
    public static final ErrorCode PLUGIN_NO_MAIN_CLASS = 
        new ErrorCode("WYN-PLUGIN-1103", "No main class found", Severity.ERROR);
    public static final ErrorCode PLUGIN_MAIN_CLASS_INSTANTIATION_FAILED = 
        new ErrorCode("WYN-PLUGIN-1104", "Main class instantiation failed", Severity.ERROR);
    public static final ErrorCode PLUGIN_CLASS_CONFLICT = 
        new ErrorCode("WYN-PLUGIN-1105", "Plugin class conflict", Severity.ERROR);
    public static final ErrorCode PLUGIN_RESOURCE_NOT_FOUND = 
        new ErrorCode("WYN-PLUGIN-1106", "Plugin resource not found", Severity.WARNING);
    
    // Plugin Lifecycle (1200-1299)
    public static final ErrorCode PLUGIN_INITIALIZATION_FAILED = 
        new ErrorCode("WYN-PLUGIN-1201", "Plugin initialization failed", Severity.ERROR);
    public static final ErrorCode PLUGIN_START_FAILED = 
        new ErrorCode("WYN-PLUGIN-1202", "Plugin start failed", Severity.ERROR);
    public static final ErrorCode PLUGIN_STOP_FAILED = 
        new ErrorCode("WYN-PLUGIN-1203", "Plugin stop failed", Severity.ERROR);
    public static final ErrorCode PLUGIN_UNLOAD_FAILED = 
        new ErrorCode("WYN-PLUGIN-1204", "Plugin unload failed", Severity.ERROR);
    public static final ErrorCode PLUGIN_ALREADY_STARTED = 
        new ErrorCode("WYN-PLUGIN-1205", "Plugin already started", Severity.WARNING);
    public static final ErrorCode PLUGIN_ALREADY_STOPPED = 
        new ErrorCode("WYN-PLUGIN-1206", "Plugin already stopped", Severity.WARNING);
    public static final ErrorCode PLUGIN_INVALID_STATE = 
        new ErrorCode("WYN-PLUGIN-1207", "Plugin in invalid state", Severity.ERROR);
    
    // Plugin Extension (1300-1399)
    public static final ErrorCode PLUGIN_EXTENSION_NOT_FOUND = 
        new ErrorCode("WYN-PLUGIN-1301", "Extension not found", Severity.ERROR);
    public static final ErrorCode PLUGIN_EXTENSION_REGISTRATION_FAILED = 
        new ErrorCode("WYN-PLUGIN-1302", "Extension registration failed", Severity.ERROR);
    public static final ErrorCode PLUGIN_EXTENSION_UNREGISTRATION_FAILED = 
        new ErrorCode("WYN-PLUGIN-1303", "Extension unregistration failed", Severity.ERROR);
    public static final ErrorCode PLUGIN_EXTENSION_CONFLICT = 
        new ErrorCode("WYN-PLUGIN-1304", "Extension conflict detected", Severity.ERROR);
    
    // ============================================================================
    // Configuration Errors (2000-2999)
    // ============================================================================
    
    // Config Loading (2000-2099)
    public static final ErrorCode CONFIG_LOAD_FAILED = 
        new ErrorCode("WYN-CONFIG-2001", "Configuration load failed", Severity.ERROR);
    public static final ErrorCode CONFIG_FILE_NOT_FOUND = 
        new ErrorCode("WYN-CONFIG-2002", "Configuration file not found", Severity.WARNING);
    public static final ErrorCode CONFIG_INVALID_FORMAT = 
        new ErrorCode("WYN-CONFIG-2003", "Invalid configuration format", Severity.ERROR);
    public static final ErrorCode CONFIG_PARSE_ERROR = 
        new ErrorCode("WYN-CONFIG-2004", "Configuration parse error", Severity.ERROR);
    public static final ErrorCode CONFIG_INVALID_SYNTAX = 
        new ErrorCode("WYN-CONFIG-2005", "Invalid configuration syntax", Severity.ERROR);
    public static final ErrorCode CONFIG_MISSING_PROPERTY = 
        new ErrorCode("WYN-CONFIG-2006", "Required configuration property missing", Severity.ERROR);
    public static final ErrorCode CONFIG_INVALID_PROPERTY = 
        new ErrorCode("WYN-CONFIG-2007", "Invalid configuration property value", Severity.WARNING);
    
    // Config Operations (2100-2199)
    public static final ErrorCode CONFIG_SAVE_FAILED = 
        new ErrorCode("WYN-CONFIG-2101", "Configuration save failed", Severity.ERROR);
    public static final ErrorCode CONFIG_RELOAD_FAILED = 
        new ErrorCode("WYN-CONFIG-2102", "Configuration reload failed", Severity.ERROR);
    public static final ErrorCode CONFIG_MERGE_FAILED = 
        new ErrorCode("WYN-CONFIG-2103", "Configuration merge failed", Severity.ERROR);
    public static final ErrorCode CONFIG_VALIDATION_FAILED = 
        new ErrorCode("WYN-CONFIG-2104", "Configuration validation failed", Severity.ERROR);
    public static final ErrorCode CONFIG_INVALID_PATH = 
        new ErrorCode("WYN-CONFIG-2105", "Invalid configuration path", Severity.WARNING);
    
    // Config Sources (2200-2299)
    public static final ErrorCode CONFIG_SOURCE_UNAVAILABLE = 
        new ErrorCode("WYN-CONFIG-2201", "Configuration source unavailable", Severity.ERROR);
    public static final ErrorCode CONFIG_SOURCE_AUTH_FAILED = 
        new ErrorCode("WYN-CONFIG-2202", "Configuration source authentication failed", Severity.ERROR);
    public static final ErrorCode CONFIG_SOURCE_TIMEOUT = 
        new ErrorCode("WYN-CONFIG-2203", "Configuration source timeout", Severity.ERROR);
    
    // ============================================================================
    // Database Errors (3000-3999)
    // ============================================================================
    
    // Connection (3000-3099)
    public static final ErrorCode DB_CONNECTION_FAILED = 
        new ErrorCode("WYN-DB-3001", "Database connection failed", Severity.CRITICAL);
    public static final ErrorCode DB_CONNECTION_POOL_EXHAUSTED = 
        new ErrorCode("WYN-DB-3002", "Database connection pool exhausted", Severity.ERROR);
    public static final ErrorCode DB_CONNECTION_TIMEOUT = 
        new ErrorCode("WYN-DB-3003", "Database connection timeout", Severity.ERROR);
    public static final ErrorCode DB_CONNECTION_CLOSED = 
        new ErrorCode("WYN-DB-3004", "Database connection closed", Severity.ERROR);
    public static final ErrorCode DB_INVALID_URL = 
        new ErrorCode("WYN-DB-3005", "Invalid database URL", Severity.ERROR);
    public static final ErrorCode DB_DRIVER_NOT_FOUND = 
        new ErrorCode("WYN-DB-3006", "Database driver not found", Severity.ERROR);
    
    // Query (3100-3199)
    public static final ErrorCode DB_QUERY_FAILED = 
        new ErrorCode("WYN-DB-3101", "Database query failed", Severity.ERROR);
    public static final ErrorCode DB_INVALID_QUERY = 
        new ErrorCode("WYN-DB-3102", "Invalid database query", Severity.ERROR);
    public static final ErrorCode DB_QUERY_TIMEOUT = 
        new ErrorCode("WYN-DB-3103", "Database query timeout", Severity.ERROR);
    public static final ErrorCode DB_RESULT_MAPPING_FAILED = 
        new ErrorCode("WYN-DB-3104", "Database result mapping failed", Severity.ERROR);
    public static final ErrorCode DB_NO_RESULTS = 
        new ErrorCode("WYN-DB-3105", "No results returned", Severity.WARNING);
    
    // Transaction (3200-3299)
    public static final ErrorCode DB_TRANSACTION_START_FAILED = 
        new ErrorCode("WYN-DB-3201", "Transaction start failed", Severity.ERROR);
    public static final ErrorCode DB_TRANSACTION_COMMIT_FAILED = 
        new ErrorCode("WYN-DB-3202", "Transaction commit failed", Severity.ERROR);
    public static final ErrorCode DB_TRANSACTION_ROLLBACK_FAILED = 
        new ErrorCode("WYN-DB-3203", "Transaction rollback failed", Severity.ERROR);
    public static final ErrorCode DB_TRANSACTION_DEADLOCK = 
        new ErrorCode("WYN-DB-3204", "Transaction deadlock detected", Severity.ERROR);
    public static final ErrorCode DB_TRANSACTION_INVALID_STATE = 
        new ErrorCode("WYN-DB-3205", "Invalid transaction state", Severity.ERROR);
    
    // Schema (3300-3399)
    public static final ErrorCode DB_SCHEMA_CREATE_FAILED = 
        new ErrorCode("WYN-DB-3301", "Schema creation failed", Severity.ERROR);
    public static final ErrorCode DB_SCHEMA_UPDATE_FAILED = 
        new ErrorCode("WYN-DB-3302", "Schema update failed", Severity.ERROR);
    public static final ErrorCode DB_SCHEMA_VERSION_MISMATCH = 
        new ErrorCode("WYN-DB-3303", "Schema version mismatch", Severity.ERROR);
    public static final ErrorCode DB_TABLE_NOT_FOUND = 
        new ErrorCode("WYN-DB-3304", "Table not found", Severity.ERROR);
    public static final ErrorCode DB_COLUMN_NOT_FOUND = 
        new ErrorCode("WYN-DB-3305", "Column not found", Severity.ERROR);
    
    // Data (3400-3499)
    public static final ErrorCode DB_DATA_INTEGRITY_VIOLATION = 
        new ErrorCode("WYN-DB-3401", "Data integrity violation", Severity.ERROR);
    public static final ErrorCode DB_UNIQUE_CONSTRAINT_VIOLATION = 
        new ErrorCode("WYN-DB-3402", "Unique constraint violation", Severity.ERROR);
    public static final ErrorCode DB_FOREIGN_KEY_VIOLATION = 
        new ErrorCode("WYN-DB-3403", "Foreign key violation", Severity.ERROR);
    public static final ErrorCode DB_DATA_NOT_FOUND = 
        new ErrorCode("WYN-DB-3404", "Data not found", Severity.WARNING);
    public static final ErrorCode DB_DATA_ALREADY_EXISTS = 
        new ErrorCode("WYN-DB-3405", "Data already exists", Severity.WARNING);
    
    // Migration (3500-3599)
    public static final ErrorCode DB_MIGRATION_FAILED = 
        new ErrorCode("WYN-DB-3501", "Migration failed", Severity.ERROR);
    public static final ErrorCode DB_MIGRATION_SCRIPT_ERROR = 
        new ErrorCode("WYN-DB-3502", "Migration script error", Severity.ERROR);
    public static final ErrorCode DB_MIGRATION_VERSION_CONFLICT = 
        new ErrorCode("WYN-DB-3503", "Migration version conflict", Severity.ERROR);
    
    // ============================================================================
    // Security Errors (4000-4999)
    // ============================================================================
    
    // Authentication (4000-4099)
    public static final ErrorCode SEC_AUTH_FAILED = 
        new ErrorCode("WYN-SEC-4001", "Authentication failed", Severity.ERROR);
    public static final ErrorCode SEC_AUTH_INVALID_CREDENTIALS = 
        new ErrorCode("WYN-SEC-4002", "Invalid credentials", Severity.WARNING);
    public static final ErrorCode SEC_AUTH_EXPIRED = 
        new ErrorCode("WYN-SEC-4003", "Authentication expired", Severity.WARNING);
    public static final ErrorCode SEC_AUTH_LOCKED = 
        new ErrorCode("WYN-SEC-4004", "Account locked", Severity.WARNING);
    public static final ErrorCode SEC_AUTH_DISABLED = 
        new ErrorCode("WYN-SEC-4005", "Account disabled", Severity.WARNING);
    public static final ErrorCode SEC_AUTH_MFA_REQUIRED = 
        new ErrorCode("WYN-SEC-4006", "MFA required", Severity.WARNING);
    public static final ErrorCode SEC_AUTH_MFA_FAILED = 
        new ErrorCode("WYN-SEC-4007", "MFA verification failed", Severity.ERROR);
    public static final ErrorCode SEC_AUTH_PROVIDER_UNAVAILABLE = 
        new ErrorCode("WYN-SEC-4008", "Authentication provider unavailable", Severity.ERROR);
    
    // Authorization (4100-4199)
    public static final ErrorCode SEC_AUTHZ_DENIED = 
        new ErrorCode("WYN-SEC-4101", "Authorization denied", Severity.WARNING);
    public static final ErrorCode SEC_AUTHZ_INSUFFICIENT_PERMISSIONS = 
        new ErrorCode("WYN-SEC-4102", "Insufficient permissions", Severity.WARNING);
    public static final ErrorCode SEC_AUTHZ_RESOURCE_ACCESS_DENIED = 
        new ErrorCode("WYN-SEC-4103", "Resource access denied", Severity.WARNING);
    public static final ErrorCode SEC_AUTHZ_ROLE_REQUIRED = 
        new ErrorCode("WYN-SEC-4104", "Role required", Severity.WARNING);
    public static final ErrorCode SEC_AUTHZ_POLICY_VIOLATION = 
        new ErrorCode("WYN-SEC-4105", "Policy violation", Severity.ERROR);
    
    // Token (4200-4299)
    public static final ErrorCode SEC_TOKEN_INVALID = 
        new ErrorCode("WYN-SEC-4201", "Invalid token", Severity.WARNING);
    public static final ErrorCode SEC_TOKEN_EXPIRED = 
        new ErrorCode("WYN-SEC-4202", "Token expired", Severity.WARNING);
    public static final ErrorCode SEC_TOKEN_MALFORMED = 
        new ErrorCode("WYN-SEC-4203", "Malformed token", Severity.WARNING);
    public static final ErrorCode SEC_TOKEN_SIGNATURE_INVALID = 
        new ErrorCode("WYN-SEC-4204", "Invalid token signature", Severity.ERROR);
    public static final ErrorCode SEC_TOKEN_REFRESH_FAILED = 
        new ErrorCode("WYN-SEC-4205", "Token refresh failed", Severity.ERROR);
    public static final ErrorCode SEC_TOKEN_REVOKED = 
        new ErrorCode("WYN-SEC-4206", "Token revoked", Severity.WARNING);
    
    // Session (4300-4399)
    public static final ErrorCode SEC_SESSION_INVALID = 
        new ErrorCode("WYN-SEC-4301", "Invalid session", Severity.WARNING);
    public static final ErrorCode SEC_SESSION_EXPIRED = 
        new ErrorCode("WYN-SEC-4302", "Session expired", Severity.WARNING);
    public static final ErrorCode SEC_SESSION_CREATION_FAILED = 
        new ErrorCode("WYN-SEC-4303", "Session creation failed", Severity.ERROR);
    public static final ErrorCode SEC_SESSION_NOT_FOUND = 
        new ErrorCode("WYN-SEC-4304", "Session not found", Severity.WARNING);
    
    // Encryption (4400-4499)
    public static final ErrorCode SEC_ENCRYPTION_FAILED = 
        new ErrorCode("WYN-SEC-4401", "Encryption failed", Severity.ERROR);
    public static final ErrorCode SEC_DECRYPTION_FAILED = 
        new ErrorCode("WYN-SEC-4402", "Decryption failed", Severity.ERROR);
    public static final ErrorCode SEC_INVALID_KEY = 
        new ErrorCode("WYN-SEC-4403", "Invalid key", Severity.ERROR);
    public static final ErrorCode SEC_KEY_NOT_FOUND = 
        new ErrorCode("WYN-SEC-4404", "Key not found", Severity.ERROR);
    
    // ============================================================================
    // Execution Errors (5000-5999)
    // ============================================================================
    
    // Agent Execution (5000-5099)
    public static final ErrorCode EXEC_AGENT_NOT_FOUND = 
        new ErrorCode("WYN-EXEC-5001", "Agent not found", Severity.ERROR);
    public static final ErrorCode EXEC_AGENT_ALREADY_RUNNING = 
        new ErrorCode("WYN-EXEC-5002", "Agent already running", Severity.WARNING);
    public static final ErrorCode EXEC_AGENT_START_FAILED = 
        new ErrorCode("WYN-EXEC-5003", "Agent start failed", Severity.ERROR);
    public static final ErrorCode EXEC_AGENT_STOP_FAILED = 
        new ErrorCode("WYN-EXEC-5004", "Agent stop failed", Severity.ERROR);
    public static final ErrorCode EXEC_AGENT_PAUSE_FAILED = 
        new ErrorCode("WYN-EXEC-5005", "Agent pause failed", Severity.ERROR);
    public static final ErrorCode EXEC_AGENT_RESUME_FAILED = 
        new ErrorCode("WYN-EXEC-5006", "Agent resume failed", Severity.ERROR);
    public static final ErrorCode EXEC_AGENT_CANCELLED = 
        new ErrorCode("WYN-EXEC-5007", "Agent cancelled", Severity.INFO);
    public static final ErrorCode EXEC_AGENT_TIMEOUT = 
        new ErrorCode("WYN-EXEC-5008", "Agent execution timeout", Severity.ERROR);
    
    // Skill Execution (5100-5199)
    public static final ErrorCode EXEC_SKILL_NOT_FOUND = 
        new ErrorCode("WYN-EXEC-5101", "Skill not found", Severity.ERROR);
    public static final ErrorCode EXEC_SKILL_EXECUTION_FAILED = 
        new ErrorCode("WYN-EXEC-5102", "Skill execution failed", Severity.ERROR);
    public static final ErrorCode EXEC_SKILL_TIMEOUT = 
        new ErrorCode("WYN-EXEC-5103", "Skill execution timeout", Severity.ERROR);
    public static final ErrorCode EXEC_SKILL_RETRY_EXHAUSTED = 
        new ErrorCode("WYN-EXEC-5104", "Skill retry exhausted", Severity.ERROR);
    
    // Workflow Execution (5200-5299)
    public static final ErrorCode EXEC_WORKFLOW_NOT_FOUND = 
        new ErrorCode("WYN-EXEC-5201", "Workflow not found", Severity.ERROR);
    public static final ErrorCode EXEC_WORKFLOW_EXECUTION_FAILED = 
        new ErrorCode("WYN-EXEC-5202", "Workflow execution failed", Severity.ERROR);
    public static final ErrorCode EXEC_WORKFLOW_STEP_FAILED = 
        new ErrorCode("WYN-EXEC-5203", "Workflow step failed", Severity.ERROR);
    public static final ErrorCode EXEC_WORKFLOW_INVALID_TRANSITION = 
        new ErrorCode("WYN-EXEC-5204", "Invalid workflow transition", Severity.ERROR);
    public static final ErrorCode EXEC_WORKFLOW_CONDITION_EVALUATION_FAILED = 
        new ErrorCode("WYN-EXEC-5205", "Workflow condition evaluation failed", Severity.ERROR);
    
    // Plan Execution (5300-5399)
    public static final ErrorCode EXEC_PLAN_NOT_FOUND = 
        new ErrorCode("WYN-EXEC-5301", "Plan not found", Severity.ERROR);
    public static final ErrorCode EXEC_PLAN_EXECUTION_FAILED = 
        new ErrorCode("WYN-EXEC-5302", "Plan execution failed", Severity.ERROR);
    public static final ErrorCode EXEC_PLAN_STEP_FAILED = 
        new ErrorCode("WYN-EXEC-5303", "Plan step failed", Severity.ERROR);
    public static final ErrorCode EXEC_PLAN_INVALID = 
        new ErrorCode("WYN-EXEC-5304", "Invalid plan", Severity.ERROR);
    
    // Reasoning (5400-5499)
    public static final ErrorCode EXEC_REASONING_FAILED = 
        new ErrorCode("WYN-EXEC-5401", "Reasoning failed", Severity.ERROR);
    public static final ErrorCode EXEC_REASONING_TIMEOUT = 
        new ErrorCode("WYN-EXEC-5402", "Reasoning timeout", Severity.ERROR);
    public static final ErrorCode EXEC_REASONING_INCONCLUSIVE = 
        new ErrorCode("WYN-EXEC-5403", "Reasoning inconclusive", Severity.WARNING);
    
    // Model Inference (5500-5599)
    public static final ErrorCode EXEC_MODEL_INVOCATION_FAILED = 
        new ErrorCode("WYN-EXEC-5501", "Model invocation failed", Severity.ERROR);
    public static final ErrorCode EXEC_MODEL_TIMEOUT = 
        new ErrorCode("WYN-EXEC-5502", "Model timeout", Severity.ERROR);
    public static final ErrorCode EXEC_MODEL_INVALID_RESPONSE = 
        new ErrorCode("WYN-EXEC-5503", "Invalid model response", Severity.ERROR);
    public static final ErrorCode EXEC_MODEL_RATE_LIMITED = 
        new ErrorCode("WYN-EXEC-5504", "Model rate limited", Severity.WARNING);
    public static final ErrorCode EXEC_MODEL_QUOTA_EXCEEDED = 
        new ErrorCode("WYN-EXEC-5505", "Model quota exceeded", Severity.ERROR);
    
    // Tool Execution (5600-5699)
    public static final ErrorCode EXEC_TOOL_NOT_FOUND = 
        new ErrorCode("WYN-EXEC-5601", "Tool not found", Severity.ERROR);
    public static final ErrorCode EXEC_TOOL_EXECUTION_FAILED = 
        new ErrorCode("WYN-EXEC-5602", "Tool execution failed", Severity.ERROR);
    public static final ErrorCode EXEC_TOOL_TIMEOUT = 
        new ErrorCode("WYN-EXEC-5603", "Tool timeout", Severity.ERROR);
    public static final ErrorCode EXEC_TOOL_INVALID_INPUT = 
        new ErrorCode("WYN-EXEC-5604", "Invalid tool input", Severity.ERROR);
    
    // ============================================================================
    // Messaging Errors (6000-6999)
    // ============================================================================
    
    // Publish (6000-6099)
    public static final ErrorCode MSG_PUBLISH_FAILED = 
        new ErrorCode("WYN-MSG-6001", "Message publish failed", Severity.ERROR);
    public static final ErrorCode MSG_PUBLISH_TIMEOUT = 
        new ErrorCode("WYN-MSG-6002", "Message publish timeout", Severity.ERROR);
    public static final ErrorCode MSG_INVALID_TOPIC = 
        new ErrorCode("WYN-MSG-6003", "Invalid topic", Severity.ERROR);
    public static final ErrorCode MSG_TOPIC_NOT_FOUND = 
        new ErrorCode("WYN-MSG-6004", "Topic not found", Severity.ERROR);
    public static final ErrorCode MSG_SERIALIZATION_FAILED = 
        new ErrorCode("WYN-MSG-6005", "Message serialization failed", Severity.ERROR);
    
    // Subscribe (6100-6199)
    public static final ErrorCode MSG_SUBSCRIBE_FAILED = 
        new ErrorCode("WYN-MSG-6101", "Subscribe failed", Severity.ERROR);
    public static final ErrorCode MSG_UNSUBSCRIBE_FAILED = 
        new ErrorCode("WYN-MSG-6102", "Unsubscribe failed", Severity.ERROR);
    public static final ErrorCode MSG_CONSUMER_GROUP_NOT_FOUND = 
        new ErrorCode("WYN-MSG-6103", "Consumer group not found", Severity.ERROR);
    public static final ErrorCode MSG_ALREADY_SUBSCRIBED = 
        new ErrorCode("WYN-MSG-6104", "Already subscribed", Severity.WARNING);
    
    // Consumer (6200-6299)
    public static final ErrorCode MSG_CONSUMER_ERROR = 
        new ErrorCode("WYN-MSG-6201", "Consumer error", Severity.ERROR);
    public static final ErrorCode MSG_CONSUMER_TIMEOUT = 
        new ErrorCode("WYN-MSG-6202", "Consumer timeout", Severity.ERROR);
    public static final ErrorCode MSG_CONSUMER_DEAD_LETTER = 
        new ErrorCode("WYN-MSG-6203", "Message sent to dead letter queue", Severity.WARNING);
    public static final ErrorCode MSG_CONSUMER_REBALANCE = 
        new ErrorCode("WYN-MSG-6204", "Consumer rebalance", Severity.INFO);
    
    // ============================================================================
    // Storage Errors (7000-7999)
    // ============================================================================
    
    // File Storage (7000-7099)
    public static final ErrorCode STORAGE_FILE_NOT_FOUND = 
        new ErrorCode("WYN-STORAGE-7001", "File not found", Severity.WARNING);
    public static final ErrorCode STORAGE_FILE_READ_FAILED = 
        new ErrorCode("WYN-STORAGE-7002", "File read failed", Severity.ERROR);
    public static final ErrorCode STORAGE_FILE_WRITE_FAILED = 
        new ErrorCode("WYN-STORAGE-7003", "File write failed", Severity.ERROR);
    public static final ErrorCode STORAGE_FILE_DELETE_FAILED = 
        new ErrorCode("WYN-STORAGE-7004", "File delete failed", Severity.ERROR);
    public static final ErrorCode STORAGE_FILE_PERMISSION_DENIED = 
        new ErrorCode("WYN-STORAGE-7005", "File permission denied", Severity.ERROR);
    public static final ErrorCode STORAGE_DISK_FULL = 
        new ErrorCode("WYN-STORAGE-7006", "Disk full", Severity.CRITICAL);
    
    // Cache (7100-7199)
    public static final ErrorCode STORAGE_CACHE_MISS = 
        new ErrorCode("WYN-STORAGE-7101", "Cache miss", Severity.INFO);
    public static final ErrorCode STORAGE_CACHE_READ_FAILED = 
        new ErrorCode("WYN-STORAGE-7102", "Cache read failed", Severity.ERROR);
    public static final ErrorCode STORAGE_CACHE_WRITE_FAILED = 
        new ErrorCode("WYN-STORAGE-7103", "Cache write failed", Severity.ERROR);
    public static final ErrorCode STORAGE_CACHE_EVICTION_FAILED = 
        new ErrorCode("WYN-STORAGE-7104", "Cache eviction failed", Severity.ERROR);
    public static final ErrorCode STORAGE_CACHE_INVALID_KEY = 
        new ErrorCode("WYN-STORAGE-7105", "Invalid cache key", Severity.WARNING);
    
    // ============================================================================
    // Network Errors (8000-8999)
    // ============================================================================
    
    public static final ErrorCode NETWORK_CONNECTION_FAILED = 
        new ErrorCode("WYN-NET-8001", "Network connection failed", Severity.ERROR);
    public static final ErrorCode NETWORK_TIMEOUT = 
        new ErrorCode("WYN-NET-8002", "Network timeout", Severity.ERROR);
    public static final ErrorCode NETWORK_DNS_RESOLUTION_FAILED = 
        new ErrorCode("WYN-NET-8003", "DNS resolution failed", Severity.ERROR);
    public static final ErrorCode NETWORK_SSL_HANDSHAKE_FAILED = 
        new ErrorCode("WYN-NET-8004", "SSL handshake failed", Severity.ERROR);
    public static final ErrorCode NETWORK_UNAVAILABLE = 
        new ErrorCode("WYN-NET-8005", "Network unavailable", Severity.CRITICAL);
    
    // ============================================================================
    // API Errors (9000-9999)
    // ============================================================================
    
    // REST API (9000-9099)
    public static final ErrorCode API_REST_NOT_FOUND = 
        new ErrorCode("WYN-API-9001", "REST API endpoint not found", Severity.WARNING);
    public static final ErrorCode API_REST_METHOD_NOT_ALLOWED = 
        new ErrorCode("WYN-API-9002", "REST API method not allowed", Severity.WARNING);
    public static final ErrorCode API_REST_INVALID_REQUEST = 
        new ErrorCode("WYN-API-9003", "Invalid REST API request", Severity.WARNING);
    public static final ErrorCode API_REST_SERIALIZATION_FAILED = 
        new ErrorCode("WYN-API-9004", "REST API serialization failed", Severity.ERROR);
    public static final ErrorCode API_REST_RESPONSE_ERROR = 
        new ErrorCode("WYN-API-9005", "REST API response error", Severity.ERROR);
    
    // GraphQL API (9100-9199)
    public static final ErrorCode API_GRAPHQL_QUERY_INVALID = 
        new ErrorCode("WYN-API-9101", "Invalid GraphQL query", Severity.WARNING);
    public static final ErrorCode API_GRAPHQL_MUTATION_FAILED = 
        new ErrorCode("WYN-API-9102", "GraphQL mutation failed", Severity.ERROR);
    public static final ErrorCode API_GRAPHQL_RESOLVER_FAILED = 
        new ErrorCode("WYN-API-9103", "GraphQL resolver failed", Severity.ERROR);
    
    // gRPC API (9200-9299)
    public static final ErrorCode API_GRPC_INVOCATION_FAILED = 
        new ErrorCode("WYN-API-9201", "gRPC invocation failed", Severity.ERROR);
    public static final ErrorCode API_GRPC_SERIALIZATION_FAILED = 
        new ErrorCode("WYN-API-9202", "gRPC serialization failed", Severity.ERROR);
    public static final ErrorCode API_GRPC_DESERIALIZATION_FAILED = 
        new ErrorCode("WYN-API-9203", "gRPC deserialization failed", Severity.ERROR);
    
    // ============================================================================
    // Observability Errors (10000-10999)
    // ============================================================================
    
    // Tracing (10000-10099)
    public static final ErrorCode OBS_TRACE_EXPORT_FAILED = 
        new ErrorCode("WYN-OBS-10001", "Trace export failed", Severity.ERROR);
    public static final ErrorCode OBS_TRACE_SPAN_CREATION_FAILED = 
        new ErrorCode("WYN-OBS-10002", "Span creation failed", Severity.ERROR);
    
    // Metrics (10100-10199)
    public static final ErrorCode OBS_METRICS_COLLECTION_FAILED = 
        new ErrorCode("WYN-OBS-10101", "Metrics collection failed", Severity.ERROR);
    public static final ErrorCode OBS_METRICS_EXPORT_FAILED = 
        new ErrorCode("WYN-OBS-10102", "Metrics export failed", Severity.ERROR);
    
    // Logging (10200-10299)
    public static final ErrorCode OBS_LOG_WRITE_FAILED = 
        new ErrorCode("WYN-OBS-10201", "Log write failed", Severity.ERROR);
    public static final ErrorCode OBS_LOG_ROTATION_FAILED = 
        new ErrorCode("WYN-OBS-10202", "Log rotation failed", Severity.ERROR);
    
    // ============================================================================
    // Audit Errors (11000-11099)
    // ============================================================================
    
    public static final ErrorCode AUDIT_WRITE_FAILED = 
        new ErrorCode("WYN-AUDIT-11001", "Audit write failed", Severity.ERROR);
    public static final ErrorCode AUDIT_QUERY_FAILED = 
        new ErrorCode("WYN-AUDIT-11002", "Audit query failed", Severity.ERROR);
    public static final ErrorCode AUDIT_EXPORT_FAILED = 
        new ErrorCode("WYN-AUDIT-11003", "Audit export failed", Severity.ERROR);
    public static final ErrorCode AUDIT_DATA_CORRUPTED = 
        new ErrorCode("WYN-AUDIT-11004", "Audit data corrupted", Severity.ERROR);
    public static final ErrorCode AUDIT_STORAGE_FULL = 
        new ErrorCode("WYN-AUDIT-11005", "Audit storage full", Severity.ERROR);
    
    // ============================================================================
    // Tenant Errors (12000-12099)
    // ============================================================================
    
    public static final ErrorCode TENANT_NOT_FOUND = 
        new ErrorCode("WYN-TENANT-12001", "Tenant not found", Severity.WARNING);
    public static final ErrorCode TENANT_ALREADY_EXISTS = 
        new ErrorCode("WYN-TENANT-12002", "Tenant already exists", Severity.WARNING);
    public static final ErrorCode TENANT_CREATION_FAILED = 
        new ErrorCode("WYN-TENANT-12003", "Tenant creation failed", Severity.ERROR);
    public static final ErrorCode TENANT_UPDATE_FAILED = 
        new ErrorCode("WYN-TENANT-12004", "Tenant update failed", Severity.ERROR);
    public static final ErrorCode TENANT_DELETION_FAILED = 
        new ErrorCode("WYN-TENANT-12005", "Tenant deletion failed", Severity.ERROR);
    public static final ErrorCode TENANT_QUOTA_EXCEEDED = 
        new ErrorCode("WYN-TENANT-12006", "Tenant quota exceeded", Severity.ERROR);
    public static final ErrorCode TENANT_SUSPENDED = 
        new ErrorCode("WYN-TENANT-12007", "Tenant suspended", Severity.WARNING);
    public static final ErrorCode TENANT_INACTIVE = 
        new ErrorCode("WYN-TENANT-12008", "Tenant inactive", Severity.WARNING);
    
    // ============================================================================
    // Rate Limiter Errors (13000-13099)
    // ============================================================================
    
    public static final ErrorCode RATE_LIMIT_EXCEEDED = 
        new ErrorCode("WYN-RATE-13001", "Rate limit exceeded", Severity.WARNING);
    public static final ErrorCode RATE_LIMIT_CONFIG_INVALID = 
        new ErrorCode("WYN-RATE-13002", "Invalid rate limit configuration", Severity.ERROR);
    public static final ErrorCode RATE_LIMIT_TOKEN_BUCKET_EMPTY = 
        new ErrorCode("WYN-RATE-13003", "Token bucket empty", Severity.WARNING);
    
    // ============================================================================
    // Circuit Breaker Errors (14000-14099)
    // ============================================================================
    
    public static final ErrorCode CIRCUIT_OPEN = 
        new ErrorCode("WYN-CB-14001", "Circuit breaker open", Severity.ERROR);
    public static final ErrorCode CIRCUIT_HALF_OPEN = 
        new ErrorCode("WYN-CB-14002", "Circuit breaker half-open", Severity.WARNING);
    public static final ErrorCode CIRCUIT_CONFIG_INVALID = 
        new ErrorCode("WYN-CB-14003", "Invalid circuit breaker configuration", Severity.ERROR);
    public static final ErrorCode CIRCUIT_MAX_CONCURRENT_EXCEEDED = 
        new ErrorCode("WYN-CB-14004", "Max concurrent requests exceeded", Severity.ERROR);
    
    // ============================================================================
    // Validation Errors (15000-15099)
    // ============================================================================
    
    public static final ErrorCode VALIDATION_FAILED = 
        new ErrorCode("WYN-VAL-15001", "Validation failed", Severity.WARNING);
    public static final ErrorCode VALIDATION_INVALID_INPUT = 
        new ErrorCode("WYN-VAL-15002", "Invalid input", Severity.WARNING);
    public static final ErrorCode VALIDATION_REQUIRED_FIELD_MISSING = 
        new ErrorCode("WYN-VAL-15003", "Required field missing", Severity.WARNING);
    public static final ErrorCode VALIDATION_INVALID_FORMAT = 
        new ErrorCode("WYN-VAL-15004", "Invalid format", Severity.WARNING);
    public static final ErrorCode VALIDATION_INVALID_RANGE = 
        new ErrorCode("WYN-VAL-15005", "Value out of range", Severity.WARNING);
    public static final ErrorCode VALIDATION_INVALID_LENGTH = 
        new ErrorCode("WYN-VAL-15006", "Invalid length", Severity.WARNING);
    public static final ErrorCode VALIDATION_REFERENCE_VIOLATION = 
        new ErrorCode("WYN-VAL-15007", "Reference violation", Severity.WARNING);
    public static final ErrorCode VALIDATION_CROSS_FIELD_CONFLICT = 
        new ErrorCode("WYN-VAL-15008", "Cross-field validation conflict", Severity.WARNING);
    
    // ============================================================================
    // Migration Errors (16000-16099)
    // ============================================================================
    
    public static final ErrorCode MIGRATION_NOT_FOUND = 
        new ErrorCode("WYN-MIG-16001", "Migration not found", Severity.ERROR);
    public static final ErrorCode MIGRATION_APPLY_FAILED = 
        new ErrorCode("WYN-MIG-16002", "Migration apply failed", Severity.ERROR);
    public static final ErrorCode MIGRATION_ROLLBACK_FAILED = 
        new ErrorCode("WYN-MIG-16003", "Migration rollback failed", Severity.ERROR);
    public static final ErrorCode MIGRATION_VERSION_CONFLICT = 
        new ErrorCode("WYN-MIG-16004", "Migration version conflict", Severity.ERROR);
    public static final ErrorCode MIGRATION_SCRIPT_NOT_FOUND = 
        new ErrorCode("WYN-MIG-16005", "Migration script not found", Severity.ERROR);
    
    // ============================================================================
    // General
    // ============================================================================
    
    public static final ErrorCode UNKNOWN_ERROR = 
        new ErrorCode("WYN-UNKNOWN-99999", "Unknown error occurred", Severity.ERROR);
    
    // ============================================================================
    // Error Code Structure
    // ============================================================================
    
    private final String code;
    private final String message;
    private final Severity severity;
    private final String category;
    private final String subsystem;
    
    private ErrorCode(String code, String message, Severity severity) {
        this.code = code;
        this.message = message;
        this.severity = severity;
        
        // Parse category and subsystem from code
        String[] parts = code.split("-");
        if (parts.length >= 3) {
            this.category = parts[1];
            this.subsystem = parts[2];
        } else {
            this.category = "UNKNOWN";
            this.subsystem = "UNKNOWN";
        }
    }
    
    /**
     * Gets the error code string.
     *
     * @return The error code
     */
    public String getCode() {
        return code;
    }
    
    /**
     * Gets the error message.
     *
     * @return The error message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Gets the severity level.
     *
     * @return The severity
     */
    public Severity getSeverity() {
        return severity;
    }
    
    /**
     * Gets the error category.
     *
     * @return The category
     */
    public String getCategory() {
        return category;
    }
    
    /**
     * Gets the subsystem.
     *
     * @return The subsystem
     */
    public String getSubsystem() {
        return subsystem;
    }
    
    /**
     * Creates a formatted error message with the code.
     *
     * @param details Additional details
     * @return Formatted error message
     */
    public String format(String details) {
        return String.format("[%s] %s: %s", code, message, details);
    }
    
    /**
     * Creates a formatted error message with the code and cause.
     *
     * @param details Additional details
     * @param cause The cause
     * @return Formatted error message
     */
    public String format(String details, Throwable cause) {
        return String.format("[%s] %s: %s - %s", code, message, details, 
            cause != null ? cause.getMessage() : "Unknown cause");
    }
    
    @Override
    public String toString() {
        return String.format("%s [%s] - %s", code, severity, message);
    }
    
    /**
     * Severity levels for errors.
     */
    public enum Severity {
        /**
         * Informational - Not an error, just information
         */
        INFO,
        
        /**
         * Warning - Something went wrong but the system can continue
         */
        WARNING,
        
        /**
         * Error - Something went wrong and the operation failed
         */
        ERROR,
        
        /**
         * Critical - Something went seriously wrong and the system may be unstable
         */
        CRITICAL
    }
}