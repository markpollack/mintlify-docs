#!/bin/bash
# Pre-commit validation script for Mintlify docs
# Run this before committing to catch issues early

echo "🔍 Running pre-commit checks..."
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

ERRORS=0

# Check 1: Broken links
echo "1️⃣  Checking for broken links..."
if mintlify broken-links 2>&1 | grep -q "No broken links found"; then
    echo -e "${GREEN}✓ No broken links${NC}"
else
    echo -e "${RED}✗ Broken links found!${NC}"
    mintlify broken-links
    ERRORS=$((ERRORS + 1))
fi
echo ""

# Check 2: Validate mint.json
echo "2️⃣  Validating mint.json syntax..."
if jq empty mint.json 2>/dev/null; then
    echo -e "${GREEN}✓ mint.json is valid JSON${NC}"
else
    echo -e "${RED}✗ mint.json has syntax errors!${NC}"
    jq . mint.json
    ERRORS=$((ERRORS + 1))
fi
echo ""

# Check 3: Git status
echo "3️⃣  Git status..."
git status --short
echo ""

# Summary
if [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}✅ All checks passed! Safe to commit.${NC}"
    exit 0
else
    echo -e "${RED}❌ $ERRORS check(s) failed. Please fix before committing.${NC}"
    exit 1
fi
