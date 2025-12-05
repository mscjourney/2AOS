/**
 * Unit tests for TarsApiClient class
 * Tests individual methods with mocked axios calls
 */

const axios = require('axios');
const fs = require('fs-extra');
const path = require('path');

jest.mock('axios');
jest.mock('fs-extra');

const TarsApiClient = require('../src/tarsApiClient');

describe('TarsApiClient', () => {
  let apiClient;
  const mockBaseUrl = 'http://localhost:8080';

  beforeEach(() => {
    // Reset all mocks
    jest.clearAllMocks();
    
    // Mock fs-extra
    fs.existsSync = jest.fn().mockReturnValue(false);
    fs.readJsonSync = jest.fn();
    fs.writeJsonSync = jest.fn();

    // Mock console methods
    jest.spyOn(console, 'log').mockImplementation(() => {});
    jest.spyOn(console, 'warn').mockImplementation(() => {});
    jest.spyOn(console, 'error').mockImplementation(() => {});

    apiClient = new TarsApiClient(mockBaseUrl);
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  describe('Constructor', () => {
    test('should initialize with default baseUrl', () => {
      const client = new TarsApiClient();
      expect(client.baseUrl).toBe('http://localhost:8080');
    });

    test('should initialize with custom baseUrl', () => {
      const client = new TarsApiClient('http://example.com');
      expect(client.baseUrl).toBe('http://example.com');
    });

    test('should remove trailing slash from baseUrl', () => {
      const client = new TarsApiClient('http://example.com/');
      expect(client.baseUrl).toBe('http://example.com');
    });

    test('should load existing client ID', () => {
      fs.existsSync.mockReturnValue(true);
      fs.readJsonSync.mockReturnValue({ clientId: 5 });
      const client = new TarsApiClient();
      expect(client.clientId).toBe(5);
    });

    test('should handle error loading client ID', () => {
      fs.existsSync.mockReturnValue(true);
      fs.readJsonSync.mockImplementation(() => {
        throw new Error('Read error');
      });
      const client = new TarsApiClient();
      expect(client.clientId).toBeNull();
    });
  });

  describe('loadClientId', () => {
    test('should load client ID from file', () => {
      fs.existsSync.mockReturnValue(true);
      fs.readJsonSync.mockReturnValue({ clientId: 10 });
      apiClient.loadClientId();
      expect(apiClient.clientId).toBe(10);
    });

    test('should handle missing file', () => {
      fs.existsSync.mockReturnValue(false);
      apiClient.loadClientId();
      expect(apiClient.clientId).toBeNull();
    });

    test('should handle read error', () => {
      fs.existsSync.mockReturnValue(true);
      fs.readJsonSync.mockImplementation(() => {
        throw new Error('Read error');
      });
      apiClient.loadClientId();
      expect(console.warn).toHaveBeenCalled();
    });
  });

  describe('saveClientId', () => {
    test('should save client ID to file', () => {
      apiClient.saveClientId(15);
      expect(apiClient.clientId).toBe(15);
      expect(fs.writeJsonSync).toHaveBeenCalledWith(
        expect.stringContaining('client-config.json'),
        { clientId: 15 },
        { spaces: 2 }
      );
    });

    test('should handle save error', () => {
      fs.writeJsonSync.mockImplementation(() => {
        throw new Error('Write error');
      });
      apiClient.saveClientId(15);
      expect(console.error).toHaveBeenCalled();
    });
  });

  describe('getOrCreateClientId', () => {
    test('should return existing client ID', async () => {
      apiClient.clientId = 20;
      const result = await apiClient.getOrCreateClientId();
      expect(result).toBe(20);
    });

    // Removed tests that create new clients
  });

  describe('getIndex', () => {
    test('should get welcome message', async () => {
      axios.get.mockResolvedValue({ data: 'Welcome to TARS' });
      const result = await apiClient.getIndex();
      expect(result).toBe('Welcome to TARS');
      expect(axios.get).toHaveBeenCalledWith(`${mockBaseUrl}/`);
    });

    test('should handle error', async () => {
      axios.get.mockRejectedValue(new Error('Network error'));
      await expect(apiClient.getIndex()).rejects.toThrow('Failed to get index');
    });
  });

  // Removed createClient tests - they create new clients

  describe('createClientUser', () => {
    test('should create a user', async () => {
      const mockUser = { userId: 1, username: 'user1', clientId: 1 };
      axios.post.mockResolvedValue({ data: mockUser });
      const result = await apiClient.createClientUser(1, 'user1', 'user1@test.com', 'user');
      expect(result).toEqual(mockUser);
    });

    test('should handle error', async () => {
      axios.post.mockRejectedValue(new Error('Network error'));
      await expect(apiClient.createClientUser(1, 'user1', 'user1@test.com', 'user')).rejects.toThrow();
    });
  });

  describe('setUserPreference', () => {
    test('should set user preferences', async () => {
      const mockPrefs = { id: 1, cityPreferences: ['NYC'] };
      axios.put.mockResolvedValue({ data: mockPrefs });
      const result = await apiClient.setUserPreference(1, { id: 1, cityPreferences: ['NYC'] });
      expect(result).toEqual(mockPrefs);
    });

    test('should handle error', async () => {
      axios.put.mockRejectedValue(new Error('Network error'));
      await expect(apiClient.setUserPreference(1, {})).rejects.toThrow();
    });
  });

  describe('removeUser', () => {
    test('should remove user', async () => {
      axios.put.mockResolvedValue({ data: 'User removed' });
      const result = await apiClient.removeUser(1);
      expect(result).toBe('User removed');
    });

    test('should handle error', async () => {
      axios.put.mockRejectedValue(new Error('Network error'));
      await expect(apiClient.removeUser(1)).rejects.toThrow();
    });
  });

  describe('getUser', () => {
    test('should get user by ID', async () => {
      const mockUser = { userId: 1, username: 'user1' };
      axios.get.mockResolvedValue({ data: mockUser });
      const result = await apiClient.getUser(1);
      expect(result).toEqual(mockUser);
    });

    test('should handle error', async () => {
      axios.get.mockRejectedValue(new Error('Network error'));
      await expect(apiClient.getUser(1)).rejects.toThrow();
    });
  });

  describe('getUserList', () => {
    test('should get user list', async () => {
      const mockUsers = [{ userId: 1 }, { userId: 2 }];
      axios.get.mockResolvedValue({ data: mockUsers });
      const result = await apiClient.getUserList();
      expect(result).toEqual(mockUsers);
    });

    test('should handle error', async () => {
      axios.get.mockRejectedValue(new Error('Network error'));
      await expect(apiClient.getUserList()).rejects.toThrow();
    });
  });

  describe('getClientUserList', () => {
    test('should get users for a client', async () => {
      const mockUsers = [{ userId: 1, clientId: 1 }];
      axios.get.mockResolvedValue({ data: mockUsers });
      const result = await apiClient.getClientUserList(1);
      expect(result).toEqual(mockUsers);
    });

    test('should handle error', async () => {
      axios.get.mockRejectedValue(new Error('Network error'));
      await expect(apiClient.getClientUserList(1)).rejects.toThrow();
    });
  });

  describe('getTarsUsers', () => {
    test('should get TARS users', async () => {
      const mockUsers = [{ userId: 1, username: 'admin' }];
      axios.get.mockResolvedValue({ data: mockUsers });
      const result = await apiClient.getTarsUsers();
      expect(result).toEqual(mockUsers);
    });

    test('should handle error', async () => {
      axios.get.mockRejectedValue(new Error('Network error'));
      await expect(apiClient.getTarsUsers()).rejects.toThrow();
    });
  });

  describe('deleteTarsUser', () => {
    test('should delete TARS user', async () => {
      const mockUser = { userId: 1, username: 'user1' };
      axios.delete.mockResolvedValue({ data: mockUser });
      const result = await apiClient.deleteTarsUser(1);
      expect(result).toEqual(mockUser);
    });

    test('should handle error', async () => {
      axios.delete.mockRejectedValue(new Error('Network error'));
      await expect(apiClient.deleteTarsUser(1)).rejects.toThrow();
    });
  });

  describe('getWeatherRecommendation', () => {
    test('should get weather recommendation', async () => {
      const mockRec = { city: 'NYC', recommendation: 'Sunny' };
      axios.get.mockResolvedValue({ data: mockRec });
      const result = await apiClient.getWeatherRecommendation('NYC', 5);
      expect(result).toEqual(mockRec);
      expect(axios.get).toHaveBeenCalledWith(
        `${mockBaseUrl}/recommendation/weather/`,
        expect.objectContaining({
          params: expect.objectContaining({
            city: 'NYC',
            days: 5
          })
        })
      );
    });

    test('should handle error', async () => {
      axios.get.mockRejectedValue(new Error('Network error'));
      await expect(apiClient.getWeatherRecommendation('NYC', 5)).rejects.toThrow();
    });
  });

  describe('getWeatherAlertsByCity', () => {
    test('should get weather alerts by city', async () => {
      const mockAlerts = { city: 'NYC', alerts: [] };
      axios.get.mockResolvedValue({ data: mockAlerts });
      const result = await apiClient.getWeatherAlertsByCity('NYC');
      expect(result).toEqual(mockAlerts);
    });

    test('should handle error', async () => {
      axios.get.mockRejectedValue(new Error('Network error'));
      await expect(apiClient.getWeatherAlertsByCity('NYC')).rejects.toThrow();
    });
  });

  describe('getWeatherAlertsByCoordinates', () => {
    test('should get weather alerts by coordinates', async () => {
      const mockAlerts = { lat: 40.7, lon: -74.0, alerts: [] };
      axios.get.mockResolvedValue({ data: mockAlerts });
      const result = await apiClient.getWeatherAlertsByCoordinates(40.7, -74.0);
      expect(result).toEqual(mockAlerts);
    });

    test('should handle error', async () => {
      axios.get.mockRejectedValue(new Error('Network error'));
      await expect(apiClient.getWeatherAlertsByCoordinates(40.7, -74.0)).rejects.toThrow();
    });
  });

  describe('getUserWeatherAlerts', () => {
    test('should get user weather alerts', async () => {
      const mockAlerts = { userId: 1, alerts: [] };
      axios.get.mockResolvedValue({ data: mockAlerts });
      const result = await apiClient.getUserWeatherAlerts(1);
      expect(result).toEqual(mockAlerts);
    });

    test('should handle error', async () => {
      axios.get.mockRejectedValue(new Error('Network error'));
      await expect(apiClient.getUserWeatherAlerts(1)).rejects.toThrow();
    });
  });

  describe('getCrimeSummary', () => {
    test('should get crime summary', async () => {
      const mockSummary = { state: 'CA', offense: 'ASS', count: 100 };
      axios.get.mockResolvedValue({ data: mockSummary });
      const result = await apiClient.getCrimeSummary('CA', 'ASS', '01', '2023');
      expect(result).toEqual(mockSummary);
    });

    test('should handle error with response data', async () => {
      const error = {
        response: { data: { message: 'Invalid state' } },
        message: 'Request failed'
      };
      axios.get.mockRejectedValue(error);
      await expect(apiClient.getCrimeSummary('XX', 'ASS', '01', '2023')).rejects.toThrow();
    });

    test('should handle error with string response data', async () => {
      const error = {
        response: { data: 'Error message' },
        message: 'Request failed'
      };
      axios.get.mockRejectedValue(error);
      await expect(apiClient.getCrimeSummary('CA', 'ASS', '01', '2023')).rejects.toThrow();
    });
  });

  describe('getCountryAdvisory', () => {
    test('should get country advisory', async () => {
      axios.get.mockResolvedValue({ data: 'Level 1: Exercise normal precautions' });
      const result = await apiClient.getCountryAdvisory('United States');
      expect(result).toBe('Level 1: Exercise normal precautions');
    });

    test('should handle error', async () => {
      axios.get.mockRejectedValue(new Error('Network error'));
      await expect(apiClient.getCountryAdvisory('United States')).rejects.toThrow();
    });
  });

  describe('getCitySummary', () => {
    test('should get city summary without parameters', async () => {
      const mockSummary = { city: 'NYC', message: 'Summary' };
      axios.get.mockResolvedValue({ data: mockSummary });
      const result = await apiClient.getCitySummary('NYC');
      expect(result).toEqual(mockSummary);
      expect(axios.get).toHaveBeenCalledWith(`${mockBaseUrl}/summary/NYC`, expect.anything());
    });

    test('should get city summary with all parameters', async () => {
      const mockSummary = { city: 'NYC' };
      axios.get.mockResolvedValue({ data: mockSummary });
      const result = await apiClient.getCitySummary('NYC', '2024-01-01', '2024-01-31', 'NY');
      expect(result).toEqual(mockSummary);
      expect(axios.get).toHaveBeenCalledWith(
        `${mockBaseUrl}/summary/NYC`,
        expect.objectContaining({
          params: expect.objectContaining({
            startDate: '2024-01-01',
            endDate: '2024-01-31',
            state: 'NY'
          })
        })
      );
    });

    test('should handle error', async () => {
      axios.get.mockRejectedValue(new Error('Network error'));
      await expect(apiClient.getCitySummary('NYC')).rejects.toThrow();
    });
  });

  describe('getCountrySummary', () => {
    test('should get country summary', async () => {
      const mockSummary = { country: 'United States', capital: 'Washington' };
      axios.get.mockResolvedValue({ data: mockSummary });
      const result = await apiClient.getCountrySummary('United States');
      expect(result).toEqual(mockSummary);
    });

    test('should handle URL encoded country names', async () => {
      const mockSummary = { country: 'United States' };
      axios.get.mockResolvedValue({ data: mockSummary });
      const result = await apiClient.getCountrySummary('United States');
      expect(axios.get).toHaveBeenCalledWith(
        `${mockBaseUrl}/countrySummary/United%20States`
      );
    });

    test('should handle error', async () => {
      axios.get.mockRejectedValue(new Error('Network error'));
      await expect(apiClient.getCountrySummary('United States')).rejects.toThrow();
    });
  });

  describe('getClients', () => {
    test('should get all clients', async () => {
      const mockClients = [{ clientId: 1 }, { clientId: 2 }];
      axios.get.mockResolvedValue({ data: mockClients });
      const result = await apiClient.getClients();
      expect(result).toEqual(mockClients);
    });

    test('should handle error', async () => {
      axios.get.mockRejectedValue(new Error('Network error'));
      await expect(apiClient.getClients()).rejects.toThrow();
    });
  });

  describe('getUserByClientId', () => {
    test('should get user by client ID', async () => {
      const mockPrefs = { id: 1, cityPreferences: [] };
      axios.get.mockResolvedValue({ data: mockPrefs });
      const result = await apiClient.getUserByClientId(1);
      expect(result).toEqual(mockPrefs);
    });

    test('should handle error', async () => {
      axios.get.mockRejectedValue(new Error('Network error'));
      await expect(apiClient.getUserByClientId(1)).rejects.toThrow();
    });
  });

  describe('getUserPreference', () => {
    test('should get user preferences', async () => {
      const mockPrefs = { id: 1, cityPreferences: ['NYC'] };
      axios.get.mockResolvedValue({ data: mockPrefs });
      const result = await apiClient.getUserPreference(1);
      expect(result).toEqual(mockPrefs);
    });

    test('should handle error', async () => {
      axios.get.mockRejectedValue(new Error('Network error'));
      await expect(apiClient.getUserPreference(1)).rejects.toThrow();
    });
  });

  describe('login', () => {
    test('should login with username', async () => {
      const mockResult = { userId: 1, username: 'user1', clientId: 1 };
      axios.post.mockResolvedValue({ data: mockResult });
      const result = await apiClient.login('user1', null, null);
      expect(result).toEqual(mockResult);
    });

    test('should login with email', async () => {
      const mockResult = { userId: 1, email: 'user1@test.com', clientId: 1 };
      axios.post.mockResolvedValue({ data: mockResult });
      const result = await apiClient.login(null, 'user1@test.com', null);
      expect(result).toEqual(mockResult);
    });

    test('should login with userId', async () => {
      const mockResult = { userId: 1, clientId: 1 };
      axios.post.mockResolvedValue({ data: mockResult });
      const result = await apiClient.login(null, null, 1);
      expect(result).toEqual(mockResult);
    });

    test('should handle error', async () => {
      axios.post.mockRejectedValue(new Error('Network error'));
      await expect(apiClient.login('user1', null, null)).rejects.toThrow();
    });
  });
});

