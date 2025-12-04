#!/bin/bash

# TARS Client Server Startup Script for GCP
# This script sets the required environment variables and starts the Node.js server

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Set environment variables for GCP deployment
export BACKEND_URL=${BACKEND_URL:-http://34.75.80.228:8080}
export PORT=${PORT:-3001}

# Display configuration
echo "=========================================="
echo "Starting TARS Client Server"
echo "=========================================="
echo "Backend URL: $BACKEND_URL"
echo "Port: $PORT"
echo "Working Directory: $SCRIPT_DIR"
echo "=========================================="
echo ""

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

# Start the server
echo "Starting server..."
npm start

