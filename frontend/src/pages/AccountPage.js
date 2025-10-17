import React, { useState, useEffect } from 'react';
import axios from 'axios';
import Header from '../components/Header';
import '../css/AccountPage.css';

function AccountPage({ username, onLogout }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    phoneNumber: ''
  });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  // 사용자 정보 조회
  const fetchUserInfo = async () => {
    try {
      const response = await axios.get('/api/auth/me', { withCredentials: true });
      setUser(response.data);
      setFormData({
        username: response.data.username || '',
        password: '',
        phoneNumber: response.data.phoneNumber || ''
      });
    } catch (error) {
      console.error('사용자 정보 조회 실패:', error);
      setError('사용자 정보를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUserInfo();
  }, []);

  // 폼 데이터 변경 핸들러
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // 수정 모드 토글
  const toggleEdit = () => {
    setEditing(!editing);
    if (!editing) {
      // 수정 모드로 들어갈 때 현재 사용자 정보로 폼 초기화
      setFormData({
        username: user.username || '',
        password: '',
        phoneNumber: user.phoneNumber || ''
      });
    }
    setMessage('');
    setError('');
  };

  // 사용자 정보 수정
  const handleUpdate = async () => {
    try {
      setError('');
      setMessage('');

      // 비밀번호가 비어있으면 제외하고 전송
      const updateData = { ...formData };
      if (!updateData.password.trim()) {
        delete updateData.password;
      }

      const response = await axios.put('/api/auth/me', updateData, { withCredentials: true });
      setUser(response.data);
      setEditing(false);
      setMessage('사용자 정보가 성공적으로 수정되었습니다.');
    } catch (error) {
      console.error('사용자 정보 수정 실패:', error);
      setError('사용자 정보 수정에 실패했습니다.');
    }
  };

  // 취소
  const handleCancel = () => {
    setEditing(false);
    setFormData({
      username: user.username || '',
      password: '',
      phoneNumber: user.phoneNumber || ''
    });
    setMessage('');
    setError('');
  };

  if (loading) {
    return (
      <div className="account-root">
        <Header username={username} onLogout={onLogout} />
        <div className="account-content">
          <div className="loading">사용자 정보를 불러오는 중...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="account-root">
      <Header username={username} onLogout={onLogout} />
      <div className="account-content">
        <div className="account-center">
          <div className="service-card">
            <div className="card-header">
              <h2>사용자 정보</h2>
              {!editing && (
                <button className="edit-btn" onClick={toggleEdit}>
                  수정
                </button>
              )}
            </div>

            <div className="user-info">
              <div className="info-item">
                <label>이름</label>
                {editing ? (
                  <input
                    type="text"
                    name="username"
                    value={formData.username}
                    onChange={handleInputChange}
                    placeholder="이름을 입력하세요"
                  />
                ) : (
                  <span>{user.username || '설정되지 않음'}</span>
                )}
              </div>

              <div className="info-item">
                <label>이메일</label>
                <span className="readonly">{user.email}</span>
                <small className="readonly-note">이메일은 수정할 수 없습니다</small>
              </div>

              <div className="info-item">
                <label>비밀번호</label>
                {editing ? (
                  <input
                    type="password"
                    name="password"
                    value={formData.password}
                    onChange={handleInputChange}
                    placeholder="새 비밀번호를 입력하세요 (변경하지 않으려면 비워두세요)"
                  />
                ) : (
                  <span>••••••••</span>
                )}
              </div>

              <div className="info-item">
                <label>핸드폰 번호</label>
                {editing ? (
                  <input
                    type="tel"
                    name="phoneNumber"
                    value={formData.phoneNumber}
                    onChange={handleInputChange}
                    placeholder="핸드폰 번호를 입력하세요"
                  />
                ) : (
                  <span>{user.phoneNumber || '설정되지 않음'}</span>
                )}
              </div>
            </div>

            {editing && (
              <div className="action-buttons">
                <button className="save-btn" onClick={handleUpdate}>
                  저장
                </button>
                <button className="cancel-btn" onClick={handleCancel}>
                  취소
                </button>
              </div>
            )}

            {message && <div className="success-message">{message}</div>}
            {error && <div className="error-message">{error}</div>}
          </div>
        </div>
      </div>
    </div>
  );
}

export default AccountPage;
