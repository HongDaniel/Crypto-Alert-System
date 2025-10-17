import React, { useState } from 'react';
import axios from 'axios';
import '../css/AlertSettings.css';

function AlertSettingsCard({ fetchAlerts }) {
  const [threshold, setThreshold] = useState('70');
  const [alertType, setAlertType] = useState('ABOVE');
  const [channels, setChannels] = useState({
    email: false,
    sms: false
  });

  const handleChannelChange = (e) => {
    const { name, checked } = e.target;
    setChannels(prev => ({ ...prev, [name]: checked }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const payload = {
      threshold: parseInt(threshold, 10),
      alertType: alertType,
      email: channels.email,
      sms: channels.sms
    };

    try {
      const response = await axios.post('/api/alerts', payload, {
        withCredentials: true // JWT HttpOnly 쿠키 전송
      });
      alert('알림 설정이 저장되었습니다!');
      fetchAlerts();
    } catch (error) {
      console.error('❌ 알림 설정 등록 실패:', error.response?.data || error.message);
      alert('알림 등록에 실패했습니다.');
    }
  };


  return (
    <section className="alert-settings">
      <h2>Alert Settings</h2>
      <form onSubmit={handleSubmit}>
        <div className="alert-field">
          <label>
            Fear &amp; Greed Index Threshold:
            <input
              type="number"
              value={threshold}
              onChange={(e) => setThreshold(e.target.value)}
              min="0"
              max="100"
              required
            />
          </label>
        </div>

        <div className="alert-field">
          <label>
            <input
              type="radio"
              name="alertType"
              value="ABOVE"
              checked={alertType === 'ABOVE'}
              onChange={() => setAlertType('ABOVE')}
            />
            Above
          </label>
          <label style={{ marginLeft: '16px' }}>
            <input
              type="radio"
              name="alertType"
              value="BELOW"
              checked={alertType === 'BELOW'}
              onChange={() => setAlertType('BELOW')}
            />
            Below
          </label>
        </div>

        <div className="alert-field">
          <label>
            <input
              type="checkbox"
              name="email"
              checked={channels.email}
              onChange={handleChannelChange}
            />
            Email
          </label>
          <label>
            <input
              type="checkbox"
              name="sms"
              checked={channels.sms}
              onChange={handleChannelChange}
            />
            SMS
          </label>
        </div>

        <button type="submit">Add New Alert Condition</button>
      </form>
    </section>
  );
}

export default AlertSettingsCard;
