// src/components/Popup.jsx
import React from 'react';
import '../css/Popup.css'; // Assuming you have a CSS file for styling

function Popup({ message, onConfirm, onCancel }) {
  return (
    <div className="popup-overlay">
      <div className="popup">
        <h3>{message}</h3>
        <div className="popup-actions">
          <button onClick={onConfirm} className="confirm-btn">확인</button>
          <button onClick={onCancel} className="cancel-btn">취소</button>
        </div>
      </div>
    </div>
  );
}

export default Popup;
