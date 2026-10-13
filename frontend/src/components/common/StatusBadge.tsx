import React from 'react';
import { CaseStatus, CasePriority } from '../../types';
import { CheckCircle2, Clock, AlertCircle, HelpCircle, Archive } from 'lucide-react';

interface StatusBadgeProps {
  status: CaseStatus;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status }) => {
  switch (status) {
    case 'OPEN':
      return (
        <span className="badge-open">
          <HelpCircle className="w-3.5 h-3.5" />
          Open
        </span>
      );
    case 'IN_PROGRESS':
      return (
        <span className="badge-in-progress">
          <Clock className="w-3.5 h-3.5" />
          In Progress
        </span>
      );
    case 'WAITING_FOR_CUSTOMER':
      return (
        <span className="badge-waiting">
          <Clock className="w-3.5 h-3.5" />
          Waiting on Customer
        </span>
      );
    case 'RESOLVED':
      return (
        <span className="badge-resolved">
          <CheckCircle2 className="w-3.5 h-3.5" />
          Resolved
        </span>
      );
    case 'CLOSED':
      return (
        <span className="badge-closed">
          <Archive className="w-3.5 h-3.5" />
          Closed
        </span>
      );
    default:
      return <span>{status}</span>;
  }
};

interface PriorityBadgeProps {
  priority: CasePriority;
}

export const PriorityBadge: React.FC<PriorityBadgeProps> = ({ priority }) => {
  switch (priority) {
    case 'CRITICAL':
      return <span className="badge-critical flex items-center gap-1"><AlertCircle className="w-3 h-3" /> Critical</span>;
    case 'HIGH':
      return <span className="badge-high">High</span>;
    case 'MEDIUM':
      return <span className="badge-medium">Medium</span>;
    case 'LOW':
      return <span className="badge-low">Low</span>;
    default:
      return <span>{priority}</span>;
  }
};
