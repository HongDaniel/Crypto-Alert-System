// src/routes/PublicRoute.js
import { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import axios from 'axios';

function PublicRoute({ children }) {
  const [isAuth, setIsAuth] = useState(false);
  const [checking, setChecking] = useState(true);

  useEffect(() => {
    axios.get('/api/auth/me', { withCredentials: true })
      .then(() => setIsAuth(true))
      .catch(() => setIsAuth(false))
      .finally(() => setChecking(false));
  }, []);

  if (checking) return null; // 로딩 중

  return isAuth ? <Navigate to="/" /> : children;
}

export default PublicRoute;
