import React, { useState, useEffect } from 'react';
import { curriculumApi } from '../../api/curriculumApi';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Table from '../../components/common/Table';
import Modal from '../../components/common/Modal';
import BackendErrorAlert from '../../components/common/BackendErrorAlert';
import { Building, Plus, RefreshCw, Edit3, Trash2, AlertTriangle } from 'lucide-react';

const DepartmentsPage = () => {
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Modal Create State
  const [showModal, setShowModal] = useState(false);
  const [createLoading, setCreateLoading] = useState(false);
  const [createError, setCreateError] = useState(null);
  const [code, setCode] = useState('');
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');

  // Modal Edit State
  const [editDept, setEditDept] = useState(null);
  const [editLoading, setEditLoading] = useState(false);
  const [editError, setEditError] = useState(null);
  const [editForm, setEditForm] = useState({ code: '', name: '', description: '' });

  // Modal Delete State
  const [deleteDept, setDeleteDept] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [deleteError, setDeleteError] = useState(null);

  const fetchDepartments = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await curriculumApi.getDepartments();
      setDepartments(res.data || []);
    } catch (err) {
      console.error(err);
      setError(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDepartments();
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setCreateLoading(true);
    setCreateError(null);

    try {
      await curriculumApi.createDepartment({ code, name, description });
      setShowModal(false);
      setCode('');
      setName('');
      setDescription('');
      fetchDepartments();
    } catch (err) {
      console.error('Department creation failed:', err);
      setCreateError(err);
    } finally {
      setCreateLoading(false);
    }
  };

  const handleOpenEdit = (dept) => {
    setEditDept(dept);
    setEditForm({
      code: dept.code || '',
      name: dept.name || '',
      description: dept.description || '',
    });
    setEditError(null);
  };

  const handleUpdate = async (e) => {
    e.preventDefault();
    if (!editDept) return;
    setEditLoading(true);
    setEditError(null);

    try {
      await curriculumApi.updateDepartment(editDept.id, editForm);
      setEditDept(null);
      fetchDepartments();
    } catch (err) {
      console.error('Department update failed:', err);
      setEditError(err);
    } finally {
      setEditLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteDept) return;
    setDeleteLoading(true);
    setDeleteError(null);

    try {
      await curriculumApi.deleteDepartment(deleteDept.id);
      setDeleteDept(null);
      fetchDepartments();
    } catch (err) {
      console.error('Department deletion failed:', err);
      setDeleteError(err);
    } finally {
      setDeleteLoading(false);
    }
  };

  const columns = [
    {
      header: 'STT',
      render: (_, idx) => <span className="text-slate-400 font-mono text-xs">{idx + 1}</span>,
      align: 'center',
    },
    {
      header: 'Mã Khoa / Bộ Môn',
      render: (d) => <span className="font-mono font-bold text-indigo-700">{d.code}</span>,
    },
    {
      header: 'Tên Khoa / Bộ Môn',
      render: (d) => (
        <div>
          <span className="font-semibold text-slate-900">{d.name}</span>
          {d.description && <p className="text-xs text-slate-500 mt-0.5">{d.description}</p>}
        </div>
      ),
    },
    {
      header: 'Số môn liên kết',
      render: (d) => (
        <span className="font-mono text-xs px-2 py-0.5 rounded bg-slate-100 text-slate-700 border border-slate-200">
          {d.totalCourses ?? 0} môn
        </span>
      ),
      align: 'center',
    },
    {
      header: 'Thao tác',
      render: (d) => (
        <div className="flex items-center justify-end space-x-1.5">
          <Button
            size="sm"
            variant="secondary"
            icon={Edit3}
            title="Sửa thông tin Khoa"
            onClick={() => handleOpenEdit(d)}
          >
            Sửa
          </Button>
          <Button
            size="sm"
            variant="danger"
            icon={Trash2}
            title="Xóa Khoa"
            onClick={() => {
              setDeleteDept(d);
              setDeleteError(null);
            }}
          >
            Xóa
          </Button>
        </div>
      ),
      align: 'right',
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight flex items-center space-x-2">
            <Building className="w-5 h-5 text-indigo-600" />
            <span>Quản lý Khoa / Bộ môn Đào tạo (UC-05)</span>
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Thiết lập danh mục các đơn vị khoa viện chủ quản môn học trong nhà trường
          </p>
        </div>
        <div className="flex items-center space-x-2">
          <Button variant="secondary" size="sm" icon={RefreshCw} onClick={fetchDepartments}>
            Làm mới
          </Button>
          <Button
            variant="primary"
            size="sm"
            icon={Plus}
            onClick={() => {
              setCreateError(null);
              setShowModal(true);
            }}
          >
            Thêm Khoa Mới
          </Button>
        </div>
      </div>

      {error && <BackendErrorAlert error={error} onClose={() => setError(null)} />}

      <Card title={`Danh mục Khoa / Bộ môn (${departments.length})`}>
        {loading ? (
          <div className="py-12 text-center text-xs text-slate-400">Đang tải danh sách khoa...</div>
        ) : (
          <Table columns={columns} data={departments} />
        )}
      </Card>

      {/* Modal create department */}
      <Modal
        isOpen={showModal}
        onClose={() => setShowModal(false)}
        title="Thêm Khoa Mới (UC-05)"
        footer={
          <>
            <Button variant="secondary" size="sm" onClick={() => setShowModal(false)}>
              Hủy
            </Button>
            <Button variant="primary" size="sm" loading={createLoading} onClick={handleCreate}>
              Lưu Khoa
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          {createError && <BackendErrorAlert error={createError} onClose={() => setCreateError(null)} />}

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Mã Khoa (Unique Code) *
            </label>
            <input
              type="text"
              value={code}
              onChange={(e) => setCode(e.target.value)}
              placeholder="ví dụ: FIT, MATH, PHYS"
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 focus:ring-indigo-500 font-mono text-slate-900 bg-white"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Tên Khoa Đầy Đủ *
            </label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="ví dụ: Khoa Công nghệ Thông tin"
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 focus:ring-indigo-500 text-slate-900 bg-white"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Mô tả / Giới thiệu
            </label>
            <textarea
              rows={3}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Mô tả tóm tắt về khoa/bộ môn..."
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 focus:ring-indigo-500 text-slate-900 bg-white"
            />
          </div>
        </div>
      </Modal>

      {/* Modal edit department */}
      <Modal
        isOpen={!!editDept}
        onClose={() => setEditDept(null)}
        title={`Chỉnh sửa Khoa: ${editDept?.code || ''}`}
        footer={
          <>
            <Button variant="secondary" size="sm" onClick={() => setEditDept(null)}>
              Hủy
            </Button>
            <Button variant="primary" size="sm" loading={editLoading} onClick={handleUpdate}>
              Cập nhật
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          {editError && <BackendErrorAlert error={editError} onClose={() => setEditError(null)} />}

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Mã Khoa *
            </label>
            <input
              type="text"
              value={editForm.code}
              onChange={(e) => setEditForm({ ...editForm, code: e.target.value })}
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 font-mono text-slate-900 bg-white"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Tên Khoa Đầy Đủ *
            </label>
            <input
              type="text"
              value={editForm.name}
              onChange={(e) => setEditForm({ ...editForm, name: e.target.value })}
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 text-slate-900 bg-white"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Mô tả
            </label>
            <textarea
              rows={3}
              value={editForm.description}
              onChange={(e) => setEditForm({ ...editForm, description: e.target.value })}
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 text-slate-900 bg-white"
            />
          </div>
        </div>
      </Modal>

      {/* Modal confirm delete department */}
      <Modal
        isOpen={!!deleteDept}
        onClose={() => setDeleteDept(null)}
        title="Xác nhận xóa Khoa / Bộ môn"
        footer={
          <>
            <Button variant="secondary" size="sm" onClick={() => setDeleteDept(null)}>
              Hủy
            </Button>
            <Button variant="danger" size="sm" loading={deleteLoading} onClick={handleDelete}>
              Xác nhận Xóa
            </Button>
          </>
        }
      >
        <div className="space-y-3">
          {deleteError && <BackendErrorAlert error={deleteError} onClose={() => setDeleteError(null)} />}
          <div className="flex items-start space-x-3 text-amber-700 bg-amber-50 p-3 rounded-lg border border-amber-200 text-xs">
            <AlertTriangle className="w-5 h-5 flex-shrink-0 text-amber-600 mt-0.5" />
            <div>
              <p className="font-semibold">Bạn có chắc chắn muốn xóa khoa này?</p>
              <p className="mt-1 text-slate-600">
                Khoa: <span className="font-bold text-slate-900">{deleteDept?.name} ({deleteDept?.code})</span>.
              </p>
              <p className="mt-1 text-slate-500">
                Lưu ý: Hệ thống chỉ cho phép xóa nếu khoa chưa có môn học liên kết.
              </p>
            </div>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default DepartmentsPage;
