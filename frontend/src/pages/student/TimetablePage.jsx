import React, { useState, useEffect } from 'react';
import { registrationApi } from '../../api/registrationApi';
import { schedulingApi } from '../../api/schedulingApi';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import BackendErrorAlert from '../../components/common/BackendErrorAlert';
import { Calendar, RefreshCw, Printer } from 'lucide-react';

const DAYS = [
  { value: 2, label: 'Thứ Hai' },
  { value: 3, label: 'Thứ Ba' },
  { value: 4, label: 'Thứ Tư' },
  { value: 5, label: 'Thứ Năm' },
  { value: 6, label: 'Thứ Sáu' },
  { value: 7, label: 'Thứ Bảy' },
  { value: 8, label: 'Chủ Nhật' },
];

const PERIODS = Array.from({ length: 12 }, (_, i) => i + 1);

const COLOR_PALETTE = [
  'bg-indigo-50 border-indigo-200 text-indigo-900',
  'bg-emerald-50 border-emerald-200 text-emerald-900',
  'bg-amber-50 border-amber-200 text-amber-900',
  'bg-purple-50 border-purple-200 text-purple-900',
  'bg-sky-50 border-sky-200 text-sky-900',
  'bg-rose-50 border-rose-200 text-rose-900',
];

const TimetablePage = () => {
  const [semesters, setSemesters] = useState([]);
  const [selectedSemesterId, setSelectedSemesterId] = useState('');
  const [schedule, setSchedule] = useState(null);
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

  const fetchSchedule = async (semesterId) => {
    if (!semesterId) return;
    setLoading(true);
    setError(null);
    try {
      const res = await registrationApi.getMySchedule(Number(semesterId));
      setSchedule(res.data || null);
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
      fetchSchedule(selectedSemesterId);
    }
  }, [selectedSemesterId]);

  // Map slots to lookup: key = `${dayOfWeek}_${period}`
  const slotMap = {};
  if (schedule?.enrolledSections) {
    schedule.enrolledSections.forEach((sec, secIdx) => {
      const color = COLOR_PALETTE[secIdx % COLOR_PALETTE.length];
      sec.scheduleSlots?.forEach((slot) => {
        for (let p = slot.startPeriod; p <= slot.endPeriod; p++) {
          slotMap[`${slot.dayOfWeek}_${p}`] = {
            section: sec,
            slot,
            color,
            isFirstPeriod: p === slot.startPeriod,
            span: slot.endPeriod - slot.startPeriod + 1,
          };
        }
      });
    });
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight flex items-center space-x-2">
            <Calendar className="w-5 h-5 text-indigo-600" />
            <span>Thời Khóa Biểu Tuần Trực Quan (UC-16)</span>
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Ma trận lịch học tuần theo các lớp học phần sinh viên đã ghi danh thành công
          </p>
        </div>
        <div className="flex items-center space-x-2">
          <Button variant="secondary" size="sm" icon={Printer} onClick={() => window.print()}>
            In TKB
          </Button>
          <Button
            variant="secondary"
            size="sm"
            icon={RefreshCw}
            onClick={() => fetchSchedule(selectedSemesterId)}
          >
            Làm mới
          </Button>
        </div>
      </div>

      {error && <BackendErrorAlert error={error} onClose={() => setError(null)} />}

      {/* Header Info Banner */}
      <Card bodyClassName="p-4 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div className="flex items-center space-x-4">
          <div>
            <span className="text-xs text-slate-500 block uppercase font-bold">Học kỳ hiển thị</span>
            <select
              value={selectedSemesterId}
              onChange={(e) => setSelectedSemesterId(e.target.value)}
              className="mt-1 px-3 py-1.5 text-xs rounded border border-slate-300 font-semibold text-slate-900 bg-white"
            >
              {semesters.map((sem) => (
                <option key={sem.id} value={sem.id}>
                  {sem.code || sem.semesterCode} - {sem.name || sem.semesterName}
                </option>
              ))}
            </select>
          </div>

          <div className="border-l border-slate-200 pl-4">
            <span className="text-xs text-slate-500 block uppercase font-bold">Tổng tín chỉ tích lũy</span>
            <div className="text-lg font-mono font-bold text-indigo-600">
              {schedule?.totalCredits || 0} TC
            </div>
          </div>
        </div>

        <div className="text-xs text-slate-500 font-mono">
          Sinh viên: <strong className="text-slate-800">{schedule?.studentName}</strong> ({schedule?.studentCode})
        </div>
      </Card>

      {/* Timetable Matrix Grid (7 Columns x 12 Rows) */}
      <Card title="Lưới Thời Khóa Biểu (Thứ 2 - Chủ Nhật, Tiết 1 - 12)">
        {loading ? (
          <div className="py-16 text-center text-xs text-slate-400">Đang tải thời khóa biểu...</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full border-collapse border border-slate-200 text-xs text-center min-w-[700px]">
              <thead>
                <tr className="bg-slate-100 text-slate-700">
                  <th className="border border-slate-200 px-3 py-2 w-20">Tiết \ Thứ</th>
                  {DAYS.map((day) => (
                    <th key={day.value} className="border border-slate-200 px-3 py-2">
                      {day.label}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {PERIODS.map((period) => (
                  <tr key={period} className="h-12 hover:bg-slate-50/50">
                    <td className="border border-slate-200 font-mono font-semibold bg-slate-50 text-slate-600">
                      Tiết {period}
                    </td>
                    {DAYS.map((day) => {
                      const cell = slotMap[`${day.value}_${period}`];
                      if (!cell) {
                        return <td key={day.value} className="border border-slate-200" />;
                      }

                      // Render block content only on the first period of the block
                      return (
                        <td
                          key={day.value}
                          className={`border border-slate-200 p-1 text-left align-top ${cell.color}`}
                        >
                          {cell.isFirstPeriod ? (
                            <div className="space-y-0.5">
                              <div className="font-bold text-[11px] leading-tight">
                                {cell.section.courseName}
                              </div>
                              <div className="font-mono text-[10px] opacity-80">
                                Lớp: {cell.section.sectionCode}
                              </div>
                              <div className="text-[10px] font-semibold opacity-90">
                                P.{cell.section.roomNumber} ({cell.section.building})
                              </div>
                              <div className="text-[9px] opacity-75 font-mono">
                                Tiết {cell.slot.startPeriod}-{cell.slot.endPeriod}
                              </div>
                            </div>
                          ) : (
                            <div className="text-[9px] opacity-50 font-mono text-center">⋮</div>
                          )}
                        </td>
                      );
                    })}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  );
};

export default TimetablePage;
