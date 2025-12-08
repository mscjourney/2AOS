const axios = require('axios');
const fs = require('fs-extra');
const path = require('path');

/**
 * TARS API Client for Node.js
 * Wraps the Java TARS API endpoints and manages persistent client ID storage
 */
class TarsApiClient {
  constructor(baseUrl = 'http://localhost:8080') {
    this.baseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl;
    this.clientConfigPath = path.join(__dirname, '..', 'client-config.json');
    this.clientId = null;
    this.loadClientId();
  }

  /**
   * Load client ID from local storage
   */
  loadClientId() {
    try {
      if (fs.existsSync(this.clientConfigPath)) {
        const config = fs.readJsonSync(this.clientConfigPath);
        this.clientId = config.clientId;
        console.log(`Loaded client ID: ${this.clientId}`);
      }
    } catch (error) {
      console.warn('Could not load client ID:', error.message);
    }
  }

  /**
   * Save client ID to local storage
   */
  saveClientId(clientId) {
    try {
      this.clientId = clientId;
      fs.writeJsonSync(this.clientConfigPath, { clientId }, { spaces: 2 });
      console.log(`Saved client ID: ${clientId}`);
    } catch (error) {
      console.error('Could not save client ID:', error.message);
    }
  }

  /**
   * Get existing client ID or create a new one
   */
  async getOrCreateClientId() {
    if (this.clientId) {
      return this.clientId;
    }

    // Try to create a new client with a unique name
    const timestamp = Date.now();
    const name = `Client-${timestamp}`;
    const email = `client-${timestamp}@tars.local`;

    try {
      const result = await this.createClient(name, email);
      if (result && result.clientId) {
        this.saveClientId(result.clientId);
        return result.clientId;
      }
    } catch (error) {
      console.error('Failed to create client:', error.message);
      throw error;
    }

    throw new Error('Failed to get or create client ID');
  }

  /**
   * Get the welcome message from the index endpoint
   */
  async getIndex() {
    try {
      const response = await axios.get(`${this.baseUrl}/`);
      return response.data;
    } catch (error) {
      throw new Error(`Failed to get index: ${error.message}`);
    }
  }

  /**
   * Create a new client
   */
  async createClient(name, email) {
    try {
      const response = await axios.post(`${this.baseUrl}/client/create`, {
        name,
        email
      });
      return response.data;
    } catch (error) {
      const errorMsg = error.response?.data 
        ? (typeof error.response.data === 'object' 
            ? JSON.stringify(error.response.data) 
            : error.response.data)
        : error.message;
      throw new Error(`Failed to create client: ${errorMsg}`);
    }
  }

  /**
   * Create a new user for a client
   */
  async createClientUser(clientId, username, email, role) {
    try {
      const response = await axios.post(`${this.baseUrl}/client/createUser`, {
        clientId,
        username,
        email,
        role
      });
      return response.data;
    } catch (error) {
      const errorMsg = error.response?.data 
        ? (typeof error.response.data === 'object' 
            ? JSON.stringify(error.response.data) 
            : error.response.data)
        : error.message;
      throw new Error(`Failed to create user: ${errorMsg}`);
    }
  }

  /**
   * Set user preferences (adds or updates)
   */
  async setUserPreference(userId, user) {
    try {
      console.log(`[tarsApiClient] Setting preference for userId: ${userId}`, user);
      const response = await axios.put(`${this.baseUrl}/setPreference/${userId}`, user);
      console.log(`[tarsApiClient] Success response:`, response.data);
      return response.data;
    } catch (error) {
      console.error(`[tarsApiClient] Error setting preference:`, error);
      console.error(`[tarsApiClient] Error response:`, error.response);
      // Extract error message from response
      let errorMsg = error.message;
      if (error.response?.data) {
        if (typeof error.response.data === 'string') {
          errorMsg = error.response.data;
        } else if (typeof error.response.data === 'object') {
          errorMsg = error.response.data.error || JSON.stringify(error.response.data);
        }
      }
      // Preserve the original error structure so server.js can access it
      const enhancedError = error; // Keep the original error
      enhancedError.message = errorMsg; // Update message
      throw enhancedError;
    }
  }

  /**
   * Remove user
   */
  async removeUser(userId) {
    try {
      const response = await axios.put(`${this.baseUrl}/user/${userId}/remove`);
      return response.data;
    } catch (error) {
      throw new Error(`Failed to remove user: ${error.response?.data || error.message}`);
    }
  }

  /**
   * Get user by ID
   */
  async getUser(userId) {
    try {
      const response = await axios.get(`${this.baseUrl}/user/${userId}`);
      return response.data;
    } catch (error) {
      throw new Error(`Failed to get user: ${error.response?.data || error.message}`);
    }
  }

  /**
   * Get all users
   */
  async getUserList() {
    try {
      const response = await axios.get(`${this.baseUrl}/userList`);
      return response.data;
    } catch (error) {
      throw new Error(`Failed to get user list: ${error.response?.data || error.message}`);
    }
  }

  /**
   * Get users for a specific client
   */
  async getClientUserList(clientId) {
    try {
      const response = await axios.get(`${this.baseUrl}/userList/client/${clientId}`);
      return response.data;
    } catch (error) {
      throw new Error(`Failed to get client user list: ${error.response?.data || error.message}`);
    }
  }

  /**
   * Get all TarsUsers from users.json
   */
  async getTarsUsers() {
    try {
      const response = await axios.get(`${this.baseUrl}/tarsUsers`);
      return response.data;
    } catch (error) {
      throw new Error(`Failed to get TARS users: ${error.response?.data || error.message}`);
    }
  }

