import React from 'react';
import { AlertCircle, XCircle } from 'lucide-react';

const BackendErrorAlert = ({ error, onClose, className = '' }) => {
  if (!error) return null;

  const code = error.code || 'ERR_UNKNOWN';
  const message = error.message || 'Đã xảy ra lỗi không xác định từ hệ thống';
  let fieldErrors = error.fieldErrors || [];
  if (fieldErrors.length === 0 && error.details && typeof error.details === 'object' && !Array.isArray(error.details)) {
    fieldErrors = Object.entries(error.details).map(([field, message]) => ({
      field,
      message: String(message),
    }));
  }

  return (
    <div
      className={`rounded-lg border border-red-300 bg-red-50 p-4 shadow-sm text-red-900 transition-all ${className}`}
      role="alert"
    >
      <div className="flex items-start justify-between">
        <div className="flex items-start space-x-3">
          <AlertCircle className="w-5 h-5 text-red-600 mt-0.5 flex-shrink-0" />
          <div className="space-y-1">
            <div className="flex items-center space-x-2 flex-wrap">
              <span className="font-semibold text-sm text-red-800">LỖI HỆ THỐNG / BACKEND:</span>
              <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-mono font-bold bg-red-200 text-red-900 border border-red-300">
                {code}
              </span>
            </div>
            <p className="text-sm font-medium text-red-700">{message}</p>

            {fieldErrors.length > 0 && (
              <div className="mt-2 pt-2 border-t border-red-200">
                <span className="text-xs font-semibold text-red-800 uppercase tracking-wider block mb-1">
                  Chi tiết vi phạm ràng buộc dữ liệu:
                </span>
                <ul className="list-disc list-inside space-y-0.5 text-xs text-red-700">
                  {fieldErrors.map((fe, index) => (
                    <li key={index}>
                      <strong className="font-mono text-red-900">{fe.field}</strong>: {fe.message}
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        </div>
        {onClose && (
          <button
            type="button"
            onClick={onClose}
            className="text-red-400 hover:text-red-600 transition-colors p-1"
            title="Đóng cảnh báo"
          >
            <XCircle className="w-5 h-5" />
          </button>
        )}
      </div>
    </div>
  );
};

export default BackendErrorAlert;
