import axiosClient from './axiosClient';

export const registrationApi = {
  getPhases: (semesterId) => axiosClient.get('/academic/phases', { params: { semesterId } }),
  createPhase: (data) => axiosClient.post('/academic/phases', data),
  getCatalogSections: (params) => axiosClient.get('/catalog/sections', { params }),
  enroll: (data) => axiosClient.post('/registration/enroll', data),
  drop: (data) => axiosClient.post('/registration/drop', data),
  getMySchedule: (semesterId) => axiosClient.get('/registration/my-schedule', { params: { semesterId } }),
};
