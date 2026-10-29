import React, { useState, useEffect } from 'react';
import { registrationApi } from '../../api/registrationApi';
import { schedulingApi } from '../../api/schedulingApi';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Table from '../../components/common/Table';
import Modal from '../../components/common/Modal';
import Badge from '../../components/common/Badge';
import BackendErrorAlert from '../../components/common/BackendErrorAlert';
import { Calendar, Plus, RefreshCw, Edit3, Trash2, XCircle, AlertTriangle } from 'lucide-react';

const toDatetimeLocal = (isoStr) => {
  if (!isoStr) return '';
  const d = new Date(isoStr);
  const pad = (n) => String(n).padStart(2, '0');
  const YYYY = d.getFullYear();
  const MM = pad(d.getMonth() + 1);
  const DD = pad(d.getDate());
  const hh = pad(d.getHours());
  const mm = pad(d.getMinutes());
  return `${YYYY}-${MM}-${DD}T${hh}:${mm}`;
};

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

  // Edit / Extend Phase Modal
  const [editPhase, setEditPhase] = useState(null);
  const [editLoading, setEditLoading] = useState(false);
  const [editError, setEditError] = useState(null);
  const [editFormData, setEditFormData] = useState({
    name: '',
    semesterId: '',
    startTime: '',
    endTime: '',
    targetCohort: '',
  });

  // Delete Phase Modal
  const [deletePhase, setDeletePhase] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [deleteError, setDeleteError] = useState(null);

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

  const handleOpenEdit = (p) => {
    setEditPhase(p);
    setEditFormData({
      name: p.name || '',
      semesterId: String(p.semesterId || selectedSemesterId),
      startTime: toDatetimeLocal(p.startTime),
      endTime: toDatetimeLocal(p.endTime),
      targetCohort: p.targetCohort || '',
    });
    setEditError(null);
  };

  const handleUpdatePhase = async (e) => {
    e.preventDefault();
    if (!editPhase) return;
    setEditLoading(true);
    setEditError(null);
    try {
      await registrationApi.updatePhase(editPhase.id, {
        name: editFormData.name?.trim(),
        semesterId: editFormData.semesterId ? Number(editFormData.semesterId) : Number(selectedSemesterId),
        startTime: editFormData.startTime ? new Date(editFormData.startTime).toISOString() : null,
        endTime: editFormData.endTime ? new Date(editFormData.endTime).toISOString() : null,
        targetCohort: editFormData.targetCohort?.trim() || null,
      });
      setEditPhase(null);
      fetchData();
    } catch (err) {
      console.error('Update phase failed:', err);
      setEditError(err);
    } finally {
      setEditLoading(false);
    }
  };

  const handleClosePhaseNow = async (p) => {
    if (!window.confirm(`Xác nhận ĐÓNG CỔNG NGAY LẬP TỨC cho đợt "${p.name}"? Sinh viên sẽ không thể gửi giao dịch đăng ký/hủy nữa.`)) {
      return;
    }
    try {
      await registrationApi.closePhaseNow(p.id);
      fetchData();
    } catch (err) {
      console.error('Force close phase failed:', err);
      setError(err);
    }
  };

  const handleDeletePhase = async () => {
    if (!deletePhase) return;
    setDeleteLoading(true);
    setDeleteError(null);
    try {
      await registrationApi.deletePhase(deletePhase.id);
      setDeletePhase(null);
      fetchData();
    } catch (err) {
      console.error('Delete phase failed:', err);
      setDeleteError(err);
    } finally {
      setDeleteLoading(false);
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
      header: 'Trạng Thái',
      render: (p) => {
        const status = getPhaseStatus(p.startTime, p.endTime);
        return <Badge variant={status.variant}>{status.label}</Badge>;
      },
      align: 'center',
    },
    {
      header: 'Thao tác',
      render: (p) => {
        const status = getPhaseStatus(p.startTime, p.endTime);
        return (
          <div className="flex items-center justify-end space-x-1.5">
            {status.variant === 'OPEN' && (
              <Button
                size="sm"
                variant="danger"
                icon={XCircle}
                title="Đóng cổng khẩn cấp ngay bây giờ"
                onClick={() => handleClosePhaseNow(p)}
              >
                Đóng ngay
              </Button>
            )}
            <Button
              size="sm"
              variant="secondary"
              icon={Edit3}
              title="Gia hạn hoặc chỉnh sửa đợt"
              onClick={() => handleOpenEdit(p)}
            >
              {status.variant === 'OPEN' ? 'Gia hạn' : 'Sửa'}
            </Button>
            {status.variant === 'DRAFT' && (
              <Button
                size="sm"
                variant="danger"
                icon={Trash2}
                title="Xóa đợt chưa bắt đầu"
                onClick={() => {
                  setDeletePhase(p);
                  setDeleteError(null);
                }}
              >
                Xóa
              </Button>
            )}
          </div>
        );
      },
      align: 'right',
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
            Thiết lập cửa sổ thời gian (BR-01 Temporal Window) cho phép sinh viên đăng ký môn học theo khóa
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

      {/* Modal edit / extend phase */}
      <Modal
        isOpen={!!editPhase}
        onClose={() => setEditPhase(null)}
        title={`Chỉnh sửa / Gia hạn Đợt: ${editPhase?.name || ''}`}
        footer={
          <>
            <Button variant="secondary" size="sm" onClick={() => setEditPhase(null)}>
              Hủy
            </Button>
            <Button variant="primary" size="sm" loading={editLoading} onClick={handleUpdatePhase}>
              Cập nhật
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          {editError && <BackendErrorAlert error={editError} onClose={() => setEditError(null)} />}

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Tên Đợt Đăng Ký *
            </label>
            <input
              type="text"
              value={editFormData.name}
              onChange={(e) => setEditFormData({ ...editFormData, name: e.target.value })}
              className="w-full px-3 py-2 text-xs rounded border border-slate-300 text-slate-900 bg-white"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Học Kỳ Áp Dụng *</label>
            <select
              value={editFormData.semesterId}
              onChange={(e) => setEditFormData({ ...editFormData, semesterId: e.target.value })}
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
                value={editFormData.startTime}
                onChange={(e) => setEditFormData({ ...editFormData, startTime: e.target.value })}
                className="w-full px-3 py-2 text-xs rounded border border-slate-300 text-slate-900 bg-white"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Thời Gian Kết Thúc (Gia hạn) *</label>
              <input
                type="datetime-local"
                value={editFormData.endTime}
                onChange={(e) => setEditFormData({ ...editFormData, endTime: e.target.value })}
                className="w-full px-3 py-2 text-xs rounded border border-slate-300 text-slate-900 bg-white"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Khóa Sinh Viên Áp Dụng</label>
            <input
              type="text"
              value={editFormData.targetCohort}
              onChange={(e) => setEditFormData({ ...editFormData, targetCohort: e.target.value })}
              placeholder="ví dụ: K65 hoặc K2026 (để trống nếu áp dụng toàn trường)"
              className="w-full px-3 py-2 text-xs rounded border border-slate-300 text-slate-900 bg-white"
            />
          </div>
        </div>
      </Modal>

      {/* Modal delete phase */}
      <Modal
        isOpen={!!deletePhase}
        onClose={() => setDeletePhase(null)}
        title="Xác nhận xóa Đợt Đăng Ký"
        footer={
          <>
            <Button variant="secondary" size="sm" onClick={() => setDeletePhase(null)}>
              Hủy
            </Button>
            <Button variant="danger" size="sm" loading={deleteLoading} onClick={handleDeletePhase}>
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
              <p className="font-semibold">Bạn có chắc chắn muốn xóa đợt đăng ký này?</p>
              <p className="mt-1 text-slate-600">
                Đợt: <span className="font-bold text-slate-900">{deletePhase?.name}</span>.
              </p>
              <p className="mt-1 text-slate-500">
                Lưu ý: Chỉ cho phép xóa đợt đăng ký khi đợt đó chưa đến thời gian mở (trạng thái CHƯA MỞ).
              </p>
            </div>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default PhasesPage;
