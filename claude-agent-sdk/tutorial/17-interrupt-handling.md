# Module 17: Interrupt Handling

Graceful shutdown during Claude execution.

## What You'll Learn

- Setting up JVM shutdown hooks
- Using `client.interrupt()` to stop ongoing operations
- Thread-safe client access with AtomicReference
- Proper resource cleanup on shutdown

## Why Interrupt Handling Matters

Long-running Claude operations should handle interrupts gracefully:
- User presses Ctrl+C
- Process receives SIGTERM (container shutdown, deployment)
- Application needs to terminate cleanly

## Shutdown Hook Pattern

```java
import org.springaicommunity.claude.agent.sdk.ClaudeClient;
import org.springaicommunity.claude.agent.sdk.ClaudeSyncClient;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

// Thread-safe state for shutdown coordination
AtomicBoolean shutdownRequested = new AtomicBoolean(false);
AtomicReference<ClaudeSyncClient> activeClient = new AtomicReference<>();

// Register shutdown hook
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    System.out.println("Shutdown signal received...");
    shutdownRequested.set(true);

    ClaudeSyncClient client = activeClient.get();
    if (client != null) {
        try {
            System.out.println("Interrupting active Claude session...");
            client.interrupt();
            System.out.println("Session interrupted.");
        } catch (Exception e) {
            System.out.println("Error during interrupt: " + e.getMessage());
        }
    }
}, "claude-shutdown-hook"));
```

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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class InterruptHandlingExample {

    private static final AtomicBoolean shutdownRequested = new AtomicBoolean(false);
    private static final AtomicReference<ClaudeSyncClient> activeClient = new AtomicReference<>();

    public static void main(String[] args) {
        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[Shutdown] Signal received...");
            shutdownRequested.set(true);

            ClaudeSyncClient client = activeClient.get();
            if (client != null) {
                client.interrupt();
                System.out.println("[Shutdown] Session interrupted.");
            }
        }, "claude-shutdown-hook"));

        System.out.println("Shutdown hook registered. Press Ctrl+C to test.\n");

        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(Path.of("."))
                .model(CLIOptions.MODEL_HAIKU)
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .build()) {

            // Store reference for shutdown hook
            activeClient.set(client);

            // Execute tasks, checking shutdown state
            client.connect("What is 2 + 2?");
            printResponse(client);

            if (!shutdownRequested.get()) {
                client.query("List three colors.");
                printResponse(client);
            }

            // Clear reference before normal close
            activeClient.set(null);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

        System.out.println("Session completed.");
    }

    private static void printResponse(ClaudeSyncClient client) {
        if (shutdownRequested.get()) {
            System.out.println("[Skipped due to shutdown]");
            return;
        }

        Iterator<ParsedMessage> response = client.receiveResponse();
        while (response.hasNext() && !shutdownRequested.get()) {
            ParsedMessage msg = response.next();
            if (msg.isRegularMessage() && msg.asMessage() instanceof AssistantMessage am) {
                am.getTextContent().ifPresent(System.out::println);
            }
        }
    }
}
```

## Key Patterns

### AtomicReference for Thread-Safe Client Access

```java
// Store client reference when session starts
activeClient.set(client);

// In shutdown hook, safely retrieve
ClaudeSyncClient client = activeClient.get();
if (client != null) {
    client.interrupt();
}

// Clear before normal close
activeClient.set(null);
```

### Checking Shutdown State in Loops

```java
while (response.hasNext() && !shutdownRequested.get()) {
    // Process response...
}
```

### Skipping Remaining Work

```java
if (shutdownRequested.get()) {
    System.out.println("Shutdown requested, skipping remaining tasks.");
    return;
}
```

## How client.interrupt() Works

The `interrupt()` method sends SIGINT to the underlying Claude CLI process:
- Current operation stops gracefully
- Session can still be closed normally
- Resources are released properly

## Lifecycle

```
Normal Flow:
  start → set activeClient → process → clear activeClient → close

Interrupt Flow:
  start → set activeClient → process → [SIGINT] → hook fires
                                                       ↓
                                           interrupt() called
                                                       ↓
                                           process stops → close
```

## Key Points

- Use `AtomicReference` for thread-safe client access from shutdown hooks
- Use `AtomicBoolean` to track shutdown state
- Call `client.interrupt()` to stop ongoing operations gracefully
- Clear the client reference before normal close to avoid double-close
- Check shutdown state in processing loops for responsive termination

## Source Code

[View on GitHub](https://github.com/spring-ai-community/claude-agent-sdk-java-tutorial/tree/main/module-17-interrupt-handling)

## Running the Example

```bash
mvn compile exec:java -pl module-17-interrupt-handling
# Press Ctrl+C during execution to test interrupt handling
```

## Next Module

[Module 18: MCP External Servers](/claude-agent-sdk/tutorial/18-mcp-external) - Connecting to external MCP servers.
