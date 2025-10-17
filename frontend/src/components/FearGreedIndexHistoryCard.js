import React, { useEffect, useState } from 'react';
import '../css/FearGreedIndexHistoryCard.css';

const API_URL = 'https://api.alternative.me/fng/?limit=7&format=json';

// 단계별 색상 매핑 (위에서 아래로 진한 오렌지 → 오렌지 → 노랑 → 연두 → 진한 초록)
const VALUE_COLORS = {
  Extreme_Fear: "#059669",   // 진한 오렌지
  Fear: "#4ade80",           // 오렌지
  Neutral: "#facc15",        // 노랑
  Greed: "#fb923c",          // 연한 초록
  Extreme_Greed: "#ea580c",  // 진한 초록
};

function capitalize(str) {
  return str ? str.charAt(0).toUpperCase() + str.slice(1).replace('_', ' ') : '';
}

const FearGreedIndexHistoryCard = () => {
  const [history, setHistory] = useState([]);

  useEffect(() => {
    const fetchHistory = async () => {
      try {
        const res = await fetch(API_URL);
        const json = await res.json();
        if (json?.data) {
          // timestamp 기준으로 내림차순 정렬 (최신 날짜가 먼저)
          const sortedData = json.data.sort((a, b) => Number(b.timestamp) - Number(a.timestamp));
          setHistory(sortedData);
        }
      } catch (e) {
        setHistory([]);
      }
    };
    fetchHistory();
  }, []);

  return (
    <div className="fgi-history-card">
      <div className="fgi-card-title">Fear &amp; Greed Index History</div>
      <div className="fgi-history-list">
        {history.length === 0 && (
          <div className="fgi-history-empty">No data available</div>
        )}
        {history.map((item) => {
          const color = VALUE_COLORS[item.value_classification.replace(' ', '_')] || '#6366f1';
          return (
            <div className="fgi-history-row" key={item.timestamp}>
              <div className="fgi-history-value" style={{ color }}>
                {item.value}
              </div>
              <div className="fgi-history-label" style={{ color, fontWeight: 700 }}>
                {capitalize(item.value_classification)}
              </div>
              <div className="fgi-history-date">
                {new Date(Number(item.timestamp) * 1000).toLocaleDateString()}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default FearGreedIndexHistoryCard;
