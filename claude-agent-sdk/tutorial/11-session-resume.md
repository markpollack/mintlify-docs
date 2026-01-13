# Module 11: Session Resume

Continuing conversations across application restarts.

## What You'll Learn

- Extracting session IDs from `ResultMessage`
- Resuming previous sessions using `query(prompt, sessionId)`
- When to use session resume vs starting fresh

## The Session ID

Every conversation has a unique session ID returned in the `ResultMessage`:

```java
for (Message msg : client.connectAndReceive("Remember this: ALPHA-123")) {
    if (msg instanceof ResultMessage rm) {
        String sessionId = rm.sessionId();
        // Save this for later resumption
    }
}
```

## Resuming a Session

Use `query(prompt, sessionId)` to continue a previous conversation:

```java
import org.springaicommunity.claude.agent.sdk.ClaudeClient;
import org.springaicommunity.claude.agent.sdk.ClaudeSyncClient;
import org.springaicommunity.claude.agent.sdk.config.PermissionMode;
import org.springaicommunity.claude.agent.sdk.transport.CLIOptions;
import org.springaicommunity.claude.agent.sdk.types.Message;
import org.springaicommunity.claude.agent.sdk.types.ResultMessage;
import java.nio.file.Path;

// Session 1: Create conversation and save session ID
String savedSessionId = null;
try (ClaudeSyncClient client = ClaudeClient.sync()
        .workingDirectory(Path.of("."))
        .model(CLIOptions.MODEL_HAIKU)
        .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
        .build()) {

    // Get conversation going and extract session ID
    for (Message msg : client.connectAndReceive("Remember this code: ALPHA-123")) {
        System.out.println(msg);
        if (msg instanceof ResultMessage rm) {
            savedSessionId = rm.sessionId();
        }
    }
}

// Later: Resume the conversation
try (ClaudeSyncClient client = ClaudeClient.sync()
        .workingDirectory(Path.of("."))
        .model(CLIOptions.MODEL_HAIKU)
        .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
        .build()) {

    client.connect();  // Connect without initial prompt
    client.query("What code did I tell you?", savedSessionId);

    // Claude remembers: "ALPHA-123"
    for (Message msg : client.messages()) {
        System.out.println(msg);
    }
}
```

## How Session Resume Works

```
┌─────────────────────────────────────────────────────────┐
│  Session 1 (sessionId: abc-123)                         │
│    User: "Remember code ALPHA-123"                      │
│    Claude: "I'll remember that"                         │
│    [Session ends, app restarts]                         │
└─────────────────────────────────────────────────────────┘
                          ↓
                   Save sessionId
                          ↓
┌─────────────────────────────────────────────────────────┐
│  Session 2 (resuming abc-123)                           │
│    User: "What code did I tell you?"                    │
│    Claude: "The code is ALPHA-123"                      │
│    [Context restored from previous session]             │
└─────────────────────────────────────────────────────────┘
```

## Storing Session IDs

For persistence across restarts, store the session ID:

| Storage | Use Case |
|---------|----------|
| Database | Long-term conversations, multi-user apps |
| Redis | Short-term caching, distributed systems |
| File | Single-user applications |
| Memory | Same-process resume (testing) |

## When to Use Session Resume

| Scenario | Use Resume? |
|----------|-------------|
| App restart during user session | Yes |
| User returns next day | Yes |
| Context no longer relevant | No, start fresh |
| Security requires isolation | No |

## Limitations

- **Session expiration**: Sessions stored by Claude CLI have a retention period. Very old session IDs may become invalid.
- **Storage dependency**: Your application must persist session IDs. If lost, the conversation cannot be resumed.
- **No context pruning**: Resumed sessions include the full history. You cannot trim old messages to reduce token usage.
- **Model changes**: Resuming a session started with one model using a different model may produce inconsistent behavior.
- **Security consideration**: Anyone with the session ID can resume the conversation. Treat session IDs as sensitive data.

<Warning>
Do not resume sessions when the previous context contains outdated or incorrect information that should be discarded.
</Warning>

## Source Code

[View on GitHub](https://github.com/spring-ai-community/claude-agent-sdk-java-tutorial/tree/main/module-11-session-resume)

## Running the Example

```bash
mvn compile exec:java -pl module-11-session-resume
```

## Next Module

[Module 12: Session Fork](/claude-agent-sdk/tutorial/12-session-fork) - Creating parallel conversation branches.
