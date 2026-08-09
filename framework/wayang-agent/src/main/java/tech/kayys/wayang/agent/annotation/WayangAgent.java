package tech.kayys.wayang.agent.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an interface as a declarative Wayang Agent.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WayangAgent {
    
    /** The type of agent (e.g., "react", "plan-and-solve"). */
    String type() default "react";
    
    /** The memory provider to use (e.g., "in-memory"). */
    String memory() default "in-memory";
    
    /** The list of tool IDs this agent should have access to. */
    String[] tools() default {};
}
