import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import Badge from '../common/Badge';
import { LogOut, User as UserIcon, Shield, GraduationCap, BookOpen, Briefcase } from 'lucide-react';
import Modal from '../common/Modal';

const getRoleIcon = (role) => {
  switch (role) {
    case 'ADMIN':
      return Shield;
    case 'ACADEMIC_STAFF':
      return Briefcase;
    case 'LECTURER':
      return BookOpen;
    case 'STUDENT':
      return GraduationCap;
    default:
      return UserIcon;
  }
};

const Header = () => {
  const { user, logout } = useAuth();
  const [showProfileModal, setShowProfileModal] = useState(false);

  const RoleIcon = getRoleIcon(user?.role);

  return (
    <>
      <header className="h-16 bg-white border-b border-slate-200/80 px-6 flex items-center justify-between sticky top-0 z-30 shadow-2xs">
        <div className="flex items-center space-x-3">
          <div className="hidden sm:block">
            <h2 className="text-sm font-semibold text-slate-700">
              Cổng Đăng ký Học phần Trực tuyến UniEnroll
            </h2>
            <p className="text-xs text-slate-400">Hệ thống quản lý đào tạo & tín chỉ thời gian thực</p>
          </div>
        </div>

        <div className="flex items-center space-x-4">
          {/* User info trigger */}
          <button
            onClick={() => setShowProfileModal(true)}
            className="flex items-center space-x-3 px-3 py-1.5 rounded-lg hover:bg-slate-50 border border-slate-200 transition-all text-left"
          >
            <div className="w-8 h-8 rounded-full bg-indigo-50 border border-indigo-200 flex items-center justify-center text-indigo-700 font-bold text-xs">
              <RoleIcon className="w-4 h-4" />
            </div>
            <div className="hidden md:block">
              <div className="text-xs font-semibold text-slate-800 flex items-center space-x-1.5">
                <span>{user?.fullName || user?.username}</span>
              </div>
              <div className="text-[11px] text-slate-400 font-mono">@{user?.username}</div>
            </div>
            <Badge variant={user?.role} className="ml-1 text-[10px]">
              {user?.role}
            </Badge>
          </button>

          {/* Logout Button */}
          <button
            onClick={logout}
            className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-medium text-slate-600 hover:text-rose-600 hover:bg-rose-50 border border-transparent hover:border-rose-200 transition-all"
            title="Đăng xuất"
          >
            <LogOut className="w-4 h-4" />
            <span className="hidden sm:inline">Thoát</span>
          </button>
        </div>
      </header>

      {/* User Profile Modal (UC-03 /users/me viewer) */}
      <Modal
        isOpen={showProfileModal}
        onClose={() => setShowProfileModal(false)}
        title="Thông tin Tài khoản & Hồ sơ Cá nhân (UC-03)"
      >
        <div className="space-y-4">
          <div className="flex items-center space-x-4 p-4 rounded-xl bg-slate-50 border border-slate-200">
            <div className="w-12 h-12 rounded-full bg-indigo-600 text-white flex items-center justify-center font-bold text-lg">
              <RoleIcon className="w-6 h-6" />
            </div>
            <div>
              <h4 className="font-bold text-slate-900 text-base">{user?.fullName}</h4>
              <p className="text-xs text-slate-500 font-mono">ID: {user?.id} • Username: {user?.username}</p>
              <div className="mt-1 flex items-center space-x-2">
                <Badge variant={user?.role}>{user?.role}</Badge>
                <Badge variant={user?.status}>{user?.status}</Badge>
              </div>
            </div>
          </div>

          <div className="space-y-2 text-sm">
            <div className="flex justify-between py-2 border-b border-slate-100">
              <span className="text-slate-500">Email:</span>
              <span className="font-medium text-slate-800">{user?.email || 'Chưa cập nhật'}</span>
            </div>

            {/* Profile specific details */}
            {user?.studentProfile && (
              <>
                <div className="flex justify-between py-2 border-b border-slate-100">
                  <span className="text-slate-500">Mã sinh viên:</span>
                  <span className="font-mono font-bold text-indigo-700">{user.studentProfile.studentCode}</span>
                </div>
                <div className="flex justify-between py-2 border-b border-slate-100">
                  <span className="text-slate-500">Chuyên ngành:</span>
                  <span className="font-medium text-slate-800">{user.studentProfile.major || 'Công nghệ thông tin'}</span>
                </div>
                <div className="flex justify-between py-2 border-b border-slate-100">
                  <span className="text-slate-500">Điểm TB tích lũy (GPA):</span>
                  <span className="font-bold text-emerald-600">{user.studentProfile.gpa ?? 'N/A'}</span>
                </div>
              </>
            )}

            {user?.lecturerProfile && (
              <>
                <div className="flex justify-between py-2 border-b border-slate-100">
                  <span className="text-slate-500">Mã giảng viên:</span>
                  <span className="font-mono font-bold text-purple-700">{user.lecturerProfile.lecturerCode}</span>
                </div>
                <div className="flex justify-between py-2 border-b border-slate-100">
                  <span className="text-slate-500">Học hàm / Học vị:</span>
                  <span className="font-medium text-slate-800">{user.lecturerProfile.academicTitle || 'Tiến sĩ / Giảng viên chính'}</span>
                </div>
              </>
            )}

            {user?.staffProfile && (
              <div className="flex justify-between py-2 border-b border-slate-100">
                <span className="text-slate-500">Mã nhân viên đào tạo:</span>
                <span className="font-mono font-bold text-indigo-700">{user.staffProfile.staffCode}</span>
              </div>
            )}
          </div>
        </div>
      </Modal>
    </>
  );
};

export default Header;
