import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './Login.css';

const API_BASE = 'http://localhost:3001/api';

function Login() {
  const [loginType, setLoginType] = useState('username'); // 'username', 'email', or 'userId'
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [userId, setUserId] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      // Build login request based on login type
      const loginData = {};
      if (loginType === 'username') {
        loginData.username = username.trim();
      } else if (loginType === 'email') {
        loginData.email = email.trim();
      } else {
        loginData.userId = parseInt(userId);
      }

      // Login via API
      const response = await axios.post(`${API_BASE}/login`, loginData);
      const userData = response.data;
      
      // Store user info in localStorage
      localStorage.setItem('loggedInUser', JSON.stringify({
        userId: userData.userId,
        clientId: userData.clientId,
        username: userData.username,
        email: userData.email,
        role: userData.role,
        preferences: userData.preferences
      }));
      
      // Navigate based on role
      const role = userData.role?.toLowerCase();
      if (role === 'admin' || role === 'administrator') {
        navigate('/admin');
      } else {
        navigate('/dashboard');
      }
    } catch (err) {
      setError(err.response?.data?.error || 'Login failed. Please check your credentials.');
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <h1>TARS</h1>
          <p>Travel Alert and Recommendation Service</p>
        </div>
        
        <form onSubmit={handleLogin} className="login-form">
          <div className="form-group">
            <label htmlFor="loginType">Login with:</label>
            <select
              id="loginType"
              value={loginType}
              onChange={(e) => {
                setLoginType(e.target.value);
                setUsername('');
                setEmail('');
                setUserId('');
                setError(null);
              }}
              className="form-control"
            >
              <option value="username">Username</option>
              <option value="email">Email</option>
              <option value="userId">User ID</option>
            </select>
          </div>

          {loginType === 'username' && (
            <div className="form-group">
              <label htmlFor="username">Username</label>
              <input
                type="text"
                id="username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Enter your username"
                required
                className="form-control"
              />
              <small className="form-help">Example: alice, bob, charlie</small>
            </div>
          )}

          {loginType === 'email' && (
            <div className="form-group">
              <label htmlFor="email">Email</label>
              <input
                type="email"
                id="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="Enter your email"
                required
                className="form-control"
              />
              <small className="form-help">Example: alice@gmail.com</small>
            </div>
          )}

          {loginType === 'userId' && (
            <div className="form-group">
              <label htmlFor="userId">User ID</label>
              <input
                type="number"
                id="userId"
                value={userId}
                onChange={(e) => setUserId(e.target.value)}
                placeholder="Enter your User ID"
                required
                className="form-control"
                min="1"
              />
              <small className="form-help">Example: 1, 2, 3</small>
            </div>
          )}

          {error && (
            <div className="alert alert-error">
              {error}
            </div>
          )}

          <button 
            type="submit" 
            className="btn btn-primary"
            disabled={loading}
          >
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>

        <div className="login-footer">
          <p>Don't have a User ID? Contact your administrator.</p>
        </div>
      </div>
    </div>
  );
}

export default Login;

