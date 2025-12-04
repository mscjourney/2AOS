#!/bin/bash

# TARS Client Server Startup Script for GCP (Background Mode)
# This script sets the required environment variables and starts the Node.js server in the background

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Set environment variables for GCP deployment
export BACKEND_URL=${BACKEND_URL:-http://34.75.80.228:8080}
export PORT=${PORT:-3001}

# Log file location
LOG_FILE="${SCRIPT_DIR}/server.log"
PID_FILE="${SCRIPT_DIR}/server.pid"

# Display configuration
echo "=========================================="
echo "Starting TARS Client Server (Background)"
echo "=========================================="
echo "Backend URL: $BACKEND_URL"
echo "Port: $PORT"
echo "Working Directory: $SCRIPT_DIR"
echo "Log File: $LOG_FILE"
echo "PID File: $PID_FILE"
echo "=========================================="
echo ""

# Check if server is already running
if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat "$PID_FILE")
    if ps -p "$OLD_PID" > /dev/null 2>&1; then
        echo "Server is already running with PID $OLD_PID"
        echo "To stop it, run: kill $OLD_PID"
        exit 1
    else
        echo "Removing stale PID file..."
        rm -f "$PID_FILE"
    fi
fi

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo "Warning: node_modules not found. Running npm install..."
    npm install
    if [ $? -ne 0 ]; then
        echo "Error: npm install failed"
        exit 1
    fi
fi

# Check if server.js exists
if [ ! -f "server.js" ]; then
    echo "Error: server.js not found in $SCRIPT_DIR"
    exit 1
fi

# Start the server in background
echo "Starting server in background..."
nohup npm start > "$LOG_FILE" 2>&1 &
SERVER_PID=$!

# Save PID
echo $SERVER_PID > "$PID_FILE"

echo "Server started with PID: $SERVER_PID"
echo "Logs are being written to: $LOG_FILE"
echo ""
echo "To view logs: tail -f $LOG_FILE"
echo "To stop server: kill $SERVER_PID"
echo "Or use: ./stop-server.sh"

