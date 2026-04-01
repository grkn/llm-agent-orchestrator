# OrchestrationLLMs

A Java framework for building **multi-agent LLM orchestration systems** using finite state machines (FSM). This project enables coordinated workflows where multiple specialized AI agents communicate, collaborate, and delegate tasks to accomplish complex goals.

## Overview

OrchestrationLLMs implements a sophisticated agent-based architecture where:
- **Multiple AI agents** represent different roles or capabilities (e.g., Product Owner, Architect, Developer, Tester)
- **Agents communicate** by sending messages through a finite state machine
- **LLM integration** powers agent decision-making and actions
- **Tool execution** allows agents to perform concrete actions (file I/O, build commands, etc.)
- **Flexible workflows** enable both sequential and parallel task execution

## Key Features

### 🤖 Multi-Agent System
- Define specialized agents with custom prompts and behaviors
- Agent-to-agent communication and delegation
- State-based workflow management with FSM

### 🔄 Action Strategies
- **Sequential tool execution**: Run dependent tools one after another
- **Parallel tool execution**: Execute multiple tools concurrently
- **Agent delegation**: Ask other agents for help via `ASK_AGENT`
- **Human interaction**: Request input with `ASK_QUESTION`
- **Task finalization**: Complete workflows with `FINALIZE_TASK`

### 🛠️ Tool Integration
Built-in tools for common development tasks:
- File operations (read, write, delete, list, search)
- Maven build execution (`mvn clean install`)
- Extensible tool system via annotations (`@Tool`, `@ToolParameter`)

### 🏗️ Clean Architecture
- **Strategy pattern** for action handling
- **Builder pattern** for agent configuration
- **FSM pattern** for workflow orchestration
- Lifecycle hooks (`onEnter`, `onExit`) for agent state transitions

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    AgentOrchestrator                        │
│  - Manages overall workflow                                 │
│  - Enforces iteration limits                                │
│  - Monitors task completion                                 │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                  AgentStateMachine                          │
│  - Tracks current agent state                               │
│  - Enforces transition rules                                │
│  - Routes messages between agents                           │
└────────────────────┬────────────────────────────────────────┘
                     │
       ┌─────────────┼─────────────┐
       ▼             ▼             ▼
   ┌───────┐    ┌───────┐    ┌───────┐
   │Agent 1│    │Agent 2│    │Agent N│
   │(PO)   │    │(Dev)  │    │(QA)   │
   └───┬───┘    └───┬───┘    └───┬───┘
       │            │            │
       └────────────┼────────────┘
                    ▼
         ┌──────────────────────┐
         │  ActionStrategy      │
         │  - Parallel          │
         │  - Sequential        │
         │  - AskAgent          │
         │  - FinalizeTask      │
         └──────────────────────┘
```

## Installation

### Prerequisites
- Java 21 or higher
- Maven 3.6+
- OpenAI-compatible LLM API endpoint

### Dependencies

```xml
<dependency>
    <groupId>com.grkn</groupId>
    <artifactId>OrchestrationLLMs</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

This project depends on:
- `ToolLibrary` (1.0-SNAPSHOT) - Tool management and reflection utilities
- `jackson-databind` (3.1.0) - JSON processing

## Quick Start

### 1. Define Agents

```java
Agent productOwner = AgentBuilder.create("product_owner")
    .prompt("""
        You are a senior technical product owner.
        Analyze requirements and divide them into sub-tasks.
        Coordinate with architect and developer agents.
        """)
    .onEnter(context -> {
        System.out.println("[Product Owner] Analyzing requirements...");
    })
    .build();

Agent developer = AgentBuilder.create("developer")
    .toolInstance(new Tools())  // Provide file/build tools
    .prompt("""
        You are an expert Java developer.
        Implement requirements using available tools.
        Ask architect for technical guidance when needed.
        Repository path: C:\\repo
        """)
    .onEnter(context -> {
        System.out.println("[Developer] Starting implementation...");
    })
    .build();

Agent architect = AgentBuilder.create("architect")
    .prompt("""
        You are a senior software architect.
        Design technical architecture for implementations.
        Answer technical questions from other agents.
        """)
    .build();
```

