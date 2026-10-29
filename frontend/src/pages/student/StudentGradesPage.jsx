import React, { useState, useEffect, useMemo } from 'react';
import { registrationApi } from '../../api/registrationApi';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import BackendErrorAlert from '../../components/common/BackendErrorAlert';
import { Award, BookOpen, CheckCircle, AlertTriangle, RefreshCw, Printer } from 'lucide-react';

const scoreToGrade = (score) => {
  if (score == null) return { letter: 'N/A', gpa4: 0, passed: false };
  if (score >= 8.5) return { letter: 'A', gpa4: 4.0, passed: true };
  if (score >= 8.0) return { letter: 'B+', gpa4: 3.5, passed: true };
  if (score >= 7.0) return { letter: 'B', gpa4: 3.0, passed: true };
  if (score >= 6.5) return { letter: 'C+', gpa4: 2.5, passed: true };
  if (score >= 5.5) return { letter: 'C', gpa4: 2.0, passed: true };
  if (score >= 5.0) return { letter: 'D+', gpa4: 1.5, passed: true };
  if (score >= 4.0) return { letter: 'D', gpa4: 1.0, passed: true };
  return { letter: 'F', gpa4: 0.0, passed: false };
};

const getClassification = (gpa4) => {
  if (gpa4 >= 3.6) return { text: 'Xuất sắc', color: 'text-purple-600 bg-purple-50 border-purple-200' };
  if (gpa4 >= 3.2) return { text: 'Giỏi', color: 'text-blue-600 bg-blue-50 border-blue-200' };
  if (gpa4 >= 2.5) return { text: 'Khá', color: 'text-emerald-600 bg-emerald-50 border-emerald-200' };
  if (gpa4 >= 2.0) return { text: 'Trung bình', color: 'text-amber-600 bg-amber-50 border-amber-200' };
  return { text: 'Yếu / Cảnh báo', color: 'text-rose-600 bg-rose-50 border-rose-200' };
};

