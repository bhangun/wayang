package tech.kayys.wayang.rag.runtime;
import tech.kayys.wayang.rag.plugin.api.*;
import tech.kayys.wayang.rag.core.*;

import jakarta.ws.rs.NameBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface AdminProtected {
}
