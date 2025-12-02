const express = require('express');
const cors = require('cors');
const path = require('path');
const fs = require('fs-extra');
const TarsApiClient = require('./src/tarsApiClient');

const app = express();
const PORT = process.env.PORT || 3001;

// Initialize API client
const apiClient = new TarsApiClient('http://localhost:8080');

// Middleware
app.use(cors());
app.use(express.json());

// Debug middleware - log ALL requests
app.use((req, res, next) => {
  console.log(`[REQUEST] ${req.method} ${req.path} ${req.originalUrl}`);
  next();
});

// CRITICAL: Register country summary route FIRST, before any other routes
// This must be a direct app.get() to ensure it's matched before static middleware
app.get('/api/countrySummary/:country', async (req, res) => {
  console.log('=== Country summary route hit ===');
  console.log('Full URL:', req.originalUrl);
  console.log('Request path:', req.path);
  console.log('Request params:', req.params);

  try {
    const { country } = req.params;
    const decodedCountry = decodeURIComponent(country);
    console.log('Fetching country summary for:', decodedCountry);
    const summary = await apiClient.getCountrySummary(decodedCountry);
    console.log('Country summary result:', summary);
    res.setHeader('Content-Type', 'application/json');
    res.json(summary);
  } catch (error) {
    console.error('Error getting country summary:', error);
    res.setHeader('Content-Type', 'application/json');
    res.status(500).json({ error: error.message });
  }
});

app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', message: 'TARS Client Server is running' });
});

