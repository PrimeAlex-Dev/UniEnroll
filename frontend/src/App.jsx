import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';

// Layouts & Guards
import AppLayout from './components/layout/AppLayout';
import RoleProtectedRoute from './components/layout/RoleProtectedRoute';

// Pages
import LoginPage from './pages/auth/LoginPage';
import UserManagementPage from './pages/admin/UserManagementPage';
import AuditLogsPage from './pages/admin/AuditLogsPage';
import DepartmentsPage from './pages/staff/DepartmentsPage';
import CoursesPage from './pages/staff/CoursesPage';
import InfrastructurePage from './pages/staff/InfrastructurePage';
import SectionsPage from './pages/staff/SectionsPage';
import PhasesPage from './pages/staff/PhasesPage';
import GradesPage from './pages/staff/GradesPage';
import OccupancyReportPage from './pages/staff/OccupancyReportPage';
import RegistrationStudioPage from './pages/student/RegistrationStudioPage';
import TimetablePage from './pages/student/TimetablePage';
import ClassRosterPage from './pages/lecturer/ClassRosterPage';

// Smart Home Redirector
const HomeRedirect = () => {
  const { user, isAuthenticated, loading } = useAuth();

  if (loading) return null;
  if (!isAuthenticated) return <Navigate to="/login" replace />;

  switch (user?.role) {
    case 'STUDENT':
      return <Navigate to="/student/registration" replace />;
    case 'LECTURER':
      return <Navigate to="/lecturer/roster" replace />;
    case 'ACADEMIC_STAFF':
      return <Navigate to="/staff/sections" replace />;
    case 'ADMIN':
      return <Navigate to="/admin/users" replace />;
    default:
      return <Navigate to="/login" replace />;
  }
};

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public Authentication Route */}
          <Route path="/login" element={<LoginPage />} />

          {/* Root Smart Redirect */}
          <Route path="/" element={<HomeRedirect />} />

          {/* Protected Application Layout */}
          <Route element={<AppLayout />}>
            {/* STUDENT ONLY ROUTES */}
            <Route element={<RoleProtectedRoute allowedRoles={['STUDENT']} />}>
              <Route path="/student/registration" element={<RegistrationStudioPage />} />
              <Route path="/student/timetable" element={<TimetablePage />} />
            </Route>

            {/* LECTURER ONLY ROUTES */}
            <Route element={<RoleProtectedRoute allowedRoles={['LECTURER']} />}>
              <Route path="/lecturer/roster" element={<ClassRosterPage />} />
            </Route>

            {/* ACADEMIC STAFF ONLY ROUTES (UC-06 đến UC-12, UC-18) */}
            <Route element={<RoleProtectedRoute allowedRoles={['ACADEMIC_STAFF']} />}>
              <Route path="/staff/sections" element={<SectionsPage />} />
              <Route path="/staff/phases" element={<PhasesPage />} />
              <Route path="/staff/courses" element={<CoursesPage />} />
              <Route path="/staff/infrastructure" element={<InfrastructurePage />} />
              <Route path="/staff/grades" element={<GradesPage />} />
              <Route path="/staff/occupancy-report" element={<OccupancyReportPage />} />
            </Route>

            {/* SHARED UC-05: KHOA & BỘ MÔN (Academic Staff & Admin) */}
            <Route element={<RoleProtectedRoute allowedRoles={['ACADEMIC_STAFF', 'ADMIN']} />}>
              <Route path="/staff/departments" element={<DepartmentsPage />} />
            </Route>

            {/* SHARED UC-19: NHẬT KÝ KIỂM TOÁN (Admin & Academic Staff) */}
            <Route element={<RoleProtectedRoute allowedRoles={['ADMIN', 'ACADEMIC_STAFF']} />}>
              <Route path="/admin/audit-logs" element={<AuditLogsPage />} />
            </Route>

            {/* ADMIN EXCLUSIVE ROUTE (UC-04: Quản lý Người dùng) */}
            <Route element={<RoleProtectedRoute allowedRoles={['ADMIN']} />}>
              <Route path="/admin/users" element={<UserManagementPage />} />
            </Route>
          </Route>

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
