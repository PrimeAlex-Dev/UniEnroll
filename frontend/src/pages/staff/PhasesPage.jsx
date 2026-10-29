import React, { useState, useEffect } from 'react';
import { registrationApi } from '../../api/registrationApi';
import { schedulingApi } from '../../api/schedulingApi';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Table from '../../components/common/Table';
import Modal from '../../components/common/Modal';
import Badge from '../../components/common/Badge';
import BackendErrorAlert from '../../components/common/BackendErrorAlert';
import { Calendar, Plus, RefreshCw } from 'lucide-react';

const PhasesPage = () => {
  const [phases, setPhases] = useState([]);
  const [semesters, setSemesters] = useState([]);
  const [selectedSemesterId, setSelectedSemesterId] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Create Phase Modal
  const [showModal, setShowModal] = useState(false);
  const [createLoading, setCreateLoading] = useState(false);
  const [createError, setCreateError] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    semesterId: '',
    startTime: '',
    endTime: '',
    targetCohort: '',
  });

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [semRes, phaseRes] = await Promise.all([
        schedulingApi.getSemesters(),
        registrationApi.getPhases(selectedSemesterId ? Number(selectedSemesterId) : undefined),
      ]);
      setSemesters(semRes.data || []);
      setPhases(phaseRes.data || []);
      if (!selectedSemesterId && semRes.data?.length > 0) {
        setSelectedSemesterId(String(semRes.data[0].id));
      }
    } catch (err) {
      console.error(err);
      setError(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [selectedSemesterId]);

  const handleCreatePhase = async (e) => {
    e.preventDefault();
    setCreateLoading(true);
    setCreateError(null);
    try {
      await registrationApi.createPhase({
        name: formData.name?.trim(),
        semesterId: formData.semesterId ? Number(formData.semesterId) : Number(selectedSemesterId),
        startTime: formData.startTime ? new Date(formData.startTime).toISOString() : null,
        endTime: formData.endTime ? new Date(formData.endTime).toISOString() : null,
        targetCohort: formData.targetCohort?.trim() || null,
      });
      setShowModal(false);
      setFormData({ name: '', semesterId: '', startTime: '', endTime: '', targetCohort: '' });
      fetchData();
    } catch (err) {
      console.error('Create phase failed:', err);
      setCreateError(err);
    } finally {
      setCreateLoading(false);
    }
  };

  const getPhaseStatus = (start, end) => {
    const now = new Date();
    const s = new Date(start);
    const e = new Date(end);
    if (now < s) return { label: 'CHƯA MỞ', variant: 'DRAFT' };
    if (now > e) return { label: 'ĐÃ ĐÓNG', variant: 'CLOSED' };
    return { label: 'ĐANG MỞ', variant: 'OPEN' };
  };

  const columns = [
    {
      header: 'Tên Đợt Đăng Ký',
      render: (p) => (
        <div>
          <span className="font-semibold text-slate-800">{p.name}</span>
          <div className="text-xs text-slate-400 font-mono">ID: {p.id}</div>
        </div>
      ),
    },
    {
      header: 'Thời Gian Bắt Đầu',
      render: (p) => (
        <span className="font-mono text-xs text-slate-700">
          {new Date(p.startTime).toLocaleString('vi-VN')}
        </span>
      ),
    },
    {
      header: 'Thời Gian Kết Thúc',
      render: (p) => (
        <span className="font-mono text-xs text-slate-700">
          {new Date(p.endTime).toLocaleString('vi-VN')}
        </span>
      ),
    },
    {
      header: 'Đối Tượng Áp Dụng',
      render: (p) => (
        <span className="font-mono text-xs bg-slate-100 text-slate-800 px-2.5 py-1 rounded border border-slate-200">
          {p.targetCohort ? `Khóa ${p.targetCohort}` : 'Toàn trường'}
        </span>
      ),
      align: 'center',
    },
    {
      header: 'Trạng Thái Đợt (BR-01)',
      render: (p) => {
        const status = getPhaseStatus(p.startTime, p.endTime);
        return <Badge variant={status.variant}>{status.label}</Badge>;
      },
      align: 'center',
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight flex items-center space-x-2">
            <Calendar className="w-5 h-5 text-indigo-600" />
            <span>Cấu hình Đợt Đăng ký Học phần (UC-12)</span>
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Thiết lập thời gian mở cổng (BR-01) và biên độ tín chỉ tối thiểu/tối đa (BR-04) cho sinh viên
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
              setShowModal(true);
            }}
          >
            Tạo Đợt Đăng Ký Mới
          </Button>
        </div>
      </div>

      {error && <BackendErrorAlert error={error} onClose={() => setError(null)} />}

      <Card title={`Danh sách Đợt Đăng ký Học phần (${phases.length})`}>
        {loading ? (
          <div className="py-12 text-center text-xs text-slate-400">Đang tải danh sách đợt đăng ký...</div>
        ) : (
          <Table columns={columns} data={phases} />
        )}
      </Card>

      {/* Modal create phase */}
      <Modal
        isOpen={showModal}
        onClose={() => setShowModal(false)}
        title="Tạo Đợt Đăng Ký Học Phần Mới (UC-12)"
        footer={
          <>
            <Button variant="secondary" size="sm" onClick={() => setShowModal(false)}>
              Hủy
            </Button>
            <Button variant="primary" size="sm" loading={createLoading} onClick={handleCreatePhase}>
              Lưu Đợt Đăng Ký
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          {createError && <BackendErrorAlert error={createError} onClose={() => setCreateError(null)} />}

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Tên Đợt Đăng Ký *
            </label>
            <input
              type="text"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              placeholder="ví dụ: ĐKHP Học kỳ 1 Năm học 2026-2027"
              className="w-full px-3 py-2 text-xs rounded border border-slate-300 text-slate-900 bg-white"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Học Kỳ Áp Dụng *</label>
            <select
              value={formData.semesterId || selectedSemesterId}
              onChange={(e) => setFormData({ ...formData, semesterId: e.target.value })}
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
              <label className="block text-xs font-semibold text-slate-700 mb-1">Thời Gian Bắt Đầu *</label>
              <input
                type="datetime-local"
                value={formData.startTime}
                onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
                className="w-full px-3 py-2 text-xs rounded border border-slate-300 text-slate-900 bg-white"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Thời Gian Kết Thúc *</label>
              <input
                type="datetime-local"
                value={formData.endTime}
                onChange={(e) => setFormData({ ...formData, endTime: e.target.value })}
                className="w-full px-3 py-2 text-xs rounded border border-slate-300 text-slate-900 bg-white"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Khóa Sinh Viên Áp Dụng</label>
            <input
              type="text"
              value={formData.targetCohort}
              onChange={(e) => setFormData({ ...formData, targetCohort: e.target.value })}
              placeholder="ví dụ: K65 hoặc K2026 (để trống nếu áp dụng toàn trường)"
              className="w-full px-3 py-2 text-xs rounded border border-slate-300 text-slate-900 bg-white"
            />
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default PhasesPage;
