# Spring AI Community Documentation

Mintlify-powered documentation for the Spring AI Community.

## Quick Start

### Local Preview (REQUIRED before commits)

```bash
# Start development server
./dev-preview.sh

# Or specify a port
./dev-preview.sh 3001

# Or use mintlify directly
mintlify dev
```

View at: http://localhost:3000

### Pre-commit Validation

**Always run before committing:**

```bash
./pre-commit-check.sh
```

This checks for:
- Broken links
- Invalid JSON syntax in mint.json
- Git status

## Development Workflow

### 1. Make Changes
Edit `.mdx` files or `mint.json`

### 2. Preview Locally
```bash
./dev-preview.sh
```

### 3. Validate
```bash
./pre-commit-check.sh
```

### 4. Commit (if checks pass)
```bash
git add <files>
git commit -m "Description"
```

### 5. Final Review
```bash
git show HEAD
```

### 6. Push
```bash
git push origin main
```

## Project Structure

```
mintlify-docs/
├── mint.json              # Navigation and config
├── index.mdx             # Homepage
├── community/            # Community docs
│   ├── index.mdx
│   ├── governance.mdx
│   └── ...
├── projects/             # Project showcase
│   ├── production/
│   └── incubating/
├── benchmarking/         # Spring AI Bench info
└── get-involved/         # Contribution guide
```

## Key Differences from AsciiDoc

| AsciiDoc | Mintlify |
|----------|----------|
| `.adoc` files | `.mdx` files |
| `asciidoctor` to build | No build step |
| `asciidoctor --watch` | `mintlify dev` |
| Attributes (`:toc:`) | Components (`<Card>`) |
| `include::file.adoc[]` | `<Snippet file="snippet.mdx" />` |
| Config in files | Config in `mint.json` |

## Important Commands

```bash
# Start dev server
mintlify dev

# Check for broken links
mintlify broken-links

# Rename file and update references
mintlify rename old-file.mdx new-file.mdx

# Validate OpenAPI spec
mintlify openapi-check api-reference/openapi.json
```

## Common Components

### Cards
```mdx
<Card title="Title" icon="icon-name" href="/path">
  Content
</Card>
```

### Card Groups
```mdx
<CardGroup cols={2}>
  <Card>...</Card>
  <Card>...</Card>
</CardGroup>
```

### Steps
```mdx
<Steps>
  <Step title="First">Content</Step>
  <Step title="Second">Content</Step>
</Steps>
```

### Accordions
```mdx
<AccordionGroup>
  <Accordion title="Question">Answer</Accordion>
</AccordionGroup>
```

### Tabs
```mdx
<Tabs>
  <Tab title="Option 1">Content</Tab>
  <Tab title="Option 2">Content</Tab>
</Tabs>
```

### Callouts
```mdx
<Note>Important information</Note>
<Warning>Warning message</Warning>
<Info>Info message</Info>
<Check>Success message</Check>
```

## Validation

- Run `./pre-commit-check.sh` before every commit
- The script checks for broken links and syntax errors
- Review changes with `git diff` before committing

## Resources

- [Mintlify Documentation](https://mintlify.com/docs)
- [Spring AI Community](https://github.com/spring-ai-community)
- [MDX Documentation](https://mdxjs.com/)
