import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ShieldAlert, ArrowLeft, Home } from 'lucide-react';

export const UnauthorizedPage: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-[70vh] flex flex-col items-center justify-center text-center p-6 space-y-4">
      <div className="w-16 h-16 rounded-2xl bg-rose-50 text-rose-600 flex items-center justify-center shadow-xs border border-rose-200">
        <ShieldAlert className="w-8 h-8" />
      </div>
      <h1 className="text-xl font-bold text-slate-900">403 - Access Prohibited</h1>
      <p className="text-xs text-slate-500 max-w-sm">
        Your authenticated user role does not possess permissions to view or execute actions on this administrative resource.
      </p>
      <div className="flex items-center gap-3 pt-2">
        <button
          onClick={() => navigate(-1)}
          className="px-3.5 py-2 border border-slate-300 rounded-lg text-xs font-semibold text-slate-700 hover:bg-slate-50 flex items-center gap-1.5"
        >
          <ArrowLeft className="w-3.5 h-3.5" /> Go Back
        </button>
        <button
          onClick={() => navigate('/')}
          className="px-4 py-2 bg-brand-600 rounded-lg text-xs font-semibold text-white hover:bg-brand-700 flex items-center gap-1.5 shadow-xs"
        >
          <Home className="w-3.5 h-3.5" /> Return Dashboard
        </button>
      </div>
    </div>
  );
};

export const NotFoundPage: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-[70vh] flex flex-col items-center justify-center text-center p-6 space-y-4">
      <div className="text-4xl font-extrabold text-slate-300">404</div>
      <h1 className="text-xl font-bold text-slate-900">Resource Not Found</h1>
      <p className="text-xs text-slate-500 max-w-sm">
        The requested support case, profile, or route does not exist or has been relocated.
      </p>
      <button
        onClick={() => navigate('/')}
        className="px-4 py-2 bg-brand-600 rounded-lg text-xs font-semibold text-white hover:bg-brand-700 flex items-center gap-1.5 shadow-xs"
      >
        <Home className="w-3.5 h-3.5" /> Return Dashboard
      </button>
    </div>
  );
};