// Get or create client ID
app.get('/api/client-id', async (req, res) => {
  try {
    const clientId = await apiClient.getOrCreateClientId();
    res.json({ clientId });
  } catch (error) {
    console.error('Error getting client ID:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get all clients - proxy to Java backend
app.get('/api/clients', async (req, res) => {
  try {
    const clients = await apiClient.getClients();
    res.json(clients);
  } catch (error) {
    console.error('Error getting clients:', error);
    res.status(500).json({ error: error.message });
  }
});

// Create a new client
app.post('/api/client/create', async (req, res) => {
  try {
    const { name, email } = req.body;
    if (!name || !email) {
      return res.status(400).json({ error: 'Name and email are required' });
    }
    const result = await apiClient.createClient(name, email);
    res.json(result);
  } catch (error) {
    console.error('Error creating client:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get index/welcome message
app.get('/api/index', async (req, res) => {
  try {
    const message = await apiClient.getIndex();
    res.json({ message });
  } catch (error) {
    console.error('Error getting index:', error);
    res.status(500).json({ error: error.message });
  }
});

// Create a user for the client
app.post('/api/client/createUser', async (req, res) => {
  try {
    const { clientId, username, email, role } = req.body;
    if (!clientId || !username || !email || !role) {
      return res.status(400).json({ error: 'clientId, username, email, and role are required' });
    }
    const user = await apiClient.createClientUser(clientId, username, email, role);
    // The Java backend returns the complete user object, so we can return it directly
    res.json(user);
  } catch (error) {
    console.error('Error creating user:', error);
    res.status(500).json({ error: error.message });
  }
});

// Set user preferences (adds or updates, call Java backend)
app.put('/api/setPreference/:id', async (req, res) => {
  try {
    const userId = parseInt(req.params.id);
    const user = req.body;
    console.log(`[server] Setting preferences for userId: ${userId} via Java backend`);
    console.log(`[server] Request body:`, JSON.stringify(user, null, 2));
    const result = await apiClient.setUserPreference(userId, user);
    console.log('[server] Java backend response:', result);
    res.json(result);
  } catch (error) {
    console.error('[server] Error setting user preference:', error);
    console.error('[server] Error response:', error.response);
    console.error('[server] Error message:', error.message);
    // Preserve status code from Java backend if available
    const statusCode = error.response?.status || 500;
    const errorMessage = error.message || 'Failed to set user preference';
    console.error(`[server] Returning status ${statusCode} with message: ${errorMessage}`);
    res.status(statusCode).json({ error: errorMessage });
  }
});

// Remove user
app.put('/api/user/:id/remove', async (req, res) => {
  try {
    const userId = parseInt(req.params.id);
    const result = await apiClient.removeUser(userId);
    res.json({ message: result });
  } catch (error) {
    console.error('Error removing user:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get user by ID
app.get('/api/user/:id', async (req, res) => {
  try {
    const userId = parseInt(req.params.id);
    const user = await apiClient.getUser(userId);
    res.json(user);
  } catch (error) {
    console.error('Error getting user:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get user preferences by clientId - proxy to Java backend
app.get('/api/user/client/:clientId', async (req, res) => {
  try {
    const clientId = parseInt(req.params.clientId);
    const userPrefs = await apiClient.getUserByClientId(clientId);
    res.json(userPrefs);
  } catch (error) {
    console.error('Error getting user by clientId:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get user preferences by userId - proxy to Java backend
app.get('/api/preferences/user/:userId', async (req, res) => {
  const userId = parseInt(req.params.userId);
  try {
    const userPrefs = await apiClient.getUserPreference(userId);
    // Add userId field for frontend compatibility
    res.json({
      ...userPrefs,
      userId: userId
    });
  } catch (error) {
    console.error('Error getting preferences by userId:', error);
    // If user not found, return empty preferences (Java returns error, but we want empty prefs)
    if (error.message.includes('not found') || error.message.includes('404')) {
      res.json({
        id: userId,
        userId: userId,
        cityPreferences: [],
        weatherPreferences: [],
        temperaturePreferences: []
      });
    } else {
      res.status(500).json({ error: error.message });
    }
  }
});

// Get all users (from userPreferences.json)
app.get('/api/userList', async (req, res) => {
  try {
    const users = await apiClient.getUserList();
    res.json(users);
  } catch (error) {
    console.error('Error getting user list:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get all TARS users - proxy to Java backend
app.get('/api/tarsUsers', async (req, res) => {
  try {
    const users = await apiClient.getTarsUsers();
    res.json(users);
  } catch (error) {
    console.error('Error getting TARS users:', error);
    res.status(500).json({ error: error.message });
  }
});


// Delete a TARS user - for admin dashboard (calls Java backend)
app.delete('/api/tarsUsers/:userId', async (req, res) => {
  try {
    const userId = parseInt(req.params.userId);
    console.log(`Deleting user with ID: ${userId}`);
    
    // Call Java backend to delete the user
    const deletedUser = await apiClient.deleteTarsUser(userId);
    console.log(`Deleted user ${deletedUser.username} (ID: ${userId}) via Java backend`);
    
    res.json({ message: `User "${deletedUser.username}" deleted successfully`, deletedUser });
  } catch (error) {
    console.error('Error deleting user:', error);
    
    // Check if it's a not found error
    if (error.message.includes('not found') || error.message.includes('404')) {
      return res.status(404).json({ error: `User with ID ${req.params.userId} not found` });
    }
    
    res.status(500).json({ error: error.message });
  }
});

// Get users for a specific client
app.get('/api/userList/client/:clientId', async (req, res) => {
  try {
    const clientId = parseInt(req.params.clientId);
    const users = await apiClient.getClientUserList(clientId);
    res.json(users);
  } catch (error) {
    console.error('Error getting client user list:', error);
    res.status(500).json({ error: error.message });
  }
});

// Login endpoint - proxy to Java backend
app.post('/api/login', async (req, res) => {
  try {
    const { username, email, userId } = req.body;
    const loginResult = await apiClient.login(username, email, userId);
    res.json(loginResult);
  } catch (error) {
    console.error('Error during login:', error);
    const errorMsg = error.response?.data 
      ? (typeof error.response.data === 'object' 
          ? JSON.stringify(error.response.data) 
          : error.response.data)
      : error.message;
    // Preserve status codes from backend
    const statusCode = error.response?.status || 500;
    res.status(statusCode).json({ error: errorMsg });
  }
});

// Get weather recommendation
app.get('/api/recommendation/weather', async (req, res) => {
  try {
    const { city, days } = req.query;
    if (!city || !days) {
      return res.status(400).json({ error: 'city and days parameters are required' });
    }
    const recommendation = await apiClient.getWeatherRecommendation(city, parseInt(days));
    res.json(recommendation);
  } catch (error) {
    console.error('Error getting weather recommendation:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get weather alerts by city
app.get('/api/alert/weather', async (req, res) => {
  try {
    const { city, lat, lon } = req.query;
    let alert;
    if (city) {
      alert = await apiClient.getWeatherAlertsByCity(city);
    } else if (lat && lon) {
      alert = await apiClient.getWeatherAlertsByCoordinates(parseFloat(lat), parseFloat(lon));
    } else {
      return res.status(400).json({ error: 'Either city or lat/lon parameters are required' });
    }
    res.json(alert);
  } catch (error) {
    console.error('Error getting weather alerts:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get user weather alerts
app.get('/api/alert/weather/user/:userId', async (req, res) => {
  try {
    const userId = parseInt(req.params.userId);
    const alerts = await apiClient.getUserWeatherAlerts(userId);
    res.json(alerts);
  } catch (error) {
    console.error('Error getting user weather alerts:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get crime summary
app.get('/api/crime/summary', async (req, res) => {
  try {
    const { state, offense, month, year } = req.query;
    if (!state || !offense || !month || !year) {
      return res.status(400).json({ error: 'state, offense, month, and year parameters are required' });
    }
    // Ensure month and year are strings as expected by Java backend
    const summary = await apiClient.getCrimeSummary(
      String(state).trim(), 
      String(offense).trim(), 
      String(month).trim(), 
      String(year).trim()
    );
    res.json(summary);
  } catch (error) {
    console.error('Error getting crime summary:', error);
    const errorMessage = error.response?.data?.message || error.response?.data || error.message;
    res.status(500).json({ error: errorMessage || 'Failed to get crime summary from backend' });
  }
});

// Get country advisory
app.get('/api/country/:country', async (req, res) => {
  try {
    const { country } = req.params;
    const advisory = await apiClient.getCountryAdvisory(country);
    res.json({ advisory });
  } catch (error) {
    console.error('Error getting country advisory:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get city summary
app.get('/api/summary/:city', async (req, res) => {
  try {
    const { city } = req.params;
    const { startDate, endDate, state } = req.query;
    const summary = await apiClient.getCitySummary(city, startDate, endDate, state);
    res.json(summary);
  } catch (error) {
    console.error('Error getting city summary:', error);
    res.status(500).json({ error: error.message });
  }
});

// Serve static files from React build (ONLY for non-API routes)
// This middleware explicitly excludes /api/* paths
app.use((req, res, next) => {
  console.log(`[STATIC MIDDLEWARE] Checking: ${req.path}`);
  // Skip ALL static file serving for API routes
  if (req.path.startsWith('/api/')) {
    console.log(`[STATIC MIDDLEWARE] Skipping API route: ${req.path}`);
    return next();
  }
  console.log(`[STATIC MIDDLEWARE] Serving static files for: ${req.path}`);
  // Only serve static files for non-API routes
  express.static(path.join(__dirname, 'client/build'))(req, res, next);
});

// Serve React app for all other non-API routes (only if build exists)
// This catch-all MUST come after all API routes and static middleware
app.get('*', (req, res) => {
  console.log(`[CATCH-ALL] Matching route: ${req.path}`);
  // Double-check: Don't serve React app for API routes
  if (req.path.startsWith('/api/')) {
    console.error('[CATCH-ALL] API route not found (caught by catch-all):', req.path);
    return res.status(404).json({ error: 'API endpoint not found: ' + req.path });
  }
  
  const buildPath = path.join(__dirname, 'client/build', 'index.html');
  if (fs.existsSync(buildPath)) {
    res.sendFile(buildPath);
  } else {
    res.status(404).json({ 
      error: 'React app not built. Run "cd client && npm run build" first, or start React dev server separately.' 
    });
  }
});

// Export app for testing
module.exports = app;

// Start server only if this file is run directly (not when required for tests)
if (require.main === module) {
  app.listen(PORT, () => {
    console.log(`TARS Client Server running on http://localhost:${PORT}`);
    console.log(`Dashboard available at http://localhost:${PORT}/dashboard`);
    console.log(`Registered routes:`);
    console.log(`  PUT /api/setPreference/:id`);
    console.log(`  PUT /api/user/:id/remove`);
    console.log(`  GET /api/user/:id`);
    // Log all registered routes for debugging
    app._router.stack.forEach((middleware) => {
      if (middleware.route) {
        const methods = Object.keys(middleware.route.methods).join(', ').toUpperCase();
        console.log(`  ${methods} ${middleware.route.path}`);
      }
    });
  });
}

