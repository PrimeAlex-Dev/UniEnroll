import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { ShieldAlert } from 'lucide-react';
import Button from '../common/Button';

const RoleProtectedRoute = ({ allowedRoles }) => {
  const { user, isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50">
        <div className="flex flex-col items-center space-y-3">
          <div className="w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin" />
          <p className="text-xs text-slate-500 font-medium">Đang xác thực phiên làm việc...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  const rawRole = user?.role || '';
  const userRole = rawRole.replace(/^ROLE_/, '');
  const normalizedAllowed = allowedRoles?.map((r) => r.replace(/^ROLE_/, '')) || [];
  
  const isAllowed =
    !allowedRoles ||
    normalizedAllowed.includes(userRole) ||
    normalizedAllowed.includes(rawRole) ||
    ((userRole === 'STAFF' || rawRole === 'ROLE_STAFF') && normalizedAllowed.includes('ACADEMIC_STAFF')) ||
    ((userRole === 'ACADEMIC_STAFF' || rawRole === 'ROLE_ACADEMIC_STAFF') && normalizedAllowed.includes('STAFF'));

  if (!isAllowed) {
    return (
      <div className="p-8 max-w-lg mx-auto my-16 text-center bg-white rounded-xl border border-rose-200 shadow-sm">
        <div className="w-12 h-12 mx-auto rounded-full bg-rose-100 text-rose-600 flex items-center justify-center mb-4">
          <ShieldAlert className="w-6 h-6" />
        </div>
        <h3 className="text-lg font-bold text-slate-800">Không có quyền truy cập (403 Forbidden)</h3>
        <p className="text-xs text-slate-500 mt-2">
          Tài khoản của bạn thuộc vai trò <strong className="font-mono text-indigo-600">{user?.role}</strong>, không được cấp quyền xem chức năng này.
        </p>
        <div className="mt-6 flex justify-center">
          <Button
            variant="secondary"
            size="sm"
            onClick={() => window.history.back()}
          >
            Quay lại trang trước
          </Button>
        </div>
      </div>
    );
  }

  return <Outlet />;
};

export default RoleProtectedRoute;
