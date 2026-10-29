import axiosClient from './axiosClient';

export const adminApi = {
  getUsers: (params) => axiosClient.get('/admin/users', { params }),
  createUser: (data) => axiosClient.post('/admin/users', data),
  updateUserStatus: (id, status) => axiosClient.put(`/admin/users/${id}/status`, { status }),
  getAuditLogs: (params) => axiosClient.get('/admin/audit-logs', { params }),
};
