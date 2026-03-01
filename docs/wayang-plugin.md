# Wayang Node Plugin Development Guide

Wayang allows developers to extend the platform with custom execution nodes via a dynamic plugin architecture. Nodes are distributed as standard Java JAR files and discovered automatically at runtime.

This guide explains how to create, build, and deploy a custom Node Plugin using the `NodeProvider` SPI.

## Prerequisites

- Java 17+
- Maven 3.8+
- Basic knowledge of JSON Schema (used for defining node configurations and data contracts)

## 1. Project Setup

Create a new Maven project and include the `wayang-plugin-spi` dependency.

```xml
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.yourcompany</groupId>
    <artifactId>my-wayang-plugin</artifactId>
    <version>1.0.0</version>

    <dependencies>
        <dependency>
            <groupId>tech.kayys.wayang</groupId>
            <artifactId>wayang-plugin-spi</artifactId>
            <version>1.0.0-SNAPSHOT</version> <!-- Match your Wayang version -->
            <scope>provided</scope>
        </dependency>
    </dependencies>
</project>
```

> [!NOTE]
> Set the scope of `wayang-plugin-spi` to `provided` so it isn't bundled inside your plugin JAR, as the Wayang host environment already provides it.

### 2.1 Defining the Configuration Schema

The best practice for defining the `configSchema` is to create a Java Record or POJO and use Wayang's `SchemaGeneratorUtils` to generate the JSON Schema automatically. This ensures your Java code and the UI configuration remain perfectly in sync.

First, add the generator dependencies to your `pom.xml`:

```xml
        <dependency>
            <groupId>tech.kayys.wayang</groupId>
            <artifactId>wayang-schema-core</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-annotations</artifactId>
            <version>2.15.2</version>
            <scope>provided</scope>
        </dependency>
```

Then, create your configuration record:

```java
package com.yourcompany.wayang.plugin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record MyNodeConfig(
    @JsonProperty(required = true)
    @JsonPropertyDescription("The API key for the external service.")
    String apiKey,

    @JsonProperty(defaultValue = "gpt-4")
    @JsonPropertyDescription("The model to use for generation.")
    String model
) {}
```

### 2.2 Implementing the NodeProvider

To expose your custom nodes to Wayang, create a class that implements the `tech.kayys.wayang.plugin.spi.node.NodeProvider` interface.

```java
package com.yourcompany.wayang.plugin;

import tech.kayys.wayang.plugin.spi.node.NodeDefinition;
import tech.kayys.wayang.plugin.spi.node.NodeProvider;
import tech.kayys.wayang.schema.generator.SchemaGeneratorUtils;

import java.util.List;
import java.util.Map;

public class MyCustomNodeProvider implements NodeProvider {

    @Override
    public List<NodeDefinition> nodes() {
        // Automatically generate the JSON Schema from your Record
        String configSchema = SchemaGeneratorUtils.generateSchema(MyNodeConfig.class);

        // Create the NodeDefinition
        NodeDefinition myNode = new NodeDefinition(
            "my-custom-node",           // Unique type ID
            "My Custom AI Node",        // Human-readable Label
            "AI Integration",           // Category in the UI
            "Text Generation",          // Sub-category
            "Connects to a custom AI model API.", // Description
            "cpu",                      // UI Icon (e.g., Lucide icon names)
            "#10B981",                  // UI Color (Hex)
            configSchema,               // Configuration schema
            "{}",                       // Input schema
            "{}",                       // Output schema
            Map.of("model", "gpt-4")    // Default configuration values
        );

        return List.of(myNode);
    }
}
```

### The `NodeDefinition` Record
The `NodeDefinition` is the core metadata object describing your node. It contains:
- **Identity & Presentation**: Type, label, category, description, icon, and color dictating how the node appears in the canvas UI.
- **Contracts**: JSON Schemas defining the structure of the node's settings (`configSchema`), incoming data (`inputSchema`), and outgoing data (`outputSchema`).

## 3. Implementing the Executor

While the `NodeProvider` tells the UI and validation engine *about* your node, the **Executor** actually runs the logic when the workflow executes. If you are building an executor for the local Java runtime (EIP pipeline or Gamelan Engine), you will implement the corresponding executor plugin interface (e.g., `ExecutorPlugin`).

Here is a simplified example of how an Executor maps the configuration provided by the UI back into your Java Record:

```java
package com.yourcompany.wayang.plugin;

import tech.kayys.gamelan.engine.plugin.ExecutorPlugin;
import tech.kayys.gamelan.engine.context.ExecutionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public class MyCustomExecutor implements ExecutorPlugin {
    
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String getSupportedNodeType() {
        return "my-custom-node"; // MUST match the type in your NodeDefinition
    }

    @Override
    public Object execute(ExecutionContext context) throws Exception {
        // 1. Extract the raw configuration map from the node
        Map<String, Object> rawConfig = context.getNode().getConfiguration();
        
        // 2. Deserialize it back into your strongly-typed Record
        MyNodeConfig config = MAPPER.convertValue(rawConfig, MyNodeConfig.class);
        
        // 3. Execute your business logic
        System.out.println("Executing with API Key: " + config.apiKey());
        System.out.println("Using model: " + config.model());
        
        String result = callExternalApi(config.apiKey(), config.model(), context.getInput());
        
        // 4. Return the output for the next node in the pipeline
        return Map.of("response", result);
    }
    
    private String callExternalApi(String key, String model, Object input) {
        // ... external API call logic ...
        return "Generated response from external API";
    }
}
```

## 3. Registering the SPI

Wayang uses Java's standard `ServiceLoader` mechanism to discover plugins. 

1. Create a `META-INF/services/` directory in your `src/main/resources/` folder.
2. Inside it, create a file named exactly: `tech.kayys.wayang.plugin.spi.node.NodeProvider`
3. The content of this file must be the fully qualified class name of your provider:

**`src/main/resources/META-INF/services/tech.kayys.wayang.plugin.spi.node.NodeProvider`**
```text
com.yourcompany.wayang.plugin.MyCustomNodeProvider
```

## 4. Building and Deployment

Build your plugin into a standard JAR file:

```bash
mvn clean package
```

### Deploying the Plugin

Wayang dynamically loads external plugins from a specific directory on your filesystem.

1. Locate the built JAR file (e.g., `target/my-wayang-plugin-1.0.0.jar`).
2. Copy the JAR file into the `~/.wayang/plugins/` directory:

```bash
mkdir -p ~/.wayang/plugins
cp target/my-wayang-plugin-1.0.0.jar ~/.wayang/plugins/
```

### Runtime Discovery

When the Wayang platform starts, the `FileNodePluginLoader` automatically:
1. Scans `~/.wayang/plugins/*.jar`.
2. Creates an isolated `PluginClassLoader` for each JAR.
3. Registers your `NodeProvider` and injects its schemas into the `BuiltinSchemaCatalog`.
4. Your node will immediately appear in the Wayang UI and be available for use in workflows.

## Troubleshooting

- **Node doesn't appear**: Verify the `META-INF/services` file is named correctly and contains the exact package path. Check the Wayang application logs for `FileNodePluginLoader` errors.
- **Dependency Issues**: If your plugin has custom external libraries (e.g., a specific database driver), build an "uber-jar" or "fat-jar" using the Maven Shade Plugin to bundle those libraries into your final JAR, ensuring they are available to the `PluginClassLoader`.
