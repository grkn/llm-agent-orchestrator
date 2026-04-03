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
- **Task finalization**: Complete workflows with `FINALIZE_TASK`

### 🛠️ Tool Integration
Built-in tools for common development tasks:
- File operations (CREATE_FILE, MODIFY_FILE, READ_FILE, DELETE_FILE, LIST_FILE, SEARCH_PATTERN_IN_FILE)
- Maven build execution (`MVN_CLEAN_INSTALL`)
- Extensible tool system via annotations (`@Tool`, `@ToolParameter`)
- **Real implementation enforcement**: Agents are prompted to write actual code, not pseudo-code or placeholders

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
Agent customer = AgentBuilder.create("customer")
    .prompt("""
        # Role
        You are a customer with business needs seeking a software solution.

        # Responsibilities
        - Communicate your business requirements and goals to the product_owner
        - Answer clarifying questions about project requirements
        - Provide clear, concise feedback on proposed solutions
        """)
    .onEnter(context -> {
        System.out.println("[Customer] Providing business requirements...");
    })
    .build();

Agent productOwner = AgentBuilder.create("product_owner")
    .prompt("""
        # Role
        You are a senior technical product owner responsible for bridging
        business needs and technical implementation.

        # Responsibilities
        - Analyze business requirements from the customer
        - Break down requirements into actionable tasks and user stories
        - Coordinate with architect for technical feasibility
        - Ensure developer understands implementation requirements
        """)
    .onEnter(context -> {
        System.out.println("[Product Owner] Analyzing requirements...");
    })
    .build();

Agent developer = AgentBuilder.create("developer")
    .toolInstance(new Tools())  // Provide file/build tools
    .prompt("""
        # Role
        You are an expert Java developer responsible for implementing features.

        # CRITICAL: YOU MUST WRITE ACTUAL CODE - NO SKIPPING ALLOWED
        - DO NOT skip implementation or just describe what needs to be done
        - YOU MUST use CREATE_FILE tool to write actual Java code files
        - YOU MUST implement complete, working code - not placeholders

        # Environment
        - Operating System: Windows
        - Repository Path: C:\\repo

        # Workflow
        1. Understand requirements from product_owner
        2. Consult architect for technical approach
        3. **ACTUALLY IMPLEMENT CODE** using CREATE_FILE/MODIFY_FILE tools
        4. Request code review from architect
        """)
    .onEnter(context -> {
        System.out.println("[Developer] Starting implementation...");
    })
    .build();

Agent architect = AgentBuilder.create("architect")
    .prompt("""
        # Role
        You are a senior software architect responsible for technical
        design and quality assurance.

        # Responsibilities
        - Design scalable, maintainable system architecture
        - Define technical approach, patterns, and best practices
        - Review code quality and architectural consistency
        - Guide developer on implementation strategies
        """)
    .build();

Agent tester = AgentBuilder.create("tester")
    .toolInstance(new Tools())
    .prompt("""
        # Role
        You are a senior software test engineer specializing in Java
        and end-to-end testing.

        # CRITICAL: YOU MUST WRITE ACTUAL TEST CODE
        - DO NOT skip test implementation or just describe test scenarios
        - YOU MUST use CREATE_FILE tool to write actual test files
        - YOU MUST implement complete Selenium + BDD test code

        # Environment
        - Repository Path: C:\\repo
        - Testing Framework: Selenium + BDD
        """)
    .build();
```

### 2. Configure Orchestrator

```java
List<Agent> agents = Arrays.asList(customer, productOwner, developer, architect, tester);

