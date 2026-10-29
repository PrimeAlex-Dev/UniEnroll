import axiosClient from './axiosClient';

export const curriculumApi = {
  getDepartments: () => axiosClient.get('/curriculum/departments'),
  createDepartment: (data) => axiosClient.post('/curriculum/departments', data),
  getCourses: () => axiosClient.get('/curriculum/courses'),
  createCourse: (data) => axiosClient.post('/curriculum/courses', data),
  getPrerequisites: (courseId) => axiosClient.get(`/curriculum/courses/${courseId}/prerequisites`),
  addPrerequisite: (courseId, data) => axiosClient.post(`/curriculum/courses/${courseId}/prerequisites`, data),
  removePrerequisite: (courseId, prereqId) => axiosClient.delete(`/curriculum/courses/${courseId}/prerequisites/${prereqId}`),
};
