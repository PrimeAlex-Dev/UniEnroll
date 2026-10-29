import React, { useState, useEffect } from 'react';
import { curriculumApi } from '../../api/curriculumApi';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Table from '../../components/common/Table';
import Modal from '../../components/common/Modal';
import Badge from '../../components/common/Badge';
import BackendErrorAlert from '../../components/common/BackendErrorAlert';
import { BookOpen, Plus, GitMerge, RefreshCw, AlertTriangle } from 'lucide-react';

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
      // Refresh list
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
      render: (c) => <span className="font-semibold text-slate-800">{c.name || c.courseName}</span>,
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
      header: 'Tiên quyết & Cấu hình',
      render: (c) => (
        <Button
          size="sm"
          variant="secondary"
          icon={GitMerge}
          onClick={() => handleOpenPrerequisites(c)}
        >
          Cấu hình Tiên quyết
        </Button>
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
        title="Thêm Môn Học Mới (UC-06 - Backend Validation)"
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
            Tổng số tín chỉ: <span className="font-bold text-indigo-700">{(Number(formData.theoryCredits) || 0) + (Number(formData.practicalCredits) || 0)}</span> tín chỉ
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Mô tả Môn Học</label>
            <textarea
              rows="2"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              placeholder="Tóm tắt nội dung môn học..."
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 text-slate-900 bg-white"
            />
          </div>
        </div>
      </Modal>

      {/* Modal Prerequisites (UC-07) with DFS Cycle Error Testing */}
      <Modal
        isOpen={!!selectedCourse}
        onClose={() => setSelectedCourse(null)}
        title={`Cấu hình Tiên Quyết: [${selectedCourse?.code || selectedCourse?.courseCode}] ${selectedCourse?.name || selectedCourse?.courseName}`}
        maxWidth="max-w-xl"
        footer={
          <Button variant="secondary" size="sm" onClick={() => setSelectedCourse(null)}>
            Đóng
          </Button>
        }
      >
        <div className="space-y-4">
          {prereqError && <BackendErrorAlert error={prereqError} onClose={() => setPrereqError(null)} />}

          <div className="p-3 bg-amber-50 rounded-lg border border-amber-200 text-xs text-amber-800 flex items-start space-x-2">
            <AlertTriangle className="w-4 h-4 text-amber-600 mt-0.5 flex-shrink-0" />
            <div>
              <strong>Quy tắc DFS Chống Chu trình (BR-02 & Anti-Cycle):</strong> Backend UniEnroll sẽ duyệt đồ thị phụ thuộc. Nếu phát hiện quan hệ phụ thuộc vòng lặp (ví dụ A cần B, B cần C, C lại cần A) sẽ ngay lập tức trả về mã lỗi <code className="font-bold text-rose-700">ERR_CIRCULAR_PREREQUISITE_DETECTED</code>.
            </div>
          </div>

          {/* Form add prerequisite */}
          <form onSubmit={handleAddPrerequisite} className="p-3 bg-slate-50 rounded-lg border border-slate-200 space-y-3">
            <span className="text-xs font-bold text-slate-800 uppercase tracking-wider block">
              + Thiết lập Môn học Tiên quyết mới
            </span>

            <div className="grid grid-cols-1 gap-3">
              <div>
                <label className="block text-[11px] font-semibold text-slate-700 mb-1">Môn Tiên Quyết Cần Học Trước</label>
                <select
                  value={newPrereqCourseId}
                  onChange={(e) => setNewPrereqCourseId(e.target.value)}
                  className="w-full px-2.5 py-1.5 text-xs rounded border border-slate-300 font-medium text-slate-900 bg-white"
                >
                  <option value="">-- Chọn Môn Tiên Quyết --</option>
                  {courses
                    .filter((c) => c.id !== selectedCourse?.id)
                    .map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.code || c.courseCode} - {c.name || c.courseName} ({c.totalCredits ?? (c.credits ?? ((c.theoryCredits || 0) + (c.practicalCredits || 0)))} TC)
                      </option>
                    ))}
                </select>
              </div>
            </div>

            <div className="flex justify-end">
              <Button type="submit" variant="primary" size="sm" loading={prereqLoading} disabled={!newPrereqCourseId}>
                Thêm Môn Tiên Quyết
              </Button>
            </div>
          </form>

          {/* List of existing prerequisites */}
          <div>
            <span className="text-xs font-bold text-slate-700 uppercase tracking-wider block mb-2">
              Danh sách điều kiện tiên quyết hiện hành ({prerequisites.length}):
            </span>
            {prerequisites.length === 0 ? (
              <p className="text-xs text-slate-400 italic py-2">Môn học này hiện chưa có điều kiện tiên quyết nào.</p>
            ) : (
              <div className="space-y-2">
                {prerequisites.map((p) => (
                  <div
                    key={p.id}
                    className="flex items-center justify-between p-2.5 rounded-lg border border-slate-200 bg-white"
                  >
                    <div>
                      <span className="font-mono font-bold text-xs text-indigo-700 mr-2">
                        {p.code || p.prerequisiteCourseCode}
                      </span>
                      <span className="text-xs font-medium text-slate-800">{p.name || p.prerequisiteCourseName}</span>
                      <span className="ml-2 text-xs text-slate-500">({p.totalCredits || p.credits || 3} TC)</span>
                    </div>
                    <Button
                      size="sm"
                      variant="danger"
                      onClick={() => handleRemovePrerequisite(p.id)}
                    >
                      Xóa
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
