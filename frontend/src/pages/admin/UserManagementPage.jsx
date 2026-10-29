import React, { useState, useEffect } from 'react';
import { adminApi } from '../../api/adminApi';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Table from '../../components/common/Table';
import Badge from '../../components/common/Badge';
import Modal from '../../components/common/Modal';
import BackendErrorAlert from '../../components/common/BackendErrorAlert';
import { UserPlus, Search, Lock, Unlock, RefreshCw } from 'lucide-react';

const UserManagementPage = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Filters
  const [roleFilter, setRoleFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [keyword, setKeyword] = useState('');

  // Modal State
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [createLoading, setCreateLoading] = useState(false);
  const [createError, setCreateError] = useState(null);

  // Form State - empty raw values to test backend validation!
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    fullName: '',
    email: '',
    role: 'ROLE_STUDENT',
    studentCode: '',
    major: '',
    lecturerCode: '',
    academicTitle: '',
    staffCode: '',
  });

  const fetchUsers = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await adminApi.getUsers({
        role: roleFilter || undefined,
        status: statusFilter || undefined,
        keyword: keyword || undefined,
      });
      setUsers(res.data || []);
    } catch (err) {
      console.error(err);
      setError(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, [roleFilter, statusFilter]);

  const handleToggleStatus = async (user) => {
    const nextStatus = user.status === 'ACTIVE' ? 'LOCKED' : 'ACTIVE';
    try {
      await adminApi.updateUserStatus(user.id, nextStatus);
      fetchUsers();
    } catch (err) {
      setError(err);
    }
  };

  // Submit without client validation to verify backend rules
  const handleCreateUser = async (e) => {
    e.preventDefault();
    setCreateLoading(true);
    setCreateError(null);

    const isStudent = formData.role === 'ROLE_STUDENT' || formData.role === 'STUDENT';
    const isLecturer = formData.role === 'ROLE_LECTURER' || formData.role === 'LECTURER';
    const isStaff = formData.role === 'ROLE_STAFF' || formData.role === 'STAFF' || formData.role === 'ACADEMIC_STAFF';

    const roleValue = isStudent
      ? 'ROLE_STUDENT'
      : isLecturer
      ? 'ROLE_LECTURER'
      : isStaff
      ? 'ROLE_STAFF'
      : 'ROLE_ADMIN';

    const payload = {
      username: formData.username?.trim(),
      password: formData.password,
      fullName: formData.fullName?.trim(),
      email: formData.email?.trim(),
      role: roleValue,
    };

    if (isStudent) {
      payload.studentCode = formData.studentCode?.trim() || undefined;
      payload.major = formData.major?.trim() || undefined;
    } else if (isLecturer) {
      payload.lecturerCode = formData.lecturerCode?.trim() || undefined;
      payload.departmentName = formData.academicTitle?.trim() || undefined;
    } else if (isStaff) {
      payload.staffCode = formData.staffCode?.trim() || undefined;
    }

    try {
      await adminApi.createUser(payload);
      setShowCreateModal(false);
      setFormData({
        username: '',
        password: '',
        fullName: '',
        email: '',
        role: 'ROLE_STUDENT',
        studentCode: '',
        major: '',
        lecturerCode: '',
        academicTitle: '',
        staffCode: '',
      });
      fetchUsers();
    } catch (err) {
      console.error('Create user error from backend:', err);
      setCreateError(err);
    } finally {
      setCreateLoading(false);
    }
  };

  const columns = [
    {
      header: 'STT',
      render: (_, idx) => <span className="text-slate-400 font-mono text-xs">{idx + 1}</span>,
      align: 'center',
    },
    {
      header: 'Tài khoản (Username)',
      render: (u) => (
        <div>
          <div className="font-semibold text-slate-800 font-mono">{u.username}</div>
          <div className="text-xs text-slate-400">ID: {u.id}</div>
        </div>
      ),
    },
    {
      header: 'Họ và tên',
      accessor: 'fullName',
      render: (u) => <span className="font-medium text-slate-900">{u.fullName}</span>,
    },
    {
      header: 'Email',
      accessor: 'email',
      render: (u) => <span className="text-slate-600 text-xs">{u.email}</span>,
    },
    {
      header: 'Vai trò',
      render: (u) => <Badge variant={u.role}>{u.role}</Badge>,
      align: 'center',
    },
    {
      header: 'Hồ sơ chuyên biệt',
      render: (u) => {
        if (u.studentProfile) {
          return (
            <span className="text-xs text-sky-700 font-mono bg-sky-50 px-2 py-0.5 rounded border border-sky-200">
              SV: {u.studentProfile.studentCode} ({u.studentProfile.major || 'CNTT'})
            </span>
          );
        }
        if (u.lecturerProfile) {
          return (
            <span className="text-xs text-purple-700 font-mono bg-purple-50 px-2 py-0.5 rounded border border-purple-200">
              GV: {u.lecturerProfile.lecturerCode} - {u.lecturerProfile.academicTitle}
            </span>
          );
        }
        if (u.staffProfile) {
          return (
            <span className="text-xs text-indigo-700 font-mono bg-indigo-50 px-2 py-0.5 rounded border border-indigo-200">
              PĐT: {u.staffProfile.staffCode}
            </span>
          );
        }
        return <span className="text-slate-400 text-xs">Admin System</span>;
      },
    },
    {
      header: 'Trạng thái',
      render: (u) => <Badge variant={u.status}>{u.status}</Badge>,
      align: 'center',
    },
    {
      header: 'Hành động',
      render: (u) => (
        <div className="flex items-center space-x-2 justify-end">
          <Button
            size="sm"
            variant={u.status === 'ACTIVE' ? 'danger' : 'success'}
            icon={u.status === 'ACTIVE' ? Lock : Unlock}
            onClick={() => handleToggleStatus(u)}
          >
            {u.status === 'ACTIVE' ? 'Khóa' : 'Kích hoạt'}
          </Button>
        </div>
      ),
      align: 'right',
    },
  ];

  return (
    <div className="space-y-6">
      {/* Top Banner / Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">
            Quản trị Người dùng & Phân quyền (UC-04)
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Quản lý tài khoản toàn trường (Sinh viên, Giảng viên, Phòng Đào tạo, Admin)
          </p>
        </div>
        <div className="flex items-center space-x-2">
          <Button variant="secondary" size="sm" icon={RefreshCw} onClick={fetchUsers}>
            Làm mới
          </Button>
          <Button
            variant="primary"
            size="sm"
            icon={UserPlus}
            onClick={() => {
              setCreateError(null);
              setShowCreateModal(true);
            }}
          >
            Tạo Tài Khoản Mới
          </Button>
        </div>
      </div>

      {/* Backend Error Alert if any */}
      {error && <BackendErrorAlert error={error} onClose={() => setError(null)} />}

      {/* Filter Toolbar */}
      <Card bodyClassName="p-4">
        <div className="grid grid-cols-1 sm:grid-cols-4 gap-3">
          <div className="relative">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
            <input
              type="text"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && fetchUsers()}
              placeholder="Tìm theo username, họ tên, email..."
              className="w-full pl-9 pr-3 py-2 text-xs rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500 text-slate-900 bg-white"
            />
          </div>

          <select
            value={roleFilter}
            onChange={(e) => setRoleFilter(e.target.value)}
            className="px-3 py-2 text-xs rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500 text-slate-900 bg-white"
          >
            <option value="">Tất cả Vai trò</option>
            <option value="STUDENT">STUDENT (Sinh viên)</option>
            <option value="LECTURER">LECTURER (Giảng viên)</option>
            <option value="ACADEMIC_STAFF">ACADEMIC_STAFF (Phòng Đào Tạo)</option>
            <option value="ADMIN">ADMIN (Quản trị viên)</option>
          </select>

          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-3 py-2 text-xs rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500 text-slate-900 bg-white"
          >
            <option value="">Tất cả Trạng thái</option>
            <option value="ACTIVE">ACTIVE (Hoạt động)</option>
            <option value="LOCKED">LOCKED (Bị khóa)</option>
          </select>

          <Button variant="secondary" size="sm" onClick={fetchUsers}>
            Áp dụng bộ lọc
          </Button>
        </div>
      </Card>

      {/* Users Table */}
      <Card title={`Danh sách Người dùng Hệ thống (${users.length})`}>
        {loading ? (
          <div className="py-12 text-center text-xs text-slate-400">Đang tải danh sách người dùng...</div>
        ) : (
          <Table columns={columns} data={users} />
        )}
      </Card>

      {/* Create User Modal (No client blocking validation!) */}
      <Modal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        title="Tạo Tài Khoản Mới (UC-04 - Backend Validated)"
        maxWidth="max-w-xl"
        footer={
          <>
            <Button variant="secondary" size="sm" onClick={() => setShowCreateModal(false)}>
              Hủy bỏ
            </Button>
            <Button
              variant="primary"
              size="sm"
              loading={createLoading}
              onClick={handleCreateUser}
            >
              Lưu Tài Khoản
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          {createError && <BackendErrorAlert error={createError} onClose={() => setCreateError(null)} />}

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Tên đăng nhập (Username) *
              </label>
              <input
                type="text"
                value={formData.username}
                onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                placeholder="ví dụ: student99"
                className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 focus:ring-indigo-500 focus:border-indigo-500 text-slate-900 bg-white"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Mật khẩu (Password) *
              </label>
              <input
                type="password"
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                placeholder="Mật khẩu khởi tạo"
                className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 focus:ring-indigo-500 focus:border-indigo-500 text-slate-900 bg-white"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Họ và Tên đầy đủ *
              </label>
              <input
                type="text"
                value={formData.fullName}
                onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                placeholder="ví dụ: Nguyễn Văn Test"
                className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 focus:ring-indigo-500 focus:border-indigo-500 text-slate-900 bg-white"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Địa chỉ Email *
              </label>
              <input
                type="email"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                placeholder="ví dụ: test@unienroll.edu.vn"
                className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 focus:ring-indigo-500 focus:border-indigo-500 text-slate-900 bg-white"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Vai trò Hệ thống (Role) *
            </label>
            <select
              value={formData.role}
              onChange={(e) => setFormData({ ...formData, role: e.target.value })}
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 focus:ring-indigo-500 focus:border-indigo-500 font-semibold text-slate-900 bg-white"
            >
              <option value="ROLE_STUDENT">Sinh viên (STUDENT)</option>
              <option value="ROLE_LECTURER">Giảng viên (LECTURER)</option>
              <option value="ROLE_STAFF">Phòng Đào Tạo (STAFF)</option>
              <option value="ROLE_ADMIN">Quản trị viên (ADMIN)</option>
            </select>
          </div>

          {/* Conditional Profile Fields */}
          {(formData.role === 'ROLE_STUDENT' || formData.role === 'STUDENT') && (
            <div className="p-3 bg-sky-50/60 rounded-lg border border-sky-100 space-y-3">
              <span className="text-xs font-bold text-sky-800 block">Hồ Sơ Sinh Viên</span>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-[11px] font-semibold text-slate-700 mb-1">Mã Sinh Viên *</label>
                  <input
                    type="text"
                    value={formData.studentCode}
                    onChange={(e) => setFormData({ ...formData, studentCode: e.target.value })}
                    placeholder="ví dụ: SV2026001"
                    className="w-full px-3 py-1.5 text-xs rounded border border-slate-300 bg-white text-slate-900"
                  />
                </div>
                <div>
                  <label className="block text-[11px] font-semibold text-slate-700 mb-1">Chuyên ngành</label>
                  <input
                    type="text"
                    value={formData.major}
                    onChange={(e) => setFormData({ ...formData, major: e.target.value })}
                    placeholder="Khoa học máy tính"
                    className="w-full px-3 py-1.5 text-xs rounded border border-slate-300 bg-white text-slate-900"
                  />
                </div>
              </div>
            </div>
          )}

          {(formData.role === 'ROLE_LECTURER' || formData.role === 'LECTURER') && (
            <div className="p-3 bg-purple-50/60 rounded-lg border border-purple-100 space-y-3">
              <span className="text-xs font-bold text-purple-800 block">Hồ Sơ Giảng Viên</span>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-[11px] font-semibold text-slate-700 mb-1">Mã Giảng Viên *</label>
                  <input
                    type="text"
                    value={formData.lecturerCode}
                    onChange={(e) => setFormData({ ...formData, lecturerCode: e.target.value })}
                    placeholder="ví dụ: GV001"
                    className="w-full px-3 py-1.5 text-xs rounded border border-slate-300 bg-white text-slate-900"
                  />
                </div>
                <div>
                  <label className="block text-[11px] font-semibold text-slate-700 mb-1">Bộ môn / Khoa phụ trách</label>
                  <input
                    type="text"
                    value={formData.academicTitle}
                    onChange={(e) => setFormData({ ...formData, academicTitle: e.target.value })}
                    placeholder="Bộ môn Công nghệ phần mềm"
                    className="w-full px-3 py-1.5 text-xs rounded border border-slate-300 bg-white text-slate-900"
                  />
                </div>
              </div>
            </div>
          )}

          {(formData.role === 'ROLE_STAFF' || formData.role === 'STAFF' || formData.role === 'ACADEMIC_STAFF') && (
            <div className="p-3 bg-indigo-50/60 rounded-lg border border-indigo-100 space-y-3">
              <span className="text-xs font-bold text-indigo-800 block">Hồ Sơ Cán Bộ Đào Tạo</span>
              <div>
                <label className="block text-[11px] font-semibold text-slate-700 mb-1">Mã Nhân Viên *</label>
                <input
                  type="text"
                  value={formData.staffCode}
                  onChange={(e) => setFormData({ ...formData, staffCode: e.target.value })}
                  placeholder="ví dụ: NV001"
                  className="w-full px-3 py-1.5 text-xs rounded border border-slate-300 bg-white text-slate-900"
                />
              </div>
            </div>
          )}
        </div>
      </Modal>
    </div>
  );
};

export default UserManagementPage;
