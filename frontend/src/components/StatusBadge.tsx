import React from 'react';
import { ConnectionState } from '../types/api';
import { CheckCircle2, AlertCircle, Loader2 } from 'lucide-react';

interface StatusBadgeProps {
  state: ConnectionState;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ state }) => {
  if (state === 'connected') {
    return (
      <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
        <CheckCircle2 className="w-3.5 h-3.5" />
        Connected
      </span>
    );
  }

  if (state === 'checking') {
    return (
      <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-amber-500/10 text-amber-400 border border-amber-500/30">
        <Loader2 className="w-3.5 h-3.5 animate-spin" />
        Checking Connection...
      </span>
    );
  }

  return (
    <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-rose-500/10 text-rose-400 border border-rose-500/30">
      <AlertCircle className="w-3.5 h-3.5" />
      Unavailable
    </span>
  );
};
