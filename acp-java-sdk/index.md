---
title: "ACP Java SDK"
sidebarTitle: "Overview"
description: "A Java SDK for the Agent Client Protocol — build both clients and agents that work with Zed, JetBrains, VS Code, and any ACP-compliant editor."
---

A pure Java implementation of the [Agent Client Protocol (ACP)](https://agentclientprotocol.com/) specification. Build clients that connect to ACP agents, or build agents that run inside code editors.

## Overview

The ACP Java SDK provides:

- **Client SDK** — connect to and interact with ACP-compliant agents (Gemini, custom agents)
- **Agent SDK** — build ACP-compliant agents that work in Zed, JetBrains, and VS Code
- **Test utilities** — in-memory transports for fast, deterministic testing

## Three Agent API Styles

| Style | Entry Point | Best For |
|-------|-------------|----------|
| **Annotation-based** | `@AcpAgent`, `@Prompt` | Least boilerplate, Spring-like feel |
| **Sync** | `AcpAgent.sync()` | Blocking handlers, plain return values |
| **Async** | `AcpAgent.async()` | Reactive applications, Project Reactor `Mono` |

All three styles produce identical protocol behavior. Choose based on programming preference.

## Quick Start

Add the dependency:

```xml
<dependency>
    <groupId>com.agentclientprotocol</groupId>
    <artifactId>acp-core</artifactId>
    <version>0.9.0</version>
</dependency>
```

For annotation-based agents, add `acp-agent-support` instead (includes `acp-core` transitively):

```xml
<dependency>
    <groupId>com.agentclientprotocol</groupId>
    <artifactId>acp-agent-support</artifactId>
    <version>0.9.0</version>
</dependency>
```

### Client — Connect to an Agent

```java
import com.agentclientprotocol.sdk.client.*;
import com.agentclientprotocol.sdk.client.transport.*;
import com.agentclientprotocol.sdk.spec.AcpSchema.*;
import java.util.List;

var params = AgentParameters.builder("gemini").arg("--experimental-acp").build();
var transport = new StdioAcpClientTransport(params);

AcpSyncClient client = AcpClient.sync(transport).build();
client.initialize();
var session = client.newSession(new NewSessionRequest("/workspace", List.of()));
var response = client.prompt(new PromptRequest(
    session.sessionId(),
    List.of(new TextContent("Hello, world!"))
));

client.close();
```

### Agent — Annotation-Based

```java
import com.agentclientprotocol.sdk.annotation.*;
import com.agentclientprotocol.sdk.agent.SyncPromptContext;
import com.agentclientprotocol.sdk.agent.support.AcpAgentSupport;
import com.agentclientprotocol.sdk.spec.AcpSchema.*;

@AcpAgent
class HelloAgent {

    @Initialize
    InitializeResponse init() {
        return InitializeResponse.ok();
    }

    @NewSession
    NewSessionResponse newSession() {
        return new NewSessionResponse(UUID.randomUUID().toString(), null, null);
    }

    @Prompt
    PromptResponse prompt(PromptRequest req, SyncPromptContext ctx) {
        ctx.sendMessage("Hello from the agent!");
        return PromptResponse.endTurn();
    }
}

AcpAgentSupport.create(new HelloAgent())
    .transport(new StdioAcpAgentTransport())
    .run();
```

## Documentation

<CardGroup cols={2}>
  <Card title="API Reference" icon="book" href="/acp-java-sdk/reference/java">
    Client API, Agent API (all three styles), protocol types, transports, errors
  </Card>
  <Card title="Tutorial" icon="graduation-cap" href="/acp-java-sdk/tutorial">
    30-module progressive tutorial from client basics to IDE integration
  </Card>
</CardGroup>

## Resources

- [GitHub Repository](https://github.com/agentclientprotocol/java-sdk) — Source code
- [ACP Java Tutorial](https://github.com/markpollack/acp-java-tutorial) — 30 hands-on modules
- [Agent Client Protocol](https://agentclientprotocol.com/) — Official ACP specification
