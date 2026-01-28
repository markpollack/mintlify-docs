#!/usr/bin/env bash
set -euo pipefail

# Onboard a new Spring.io blog post into the Mintlify docs site.
#
# Usage:
#   ./tools/onboard-post.sh <source.md> <topic-dir/slug>
#
# Example:
#   ./tools/onboard-post.sh ~/downloads/spring-ai-new-feature.md agents/new-feature
#
# What it does:
#   1. Converts the source markdown to Mintlify-compatible MDX
#   2. Places it in blog/<topic-dir>/<slug>.mdx
#   3. Adds the mapping to tools/topic-mapping.properties
#   4. Adds the page to mint.json navigation (you must verify placement)
#   5. Runs validation
#
# Prerequisites:
#   - jbang (https://www.jbang.dev/)
#   - mintlify CLI (npm i -g mintlify)

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

if [ $# -lt 2 ]; then
    echo "Usage: $0 <source.md> <topic-dir/slug>"
    echo ""
    echo "Topic directories: agents, mcp, tools, model-providers, prompts-and-output,"
    echo "                   getting-started, advisors, community"
    echo ""
    echo "Example: $0 ~/downloads/spring-ai-new-feature.md agents/new-feature"
    exit 1
fi

SOURCE="$1"
TARGET="$2"
TOPIC_DIR=$(dirname "$TARGET")
SLUG=$(basename "$TARGET")
OUTPUT_FILE="$REPO_ROOT/blog/${TARGET}.mdx"
SOURCE_SLUG=$(basename "$SOURCE" .md)

if [ ! -f "$SOURCE" ]; then
    echo "ERROR: Source file not found: $SOURCE"
    exit 1
fi

# Valid topic directories
VALID_TOPICS="agents mcp tools model-providers prompts-and-output getting-started advisors community"
if ! echo "$VALID_TOPICS" | grep -qw "$TOPIC_DIR"; then
    echo "ERROR: Unknown topic directory: $TOPIC_DIR"
    echo "Valid: $VALID_TOPICS"
    exit 1
fi

echo "=== Onboarding: $SOURCE_SLUG -> blog/$TARGET ==="
echo ""

# Step 1: Convert
mkdir -p "$REPO_ROOT/blog/$TOPIC_DIR"
jbang "$SCRIPT_DIR/SpringToMdx.java" "$SOURCE" -o "$OUTPUT_FILE"

# Step 2: Add mapping
echo "" >> "$SCRIPT_DIR/topic-mapping.properties"
echo "$SOURCE_SLUG = $TARGET" >> "$SCRIPT_DIR/topic-mapping.properties"
echo "Added mapping: $SOURCE_SLUG = $TARGET"

# Step 3: Remind about mint.json
echo ""
echo "=== MANUAL STEP REQUIRED ==="
echo "Add this to the appropriate group in mint.json navigation:"
echo ""
echo "  \"blog/$TARGET\""
echo ""
echo "For example, in the \"$(echo "$TOPIC_DIR" | sed 's/-/ /g' | sed 's/\b\(.\)/\u\1/g')\" group."
echo ""

# Step 4: Validate
echo "=== Running validation ==="
cd "$REPO_ROOT"
jbang tools/SpringToMdx.java --validate blog/

echo ""
echo "Done! Don't forget to update mint.json navigation."
