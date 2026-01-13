# Module 03: ClaudeSyncClient

Multi-turn conversations with context preservation.

## What You'll Learn

- Creating clients with `ClaudeClient.sync()`
- Multi-turn conversations where Claude remembers context
- Simple text responses with `connectText()` and `queryText()`
- Full message access with `connectAndReceive()`

## Query vs ClaudeSyncClient

| Feature | `Query` | `ClaudeSyncClient` |
|---------|---------|-------------------|
| Context | New session each call | Preserved across calls |
| Use case | One-off questions | Conversations |
| Lifecycle | Automatic | Manual (try-with-resources) |
| Follow-ups | Not supported | Fully supported |
| Hooks/MCP | Not supported | Supported |

## Multi-Turn Conversation (Simple)

For most use cases, you just want the text response:

```java
import org.springaicommunity.claude.agent.sdk.ClaudeClient;
import org.springaicommunity.claude.agent.sdk.ClaudeSyncClient;
import java.nio.file.Path;

try (ClaudeSyncClient client = ClaudeClient.sync()
        .workingDirectory(Path.of("."))
        .build()) {

    String answer1 = client.connectText("What's the capital of France?");
    System.out.println(answer1);  // "Paris"

    String answer2 = client.queryText("What's the population of that city?");
    System.out.println(answer2);  // Claude remembers "Paris"

    String answer3 = client.queryText("What are its famous landmarks?");
    System.out.println(answer3);
}
```

## Multi-Turn with Message Access

When you need metadata, tool use details, or cost information:

```java
import org.springaicommunity.claude.agent.sdk.ClaudeClient;
import org.springaicommunity.claude.agent.sdk.ClaudeSyncClient;
import org.springaicommunity.claude.agent.sdk.types.Message;
import org.springaicommunity.claude.agent.sdk.types.AssistantMessage;
import org.springaicommunity.claude.agent.sdk.types.ResultMessage;
import java.nio.file.Path;

try (ClaudeSyncClient client = ClaudeClient.sync()
        .workingDirectory(Path.of("."))
        .build()) {

    for (Message msg : client.connectAndReceive("List files in current directory")) {
        System.out.println(msg);  // All message types have useful toString()

        if (msg instanceof AssistantMessage am) {
            am.getToolUses().forEach(tool ->
                System.out.println("Tool used: " + tool.name()));
        } else if (msg instanceof ResultMessage rm) {
            System.out.printf("Cost: $%.6f%n", rm.totalCostUsd());
        }
    }
}
```

## Key Points

- **Use try-with-resources**: Clients hold resources that must be released
- **Factory pattern**: Use `ClaudeClient.sync()` to create clients
- **Text methods for 80% use case**: `connectText()` and `queryText()` return `String`
- **Message iteration for 20% use case**: `connectAndReceive()` returns `Iterable<Message>`
- **Good toString()**: All message types print useful information

## ClaudeSyncClient Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `connectText(prompt)` | `String` | Start session, return text response |
| `queryText(prompt)` | `String` | Follow-up, return text response |
| `connectAndReceive(prompt)` | `Iterable<Message>` | Start session, iterate all messages |
| `queryAndReceive(prompt)` | `Iterable<Message>` | Follow-up, iterate all messages |
| `interrupt()` | `void` | Stop current operation |
| `close()` | `void` | End session (called by try-with-resources) |

## Builder Options

```java
ClaudeClient.sync()
    .workingDirectory(Path.of("."))       // Required
    .model("claude-sonnet-4-20250514")    // Model selection
    .appendSystemPrompt("Be concise")      // Add to default prompt (recommended)
    .timeout(Duration.ofMinutes(5))        // Timeout
    .permissionMode(PermissionMode.DEFAULT) // Permission handling
    .hookRegistry(hookRegistry)            // Tool hooks
    .mcpServer("name", config)             // MCP servers
    .build();
```

## Source Code

[View on GitHub](https://github.com/spring-ai-community/claude-agent-sdk-java-tutorial/tree/main/module-03-sync-client)

## Running the Example

```bash
mvn compile exec:java -pl module-03-sync-client
```

## Next Module

[Module 04: ClaudeAsyncClient](/claude-agent-sdk/tutorial/04-async-client) - Reactive, composable, non-blocking chains.
