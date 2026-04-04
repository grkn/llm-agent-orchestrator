# LLM Agent Orchestrator

[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

> **A sophisticated Java framework for building multi-agent LLM orchestration systems powered by Finite State Machines**

Build intelligent, collaborative AI workflows where multiple specialized agents work together to solve complex tasks through structured communication, intelligent routing, and automated decision-making.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Architecture](#architecture)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Core Concepts](#core-concepts)
- [Agent Configuration](#agent-configuration)
- [Intelligent Routing](#intelligent-routing)
- [Transition Validation](#transition-validation)
- [Action Strategies](#action-strategies)
- [Tool System](#tool-system)
- [Context Management](#context-management)
- [Advanced Usage](#advanced-usage)
- [Complete Examples](#complete-examples)
- [Best Practices](#best-practices)
- [API Reference](#api-reference)
- [Troubleshooting](#troubleshooting)
- [Performance & Optimization](#performance--optimization)
- [Roadmap](#roadmap)
- [Contributing](#contributing)

---

## 🎯 Overview

**LLM Agent Orchestrator** is a production-ready Java framework that enables you to create sophisticated multi-agent systems where AI agents collaborate to accomplish complex tasks. Built on finite state machine principles, it provides:

- **Multi-agent collaboration** with clearly defined roles and responsibilities
- **Intelligent routing** using AI-powered decision making
- **Validation rules** to enforce workflow constraints
- **Tool integration** for real-world actions (file I/O, API calls, builds)
- **Context sharing** for stateful agent communication
- **Strategy patterns** for flexible action execution

### Use Cases

- **Software Development Workflows**: Orchestrate product owners, architects, developers, and testers
- **Content Generation Pipelines**: Coordinate researchers, writers, editors, and reviewers
- **Data Processing**: Chain analysts, transformers, validators, and publishers
- **Customer Service**: Route inquiries through support tiers with escalation logic
- **DevOps Automation**: Coordinate build, test, deploy, and monitoring agents

---

## ✨ Key Features

### 🤖 Multi-Agent Orchestration

- **Specialized Agents**: Create agents with specific roles, capabilities, and tools
- **FSM-Based State Management**: Agents transition through well-defined states
- **Agent Communication**: Agents can ask questions and delegate tasks to each other
- **Lifecycle Hooks**: Execute custom logic on agent entry/exit with `onEnter()` and `onExit()`
- **Finalization Tracking**: Agents signal completion when their work is done

### 🧠 Intelligent Routing System

- **AI-Powered Decisions**: LLM analyzes tasks and automatically selects the best agent
- **Context-Aware Routing**: Considers agent capabilities, workflow history, and current state
- **Confidence Scoring**: Routing decisions include confidence levels and reasoning
- **Fallback Mechanisms**: Graceful handling when optimal routing is unclear

### 🛡️ Transition Validation

- **Pre-built Rules**: 10+ validation rules for common scenarios (finalization, recursion, limits)
- **Custom Validators**: Implement complex business logic with custom validation functions
- **Strict Mode**: Enforce or warn on validation failures
- **Rule Composition**: Combine validators with AND/OR logic

### 🔄 Flexible Action Strategies

- **Sequential Execution**: Run tools one after another with output chaining
- **Parallel Execution**: Execute multiple tools concurrently for performance
- **Agent Delegation**: Route sub-tasks to specialized agents
- **Task Finalization**: Signal completion with results

### 🛠️ Tool Integration

- **Annotation-Based**: Define tools with `@Tool` and `@ToolParameter` annotations
- **Built-in Tools**: File operations, Maven builds, and more
- **Stateful Tools**: Tool instances can maintain state across calls
- **Type-Safe Parameters**: Automatic parameter extraction and validation
- **Tool Discovery**: Automatic detection via reflection

### 📊 Context Management

- **Shared State**: Store and retrieve data across agent transitions
- **Metadata Tracking**: Iteration counts, finalized agents, routing history
- **State Machine Access**: Agents can query available transitions and agents
- **Per-Agent State**: Maintain agent-specific state (e.g., LLM conversation IDs)

---

## 🏗️ Architecture

### System Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         AgentOrchestrator                               │
│  • Manages workflow lifecycle and iteration control                     │
│  • Tracks finalized agents and completion status                        │
│  • Enforces maximum iteration limits                                    │
│  • Coordinates message passing between agents                           │
└──────────────────────────┬──────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                       AgentStateMachine                                 │
│  • Current agent state tracking                                         │
│  • Transition rule enforcement                                          │
│  • Message routing between agents                                       │
│  • Integration point for routing and validation                         │
└────────────┬────────────────────────────────────────────────────────────┘
             │
             ├──────────────────────────┬─────────────────────────────────┐
             ▼                          ▼                                 ▼
  ┌────────────────────┐    ┌─────────────────────────┐    ┌──────────────────────┐
  │ TransitionAgent    │    │ IntelligentTransition   │    │  TransitionListener  │
  │  (Validation)      │    │  Agent (Smart Router)   │    │   (Observability)    │
  ├────────────────────┤    ├─────────────────────────┤    ├──────────────────────┤
  │ • Rule validation  │    │ • LLM-powered routing   │    │ • Audit logging      │
  │ • Custom validators│    │ • Context analysis      │    │ • Metrics collection │
  │ • Strict/warn mode │    │ • Capability matching   │    │ • Event publishing   │
  │ • Deny reasons     │    │ • Confidence scoring    │    │ • Notifications      │
  └────────────────────┘    └─────────────────────────┘    └──────────────────────┘
             │
             ▼
    ┌────────┴────────┬────────────────┬────────────┐
    ▼                 ▼                ▼            ▼
┌─────────┐      ┌─────────┐     ┌─────────┐  ┌─────────┐
│ Agent A │◄────►│ Agent B │◄───►│ Agent C │  │ Agent N │
│  (PO)   │      │  (Dev)  │     │  (QA)   │  │  (...)  │
└────┬────┘      └────┬────┘     └────┬────┘  └────┬────┘
     │                │               │            │
     │    ┌───────────┴───────────────┴────────────┘
     │    │
     ▼    ▼
┌─────────────────────────────────────────────────────────┐
│                   AgentContext                          │
│  • Shared state storage (key-value pairs)               │
│  • State machine reference for queries                  │
│  • Metadata (iteration count, finalized agents)         │
│  • Per-agent state isolation                            │
└─────────────────┬───────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────────┐
│                 Message Processing                      │
│                                                         │
│  1. Agent.process(message, context)                    │
│  2. Build LLM prompt with role + tools + context       │
│  3. Execute LLM with retry on invalid actions          │
│  4. Parse action (SEQUENTIAL, PARALLEL, ASK, FINALIZE) │
│  5. Select and execute ActionStrategy                  │
│  6. Agent.shouldTransition(message, context)           │
│  7. Validate or route to next agent                    │
└─────────────────┬───────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────────┐
│               ActionStrategy (Strategy Pattern)         │
│                                                         │
│  ┌─────────────────┐  ┌─────────────────┐             │
│  │  Sequential     │  │   Parallel      │             │
│  │  • Run tools    │  │   • Concurrent  │             │
│  │    one by one   │  │     execution   │             │
│  │  • Chain output │  │   • Collect all │             │
│  └─────────────────┘  └─────────────────┘             │
│                                                         │
│  ┌─────────────────┐  ┌─────────────────┐             │
│  │   AskAgent      │  │  FinalizeTask   │             │
│  │  • Route to     │  │   • Signal      │             │
│  │    other agent  │  │     completion  │             │
│  │  • Pass message │  │   • Return      │             │
│  └─────────────────┘  └─────────────────┘             │
└─────────────────┬───────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────────┐
│                  Tool Execution Layer                   │
│                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │ File Tools   │  │ Build Tools  │  │ Custom Tools │ │
│  │ • CREATE     │  │ • Maven      │  │ • HTTP API   │ │
│  │ • READ       │  │ • Gradle     │  │ • Database   │ │
│  │ • MODIFY     │  │ • npm        │  │ • Cloud SDK  │ │
│  │ • DELETE     │  │ • Docker     │  │ • Anything!  │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### Component Responsibilities

| Component | Purpose | Key Methods |
|-----------|---------|-------------|
| **AgentOrchestrator** | Workflow coordinator | `process()`, `builder()` |
| **AgentStateMachine** | FSM state manager | `transitionTo()`, `processMessage()`, `determineNextAgent()` |
| **Agent** | Specialized AI worker | `process()`, `shouldTransition()`, `onEnter()`, `onExit()` |
| **IntelligentTransitionAgent** | AI-powered router | `determineNextAgent()` |
| **TransitionAgent** | Rule-based validator | `validateTransition()` |
| **TransitionValidator** | Validation logic | `validate()` |
| **TransitionRule** | Pre-built rules | Static factory methods |
| **AgentContext** | Shared state | `setState()`, `getState()`, `getStateMachine()` |
| **ActionStrategy** | Execution logic | `execute()` |
| **Message** | Agent communication | `builder()`, `getPayload()`, `getMetadata()` |

---

## 📦 Installation

### Prerequisites

- **Java 21+** (uses modern Java features like text blocks, records, pattern matching)
- **Maven 3.6+** for dependency management and building
- **LLM API Access** (OpenAI GPT-4, Claude, or compatible endpoint)

### Maven Dependency

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>com.grkn</groupId>
    <artifactId>OrchestrationLLMs</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### Required Dependencies

The framework automatically includes:

```xml
<dependencies>
    <!-- Tool management and reflection utilities -->
    <dependency>
        <groupId>com.grkn</groupId>
        <artifactId>ToolLibrary</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>

    <!-- JSON processing -->
    <dependency>
        <groupId>tools.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>3.1.0</version>
    </dependency>
</dependencies>
```

### Configuration

Set up LLM connection (environment variables or Properties):

```java
// Option 1: Environment variables
export OPENAI_API_KEY="sk-..."
export OPENAI_BASE_URL="https://api.openai.com/v1/chat/completions"
export OPENAI_MODEL="gpt-4"

// Option 2: Programmatic configuration
Properties.INSTANCE.setOpenAIKey("sk-...");
Properties.INSTANCE.setBaseUrl("https://api.openai.com/v1/chat/completions");
Properties.INSTANCE.setOpenAIModel("gpt-4");
```

---

## 🚀 Quick Start

### Minimal Example (3 Steps)

```java
import com.grkn.orchestration.llms.fsm.*;
import com.grkn.orchestration.llms.orchestrator.AgentOrchestrator;
import java.util.List;

public class QuickStartExample {
    public static void main(String[] args) throws Exception {
        // Step 1: Create agents
        Agent planner = AgentBuilder.create("planner")
            .prompt("You plan tasks and break them into steps.")
            .build();

        Agent executor = AgentBuilder.create("executor")
            .prompt("You execute tasks and report results.")
            .build();

        // Step 2: Configure orchestrator
        AgentOrchestrator orchestrator = AgentOrchestrator.builder(List.of(planner, executor))
            .initialAgent("planner")
            .addTransition("planner", "executor")
            .addTransition("executor", "planner")
            .maxIterations(50)
            .build();

        // Step 3: Run workflow
        var result = orchestrator.process();
        System.out.println("Result: " + result.getAnswer());
    }
}
```

---

## 💡 Core Concepts

### 1. Agents

**Agents** are autonomous AI entities with:
- **Role**: Specialized responsibility (e.g., "developer", "tester")
- **Prompt**: Instructions defining behavior and capabilities
- **Tools**: Actions the agent can perform (file I/O, API calls)
- **Lifecycle**: `onEnter()` and `onExit()` hooks

```java
Agent developer = AgentBuilder.create("developer")
    .prompt("""
        # Role: Senior Java Developer

        # Responsibilities:
        - Implement features according to requirements
        - Write clean, maintainable code
        - Request code reviews from architect

        # Tools Available:
        You have access to file creation, modification, and Maven builds.
        """)
    .toolInstance(new DevelopmentTools())
    .onEnter(context -> System.out.println("Developer starting work..."))
    .onExit(context -> System.out.println("Developer finished."))
    .build();
```

### 2. Finite State Machine

The **AgentStateMachine** manages:
- **Current State**: Which agent is active
- **Transitions**: Allowed agent-to-agent routing
- **Messages**: Communication between agents
- **Validation**: Rule enforcement

```java
AgentStateMachine stateMachine = new AgentStateMachine();
stateMachine.registerAgent(agentA);
stateMachine.registerAgent(agentB);
stateMachine.addTransition("agentA", "agentB");
stateMachine.setInitialAgent("agentA");
```

### 3. Messages

**Messages** carry data between agents:

```java
Message message = Message.builder("targetAgent")
    .sender("sourceAgent")
    .payload(apiResponse)
    .metadata("iteration", 5)
    .metadata("priority", "high")
    .build();
```

### 4. Actions

Agents respond with **Actions**:
- `RUN_TOOL_SEQUENTIAL`: Execute tools one by one
- `RUN_TOOL_PARALLEL`: Execute tools concurrently
- `ASK_AGENT`: Delegate to another agent
- `FINALIZE_TASK`: Signal completion

### 5. Context

**AgentContext** provides shared state:

```java
// Store data
context.setState("user_requirements", requirements);

// Retrieve data
String reqs = (String) context.getState("user_requirements");

// Access state machine
AgentStateMachine sm = context.getStateMachine();
```

---

## 🎨 Agent Configuration

### Basic Agent

```java
Agent basicAgent = AgentBuilder.create("basic")
    .prompt("You are a helpful assistant.")
    .build();
```

### Agent with Tools

```java
Agent toolAgent = AgentBuilder.create("worker")
    .prompt("You perform file operations.")
    .toolInstance(new FileTools())
    .build();
```

### Agent with Lifecycle Hooks

```java
Agent lifecycleAgent = AgentBuilder.create("lifecycle")
    .prompt("You coordinate workflows.")
    .onEnter(context -> {
        System.out.println("Agent activated");
        context.setState("start_time", System.currentTimeMillis());
    })
    .onExit(context -> {
        long duration = System.currentTimeMillis() -
            (Long) context.getState("start_time");
        System.out.println("Agent ran for " + duration + "ms");
    })
    .build();
```

### Custom Agent Implementation

```java
public class CustomAgent extends AbstractAgent {
    public CustomAgent() {
        super("custom", toolInstance, "Custom agent prompt");
    }

    @Override
    public Message process(Message message, AgentContext context) throws Exception {
        // Custom processing logic
        ApiResponse response = /* ... */;
        message.setPayload(response);
        return message;
    }

    @Override
    public String shouldTransition(Message message, AgentContext context) {
        // Custom routing logic
        if (message.getPayload().getAction().equals("ASK_AGENT")) {
            return message.getPayload().getAgentName();
        }
        return this.getName(); // Stay with current agent
    }
}
```

---

## 🧠 Intelligent Routing

### Overview

The **IntelligentTransitionAgent** uses LLM to analyze tasks and automatically route to the best agent based on:
- Current task requirements
- Agent capabilities and roles
- Workflow history and context
- Finalized agents status

### Basic Usage

```java
// Create intelligent router
IntelligentTransitionAgent router = IntelligentTransitionAgent.builder()
    .enableValidation(true)
    .build();

// Attach to state machine
orchestrator.getStateMachine().setIntelligentTransitionAgent(router);

// Now routing happens automatically!
```

### With Validation Rules

```java
IntelligentTransitionAgent router = IntelligentTransitionAgent.builder()
    .addValidator(TransitionRule.maxTransitions(20))  // Prevent loops
    .addValidator(TransitionRule.noRecursion())       // No self-calls
    .enableValidation(true)
    .build();
```

### Custom LLM Client

```java
IntelligentTransitionAgent router = IntelligentTransitionAgent.builder()
    .client(myCustomLLMClient)  // Use custom client
    .enableValidation(true)
    .build();
```

### How It Works

1. **Analyzes** the current message and task
2. **Reviews** all available agents and their capabilities
3. **Considers** allowed transitions and context state
4. **Queries** LLM for intelligent routing decision
5. **Returns** decision with target agent, reasoning, and confidence

### Routing Decision Format

```java
TransitionDecision decision = router.determineNextAgent(currentAgent, message, context);

System.out.println("Target: " + decision.getTargetAgent());
System.out.println("Should transition: " + decision.shouldTransition());
System.out.println("Reasoning: " + decision.getReasoning());
System.out.println("Confidence: " + decision.getConfidence());
```

### Manual Usage

```java
// Use determineNextAgent in AgentStateMachine
String nextAgent = stateMachine.determineNextAgent(message);

// Or call directly
TransitionDecision decision = intelligentRouter.determineNextAgent(
    currentAgent, message, context
);

if (decision.shouldTransition()) {
    stateMachine.transitionTo(decision.getTargetAgent());
}
```

---

## 🛡️ Transition Validation

### Overview

**TransitionAgent** validates transitions against custom rules before execution, ensuring workflow integrity and preventing invalid state changes.

### Basic Validation

```java
TransitionAgent validator = TransitionAgent.builder()
    .addValidator(TransitionRule.requireFinalization())
    .strictMode(true)
    .build();

stateMachine.setTransitionAgent(validator);
```

### Pre-built Rules

#### 1. Require Finalization

Agent must finalize task before transitioning:

```java
.addValidator(TransitionRule.requireFinalization())
```

#### 2. Max Transitions

Limit number of transitions per agent:

```java
.addValidator(TransitionRule.maxTransitions(10))
```

#### 3. No Recursion

Prevent agents from calling themselves:

```java
.addValidator(TransitionRule.noRecursion())
```

#### 4. Allow Only To

Whitelist allowed target agents:

```java
.addValidator(TransitionRule.allowOnlyTo(Set.of("dev", "test", "qa")))
```

#### 5. Block From

Blacklist source agents:

```java
.addValidator(TransitionRule.blockFrom(Set.of("deprecated_agent")))
```

#### 6. Require Action

Require specific action before transition:

```java
.addValidator(TransitionRule.requireAction(Action.FINALIZE_TASK))
```

#### 7. Require Context State

Validate context conditions:

```java
.addValidator(TransitionRule.requireContextState("approved", true))
```

#### 8. Require Sender

Validate message sender:

```java
.addValidator(TransitionRule.requireSender("supervisor"))
```

### Combining Rules

#### AND Logic

All validators must pass:

```java
.addValidator(TransitionRule.and(
    TransitionRule.requireFinalization(),
    TransitionRule.maxTransitions(5),
    TransitionRule.requireAction(Action.FINALIZE_TASK)
))
```

#### OR Logic

At least one validator must pass:

```java
.addValidator(TransitionRule.or(
    TransitionRule.requireSender("admin"),
    TransitionRule.requireContextState("emergency", true)
))
```

### Custom Validators

Implement custom validation logic:

```java
TransitionValidator customValidator = (fromAgent, toAgent, message, context) -> {
    // Custom business logic
    if (fromAgent.getName().equals("junior") && toAgent.getName().equals("production")) {
        return TransitionValidator.TransitionValidationResult.deny(
            "Junior agents cannot deploy to production"
        );
    }

    // Check context conditions
    Integer errorCount = (Integer) context.getState("error_count");
    if (errorCount != null && errorCount > 3) {
        return TransitionValidator.TransitionValidationResult.deny(
            "Too many errors - cannot proceed"
        );
    }

    return TransitionValidator.TransitionValidationResult.allow();
};

TransitionAgent validator = TransitionAgent.builder()
    .addValidator(customValidator)
    .strictMode(true)
    .build();
```

### Strict vs. Warn Mode

```java
// Strict mode: Block invalid transitions
TransitionAgent strictValidator = TransitionAgent.builder()
    .addValidator(rules)
    .strictMode(true)  // Throws exception on failure
    .build();

// Warn mode: Log warnings but allow transitions
TransitionAgent warnValidator = TransitionAgent.builder()
    .addValidator(rules)
    .strictMode(false)  // Logs warning, allows transition
    .build();
```

---

## 🔄 Action Strategies

### Sequential Execution

Execute tools one after another, chaining outputs:

```json
{
  "action": "RUN_TOOL_SEQUENTIAL",
  "toolNames": ["READ_FILE", "MODIFY_FILE", "WRITE_FILE"],
  "inputs": [
    {"path": "/data/input.txt"},
    {"operation": "transform"},
    {"path": "/data/output.txt", "content": "..."}
  ]
}
```

**Use When:**
- Tools depend on previous outputs
- Order matters
- Sequential processing is required

### Parallel Execution

Execute multiple tools concurrently:

```json
{
  "action": "RUN_TOOL_PARALLEL",
  "toolNames": ["READ_FILE", "READ_FILE", "READ_FILE"],
  "inputs": [
    {"path": "/data/file1.txt"},
    {"path": "/data/file2.txt"},
    {"path": "/data/file3.txt"}
  ]
}
```

**Use When:**
- Tools are independent
- No dependencies between operations
- Performance is critical

### Agent Delegation

Route task to another agent:

```json
{
  "action": "ASK_AGENT",
  "agentName": "architect",
  "answer": "Please review this code for architectural issues"
}
```

**Use When:**
- Task requires specialized expertise
- Another agent has required capabilities
- Coordination is needed

### Task Finalization

Signal completion and provide results:

```json
{
  "action": "FINALIZE_TASK",
  "answer": "Implementation completed successfully. All tests passing."
}
```

**Use When:**
- Agent's work is complete
- Ready to move to next agent or finish
- Final results are available

---

## 🛠️ Tool System

### Built-in File Tools

```java
public class FileTools {
    @Tool(name = "CREATE_FILE", description = "Create a new file")
    public String createFile(
        @ToolParameter(description = "Absolute file path") String absolutePath,
        @ToolParameter(description = "File content") String content
    ) {
        // Implementation
        return "File created: " + absolutePath;
    }

    @Tool(name = "READ_FILE", description = "Read file contents")
    public String readFile(
        @ToolParameter(description = "Absolute file path") String absolutePath
    ) {
        // Implementation
        return Files.readString(Path.of(absolutePath));
    }

    @Tool(name = "MODIFY_FILE", description = "Modify existing file")
    public String modifyFile(
        @ToolParameter(description = "Absolute file path") String absolutePath,
        @ToolParameter(description = "New content") String content
    ) {
        // Implementation
        return "File modified: " + absolutePath;
    }

    @Tool(name = "DELETE_FILE", description = "Delete a file")
    public String deleteFile(
        @ToolParameter(description = "Absolute file path") String absolutePath
    ) {
        // Implementation
        Files.delete(Path.of(absolutePath));
        return "File deleted: " + absolutePath;
    }
}
```

### Built-in Build Tools

```java
public class BuildTools {
    @Tool(name = "MVN_CLEAN_INSTALL", description = "Run Maven clean install")
    public String mavenBuild(
        @ToolParameter(description = "Project directory") String path
    ) {
        // Execute Maven
        ProcessBuilder pb = new ProcessBuilder("mvn", "clean", "install");
        pb.directory(new File(path));
        // ... execute and return output
    }
}
```

### Custom Tools

```java
@Tool
public class CustomTools {

    @Tool(name = "SEND_EMAIL", description = "Send email notification")
    public String sendEmail(
        @ToolParameter(description = "Recipient email") String to,
        @ToolParameter(description = "Email subject") String subject,
        @ToolParameter(description = "Email body") String body
    ) {
        // Email implementation
        return "Email sent to " + to;
    }

    @Tool(name = "CALL_API", description = "Make HTTP API call")
    public String callApi(
        @ToolParameter(description = "API endpoint URL") String url,
        @ToolParameter(description = "HTTP method") String method,
        @ToolParameter(description = "Request body") String requestBody
    ) {
        // HTTP client implementation
        return "API response: " + response;
    }

    @Tool(name = "QUERY_DATABASE", description = "Execute SQL query")
    public String queryDatabase(
        @ToolParameter(description = "SQL query") String sql
    ) {
        // Database connection and query
        return "Query results: " + resultSet;
    }
}
```

### Stateful Tools

Tools can maintain state across calls:

```java
@Tool
public class StatefulDatabaseTools {
    private Connection connection;

    public StatefulDatabaseTools(String connectionString) {
        this.connection = DriverManager.getConnection(connectionString);
    }

    @Tool(name = "BEGIN_TRANSACTION", description = "Start database transaction")
    public String beginTransaction() {
        connection.setAutoCommit(false);
        return "Transaction started";
    }

    @Tool(name = "COMMIT", description = "Commit transaction")
    public String commit() {
        connection.commit();
        return "Transaction committed";
    }

    @Tool(name = "ROLLBACK", description = "Rollback transaction")
    public String rollback() {
        connection.rollback();
        return "Transaction rolled back";
    }
}
```

### Using Tools in Agents

```java
Agent developer = AgentBuilder.create("developer")
    .toolInstance(new FileTools())       // Single tool set
    .prompt("You are a developer...")
    .build();

Agent fullStack = AgentBuilder.create("fullstack")
    .toolInstance(new FileTools())        // Multiple tool sets
    .toolInstance(new BuildTools())       // via multiple instances
    .toolInstance(new DatabaseTools())
    .prompt("You are a full-stack developer...")
    .build();
```

---

## 📊 Context Management

### Basic State Operations

```java
// Store primitive values
context.setState("iteration_count", 5);
context.setState("user_name", "Alice");
context.setState("is_approved", true);

// Retrieve values
int count = (int) context.getState("iteration_count");
String name = (String) context.getState("user_name");
boolean approved = (boolean) context.getState("is_approved");

// Clear all state
context.clearState();
```

### Complex State Objects

```java
// Store collections
List<String> requirements = List.of("req1", "req2", "req3");
context.setState("requirements", requirements);

Map<String, Object> config = Map.of(
    "environment", "production",
    "timeout", 30000,
    "retries", 3
);
context.setState("config", config);

// Store custom objects
class UserProfile {
    String name;
    List<String> roles;
    Map<String, String> preferences;
}
UserProfile profile = new UserProfile();
context.setState("user_profile", profile);

// Retrieve
List<String> reqs = (List<String>) context.getState("requirements");
UserProfile user = (UserProfile) context.getState("user_profile");
```

### Per-Agent State

Agents can maintain their own isolated state:

```java
Agent agent = AgentBuilder.create("worker")
    .onEnter(context -> {
        // Agent-specific state with namespaced key
        String key = "worker-conversation-id";
        String conversationId = (String) context.getState(key);
        if (conversationId == null) {
            conversationId = UUID.randomUUID().toString();
            context.setState(key, conversationId);
        }
    })
    .build();
```

### Metadata in Messages

```java
// Create message with metadata
Message message = Message.builder("targetAgent")
    .payload(response)
    .metadata("iteration", iterationCount)
    .metadata("finalizedAgents", finalizedSet)
    .metadata("priority", "high")
    .metadata("deadline", Instant.now().plusHours(2))
    .build();

// Retrieve metadata
int iteration = (int) message.getMetadata().get("iteration");
Set<String> finalized = (Set<String>) message.getMetadata().get("finalizedAgents");
```

### State Machine Queries

```java
// Access state machine from context
AgentStateMachine sm = context.getStateMachine();

// Query available agents
Collection<Agent> allAgents = sm.getAllAgents();
Agent specific = sm.getAgent("developer");

// Query transitions
Set<String> allowedTransitions = sm.getTransitions("currentAgent");

// Get current state
Agent currentAgent = sm.getCurrentAgent();
Agent initialAgent = sm.getInitialAgent();
```

---

## 🚀 Advanced Usage

### Complete Workflow Example

```java
public class SoftwareDevelopmentWorkflow {
    public static void main(String[] args) throws Exception {
        // 1. Define agents with roles
        Agent productOwner = AgentBuilder.create("product_owner")
            .prompt("""
                # Role: Senior Product Owner
                # Responsibilities:
                - Gather and analyze requirements
                - Create user stories and acceptance criteria
                - Coordinate with architect and developer
                - Ensure business value is delivered
                """)
            .build();

        Agent architect = AgentBuilder.create("architect")
            .prompt("""
                # Role: Software Architect
                # Responsibilities:
                - Design system architecture
                - Define technical standards
                - Review code for architectural compliance
                - Guide implementation decisions
                """)
            .build();

        Agent developer = AgentBuilder.create("developer")
            .toolInstance(new FileTools())
            .toolInstance(new BuildTools())
            .prompt("""
                # Role: Senior Java Developer

                # CRITICAL RULES:
                - YOU MUST write actual code using CREATE_FILE tool
                - NO pseudo-code or placeholders allowed
                - Implement complete, working solutions

                # Responsibilities:
                - Implement features based on requirements
                - Write clean, tested code
                - Request code reviews from architect
                - Fix bugs and issues
                """)
            .build();

        Agent tester = AgentBuilder.create("tester")
            .toolInstance(new FileTools())
            .toolInstance(new BuildTools())
            .prompt("""
                # Role: Senior QA Engineer

                # CRITICAL RULES:
                - YOU MUST write actual test code
                - Create comprehensive test suites
                - No skipping test implementation

                # Responsibilities:
                - Write and execute tests
                - Report bugs to developer
                - Validate fixes
                - Ensure quality standards
                """)
            .build();

        // 2. Configure intelligent routing
        IntelligentTransitionAgent router = IntelligentTransitionAgent.builder()
            .addValidator(TransitionRule.maxTransitions(30))
            .enableValidation(true)
            .build();

        // 3. Configure validation rules
        TransitionAgent validator = TransitionAgent.builder()
            .addValidator(TransitionRule.and(
                TransitionRule.maxTransitions(50),
                TransitionRule.noRecursion()
            ))
            .strictMode(true)
            .build();

        // 4. Build orchestrator with transitions
        AgentOrchestrator orchestrator = AgentOrchestrator.builder(
            List.of(productOwner, architect, developer, tester)
        )
            .initialAgent("product_owner")
            // Define workflow transitions
            .addTransition("product_owner", "architect")
            .addTransition("product_owner", "developer")
            .addTransition("architect", "developer")
            .addTransition("architect", "product_owner")
            .addTransition("developer", "architect")
            .addTransition("developer", "tester")
            .addTransition("tester", "developer")
            .addTransition("tester", "product_owner")
            .allowRecursive("developer")  // Developer can iterate
            // Add monitoring
            .addTransitionListener((from, to) -> {
                System.out.printf("[TRANSITION] %s -> %s%n",
                    from.getName(), to.getName());
            })
            .maxIterations(100)
            .enableLogging(true)
            .build();

        // 5. Attach routing and validation
        AgentStateMachine sm = orchestrator.getStateMachine();
        sm.setIntelligentTransitionAgent(router);
        sm.setTransitionAgent(validator);

        // 6. Execute workflow
        System.out.println("Starting software development workflow...\n");

        var result = orchestrator.process();

        System.out.println("\n=== WORKFLOW COMPLETE ===");
        System.out.println("Final Result: " + result.getAnswer());
    }
}
```

### Dynamic Agent Registration

```java
AgentStateMachine sm = new AgentStateMachine();

// Register agents dynamically
List<String> roles = List.of("analyst", "designer", "implementer");
for (String role : roles) {
    Agent agent = AgentBuilder.create(role)
        .prompt("You are a " + role)
        .build();
    sm.registerAgent(agent);
}

// Configure transitions dynamically
for (int i = 0; i < roles.size() - 1; i++) {
    sm.addTransition(roles.get(i), roles.get(i + 1));
}
```

### Conditional Transitions

```java
Agent router = new AbstractAgent("router", null, "Routing agent") {
    @Override
    public String shouldTransition(Message message, AgentContext context) {
        ApiResponse response = message.getPayload();

        // Route based on message content
        if (response.getAnswer().contains("error")) {
            return "error_handler";
        } else if (response.getAnswer().contains("review")) {
            return "reviewer";
        } else {
            return "processor";
        }
    }

    @Override
    public Message process(Message message, AgentContext context) throws Exception {
        // Routing logic
        return message;
    }
};
```

### Error Recovery

```java
Agent resilientAgent = AgentBuilder.create("resilient")
    .prompt("You handle errors gracefully...")
    .onEnter(context -> {
        context.setState("error_count", 0);
    })
    .build();

// In custom agent process method:
@Override
public Message process(Message message, AgentContext context) throws Exception {
    try {
        // Normal processing
        return processNormally(message, context);
    } catch (Exception e) {
        // Track errors
        int errorCount = (int) context.getState("error_count");
        context.setState("error_count", errorCount + 1);

        if (errorCount >= 3) {
            // Escalate after 3 errors
            message.setType("supervisor");
            message.getPayload().setAnswer("Need help: " + e.getMessage());
        } else {
            // Retry
            message.setType(this.getName());
        }
        return message;
    }
}
```

### Async Processing (Custom Implementation)

```java
public class AsyncOrchestrator {
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public CompletableFuture<ApiResponse> processAsync(AgentOrchestrator orchestrator) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return orchestrator.process();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    public void shutdown() {
        executor.shutdown();
    }
}

// Usage
AsyncOrchestrator async = new AsyncOrchestrator();
CompletableFuture<ApiResponse> future = async.processAsync(orchestrator);

future.thenAccept(result -> {
    System.out.println("Async result: " + result.getAnswer());
}).exceptionally(ex -> {
    System.err.println("Error: " + ex.getMessage());
    return null;
});
```

---

## 📚 Complete Examples

### Example 1: Content Generation Pipeline

```java
public class ContentPipeline {
    public static void main(String[] args) throws Exception {
        Agent researcher = AgentBuilder.create("researcher")
            .toolInstance(new WebTools())
            .prompt("Research topics and gather information")
            .build();

        Agent writer = AgentBuilder.create("writer")
            .toolInstance(new FileTools())
            .prompt("Write engaging content based on research")
            .build();

        Agent editor = AgentBuilder.create("editor")
            .prompt("Review and improve content quality")
            .build();

        Agent publisher = AgentBuilder.create("publisher")
            .toolInstance(new PublishingTools())
            .prompt("Format and publish finalized content")
            .build();

        AgentOrchestrator pipeline = AgentOrchestrator.builder(
            List.of(researcher, writer, editor, publisher)
        )
            .initialAgent("researcher")
            .addTransition("researcher", "writer")
            .addTransition("writer", "editor")
            .addTransition("editor", "writer")     // Edits may require rewrites
            .addTransition("editor", "publisher")
            .maxIterations(30)
            .build();

        var result = pipeline.process();
        System.out.println("Published: " + result.getAnswer());
    }
}
```

### Example 2: Data Processing Workflow

```java
public class DataProcessing {
    public static void main(String[] args) throws Exception {
        Agent extractor = AgentBuilder.create("extractor")
            .toolInstance(new DataTools())
            .prompt("Extract data from various sources")
            .build();

        Agent transformer = AgentBuilder.create("transformer")
            .toolInstance(new DataTools())
            .prompt("Clean and transform data")
            .build();

        Agent validator = AgentBuilder.create("validator")
            .prompt("Validate data quality and completeness")
            .build();

        Agent loader = AgentBuilder.create("loader")
            .toolInstance(new DatabaseTools())
            .prompt("Load data into target systems")
            .build();

        // ETL pipeline
        AgentOrchestrator etl = AgentOrchestrator.builder(
            List.of(extractor, transformer, validator, loader)
        )
            .initialAgent("extractor")
            .addTransition("extractor", "transformer")
            .addTransition("transformer", "validator")
            .addTransition("validator", "loader")
            .addTransition("validator", "transformer")  // Back for fixes
            .maxIterations(20)
            .build();

        var result = etl.process();
    }
}
```

### Example 3: Customer Support Routing

```java
public class CustomerSupport {
    public static void main(String[] args) throws Exception {
        Agent tier1 = AgentBuilder.create("tier1_support")
            .toolInstance(new TicketingTools())
            .prompt("Handle basic customer inquiries")
            .build();

        Agent tier2 = AgentBuilder.create("tier2_support")
            .toolInstance(new TicketingTools())
            .prompt("Handle complex technical issues")
            .build();

        Agent specialist = AgentBuilder.create("specialist")
            .toolInstance(new TicketingTools())
            .prompt("Handle escalated and critical issues")
            .build();

        // Intelligent routing for support tiers
        IntelligentTransitionAgent router = IntelligentTransitionAgent.builder()
            .addValidator(TransitionRule.maxTransitions(10))
            .build();

        AgentOrchestrator support = AgentOrchestrator.builder(
            List.of(tier1, tier2, specialist)
        )
            .initialAgent("tier1_support")
            .addTransition("tier1_support", "tier2_support")
            .addTransition("tier2_support", "specialist")
            .addTransition("tier2_support", "tier1_support")  // Resolution
            .allowRecursive("tier1_support")
            .maxIterations(15)
            .build();

        support.getStateMachine().setIntelligentTransitionAgent(router);

        var result = support.process();
    }
}
```

---

## ✅ Best Practices

### 1. Agent Design

**✅ DO:**
- Give agents clear, focused responsibilities
- Use structured prompts with sections (Role, Responsibilities, Tools)
- Include CRITICAL rules to prevent unwanted behavior
- Specify exact tool names agents should use

**❌ DON'T:**
- Create "do everything" agents with vague prompts
- Assume agents will infer tool availability
- Skip enforcement rules in prompts

```java
// ✅ Good
Agent developer = AgentBuilder.create("developer")
    .prompt("""
        # Role: Java Developer

        # CRITICAL:
        - YOU MUST use CREATE_FILE to write actual code
        - NO pseudo-code or placeholders

        # Tools: CREATE_FILE, MODIFY_FILE, MVN_CLEAN_INSTALL
        """)
    .toolInstance(new DevTools())
    .build();

// ❌ Bad
Agent developer = AgentBuilder.create("developer")
    .prompt("Do development stuff")
    .build();
```

### 2. Transition Design

**✅ DO:**
- Define explicit transition rules
- Allow bidirectional communication where needed
- Use recursive transitions for iterative tasks
- Document why each transition exists

**❌ DON'T:**
- Allow unrestricted transitions
- Create circular dependencies without termination
- Forget to define transitions back for questions

```java
// ✅ Good
.addTransition("developer", "architect")  // For design questions
.addTransition("architect", "developer")  // For implementation
.addTransition("developer", "tester")     // For testing
.addTransition("tester", "developer")     // For bug fixes
.allowRecursive("developer")              // For multi-step work

// ❌ Bad
.addTransition("agent1", "agent2")
.addTransition("agent2", "agent3")
.addTransition("agent3", "agent1")  // Circular with no exit
```

### 3. Context Usage

**✅ DO:**
- Use namespaced keys for agent-specific state
- Clear state when no longer needed
- Document what state keys mean
- Use type-safe wrappers for complex state

**❌ DON'T:**
- Use generic key names that might conflict
- Store sensitive data without encryption
- Forget to clean up state between runs

```java
// ✅ Good
String key = agentName + "-conversation-id";
context.setState(key, conversationId);

// ❌ Bad
context.setState("id", someId);  // Too generic
```

### 4. Error Handling

**✅ DO:**
- Validate agent responses before processing
- Track error counts and implement retry logic
- Provide fallback mechanisms
- Log errors with context

**❌ DON'T:**
- Ignore validation failures
- Allow infinite retry loops
- Swallow exceptions without logging

### 5. Performance

**✅ DO:**
- Use parallel execution for independent tools
- Set reasonable iteration limits
- Cache expensive operations
- Monitor execution time

**❌ DON'T:**
- Use sequential execution when parallel would work
- Set iteration limits too high
- Perform redundant LLM calls

### 6. Testing

**✅ DO:**
- Test individual agents in isolation
- Test transition validation rules
- Test with mock LLM responses
- Test error scenarios

**❌ DON'T:**
- Only test happy paths
- Skip validation rule testing
- Test only with real LLM calls (expensive + flaky)

---

## 📖 API Reference

### AgentOrchestrator

```java
public class AgentOrchestrator {
    // Main execution
    public ApiResponse process() throws Exception

    // Access state machine
    public AgentStateMachine getStateMachine()

    // Builder
    public static Builder builder(List<Agent> agents)

    public static class Builder {
        public Builder initialAgent(String agentName)
        public Builder addTransition(String from, String to)
        public Builder allowRecursive(String agentName)
        public Builder addTransitionListener(TransitionListener listener)
        public Builder maxIterations(int max)
        public Builder enableLogging(boolean enable)
        public AgentOrchestrator build()
    }
}
```

### AgentStateMachine

```java
public class AgentStateMachine {
    // Agent management
    public AgentStateMachine registerAgent(Agent agent)
    public Agent getAgent(String name)
    public Collection<Agent> getAllAgents()
    public Agent getCurrentAgent()
    public Agent getInitialAgent()

    // Transitions
    public AgentStateMachine addTransition(String from, String to)
    public AgentStateMachine addRecursiveTransition(String agent)
    public Set<String> getTransitions(String agentName)
    public void transitionTo(String agentName)

    // Lifecycle
    public void start()
    public void reset()
    public Message processMessage(Message message)

    // Routing
    public String determineNextAgent(Message message)
    public void setIntelligentTransitionAgent(IntelligentTransitionAgent agent)
    public void setTransitionAgent(TransitionAgent agent)

    // Listeners
    public void addTransitionListener(TransitionListener listener)
    public void removeTransitionListener(TransitionListener listener)

    // Context
    public AgentContext getContext()
}
```

### Agent / AbstractAgent

```java
public interface Agent {
    String getName()
    String getPrompt()
    String toolDescription()

    void onEnter(AgentContext context)
    void onExit(AgentContext context)

    Message process(Message message, AgentContext context) throws Exception
    String shouldTransition(Message message, AgentContext context)
}
```

### AgentBuilder

```java
public class AgentBuilder {
    public static AgentBuilder create(String name)

    public AgentBuilder name(String name)
    public AgentBuilder prompt(String prompt)
    public AgentBuilder toolInstance(Object toolInstance)
    public AgentBuilder onEnter(Consumer<AgentContext> handler)
    public AgentBuilder onExit(Consumer<AgentContext> handler)

    public Agent build()
}
```

### IntelligentTransitionAgent

```java
public class IntelligentTransitionAgent {
    public TransitionDecision determineNextAgent(
        Agent currentAgent,
        Message message,
        AgentContext context
    )

    public static Builder builder()

    public static class Builder {
        public Builder client(Client client)
        public Builder addValidator(TransitionValidator validator)
        public Builder enableValidation(boolean enable)
        public IntelligentTransitionAgent build()
    }
}
```

### TransitionAgent

```java
public class TransitionAgent {
    public TransitionValidationResult validateTransition(
        Agent fromAgent,
        Agent toAgent,
        Message message,
        AgentContext context
    )

    public static Builder builder()

    public static class Builder {
        public Builder addValidator(TransitionValidator validator)
        public Builder strictMode(boolean strict)
        public TransitionAgent build()
    }
}
```

### TransitionRule

```java
public class TransitionRule {
    // Pre-built rules (static factory methods)
    public static TransitionValidator requireFinalization()
    public static TransitionValidator allowOnlyTo(Set<String> agents)
    public static TransitionValidator blockFrom(Set<String> agents)
    public static TransitionValidator maxTransitions(int max)
    public static TransitionValidator noRecursion()
    public static TransitionValidator requireAction(Action action)
    public static TransitionValidator requireContextState(String key, Object value)
    public static TransitionValidator requireSender(String sender)

    // Composition
    public static TransitionValidator and(TransitionValidator... validators)
    public static TransitionValidator or(TransitionValidator... validators)
}
```

### AgentContext

```java
public class AgentContext {
    public void setState(String key, Object value)
    public Object getState(String key)
    public void clearState()

    public AgentStateMachine getStateMachine()
}
```

### Message

```java
public class Message {
    public String getType()
    public void setType(String type)

    public String getSender()
    public void setSender(String sender)

    public ApiResponse getPayload()
    public void setPayload(ApiResponse payload)

    public Map<String, Object> getMetadata()

    public static Builder builder(String type)

    public static class Builder {
        public Builder sender(String sender)
        public Builder payload(ApiResponse payload)
        public Builder metadata(String key, Object value)
        public Message build()
    }
}
```

---

## 🔧 Troubleshooting

### Common Issues

#### Issue: `IllegalStateException: Transition not allowed`

**Cause**: Attempted transition not registered in state machine

**Solution**:
```java
orchestrator.addTransition("sourceAgent", "targetAgent");
```

#### Issue: `Max iterations reached`

**Cause**: Workflow didn't complete within iteration limit

**Solutions**:
- Increase limit: `.maxIterations(200)`
- Check for infinite loops in agent logic
- Ensure agents call `FINALIZE_TASK` when done
- Review transition patterns for circular dependencies

#### Issue: `No strategy found for action`

**Cause**: LLM returned invalid action string

**Solution**:
- Improve agent prompt to emphasize valid actions
- Add retry logic (framework does this automatically)
- Check LLM model configuration

#### Issue: Agent doesn't finalize

**Cause**: Agent keeps routing to itself or others

**Solutions**:
- Add explicit finalization criteria in prompt
- Use `TransitionRule.requireFinalization()`
- Check `shouldTransition()` logic
- Monitor finalized agents set

#### Issue: Tool not found

**Cause**: Tool not properly registered or name mismatch

**Solutions**:
```java
// Ensure tool is annotated
@Tool(name = "MY_TOOL", description = "...")
public String myTool(...) { }

// Ensure tool instance is attached
.toolInstance(new MyTools())

// Check tool name in agent response matches annotation
```

#### Issue: Context state not persisting

**Cause**: State cleared or using wrong key

**Solutions**:
```java
// Use namespaced keys
String key = agentName + "-state-key";
context.setState(key, value);

// Avoid clearing unnecessarily
// context.clearState(); // Only when needed
```

#### Issue: Intelligent routing not working

**Cause**: Router not attached to state machine

**Solution**:
```java
IntelligentTransitionAgent router = ...;
orchestrator.getStateMachine().setIntelligentTransitionAgent(router);
```

#### Issue: Validation rules not enforced

**Cause**: Validator not attached or strict mode disabled

**Solution**:
```java
TransitionAgent validator = TransitionAgent.builder()
    .addValidator(...)
    .strictMode(true)  // Enable strict enforcement
    .build();

stateMachine.setTransitionAgent(validator);
```

### Debug Tips

**Enable verbose logging**:
```java
orchestrator.enableLogging(true);
```

**Add transition listeners**:
```java
orchestrator.addTransitionListener((from, to) -> {
    System.out.printf("[%s] %s -> %s%n",
        Instant.now(), from.getName(), to.getName());
});
```

**Monitor context state**:
```java
.onEnter(context -> {
    System.out.println("Context state: " + context.getState("debug_info"));
})
```

**Track iterations**:
```java
.metadata("iteration", currentIteration)
```

---

## ⚡ Performance & Optimization

### Performance Considerations

#### 1. Use Parallel Execution

```java
// ❌ Slow: Sequential when independent
{
  "action": "RUN_TOOL_SEQUENTIAL",
  "toolNames": ["READ_FILE", "READ_FILE", "READ_FILE"]
}

// ✅ Fast: Parallel for independent operations
{
  "action": "RUN_TOOL_PARALLEL",
  "toolNames": ["READ_FILE", "READ_FILE", "READ_FILE"]
}
```

#### 2. Optimize Iteration Limits

```java
// Balance between completion and performance
.maxIterations(50)  // Good for most workflows

// Too high = waste if stuck
.maxIterations(1000)  // ❌

// Too low = premature termination
.maxIterations(5)  // ❌
```

#### 3. Cache LLM Responses

```java
// Custom client with caching
public class CachedLLMClient implements Client {
    private final Cache<String, ApiResponse> cache = ...;

    @Override
    public ApiResponse execute(Properties props, String prompt, String responseId) {
        String cacheKey = hash(prompt);
        return cache.get(cacheKey, key -> {
            return realClient.execute(props, prompt, responseId);
        });
    }
}
```

#### 4. Optimize Prompts

```java
// ❌ Too verbose
"""
You are a very experienced, highly skilled, world-class expert senior
software engineer with 20 years of experience in Java, Spring, Hibernate...
[500 more words]
"""

// ✅ Concise and clear
"""
# Role: Senior Java Developer
# Responsibilities:
- Implement features
- Write tests
- Review code
"""
```

#### 5. Limit Tool Calls

```java
// Guide agents to batch operations
"""
When creating multiple files, use RUN_TOOL_PARALLEL with multiple
CREATE_FILE calls instead of calling CREATE_FILE multiple times.
"""
```

### Monitoring

```java
public class PerformanceMonitor implements TransitionListener {
    private final Map<String, Long> agentTimes = new ConcurrentHashMap<>();
    private long transitionStart;

    @Override
    public void onTransition(Agent from, Agent to) {
        long now = System.currentTimeMillis();

        if (transitionStart > 0) {
            long duration = now - transitionStart;
            agentTimes.merge(from.getName(), duration, Long::sum);
        }

        transitionStart = now;
    }

    public void printStats() {
        System.out.println("=== Agent Performance ===");
        agentTimes.forEach((agent, time) -> {
            System.out.printf("%s: %dms%n", agent, time);
        });
    }
}

// Usage
PerformanceMonitor monitor = new PerformanceMonitor();
orchestrator.addTransitionListener(monitor);
orchestrator.process();
monitor.printStats();
```

---

## 🗺️ Roadmap

### Completed ✅

- [x] Multi-agent orchestration with FSM
- [x] Agent-to-agent communication and delegation
- [x] Tool integration (file operations, Maven builds)
- [x] Action strategies (sequential, parallel, delegation, finalization)
- [x] Intelligent transition agent with LLM-powered routing
- [x] Transition validation rules and custom validators
- [x] Context-aware agent decision making
- [x] Lifecycle hooks (onEnter, onExit)
- [x] Builder pattern for agent configuration
- [x] Transition listeners for observability
- [x] Finalized agent tracking
- [x] Per-agent state isolation
- [x] Simplified agent process method architecture
- [x] Structured prompt engineering

### In Progress 🚧

- [ ] Comprehensive test suite
- [ ] Performance benchmarking tools
- [ ] Enhanced error recovery mechanisms

### Planned 📅

**Q2 2024:**
- [ ] Async/reactive agent execution
- [ ] State persistence layer (Redis, Database)
- [ ] Streaming LLM responses
- [ ] Agent behavior testing utilities

**Q3 2024:**
- [ ] Multiple LLM provider support (Claude, Gemini, local models)
- [ ] Visual workflow designer (web UI)
- [ ] Agent performance metrics dashboard
- [ ] Workflow templates library

**Q4 2024:**
- [ ] Web UI for monitoring agent interactions
- [ ] Distributed agent execution
- [ ] A/B testing framework for agent prompts
- [ ] Agent learning and optimization

**Future:**
- [ ] Agent marketplace for pre-built agents
- [ ] Natural language workflow definition
- [ ] Multi-modal agent support (vision, audio)
- [ ] Integration with popular frameworks (Spring, Quarkus)

---

## 🤝 Contributing

Contributions are welcome! Areas for improvement:

### High Priority
- Additional built-in tools (Git, Docker, Kubernetes, AWS, etc.)
- Enhanced error recovery mechanisms
- Support for streaming LLM responses
- Agent behavior testing utilities
- Performance optimizations

### Medium Priority
- Documentation improvements
- Example workflows (DevOps, data science, content creation)
- Integration with popular frameworks
- UI components

### How to Contribute

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### Development Setup

```bash
# Clone repository
git clone https://github.com/grkn/llm-agent-orchestrator.git
cd llm-agent-orchestrator

# Build
mvn clean install

# Run tests
mvn test

# Run example
mvn exec:java -Dexec.mainClass="com.grkn.orchestration.example.YourExample"
```

### Code Style

- Follow Java conventions
- Add Javadoc for public APIs
- Include unit tests for new features
- Update README with new features

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

---

## 👤 Author

**Gürkan (@grkn)**

- GitHub: [@grkn](https://github.com/grkn)
- Repository: [llm-agent-orchestrator](https://github.com/grkn/llm-agent-orchestrator)

---

## 🙏 Acknowledgments

- Inspired by multi-agent systems research
- Built with Java 21 and modern patterns
- Powered by LLM technology

---

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/grkn/llm-agent-orchestrator/issues)
- **Discussions**: [GitHub Discussions](https://github.com/grkn/llm-agent-orchestrator/discussions)
- **Email**: [Contact via GitHub](https://github.com/grkn)

---

## ⚠️ Important Notes

### Production Deployment

- **API Key Security**: Never commit API keys. Use environment variables or secret management.
- **Rate Limiting**: Implement rate limiting for LLM API calls.
- **Cost Monitoring**: LLM calls can be expensive. Monitor usage.
- **Error Handling**: Implement comprehensive error handling and retry logic.
- **Logging**: Enable detailed logging for debugging production issues.

### Best Practices for Production

```java
// ✅ Use environment variables
String apiKey = System.getenv("OPENAI_API_KEY");
Properties.INSTANCE.setOpenAIKey(apiKey);

// ✅ Implement rate limiting
RateLimiter limiter = RateLimiter.create(10.0); // 10 requests/second
limiter.acquire();
response = client.execute(...);

// ✅ Monitor costs
logger.info("LLM call cost: $" + estimateCost(prompt, model));

// ✅ Set reasonable timeouts
.maxIterations(100)  // Prevent runaway workflows

// ✅ Add comprehensive error handling
try {
    orchestrator.process();
} catch (IllegalStateException e) {
    logger.error("State transition error", e);
    notifyOps(e);
} catch (Exception e) {
    logger.error("Unexpected error", e);
    rollback();
}
```

---

**Ready to build intelligent agent systems? Get started with the [Quick Start](#quick-start) guide!**

