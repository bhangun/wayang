package tech.kayys.wayang.mcp.client.runtime.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking parameters in MCP tool and prompt methods.
 * Provides metadata for parameter validation and documentation.
 */
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MCPParam {
    
    /**
     * Parameter name. If not specified, uses the parameter name from bytecode.
     */
    String name() default "";
    
    /**
     * Parameter description for documentation
     */
    String description() default "";
    
    /**
     * Whether this parameter is required
     */
    boolean required() default true;
    
    /**
     * Default value as string (will be converted to parameter type)
     */
    String defaultValue() default "";
    
    /**
     * Parameter type schema information
     */
    String type() default "";
    
    /**
     * JSON Schema for complex parameter validation
     */
    String schema() default "";
    
    /**
     * Example values for documentation
     */
    String[] examples() default {};
    
    /**
     * Minimum value for numeric parameters
     */
    double min() default Double.NEGATIVE_INFINITY;
    
    /**
     * Maximum value for numeric parameters  
     */
    double max() default Double.POSITIVE_INFINITY;
    
    /**
     * Minimum length for string parameters
     */
    int minLength() default 0;
    
    /**
     * Maximum length for string parameters
     */
    int maxLength() default Integer.MAX_VALUE;
    
    /**
     * Pattern for string validation (regex)
     */
    String pattern() default "";
    
    /**
     * Enum values for choice parameters
     */
    String[] enumValues() default {};
    
    /**
     * Whether the parameter can be null
     */
    boolean nullable() default false;
    
    /**
     * Format hint for the parameter (e.g., "date-time", "email", "uri")
     */
    String format() default "";
    
    /**
     * Additional metadata as key-value pairs
     */
    String[] metadata() default {};
}
