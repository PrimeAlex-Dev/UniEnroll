import React, { useState, useEffect } from 'react';
import { schedulingApi } from '../../api/schedulingApi';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Table from '../../components/common/Table';
import Modal from '../../components/common/Modal';
import Badge from '../../components/common/Badge';
import BackendErrorAlert from '../../components/common/BackendErrorAlert';
import { Calendar, DoorClosed, Plus, RefreshCw, Edit3, Trash2, AlertTriangle, ArrowRight } from 'lucide-react';

const InfrastructurePage = () => {
  const [semesters, setSemesters] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Semester Create Modal
  const [showSemesterModal, setShowSemesterModal] = useState(false);
  const [semLoading, setSemLoading] = useState(false);
  const [semError, setSemError] = useState(null);
  const [semForm, setSemForm] = useState({
    code: '',
    name: '',
    startDate: '',
    endDate: '',
  });

  // Semester Edit Modal
  const [editSem, setEditSem] = useState(null);
  const [editSemLoading, setEditSemLoading] = useState(false);
  const [editSemError, setEditSemError] = useState(null);
  const [editSemForm, setEditSemForm] = useState({
    code: '',
    name: '',
    startDate: '',
    endDate: '',
    status: 'PLANNED',
  });

  // Semester Delete Modal
  const [deleteSem, setDeleteSem] = useState(null);
  const [deleteSemLoading, setDeleteSemLoading] = useState(false);
  const [deleteSemError, setDeleteSemError] = useState(null);

  // Room Create Modal
  const [showRoomModal, setShowRoomModal] = useState(false);
  const [roomLoading, setRoomLoading] = useState(false);
  const [roomError, setRoomError] = useState(null);
  const [roomForm, setRoomForm] = useState({
    building: '',
    roomNumber: '',
    capacity: '',
  });

  // Room Edit Modal
  const [editRoom, setEditRoom] = useState(null);
  const [editRoomLoading, setEditRoomLoading] = useState(false);
  const [editRoomError, setEditRoomError] = useState(null);
  const [editRoomForm, setEditRoomForm] = useState({
    building: '',
    roomNumber: '',
    capacity: '',
  });

  // Room Delete Modal
  const [deleteRoom, setDeleteRoom] = useState(null);
  const [deleteRoomLoading, setDeleteRoomLoading] = useState(false);
  const [deleteRoomError, setDeleteRoomError] = useState(null);

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [semRes, roomRes] = await Promise.all([
        schedulingApi.getSemesters(),
        schedulingApi.getRooms(),
      ]);
      setSemesters(semRes.data || []);
      setRooms(roomRes.data || []);
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

  // Semester Handlers
  const handleCreateSemester = async (e) => {
    e.preventDefault();
    setSemLoading(true);
    setSemError(null);
    try {
      await schedulingApi.createSemester({
        code: semForm.code.trim(),
        name: semForm.name.trim(),
        startDate: semForm.startDate,
        endDate: semForm.endDate,
      });
      setShowSemesterModal(false);
      setSemForm({ code: '', name: '', startDate: '', endDate: '' });
      fetchData();
    } catch (err) {
      setSemError(err);
    } finally {
      setSemLoading(false);
    }
  };

  const handleOpenEditSem = (sem) => {
    setEditSem(sem);
    setEditSemForm({
      code: sem.code || sem.semesterCode || '',
      name: sem.name || sem.semesterName || '',
      startDate: sem.startDate || '',
      endDate: sem.endDate || '',
      status: sem.status || 'PLANNED',
    });
    setEditSemError(null);
  };

  const handleUpdateSemester = async (e) => {
    e.preventDefault();
    if (!editSem) return;
    setEditSemLoading(true);
    setEditSemError(null);
    try {
      await schedulingApi.updateSemester(editSem.id, {
        code: editSemForm.code.trim(),
        name: editSemForm.name.trim(),
        startDate: editSemForm.startDate,
        endDate: editSemForm.endDate,
        status: editSemForm.status,
      });
      setEditSem(null);
      fetchData();
    } catch (err) {
      setEditSemError(err);
    } finally {
      setEditSemLoading(false);
    }
  };

  const handleChangeSemStatus = async (semId, newStatus) => {
    try {
      await schedulingApi.updateSemesterStatus(semId, newStatus);
      fetchData();
    } catch (err) {
      console.error('Change status failed:', err);
      setError(err);
    }
  };

  const handleDeleteSemester = async () => {
    if (!deleteSem) return;
    setDeleteSemLoading(true);
    setDeleteSemError(null);
    try {
      await schedulingApi.deleteSemester(deleteSem.id);
      setDeleteSem(null);
      fetchData();
    } catch (err) {
      setDeleteSemError(err);
    } finally {
      setDeleteSemLoading(false);
    }
  };

  // Room Handlers
  const handleCreateRoom = async (e) => {
    e.preventDefault();
    setRoomLoading(true);
    setRoomError(null);
    try {
      await schedulingApi.createRoom({
        building: roomForm.building.trim(),
        roomNumber: roomForm.roomNumber.trim(),
        capacity: roomForm.capacity !== '' ? Number(roomForm.capacity) : null,
      });
      setShowRoomModal(false);
      setRoomForm({ building: '', roomNumber: '', capacity: '' });
      fetchData();
    } catch (err) {
      setRoomError(err);
    } finally {
      setRoomLoading(false);
    }
  };

  const handleOpenEditRoom = (room) => {
    setEditRoom(room);
    setEditRoomForm({
      building: room.building || '',
      roomNumber: room.roomNumber || '',
      capacity: String(room.capacity || ''),
    });
    setEditRoomError(null);
  };

  const handleUpdateRoom = async (e) => {
    e.preventDefault();
    if (!editRoom) return;
    setEditRoomLoading(true);
    setEditRoomError(null);
    try {
      await schedulingApi.updateRoom(editRoom.id, {
        building: editRoomForm.building.trim(),
        roomNumber: editRoomForm.roomNumber.trim(),
        capacity: editRoomForm.capacity !== '' ? Number(editRoomForm.capacity) : null,
      });
      setEditRoom(null);
      fetchData();
    } catch (err) {
      setEditRoomError(err);
    } finally {
      setEditRoomLoading(false);
    }
  };

  const handleDeleteRoom = async () => {
    if (!deleteRoom) return;
    setDeleteRoomLoading(true);
    setDeleteRoomError(null);
    try {
      await schedulingApi.deleteRoom(deleteRoom.id);
      setDeleteRoom(null);
      fetchData();
    } catch (err) {
      setDeleteRoomError(err);
    } finally {
      setDeleteRoomLoading(false);
    }
  };

  const semesterColumns = [
    {
      header: 'Mã & Tên Học Kỳ',
      render: (s) => (
        <div>
          <span className="font-mono font-bold text-indigo-700 mr-2">{s.code || s.semesterCode}</span>
          <span className="font-semibold text-slate-800">{s.name || s.semesterName}</span>
          <div className="font-mono text-[11px] text-slate-500 mt-0.5">
            {s.startDate} đến {s.endDate}
          </div>
        </div>
      ),
    },
    {
      header: 'Trạng Thái',
      render: (s) => <Badge variant={s.status}>{s.status}</Badge>,
      align: 'center',
    },
    {
      header: 'Thao tác',
      render: (s) => (
        <div className="flex items-center justify-end space-x-1">
          {s.status === 'PLANNED' && (
            <Button
              size="sm"
              variant="secondary"
              title="Kích hoạt học kỳ (ACTIVE)"
              onClick={() => handleChangeSemStatus(s.id, 'ACTIVE')}
            >
              Mở kỳ
            </Button>
          )}
          {s.status === 'ACTIVE' && (
            <Button
              size="sm"
              variant="secondary"
              title="Kết thúc học kỳ (FINISHED)"
              onClick={() => handleChangeSemStatus(s.id, 'FINISHED')}
            >
              Đóng kỳ
            </Button>
          )}
          <Button
            size="sm"
            variant="secondary"
            icon={Edit3}
            title="Sửa học kỳ"
            onClick={() => handleOpenEditSem(s)}
          >
            Sửa
          </Button>
          <Button
            size="sm"
            variant="danger"
            icon={Trash2}
            title="Xóa học kỳ"
            onClick={() => {
              setDeleteSem(s);
              setDeleteSemError(null);
            }}
          >
            Xóa
          </Button>
        </div>
      ),
      align: 'right',
    },
  ];

  const roomColumns = [
    {
      header: 'Phòng & Tòa nhà',
      render: (r) => (
        <div>
          <span className="font-mono font-bold text-indigo-700 mr-2">{r.roomNumber}</span>
          <span className="font-medium text-slate-700">{r.building}</span>
        </div>
      ),
    },
    {
      header: 'Sức Chứa',
      render: (r) => (
        <span className="font-mono font-bold text-xs bg-emerald-50 text-emerald-800 px-2 py-0.5 rounded border border-emerald-200">
          {r.capacity} chỗ
        </span>
      ),
      align: 'center',
    },
    {
      header: 'Thao tác',
      render: (r) => (
        <div className="flex items-center justify-end space-x-1">
          <Button
            size="sm"
            variant="secondary"
            icon={Edit3}
            title="Sửa phòng học"
            onClick={() => handleOpenEditRoom(r)}
          >
            Sửa
          </Button>
          <Button
            size="sm"
            variant="danger"
            icon={Trash2}
            title="Xóa phòng học"
            onClick={() => {
              setDeleteRoom(r);
              setDeleteRoomError(null);
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
            <Calendar className="w-5 h-5 text-indigo-600" />
            <span>Hạ Tầng Đào Tạo: Học Kỳ & Phòng Học (UC-08)</span>
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Quản trị thời gian học kỳ, trạng thái vòng đời và phòng học phục vụ kiểm soát sức chứa (BR-09)
          </p>
        </div>
        <div className="flex items-center space-x-2">
          <Button variant="secondary" size="sm" icon={RefreshCw} onClick={fetchData}>
            Làm mới
          </Button>
          <Button
            variant="secondary"
            size="sm"
            icon={Plus}
            onClick={() => {
              setRoomError(null);
              setShowRoomModal(true);
            }}
          >
            Thêm Phòng Học
          </Button>
          <Button
            variant="primary"
            size="sm"
            icon={Plus}
            onClick={() => {
              setSemError(null);
              setShowSemesterModal(true);
            }}
          >
            Thêm Học Kỳ Mới
          </Button>
        </div>
      </div>

      {error && <BackendErrorAlert error={error} onClose={() => setError(null)} />}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Semesters Card */}
        <Card
          title={`Danh mục Học kỳ (${semesters.length})`}
          action={
            <Button
              variant="primary"
              size="sm"
              icon={Plus}
              onClick={() => {
                setSemError(null);
                setShowSemesterModal(true);
              }}
            >
              Thêm Học Kỳ
            </Button>
          }
        >
          {loading ? (
            <div className="py-12 text-center text-xs text-slate-400">Đang nạp dữ liệu học kỳ...</div>
          ) : (
            <Table columns={semesterColumns} data={semesters} />
          )}
        </Card>

        {/* Rooms Card */}
        <Card
          title={`Cơ sở vật chất Phòng học (${rooms.length})`}
          action={
            <Button
              variant="primary"
              size="sm"
              icon={Plus}
              onClick={() => {
                setRoomError(null);
                setShowRoomModal(true);
              }}
            >
              Thêm Phòng Học
            </Button>
          }
        >
          {loading ? (
            <div className="py-12 text-center text-xs text-slate-400">Đang nạp dữ liệu phòng học...</div>
          ) : (
            <Table columns={roomColumns} data={rooms} />
          )}
        </Card>
      </div>

      {/* Modal Create Semester */}
      <Modal
        isOpen={showSemesterModal}
        onClose={() => setShowSemesterModal(false)}
        title="Thêm Học Kỳ Mới (UC-08)"
        footer={
          <>
            <Button variant="secondary" size="sm" onClick={() => setShowSemesterModal(false)}>
              Hủy
            </Button>
            <Button variant="primary" size="sm" loading={semLoading} onClick={handleCreateSemester}>
              Lưu Học Kỳ
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          {semError && <BackendErrorAlert error={semError} onClose={() => setSemError(null)} />}

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Mã Học Kỳ *</label>
            <input
              type="text"
              value={semForm.code}
              onChange={(e) => setSemForm({ ...semForm, code: e.target.value })}
              placeholder="ví dụ: 20261, 20262"
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 font-mono text-slate-900 bg-white"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Tên Học Kỳ *</label>
            <input
              type="text"
              value={semForm.name}
              onChange={(e) => setSemForm({ ...semForm, name: e.target.value })}
              placeholder="ví dụ: Học kỳ 1 Năm học 2026-2027"
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 text-slate-900 bg-white"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Ngày Bắt Đầu *</label>
              <input
                type="date"
                value={semForm.startDate}
                onChange={(e) => setSemForm({ ...semForm, startDate: e.target.value })}
                className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 font-mono text-slate-900 bg-white"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Ngày Kết Thúc *</label>
              <input
                type="date"
                value={semForm.endDate}
                onChange={(e) => setSemForm({ ...semForm, endDate: e.target.value })}
                className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 font-mono text-slate-900 bg-white"
              />
            </div>
          </div>
        </div>
      </Modal>

      {/* Modal Edit Semester */}
      <Modal
        isOpen={!!editSem}
        onClose={() => setEditSem(null)}
        title={`Chỉnh sửa Học Kỳ: ${editSem?.code || editSem?.semesterCode || ''}`}
        footer={
          <>
            <Button variant="secondary" size="sm" onClick={() => setEditSem(null)}>
              Hủy
            </Button>
            <Button variant="primary" size="sm" loading={editSemLoading} onClick={handleUpdateSemester}>
              Cập nhật
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          {editSemError && <BackendErrorAlert error={editSemError} onClose={() => setEditSemError(null)} />}

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Mã Học Kỳ *</label>
            <input
              type="text"
              value={editSemForm.code}
              onChange={(e) => setEditSemForm({ ...editSemForm, code: e.target.value })}
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 font-mono text-slate-900 bg-white"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Tên Học Kỳ *</label>
            <input
              type="text"
              value={editSemForm.name}
              onChange={(e) => setEditSemForm({ ...editSemForm, name: e.target.value })}
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 text-slate-900 bg-white"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Ngày Bắt Đầu *</label>
              <input
                type="date"
                value={editSemForm.startDate}
                onChange={(e) => setEditSemForm({ ...editSemForm, startDate: e.target.value })}
                className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 font-mono text-slate-900 bg-white"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Ngày Kết Thúc *</label>
              <input
                type="date"
                value={editSemForm.endDate}
                onChange={(e) => setEditSemForm({ ...editSemForm, endDate: e.target.value })}
                className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 font-mono text-slate-900 bg-white"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Trạng Thái Vòng Đời</label>
            <select
              value={editSemForm.status}
              onChange={(e) => setEditSemForm({ ...editSemForm, status: e.target.value })}
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 font-medium text-slate-900 bg-white"
            >
              <option value="PLANNED">PLANNED (Lên kế hoạch)</option>
              <option value="ACTIVE">ACTIVE (Đang diễn ra)</option>
              <option value="FINISHED">FINISHED (Đã kết thúc)</option>
              <option value="ARCHIVED">ARCHIVED (Lưu trữ)</option>
            </select>
          </div>
        </div>
      </Modal>

      {/* Modal Delete Semester */}
      <Modal
        isOpen={!!deleteSem}
        onClose={() => setDeleteSem(null)}
        title="Xác nhận xóa Học Kỳ"
        footer={
          <>
            <Button variant="secondary" size="sm" onClick={() => setDeleteSem(null)}>
              Hủy
            </Button>
            <Button variant="danger" size="sm" loading={deleteSemLoading} onClick={handleDeleteSemester}>
              Xác nhận Xóa
            </Button>
          </>
        }
      >
        <div className="space-y-3">
          {deleteSemError && <BackendErrorAlert error={deleteSemError} onClose={() => setDeleteSemError(null)} />}
          <div className="flex items-start space-x-3 text-amber-700 bg-amber-50 p-3 rounded-lg border border-amber-200 text-xs">
            <AlertTriangle className="w-5 h-5 flex-shrink-0 text-amber-600 mt-0.5" />
            <div>
              <p className="font-semibold">Bạn có chắc chắn muốn xóa học kỳ này?</p>
              <p className="mt-1 text-slate-600">
                Học kỳ: <span className="font-bold text-slate-900">{deleteSem?.name || deleteSem?.semesterName} ({deleteSem?.code || deleteSem?.semesterCode})</span>.
              </p>
              <p className="mt-1 text-slate-500">
                Lưu ý: Chỉ cho phép xóa nếu học kỳ chưa có lớp học phần nào mở và chưa có đợt đăng ký nào.
              </p>
            </div>
          </div>
        </div>
      </Modal>

      {/* Modal Create Room */}
      <Modal
        isOpen={showRoomModal}
        onClose={() => setShowRoomModal(false)}
        title="Thêm Phòng Học Mới (UC-08)"
        footer={
          <>
            <Button variant="secondary" size="sm" onClick={() => setShowRoomModal(false)}>
              Hủy
            </Button>
            <Button variant="primary" size="sm" loading={roomLoading} onClick={handleCreateRoom}>
              Lưu Phòng Học
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          {roomError && <BackendErrorAlert error={roomError} onClose={() => setRoomError(null)} />}

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Tòa Nhà *</label>
            <input
              type="text"
              value={roomForm.building}
              onChange={(e) => setRoomForm({ ...roomForm, building: e.target.value })}
              placeholder="ví dụ: Tòa H1, Tòa B, Tòa CNTT"
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 text-slate-900 bg-white"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Số Phòng Học *</label>
            <input
              type="text"
              value={roomForm.roomNumber}
              onChange={(e) => setRoomForm({ ...roomForm, roomNumber: e.target.value })}
              placeholder="ví dụ: 101, 202-H1, LAB-01"
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 font-mono text-slate-900 bg-white"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Sức Chứa Chỗ Ngồi (Chỉ tiêu tối đa) *</label>
            <input
              type="number"
              min="1"
              value={roomForm.capacity}
              onChange={(e) => setRoomForm({ ...roomForm, capacity: e.target.value })}
              placeholder="ví dụ: 60 (chỗ)"
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 font-mono text-slate-900 bg-white"
            />
          </div>
        </div>
      </Modal>

      {/* Modal Edit Room */}
      <Modal
        isOpen={!!editRoom}
        onClose={() => setEditRoom(null)}
        title={`Chỉnh sửa Phòng Học: ${editRoom?.roomNumber || ''} (${editRoom?.building || ''})`}
        footer={
          <>
            <Button variant="secondary" size="sm" onClick={() => setEditRoom(null)}>
              Hủy
            </Button>
            <Button variant="primary" size="sm" loading={editRoomLoading} onClick={handleUpdateRoom}>
              Cập nhật
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          {editRoomError && <BackendErrorAlert error={editRoomError} onClose={() => setEditRoomError(null)} />}

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Tòa Nhà *</label>
            <input
              type="text"
              value={editRoomForm.building}
              onChange={(e) => setEditRoomForm({ ...editRoomForm, building: e.target.value })}
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 text-slate-900 bg-white"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Số Phòng Học *</label>
            <input
              type="text"
              value={editRoomForm.roomNumber}
              onChange={(e) => setEditRoomForm({ ...editRoomForm, roomNumber: e.target.value })}
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 font-mono text-slate-900 bg-white"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Sức Chứa Chỗ Ngồi *</label>
            <input
              type="number"
              min="1"
              value={editRoomForm.capacity}
              onChange={(e) => setEditRoomForm({ ...editRoomForm, capacity: e.target.value })}
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 font-mono text-slate-900 bg-white"
            />
            <p className="text-[11px] text-slate-500 mt-1">
              Lưu ý: Không thể giảm sức chứa thấp hơn sĩ số tối đa của lớp học phần đang xếp ở phòng này.
            </p>
          </div>
        </div>
      </Modal>

      {/* Modal Delete Room */}
      <Modal
        isOpen={!!deleteRoom}
        onClose={() => setDeleteRoom(null)}
        title="Xác nhận xóa Phòng Học"
        footer={
          <>
            <Button variant="secondary" size="sm" onClick={() => setDeleteRoom(null)}>
              Hủy
            </Button>
            <Button variant="danger" size="sm" loading={deleteRoomLoading} onClick={handleDeleteRoom}>
              Xác nhận Xóa
            </Button>
          </>
        }
      >
        <div className="space-y-3">
          {deleteRoomError && <BackendErrorAlert error={deleteRoomError} onClose={() => setDeleteRoomError(null)} />}
          <div className="flex items-start space-x-3 text-amber-700 bg-amber-50 p-3 rounded-lg border border-amber-200 text-xs">
            <AlertTriangle className="w-5 h-5 flex-shrink-0 text-amber-600 mt-0.5" />
            <div>
              <p className="font-semibold">Bạn có chắc chắn muốn xóa phòng học này?</p>
              <p className="mt-1 text-slate-600">
                Phòng: <span className="font-bold text-slate-900">{deleteRoom?.roomNumber}</span> - Tòa <span className="font-bold text-slate-900">{deleteRoom?.building}</span> (Sức chứa: {deleteRoom?.capacity} chỗ).
              </p>
              <p className="mt-1 text-slate-500">
                Lưu ý: Hệ thống chỉ cho phép xóa phòng học nếu phòng chưa từng được xếp lịch cho lớp học phần nào.
              </p>
            </div>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default InfrastructurePage;
