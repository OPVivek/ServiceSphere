import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { apiClient } from '../api/client';
import { CaseDetail, CaseStatus, AgentProfile } from '../types';
import { StatusBadge, PriorityBadge } from '../components/common/StatusBadge';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { 
  ArrowLeft, 
  Clock, 
  Send, 
  User, 
  ShieldAlert, 
  CheckCircle2, 
  RotateCcw, 
  AlertTriangle, 
  MessageSquare, 
  History, 
  Lock, 
  UserCheck 
} from 'lucide-react';

export const CaseDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();
  const navigate = useNavigate();

  const [caseDetail, setCaseDetail] = useState<CaseDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'timeline' | 'comments' | 'internal'>('comments');

  // Comment submission state
  const [commentText, setCommentText] = useState('');
  const [isInternalNote, setIsInternalNote] = useState(false);
  const [submittingComment, setSubmittingComment] = useState(false);

  // Status transition prompt
  const [statusReason, setStatusReason] = useState('');
  const [showStatusModal, setShowStatusModal] = useState<CaseStatus | null>(null);

  // Reassignment state
  const [agents, setAgents] = useState<AgentProfile[]>([]);
  const [selectedAgentId, setSelectedAgentId] = useState<number | ''>('');
  const [showAssignModal, setShowAssignModal] = useState(false);

  const fetchCaseDetail = async () => {
    try {
      const res = await apiClient.get(`/cases/${id}`);
      setCaseDetail(res.data.data);
    } catch (err: any) {
      console.error('Failed to load case detail', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCaseDetail();
    if (user?.role === 'ADMIN' || user?.role === 'AGENT') {
      apiClient.get('/agents').then((res) => setAgents(res.data.data)).catch(() => {});
    }
  }, [id, user]);

  const handleAddComment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!commentText.trim()) return;
    setSubmittingComment(true);

    try {
      await apiClient.post(`/cases/${id}/comments`, {
        message: commentText.trim(),
        internal: user?.role !== 'CUSTOMER' && isInternalNote,
      });
      setCommentText('');
      setIsInternalNote(false);
      fetchCaseDetail();
    } catch (err) {
      console.error('Failed to post comment', err);
    } finally {
      setSubmittingComment(false);
    }
  };

  const handleStatusTransition = async (newStatus: CaseStatus) => {
    try {
      await apiClient.patch(`/cases/${id}/status`, {
        status: newStatus,
        reason: statusReason || undefined,
      });
      setShowStatusModal(null);
      setStatusReason('');
      fetchCaseDetail();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Status transition failed');
    }
  };

  const handleReassign = async () => {
    if (!selectedAgentId) return;
    try {
      await apiClient.post(`/cases/${id}/assign`, {
        agentId: selectedAgentId,
        note: 'Manual assignment via console',
      });
      setShowAssignModal(false);
      fetchCaseDetail();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Reassignment failed');
    }
  };

  if (loading || !caseDetail) {
    return <LoadingSpinner message="Loading case details..." />;
  }

  const { caseDetails, comments, history } = caseDetail;
  const isStaff = user?.role === 'ADMIN' || user?.role === 'AGENT';

  // Filter comments for tabs
  const publicComments = comments.filter((c) => !c.internal);
  const internalNotes = comments.filter((c) => c.internal);

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      {/* Top Header / Breadcrumbs */}
      <div>
        <button
          onClick={() => navigate('/cases')}
          className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-500 hover:text-slate-800 transition-colors mb-2"
        >
          <ArrowLeft className="w-3.5 h-3.5" /> Back to Cases
        </button>

        <div className="bg-white rounded-xl border border-slate-200 shadow-xs p-5 sm:p-6 space-y-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div className="flex items-center gap-3">
              <span className="font-mono text-sm font-bold text-brand-600 bg-brand-50 border border-brand-200 px-3 py-1 rounded-lg">
                {caseDetails.caseNumber}
              </span>
              <StatusBadge status={caseDetails.status} />
              <PriorityBadge priority={caseDetails.priority} />
            </div>

            {/* SLA Status Pill */}
            <div className="flex items-center gap-2 text-xs">
              {caseDetails.slaEscalated ? (
                <span className="bg-rose-50 border border-rose-200 text-rose-700 font-bold px-3 py-1 rounded-full flex items-center gap-1.5">
                  <AlertTriangle className="w-3.5 h-3.5 text-rose-600" />
                  SLA Breached & Escalated
                </span>
              ) : (
                <span className="bg-emerald-50 border border-emerald-200 text-emerald-700 font-medium px-3 py-1 rounded-full flex items-center gap-1.5">
                  <Clock className="w-3.5 h-3.5 text-emerald-600" />
                  Within SLA Targets
                </span>
              )}
            </div>
          </div>

          <div>
            <h1 className="text-xl font-bold text-slate-900 leading-snug">
              {caseDetails.title}
            </h1>
            <p className="text-xs text-slate-500 mt-1">
              Category: <strong className="text-slate-700">{caseDetails.category}</strong> • Created by{' '}
              <strong className="text-slate-700">{caseDetails.customerName}</strong> ({caseDetails.customerEmail})
            </p>
          </div>

          {/* Action Toolbar */}
          <div className="pt-3 border-t border-slate-100 flex flex-wrap items-center justify-between gap-3">
            {/* Status Transition Buttons */}
            <div className="flex flex-wrap items-center gap-2">
              {caseDetails.status === 'OPEN' && (
                <>
                  <button
                    onClick={() => handleStatusTransition('IN_PROGRESS')}
                    className="px-3 py-1.5 bg-amber-600 hover:bg-amber-700 text-white rounded-lg text-xs font-semibold shadow-xs"
                  >
                    Start Working (In Progress)
                  </button>
                  <button
                    onClick={() => handleStatusTransition('WAITING_FOR_CUSTOMER')}
                    className="px-3 py-1.5 bg-purple-600 hover:bg-purple-700 text-white rounded-lg text-xs font-semibold shadow-xs"
                  >
                    Wait on Customer
                  </button>
                  <button
                    onClick={() => handleStatusTransition('RESOLVED')}
                    className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-semibold shadow-xs flex items-center gap-1"
                  >
                    <CheckCircle2 className="w-3.5 h-3.5" /> Resolve Case
                  </button>
                </>
              )}

              {caseDetails.status === 'IN_PROGRESS' && (
                <>
                  <button
                    onClick={() => handleStatusTransition('WAITING_FOR_CUSTOMER')}
                    className="px-3 py-1.5 bg-purple-600 hover:bg-purple-700 text-white rounded-lg text-xs font-semibold shadow-xs"
                  >
                    Wait on Customer
                  </button>
                  <button
                    onClick={() => handleStatusTransition('RESOLVED')}
                    className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-semibold shadow-xs flex items-center gap-1"
                  >
                    <CheckCircle2 className="w-3.5 h-3.5" /> Resolve Case
                  </button>
                </>
              )}

              {caseDetails.status === 'WAITING_FOR_CUSTOMER' && (
                <>
                  <button
                    onClick={() => handleStatusTransition('IN_PROGRESS')}
                    className="px-3 py-1.5 bg-amber-600 hover:bg-amber-700 text-white rounded-lg text-xs font-semibold shadow-xs"
                  >
                    Resume (In Progress)
                  </button>
                  <button
                    onClick={() => handleStatusTransition('RESOLVED')}
                    className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-semibold shadow-xs flex items-center gap-1"
                  >
                    <CheckCircle2 className="w-3.5 h-3.5" /> Resolve Case
                  </button>
                </>
              )}

              {caseDetails.status === 'RESOLVED' && (
                <>
                  <button
                    onClick={() => setShowStatusModal('OPEN')}
                    className="px-3 py-1.5 bg-rose-600 hover:bg-rose-700 text-white rounded-lg text-xs font-semibold shadow-xs flex items-center gap-1"
                  >
                    <RotateCcw className="w-3.5 h-3.5" /> Reopen Case
                  </button>
                  <button
                    onClick={() => handleStatusTransition('CLOSED')}
                    className="px-3 py-1.5 bg-slate-700 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold shadow-xs"
                  >
                    Close Permanently
                  </button>
                </>
              )}

              {caseDetails.status === 'CLOSED' && (
                <span className="text-xs text-slate-400 italic font-medium">
                  This case is closed and archived.
                </span>
              )}
            </div>

            {/* Staff Assignment Control */}
            {isStaff && (
              <div className="flex items-center gap-2">
                <span className="text-xs text-slate-500 font-medium">
                  Assigned: <strong className="text-slate-800">{caseDetails.assignedAgentName || 'Unassigned'}</strong>
                </span>
                <button
                  onClick={() => setShowAssignModal(true)}
                  className="px-2.5 py-1 text-xs font-semibold border border-slate-300 hover:bg-slate-50 text-slate-700 rounded-md"
                >
                  Reassign
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Main Grid: Details & Discussion */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column (2 cols): Details, Tabs & Comments */}
        <div className="lg:col-span-2 space-y-6">
          {/* Issue Description Card */}
          <div className="bg-white rounded-xl border border-slate-200 shadow-xs p-5">
            <h2 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">
              Case Description & Details
            </h2>
            <div className="text-xs text-slate-800 leading-relaxed whitespace-pre-line bg-slate-50 p-4 rounded-lg border border-slate-100 font-sans">
              {caseDetails.description}
            </div>
          </div>

          {/* Activity / Discussion Tabs */}
          <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
            <div className="border-b border-slate-200 bg-slate-50 flex items-center px-4">
              <button
                onClick={() => setActiveTab('comments')}
                className={`py-3 px-3 text-xs font-bold flex items-center gap-1.5 border-b-2 transition-colors ${
                  activeTab === 'comments'
                    ? 'border-brand-600 text-brand-600'
                    : 'border-transparent text-slate-500 hover:text-slate-800'
                }`}
              >
                <MessageSquare className="w-3.5 h-3.5" />
                Customer Discussion ({publicComments.length})
              </button>

              {isStaff && (
                <button
                  onClick={() => setActiveTab('internal')}
                  className={`py-3 px-3 text-xs font-bold flex items-center gap-1.5 border-b-2 transition-colors ${
                    activeTab === 'internal'
                      ? 'border-amber-600 text-amber-700 bg-amber-50/50'
                      : 'border-transparent text-slate-500 hover:text-slate-800'
                  }`}
                >
                  <Lock className="w-3.5 h-3.5 text-amber-600" />
                  Internal Notes ({internalNotes.length})
                </button>
              )}

              <button
                onClick={() => setActiveTab('timeline')}
                className={`py-3 px-3 text-xs font-bold flex items-center gap-1.5 border-b-2 transition-colors ${
                  activeTab === 'timeline'
                    ? 'border-brand-600 text-brand-600'
                    : 'border-transparent text-slate-500 hover:text-slate-800'
                }`}
              >
                <History className="w-3.5 h-3.5" />
                Timeline Audit ({history.length})
              </button>
            </div>

            {/* Tab Contents */}
            <div className="p-5 space-y-4">
              {activeTab === 'comments' && (
                <div className="space-y-4">
                  {publicComments.length === 0 ? (
                    <div className="text-center py-8 text-xs text-slate-400">
                      No customer replies yet. Add a response below.
                    </div>
                  ) : (
                    publicComments.map((comment) => (
                      <div
                        key={comment.id}
                        className={`p-4 rounded-xl border text-xs leading-relaxed ${
                          comment.authorRole === 'CUSTOMER'
                            ? 'bg-slate-50 border-slate-200'
                            : 'bg-blue-50/60 border-blue-100'
                        }`}
                      >
                        <div className="flex items-center justify-between mb-2">
                          <div className="flex items-center gap-2">
                            <span className="font-bold text-slate-900">{comment.authorName}</span>
                            <span
                              className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                                comment.authorRole === 'CUSTOMER'
                                  ? 'bg-emerald-100 text-emerald-800'
                                  : 'bg-indigo-100 text-indigo-800'
                              }`}
                            >
                              {comment.authorRole}
                            </span>
                          </div>
                          <span className="text-[10px] text-slate-400">
                            {new Date(comment.createdAt).toLocaleString()}
                          </span>
                        </div>
                        <p className="text-slate-800 whitespace-pre-line">{comment.message}</p>
                      </div>
                    ))
                  )}
                </div>
              )}

              {activeTab === 'internal' && isStaff && (
                <div className="space-y-4">
                  <div className="p-2.5 bg-amber-50 border border-amber-200 rounded-lg text-amber-800 text-[11px] flex items-center gap-2">
                    <Lock className="w-4 h-4 shrink-0 text-amber-600" />
                    <span>Internal notes are private and NEVER visible to customers.</span>
                  </div>

                  {internalNotes.length === 0 ? (
                    <div className="text-center py-8 text-xs text-slate-400">
                      No internal notes recorded.
                    </div>
                  ) : (
                    internalNotes.map((note) => (
                      <div
                        key={note.id}
                        className="p-4 rounded-xl border border-amber-200/80 bg-amber-50/30 text-xs leading-relaxed"
                      >
                        <div className="flex items-center justify-between mb-2">
                          <div className="flex items-center gap-2">
                            <span className="font-bold text-slate-900">{note.authorName}</span>
                            <span className="text-[10px] font-bold bg-amber-100 text-amber-800 px-2 py-0.5 rounded-full border border-amber-200">
                              STAFF ONLY
                            </span>
                          </div>
                          <span className="text-[10px] text-slate-400">
                            {new Date(note.createdAt).toLocaleString()}
                          </span>
                        </div>
                        <p className="text-slate-800 whitespace-pre-line">{note.message}</p>
                      </div>
                    ))
                  )}
                </div>
              )}

              {activeTab === 'timeline' && (
                <div className="space-y-3 relative before:absolute before:inset-0 before:left-3.5 before:w-0.5 before:bg-slate-200">
                  {history.map((event) => (
                    <div key={event.id} className="relative flex items-start gap-3 pl-7 text-xs">
                      <div className="absolute left-2.5 top-1.5 w-2.5 h-2.5 rounded-full bg-brand-500 ring-4 ring-white" />
                      <div className="flex-1 bg-slate-50 p-3 rounded-lg border border-slate-200">
                        <div className="flex items-center justify-between">
                          <span className="font-bold text-slate-800">{event.action}</span>
                          <span className="text-[10px] text-slate-400">
                            {new Date(event.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                          </span>
                        </div>
                        <p className="text-slate-600 mt-0.5 text-[11px]">{event.note}</p>
                        <span className="text-[10px] text-slate-400 block mt-1">
                          Actor: <strong>{event.actorName}</strong> ({event.actorRole})
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Comment Form */}
            {caseDetails.status !== 'CLOSED' && (
              <form onSubmit={handleAddComment} className="p-4 border-t border-slate-200 bg-slate-50 space-y-3">
                <textarea
                  rows={3}
                  required
                  value={commentText}
                  onChange={(e) => setCommentText(e.target.value)}
                  placeholder={
                    isInternalNote
                      ? 'Write a private note visible only to support staff...'
                      : 'Write a response visible to customer...'
                  }
                  className="w-full px-3.5 py-2.5 text-xs bg-white border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 text-slate-900"
                />

                <div className="flex items-center justify-between">
                  {isStaff ? (
                    <label className="flex items-center gap-2 cursor-pointer text-xs font-semibold text-slate-700">
                      <input
                        type="checkbox"
                        checked={isInternalNote}
                        onChange={(e) => setIsInternalNote(e.target.checked)}
                        className="rounded border-slate-300 text-brand-600 focus:ring-brand-500"
                      />
                      <span>Internal staff note (Private)</span>
                    </label>
                  ) : (
                    <span className="text-[11px] text-slate-400">Customer visible reply</span>
                  )}

                  <button
                    type="submit"
                    disabled={submittingComment}
                    className="px-4 py-2 bg-brand-600 hover:bg-brand-700 text-white rounded-lg text-xs font-semibold shadow-xs flex items-center gap-1.5 disabled:opacity-50"
                  >
                    <Send className="w-3.5 h-3.5" />
                    {submittingComment ? 'Sending...' : 'Post Reply'}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>

        {/* Right Column (1 col): Metadata & SLA Summary */}
        <div className="space-y-5">
          {/* SLA Tracking Info */}
          <div className="bg-white rounded-xl border border-slate-200 shadow-xs p-5 space-y-3">
            <h3 className="text-xs font-bold text-slate-800 uppercase tracking-wide flex items-center gap-1.5">
              <Clock className="w-4 h-4 text-brand-600" />
              SLA Metrics & Timestamps
            </h3>

            <div className="space-y-2.5 text-xs">
              <div className="flex justify-between py-1 border-b border-slate-100">
                <span className="text-slate-500">First Response Target:</span>
                <span className="font-mono text-slate-800">
                  {new Date(caseDetails.firstResponseDeadline).toLocaleString()}
                </span>
              </div>
              <div className="flex justify-between py-1 border-b border-slate-100">
                <span className="text-slate-500">Resolution Deadline:</span>
                <span className="font-mono text-slate-800">
                  {new Date(caseDetails.resolutionDeadline).toLocaleString()}
                </span>
              </div>
              <div className="flex justify-between py-1 border-b border-slate-100">
                <span className="text-slate-500">First Responded At:</span>
                <span className="font-mono text-slate-800">
                  {caseDetails.firstRespondedAt
                    ? new Date(caseDetails.firstRespondedAt).toLocaleString()
                    : 'Awaiting response'}
                </span>
              </div>
              <div className="flex justify-between py-1">
                <span className="text-slate-500">Reopen Count:</span>
                <span className="font-bold text-slate-800">{caseDetails.reopenCount}</span>
              </div>
            </div>
          </div>

          {/* Customer 360 Quick Link */}
          <div className="bg-white rounded-xl border border-slate-200 shadow-xs p-5 space-y-3">
            <h3 className="text-xs font-bold text-slate-800 uppercase tracking-wide flex items-center gap-1.5">
              <UserCheck className="w-4 h-4 text-purple-600" />
              Customer Information
            </h3>

            <div className="text-xs space-y-1">
              <p className="font-bold text-slate-900">{caseDetails.customerName}</p>
              <p className="text-slate-500">{caseDetails.customerEmail}</p>
            </div>

            <button
              onClick={() => navigate(`/customers/${caseDetails.customerId}/360`)}
              className="w-full mt-2 py-2 px-3 bg-purple-50 hover:bg-purple-100 text-purple-700 font-semibold rounded-lg text-xs transition-colors"
            >
              View Customer 360 Overview
            </button>
          </div>
        </div>
      </div>

      {/* Reopen Reason Modal */}
      {showStatusModal === 'OPEN' && (
        <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl max-w-md w-full p-6 shadow-xl space-y-4">
            <h3 className="text-sm font-bold text-slate-900">Reopen Support Case</h3>
            <p className="text-xs text-slate-500">
              Please explain why this case is being reopened so our agents can resume investigation immediately.
            </p>
            <textarea
              rows={3}
              required
              value={statusReason}
              onChange={(e) => setStatusReason(e.target.value)}
              placeholder="e.g. The issue recurred when testing with secondary credentials..."
              className="w-full p-3 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500"
            />
            <div className="flex justify-end gap-2">
              <button
                onClick={() => setShowStatusModal(null)}
                className="px-3.5 py-1.5 border border-slate-300 rounded-lg text-xs font-semibold text-slate-600"
              >
                Cancel
              </button>
              <button
                onClick={() => handleStatusTransition('OPEN')}
                className="px-4 py-1.5 bg-rose-600 text-white rounded-lg text-xs font-semibold"
              >
                Confirm Reopen
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Reassignment Modal */}
      {showAssignModal && (
        <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl max-w-md w-full p-6 shadow-xl space-y-4">
            <h3 className="text-sm font-bold text-slate-900">Reassign Support Agent</h3>
            <p className="text-xs text-slate-500">
              Select an available agent with appropriate category expertise:
            </p>

            <select
              value={selectedAgentId}
              onChange={(e) => setSelectedAgentId(Number(e.target.value))}
              className="w-full p-2.5 text-xs border border-slate-300 rounded-lg bg-slate-50 font-medium"
            >
              <option value="">Select an Agent...</option>
              {agents.map((ag) => (
                <option key={ag.userId} value={ag.userId}>
                  {ag.name} ({ag.skills}) - {ag.currentActiveCases}/{ag.maxActiveCases} cases
                </option>
              ))}
            </select>

            <div className="flex justify-end gap-2 pt-2">
              <button
                onClick={() => setShowAssignModal(false)}
                className="px-3.5 py-1.5 border border-slate-300 rounded-lg text-xs font-semibold text-slate-600"
              >
                Cancel
              </button>
              <button
                onClick={handleReassign}
                disabled={!selectedAgentId}
                className="px-4 py-1.5 bg-brand-600 text-white rounded-lg text-xs font-semibold disabled:opacity-50"
              >
                Confirm Assignment
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