### 2. Configure Orchestrator

```java
AgentOrchestrator orchestrator = AgentOrchestrator.builder(
        Arrays.asList(productOwner, developer, architect)
    )
    .initialAgent("product_owner")
    .addTransition("product_owner", "developer")
    .addTransition("product_owner", "architect")
    .addTransition("developer", "architect")
    .addTransition("architect", "developer")
    .allowRecursive("developer")  // Developer can call itself
    .addTransitionListener((from, to) ->
        System.out.printf("Transition: %s → %s%n", from.getName(), to.getName())
    )
    .maxIterations(100)
    .enableLogging(true)
    .build();
```

### 3. Execute Workflow

```java
ApiResponse result = orchestrator.process();
System.out.println("Final Result: " + result.getAnswer());
```

## Agent Action Format

Agents communicate using structured JSON responses:

```json
{
  "action": "RUN_TOOL_SEQUENTIAL | RUN_TOOL_PARALLEL | ASK_QUESTION | ASK_AGENT | FINALIZE_TASK",
  "toolNames": ["WRITE_FILE", "MVN_CLEAN_INSTALL"],
  "inputs": [
    {"absolutePath": "C:\\repo\\Main.java", "content": "public class Main {}"},
    {"path": "C:\\repo"}
  ],
  "answer": "Implementation completed successfully",
  "agentName": "architect",
  "toolOutput": "{\"result\": \"success\"}"
}
```

### Available Actions

| Action | Description | Required Fields |
|--------|-------------|----------------|
| `RUN_TOOL_SEQUENTIAL` | Execute tools one by one, passing results forward | `toolNames`, `inputs` |
| `RUN_TOOL_PARALLEL` | Execute multiple tools concurrently | `toolNames`, `inputs` |
| `ASK_AGENT` | Delegate task to another agent | `agentName`, `answer` |
| `FINALIZE_TASK` | Complete the workflow | `answer` |

## Built-in Tools

The `Tools` class provides essential file and build operations:

| Tool | Description | Parameters |
|------|-------------|-----------|
| `READ_FILE` | Read file contents | `path` (String) |
| `WRITE_FILE` | Create/overwrite file | `WritePayload` (path, content) |
| `REWRITE_FILE` | Update existing file | `WritePayload` (path, content) |
| `DELETE_FILE` | Delete file | `path` (String) |
| `LIST_FILE` | List files recursively | `path` (String) |
| `SEARCH_PATTERN_IN_FILE` | Find pattern in file | `SearchPatternPayload` (path, pattern) |
| `MVN_CLEAN_INSTALL` | Run Maven build | `path` (String) |

### Creating Custom Tools

```java
@Tool
public class CustomTools {

    @Tool(name = "DEPLOY_APP", description = "Deploy application to server")
    public String deploy(
        @ToolParameter(description = "Deployment environment")
        String environment
    ) {
        // Implementation
        return "Deployed to " + environment;
    }
}

// Use in agent
Agent devOps = AgentBuilder.create("devops")
    .toolInstance(new CustomTools())
    .prompt("You are a DevOps engineer...")
    .build();
```

## Configuration

Set up LLM connection via `Properties` class:

```java
// Configure in Properties.INSTANCE or environment variables
Properties.INSTANCE.setBaseUrl("https://api.openai.com/v1/chat/completions");
Properties.INSTANCE.setOpenAIKey("your-api-key");
Properties.INSTANCE.setOpenAIModel("gpt-4");
```

## Example: Software Development Workflow

See `OrchestrationExample.java` for a complete example that orchestrates:

1. **Product Owner** - Analyzes requirements, breaks down tasks
2. **Architect** - Designs technical architecture
3. **Developer** - Implements code using file tools
4. **Tester** - Writes and executes tests

The workflow demonstrates:
- Cross-agent communication (Developer ↔ Architect)
- Tool execution (file I/O, Maven builds)
- Recursive agent calls (Developer calling itself for multi-step tasks)
- Task delegation and finalization

