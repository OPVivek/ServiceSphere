import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { apiClient } from '../api/client';
import { SlaPolicy, SlaDashboard, SupportCase } from '../types';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { 
  Timer, 
  AlertTriangle, 
  Clock, 
  CheckCircle2, 
  Settings, 
  TrendingUp, 
  Save 
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const SlaMonitorPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [dashboard, setDashboard] = useState<SlaDashboard | null>(null);
  const [policies, setPolicies] = useState<SlaPolicy[]>([]);
  const [loading, setLoading] = useState(true);

  // Policy editing state
  const [editingPriority, setEditingPriority] = useState<string | null>(null);
  const [editResponseHours, setEditResponseHours] = useState<number>(0);
  const [editResolutionHours, setEditResolutionHours] = useState<number>(0);

  const fetchSlaData = async () => {
    setLoading(true);
    try {
      const [dashRes, policiesRes] = await Promise.all([
        apiClient.get('/sla/dashboard'),
        apiClient.get('/sla/policies'),
      ]);
      setDashboard(dashRes.data.data);
      setPolicies(policiesRes.data.data);
    } catch (err) {
      console.error('Failed to load SLA monitor data', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSlaData();
  }, []);

  const handleStartEdit = (policy: SlaPolicy) => {
    setEditingPriority(policy.priority);
    setEditResponseHours(policy.firstResponseHours);
    setEditResolutionHours(policy.resolutionHours);
  };

  const handleSavePolicy = async (priority: string) => {
    try {
      await apiClient.put(`/sla/policies/${priority}?firstResponseHours=${editResponseHours}&resolutionHours=${editResolutionHours}`);
      setEditingPriority(null);
      fetchSlaData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to update policy');
    }
  };

  if (loading || !dashboard) {
    return <LoadingSpinner message="Auditing SLA compliance..." />;
  }

  const isAdmin = user?.role === 'ADMIN';

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-extrabold text-slate-900 tracking-tight">
          SLA Compliance & Target Monitoring
        </h1>
        <p className="text-xs text-slate-500 mt-0.5">
          Real-time service level agreement guarantees, early warning detection, and escalation triggers
        </p>
      </div>

      {/* 4 SLA KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
          <span className="text-xs font-bold text-slate-500 uppercase tracking-wide">Active Cases</span>
          <p className="text-2xl font-extrabold text-slate-900 mt-2">{dashboard.totalActiveCases}</p>
          <span className="text-[11px] text-slate-400 mt-1 block">Subject to SLA deadlines</span>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
          <span className="text-xs font-bold text-emerald-600 uppercase tracking-wide">Within SLA</span>
          <p className="text-2xl font-extrabold text-emerald-600 mt-2">{dashboard.withinSlaCases}</p>
          <span className="text-[11px] text-emerald-600 mt-1 block">Operating within targets</span>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
          <span className="text-xs font-bold text-amber-600 uppercase tracking-wide">Approaching Breach</span>
          <p className="text-2xl font-extrabold text-amber-600 mt-2">{dashboard.approachingBreachCases}</p>
          <span className="text-[11px] text-amber-600 mt-1 block">Deadline within &lt;2 hours</span>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
          <span className="text-xs font-bold text-rose-600 uppercase tracking-wide">Breached Cases</span>
          <p className="text-2xl font-extrabold text-rose-600 mt-2">{dashboard.breachedCases}</p>
          <span className="text-[11px] text-rose-500 mt-1 block">Escalated to management</span>
        </div>
      </div>

      {/* SLA Policy Definitions Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="p-4 border-b border-slate-200 bg-slate-50/50 flex items-center justify-between">
          <div>
            <h2 className="text-xs font-bold text-slate-800 uppercase tracking-wide flex items-center gap-1.5">
              <Settings className="w-4 h-4 text-brand-600" />
              SLA Priority Policy Matrix
            </h2>
            <p className="text-[11px] text-slate-500">Configured response and resolution targets stored in UTC</p>
          </div>
          {isAdmin && (
            <span className="text-[10px] font-bold text-indigo-700 bg-indigo-50 border border-indigo-200 px-2 py-0.5 rounded">
              ADMIN CONFIGURABLE
            </span>
          )}
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-slate-200 bg-slate-50 text-[11px] font-bold text-slate-500 uppercase tracking-wider">
                <th className="py-3 px-4">Priority Level</th>
                <th className="py-3 px-4">First Response Target</th>
                <th className="py-3 px-4">Resolution Deadline</th>
                <th className="py-3 px-4">Description</th>
                {isAdmin && <th className="py-3 px-4 text-right">Actions</th>}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-xs text-slate-700">
              {policies.map((p) => {
                const isEditing = editingPriority === p.priority;
                return (
                  <tr key={p.id} className="hover:bg-slate-50 transition-colors">
                    <td className="py-3 px-4 font-bold text-slate-900">{p.priority}</td>
                    <td className="py-3 px-4">
                      {isEditing ? (
                        <div className="flex items-center gap-1">
                          <input
                            type="number"
                            value={editResponseHours}
                            onChange={(e) => setEditResponseHours(Number(e.target.value))}
                            className="w-16 p-1 text-xs border border-slate-300 rounded"
                          />
                          <span className="text-slate-500">hours</span>
                        </div>
                      ) : (
                        <span className="font-semibold text-brand-600">{p.firstResponseHours} hours</span>
                      )}
                    </td>
                    <td className="py-3 px-4">
                      {isEditing ? (
                        <div className="flex items-center gap-1">
                          <input
                            type="number"
                            value={editResolutionHours}
                            onChange={(e) => setEditResolutionHours(Number(e.target.value))}
                            className="w-16 p-1 text-xs border border-slate-300 rounded"
                          />
                          <span className="text-slate-500">hours</span>
                        </div>
                      ) : (
                        <span className="font-semibold text-indigo-600">{p.resolutionHours} hours</span>
                      )}
                    </td>
                    <td className="py-3 px-4 text-slate-500 max-w-sm">{p.description}</td>
                    {isAdmin && (
                      <td className="py-3 px-4 text-right">
                        {isEditing ? (
                          <div className="flex items-center justify-end gap-1.5">
                            <button
                              onClick={() => handleSavePolicy(p.priority)}
                              className="px-2.5 py-1 bg-brand-600 text-white rounded text-xs font-semibold flex items-center gap-1"
                            >
                              <Save className="w-3 h-3" /> Save
                            </button>
                            <button
                              onClick={() => setEditingPriority(null)}
                              className="px-2.5 py-1 border border-slate-300 text-slate-600 rounded text-xs font-semibold"
                            >
                              Cancel
                            </button>
                          </div>
                        ) : (
                          <button
                            onClick={() => handleStartEdit(p)}
                            className="px-2.5 py-1 border border-slate-300 hover:bg-slate-100 text-slate-700 rounded text-xs font-semibold"
                          >
                            Edit Targets
                          </button>
                        )}
                      </td>
                    )}
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
