import React, { useState, useEffect } from 'react';
import { apiClient } from '../api/client';
import { AnalyticsData } from '../types';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  Tooltip, 
  ResponsiveContainer, 
  LineChart, 
  Line, 
  PieChart, 
  Pie, 
  Cell, 
  CartesianGrid, 
  Legend 
} from 'recharts';
import { 
  TrendingUp, 
  Clock, 
  CheckCircle2, 
  AlertTriangle, 
  RotateCcw, 
  Zap, 
  Users 
} from 'lucide-react';

export const AnalyticsPage: React.FC = () => {
  const [data, setData] = useState<AnalyticsData | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchAnalytics = async () => {
      try {
        const res = await apiClient.get('/analytics');
        setData(res.data.data);
      } catch (err) {
        console.error('Failed to load analytics', err);
      } finally {
        setLoading(false);
      }
    };

    fetchAnalytics();
  }, []);

  if (loading || !data) {
    return <LoadingSpinner message="Calculating real-time support analytics..." />;
  }

  // Format Recharts data
  const priorityData = Object.entries(data.casesByPriority || {}).map(([key, value]) => ({
    name: key,
    count: value,
  }));

  const categoryData = Object.entries(data.casesByCategory || {}).map(([key, value]) => ({
    name: key.replace('_', ' '),
    count: value,
  }));

  const COLORS = ['#2563eb', '#4f46e5', '#0891b2', '#f59e0b', '#10b981', '#ef4444'];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-extrabold text-slate-900 tracking-tight">
          Executive Analytics & Support Intelligence
        </h1>
        <p className="text-xs text-slate-500 mt-0.5">
          Real-time aggregation of support volume, resolution efficiency, and team capacity
        </p>
      </div>

      {/* 6 Top Metric Cards */}
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-3.5">
        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs">
          <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wide">Total Volume</span>
          <p className="text-2xl font-extrabold text-slate-900 mt-1">{data.totalCases}</p>
          <span className="text-[10px] text-slate-400 mt-0.5 block">Lifetime cases</span>
        </div>

        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs">
          <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wide">SLA Compliance</span>
          <p className="text-2xl font-extrabold text-emerald-600 mt-1">{data.slaComplianceRate}%</p>
          <span className="text-[10px] text-slate-400 mt-0.5 block">Target: 95%+</span>
        </div>

        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs">
          <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wide">Avg Response</span>
          <p className="text-2xl font-extrabold text-brand-600 mt-1">{data.avgFirstResponseHours}h</p>
          <span className="text-[10px] text-slate-400 mt-0.5 block">First reply MTTA</span>
        </div>

        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs">
          <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wide">Avg Resolution</span>
          <p className="text-2xl font-extrabold text-indigo-600 mt-1">{data.avgResolutionHours}h</p>
          <span className="text-[10px] text-slate-400 mt-0.5 block">Mean time to resolve</span>
        </div>

        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs">
          <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wide">Overdue Cases</span>
          <p className="text-2xl font-extrabold text-rose-600 mt-1">{data.overdueCases}</p>
          <span className="text-[10px] text-rose-500 font-semibold mt-0.5 block">Breached deadline</span>
        </div>

        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs">
          <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wide">Reopening Rate</span>
          <p className="text-2xl font-extrabold text-amber-600 mt-1">{data.reopeningRate}%</p>
          <span className="text-[10px] text-slate-400 mt-0.5 block">Reopened after resolve</span>
        </div>
      </div>

      {/* Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Chart 1: Priority Distribution Bar Chart */}
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs space-y-3">
          <div className="flex items-center justify-between border-b border-slate-100 pb-2">
            <h2 className="text-xs font-bold text-slate-800 uppercase tracking-wide">
              Cases by Priority
            </h2>
            <span className="text-[11px] text-slate-400">Severity distribution</span>
          </div>

          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={priorityData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                <XAxis dataKey="name" tick={{ fontSize: 11 }} />
                <YAxis allowDecimals={false} tick={{ fontSize: 11 }} />
                <Tooltip
                  contentStyle={{ backgroundColor: '#1e293b', color: '#fff', borderRadius: '8px', fontSize: '11px' }}
                />
                <Bar dataKey="count" fill="#3b82f6" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Chart 2: Category Breakdown Pie Chart */}
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs space-y-3">
          <div className="flex items-center justify-between border-b border-slate-100 pb-2">
            <h2 className="text-xs font-bold text-slate-800 uppercase tracking-wide">
              Category Distribution
            </h2>
            <span className="text-[11px] text-slate-400">Cases by topic</span>
          </div>

          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={categoryData}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={80}
                  paddingAngle={5}
                  dataKey="count"
                >
                  {categoryData.map((_, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip
                  contentStyle={{ backgroundColor: '#1e293b', color: '#fff', borderRadius: '8px', fontSize: '11px' }}
                />
                <Legend iconType="circle" wrapperStyle={{ fontSize: '11px', paddingTop: '10px' }} />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Chart 3: Volume Timeline */}
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs space-y-3">
          <div className="flex items-center justify-between border-b border-slate-100 pb-2">
            <h2 className="text-xs font-bold text-slate-800 uppercase tracking-wide">
              Case Volume Over Time
            </h2>
            <span className="text-[11px] text-slate-400">Daily creation trends</span>
          </div>

          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={data.volumeOverTime} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                <XAxis dataKey="label" tick={{ fontSize: 11 }} />
                <YAxis allowDecimals={false} tick={{ fontSize: 11 }} />
                <Tooltip
                  contentStyle={{ backgroundColor: '#1e293b', color: '#fff', borderRadius: '8px', fontSize: '11px' }}
                />
                <Line type="monotone" dataKey="count" stroke="#6366f1" strokeWidth={2.5} dot={{ r: 4 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Chart 4: Agent Team Workload Capacity */}
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs space-y-4">
          <div className="flex items-center justify-between border-b border-slate-100 pb-2">
            <h2 className="text-xs font-bold text-slate-800 uppercase tracking-wide">
              Agent Workload Balancing
            </h2>
            <span className="text-[11px] text-slate-400">Headroom vs capacity</span>
          </div>

          <div className="space-y-4 pt-2">
            {data.agentWorkloads?.map((ag, i) => (
              <div key={i} className="space-y-1">
                <div className="flex justify-between text-xs font-semibold text-slate-700">
                  <span>{ag.agentName}</span>
                  <span className="font-mono text-[11px] text-slate-500">
                    {ag.currentCases} / {ag.maxCapacity} ({ag.utilizationRate}%)
                  </span>
                </div>
                <div className="w-full h-2.5 bg-slate-100 rounded-full overflow-hidden">
                  <div
                    className={`h-full rounded-full ${
                      ag.utilizationRate >= 80
                        ? 'bg-rose-500'
                        : ag.utilizationRate >= 50
                        ? 'bg-amber-500'
                        : 'bg-brand-500'
                    }`}
                    style={{ width: `${Math.min(100, ag.utilizationRate)}%` }}
                  />
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
