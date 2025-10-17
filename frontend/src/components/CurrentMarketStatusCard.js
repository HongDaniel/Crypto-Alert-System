import React, { useEffect, useState } from 'react';
import GaugeChart from 'react-gauge-chart';
import '../css/CurrentMarketStatusCard.css';

// 상태별 컬러 팔레트
const VALUE_COLORS = {
  Extreme_Fear: "#059669",
  Fear: "#4ade80",
  Neutral: "#facc15",
  Greed: "#fb923c",
  Extreme_Greed: "#ea580c",
};

function getClassificationClass(status) {
  return status ? status.replace(' ', '-').toLowerCase() : '';
}

const CurrentMarketStatusCard = () => {
  const [indexValue, setIndexValue] = useState(0);
  const [classification, setClassification] = useState('');
  const [lastUpdated, setLastUpdated] = useState('');

  useEffect(() => {
    const fetchFearGreedData = async () => {
      try {
        const response = await fetch('https://api.alternative.me/fng/');
        const data = await response.json();
        if (data?.data && data.data.length > 0) {
          const latest = data.data[0];
          const value = Number(latest.value);
          setIndexValue(value);
          setClassification(latest.value_classification);
          const timestamp = Number(latest.timestamp);
          const updatedDate = new Date(timestamp * 1000);
          setLastUpdated(updatedDate.toLocaleString());
        }
      } catch (error) {
        console.error('Error fetching Fear & Greed Index:', error);
      }
    };

    fetchFearGreedData();
  }, []);

  // 게이지 색상 배열
  const gaugeColors = [
    VALUE_COLORS.Extreme_Fear,
    VALUE_COLORS.Fear,
    VALUE_COLORS.Neutral,
    VALUE_COLORS.Greed,
    VALUE_COLORS.Extreme_Greed,
  ];
  const statusColor = VALUE_COLORS[classification.replace(' ', '_')] || "#6366f1";

  return (
    <div className="current-market-status-card">
      <div className="cms-title">Current Market Status</div>
      <div className="cms-subtitle">Crypto Fear &amp; Greed Index</div>
      <div className="cms-gauge-section">
        <div className="cms-gaugechart-wrapper">
          <GaugeChart
            id="crypto-fear-gauge"
            nrOfLevels={120}
            colors={gaugeColors}
            arcWidth={0.22}
            percent={indexValue / 100}
            needleColor="transparent"
            needleBaseColor="transparent"
            animate={false}
            textColor="transparent"
            arcPadding={0}
            hideText={true}
          />
          <div
            className="cms-gauge-center-value"
            style={{ color: statusColor }}
          >
            {indexValue}
          </div>
        </div>
        {/* 숫자 + 상태명 게이지 아래에 한 번만 표출 */}
        <div
          className={`cms-main-value-status ${getClassificationClass(classification)}`}
        >
          <span className="cms-main-status" style={{ color: statusColor }}>{classification}</span>
        </div>
      </div>
      <div className="cms-info">
        <span className="cms-last-updated">Last updated: {lastUpdated}</span>
      </div>
    </div>
  );
};

export default CurrentMarketStatusCard;
