import React, { useState, useEffect } from 'react';
import { registrationApi } from '../../api/registrationApi';
import { schedulingApi } from '../../api/schedulingApi';
import { curriculumApi } from '../../api/curriculumApi';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Table from '../../components/common/Table';
import Badge from '../../components/common/Badge';
import Modal from '../../components/common/Modal';
import BackendErrorAlert from '../../components/common/BackendErrorAlert';
import {
  GraduationCap,
  Search,
  CheckCircle2,
  Trash2,
  AlertTriangle,
  Clock,
  Layers,
  BookCheck,
  RefreshCw,
  Users,
  Mail,
  MapPin,
  CalendarDays,
} from 'lucide-react';

const RegistrationStudioPage = () => {
  const [semesters, setSemesters] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [selectedSemesterId, setSelectedSemesterId] = useState('');
  const [departmentId, setDepartmentId] = useState('');
  const [keyword, setKeyword] = useState('');

  // Catalog & Schedule State
  const [catalogSections, setCatalogSections] = useState([]);
  const [mySchedule, setMySchedule] = useState(null);
  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);

  // Errors & Notifications
  const [error, setError] = useState(null);
  const [successNotice, setSuccessNotice] = useState(null);

  // Class Roster Modal State (Xem bạn học cùng lớp & giảng viên)
  const [isRosterModalOpen, setIsRosterModalOpen] = useState(false);
  const [rosterData, setRosterData] = useState(null);
  const [rosterLoading, setRosterLoading] = useState(false);
  const [rosterError, setRosterError] = useState(null);
  const [rosterFilter, setRosterFilter] = useState('');

  const initData = async () => {
    try {
      const [semRes, deptRes] = await Promise.all([
        schedulingApi.getSemesters(),
        curriculumApi.getDepartments(),
      ]);
      setSemesters(semRes.data || []);
      setDepartments(deptRes.data || []);

      if (semRes.data?.length > 0) {
        setSelectedSemesterId(String(semRes.data[0].id));
      }
    } catch (err) {
      console.error(err);
      setError(err);
    }
  };

  const fetchCatalogAndSchedule = async (semesterId) => {
    if (!semesterId) return;
    setLoading(true);
    try {
      const [catRes, schedRes] = await Promise.all([
        registrationApi.getCatalogSections({
          semesterId: Number(semesterId),
          departmentId: departmentId ? Number(departmentId) : undefined,
          keyword: keyword || undefined,
        }),
        registrationApi.getMySchedule(Number(semesterId)),
      ]);
      setCatalogSections(catRes.data || []);
      setMySchedule(schedRes.data || null);
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
      fetchCatalogAndSchedule(selectedSemesterId);
    }
  }, [selectedSemesterId, departmentId]);

  // RAW ENROLL ACTION: Submits directly without client validation to trigger backend invariants!
  const handleEnroll = async (section) => {
    setError(null);
    setSuccessNotice(null);
    setActionLoading(true);

    try {
      const res = await registrationApi.enroll({ sectionId: section.id });
      setSuccessNotice(`Đăng ký thành công lớp [${section.sectionCode}] ${section.courseName}!`);
      // Reload catalog and schedule immediately
      fetchCatalogAndSchedule(selectedSemesterId);
    } catch (err) {
      console.error('Registration failed from backend:', err);
      // Directly render backend error code, message & fieldErrors
      setError(err);
    } finally {
      setActionLoading(false);
    }
  };

  // RAW DROP ACTION: Submits directly to backend
  const handleDrop = async (sectionId, sectionCode) => {
    if (!window.confirm(`Bạn có chắc chắn muốn hủy đăng ký học phần [${sectionCode}]?`)) return;
    setError(null);
    setSuccessNotice(null);
    setActionLoading(true);

    try {
      await registrationApi.drop({ sectionId });
      setSuccessNotice(`Đã hủy học phần [${sectionCode}] thành công.`);
      fetchCatalogAndSchedule(selectedSemesterId);
    } catch (err) {
      console.error('Drop failed from backend:', err);
      setError(err);
    } finally {
      setActionLoading(false);
    }
  };

  const formatDayOfWeek = (day) => {
    if (!day) return '';
    const d = String(day).toUpperCase();
    switch (d) {
      case 'MONDAY': case '2': return 'Thứ Hai';
      case 'TUESDAY': case '3': return 'Thứ Ba';
      case 'WEDNESDAY': case '4': return 'Thứ Tư';
      case 'THURSDAY': case '5': return 'Thứ Năm';
      case 'FRIDAY': case '6': return 'Thứ Sáu';
      case 'SATURDAY': case '7': return 'Thứ Bảy';
      case 'SUNDAY': case '8': case '1': return 'Chủ Nhật';
      default: return d;
    }
  };

  const handleOpenRoster = async (sectionId) => {
    setIsRosterModalOpen(true);
    setRosterLoading(true);
    setRosterError(null);
    setRosterData(null);
    setRosterFilter('');
    try {
      const res = await registrationApi.getSectionRoster(sectionId);
      setRosterData(res.data);
    } catch (err) {
      console.error('Failed to load roster:', err);
      setRosterError(err);
    } finally {
      setRosterLoading(false);
    }
  };

  const catalogColumns = [
    {
      header: 'Mã Lớp',
      render: (s) => (
        <div>
          <span className="font-mono font-bold text-indigo-700">{s.sectionCode}</span>
          <div className="text-[11px] text-slate-400 font-mono">ID: {s.id}</div>
        </div>
      ),
    },
    {
      header: 'Tên Học Phần',
      render: (s) => (
        <div>
          <span className="font-semibold text-slate-800">{s.courseName}</span>
          <div className="text-xs font-mono text-slate-500">
            {s.courseCode} • <span className="font-bold text-indigo-600">{s.credits} Tín chỉ</span>
          </div>
        </div>
      ),
    },
    {
      header: 'Phòng & Địa Điểm',
      render: (s) => (
        <span className="font-mono text-xs text-slate-700 bg-slate-100 px-2 py-0.5 rounded border border-slate-200">
          {s.building} - P.{s.roomNumber}
        </span>
      ),
    },
    {
      header: 'Lịch Học (Slots)',
      render: (s) => (
        <div className="space-y-0.5 text-xs">
          {s.scheduleSlots && s.scheduleSlots.length > 0 ? (
            s.scheduleSlots.map((slot, idx) => (
              <div key={idx} className="font-mono text-slate-700">
                Thứ {slot.dayOfWeek} (Tiết {slot.startPeriod}-{slot.endPeriod})
              </div>
            ))
          ) : (
            <span className="text-slate-400 italic">Chưa xếp lịch</span>
          )}
        </div>
      ),
    },
    {
      header: 'Chỗ Trống (Sức chứa)',
      render: (s) => {
        const isFull = s.availableSlots <= 0;
        return (
          <div className="text-xs">
            <span
              className={`font-mono font-bold px-2 py-0.5 rounded-full border ${
                isFull
                  ? 'bg-rose-50 text-rose-700 border-rose-200'
                  : 'bg-emerald-50 text-emerald-700 border-emerald-200'
              }`}
            >
              {s.enrolledCount} / {s.maxCapacity} ({isFull ? 'Hết chỗ' : `Còn ${s.availableSlots} chỗ`})
            </span>
          </div>
        );
      },
      align: 'center',
    },
    {
      header: 'Đăng Ký',
      render: (s) => {
        const isAlreadyEnrolled = mySchedule?.enrolledSections?.some((sec) => sec.id === s.id);
        if (isAlreadyEnrolled) {
          return (
            <span className="inline-flex items-center text-xs font-bold text-blue-700 bg-blue-50 px-2.5 py-1 rounded border border-blue-200">
              <CheckCircle2 className="w-3.5 h-3.5 mr-1" /> Đã đăng ký
            </span>
          );
        }
        return (
          <Button
            size="sm"
            variant="primary"
            loading={actionLoading}
            onClick={() => handleEnroll(s)}
          >
            Đăng Ký
          </Button>
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
            <GraduationCap className="w-5 h-5 text-indigo-600" />
            <span>Studio Đăng Ký Học Phần Trực Tuyến (UC-13, UC-14, UC-15)</span>
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Động cơ đăng ký thời gian thực kiểm soát 6 Invariants nghiêm ngặt (BR-01 đến BR-06)
          </p>
        </div>
        <Button
          variant="secondary"
          size="sm"
          icon={RefreshCw}
          onClick={() => fetchCatalogAndSchedule(selectedSemesterId)}
        >
          Làm mới
        </Button>
      </div>

      {/* Backend Error Alert Renderer (Live Test of Backend Exceptions) */}
      {error && <BackendErrorAlert error={error} onClose={() => setError(null)} />}

      {/* Success Notice */}
      {successNotice && (
        <div className="p-4 rounded-lg bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs flex items-center space-x-2">
          <CheckCircle2 className="w-4 h-4 text-emerald-600 flex-shrink-0" />
          <span className="font-semibold">{successNotice}</span>
        </div>
      )}

      {/* Top Credit Summary & Active Semester Banner */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card bodyClassName="p-4 flex items-center space-x-4">
          <div className="w-10 h-10 rounded-xl bg-indigo-50 border border-indigo-200 flex items-center justify-center text-indigo-700 font-bold">
            <BookCheck className="w-5 h-5" />
          </div>
          <div>
            <div className="text-xs text-slate-500 font-semibold uppercase">Tổng tín chỉ đã chọn</div>
            <div className="text-xl font-extrabold text-indigo-600 font-mono">
              {mySchedule?.totalCredits ?? 0} <span className="text-xs font-normal text-slate-500">Tín chỉ</span>
            </div>
          </div>
        </Card>

        <Card bodyClassName="p-4 flex items-center space-x-4">
          <div className="w-10 h-10 rounded-xl bg-emerald-50 border border-emerald-200 flex items-center justify-center text-emerald-700 font-bold">
            <Layers className="w-5 h-5" />
          </div>
          <div>
            <div className="text-xs text-slate-500 font-semibold uppercase">Số môn đang ghi danh</div>
            <div className="text-xl font-extrabold text-emerald-600 font-mono">
              {mySchedule?.enrolledSections?.length ?? 0} <span className="text-xs font-normal text-slate-500">Lớp</span>
            </div>
          </div>
        </Card>

        <Card bodyClassName="p-4">
          <label className="text-[11px] font-bold text-slate-500 uppercase tracking-wider block mb-1">
            Học kỳ đăng ký hiện tại
          </label>
          <select
            value={selectedSemesterId}
            onChange={(e) => setSelectedSemesterId(e.target.value)}
            className="w-full px-2.5 py-1 text-xs rounded border border-slate-300 font-semibold text-slate-900 bg-white"
          >
            {semesters.map((sem) => (
              <option key={sem.id} value={sem.id}>
                {sem.code || sem.semesterCode} - {sem.name || sem.semesterName}
              </option>
            ))}
          </select>
        </Card>
      </div>

      {/* Enrolled Sections Panel (UC-15 - Drop Support) */}
      <Card
        title={`Các Học Phần Đang Ghi Danh Trong Kỳ (${mySchedule?.enrolledSections?.length || 0})`}
        subtitle="Danh sách các lớp học phần bạn đã đăng ký thành công"
      >
        {!mySchedule?.enrolledSections || mySchedule.enrolledSections.length === 0 ? (
          <div className="py-6 text-center text-xs text-slate-400 italic">
            Bạn chưa đăng ký học phần nào trong học kỳ này.
          </div>
        ) : (
          <div className="divide-y divide-slate-100">
            {mySchedule.enrolledSections.map((sec) => (
              <div
                key={sec.id}
                className="py-3 flex flex-col sm:flex-row sm:items-center justify-between gap-2"
              >
                <div>
                  <div className="flex items-center space-x-2">
                    <span className="font-mono font-bold text-xs text-indigo-700">{sec.sectionCode}</span>
                    <span className="font-semibold text-sm text-slate-800">{sec.courseName}</span>
                    <Badge variant="ENROLLED">ĐÃ GHI DANH</Badge>
                  </div>
                  <div className="text-xs text-slate-500 mt-1 flex flex-wrap items-center gap-x-2 gap-y-0.5">
                    <span>{sec.credits} Tín chỉ</span>
                    <span>•</span>
                    <span className="text-slate-700 font-medium">
                      GV: {sec.lecturerName || 'Chưa phân công'}
                    </span>
                    {sec.building && sec.roomNumber && (
                      <>
                        <span>•</span>
                        <span>Phòng: {sec.building} - {sec.roomNumber}</span>
                      </>
                    )}
                    {sec.scheduleSlots?.map((sl, i) => (
                      <span key={i} className="font-mono text-indigo-600 bg-indigo-50 px-1.5 py-0.5 rounded text-[11px]">
                        {formatDayOfWeek(sl.dayOfWeek)} (Tiết {sl.startPeriod}-{sl.endPeriod})
                      </span>
                    ))}
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  <Button
                    size="sm"
                    variant="outline"
                    icon={Users}
                    onClick={() => handleOpenRoster(sec.id)}
                  >
                    Xem Lớp & Bạn Học
                  </Button>
                  <Button
                    size="sm"
                    variant="danger"
                    icon={Trash2}
                    loading={actionLoading}
                    onClick={() => handleDrop(sec.id, sec.sectionCode)}
                  >
                    Hủy Môn (Drop)
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </Card>

      {/* Catalog Search & Registration Table (UC-13, UC-14) */}
      <Card
        title="Danh Mục Lớp Học Phần Mở Đăng Ký (UC-13)"
        subtitle="Chọn môn học và nhấn 'Đăng ký' để kiểm thử đường ống xác thực thời gian thực của Backend"
      >
        <div className="mb-4 grid grid-cols-1 sm:grid-cols-3 gap-3">
          <div className="relative">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
            <input
              type="text"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && fetchCatalogAndSchedule(selectedSemesterId)}
              placeholder="Tìm theo mã lớp, tên môn..."
              className="w-full pl-9 pr-3 py-1.5 text-xs rounded border border-slate-300 text-slate-900 bg-white"
            />
          </div>

          <select
            value={departmentId}
            onChange={(e) => setDepartmentId(e.target.value)}
            className="px-3 py-1.5 text-xs rounded border border-slate-300 text-slate-900 bg-white"
          >
            <option value="">Tất cả Khoa chủ quản</option>
            {departments.map((d) => (
              <option key={d.id} value={d.id}>
                {d.name}
              </option>
            ))}
          </select>

          <Button
            variant="secondary"
            size="sm"
            onClick={() => fetchCatalogAndSchedule(selectedSemesterId)}
          >
            Tìm kiếm
          </Button>
        </div>

        {loading ? (
          <div className="py-12 text-center text-xs text-slate-400">Đang tải danh mục học phần...</div>
        ) : (
          <Table columns={catalogColumns} data={catalogSections} />
        )}
      </Card>

      {/* Class Roster & Classmates Modal */}
      <Modal
        isOpen={isRosterModalOpen}
        onClose={() => setIsRosterModalOpen(false)}
        title={
          rosterData
            ? `Chi Tiết Lớp & Danh Sách Bạn Học: ${rosterData.sectionCode} - ${rosterData.courseName}`
            : 'Chi Tiết Lớp Học Phần'
        }
        maxWidth="max-w-4xl"
        footer={
          <Button variant="outline" size="sm" onClick={() => setIsRosterModalOpen(false)}>
            Đóng
          </Button>
        }
      >
        <div className="space-y-4">
          <BackendErrorAlert error={rosterError} />

          {rosterLoading ? (
            <div className="py-12 text-center text-sm text-slate-500">
              <RefreshCw className="w-6 h-6 animate-spin mx-auto mb-2 text-indigo-600" />
              Đang tải thông tin lớp và danh sách bạn học...
            </div>
          ) : rosterData ? (
            <>
              {/* Thông tin tổng quan của Lớp */}
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 bg-slate-50 p-4 rounded-xl border border-slate-200 text-xs">
                <div>
                  <div className="text-slate-400 font-semibold uppercase text-[10px]">Giảng Viên Phụ Trách</div>
                  <div className="text-slate-900 font-bold text-sm mt-0.5 flex items-center gap-1.5">
                    <span>{rosterData.lecturerName || 'Chưa phân công'}</span>
                  </div>
                  {rosterData.lecturerEmail && (
                    <div className="text-indigo-600 font-mono mt-0.5 flex items-center gap-1">
                      <Mail className="w-3.5 h-3.5 text-indigo-500" />
                      <span>{rosterData.lecturerEmail}</span>
                    </div>
                  )}
                </div>

                <div>
                  <div className="text-slate-400 font-semibold uppercase text-[10px]">Sĩ Số Hiện Tại</div>
                  <div className="text-slate-900 font-bold text-sm mt-0.5">
                    {rosterData.enrolledCount} / {rosterData.capacity} sinh viên
                  </div>
                  <div className="text-emerald-600 mt-0.5 font-medium">
                    {rosterData.capacity - rosterData.enrolledCount > 0
                      ? `Còn trống ${rosterData.capacity - rosterData.enrolledCount} chỗ`
                      : 'Lớp đã đủ sĩ số'}
                  </div>
                </div>

                <div>
                  <div className="text-slate-400 font-semibold uppercase text-[10px]">Lịch Học & Phòng</div>
                  {rosterData.schedules && rosterData.schedules.length > 0 ? (
                    <div className="space-y-1 mt-0.5">
                      {rosterData.schedules.map((s, idx) => (
                        <div key={idx} className="text-slate-800 font-medium flex items-center gap-1">
                          <CalendarDays className="w-3.5 h-3.5 text-slate-500 flex-shrink-0" />
                          <span>
                            {formatDayOfWeek(s.dayOfWeek)}: Tiết {s.startPeriod}-{s.endPeriod}
                          </span>
                          <span className="text-slate-400">•</span>
                          <span className="text-indigo-700 font-semibold">
                            {s.roomNumber ? `${s.building || ''} ${s.roomNumber}` : 'Chưa xếp phòng'}
                          </span>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div className="text-slate-500 italic mt-0.5">Chưa xếp lịch học</div>
                  )}
                </div>
              </div>

              {/* Danh sách sinh viên cùng lớp */}
              <div className="space-y-3">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pt-2">
                  <div className="flex items-center gap-2">
                    <Users className="w-4 h-4 text-indigo-600" />
                    <h4 className="text-sm font-bold text-slate-800">
                      Danh Sách Sinh Viên Cùng Lớp ({rosterData.students?.length || 0})
                    </h4>
                  </div>
                  <div className="relative">
                    <Search className="w-3.5 h-3.5 text-slate-400 absolute left-2.5 top-2" />
                    <input
                      type="text"
                      value={rosterFilter}
                      onChange={(e) => setRosterFilter(e.target.value)}
                      placeholder="Tìm bạn học theo tên, MSSV..."
                      className="pl-8 pr-3 py-1 text-xs rounded-lg border border-slate-300 text-slate-800 w-full sm:w-60 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                    />
                  </div>
                </div>

                <div className="overflow-x-auto rounded-lg border border-slate-200">
                  <table className="w-full text-left text-xs text-slate-600">
                    <thead className="bg-slate-50 text-[11px] font-semibold uppercase text-slate-500 border-b border-slate-200">
                      <tr>
                        <th className="py-2.5 px-3 text-center w-10">STT</th>
                        <th className="py-2.5 px-3">Mã SV</th>
                        <th className="py-2.5 px-3">Họ và Tên</th>
                        <th className="py-2.5 px-3">Khóa / Ngành</th>
                        <th className="py-2.5 px-3">Email Liên Hệ</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {(() => {
                        const students = (rosterData.students || []).filter((st) => {
                          if (!rosterFilter) return true;
                          const q = rosterFilter.toLowerCase();
                          return (
                            (st.studentCode && st.studentCode.toLowerCase().includes(q)) ||
                            (st.fullName && st.fullName.toLowerCase().includes(q)) ||
                            (st.email && st.email.toLowerCase().includes(q)) ||
                            (st.major && st.major.toLowerCase().includes(q))
                          );
                        });

                        if (students.length === 0) {
                          return (
                            <tr>
                              <td colSpan={5} className="py-6 text-center text-slate-400 italic">
                                Không tìm thấy sinh viên phù hợp.
                              </td>
                            </tr>
                          );
                        }

                        return students.map((st, i) => (
                          <tr key={st.studentId || i} className="hover:bg-slate-50">
                            <td className="py-2 px-3 text-center text-slate-400">{i + 1}</td>
                            <td className="py-2 px-3 font-mono font-bold text-indigo-700">
                              {st.studentCode}
                            </td>
                            <td className="py-2 px-3 font-medium text-slate-900">
                              {st.fullName}
                            </td>
                            <td className="py-2 px-3 text-slate-500">
                              {st.cohort ? `K${st.cohort} - ` : ''}{st.major || '-'}
                            </td>
                            <td className="py-2 px-3 text-slate-600 font-mono">
                              {st.email ? (
                                <a
                                  href={`mailto:${st.email}`}
                                  className="text-indigo-600 hover:underline flex items-center gap-1"
                                >
                                  <Mail className="w-3 h-3 text-indigo-400" />
                                  <span>{st.email}</span>
                                </a>
                              ) : (
                                '-'
                              )}
                            </td>
                          </tr>
                        ));
                      })()}
                    </tbody>
                  </table>
                </div>
              </div>
            </>
          ) : null}
        </div>
      </Modal>
    </div>
  );
};

export default RegistrationStudioPage;
