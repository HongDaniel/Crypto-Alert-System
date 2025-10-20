import React, { useState } from 'react';
import axios from 'axios';
import Popup from '../components/Popup'; // Import the Popup component
import '../css/ActiveAlertList.css';

function ActiveAlertList({ alerts, fetchAlerts }) {
  const [editingId, setEditingId] = useState(null);
  const [isChangePopupVisible, setChangePopupVisible] = useState(false);
  const [isDeletePopupVisible, setDeletePopupVisible] = useState(false);
  const [alertToDelete, setAlertToDelete] = useState(null);
  const [isExecuting, setIsExecuting] = useState(false);
  const [editedAlerts, setEditedAlerts] = useState({});

  // Toggle editing mode for an alert
  const toggleEdit = (id, alert) => {
    setEditingId(editingId === id ? null : id);
    setEditedAlerts({
      ...editedAlerts,
      [id]: {
        threshold: alert.threshold,
        alertType: alert.alertType,
        email: alert.email, // methods.email 대신 email 직접 사용
        sms: alert.sms,     // methods.sms 대신 sms 직접 사용
      }
    });
  };

  // Handle method change (email, sms)
  const handleMethodChange = (method, alertId) => {
    setEditedAlerts((prev) => ({
      ...prev,
      [alertId]: {
        ...prev[alertId],
        [method]: !prev[alertId][method],
      }
    }));
  };

  // Handle input change for threshold and alertType
  const handleInputChange = (e, alertId) => {
    const { name, value } = e.target;
    setEditedAlerts((prev) => ({
      ...prev,
      [alertId]: {
        ...prev[alertId],
        [name]: value,
      }
    }));
  };

  // Handle alert deletion
  const handleDelete = async () => {
    try {
      await axios.delete(`/api/alerts/${alertToDelete}`, { withCredentials: true });
      setDeletePopupVisible(false); // Hide the popup
      alert('알림이 정상적으로 삭제되었습니다.');
      fetchAlerts(); // Fetch the alerts again to refresh the list
    } catch (error) {
      console.error('Error deleting alert:', error);
      alert('Failed to delete alert');
    }
  };

  // Show the delete confirmation popup
  const showDeletePopup = (id) => {
    setAlertToDelete(id);
    setDeletePopupVisible(true);
  };

  // Show the change confirmation popup
  const showChangePopup = () => {
    setChangePopupVisible(true);
  };

  // Handle the cancel action on the popup
  const handleCancel = () => {
    setDeletePopupVisible(false); // Hide the delete popup
    setChangePopupVisible(false); // Hide the change popup
    setAlertToDelete(null); // Reset the alert to delete
  };

  // Handle the alert update
  const handleUpdate = async () => {
    try {
      const editedAlert = editedAlerts[editingId];
      const updatedAlert = {
        threshold: parseInt(editedAlert.threshold, 10),
        alertType: editedAlert.alertType,
        email: editedAlert.email,
        sms: editedAlert.sms,
      };
      await axios.put(`/api/alerts/${editingId}`, updatedAlert, { withCredentials: true });
      fetchAlerts(); // Refresh the alerts after update
      setEditingId(null); // Exit editing mode
      setChangePopupVisible(false); // Hide the change popup
      console.log('Alert updated successfully');
    } catch (error) {
      console.error('Error updating alert:', error);
    }
  };

  // Handle manual alert execution - 모든 Active Alert 확인
  const handleManualExecute = async () => {
    if (alerts.length === 0) {
      alert('실행할 활성 알림이 없습니다.');
      return;
    }

    setIsExecuting(true);
    
    try {
      // 모든 Active Alert를 확인하는 API 호출 (payload 없이)
      const response = await axios.post('/api/alert/send', {}, {
        withCredentials: true
      });
      
      // 응답 메시지 표시
      const message = response.data || '알림이 수동으로 실행되었습니다!';
      alert(message);
      
      // Alert History 새로고침을 위해 부모 컴포넌트에 알림
      if (typeof fetchAlerts === 'function') {
        fetchAlerts();
      }
    } catch (error) {
      console.error('❌ 수동 알림 실행 실패:', error.response?.data || error.message);
      const errorMessage = error.response?.data || '알림 실행에 실패했습니다.';
      alert(errorMessage);
    } finally {
      setIsExecuting(false);
    }
  };

  return (
    <div className="active-alert-list">
      <div className="alert-header">
        <h2>Active Alerts</h2>
        <button 
          className="execute-alert-btn"
          onClick={handleManualExecute}
          disabled={isExecuting || alerts.length === 0}
        >
          {isExecuting ? 'Executing...' : 'Execute Alert Now'}
        </button>
      </div>
      {alerts.map((alert) => {
        const isEditing = editingId === alert.id;
        const editedAlert = editedAlerts[alert.id] || alert;
        const statusText =
          alert.alertType === 'ABOVE'
            ? `Index ≥ ${alert.threshold}`
            : `Index ≤ ${alert.threshold}`;

        return (
          <div key={alert.id} className="alert-row">
            <div className="alert-info">
              <div className={`alert-condition ${alert.alertType.toLowerCase()}`}>
                {statusText}
              </div>
              {isEditing && (
                <div className="alert-inputs">
                  <input
                    type="number"
                    name="threshold"
                    value={editedAlert.threshold}
                    onChange={(e) => handleInputChange(e, alert.id)}
                    min="0"
                    max="100"
                  />
                  <select
                    name="alertType"
                    value={editedAlert.alertType}
                    onChange={(e) => handleInputChange(e, alert.id)}
                  >
                    <option value="ABOVE">Above</option>
                    <option value="BELOW">Below</option>
                  </select>
                </div>
              )}
              <div className="alert-methods">
                <label>
                  <input
                    type="checkbox"
                    checked={isEditing ? editedAlert.email : alert.email}
                    disabled={!isEditing}
                    onChange={() => handleMethodChange('email', alert.id)}
                  />
                  Email
                </label>
                <label>
                  <input
                    type="checkbox"
                    checked={isEditing ? editedAlert.sms : alert.sms}
                    disabled={!isEditing}
                    onChange={() => handleMethodChange('sms', alert.id)}
                  />
                  SMS
                </label>
              </div>
            </div>
            <div className="alert-actions">
              <button onClick={() => (isEditing ? showChangePopup() : toggleEdit(alert.id, alert))}>
                {isEditing ? 'Save' : 'Change'}
              </button>
              <button className="delete-btn" onClick={() => showDeletePopup(alert.id)}>
                Delete
              </button>
            </div>
          </div>
        );
      })}

      {/* 팝업이 활성화되면 표시 */}
      {isDeletePopupVisible && (
        <Popup
          message="알림을 삭제하시겠습니까?"
          onConfirm={handleDelete} // 삭제 실행
          onCancel={handleCancel}  // 취소 시 팝업 숨기기
        />
      )}
      {isChangePopupVisible && (
        <Popup
          message="알림을 수정하시겠습니까?"
          onConfirm={handleUpdate} // 수정 실행
          onCancel={handleCancel}  // 취소 시 팝업 숨기기
        />
      )}
    </div>
  );
}

export default ActiveAlertList;
