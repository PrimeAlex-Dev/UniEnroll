import React, { useState, useEffect } from 'react';
import { reportingApi } from '../../api/reportingApi';
import { schedulingApi } from '../../api/schedulingApi';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Table from '../../components/common/Table';
import Badge from '../../components/common/Badge';
import BackendErrorAlert from '../../components/common/BackendErrorAlert';
import { BarChart3, AlertTriangle, RefreshCw, Printer, CheckCircle } from 'lucide-react';

const OccupancyReportPage = () => {
  const [semesters, setSemesters] = useState([]);
  const [selectedSemesterId, setSelectedSemesterId] = useState('');
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const initData = async () => {
    try {
      const res = await schedulingApi.getSemesters();
      setSemesters(res.data || []);
      if (res.data?.length > 0) {
        setSelectedSemesterId(String(res.data[0].id));
      }
    } catch (err) {
      console.error(err);
      setError(err);
    }
  };

  const fetchReport = async (semesterId) => {
    if (!semesterId) return;
    setLoading(true);
    setError(null);
    try {
      const res = await reportingApi.getOccupancyReport(Number(semesterId));
      setReport(res.data || null);
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
      fetchReport(selectedSemesterId);
    }
  }, [selectedSemesterId]);

  const columns = [
    {
      header: 'Mã Lớp Học Phần',
      render: (s) => (
        <div>
          <span className="font-mono font-bold text-indigo-700">{s.sectionCode}</span>
          <div className="text-[11px] text-slate-400 font-mono">ID: {s.sectionId}</div>
        </div>
      ),
    },
    {
      header: 'Tên Học Phần',
      accessor: 'courseName',
      render: (s) => <span className="font-semibold text-slate-800">{s.courseName}</span>,
    },
    {
      header: 'Giảng Viên & Phòng',
      render: (s) => (
        <div className="text-xs">
          <div className="font-medium text-slate-700">{s.lecturerName || 'Chưa phân công'}</div>
          <div className="font-mono text-slate-500">Phòng {s.roomNumber || '—'}</div>
        </div>
      ),
    },
    {
      header: 'Sĩ Số / Sức Chứa',
      render: (s) => (
        <span className="font-mono text-xs font-bold text-slate-800">
          {s.enrolledCount} / {s.maxCapacity}
        </span>
      ),
      align: 'center',
    },
    {
      header: 'Tiến Độ Lấp Đầy',
      render: (s) => {
        const pct = Math.min(Math.round(s.occupancyPercentage || 0), 100);
        return (
          <div className="w-40">
            <div className="flex justify-between text-[11px] font-mono mb-1">
              <span className="font-bold text-slate-700">{s.occupancyPercentage?.toFixed(1)}%</span>
            </div>
            <div className="w-full bg-slate-200 rounded-full h-2 overflow-hidden">
              <div
                className={`h-2 rounded-full transition-all ${
                  s.underEnrolled ? 'bg-amber-500' : 'bg-indigo-600'
                }`}
                style={{ width: `${pct}%` }}
              />
            </div>
          </div>
        );
      },
    },
    {
      header: 'Cảnh Báo Vắng',
      render: (s) =>
        s.underEnrolled ? (
          <span className="inline-flex items-center text-xs font-bold text-amber-700 bg-amber-50 px-2 py-0.5 rounded border border-amber-200">
            <AlertTriangle className="w-3.5 h-3.5 mr-1 text-amber-600" /> &lt; 30% Sĩ số
          </span>
        ) : (
          <span className="inline-flex items-center text-xs font-medium text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-200">
            <CheckCircle className="w-3.5 h-3.5 mr-1 text-emerald-600" /> Đạt chỉ tiêu
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
            <BarChart3 className="w-5 h-5 text-indigo-600" />
            <span>Báo Cáo Tỷ Lệ Lấp Đầy Lớp Học Phần (UC-18)</span>
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Theo dõi tỷ lệ ghi danh, tổng hợp năng lực đáp ứng chỗ ngồi và cảnh báo các lớp dưới ngưỡng lấp đầy
          </p>
        </div>
        <div className="flex items-center space-x-2">
          <Button variant="secondary" size="sm" icon={Printer} onClick={() => window.print()}>
            In Báo Cáo
          </Button>
          <Button
            variant="secondary"
            size="sm"
            icon={RefreshCw}
            onClick={() => fetchReport(selectedSemesterId)}
          >
            Làm mới
          </Button>
        </div>
      </div>

      {error && <BackendErrorAlert error={error} onClose={() => setError(null)} />}

      {/* Semester Picker Card */}
      <Card bodyClassName="p-4 flex items-center space-x-3">
        <label className="text-xs font-bold text-slate-700 uppercase tracking-wider">
          Chọn Học Kỳ Thống Kê:
        </label>
        <select
          value={selectedSemesterId}
          onChange={(e) => setSelectedSemesterId(e.target.value)}
          className="px-3 py-1.5 text-xs rounded border border-slate-300 font-semibold text-slate-900 bg-white"
        >
          {semesters.map((s) => (
            <option key={s.id} value={s.id}>
              {s.code || s.semesterCode} - {s.name || s.semesterName}
            </option>
          ))}
        </select>
      </Card>

      {/* KPI Cards */}
      {report && (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
            <Card bodyClassName="p-5">
              <span className="text-xs font-semibold text-slate-500 uppercase block">Tổng Lớp Học Phần</span>
              <div className="text-2xl font-black text-slate-900 font-mono mt-1">
                {report.totalSections} <span className="text-xs font-normal text-slate-500">Lớp</span>
              </div>
            </Card>

            <Card bodyClassName="p-5">
              <span className="text-xs font-semibold text-slate-500 uppercase block">Tổng Năng Lực Chỗ Ngồi</span>
              <div className="text-2xl font-black text-slate-900 font-mono mt-1">
                {report.totalCapacity} <span className="text-xs font-normal text-slate-500">Chỗ</span>
              </div>
            </Card>

            <Card bodyClassName="p-5">
              <span className="text-xs font-semibold text-slate-500 uppercase block">Lượt Đăng Ký Thực Tế</span>
              <div className="text-2xl font-black text-emerald-600 font-mono mt-1">
                {report.totalEnrolled} <span className="text-xs font-normal text-slate-500">Lượt</span>
              </div>
            </Card>

            <Card bodyClassName="p-5">
              <span className="text-xs font-semibold text-slate-500 uppercase block">Tỷ Lệ Lấp Đầy Toàn Trường</span>
              <div className="text-2xl font-black text-indigo-600 font-mono mt-1">
                {report.overallOccupancyPercentage?.toFixed(1)}%
              </div>
            </Card>
          </div>

          <Card
            title={`Chi Tiết Tỷ Lệ Lấp Đầy Từng Lớp (${report.sectionOccupancies?.length || 0})`}
            subtitle="Cảnh báo tự động các lớp học phần có tỷ lệ đăng ký dưới 30% để Phòng Đào Tạo xem xét kế hoạch giảng dạy"
          >
            {loading ? (
              <div className="py-12 text-center text-xs text-slate-400">Đang tải báo cáo...</div>
            ) : (
              <Table columns={columns} data={report.sectionOccupancies} />
            )}
          </Card>
        </>
      )}
    </div>
  );
};

export default OccupancyReportPage;
