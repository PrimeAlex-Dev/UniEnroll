import axiosClient from './axiosClient';

export const registrationApi = {
  getPhases: (semesterId) => axiosClient.get('/academic/phases', { params: { semesterId } }),
  createPhase: (data) => axiosClient.post('/academic/phases', data),
  updatePhase: (id, data) => axiosClient.put(`/academic/phases/${id}`, data),
  closePhaseNow: (id) => axiosClient.put(`/academic/phases/${id}/close`),
  deletePhase: (id) => axiosClient.delete(`/academic/phases/${id}`),
  getCatalogSections: (params) => axiosClient.get('/catalog/sections', { params }),
  enroll: (data) => axiosClient.post('/registration/enroll', data),
  drop: (data) => axiosClient.post('/registration/drop', data),
  getMySchedule: (semesterId) => axiosClient.get('/registration/my-schedule', { params: { semesterId } }),
  getMyTranscript: () => axiosClient.get('/student/grades/my-transcript'),
  getSectionRoster: (sectionId) => axiosClient.get(`/student/registrations/sections/${sectionId}/roster`),
};
