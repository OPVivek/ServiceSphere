import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '../api/client';
import { CasePriority, CaseCategory, KnowledgeArticle } from '../types';
import { 
  ArrowLeft, 
  Send, 
  Sparkles, 
  BookOpen, 
  Clock, 
  ShieldAlert, 
  AlertCircle 
} from 'lucide-react';

export const CreateCasePage: React.FC = () => {
  const navigate = useNavigate();

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState<CaseCategory>('TECHNICAL');
  const [priority, setPriority] = useState<CasePriority>('MEDIUM');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Smart Knowledge Article Suggestions based on title keywords
  const [suggestions, setSuggestions] = useState<KnowledgeArticle[]>([]);

  useEffect(() => {
    if (title.trim().length > 3) {
      const delay = setTimeout(async () => {
        try {
          const res = await apiClient.get(`/knowledge/suggestions?keyword=${encodeURIComponent(title.trim())}`);
          setSuggestions(res.data.data);
        } catch {
          // Ignore
        }
      }, 400);
      return () => clearTimeout(delay);
    } else {
      setSuggestions([]);
    }
  }, [title]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const res = await apiClient.post('/cases', {
        title,
        description,
        category,
        priority,
      });
      const newCase = res.data.data;
      navigate(`/cases/${newCase.id}`);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to submit case. Please review required fields.');
    } finally {
      setLoading(false);
    }
  };

  const slaTargets: Record<CasePriority, { response: string; resolution: string; desc: string }> = {
    CRITICAL: { response: '1 hour', resolution: '4 hours', desc: 'Production halting emergency with immediate escalation.' },
    HIGH: { response: '4 hours', resolution: '12 hours', desc: 'High severity impact on core customer workflows.' },
    MEDIUM: { response: '8 hours', resolution: '24 hours', desc: 'Standard business operational inquiry or defect.' },
    LOW: { response: '24 hours', resolution: '72 hours', desc: 'Minor cosmetic question or feature feedback.' },
  };

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      {/* Top Breadcrumb */}
      <div>
        <button
          onClick={() => navigate('/cases')}
          className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-500 hover:text-slate-800 transition-colors mb-2"
        >
          <ArrowLeft className="w-3.5 h-3.5" /> Back to Cases
        </button>
        <h1 className="text-xl font-extrabold text-slate-900 tracking-tight">
          Create New Support Case
        </h1>
        <p className="text-xs text-slate-500 mt-0.5">
          Submit an issue to our intelligent routing engine for automated agent dispatch
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main Form (2 cols) */}
        <div className="lg:col-span-2 bg-white rounded-xl border border-slate-200 shadow-xs p-6">
          {error && (
            <div className="mb-5 p-3.5 bg-rose-50 border border-rose-200 rounded-lg flex items-center gap-2.5 text-xs text-rose-700">
              <AlertCircle className="w-4 h-4 shrink-0 text-rose-600" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                Case Title / Subject *
              </label>
              <input
                type="text"
                required
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="e.g. Okta SAML 2.0 Single Sign-On Returning 403 Forbidden"
                className="w-full px-3.5 py-2.5 text-xs bg-slate-50 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 focus:bg-white text-slate-900 font-medium"
              />
              <span className="text-[11px] text-slate-400 mt-1 block">
                Be specific. As you type, relevant knowledge articles will appear automatically.
              </span>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                  Category *
                </label>
                <select
                  value={category}
                  onChange={(e) => setCategory(e.target.value as CaseCategory)}
                  className="w-full px-3.5 py-2.5 text-xs bg-slate-50 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 font-medium text-slate-800"
                >
                  <option value="TECHNICAL">Technical & API</option>
                  <option value="BILLING">Billing & Payments</option>
                  <option value="ACCOUNT">Account & Access</option>
                  <option value="GENERAL">General Inquiries</option>
                  <option value="FEATURE_REQUEST">Feature Request</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                  Priority Target *
                </label>
                <select
                  value={priority}
                  onChange={(e) => setPriority(e.target.value as CasePriority)}
                  className="w-full px-3.5 py-2.5 text-xs bg-slate-50 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 font-medium text-slate-800"
                >
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>
                  <option value="CRITICAL">Critical (Production Emergency)</option>
                </select>
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                Detailed Description & Reproduction Steps *
              </label>
              <textarea
                required
                rows={6}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Describe what occurred, impacted systems, steps to reproduce, or relevant error codes..."
                className="w-full px-3.5 py-2.5 text-xs bg-slate-50 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 focus:bg-white text-slate-900 leading-relaxed font-sans"
              />
            </div>

            <div className="pt-2 flex justify-end gap-3 border-t border-slate-100">
              <button
                type="button"
                onClick={() => navigate('/cases')}
                className="px-4 py-2 border border-slate-300 text-slate-700 text-xs font-semibold rounded-lg hover:bg-slate-50"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={loading}
                className="px-5 py-2 bg-brand-600 hover:bg-brand-700 text-white text-xs font-semibold rounded-lg shadow-sm flex items-center gap-2 disabled:opacity-50"
              >
                {loading ? 'Submitting...' : 'Submit Case'}
                <Send className="w-3.5 h-3.5" />
              </button>
            </div>
          </form>
        </div>

        {/* Right Sidebar: Live SLA Preview + Smart Article Suggestions (1 col) */}
        <div className="space-y-5">
          {/* SLA Target Preview */}
          <div className="bg-white rounded-xl border border-slate-200 shadow-xs p-5">
            <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-slate-700 mb-3">
              <Clock className="w-4 h-4 text-brand-600" />
              <span>SLA Target Guarantee</span>
            </div>

            <div className="bg-slate-50 border border-slate-200 rounded-lg p-3.5 space-y-2">
              <div className="flex justify-between items-center text-xs">
                <span className="text-slate-500">First Response Target:</span>
                <span className="font-bold text-slate-800">{slaTargets[priority].response}</span>
              </div>
              <div className="flex justify-between items-center text-xs">
                <span className="text-slate-500">Resolution Deadline:</span>
                <span className="font-bold text-slate-800">{slaTargets[priority].resolution}</span>
              </div>
              <div className="text-[11px] text-slate-400 pt-1 border-t border-slate-200">
                {slaTargets[priority].desc}
              </div>
            </div>
          </div>

          {/* Smart Suggestions */}
          {suggestions.length > 0 && (
            <div className="bg-amber-50/50 rounded-xl border border-amber-200 shadow-xs p-5 space-y-3">
              <div className="flex items-center gap-2 text-xs font-bold text-amber-900 uppercase tracking-wide">
                <Sparkles className="w-4 h-4 text-amber-600" />
                <span>Related Knowledge Base Articles</span>
              </div>
              <p className="text-[11px] text-amber-800">
                These guides might answer your question immediately:
              </p>
              <div className="space-y-2">
                {suggestions.map((art) => (
                  <div
                    key={art.id}
                    onClick={() => window.open(`/knowledge`, '_blank')}
                    className="bg-white p-3 rounded-lg border border-amber-200/80 hover:border-amber-400 cursor-pointer shadow-xs transition-colors"
                  >
                    <p className="text-xs font-bold text-slate-800 hover:text-brand-600 flex items-center gap-1">
                      <BookOpen className="w-3.5 h-3.5 text-amber-600 shrink-0" />
                      {art.title}
                    </p>
                    <p className="text-[11px] text-slate-500 line-clamp-2 mt-1">
                      {art.summary}
                    </p>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
