import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Dashboard from './components/Dashboard';
import Login from './components/Login';
import UserProfile from './components/UserProfile';
import AdminDashboard from './components/AdminDashboard';
import './App.css';

// Protected Route component
const ProtectedRoute = ({ children }) => {
  const loggedInUser = localStorage.getItem('loggedInUser');
  return loggedInUser ? children : <Navigate to="/login" />;
};

// Admin Route component
const AdminRoute = ({ children }) => {
  const loggedInUser = localStorage.getItem('loggedInUser');
  if (!loggedInUser) {
    return <Navigate to="/login" />;
  }
  
  const user = JSON.parse(loggedInUser);
  const role = user.role?.toLowerCase();
  if (role !== 'admin' && role !== 'administrator') {
    return <Navigate to="/dashboard" />;
  }
  
  return children;
};

function App() {
  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/profile" element={<ProtectedRoute><UserProfile /></ProtectedRoute>} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/admin" element={<AdminRoute><AdminDashboard /></AdminRoute>} />
          <Route path="/" element={<Navigate to="/dashboard" />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
