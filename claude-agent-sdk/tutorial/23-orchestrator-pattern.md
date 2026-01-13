# Module 23: Orchestrator Pattern

Hierarchical multi-agent coordination.

## What You'll Learn

- The orchestrator pattern for multi-agent systems
- Defining a master agent that coordinates workers
- Delegating tasks and synthesizing results
- When to use orchestration vs simple parallelization

## What is the Orchestrator Pattern?

In the orchestrator pattern:
- A **master agent** (orchestrator) receives the task and decides how to delegate
- **Worker agents** are specialized for specific tasks
- The orchestrator collects worker outputs and synthesizes a cohesive result

```
ORCHESTRATOR (Master)
    ├── Worker 1 (security-worker)
    └── Worker 2 (quality-worker)
```

## Orchestrator vs Parallel

| Aspect | Parallel (Module 22) | Orchestrator (Module 23) |
|--------|---------------------|--------------------------|
| Control | Claude decides execution | Orchestrator agent decides |
| Synthesis | Claude aggregates | Orchestrator agent synthesizes |
| Flexibility | Fixed parallel execution | Dynamic delegation |
| Use case | Known parallel tasks | Complex coordination |

## Defining the Orchestrator Team

```java
String agentsJson = """
    {
      "orchestrator": {
        "description": "Master agent that coordinates analysis and synthesizes results",
        "prompt": "You are an orchestrator. Delegate to worker agents and synthesize their findings into a brief summary."
      },
      "security-worker": {
        "description": "Finds security issues",
        "prompt": "You are a security expert. Find security vulnerabilities. Be very brief."
      },
      "quality-worker": {
        "description": "Reviews code quality",
        "prompt": "You are a code quality expert. Find maintainability issues. Be very brief."
      }
    }
    """;
```

## Prompting the Orchestrator

Tell Claude it IS the orchestrator:

```java
String prompt = """
    You are the ORCHESTRATOR. Analyze this code by delegating to security-worker and quality-worker:

    public class UserAuth {
        String password = "admin123";
        public boolean login(String user, String pwd) {
            return pwd.equals(password);
        }
    }

    1. Briefly explain your delegation strategy
    2. Delegate to BOTH workers
    3. Synthesize findings (max 3 bullet points)
    """;
```

## Complete Example

```java
import org.springaicommunity.claude.agent.sdk.ClaudeClient;
import org.springaicommunity.claude.agent.sdk.ClaudeSyncClient;
import org.springaicommunity.claude.agent.sdk.config.PermissionMode;
import org.springaicommunity.claude.agent.sdk.parsing.ParsedMessage;
import org.springaicommunity.claude.agent.sdk.transport.CLIOptions;
import org.springaicommunity.claude.agent.sdk.types.AssistantMessage;
import org.springaicommunity.claude.agent.sdk.types.ResultMessage;

import java.nio.file.Path;
import java.util.Iterator;

String agentsJson = """
    {
      "orchestrator": {
        "description": "Coordinates analysis and synthesizes results",
        "prompt": "You are an orchestrator. Delegate to workers and synthesize findings."
      },
      "security-worker": {
        "description": "Finds security issues",
        "prompt": "You are a security expert. Find vulnerabilities. Be very brief."
      },
      "quality-worker": {
        "description": "Reviews code quality",
        "prompt": "You are a code quality expert. Find maintainability issues. Be very brief."
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
        You are the ORCHESTRATOR. Analyze this code by delegating to security-worker and quality-worker:

        public class UserAuth {
            String password = "admin123";
            public boolean login(String user, String pwd) {
                return pwd.equals(password);
            }
        }

        1. Briefly explain delegation strategy
        2. Delegate to BOTH workers
        3. Synthesize findings (max 3 bullet points)
        """;

    client.connect(prompt);

    Iterator<ParsedMessage> response = client.receiveResponse();
    while (response.hasNext()) {
        ParsedMessage msg = response.next();
        if (msg.isRegularMessage()) {
            if (msg.asMessage() instanceof AssistantMessage am) {
                am.getTextContent().ifPresent(System.out::println);
            } else if (msg.asMessage() instanceof ResultMessage rm) {
                if (rm.totalCostUsd() != null) {
                    System.out.printf("[Cost: $%.4f]%n", rm.totalCostUsd());
                }
            }
        }
    }
}
```

## How It Works

```
1. Prompt tells Claude: "You are the ORCHESTRATOR"

2. Orchestrator (Claude with orchestrator role):
   - Analyzes the task
   - Decides delegation strategy
   - Spawns workers via Task tool

3. Workers execute independently:
   security-worker → security findings
   quality-worker → quality findings

4. Orchestrator synthesizes:
   - Collects all worker outputs
   - Prioritizes and deduplicates
   - Produces final summary
```

## Performance Tips

| Tip | Reason |
|-----|--------|
| Limit to 2-3 workers | 4+ workers can cause timeouts |
| Keep prompts brief | Shorter prompts = faster responses |
| Ask for concise output | "Be very brief", "max 3 bullets" |
| Explicit delegation | Tell orchestrator exactly what to do |

## Orchestrator Pattern Benefits

- **Separation of concerns**: Each worker specializes
- **Intelligent delegation**: Orchestrator decides who does what
- **Synthesis**: Master combines perspectives coherently
- **Scalability**: Add workers for new domains
- **Context preservation**: Orchestrator maintains overall context

## When to Use Orchestrator Pattern

| Scenario | Pattern |
|----------|---------|
| Fixed parallel analysis | Parallel (Module 22) |
| Dynamic delegation | Orchestrator |
| Need synthesis | Orchestrator |
| Simple multi-view | Parallel |
| Complex coordination | Orchestrator |

## Key Points

- Orchestrator pattern uses a master agent to coordinate workers
- Define orchestrator and workers in the same agents JSON
- Prompt Claude as "You are the ORCHESTRATOR"
- Give explicit delegation instructions (1, 2, 3 steps)
- Keep worker count low (2-3) for reliability
- Use brief prompts to optimize response time

## Source Code

[View on GitHub](https://github.com/spring-ai-community/claude-agent-sdk-java-tutorial/tree/main/module-23-subagents-patterns)

## Running the Example

```bash
mvn compile exec:java -pl module-23-subagents-patterns
```

## Tutorial Complete

Congratulations! You've completed the Claude Agent SDK Java tutorial. You've learned:

- **Fundamentals**: Query API, sync/async clients, message types
- **Configuration**: CLI options, tool permissions, permission modes
- **Sessions**: Multi-turn conversations, resume, fork
- **Safety & Control**: Permission callbacks, hooks, interrupt handling
- **MCP Integration**: External servers, multiple servers, hooks integration
- **Multi-Agent**: Subagents, parallel execution, orchestrator pattern

For more details, see the [API Reference](/claude-agent-sdk/reference/java).
