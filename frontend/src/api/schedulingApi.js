import axiosClient from './axiosClient';

export const schedulingApi = {
  // Học kỳ
  getSemesters: () => axiosClient.get('/academic/semesters'),
  createSemester: (data) => axiosClient.post('/academic/semesters', data),
  updateSemester: (id, data) => axiosClient.put(`/academic/semesters/${id}`, data),
  updateSemesterStatus: (id, status) => axiosClient.patch(`/academic/semesters/${id}/status`, null, { params: { status } }),
  deleteSemester: (id) => axiosClient.delete(`/academic/semesters/${id}`),

  // Phòng học
  getRooms: () => axiosClient.get('/academic/rooms'),
  createRoom: (data) => axiosClient.post('/academic/rooms', data),
  updateRoom: (id, data) => axiosClient.put(`/academic/rooms/${id}`, data),
  deleteRoom: (id) => axiosClient.delete(`/academic/rooms/${id}`),

  // Lớp học phần
  getSections: (semesterId) => axiosClient.get('/academic/sections', { params: { semesterId } }),
  createSection: (data) => axiosClient.post('/academic/sections', data),
  updateSection: (id, data) => axiosClient.put(`/academic/sections/${id}`, data),
  deleteSection: (id) => axiosClient.delete(`/academic/sections/${id}`),
  addScheduleSlot: (sectionId, data) => axiosClient.post(`/academic/sections/${sectionId}/slots`, data),
  deleteScheduleSlot: (sectionId, slotId) => axiosClient.delete(`/academic/sections/${sectionId}/slots/${slotId}`),
  updateCapacity: (sectionId, data) => axiosClient.put(`/academic/sections/${sectionId}/capacity`, data),
  cancelSection: (sectionId) => axiosClient.put(`/academic/sections/${sectionId}/cancel`),

  // Điểm số
  saveGrade: (data) => axiosClient.post('/academic/grades', data),
};
