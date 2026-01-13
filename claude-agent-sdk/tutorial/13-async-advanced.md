# Module 13: Advanced Async Patterns

Cross-turn handlers and advanced reactive patterns.

## What You'll Learn

- Cross-turn message handlers for logging and metrics
- Error handling patterns in reactive streams
- Resource management for long-lived clients
- When to use advanced patterns vs simple TurnSpec

## Prerequisites

This module assumes you've completed [Module 04: ClaudeAsyncClient](/claude-agent-sdk/tutorial/04-async-client) and understand the TurnSpec pattern.

## Cross-Turn Message Handlers

Register handlers that receive messages across all conversation turns:

```java
ClaudeAsyncClient client = ClaudeClient.async()
    .workingDirectory(Path.of("."))
    .build();

// These handlers receive ALL messages, across ALL turns
client.onMessage(msg -> {
    logger.info("Message received: {}", msg.getType());
});

client.onResult(result -> {
    metrics.recordCost(result.totalCostUsd());
    metrics.recordTurns(result.numTurns());
});
```

## Key Reactive Operators

| Operator | Purpose |
|----------|---------|
| `doOnNext` | Side effects for each element |
| `doOnSuccess` | Side effect for Mono result |
| `flatMap` | Chain to next operation (enables multi-turn) |
| `filter` | Select specific message types |
| `doOnError` | Handle errors |
| `subscribe()` | Start the stream (nothing happens until you subscribe) |

## Error Handling

```java
client.query(prompt).messages()
    .doOnError(error -> {
        logger.error("Stream error", error);
    })
    .onErrorResume(error -> Flux.empty())
    .subscribe();
```

## Use Case: Spring WebFlux SSE Endpoint

One common use case is streaming responses via Server-Sent Events:

```java
@RestController
public class ChatController {
    private final ClaudeAsyncClient client;

    public ChatController() {
        this.client = ClaudeClient.async()
            .workingDirectory(Path.of("."))
            .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
            .build();
    }

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestParam String message) {
        return client.query(message).textStream();
    }
}
```

This pattern applies to any reactive web framework, not just Spring WebFlux.

## When to Use ClaudeAsyncClient

| Scenario | Recommendation |
|----------|----------------|
| Reactive web application | ClaudeAsyncClient |
| SSE streaming to browser | ClaudeAsyncClient |
| High-concurrency server | ClaudeAsyncClient |
| CLI tool or script | ClaudeSyncClient |
| Traditional blocking web app | ClaudeSyncClient |
| Simpler debugging needs | ClaudeSyncClient |

## Tradeoffs and Limitations

**Complexity**: Reactive programming has a steeper learning curve. Stack traces are harder to read, and debugging requires understanding the reactive execution model.

**Error handling**: Errors propagate differently in reactive streams. Forgetting to handle errors in the reactive chain can cause silent failures.

**Blocking operations**: Mixing blocking calls (e.g., JDBC) with reactive streams defeats the purpose and can cause thread starvation. Use reactive-compatible libraries throughout.

**Testing**: Unit testing reactive code requires `StepVerifier` or similar utilities. Standard JUnit assertions don't work directly with `Flux` and `Mono`.

**Resource management**: The async client is typically a long-lived managed bean (not created per-request). Close it during application shutdown, not after each request.

## Source Code

[View on GitHub](https://github.com/spring-ai-community/claude-agent-sdk-java-tutorial/tree/main/module-13-async-advanced)

## Running the Example

```bash
mvn compile exec:java -pl module-13-async-advanced
```

## Next Module

Module 14: Permission Callbacks (coming soon) - Dynamic control over tool execution.
