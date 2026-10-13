import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { 
  LayoutDashboard, 
  Inbox, 
  PlusCircle, 
  Users, 
  Headphones, 
  BookOpen, 
  BarChart3, 
  Timer, 
  ShieldCheck 
} from 'lucide-react';

export const Sidebar: React.FC = () => {
  const { user } = useAuth();
  const role = user?.role;

  const links = [
    { to: '/', label: 'Dashboard', icon: LayoutDashboard, roles: ['ADMIN', 'AGENT', 'CUSTOMER'] },
    { to: '/cases', label: 'Support Cases', icon: Inbox, roles: ['ADMIN', 'AGENT', 'CUSTOMER'] },
    { to: '/cases/new', label: 'Create Case', icon: PlusCircle, roles: ['ADMIN', 'AGENT', 'CUSTOMER'] },
    { to: '/customers', label: 'Customer Directory', icon: Users, roles: ['ADMIN', 'AGENT'] },
    { to: '/agent-queue', label: 'Agent Workspace', icon: Headphones, roles: ['ADMIN', 'AGENT'] },
    { to: '/knowledge', label: 'Knowledge Base', icon: BookOpen, roles: ['ADMIN', 'AGENT', 'CUSTOMER'] },
    { to: '/analytics', label: 'Analytics & KPIs', icon: BarChart3, roles: ['ADMIN', 'AGENT'] },
    { to: '/sla', label: 'SLA Monitoring', icon: Timer, roles: ['ADMIN', 'AGENT'] },
    { to: '/admin/users', label: 'System Admin', icon: ShieldCheck, roles: ['ADMIN'] },
  ];

  const visibleLinks = links.filter((link) => !role || link.roles.includes(role));

  return (
    <aside className="w-64 bg-slate-900 text-slate-300 min-h-[calc(100vh-4rem)] flex flex-col justify-between shrink-0 border-r border-slate-800">
      <div className="p-4 space-y-6">
        <div>
          <div className="px-3 mb-2 text-[11px] font-bold uppercase tracking-wider text-slate-400">
            Workspace
          </div>
          <nav className="space-y-1">
            {visibleLinks.map((item) => {
              const Icon = item.icon;
              return (
                <NavLink
                  key={item.to}
                  to={item.to}
                  end={item.to === '/'}
                  className={({ isActive }) =>
                    `flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-xs font-semibold transition-all ${
                      isActive
                        ? 'bg-brand-600 text-white shadow-sm'
                        : 'text-slate-300 hover:bg-slate-800 hover:text-white'
                    }`
                  }
                >
                  <Icon className="w-4 h-4 shrink-0" />
                  <span>{item.label}</span>
                </NavLink>
              );
            })}
          </nav>
        </div>
      </div>

      <div className="p-4 border-t border-slate-800">
        <div className="bg-slate-800/80 rounded-lg p-3 text-[11px] text-slate-400 border border-slate-700/50">
          <p className="font-semibold text-slate-200 mb-0.5">ServiceSphere Enterprise</p>
          <p className="text-[10px] text-slate-400">Spring Boot 3 + PostgreSQL + React</p>
        </div>
      </div>
    </aside>
  );
};
