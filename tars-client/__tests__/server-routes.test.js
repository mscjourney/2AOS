/**
 * Unit tests for Express server routes in server.js
 * These tests mock the TarsApiClient to test the Express route handlers
 */

const request = require('supertest');

// Mock TarsApiClient before requiring server
const mockApiClient = {
  getOrCreateClientId: jest.fn(),
  getClients: jest.fn(),
  createClient: jest.fn(),
  getIndex: jest.fn(),
  createClientUser: jest.fn(),
  setUserPreference: jest.fn(),
  removeUser: jest.fn(),
  getUser: jest.fn(),
  getUserByClientId: jest.fn(),
  getUserPreference: jest.fn(),
  getUserList: jest.fn(),
  getClientUserList: jest.fn(),
  getTarsUsers: jest.fn(),
  deleteTarsUser: jest.fn(),
  login: jest.fn(),
  getWeatherRecommendation: jest.fn(),
  getWeatherAlertsByCity: jest.fn(),
  getWeatherAlertsByCoordinates: jest.fn(),
  getUserWeatherAlerts: jest.fn(),
  getCrimeSummary: jest.fn(),
  getCountryAdvisory: jest.fn(),
  getCitySummary: jest.fn(),
  getCountrySummary: jest.fn(),
  loadClientId: jest.fn()
};

jest.mock('../src/tarsApiClient', () => {
  return jest.fn().mockImplementation(() => mockApiClient);
});

jest.mock('fs-extra', () => ({
  existsSync: jest.fn().mockReturnValue(false),
  readJsonSync: jest.fn(),
  writeJsonSync: jest.fn()
}));

