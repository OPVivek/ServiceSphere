import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { AppLayout } from './components/layout/AppLayout';

import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { DashboardPage } from './pages/DashboardPage';
import { CaseListPage } from './pages/CaseListPage';
import { CreateCasePage } from './pages/CreateCasePage';
import { CaseDetailPage } from './pages/CaseDetailPage';
import { Customer360Page } from './pages/Customer360Page';
import { CustomerManagementPage } from './pages/CustomerManagementPage';
import { AgentQueuePage } from './pages/AgentQueuePage';
import { KnowledgeBasePage } from './pages/KnowledgeBasePage';
import { AnalyticsPage } from './pages/AnalyticsPage';
import { SlaMonitorPage } from './pages/SlaMonitorPage';
import { AdminUsersPage } from './pages/AdminUsersPage';
import { NotFoundPage, UnauthorizedPage } from './pages/NotFoundPage';

export const App: React.FC = () => {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public Auth Routes */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/unauthorized" element={<UnauthorizedPage />} />

          {/* Protected Application Workspace */}
          <Route
            element={
              <ProtectedRoute>
                <AppLayout />
              </ProtectedRoute>
            }
          >
            <Route path="/" element={<DashboardPage />} />
            <Route path="/cases" element={<CaseListPage />} />
            <Route path="/cases/new" element={<CreateCasePage />} />
            <Route path="/cases/:id" element={<CaseDetailPage />} />
            <Route path="/customers/:id/360" element={<Customer360Page />} />
            <Route path="/knowledge" element={<KnowledgeBasePage />} />

            {/* Staff-Only Routes (Admin & Agent) */}
            <Route
              path="/customers"
              element={
                <ProtectedRoute allowedRoles={['ADMIN', 'AGENT']}>
                  <CustomerManagementPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/agent-queue"
              element={
                <ProtectedRoute allowedRoles={['ADMIN', 'AGENT']}>
                  <AgentQueuePage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/analytics"
              element={
                <ProtectedRoute allowedRoles={['ADMIN', 'AGENT']}>
                  <AnalyticsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/sla"
              element={
                <ProtectedRoute allowedRoles={['ADMIN', 'AGENT']}>
                  <SlaMonitorPage />
                </ProtectedRoute>
              }
            />

            {/* Admin-Only Governance */}
            <Route
              path="/admin/users"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <AdminUsersPage />
                </ProtectedRoute>
              }
            />
          </Route>

          {/* 404 Fallback */}
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
};

export default App;
