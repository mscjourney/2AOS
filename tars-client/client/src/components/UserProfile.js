import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './UserProfile.css';

const API_BASE = 'http://localhost:3001/api';

function UserProfile() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [userInfo, setUserInfo] = useState(null); // Store username, email, role from localStorage
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  
  const [preferences, setPreferences] = useState({
    cityPreferences: [],
    weatherPreferences: [],
    temperaturePreferences: []
  });

  const [newCity, setNewCity] = useState('');
  const [newWeather, setNewWeather] = useState('');
  const [newTemperature, setNewTemperature] = useState('');
  
  // Edit state: { type: 'city'|'weather'|'temperature', index: number, value: string }
  const [editing, setEditing] = useState(null);

  useEffect(() => {
    const loggedInUser = localStorage.getItem('loggedInUser');
    if (!loggedInUser) {
      navigate('/login');
      return;
    }

    loadUserProfile();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [navigate]);

  // Debug: Log preferences whenever they change
  useEffect(() => {
    console.log('Preferences state updated:', preferences);
  }, [preferences]);

  const loadUserProfile = async () => {
    try {
      setLoading(true);
      setError(null);
      const loggedInUser = JSON.parse(localStorage.getItem('loggedInUser'));
      
      if (!loggedInUser) {
        navigate('/login');
        return;
      }
      
      // Store user info (username, email, role) from localStorage
      setUserInfo({
        username: loggedInUser.username,
        email: loggedInUser.email,
        role: loggedInUser.role
      });
      
      // Initialize user with data from localStorage first
      const initialUser = {
        id: loggedInUser.userId,
        clientId: loggedInUser.clientId,
        cityPreferences: [],
        weatherPreferences: [],
        temperaturePreferences: []
      };
      setUser(initialUser);
      
      // Get user preferences from userPreferences.json by clientId
      try {
        console.log(`Loading preferences for clientId: ${loggedInUser.clientId}`);
        const response = await axios.get(`${API_BASE}/user/client/${loggedInUser.clientId}`);
        console.log('Full API Response:', JSON.stringify(response.data, null, 2));
        
        // Extract preferences from response
        const cityPrefs = Array.isArray(response.data.cityPreferences) ? response.data.cityPreferences : [];
        const weatherPrefs = Array.isArray(response.data.weatherPreferences) ? response.data.weatherPreferences : [];
        const tempPrefs = Array.isArray(response.data.temperaturePreferences) ? response.data.temperaturePreferences : [];
        
        console.log('Extracted preferences:', {
          cities: cityPrefs,
          weather: weatherPrefs,
          temp: tempPrefs
        });
        
        // Set user data (preferences from userPreferences.json)
        // IMPORTANT: Store the preference entry's id (from userPreferences.json) - this is what we need to update
        // For charlie (userId: 3), the preference entry has id: 5 in userPreferences.json
        const userWithPrefs = {
          id: response.data.id, // This is the id from userPreferences.json entry (e.g., 5 for clientId 3)
          userId: loggedInUser.userId, // Keep userId for reference (e.g., 3 for charlie)
          clientId: loggedInUser.clientId,
          cityPreferences: cityPrefs,
          weatherPreferences: weatherPrefs,
          temperaturePreferences: tempPrefs
        };
        console.log('Setting user with preference entry id:', response.data.id, 'userId:', loggedInUser.userId);
        setUser(userWithPrefs);
        
        // Set preferences from userPreferences.json (mapped by clientId)
        const prefsToSet = {
          cityPreferences: cityPrefs,
          weatherPreferences: weatherPrefs,
          temperaturePreferences: tempPrefs
        };
        console.log('Setting preferences state:', prefsToSet);
        setPreferences(prefsToSet);
        
        console.log('Successfully loaded preferences from userPreferences.json (by clientId):', {
          clientId: loggedInUser.clientId,
          userId: loggedInUser.userId,
          preferenceEntryId: response.data.id,
          cityPreferences: cityPrefs,
          weatherPreferences: weatherPrefs,
          temperaturePreferences: tempPrefs
        });
      } catch (prefErr) {
        console.error('Error loading preferences for clientId:', prefErr);
        console.error('Error details:', prefErr.response?.data || prefErr.message);
        console.log('Using empty preferences for clientId:', loggedInUser.clientId);
        // Preferences don't exist yet, use empty preferences
        setPreferences({
          cityPreferences: [],
          weatherPreferences: [],
          temperaturePreferences: []
        });
      }
    } catch (err) {
      console.error('Error loading user profile:', err);
      const loggedInUser = JSON.parse(localStorage.getItem('loggedInUser'));
      if (loggedInUser) {
        // Set user with empty preferences if there's an error
        setUser({
          id: loggedInUser.userId,
          clientId: loggedInUser.clientId,
          cityPreferences: [],
          weatherPreferences: [],
          temperaturePreferences: []
        });
        setPreferences({
          cityPreferences: [],
          weatherPreferences: [],
          temperaturePreferences: []
        });
        setError('Could not load preferences. You can add preferences below.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('loggedInUser');
    navigate('/login');
  };

  const handleAddAllPreferences = () => {
    const updatedPreferences = { ...preferences };
    let hasChanges = false;

    // Parse and add cities
    if (newCity.trim()) {
      const cities = newCity.split(',').map(c => c.trim()).filter(c => c && !updatedPreferences.cityPreferences.includes(c));
      if (cities.length > 0) {
        updatedPreferences.cityPreferences = [...updatedPreferences.cityPreferences, ...cities];
        hasChanges = true;
      }
      setNewCity('');
    }

    // Parse and add weather preferences
    if (newWeather.trim()) {
      const weathers = newWeather.split(',').map(w => w.trim()).filter(w => w && !updatedPreferences.weatherPreferences.includes(w));
      if (weathers.length > 0) {
        updatedPreferences.weatherPreferences = [...updatedPreferences.weatherPreferences, ...weathers];
        hasChanges = true;
      }
      setNewWeather('');
    }

    // Parse and add temperature preferences
    if (newTemperature.trim()) {
      const temps = newTemperature.split(',').map(t => t.trim()).filter(t => t && !updatedPreferences.temperaturePreferences.includes(t));
      if (temps.length > 0) {
        updatedPreferences.temperaturePreferences = [...updatedPreferences.temperaturePreferences, ...temps];
        hasChanges = true;
      }
      setNewTemperature('');
    }

    if (hasChanges) {
      setPreferences(updatedPreferences);
      setSuccess('Preferences added! Click "Save Preferences" to save changes.');
    }
  };

  const handleRemoveCity = (city) => {
    setPreferences({
      ...preferences,
      cityPreferences: preferences.cityPreferences.filter(c => c !== city)
    });
  };

  const handleRemoveWeather = (weather) => {
    setPreferences({
      ...preferences,
      weatherPreferences: preferences.weatherPreferences.filter(w => w !== weather)
    });
  };

  const handleRemoveTemperature = (temp) => {
    setPreferences({
      ...preferences,
      temperaturePreferences: preferences.temperaturePreferences.filter(t => t !== temp)
    });
  };

  const handleStartEdit = (type, index, currentValue) => {
    setEditing({ type, index, value: currentValue });
  };

  const handleCancelEdit = () => {
    setEditing(null);
  };

  const handleSaveEdit = () => {
    if (!editing) return;
    
    const { type, index, value } = editing;
    const trimmedValue = value.trim();
    
    if (!trimmedValue) {
      setError('Value cannot be empty');
      return;
    }

    const updatedPreferences = { ...preferences };
    
    if (type === 'city') {
      // Check for duplicates
      if (updatedPreferences.cityPreferences.includes(trimmedValue) && 
          updatedPreferences.cityPreferences[index] !== trimmedValue) {
        setError('This city already exists');
        return;
      }
      updatedPreferences.cityPreferences[index] = trimmedValue;
    } else if (type === 'weather') {
      if (updatedPreferences.weatherPreferences.includes(trimmedValue) && 
          updatedPreferences.weatherPreferences[index] !== trimmedValue) {
        setError('This weather preference already exists');
        return;
      }
      updatedPreferences.weatherPreferences[index] = trimmedValue;
    } else if (type === 'temperature') {
      if (updatedPreferences.temperaturePreferences.includes(trimmedValue) && 
          updatedPreferences.temperaturePreferences[index] !== trimmedValue) {
        setError('This temperature preference already exists');
        return;
      }
      updatedPreferences.temperaturePreferences[index] = trimmedValue;
    }
    
    setPreferences(updatedPreferences);
    setEditing(null);
    setError(null);
    setSuccess('Preference updated! Click "Save Preferences" to save changes.');
  };

  const handleUpdatePreferences = async () => {
    try {
      setUpdating(true);
      setError(null);
      setSuccess(null);
      
      const loggedInUser = JSON.parse(localStorage.getItem('loggedInUser'));
      if (!loggedInUser) {
        setError('User not logged in');
        return;
      }
      
      // Use the preference entry's id from user state (this is the id from userPreferences.json)
      // For charlie: userId=3, but preference entry id=5 in userPreferences.json
      // We MUST use the preference entry id (5) to update, not userId (3)
      const currentUser = user || {
        id: null,
        clientId: loggedInUser.clientId,
        cityPreferences: [],
        weatherPreferences: [],
        temperaturePreferences: []
      };
      
      // CRITICAL: Use the preference entry id from userPreferences.json (user.id)
      // This is the id that exists in the file (e.g., 5), not userId (e.g., 3)
      // If no preference entry exists yet (id is null), use userId to create new entry
      const preferenceId = currentUser.id || loggedInUser.userId;
      
      console.log('Saving preferences:', {
        'user.id (preference entry id)': currentUser.id,
        'userId (from localStorage)': loggedInUser.userId,
        'clientId': loggedInUser.clientId,
        'Using preferenceId for update': preferenceId,
        'preferences': preferences
      });
      
      const updatedUser = {
        id: preferenceId,
        clientId: loggedInUser.clientId,
        cityPreferences: preferences.cityPreferences,
        weatherPreferences: preferences.weatherPreferences,
        temperaturePreferences: preferences.temperaturePreferences
      };

      console.log('Saving preferences with:', {
        preferenceId,
        clientId: loggedInUser.clientId,
        userId: loggedInUser.userId,
        preferences
      });

      // Try to update first (if entry exists)
      let response;
      try {
        response = await axios.put(`${API_BASE}/user/${preferenceId}/update`, updatedUser);
        console.log('Update successful:', response.data);
      } catch (updateErr) {
        // If update fails with "not found", try to add/create the entry
        if (updateErr.response?.status === 404 || updateErr.response?.data?.error?.includes('not found')) {
          console.log('Entry not found, creating new entry...');
          response = await axios.put(`${API_BASE}/user/${preferenceId}/add`, updatedUser);
          console.log('Add successful:', response.data);
        } else {
          throw updateErr;
        }
      }
      
      setUser({
        ...response.data,
        id: response.data.id || preferenceId,
        clientId: loggedInUser.clientId
      });
      setSuccess('Preferences updated successfully!');
      
      // Reload preferences to ensure we have the latest data
      await loadUserProfile();
    } catch (err) {
      console.error('Error saving preferences:', err);
      setError(err.response?.data?.error || 'Failed to update preferences');
    } finally {
      setUpdating(false);
    }
  };

  if (loading) {
    return (
      <div className="profile-container">
        <nav className="navbar">
          <div className="nav-container">
            <div className="nav-brand-section">
              <span className="nav-logo">TARS</span>
            </div>
          </div>
        </nav>
        <div className="loading">Loading profile...</div>
      </div>
    );
  }

  // Get user from localStorage if user state is not set
  const loggedInUser = localStorage.getItem('loggedInUser') 
    ? JSON.parse(localStorage.getItem('loggedInUser'))
    : null;
  
  if (!user && !loggedInUser) {
    navigate('/login');
    return null;
  }

  // Use user state if available, otherwise create from localStorage
  const displayUser = user || {
    id: loggedInUser?.userId,
    clientId: loggedInUser?.clientId,
    cityPreferences: [],
    weatherPreferences: [],
    temperaturePreferences: []
  };

  return (
    <div className="profile-container">
      <nav className="navbar">
        <div className="nav-container">
          <div className="nav-brand-section">
            <span className="nav-logo">TARS</span>
            <span className="client-id">User ID: {displayUser.id}</span>
          </div>
          <ul className="nav-menu">
            <li className="nav-item">
              <button onClick={() => navigate('/dashboard')} className="nav-link-btn">
                Dashboard
              </button>
            </li>
            <li className="nav-item">
              <button onClick={handleLogout} className="nav-link-btn">
                Logout
              </button>
            </li>
          </ul>
        </div>
      </nav>

      <div className="profile-content">
        <div className="profile-header">
          <h1>My Profile</h1>
          <p className="profile-subtitle">Manage your preferences and information</p>
        </div>

        {(error || success) && (
          <div className="alert-container">
            {error && <div className="alert alert-error"><strong>Error:</strong> {error}</div>}
            {success && <div className="alert alert-success"><strong>Success!</strong> {success}</div>}
          </div>
        )}

        <div className="profile-grid">
          <div className="profile-card">
            <h2>User Information</h2>
            <div className="info-section">
              <div className="info-item">
                <span className="info-label">User ID:</span>
                <span className="info-value">{displayUser.id}</span>
              </div>
              <div className="info-item">
                <span className="info-label">Client ID:</span>
                <span className="info-value">{displayUser.clientId}</span>
              </div>
              {userInfo && userInfo.username && (
                <div className="info-item">
                  <span className="info-label">Username:</span>
                  <span className="info-value">{userInfo.username}</span>
                </div>
              )}
              {userInfo && userInfo.email && (
                <div className="info-item">
                  <span className="info-label">Email:</span>
                  <span className="info-value">{userInfo.email}</span>
                </div>
              )}
              {userInfo && userInfo.role && (
                <div className="info-item">
                  <span className="info-label">Role:</span>
                  <span className="info-value">{userInfo.role}</span>
                </div>
              )}
            </div>
          </div>

          <div className="profile-card">
            <h2>Add Preferences</h2>
            <p className="preference-source">Add preferences to save to userPreferences.json</p>
            <form onSubmit={(e) => { e.preventDefault(); handleAddAllPreferences(); }} className="preference-form">
              <div className="form-group">
                <label>City (comma-separated):</label>
                <input
                  type="text"
                  value={newCity}
                  onChange={(e) => setNewCity(e.target.value)}
                  placeholder="e.g., New York, Paris, Boston"
                  className="form-control"
                />
                <small className="form-help">Enter cities separated by commas</small>
              </div>
              
              <div className="form-group">
                <label>Weather Preferences (comma-separated):</label>
                <input
                  type="text"
                  value={newWeather}
                  onChange={(e) => setNewWeather(e.target.value)}
                  placeholder="e.g., Sunny, Clear, Rainy"
                  className="form-control"
                />
                <small className="form-help">Enter weather types separated by commas</small>
              </div>
              
              <div className="form-group">
                <label>Temperature Preferences (comma-separated):</label>
                <input
                  type="text"
                  value={newTemperature}
                  onChange={(e) => setNewTemperature(e.target.value)}
                  placeholder="e.g., 70F, 60-80F, 50F"
                  className="form-control"
                />
                <small className="form-help">Enter temperatures separated by commas</small>
              </div>
              
              <button type="submit" className="btn btn-primary">Add Preferences</button>
            </form>
          </div>

          <div className="profile-card">
            <h2>City Preferences</h2>
            <p className="preference-source">Loaded from userPreferences.json</p>
            <div className="preference-list">
              {preferences.cityPreferences.length === 0 ? (
                <p className="empty-state">No city preferences set</p>
              ) : (
                preferences.cityPreferences.map((city, idx) => (
                  <div key={idx} className="preference-item">
                    {editing && editing.type === 'city' && editing.index === idx ? (
                      <div className="edit-mode">
                        <input
                          type="text"
                          value={editing.value}
                          onChange={(e) => setEditing({...editing, value: e.target.value})}
                          onKeyPress={(e) => {
                            if (e.key === 'Enter') handleSaveEdit();
                            if (e.key === 'Escape') handleCancelEdit();
                          }}
                          className="edit-input"
                          autoFocus
                        />
                        <button onClick={handleSaveEdit} className="btn-save-edit">✓</button>
                        <button onClick={handleCancelEdit} className="btn-cancel-edit">✕</button>
                      </div>
                    ) : (
                      <>
                        <span>{city}</span>
                        <div className="preference-actions">
                          <button onClick={() => handleStartEdit('city', idx, city)} className="btn-edit">Edit</button>
                          <button onClick={() => handleRemoveCity(city)} className="btn-remove">×</button>
                        </div>
                      </>
                    )}
                  </div>
                ))
              )}
            </div>
          </div>

          <div className="profile-card">
            <h2>Weather Preferences</h2>
            <p className="preference-source">Loaded from userPreferences.json</p>
            <div className="preference-list">
              {preferences.weatherPreferences.length === 0 ? (
                <p className="empty-state">No weather preferences set</p>
              ) : (
                preferences.weatherPreferences.map((weather, idx) => (
                  <div key={idx} className="preference-item">
                    {editing && editing.type === 'weather' && editing.index === idx ? (
                      <div className="edit-mode">
                        <input
                          type="text"
                          value={editing.value}
                          onChange={(e) => setEditing({...editing, value: e.target.value})}
                          onKeyPress={(e) => {
                            if (e.key === 'Enter') handleSaveEdit();
                            if (e.key === 'Escape') handleCancelEdit();
                          }}
                          className="edit-input"
                          autoFocus
                        />
                        <button onClick={handleSaveEdit} className="btn-save-edit">✓</button>
                        <button onClick={handleCancelEdit} className="btn-cancel-edit">✕</button>
                      </div>
                    ) : (
                      <>
                        <span>{weather}</span>
                        <div className="preference-actions">
                          <button onClick={() => handleStartEdit('weather', idx, weather)} className="btn-edit">Edit</button>
                          <button onClick={() => handleRemoveWeather(weather)} className="btn-remove">×</button>
                        </div>
                      </>
                    )}
                  </div>
                ))
              )}
            </div>
          </div>

          <div className="profile-card">
            <h2>Temperature Preferences</h2>
            <p className="preference-source">Loaded from userPreferences.json</p>
            <div className="preference-list">
              {preferences.temperaturePreferences.length === 0 ? (
                <p className="empty-state">No temperature preferences set</p>
              ) : (
                preferences.temperaturePreferences.map((temp, idx) => (
                  <div key={idx} className="preference-item">
                    {editing && editing.type === 'temperature' && editing.index === idx ? (
                      <div className="edit-mode">
                        <input
                          type="text"
                          value={editing.value}
                          onChange={(e) => setEditing({...editing, value: e.target.value})}
                          onKeyPress={(e) => {
                            if (e.key === 'Enter') handleSaveEdit();
                            if (e.key === 'Escape') handleCancelEdit();
                          }}
                          className="edit-input"
                          autoFocus
                        />
                        <button onClick={handleSaveEdit} className="btn-save-edit">✓</button>
                        <button onClick={handleCancelEdit} className="btn-cancel-edit">✕</button>
                      </div>
                    ) : (
                      <>
                        <span>{temp}</span>
                        <div className="preference-actions">
                          <button onClick={() => handleStartEdit('temperature', idx, temp)} className="btn-edit">Edit</button>
                          <button onClick={() => handleRemoveTemperature(temp)} className="btn-remove">×</button>
                        </div>
                      </>
                    )}
                  </div>
                ))
              )}
            </div>
          </div>
        </div>

        <div className="profile-actions">
          <button 
            onClick={handleUpdatePreferences} 
            className="btn btn-primary btn-save"
            disabled={updating}
          >
            {updating ? 'Updating...' : 'Save Preferences'}
          </button>
        </div>
      </div>
    </div>
  );
}

export default UserProfile;