  /**
   * Delete a TarsUser by userId
   */
  async deleteTarsUser(userId) {
    try {
      const response = await axios.delete(`${this.baseUrl}/tarsUsers/${userId}`);
      return response.data;
    } catch (error) {
      throw new Error(`Failed to delete TARS user: ${error.response?.data || error.message}`);
    }
  }

  /**
   * Get weather recommendation
   */
  async getWeatherRecommendation(city, days) {
    try {
      const response = await axios.get(`${this.baseUrl}/recommendation/weather`, {
        params: { city, days }
      });
      return response.data;
    } catch (error) {
      throw new Error(`Failed to get weather recommendation: ${error.response?.data || error.message}`);
    }
  }

  /**
   * Get weather recommendation for a user based on their preferences (getUserRec)
   * userId is now a path variable for consistency
   */
  async getUserWeatherRecommendation(city, userId, days) {
    try {
      const response = await axios.get(`${this.baseUrl}/recommendation/weather/user/${userId}`, {
        params: { city, days }
      });
      return response.data;
    } catch (error) {
      throw new Error(`Failed to get user weather recommendation: ${error.response?.data || error.message}`);
    }
  }

  /**
   * Get weather alerts by city
   */
  async getWeatherAlertsByCity(city) {
    try {
      const response = await axios.get(`${this.baseUrl}/alert/weather`, {
        params: { city }
      });
      return response.data;
    } catch (error) {
      throw new Error(`Failed to get weather alerts: ${error.response?.data || error.message}`);
    }
  }

  /**
   * Get weather alerts by coordinates
   */
  async getWeatherAlertsByCoordinates(lat, lon) {
    try {
      const response = await axios.get(`${this.baseUrl}/alert/weather`, {
        params: { lat, lon }
      });
      return response.data;
    } catch (error) {
      throw new Error(`Failed to get weather alerts: ${error.response?.data || error.message}`);
    }
  }

  /**
   * Get user weather alerts
   */
  async getUserWeatherAlerts(userId) {
    try {
      const response = await axios.get(`${this.baseUrl}/alert/weather/user/${userId}`);
      return response.data;
    } catch (error) {
      throw new Error(`Failed to get user weather alerts: ${error.response?.data || error.message}`);
    }
  }

  /**
   * Get crime summary
   */
  async getCrimeSummary(state, offense, month, year) {
    try {
      const response = await axios.get(`${this.baseUrl}/crime/summary`, {
        params: { 
          state: String(state), 
          offense: String(offense), 
          month: String(month), 
          year: String(year) 
        }
      });
      return response.data;
    } catch (error) {
      const errorMsg = error.response?.data?.message || error.response?.data || error.message;
      throw new Error(`Failed to get crime summary: ${errorMsg}`);
    }
  }

  /**
   * Get country advisory
   */
  async getCountryAdvisory(country) {
    try {
      const response = await axios.get(`${this.baseUrl}/country/${encodeURIComponent(country)}`);
      return response.data;
    } catch (error) {
      throw new Error(`Failed to get country advisory: ${error.response?.data || error.message}`);
    }
  }

  /**
   * Get country summary
   */
  async getCountrySummary(country) {
    try {
      const response = await axios.get(
        `${this.baseUrl}/countrySummary/${encodeURIComponent(country)}`
      );
      return response.data;
    } catch (error) {
      const errorMsg = error.response?.data 
        ? (typeof error.response.data === 'object' 
            ? JSON.stringify(error.response.data) 
            : error.response.data)
        : error.message;
      throw new Error(`Failed to get country summary: ${errorMsg}`);
    }
  }

  /**
   * Get all clients
   */
  async getClients() {
    try {
      const response = await axios.get(`${this.baseUrl}/clients`);
      return response.data;
    } catch (error) {
      const errorMsg = error.response?.data 
        ? (typeof error.response.data === 'object' 
            ? JSON.stringify(error.response.data) 
            : error.response.data)
        : error.message;
      throw new Error(`Failed to get clients: ${errorMsg}`);
    }
  }

  /**
   * Get user by clientId
   */
  async getUserByClientId(clientId) {
    try {
      const response = await axios.get(`${this.baseUrl}/user/client/${clientId}`);
      return response.data;
    } catch (error) {
      const errorMsg = error.response?.data 
        ? (typeof error.response.data === 'object' 
            ? JSON.stringify(error.response.data) 
            : error.response.data)
        : error.message;
      throw new Error(`Failed to get user by clientId: ${errorMsg}`);
    }
  }

  /**
   * Get user preferences by userId
   */
  async getUserPreference(userId) {
    try {
      const response = await axios.get(`${this.baseUrl}/retrievePreference/${userId}`);
      return response.data;
    } catch (error) {
      const errorMsg = error.response?.data 
        ? (typeof error.response.data === 'object' 
            ? JSON.stringify(error.response.data) 
            : error.response.data)
        : error.message;
      throw new Error(`Failed to get user preference: ${errorMsg}`);
    }
  }

  /**
   * Login user by username, email, or userId
   */
  async login(username, email, userId) {
    try {
      const body = {};
      if (username) body.username = username;
      if (email) body.email = email;
      if (userId) body.userId = userId;
      
      const response = await axios.post(`${this.baseUrl}/login`, body);
      return response.data;
    } catch (error) {
      const errorMsg = error.response?.data 
        ? (typeof error.response.data === 'object' 
            ? JSON.stringify(error.response.data) 
            : error.response.data)
        : error.message;
      throw new Error(`Failed to login: ${errorMsg}`);
    }
  }
}

module.exports = TarsApiClient;

