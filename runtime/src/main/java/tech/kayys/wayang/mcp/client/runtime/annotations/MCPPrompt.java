package tech.kayys.wayang.mcp.client.runtime.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.util.Nonbinding;

/**
 * Marks a method as an MCP Prompt handler
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MCPPrompt {
    /**
     * Prompt name
     */
    @Nonbinding
    String name() default "";
    
    /**
     * Prompt description
     */
    @Nonbinding
    String description() default "";
    
    /**
     * Required arguments for the prompt
     */
    @Nonbinding
    PromptArg[] arguments() default {};
    
    /**
     * Whether the prompt is dynamic (requires runtime arguments)
     */
    @Nonbinding
    boolean dynamic() default false;
    
    /**
     * Template engine to use (if any)
     */
    @Nonbinding
    String templateEngine() default "";
}
