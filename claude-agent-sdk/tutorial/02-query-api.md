# Module 02: Query API

Deep dive into the Query class and its methods.

## What You'll Learn

- `Query.execute()` for getting response metadata
- `QueryResult` structure (text, cost, tokens, session ID)
- `QueryOptions` for configuring model, timeout, system prompt

## The Query Methods

| Method | Returns | Use When |
|--------|---------|----------|
| `Query.text(prompt)` | `String` | You only need the text response |
| `Query.execute(prompt)` | `QueryResult` | You need metadata (cost, tokens) |
| `Query.query(prompt)` | `Iterable<Message>` | You want to iterate over messages |

## Query.execute() with Metadata

```java
import org.springaicommunity.claude.agent.sdk.Query;
import org.springaicommunity.claude.agent.sdk.types.QueryResult;

QueryResult result = Query.execute("Write a haiku about Java");

// Get the text response
String text = result.text().orElse("(no response)");
System.out.println(text);

// Access metadata
System.out.printf("Cost: $%.6f%n", result.metadata().cost().calculateTotal());
System.out.printf("Input tokens: %d%n", result.metadata().usage().inputTokens());
System.out.printf("Output tokens: %d%n", result.metadata().usage().outputTokens());
System.out.printf("Duration: %d ms%n", result.metadata().durationMs());
System.out.printf("Session ID: %s%n", result.metadata().sessionId());
```

## QueryOptions Configuration

```java
import org.springaicommunity.claude.agent.sdk.QueryOptions;
import java.time.Duration;

QueryOptions options = QueryOptions.builder()
    .model("claude-sonnet-4-20250514")
    .appendSystemPrompt("Be concise. Answer in one sentence.")  // Add to defaults
    .timeout(Duration.ofMinutes(2))
    .build();

String response = Query.text("What is dependency injection?", options);
```

### Available Options

| Option | Type | Description |
|--------|------|-------------|
| `model` | `String` | Model to use (e.g., "claude-sonnet-4-20250514") |
| `systemPrompt` | `String` | System instructions for Claude |
| `timeout` | `Duration` | Maximum time to wait |
| `workingDirectory` | `Path` | Directory for file operations |
| `allowedTools` | `List<String>` | Tools Claude can use |
| `disallowedTools` | `List<String>` | Tools Claude cannot use |
| `maxTurns` | `Integer` | Maximum conversation turns |

## Source Code

[View on GitHub](https://github.com/spring-ai-community/claude-agent-sdk-java-tutorial/tree/main/module-02-query-api)

## Running the Example

```bash
mvn compile exec:java -pl module-02-query-api
```

## Next Module

[Module 03: ClaudeSyncClient](/claude-agent-sdk/tutorial/03-sync-client) - Blocking, simple sequential multi-turn conversations.
