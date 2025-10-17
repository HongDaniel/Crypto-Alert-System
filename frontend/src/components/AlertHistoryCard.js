
// src/components/AlertHistory.jsx
import React, { useState, useEffect } from 'react';
import axios from 'axios';
import '../css/AlertHistory.css';

function AlertHistoryCard() {
  const [historyData, setHistoryData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedHistory, setSelectedHistory] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [modalLoading, setModalLoading] = useState(false);

  useEffect(() => {
    fetchAlertHistory();
  }, []);

  const fetchAlertHistory = async () => {
    try {
      setLoading(true);
      const response = await axios.get('/api/alert-history/recent', { withCredentials: true });
      setHistoryData(response.data);
      setError(null);
    } catch (err) {
      console.error('Error fetching alert history:', err);
      // API 오류인 경우에만 에러 메시지 표시
      if (err.response && err.response.status >= 400) {
        setError('Failed to load alert history');
      } else {
        setError(null); // 네트워크 오류 등은 무시하고 빈 데이터로 처리
      }
      setHistoryData([]);
    } finally {
      setLoading(false);
    }
  };

  const formatDateTime = (dateTimeString) => {
    const date = new Date(dateTimeString);
    return date.toLocaleString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getStatusText = (sentEmail, sentSms) => {
    const channels = [];
    if (sentEmail) channels.push('Email');
    if (sentSms) channels.push('SMS');
    return channels.length > 0 ? `Sent via ${channels.join(', ')}` : 'Failed';
  };

  const getStatusClass = (sentEmail, sentSms) => {
    return (sentEmail || sentSms) ? 'status-success' : 'status-failed';
  };

  const handleHistoryClick = async (historyId) => {
    try {
      setModalLoading(true);
      const response = await axios.get(`/api/alert-history/${historyId}`, { withCredentials: true });
      setSelectedHistory(response.data);
      setShowModal(true);
    } catch (err) {
      console.error('Error fetching alert history detail:', err);
      alert('알림 상세 정보를 불러오는데 실패했습니다.');
    } finally {
      setModalLoading(false);
    }
  };

  const closeModal = () => {
    setShowModal(false);
    setSelectedHistory(null);
  };

  const formatContent = (content) => {
    if (!content) return '내용 없음';
    // HTML 태그 제거하고 텍스트만 표시
    return content.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ').trim();
  };

  if (loading) {
    return (
      <section className="alert-history">
        <h2>Alert History</h2>
        <div className="loading">Loading...</div>
      </section>
    );
  }

  if (error) {
    return (
      <section className="alert-history">
        <h2>Alert History</h2>
        <div className="error">{error}</div>
        <button onClick={fetchAlertHistory} className="retry-btn">Retry</button>
      </section>
    );
  }

  return (
    <>
      <section className="alert-history">
        <h2>Alert History</h2>
        {historyData.length === 0 ? (
          <div className="no-data">
            {error ? 'Failed to load alert history' : 'No alerts activated'}
          </div>
        ) : (
          <ul>
            {historyData.map((item) => (
              <li key={item.id} className="history-item" onClick={() => handleHistoryClick(item.id)}>
                <div className="index-info">Index reached {item.triggeredIndex}</div>
                <div className="time-info">{formatDateTime(item.triggeredAt)}</div>
                <div className={`status-info ${getStatusClass(item.sentEmail, item.sentSms)}`}>
                  {getStatusText(item.sentEmail, item.sentSms)}
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>

      {/* 알림 상세 모달 */}
      {showModal && (
        <div className="modal-overlay" onClick={closeModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>알림 상세 정보</h3>
              <button className="modal-close" onClick={closeModal}>×</button>
            </div>
            <div className="modal-body">
              {modalLoading ? (
                <div className="modal-loading">로딩 중...</div>
              ) : selectedHistory ? (
                <div className="alert-detail">
                  <div className="detail-section">
                    <h4>기본 정보</h4>
                    <p><strong>발동 지수:</strong> {selectedHistory.triggeredIndex}</p>
                    <p><strong>발동 시간:</strong> {formatDateTime(selectedHistory.triggeredAt)}</p>
                    <p><strong>발송 상태:</strong> {getStatusText(selectedHistory.sentEmail, selectedHistory.sentSms)}</p>
                  </div>

                  {selectedHistory.sentEmail && selectedHistory.emailSubject && (
                    <div className="detail-section">
                      <h4>이메일 내용</h4>
                      <p><strong>제목:</strong> {selectedHistory.emailSubject}</p>
                      <div className="content-box">
                        <pre>{formatContent(selectedHistory.emailContent)}</pre>
                      </div>
                    </div>
                  )}

                  {selectedHistory.sentSms && selectedHistory.smsContent && (
                    <div className="detail-section">
                      <h4>SMS 내용</h4>
                      <div className="content-box">
                        <pre>{selectedHistory.smsContent}</pre>
                      </div>
                    </div>
                  )}

                  {!selectedHistory.sentEmail && !selectedHistory.sentSms && (
                    <div className="detail-section">
                      <p className="no-content">발송된 메시지가 없습니다.</p>
                    </div>
                  )}
                </div>
              ) : (
                <div className="modal-error">데이터를 불러올 수 없습니다.</div>
              )}
            </div>
          </div>
        </div>
      )}
    </>
  );
}

export default AlertHistoryCard;
