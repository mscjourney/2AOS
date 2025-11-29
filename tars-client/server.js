const express = require('express');
const cors = require('cors');
const path = require('path');
const fs = require('fs-extra');
const TarsApiClient = require('./src/tarsApiClient');

const app = express();
const PORT = 3001;

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

// Get all clients
app.get('/api/clients', async (req, res) => {
  try {
    const clientsJsonPath = path.resolve(__dirname, '..', 'TeamProject', 'data', 'clients.json');
    let clients = [];
    
    if (fs.existsSync(clientsJsonPath)) {
      const data = await fs.readJson(clientsJsonPath);
      clients = Array.isArray(data) ? data : [];
      console.log(`Loaded ${clients.length} clients from clients.json`);
    } else {
      console.error('clients.json not found at:', clientsJsonPath);
      return res.json([]);
    }
    
    res.json(Array.isArray(clients) ? clients : []);
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

// Add user preferences (call Java backend)
app.put('/api/user/:id/add', async (req, res) => {
  try {
    const userId = parseInt(req.params.id);
    const user = req.body;
    console.log(`Adding preferences for userId: ${userId} via Java backend`, user);
    const result = await apiClient.addUser(userId, user);
    console.log('Java backend response:', result);
    res.json(result);
  } catch (error) {
    console.error('Error adding user:', error);
    res.status(500).json({ error: error.message });
  }
});

// Update user preferences (call Java backend)
app.put('/api/user/:id/update', async (req, res) => {
  try {
    const userId = parseInt(req.params.id);
    const user = req.body;
    console.log(`Updating preferences for userId: ${userId} via Java backend`, user);
    const result = await apiClient.updateUser(userId, user);
    console.log('Java backend response:', result);
    res.json(result);
  } catch (error) {
    console.error('Error updating user:', error);
    res.status(500).json({ error: error.message });
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

// Get user preferences by clientId (from userPreferences.json)
app.get('/api/user/client/:clientId', async (req, res) => {
  try {
    const clientId = parseInt(req.params.clientId);
    console.log(`Loading preferences for clientId: ${clientId}`);
    
    // Read directly from userPreferences.json file
    const userPrefsJsonPath = path.join(__dirname, '..', 'TeamProject', 'data', 'userPreferences.json');
    let allUsers = [];
    
    if (fs.existsSync(userPrefsJsonPath)) {
      allUsers = await fs.readJson(userPrefsJsonPath);
      console.log(`Read ${allUsers.length} preference entries directly from userPreferences.json`);
      console.log('Available entries:', allUsers.map(u => `id:${u.id}, clientId:${u.clientId} (type: ${typeof u.clientId})`).join(', '));
    } else {
      console.error('userPreferences.json not found at:', userPrefsJsonPath);
      return res.json({
        id: null,
        clientId: clientId,
        cityPreferences: [],
        weatherPreferences: [],
        temperaturePreferences: []
      });
    }
    
    // Find user preferences that match the clientId (ensure both are numbers for comparison)
    const userPrefs = allUsers.find(user => {
      const userClientId = typeof user.clientId === 'string' ? parseInt(user.clientId) : user.clientId;
      return userClientId === clientId;
    });
    
    if (userPrefs) {
      console.log(`Found preferences for clientId ${clientId}:`, {
        id: userPrefs.id,
        clientId: userPrefs.clientId,
        cities: userPrefs.cityPreferences?.length || 0,
        weather: userPrefs.weatherPreferences?.length || 0,
        temp: userPrefs.temperaturePreferences?.length || 0
      });
      res.json(userPrefs);
    } else {
      console.log(`No preferences found for clientId ${clientId}, returning empty preferences`);
      // Return empty preferences if not found
      res.json({
        id: null,
        clientId: clientId,
        cityPreferences: [],
        weatherPreferences: [],
        temperaturePreferences: []
      });
    }
  } catch (error) {
    console.error('Error getting user by clientId:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get user preferences by userId (from userPreferences.json) - for profile page
app.get('/api/preferences/user/:userId', async (req, res) => {
  try {
    const userId = parseInt(req.params.userId);
    console.log(`Loading preferences for userId: ${userId}`);
    
    // Read directly from userPreferences.json file
    const userPrefsJsonPath = path.join(__dirname, '..', 'TeamProject', 'data', 'userPreferences.json');
    let allPrefs = [];
    
    if (fs.existsSync(userPrefsJsonPath)) {
      allPrefs = await fs.readJson(userPrefsJsonPath);
      console.log(`Read ${allPrefs.length} preference entries from userPreferences.json`);
    } else {
      console.error('userPreferences.json not found at:', userPrefsJsonPath);
      return res.json({
        id: userId,
        userId: userId,
        cityPreferences: [],
        weatherPreferences: [],
        temperaturePreferences: []
      });
    }
    
    // Find user preferences that match the userId (stored as 'id' in the file)
    const userPrefs = allPrefs.find(pref => {
      const prefId = typeof pref.id === 'string' ? parseInt(pref.id) : pref.id;
      return prefId === userId;
    });
    
    if (userPrefs) {
      console.log(`Found preferences for userId ${userId}:`, {
        id: userPrefs.id,
        clientId: userPrefs.clientId,
        cities: userPrefs.cityPreferences?.length || 0,
        weather: userPrefs.weatherPreferences?.length || 0,
        temp: userPrefs.temperaturePreferences?.length || 0
      });
      res.json({
        ...userPrefs,
        userId: userId
      });
    } else {
      console.log(`No preferences found for userId ${userId}, returning empty preferences`);
      // Return empty preferences if not found
      res.json({
        id: userId,
        userId: userId,
        cityPreferences: [],
        weatherPreferences: [],
        temperaturePreferences: []
      });
    }
  } catch (error) {
    console.error('Error getting preferences by userId:', error);
    res.status(500).json({ error: error.message });
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

// Get all TARS users (from users.json) - for admin dashboard
app.get('/api/tarsUsers', async (req, res) => {
  try {
    // Read users.json directly
    const usersJsonPath = path.resolve(__dirname, '..', 'TeamProject', 'data', 'users.json');
    
    if (!fs.existsSync(usersJsonPath)) {
      console.error('users.json not found at:', usersJsonPath);
      return res.json([]);
    }
    
    const tarsUsers = await fs.readJson(usersJsonPath);
    console.log(`Loaded ${Array.isArray(tarsUsers) ? tarsUsers.length : 0} users from users.json`);
    
    // Ensure we always return an array
    res.json(Array.isArray(tarsUsers) ? tarsUsers : []);
  } catch (error) {
    console.error('Error getting TARS users:', error);
    // Return empty array on error instead of error object
    res.json([]);
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

// Login endpoint - find user by username, email, or userId
app.post('/api/login', async (req, res) => {
  try {
    const { username, email, userId } = req.body;
    
    console.log('Login attempt:', { username, email, userId });
    
    if (!username && !email && !userId) {
      return res.status(400).json({ error: 'Username, email, or userId is required' });
    }

    // Read users.json directly to get TarsUser data (username, email, userId)
    const usersJsonPath = path.join(__dirname, '..', 'TeamProject', 'data', 'users.json');
    let tarsUsers = [];
    
    try {
      if (fs.existsSync(usersJsonPath)) {
        tarsUsers = await fs.readJson(usersJsonPath);
        console.log(`Loaded ${tarsUsers.length} users from database`);
      } else {
        console.error('users.json not found at:', usersJsonPath);
        return res.status(500).json({ error: 'User database not found' });
      }
    } catch (err) {
      console.error('Error reading users.json:', err);
      return res.status(500).json({ error: 'Failed to read user database: ' + err.message });
    }

    // Find user by userId, username, or email
    let foundTarsUser = null;
    
    if (userId) {
      foundTarsUser = tarsUsers.find(u => u.userId === parseInt(userId));
      console.log('Searching by userId:', userId, 'Found:', foundTarsUser ? 'Yes' : 'No');
    } else if (username) {
      const searchUsername = username.toLowerCase().trim();
      foundTarsUser = tarsUsers.find(u => 
        u.username && u.username.toLowerCase() === searchUsername
      );
      console.log('Searching by username:', searchUsername, 'Found:', foundTarsUser ? 'Yes' : 'No');
    } else if (email) {
      const searchEmail = email.toLowerCase().trim();
      foundTarsUser = tarsUsers.find(u => 
        u.email && u.email.toLowerCase() === searchEmail
      );
      console.log('Searching by email:', searchEmail);
      console.log('Available emails:', tarsUsers.map(u => u.email).join(', '));
      console.log('Found:', foundTarsUser ? 'Yes' : 'No');
    }

    if (!foundTarsUser) {
      return res.status(404).json({ error: 'User not found. Please check your credentials.' });
    }

    // Check if user is active
    if (!foundTarsUser.active) {
      return res.status(403).json({ error: 'User account is inactive.' });
    }

    // Get user preferences using userId
    let userPreferences = null;
    try {
      userPreferences = await apiClient.getUser(foundTarsUser.userId);
    } catch (err) {
      // User preferences might not exist yet, that's okay
      console.log('User preferences not found for userId', foundTarsUser.userId, ', creating empty preferences');
      userPreferences = {
        id: foundTarsUser.userId,
        clientId: foundTarsUser.clientId,
        cityPreferences: [],
        weatherPreferences: [],
        temperaturePreferences: []
      };
    }

    console.log('Login successful for user:', foundTarsUser.username || foundTarsUser.email);

    // Return user data for login
    res.json({
      userId: foundTarsUser.userId,
      clientId: foundTarsUser.clientId,
      username: foundTarsUser.username,
      email: foundTarsUser.email,
      role: foundTarsUser.role,
      preferences: {
        cityPreferences: userPreferences.cityPreferences || [],
        weatherPreferences: userPreferences.weatherPreferences || [],
        temperaturePreferences: userPreferences.temperaturePreferences || []
      }
    });
  } catch (error) {
    console.error('Error during login:', error);
    res.status(500).json({ error: error.message });
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

// Start server
app.listen(PORT, () => {
  console.log(`TARS Client Server running on http://localhost:${PORT}`);
  console.log(`Dashboard available at http://localhost:${PORT}/dashboard`);
});

