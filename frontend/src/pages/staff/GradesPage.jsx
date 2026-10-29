import React, { useState, useEffect } from 'react';
import { schedulingApi } from '../../api/schedulingApi';
import { curriculumApi } from '../../api/curriculumApi';
import { adminApi } from '../../api/adminApi';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import BackendErrorAlert from '../../components/common/BackendErrorAlert';
import Badge from '../../components/common/Badge';
import { Award, CheckCircle2 } from 'lucide-react';

const GradesPage = () => {
  const [students, setStudents] = useState([]);
  const [courses, setCourses] = useState([]);
  const [semesters, setSemesters] = useState([]);
  const [loading, setLoading] = useState(false);
  const [successMsg, setSuccessMsg] = useState(null);
  const [error, setError] = useState(null);

  const [form, setForm] = useState({
    studentId: '',
    courseId: '',
    semesterId: '',
    status: 'PASSED',
    finalScore: '8.0',
  });

  const fetchData = async () => {
    try {
      const [uRes, cRes, sRes] = await Promise.all([
        adminApi.getUsers({ role: 'STUDENT' }),
        curriculumApi.getCourses(),
        schedulingApi.getSemesters(),
      ]);
      setStudents(uRes.data || []);
      setCourses(cRes.data || []);
      setSemesters(sRes.data || []);

      if (uRes.data?.length > 0 && !form.studentId) {
        const firstStu = uRes.data[0];
        const stuId = firstStu.studentProfile?.id || firstStu.studentProfile?.studentProfileId || firstStu.id;
        setForm((prev) => ({ ...prev, studentId: String(stuId) }));
      }
      if (cRes.data?.length > 0 && !form.courseId) {
        setForm((prev) => ({ ...prev, courseId: String(cRes.data[0].id) }));
      }
      if (sRes.data?.length > 0 && !form.semesterId) {
        setForm((prev) => ({ ...prev, semesterId: String(sRes.data[0].id) }));
      }
    } catch (err) {
      console.error(err);
      setError(err);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleSaveGrade = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccessMsg(null);
    try {
      await schedulingApi.saveGrade({
        studentId: form.studentId ? Number(form.studentId) : '',
        courseId: form.courseId ? Number(form.courseId) : '',
        semesterId: form.semesterId ? Number(form.semesterId) : '',
        status: form.status,
        finalScore: form.finalScore !== '' ? Number(form.finalScore) : null,
      });
      setSuccessMsg(
        `Đã lưu kết quả [${form.status}] cho sinh viên ID ${form.studentId} đối với môn học ID ${form.courseId} thành công!`
      );
    } catch (err) {
      console.error('Save grade failed:', err);
      setError(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6 max-w-4xl">
      <div>
        <h1 className="text-xl font-bold text-slate-900 tracking-tight flex items-center space-x-2">
          <Award className="w-5 h-5 text-indigo-600" />
          <span>Tiện ích Nhập Điểm Phục Vụ Kiểm Thử Tiên Quyết (Test Harness)</span>
        </h1>
        <p className="text-xs text-slate-500 mt-1">
          Hỗ trợ nhập trạng thái điểm (PASSED / FAILED) của sinh viên đối với các môn học để kiểm thử điều kiện tiên quyết (BR-02) khi đăng ký học phần.
        </p>
      </div>

      {error && <BackendErrorAlert error={error} onClose={() => setError(null)} />}

      {successMsg && (
        <div className="p-4 rounded-lg bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs flex items-center space-x-2">
          <CheckCircle2 className="w-4 h-4 text-emerald-600 flex-shrink-0" />
          <span className="font-semibold">{successMsg}</span>
        </div>
      )}

      <Card title="Thiết lập Điểm số Sinh viên">
        <form onSubmit={handleSaveGrade} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Chọn Sinh Viên *
              </label>
              <select
                value={form.studentId}
                onChange={(e) => setForm({ ...form, studentId: e.target.value })}
                className="w-full px-3 py-2 text-xs rounded border border-slate-300 font-medium text-slate-900 bg-white"
              >
                <option value="">-- Chọn Sinh viên --</option>
                {students.map((st) => {
                  const sId = st.studentProfile?.id || st.studentProfile?.studentProfileId || st.id;
                  return (
                    <option key={st.id} value={sId}>
                      {st.fullName} ({st.username}) - {st.studentProfile?.studentCode || `ID ${st.id}`}
                    </option>
                  );
                })}
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Chọn Môn Học Đã Học *
              </label>
              <select
                value={form.courseId}
                onChange={(e) => setForm({ ...form, courseId: e.target.value })}
                className="w-full px-3 py-2 text-xs rounded border border-slate-300 font-medium text-slate-900 bg-white"
              >
                <option value="">-- Chọn Môn học --</option>
                {courses.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.code || c.courseCode} - {c.name || c.courseName}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Học Kỳ Đã Học *
              </label>
              <select
                value={form.semesterId}
                onChange={(e) => setForm({ ...form, semesterId: e.target.value })}
                className="w-full px-3 py-2 text-xs rounded border border-slate-300 font-medium text-slate-900 bg-white"
              >
                <option value="">-- Chọn Học kỳ --</option>
                {semesters.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.code || s.semesterCode} - {s.name || s.semesterName}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Điểm Tổng Kết (Thang 10)
              </label>
              <input
                type="number"
                step="0.1"
                min="0"
                max="10"
                value={form.finalScore}
                onChange={(e) => setForm({ ...form, finalScore: e.target.value })}
                placeholder="ví dụ: 8.5"
                className="w-full px-3 py-2 text-xs rounded border border-slate-300 font-mono text-slate-900 bg-white"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Trạng Thái Kết Quả Môn Học *
            </label>
            <div className="flex items-center space-x-4 pt-1">
              <label className="flex items-center space-x-2 text-xs font-medium text-slate-700 cursor-pointer">
                <input
                  type="radio"
                  name="status"
                  value="PASSED"
                  checked={form.status === 'PASSED'}
                  onChange={(e) => setForm({ ...form, status: e.target.value })}
                  className="text-emerald-600 focus:ring-emerald-500"
                />
                <Badge variant="PASSED">PASSED (Đạt - Thỏa điều kiện tiên quyết)</Badge>
              </label>

              <label className="flex items-center space-x-2 text-xs font-medium text-slate-700 cursor-pointer">
                <input
                  type="radio"
                  name="status"
                  value="FAILED"
                  checked={form.status === 'FAILED'}
                  onChange={(e) => setForm({ ...form, status: e.target.value })}
                  className="text-rose-600 focus:ring-rose-500"
                />
                <Badge variant="FAILED">FAILED (Không đạt - Bị chặn khi đăng ký môn sau)</Badge>
              </label>
            </div>
          </div>

          <div className="pt-3 border-t border-slate-100 flex justify-end">
            <Button type="submit" variant="primary" size="md" loading={loading}>
              Lưu Kết Quả Điểm Số
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
};

export default GradesPage;