AgentOrchestrator orchestrator = AgentOrchestrator.builder(agents)
    .initialAgent("customer")
    // Customer transitions
    .addTransition("customer", "product_owner")
    .addTransition("customer", "developer")
    .addTransition("customer", "architect")
    // Product Owner transitions
    .addTransition("product_owner", "customer")
    .addTransition("product_owner", "architect")
    .addTransition("product_owner", "developer")
    .addTransition("product_owner", "tester")
    // Architect transitions
    .addTransition("architect", "customer")
    .addTransition("architect", "product_owner")
    .addTransition("architect", "developer")
    .addTransition("architect", "tester")
    // Developer transitions
    .addTransition("developer", "product_owner")
    .addTransition("developer", "architect")
    .addTransition("developer", "tester")
    .allowRecursive("developer")  // Developer can call itself for multi-step tasks
    // Tester transitions
    .addTransition("tester", "developer")
    .addTransition("tester", "product_owner")
    .addTransition("tester", "architect")
    .addTransitionListener((from, to) ->
        System.out.printf("Asking question from agent: %s To agent: %s%n",
            from.getName(), to.getName())
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

| Tool | Description | Parameters | Example Usage |
|------|-------------|-----------|---------------|
| `CREATE_FILE` | Create new file | `WritePayload` (absolutePath, content) | Creating Java classes, configs |
| `READ_FILE` | Read file contents | `absolutePath` (String) | Reading existing code |
| `MODIFY_FILE` | Update existing file | `WritePayload` (absolutePath, content) | Editing existing files |
| `DELETE_FILE` | Delete file | `absolutePath` (String) | Removing old files |
| `LIST_FILE` | List files recursively | `path` (String) | Exploring directory structure |
| `SEARCH_PATTERN_IN_FILE` | Find pattern in file | `SearchPatternPayload` (path, pattern) | Finding specific code |
| `MVN_CLEAN_INSTALL` | Run Maven build | `path` (String) | Building and testing project |

**Note**: Developer and Tester agents are configured with explicit instructions to use these tools to create actual implementation files, not just describe or plan what should be done.

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

## Example: E-Commerce Development Workflow

See `OrchestrationExample.java` for a complete example that orchestrates a full software development lifecycle:

### Agents in Action

1. **Customer** - Provides business requirements and answers clarification questions
2. **Product Owner** - Analyzes requirements, breaks down into user stories, coordinates the team
3. **Architect** - Designs technical architecture (Spring Boot, PostgreSQL, JWT auth, payment gateways)
4. **Developer** - Implements actual Java code (entities, repositories, services, controllers)
5. **Tester** - Writes end-to-end tests using Selenium and BDD approach

### Real-World Example Output

The framework successfully orchestrates an e-commerce website implementation:
- **Customer** specifies need for product catalog, shopping cart, user accounts, payment processing (Stripe/PayPal)
- **Product Owner** gathers requirements: 500 products across electronics/home appliances, 3-month timeline
- **Architect** designs Spring Boot REST API with PostgreSQL, JWT authentication, and payment gateway integration
- **Developer** creates complete implementation:
  - Entity classes (Category, Product, User) with JPA annotations
  - Repository interfaces extending JpaRepository
  - Service layer with business logic, validation, password encryption
  - REST controllers with CRUD endpoints
  - Configuration files (application.properties, pom.xml)
  - Main application class (EcommerceApplication)
- **Tester** validates implementation with BDD tests

### Workflow Features Demonstrated

- **Multi-agent collaboration**: Customer → Product Owner → Architect → Developer → Tester
- **Bidirectional communication**: Agents can ask clarifying questions and iterate
- **Tool execution**: Developer uses CREATE_FILE, MODIFY_FILE, READ_FILE, EXECUTE (Maven)
- **Actual code generation**: Framework produces real, compilable Java code (not pseudo-code)
- **Recursive calls**: Developer can call itself for multi-step implementations
- **Smart routing**: Agents know when to consult architect vs product owner
- **Task finalization**: Agents finalize when their work is complete (customer finalized at iteration 14, product_owner at 18)

### Complete Agent Transition Graph

```
Customer ⇄ Product Owner ⇄ Architect
                ↓              ↓
            Developer ⇄ Architect
                ↓              ↓
            Tester ⇄ Developer
```

All agents can communicate with each other as needed, enabling flexible collaboration patterns.

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

### 1. Clear Agent Responsibilities with Structured Prompts
Define specific roles using structured markdown prompts:
```java
// ✅ Good - Structured, focused responsibility with clear enforcement
Agent developer = AgentBuilder.create("developer")
    .toolInstance(new Tools())
    .prompt("""
        # Role
        You are an expert Java developer.

        # CRITICAL: YOU MUST WRITE ACTUAL CODE - NO SKIPPING ALLOWED
        - DO NOT skip implementation or just describe what needs to be done
        - YOU MUST use CREATE_FILE tool to write actual Java code files
        - YOU MUST implement complete, working code - not placeholders

        # Workflow
        1. Understand requirements
        2. Consult architect for design
        3. **ACTUALLY IMPLEMENT CODE** using tools
        4. Request code review
        """)
    .build();

// ❌ Avoid - Vague and too broad
Agent generalAgent = AgentBuilder.create("general")
    .prompt("Do everything related to development")
    .build();
```

**Key improvements in prompts**:
- Use markdown headers (# Role, # Responsibilities, # Workflow)
- Include CRITICAL sections with enforcement rules (prevents agents from skipping implementation)
- Specify exact tool names agents should use (CREATE_FILE, MODIFY_FILE, etc.)
- Define clear communication rules (when to ask which agent)

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

- [x] Multi-agent orchestration with FSM
- [x] Tool integration for file operations and Maven builds
- [x] Agent-to-agent communication and delegation
- [x] Structured prompt engineering with enforcement rules
- [x] Real-world e-commerce development example
- [ ] Retry logic and error recovery mechanisms
- [ ] Add async/reactive agent execution
- [ ] Implement state persistence layer
- [ ] Support for multiple LLM providers (Claude, Gemini, etc.)
- [ ] Visual workflow designer
- [ ] Agent performance metrics and monitoring
- [ ] Comprehensive test suite
- [ ] Web UI for monitoring agent interactions

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
