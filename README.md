# LLM Agent Orchestrator

A Java 21 framework for building multi-agent LLM workflows with a finite-state-machine (FSM) orchestration model. This framework enables complex AI agent collaboration through structured state transitions, intelligent routing, and tool execution.

## Table of Contents

1. [Overview](#overview)
2. [Key Features](#key-features)
3. [Project Status](#project-status)
4. [Architecture](#architecture)
5. [Execution Lifecycle](#execution-lifecycle)
6. [Core Packages and Classes](#core-packages-and-classes)
7. [Prerequisites](#prerequisites)
8. [Installation and Build](#installation-and-build)
9. [Configuration](#configuration)
10. [Quick Start](#quick-start)
11. [Action Model](#action-model)
12. [Tool Integration](#tool-integration)
13. [Transition Model](#transition-model)
14. [Advanced Features](#advanced-features)
15. [Extending the Framework](#extending-the-framework)
16. [Observability and Logging](#observability-and-logging)
17. [Error Handling and Retries](#error-handling-and-retries)
18. [Known Limitations](#known-limitations)
19. [Troubleshooting](#troubleshooting)
20. [Roadmap Suggestions](#roadmap-suggestions)
21. [License](#license)

## Overview

The LLM Agent Orchestrator provides a robust framework for coordinating multiple AI agents in complex workflows. Each agent specializes in specific tasks and communicates through a structured message-passing system. The framework handles:

- **Agent lifecycle management** - Registration, initialization, and finalization
- **State machine coordination** - Explicit transitions with validation
- **LLM integration** - OpenAI-compatible API calls with retry logic
- **Tool execution** - Sequential and parallel tool invocation
- **Message routing** - Context-aware agent-to-agent communication
- **Transition intelligence** - Automatic agent selection when explicit routes aren't defined

## Key Features

✅ **Fluent Agent Builder** - Create agents without writing separate classes
✅ **FSM-Based Orchestration** - Explicit state transitions with validation rules
✅ **Intelligent Routing** - TransitionAgent automatically selects appropriate agents
✅ **Tool Execution Pipeline** - Annotation-based tool discovery and execution
✅ **Retry Mechanisms** - Built-in retry logic for LLM calls and action validation
✅ **Parallel Tool Execution** - Concurrent tool invocation with thread pool management
✅ **Context Management** - Shared state across agent interactions
✅ **Finalization Tracking** - Per-agent task completion monitoring

## Project Status

This codebase is a **framework-level implementation** intended as a library module (not a standalone runnable application).

**Repository characteristics:**
- ✅ Production-ready agent orchestration framework
- ✅ Comprehensive agent lifecycle management
- ✅ Built with Java 21 and Maven
- ⚠️ No main class (library module)
- ⚠️ No tests in `src/test` (test coverage needed)
- ⚠️ Depends on external artifact: `com.grkn:ToolLibrary:1.0-SNAPSHOT`
- ⚠️ Includes prebuilt jar in `target/` from prior build

## Architecture

### High-Level Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    AgentOrchestrator                        │
│  - Manages workflow loop                                    │
│  - Tracks agent finalization                                │
│  - Enforces iteration limits                                │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  AgentStateMachine                          │
│  - Registers agents and transitions                         │
│  - Manages current state                                    │
│  - Notifies listeners                                       │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   Current Agent                             │
│  (Built via AgentBuilder extending AbstractAgent)           │
│  - Builds prompts from templates                            │
│  - Calls LLM via ChatGptClient                             │
│  - Validates and executes actions                           │
└────────────────────────┬────────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        ▼                ▼                ▼
┌─────────────┐  ┌──────────────┐  ┌────────────┐
│ ChatGptClient│  │TransitionAgent│  │ActionStrategy│
│ - HTTP calls │  │ - Validates   │  │ - RUN_TOOL │
│ - Retries    │  │   transitions │  │ - ASK_AGENT│
│ - Backoff    │  │ - Auto-routes │  │ - FINALIZE │
└─────────────┘  └──────────────┘  └──────┬─────┘
                                           │
                         ┌─────────────────┴──────────────┐
                         ▼                                 ▼
              ┌─────────────────────┐         ┌──────────────────┐
              │DefaultToolRunnerImpl│         │  Message Router  │
              │ - Sequential exec   │         │  - Next agent    │
              │ - Parallel exec     │         │  - Payload       │
              └─────────────────────┘         └──────────────────┘
```

### Workflow Steps

1. **Agent Definition** - Define agents with `AgentBuilder` specifying prompts and tools
2. **Orchestrator Creation** - Create `AgentOrchestrator` with agents and transition rules
3. **FSM Initialization** - Start state machine at designated initial agent
4. **Agent Processing** - Active agent builds prompt and calls LLM via `ChatGptClient`
5. **Action Execution** - LLM returns `ApiResponse` with action type and parameters
6. **Strategy Dispatch** - Matching strategy executes (`RUN_TOOL_*`, `ASK_AGENT`, `FINALIZE_TASK`)
7. **Transition Decision** - FSM determines next agent via explicit rules or `TransitionAgent`
8. **Loop Continuation** - Process repeats until completion criteria met

## Execution Lifecycle

The `AgentOrchestrator.process()` method orchestrates the entire workflow:

```java
1. stateMachine.start()
   → Initializes FSM with initial agent

2. Create initial Message
   → iteration = 0
   → finalizedAgents = {}
   → type = initialAgent.name

3. Main loop (while iteration < maxIterations):
   a. Process message through current agent
      → Build prompt with template
      → Execute LLM call with retry
      → Validate action enum
      → Execute action strategy

   b. Check for finalization
      → If FINALIZE_TASK: mark agent as finalized
      → If all agents finalized: exit loop

   c. Determine next agent
      → From message.type (set by ASK_AGENT)
      → Or via TransitionAgent decision logic

   d. Validate transition
      → Check explicit transitions in FSM
      → Or use TransitionAgent for intelligent routing

   e. Update message for next agent
      → Rebuild with new type/sender
      → Carry forward payload
      → Update metadata (iteration, finalizedAgents)

4. Return final ApiResponse
```

### Important Behaviors

- **Per-Agent Finalization** - Each agent tracks its own completion state
- **Re-activation Support** - Finalized agents can be un-finalized if targeted by another agent
- **Iteration Safety** - Hard limit prevents infinite loops
- **Context Preservation** - Shared context persists across all agent transitions

## Core Packages and Classes

### `com.grkn.orchestration.llms.orchestrator`

#### **AgentOrchestrator**
Main coordinator and loop controller providing:
- Builder API for transitions, recursion, logging, listeners, and iteration limits
- Workflow execution via `process()` method
- Agent finalization tracking
- Iteration limit enforcement

**Key Methods:**
- `process()` - Executes orchestration workflow
- `builder(List<Agent>)` - Creates builder instance
- Builder methods: `maxIterations()`, `enableLogging()`, `initialAgent()`, `addTransition()`, `allowRecursive()`

### `com.grkn.orchestration.llms.fsm`

#### **Agent (Interface)**
Contract for all agents defining:
- `process(Message, AgentContext)` - Core processing logic
- `shouldTransition(Message, AgentContext)` - Transition decision logic
- `getName()` - Agent identifier
- `getPrompt()` - Agent's role description
- Lifecycle hooks: `onEnter()`, `onExit()`

#### **AbstractAgent**
Base implementation providing:
- Name, prompt, and tool metadata management
- Tool method extraction via reflection
- Tool description generation for prompts
- Template method pattern for agent behavior

#### **AgentBuilder**
Fluent builder creating `AbstractAgent` implementations with:
- Prompt composition using orchestrator template
- LLM call execution with retry logic
- Action enum validation
- Action strategy dispatch
- Transition decision via `TransitionAgent` fallback

**Core Builder Methods:**
```java
AgentBuilder.create("AgentName")
    .prompt("Agent's role and responsibilities")
    .toolInstance(toolsObject)
    .onEnter(context -> { /* setup logic */ })
    .onExit(context -> { /* cleanup logic */ })
    .build()
```

#### **AgentStateMachine**
FSM manager handling:
- Agent registration and retrieval
- Transition registration (explicit and recursive)
- State transitions with validation
- Transition listener notifications
- Shared context management

**Key Methods:**
- `registerAgent(Agent)` - Add agent to FSM
- `addTransition(String from, String to)` - Define allowed transition
- `addRecursiveTransition(String agentName)` - Allow self-loops
- `transitionTo(String agentName)` - Execute transition
- `processMessage(Message)` - Route message through current agent

#### **AgentContext**
Shared state container providing:
- Key-value state storage (`setState()`, `getState()`)
- Reference to `AgentStateMachine`
- Internal message routing helper

#### **Message**
Agent-to-agent communication envelope containing:
- `type` - Next agent identifier
- `payload` - `ApiResponse` object
- `sender` - Source agent name
- `metadata` - Map for iteration count, finalized agents, etc.

#### **TransitionAgent**
Intelligent transition validator that:
- Evaluates transitions with `TransitionValidator` chain
- Includes `DefaultTransitionValidator` that queries LLM for best agent
- Supports strict/non-strict validation modes
- Logs denied transitions with reasons

**Default Behavior:**
1. If requested transition exists in FSM → allow
2. If not allowed → ask LLM to select most responsible agent
3. Return selected agent or deny with reason

#### **TransitionValidator (Interface)**
Custom transition rule contract returning:
- `TransitionValidationResult` with allow/deny status
- Reason string (agent name if allowed, error if denied)

### `com.grkn.orchestration.llms.core`

#### **ChatGptClient**
HTTP client wrapper for LLM calls featuring:
- Singleton pattern (`ChatGptClient.INSTANCE`)
- Exponential backoff retry (up to 5 attempts)
- Response ID tracking for conversation continuity
- HTTP 429 retry handling
- Error handling for HTTP >= 400

**Retry Conditions:**
- HTTP 429 (rate limit)
- IOException
- InterruptedException

#### **DefaultToolFinder**
Tool discovery service:
- Scans methods for `@Tool(name="...")` annotation
- Returns matching method for tool name
- Used by `LlmCallable` for invocation

#### **LlmCallable**
Tool invocation wrapper:
- Parses tool input JSON to parameter type
- Invokes method via reflection
- Wraps result in `CallableResponse`
- Captures exceptions as errors

#### **DefaultToolRunnerImpl**
Tool execution engine:
- **Sequential execution** - Tools run one by one
- **Parallel execution** - Fixed thread pool (size 50)
- Returns `List<CallableResponse>` with results/errors

### `com.grkn.orchestration.llms.strategy`

#### **ActionStrategy (Interface)**
Strategy pattern contract for action execution.

#### **ActionStrategyFactory**
Maps `Action` enum values to implementations:
- `RUN_TOOL_SEQUENTIAL` → `RunToolSequentialStrategy`
- `RUN_TOOL_PARALLEL` → `RunToolParallelStrategy`
- `ASK_AGENT` → `AskAgentStrategy`
- `FINALIZE_TASK` → `FinalizeStrategy`

#### **RunToolSequentialStrategy**
Executes tool list in order:
- Calls `DefaultToolRunnerImpl.runSequential()`
- Writes first result/error to `toolOutput`
- Returns message with updated payload

#### **RunToolParallelStrategy**
Executes tools concurrently:
- Calls `DefaultToolRunnerImpl.runParallel()`
- Writes list of results/errors to `toolOutput`
- Returns message with updated payload

#### **AskAgentStrategy**
Routes to another agent:
- Builds new message with `type = apiResponse.agentName`
- Preserves payload
- Updates sender to current agent

#### **FinalizeStrategy**
Signals task completion:
- Returns response payload unchanged
- Triggers finalization in orchestrator loop

### `com.grkn.orchestration.llms.dto`

#### **ApiResponse**
Structured LLM response containing:
- `action` - Action type enum value
- `agentName` - Target agent for `ASK_AGENT`
- `toolNames` - List of tools to execute
- `inputs` - List of input objects for tools
- `answer` - Message content or final answer
- `toolOutput` - Tool execution results (set by framework)
- `responseId` - Conversation tracking ID

#### **CallableResponse**
Tool invocation result:
- `result` - Successful execution result
- `error` - Exception message if failed
- `toolName` - Name of invoked tool

### `com.grkn.orchestration.llms.properties`

#### **Properties**
Singleton runtime configuration:
- `openAIKey` - API authentication key
- `openAIModel` - Model identifier (e.g., "gpt-4")
- `baseUrl` - API endpoint URL
- Accessed via `Properties.INSTANCE`

### `com.grkn.orchestration.llms.enums`

#### **Action**
Available action types:
- `RUN_TOOL_SEQUENTIAL` - Execute tools one by one
- `RUN_TOOL_PARALLEL` - Execute tools concurrently
- `ASK_AGENT` - Delegate to another agent
- `FINALIZE_TASK` - Mark agent as complete

### `com.grkn.orchestration.llms.interfaces`

#### **Client**
LLM client contract:
- `execute(Properties, String prompt, String responseId)` - Make LLM call
- Implemented by `ChatGptClient`

#### **ToolFinder**
Tool discovery contract:
- `findTool(String toolName, List<Method> methods)` - Locate tool method
- Implemented by `DefaultToolFinder`

#### **ToolRunner**
Tool execution contract:
- `runSequential(List<ToolCall>)` - Sequential execution
- `runParallel(List<ToolCall>)` - Concurrent execution
- Implemented by `DefaultToolRunnerImpl`

## Prerequisites

- **Java 21+** - Required for language features
- **Maven 3.9+** - Build and dependency management
- **OpenAI-compatible endpoint** - LLM API access
- **ToolLibrary dependency** - `com.grkn:ToolLibrary:1.0-SNAPSHOT` must be available

## Installation and Build

### Clone and Build

```bash
git clone <repository-url>
cd llm-agent-orchestrator
mvn clean package
```

### Install Dependency

If Maven cannot resolve `com.grkn:ToolLibrary:1.0-SNAPSHOT`:

```bash
# Install ToolLibrary to local Maven repository first
cd path/to/ToolLibrary
mvn clean install

# Then build this project
cd path/to/llm-agent-orchestrator
mvn clean package
```

### Maven Coordinates

To use this framework in your project:

```xml
<dependency>
    <groupId>com.grkn</groupId>
    <artifactId>OrchestrationLLMs</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Configuration

Before running orchestration, configure the singleton `Properties.INSTANCE`:

```java
import com.grkn.orchestration.llms.properties.Properties;

Properties.INSTANCE.setOpenAIKey(System.getenv("OPENAI_API_KEY"));
Properties.INSTANCE.setOpenAIModel("gpt-4");
Properties.INSTANCE.setBaseUrl("https://api.openai.com/v1/responses");
```

### Environment Variables

Recommended approach using environment variables:

```bash
export OPENAI_API_KEY="your-api-key-here"
export OPENAI_MODEL="gpt-4"
export OPENAI_BASE_URL="https://api.openai.com/v1/responses"
```

### Notes

- `ChatGptClient` expects an OpenAI Responses-style endpoint
- Response format: `output[].content[0].text` for extracted text
- If using a gateway/proxy, ensure it matches this response schema

## Quick Start

Minimal end-to-end example demonstrating agent creation, transition setup, and orchestration:

```java
import com.grkn.orchestration.llms.dto.ApiResponse;
import com.grkn.orchestration.llms.fsm.Agent;
import com.grkn.orchestration.llms.fsm.AgentBuilder;
import com.grkn.orchestration.llms.orchestrator.AgentOrchestrator;
import com.grkn.orchestration.llms.properties.Properties;

import java.util.List;

public class QuickStartDemo {
    public static void main(String[] args) throws Exception {
        // Configure LLM client
        Properties.INSTANCE.setOpenAIKey(System.getenv("OPENAI_API_KEY"));
        Properties.INSTANCE.setOpenAIModel("gpt-4");
        Properties.INSTANCE.setBaseUrl("https://api.openai.com/v1/responses");

        // Define agents with prompts (no tools in this example)
        Agent planner = AgentBuilder.create("Planner")
                .prompt("You break user requests into actionable tasks and create execution plans.")
                .toolInstance(null)
                .build();

        Agent implementer = AgentBuilder.create("Implementer")
                .prompt("You implement the planned tasks and verify the changes work correctly.")
                .toolInstance(null)
                .build();

        Agent reviewer = AgentBuilder.create("Reviewer")
                .prompt("You review completed work and determine if the original goal is achieved.")
                .toolInstance(null)
                .build();

        // Create orchestrator with transition rules
        AgentOrchestrator orchestrator = AgentOrchestrator.builder(List.of(planner, implementer, reviewer))
                .initialAgent("Planner")
                .addTransition("Planner", "Implementer")
                .addTransition("Implementer", "Reviewer")
                .addTransition("Reviewer", "Planner")  // Allow iteration
                .allowRecursive("Implementer")  // Implementer can call itself
                .maxIterations(50)
                .enableLogging(true)
                .build();

        // Run orchestration
        ApiResponse finalResponse = orchestrator.process();

        System.out.println("=== Final Result ===");
        System.out.println(finalResponse.getAnswer());
    }
}
```

## Action Model

The LLM must return JSON compatible with `ApiResponse` schema.

### Response Schema

```json
{
  "action": "<RUN_TOOL_SEQUENTIAL|RUN_TOOL_PARALLEL|ASK_AGENT|FINALIZE_TASK>",
  "toolNames": ["tool1", "tool2"],
  "inputs": [{"param1": "value1"}, {"param2": "value2"}],
  "answer": "<message content or final answer>",
  "agentName": "<target agent name>",
  "toolOutput": "<tool execution results>"
}
```

### Field Descriptions

| Field | Required | Used By | Description |
|-------|----------|---------|-------------|
| `action` | ✅ | All | Action type enum value |
| `toolNames` | ⚠️ | RUN_TOOL_* | Array of tool names (max 3) |
| `inputs` | ⚠️ | RUN_TOOL_* | Array of input objects (max 3) |
| `answer` | ⚠️ | ASK_AGENT, FINALIZE_TASK | Message content or final result |
| `agentName` | ⚠️ | ASK_AGENT | Target agent identifier |
| `toolOutput` | ❌ | System | Tool results (framework-managed) |

### Action Semantics

#### **RUN_TOOL_SEQUENTIAL**
Executes tools one by one in order:
- **Use When:** Tools don't depend on each other, order matters
- **Example:** Read file → Process content → Write result
- **Limit:** Maximum 3 tools per call
- **Output:** First tool result written to `toolOutput`

#### **RUN_TOOL_PARALLEL**
Executes tools concurrently via thread pool:
- **Use When:** Independent operations, speed is critical
- **Example:** Read multiple files simultaneously
- **Limit:** Maximum 3 tools per call
- **Output:** List of all results written to `toolOutput`

#### **ASK_AGENT**
Delegates to another agent:
- **Use When:** Task requires specialized agent expertise
- **Example:** Planner asks Implementer to execute changes
- **Routing:** Creates message with `type = agentName`
- **Validation:** TransitionAgent validates/selects appropriate agent

#### **FINALIZE_TASK**
Marks current agent as finalized:
- **Use When:** Agent's work is complete
- **Effect:** Agent marked in `finalizedAgents` set
- **Completion:** Orchestration ends when all agents finalized
- **Re-activation:** Finalized agents can be un-finalized if targeted

## Tool Integration

Tool invocation uses reflection-based discovery with annotation scanning from `ToolLibrary`.

### How It Works

```
1. AbstractAgent scans tool methods from toolClassInstance
   ↓
2. DefaultToolFinder locates method by @Tool(name = "...")
   ↓
3. LlmCallable reads tool input and maps to parameter type
   ↓
4. Method invoked via reflection with parsed parameters
   ↓
5. Strategy writes tool result/error to ApiResponse.toolOutput
```

### Tool Class Example

```java
import com.grkn.tool.library.annotation.Tool;
import com.grkn.tool.library.annotation.ToolParameter;

public class FileTools {

    @Tool(name = "read_file")
    public String readFile(@ToolParameter String path) {
        // Read and return file contents
        return Files.readString(Path.of(path));
    }

    @Tool(name = "write_file")
    public void writeFile(
        @ToolParameter String path,
        @ToolParameter String content
    ) {
        // Write content to file
        Files.writeString(Path.of(path), content);
    }

    @Tool(name = "list_directory")
    public List<String> listDirectory(@ToolParameter String path) {
        // Return list of files in directory
        return Files.list(Path.of(path))
            .map(Path::toString)
            .toList();
    }
}
```

### Attaching Tools to Agent

```java
FileTools fileTools = new FileTools();

Agent developer = AgentBuilder.create("Developer")
        .prompt("You implement code changes using file operations.")
        .toolInstance(fileTools)
        .build();
```

### LLM Tool Invocation Example

**Sequential Execution:**
```json
{
  "action": "RUN_TOOL_SEQUENTIAL",
  "toolNames": ["read_file", "write_file"],
  "inputs": [
    {"path": "/src/Main.java"},
    {"path": "/src/Main.java", "content": "public class Main {}"}
  ],
  "answer": "Reading and updating Main.java"
}
```

**Parallel Execution:**
```json
{
  "action": "RUN_TOOL_PARALLEL",
  "toolNames": ["read_file", "read_file", "read_file"],
  "inputs": [
    {"path": "/src/A.java"},
    {"path": "/src/B.java"},
    {"path": "/src/C.java"}
  ],
  "answer": "Reading multiple files simultaneously"
}
```

### Tool Requirements

✅ Must have `@Tool(name = "unique_name")` annotation
✅ Parameters must have `@ToolParameter` annotation
✅ Tool names must be unique within agent's tool instance
✅ Input JSON must match parameter types (Jackson deserialization)

## Transition Model

Transitions are **explicit** in `AgentStateMachine` with opt-in features:

### Explicit Transitions

Only configured transitions are allowed:

```java
orchestrator.addTransition("Planner", "Implementer");  // Planner → Implementer allowed
orchestrator.addTransition("Implementer", "Reviewer"); // Implementer → Reviewer allowed
// Planner → Reviewer NOT allowed (not configured)
```

### Recursive Transitions

Self-loops require explicit opt-in:

```java
orchestrator.allowRecursive("Implementer");  // Implementer → Implementer allowed
```

### Agent Transition Decision Path

```
1. Agent.process(Message, AgentContext) returns Message
   ↓
2. Agent.shouldTransition(Message, AgentContext) determines next target
   ↓
3. If target ≠ current: FSM executes transitionTo(next)
   ↓
4. Transition listeners notified
   ↓
5. Next agent's onEnter() called
```

### TransitionAgent Default Behavior

Intelligent routing when explicit transition doesn't exist:

```
1. Check if requested transition exists in FSM
   ├─ YES → Allow transition
   └─ NO  → Ask LLM to select most responsible agent
            ↓
            Analyze agent prompts and decision message
            ↓
            Return agent name with highest responsibility match
```

**LLM Selection Prompt:**
```
Rules:
- Read prompt of current agent and available agents
- Extract responsibility of available agents
- Choose agent with highest responsibility ratio
- Cannot choose current agent
- Must choose one agent except current agent

Available agents with prompt:
1-) Planner
    Prompt: You break requests into actionable tasks
2-) Implementer
    Prompt: You implement requested changes
...

Current agent: Planner

Decision Message: "I need to execute the planned tasks"

Result: {"agentName": "Implementer"}
```

### Transition Validation

Custom validators can be added:

```java
TransitionValidator customValidator = (from, to, msg, ctx) -> {
    if ("RestrictedAgent".equals(to.getName())) {
        return TransitionValidator.TransitionValidationResult
            .deny("Access to RestrictedAgent is forbidden");
    }
    return TransitionValidator.TransitionValidationResult
        .allow(to.getName());
};

// Use in custom agent implementation
TransitionAgent transitionAgent = TransitionAgent.builder()
    .addValidator(customValidator)
    .strictMode(true)
    .build();
```

## Advanced Features

### Lifecycle Hooks

```java
Agent agent = AgentBuilder.create("DataProcessor")
    .prompt("Process data with lifecycle management")
    .toolInstance(tools)
    .onEnter(context -> {
        // Initialize resources
        context.setState("startTime", System.currentTimeMillis());
        System.out.println("Agent activated");
    })
    .onExit(context -> {
        // Cleanup resources
        long duration = System.currentTimeMillis()
                      - (Long) context.getState("startTime");
        System.out.println("Agent duration: " + duration + "ms");
    })
    .build();
```

### Shared Context Usage

```java
// In one agent
context.setState("analysisResults", results);

// In another agent
Object results = context.getState("analysisResults");
```

### Transition Listeners

```java
orchestrator = AgentOrchestrator.builder(agents)
    .addTransitionListener((from, to) -> {
        System.out.println(String.format(
            "Transition: %s → %s",
            from.getName(),
            to.getName()
        ));

        // Log to monitoring system
        metrics.recordTransition(from.getName(), to.getName());
    })
    .build();
```

### Custom Agent Implementation

For advanced scenarios, extend `AbstractAgent` directly:

```java
public class CustomAgent extends AbstractAgent {
    public CustomAgent() {
        super("CustomAgent", toolsInstance, "Custom prompt");
    }

    @Override
    public Message process(Message message, AgentContext context) throws Exception {
        // Custom processing logic
        // Access tool methods via getToolMethods()
        // Build custom prompts
        // Implement domain-specific behavior
        return customMessage;
    }

    @Override
    public String shouldTransition(Message message, AgentContext context) {
        // Custom transition logic
        return nextAgentName;
    }

    @Override
    public void onEnter(AgentContext context) {
        // Custom initialization
    }

    @Override
    public void onExit(AgentContext context) {
        // Custom cleanup
    }
}
```

## Extending the Framework

### 1. Custom Transition Validation

Implement `TransitionValidator` for complex routing rules:

```java
public class TimeBasedValidator implements TransitionValidator {
    @Override
    public TransitionValidationResult validate(
        Agent fromAgent,
        Agent toAgent,
        Message message,
        AgentContext context
    ) {
        int iteration = (int) message.getMetadata().get("iteration");

        // Prevent certain transitions after iteration threshold
        if (iteration > 10 && "ExpensiveAgent".equals(toAgent.getName())) {
            return TransitionValidationResult.deny(
                "ExpensiveAgent disabled after iteration 10"
            );
        }

        return TransitionValidationResult.allow(toAgent.getName());
    }
}
```

### 2. Custom LLM Client

Implement `Client` interface for different LLM providers:

```java
public class AzureOpenAIClient implements Client {
    @Override
    public ApiResponse execute(
        Properties properties,
        String prompt,
        String responseId
    ) {
        // Azure-specific implementation
        // - Custom authentication headers
        // - Different endpoint format
        // - Response schema mapping
        // - Metrics/tracing instrumentation
        return apiResponse;
    }
}
```

### 3. Custom Action Strategies

Add new action types and strategies:

```java
// 1. Add to Action enum
public enum Action {
    RUN_TOOL_SEQUENTIAL,
    RUN_TOOL_PARALLEL,
    ASK_AGENT,
    FINALIZE_TASK,
    CUSTOM_ACTION  // New action
}

// 2. Implement ActionStrategy
public class CustomActionStrategy implements ActionStrategy {
    @Override
    public Message execute(
        Message message,
        ApiResponse apiResponse,
        Agent agent
    ) throws Exception {
        // Custom action logic
        return updatedMessage;
    }
}

// 3. Register in ActionStrategyFactory
ActionStrategyFactory.register(
    Action.CUSTOM_ACTION,
    new CustomActionStrategy()
);
```

### 4. Custom Tool Discovery

Implement `ToolFinder` for alternative discovery mechanisms:

```java
public class ConfigBasedToolFinder implements ToolFinder {
    @Override
    public Method findTool(String toolName, List<Method> methods) {
        // Load tool mappings from configuration
        // Support versioning
        // Enable dynamic tool registration
        return matchingMethod;
    }
}
```

## Observability and Logging

### Built-in Logging

Framework uses `java.util.logging.Logger`:

```java
// In AgentOrchestrator
logger.info("Starting orchestration workflow");
logger.info(String.format("Iteration %d - Current Agent: %s", iteration, agentName));
logger.info("Agent finalized: " + agentName);
logger.warning("Max iterations reached. Stopping orchestration.");

// In AgentBuilder
logger.info("Invalid action: " + action + " - retrying...");

// In TransitionAgent
logger.warning(String.format("Transition denied: %s -> %s. Reason: %s", from, to, reason));
logger.info(String.format("Transition allowed: %s -> %s", from, to));
```

### Configure Logging

```java
import java.util.logging.*;

// Set log level
Logger rootLogger = Logger.getLogger("");
rootLogger.setLevel(Level.INFO);

// Custom formatter
ConsoleHandler handler = new ConsoleHandler();
handler.setFormatter(new SimpleFormatter() {
    @Override
    public synchronized String format(LogRecord record) {
        return String.format(
            "[%s] %s - %s%n",
            record.getLevel(),
            record.getLoggerName(),
            record.getMessage()
        );
    }
});
rootLogger.addHandler(handler);
```

### Transition Listeners

```java
orchestrator = AgentOrchestrator.builder(agents)
    .addTransitionListener((from, to) -> {
        // Console logging
        System.out.println(from.getName() + " → " + to.getName());

        // File logging
        logToFile(from.getName(), to.getName());

        // Metrics collection
        metrics.increment("transitions",
            Map.of("from", from.getName(), "to", to.getName()));

        // Distributed tracing
        span.addEvent("agent_transition",
            Map.of("from_agent", from.getName(), "to_agent", to.getName()));
    })
    .build();
```

### Monitoring Best Practices

```java
public class MonitoringOrchestrator {
    public static void main(String[] args) throws Exception {
        // Track orchestration metrics
        long startTime = System.currentTimeMillis();
        int[] transitionCount = {0};

        AgentOrchestrator orchestrator = AgentOrchestrator.builder(agents)
            .enableLogging(true)
            .addTransitionListener((from, to) -> {
                transitionCount[0]++;
                System.out.println(String.format(
                    "Transition #%d: %s → %s (elapsed: %dms)",
                    transitionCount[0],
                    from.getName(),
                    to.getName(),
                    System.currentTimeMillis() - startTime
                ));
            })
            .build();

        ApiResponse result = orchestrator.process();

        System.out.println("=== Orchestration Summary ===");
        System.out.println("Total transitions: " + transitionCount[0]);
        System.out.println("Total duration: " + (System.currentTimeMillis() - startTime) + "ms");
        System.out.println("Final answer: " + result.getAnswer());
    }
}
```

## Error Handling and Retries

### LLM Call Retries (ChatGptClient)

**Retry Strategy:**
- **Max Attempts:** 5
- **Backoff:** Exponential starting at 1 second
- **Retryable Errors:**
  - HTTP 429 (rate limit)
  - IOException
  - InterruptedException

**Implementation:**
```java
int retryCount = 0;
while (retryCount < 5) {
    try {
        return executeHttpCall();
    } catch (IOException | InterruptedException e) {
        retryCount++;
        if (retryCount >= 5) throw e;
        Thread.sleep(1000 * (long) Math.pow(2, retryCount));
    } catch (HttpException e) {
        if (e.getStatusCode() == 429) {
            retryCount++;
            Thread.sleep(1000 * (long) Math.pow(2, retryCount));
        } else if (e.getStatusCode() >= 400) {
            throw new IllegalStateException("HTTP error: " + e.getStatusCode());
        }
    }
}
```

### Action Validity Retries (AgentBuilder)

**Retry Strategy:**
- **Max Attempts:** 5
- **Validation:** Action string must map to `Action` enum
- **Failure:** Throws `IllegalStateException` after retries exhausted

**Implementation:**
```java
int retryCount = 0;
while (retryCount < 5) {
    ApiResponse response = client.execute(properties, prompt, responseId);
    if (isValidAction(response.getAction())) {
        return response;
    }
    retryCount++;
    logger.info("Invalid action: " + response.getAction() + " - retrying...");
}
throw new IllegalStateException("Failed to get valid action after 5 retries");
```

### Tool Execution Failures

Tool failures are **captured**, not retried:

```java
try {
    Object result = method.invoke(toolInstance, params);
    return CallableResponse.success(toolName, result);
} catch (Exception e) {
    return CallableResponse.error(toolName, e.getMessage());
}
```

**Error Propagation:**
- Errors serialized into `ApiResponse.toolOutput`
- LLM receives error details in next prompt
- LLM can retry tool with corrected parameters

### Best Practices

```java
// 1. Validate configuration before orchestration
if (Properties.INSTANCE.getOpenAIKey() == null) {
    throw new IllegalStateException("OpenAI API key not configured");
}

// 2. Set reasonable iteration limits
orchestrator.maxIterations(100);  // Prevent infinite loops

// 3. Handle orchestration exceptions
try {
    ApiResponse result = orchestrator.process();
    processResult(result);
} catch (IllegalStateException e) {
    logger.severe("Orchestration failed: " + e.getMessage());
    // Fallback logic
} catch (Exception e) {
    logger.severe("Unexpected error: " + e.getMessage());
    // Error recovery
}

// 4. Validate tool instances
if (toolInstance == null) {
    logger.warning("Agent has no tools configured");
}
```

## Known Limitations

### 1. TransitionAgent Strict Mode Partial Enforcement
**Issue:** In current logic, denied validator results may still lead to allowed transition after loop completion.

**Impact:** Strict mode may not fully prevent forbidden transitions.

**Workaround:** Use transition listeners to enforce additional validation.

### 2. Sequential Tool Strategy Only Returns First Result
**Issue:** `RunToolSequentialStrategy` serializes only `results.getFirst()` even when multiple tools run.

**Impact:** Subsequent tool results are lost.

**Workaround:** Use `RUN_TOOL_PARALLEL` or make separate sequential calls.

### 3. Shared Mutable Singleton Configuration
**Issue:** `Properties.INSTANCE` is process-global and not thread/request isolated.

**Impact:** Concurrent orchestrations share same configuration.

**Workaround:** Ensure configuration is set once at startup, not per-request.

### 4. Hardcoded Thread Pool Size
**Issue:** `DefaultToolRunnerImpl` uses fixed pool size of 50 threads.

**Impact:** No external tuning hook for different workloads.

**Workaround:** Fork implementation and adjust `Executors.newFixedThreadPool(50)`.

### 5. Tight Coupling to Response Schema
**Issue:** `ChatGptClient` expects specific JSON shape: `output[].content[0].text`.

**Impact:** Incompatible with non-OpenAI response formats.

**Workaround:** Implement custom `Client` interface for different providers.

### 6. No Built-in Persistence/Checkpointing
**Issue:** Context is in-memory only; no state serialization.

**Impact:** Cannot resume orchestration after crash.

**Workaround:** Implement custom persistence via transition listeners and context state.

### 7. No Tests Included
**Issue:** Repository has empty `src/test` directory.

**Impact:** No regression protection, unclear behavior edge cases.

**Recommendation:** Add integration and unit tests (see Roadmap).

### 8. External Dependency on ToolLibrary
**Issue:** Requires `com.grkn:ToolLibrary:1.0-SNAPSHOT` which may not be publicly available.

**Impact:** Build fails without this dependency.

**Workaround:** Install ToolLibrary locally or publish to private Maven repo.

## Troubleshooting

### Maven Dependency Resolution Fails for ToolLibrary

**Symptom:**
```
[ERROR] Failed to execute goal on project OrchestrationLLMs:
Could not resolve dependencies for project com.grkn:OrchestrationLLMs:1.0-SNAPSHOT:
Could not find artifact com.grkn:ToolLibrary:1.0-SNAPSHOT
```

**Fix:**
```bash
# 1. Install ToolLibrary to local Maven repository
cd path/to/ToolLibrary
mvn clean install

# 2. Verify installation
ls ~/.m2/repository/com/grkn/ToolLibrary/1.0-SNAPSHOT/

# 3. Rebuild this project
cd path/to/llm-agent-orchestrator
mvn clean package
```

**Alternative:** Configure private Maven repository in `~/.m2/settings.xml`.

### LLM Returns Invalid JSON / Invalid Action

**Symptom:**
```
INFO: Invalid action: run_tool - retrying...
INFO: Invalid action: execute_sequential - retrying...
Exception: Failed to get valid action after 5 retries
```

**Root Causes:**
- LLM not following JSON schema
- Action string doesn't match enum exactly
- Response parsing error

**Fix:**
```java
// 1. Verify model capability
Properties.INSTANCE.setOpenAIModel("gpt-4");  // Use more capable model

// 2. Tighten prompts with examples
String customPrompt = """
Your prompt here...

Example valid response:
{
  "action": "RUN_TOOL_SEQUENTIAL",
  "toolNames": ["read_file"],
  "inputs": [{"path": "/example.txt"}],
  "answer": "Reading file"
}
""";

// 3. Verify endpoint returns correct format
// Expected: output[].content[0].text contains JSON string
```

### Transition Exceptions

**Symptom:**
```
Exception: Transition from 'Planner' to 'Reviewer' is not allowed
```

**Root Cause:** Requested transition not registered in FSM.

**Fix:**
```java
// Add missing transition
orchestrator.addTransition("Planner", "Reviewer");

// OR allow TransitionAgent to auto-route
// (TransitionAgent will ask LLM to choose appropriate agent)

// OR enable recursive if same agent
orchestrator.allowRecursive("Planner");
```

### Null or Missing Tool Execution

**Symptom:**
```
toolOutput: {"error": "Tool not found: read_file"}
```

**Root Causes:**
- Tool method missing `@Tool` annotation
- Tool name mismatch
- Parameter missing `@ToolParameter`
- Tool instance not attached to agent

**Fix:**
```java
// 1. Verify annotation
@Tool(name = "read_file")  // Must match exactly
public String readFile(@ToolParameter String path) { ... }

// 2. Verify tool attachment
Agent agent = AgentBuilder.create("Developer")
    .toolInstance(new FileTools())  // Don't forget this!
    .build();

// 3. Verify LLM uses correct name
{
  "action": "RUN_TOOL_SEQUENTIAL",
  "toolNames": ["read_file"],  // Must match annotation exactly
  ...
}
```

### Max Iterations Reached

**Symptom:**
```
WARNING: Max iterations reached. Stopping orchestration.
```

**Root Causes:**
- Infinite loop between agents
- No agent finalizing
- TransitionAgent unable to find appropriate agent

**Fix:**
```java
// 1. Increase limit if workflow is legitimately long
orchestrator.maxIterations(200);

// 2. Add finalization condition to prompts
String prompt = """
You are a Reviewer. After verifying all work is complete,
you MUST use FINALIZE_TASK action to end the workflow.
""";

// 3. Debug transition loop with listener
orchestrator.addTransitionListener((from, to) -> {
    System.out.println(from.getName() + " -> " + to.getName());
    // Identify ping-pong between same agents
});
```

### Memory Issues with Large Contexts

**Symptom:** OutOfMemoryError or slow performance.

**Root Cause:** Context accumulating large objects across iterations.

**Fix:**
```java
// Clean up context in onExit
agent.onExit(context -> {
    context.setState("largeData", null);  // Clear after use
});

// Or use weak references for cached data
Map<String, Object> cache = new WeakHashMap<>();
context.setState("cache", cache);
```

## Roadmap Suggestions

High-impact improvements for evolving this project:

### 1. Add Comprehensive Tests
**Priority:** 🔴 Critical

```
- Unit tests for core classes (AgentStateMachine, AgentBuilder, etc.)
- Integration tests for orchestration workflows
- Mock LLM client for deterministic testing
- Tool execution tests
- Transition validation tests
```

### 2. Externalize Configuration
**Priority:** 🟡 High

```
- Replace Properties singleton with per-orchestration config
- Support multiple LLM providers simultaneously
- Environment-specific configuration (dev/staging/prod)
- Configurable retry/backoff parameters
- Configurable thread pool sizing
```

### 3. Add Structured Telemetry
**Priority:** 🟡 High

```
- OpenTelemetry integration
- Metrics: transition counts, duration, tool execution times
- Distributed tracing across agent boundaries
- Custom span attributes for agent metadata
- Prometheus exporter
```

### 4. Harden Transition Validation
**Priority:** 🟡 High

```
- Fix strict mode semantics
- Add transition pre-conditions and post-conditions
- Circuit breaker for failing transitions
- Transition timeout support
- Better error messages for denied transitions
```

### 5. Pluggable Prompt Templates
**Priority:** 🟢 Medium

```
- Template engine integration (Mustache, Freemarker)
- Per-agent custom templates
- Template versioning
- Multi-language template support
- Prompt optimization tools
```

### 6. Immutable Per-Run Configuration
**Priority:** 🟢 Medium

```
- Builder-based orchestration config
- Thread-safe multi-tenant support
- Configuration validation at build time
- Configuration snapshots for auditing
```

### 7. Streaming Response Support
**Priority:** 🟢 Medium

```
- SSE (Server-Sent Events) support
- Reactive streams integration
- Partial response handling
- Real-time progress updates
- Streaming tool output
```

### 8. Enhanced Tool System
**Priority:** 🟢 Medium

```
- Tool versioning and deprecation
- Tool composition (tool calling tools)
- Tool execution sandboxing
- Tool rate limiting
- Tool metrics and profiling
```

### 9. Persistence and Checkpointing
**Priority:** 🔵 Low

```
- Serialize orchestration state
- Resume from checkpoint
- State machine snapshots
- Audit trail of transitions
- Replay capabilities
```

### 10. Developer Experience
**Priority:** 🔵 Low

```
- CLI for testing agents
- Visual orchestration builder
- Agent debugging tools
- Performance profiler
- Documentation generator from code
```

## License

No license file is currently present in this repository.

**Recommendation:** Add a `LICENSE` file to define usage terms.

**Common Options:**
- **MIT** - Permissive, allows commercial use
- **Apache 2.0** - Permissive, includes patent grant
- **GPL-3.0** - Copyleft, derivatives must be open source

**Add License:**
```bash
# Create LICENSE file with your chosen license text
echo "MIT License" > LICENSE
echo "" >> LICENSE
echo "Copyright (c) $(date +%Y) Your Name" >> LICENSE
# ... add full license text
```

---

## Contributing

Contributions are welcome! This framework is designed to be extensible.

**Areas for Contribution:**
- Add tests (unit, integration, E2E)
- Improve error handling and validation
- Add examples and tutorials
- Implement additional action strategies
- Create tool library integrations
- Improve documentation

---

## Support

For issues, questions, or feature requests, please open an issue on the repository.

---

**Built with Java 21 | Powered by LLM Orchestration**