const StudentGradesPage = () => {
  const [grades, setGrades] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [selectedSemester, setSelectedSemester] = useState('ALL');

  const fetchGrades = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await registrationApi.getMyTranscript();
      setGrades(res.data || []);
    } catch (err) {
      console.error(err);
      setError(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchGrades();
  }, []);

  const semestersList = useMemo(() => {
    const map = new Map();
    grades.forEach((g) => {
      if (g.semesterId && g.semesterName) {
        map.set(String(g.semesterId), g.semesterName);
      }
    });
    return Array.from(map.entries()).map(([id, name]) => ({ id, name }));
  }, [grades]);

  const filteredGrades = useMemo(() => {
    if (selectedSemester === 'ALL') return grades;
    return grades.filter((g) => String(g.semesterId) === selectedSemester);
  }, [grades, selectedSemester]);

  // Tính toán thống kê
  const stats = useMemo(() => {
    let totalCredits = 0;
    let earnedCredits = 0;
    let sumWeight10 = 0;
    let sumWeight4 = 0;

    filteredGrades.forEach((g) => {
      const cr = g.totalCredits || 0;
      const score = g.finalScore;
      if (score != null) {
        const { gpa4, passed } = scoreToGrade(score);
        totalCredits += cr;
        sumWeight10 += score * cr;
        sumWeight4 += gpa4 * cr;
        if (passed || g.status === 'PASSED') {
          earnedCredits += cr;
        }
      }
    });

    const gpa10 = totalCredits > 0 ? (sumWeight10 / totalCredits).toFixed(2) : '0.00';
    const gpa4 = totalCredits > 0 ? (sumWeight4 / totalCredits).toFixed(2) : '0.00';
    const classification = getClassification(Number(gpa4));

    return {
      totalCourses: filteredGrades.length,
      totalCredits,
      earnedCredits,
      gpa10,
      gpa4,
      classification,
    };
  }, [filteredGrades]);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Bảng Điểm & Kết Quả Học Tập</h1>
          <p className="text-sm text-gray-500 mt-1">
            Tra cứu kết quả học tập cá nhân, điểm tổng kết môn học và điểm tích lũy học vụ
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Button
            variant="outline"
            onClick={() => window.print()}
            className="flex items-center gap-2"
          >
            <Printer className="w-4 h-4" />
            <span>In Bảng Điểm</span>
          </Button>
          <Button
            variant="outline"
            onClick={fetchGrades}
            disabled={loading}
            className="flex items-center gap-2"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
            <span>Làm Mới</span>
          </Button>
        </div>
      </div>

      <BackendErrorAlert error={error} />

      {/* Filter bar */}
      <Card className="p-4 bg-white border border-gray-200">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <label className="text-sm font-medium text-gray-700">Lọc theo Học kỳ:</label>
            <select
              value={selectedSemester}
              onChange={(e) => setSelectedSemester(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500 min-w-[200px]"
            >
              <option value="ALL">-- Tất cả học kỳ (Toàn khóa) --</option>
              {semestersList.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))}
            </select>
          </div>
          <div className="text-sm text-gray-500">
            Tổng cộng: <strong className="text-gray-900">{filteredGrades.length}</strong> học phần đã ghi nhận kết quả
          </div>
        </div>
      </Card>

      {/* Summary KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-white rounded-xl border border-gray-200 p-5 shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-gray-500">
              GPA Tích Lũy (Thang 4)
            </span>
            <span className="p-2 bg-indigo-50 text-indigo-600 rounded-lg">
              <Award className="w-5 h-5" />
            </span>
          </div>
          <div className="mt-2 flex items-baseline gap-2">
            <span className="text-3xl font-extrabold text-indigo-600">{stats.gpa4}</span>
            <span className="text-xs text-gray-400">/ 4.00</span>
          </div>
          <div className="mt-2 text-xs">
            <span className={`inline-block px-2 py-0.5 font-medium rounded-full border ${stats.classification.color}`}>
              {stats.classification.text}
            </span>
          </div>
        </div>

        <div className="bg-white rounded-xl border border-gray-200 p-5 shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-gray-500">
              Điểm TB (Thang 10)
            </span>
            <span className="p-2 bg-blue-50 text-blue-600 rounded-lg">
              <Award className="w-5 h-5" />
            </span>
          </div>
          <div className="mt-2 flex items-baseline gap-2">
            <span className="text-3xl font-extrabold text-blue-600">{stats.gpa10}</span>
            <span className="text-xs text-gray-400">/ 10.00</span>
          </div>
          <p className="mt-2 text-xs text-gray-500">Trung bình theo trọng số tín chỉ</p>
        </div>

        <div className="bg-white rounded-xl border border-gray-200 p-5 shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-gray-500">
              Tín Chỉ Tích Lũy Đạt
            </span>
            <span className="p-2 bg-emerald-50 text-emerald-600 rounded-lg">
              <CheckCircle className="w-5 h-5" />
            </span>
          </div>
          <div className="mt-2 flex items-baseline gap-2">
            <span className="text-3xl font-extrabold text-emerald-600">{stats.earnedCredits}</span>
            <span className="text-xs text-gray-400">/ {stats.totalCredits} TC đã học</span>
          </div>
          <p className="mt-2 text-xs text-emerald-700">
            Tỷ lệ đạt: {stats.totalCredits > 0 ? Math.round((stats.earnedCredits / stats.totalCredits) * 100) : 100}%
          </p>
        </div>

        <div className="bg-white rounded-xl border border-gray-200 p-5 shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-gray-500">
              Số Môn Đã Hoàn Thành
            </span>
            <span className="p-2 bg-purple-50 text-purple-600 rounded-lg">
              <BookOpen className="w-5 h-5" />
            </span>
          </div>
          <div className="mt-2 flex items-baseline gap-2">
            <span className="text-3xl font-extrabold text-gray-900">{stats.totalCourses}</span>
            <span className="text-xs text-gray-400">môn học</span>
          </div>
          <p className="mt-2 text-xs text-gray-500">
            Đợt lọc: {selectedSemester === 'ALL' ? 'Toàn khóa' : 'Học kỳ đã chọn'}
          </p>
        </div>
      </div>

      {/* Main Table */}
      <Card className="overflow-hidden border border-gray-200 bg-white">
        <div className="p-4 border-b border-gray-100 flex items-center justify-between">
          <h2 className="text-base font-semibold text-gray-900">Chi Tiết Điểm Các Học Phần</h2>
          <span className="text-xs text-gray-500">Quy chế đào tạo theo hệ thống tín chỉ</span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-gray-600">
            <thead className="bg-gray-50 text-xs font-semibold uppercase text-gray-500 border-b border-gray-200">
              <tr>
                <th className="py-3 px-4 text-center w-12">STT</th>
                <th className="py-3 px-4">Mã Môn</th>
                <th className="py-3 px-4">Tên Môn Học</th>
                <th className="py-3 px-4 text-center">Tín Chỉ</th>
                <th className="py-3 px-4">Học Kỳ</th>
                <th className="py-3 px-4 text-center">Điểm Hệ 10</th>
                <th className="py-3 px-4 text-center">Điểm Chữ</th>
                <th className="py-3 px-4 text-center">Hệ 4</th>
                <th className="py-3 px-4 text-center">Kết Quả</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {filteredGrades.length === 0 ? (
                <tr>
                  <td colSpan={9} className="py-12 text-center text-gray-400">
                    <div className="flex flex-col items-center justify-center gap-2">
                      <AlertTriangle className="w-8 h-8 text-gray-300" />
                      <p className="text-sm">Chưa có dữ liệu điểm cho học kỳ này.</p>
                      <p className="text-xs text-gray-400">
                        Điểm sẽ xuất hiện sau khi cán bộ đào tạo hoặc giảng viên hoàn tất nhập điểm học phần.
                      </p>
                    </div>
                  </td>
                </tr>
              ) : (
                filteredGrades.map((g, idx) => {
                  const { letter, gpa4, passed } = scoreToGrade(g.finalScore);
                  const isPassed = g.status === 'PASSED' || (g.finalScore != null && g.finalScore >= 4.0);

                  return (
                    <tr key={g.id || idx} className="hover:bg-gray-50 transition-colors">
                      <td className="py-3 px-4 text-center text-xs text-gray-400 font-medium">
                        {idx + 1}
                      </td>
                      <td className="py-3 px-4 font-mono font-medium text-indigo-600">
                        {g.courseCode}
                      </td>
                      <td className="py-3 px-4 font-medium text-gray-900">
                        {g.courseName}
                      </td>
                      <td className="py-3 px-4 text-center font-medium">
                        {g.totalCredits}
                      </td>
                      <td className="py-3 px-4 text-xs text-gray-500">
                        {g.semesterName || '-'}
                      </td>
                      <td className="py-3 px-4 text-center font-bold text-gray-900">
                        {g.finalScore != null ? g.finalScore.toFixed(1) : '-'}
                      </td>
                      <td className="py-3 px-4 text-center">
                        <span className={`inline-block px-2 py-0.5 rounded text-xs font-bold ${
                          letter === 'A' ? 'bg-purple-100 text-purple-700' :
                          letter.startsWith('B') ? 'bg-blue-100 text-blue-700' :
                          letter.startsWith('C') ? 'bg-emerald-100 text-emerald-700' :
                          letter.startsWith('D') ? 'bg-amber-100 text-amber-700' :
                          'bg-rose-100 text-rose-700'
                        }`}>
                          {letter}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-center font-medium text-gray-700">
                        {g.finalScore != null ? gpa4.toFixed(1) : '-'}
                      </td>
                      <td className="py-3 px-4 text-center">
                        <Badge variant={isPassed ? 'success' : 'danger'}>
                          {isPassed ? 'ĐẠT' : 'KHÔNG ĐẠT'}
                        </Badge>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  );
};

export default StudentGradesPage;
