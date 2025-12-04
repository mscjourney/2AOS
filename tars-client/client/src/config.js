// API Configuration
// Uses window.location.origin in production, or environment variable for development
const getApiBase = () => {
  // Check for environment variable (useful for development)
  if (process.env.REACT_APP_API_BASE) {
    return process.env.REACT_APP_API_BASE;
  }
  
  return `${window.location.origin}/api`;
};

export const API_BASE = getApiBase();

