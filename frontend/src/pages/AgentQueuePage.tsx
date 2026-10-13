import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { apiClient } from '../api/client';
import { SupportCase, AgentProfile } from '../types';
import { StatusBadge, PriorityBadge } from '../components/common/StatusBadge';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { 
  Headphones, 
  CheckCircle, 
  Clock, 
  AlertCircle, 
  ToggleLeft, 
  ToggleRight, 
  ArrowRight, 
  UserPlus 
} from 'lucide-react';

export const AgentQueuePage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [agents, setAgents] = useState<AgentProfile[]>([]);
  const [unassignedCases, setUnassignedCases] = useState<SupportCase[]>([]);
  const [myCases, setMyCases] = useState<SupportCase[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchWorkspace = async () => {
    setLoading(true);
    try {
      const [agentsRes, unassignedRes, myRes] = await Promise.all([
        apiClient.get('/agents'),
        apiClient.get('/cases?size=20'),
        user ? apiClient.get(`/cases?agentId=${user.id}&size=20`) : Promise.resolve({ data: { data: { content: [] } } }),
      ]);

      setAgents(agentsRes.data.data);
      // Filter for unassigned
      const allCases: SupportCase[] = unassignedRes.data.data.content;
      setUnassignedCases(allCases.filter((c) => !c.assignedAgentId && c.status !== 'RESOLVED' && c.status !== 'CLOSED'));
      setMyCases(myRes.data.data.content);
    } catch (err) {
      console.error('Failed to load agent workspace', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchWorkspace();
  }, [user]);

  const currentAgentProfile = agents.find((ag) => ag.userId === user?.id);

  const handleToggleAvailability = async () => {
    if (!currentAgentProfile) return;
    try {
      await apiClient.patch(`/agents/${user?.id}/availability`, {
        available: !currentAgentProfile.available,
      });
      fetchWorkspace();
    } catch (err) {
      console.error('Failed to toggle availability', err);
    }
  };

  const handleClaimCase = async (caseId: number) => {
    if (!user) return;
    try {
      await apiClient.post(`/cases/${caseId}/assign`, {
        agentId: user.id,
        note: 'Claimed by agent from unassigned queue',
      });
      fetchWorkspace();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to claim case');
    }
  };

  if (loading) {
    return <LoadingSpinner message="Loading agent queue..." />;
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-extrabold text-slate-900 tracking-tight">
            Agent Queue & Routing Workspace
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Triage unassigned cases, balance team workloads, and manage live availability
          </p>
        </div>

        {/* Current Agent Availability Toggle Card */}
        {currentAgentProfile && (
          <div className="bg-white px-4 py-2.5 rounded-xl border border-slate-200 shadow-xs flex items-center gap-4">
            <div>
              <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wide block">
                Routing Status
              </span>
              <span
                className={`text-xs font-bold ${
                  currentAgentProfile.available ? 'text-emerald-600' : 'text-slate-400'
                }`}
              >
                {currentAgentProfile.available ? 'Available for auto-dispatch' : 'Unavailable (Paused)'}
              </span>
            </div>
            <button
              onClick={handleToggleAvailability}
              className={`p-1 rounded-lg transition-colors ${
                currentAgentProfile.available ? 'text-emerald-600 hover:bg-emerald-50' : 'text-slate-400 hover:bg-slate-100'
              }`}
            >
              {currentAgentProfile.available ? (
                <ToggleRight className="w-8 h-8" />
              ) : (
                <ToggleLeft className="w-8 h-8" />
              )}
            </button>
          </div>
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column (2 cols): Unassigned Queue & My Cases */}
        <div className="lg:col-span-2 space-y-6">
          {/* Unassigned Triage Queue */}
          <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
            <div className="p-4 border-b border-slate-200 bg-amber-50/50 flex items-center justify-between">
              <div>
                <h2 className="text-xs font-bold text-amber-900 uppercase tracking-wide flex items-center gap-1.5">
                  <AlertCircle className="w-4 h-4 text-amber-600" />
                  Unassigned Triage Queue ({unassignedCases.length})
                </h2>
                <p className="text-[11px] text-amber-700">
                  Cases awaiting agent claiming or manual assignment
                </p>
              </div>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-slate-200 bg-slate-50 text-[11px] font-bold text-slate-500 uppercase tracking-wider">
                    <th className="py-2.5 px-3.5">Case #</th>
                    <th className="py-2.5 px-3.5">Subject</th>
                    <th className="py-2.5 px-3.5">Category</th>
                    <th className="py-2.5 px-3.5">Priority</th>
                    <th className="py-2.5 px-3.5 text-right">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 text-xs text-slate-700">
                  {unassignedCases.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="py-8 text-center text-slate-400">
                        Zero unassigned cases. Auto-assignment routing engine is operating at full clearance!
                      </td>
                    </tr>
                  ) : (
                    unassignedCases.map((c) => (
                      <tr key={c.id} className="hover:bg-slate-50 transition-colors">
                        <td className="py-2.5 px-3.5 font-mono font-bold text-brand-600">{c.caseNumber}</td>
                        <td className="py-2.5 px-3.5 font-semibold text-slate-900 max-w-xs truncate">{c.title}</td>
                        <td className="py-2.5 px-3.5 text-slate-500">{c.category}</td>
                        <td className="py-2.5 px-3.5"><PriorityBadge priority={c.priority} /></td>
                        <td className="py-2.5 px-3.5 text-right">
                          <button
                            onClick={() => handleClaimCase(c.id)}
                            className="px-2.5 py-1 text-xs font-semibold bg-brand-600 hover:bg-brand-700 text-white rounded-md shadow-xs transition-colors inline-flex items-center gap-1"
                          >
                            <UserPlus className="w-3.5 h-3.5" /> Claim Case
                          </button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>

          {/* My Assigned Active Cases */}
          <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
            <div className="p-4 border-b border-slate-200 bg-slate-50/50 flex items-center justify-between">
              <div>
                <h2 className="text-xs font-bold text-slate-800 uppercase tracking-wide">
                  My Active Caseload ({myCases.length})
                </h2>
                <p className="text-[11px] text-slate-500">Cases currently assigned to you</p>
              </div>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-slate-200 bg-slate-50 text-[11px] font-bold text-slate-500 uppercase tracking-wider">
                    <th className="py-2.5 px-3.5">Case #</th>
                    <th className="py-2.5 px-3.5">Subject</th>
                    <th className="py-2.5 px-3.5">Status</th>
                    <th className="py-2.5 px-3.5">Priority</th>
                    <th className="py-2.5 px-3.5">Deadline</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 text-xs text-slate-700">
                  {myCases.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="py-8 text-center text-slate-400">
                        No cases currently assigned to you.
                      </td>
                    </tr>
                  ) : (
                    myCases.map((c) => (
                      <tr
                        key={c.id}
                        onClick={() => navigate(`/cases/${c.id}`)}
                        className="hover:bg-slate-50 cursor-pointer transition-colors"
                      >
                        <td className="py-2.5 px-3.5 font-mono font-bold text-brand-600">{c.caseNumber}</td>
                        <td className="py-2.5 px-3.5 font-semibold text-slate-900 max-w-xs truncate">{c.title}</td>
                        <td className="py-2.5 px-3.5"><StatusBadge status={c.status} /></td>
                        <td className="py-2.5 px-3.5"><PriorityBadge priority={c.priority} /></td>
                        <td className="py-2.5 px-3.5 text-slate-400 font-mono text-[11px]">
                          {new Date(c.resolutionDeadline).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* Right Column (1 col): Team Workload Roster */}
        <div className="bg-white rounded-xl border border-slate-200 shadow-xs p-5 space-y-4">
          <div className="border-b border-slate-100 pb-3">
            <h2 className="text-xs font-bold text-slate-800 uppercase tracking-wide">
              Team Workload & Capacity
            </h2>
            <p className="text-[11px] text-slate-500">Live agent routing distribution</p>
          </div>

          <div className="space-y-4">
            {agents.map((ag) => (
              <div key={ag.id} className="p-3 bg-slate-50 rounded-xl border border-slate-200/80 space-y-2">
                <div className="flex items-center justify-between">
                  <div>
                    <strong className="text-xs text-slate-900 block">{ag.name}</strong>
                    <span className="text-[10px] text-slate-500">{ag.email}</span>
                  </div>
                  <span
                    className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                      ag.available
                        ? 'bg-emerald-100 text-emerald-800'
                        : 'bg-slate-200 text-slate-600'
                    }`}
                  >
                    {ag.available ? 'ONLINE' : 'PAUSED'}
                  </span>
                </div>

                <div className="text-[11px] text-slate-600">
                  <span className="font-semibold text-slate-500">Skills: </span>
                  <span className="bg-white px-1.5 py-0.5 rounded border border-slate-200 text-slate-700 font-mono text-[10px]">
                    {ag.skills}
                  </span>
                </div>

                <div className="space-y-1">
                  <div className="flex justify-between text-[11px] font-semibold text-slate-600">
                    <span>Active Load:</span>
                    <span>{ag.currentActiveCases} / {ag.maxActiveCases} cases</span>
                  </div>
                  <div className="w-full h-1.5 bg-slate-200 rounded-full overflow-hidden">
                    <div
                      className={`h-full rounded-full ${
                        ag.currentActiveCases >= ag.maxActiveCases
                          ? 'bg-rose-500'
                          : ag.currentActiveCases >= ag.maxActiveCases * 0.7
                          ? 'bg-amber-500'
                          : 'bg-brand-500'
                      }`}
                      style={{
                        width: `${Math.min(100, (ag.currentActiveCases / ag.maxActiveCases) * 100)}%`,
                      }}
                    />
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
