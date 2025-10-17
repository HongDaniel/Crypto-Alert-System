// src/components/Dashboard.jsx
import React from 'react';

import CurrentMarketStatusCard from './CurrentMarketStatusCard';
import AlertSettingsCard from './AlertSettingsCard';
import AlertHistoryCard from './AlertHistoryCard';
import FearGreedIndexHistoryCard from './FearGreedIndexHistoryCard'; 
import '../css/Dashboard.css';

function Dashboard() {
  return (
    <div className="dashboard-2col">
      <div className="dashboard-col">
        <div className="service-card service-card-left">
          <CurrentMarketStatusCard />
        </div>
        {/* <div className="service-card">
          <AlertSettingsCard />
        </div> */}
      </div>
      <div className="dashboard-col">
        <div className="service-card">
          <FearGreedIndexHistoryCard />
        </div>
        <div className="service-card-right">
          <AlertHistoryCard />
        </div>  
      </div>
    </div>
  );
}

export default Dashboard;
