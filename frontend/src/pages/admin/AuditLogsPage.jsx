import React, { useState, useEffect } from 'react';
import { adminApi } from '../../api/adminApi';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Table from '../../components/common/Table';
import Badge from '../../components/common/Badge';
import Modal from '../../components/common/Modal';
import BackendErrorAlert from '../../components/common/BackendErrorAlert';
import { ShieldCheck, RefreshCw, Eye, Search, FileCode } from 'lucide-react';

const AuditLogsPage = () => {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Filters
  const [actionFilter, setActionFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [studentId, setStudentId] = useState('');
  const [sectionId, setSectionId] = useState('');

  // Selected Log for Snapshot inspection
  const [selectedLog, setSelectedLog] = useState(null);

  const fetchAuditLogs = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await adminApi.getAuditLogs({
        action: actionFilter || undefined,
        status: statusFilter || undefined,
        studentId: studentId ? Number(studentId) : undefined,
        sectionId: sectionId ? Number(sectionId) : undefined,
      });
      setLogs(res.data || []);
    } catch (err) {
      console.error(err);
      setError(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAuditLogs();
  }, [actionFilter, statusFilter]);

  const columns = [
    {
      header: 'Thời gian',
      render: (l) => (
        <span className="text-xs font-mono text-slate-600">
          {new Date(l.createdAt).toLocaleString('vi-VN')}
        </span>
      ),
    },
    {
      header: 'Sinh viên',
      render: (l) => (
        <div>
          <span className="font-semibold text-slate-800">{l.studentName || 'Sinh viên'}</span>
          <div className="text-[11px] font-mono text-indigo-600 font-bold">
            {l.studentCode || `ID: ${l.studentId}`}
          </div>
        </div>
      ),
    },
    {
      header: 'Lớp Học Phần',
      render: (l) => (
        <div>
          <span className="font-semibold text-slate-800">{l.courseName || 'Học phần'}</span>
          <div className="text-[11px] font-mono text-slate-500">
            {l.sectionCode || `Section #${l.sectionId}`}
          </div>
        </div>
      ),
    },
    {
      header: 'Hành động',
      render: (l) => (
        <span className="font-mono text-xs font-bold px-2 py-0.5 rounded bg-slate-100 text-slate-800 border border-slate-200">
          {l.action}
        </span>
      ),
      align: 'center',
    },
    {
      header: 'Trạng thái',
      render: (l) => <Badge variant={l.status}>{l.status}</Badge>,
      align: 'center',
    },
    {
      header: 'Mã lỗi (nếu thất bại)',
      render: (l) =>
        l.errorCode ? (
          <div>
            <span className="text-[11px] font-mono font-bold text-rose-700 bg-rose-50 px-1.5 py-0.5 rounded border border-rose-200">
              {l.errorCode}
            </span>
            <div className="text-[10px] text-rose-600 truncate max-w-xs">{l.errorMessage}</div>
          </div>
        ) : (
          <span className="text-xs text-slate-400 font-mono">—</span>
        ),
    },
    {
      header: 'IP Máy trạm',
      accessor: 'clientIp',
      render: (l) => <span className="text-xs font-mono text-slate-500">{l.clientIp || '127.0.0.1'}</span>,
    },
    {
      header: 'Snapshot',
      render: (l) => (
        <Button
          size="sm"
          variant="secondary"
          icon={Eye}
          onClick={() => setSelectedLog(l)}
        >
          Xem JSON
        </Button>
      ),
      align: 'center',
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight flex items-center space-x-2">
            <span>Nhật ký Kiểm toán Hệ thống (UC-19)</span>
            <span className="text-xs font-mono font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-200">
              Bất biến (Immutable Trail)
            </span>
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Ghi vết toàn bộ hành vi Đăng ký / Hủy học phần (kể cả thành công và thất bại) phục vụ thanh tra.
          </p>
        </div>
        <Button variant="secondary" size="sm" icon={RefreshCw} onClick={fetchAuditLogs}>
          Làm mới Nhật ký
        </Button>
      </div>

      {error && <BackendErrorAlert error={error} onClose={() => setError(null)} />}

      {/* Filter Toolbar */}
      <Card bodyClassName="p-4">
        <div className="grid grid-cols-1 sm:grid-cols-4 gap-3">
          <select
            value={actionFilter}
            onChange={(e) => setActionFilter(e.target.value)}
            className="px-3 py-2 text-xs rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500 text-slate-900 bg-white"
          >
            <option value="">Tất cả Hành động (Action)</option>
            <option value="ENROLL">ENROLL (Đăng ký)</option>
            <option value="DROP">DROP (Hủy môn)</option>
            <option value="CANCEL">CANCEL (Hủy lớp)</option>
          </select>

          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-3 py-2 text-xs rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500 text-slate-900 bg-white"
          >
            <option value="">Tất cả Trạng thái</option>
            <option value="SUCCESS">SUCCESS (Thành công)</option>
            <option value="FAILED">FAILED (Thất bại)</option>
          </select>

          <input
            type="number"
            value={studentId}
            onChange={(e) => setStudentId(e.target.value)}
            placeholder="Lọc theo Student ID..."
            className="px-3 py-2 text-xs rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500 text-slate-900 bg-white"
          />

          <Button variant="secondary" size="sm" icon={Search} onClick={fetchAuditLogs}>
            Tìm kiếm Logs
          </Button>
        </div>
      </Card>

      {/* Audit Logs Table */}
      <Card title={`Dữ liệu Nhật ký Kiểm toán (${logs.length} bản ghi ghi nhận)`}>
        {loading ? (
          <div className="py-12 text-center text-xs text-slate-400">Đang tải nhật ký kiểm toán...</div>
        ) : (
          <Table columns={columns} data={logs} />
        )}
      </Card>

      {/* Snapshot Modal */}
      <Modal
        isOpen={!!selectedLog}
        onClose={() => setSelectedLog(null)}
        title="Chi tiết Dữ liệu Snapshot Giao dịch (Audit Snapshot)"
        maxWidth="max-w-2xl"
        footer={
          <Button variant="secondary" size="sm" onClick={() => setSelectedLog(null)}>
            Đóng
          </Button>
        }
      >
        {selectedLog && (
          <div className="space-y-3">
            <div className="flex items-center justify-between text-xs p-3 bg-slate-50 rounded-lg border border-slate-200">
              <div>
                <span className="font-semibold text-slate-700">Giao dịch #{selectedLog.id}</span>
                <span className="mx-2 text-slate-300">•</span>
                <span className="font-mono text-slate-500">{new Date(selectedLog.createdAt).toISOString()}</span>
              </div>
              <Badge variant={selectedLog.status}>{selectedLog.status}</Badge>
            </div>

            <div>
              <span className="text-xs font-bold text-slate-700 uppercase tracking-wider block mb-1">
                Dữ liệu Snapshot lưu trong DB (Immutable JSON Payload):
              </span>
              <pre className="p-4 bg-slate-950 text-emerald-400 font-mono text-xs rounded-lg overflow-x-auto max-h-80 border border-slate-800">
                {selectedLog.snapshotData
                  ? JSON.stringify(JSON.parse(selectedLog.snapshotData), null, 2)
                  : '// Không có snapshot phụ trợ'}
              </pre>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default AuditLogsPage;
