import React, { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../api/authApi';

const AuthContext = createContext(null);

export const normalizeUser = (rawUser) => {
  if (!rawUser) return null;
  const rawRole = rawUser.role || '';
  let role = rawRole;
  if (rawRole === 'ROLE_STUDENT' || rawRole === 'STUDENT') role = 'STUDENT';
  else if (rawRole === 'ROLE_LECTURER' || rawRole === 'LECTURER') role = 'LECTURER';
  else if (rawRole === 'ROLE_STAFF' || rawRole === 'STAFF' || rawRole === 'ROLE_ACADEMIC_STAFF' || rawRole === 'ACADEMIC_STAFF') role = 'ACADEMIC_STAFF';
  else if (rawRole === 'ROLE_ADMIN' || rawRole === 'ADMIN') role = 'ADMIN';

  const fullName = rawUser.profileDetails?.fullName || rawUser.fullName || rawUser.username;

  return {
    ...rawUser,
    role,
    fullName,
    studentProfile: role === 'STUDENT' ? (rawUser.studentProfile || rawUser.profileDetails) : null,
    lecturerProfile: role === 'LECTURER' ? (rawUser.lecturerProfile || rawUser.profileDetails) : null,
    staffProfile: role === 'ACADEMIC_STAFF' ? (rawUser.staffProfile || rawUser.profileDetails) : null,
  };
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem('user');
    return savedUser ? normalizeUser(JSON.parse(savedUser)) : null;
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initAuth = async () => {
      const token = localStorage.getItem('accessToken');
      if (token) {
        try {
          const res = await authApi.getCurrentUser();
          if (res?.data) {
            const normalized = normalizeUser(res.data);
            setUser(normalized);
            localStorage.setItem('user', JSON.stringify(normalized));
          }
        } catch (error) {
          console.error('Session restore failed:', error);
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          localStorage.removeItem('user');
          setUser(null);
        }
      }
      setLoading(false);
    };

    initAuth();
  }, []);

  const login = async (username, password) => {
    const res = await authApi.login({ username, password });
    if (res?.data) {
      const { accessToken, refreshToken, user: authUser } = res.data;
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);

      // Fetch comprehensive profile (/users/me)
      try {
        const profileRes = await authApi.getCurrentUser();
        const fullUser = profileRes?.data || authUser;
        const normalized = normalizeUser(fullUser);
        setUser(normalized);
        localStorage.setItem('user', JSON.stringify(normalized));
        return normalized;
      } catch (err) {
        const normalized = normalizeUser(authUser);
        setUser(normalized);
        localStorage.setItem('user', JSON.stringify(normalized));
        return normalized;
      }
    }
  };

  const logout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    setUser(null);
  };

  const hasRole = (roles) => {
    if (!user || !user.role) return false;
    if (Array.isArray(roles)) {
      return roles.includes(user.role);
    }
    return user.role === roles;
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        isAuthenticated: !!user,
        login,
        logout,
        hasRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
