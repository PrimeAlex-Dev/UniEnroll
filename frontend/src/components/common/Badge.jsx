import React from 'react';

const BADGE_VARIANTS = {
  OPEN: 'bg-emerald-50 text-emerald-700 border-emerald-200',
  DRAFT: 'bg-slate-100 text-slate-700 border-slate-200',
  CLOSED: 'bg-amber-50 text-amber-700 border-amber-200',
  CANCELLED: 'bg-rose-50 text-rose-700 border-rose-200',
  ENROLLED: 'bg-blue-50 text-blue-700 border-blue-200',
  DROPPED: 'bg-zinc-100 text-zinc-600 border-zinc-200',
  ACTIVE: 'bg-emerald-50 text-emerald-700 border-emerald-200',
  LOCKED: 'bg-red-50 text-red-700 border-red-200',
  PASSED: 'bg-emerald-50 text-emerald-700 border-emerald-200',
  FAILED: 'bg-rose-50 text-rose-700 border-rose-200',
  SUCCESS: 'bg-emerald-50 text-emerald-700 border-emerald-200',
  STUDENT: 'bg-sky-50 text-sky-700 border-sky-200',
  LECTURER: 'bg-purple-50 text-purple-700 border-purple-200',
  ACADEMIC_STAFF: 'bg-indigo-50 text-indigo-700 border-indigo-200',
  ADMIN: 'bg-rose-50 text-rose-700 border-rose-200',
  DEFAULT: 'bg-slate-50 text-slate-700 border-slate-200',
};

const Badge = ({ children, variant, className = '' }) => {
  const variantClass = BADGE_VARIANTS[variant] || BADGE_VARIANTS.DEFAULT;

  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border ${variantClass} ${className}`}
    >
      {children || variant}
    </span>
  );
};

export default Badge;
