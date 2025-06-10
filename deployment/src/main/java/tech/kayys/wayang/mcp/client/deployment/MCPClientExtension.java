package tech.kayys.wayang.mcp.client.deployment;

import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.deployment.BeanDefiningAnnotationBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveHierarchyBuildItem;
import io.quarkus.gizmo.ClassCreator;
import io.quarkus.gizmo.ClassOutput;
import io.quarkus.gizmo.MethodCreator;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;


import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.objectweb.asm.Opcodes;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.mcp.client.runtime.MCPClientRecorder;
import tech.kayys.wayang.mcp.client.runtime.annotations.MCPClient;
import tech.kayys.wayang.mcp.client.runtime.annotations.MCPMethod;
import tech.kayys.wayang.mcp.client.runtime.client.MCPClientConfiguration;
import tech.kayys.wayang.mcp.client.runtime.client.MCPClientFactory;
import tech.kayys.wayang.mcp.client.runtime.client.MCPClientProducer;
import tech.kayys.wayang.mcp.client.runtime.client.MCPConnectionManager;
import tech.kayys.wayang.mcp.client.runtime.config.MCPRuntimeConfig;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.GeneratedClassBuildItem;
import tech.kayys.wayang.mcp.client.runtime.annotations.MCPClientQualifier;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.builditem.RunTimeConfigurationDefaultBuildItem;
import tech.kayys.wayang.mcp.client.runtime.config.MCPConfigProducer;
import tech.kayys.wayang.mcp.client.runtime.transport.MCPTransportFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MCPClientExtension {

    private static final String FEATURE = "mcp-client";
    
    private static final DotName MCP_CLIENT = DotName.createSimple(MCPClient.class.getName());
    private static final DotName MCP_METHOD = DotName.createSimple(MCPMethod.class.getName());

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem beans() {
        return AdditionalBeanBuildItem.builder()
            .addBeanClass(MCPClientConfiguration.class)
            .addBeanClass(MCPClientFactory.class)
            .addBeanClass(MCPClientProducer.class)
            .addBeanClass(MCPConfigProducer.class)
            .addBeanClass(MCPTransportFactory.class)
            .build();
    }
    
    @BuildStep
    RunTimeConfigurationDefaultBuildItem configuration() {
        return new RunTimeConfigurationDefaultBuildItem("quarkus.mcp.servers.*.transport.type", "string");
    }

    @BuildStep
    void registerBeanDefiningAnnotations(BuildProducer<BeanDefiningAnnotationBuildItem> beanDefiningAnnotations) {
        beanDefiningAnnotations.produce(new BeanDefiningAnnotationBuildItem(MCP_CLIENT));
    }

    @BuildStep
    void registerReflectiveClasses(
            CombinedIndexBuildItem combinedIndex,
            BuildProducer<ReflectiveClassBuildItem> reflectiveClasses,
            BuildProducer<ReflectiveHierarchyBuildItem> reflectiveHierarchy) {

        IndexView index = combinedIndex.getIndex();
        // Register MCP-related classes for reflection
        reflectiveClasses.produce(ReflectiveClassBuildItem.builder(
                MCPClient.class,
                MCPMethod.class,
                MCPClientConfiguration.class,
                MCPClientFactory.class,
                MCPConnectionManager.class
        ).methods().fields().build());

        // Register all classes annotated with @MCPClient
        for (AnnotationInstance annotation : index.getAnnotations(MCP_CLIENT)) {
            if (annotation.target().kind() == AnnotationTarget.Kind.CLASS) {
                ClassInfo classInfo = annotation.target().asClass();
                reflectiveClasses.produce(ReflectiveClassBuildItem.builder(classInfo.name().toString())
                        .methods().fields().build());
                
                // Register parameter and return types for reflection
                registerMethodTypes(classInfo, reflectiveHierarchy);
            }
        }
    }

    private void registerMethodTypes(ClassInfo classInfo, BuildProducer<ReflectiveHierarchyBuildItem> reflectiveHierarchy) {
        for (MethodInfo method : classInfo.methods()) {
            if (method.hasAnnotation(MCP_METHOD)) {
                // Register return type
                Type returnType = method.returnType();
                if (returnType.kind() == Type.Kind.CLASS) {
                    reflectiveHierarchy.produce(new ReflectiveHierarchyBuildItem(
                            returnType));
                }
                
                // Register parameter types
                for (Type paramType : method.parameterTypes()) {
                    if (paramType.kind() == Type.Kind.CLASS) {
                        reflectiveHierarchy.produce(new ReflectiveHierarchyBuildItem(
                                paramType));
                    }
                }
            }
        }
    }

    @BuildStep
    void generateMCPClients(
            CombinedIndexBuildItem combinedIndex,
            BuildProducer<GeneratedClassBuildItem> generatedClasses,
            BuildProducer<UnremovableBeanBuildItem> unremovableBeans) {

        IndexView index = combinedIndex.getIndex();
        for (AnnotationInstance annotation : index.getAnnotations(MCP_CLIENT)) {
            if (annotation.target().kind() == AnnotationTarget.Kind.CLASS) {
                ClassInfo classInfo = annotation.target().asClass();
                generateMCPClientImplementation(classInfo, annotation, generatedClasses);
                
                // Make the generated implementation unremovable and register for the interface type with the qualifier
                unremovableBeans.produce(UnremovableBeanBuildItem.beanTypes(
                    DotName.createSimple(classInfo.name().toString()),
                    DotName.createSimple(classInfo.name().toString() + "$$MCPClientImpl")
                ));

                // Register the interface as a CDI bean
                unremovableBeans.produce(UnremovableBeanBuildItem.beanTypes(
                    DotName.createSimple(classInfo.name().toString())
                ));
            }
        }
    }

    private void generateMCPClientImplementation(ClassInfo interfaceInfo, AnnotationInstance annotation, BuildProducer<GeneratedClassBuildItem> generatedClasses) {
        String interfaceName = interfaceInfo.name().toString();
        String implClassName = interfaceName + "$$MCPClientImpl";
        
        // Extract configuration from annotation
        String configPrefix = annotation.value("configPrefix") != null 
            ? annotation.value("configPrefix").asString() 
            : interfaceInfo.name().local().toLowerCase();
        
        String serverName = annotation.value("serverName") != null 
            ? annotation.value("serverName").asString() 
            : "default";

        ClassOutput classOutput = new ClassOutput() {
            @Override
            public void write(String name, byte[] data) {
                generatedClasses.produce(new GeneratedClassBuildItem(true, name, data));
            }
        };

        try (ClassCreator classCreator = ClassCreator.builder()
                .classOutput(classOutput)
                .className(implClassName)
                .interfaces(interfaceName)
                .build()) {

            classCreator.addAnnotation(ApplicationScoped.class);
            classCreator.addAnnotation(MCPClientQualifier.class).addValue("value", serverName);

            // Add fields
            var connectionManagerField = classCreator.getFieldCreator("connectionManager", MCPConnectionManager.class)
                    .setModifiers(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL);

            var configField = classCreator.getFieldCreator("config", MCPClientConfiguration.class)
                    .setModifiers(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL);

            // Constructor
            try (MethodCreator constructor = classCreator.getMethodCreator("<init>", void.class, 
                    MCPConnectionManager.class, MCPClientConfiguration.class)) {
                constructor.setModifiers(Opcodes.ACC_PUBLIC);
                constructor.addAnnotation(jakarta.inject.Inject.class);

                ResultHandle self = constructor.getThis();
                constructor.invokeSpecialMethod(
                    MethodDescriptor.ofConstructor(Object.class), self);
                
                constructor.writeInstanceField(connectionManagerField.getFieldDescriptor(), self, constructor.getMethodParam(0));
                constructor.writeInstanceField(configField.getFieldDescriptor(), self, constructor.getMethodParam(1));
                constructor.returnValue(null);
            }

            // Implement methods
            for (MethodInfo methodInfo : interfaceInfo.methods()) {
                if (methodInfo.hasAnnotation(MCP_METHOD)) {
                    generateMethodImplementation(classCreator, methodInfo, connectionManagerField, configField, serverName);
                }
            }
        }
    }

    private void generateMethodImplementation(
            ClassCreator classCreator, 
            MethodInfo methodInfo,
            io.quarkus.gizmo.FieldCreator connectionManagerField,
            io.quarkus.gizmo.FieldCreator configField,
            String serverName) {
        
        AnnotationInstance mcpMethodAnnotation = methodInfo.annotation(MCP_METHOD);
        String mcpMethodName = mcpMethodAnnotation.value("value") != null 
            ? mcpMethodAnnotation.value("value").asString() 
            : methodInfo.name();

        int paramCount = methodInfo.parametersCount();
        String[] parameterNames = new String[paramCount];
        Type[] parameterTypes = new Type[paramCount];
        for (int i = 0; i < paramCount; i++) {
            parameterTypes[i] = methodInfo.parameterType(i);
            // Fallback for parameter names if not present
            String paramName = methodInfo.parameterName(i);
            if (paramName == null || paramName.isEmpty()) {
                paramName = "arg" + i;
            }
            parameterNames[i] = paramName;
        }

        Class<?>[] javaParameterTypes = new Class<?>[paramCount];
        for (int i = 0; i < paramCount; i++) {
            javaParameterTypes[i] = safeConvertToJavaType(parameterTypes[i]);
        }
        Class<?> returnType = safeConvertToJavaType(methodInfo.returnType());

        try (MethodCreator methodCreator = classCreator.getMethodCreator(
                methodInfo.name(), returnType, javaParameterTypes)) {
            
            methodCreator.setModifiers(Opcodes.ACC_PUBLIC);

            ResultHandle self = methodCreator.getThis();
            ResultHandle connectionManager = methodCreator.readInstanceField(connectionManagerField.getFieldDescriptor(), self);
            
            // Create parameters map
            ResultHandle paramsMap = methodCreator.newInstance(MethodDescriptor.ofConstructor(HashMap.class));
            
            for (int i = 0; i < parameterNames.length; i++) {
                ResultHandle paramName = methodCreator.load(parameterNames[i]);
                ResultHandle paramValue = methodCreator.getMethodParam(i);
                methodCreator.invokeInterfaceMethod(
                    MethodDescriptor.ofMethod(Map.class, "put", Object.class, Object.class, Object.class),
                    paramsMap, paramName, paramValue);
            }

            // Call MCP method
            ResultHandle serverNameHandle = methodCreator.load(serverName);
            ResultHandle methodNameHandle = methodCreator.load(mcpMethodName);
            
            ResultHandle result = methodCreator.invokeVirtualMethod(
                MethodDescriptor.ofMethod(MCPConnectionManager.class, "callMethod", 
                    CompletableFuture.class, String.class, String.class, Map.class),
                connectionManager, serverNameHandle, methodNameHandle, paramsMap);

            // TODO: Support async return types like Uni/Mono if needed
            if (CompletableFuture.class.isAssignableFrom(returnType)) {
                methodCreator.returnValue(result);
            } else {
                // Block and get result
                ResultHandle blockingResult = methodCreator.invokeVirtualMethod(
                    MethodDescriptor.ofMethod(CompletableFuture.class, "join", Object.class),
                    result);
                
                if (returnType != Object.class) {
                    blockingResult = methodCreator.checkCast(blockingResult, returnType);
                }
                methodCreator.returnValue(blockingResult);
            }
        } catch (Exception e) {
            // Log error in method generation
            System.err.println("[MCPClientExtension] Error generating method implementation for " + methodInfo.name() + ": " + e);
        }
    }

    // Safe class loading for build steps (avoids Class.forName at build time)
    private Class<?> safeConvertToJavaType(Type type) {
        switch (type.kind()) {
            case VOID:
                return void.class;
            case PRIMITIVE:
                return convertPrimitiveType(type.asPrimitiveType());
            case CLASS:
                try {
                    return Class.forName(type.name().toString(), false, Thread.currentThread().getContextClassLoader());
                } catch (ClassNotFoundException e) {
                    System.err.println("[MCPClientExtension] Warning: Could not load class " + type.name() + ", using Object.class");
                    return Object.class;
                }
            case ARRAY:
                return Object[].class; // Simplified
            case PARAMETERIZED_TYPE:
                try {
                    return Class.forName(type.asParameterizedType().name().toString(), false, Thread.currentThread().getContextClassLoader());
                } catch (ClassNotFoundException e) {
                    System.err.println("[MCPClientExtension] Warning: Could not load parameterized class " + type.asParameterizedType().name() + ", using Object.class");
                    return Object.class;
                }
            default:
                return Object.class;
        }
    }

    private Class<?> convertPrimitiveType(org.jboss.jandex.PrimitiveType primitiveType) {
        switch (primitiveType.primitive()) {
            case BOOLEAN:
                return boolean.class;
            case BYTE:
                return byte.class;
            case SHORT:
                return short.class;
            case INT:
                return int.class;
            case LONG:
                return long.class;
            case FLOAT:
                return float.class;
            case DOUBLE:
                return double.class;
            case CHAR:
                return char.class;
            default:
                return Object.class;
        }
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void configureMCPClients(
            MCPClientRecorder recorder,
            MCPRuntimeConfig config,
            BeanContainerBuildItem beanContainer) {
        recorder.initializeMCPConnections(config, beanContainer.getValue());
    }

    @BuildStep
    void registerProducers(BuildProducer<UnremovableBeanBuildItem> unremovableBeans) {
        // Ensure our producer classes are not removed
        unremovableBeans.produce(UnremovableBeanBuildItem.beanClassNames(
            MCPConnectionManager.class.getName(),
            MCPClientFactory.class.getName(),
            MCPClientProducer.class.getName()
        ));
    }
}


