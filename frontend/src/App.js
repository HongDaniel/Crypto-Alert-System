// src/App.js
import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import axios from 'axios';

import './css/App.css';
import RegisterPage from './pages/RegisterPage';
import LoginPage from './pages/LoginPage';
import MainPage from './pages/MainPage';
import NotificationsPage from './pages/NotificationsPage';
import AccountPage from './pages/AccountPage';
import PrivateRoute from './components/PrivateRoute';
import PublicRoute from './components/PublicRoute';

function App() {
  const [username, setUsername] = useState('');
  const [authChecked, setAuthChecked] = useState(false);

  useEffect(() => {
    axios.get('/api/auth/me', { withCredentials: true })
      .then((res) => {
        console.log('User authenticated:', res);
        setUsername(res.data.username);
        setAuthChecked(true);
      })
      .catch(() => {
        setUsername('');
        setAuthChecked(true);
      });
  }, []);

  
  const handleLogout = async () => {
    try {
      await axios.post('/api/auth/logout', {}, { withCredentials: true });
      window.location.href = '/login'; // 또는 navigate('/login')
    } catch (err) {
      console.error('Logout failed', err);
      alert('로그아웃 실패');
    }
  };
  

  if (!authChecked) return null; // 로딩 중

  return (
    <div className="App">
      <Router>
        <Routes>
          <Route
            path="/register"
            element={
              <PublicRoute>
                <RegisterPage />
              </PublicRoute>
            }
          />
          <Route
            path="/login"
            element={
              <PublicRoute>
                <LoginPage />
              </PublicRoute>
            }
          />
          <Route
            path="/"
            element={
              <PrivateRoute>
                <MainPage username={username} onLogout={handleLogout} />
              </PrivateRoute>
            }
          />
          <Route
            path="/notifications"
            element={
              <PrivateRoute>
                <NotificationsPage username={username} onLogout={handleLogout} />
              </PrivateRoute>
            }
          />
          <Route
            path="/account"
            element={
              <PrivateRoute>
                <AccountPage username={username} onLogout={handleLogout} />
              </PrivateRoute>
            }
          />
        </Routes>
      </Router>
    </div>
  );
}

export default App;
