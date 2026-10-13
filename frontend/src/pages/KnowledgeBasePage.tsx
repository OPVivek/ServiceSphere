import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { apiClient } from '../api/client';
import { KnowledgeArticle } from '../types';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { 
  BookOpen, 
  Search, 
  ThumbsUp, 
  ThumbsDown, 
  PlusCircle, 
  Eye, 
  X, 
  Tag, 
  User 
} from 'lucide-react';

export const KnowledgeBasePage: React.FC = () => {
  const { user } = useAuth();
  const [articles, setArticles] = useState<KnowledgeArticle[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('');

  // Selected article reader modal
  const [activeArticle, setActiveArticle] = useState<KnowledgeArticle | null>(null);

  // New article modal (Staff only)
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newTitle, setNewTitle] = useState('');
  const [newSummary, setNewSummary] = useState('');
  const [newBody, setNewBody] = useState('');
  const [newCategory, setNewCategory] = useState('TECHNICAL');
  const [newTags, setNewTags] = useState('');
  const [newPublished, setNewPublished] = useState(true);

  const fetchArticles = async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      if (search) params.append('query', search);
      if (selectedCategory) params.append('category', selectedCategory);
      params.append('size', '20');

      const res = await apiClient.get(`/knowledge?${params.toString()}`);
      setArticles(res.data.data.content);
    } catch (err) {
      console.error('Failed to load articles', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchArticles();
  }, [search, selectedCategory]);

  const handleOpenArticle = async (articleId: number) => {
    try {
      const res = await apiClient.get(`/knowledge/${articleId}`);
      setActiveArticle(res.data.data);
    } catch (err) {
      console.error('Failed to load article detail', err);
    }
  };

  const handleFeedback = async (helpful: boolean) => {
    if (!activeArticle) return;
    try {
      await apiClient.post(`/knowledge/${activeArticle.id}/feedback?helpful=${helpful}`);
      setActiveArticle((prev) =>
        prev
          ? {
              ...prev,
              helpfulCount: helpful ? prev.helpfulCount + 1 : prev.helpfulCount,
              notHelpfulCount: !helpful ? prev.notHelpfulCount + 1 : prev.notHelpfulCount,
            }
          : null
      );
      fetchArticles();
    } catch (err) {
      console.error('Feedback failed', err);
    }
  };

  const handleCreateArticle = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await apiClient.post('/knowledge', {
        title: newTitle,
        summary: newSummary,
        body: newBody,
        category: newCategory,
        tags: newTags,
        published: newPublished,
      });
      setShowCreateModal(false);
      setNewTitle('');
      setNewSummary('');
      setNewBody('');
      setNewTags('');
      fetchArticles();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to create article');
    }
  };

  const categories = ['TECHNICAL', 'BILLING', 'ACCOUNT', 'GENERAL'];
  const isStaff = user?.role === 'ADMIN' || user?.role === 'AGENT';

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-extrabold text-slate-900 tracking-tight">
            Knowledge Base & Documentation
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Searchable troubleshooting articles, integration guides, and FAQs
          </p>
        </div>

        {isStaff && (
          <button
            onClick={() => setShowCreateModal(true)}
            className="bg-brand-600 hover:bg-brand-700 text-white font-semibold px-4 py-2 rounded-lg text-xs shadow-xs transition-colors flex items-center gap-1.5 self-start sm:self-auto"
          >
            <PlusCircle className="w-4 h-4" /> Publish Article
          </button>
        )}
      </div>

      {/* Search & Category Filter */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs space-y-4">
        <div className="relative">
          <Search className="w-4 h-4 absolute left-3 top-3 text-slate-400" />
          <input
            type="text"
            placeholder="Search guides by keywords (e.g. Okta SAML, rate limits, duplicate charge)..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-9 pr-3.5 py-2.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 focus:bg-white text-slate-800"
          />
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <button
            onClick={() => setSelectedCategory('')}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors ${
              selectedCategory === ''
                ? 'bg-slate-900 text-white'
                : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
            }`}
          >
            All Categories
          </button>
          {categories.map((cat) => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat)}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors ${
                selectedCategory === cat
                  ? 'bg-brand-600 text-white'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              {cat}
            </button>
          ))}
        </div>
      </div>

      {/* Article Cards Grid */}
      {loading ? (
        <LoadingSpinner message="Searching knowledge articles..." />
      ) : articles.length === 0 ? (
        <div className="bg-white p-12 text-center rounded-xl border border-slate-200 text-slate-400 text-xs">
          No knowledge articles found matching your query.
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {articles.map((art) => (
            <div
              key={art.id}
              onClick={() => handleOpenArticle(art.id)}
              className="bg-white p-5 rounded-xl border border-slate-200 hover:border-brand-300 hover:shadow-md cursor-pointer transition-all flex flex-col justify-between space-y-3 group"
            >
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-[10px] font-bold uppercase tracking-wider text-brand-700 bg-brand-50 border border-brand-200 px-2 py-0.5 rounded">
                    {art.category}
                  </span>
                  {!art.published && (
                    <span className="text-[10px] font-bold text-amber-700 bg-amber-50 px-2 py-0.5 rounded border border-amber-200">
                      DRAFT
                    </span>
                  )}
                </div>

                <h3 className="text-sm font-bold text-slate-900 group-hover:text-brand-600 transition-colors line-clamp-2">
                  {art.title}
                </h3>

                <p className="text-xs text-slate-500 line-clamp-3 leading-relaxed">
                  {art.summary}
                </p>
              </div>

              <div className="pt-3 border-t border-slate-100 flex items-center justify-between text-[11px] text-slate-400">
                <span className="flex items-center gap-1">
                  <Eye className="w-3.5 h-3.5" /> {art.viewCount} views
                </span>
                <span className="flex items-center gap-1 text-emerald-600 font-semibold">
                  <ThumbsUp className="w-3 h-3" /> {art.helpfulCount} helpful
                </span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Article Detail Drawer / Modal */}
      {activeArticle && (
        <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl max-w-2xl w-full max-h-[85vh] flex flex-col shadow-2xl overflow-hidden">
            <div className="p-5 border-b border-slate-200 flex items-start justify-between bg-slate-50">
              <div className="space-y-1 pr-6">
                <span className="text-[10px] font-bold uppercase tracking-wider text-brand-700 bg-brand-50 border border-brand-200 px-2 py-0.5 rounded">
                  {activeArticle.category}
                </span>
                <h2 className="text-base font-bold text-slate-900">
                  {activeArticle.title}
                </h2>
                <p className="text-[11px] text-slate-500">
                  By {activeArticle.authorName} • Updated {new Date(activeArticle.updatedAt).toLocaleDateString()}
                </p>
              </div>
              <button
                onClick={() => setActiveArticle(null)}
                className="p-1 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-200"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="p-6 overflow-y-auto space-y-4 text-xs leading-relaxed text-slate-800">
              <div className="p-3 bg-slate-50 rounded-lg border border-slate-200 font-medium text-slate-700">
                {activeArticle.summary}
              </div>

              <div className="prose prose-xs max-w-none whitespace-pre-line font-sans leading-relaxed text-slate-800">
                {activeArticle.body}
              </div>

              {activeArticle.tags && (
                <div className="pt-3 border-t border-slate-100 flex items-center gap-2 text-[11px] text-slate-500">
                  <Tag className="w-3.5 h-3.5" />
                  <span>Tags: {activeArticle.tags}</span>
                </div>
              )}
            </div>

            {/* Helpful Feedback Bar */}
            <div className="p-4 border-t border-slate-200 bg-slate-50 flex items-center justify-between">
              <span className="text-xs font-semibold text-slate-700">
                Was this guide helpful?
              </span>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => handleFeedback(true)}
                  className="px-3 py-1.5 bg-emerald-50 hover:bg-emerald-100 text-emerald-700 border border-emerald-200 rounded-lg text-xs font-semibold flex items-center gap-1 transition-colors"
                >
                  <ThumbsUp className="w-3.5 h-3.5" /> Yes ({activeArticle.helpfulCount})
                </button>
                <button
                  onClick={() => handleFeedback(false)}
                  className="px-3 py-1.5 bg-rose-50 hover:bg-rose-100 text-rose-700 border border-rose-200 rounded-lg text-xs font-semibold flex items-center gap-1 transition-colors"
                >
                  <ThumbsDown className="w-3.5 h-3.5" /> No ({activeArticle.notHelpfulCount})
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Create Article Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl max-w-xl w-full p-6 shadow-xl space-y-4 max-h-[90vh] overflow-y-auto">
            <h3 className="text-sm font-bold text-slate-900">Publish Knowledge Base Article</h3>
            <form onSubmit={handleCreateArticle} className="space-y-3 text-xs">
              <div>
                <label className="block font-bold text-slate-700 mb-1">Title *</label>
                <input
                  type="text"
                  required
                  value={newTitle}
                  onChange={(e) => setNewTitle(e.target.value)}
                  placeholder="e.g. How to configure OAuth 2.0 Webhooks"
                  className="w-full p-2 border border-slate-300 rounded-lg"
                />
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Summary *</label>
                <input
                  type="text"
                  required
                  value={newSummary}
                  onChange={(e) => setNewSummary(e.target.value)}
                  placeholder="Short 1-2 sentence overview for search snippets"
                  className="w-full p-2 border border-slate-300 rounded-lg"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-bold text-slate-700 mb-1">Category *</label>
                  <select
                    value={newCategory}
                    onChange={(e) => setNewCategory(e.target.value)}
                    className="w-full p-2 border border-slate-300 rounded-lg bg-slate-50"
                  >
                    <option value="TECHNICAL">Technical</option>
                    <option value="BILLING">Billing</option>
                    <option value="ACCOUNT">Account</option>
                    <option value="GENERAL">General</option>
                  </select>
                </div>
                <div>
                  <label className="block font-bold text-slate-700 mb-1">Tags (Comma-separated)</label>
                  <input
                    type="text"
                    value={newTags}
                    onChange={(e) => setNewTags(e.target.value)}
                    placeholder="oauth, webhooks, security"
                    className="w-full p-2 border border-slate-300 rounded-lg"
                  />
                </div>
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Article Body (Markdown supported) *</label>
                <textarea
                  rows={8}
                  required
                  value={newBody}
                  onChange={(e) => setNewBody(e.target.value)}
                  placeholder="Detailed troubleshooting steps, codes, or instructions..."
                  className="w-full p-2.5 border border-slate-300 rounded-lg font-sans"
                />
              </div>

              <div className="flex items-center gap-2">
                <input
                  type="checkbox"
                  id="pub"
                  checked={newPublished}
                  onChange={(e) => setNewPublished(e.target.checked)}
                  className="rounded border-slate-300 text-brand-600 focus:ring-brand-500"
                />
                <label htmlFor="pub" className="font-semibold text-slate-700">Publish immediately (visible to customers)</label>
              </div>

              <div className="flex justify-end gap-2 pt-3 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-3.5 py-1.5 border border-slate-300 rounded-lg text-slate-600 font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-1.5 bg-brand-600 text-white rounded-lg font-semibold"
                >
                  Save Article
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
