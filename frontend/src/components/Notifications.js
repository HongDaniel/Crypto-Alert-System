// src/pages/NotificationsPage.jsx
import React, { useState, useEffect } from 'react';
import AlertSettingsCard from './AlertSettingsCard';
import ActiveAlertList from './ActiveAlertList';
import AlertHistoryCard from './AlertHistoryCard';
import axios from 'axios';

import '../css/Notifications.css';

function Notifications({ username, onLogout }) {
  const [alerts, setAlerts] = useState([]);

  // Fetch all alerts on component mount and when an alert is added
  const fetchAlerts = async () => {
    try {
      const response = await axios.get('/api/alerts', { withCredentials: true });
      setAlerts(response.data); // Store alerts in state directly
    } catch (error) {
      console.error('Error fetching alerts:', error);
    }
  };

  // Fetch alerts on component mount
  useEffect(() => {
    fetchAlerts();
  }, []);
  return (
    <div className="notifications-page">
      <div className="notification-top">
        <AlertSettingsCard fetchAlerts={fetchAlerts}/>
      </div>

      <div className="notification-bottom">
        <div className="notification-left">
          <ActiveAlertList alerts={alerts} fetchAlerts={fetchAlerts} />
        </div>
        <div className="notification-right">
          <AlertHistoryCard />
        </div>
      </div>
    </div>
  );
}

export default Notifications;
