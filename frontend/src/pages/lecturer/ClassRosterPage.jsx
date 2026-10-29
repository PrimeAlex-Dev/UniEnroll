import React, { useState, useEffect } from 'react';
import { reportingApi } from '../../api/reportingApi';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Table from '../../components/common/Table';
import Badge from '../../components/common/Badge';
import BackendErrorAlert from '../../components/common/BackendErrorAlert';
import { Users, Search, RefreshCw, Printer } from 'lucide-react';

const ClassRosterPage = () => {
  const [sectionIdInput, setSectionIdInput] = useState('1');
  const [rosterData, setRosterData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchRoster = async (id) => {
    if (!id) return;
    setLoading(true);
    setError(null);
    try {
      const res = await reportingApi.getLecturerRoster(Number(id));
      setRosterData(res.data || null);
    } catch (err) {
      console.error('Fetch roster failed:', err);
      // Renders ERR_UNAUTHORIZED_SECTION_ACCESS if section belongs to another lecturer
      setError(err);
      setRosterData(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRoster(sectionIdInput);
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    fetchRoster(sectionIdInput);
  };

  const studentColumns = [
    {
      header: 'STT',
      render: (_, idx) => <span className="font-mono text-xs text-slate-400">{idx + 1}</span>,
      align: 'center',
    },
    {
      header: 'Mã Sinh Viên',
      render: (s) => <span className="font-mono font-bold text-indigo-700">{s.studentCode}</span>,
    },
    {
      header: 'Họ và Tên',
      render: (s) => <span className="font-semibold text-slate-800">{s.fullName}</span>,
    },
    {
      header: 'Email',
      accessor: 'email',
      render: (s) => <span className="text-xs text-slate-600">{s.email}</span>,
    },
    {
      header: 'Chuyên Ngành',
      accessor: 'major',
      render: (s) => <span className="text-xs text-slate-700">{s.major || 'CNTT'}</span>,
    },
    {
      header: 'Thời Gian Ghi Danh',
      render: (s) => (
        <span className="font-mono text-xs text-slate-500">
          {s.enrolledAt ? new Date(s.enrolledAt).toLocaleString('vi-VN') : '—'}
        </span>
      ),
    },
    {
      header: 'Trạng Thái',
      render: (s) => <Badge variant={s.enrollmentStatus}>{s.enrollmentStatus}</Badge>,
      align: 'center',
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight flex items-center space-x-2">
            <Users className="w-5 h-5 text-indigo-600" />
            <span>Danh Sách Điểm Danh Lớp Học Phần (UC-17)</span>
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Tra cứu danh sách sinh viên ghi danh và tỷ lệ lấp đầy của lớp do Giảng viên phụ trách
          </p>
        </div>
        <div className="flex items-center space-x-2">
          <Button variant="secondary" size="sm" icon={Printer} onClick={() => window.print()}>
            In Bảng Điểm Danh
          </Button>
          <Button
            variant="secondary"
            size="sm"
            icon={RefreshCw}
            onClick={() => fetchRoster(sectionIdInput)}
          >
            Làm mới
          </Button>
        </div>
      </div>

      {/* Backend Error Alert Renderer (e.g. ERR_UNAUTHORIZED_SECTION_ACCESS) */}
      {error && <BackendErrorAlert error={error} onClose={() => setError(null)} />}

      {/* Section ID Search Toolbar */}
      <Card bodyClassName="p-4">
        <form onSubmit={handleSearch} className="flex items-center space-x-3">
          <label className="text-xs font-bold text-slate-700 uppercase tracking-wider">
            Nhập ID Lớp Học Phần:
          </label>
          <input
            type="number"
            value={sectionIdInput}
            onChange={(e) => setSectionIdInput(e.target.value)}
            placeholder="ví dụ: 1, 2, 3..."
            className="w-32 px-3 py-1.5 text-xs rounded border border-slate-300 font-mono text-slate-900 bg-white"
          />
          <Button type="submit" variant="primary" size="sm" icon={Search} loading={loading}>
            Truy Cập Danh Sách
          </Button>
          <span className="text-[11px] text-slate-400 italic">
            (Nhập ID lớp của giảng viên khác để kiểm thử lỗi phân quyền ERR_UNAUTHORIZED_SECTION_ACCESS)
          </span>
        </form>
      </Card>

      {/* Roster Overview Details */}
      {rosterData && (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
            <Card bodyClassName="p-4">
              <span className="text-[11px] text-slate-500 font-semibold uppercase block">Mã Lớp Học Phần</span>
              <span className="text-lg font-mono font-bold text-indigo-700">{rosterData.sectionCode}</span>
              <div className="text-xs text-slate-500 font-medium">{rosterData.courseName}</div>
            </Card>

            <Card bodyClassName="p-4">
              <span className="text-[11px] text-slate-500 font-semibold uppercase block">Học Kỳ & Phòng</span>
              <span className="text-base font-semibold text-slate-800">{rosterData.semesterCode || rosterData.semesterName}</span>
              <div className="text-xs text-slate-500 font-mono">Phòng: {rosterData.roomNumber}</div>
            </Card>

            <Card bodyClassName="p-4">
              <span className="text-[11px] text-slate-500 font-semibold uppercase block">Sĩ Số Đã Đăng Ký</span>
              <div className="text-lg font-mono font-bold text-emerald-600">
                {rosterData.enrolledCount} / {rosterData.maxCapacity} <span className="text-xs font-normal text-slate-500">Sinh viên</span>
              </div>
            </Card>

            <Card bodyClassName="p-4">
              <span className="text-[11px] text-slate-500 font-semibold uppercase block">Tỷ Lệ Lấp Đầy</span>
              <div className="text-lg font-mono font-bold text-indigo-600">
                {rosterData.fillRatePercentage?.toFixed(1)}%
              </div>
            </Card>
          </div>

          <Card title={`Danh sách Sinh viên Ghi danh (${rosterData.students?.length || 0})`}>
            <Table columns={studentColumns} data={rosterData.students} />
          </Card>
        </>
      )}
    </div>
  );
};

export default ClassRosterPage;
