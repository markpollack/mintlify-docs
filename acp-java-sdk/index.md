---
title: "ACP Java SDK"
sidebarTitle: "Overview"
description: "A Java SDK for the Agent Client Protocol — build both clients and agents that work with Zed, JetBrains, VS Code, and any ACP-compliant editor."
---

A pure Java implementation of the [Agent Client Protocol (ACP)](https://agentclientprotocol.com/) specification. Build clients that connect to ACP agents, or build agents that run inside code editors.

## How ACP Works

ACP uses a subprocess model. A **client** (your application, or an editor like Zed) launches an **agent** as a child process and communicates over stdin/stdout using JSON-RPC messages. The protocol has three phases:

1. **Initialize** — client and agent exchange protocol versions and capabilities
2. **Session** — client creates a session with a working directory context
3. **Prompt** — client sends messages, agent streams back responses

This is the same mechanism that Zed, JetBrains, and VS Code use to talk to AI agents. The SDK lets you build either side of that conversation.

## Overview

The ACP Java SDK provides:

- **Client SDK** — connect to and interact with any ACP-compliant agent
- **Agent SDK** — build ACP-compliant agents that work in Zed, JetBrains, and VS Code
- **Test utilities** — in-memory transports for fast, deterministic testing

## Quick Start

### Try it now

The fastest way to see ACP in action is to clone the tutorial and run a module. The client example talks to [Gemini CLI](https://github.com/google-gemini/gemini-cli) (requires `GEMINI_API_KEY`). The agent example runs locally with no API key.

```bash
git clone https://github.com/markpollack/acp-java-tutorial.git
cd acp-java-tutorial

# Client: talk to Gemini as an ACP agent
export GEMINI_API_KEY=your-key-here
./mvnw exec:java -pl module-01-first-contact

# Agent: build and run your own (no API key needed)
./mvnw package -pl module-12-echo-agent -q
./mvnw exec:java -pl module-12-echo-agent
```

### Client — Connect to an Agent

This example launches [Gemini CLI](https://github.com/google-gemini/gemini-cli) as an ACP agent subprocess and sends it a prompt. Any CLI tool that speaks ACP over stdin/stdout works here — Gemini CLI is one such tool.

`AgentParameters` builds the command line (`gemini --experimental-acp`). `StdioAcpClientTransport` spawns the process and handles the JSON-RPC framing. The `sessionUpdateConsumer` receives the agent's response text as it streams in — without it, you'd get the stop reason but no visible output.

```java
import com.agentclientprotocol.sdk.client.*;
import com.agentclientprotocol.sdk.client.transport.*;
import com.agentclientprotocol.sdk.spec.AcpSchema.*;
import java.util.List;

// Launch Gemini CLI as an ACP agent subprocess
var params = AgentParameters.builder("gemini").arg("--experimental-acp").build();
var transport = new StdioAcpClientTransport(params);

AcpSyncClient client = AcpClient.sync(transport)
    .sessionUpdateConsumer(notification -> {
        // Print the agent's response as it streams in
        if (notification.update() instanceof AgentMessageChunk msg) {
            System.out.print(((TextContent) msg.content()).text());
        }
    })
    .build();

client.initialize();
var session = client.newSession(new NewSessionRequest(".", List.of()));

var response = client.prompt(new PromptRequest(
    session.sessionId(),
    List.of(new TextContent("What is 2+2? Reply with just the number."))
));

System.out.println("\nStop reason: " + response.stopReason());
client.close();
// Output: 4
// Stop reason: END_TURN
```

<Tip>
Run this yourself: [Module 01: First Contact](https://github.com/markpollack/acp-java-tutorial/tree/main/module-01-first-contact) — full source with error handling and setup.
</Tip>

### Agent — Build Your Own

This is the other side of the conversation. When you build an agent, **editors and clients launch your code** as a subprocess and send it JSON-RPC messages over stdin. Your agent handles three request types: initialize, new session, and prompt.

The stdio transport reads from stdin and writes to stdout. `run()` blocks until the client disconnects. The module includes a demo client that launches this agent as a subprocess and exercises it — you'll see `Echo: Hello!` printed when you run it.

```java
import com.agentclientprotocol.sdk.annotation.*;
import com.agentclientprotocol.sdk.agent.SyncPromptContext;
import com.agentclientprotocol.sdk.agent.support.AcpAgentSupport;
import com.agentclientprotocol.sdk.spec.AcpSchema.*;

@AcpAgent
class EchoAgent {

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
        String text = req.prompt().stream()
            .filter(c -> c instanceof TextContent)
            .map(c -> ((TextContent) c).text())
            .findFirst().orElse("");
        ctx.sendMessage("Echo: " + text);
        return PromptResponse.endTurn();
    }
}

// Reads JSON-RPC from stdin, writes to stdout — editors connect here
AcpAgentSupport.create(new EchoAgent())
    .transport(new StdioAcpAgentTransport())
    .run();
```

<Tip>
Run this yourself: [Module 12: Echo Agent](https://github.com/markpollack/acp-java-tutorial/tree/main/module-12-echo-agent) — includes a demo client that launches the agent and sends test prompts. No API key required.
</Tip>

### Adding the SDK to your project

```xml
<dependency>
    <groupId>com.agentclientprotocol</groupId>
    <artifactId>acp-core</artifactId>
    <version>0.9.0</version>
</dependency>
```

For annotation-based agents (as shown above), add `acp-agent-support` instead — it includes `acp-core` transitively:

```xml
<dependency>
    <groupId>com.agentclientprotocol</groupId>
    <artifactId>acp-agent-support</artifactId>
    <version>0.9.0</version>
</dependency>
```

## Three Agent API Styles

| Style | Entry Point | Best For |
|-------|-------------|----------|
| **Annotation-based** | `@AcpAgent`, `@Prompt` | Least boilerplate, declarative style |
| **Sync** | `AcpAgent.sync()` | Blocking handlers, plain return values |
| **Async** | `AcpAgent.async()` | Reactive applications, Project Reactor `Mono` |

All three styles produce identical protocol behavior. Choose based on programming preference.

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
