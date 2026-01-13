# Module 12: Session Fork

Creating parallel conversation branches for exploration.

## What You'll Learn

- Forking a session to create independent branches
- Using `CLIOptions.forkSession(true)`
- When forking is preferable to continuing

## Fork vs Resume

| Operation | Context | History |
|-----------|---------|---------|
| Resume | Continues same session | Adds to original history |
| Fork | Copies context | Creates new, independent history |

## Why Fork?

Forking creates a snapshot you can explore without affecting the original:

- Try alternative approaches
- Run "what if" scenarios
- Parallel experimentation
- Safe exploration without side effects

## Forking a Session

```java
import org.springaicommunity.claude.agent.sdk.ClaudeClient;
import org.springaicommunity.claude.agent.sdk.ClaudeSyncClient;
import org.springaicommunity.claude.agent.sdk.config.PermissionMode;
import org.springaicommunity.claude.agent.sdk.transport.CLIOptions;
import org.springaicommunity.claude.agent.sdk.types.Message;
import org.springaicommunity.claude.agent.sdk.types.ResultMessage;
import java.nio.file.Path;

// Original session - extract session ID
String originalSessionId = null;
try (ClaudeSyncClient client = ClaudeClient.sync()
        .workingDirectory(Path.of("."))
        .model(CLIOptions.MODEL_HAIKU)
        .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
        .build()) {

    for (Message msg : client.connectAndReceive("We're building an app with PostgreSQL.")) {
        System.out.println(msg);
        if (msg instanceof ResultMessage rm) {
            originalSessionId = rm.sessionId();
        }
    }
}

// Fork for alternative exploration
CLIOptions forkOptions = CLIOptions.builder()
    .model(CLIOptions.MODEL_HAIKU)
    .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
    .forkSession(true)  // Enable forking
    .build();

try (ClaudeSyncClient forkedClient = ClaudeClient.sync(forkOptions)
        .workingDirectory(Path.of("."))
        .build()) {

    forkedClient.connect();
    forkedClient.query("What if we used MongoDB instead?", originalSessionId);

    // Creates a new branch with different session ID
    String forkedSessionId = null;
    for (Message msg : forkedClient.messages()) {
        System.out.println(msg);
        if (msg instanceof ResultMessage rm) {
            forkedSessionId = rm.sessionId();
            // forkedSessionId != originalSessionId
        }
    }
}
```

## How Forking Works

```
                    ┌──────────────────────┐
                    │  Original Session    │
                    │  "Using PostgreSQL"  │
                    └──────────┬───────────┘
                               │
           ┌───────────────────┴───────────────────┐
           │                                       │
           ▼                                       ▼
┌──────────────────────┐             ┌──────────────────────┐
│  Continue Original   │             │  Forked Session      │
│  "ORM for PostgreSQL?"│             │  "What about MongoDB?"│
│  Context: PostgreSQL │             │  Context: PostgreSQL  │
│                      │             │  (copied at fork)     │
└──────────────────────┘             └──────────────────────┘
```

## Fork Use Cases

| Scenario | Benefit |
|----------|---------|
| A/B testing prompts | Compare approaches side-by-side |
| Exploring alternatives | "What if we did X instead?" |
| Safe experimentation | Try risky changes without affecting main flow |
| User branching | Let users explore different paths |

## Key Points

- Fork copies context but creates a new session ID
- Original session remains unchanged
- Both branches can continue independently
- Forked sessions can be forked again

## Tradeoffs

- **Storage multiplication**: Each fork creates a full copy of the session state. Forking a large conversation multiple times increases storage proportionally.
- **No merge capability**: Forked branches cannot be merged back. Insights from one branch must be manually transferred to another.
- **Coordination complexity**: Managing multiple active branches requires tracking multiple session IDs and their relationships.
- **Context duplication cost**: Each forked branch carries the full history. Querying both branches costs the same as two independent conversations of that length.

<Warning>
Avoid forking for routine follow-up questions. Fork only when you need to preserve the original session state for later use.
</Warning>

## Source Code

[View on GitHub](https://github.com/spring-ai-community/claude-agent-sdk-java-tutorial/tree/main/module-12-session-fork)

## Running the Example

```bash
mvn compile exec:java -pl module-12-session-fork
```

## Next Module

[Module 13: Advanced Async Patterns](/claude-agent-sdk/tutorial/13-async-advanced) - Cross-turn handlers and advanced reactive patterns.
