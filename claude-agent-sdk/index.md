---
title: "Claude Agent SDK for Java"
sidebarTitle: "Overview"
description: "A Java SDK for integrating with Claude Code CLI, enabling programmatic control of Claude as an AI coding agent."
---

A Java SDK for integrating with Claude Code CLI, enabling programmatic control of Claude as an AI coding agent.

## Overview

The Claude Agent SDK allows Java applications to:

- Execute queries and receive responses from Claude
- Maintain multi-turn conversation sessions
- Control tool permissions and safety settings
- Integrate with MCP (Model Context Protocol) servers
- Intercept and customize tool execution with hooks

## Three-API Architecture

| API | Class | Programming Style | Best For |
|-----|-------|-------------------|----------|
| **One-shot** | `Query` | Static methods | CLI tools, scripts, simple queries |
| **Blocking** | `ClaudeSyncClient` | Iterator-based | Traditional applications, synchronous workflows |
| **Reactive** | `ClaudeAsyncClient` | Flux/Mono | Non-blocking applications, high concurrency |

Both `ClaudeSyncClient` and `ClaudeAsyncClient` support the full feature set: multi-turn conversations, hooks, MCP integration, and permission callbacks. They differ only in programming paradigm (blocking vs non-blocking).

## Quick Start

Add the dependency to your project:

```xml
<dependency>
    <groupId>org.springaicommunity</groupId>
    <artifactId>claude-code-sdk</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Simple Query

```java
import org.springaicommunity.claude.agent.sdk.Query;

String answer = Query.text("What is 2 + 2?");
System.out.println(answer);  // "4"
```

### Multi-Turn Conversation

Both clients support multi-turn conversations with full context preservation. Choose based on your programming paradigm preference:

<Tabs>
  <Tab title="Blocking">
```java
import org.springaicommunity.claude.agent.sdk.ClaudeClient;
import org.springaicommunity.claude.agent.sdk.ClaudeSyncClient;
import java.nio.file.Path;

try (ClaudeSyncClient client = ClaudeClient.sync()
        .workingDirectory(Path.of("."))
        .build()) {

    String response1 = client.connectText("My favorite color is blue.");
    System.out.println(response1);

    String response2 = client.queryText("What is my favorite color?");
    System.out.println(response2);  // "blue"

    String response3 = client.queryText("Spell it backwards.");
    System.out.println(response3);  // "eulb"
}
```
  </Tab>
  <Tab title="Reactive">
```java
import org.springaicommunity.claude.agent.sdk.ClaudeClient;
import org.springaicommunity.claude.agent.sdk.ClaudeAsyncClient;
import java.nio.file.Path;

ClaudeAsyncClient client = ClaudeClient.async()
    .workingDirectory(Path.of("."))
    .build();

// Multi-turn with elegant flatMap chaining
client.connect("My favorite color is blue.").text()
    .doOnSuccess(System.out::println)
    .flatMap(r1 -> client.query("What is my favorite color?").text())
    .doOnSuccess(System.out::println)  // "blue"
    .flatMap(r2 -> client.query("Spell it backwards.").text())
    .doOnSuccess(System.out::println)  // "eulb"
    .subscribe();  // Non-blocking
```
  </Tab>
</Tabs>

## Documentation

<CardGroup cols={2}>
  <Card title="API Reference" icon="book" href="/claude-agent-sdk/reference/java">
    Complete API documentation for all classes, methods, and types
  </Card>
  <Card title="Tutorial" icon="graduation-cap" href="/claude-agent-sdk/tutorial">
    Step-by-step guide from basics to advanced patterns
  </Card>
</CardGroup>

## Resources

- [GitHub Repository](https://github.com/spring-ai-community/claude-agent-sdk-java) - Source code and examples
- [Claude Code Documentation](https://docs.anthropic.com/en/docs/agents-and-tools/claude-code/overview) - Official Claude Code docs
