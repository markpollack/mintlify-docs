# Module 09: Structured Outputs

Getting Claude to return structured JSON responses.

## What You'll Learn

- Using `JsonSchema` to define output structure
- Configuring structured outputs with `CLIOptions`
- Parsing JSON responses with Jackson

## Why Structured Outputs?

Instead of parsing free-form text, get Claude to return predictable JSON:

```json
{
  "answer": 105,
  "explanation": "15 multiplied by 7 equals 105"
}
```

## Defining a Schema

Use `JsonSchema` to define the expected output structure:

```java
import org.springaicommunity.claude.agent.sdk.types.JsonSchema;
import java.util.List;
import java.util.Map;

// Simple schema: { "answer": number, "explanation": string }
JsonSchema schema = JsonSchema.ofObject(
    Map.of(
        "answer", Map.of("type", "number"),
        "explanation", Map.of("type", "string")
    ),
    List.of("answer", "explanation")  // Required fields
);
```

## Using Structured Outputs

Structured outputs require `CLIOptions` with `ClaudeSyncClient`:

```java
import org.springaicommunity.claude.agent.sdk.ClaudeClient;
import org.springaicommunity.claude.agent.sdk.ClaudeSyncClient;
import org.springaicommunity.claude.agent.sdk.transport.CLIOptions;
import org.springaicommunity.claude.agent.sdk.config.PermissionMode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;

// Build CLIOptions with jsonSchema
CLIOptions options = CLIOptions.builder()
    .model(CLIOptions.MODEL_HAIKU)
    .jsonSchema(schema.toMap())
    .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
    .build();

try (ClaudeSyncClient client = ClaudeClient.sync(options)
        .workingDirectory(Path.of("."))
        .build()) {

    String response = client.connectText("What is 15 * 7? Provide answer and explanation.");

    // Parse JSON response
    ObjectMapper mapper = new ObjectMapper();
    JsonNode json = mapper.readTree(response);
    int answer = json.get("answer").asInt();
    String explanation = json.get("explanation").asText();

    System.out.println("Answer: " + answer);
    System.out.println("Explanation: " + explanation);
}
```

## Nested Schemas

Define complex structures with arrays and nested objects:

```java
// Schema for: { "languages": [{ "name": string, "year": number }] }
JsonSchema schema = JsonSchema.ofObject(
    Map.of(
        "languages", Map.of(
            "type", "array",
            "items", Map.of(
                "type", "object",
                "properties", Map.of(
                    "name", Map.of("type", "string"),
                    "year", Map.of("type", "integer"),
                    "creator", Map.of("type", "string")
                ),
                "required", List.of("name", "year")
            )
        )
    ),
    List.of("languages")
);
```

## JsonSchema Methods

| Method | Description |
|--------|-------------|
| `JsonSchema.ofObject(properties, required)` | Create object schema with required fields |
| `JsonSchema.ofObject(properties)` | Create object schema (all optional) |
| `JsonSchema.fromMap(map)` | Create from existing Map |
| `schema.toMap()` | Convert to Map for CLIOptions |

## JSON Types Reference

| Type | JSON Schema | Java Type |
|------|-------------|-----------|
| String | `"type": "string"` | `String` |
| Number | `"type": "number"` | `double` |
| Integer | `"type": "integer"` | `int` |
| Boolean | `"type": "boolean"` | `boolean` |
| Array | `"type": "array"` | `List` |
| Object | `"type": "object"` | `Map` or POJO |

## Note on Query API

The simplified `QueryOptions` class does not support `jsonSchema`. Use `CLIOptions` with `ClaudeSyncClient` for structured outputs.

## Limitations

- **No strict enforcement**: Claude attempts to match your schema but may occasionally produce invalid JSON or missing fields. Always validate the response.
- **Schema complexity**: Deeply nested schemas (>3-4 levels) or schemas with many optional fields increase the chance of malformed output.
- **Error handling**: When Claude fails to produce valid JSON, you receive raw text. Wrap parsing in try-catch:

```java
try {
    JsonNode json = mapper.readTree(response);
    // process structured data
} catch (JsonProcessingException e) {
    // fallback: treat response as unstructured text
    log.warn("Failed to parse structured output: {}", response);
}
```

- **No partial results**: If Claude's response is truncated (due to `maxTokens`), the JSON will be incomplete and unparseable.
- **Tool use interactions**: When Claude uses tools during a structured output request, intermediate messages are unstructured. Only the final response follows the schema.

## Source Code

[View on GitHub](https://github.com/spring-ai-community/claude-agent-sdk-java-tutorial/tree/main/module-09-structured-outputs)

## Running the Example

```bash
mvn compile exec:java -pl module-09-structured-outputs
```

## Next Module

[Module 10: Multi-Turn Conversations](/claude-agent-sdk/tutorial/10-multi-turn) - Building conversational applications with context.
