# TARS Node.js Client

A standalone Node.js client application for the TARS API with a React dashboard interface. Each instance of this client maintains its own persistent `clientId` and can connect to the TARS service simultaneously with other instances.

## Features

- **Persistent Client ID**: Each Node.js instance stores its `clientId` locally in `client-config.json`
- **React Dashboard**: Modern web UI accessible at `http://localhost:3001/dashboard`
- **Multiple Instance Support**: Each Node.js instance is a separate client that can connect simultaneously
- **Full API Coverage**: Access to all TARS API endpoints through the dashboard
- **Admin Dashboard**: User management interface for administrators
- **Crime Summary**: Access to FBI crime statistics
- **Weather Services**: Weather recommendations and alerts
- **City Summaries**: Comprehensive city information

## Architecture

The application consists of three main components:

### 1. Node.js Server (`server.js`)
- **Purpose**: Express.js server that acts as a middleware/proxy between the React frontend and the Java backend
- **Port**: Runs on port 3001 by default
- **Responsibilities**:
  - Serves the React frontend (static files from `client/build/`)
  - Provides REST API endpoints (`/api/*`) that proxy requests to the Java backend
  - Handles CORS for cross-origin requests
  - Manages file operations (reading/writing JSON files like `users.json`, `clients.json`)
  - Provides a unified API interface for the frontend

### 2. API Client (`src/tarsApiClient.js`)
- **Purpose**: Wraps the Java TARS API endpoints and manages client ID persistence
- **Responsibilities**:
  - Communicates with the Java backend running on `http://localhost:8080`
  - Manages persistent client ID storage in `client-config.json`
  - Provides methods for all TARS API operations (users, weather, crime, etc.)
  - Handles error responses and converts them to user-friendly messages

### 3. React Frontend (`client/`)
- **Purpose**: Modern web-based user interface
- **Components**:
  - `Dashboard.js`: Main dashboard with tabs for Crime Summary, Weather, and Country Summary
  - `AdminDashboard.js`: Admin-only interface for user management
  - `Login.js`: User authentication
  - `UserProfile.js`: User profile and preferences management
- **Build**: Production build is served as static files by the Express server

## Prerequisites

Before running the Node.js client, ensure you have:

