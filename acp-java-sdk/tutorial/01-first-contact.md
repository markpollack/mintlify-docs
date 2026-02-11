# Module 01: First Contact

Your first ACP client — connect to a Gemini agent and send a prompt.

## What You'll Learn

- Configuring agent process parameters with `AgentParameters`
- Creating a stdio transport for subprocess communication
- Building a synchronous ACP client
- The initialize → newSession → prompt lifecycle

## Prerequisites

1. **Gemini CLI with ACP support**
   ```bash
   gemini --experimental-acp --version
   ```

2. **API key**
   ```bash
   export GEMINI_API_KEY=your-key-here
   ```

3. **Java 21 or later**

## The Code

```java
import com.agentclientprotocol.sdk.client.*;
import com.agentclientprotocol.sdk.client.transport.*;
import com.agentclientprotocol.sdk.spec.AcpSchema.*;
import java.util.List;

// 1. Configure the agent process
var params = AgentParameters.builder("gemini")
    .arg("--experimental-acp")
    .build();

// 2. Create stdio transport (launches agent as subprocess)
var transport = new StdioAcpClientTransport(params);

// 3. Build synchronous client
AcpSyncClient client = AcpClient.sync(transport).build();

// 4. Initialize — handshake with agent
client.initialize();

// 5. Create session — establishes working context
var session = client.newSession(
    new NewSessionRequest("/workspace", List.of()));

// 6. Send prompt — get response
var response = client.prompt(new PromptRequest(
    session.sessionId(),
    List.of(new TextContent("What is 2+2?"))
));

System.out.println("Stop reason: " + response.stopReason());
client.close();
```

## How It Works

ACP communication follows a three-phase lifecycle:

1. **Initialize** — client and agent exchange protocol versions and capabilities
2. **New Session** — establishes a working directory context for the conversation
3. **Prompt** — sends content and receives a response with a stop reason

The `StdioAcpClientTransport` launches the agent as a subprocess and communicates via JSON-RPC over stdin/stdout. This is the same transport Zed, JetBrains, and VS Code use.

## Source Code

[View on GitHub](https://github.com/markpollack/acp-java-tutorial/tree/main/module-01-first-contact)

## Running the Example

```bash
export GEMINI_API_KEY=your-key-here
./mvnw exec:java -pl module-01-first-contact
```

## Next Module

[Module 05: Streaming Updates](/acp-java-sdk/tutorial/05-streaming-updates) — receive real-time updates while the agent processes your prompt.

Or skip to [Module 12: Echo Agent](/acp-java-sdk/tutorial/12-echo-agent) to build your own agent (no API key required).
