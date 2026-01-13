# Module 01: Hello World

Your first Claude Agent SDK program.

## What You'll Learn

- How to add the SDK dependency to your project
- The simplest way to query Claude: `Query.text()`
- Running a query and getting a text response

## Prerequisites

1. **Claude CLI installed and authenticated**
   ```bash
   npm install -g @anthropic-ai/claude-code
   claude login
   ```

2. **Java 21 or later**

## The Code

```java
import org.springaicommunity.claude.agent.sdk.Query;

public class HelloWorld {
    public static void main(String[] args) {
        // One line - that's it!
        String answer = Query.text("What is 2 + 2?");
        System.out.println(answer);
    }
}
```

## Source Code

[View on GitHub](https://github.com/spring-ai-community/claude-agent-sdk-java-tutorial/tree/main/module-01-hello-world)

## Running the Example

```bash
mvn compile exec:java -pl module-01-hello-world
```

## How It Works

1. `Query.text()` is a static method that takes a prompt string
2. It starts the Claude CLI as a subprocess
3. Sends your prompt to Claude
4. Returns Claude's text response as a String
5. The CLI subprocess exits automatically

## Key Points

- **Stateless**: Each `Query.text()` call creates a new session
- **Blocking**: The call waits for Claude's complete response
- **Simple**: No configuration required for basic usage

## Next Module

[Module 02: Query API](/claude-agent-sdk/tutorial/02-query-api) - Learn about `Query.execute()` for getting response metadata like cost and token usage.
