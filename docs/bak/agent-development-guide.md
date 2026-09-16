# Agent Development Guide

Wayang's architecture ensures that the core platform is completely decoupled from specific agent behaviors. You can build new agents as standard Java modules and plug them into the framework without modifying Wayang's core.

## 1. Project Setup
Create a new Maven or Gradle module (e.g., `wayang-agent-react`).
Add the `wayang-spi` as a provided or compile dependency.

```xml
<dependency>
    <groupId>tech.kayys.wayang</groupId>
    <artifactId>wayang-spi</artifactId>
    <version>${wayang.version}</version>
    <scope>provided</scope>
</dependency>
```

## 2. Implement the Agent SPI
Your agent must implement the `tech.kayys.wayang.spi.agent.Agent` interface.

```java
package tech.kayys.wayang.agent.react;

import tech.kayys.wayang.spi.agent.Agent;
import tech.kayys.wayang.spi.agent.AgentPipeline;
import tech.kayys.wayang.provider.ProviderStrategy;
import tech.kayys.wayang.provider.ChatMessage;
import tech.kayys.wayang.provider.ToolSpec;
import tech.kayys.wayang.core.WayangKernel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ReActAgent implements Agent {

    @Inject
    WayangKernel kernel; // Access to registries and inference providers

    private ProviderStrategy llmProvider;
    private List<ToolSpec> availableTools;

    @Override
    public String getId() {
        return "react-agent";
    }

    @Override
    public AgentPipeline getPipeline() {
        return new ReActPipeline(llmProvider, availableTools);
    }

    @Override
    public void initialize() throws Exception {
        // 1. Fetch the default LLM provider (e.g., Gollek)
        this.llmProvider = kernel.getProviderRegistry().getDefaultProvider();
        
        // 2. Load allowed tools from the Skill Registry
        this.availableTools = kernel.getSkillRegistry().getToolsForProfile("react-profile");
        
        System.out.println("ReAct Agent initialized with " + availableTools.size() + " tools.");
    }

    @Override
    public Object process(Object request) throws Exception {
        String userQuery = (String) request;
        return getPipeline().execute(userQuery);
    }
}
```

### Building the ReAct Pipeline
The `AgentPipeline` interface allows you to encapsulate the complex step-by-step logic. Here is a complete implementation of the ReAct (Reasoning and Acting) loop.

```java
package tech.kayys.wayang.agent.react;

import tech.kayys.wayang.spi.agent.AgentPipeline;
import tech.kayys.wayang.provider.ProviderStrategy;
import tech.kayys.wayang.provider.ChatMessage;
import tech.kayys.wayang.provider.ToolSpec;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class ReActPipeline implements AgentPipeline {

    private final ProviderStrategy llm;
    private final List<ToolSpec> tools;
    private static final int MAX_ITERATIONS = 5;
    private static final String SYSTEM_PROMPT = 
        "You are a ReAct agent. Answer the user's question by alternating between Thought, Action, and Observation. " +
        "When you have the final answer, output 'Final Answer: [your answer]'.";

    public ReActPipeline(ProviderStrategy llm, List<ToolSpec> tools) {
        this.llm = llm;
        this.tools = tools;
    }

    @Override
    public Object execute(Object input) throws Exception {
        String query = (String) input;
        List<ChatMessage> conversation = new ArrayList<>();
        conversation.add(new ChatMessage(ChatMessage.Role.USER, query));

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            AtomicReference<String> modelOutput = new AtomicReference<>("");
            
            // 1. Call the LLM Provider
            llm.streamChat(conversation, SYSTEM_PROMPT, tools, 0.2, 1000, event -> {
                if (event instanceof StreamEvent.TextDelta) {
                    modelOutput.set(modelOutput.get() + ((StreamEvent.TextDelta) event).text());
                }
            });

            String response = modelOutput.get();
            conversation.add(new ChatMessage(ChatMessage.Role.ASSISTANT, response));

            // 2. Check for Final Answer
            if (response.contains("Final Answer:")) {
                return response.substring(response.indexOf("Final Answer:") + 13).trim();
            }

            // 3. Execute Tool Action (Simplified parsing)
            if (response.contains("Action:")) {
                String actionStr = extractAction(response); // e.g. "search(query='Tokyo weather')"
                String observation = executeTool(actionStr);
                
                // 4. Append Observation
                conversation.add(new ChatMessage(ChatMessage.Role.USER, "Observation: " + observation));
            }
        }
        
        return "Agent failed to reach a conclusion within " + MAX_ITERATIONS + " steps.";
    }

    private String extractAction(String response) {
        // Parse the requested action from the text
        return "parsed_action"; 
    }

    private String executeTool(String action) {
        // Route tool execution to the Wayang Tool SPI
        return "It is 75 degrees in Tokyo."; 
    }
}
```

## 3. Registering the Agent
Wayang's Kernel automatically scans for plugins. You can expose your agent in two ways:

### Method A: CDI (Recommended)
Annotate your class with `@ApplicationScoped` (from `jakarta.enterprise.context`) and include an empty `META-INF/beans.xml` in your `src/main/resources`. Wayang's Weld container will automatically discover and instantiate it.

### Method B: Java ServiceLoader
If you want to avoid CDI annotations, create a file at `src/main/resources/META-INF/services/tech.kayys.wayang.spi.agent.Agent`.
Inside the file, put the fully qualified name of your class:
```text
tech.kayys.wayang.agent.custom.MyCustomAgent
```

## 4. Using Your Agent
Once dropped into the Wayang classpath (or plugin directory), your agent is automatically registered in the `AgentRegistry`.

```java
Agent myAgent = wayangKernel.getAgentRegistry().getAgent("my-custom-agent");
Object result = myAgent.process(new UserRequest("Analyze this data."));
```

## 5. Context Management (`wayang-context`)

Wayang natively supports advanced context window management via the `wayang-context` module.

When building an agent, you can configure a `ContextCompiler` (such as the `BudgetedContextCompiler`) in the `AgentBuilder` or manually in your Agent implementation. This compiler automatically trims, summarizes, and prioritizes the chat history and system prompts to ensure they fit within the LLM's maximum token context window.

```java
AgentBuilder builder = new AgentBuilder(kernel)
    .withContextCompiler(new BudgetedContextCompiler(8000));
```
