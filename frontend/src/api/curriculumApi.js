import axiosClient from './axiosClient';

export const curriculumApi = {
  getDepartments: () => axiosClient.get('/curriculum/departments'),
  createDepartment: (data) => axiosClient.post('/curriculum/departments', data),
  updateDepartment: (id, data) => axiosClient.put(`/curriculum/departments/${id}`, data),
  deleteDepartment: (id) => axiosClient.delete(`/curriculum/departments/${id}`),
  getCourses: () => axiosClient.get('/curriculum/courses'),
  createCourse: (data) => axiosClient.post('/curriculum/courses', data),
  updateCourse: (id, data) => axiosClient.put(`/curriculum/courses/${id}`, data),
  deleteCourse: (id) => axiosClient.delete(`/curriculum/courses/${id}`),
  getPrerequisites: (courseId) => axiosClient.get(`/curriculum/courses/${courseId}/prerequisites`),
  addPrerequisite: (courseId, data) => axiosClient.post(`/curriculum/courses/${courseId}/prerequisites`, data),
  removePrerequisite: (courseId, prereqId) => axiosClient.delete(`/curriculum/courses/${courseId}/prerequisites/${prereqId}`),
};
