import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { API_BASE } from '../config';
import './Dashboard.css';

function Dashboard() {
  const navigate = useNavigate();
  const [clientId, setClientId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [activeTab, setActiveTab] = useState('overview');
  
  // Overview state
  const [welcomeMessage, setWelcomeMessage] = useState('');
  const [userPreferences, setUserPreferences] = useState(null);
  const [loggedInUser, setLoggedInUser] = useState(null);
  
  // Crime summary state
  const [crimeState, setCrimeState] = useState('');
  const [crimeOffense, setCrimeOffense] = useState('');
  const [crimeMonth, setCrimeMonth] = useState('');
  const [crimeYear, setCrimeYear] = useState('');
  const [crimeSummary, setCrimeSummary] = useState(null);
  
  // Weather state
  const [weatherCity, setWeatherCity] = useState('New York');
  const [weatherDays, setWeatherDays] = useState(7);
  const [weatherRecommendation, setWeatherRecommendation] = useState(null);
  const [weatherAlerts, setWeatherAlerts] = useState(null);
  const [alertCity, setAlertCity] = useState('New York');
  const [alertLat, setAlertLat] = useState('');
  const [alertLon, setAlertLon] = useState('');
  
  // Country summary state
  const [countrySummary, setCountrySummary] = useState(null);
  const [summaryCountry, setSummaryCountry] = useState('United States');
  
  // Personal alerts state
  const [personalAlerts, setPersonalAlerts] = useState([]);
  const [loadingAlerts, setLoadingAlerts] = useState(false);
  const [userRecommendations, setUserRecommendations] = useState([]);
  const [loadingRecommendations, setLoadingRecommendations] = useState(false);
  
  useEffect(() => {
    initializeClient();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Auto-fetch personal alerts when user is logged in and tab is active
  useEffect(() => {
    if (activeTab === 'personal-alerts' && loggedInUser && loggedInUser.userId) {
      handleGetPersonalAlerts();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeTab, loggedInUser]);

  const initializeClient = async () => {
    try {
      setLoading(true);
      
      // Check if user is logged in
      const loggedInUserData = localStorage.getItem('loggedInUser');
      if (loggedInUserData) {
        const user = JSON.parse(loggedInUserData);
        setLoggedInUser(user);
        setClientId(user.clientId);
        
        // Load user preferences
        try {
          const prefsResponse = await axios.get(`${API_BASE}/preferences/user/${user.userId}`);
          console.log('Loaded user preferences:', prefsResponse.data);
          setUserPreferences(prefsResponse.data);
        } catch (prefsErr) {
          console.error('Error loading preferences:', prefsErr);
          // Set empty preferences if not found
          setUserPreferences({
            cityPreferences: [],
            weatherPreferences: [],
            temperaturePreferences: []
          });
        }
      } else {
        // Get or create client ID for non-logged in users
        const response = await axios.get(`${API_BASE}/client-id`);
        setClientId(response.data.clientId);
      }
      
      // Get welcome message
      const welcomeRes = await axios.get(`${API_BASE}/index`);
      setWelcomeMessage(welcomeRes.data.message);
    } catch (err) {
      setError(err.response?.data?.error || err.message);
      console.error('Initialization error:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleGetCrimeSummary = async () => {
    try {
      setError(null);
      setSuccess(null);
      if (!crimeState || !crimeOffense || !crimeMonth || !crimeYear) {
        setError('All fields are required: State, Offense, Month, and Year');
        return;
      }
      
      // Validate month and year
      const monthNum = parseInt(crimeMonth);
      const yearNum = parseInt(crimeYear);
      if (isNaN(monthNum) || monthNum < 1 || monthNum > 12) {
        setError('Month must be a number between 1 and 12');
        return;
      }
      if (isNaN(yearNum) || yearNum < 2000 || yearNum > 2025) {
        setError('Year must be a number between 2000 and 2025');
        return;
      }
      
      // Format month as two digits (e.g., 1 -> "01", 11 -> "11")
      const formattedMonth = String(monthNum).padStart(2, '0');
      
      const response = await axios.get(`${API_BASE}/crime/summary`, {
        params: {
          state: crimeState.trim().toUpperCase(),
          offense: crimeOffense.trim().toUpperCase(),
          month: formattedMonth,
          year: String(yearNum)
        }
      });
      setCrimeSummary(response.data);
      setSuccess('Crime summary retrieved successfully');
    } catch (err) {
      console.error('Crime summary error:', err);
      const errorMsg = err.response?.data?.error || err.response?.data?.message || err.message || 'Failed to get crime summary';
      setError(errorMsg);
      setCrimeSummary(null);
    }
  };

  const handleGetWeatherRecommendation = async () => {
    try {
      setError(null);
      const response = await axios.get(`${API_BASE}/recommendation/weather`, {
        params: { city: weatherCity, days: weatherDays }
      });
      setWeatherRecommendation(response.data);
    } catch (err) {
      setError(err.response?.data?.error || err.message);
    }
  };

  const handleGetWeatherAlerts = async () => {
    try {
      setError(null);
      let params = {};
      if (alertCity) {
        params.city = alertCity;
      } else if (alertLat && alertLon) {
        params.lat = alertLat;
        params.lon = alertLon;
      } else {
        setError('Please provide either a city or coordinates');
        return;
      }
      const response = await axios.get(`${API_BASE}/alert/weather`, { params });
      setWeatherAlerts(response.data);
    } catch (err) {
      setError(err.response?.data?.error || err.message);
    }
  };

  const handleGetCountrySummary = async () => {
    try {
      setError(null);
      setCountrySummary(null); // Clear previous results
      const response = await axios.get(`${API_BASE}/countrySummary/${encodeURIComponent(summaryCountry)}`);
      console.log('Country summary response:', response.data);
      console.log('Response type:', typeof response.data);
      console.log('Response keys:', response.data ? Object.keys(response.data) : 'null');
      console.log('Country value:', response.data?.country);
      console.log('Capital value:', response.data?.capital);
      console.log('Message value:', response.data?.message);
      
      // Check if response is HTML (route not matching)
      if (typeof response.data === 'string' && response.data.includes('<!doctype html>')) {
        console.error('Received HTML instead of JSON - route not matching on server');
        setError('Server routing error: Received HTML instead of JSON. Please check server logs.');
        setCountrySummary(null);
        return;
      }
      
      if (response.data) {
        // Handle both direct response and wrapped response
        const data = response.data.data || response.data;
        // Verify it's actually JSON data, not HTML
        if (typeof data === 'string' && data.includes('<!doctype html>')) {
          console.error('Response data is HTML string');
          setError('Server returned HTML instead of JSON data');
          setCountrySummary(null);
          return;
        }
        setCountrySummary(data);
      } else {
        setError('No data received from server');
      }
    } catch (err) {
      console.error('Error getting country summary:', err);
      console.error('Error response:', err.response);
      setCountrySummary(null); // Clear on error
      const errorMsg = err.response?.data?.error 
        ? (typeof err.response.data.error === 'object' 
            ? JSON.stringify(err.response.data.error) 
            : err.response.data.error)
        : err.response?.data 
        ? (typeof err.response.data === 'object' 
            ? JSON.stringify(err.response.data) 
            : err.response.data)
        : err.message;
      setError(errorMsg || 'Failed to fetch country summary');
    }
  };

  const handleGetPersonalAlerts = async () => {
    if (!loggedInUser || !loggedInUser.userId) {
      setError('Please log in to view your personal alerts');
      return;
    }
    
    try {
      setError(null);
      setLoadingAlerts(true);
      const response = await axios.get(`${API_BASE}/alert/weather/user/${loggedInUser.userId}`);
      // Response is an array of WeatherAlert objects
      setPersonalAlerts(Array.isArray(response.data) ? response.data : []);
    } catch (err) {
      console.error('Error getting personal alerts:', err);
      const errorMsg = err.response?.data?.error || err.message;
      setError(errorMsg);
      setPersonalAlerts([]);
    } finally {
      setLoadingAlerts(false);
    }
  };

  const handleGetUserRecommendations = async () => {
    if (!loggedInUser || !loggedInUser.userId) {
      setError('Please log in to view your personal recommendations');
      return;
    }

    if (!userPreferences || !userPreferences.cityPreferences || userPreferences.cityPreferences.length === 0) {
      setError('Please add city preferences to your profile to get recommendations');
      return;
    }
    
    try {
      setError(null);
      setLoadingRecommendations(true);
      const recommendations = [];
      
      // Get recommendations for each city in user's preferences
      for (const city of userPreferences.cityPreferences) {
        try {
          const response = await axios.get(`${API_BASE}/getUserRec/${loggedInUser.userId}`, {
            params: {
              city: city,
              days: 7 // Default to 7 days
            }
          });
          recommendations.push({
            city: city,
            ...response.data
          });
        } catch (err) {
          console.error(`Error getting recommendation for ${city}:`, err);
          // Continue with other cities even if one fails
        }
      }
      
      setUserRecommendations(recommendations);
    } catch (err) {
      console.error('Error getting user recommendations:', err);
      const errorMsg = err.response?.data?.error || err.message;
      setError(errorMsg);
      setUserRecommendations([]);
    } finally {
      setLoadingRecommendations(false);
    }
  };

  if (loading) {
    return (
      <div className="dashboard">
        <div className="loading">Loading dashboard...</div>
      </div>
    );
  }

  return (
    <div className="dashboard">
      <nav className="navbar">
        <div className="nav-container">
          <div className="nav-brand-section">
            <Link to="/dashboard" className="nav-logo">
              TARS
            </Link>
            <span className="client-id">Client ID: {clientId}</span>
          </div>
          <ul className="nav-menu">
            <li className="nav-item">
              <Link to="/dashboard" className="nav-link">
                Dashboard
              </Link>
            </li>
            {localStorage.getItem('loggedInUser') && (
              <>
                <li className="nav-item">
                  <Link to="/profile" className="nav-link">
                    My Profile
                  </Link>
                </li>
                {(() => {
                  const loggedInUser = JSON.parse(localStorage.getItem('loggedInUser'));
                  const role = loggedInUser?.role?.toLowerCase();
                  if (role === 'admin' || role === 'administrator') {
                    return (
                      <li className="nav-item">
                        <Link to="/admin" className="nav-link">
                          Admin
                        </Link>
                      </li>
                    );
                  }
                  return null;
                })()}
                <li className="nav-item">
                  <button 
                    onClick={() => {
                      localStorage.removeItem('loggedInUser');
                      navigate('/login');
                    }} 
                    className="nav-link-btn"
                  >
                    Logout
                  </button>
                </li>
              </>
            )}
            {!localStorage.getItem('loggedInUser') && (
              <li className="nav-item">
                <Link to="/login" className="nav-link">
                  Login
                </Link>
              </li>
            )}
          </ul>
        </div>
      </nav>
      
      <div className="dashboard-header">
        <h1>Welcome to TARS</h1>
      </div>

      {(error || success) && (
        <div className="container" style={{ width: '100%', margin: '0 auto', padding: '0 2.5rem' }}>
          {error && <div className="alert alert-error"><strong>Error:</strong> {error}</div>}
          {success && <div className="alert alert-success"><strong>Success!</strong> {success}</div>}
        </div>
      )}

      <div className="tabs">
        <button 
          className={activeTab === 'overview' ? 'tab active' : 'tab'}
          onClick={() => setActiveTab('overview')}
        >
          Dashboard
        </button>
        <button 
          className={activeTab === 'crime' ? 'tab active' : 'tab'}
          onClick={() => setActiveTab('crime')}
        >
          Crime Summary
        </button>
        <button 
          className={activeTab === 'weather' ? 'tab active' : 'tab'}
          onClick={() => setActiveTab('weather')}
        >
          Weather
        </button>
        <button 
          className={activeTab === 'country' ? 'tab active' : 'tab'}
          onClick={() => setActiveTab('country')}
        >
          Country Summary
        </button>
        {loggedInUser && (
          <button 
            className={activeTab === 'personal-alerts' ? 'tab active' : 'tab'}
            onClick={() => setActiveTab('personal-alerts')}
          >
            Personal Alerts
          </button>
        )}
      </div>

      <div className="tab-content">
        {activeTab === 'overview' && (
          <div className="overview">
            <div className="card">
              <h2>Welcome</h2>
              <p className="welcome-message">{welcomeMessage}</p>
            </div>
            <div className="card">
              <h2>Client Information</h2>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <p><strong>Client ID:</strong> <span style={{ color: '#667eea', fontWeight: '600' }}>{clientId}</span></p>
                {loggedInUser && (
                  <>
                    <p><strong>User ID:</strong> <span style={{ color: '#667eea', fontWeight: '600' }}>{loggedInUser.userId}</span></p>
                    <p><strong>Username:</strong> <span style={{ color: '#667eea', fontWeight: '600' }}>{loggedInUser.username}</span></p>
                    <p><strong>Email:</strong> <span style={{ color: '#667eea', fontWeight: '600' }}>{loggedInUser.email}</span></p>
                  </>
                )}
                <p><strong>Status:</strong> <span style={{ color: '#38a169', fontWeight: '600' }}>● Connected</span></p>
              </div>
            </div>
            {loggedInUser && userPreferences && (
              <div className="card">
                <h2>Your Preferences</h2>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  <div>
                    <p style={{ fontWeight: '600', color: '#2d3748', marginBottom: '0.5rem' }}>City Preferences:</p>
                    {userPreferences.cityPreferences && userPreferences.cityPreferences.length > 0 ? (
                      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                        {userPreferences.cityPreferences.map((city, idx) => (
                          <span key={idx} style={{
                            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                            color: 'white',
                            padding: '0.25rem 0.75rem',
                            borderRadius: '12px',
                            fontSize: '0.9rem'
                          }}>
                            {city}
                          </span>
                        ))}
                      </div>
                    ) : (
                      <p style={{ color: '#718096', fontSize: '0.9rem' }}>No city preferences set</p>
                    )}
                  </div>
                  <div>
                    <p style={{ fontWeight: '600', color: '#2d3748', marginBottom: '0.5rem' }}>Weather Preferences:</p>
                    {userPreferences.weatherPreferences && userPreferences.weatherPreferences.length > 0 ? (
                      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                        {userPreferences.weatherPreferences.map((weather, idx) => (
                          <span key={idx} style={{
                            background: '#38a169',
                            color: 'white',
                            padding: '0.25rem 0.75rem',
                            borderRadius: '12px',
                            fontSize: '0.9rem'
                          }}>
                            {weather}
                          </span>
                        ))}
                      </div>
                    ) : (
                      <p style={{ color: '#718096', fontSize: '0.9rem' }}>No weather preferences set</p>
                    )}
                  </div>
                  <div>
                    <p style={{ fontWeight: '600', color: '#2d3748', marginBottom: '0.5rem' }}>Temperature Preferences:</p>
                    {userPreferences.temperaturePreferences && userPreferences.temperaturePreferences.length > 0 ? (
                      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                        {userPreferences.temperaturePreferences.map((temp, idx) => (
                          <span key={idx} style={{
                            background: '#ed8936',
                            color: 'white',
                            padding: '0.25rem 0.75rem',
                            borderRadius: '12px',
                            fontSize: '0.9rem'
                          }}>
                            {temp}
                          </span>
                        ))}
                      </div>
                    ) : (
                      <p style={{ color: '#718096', fontSize: '0.9rem' }}>No temperature preferences set</p>
                    )}
                  </div>
                  <Link 
                    to="/profile" 
                    style={{
                      display: 'inline-block',
                      background: '#667eea',
                      color: 'white',
                      padding: '0.5rem 1rem',
                      borderRadius: '6px',
                      textDecoration: 'none',
                      textAlign: 'center',
                      fontSize: '0.9rem',
                      fontWeight: '500',
                      marginTop: '0.5rem'
                    }}
                  >
                    Edit Preferences
                  </Link>
                </div>
              </div>
            )}
            <div className="card">
              <h2>Quick Actions</h2>
              <p style={{ marginBottom: '1rem' }}>Use the tabs above to:</p>
              <ul style={{ listStyle: 'none', padding: 0 }}>
                <li style={{ padding: '0.5rem 0', color: '#4a5568' }}>✓ Get crime statistics and summaries</li>
                <li style={{ padding: '0.5rem 0', color: '#4a5568' }}>✓ Get weather recommendations and alerts</li>
                <li style={{ padding: '0.5rem 0', color: '#4a5568' }}>✓ View comprehensive country summaries</li>
              </ul>
              {localStorage.getItem('loggedInUser') && (
                <div style={{ marginTop: '1.5rem', paddingTop: '1.5rem', borderTop: '2px solid #e2e8f0' }}>
                  <Link 
                    to="/profile" 
                    style={{
                      display: 'inline-block',
                      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                      color: 'white',
                      padding: '0.75rem 1.5rem',
                      borderRadius: '8px',
                      textDecoration: 'none',
                      fontWeight: '600',
                      transition: 'all 0.3s',
                      boxShadow: '0 2px 4px rgba(102, 126, 234, 0.3)'
                    }}
                    onMouseEnter={(e) => {
                      e.target.style.transform = 'translateY(-2px)';
                      e.target.style.boxShadow = '0 4px 8px rgba(102, 126, 234, 0.4)';
                    }}
                    onMouseLeave={(e) => {
                      e.target.style.transform = 'translateY(0)';
                      e.target.style.boxShadow = '0 2px 4px rgba(102, 126, 234, 0.3)';
                    }}
                  >
                    Go to My Profile →
                  </Link>
                </div>
              )}
            </div>
          </div>
        )}

        {activeTab === 'crime' && (
          <div className="crime-summary">
            <div className="card">
              <h2>Crime Summary</h2>
              <div className="form-group">
                <label>State:</label>
                <input
                  type="text"
                  value={crimeState}
                  onChange={(e) => setCrimeState(e.target.value)}
                  className="form-control"
                  placeholder="e.g., New York or NY"
                  required
                />
              </div>
              <div className="form-group">
                <label>Offense Code:</label>
                <input
                  type="text"
                  value={crimeOffense}
                  onChange={(e) => setCrimeOffense(e.target.value.toUpperCase())}
                  className="form-control"
                  placeholder="e.g., ASS (Assault), BUR (Burglary), HOM (Homicide), ROB (Robbery)"
                  required
                />
                <small style={{ color: '#718096', fontSize: '0.85rem', marginTop: '0.25rem', display: 'block' }}>
                  Common codes: ASS (Assault), BUR (Burglary), HOM (Homicide), ROB (Robbery), LAR (Larceny), MVT (Motor Vehicle Theft)
                </small>
              </div>
              <div className="form-group">
                <label>Month:</label>
                <input
                  type="number"
                  min="1"
                  max="12"
                  value={crimeMonth}
                  onChange={(e) => {
                    const val = e.target.value;
                    // Ensure it's a valid month
                    if (val === '' || (parseInt(val) >= 1 && parseInt(val) <= 12)) {
                      setCrimeMonth(val);
                    }
                  }}
                  className="form-control"
                  placeholder="1-12"
                  required
                />
                <small style={{ color: '#718096', fontSize: '0.85rem', marginTop: '0.25rem', display: 'block' }}>
                  Enter month as number (1-12). Will be formatted as two digits (e.g., 1 becomes 01).
                </small>
              </div>
              <div className="form-group">
                <label>Year:</label>
                <input
                  type="number"
                  min="2000"
                  max="2024"
                  value={crimeYear}
                  onChange={(e) => setCrimeYear(e.target.value)}
                  className="form-control"
                  placeholder="e.g., 2024"
                  required
                />
              </div>
              <button onClick={handleGetCrimeSummary} className="btn-primary">Get Crime Summary</button>
              {crimeSummary && (
                <div className="result" style={{ marginTop: '2rem' }}>
                  <h3>Crime Summary</h3>
                  {crimeSummary.state && (
                    <p><strong>State:</strong> {crimeSummary.state}</p>
                  )}
                  {crimeSummary.offense && (
                    <p><strong>Offense:</strong> {crimeSummary.offense}</p>
                  )}
                  {crimeSummary.month && crimeSummary.year && (
                    <p><strong>Period:</strong> {crimeSummary.month}/{crimeSummary.year}</p>
                  )}
                  {crimeSummary.message && (
                    <div className="summary-message" style={{ 
                      marginTop: '1rem', 
                      padding: '1rem', 
                      background: '#f7fafc', 
                      borderRadius: '8px',
                      borderLeft: '4px solid #667eea'
                    }}>
                      <p>{crimeSummary.message}</p>
                    </div>
                  )}
                  {crimeSummary.statistics && Object.keys(crimeSummary.statistics).length > 0 && (
                    <div style={{ marginTop: '1.5rem' }}>
                      <h4>Statistics:</h4>
                      <div className="current-conditions">
                        {Object.entries(crimeSummary.statistics).map(([key, value]) => (
                          <p key={key}><strong>{key}:</strong> {String(value)}</p>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>
        )}

        {activeTab === 'weather' && (
          <div className="weather">
            <div className="weather-section">
              <h2>Weather Recommendations</h2>
              <div className="form-group">
                <label>City:</label>
                <input
                  type="text"
                  value={weatherCity}
                  onChange={(e) => setWeatherCity(e.target.value)}
                  className="form-control"
                  placeholder="e.g., New York"
                />
              </div>
              <div className="form-group">
                <label>Number of Days (1-14):</label>
                <input
                  type="number"
                  min="1"
                  max="14"
                  value={weatherDays}
                  onChange={(e) => setWeatherDays(parseInt(e.target.value))}
                  className="form-control"
                />
              </div>
              <button onClick={handleGetWeatherRecommendation} className="btn-primary">Get Recommendations</button>
              {weatherRecommendation && (
                <div className="result">
                  <h3>Weather Recommendation for {weatherRecommendation.city || weatherCity}</h3>
                  <p>{weatherRecommendation.message}</p>
                  {weatherRecommendation.recommendedDays && weatherRecommendation.recommendedDays.length > 0 && (
                    <div>
                      <h4>Recommended Days:</h4>
                      <ul>
                        {weatherRecommendation.recommendedDays.map((day, idx) => (
                          <li key={idx}>{day}</li>
                        ))}
                      </ul>
                    </div>
                  )}
                </div>
              )}
            </div>

            <div className="weather-section">
              <h2>Weather Alerts</h2>
              <div className="form-group">
                <label>City:</label>
                <input
                  type="text"
                  value={alertCity}
                  onChange={(e) => setAlertCity(e.target.value)}
                  className="form-control"
                  placeholder="e.g., New York"
                />
              </div>
              <div className="form-group">
                <label>Or use coordinates:</label>
                <div className="form-row">
                  <input
                    type="number"
                    step="any"
                    value={alertLat}
                    onChange={(e) => setAlertLat(e.target.value)}
                    className="form-control"
                    placeholder="Latitude"
                  />
                  <input
                    type="number"
                    step="any"
                    value={alertLon}
                    onChange={(e) => setAlertLon(e.target.value)}
                    className="form-control"
                    placeholder="Longitude"
                  />
                </div>
              </div>
              <button onClick={handleGetWeatherAlerts} className="btn-primary">Get Alerts</button>
              {weatherAlerts && (
                <div className="result">
                  <h3>Weather Alert for {weatherAlerts.location || alertCity}</h3>
                  {weatherAlerts.timestamp && (
                    <p><strong>Timestamp:</strong> {weatherAlerts.timestamp}</p>
                  )}
                  {weatherAlerts.alerts && weatherAlerts.alerts.length > 0 && (
                    <div>
                      <h4>Active Alerts:</h4>
                      {weatherAlerts.alerts.map((alertItem, idx) => (
                        <div key={idx} className="alert-item">
                          {Object.entries(alertItem).map(([key, value]) => (
                            <p key={key}><strong>{key}:</strong> {String(value)}</p>
                          ))}
                        </div>
                      ))}
                    </div>
                  )}
                  {weatherAlerts.recommendations && weatherAlerts.recommendations.length > 0 && (
                    <div>
                      <h4>Recommendations:</h4>
                      <ul>
                        {weatherAlerts.recommendations.map((rec, idx) => (
                          <li key={idx}>{rec}</li>
                        ))}
                      </ul>
                    </div>
                  )}
                  {weatherAlerts.currentConditions && Object.keys(weatherAlerts.currentConditions).length > 0 && (
                    <div>
                      <h4>Current Conditions:</h4>
                      <div className="current-conditions">
                        {Object.entries(weatherAlerts.currentConditions).map(([key, value]) => (
                          <p key={key}><strong>{key}:</strong> {String(value)}</p>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>
        )}

        {activeTab === 'country' && (
          <div className="country-summary">
            <div className="card">
              <h2>Country Summary</h2>
              {error && <div className="alert alert-error"><strong>Error:</strong> {error}</div>}
              <div className="form-group">
                <label>Country:</label>
                <input
                  type="text"
                  value={summaryCountry}
                  onChange={(e) => setSummaryCountry(e.target.value)}
                  className="form-control"
                  placeholder="e.g., United States"
                />
              </div>
              <button onClick={handleGetCountrySummary} className="btn-primary">Get Country Summary</button>
              {countrySummary && (
                <div className="result">
                  <h3>Summary for {countrySummary.country || summaryCountry || 'Unknown Country'}</h3>
                  {countrySummary.capital && (
                    <p><strong>Capital:</strong> {countrySummary.capital}</p>
                  )}
                  {countrySummary.message && (
                    <p className="summary-message">{countrySummary.message}</p>
                  )}
                  {(!countrySummary.country && !countrySummary.capital && !countrySummary.message && !countrySummary.travelAdvisory) && (
                    <div className="alert" style={{background: '#fff3cd', borderLeft: '4px solid #ffc107', padding: '1rem', marginTop: '1rem'}}>
                      <strong>Debug Info:</strong> Response received but structure may be unexpected. Check browser console for details.
                      <details style={{marginTop: '0.5rem'}}>
                        <summary style={{cursor: 'pointer', color: '#667eea'}}>Show raw response</summary>
                        <pre style={{marginTop: '0.5rem', fontSize: '0.85rem', overflow: 'auto', background: '#f7fafc', padding: '0.5rem', borderRadius: '4px'}}>
                          {JSON.stringify(countrySummary, null, 2)}
                        </pre>
                      </details>
                    </div>
                  )}
                  
                  {countrySummary.travelAdvisory && (
                    <div className="summary-section">
                      <h4>Travel Advisory</h4>
                      {countrySummary.travelAdvisory.country && (
                        <p><strong>Country:</strong> {countrySummary.travelAdvisory.country}</p>
                      )}
                      {countrySummary.travelAdvisory.level && (
                        <p><strong>Advisory Level:</strong> <span style={{ 
                          color: countrySummary.travelAdvisory.level.includes('Level 4') ? '#e53e3e' :
                                 countrySummary.travelAdvisory.level.includes('Level 3') ? '#dd6b20' :
                                 countrySummary.travelAdvisory.level.includes('Level 2') ? '#d69e2e' : '#38a169',
                          fontWeight: '600'
                        }}>{countrySummary.travelAdvisory.level}</span></p>
                      )}
                      {countrySummary.travelAdvisory.risk_indicators && countrySummary.travelAdvisory.risk_indicators.length > 0 && (
                        <div>
                          <strong>Risk Indicators:</strong>
                          <ul>
                            {countrySummary.travelAdvisory.risk_indicators.map((risk, idx) => (
                              <li key={idx}>{risk}</li>
                            ))}
                          </ul>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>
        )}

        {activeTab === 'personal-alerts' && (
          <div className="personal-alerts">
            <div className="card">
              <h2>Personal Weather Alerts & Recommendations</h2>
              <p style={{ color: '#718096', marginBottom: '1.5rem' }}>
                Weather alerts and personalized recommendations for all cities in your preferences
              </p>
              
              {!loggedInUser ? (
                <div className="alert" style={{ background: '#fff3cd', borderLeft: '4px solid #ffc107', padding: '1rem' }}>
                  <strong>Please log in</strong> to view your personal weather alerts and recommendations based on your city preferences.
                </div>
              ) : (
                <>
                  <button 
                    onClick={handleGetPersonalAlerts} 
                    className="btn-primary"
                    disabled={loadingAlerts}
                    style={{ marginBottom: '1.5rem' }}
                  >
                    {loadingAlerts ? 'Loading...' : 'Refresh Alerts'}
                  </button>
                  
                  {loadingAlerts && (
                    <div style={{ marginTop: '1rem', textAlign: 'center', color: '#718096' }}>
                      Loading your alerts...
                    </div>
                  )}
                  
                  {!loadingAlerts && personalAlerts.length === 0 && (
                    <div className="alert" style={{ marginTop: '1.5rem', background: '#e6fffa', borderLeft: '4px solid #38a169', padding: '1rem' }}>
                      <strong>No alerts found.</strong> Make sure you have city preferences set in your profile.
                    </div>
                  )}
                  
                  {!loadingAlerts && personalAlerts.length > 0 && (
                    <div style={{ marginTop: '2rem' }}>
                      <h3 style={{ marginBottom: '1.5rem', color: '#2d3748' }}>
                        {personalAlerts.length} Alert{personalAlerts.length !== 1 ? 's' : ''} Found
                      </h3>
                      {personalAlerts.map((alert, idx) => (
                        <div key={idx} className="result" style={{ marginBottom: '2rem', border: '1px solid #e2e8f0', borderRadius: '8px', padding: '1.5rem' }}>
                          <h3 style={{ color: '#667eea', marginBottom: '1rem' }}>
                            {alert.location || `Alert ${idx + 1}`}
                          </h3>
                          
                          {alert.timestamp && (
                            <p style={{ marginBottom: '0.5rem', color: '#718096' }}>
                              <strong>Timestamp:</strong> {alert.timestamp}
                            </p>
                          )}
                          
                          {alert.alerts && alert.alerts.length > 0 && (
                            <div style={{ marginTop: '1rem' }}>
                              <h4 style={{ color: '#e53e3e', marginBottom: '0.75rem' }}>⚠️ Active Alerts:</h4>
                              {alert.alerts.map((alertItem, alertIdx) => (
                                <div key={alertIdx} className="alert-item" style={{ 
                                  background: '#fed7d7', 
                                  padding: '1rem', 
                                  borderRadius: '6px', 
                                  marginBottom: '0.75rem',
                                  borderLeft: '4px solid #e53e3e'
                                }}>
                                  {Object.entries(alertItem).map(([key, value]) => (
                                    <p key={key} style={{ margin: '0.25rem 0' }}>
                                      <strong>{key}:</strong> {String(value)}
                                    </p>
                                  ))}
                                </div>
                              ))}
                            </div>
                          )}
                          
                          {alert.recommendations && alert.recommendations.length > 0 && (
                            <div style={{ marginTop: '1rem' }}>
                              <h4 style={{ color: '#38a169', marginBottom: '0.75rem' }}>💡 Recommendations:</h4>
                              <ul style={{ paddingLeft: '1.5rem' }}>
                                {alert.recommendations.map((rec, recIdx) => (
                                  <li key={recIdx} style={{ marginBottom: '0.5rem', color: '#4a5568' }}>
                                    {rec}
                                  </li>
                                ))}
                              </ul>
                            </div>
                          )}
                          
                          {alert.currentConditions && Object.keys(alert.currentConditions).length > 0 && (
                            <div style={{ marginTop: '1rem' }}>
                              <h4 style={{ marginBottom: '0.75rem', color: '#2d3748' }}>Current Conditions:</h4>
                              <div className="current-conditions" style={{ 
                                display: 'grid', 
                                gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', 
                                gap: '0.75rem',
                                background: '#f7fafc',
                                padding: '1rem',
                                borderRadius: '6px'
                              }}>
                                {Object.entries(alert.currentConditions).map(([key, value]) => (
                                  <p key={key} style={{ margin: 0 }}>
                                    <strong>{key}:</strong> {String(value)}
                                  </p>
                                ))}
                              </div>
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  )}

                  {/* Get Recommendations Button - Moved to bottom after alerts */}
                  <div style={{ marginTop: '3rem', paddingTop: '2rem', borderTop: '2px solid #e2e8f0' }}>
                    <button 
                      onClick={handleGetUserRecommendations} 
                      className="btn-primary"
                      disabled={loadingRecommendations}
                      style={{ background: '#38a169' }}
                    >
                      {loadingRecommendations ? 'Loading...' : 'Get Personalized Recommendations'}
                    </button>
                  </div>

                  {/* User Recommendations Section */}
                  <div style={{ marginTop: '2rem' }}>
                    <h3 style={{ marginBottom: '1rem', color: '#2d3748' }}>🌤️ Personalized Weather Recommendations</h3>
                    <p style={{ color: '#718096', marginBottom: '1.5rem' }}>
                      Weather recommendations tailored to your preferences for each city you follow
                    </p>

                    {loadingRecommendations && (
                      <div style={{ textAlign: 'center', color: '#718096', padding: '2rem' }}>
                        Loading recommendations...
                      </div>
                    )}

                    {!loadingRecommendations && userRecommendations.length === 0 && (
                      <div className="alert" style={{ background: '#e6fffa', borderLeft: '4px solid #38a169', padding: '1rem' }}>
                        <strong>No recommendations yet.</strong> Click "Get Recommendations" to see personalized weather recommendations for your cities.
                      </div>
                    )}

                    {!loadingRecommendations && userRecommendations.length > 0 && (
                      <div>
                        <h4 style={{ marginBottom: '1.5rem', color: '#2d3748' }}>
                          {userRecommendations.length} Recommendation{userRecommendations.length !== 1 ? 's' : ''} Found
                        </h4>
                        {userRecommendations.map((rec, idx) => (
                          <div key={idx} className="result" style={{ 
                            marginBottom: '2rem', 
                            border: '1px solid #c6f6d5', 
                            borderRadius: '8px', 
                            padding: '1.5rem',
                            background: '#f0fff4'
                          }}>
                            <h3 style={{ color: '#38a169', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                              🌍 {rec.city || `City ${idx + 1}`}
                            </h3>
                            
                            {rec.message && (
                              <p style={{ marginBottom: '1rem', color: '#2d3748', fontSize: '1.1rem', fontWeight: '500' }}>
                                {rec.message}
                              </p>
                            )}

                            {rec.recommendedDays && rec.recommendedDays.length > 0 && (
                              <div style={{ marginTop: '1rem' }}>
                                <h4 style={{ color: '#38a169', marginBottom: '0.75rem' }}>📅 Recommended Days:</h4>
                                <div style={{ 
                                  display: 'flex', 
                                  flexWrap: 'wrap', 
                                  gap: '0.5rem',
                                  marginTop: '0.5rem'
                                }}>
                                  {rec.recommendedDays.map((day, dayIdx) => (
                                    <span 
                                      key={dayIdx}
                                      style={{
                                        background: '#38a169',
                                        color: 'white',
                                        padding: '0.5rem 1rem',
                                        borderRadius: '6px',
                                        fontWeight: '500',
                                        fontSize: '0.9rem'
                                      }}
                                    >
                                      {day}
                                    </span>
                                  ))}
                                </div>
                                <p style={{ marginTop: '1rem', color: '#718096', fontSize: '0.9rem' }}>
                                  <strong>{rec.recommendedDays.length}</strong> day{rec.recommendedDays.length !== 1 ? 's' : ''} match your weather and temperature preferences
                                </p>
                              </div>
                            )}

                            {(!rec.recommendedDays || rec.recommendedDays.length === 0) && (
                              <div className="alert" style={{ 
                                background: '#fff3cd', 
                                borderLeft: '4px solid #ffc107', 
                                padding: '1rem',
                                marginTop: '1rem'
                              }}>
                                <strong>No matching days found</strong> in the next 7 days for your preferences in {rec.city}.
                              </div>
                            )}
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </>
              )}
            </div>
          </div>
        )}
      </div>

      <footer className="footer">
        <p>&copy; 2024 TARS - Travel Alert and Recommendation Service</p>
      </footer>
    </div>
  );
}

export default Dashboard;
