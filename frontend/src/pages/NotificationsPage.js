// src/pages/NotificationDashboard.js
import React from 'react';
import Header from '../components/Header';
import Notifications from '../components/Notifications';
import '../css/MainPage.css';

function NotificationsPage({ username, onLogout }) {
  return (
    <div className="dashboard-root">
      <Header username={username} onLogout={onLogout} />
      <Notifications />
    </div>
  );
}

export default NotificationsPage;
