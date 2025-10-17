// src/pages/LoginPage.jsx
import React, { useState } from 'react';
import '../css/LoginPage.css';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

function LoginPage() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: '',
    password: '',
  });

  const handleChange = e => {
    setFormData(prev => ({
      ...prev,
      [e.target.name]: e.target.value
    }));
  };

  const handleSubmit = async e => {
    e.preventDefault();

    try {
      const response = await axios.post(
        '/api/auth/login',
        formData,
        {
          withCredentials: true // ✅ HttpOnly 쿠키를 프론트에 포함시킴
        }
      );

      console.log('✅ 로그인 성공:', response.data);

      // 이후 유저 상태 조회도 가능 (ex. /api/auth/me)
      // navigate('/');
      window.location.href = '/'; // ✅ 쿠키 즉시 반영, App.js useEffect 재실행
    } catch (error) {
      console.error('❌ 로그인 실패:', error.response?.data || error.message);
      alert('로그인 실패: 이메일/비밀번호를 확인하세요');
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h2 className="auth-title">Freedom</h2>
        <p className="auth-subtitle">Crypto Alert System</p>

        <form onSubmit={handleSubmit}>
          <label>Email</label>
          <input
            type="email"
            name="email"
            placeholder="you@example.com"
            value={formData.email}
            onChange={handleChange}
            required
          />

          <label>Password</label>
          <input
            type="password"
            name="password"
            placeholder="••••••••"
            value={formData.password}
            onChange={handleChange}
            required
          />

          <button type="submit">Login</button>
        </form>

        <p className="auth-bottom-text">
          Don't have an account? <a href="/register">Register</a>
        </p>
      </div>
    </div>
  );
}

export default LoginPage;