1. **Node.js** (v14 or higher) - [Download Node.js](https://nodejs.org/)
2. **npm** (comes with Node.js) or **yarn**
3. **TARS Java Backend** running on `http://localhost:8080`
   - Navigate to `TeamProject/` directory
   - Run: `mvn spring-boot:run`
   - The Java service must be running before starting the Node.js server

## Installation

1. Install server dependencies:
```bash
npm install
```

2. Install client dependencies:
```bash
cd client
npm install
cd ..
```

Or install all at once:
```bash
npm run install-all
```

## How to Run the Application

### Step-by-Step Setup

1. **Install Dependencies** (first time only):
```bash
# Install server dependencies
npm install

# Install client dependencies
cd client
npm install
cd ..
```

Or install everything at once:
```bash
npm run install-all
```

2. **Start the Java Backend** (required):
   - Open a terminal and navigate to the Java project:
   ```bash
   cd ../TeamProject
   mvn spring-boot:run
   ```
   - Wait for the message: "Started TarsApplication" 
   - The Java backend should be running on `http://localhost:8080`
   - **Keep this terminal open** - the Java service must stay running

3. **Build the React Frontend** (production mode):
```bash
cd client
npm run build
cd ..
```
   This creates an optimized production build in `client/build/`

4. **Start the Node.js Server**:
   - In a new terminal, navigate to the `tars-client` directory:
   ```bash
   cd tars-client
   npm start
   ```
   - You should see: `TARS Client Server running on http://localhost:3001`

5. **Access the Application**:
   - Open your browser and go to: `http://localhost:3001`
   - You'll be redirected to the login page
   - Use admin credentials to access the admin dashboard
   - Regular users can access the main dashboard

### Development Mode (with Hot Reload)

For development with automatic code reloading:

1. **Start Java Backend** (same as above):
```bash
cd TeamProject
mvn spring-boot:run
```

2. **Start React Dev Server** (Terminal 1):
```bash
cd client
npm start
```
This starts React on `http://localhost:3000` with hot reload

3. **Start Node.js API Server** (Terminal 2):
```bash
npm run dev
```
This starts the Express server on `http://localhost:3001` with nodemon (auto-reload)

4. **Access the Application**:
   - React dev server: `http://localhost:3000`
   - The React app will proxy API requests to `http://localhost:3001/api`

### Quick Start Commands

```bash
# Production mode (recommended)
npm start                    # Start Node.js server (after building React app)

# Development mode
npm run dev                  # Start Node.js server with auto-reload
cd client && npm start        # Start React dev server (separate terminal)

# Build React app
npm run build                # Build React app for production
```

## Running Multiple Clients Simultaneously

You can run multiple client instances on different ports to simulate different users accessing the system simultaneously. This is useful for testing multi-user scenarios, admin functionality, and concurrent access patterns.

### Setup for Multiple Clients

1. **Build the React Frontend** (one time, shared by all instances):
```bash
cd client
npm run build
cd ..
```

2. **Start the Java Backend** (required for all clients):
```bash
cd ../TeamProject
mvn spring-boot:run
```

3. **Start First Client Instance** (Terminal 1 - Default port 3001):
```bash
cd tars-client
PORT=3001 npm start
```
- Access at: `http://localhost:3001`
- Example: **Alice (Admin)** - Use admin credentials to log in

4. **Start Second Client Instance** (Terminal 2 - Port 3002):
```bash
cd tars-client
PORT=3002 npm start
```
- Access at: `http://localhost:3002`
- Example: **Charlie (Regular User)** - Use regular user credentials to log in

### Example: Running Alice (Admin) and Charlie (Regular User)

**Terminal 1 - Alice (Admin on port 3001):**
```bash
cd tars-client
PORT=3001 npm start
```
- Open browser: `http://localhost:3001`
- Login as: **Alice** (admin user)
- Access: Admin dashboard for user management

**Terminal 2 - Charlie (Regular User on port 3002):**
```bash
cd tars-client
PORT=3002 npm start
```
- Open browser: `http://localhost:3002`
- Login as: **Charlie** (regular user)
- Access: Regular dashboard with personal alerts, preferences, etc.

### Important Notes

- **Each client instance maintains its own `clientId`**: Each instance stores its client ID in `client-config.json` in the `tars-client` directory. If you want separate client IDs for each instance, you can:
  - Run each instance from a different directory, OR
  - Manually edit `client-config.json` between runs


- **Independent Sessions**: Each browser tab/window maintains its own login session. You can have:
  - Alice logged in on `http://localhost:3001` (admin)
  - Charlie logged in on `http://localhost:3002` (regular user)
  - Both accessing the system simultaneously

-



### How the Server Works

1. **Client Request**: User interacts with React frontend (e.g., clicks "Get Crime Summary")
2. **Frontend API Call**: React makes HTTP request to `http://localhost:3001/api/crime/summary`
3. **Express Route Handler**: `server.js` receives the request at `/api/crime/summary`
4. **API Client Call**: Server uses `TarsApiClient` to forward request to Java backend
5. **Java Backend Processing**: Java service processes request, reads/writes data files
6. **Response Chain**: Response flows back through the chain to the browser

### Key Server Files

- **`server.js`**: Main Express server file
  - Defines all `/api/*` routes
  - Handles CORS, JSON parsing, static file serving
  - Proxies requests to Java backend via `TarsApiClient`
  
- **`src/tarsApiClient.js`**: API client wrapper
  - Encapsulates all Java backend API calls
  - Manages client ID persistence
  - Handles errors and response formatting

- **`client/build/`**: Production React build
  - Static HTML, CSS, and JavaScript files
  - Served by Express as static files
  - Created by running `npm run build` in `client/` directory

## Dashboard Features

### Main Dashboard (Regular Users)

- **Crime Summary Tab**: 
  - Query FBI crime statistics by state, offense code, month, and year
  - Offense codes: ASS (Assault), BUR (Burglary), HOM (Homicide), ROB (Robbery), LAR (Larceny), MVT (Motor Vehicle Theft)
  
- **Weather Tab**:
  - Get weather recommendations for cities
  - Retrieve weather alerts by city or coordinates
  - Configure forecast days (1-14)

- **Country Summary Tab**:
  - Get comprehensive country summaries
  - View travel advisories and safety information

### Admin Dashboard (Admin Users Only)

- **User Management**:
  - View all users from `users.json`
  - Create new users (username, email, role)
  - Users are automatically assigned to the admin's client ID

## End-to-End Testing

The client includes comprehensive end-to-end tests that exercise the full client functionality by making real HTTP requests to a running TARS service instance.

### Prerequisites

1. **TARS Java Backend must be running**
   ```bash
   cd ../TeamProject
   mvn spring-boot:run
   ```
   The service should be accessible at `http://localhost:8080`

2. **Install test dependencies**
   ```bash
   npm install
   ```

### Running Tests

#### Automated (Recommended)

```bash
npm test
```

This runs all tests including:
- **Unit tests** for `TarsApiClient` (`__tests__/tarsApiClient.test.js`)
- **Server route tests** for Express API endpoints (`__tests__/server-routes.test.js`)
- **End-to-end tests** that require a running Java backend (`__tests__/e2e/client-service-e2e.test.js`)

#### Running Specific Test Suites

```bash
# Run only E2E tests (requires Java backend running)
npm run test:e2e


## Code Coverage

The project includes comprehensive test coverage reporting. You can generate and view coverage reports to see which parts of the codebase are tested.

### Generating Coverage Reports

```bash
# Generate coverage report for all tests
npm run test:coverage

# Generate coverage report for E2E tests only
npm run test:coverage:e2e
```

The project maintains high test coverage:
- **Overall**: >90% statement coverage
- **API Client**: >95% statement coverage
- **Server Routes**: >85% statement coverage

### Coverage Configuration

Coverage is configured in `jest.config.js`:
- Coverage is collected from `src/**/*.js` and `server.js`
- React client tests are excluded (they have their own test suite)
- Coverage reports are generated in `coverage/` directory
- Reports are automatically added to `.gitignore` (not committed to repository)
