import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './AdminDashboard.css';

const API_BASE = 'http://localhost:3001/api';

function AdminDashboard() {
  const navigate = useNavigate();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [loggedInUser, setLoggedInUser] = useState(null);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [newUser, setNewUser] = useState({
    username: '',
    email: '',
    role: 'user'
  });

  useEffect(() => {
    const loggedInUserData = localStorage.getItem('loggedInUser');
    if (!loggedInUserData) {
      navigate('/login');
      return;
    }

    const user = JSON.parse(loggedInUserData);
    setLoggedInUser(user);
    
    // Check if user is admin
    const role = user.role?.toLowerCase();
    if (role !== 'admin' && role !== 'administrator') {
      navigate('/dashboard');
      return;
    }

    loadUsers();
  }, [navigate]);

  const loadUsers = async () => {
    try {
      setLoading(true);
      setError(null);
      // Load users from users.json
      const response = await axios.get(`${API_BASE}/tarsUsers`);
      
      // Ensure response.data is an array
      const usersData = Array.isArray(response.data) ? response.data : [];
      setUsers(usersData);
    } catch (err) {
      console.error('Error loading users:', err);
      setError('Failed to load users: ' + (err.response?.data?.error || err.message));
      setUsers([]); // Set empty array on error
    } finally {
      setLoading(false);
    }
  };

  const handleCreateUser = async (e) => {
    e.preventDefault();
    
    if (!newUser.username || !newUser.email || !newUser.role) {
      setError('All fields are required');
      return;
    }

    if (!loggedInUser || !loggedInUser.clientId) {
      setError('Unable to determine client ID. Please log in again.');
      return;
    }

    try {
      setError(null);
      setSuccess(null);
      
      const response = await axios.post(`${API_BASE}/client/createUser`, {
        clientId: loggedInUser.clientId,
        username: newUser.username,
        email: newUser.email,
        role: newUser.role
      });
      
      // Add the created user to the state immediately if we have the full user object
      if (response.data && response.data.userId) {
        setUsers(prevUsers => {
          // Check if user already exists to avoid duplicates
          const exists = prevUsers.some(u => u.userId === response.data.userId);
          if (exists) {
            return prevUsers;
          }
          return [...prevUsers, response.data];
        });
      }
      
      setSuccess(`User "${newUser.username}" created successfully!`);
      setNewUser({
        username: '',
        email: '',
        role: 'user'
      });
      setShowCreateForm(false);
      
      // Reload the list to ensure we have the latest data from the backend
      await loadUsers();
    } catch (err) {
      console.error('Error creating user:', err);
      setError('Failed to create user: ' + (err.response?.data?.error || err.message));
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setNewUser(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleLogout = () => {
    localStorage.removeItem('loggedInUser');
    navigate('/login');
  };

  if (loading) {
    return (
      <div className="admin-container">
        <div className="loading">Loading admin dashboard...</div>
      </div>
    );
  }

  return (
    <div className="admin-container">
      <nav className="navbar">
        <div className="nav-container">
          <div className="nav-brand-section">
            <span className="nav-logo">TARS Admin</span>
            {loggedInUser && (
              <span className="admin-badge">Admin: {loggedInUser.username || loggedInUser.email}</span>
            )}
          </div>
          <ul className="nav-menu">
            <li className="nav-item">
              <button onClick={handleLogout} className="nav-link-btn">
                Logout
              </button>
            </li>
          </ul>
        </div>
      </nav>

      <div className="admin-content">
        <div className="admin-header">
          <h1>Admin Dashboard</h1>
          <p className="admin-subtitle">Manage users</p>
        </div>

        {(error || success) && (
          <div className="alert-container">
            {error && <div className="alert alert-error"><strong>Error:</strong> {error}</div>}
            {success && <div className="alert alert-success"><strong>Success!</strong> {success}</div>}
          </div>
        )}

        <div className="users-section">
          <div className="section-header">
            <h2>All Users ({users.length})</h2>
            <div className="header-actions">
              <button 
                onClick={() => setShowCreateForm(!showCreateForm)} 
                className="btn btn-primary"
              >
                {showCreateForm ? 'Cancel' : 'Create User'}
              </button>
              <button onClick={loadUsers} className="btn btn-secondary btn-refresh">Refresh</button>
            </div>
          </div>

          {showCreateForm && (
            <div className="create-user-form">
              <h3>Create New User</h3>
              <form onSubmit={handleCreateUser}>
                <div className="form-group">
                  <label htmlFor="username">Username *</label>
                  <input
                    type="text"
                    id="username"
                    name="username"
                    value={newUser.username}
                    onChange={handleInputChange}
                    required
                    placeholder="Enter username"
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="email">Email *</label>
                  <input
                    type="email"
                    id="email"
                    name="email"
                    value={newUser.email}
                    onChange={handleInputChange}
                    required
                    placeholder="Enter email"
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="role">Role *</label>
                  <select
                    id="role"
                    name="role"
                    value={newUser.role}
                    onChange={handleInputChange}
                    required
                  >
                    <option value="user">User</option>
                    <option value="admin">Admin</option>
                    <option value="ADMIN">ADMIN</option>
                  </select>
                </div>
                <div className="form-actions">
                  <button type="submit" className="btn btn-primary">Create User</button>
                  <button type="button" onClick={() => setShowCreateForm(false)} className="btn btn-secondary">Cancel</button>
                </div>
              </form>
            </div>
          )}

          {users.length === 0 ? (
            <div className="empty-state">No users found</div>
          ) : (
            <div className="users-grid">
              {Array.isArray(users) && users.map((user) => (
                <div key={user.userId} className="user-card">
                  <div className="user-header">
                    <h3>User ID: {user.userId}</h3>
                  </div>
                  
                  <div className="user-info">
                    <div className="info-row">
                      <span className="info-label">Username:</span>
                      <span className="info-value">{user.username || 'N/A'}</span>
                    </div>
                    <div className="info-row">
                      <span className="info-label">Email:</span>
                      <span className="info-value">{user.email || 'N/A'}</span>
                    </div>
                    <div className="info-row">
                      <span className="info-label">Client ID:</span>
                      <span className="info-value">{user.clientId}</span>
                    </div>
                    <div className="info-row">
                      <span className="info-label">Role:</span>
                      <span className="info-value">{user.role || 'N/A'}</span>
                    </div>
                    <div className="info-row">
                      <span className="info-label">Active:</span>
                      <span className="info-value">{user.active ? 'Yes' : 'No'}</span>
                    </div>
                    {user.signUpDate && (
                      <div className="info-row">
                        <span className="info-label">Sign Up Date:</span>
                        <span className="info-value">{new Date(user.signUpDate).toLocaleDateString()}</span>
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default AdminDashboard;

