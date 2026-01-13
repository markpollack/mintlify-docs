# Module 04: ClaudeAsyncClient

Reactive, composable, non-blocking chains with Project Reactor.

## What You'll Learn

- Creating clients with `ClaudeClient.async()`
- The TurnSpec pattern for response handling
- Multi-turn conversations with flatMap chaining
- When to choose async vs sync clients

## ClaudeSyncClient vs ClaudeAsyncClient

Both clients provide identical capabilities. They differ only in programming paradigm:

| Feature | ClaudeSyncClient | ClaudeAsyncClient |
|---------|------------------|-------------------|
| Style | Blocking, sequential | Reactive, composable |
| Returns | `String`, `Iterable<Message>` | `TurnSpec` → `Mono`/`Flux` |
| Multi-turn | Supported | Supported |
| Hooks/MCP | Supported | Supported |
| Best for | Simple scripts, CLI tools | Reactive apps, composition |

## TurnSpec: Response Handling

`ClaudeAsyncClient` returns a `TurnSpec` that provides three ways to handle responses:

| Method | Returns | Use Case |
|--------|---------|----------|
| `.text()` | `Mono<String>` | Collected text, enables flatMap chaining |
| `.textStream()` | `Flux<String>` | Streaming text as it arrives |
| `.messages()` | `Flux<Message>` | All message types for metadata access |

## Multi-Turn Conversation

The `.text()` method returns `Mono<String>`, enabling elegant flatMap chaining:

```java
import org.springaicommunity.claude.agent.sdk.ClaudeClient;
import org.springaicommunity.claude.agent.sdk.ClaudeAsyncClient;
import java.nio.file.Path;

ClaudeAsyncClient client = ClaudeClient.async()
    .workingDirectory(Path.of("."))
    .build();

// Multi-turn with elegant flatMap chaining
client.connect("What's the capital of France?").text()
    .doOnSuccess(System.out::println)  // "Paris"
    .flatMap(r1 -> client.query("What's the population of that city?").text())
    .doOnSuccess(System.out::println)  // Claude remembers "Paris"
    .flatMap(r2 -> client.query("What are its famous landmarks?").text())
    .doOnSuccess(System.out::println)
    .subscribe();  // Non-blocking
```

## Text Streaming

For streaming text as it arrives:

```java
client.query("Explain recursion").textStream()
    .doOnNext(System.out::print)
    .subscribe();
```

## Full Message Access

When you need metadata, tool use details, or cost information:

```java
import org.springaicommunity.claude.agent.sdk.types.Message;
import org.springaicommunity.claude.agent.sdk.types.AssistantMessage;
import org.springaicommunity.claude.agent.sdk.types.ResultMessage;

client.query("List files").messages()
    .doOnNext(msg -> {
        System.out.println(msg);  // All types have useful toString()

        if (msg instanceof AssistantMessage am) {
            am.getToolUses().forEach(tool ->
                System.out.println("Tool used: " + tool.name()));
        } else if (msg instanceof ResultMessage rm) {
            System.out.printf("Cost: $%.6f%n", rm.totalCostUsd());
        }
    })
    .subscribe();
```

## Key Points

- **Factory pattern**: Use `ClaudeClient.async()` to create clients
- **TurnSpec pattern**: `connect()`/`query()` return `TurnSpec`, not void
- **flatMap for multi-turn**: Chain turns with `.text().flatMap(r -> ...)`
- **Non-blocking**: Use `.subscribe()`, avoid `.block()`
- **Same capabilities**: Hooks, MCP, permissions all work identically to sync

## ClaudeAsyncClient Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `connect(prompt)` | `TurnSpec` | Start session with prompt |
| `query(prompt)` | `TurnSpec` | Send follow-up message |
| `onMessage(handler)` | `ClaudeAsyncClient` | Register cross-turn message handler |
| `onResult(handler)` | `ClaudeAsyncClient` | Register cross-turn result handler |
| `close()` | `Mono<Void>` | End session |

## TurnSpec Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `.text()` | `Mono<String>` | Collected text response |
| `.textStream()` | `Flux<String>` | Streaming text |
| `.messages()` | `Flux<Message>` | All message types |

## Builder Options

```java
ClaudeClient.async()
    .workingDirectory(Path.of("."))       // Required
    .model("claude-sonnet-4-20250514")    // Model selection
    .appendSystemPrompt("Be concise")      // Add to default prompt (recommended)
    .timeout(Duration.ofMinutes(5))        // Timeout
    .permissionMode(PermissionMode.DEFAULT) // Permission handling
    .hookRegistry(hookRegistry)            // Tool hooks
    .mcpServer("name", config)             // MCP servers
    .build();
```

## When to Choose

| Scenario | Recommendation |
|----------|----------------|
| Simple script or CLI | ClaudeSyncClient |
| Traditional blocking app | ClaudeSyncClient |
| Simpler debugging | ClaudeSyncClient |
| Reactive application | ClaudeAsyncClient |
| Composing with other reactive streams | ClaudeAsyncClient |
| High-concurrency server | ClaudeAsyncClient |

## Source Code

[View on GitHub](https://github.com/spring-ai-community/claude-agent-sdk-java-tutorial/tree/main/module-04-async-client)

## Running the Example

```bash
mvn compile exec:java -pl module-04-async-client
```

## Next Module

[Module 05: Message Types](/claude-agent-sdk/tutorial/05-message-types) - Understanding the different message types and content blocks.
