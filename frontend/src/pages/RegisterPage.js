import React, { useState } from 'react';
import '../css/RegisterPage.css';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    email: '',
    username: '',
    password: '',
    confirmPassword: '',
    agree: false,
  });

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm({ ...form, [name]: type === 'checkbox' ? checked : value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (form.password !== form.confirmPassword) {
      alert('Passwords do not match');
      return;
    }

    if (!form.agree) {
      alert('You must agree to the terms!');
      return;
    }

    try {
      const response = await axios.post('/api/users/signup', {
        email: form.email,
        username: form.username,
        password: form.password
      });

      console.log('✅ Signup success:', response.data);
      alert('회원가입 완료! 로그인 페이지로 이동합니다.');
      navigate('/login');
    } catch (err) {
      console.error('❌ Signup failed:', err.response?.data || err.message);
      alert('회원가입 실패! 서버 응답을 확인하세요.');
    }
  };

  return (
    <div className="register-container">
      <div className="register-card">
        <h1 className="register-title">Freedom</h1>
        <h2 className="register-subtitle">Create Your Account</h2>

        <form onSubmit={handleSubmit} className="register-form">
          <label>Email</label>
          <input
            name="email"
            type="email"
            value={form.email}
            onChange={handleChange}
            required
          />

          <label>Username</label>
          <input
            name="username"
            type="text"
            value={form.username}
            onChange={handleChange}
            required
          />

          <label>Password</label>
          <input
            name="password"
            type="password"
            value={form.password}
            onChange={handleChange}
            required
          />

          <label>Confirm Password</label>
          <input
            name="confirmPassword"
            type="password"
            value={form.confirmPassword}
            onChange={handleChange}
            required
          />

          <div className="register-checkbox">
            <input
              type="checkbox"
              name="agree"
              checked={form.agree}
              onChange={handleChange}
              required
            />
            <span>I agree to the Terms of Service</span>
          </div>

          <button type="submit" className="register-button">Create Account</button>

          <p className="register-login-link">
            Already have an account? <a href="/login">Login</a>
          </p>
        </form>
      </div>
    </div>
  );
}

export default RegisterPage;
