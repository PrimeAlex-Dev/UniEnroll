import axiosClient from './axiosClient';

export const authApi = {
  login: (data) => axiosClient.post('/auth/login', data),
  refresh: (refreshToken) => axiosClient.post('/auth/refresh', { refreshToken }),
  getCurrentUser: () => axiosClient.get('/users/me'),
};
