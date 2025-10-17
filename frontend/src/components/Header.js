import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../css/Header.css';

function Header({ username, onLogout }) {
  const navigate = useNavigate(); // ✅ 이 줄이 꼭 필요합니다!

  return (
    <header className="header">
      <div className="header-logo" onClick={() => navigate('/')} style={{ cursor: 'pointer' }}>
        Freedom
      </div>
      <div className="header-btn-group">
        <button className="header-btn" onClick={() => navigate('/notifications')}>Alert Settings</button>
        <button className="header-btn" onClick={() => navigate('/account')}>Account</button>
        <div className="header-user">
          <span className="header-username">{username} 님</span>
          <button className="header-logout-btn" onClick={onLogout}>로그아웃</button>
        </div>
      </div>
    </header>
  );
}


export default Header;
