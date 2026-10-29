import React, { useState, useEffect } from 'react';
import { curriculumApi } from '../../api/curriculumApi';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Table from '../../components/common/Table';
import Modal from '../../components/common/Modal';
import Badge from '../../components/common/Badge';
import BackendErrorAlert from '../../components/common/BackendErrorAlert';
import { BookOpen, Plus, GitMerge, RefreshCw, AlertTriangle, Edit3, Trash2 } from 'lucide-react';

const CoursesPage = () => {
  const [courses, setCourses] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Create Course Modal State
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [createLoading, setCreateLoading] = useState(false);
  const [createError, setCreateError] = useState(null);
  const [formData, setFormData] = useState({
    code: '',
    name: '',
    theoryCredits: '3',
    practicalCredits: '0',
    departmentId: '',
    description: '',
  });

  // Edit Course Modal State
  const [editCourse, setEditCourse] = useState(null);
  const [editLoading, setEditLoading] = useState(false);
  const [editError, setEditError] = useState(null);
  const [editForm, setEditForm] = useState({
    code: '',
    name: '',
    theoryCredits: '3',
    practicalCredits: '0',
    departmentId: '',
    description: '',
  });

  // Delete Course Modal State
  const [deleteCourse, setDeleteCourse] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [deleteError, setDeleteError] = useState(null);

  // Prerequisite Modal State
  const [selectedCourse, setSelectedCourse] = useState(null);
  const [prerequisites, setPrerequisites] = useState([]);
  const [prereqLoading, setPrereqLoading] = useState(false);
  const [prereqError, setPrereqError] = useState(null);
  const [newPrereqCourseId, setNewPrereqCourseId] = useState('');

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [coursesRes, deptsRes] = await Promise.all([
        curriculumApi.getCourses(),
        curriculumApi.getDepartments(),
      ]);
      setCourses(coursesRes.data || []);
      setDepartments(deptsRes.data || []);
    } catch (err) {
      console.error(err);
      setError(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleCreateCourse = async (e) => {
    e.preventDefault();
    setCreateLoading(true);
    setCreateError(null);

    try {
      await curriculumApi.createCourse({
        code: formData.code.trim(),
        name: formData.name.trim(),
        theoryCredits: formData.theoryCredits !== '' ? Number(formData.theoryCredits) : 0,
        practicalCredits: formData.practicalCredits !== '' ? Number(formData.practicalCredits) : 0,
        departmentId: formData.departmentId ? Number(formData.departmentId) : '',
        description: formData.description?.trim() || null,
      });
      setShowCreateModal(false);
      setFormData({ code: '', name: '', theoryCredits: '3', practicalCredits: '0', departmentId: '', description: '' });
      fetchData();
    } catch (err) {
      console.error('Create course error:', err);
      setCreateError(err);
    } finally {
      setCreateLoading(false);
    }
  };

  const handleOpenEdit = (course) => {
    setEditCourse(course);
    setEditForm({
      code: course.code || course.courseCode || '',
      name: course.name || course.courseName || '',
      theoryCredits: String(course.theoryCredits ?? 3),
      practicalCredits: String(course.practicalCredits ?? 0),
      departmentId: String(course.departmentId || ''),
      description: course.description || '',
    });
    setEditError(null);
  };

  const handleUpdateCourse = async (e) => {
    e.preventDefault();
    if (!editCourse) return;
    setEditLoading(true);
    setEditError(null);

    try {
      await curriculumApi.updateCourse(editCourse.id, {
        code: editForm.code.trim(),
        name: editForm.name.trim(),
        theoryCredits: editForm.theoryCredits !== '' ? Number(editForm.theoryCredits) : 0,
        practicalCredits: editForm.practicalCredits !== '' ? Number(editForm.practicalCredits) : 0,
        departmentId: editForm.departmentId ? Number(editForm.departmentId) : '',
        description: editForm.description?.trim() || null,
      });
      setEditCourse(null);
      fetchData();
    } catch (err) {
      console.error('Update course error:', err);
      setEditError(err);
    } finally {
      setEditLoading(false);
    }
  };

  const handleDeleteCourse = async () => {
    if (!deleteCourse) return;
    setDeleteLoading(true);
    setDeleteError(null);

    try {
      await curriculumApi.deleteCourse(deleteCourse.id);
      setDeleteCourse(null);
      fetchData();
    } catch (err) {
      console.error('Delete course error:', err);
      setDeleteError(err);
    } finally {
      setDeleteLoading(false);
    }
  };

  const handleOpenPrerequisites = async (course) => {
    setSelectedCourse(course);
    setPrereqError(null);
    setNewPrereqCourseId('');
    setPrereqLoading(true);
    try {
      const res = await curriculumApi.getPrerequisites(course.id);
      setPrerequisites(res.data || []);
    } catch (err) {
      setPrereqError(err);
    } finally {
      setPrereqLoading(false);
    }
  };

  const handleAddPrerequisite = async (e) => {
    e.preventDefault();
    setPrereqLoading(true);
    setPrereqError(null);

    try {
      await curriculumApi.addPrerequisite(selectedCourse.id, {
        prerequisiteCourseIds: [Number(newPrereqCourseId)],
      });
      const res = await curriculumApi.getPrerequisites(selectedCourse.id);
      setPrerequisites(res.data || []);
      setNewPrereqCourseId('');
    } catch (err) {
      console.error('Add prerequisite failed (DFS cycle or invalid):', err);
      setPrereqError(err);
    } finally {
      setPrereqLoading(false);
    }
  };

  const handleRemovePrerequisite = async (prereqCourseId) => {
    setPrereqLoading(true);
    setPrereqError(null);
    try {
      await curriculumApi.removePrerequisite(selectedCourse.id, prereqCourseId);
      const res = await curriculumApi.getPrerequisites(selectedCourse.id);
      setPrerequisites(res.data || []);
    } catch (err) {
      console.error('Remove prerequisite failed:', err);
      setPrereqError(err);
    } finally {
      setPrereqLoading(false);
    }
  };

  const columns = [
    {
      header: 'STT',
      render: (_, idx) => <span className="text-slate-400 font-mono text-xs">{idx + 1}</span>,
      align: 'center',
    },
    {
      header: 'Mã Môn Học',
      render: (c) => <span className="font-mono font-bold text-indigo-700">{c.code || c.courseCode}</span>,
    },
    {
      header: 'Tên Môn Học',
      render: (c) => (
        <div>
          <span className="font-semibold text-slate-800">{c.name || c.courseName}</span>
          {c.description && <p className="text-xs text-slate-500 mt-0.5">{c.description}</p>}
        </div>
      ),
    },
    {
      header: 'Số Tín Chỉ',
      render: (c) => (
        <span className="font-bold text-xs bg-slate-100 text-slate-700 px-2 py-0.5 rounded border border-slate-200">
          {c.totalCredits ?? (c.credits ?? ((c.theoryCredits || 0) + (c.practicalCredits || 0)))} TC
        </span>
      ),
      align: 'center',
    },
    {
      header: 'Khoa Phụ Trách',
      render: (c) => <span className="text-slate-600 text-xs">{c.departmentName || `Khoa #${c.departmentId}`}</span>,
    },
    {
      header: 'Tiên quyết',
      render: (c) => (
        <Button
          size="sm"
          variant="secondary"
          icon={GitMerge}
          title="Cấu hình danh sách môn tiên quyết"
          onClick={() => handleOpenPrerequisites(c)}
        >
          Tiên quyết
        </Button>
      ),
      align: 'center',
    },
    {
      header: 'Thao tác',
      render: (c) => (
        <div className="flex items-center justify-end space-x-1.5">
          <Button
            size="sm"
            variant="secondary"
            icon={Edit3}
            title="Sửa thông tin môn học"
            onClick={() => handleOpenEdit(c)}
          >
            Sửa
          </Button>
          <Button
            size="sm"
            variant="danger"
            icon={Trash2}
            title="Xóa môn học"
            onClick={() => {
              setDeleteCourse(c);
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
            <BookOpen className="w-5 h-5 text-indigo-600" />
            <span>Quản lý Môn học & Tiên quyết (UC-06, UC-07)</span>
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Quản lý số tín chỉ, khoa chủ quản và đồ thị điều kiện tiên quyết chống vòng lặp (DFS Anti-Cycle)
          </p>
        </div>
        <div className="flex items-center space-x-2">
          <Button variant="secondary" size="sm" icon={RefreshCw} onClick={fetchData}>
            Làm mới
          </Button>
          <Button
            variant="primary"
            size="sm"
            icon={Plus}
            onClick={() => {
              setCreateError(null);
              setShowCreateModal(true);
            }}
          >
            Thêm Môn Học Mới
          </Button>
        </div>
      </div>

      {error && <BackendErrorAlert error={error} onClose={() => setError(null)} />}

      <Card title={`Danh mục Môn học (${courses.length})`}>
        {loading ? (
          <div className="py-12 text-center text-xs text-slate-400">Đang tải danh mục môn học...</div>
        ) : (
          <Table columns={columns} data={courses} />
        )}
      </Card>

      {/* Modal Create Course */}
      <Modal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        title="Thêm Môn Học Mới (UC-06)"
        footer={
          <>
            <Button variant="secondary" size="sm" onClick={() => setShowCreateModal(false)}>
              Hủy
            </Button>
            <Button variant="primary" size="sm" loading={createLoading} onClick={handleCreateCourse}>
              Lưu Môn Học
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          {createError && <BackendErrorAlert error={createError} onClose={() => setCreateError(null)} />}

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Mã Môn Học *</label>
              <input
                type="text"
                value={formData.code}
                onChange={(e) => setFormData({ ...formData, code: e.target.value })}
                placeholder="ví dụ: CS101, MATH201"
                className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 font-mono text-slate-900 bg-white"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Khoa Phụ Trách *</label>
              <select
                value={formData.departmentId}
                onChange={(e) => setFormData({ ...formData, departmentId: e.target.value })}
                className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 font-medium text-slate-900 bg-white"
              >
                <option value="">-- Chọn Khoa chủ quản --</option>
                {departments.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.code} - {d.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Tên Môn Học *</label>
            <input
              type="text"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              placeholder="ví dụ: Cấu trúc dữ liệu và Giải thuật"
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 text-slate-900 bg-white"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Tín Chỉ Lý Thuyết *</label>
              <input
                type="number"
                min="0"
                value={formData.theoryCredits}
                onChange={(e) => setFormData({ ...formData, theoryCredits: e.target.value })}
                placeholder="ví dụ: 3"
                className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 font-mono text-slate-900 bg-white"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Tín Chỉ Thực Hành *</label>
              <input
                type="number"
                min="0"
                value={formData.practicalCredits}
                onChange={(e) => setFormData({ ...formData, practicalCredits: e.target.value })}
                placeholder="ví dụ: 0 hoặc 1"
                className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 font-mono text-slate-900 bg-white"
              />
            </div>
          </div>
          <div className="text-[11px] text-slate-500 font-medium">
            Tổng số tín chỉ: <span className="font-bold text-indigo-600">{(Number(formData.theoryCredits) || 0) + (Number(formData.practicalCredits) || 0)} TC</span>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Mô Tả Tóm Tắt Môn Học</label>
            <textarea
              rows={3}
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              placeholder="Mô tả chuẩn đầu ra, đề cương học phần..."
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 text-slate-900 bg-white"
            />
          </div>
        </div>
      </Modal>

      {/* Modal Edit Course */}
      <Modal
        isOpen={!!editCourse}
        onClose={() => setEditCourse(null)}
        title={`Chỉnh sửa Môn Học: ${editCourse?.code || editCourse?.courseCode || ''}`}
        footer={
          <>
            <Button variant="secondary" size="sm" onClick={() => setEditCourse(null)}>
              Hủy
            </Button>
            <Button variant="primary" size="sm" loading={editLoading} onClick={handleUpdateCourse}>
              Cập nhật
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          {editError && <BackendErrorAlert error={editError} onClose={() => setEditError(null)} />}

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Mã Môn Học *</label>
              <input
                type="text"
                value={editForm.code}
                onChange={(e) => setEditForm({ ...editForm, code: e.target.value })}
                className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 font-mono text-slate-900 bg-white"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Khoa Phụ Trách *</label>
              <select
                value={editForm.departmentId}
                onChange={(e) => setEditForm({ ...editForm, departmentId: e.target.value })}
                className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 font-medium text-slate-900 bg-white"
              >
                <option value="">-- Chọn Khoa chủ quản --</option>
                {departments.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.code} - {d.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Tên Môn Học *</label>
            <input
              type="text"
              value={editForm.name}
              onChange={(e) => setEditForm({ ...editForm, name: e.target.value })}
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 text-slate-900 bg-white"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Tín Chỉ Lý Thuyết *</label>
              <input
                type="number"
                min="0"
                value={editForm.theoryCredits}
                onChange={(e) => setEditForm({ ...editForm, theoryCredits: e.target.value })}
                className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 font-mono text-slate-900 bg-white"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Tín Chỉ Thực Hành *</label>
              <input
                type="number"
                min="0"
                value={editForm.practicalCredits}
                onChange={(e) => setEditForm({ ...editForm, practicalCredits: e.target.value })}
                className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 font-mono text-slate-900 bg-white"
              />
            </div>
          </div>
          <div className="text-[11px] text-slate-500 font-medium">
            Tổng số tín chỉ: <span className="font-bold text-indigo-600">{(Number(editForm.theoryCredits) || 0) + (Number(editForm.practicalCredits) || 0)} TC</span>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Mô Tả Tóm Tắt</label>
            <textarea
              rows={3}
              value={editForm.description}
              onChange={(e) => setEditForm({ ...editForm, description: e.target.value })}
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 text-slate-900 bg-white"
            />
          </div>
        </div>
      </Modal>

      {/* Modal Delete Course */}
      <Modal
        isOpen={!!deleteCourse}
        onClose={() => setDeleteCourse(null)}
        title="Xác nhận xóa Môn Học"
        footer={
          <>
            <Button variant="secondary" size="sm" onClick={() => setDeleteCourse(null)}>
              Hủy
            </Button>
            <Button variant="danger" size="sm" loading={deleteLoading} onClick={handleDeleteCourse}>
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
              <p className="font-semibold">Bạn có chắc chắn muốn xóa môn học này?</p>
              <p className="mt-1 text-slate-600">
                Môn học: <span className="font-bold text-slate-900">{deleteCourse?.name || deleteCourse?.courseName} ({deleteCourse?.code || deleteCourse?.courseCode})</span>.
              </p>
              <p className="mt-1 text-slate-500">
                Lưu ý: Hệ thống chỉ cho phép xóa nếu môn học chưa từng mở lớp học phần, chưa có điểm số lịch sử và không là môn tiên quyết của môn khác.
              </p>
            </div>
          </div>
        </div>
      </Modal>

      {/* Modal Prerequisites Setup (UC-07) */}
      <Modal
        isOpen={!!selectedCourse}
        onClose={() => setSelectedCourse(null)}
        title={`Cấu hình Môn Tiên Quyết: ${selectedCourse?.name || selectedCourse?.courseName} (${selectedCourse?.code || selectedCourse?.courseCode})`}
        footer={
          <Button variant="secondary" size="sm" onClick={() => setSelectedCourse(null)}>
            Đóng
          </Button>
        }
      >
        <div className="space-y-5">
          {prereqError && <BackendErrorAlert error={prereqError} onClose={() => setPrereqError(null)} />}

          {/* Form add prerequisite */}
          <form onSubmit={handleAddPrerequisite} className="bg-slate-50 p-3 rounded-lg border border-slate-200 space-y-3">
            <label className="block text-xs font-semibold text-slate-700">Thêm Môn Tiên Quyết Mới:</label>
            <div className="flex space-x-2">
              <select
                value={newPrereqCourseId}
                onChange={(e) => setNewPrereqCourseId(e.target.value)}
                className="flex-1 px-3 py-1.5 text-xs rounded border border-slate-300 font-medium text-slate-900 bg-white"
              >
                <option value="">-- Chọn môn học tiên quyết --</option>
                {courses
                  .filter((c) => c.id !== selectedCourse?.id)
                  .map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.code || c.courseCode} - {c.name || c.courseName} ({c.totalCredits || c.credits || 3} TC)
                    </option>
                  ))}
              </select>
              <Button
                type="submit"
                variant="primary"
                size="sm"
                loading={prereqLoading}
                disabled={!newPrereqCourseId}
              >
                Gán Tiên Quyết
              </Button>
            </div>
            <p className="text-[11px] text-slate-500">
              Hệ thống tự động chạy thuật toán DFS ngăn chặn triệt để vòng lặp điều kiện tiên quyết (Circular Dependency).
            </p>
          </form>

          {/* Current prerequisites list */}
          <div>
            <h4 className="text-xs font-bold text-slate-800 uppercase tracking-wider mb-2">
              Danh sách môn tiên quyết bắt buộc ({prerequisites.length}):
            </h4>
            {prereqLoading && prerequisites.length === 0 ? (
              <div className="py-6 text-center text-xs text-slate-400">Đang tải danh sách tiên quyết...</div>
            ) : prerequisites.length === 0 ? (
              <div className="py-6 text-center text-xs text-slate-400 border border-dashed rounded-lg bg-slate-50/50">
                Môn học này hiện chưa có ràng buộc môn học tiên quyết.
              </div>
            ) : (
              <div className="space-y-2">
                {prerequisites.map((p) => (
                  <div
                    key={p.id}
                    className="flex items-center justify-between p-2.5 rounded-lg border border-slate-200 bg-white text-xs"
                  >
                    <div>
                      <span className="font-mono font-bold text-indigo-700 mr-2">{p.code || p.courseCode}</span>
                      <span className="font-medium text-slate-800">{p.name || p.courseName}</span>
                      <span className="ml-2 text-slate-400">({p.totalCredits || p.credits || 3} TC)</span>
                    </div>
                    <Button
                      size="sm"
                      variant="danger"
                      onClick={() => handleRemovePrerequisite(p.id)}
                      loading={prereqLoading}
                    >
                      Hủy gán
                    </Button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default CoursesPage;
