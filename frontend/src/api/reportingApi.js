import axiosClient from './axiosClient';

export const reportingApi = {
  getLecturerRoster: (sectionId) => axiosClient.get(`/lecturer/sections/${sectionId}/roster`),
  getOccupancyReport: (semesterId) => axiosClient.get('/reports/occupancy', { params: { semesterId } }),
};