describe('Express Server Routes', () => {
  let app;

  beforeEach(() => {
    // Reset all mocks before each test
    Object.values(mockApiClient).forEach(mock => {
      if (jest.isMockFunction(mock)) {
        mock.mockClear();
      }
    });

    // Mock console to reduce noise
    jest.spyOn(console, 'log').mockImplementation(() => {});
    jest.spyOn(console, 'error').mockImplementation(() => {});

    // Clear module cache and require server (will use mocked TarsApiClient)
    jest.resetModules();
    app = require('../server');
  });

  afterEach(() => {
    jest.clearAllMocks();
    console.log.mockRestore();
    console.error.mockRestore();
  });

  describe('GET /api/health', () => {
    test('should return health status', async () => {
      const response = await request(app)
        .get('/api/health')
        .expect(200);

      expect(response.body).toEqual({
        status: 'ok',
        message: 'TARS Client Server is running'
      });
    });
  });

  // Removed GET /api/client-id tests - getOrCreateClientId can create new clients

  describe('GET /api/clients', () => {
    test('should return list of clients', async () => {
      const mockClients = [
        { clientId: 1, name: 'Client 1', email: 'client1@test.com' },
        { clientId: 2, name: 'Client 2', email: 'client2@test.com' }
      ];
      mockApiClient.getClients.mockResolvedValue(mockClients);

      const response = await request(app)
        .get('/api/clients')
        .expect(200);

      expect(response.body).toEqual(mockClients);
    });

    test('should handle errors', async () => {
      mockApiClient.getClients.mockRejectedValue(new Error('Failed to get clients'));

      const response = await request(app)
        .get('/api/clients')
        .expect(500);

      expect(response.body).toHaveProperty('error');
    });
  });

  // Removed POST /api/client/create tests - they create new clients

  describe('GET /api/index', () => {
    test('should return welcome message', async () => {
      mockApiClient.getIndex.mockResolvedValue('Welcome to TARS');

      const response = await request(app)
        .get('/api/index')
        .expect(200);

      expect(response.body).toEqual({ message: 'Welcome to TARS' });
    });

    test('should handle errors', async () => {
      mockApiClient.getIndex.mockRejectedValue(new Error('Failed to get index'));

      const response = await request(app)
        .get('/api/index')
        .expect(500);

      expect(response.body).toHaveProperty('error');
    });
  });

  describe('POST /api/client/createUser', () => {
    test('should create a user', async () => {
      const mockUser = {
        userId: 10,
        clientId: 1,
        username: 'testuser',
        email: 'test@test.com',
        role: 'user'
      };
      mockApiClient.createClientUser.mockResolvedValue(mockUser);

      const response = await request(app)
        .post('/api/client/createUser')
        .send({
          clientId: 1,
          username: 'testuser',
          email: 'test@test.com',
          role: 'user'
        })
        .expect(200);

      expect(response.body).toEqual(mockUser);
      expect(mockApiClient.createClientUser).toHaveBeenCalledWith(1, 'testuser', 'test@test.com', 'user');
    });

    test('should return 400 if required fields are missing', async () => {
      const response = await request(app)
        .post('/api/client/createUser')
        .send({ clientId: 1, username: 'test' })
        .expect(400);

      expect(response.body).toHaveProperty('error', 'clientId, username, email, and role are required');
    });

    test('should handle errors', async () => {
      mockApiClient.createClientUser.mockRejectedValue(new Error('Failed to create user'));

      const response = await request(app)
        .post('/api/client/createUser')
        .send({
          clientId: 1,
          username: 'test',
          email: 'test@test.com',
          role: 'user'
        })
        .expect(500);

      expect(response.body).toHaveProperty('error');
    });
  });

  describe('PUT /api/setPreference/:id', () => {
    test('should set user preferences', async () => {
      const mockPrefs = {
        id: 5,
        cityPreferences: ['New York'],
        weatherPreferences: ['Sunny']
      };
      mockApiClient.setUserPreference.mockResolvedValue(mockPrefs);

      const response = await request(app)
        .put('/api/setPreference/5')
        .send({
          id: 5,
          cityPreferences: ['New York'],
          weatherPreferences: ['Sunny']
        })
        .expect(200);

      expect(response.body).toEqual(mockPrefs);
    });

    test('should handle errors with status code', async () => {
      const error = new Error('Bad request');
      error.response = { status: 400 };
      mockApiClient.setUserPreference.mockRejectedValue(error);

      const response = await request(app)
        .put('/api/setPreference/5')
        .send({ id: 5 })
        .expect(400);

      expect(response.body).toHaveProperty('error');
    });
  });

  describe('GET /api/user/:id', () => {
    test('should get user by ID', async () => {
      const mockUser = { userId: 5, username: 'testuser' };
      mockApiClient.getUser.mockResolvedValue(mockUser);

      const response = await request(app)
        .get('/api/user/5')
        .expect(200);

      expect(response.body).toEqual(mockUser);
    });

    test('should handle errors', async () => {
      mockApiClient.getUser.mockRejectedValue(new Error('User not found'));

      const response = await request(app)
        .get('/api/user/999')
        .expect(500);

      expect(response.body).toHaveProperty('error');
    });
  });

  describe('GET /api/user/client/:clientId', () => {
    test('should get user by clientId', async () => {
      const mockPrefs = { id: 5, cityPreferences: [] };
      mockApiClient.getUserByClientId.mockResolvedValue(mockPrefs);

      const response = await request(app)
        .get('/api/user/client/1')
        .expect(200);

      expect(response.body).toEqual(mockPrefs);
    });

    test('should handle errors', async () => {
      mockApiClient.getUserByClientId.mockRejectedValue(new Error('No user found'));

      const response = await request(app)
        .get('/api/user/client/999')
        .expect(500);

      expect(response.body).toHaveProperty('error');
    });
  });

  describe('GET /api/preferences/user/:userId', () => {
    test('should get user preferences', async () => {
      const mockPrefs = { id: 5, cityPreferences: ['NYC'] };
      mockApiClient.getUserPreference.mockResolvedValue(mockPrefs);

      const response = await request(app)
        .get('/api/preferences/user/5')
        .expect(200);

      expect(response.body).toEqual({ ...mockPrefs, userId: 5 });
    });

    test('should return empty preferences if user not found', async () => {
      const error = new Error('User not found');
      error.message = 'User not found';
      mockApiClient.getUserPreference.mockRejectedValue(error);

      const response = await request(app)
        .get('/api/preferences/user/999')
        .expect(200);

      expect(response.body).toEqual({
        id: 999,
        userId: 999,
        cityPreferences: [],
        weatherPreferences: [],
        temperaturePreferences: []
      });
    });
  });

  describe('POST /api/login', () => {
    test('should login with username', async () => {
      const mockLoginResult = {
        userId: 5,
        username: 'testuser',
        clientId: 1
      };
      mockApiClient.login.mockResolvedValue(mockLoginResult);

      const response = await request(app)
        .post('/api/login')
        .send({ username: 'testuser' })
        .expect(200);

      expect(response.body).toEqual(mockLoginResult);
    });

    test('should handle login errors', async () => {
      const error = new Error('Invalid credentials');
      error.response = { status: 401 };
      mockApiClient.login.mockRejectedValue(error);

      const response = await request(app)
        .post('/api/login')
        .send({ username: 'invalid' })
        .expect(401);

      expect(response.body).toHaveProperty('error');
    });
  });

  describe('GET /api/recommendation/weather', () => {
    test('should get weather recommendation', async () => {
      const mockRecommendation = { city: 'New York', days: 5 };
      mockApiClient.getWeatherRecommendation.mockResolvedValue(mockRecommendation);

      const response = await request(app)
        .get('/api/recommendation/weather?city=New York&days=5')
        .expect(200);

      expect(response.body).toEqual(mockRecommendation);
    });

    test('should return 400 if parameters missing', async () => {
      const response = await request(app)
        .get('/api/recommendation/weather?city=New York')
        .expect(400);

      expect(response.body).toHaveProperty('error', 'city and days parameters are required');
    });
  });

  describe('GET /api/alert/weather', () => {
    test('should get weather alerts by city', async () => {
      const mockAlerts = { city: 'New York', alerts: [] };
      mockApiClient.getWeatherAlertsByCity.mockResolvedValue(mockAlerts);

      const response = await request(app)
        .get('/api/alert/weather?city=New York')
        .expect(200);

      expect(response.body).toEqual(mockAlerts);
    });

    test('should get weather alerts by coordinates', async () => {
      const mockAlerts = { lat: 40.7, lon: -74.0, alerts: [] };
      mockApiClient.getWeatherAlertsByCoordinates.mockResolvedValue(mockAlerts);

      const response = await request(app)
        .get('/api/alert/weather?lat=40.7&lon=-74.0')
        .expect(200);

      expect(response.body).toEqual(mockAlerts);
    });

    test('should return 400 if no city or coordinates', async () => {
      const response = await request(app)
        .get('/api/alert/weather')
        .expect(400);

      expect(response.body).toHaveProperty('error', 'Either city or lat/lon parameters are required');
    });
  });

  describe('GET /api/crime/summary', () => {
    test('should get crime summary', async () => {
      const mockSummary = { state: 'CA', offense: 'ASS', count: 100 };
      mockApiClient.getCrimeSummary.mockResolvedValue(mockSummary);

      const response = await request(app)
        .get('/api/crime/summary?state=CA&offense=ASS&month=01&year=2023')
        .expect(200);

      expect(response.body).toEqual(mockSummary);
    });

    test('should return 400 if parameters missing', async () => {
      const response = await request(app)
        .get('/api/crime/summary?state=CA&offense=ASS')
        .expect(400);

      expect(response.body).toHaveProperty('error', 'state, offense, month, and year parameters are required');
    });
  });

  describe('GET /api/country/:country', () => {
    test('should get country advisory', async () => {
      mockApiClient.getCountryAdvisory.mockResolvedValue('Level 1: Exercise normal precautions');

      const response = await request(app)
        .get('/api/country/United States')
        .expect(200);

      expect(response.body).toEqual({ advisory: 'Level 1: Exercise normal precautions' });
    });
  });

  describe('GET /api/summary/:city', () => {
    test('should get city summary', async () => {
      const mockSummary = { city: 'New York', message: 'Summary' };
      mockApiClient.getCitySummary.mockResolvedValue(mockSummary);

      const response = await request(app)
        .get('/api/summary/New York')
        .expect(200);

      expect(response.body).toEqual(mockSummary);
    });

    test('should get city summary with query parameters', async () => {
      const mockSummary = { city: 'New York' };
      mockApiClient.getCitySummary.mockResolvedValue(mockSummary);

      const response = await request(app)
        .get('/api/summary/New York?state=NY&startDate=2024-01-01&endDate=2024-01-31')
        .expect(200);

      expect(response.body).toEqual(mockSummary);
      expect(mockApiClient.getCitySummary).toHaveBeenCalledWith('New York', '2024-01-01', '2024-01-31', 'NY');
    });
  });

  describe('GET /api/countrySummary/:country', () => {
    test('should get country summary', async () => {
      const mockSummary = { country: 'United States', capital: 'Washington' };
      mockApiClient.getCountrySummary.mockResolvedValue(mockSummary);

      const response = await request(app)
        .get('/api/countrySummary/United States')
        .expect(200);

      expect(response.body).toEqual(mockSummary);
      expect(response.headers['content-type']).toMatch(/json/);
    });

    test('should handle URL encoded country names', async () => {
      const mockSummary = { country: 'United States' };
      mockApiClient.getCountrySummary.mockResolvedValue(mockSummary);

      const response = await request(app)
        .get('/api/countrySummary/United%20States')
        .expect(200);

      expect(response.body).toEqual(mockSummary);
    });
  });

  describe('GET /api/userList', () => {
    test('should get user list', async () => {
      const mockUsers = [{ userId: 1 }, { userId: 2 }];
      mockApiClient.getUserList.mockResolvedValue(mockUsers);

      const response = await request(app)
        .get('/api/userList')
        .expect(200);

      expect(response.body).toEqual(mockUsers);
    });
  });

  describe('GET /api/tarsUsers', () => {
    test('should get TARS users', async () => {
      const mockUsers = [{ userId: 1, username: 'user1' }];
      mockApiClient.getTarsUsers.mockResolvedValue(mockUsers);

      const response = await request(app)
        .get('/api/tarsUsers')
        .expect(200);

      expect(response.body).toEqual(mockUsers);
    });
  });

  describe('DELETE /api/tarsUsers/:userId', () => {
    test('should delete TARS user', async () => {
      const mockDeletedUser = { userId: 5, username: 'deleted' };
      mockApiClient.deleteTarsUser.mockResolvedValue(mockDeletedUser);

      const response = await request(app)
        .delete('/api/tarsUsers/5')
        .expect(200);

      expect(response.body).toHaveProperty('message');
      expect(response.body.deletedUser).toEqual(mockDeletedUser);
    });

    test('should return 404 if user not found', async () => {
      const error = new Error('User not found');
      error.message = 'User not found';
      mockApiClient.deleteTarsUser.mockRejectedValue(error);

      const response = await request(app)
        .delete('/api/tarsUsers/999')
        .expect(404);

      expect(response.body).toHaveProperty('error');
    });
  });

  describe('GET /api/userList/client/:clientId', () => {
    test('should get users for a client', async () => {
      const mockUsers = [{ userId: 1, clientId: 1 }];
      mockApiClient.getClientUserList.mockResolvedValue(mockUsers);

      const response = await request(app)
        .get('/api/userList/client/1')
        .expect(200);

      expect(response.body).toEqual(mockUsers);
    });
  });

  describe('GET /api/alert/weather/user/:userId', () => {
    test('should get user weather alerts', async () => {
      const mockAlerts = { userId: 5, alerts: [] };
      mockApiClient.getUserWeatherAlerts.mockResolvedValue(mockAlerts);

      const response = await request(app)
        .get('/api/alert/weather/user/5')
        .expect(200);

      expect(response.body).toEqual(mockAlerts);
    });
  });

  describe('PUT /api/user/:id/remove', () => {
    test('should remove user', async () => {
      mockApiClient.removeUser.mockResolvedValue('User removed');

      const response = await request(app)
        .put('/api/user/5/remove')
        .expect(200);

      expect(response.body).toEqual({ message: 'User removed' });
    });

    test('should handle errors', async () => {
      mockApiClient.removeUser.mockRejectedValue(new Error('Failed to remove user'));

      const response = await request(app)
        .put('/api/user/5/remove')
        .expect(500);

      expect(response.body).toHaveProperty('error');
    });
  });

  describe('Error handling paths', () => {
    test('should handle getUserList errors', async () => {
      mockApiClient.getUserList.mockRejectedValue(new Error('Database error'));

      const response = await request(app)
        .get('/api/userList')
        .expect(500);

      expect(response.body).toHaveProperty('error');
    });

    test('should handle getTarsUsers errors', async () => {
      mockApiClient.getTarsUsers.mockRejectedValue(new Error('Database error'));

      const response = await request(app)
        .get('/api/tarsUsers')
        .expect(500);

      expect(response.body).toHaveProperty('error');
    });

    test('should handle getClientUserList errors', async () => {
      mockApiClient.getClientUserList.mockRejectedValue(new Error('Database error'));

      const response = await request(app)
        .get('/api/userList/client/1')
        .expect(500);

      expect(response.body).toHaveProperty('error');
    });

    test('should handle getUserWeatherAlerts errors', async () => {
      mockApiClient.getUserWeatherAlerts.mockRejectedValue(new Error('Service error'));

      const response = await request(app)
        .get('/api/alert/weather/user/5')
        .expect(500);

      expect(response.body).toHaveProperty('error');
    });

    test('should handle getCitySummary errors', async () => {
      mockApiClient.getCitySummary.mockRejectedValue(new Error('Service error'));

      const response = await request(app)
        .get('/api/summary/InvalidCity')
        .expect(500);

      expect(response.body).toHaveProperty('error');
    });

    test('should handle getCountrySummary errors', async () => {
      mockApiClient.getCountrySummary.mockRejectedValue(new Error('Service error'));

      const response = await request(app)
        .get('/api/countrySummary/InvalidCountry')
        .expect(500);

      expect(response.body).toHaveProperty('error');
    });

    test('should handle getWeatherRecommendation errors', async () => {
      mockApiClient.getWeatherRecommendation.mockRejectedValue(new Error('Service error'));

      const response = await request(app)
        .get('/api/recommendation/weather?city=NYC&days=5')
        .expect(500);

      expect(response.body).toHaveProperty('error');
    });

    test('should handle getWeatherAlerts errors', async () => {
      mockApiClient.getWeatherAlertsByCity.mockRejectedValue(new Error('Service error'));

      const response = await request(app)
        .get('/api/alert/weather?city=NYC')
        .expect(500);

      expect(response.body).toHaveProperty('error');
    });

    test('should handle getCrimeSummary errors', async () => {
      const error = new Error('Service error');
      error.response = { data: { message: 'Invalid parameters' } };
      mockApiClient.getCrimeSummary.mockRejectedValue(error);

      const response = await request(app)
        .get('/api/crime/summary?state=CA&offense=ASS&month=01&year=2023')
        .expect(500);

      expect(response.body).toHaveProperty('error');
    });
  });
});

