import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { apiClient } from '../api/client';
import { Customer360 } from '../types';
import { StatusBadge, PriorityBadge } from '../components/common/StatusBadge';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { 
  Building, 
  Mail, 
  Phone, 
  Briefcase, 
  CheckCircle, 
  Clock, 
  TrendingUp, 
  ArrowLeft, 
  Globe 
} from 'lucide-react';

export const Customer360Page: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [customerData, setCustomerData] = useState<Customer360 | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchCustomer360 = async () => {
      try {
        const res = await apiClient.get(`/customers/${id}/360`);
        setCustomerData(res.data.data);
      } catch (err) {
        console.error('Failed to fetch Customer 360 data', err);
      } finally {
        setLoading(false);
      }
    };

    fetchCustomer360();
  }, [id]);

  if (loading || !customerData) {
    return <LoadingSpinner message="Aggregating Customer 360 profile..." />;
  }

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      {/* Top Header */}
      <div>
        <button
          onClick={() => navigate(-1)}
          className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-500 hover:text-slate-800 transition-colors mb-2"
        >
          <ArrowLeft className="w-3.5 h-3.5" /> Back
        </button>
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-xl border border-slate-200 shadow-xs">
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-xl font-extrabold text-slate-900 tracking-tight">
                {customerData.name}
              </h1>
              {customerData.account?.tier && (
                <span className="bg-purple-50 text-purple-700 border border-purple-200 text-[10px] font-bold px-2 py-0.5 rounded-full">
                  {customerData.account.tier} TIER
                </span>
              )}
            </div>
            <p className="text-xs text-slate-500 mt-1 flex items-center gap-3">
              <span className="flex items-center gap-1"><Mail className="w-3.5 h-3.5" /> {customerData.email}</span>
              {customerData.phone && (
                <span className="flex items-center gap-1"><Phone className="w-3.5 h-3.5" /> {customerData.phone}</span>
              )}
              {customerData.jobTitle && (
                <span className="flex items-center gap-1"><Briefcase className="w-3.5 h-3.5" /> {customerData.jobTitle}</span>
              )}
            </p>
          </div>

          {customerData.account && (
            <div className="bg-slate-50 p-3.5 rounded-lg border border-slate-200 text-xs text-right">
              <span className="text-[10px] uppercase font-bold text-slate-400 block">Company Account</span>
              <strong className="text-slate-800 text-sm">{customerData.account.name}</strong>
              <span className="text-slate-500 text-[11px] block">{customerData.account.industry}</span>
            </div>
          )}
        </div>
      </div>

      {/* 4 Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
          <span className="text-xs font-bold text-slate-500 uppercase tracking-wide">Total Inquiries</span>
          <p className="text-2xl font-extrabold text-slate-900 mt-2">{customerData.totalCases}</p>
          <span className="text-[11px] text-slate-400 mt-1 block">Lifetime tickets submitted</span>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
          <span className="text-xs font-bold text-slate-500 uppercase tracking-wide">Active Inquiries</span>
          <p className="text-2xl font-extrabold text-brand-600 mt-2">{customerData.openCases}</p>
          <span className="text-[11px] text-slate-400 mt-1 block">Currently undergoing resolution</span>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
          <span className="text-xs font-bold text-slate-500 uppercase tracking-wide">Resolved Cases</span>
          <p className="text-2xl font-extrabold text-emerald-600 mt-2">{customerData.resolvedCases}</p>
          <span className="text-[11px] text-slate-400 mt-1 block">Successfully closed</span>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
          <span className="text-xs font-bold text-slate-500 uppercase tracking-wide">SLA Compliance</span>
          <p className="text-2xl font-extrabold text-emerald-600 mt-2">{customerData.slaCompliancePercentage}%</p>
          <span className="text-[11px] text-slate-400 mt-1 block">Delivered within contract SLA</span>
        </div>
      </div>

      {/* Support Case History */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="p-4 border-b border-slate-200 bg-slate-50/50 flex items-center justify-between">
          <h2 className="text-xs font-bold text-slate-800 uppercase tracking-wide">
            Support Case History
          </h2>
          <span className="text-xs text-slate-500">{customerData.recentCases.length} cases recorded</span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-slate-200 bg-slate-50/80 text-[11px] font-bold text-slate-500 uppercase tracking-wider">
                <th className="py-2.5 px-3.5">Case #</th>
                <th className="py-2.5 px-3.5">Subject</th>
                <th className="py-2.5 px-3.5">Category</th>
                <th className="py-2.5 px-3.5">Priority</th>
                <th className="py-2.5 px-3.5">Status</th>
                <th className="py-2.5 px-3.5">Agent</th>
                <th className="py-2.5 px-3.5">Submitted</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-xs text-slate-700">
              {customerData.recentCases.map((c) => (
                <tr
                  key={c.id}
                  onClick={() => navigate(`/cases/${c.id}`)}
                  className="hover:bg-slate-50 cursor-pointer transition-colors"
                >
                  <td className="py-2.5 px-3.5 font-mono font-bold text-brand-600">{c.caseNumber}</td>
                  <td className="py-2.5 px-3.5 font-semibold text-slate-900 max-w-sm truncate">{c.title}</td>
                  <td className="py-2.5 px-3.5 text-slate-500">{c.category}</td>
                  <td className="py-2.5 px-3.5"><PriorityBadge priority={c.priority} /></td>
                  <td className="py-2.5 px-3.5"><StatusBadge status={c.status} /></td>
                  <td className="py-2.5 px-3.5 text-slate-600">{c.assignedAgentName?.replace(/Sarah Jenkins/gi, 'Sarah') || 'Unassigned'}</td>
                  <td className="py-2.5 px-3.5 text-slate-400">{new Date(c.createdAt).toLocaleDateString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
