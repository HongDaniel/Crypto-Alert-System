// src/pages/NotificationDashboard.js
import React from 'react';
import Header from '../components/Header';
import Dashboard from '../components/Dashboard';
import '../css/MainPage.css';

function MainPage({ username, onLogout }) {
  return (
    <div className="dashboard-root">
      <Header username={username} onLogout={onLogout} />
      <Dashboard />
    </div>
  );
}

export default MainPage;
