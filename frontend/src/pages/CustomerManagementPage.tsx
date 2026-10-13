import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '../api/client';
import { Contact, Account } from '../types';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { 
  Users, 
  Building, 
  Search, 
  PlusCircle, 
  ExternalLink, 
  Mail, 
  Phone, 
  Globe 
} from 'lucide-react';

export const CustomerManagementPage: React.FC = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<'contacts' | 'accounts'>('contacts');
  const [contacts, setContacts] = useState<Contact[]>([]);
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);

  // New Account Modal State
  const [showAccountModal, setShowAccountModal] = useState(false);
  const [accountName, setAccountName] = useState('');
  const [industry, setIndustry] = useState('');
  const [tier, setTier] = useState('STANDARD');
  const [website, setWebsite] = useState('');
  const [phone, setPhone] = useState('');

  const fetchData = async () => {
    setLoading(true);
    try {
      if (activeTab === 'contacts') {
        const res = await apiClient.get(`/customers?query=${encodeURIComponent(search)}`);
        setContacts(res.data.data.content);
      } else {
        const res = await apiClient.get(`/accounts?query=${encodeURIComponent(search)}`);
        setAccounts(res.data.data.content);
      }
    } catch (err) {
      console.error('Failed to fetch CRM records', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [activeTab, search]);

  const handleCreateAccount = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await apiClient.post('/accounts', {
        name: accountName,
        industry,
        tier,
        website,
        phone,
      });
      setShowAccountModal(false);
      setAccountName('');
      setIndustry('');
      setWebsite('');
      setPhone('');
      fetchData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to create account');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-extrabold text-slate-900 tracking-tight">
            Customer Relationship Management
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Directory of client accounts, associated contacts, and enterprise profiles
          </p>
        </div>

        {activeTab === 'accounts' && (
          <button
            onClick={() => setShowAccountModal(true)}
            className="bg-brand-600 hover:bg-brand-700 text-white font-semibold px-4 py-2 rounded-lg text-xs shadow-xs transition-colors flex items-center gap-1.5 self-start sm:self-auto"
          >
            <PlusCircle className="w-4 h-4" /> Add Company Account
          </button>
        )}
      </div>

      {/* Tabs & Search Header */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="flex items-center gap-2 border-b border-slate-200 pb-2 sm:pb-0 sm:border-0">
            <button
              onClick={() => { setActiveTab('contacts'); setSearch(''); }}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold flex items-center gap-1.5 transition-colors ${
                activeTab === 'contacts'
                  ? 'bg-brand-600 text-white shadow-xs'
                  : 'text-slate-600 hover:bg-slate-100'
              }`}
            >
              <Users className="w-3.5 h-3.5" />
              Contacts ({contacts.length})
            </button>
            <button
              onClick={() => { setActiveTab('accounts'); setSearch(''); }}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold flex items-center gap-1.5 transition-colors ${
                activeTab === 'accounts'
                  ? 'bg-brand-600 text-white shadow-xs'
                  : 'text-slate-600 hover:bg-slate-100'
              }`}
            >
              <Building className="w-3.5 h-3.5" />
              Company Accounts ({accounts.length})
            </button>
          </div>

          <div className="relative w-full sm:w-72">
            <Search className="w-4 h-4 absolute left-3 top-2.5 text-slate-400" />
            <input
              type="text"
              placeholder={`Search ${activeTab}...`}
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-9 pr-3.5 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 focus:bg-white"
            />
          </div>
        </div>
      </div>

      {/* Table Data */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        {loading ? (
          <LoadingSpinner message={`Loading ${activeTab}...`} />
        ) : activeTab === 'contacts' ? (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-slate-200 bg-slate-50/80 text-[11px] font-bold text-slate-500 uppercase tracking-wider">
                  <th className="py-3 px-4">Contact Name</th>
                  <th className="py-3 px-4">Email</th>
                  <th className="py-3 px-4">Company Account</th>
                  <th className="py-3 px-4">Job Title</th>
                  <th className="py-3 px-4">Phone</th>
                  <th className="py-3 px-4 text-right">Customer 360</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-xs text-slate-700">
                {contacts.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="py-8 text-center text-slate-400">
                      No contacts found matching search query.
                    </td>
                  </tr>
                ) : (
                  contacts.map((c) => (
                    <tr key={c.id} className="hover:bg-slate-50 transition-colors">
                      <td className="py-3 px-4 font-semibold text-slate-900">{c.fullName}</td>
                      <td className="py-3 px-4 text-slate-600">{c.email}</td>
                      <td className="py-3 px-4 font-medium text-slate-800">{c.accountName || '—'}</td>
                      <td className="py-3 px-4 text-slate-500">{c.jobTitle || '—'}</td>
                      <td className="py-3 px-4 text-slate-500">{c.phone || '—'}</td>
                      <td className="py-3 px-4 text-right">
                        <button
                          onClick={() => navigate(`/customers/${c.userId}/360`)}
                          className="px-2.5 py-1 text-xs font-semibold text-purple-700 hover:text-purple-900 bg-purple-50 hover:bg-purple-100 rounded-md transition-colors inline-flex items-center gap-1"
                        >
                          Customer 360 <ExternalLink className="w-3 h-3" />
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-slate-200 bg-slate-50/80 text-[11px] font-bold text-slate-500 uppercase tracking-wider">
                  <th className="py-3 px-4">Company Name</th>
                  <th className="py-3 px-4">Tier</th>
                  <th className="py-3 px-4">Industry</th>
                  <th className="py-3 px-4">Website</th>
                  <th className="py-3 px-4">Phone</th>
                  <th className="py-3 px-4">Onboarded</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-xs text-slate-700">
                {accounts.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="py-8 text-center text-slate-400">
                      No company accounts found.
                    </td>
                  </tr>
                ) : (
                  accounts.map((acc) => (
                    <tr key={acc.id} className="hover:bg-slate-50 transition-colors">
                      <td className="py-3 px-4 font-bold text-slate-900">{acc.name}</td>
                      <td className="py-3 px-4">
                        <span className="bg-slate-100 text-slate-700 font-semibold text-[10px] px-2 py-0.5 rounded-full border border-slate-200">
                          {acc.tier}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-slate-600">{acc.industry || '—'}</td>
                      <td className="py-3 px-4 text-brand-600 truncate max-w-xs">{acc.website || '—'}</td>
                      <td className="py-3 px-4 text-slate-500">{acc.phone || '—'}</td>
                      <td className="py-3 px-4 text-slate-400">{new Date(acc.createdAt).toLocaleDateString()}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* New Account Modal */}
      {showAccountModal && (
        <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl max-w-md w-full p-6 shadow-xl space-y-4">
            <h3 className="text-sm font-bold text-slate-900">Add New Company Account</h3>
            <form onSubmit={handleCreateAccount} className="space-y-3 text-xs">
              <div>
                <label className="block font-bold text-slate-700 mb-1">Company Name *</label>
                <input
                  type="text"
                  required
                  value={accountName}
                  onChange={(e) => setAccountName(e.target.value)}
                  placeholder="e.g. Stripe Inc."
                  className="w-full p-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500"
                />
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Industry</label>
                <input
                  type="text"
                  value={industry}
                  onChange={(e) => setIndustry(e.target.value)}
                  placeholder="Fintech & Payments"
                  className="w-full p-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500"
                />
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Account Tier</label>
                <select
                  value={tier}
                  onChange={(e) => setTier(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg bg-slate-50"
                >
                  <option value="STANDARD">Standard</option>
                  <option value="GROWTH">Growth</option>
                  <option value="ENTERPRISE">Enterprise</option>
                </select>
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Website</label>
                <input
                  type="url"
                  value={website}
                  onChange={(e) => setWebsite(e.target.value)}
                  placeholder="https://stripe.com"
                  className="w-full p-2 border border-slate-300 rounded-lg"
                />
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Phone</label>
                <input
                  type="text"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  placeholder="+1-800-555-0199"
                  className="w-full p-2 border border-slate-300 rounded-lg"
                />
              </div>

              <div className="flex justify-end gap-2 pt-3 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setShowAccountModal(false)}
                  className="px-3.5 py-1.5 border border-slate-300 rounded-lg text-slate-600 font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-1.5 bg-brand-600 text-white rounded-lg font-semibold"
                >
                  Create Account
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
