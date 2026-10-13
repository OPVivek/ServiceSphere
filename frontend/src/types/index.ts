export type Role = 'ADMIN' | 'AGENT' | 'CUSTOMER';

export type CaseStatus = 'OPEN' | 'IN_PROGRESS' | 'WAITING_FOR_CUSTOMER' | 'RESOLVED' | 'CLOSED';

export type CasePriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export type CaseCategory = 'BILLING' | 'TECHNICAL' | 'ACCOUNT' | 'GENERAL' | 'FEATURE_REQUEST';

export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  role: Role;
  active: boolean;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: Role;
}

export interface Account {
  id: number;
  name: string;
  industry?: string;
  tier?: string;
  website?: string;
  phone?: string;
  createdAt: string;
}

export interface Contact {
  id: number;
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  accountId?: number;
  accountName?: string;
  jobTitle?: string;
  phone?: string;
  createdAt: string;
}

export interface SupportCase {
  id: number;
  caseNumber: string;
  title: string;
  description: string;
  category: CaseCategory;
  priority: CasePriority;
  status: CaseStatus;
  customerId: number;
  customerName: string;
  customerEmail: string;
  assignedAgentId?: number;
  assignedAgentName?: string;
  firstResponseDeadline: string;
  resolutionDeadline: string;
  firstRespondedAt?: string;
  resolvedAt?: string;
  closedAt?: string;
  slaEscalated: boolean;
  reopenCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CaseComment {
  id: number;
  caseId: number;
  authorId: number;
  authorName: string;
  authorRole: string;
  message: string;
  internal: boolean;
  createdAt: string;
}

export interface CaseHistory {
  id: number;
  caseId: number;
  actorId: number;
  actorName: string;
  actorRole: string;
  action: string;
  fromValue?: string;
  toValue?: string;
  note?: string;
  createdAt: string;
}

export interface CaseDetail {
  caseDetails: SupportCase;
  comments: CaseComment[];
  history: CaseHistory[];
}

export interface Customer360 {
  customerId: number;
  name: string;
  email: string;
  phone?: string;
  jobTitle?: string;
  account?: Account;
  totalCases: number;
  openCases: number;
  resolvedCases: number;
  slaCompliancePercentage: number;
  recentCases: SupportCase[];
}

export interface AgentProfile {
  id: number;
  userId: number;
  name: string;
  email: string;
  skills: string;
  maxActiveCases: number;
  currentActiveCases: number;
  available: boolean;
}

export interface SlaPolicy {
  id: number;
  priority: CasePriority;
  firstResponseHours: number;
  resolutionHours: number;
  description?: string;
}

export interface SlaDashboard {
  totalActiveCases: number;
  withinSlaCases: number;
  approachingBreachCases: number;
  breachedCases: number;
  compliancePercentage: number;
}

export interface KnowledgeArticle {
  id: number;
  title: string;
  summary: string;
  body: string;
  category: string;
  tags?: string;
  published: boolean;
  authorId: number;
  authorName: string;
  viewCount: number;
  helpfulCount: number;
  notHelpfulCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface NotificationItem {
  id: number;
  title: string;
  message: string;
  type: string;
  referenceId?: number;
  referenceType?: string;
  read: boolean;
  createdAt: string;
}

export interface AnalyticsData {
  totalCases: number;
  openCases: number;
  inProgressCases: number;
  waitingCustomerCases: number;
  resolvedCases: number;
  closedCases: number;
  overdueCases: number;
  slaComplianceRate: number;
  avgFirstResponseHours: number;
  avgResolutionHours: number;
  reopeningRate: number;
  casesByPriority: Record<string, number>;
  casesByCategory: Record<string, number>;
  agentWorkloads: Array<{
    agentName: string;
    currentCases: number;
    maxCapacity: number;
    utilizationRate: number;
  }>;
  volumeOverTime: Array<{
    label: string;
    count: number;
  }>;
}

export interface AuditLogItem {
  id: number;
  actorId?: number;
  actorName?: string;
  actorEmail?: string;
  action: string;
  entityType: string;
  entityId: string;
  details?: string;
  ipAddress?: string;
  createdAt: string;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}
