# Module 21: Subagents Introduction

Defining and spawning custom subagents.

## What You'll Learn

- Defining agents with JSON configuration
- Using the `--agents` CLI option via CLIOptions
- Instructing Claude to spawn subagents via the Task tool
- Agent definition structure (description and prompt)

## What are Subagents?

Subagents are specialized Claude instances with custom system prompts. When you define agents, Claude can spawn them using the Task tool to delegate specialized work.

## Agent Definition Format

Agents are defined in JSON:

```json
{
  "agent-name": {
    "description": "What the agent does (shown to Claude)",
    "prompt": "System prompt for the agent (its personality/role)"
  }
}
```

## Defining Agents with CLIOptions

Use `CLIOptions.builder().agents()` to define custom agents:

```java
import org.springaicommunity.claude.agent.sdk.ClaudeClient;
import org.springaicommunity.claude.agent.sdk.ClaudeSyncClient;
import org.springaicommunity.claude.agent.sdk.config.PermissionMode;
import org.springaicommunity.claude.agent.sdk.transport.CLIOptions;

import java.nio.file.Path;

// Define a code reviewer agent
String agentsJson = """
    {
      "code-reviewer": {
        "description": "Reviews code for best practices and potential issues",
        "prompt": "You are a code reviewer. Analyze code for bugs, performance issues, security vulnerabilities, and adherence to best practices. Provide constructive feedback in a concise format."
      }
    }
    """;

// Build CLI options with agents
CLIOptions options = CLIOptions.builder()
    .model(CLIOptions.MODEL_HAIKU)
    .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
    .agents(agentsJson)
    .build();

// Use sync(options) pattern to include agents
try (ClaudeSyncClient client = ClaudeClient.sync(options)
        .workingDirectory(Path.of("."))
        .build()) {

    // Tell Claude to use the agent
    client.connect("Use the code-reviewer agent to review this code: ...");
    // ...
}
```

## ClaudeClient.sync(options) Pattern

When using agents, you must use the `ClaudeClient.sync(CLIOptions)` pattern:

```java
// This pattern is required for agents
CLIOptions options = CLIOptions.builder()
    .agents(agentsJson)
    .build();

try (ClaudeSyncClient client = ClaudeClient.sync(options)
        .workingDirectory(Path.of("."))
        .build()) {
    // ...
}
```

The fluent builder (`ClaudeClient.sync().xyz()`) does not have an `.agents()` method.

## Complete Example

```java
import org.springaicommunity.claude.agent.sdk.ClaudeClient;
import org.springaicommunity.claude.agent.sdk.ClaudeSyncClient;
import org.springaicommunity.claude.agent.sdk.config.PermissionMode;
import org.springaicommunity.claude.agent.sdk.parsing.ParsedMessage;
import org.springaicommunity.claude.agent.sdk.transport.CLIOptions;
import org.springaicommunity.claude.agent.sdk.types.AssistantMessage;

import java.nio.file.Path;
import java.util.Iterator;

String agentsJson = """
    {
      "code-reviewer": {
        "description": "Reviews code for best practices and potential issues",
        "prompt": "You are a code reviewer. Analyze code for bugs, performance, and security. Be concise."
      }
    }
    """;

CLIOptions options = CLIOptions.builder()
    .model(CLIOptions.MODEL_HAIKU)
    .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
    .agents(agentsJson)
    .build();

try (ClaudeSyncClient client = ClaudeClient.sync(options)
        .workingDirectory(Path.of("."))
        .build()) {

    String prompt = """
        Use the code-reviewer agent to review this Java code:

        public class Example {
            public static void main(String[] args) {
                String password = "admin123";
                for(int i=0; i<1000; i++) {
                    System.out.println(password);
                }
            }
        }

        Provide a brief review with 2-3 key issues.
        """;

    client.connect(prompt);

    Iterator<ParsedMessage> response = client.receiveResponse();
    while (response.hasNext()) {
        ParsedMessage msg = response.next();
        if (msg.isRegularMessage() && msg.asMessage() instanceof AssistantMessage am) {
            am.getTextContent().ifPresent(System.out::println);
        }
    }
}
```

## How It Works

1. You define agents in JSON and pass to CLIOptions
2. Claude receives the agent definitions
3. When prompted to use an agent, Claude spawns it via the Task tool
4. The subagent runs with its defined system prompt
5. Results return to the main Claude instance

```
Main Claude → Task tool → Subagent (with custom prompt)
                              ↓
                         Does work
                              ↓
Main Claude ← Results ← Subagent
```

## Agent Design Tips

| Aspect | Guidance |
|--------|----------|
| Description | Short, tells Claude when to use this agent |
| Prompt | System prompt defining the agent's expertise |
| Naming | Use kebab-case: `code-reviewer`, `security-auditor` |
| Brevity | Ask agents to "be concise" to reduce response time |

## Key Points

- Define agents as JSON with `description` and `prompt` fields
- Use `CLIOptions.builder().agents(json)` to configure
- Must use `ClaudeClient.sync(options)` pattern (not fluent builder)
- Prompt Claude to "use the agent-name agent" to invoke
- Subagents run via Claude's Task tool

## Source Code

[View on GitHub](https://github.com/spring-ai-community/claude-agent-sdk-java-tutorial/tree/main/module-21-subagents-intro)

## Running the Example

```bash
mvn compile exec:java -pl module-21-subagents-intro
```

## Next Module

[Module 22: Subagents Parallel](/claude-agent-sdk/tutorial/22-subagents-parallel) - Running multiple agents in parallel.
