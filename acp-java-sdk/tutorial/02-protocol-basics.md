# Module 02: Protocol Basics

Deep dive into the ACP initialize handshake and version negotiation.

## What You'll Learn

- The `InitializeRequest` and `InitializeResponse` structure
- Protocol version negotiation semantics
- Client and agent capability exchange

## The Code

```java
// Initialize with explicit protocol version and capabilities
var initResponse = client.initialize(
    new InitializeRequest(1, new ClientCapabilities(
        new FileSystemCapability(true, true),  // read, write
        false  // terminalExecution
    )));

System.out.println("Protocol version: " + initResponse.protocolVersion());
System.out.println("Agent capabilities: " + initResponse.agentCapabilities());
System.out.println("Existing sessions: " + initResponse.sessionIds().size());
```

## How It Works

The initialize handshake is the first message exchange in ACP. It establishes:

1. **Protocol version** — both sides agree on a compatible version
2. **Client capabilities** — what the client can provide (file system, terminal)
3. **Agent capabilities** — what the agent supports (load session, image content)

The handshake must complete before any session or prompt operations. If versions are incompatible, the connection fails.

## Source Code

[View on GitHub](https://github.com/markpollack/acp-java-tutorial/tree/main/module-02-protocol-basics)

## Running the Example

```bash
export GEMINI_API_KEY=your-key-here
./mvnw exec:java -pl module-02-protocol-basics
```

## Next Module

[Module 03: Sessions](/acp-java-sdk/tutorial/03-sessions) — create and manage conversation sessions.
