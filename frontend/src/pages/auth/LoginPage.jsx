import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import BackendErrorAlert from '../../components/common/BackendErrorAlert';
import Button from '../../components/common/Button';
import { LogIn, GraduationCap, Briefcase, BookOpen, Shield } from 'lucide-react';

const LoginPage = () => {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Raw submission - NO client blocking validation!
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const loggedInUser = await login(username, password);
      // Route intelligently based on user role
      if (loggedInUser.role === 'STUDENT') {
        navigate('/student/registration');
      } else if (loggedInUser.role === 'LECTURER') {
        navigate('/lecturer/roster');
      } else if (loggedInUser.role === 'ACADEMIC_STAFF') {
        navigate('/staff/sections');
      } else if (loggedInUser.role === 'ADMIN') {
        navigate('/admin/users');
      } else {
        navigate('/');
      }
    } catch (err) {
      console.error('Login error:', err);
      setError(err);
    } finally {
      setLoading(false);
    }
  };

  const handleQuickFill = (u, p) => {
    setUsername(u);
    setPassword(p);
    setError(null);
  };

  return (
    <div className="min-h-screen bg-slate-900 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <div className="flex justify-center">
          <div className="w-12 h-12 rounded-2xl bg-indigo-600 flex items-center justify-center text-white font-black text-xl shadow-lg shadow-indigo-500/30">
            UE
          </div>
        </div>
        <h2 className="mt-4 text-center text-2xl font-extrabold text-white tracking-tight">
          Hệ thống Đăng ký Học phần UniEnroll
        </h2>
        <p className="mt-1 text-center text-xs text-slate-400">
          Cổng thông tin đào tạo tín chỉ thời gian thực (SPA Frontend)
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-6 shadow-xl rounded-2xl sm:px-10 border border-slate-100">
          {/* Backend Error Alert Renderer */}
          {error && (
            <div className="mb-5">
              <BackendErrorAlert error={error} onClose={() => setError(null)} />
            </div>
          )}

          <form onSubmit={handleSubmit} autoComplete="off" className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Tên đăng nhập (Username)
              </label>
              <input
                type="text"
                autoComplete="off"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Ví dụ: student, staff, lecturer, admin..."
                className="w-full px-3.5 py-2.5 rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-sm transition-all text-slate-900 bg-white"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Mật khẩu (Password)
              </label>
              <input
                type="password"
                autoComplete="new-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Nhập mật khẩu..."
                className="w-full px-3.5 py-2.5 rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-sm transition-all text-slate-900 bg-white"
              />
            </div>

            <div className="pt-2">
              <Button
                type="submit"
                variant="primary"
                loading={loading}
                icon={LogIn}
                className="w-full py-2.5"
              >
                Đăng Nhập Vào Hệ Thống
              </Button>
            </div>
          </form>

          {/* Quick-Fill Testing Accounts Panel */}
          <div className="mt-8 pt-6 border-t border-slate-100">
            <div className="text-center mb-3">
              <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
                ⚡ Chọn nhanh tài khoản kiểm thử (Password: 123456)
              </span>
            </div>
            <div className="grid grid-cols-2 gap-2 text-xs">
              <button
                type="button"
                onClick={() => handleQuickFill('student', '123456')}
                className="flex items-center p-2 rounded-lg border border-slate-200 hover:border-sky-500 hover:bg-sky-50 transition-all text-left"
              >
                <GraduationCap className="w-4 h-4 text-sky-600 mr-2 flex-shrink-0" />
                <div>
                  <div className="font-semibold text-slate-800">Sinh viên</div>
                  <div className="text-[10px] text-slate-400 font-mono">student</div>
                </div>
              </button>

              <button
                type="button"
                onClick={() => handleQuickFill('staff', '123456')}
                className="flex items-center p-2 rounded-lg border border-slate-200 hover:border-indigo-500 hover:bg-indigo-50 transition-all text-left"
              >
                <Briefcase className="w-4 h-4 text-indigo-600 mr-2 flex-shrink-0" />
                <div>
                  <div className="font-semibold text-slate-800">Phòng Đào Tạo</div>
                  <div className="text-[10px] text-slate-400 font-mono">staff</div>
                </div>
              </button>

              <button
                type="button"
                onClick={() => handleQuickFill('lecturer', '123456')}
                className="flex items-center p-2 rounded-lg border border-slate-200 hover:border-purple-500 hover:bg-purple-50 transition-all text-left"
              >
                <BookOpen className="w-4 h-4 text-purple-600 mr-2 flex-shrink-0" />
                <div>
                  <div className="font-semibold text-slate-800">Giảng viên</div>
                  <div className="text-[10px] text-slate-400 font-mono">lecturer</div>
                </div>
              </button>

              <button
                type="button"
                onClick={() => handleQuickFill('admin', '123456')}
                className="flex items-center p-2 rounded-lg border border-slate-200 hover:border-rose-500 hover:bg-rose-50 transition-all text-left"
              >
                <Shield className="w-4 h-4 text-rose-600 mr-2 flex-shrink-0" />
                <div>
                  <div className="font-semibold text-slate-800">Quản trị viên</div>
                  <div className="text-[10px] text-slate-400 font-mono">admin</div>
                </div>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
