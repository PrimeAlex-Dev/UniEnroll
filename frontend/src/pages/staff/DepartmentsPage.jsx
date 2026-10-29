import React, { useState, useEffect } from 'react';
import { curriculumApi } from '../../api/curriculumApi';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Table from '../../components/common/Table';
import Modal from '../../components/common/Modal';
import BackendErrorAlert from '../../components/common/BackendErrorAlert';
import { Building, Plus, RefreshCw } from 'lucide-react';

const DepartmentsPage = () => {
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Modal State
  const [showModal, setShowModal] = useState(false);
  const [createLoading, setCreateLoading] = useState(false);
  const [createError, setCreateError] = useState(null);

  // Form State - raw empty values
  const [code, setCode] = useState('');
  const [name, setName] = useState('');

  const fetchDepartments = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await curriculumApi.getDepartments();
      setDepartments(res.data || []);
    } catch (err) {
      console.error(err);
      setError(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDepartments();
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setCreateLoading(true);
    setCreateError(null);

    try {
      await curriculumApi.createDepartment({ code, name });
      setShowModal(false);
      setCode('');
      setName('');
      fetchDepartments();
    } catch (err) {
      console.error('Department creation failed:', err);
      setCreateError(err);
    } finally {
      setCreateLoading(false);
    }
  };

  const columns = [
    {
      header: 'STT',
      render: (_, idx) => <span className="text-slate-400 font-mono text-xs">{idx + 1}</span>,
      align: 'center',
    },
    {
      header: 'Mã Khoa / Bộ Môn',
      render: (d) => <span className="font-mono font-bold text-indigo-700">{d.code}</span>,
    },
    {
      header: 'Tên Khoa / Bộ Môn',
      render: (d) => <span className="font-semibold text-slate-900">{d.name}</span>,
    },
    {
      header: 'Thời gian tạo',
      render: (d) => (
        <span className="text-xs text-slate-500 font-mono">
          {d.createdAt ? new Date(d.createdAt).toLocaleDateString('vi-VN') : 'Mặc định'}
        </span>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight flex items-center space-x-2">
            <Building className="w-5 h-5 text-indigo-600" />
            <span>Quản lý Khoa / Bộ môn Đào tạo (UC-05)</span>
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Thiết lập danh mục các đơn vị khoa viện chủ quản môn học trong nhà trường
          </p>
        </div>
        <div className="flex items-center space-x-2">
          <Button variant="secondary" size="sm" icon={RefreshCw} onClick={fetchDepartments}>
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
            Thêm Khoa Mới
          </Button>
        </div>
      </div>

      {error && <BackendErrorAlert error={error} onClose={() => setError(null)} />}

      <Card title={`Danh mục Khoa / Bộ môn (${departments.length})`}>
        {loading ? (
          <div className="py-12 text-center text-xs text-slate-400">Đang tải danh sách khoa...</div>
        ) : (
          <Table columns={columns} data={departments} />
        )}
      </Card>

      {/* Modal create department without client validation */}
      <Modal
        isOpen={showModal}
        onClose={() => setShowModal(false)}
        title="Thêm Khoa Mới (UC-05 - Test Duplicate Code)"
        footer={
          <>
            <Button variant="secondary" size="sm" onClick={() => setShowModal(false)}>
              Hủy
            </Button>
            <Button variant="primary" size="sm" loading={createLoading} onClick={handleCreate}>
              Lưu Khoa
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          {createError && <BackendErrorAlert error={createError} onClose={() => setCreateError(null)} />}

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Mã Khoa (Unique Code) *
            </label>
            <input
              type="text"
              value={code}
              onChange={(e) => setCode(e.target.value)}
              placeholder="ví dụ: FIT, MATH, PHYS (Trùng lặp sẽ ném ERR_DEPARTMENT_CODE_EXISTS)"
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 focus:ring-indigo-500 font-mono text-slate-900 bg-white"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Tên Khoa Đầy Đủ *
            </label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="ví dụ: Khoa Công nghệ Thông tin"
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 focus:ring-indigo-500 text-slate-900 bg-white"
            />
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default DepartmentsPage;
