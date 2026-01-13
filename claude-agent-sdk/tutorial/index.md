---
title: "Tutorial"
sidebarTitle: "Overview"
description: "A progressive tutorial for learning the Claude Agent SDK Java."
---

A progressive, hands-on tutorial. Each module takes 15-30 minutes and focuses on one concept.

## Prerequisites

- Java 21 or later
- Maven 3.8+
- Claude CLI installed and authenticated (`claude login`)

## Tutorial Structure

| Part | Modules | Topics |
|------|---------|--------|
| **1. Fundamentals** | 01-04 | Query API, ClaudeSyncClient, Message Types |
| **2. Configuration** | 05-08 | CLI Options, Permissions, Structured Output |
| **3. Sessions & State** | 09-12 | Multi-turn, Resume, Fork, Streaming |
| **4. Safety & Control** | 13-16 | Callbacks, Hooks, Interrupts |
| **5. MCP Integration** | 17-19 | External servers, Spring AI, Custom tools |
| **6. Multi-Agent** | 20-22 | Subagents, Parallel execution, Patterns |
| **Capstone** | Final | Complete research agent project |

## Getting the Code

```bash
git clone https://github.com/spring-ai-community/claude-agent-sdk-java-tutorial.git
cd claude-agent-sdk-java-tutorial
```

## Running a Module

```bash
mvn compile exec:java -pl module-01-hello-world
```

## Three API Styles

The SDK provides three ways to interact with Claude:

| API | Class | Programming Style | Best For |
|-----|-------|-------------------|----------|
| **One-shot** | `Query` | Static methods | Simple queries, scripts |
| **Blocking** | `ClaudeSyncClient` | Iterator-based | Traditional applications, synchronous workflows |
| **Reactive** | `ClaudeAsyncClient` | Flux/Mono | Non-blocking applications, high concurrency |

Both `ClaudeSyncClient` and `ClaudeAsyncClient` support the full feature set: multi-turn conversations, hooks, MCP integration, and permission callbacks. They differ only in programming paradigm.

The tutorial primarily uses `Query` for simple examples and `ClaudeSyncClient` for advanced features (blocking is more accessible to most developers).

## Start Learning

Begin with [Module 01: Hello World](/claude-agent-sdk/tutorial/01-hello-world) to write your first Claude Agent SDK program.
