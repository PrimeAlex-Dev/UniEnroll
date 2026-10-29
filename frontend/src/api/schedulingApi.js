import axiosClient from './axiosClient';

export const schedulingApi = {
  getSemesters: () => axiosClient.get('/academic/semesters'),
  createSemester: (data) => axiosClient.post('/academic/semesters', data),
  getRooms: () => axiosClient.get('/academic/rooms'),
  createRoom: (data) => axiosClient.post('/academic/rooms', data),
  getSections: (semesterId) => axiosClient.get('/academic/sections', { params: { semesterId } }),
  createSection: (data) => axiosClient.post('/academic/sections', data),
  addScheduleSlot: (sectionId, data) => axiosClient.post(`/academic/sections/${sectionId}/slots`, data),
  updateCapacity: (sectionId, data) => axiosClient.put(`/academic/sections/${sectionId}/capacity`, data),
  cancelSection: (sectionId) => axiosClient.put(`/academic/sections/${sectionId}/cancel`),
  saveGrade: (data) => axiosClient.post('/academic/grades', data),
};
