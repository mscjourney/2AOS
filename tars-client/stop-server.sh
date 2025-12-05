#!/bin/bash

# TARS Client Server Stop Script
# This script stops the Node.js server if it's running in the background or foreground

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PID_FILE="${SCRIPT_DIR}/server.pid"

# First, try to stop using PID file (if started with background script)
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p "$PID" > /dev/null 2>&1; then
        echo "Stopping server with PID $PID (from PID file)..."
        kill "$PID"
        sleep 2
        if ps -p "$PID" > /dev/null 2>&1; then
            echo "Server did not stop gracefully. Force killing..."
            kill -9 "$PID"
            sleep 1
        fi
        rm -f "$PID_FILE"
        if ! ps -p "$PID" > /dev/null 2>&1; then
            echo "Server stopped successfully"
            exit 0
        fi
    else
        echo "Removing stale PID file..."
        rm -f "$PID_FILE"
    fi
fi

# If no PID file or PID file method didn't work, find process by port or name
echo "Checking for server process on port 3001..."
PORT_PID=$(ss -tlnp 2>/dev/null | grep :3001 | grep -oP 'pid=\K[0-9]+' | head -1)

if [ -z "$PORT_PID" ]; then
    # Try alternative method
    PORT_PID=$(lsof -ti:3001 2>/dev/null | head -1)
fi

if [ -n "$PORT_PID" ]; then
    echo "Found server process with PID $PORT_PID on port 3001"
    echo "Stopping server..."
    kill "$PORT_PID"
    sleep 2
    if ps -p "$PORT_PID" > /dev/null 2>&1; then
        echo "Server did not stop gracefully. Force killing..."
        kill -9 "$PORT_PID"
        sleep 1
    fi
    if ! ps -p "$PORT_PID" > /dev/null 2>&1; then
        echo "Server stopped successfully"
        exit 0
    else
        echo "Error: Failed to stop server"
        exit 1
    fi
fi

# Last resort: find by process name
echo "Checking for node server.js processes..."
NODE_PID=$(ps aux | grep "node.*server.js" | grep -v grep | awk '{print $2}' | head -1)

if [ -n "$NODE_PID" ]; then
    echo "Found node server.js process with PID $NODE_PID"
    echo "Stopping server..."
    kill "$NODE_PID"
    sleep 2
    if ps -p "$NODE_PID" > /dev/null 2>&1; then
        echo "Server did not stop gracefully. Force killing..."
        kill -9 "$NODE_PID"
        sleep 1
    fi
    if ! ps -p "$NODE_PID" > /dev/null 2>&1; then
        echo "Server stopped successfully"
        exit 0
    else
        echo "Error: Failed to stop server"
        exit 1
    fi
fi

echo "No server process found running on port 3001 or as node server.js"
exit 0

PID=$(cat "$PID_FILE")

if ! ps -p "$PID" > /dev/null 2>&1; then
    echo "Server with PID $PID is not running. Removing stale PID file."
    rm -f "$PID_FILE"
    exit 1
fi

echo "Stopping server with PID $PID..."
kill "$PID"

# Wait a bit for graceful shutdown
sleep 2

# Check if it's still running
if ps -p "$PID" > /dev/null 2>&1; then
    echo "Server did not stop gracefully. Force killing..."
    kill -9 "$PID"
    sleep 1
fi

# Remove PID file
rm -f "$PID_FILE"

if ps -p "$PID" > /dev/null 2>&1; then
    echo "Error: Failed to stop server"
    exit 1
else
    echo "Server stopped successfully"
fi

