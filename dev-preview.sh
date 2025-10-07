#!/bin/bash
# Mintlify Development Preview Script
# Usage: ./dev-preview.sh [port]

PORT=${1:-3000}
LOG_FILE="/tmp/mintlify-dev.log"

echo "🚀 Starting Mintlify development server..."
echo "📝 Log file: $LOG_FILE"
echo ""

# Start mintlify dev server
mintlify dev --port $PORT > "$LOG_FILE" 2>&1 &
MINTLIFY_PID=$!

# Wait a moment for server to start
sleep 3

# Check if server is running
if ps -p $MINTLIFY_PID > /dev/null; then
    echo "✅ Server started successfully (PID: $MINTLIFY_PID)"
    echo "🌐 View at: http://localhost:$PORT"
    echo ""
    echo "📋 Recent log output:"
    echo "---"
    tail -15 "$LOG_FILE"
    echo "---"
    echo ""
    echo "💡 Tips:"
    echo "  - View full logs: tail -f $LOG_FILE"
    echo "  - Stop server: kill $MINTLIFY_PID"
    echo "  - Check links: mintlify broken-links"
    echo ""
    echo "Server running in background. Press Ctrl+C to exit this script (server will continue)."
    echo "To stop server later: kill $MINTLIFY_PID"
else
    echo "❌ Server failed to start. Check logs:"
    cat "$LOG_FILE"
    exit 1
fi
