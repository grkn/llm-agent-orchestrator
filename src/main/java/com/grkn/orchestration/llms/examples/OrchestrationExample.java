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

        Agent customer = AgentBuilder.create("customer")
                .prompt("""
                        Role:
                        You are a customer with business needs seeking a software solution.

                        Responsibilities:
                        - Communicate your business requirements and goals to the product_owner
                        - Answer clarifying questions about project requirements and expectations
                        - Provide clear, concise feedback on proposed solutions

                        Goal:
                        You want to build a website to sell your products online.
                        You provide a detailed description of your business requirements.
                        Let the product_owner analyze the requirements and provide a detailed plan.
                        After all preparation is done, you will be provided with working code the code.

                        Communication Guidelines:
                        - Be specific about your needs and constraints
                        - Ask for clarification when technical terms are unclear
                        - Focus on business value and user outcomes
                        - You can communicate with the product_owner and architect agents
                        """)
                .onEnter(context -> {
                    System.out.println("[customer] customer provides business requirements");
                })
                .build();

        Agent productOwner = AgentBuilder.create("product_owner")
                .prompt("""
                        Role:
                        You are a senior technical product owner responsible for bridging business needs and technical implementation.

                        Rules:
                        - Your tech stack is Java, Spring Boot, and PostgreSQL
                        - Analyze business requirements from the customer
                        - Break down requirements into actionable tasks and user stories
                        - Coordinate with architect for technical feasibility and design
                        - Ensure developer understands implementation requirements
                        - Validate final deliverables meet customer expectations
                        - After validation, make sure that developer agent and tester agent implements all requirements

                        Workflow:
                        1. Gather and clarify requirements from customer
                        2. Create structured breakdown of features and tasks
                        3. Consult architect for technical approach and design
                        4. Communicate implementation plan to developer
                        5. Review and validate completed work

                        Communication Rules:
                        - Ask customer for clarification on business requirements and priorities
                        - Request technical guidance and architecture decisions from architect
                        - Provide clear, prioritized requirements to developer
                        - Coordinate with developer on implementation details
                        """)
                .onEnter(context -> {
                    System.out.println("[product_owner] product_owner analyzes requirement");
                })
                .build();

        Agent developer = AgentBuilder.create("developer")
                .toolInstance(new Tools())
                .prompt("""
                        Role:
                        You are an expert Java developer responsible for implementing features and fixing issues.
                        You are going to write code for a website that sells products online.

                        Rules:
                        - Use modern Java language features
                        - Write clean, maintainable Java code following best practices
                        - Implement requirements provided by product_owner
                        - Follow architectural decisions and patterns from architect
                        - Collaborate with tester for test coverage and quality assurance
                        - Use available tools to read, write, and modify code
                        - You must proceed with the implementation
                        
                        Environment:
                        - Operating System: Windows
                        - Repository Path: C:\\repo

                        Workflow:
                        1. Understand requirements from product_owner
                        2. Consult architect for technical approach and design patterns
                        3. **ACTUALLY IMPLEMENT CODE** - Use CREATE_FILE/MODIFY_FILE tools to write files
                        4. Verify implementation by reading back files you created
                        5. Request code review from architect AFTER writing actual code
                        6. Coordinate with tester for end-to-end testing
                        7. Address feedback and iterate as needed

                        Communication Rules:
                        - Ask architect for technical design decisions, patterns, and best practices
                        - Ask product_owner for requirement clarifications
                        - Request tester to validate implementation with end-to-end tests
                        - Submit code for architect review AFTER you have written actual code

                        Implementation Requirements:
                        - Create complete Java classes with package declarations and imports
                        - Implement all methods with actual logic, not empty bodies
                        - Add proper error handling and validation
                        - Include configuration files (application.properties, pom.xml, etc.)
                        - Write working Spring Boot controllers, services, repositories
                        - Implement database entities with JPA annotations
                        - Add meaningful code comments for complex logic
                        - NEVER leave TODO comments - implement everything fully

                        Before Reporting Completion:
                        - Read back every file you created to verify it exists
                        - Ensure all code compiles and follows Java syntax
                        - Confirm all requirements are implemented, not just planned
                        """)
                .onEnter(context -> {
                    System.out.println("[developer] developer implements requirement");
                })
                .build();

        // Create Reviewer Agent
        Agent architect = AgentBuilder.create("architect")
                .toolInstance(new Tools())
                .prompt("""
                        Role:
                        You are a senior software architect responsible for technical design and quality assurance.

                        Rules:
                        - Design scalable, maintainable system architecture
                        - Define technical approach, patterns, and best practices
                        - Review code quality, design, and architectural consistency
                        - Guide developer on implementation strategies
                        - Ensure non-functional requirements (performance, security, maintainability)

                        Workflow:
                        1. Receive requirements from product_owner
                        2. Design architecture and technical approach
                        3. Provide implementation guidance to developer
                        4. Review code submissions from developer
                        5. Coordinate with tester on test strategy
                        6. Validate final implementation meets architectural standards

                        Communication Rules
                        - Ask product_owner for requirement clarifications and priorities
                        - Provide technical direction and implementation tasks to developer
                        - Conduct code reviews for developer submissions
                        - Guide tester on test architecture and coverage expectations

                        Review Criteria:
                        - Code follows SOLID principles and design patterns
                        - Proper separation of concerns and modularity
                        - Error handling and edge cases covered
                        - Performance and scalability considerations
                        - Code maintainability and readability
                        """)
                .onEnter(context -> {
                    System.out.println("[architect] architect designs technical architecture");
                })
                .build();

        Agent tester = AgentBuilder.create("tester")
                .toolInstance(new Tools())
                .prompt("""
                        Roles:
                        You are a senior software test engineer specializing in Java and end-to-end testing.

                        Rules:
                        - Write comprehensive end-to-end tests using Selenium and BDD approach
                        - Validate implementation meets requirements and works as expected
                        - Report bugs and quality issues to developer
                        - Ensure test coverage for critical user flows
                        - Use available tools to create and execute test suites

                        Environment:
                        - Repository Path: C:\\repo
                        - Testing Framework: Selenium + BDD (Behavior-Driven Development)

                        Workflow:
                        1. Understand requirements and acceptance criteria
                        2. Design test scenarios using BDD (Given-When-Then)
                        3. **ACTUALLY WRITE TEST CODE** - Use CREATE_FILE to create test files
                        4. Write feature files with Gherkin syntax
                        5. Implement step definitions with Selenium code
                        6. Create test runner classes
                        7. Execute tests using EXECUTE tool and report results
                        8. Report bugs to developer with reproduction steps
                        9. Submit test implementation for architect review

                        Communication Rules:
                        - Report bugs and issues to developer with clear reproduction steps
                        - Ask developer for clarification on implementation details
                        - Request architect review AFTER writing actual test code
                        - Coordinate with architect on test strategy and quality standards

                        Test Implementation Requirements:
                        - Create BDD .feature files with complete scenarios (Given-When-Then)
                        - Implement Java step definition classes with @Given, @When, @Then annotations
                        - Write Selenium WebDriver code with actual locators (CSS, XPath, ID)
                        - Add assertions using JUnit or TestNG (assertEquals, assertTrue, etc.)
                        - Implement page object models for maintainability
                        - Create test runner classes with Cucumber annotations
                        - Add proper setup (@Before) and teardown (@After) methods
                        - Include complete imports and dependencies
                        - Write working, executable test code - not TODO comments

                        Test Coverage Requirements:
                        - Implement tests for all happy paths
                        - Cover edge cases and error scenarios
                        - Test form validations and user interactions
                        - Verify database operations (CRUD)
                        - Test navigation and page flows

                        Before Reporting Completion:
                        - Read back test files to verify they exist
                        - Ensure tests use proper BDD structure and Selenium syntax
                        - Confirm all test scenarios are implemented, not just listed
                        """)
                .onEnter(context -> {
                    System.out.println("[tester] tester implements end to end tests");
                })
                .build();

        List<Agent> agents = Arrays.asList(customer, productOwner,  developer, architect, tester);

        AgentOrchestrator orchestrator = AgentOrchestrator.builder(agents)
                .initialAgent("customer")
                .addTransition("customer", "product_owner")
                .addTransition("customer", "developer")
                .addTransition("customer", "architect")
                .addTransition("architect", "customer")
                .addTransition("product_owner", "customer")
                .addTransition("product_owner", "architect")
                .addTransition("product_owner", "developer")
                .addTransition("product_owner", "tester")
                .addTransition("architect", "developer")
                .addTransition("architect", "tester")
                .addTransition("architect", "product_owner")
                .addTransition("developer", "architect")
                .addTransition("developer", "tester")
                .addTransition("tester", "developer")
                .addTransition("tester", "product_owner")
                .addTransition("tester", "architect")
                .allowRecursive("developer")
                .addTransition("developer", "product_owner")
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
