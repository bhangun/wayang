package tech.kayys.wayang.mcp.client.runtime.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a prompt argument
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PromptArg {
    /**
     * Argument name
     */
    String name();
    
    /**
     * Argument description
     */
    String description() default "";
    
    /**
     * Whether the argument is required
     */
    boolean required() default true;
    
    /**
     * Default value
     */
    String defaultValue() default "";
}
