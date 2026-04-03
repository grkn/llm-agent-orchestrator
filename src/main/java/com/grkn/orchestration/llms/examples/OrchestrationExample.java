package com.grkn.orchestration.llms.examples;

import com.grkn.orchestration.llms.dto.ApiResponse;
import com.grkn.orchestration.llms.fsm.Agent;
import com.grkn.orchestration.llms.fsm.AgentBuilder;
import com.grkn.orchestration.llms.fsm.AgentStateMachine;
import com.grkn.orchestration.llms.orchestrator.AgentOrchestrator;
import com.grkn.orchestration.llms.tools.Tools;

import java.util.Arrays;
import java.util.List;

/**
 * Example demonstrating multi-agent orchestration with LLM communication.
 *
 * This example shows:
 * 1. Creating multiple specialized agents
 * 2. Defining agent transitions
 * 3. Running orchestration workflow
 * 4. Agents communicating through LLM requests/responses
 */
public class OrchestrationExample {

    public static void main(String[] args) throws Exception {
        implementationWorkflow();
    }

    /**
     * Example: Code analysis, review, and optimization workflow
     */
    private static void implementationWorkflow() throws Exception {
        System.out.println("=== Code Workflow ===\n");

        Agent productOwner = AgentBuilder.create("product_owner")
                .prompt("""
                        You are a senior technical product owner.
                        You will analyze requirement and divide it into sub tasks and sub problems.
                        Sub problems will be asked to architect agent to get technical details.
                        Developer agent is going to implement it.
                        
                        Goal:
                        You will implement Spring Boot application with Java 17 and Spring Boot 3.0.
                        You have to implement a REST API with Spring WebFlux and Spring Data JPA.
                        You have to use h2 database for development and production.
                        You have to create Controller, Service, Repository, and DTO classes.
                        You have to implement CRUD operations for a simple entity.
                        
                        Current agent:
                        - product_owner
                        
                        Available Agents:
                        - developer
                        - architect
                        - tester
                        """)
                .onEnter(context -> {
                    System.out.println("[product_owner] product_owner analyzes requirement");
                })
                .build();

        Agent developer = AgentBuilder.create("developer")
                .toolInstance(new Tools())
                .prompt("""
                        You are a expert java developer.
                        You will implement requirement into the repository.
                        My operating system is windows so you can create under C:\\repo folder
                        if you have a technical question, you can ask question to architect for technical details.
                        if you have a requirement question, you can ask question to product owner agent
                        you can also request end to end test from tester agent by ASK_AGENT
                        
                        Rule:
                        - Developer agent will implement requirement and ask architect agent for technical details.
                        - If you have any question to architect, use ASK_AGENT action with "answer" field for question
                        - If you have question or work needs to be done, prioritize it and accept as temporary goal without losing main goal.
                        - Finalize task and return final result only.
                        - Review process must to be done with architect agent
                        
                        Your goal is to complete the requested engineering task using available tools.
                        
                        Repository path:
                        - C:\\repo
                        
                        Current agent:
                        - developer
                        
                        Available Agents:
                        - product_owner
                        - architect
                        - tester
                        """)
                .onEnter(context -> {
                    System.out.println("[developer] developer implements requirement");
                })
                .build();

        // Create Reviewer Agent
        Agent architect = AgentBuilder.create("architect")
                .toolInstance(new Tools())
                .prompt("""
                        You are a senior software architect.
                        You get requirements from product owner and you will design a architecture for implementation
                        Your responsibility is to design a technical architecture for implementation and code review.
                        After that you will ask developer agent to implement it.
                        
                        Rule:
                        - If you have any task to developer, use ASK_AGENT action with "answer" field for task
                        - Don't finalize task until developer agent will implement it.
                        
                        Current agent:
                        - architect
                        
                        Available Agents:
                        - product_owner
                        - developer
                        - tester
                        """)
                .onEnter(context -> {
                    System.out.println("[architect] architect designs technical architecture");
                })
                .build();

        Agent tester = AgentBuilder.create("tester")
                .toolInstance(new Tools())
                .prompt("""
                        You are a senior software java test engineer.
                        You will write end to end tests for existing implementation with selenium and bdd approach will be used.
                        If you have a question or bug, you can communicate with developer agent or architect agent for any improvement.
                       
                        Your goal is to complete the requested engineering task using available tools.
                        
                        Rule:
                        - Review process must to be done with architect agent
                        
                        Repository path:
                        - C:\\repo
                        
                        Current agent:
                        - tester
                        
                        Available Agents:
                        - product_owner
                        - developer
                        - architect
                        """)
                .onEnter(context -> {
                    System.out.println("[tester] tester implements end to end tests");
                })
                .build();

        List<Agent> agents = Arrays.asList(productOwner,  developer, architect, tester);

        AgentOrchestrator orchestrator = AgentOrchestrator.builder(agents)
                .initialAgent("product_owner")
                .addTransition("product_owner", "architect")
                .addTransition("product_owner", "developer")
                .addTransition("architect", "developer")
                .addTransition("architect", "tester")
                .addTransition("architect", "product_owner")
                .addTransition("developer", "architect")
                .addTransition("developer", "tester")
                .addTransition("tester", "developer")
                .addTransition("tester", "architect")
                .allowRecursive("developer")
                .addTransitionListener((from, to) -> System.out.printf("""
                        Asking question from agent: %s To agent: %s
                        %n""", from, to))
                .enableLogging(true)
                .maxIterations(100)
                .build();


        ApiResponse result = orchestrator.process();

        System.out.println("\n[Result] Code Implementation completed");
        System.out.println("\n" + "=".repeat(50) + "\n");

        System.out.println(result.toString());
    }


}