## Advanced Features

### State Machine Transitions

```java
// Define allowed transitions
orchestrator
    .addTransition("agent_a", "agent_b")  // A can route to B
    .addTransition("agent_b", "agent_c")  // B can route to C
    .allowRecursive("agent_a");           // A can call itself

// Attempting invalid transitions throws IllegalStateException
```

### Context Sharing

```java
Agent analyst = AgentBuilder.create("analyst")
    .onEnter(context -> {
        // Access shared state
        String data = context.getState("analysis_data");

        // Store results
        context.setState("findings", results);
    })
    .build();
```

### Transition Listeners

```java
orchestrator.addTransitionListener((from, to) -> {
    logger.info("Workflow transition: {} -> {}",
        from.getName(), to.getName());

    // Custom logic (metrics, notifications, etc.)
});
```

## Best Practices

### 1. Clear Agent Responsibilities
Define specific roles for each agent to avoid confusion:
```java
// ✅ Good - Focused responsibility
Agent codeReviewer = AgentBuilder.create("reviewer")
    .prompt("Review code for quality, security, and best practices")
    .build();

// ❌ Avoid - Too broad
Agent generalAgent = AgentBuilder.create("general")
    .prompt("Do everything related to development")
    .build();
```

### 2. Explicit Transition Rules
Define clear transition paths to prevent invalid state changes:
```java
// Define explicit workflow
builder
    .initialAgent("planner")
    .addTransition("planner", "implementer")
    .addTransition("implementer", "tester")
    .addTransition("tester", "implementer")  // Allow back for bug fixes
    .addTransition("tester", "planner");     // Final review
```

### 3. Set Reasonable Iteration Limits
Prevent infinite loops while allowing complex tasks:
```java
orchestrator
    .maxIterations(50)  // Adjust based on task complexity
    .build();
```

### 4. Use Tool Instances for Stateful Operations
```java
// Stateful tools can maintain context
class DatabaseTools {
    private Connection connection;

    @Tool(name = "QUERY_DB")
    public String query(String sql) { /* ... */ }
}

Agent dataAgent = AgentBuilder.create("data_agent")
    .toolInstance(new DatabaseTools())
    .build();
```

## Troubleshooting

### Common Issues

**Issue**: `IllegalStateException: Transition not allowed`
- **Solution**: Ensure transitions are registered with `.addTransition(from, to)`

**Issue**: `Max iterations reached`
- **Solution**: Increase `.maxIterations()` or check for agent logic loops

**Issue**: `No strategy found for action`
- **Solution**: Verify agent returns valid action: `RUN_TOOL_SEQUENTIAL`, `RUN_TOOL_PARALLEL`, `ASK_AGENT`, `ASK_QUESTION`, or `FINALIZE_TASK`

**Issue**: LLM returns malformed JSON
- **Solution**: Improve agent prompts to emphasize JSON format requirements. The framework uses permissive JSON parsing with single quotes allowed.

## Limitations

- **Iteration limit**: Default 100 iterations per workflow (configurable)
- **Synchronous execution**: Agent transitions happen sequentially
- **No persistence**: State is in-memory only (implement custom `AgentContext` for persistence)
- **LLM dependency**: Requires OpenAI-compatible API endpoint

## Roadmap

- [ ] Add async/reactive agent execution
- [ ] Implement state persistence layer
- [ ] Support for multiple LLM providers
- [ ] Visual workflow designer
- [ ] Agent performance metrics and monitoring
- [ ] Comprehensive test suite

## Contributing

Contributions are welcome! Areas for improvement:
- Additional built-in tools
- Enhanced error recovery mechanisms
- Support for streaming LLM responses
- Agent behavior testing utilities

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Credits

Developed by [@grkn](https://github.com/grkn)

## Support

For issues, questions, or feature requests, please open an issue on the GitHub repository.

---

**Note**: This framework is designed for orchestrating complex workflows with LLM-powered agents. Ensure proper API key management and rate limiting when deploying to production environments.
