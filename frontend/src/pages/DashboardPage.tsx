import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { apiClient } from '../api/client';
import { SupportCase, AnalyticsData, SlaDashboard } from '../types';
import { StatusBadge, PriorityBadge } from '../components/common/StatusBadge';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { 
  Inbox, 
  Clock, 
  AlertTriangle, 
  CheckCircle, 
  PlusCircle, 
  ArrowRight, 
  ShieldAlert, 
  Headphones, 
  TrendingUp, 
  UserCheck 
} from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [recentCases, setRecentCases] = useState<SupportCase[]>([]);
  const [analytics, setAnalytics] = useState<AnalyticsData | null>(null);
  const [slaData, setSlaData] = useState<SlaDashboard | null>(null);

  useEffect(() => {
    const loadDashboardData = async () => {
      setLoading(true);
      try {
        if (user?.role === 'CUSTOMER') {
          // Customer dashboard data
          const res = await apiClient.get('/cases?size=5');
          setRecentCases(res.data.data.content);
        } else {
          // Staff dashboard data
          const [casesRes, analyticsRes, slaRes] = await Promise.all([
            apiClient.get('/cases?size=6'),
            apiClient.get('/analytics'),
            apiClient.get('/sla/dashboard'),
          ]);
          setRecentCases(casesRes.data.data.content);
          setAnalytics(analyticsRes.data.data);
          setSlaData(slaRes.data.data);
        }
      } catch (err) {
        console.error('Failed to load dashboard data', err);
      } finally {
        setLoading(false);
      }
    };

    if (user) {
      loadDashboardData();
    }
  }, [user]);

  if (loading) {
    return <LoadingSpinner message="Loading workspace dashboard..." />;
  }

  // --- CUSTOMER DASHBOARD VIEW ---
  if (user?.role === 'CUSTOMER') {
    const openCount = recentCases.filter((c) => c.status !== 'RESOLVED' && c.status !== 'CLOSED').length;
    const resolvedCount = recentCases.filter((c) => c.status === 'RESOLVED' || c.status === 'CLOSED').length;

    return (
      <div className="space-y-6">
        {/* Welcome Header */}
        <div className="bg-gradient-to-r from-slate-900 to-slate-800 rounded-2xl p-6 sm:p-8 text-white shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <span className="text-xs font-bold uppercase tracking-wider text-brand-400 bg-brand-950/60 px-2.5 py-1 rounded-md border border-brand-800">
              Customer Support Portal
            </span>
            <h1 className="text-2xl font-bold tracking-tight mt-2">
              Welcome back, {user.firstName}!
            </h1>
            <p className="text-sm text-slate-300 mt-1">
              Submit support tickets, monitor real-time resolution SLA, and access troubleshooting guides.
            </p>
          </div>
          <button
            onClick={() => navigate('/cases/new')}
            className="self-start sm:self-auto bg-brand-600 hover:bg-brand-500 text-white font-semibold px-4 py-2.5 rounded-xl shadow-md transition-all flex items-center gap-2 text-sm shrink-0"
          >
            <PlusCircle className="w-4 h-4" />
            Open Support Case
          </button>
        </div>

        {/* Customer Stats Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-slate-500 uppercase tracking-wide">Active Cases</span>
              <div className="w-8 h-8 rounded-lg bg-blue-50 text-brand-600 flex items-center justify-center">
                <Clock className="w-4 h-4" />
              </div>
            </div>
            <p className="text-2xl font-extrabold text-slate-900 mt-2">{openCount}</p>
            <span className="text-xs text-slate-400 mt-1 block">In review by support team</span>
          </div>

          <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-slate-500 uppercase tracking-wide">Resolved Issues</span>
              <div className="w-8 h-8 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center">
                <CheckCircle className="w-4 h-4" />
              </div>
            </div>
            <p className="text-2xl font-extrabold text-slate-900 mt-2">{resolvedCount}</p>
            <span className="text-xs text-slate-400 mt-1 block">Successfully resolved tickets</span>
          </div>

          <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-slate-500 uppercase tracking-wide">Customer 360</span>
              <div className="w-8 h-8 rounded-lg bg-purple-50 text-purple-600 flex items-center justify-center">
                <UserCheck className="w-4 h-4" />
              </div>
            </div>
            <Link
              to={`/customers/${user.id}/360`}
              className="mt-3 inline-flex items-center gap-1.5 text-xs font-bold text-brand-600 hover:text-brand-800"
            >
              View Full Support Profile <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>
        </div>

        {/* Customer Recent Cases Table */}
        <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
          <div className="p-4 sm:p-5 border-b border-slate-200 flex items-center justify-between bg-slate-50/50">
            <div>
              <h2 className="text-sm font-bold text-slate-900">Your Support Cases</h2>
              <p className="text-xs text-slate-500">Track current status and replies</p>
            </div>
            <Link to="/cases" className="text-xs font-semibold text-brand-600 hover:text-brand-800 flex items-center gap-1">
              View all <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-slate-200 bg-slate-50/80 text-[11px] font-bold text-slate-500 uppercase tracking-wider">
                  <th className="py-3 px-4">Case Number</th>
                  <th className="py-3 px-4">Subject</th>
                  <th className="py-3 px-4">Category</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4">Assigned Agent</th>
                  <th className="py-3 px-4">Created</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-xs text-slate-700">
                {recentCases.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="py-8 text-center text-slate-400">
                      No support cases submitted yet. Click "Open Support Case" to get help.
                    </td>
                  </tr>
                ) : (
                  recentCases.map((c) => (
                    <tr
                      key={c.id}
                      onClick={() => navigate(`/cases/${c.id}`)}
                      className="hover:bg-slate-50 cursor-pointer transition-colors"
                    >
                      <td className="py-3 px-4 font-mono font-bold text-brand-600">{c.caseNumber}</td>
                      <td className="py-3 px-4 font-semibold text-slate-900 max-w-xs truncate">{c.title}</td>
                      <td className="py-3 px-4 text-slate-500">{c.category}</td>
                      <td className="py-3 px-4"><StatusBadge status={c.status} /></td>
                      <td className="py-3 px-4 text-slate-600">{c.assignedAgentName || 'Unassigned'}</td>
                      <td className="py-3 px-4 text-slate-400">{new Date(c.createdAt).toLocaleDateString()}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    );
  }

  // --- STAFF (ADMIN & AGENT) DASHBOARD VIEW ---
  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-extrabold text-slate-900 tracking-tight">
            Support Operations Console
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Operational KPIs, workload distribution, and SLA monitoring
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => navigate('/agent-queue')}
            className="px-3.5 py-2 text-xs font-semibold bg-white border border-slate-300 text-slate-700 hover:bg-slate-50 rounded-lg shadow-xs flex items-center gap-1.5"
          >
            <Headphones className="w-3.5 h-3.5 text-indigo-600" />
            Agent Workspace
          </button>
          <button
            onClick={() => navigate('/cases/new')}
            className="px-3.5 py-2 text-xs font-semibold bg-brand-600 hover:bg-brand-700 text-white rounded-lg shadow-xs flex items-center gap-1.5"
          >
            <PlusCircle className="w-3.5 h-3.5" />
            New Case
          </button>
        </div>
      </div>

      {/* 4 Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-4 sm:p-5 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-bold uppercase tracking-wider">Active Open Cases</span>
            <div className="w-8 h-8 rounded-lg bg-blue-50 text-brand-600 flex items-center justify-center">
              <Inbox className="w-4 h-4" />
            </div>
          </div>
          <p className="text-2xl font-extrabold text-slate-900 mt-2">{analytics?.openCases ?? 0}</p>
          <span className="text-[11px] text-slate-400 mt-1 block">Awaiting initial investigation</span>
        </div>

        <div className="bg-white p-4 sm:p-5 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-bold uppercase tracking-wider">In Progress</span>
            <div className="w-8 h-8 rounded-lg bg-amber-50 text-amber-600 flex items-center justify-center">
              <Clock className="w-4 h-4" />
            </div>
          </div>
          <p className="text-2xl font-extrabold text-slate-900 mt-2">{analytics?.inProgressCases ?? 0}</p>
          <span className="text-[11px] text-slate-400 mt-1 block">Under active agent resolution</span>
        </div>

        <div className="bg-white p-4 sm:p-5 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-bold uppercase tracking-wider">SLA Breached</span>
            <div className="w-8 h-8 rounded-lg bg-rose-50 text-rose-600 flex items-center justify-center">
              <AlertTriangle className="w-4 h-4" />
            </div>
          </div>
          <p className="text-2xl font-extrabold text-rose-600 mt-2">{slaData?.breachedCases ?? 0}</p>
          <span className="text-[11px] text-rose-500 font-semibold mt-1 block">Requires immediate escalation</span>
        </div>

        <div className="bg-white p-4 sm:p-5 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-bold uppercase tracking-wider">SLA Compliance</span>
            <div className="w-8 h-8 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center">
              <TrendingUp className="w-4 h-4" />
            </div>
          </div>
          <p className="text-2xl font-extrabold text-emerald-600 mt-2">{analytics?.slaComplianceRate ?? 100}%</p>
          <span className="text-[11px] text-slate-400 mt-1 block">Target threshold: 95%</span>
        </div>
      </div>

      {/* Urgent Attention / Recent Cases Workspace */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2 Cols: Cases Stream */}
        <div className="lg:col-span-2 bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
          <div className="p-4 border-b border-slate-200 flex items-center justify-between bg-slate-50/50">
            <div>
              <h2 className="text-xs font-bold text-slate-800 uppercase tracking-wide">
                Urgent Queue & Recent Cases
              </h2>
              <p className="text-[11px] text-slate-500">Cases requiring triage and response</p>
            </div>
            <Link to="/cases" className="text-xs font-semibold text-brand-600 hover:text-brand-800 flex items-center gap-1">
              All Cases <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-slate-200 bg-slate-50/80 text-[11px] font-bold text-slate-500 uppercase tracking-wider">
                  <th className="py-2.5 px-3.5">Case #</th>
                  <th className="py-2.5 px-3.5">Title</th>
                  <th className="py-2.5 px-3.5">Priority</th>
                  <th className="py-2.5 px-3.5">Status</th>
                  <th className="py-2.5 px-3.5">Agent</th>
                  <th className="py-2.5 px-3.5">SLA</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-xs text-slate-700">
                {recentCases.map((c) => (
                  <tr
                    key={c.id}
                    onClick={() => navigate(`/cases/${c.id}`)}
                    className="hover:bg-slate-50 cursor-pointer transition-colors"
                  >
                    <td className="py-2.5 px-3.5 font-mono font-bold text-brand-600">{c.caseNumber}</td>
                    <td className="py-2.5 px-3.5 font-semibold text-slate-900 max-w-xs truncate">{c.title}</td>
                    <td className="py-2.5 px-3.5"><PriorityBadge priority={c.priority} /></td>
                    <td className="py-2.5 px-3.5"><StatusBadge status={c.status} /></td>
                    <td className="py-2.5 px-3.5 text-slate-600">{c.assignedAgentName?.replace(/Sarah Jenkins/gi, 'Sarah') || <span className="text-rose-500 font-semibold">Unassigned</span>}</td>
                    <td className="py-2.5 px-3.5">
                      {c.slaEscalated ? (
                        <span className="text-rose-600 font-bold flex items-center gap-1 text-[11px]">
                          <AlertTriangle className="w-3 h-3" /> Escalated
                        </span>
                      ) : (
                        <span className="text-emerald-600 font-medium text-[11px]">In SLA</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Right 1 Col: Agent Workload Distribution */}
        <div className="bg-white rounded-xl border border-slate-200 shadow-xs p-5 space-y-4">
          <div className="flex items-center justify-between border-b border-slate-100 pb-3">
            <div>
              <h2 className="text-xs font-bold text-slate-800 uppercase tracking-wide">
                Agent Workloads
              </h2>
              <p className="text-[11px] text-slate-500">Live active cases vs capacity</p>
            </div>
            <Link to="/agent-queue" className="text-xs text-brand-600 hover:text-brand-800 font-semibold">
              Manage
            </Link>
          </div>

          <div className="space-y-3.5">
            {analytics?.agentWorkloads?.map((agent, idx) => (
              <div key={idx} className="space-y-1">
                <div className="flex items-center justify-between text-xs font-semibold text-slate-700">
                  <span>{agent.agentName?.replace(/Sarah Jenkins/gi, 'Sarah')}</span>
                  <span className="text-slate-500 font-mono text-[11px]">
                    {agent.currentCases} / {agent.maxCapacity} cases
                  </span>
                </div>
                <div className="w-full h-2 bg-slate-100 rounded-full overflow-hidden">
                  <div
                    className={`h-full rounded-full transition-all ${
                      agent.utilizationRate >= 80
                        ? 'bg-rose-500'
                        : agent.utilizationRate >= 50
                        ? 'bg-amber-500'
                        : 'bg-brand-500'
                    }`}
                    style={{ width: `${Math.min(100, agent.utilizationRate)}%` }}
                  />
                </div>
              </div>
            ))}
          </div>

          <div className="pt-2 border-t border-slate-100">
            <div className="bg-slate-50 rounded-lg p-3 text-[11px] text-slate-600 space-y-1">
              <div className="flex justify-between">
                <span>Avg First Response:</span>
                <span className="font-bold text-slate-800">{analytics?.avgFirstResponseHours ?? 0} hrs</span>
              </div>
              <div className="flex justify-between">
                <span>Avg Resolution Time:</span>
                <span className="font-bold text-slate-800">{analytics?.avgResolutionHours ?? 0} hrs</span>
              </div>
              <div className="flex justify-between">
                <span>Reopening Rate:</span>
                <span className="font-bold text-slate-800">{analytics?.reopeningRate ?? 0}%</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
