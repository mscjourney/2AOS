/**
 * End-to-end tests for TARS Client connecting to TARS Service.
 * 
 * These tests exercise the full client functionality by making real HTTP requests
 * to a running TARS service instance.
 * 
 * Prerequisites:
 * - TARS Java backend must be running on http://localhost:8080
 * - Run: cd TeamProject && mvn spring-boot:run
 * 
*/

const TarsApiClient = require('../../src/tarsApiClient');
const fs = require('fs-extra');
const path = require('path');

describe('TARS Client End-to-End Tests', () => {
  const BASE_URL = process.env.TARS_BACKEND_URL || 'http://localhost:8080';
  let apiClient;
  let testClientConfigPath;
  

  beforeAll(() => {
    // Use a separate config file for E2E tests to avoid interfering with dev config
    testClientConfigPath = path.join(__dirname, '../../test-client-config.json');
    
    // Clean up any existing test config
    if (fs.existsSync(testClientConfigPath)) {
      fs.removeSync(testClientConfigPath);
    }
  });

  beforeEach(() => {
    apiClient = new TarsApiClient(BASE_URL);
    
    apiClient.clientConfigPath = testClientConfigPath;
    apiClient.clientId = null; // Force reload
  });

  afterEach(() => {
    // Clean up test config after each test
    if (fs.existsSync(testClientConfigPath)) {
      fs.removeSync(testClientConfigPath);
    }
  });

  afterAll(() => {
    if (fs.existsSync(testClientConfigPath)) {
      fs.removeSync(testClientConfigPath);
    }
  });

  describe('Client Retrieval', () => {
    test('should retrieve all clients', async () => {
      const clients = await apiClient.getClients();
      
      expect(Array.isArray(clients)).toBe(true);
      expect(clients.length).toBeGreaterThan(0);
      
      const firstClient = clients[0];
      expect(firstClient.clientId).toBeDefined();
      expect(firstClient.name).toBeDefined();
      expect(firstClient.email).toBeDefined();
    }, 10000);
  });

  describe('Weather Services', () => {
    test('should get weather recommendation', async () => {
      const recommendation = await apiClient.getWeatherRecommendation('New York', 5);
      
      expect(recommendation).toBeDefined();
      expect(recommendation.city).toBeDefined();
    }, 15000);

    test('should get weather alerts by city', async () => {
      const alerts = await apiClient.getWeatherAlertsByCity('New York');
      
      expect(alerts).toBeDefined();
    }, 15000);
  });

  describe('Crime Summary', () => {
    test('should get crime summary', async () => {
      const summary = await apiClient.getCrimeSummary('CA', 'ASS', '01', '2023');
      
      expect(summary).toBeDefined();
    }, 15000);
  });

  describe('City Summary', () => {
    test('should get city summary', async () => {
      const summary = await apiClient.getCitySummary('New York');
      
      expect(summary).toBeDefined();
    }, 15000);
  });

  describe('Country Services', () => {
    test('should get country advisory', async () => {
      const advisory = await apiClient.getCountryAdvisory('United States');
      
      expect(advisory).toBeDefined();
      expect(typeof advisory).toBe('string');
    }, 15000);

    test('should get country summary', async () => {
      const summary = await apiClient.getCountrySummary('United States');
      
      expect(summary).toBeDefined();
    }, 15000);
  });

});

