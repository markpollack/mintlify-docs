# Module 22: Subagents Parallel

Running multiple agents in parallel.

## What You'll Learn

- Defining multiple specialized agents
- Prompting Claude to execute agents in parallel
- Coordinating results from parallel agent executions
- Cost considerations for multi-agent workflows

## Multiple Agent Definitions

Define multiple agents in a single JSON configuration:

```java
String agentsJson = """
    {
      "analyzer": {
        "description": "Analyzes code structure, patterns, and architecture",
        "prompt": "You are a code analyzer. Examine code structure and design patterns. Provide 2-3 bullet points."
      },
      "security-auditor": {
        "description": "Audits code for security vulnerabilities",
        "prompt": "You are a security auditor. Find injection flaws, hardcoded secrets, unsafe operations. Be brief."
      },
      "performance-reviewer": {
        "description": "Reviews code for performance issues",
        "prompt": "You are a performance expert. Identify inefficiencies and optimization opportunities. Be concise."
      }
    }
    """;
```

## Prompting for Parallel Execution

Explicitly tell Claude to run agents in parallel:

```java
String prompt = """
    Review this code using ALL THREE agents (analyzer, security-auditor, performance-reviewer) IN PARALLEL:

    public class UserService {
        private static final String DB_PASSWORD = "secret123";

        public User findUser(String userId) {
            String query = "SELECT * FROM users WHERE id = '" + userId + "'";
            List<User> results = new ArrayList<>();
            for (int i = 0; i < 1000000; i++) {
                results.add(executeQuery(query));
            }
            return results.get(0);
        }
    }

    Run all three agents simultaneously and summarize their findings.
    """;
```

Claude will make parallel Task tool calls for each agent.

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
      "analyzer": {
        "description": "Analyzes code structure and patterns",
        "prompt": "You are a code analyzer. Examine structure and design patterns. Be brief (2-3 bullets)."
      },
      "security-auditor": {
        "description": "Finds security vulnerabilities",
        "prompt": "You are a security auditor. Find vulnerabilities. Be very brief."
      },
      "performance-reviewer": {
        "description": "Identifies performance issues",
        "prompt": "You are a performance expert. Find inefficiencies. Be concise."
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
        Review this code using ALL THREE agents (analyzer, security-auditor, performance-reviewer) IN PARALLEL:

        public class UserService {
            private static final String DB_PASSWORD = "secret123";

            public User findUser(String userId) {
                String query = "SELECT * FROM users WHERE id = '" + userId + "'";
                List<User> results = new ArrayList<>();
                for (int i = 0; i < 1000000; i++) {
                    results.add(executeQuery(query));
                }
                return results.get(0);
            }
        }

        Run all three agents simultaneously using the Task tool and summarize their findings.
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
                    System.out.printf("[Total Cost: $%.4f]%n", rm.totalCostUsd());
                }
            }
        }
    }
}
```

## How Parallel Execution Works

```
Main Claude receives prompt
         │
         ├──→ Task tool → Analyzer agent ──────┐
         │                                      │
         ├──→ Task tool → Security agent ──────┼──→ Results aggregated
         │                                      │
         └──→ Task tool → Performance agent ───┘
                                               │
Main Claude ←─────── Combined summary ─────────┘
```

## Result Aggregation

Claude automatically:
1. Collects results from all parallel agents
2. Categorizes findings by domain (security, performance, architecture)
3. Synthesizes into a coherent summary
4. Provides recommended actions

## Cost Considerations

Parallel execution increases cost:

| Component | Cost Factor |
|-----------|-------------|
| Main Claude | Base cost for orchestration |
| Each subagent | Additional API call |
| Result synthesis | Main Claude processes all results |

Example: 3 agents might cost ~3x a single agent, plus orchestration overhead.

## When to Use Parallel Agents

| Scenario | Benefit |
|----------|---------|
| Comprehensive code review | Security + performance + architecture in one pass |
| Multi-perspective analysis | Different experts examine same code |
| Time-sensitive reviews | Faster than sequential agent calls |
| Independent subtasks | Tasks that don't depend on each other |

## Key Points

- Define multiple agents in a single JSON configuration
- Explicitly prompt Claude to run agents "IN PARALLEL"
- Claude handles Task tool parallelization automatically
- Results are aggregated and summarized by main Claude
- Cost scales with number of agents

<Note>
Keep agent prompts brief with instructions like "Be concise" or "2-3 bullet points" to reduce response time and cost.
</Note>

## Source Code

[View on GitHub](https://github.com/spring-ai-community/claude-agent-sdk-java-tutorial/tree/main/module-22-subagents-parallel)

## Running the Example

```bash
mvn compile exec:java -pl module-22-subagents-parallel
```

## Next Module

[Module 23: Orchestrator Pattern](/claude-agent-sdk/tutorial/23-orchestrator-pattern) - Hierarchical multi-agent coordination.
