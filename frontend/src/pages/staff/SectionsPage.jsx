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
import { Layers, Plus, CalendarPlus, Edit3, XCircle, RefreshCw } from 'lucide-react';

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

  // Modal Add Schedule Slot (UC-10)
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

  const handleAddSlot = async (e) => {
    e.preventDefault();
    setSlotLoading(true);
    setSlotError(null);
    try {
      await schedulingApi.addScheduleSlot(selectedSectionForSlot.id, {
        roomId: slotForm.roomId ? Number(slotForm.roomId) : '',
        dayOfWeek: slotForm.dayOfWeek,
        startPeriod: slotForm.startPeriod ? Number(slotForm.startPeriod) : '',
        endPeriod: slotForm.endPeriod ? Number(slotForm.endPeriod) : '',
      });
      setSelectedSectionForSlot(null);
      setSlotForm({ roomId: '', dayOfWeek: 'MONDAY', startPeriod: '1', endPeriod: '3' });
      fetchSections(selectedSemesterId);
    } catch (err) {
      console.error('Schedule slot failed (collision):', err);
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
      console.error('Update capacity failed:', err);
      setCapError(err);
    } finally {
      setCapLoading(false);
    }
  };

  const handleCancelSection = async (section) => {
    if (!window.confirm(`Bạn có chắc chắn muốn hủy Lớp học phần ${section.sectionCode}?`)) return;
    try {
      await schedulingApi.cancelSection(section.id);
      fetchSections(selectedSemesterId);
    } catch (err) {
      setError(err);
    }
  };

  const columns = [
    {
      header: 'Mã Lớp Học Phần',
      render: (s) => (
        <div>
          <span className="font-mono font-bold text-indigo-700">{s.sectionCode}</span>
          <div className="text-xs text-slate-400">ID: {s.id}</div>
        </div>
      ),
    },
    {
      header: 'Môn Học',
      render: (s) => (
        <div>
          <div className="font-semibold text-slate-800">{s.courseName}</div>
          <div className="text-xs font-mono text-slate-500">{s.courseCode} ({s.credits} TC)</div>
        </div>
      ),
    },
    {
      header: 'Phòng Học',
      render: (s) => (
        <span className="font-mono text-xs bg-slate-100 text-slate-700 px-2 py-0.5 rounded border border-slate-200">
          {s.building} - P.{s.roomNumber}
        </span>
      ),
    },
    {
      header: 'Sĩ Số / Sức Chứa',
      render: (s) => {
        const isFull = s.enrolledCount >= s.maxCapacity;
        return (
          <div className="text-xs">
            <span className={`font-mono font-bold ${isFull ? 'text-rose-600' : 'text-emerald-700'}`}>
              {s.enrolledCount} / {s.maxCapacity}
            </span>
            {isFull && (
              <span className="ml-1 text-[10px] font-bold text-rose-600 bg-rose-50 px-1 rounded">
                HẾT CHỖ
              </span>
            )}
          </div>
        );
      },
      align: 'center',
    },
    {
      header: 'Lịch Học (Slots)',
      render: (s) => {
        if (!s.scheduleSlots || s.scheduleSlots.length === 0) {
          return <span className="text-xs text-amber-600 italic">Chưa xếp lịch</span>;
        }
        return (
          <div className="space-y-1">
            {s.scheduleSlots.map((slot, idx) => (
              <div key={idx} className="text-[11px] font-mono text-slate-700">
                • Thứ {slot.dayOfWeek} (Tiết {slot.startPeriod}-{slot.endPeriod}) - GV: {slot.lecturerName || `ID ${slot.lecturerId}`}
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
            title="Thêm slot lịch học (Xung đột TKB sẽ ném lỗi)"
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
            title="Điều chỉnh sức chứa C_max"
            onClick={() => {
              setSelectedSectionForCap(s);
              setNewCapacity(String(s.maxCapacity));
              setCapError(null);
            }}
          >
            Sức chứa
          </Button>
          {s.status !== 'CANCELLED' && (
            <Button
              size="sm"
              variant="danger"
              icon={XCircle}
              title="Hủy lớp học phần"
              onClick={() => handleCancelSection(s)}
            >
              Hủy
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
            Mở lớp, kiểm soát sức chứa phòng (BR-09), phát hiện xung đột lịch giảng viên & phòng học
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
            className="px-3 py-1.5 text-xs rounded border border-slate-300 font-semibold text-slate-900 bg-white"
          >
            {semesters.map((sem) => (
              <option key={sem.id} value={sem.id}>
                {sem.code || sem.semesterCode} - {sem.name || sem.semesterName}
              </option>
            ))}
          </select>
        </div>
      </Card>

      {/* Sections Table */}
      <Card title={`Danh sách Lớp học phần (${sections.length})`}>
        {loading ? (
          <div className="py-12 text-center text-xs text-slate-400">Đang tải danh sách lớp học phần...</div>
        ) : (
          <Table columns={columns} data={sections} />
        )}
      </Card>

      {/* Modal Open Section (UC-09) */}
      <Modal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        title="Mở Lớp Học Phần Mới (UC-09 - Kiểm tra sức chứa phòng BR-09)"
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
              <option value="">-- Chọn Môn học --</option>
              {courses.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.code || c.courseCode} - {c.name || c.courseName} ({c.totalCredits ?? (c.credits ?? 3)} TC)
                </option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Mã Lớp Học Phần *</label>
              <input
                type="text"
                value={sectionForm.sectionCode}
                onChange={(e) => setSectionForm({ ...sectionForm, sectionCode: e.target.value })}
                placeholder="ví dụ: CS101_01"
                className="w-full px-3 py-2 text-xs rounded border border-slate-300 font-mono text-slate-900 bg-white"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Học Kỳ *</label>
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
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Sức Chứa Lớp (Chỉ tiêu) *
              </label>
              <input
                type="number"
                min="1"
                value={sectionForm.capacity}
                onChange={(e) => setSectionForm({ ...sectionForm, capacity: e.target.value })}
                placeholder="ví dụ: 40 hoặc 50"
                className="w-full px-3 py-2 text-xs rounded border border-slate-300 font-mono text-slate-900 bg-white"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Giảng Viên Phụ Trách</label>
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
        </div>
      </Modal>

      {/* Modal Add Schedule Slot (UC-10 - Collision check) */}
      <Modal
        isOpen={!!selectedSectionForSlot}
        onClose={() => setSelectedSectionForSlot(null)}
        title={`Xếp Lịch Học: [${selectedSectionForSlot?.sectionCode}] ${selectedSectionForSlot?.courseName}`}
        footer={
          <>
            <Button variant="secondary" size="sm" onClick={() => setSelectedSectionForSlot(null)}>
              Hủy
            </Button>
            <Button variant="primary" size="sm" loading={slotLoading} onClick={handleAddSlot}>
              Lưu Lịch Học
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          {slotError && <BackendErrorAlert error={slotError} onClose={() => setSlotError(null)} />}

          <div className="p-3 bg-amber-50 rounded border border-amber-200 text-xs text-amber-800">
            <strong>Kiểm Tra Xung Đột Lịch (BR-03 & Conflict Algorithm):</strong> Nếu phòng học hoặc giảng viên đã có lịch trong cùng khung giờ, Backend sẽ trả về lỗi <code className="font-bold text-rose-700">ERR_ROOM_SCHEDULE_CONFLICT</code> hoặc <code className="font-bold text-rose-700">ERR_LECTURER_SCHEDULE_CONFLICT</code>. Sức chứa phòng phải lớn hơn hoặc bằng sức chứa lớp ({selectedSectionForSlot?.capacity || selectedSectionForSlot?.maxCapacity || 0} chỗ).
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
                <option value="MONDAY">Thứ Hai (Monday)</option>
                <option value="TUESDAY">Thứ Ba (Tuesday)</option>
                <option value="WEDNESDAY">Thứ Tư (Wednesday)</option>
                <option value="THURSDAY">Thứ Năm (Thursday)</option>
                <option value="FRIDAY">Thứ Sáu (Friday)</option>
                <option value="SATURDAY">Thứ Bảy (Saturday)</option>
                <option value="SUNDAY">Chủ Nhật (Sunday)</option>
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
        </div>
      </Modal>

      {/* Modal Edit Capacity (UC-11) */}
      <Modal
        isOpen={!!selectedSectionForCap}
        onClose={() => setSelectedSectionForCap(null)}
        title={`Điều Chỉnh Sức Chứa: [${selectedSectionForCap?.sectionCode}]`}
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

          <p className="text-xs text-slate-600">
            Số sinh viên đang theo học hiện tại: <strong className="font-bold text-indigo-700">{selectedSectionForCap?.enrolledCount}</strong>.
            Sức chứa mới không được nhỏ hơn số sinh viên đã đăng ký (thử nhập số nhỏ hơn để test <code className="font-bold text-rose-700">ERR_INVALID_CAPACITY_BOUND</code>).
          </p>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Sức Chứa Mới (C_new) *
            </label>
            <input
              type="number"
              value={newCapacity}
              onChange={(e) => setNewCapacity(e.target.value)}
              className="w-full px-3 py-2 text-xs rounded border border-slate-300 font-mono text-slate-900 bg-white"
            />
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default SectionsPage;
