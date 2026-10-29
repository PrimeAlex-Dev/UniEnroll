import React, { useState, useEffect } from 'react';
import { schedulingApi } from '../../api/schedulingApi';
import { curriculumApi } from '../../api/curriculumApi';
import { adminApi } from '../../api/adminApi';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Table from '../../components/common/Table';
import Modal from '../../components/common/Modal';
import Badge from '../../components/common/Badge';
import BackendErrorAlert from '../../components/common/BackendErrorAlert';
import { Layers, Plus, CalendarPlus, Edit3, XCircle, RefreshCw, Trash2, AlertTriangle } from 'lucide-react';

const SectionsPage = () => {
  const [sections, setSections] = useState([]);
  const [semesters, setSemesters] = useState([]);
  const [courses, setCourses] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [lecturers, setLecturers] = useState([]);
  const [selectedSemesterId, setSelectedSemesterId] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Modal Create Section (UC-09)
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [createLoading, setCreateLoading] = useState(false);
  const [createError, setCreateError] = useState(null);
  const [sectionForm, setSectionForm] = useState({
    courseId: '',
    semesterId: '',
    sectionCode: '',
    capacity: '',
    lecturerId: '',
  });

  // Modal Edit Section (UC-09)
  const [editSection, setEditSection] = useState(null);
  const [editLoading, setEditLoading] = useState(false);
  const [editError, setEditError] = useState(null);
  const [editForm, setEditForm] = useState({
    sectionCode: '',
    capacity: '',
    lecturerId: '',
  });

  // Modal Delete Section (UC-09)
  const [deleteSection, setDeleteSection] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [deleteError, setDeleteError] = useState(null);

  // Modal Schedule Slots (UC-10)
  const [selectedSectionForSlot, setSelectedSectionForSlot] = useState(null);
  const [slotLoading, setSlotLoading] = useState(false);
  const [slotError, setSlotError] = useState(null);
  const [slotForm, setSlotForm] = useState({
    roomId: '',
    dayOfWeek: 'MONDAY',
    startPeriod: '1',
    endPeriod: '3',
  });

  // Modal Edit Capacity (UC-11)
  const [selectedSectionForCap, setSelectedSectionForCap] = useState(null);
  const [capLoading, setCapLoading] = useState(false);
  const [capError, setCapError] = useState(null);
  const [newCapacity, setNewCapacity] = useState('');

  const initData = async () => {
    try {
      const [semRes, cRes, rRes, uRes] = await Promise.all([
        schedulingApi.getSemesters(),
        curriculumApi.getCourses(),
        schedulingApi.getRooms(),
        adminApi.getUsers({ role: 'LECTURER' }),
      ]);
      setSemesters(semRes.data || []);
      setCourses(cRes.data || []);
      setRooms(rRes.data || []);
      setLecturers(uRes.data || []);

      if (semRes.data && semRes.data.length > 0) {
        setSelectedSemesterId(String(semRes.data[0].id));
      }
    } catch (err) {
      console.error(err);
      setError(err);
    }
  };

  const fetchSections = async (semesterId) => {
    setLoading(true);
    setError(null);
    try {
      const res = await schedulingApi.getSections(semesterId ? Number(semesterId) : undefined);
      setSections(res.data || []);
      // If slot modal is open, refresh selectedSectionForSlot with fresh data
      if (selectedSectionForSlot) {
        const refreshed = (res.data || []).find((s) => s.id === selectedSectionForSlot.id);
        if (refreshed) {
          setSelectedSectionForSlot(refreshed);
        }
      }
    } catch (err) {
      console.error(err);
      setError(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    initData();
  }, []);

  useEffect(() => {
    if (selectedSemesterId) {
      fetchSections(selectedSemesterId);
    }
  }, [selectedSemesterId]);

  const handleCreateSection = async (e) => {
    e.preventDefault();
    setCreateLoading(true);
    setCreateError(null);
    try {
      await schedulingApi.createSection({
        courseId: sectionForm.courseId ? Number(sectionForm.courseId) : '',
        semesterId: sectionForm.semesterId ? Number(sectionForm.semesterId) : Number(selectedSemesterId),
        sectionCode: sectionForm.sectionCode?.trim(),
        capacity: sectionForm.capacity ? Number(sectionForm.capacity) : '',
        lecturerId: sectionForm.lecturerId ? Number(sectionForm.lecturerId) : null,
      });
      setShowCreateModal(false);
      setSectionForm({ courseId: '', semesterId: '', sectionCode: '', capacity: '', lecturerId: '' });
      fetchSections(selectedSemesterId);
    } catch (err) {
      console.error('Create section failed:', err);
      setCreateError(err);
    } finally {
      setCreateLoading(false);
    }
  };

  const handleOpenEdit = (s) => {
    setEditSection(s);
    setEditForm({
      sectionCode: s.sectionCode || '',
      capacity: String(s.capacity || s.maxCapacity || ''),
      lecturerId: String(s.lecturerId || ''),
    });
    setEditError(null);
  };

  const handleUpdateSection = async (e) => {
    e.preventDefault();
    if (!editSection) return;
    setEditLoading(true);
    setEditError(null);
    try {
      await schedulingApi.updateSection(editSection.id, {
        sectionCode: editForm.sectionCode?.trim(),
        capacity: editForm.capacity ? Number(editForm.capacity) : '',
        lecturerId: editForm.lecturerId ? Number(editForm.lecturerId) : null,
        semesterId: editSection.semesterId,
        courseId: editSection.courseId,
      });
      setEditSection(null);
      fetchSections(selectedSemesterId);
    } catch (err) {
      console.error('Update section failed:', err);
      setEditError(err);
    } finally {
      setEditLoading(false);
    }
  };

  const handleDeleteSection = async () => {
    if (!deleteSection) return;
    setDeleteLoading(true);
    setDeleteError(null);
    try {
      await schedulingApi.deleteSection(deleteSection.id);
      setDeleteSection(null);
      fetchSections(selectedSemesterId);
    } catch (err) {
      console.error('Delete section failed:', err);
      setDeleteError(err);
    } finally {
      setDeleteLoading(false);
    }
  };

  const handleAddSlot = async (e) => {
    e.preventDefault();
    setSlotLoading(true);
    setSlotError(null);
    try {
      const res = await schedulingApi.addScheduleSlot(selectedSectionForSlot.id, {
        roomId: slotForm.roomId ? Number(slotForm.roomId) : '',
        dayOfWeek: slotForm.dayOfWeek,
        startPeriod: slotForm.startPeriod ? Number(slotForm.startPeriod) : '',
        endPeriod: slotForm.endPeriod ? Number(slotForm.endPeriod) : '',
      });
      if (res.data) {
        setSelectedSectionForSlot(res.data);
      }
      setSlotForm({ roomId: '', dayOfWeek: 'MONDAY', startPeriod: '1', endPeriod: '3' });
      fetchSections(selectedSemesterId);
    } catch (err) {
      console.error('Schedule slot failed (collision):', err);
      setSlotError(err);
    } finally {
      setSlotLoading(false);
    }
  };

  const handleDeleteSlot = async (slotId) => {
    setSlotLoading(true);
    setSlotError(null);
    try {
      const res = await schedulingApi.deleteScheduleSlot(selectedSectionForSlot.id, slotId);
      if (res.data) {
        setSelectedSectionForSlot(res.data);
      }
      fetchSections(selectedSemesterId);
    } catch (err) {
      console.error('Delete schedule slot failed:', err);
      setSlotError(err);
    } finally {
      setSlotLoading(false);
    }
  };

  const handleUpdateCapacity = async (e) => {
    e.preventDefault();
    setCapLoading(true);
    setCapError(null);
    try {
      await schedulingApi.updateCapacity(selectedSectionForCap.id, {
        newCapacity: Number(newCapacity),
      });
      setSelectedSectionForCap(null);
      fetchSections(selectedSemesterId);
    } catch (err) {
      console.error('Update capacity error (BR-02/BR-09):', err);
      setCapError(err);
    } finally {
      setCapLoading(false);
    }
  };

  const handleCancelSection = async (s) => {
    if (!window.confirm(`Bạn có chắc chắn muốn HỦY Lớp học phần ${s.sectionCode}? Lớp sẽ chuyển trạng thái CANCELLED và thu hồi toàn bộ lịch học.`)) {
      return;
    }
    try {
      await schedulingApi.cancelSection(s.id);
      fetchSections(selectedSemesterId);
    } catch (err) {
      console.error('Cancel section error:', err);
      setError(err);
    }
  };

  const columns = [
    {
      header: 'Mã Lớp HP',
      render: (s) => (
        <div>
          <span className="font-mono font-bold text-indigo-700">{s.sectionCode}</span>
          <div className="text-[11px] text-slate-400">ID: {s.id}</div>
        </div>
      ),
    },
    {
      header: 'Môn Học & Tín Chỉ',
      render: (s) => (
        <div>
          <span className="font-semibold text-slate-800">{s.courseName}</span>
          <div className="font-mono text-xs text-slate-500">
            {s.courseCode} • {s.totalCredits ?? 3} Tín chỉ
          </div>
        </div>
      ),
    },
    {
      header: 'Giảng Viên',
      render: (s) => (
        <span className="text-xs text-slate-700 font-medium">
          {s.lecturerName ? `GV. ${s.lecturerName}` : <span className="text-slate-400 italic">Chưa phân công</span>}
        </span>
      ),
    },
    {
      header: 'Sĩ Số / Sức Chứa',
      render: (s) => {
        const enrolled = s.enrolledCount || 0;
        const cap = s.capacity || s.maxCapacity || 0;
        const isFull = enrolled >= cap;
        return (
          <div className="text-xs font-mono">
            <span className={`font-bold ${isFull ? 'text-rose-600' : 'text-emerald-700'}`}>
              {enrolled}
            </span>
            <span className="text-slate-400"> / {cap}</span>
          </div>
        );
      },
      align: 'center',
    },
    {
      header: 'Thời Khóa Biểu (Slots)',
      render: (s) => {
        const slots = s.scheduleSlots || [];
        if (slots.length === 0) {
          return <span className="text-xs text-amber-600 font-mono italic">Chưa xếp lịch</span>;
        }
        return (
          <div className="space-y-1">
            {slots.map((slot) => (
              <div
                key={slot.id}
                className="text-[11px] font-mono bg-slate-50 text-slate-700 px-2 py-0.5 rounded border border-slate-200"
              >
                {slot.dayOfWeek} (T.{slot.startPeriod}-{slot.endPeriod}) • {slot.roomNumber || 'Chưa gán phòng'}
              </div>
            ))}
          </div>
        );
      },
    },
    {
      header: 'Trạng Thái',
      render: (s) => <Badge variant={s.status}>{s.status}</Badge>,
      align: 'center',
    },
    {
      header: 'Hành Động',
      render: (s) => (
        <div className="flex items-center space-x-1.5 justify-end">
          <Button
            size="sm"
            variant="secondary"
            icon={CalendarPlus}
            title="Quản lý lịch học"
            onClick={() => {
              setSelectedSectionForSlot(s);
              setSlotError(null);
            }}
          >
            Lịch
          </Button>
          <Button
            size="sm"
            variant="secondary"
            icon={Edit3}
            title="Sửa thông tin lớp (Mã lớp, Giảng viên, Sức chứa)"
            onClick={() => handleOpenEdit(s)}
          >
            Sửa
          </Button>
          {s.status !== 'CANCELLED' && (
            <Button
              size="sm"
              variant="secondary"
              icon={XCircle}
              title="Hủy lớp học phần"
              onClick={() => handleCancelSection(s)}
            >
              Hủy
            </Button>
          )}
          {s.enrolledCount === 0 && (
            <Button
              size="sm"
              variant="danger"
              icon={Trash2}
              title="Xóa lớp học phần rỗng"
              onClick={() => {
                setDeleteSection(s);
                setDeleteError(null);
              }}
            >
              Xóa
            </Button>
          )}
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
            <Layers className="w-5 h-5 text-indigo-600" />
            <span>Quản lý Lớp học phần & Xếp lịch (UC-09, UC-10, UC-11)</span>
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Mở lớp, chỉnh sửa giảng viên, xếp lịch học từng buổi và kiểm soát sức chứa phòng học
          </p>
        </div>
        <div className="flex items-center space-x-2">
          <Button variant="secondary" size="sm" icon={RefreshCw} onClick={() => fetchSections(selectedSemesterId)}>
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
            Mở Lớp Học Phần Mới
          </Button>
        </div>
      </div>

      {error && <BackendErrorAlert error={error} onClose={() => setError(null)} />}

      {/* Semester Selector Card */}
      <Card bodyClassName="p-4">
        <div className="flex items-center space-x-3">
          <label className="text-xs font-bold text-slate-700 uppercase tracking-wider">Học kỳ hiển thị:</label>
          <select
            value={selectedSemesterId}
            onChange={(e) => setSelectedSemesterId(e.target.value)}
            className="px-3 py-1.5 text-xs rounded-lg border border-slate-300 font-medium text-slate-900 bg-white min-w-[240px]"
          >
            {semesters.map((s) => (
              <option key={s.id} value={s.id}>
                {s.code || s.semesterCode} - {s.name || s.semesterName}
              </option>
            ))}
          </select>
          <span className="text-xs text-slate-400 font-mono">
            Tổng cộng: {sections.length} lớp học phần trong kỳ
          </span>
        </div>
      </Card>

      <Card title={`Danh sách Lớp học phần (${sections.length})`}>
        {loading ? (
          <div className="py-12 text-center text-xs text-slate-400">Đang tải danh sách lớp học phần...</div>
        ) : (
          <Table columns={columns} data={sections} />
        )}
      </Card>

      {/* Modal Create Section (UC-09) */}
      <Modal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        title="Mở Lớp Học Phần Mới (UC-09)"
        footer={
          <>
            <Button variant="secondary" size="sm" onClick={() => setShowCreateModal(false)}>
              Hủy
            </Button>
            <Button variant="primary" size="sm" loading={createLoading} onClick={handleCreateSection}>
              Mở Lớp
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          {createError && <BackendErrorAlert error={createError} onClose={() => setCreateError(null)} />}

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Môn Học *</label>
            <select
              value={sectionForm.courseId}
              onChange={(e) => setSectionForm({ ...sectionForm, courseId: e.target.value })}
              className="w-full px-3 py-2 text-xs rounded border border-slate-300 font-medium text-slate-900 bg-white"
            >
              <option value="">-- Chọn Môn học mở lớp --</option>
              {courses.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.code || c.courseCode} - {c.name || c.courseName} ({c.totalCredits || c.credits || 3} TC)
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Học Kỳ Áp Dụng *</label>
            <select
              value={sectionForm.semesterId || selectedSemesterId}
              onChange={(e) => setSectionForm({ ...sectionForm, semesterId: e.target.value })}
              className="w-full px-3 py-2 text-xs rounded border border-slate-300 text-slate-900 bg-white"
            >
              {semesters.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.code || s.semesterCode} - {s.name || s.semesterName}
                </option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Mã Lớp Học Phần (Unique) *
              </label>
              <input
                type="text"
                value={sectionForm.sectionCode}
                onChange={(e) => setSectionForm({ ...sectionForm, sectionCode: e.target.value })}
                placeholder="ví dụ: CS101.N01, MATH201.01"
                className="w-full px-3 py-2 text-xs rounded border border-slate-300 font-mono text-slate-900 bg-white"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Chỉ Tiêu Sức Chứa (C_max) *
              </label>
              <input
                type="number"
                min="1"
                value={sectionForm.capacity}
                onChange={(e) => setSectionForm({ ...sectionForm, capacity: e.target.value })}
                placeholder="ví dụ: 50 (sinh viên)"
                className="w-full px-3 py-2 text-xs rounded border border-slate-300 font-mono text-slate-900 bg-white"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Phân Công Giảng Viên
            </label>
            <select
              value={sectionForm.lecturerId}
              onChange={(e) => setSectionForm({ ...sectionForm, lecturerId: e.target.value })}
              className="w-full px-3 py-2 text-xs rounded border border-slate-300 text-slate-900 bg-white"
            >
              <option value="">-- Phân công giảng viên (tùy chọn) --</option>
              {lecturers.map((lec) => {
                const lId = lec.lecturerProfile?.id || lec.lecturerProfile?.lecturerProfileId || lec.id;
                return (
                  <option key={lec.id} value={lId}>
                    {lec.fullName} ({lec.username})
                  </option>
                );
              })}
            </select>
          </div>
        </div>
      </Modal>

      {/* Modal Edit Section (UC-09) */}
      <Modal
        isOpen={!!editSection}
        onClose={() => setEditSection(null)}
        title={`Chỉnh sửa Lớp: ${editSection?.sectionCode} (${editSection?.courseName})`}
        footer={
          <>
            <Button variant="secondary" size="sm" onClick={() => setEditSection(null)}>
              Hủy
            </Button>
            <Button variant="primary" size="sm" loading={editLoading} onClick={handleUpdateSection}>
              Cập nhật
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          {editError && <BackendErrorAlert error={editError} onClose={() => setEditError(null)} />}

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Mã Lớp Học Phần *
              </label>
              <input
                type="text"
                value={editForm.sectionCode}
                onChange={(e) => setEditForm({ ...editForm, sectionCode: e.target.value })}
                className="w-full px-3 py-2 text-xs rounded border border-slate-300 font-mono text-slate-900 bg-white"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Chỉ Tiêu Sức Chứa (C_max) *
              </label>
              <input
                type="number"
                min="1"
                value={editForm.capacity}
                onChange={(e) => setEditForm({ ...editForm, capacity: e.target.value })}
                className="w-full px-3 py-2 text-xs rounded border border-slate-300 font-mono text-slate-900 bg-white"
              />
              <p className="text-[11px] text-slate-500 mt-0.5">
                Đã đăng ký: {editSection?.enrolledCount || 0} SV
              </p>
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Phân Công Giảng Viên
            </label>
            <select
              value={editForm.lecturerId}
              onChange={(e) => setEditForm({ ...editForm, lecturerId: e.target.value })}
              className="w-full px-3 py-2 text-xs rounded border border-slate-300 text-slate-900 bg-white"
            >
              <option value="">-- Chưa phân công --</option>
              {lecturers.map((lec) => {
                const lId = lec.lecturerProfile?.id || lec.lecturerProfile?.lecturerProfileId || lec.id;
                return (
                  <option key={lec.id} value={lId}>
                    {lec.fullName} ({lec.username})
                  </option>
                );
              })}
            </select>
            <p className="text-[11px] text-slate-500 mt-1">
              Hệ thống tự động kiểm tra trùng lịch dạy của giảng viên với các buổi học hiện tại của lớp.
            </p>
          </div>
        </div>
      </Modal>

      {/* Modal Delete Section (UC-09) */}
      <Modal
        isOpen={!!deleteSection}
        onClose={() => setDeleteSection(null)}
        title="Xác nhận xóa Lớp Học Phần"
        footer={
          <>
            <Button variant="secondary" size="sm" onClick={() => setDeleteSection(null)}>
              Hủy
            </Button>
            <Button variant="danger" size="sm" loading={deleteLoading} onClick={handleDeleteSection}>
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
              <p className="font-semibold">Bạn có chắc chắn muốn xóa vĩnh viễn lớp học phần này?</p>
              <p className="mt-1 text-slate-600">
                Lớp: <span className="font-bold text-slate-900">{deleteSection?.sectionCode}</span> ({deleteSection?.courseName}).
              </p>
              <p className="mt-1 text-slate-500">
                Lưu ý: Chỉ cho phép xóa khi lớp chưa có sinh viên nào đăng ký (sĩ số = 0). Nếu lớp đã có sinh viên, hãy chọn chức năng "Hủy lớp".
              </p>
            </div>
          </div>
        </div>
      </Modal>

      {/* Modal Manage Schedule Slots (UC-10) */}
      <Modal
        isOpen={!!selectedSectionForSlot}
        onClose={() => setSelectedSectionForSlot(null)}
        title={`Quản Lý Lịch Học: [${selectedSectionForSlot?.sectionCode}] ${selectedSectionForSlot?.courseName}`}
        footer={
          <Button variant="secondary" size="sm" onClick={() => setSelectedSectionForSlot(null)}>
            Đóng
          </Button>
        }
      >
        <div className="space-y-5">
          {slotError && <BackendErrorAlert error={slotError} onClose={() => setSlotError(null)} />}

          {/* Current Schedule Slots List */}
          <div>
            <h4 className="text-xs font-bold text-slate-800 uppercase tracking-wider mb-2">
              Các Buổi Học Hiện Tại ({(selectedSectionForSlot?.scheduleSlots || []).length}):
            </h4>
            {(selectedSectionForSlot?.scheduleSlots || []).length === 0 ? (
              <div className="py-4 text-center text-xs text-slate-400 border border-dashed rounded-lg bg-slate-50">
                Lớp học phần này hiện chưa được xếp buổi học nào.
              </div>
            ) : (
              <div className="space-y-2">
                {(selectedSectionForSlot?.scheduleSlots || []).map((slot) => (
                  <div
                    key={slot.id}
                    className="flex items-center justify-between p-2.5 rounded-lg border border-slate-200 bg-white text-xs"
                  >
                    <div>
                      <span className="font-bold text-indigo-700 mr-2">{slot.dayOfWeek}</span>
                      <span className="font-mono text-slate-800 mr-2">Tiết {slot.startPeriod} - {slot.endPeriod}</span>
                      <span className="text-slate-500">
                        • Phòng: <span className="font-semibold text-slate-700">{slot.roomNumber} ({slot.building})</span>
                        <span className="ml-1 text-[11px] text-slate-400">(sức chứa {slot.roomCapacity} chỗ)</span>
                      </span>
                    </div>
                    <Button
                      size="sm"
                      variant="danger"
                      onClick={() => handleDeleteSlot(slot.id)}
                      loading={slotLoading}
                    >
                      Xóa buổi này
                    </Button>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Form Add Slot */}
          <form onSubmit={handleAddSlot} className="bg-slate-50 p-4 rounded-lg border border-slate-200 space-y-3">
            <h4 className="text-xs font-bold text-slate-800 uppercase tracking-wider">
              Thêm Buổi Học Mới:
            </h4>
            <div className="p-2.5 bg-amber-50 rounded border border-amber-200 text-[11px] text-amber-800">
              Kiểm tra xung đột tự động: Phòng học và giảng viên không được trùng khung giờ với lớp khác trong kỳ. Sức chứa phòng phải &ge; chỉ tiêu lớp ({selectedSectionForSlot?.capacity || selectedSectionForSlot?.maxCapacity || 0} chỗ).
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Phòng Học *</label>
              <select
                value={slotForm.roomId}
                onChange={(e) => setSlotForm({ ...slotForm, roomId: e.target.value })}
                className="w-full px-3 py-2 text-xs rounded border border-slate-300 font-medium text-slate-900 bg-white"
              >
                <option value="">-- Chọn Phòng học bố trí --</option>
                {rooms.map((r) => (
                  <option key={r.id} value={r.id}>
                    {r.building} - {r.roomNumber} (Sức chứa: {r.capacity} chỗ)
                  </option>
                ))}
              </select>
            </div>

            <div className="grid grid-cols-3 gap-3">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Thứ Trong Tuần *</label>
                <select
                  value={slotForm.dayOfWeek}
                  onChange={(e) => setSlotForm({ ...slotForm, dayOfWeek: e.target.value })}
                  className="w-full px-2.5 py-1.5 text-xs rounded border border-slate-300 text-slate-900 bg-white"
                >
                  <option value="MONDAY">Thứ Hai</option>
                  <option value="TUESDAY">Thứ Ba</option>
                  <option value="WEDNESDAY">Thứ Tư</option>
                  <option value="THURSDAY">Thứ Năm</option>
                  <option value="FRIDAY">Thứ Sáu</option>
                  <option value="SATURDAY">Thứ Bảy</option>
                  <option value="SUNDAY">Chủ Nhật</option>
                </select>
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Tiết Bắt Đầu (1-12) *</label>
                <input
                  type="number"
                  min="1"
                  max="12"
                  value={slotForm.startPeriod}
                  onChange={(e) => setSlotForm({ ...slotForm, startPeriod: e.target.value })}
                  className="w-full px-2.5 py-1.5 text-xs rounded border border-slate-300 font-mono text-slate-900 bg-white"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Tiết Kết Thúc *</label>
                <input
                  type="number"
                  min="1"
                  max="12"
                  value={slotForm.endPeriod}
                  onChange={(e) => setSlotForm({ ...slotForm, endPeriod: e.target.value })}
                  className="w-full px-2.5 py-1.5 text-xs rounded border border-slate-300 font-mono text-slate-900 bg-white"
                />
              </div>
            </div>

            <div className="flex justify-end pt-2">
              <Button type="submit" variant="primary" size="sm" loading={slotLoading} disabled={!slotForm.roomId}>
                Thêm Buổi Học
              </Button>
            </div>
          </form>
        </div>
      </Modal>

      {/* Modal Edit Capacity (UC-11) */}
      <Modal
        isOpen={!!selectedSectionForCap}
        onClose={() => setSelectedSectionForCap(null)}
        title={`Điều Chỉnh Sức Chứa: ${selectedSectionForCap?.sectionCode}`}
        footer={
          <>
            <Button variant="secondary" size="sm" onClick={() => setSelectedSectionForCap(null)}>
              Hủy
            </Button>
            <Button variant="primary" size="sm" loading={capLoading} onClick={handleUpdateCapacity}>
              Cập Nhật Sức Chứa
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          {capError && <BackendErrorAlert error={capError} onClose={() => setCapError(null)} />}

          <div className="text-xs text-slate-600 bg-slate-50 p-3 rounded border">
            <div>Số sinh viên đã đăng ký hiện tại: <span className="font-bold text-slate-900">{selectedSectionForCap?.enrolledCount || 0}</span></div>
            <div>Sức chứa chỉ tiêu cũ: <span className="font-bold text-slate-900">{selectedSectionForCap?.capacity || selectedSectionForCap?.maxCapacity}</span></div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Chỉ Tiêu Sức Chứa Mới (C_max) *
            </label>
            <input
              type="number"
              min="1"
              value={newCapacity}
              onChange={(e) => setNewCapacity(e.target.value)}
              className="w-full px-3 py-2 text-xs rounded border border-slate-300 font-mono text-slate-900 bg-white"
            />
            <p className="text-[11px] text-slate-500 mt-1">
              BR-09: Không được phép hạ sức chứa thấp hơn số sinh viên hiện đã đăng ký ({selectedSectionForCap?.enrolledCount || 0}).
            </p>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default SectionsPage;
