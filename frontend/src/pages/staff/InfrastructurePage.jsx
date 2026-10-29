import React, { useState, useEffect } from 'react';
import { schedulingApi } from '../../api/schedulingApi';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Table from '../../components/common/Table';
import Modal from '../../components/common/Modal';
import BackendErrorAlert from '../../components/common/BackendErrorAlert';
import { Calendar, DoorClosed, Plus, RefreshCw } from 'lucide-react';

const InfrastructurePage = () => {
  const [semesters, setSemesters] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Semester Modal
  const [showSemesterModal, setShowSemesterModal] = useState(false);
  const [semLoading, setSemLoading] = useState(false);
  const [semError, setSemError] = useState(null);
  const [semForm, setSemForm] = useState({
    code: '',
    name: '',
    startDate: '',
    endDate: '',
  });

  // Room Modal
  const [showRoomModal, setShowRoomModal] = useState(false);
  const [roomLoading, setRoomLoading] = useState(false);
  const [roomError, setRoomError] = useState(null);
  const [roomForm, setRoomForm] = useState({
    building: '',
    roomNumber: '',
    capacity: '',
  });

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

  const semesterColumns = [
    {
      header: 'Mã Học Kỳ',
      render: (s) => <span className="font-mono font-bold text-indigo-700">{s.code || s.semesterCode}</span>,
    },
    {
      header: 'Tên Học Kỳ',
      render: (s) => <span className="font-semibold text-slate-800">{s.name || s.semesterName}</span>,
    },
    {
      header: 'Ngày bắt đầu',
      render: (s) => <span className="font-mono text-xs text-slate-600">{s.startDate}</span>,
    },
    {
      header: 'Ngày kết thúc',
      render: (s) => <span className="font-mono text-xs text-slate-600">{s.endDate}</span>,
    },
  ];

  const roomColumns = [
    {
      header: 'Tòa nhà',
      render: (r) => <span className="font-bold text-slate-800">{r.building}</span>,
    },
    {
      header: 'Số Phòng Học',
      render: (r) => <span className="font-mono font-bold text-indigo-700">{r.roomNumber}</span>,
    },
    {
      header: 'Sức Chứa Chỗ Ngồi (C_room)',
      render: (r) => (
        <span className="font-mono font-bold text-xs bg-emerald-50 text-emerald-800 px-2.5 py-1 rounded border border-emerald-200">
          {r.capacity} chỗ
        </span>
      ),
      align: 'center',
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
            Quản trị thời gian học kỳ và phòng học phục vụ xếp thời khóa biểu và kiểm soát sức chứa (BR-09)
          </p>
        </div>
        <Button variant="secondary" size="sm" icon={RefreshCw} onClick={fetchData}>
          Làm mới
        </Button>
      </div>

      {error && <BackendErrorAlert error={error} onClose={() => setError(null)} />}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Semesters Card */}
        <Card
          title={`Học kỳ Năm học (${semesters.length})`}
          action={
            <Button
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
          <Table columns={semesterColumns} data={semesters} />
        </Card>

        {/* Rooms Card */}
        <Card
          title={`Phòng học & Giảng đường (${rooms.length})`}
          action={
            <Button
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
          <Table columns={roomColumns} data={rooms} />
        </Card>
      </div>

      {/* Modal Add Semester */}
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
              placeholder="ví dụ: 2026_1"
              className="w-full px-3 py-2 text-xs rounded border border-slate-300 font-mono text-slate-900 bg-white"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Tên Học Kỳ *</label>
            <input
              type="text"
              value={semForm.name}
              onChange={(e) => setSemForm({ ...semForm, name: e.target.value })}
              placeholder="ví dụ: Học kỳ 1 Năm học 2026-2027"
              className="w-full px-3 py-2 text-xs rounded border border-slate-300 text-slate-900 bg-white"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Ngày Bắt Đầu *</label>
              <input
                type="date"
                value={semForm.startDate}
                onChange={(e) => setSemForm({ ...semForm, startDate: e.target.value })}
                className="w-full px-3 py-2 text-xs rounded border border-slate-300 text-slate-900 bg-white"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Ngày Kết Thúc *</label>
              <input
                type="date"
                value={semForm.endDate}
                onChange={(e) => setSemForm({ ...semForm, endDate: e.target.value })}
                className="w-full px-3 py-2 text-xs rounded border border-slate-300 text-slate-900 bg-white"
              />
            </div>
          </div>
        </div>
      </Modal>

      {/* Modal Add Room */}
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

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Tòa Nhà *</label>
              <input
                type="text"
                value={roomForm.building}
                onChange={(e) => setRoomForm({ ...roomForm, building: e.target.value })}
                placeholder="ví dụ: Tòa A, Tòa B"
                className="w-full px-3 py-2 text-xs rounded border border-slate-300 text-slate-900 bg-white"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Số Phòng *</label>
              <input
                type="text"
                value={roomForm.roomNumber}
                onChange={(e) => setRoomForm({ ...roomForm, roomNumber: e.target.value })}
                placeholder="ví dụ: A101, B202"
                className="w-full px-3 py-2 text-xs rounded border border-slate-300 font-mono text-slate-900 bg-white"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Sức Chứa Chỗ Ngồi (C_room) *
            </label>
            <input
              type="number"
              value={roomForm.capacity}
              onChange={(e) => setRoomForm({ ...roomForm, capacity: e.target.value })}
              placeholder="ví dụ: 40 (Sức chứa lớp mở không được vượt số này - BR-09)"
              className="w-full px-3 py-2 text-xs rounded border border-slate-300 font-mono text-slate-900 bg-white"
            />
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default InfrastructurePage;
