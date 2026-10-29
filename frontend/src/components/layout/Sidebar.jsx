import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  GraduationCap,
  Calendar,
  BookOpen,
  Users,
  Building,
  Layers,
  FileSpreadsheet,
  BarChart3,
  ShieldAlert,
  Award,
} from 'lucide-react';

const Sidebar = () => {
  const { user } = useAuth();
  const role = user?.role;

  const navItems = [
    // STUDENT NAV ITEMS
    {
      label: 'Đăng ký Học phần',
      path: '/student/registration',
      icon: GraduationCap,
      roles: ['STUDENT'],
      badge: 'Chính',
    },
    {
      label: 'Thời khóa biểu Tuần',
      path: '/student/timetable',
      icon: Calendar,
      roles: ['STUDENT'],
    },

    // LECTURER NAV ITEMS
    {
      label: 'Danh sách Điểm danh Lớp',
      path: '/lecturer/roster',
      icon: Users,
      roles: ['LECTURER'],
    },

    // ACADEMIC STAFF ONLY NAV ITEMS (Chỉ dành riêng cho Cán bộ Đào tạo - UC-06 đến UC-12, UC-18)
    {
      label: 'Quản lý Lớp học phần',
      path: '/staff/sections',
      icon: Layers,
      roles: ['ACADEMIC_STAFF'],
    },
    {
      label: 'Đợt Đăng ký Học phần',
      path: '/staff/phases',
      icon: Calendar,
      roles: ['ACADEMIC_STAFF'],
    },
    {
      label: 'Môn học & Tiên quyết',
      path: '/staff/courses',
      icon: BookOpen,
      roles: ['ACADEMIC_STAFF'],
    },
    {
      label: 'Học kỳ & Phòng học',
      path: '/staff/infrastructure',
      icon: FileSpreadsheet,
      roles: ['ACADEMIC_STAFF'],
    },
    {
      label: 'Tiện ích Nhập điểm',
      path: '/staff/grades',
      icon: Award,
      roles: ['ACADEMIC_STAFF'],
      badge: 'Test Data',
    },
    {
      label: 'Báo cáo Tỷ lệ Lấp đầy',
      path: '/staff/occupancy-report',
      icon: BarChart3,
      roles: ['ACADEMIC_STAFF'],
    },

    // SHARED UC-05: KHOA & BỘ MÔN (Academic Staff & Admin)
    {
      label: 'Khoa & Bộ môn',
      path: '/staff/departments',
      icon: Building,
      roles: ['ACADEMIC_STAFF', 'ADMIN'],
    },

    // ADMIN EXCLUSIVE (UC-04) & SHARED AUDIT (UC-19)
    {
      label: 'Quản lý Người dùng',
      path: '/admin/users',
      icon: Users,
      roles: ['ADMIN'],
    },
    {
      label: 'Nhật ký Kiểm toán (Audit)',
      path: '/admin/audit-logs',
      icon: ShieldAlert,
      roles: ['ADMIN', 'ACADEMIC_STAFF'],
      badge: 'Bất biến',
    },
  ];

  const userRole = (user?.role || '').replace(/^ROLE_/, '');
  const filteredNav = navItems.filter((item) =>
    item.roles.some(
      (r) =>
        r === user?.role ||
        r === userRole ||
        (userRole === 'STAFF' && r === 'ACADEMIC_STAFF') ||
        (userRole === 'ACADEMIC_STAFF' && r === 'STAFF')
    )
  );

  return (
    <aside className="w-64 bg-slate-900 text-slate-300 flex flex-col flex-shrink-0 min-h-screen border-r border-slate-800">
      {/* Brand Header */}
      <div className="h-16 flex items-center px-6 border-b border-slate-800/80 bg-slate-950/40">
        <div className="flex items-center space-x-3">
          <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-indigo-600 to-indigo-500 text-white flex items-center justify-center font-black shadow-md shadow-indigo-500/20">
            UE
          </div>
          <div>
            <h1 className="font-bold text-white text-base tracking-tight leading-tight">UniEnroll</h1>
            <span className="text-[10px] font-mono text-indigo-400 font-semibold uppercase tracking-wider block">
              PORTAL SPA 2.0
            </span>
          </div>
        </div>
      </div>

      {/* Role Pill Banner */}
      <div className="px-6 py-3 border-b border-slate-800/60 bg-slate-900/50">
        <div className="flex items-center justify-between text-xs">
          <span className="text-slate-400">Không gian làm việc:</span>
          <span className="font-mono text-xs font-semibold text-indigo-400 bg-indigo-950/60 px-2 py-0.5 rounded border border-indigo-900/60">
            {role}
          </span>
        </div>
      </div>

      {/* Navigation List */}
      <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
        <div className="px-3 pb-2 text-[11px] font-semibold text-slate-300 uppercase tracking-wider">
          Menu Nghiệp Vụ
        </div>
        {filteredNav.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) =>
              `flex items-center justify-between px-3 py-2.5 rounded-lg text-xs font-medium transition-all ${
                isActive
                  ? 'bg-indigo-600 text-white font-semibold shadow-xs shadow-indigo-600/30'
                  : 'text-slate-300 hover:text-white hover:bg-slate-800/60'
              }`
            }
          >
            <div className="flex items-center space-x-3">
              <item.icon className="w-4 h-4 flex-shrink-0" />
              <span>{item.label}</span>
            </div>
            {item.badge && (
              <span className="text-[9px] font-bold px-1.5 py-0.5 rounded bg-slate-800 text-indigo-300 border border-slate-700">
                {item.badge}
              </span>
            )}
          </NavLink>
        ))}
      </nav>

      {/* Footer Info */}
      <div className="p-4 border-t border-slate-800/80 bg-slate-950/40 text-[11px] text-slate-400 text-center">
        <span>UniEnroll Core v1.0.0</span>
        <div className="text-[10px] text-slate-400 font-mono mt-0.5">MS SQL Server • Spring Boot 3</div>
      </div>
    </aside>
  );
};

export default Sidebar;
