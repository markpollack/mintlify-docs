# Module 07: Agent Requests (Client Side)

Handle file read/write requests from agents on the client side.

## What You'll Learn

- Registering `readTextFileHandler` and `writeTextFileHandler`
- Advertising file system capabilities via `ClientCapabilities`
- The inverted request flow: agents request, clients serve

## The Code

```java
AcpSyncClient client = AcpClient.sync(transport)
    .readTextFileHandler(req -> {
        Path path = Path.of(req.path());
        if (!Files.exists(path)) {
            throw new RuntimeException("File not found: " + req.path());
        }
        return new ReadTextFileResponse(Files.readString(path));
    })
    .writeTextFileHandler(req -> {
        Files.writeString(Path.of(req.path()), req.content());
        return new WriteTextFileResponse();
    })
    .sessionUpdateConsumer(notification -> { /* handle updates */ })
    .build();

// Advertise file system capabilities
client.initialize(new InitializeRequest(1,
    new ClientCapabilities(
        new FileSystemCapability(true, true),  // read=true, write=true
        false  // terminalExecution
    )));
```

## Inverted Request Flow

In ACP, the roles are inverted compared to traditional client-server:
- The **agent** requests files from the **client**
- The **client** serves files to the **agent**

This allows agents to access the user's local filesystem through a controlled interface. The client decides which files to expose and how to handle writes.

<Warning>
Throw exceptions from handlers for errors. The SDK converts exceptions to JSON-RPC error responses. Do not return error strings as content.
</Warning>

## Source Code

[View on GitHub](https://github.com/markpollack/acp-java-tutorial/tree/main/module-07-agent-requests)

## Running the Example

```bash
export GEMINI_API_KEY=your-key-here
./mvnw exec:java -pl module-07-agent-requests
```

## Next Module

[Module 08: Permissions](/acp-java-sdk/tutorial/08-permissions) — handle permission requests from agents.
