import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { DoctorQueueItem, TriagePriority, ClinicalSummaryStatus } from '../../types/doctor';
import { doctorService } from '../../services/doctorService';

interface DoctorDashboardProps {
  authToken?: string;
  onSelectCase: (sessionId: string) => void;
}

export const DoctorDashboard: React.FC<DoctorDashboardProps> = ({ authToken, onSelectCase }) => {
  const [queue, setQueue] = useState<DoctorQueueItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Filters
  const [priorityFilter, setPriorityFilter] = useState<string>('ALL');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [searchQuery, setSearchQuery] = useState<string>('');

  const loadQueue = useCallback(async () => {
    setLoading(true);
    setErrorMessage(null);
    try {
      const priorityParam = priorityFilter !== 'ALL' ? (priorityFilter as TriagePriority) : undefined;
      const statusParam = statusFilter !== 'ALL' ? (statusFilter as ClinicalSummaryStatus) : undefined;
      const res = await doctorService.getQueue(priorityParam, statusParam, authToken);
      if (res.data) {
        setQueue(res.data);
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to load consultation queue.';
      setErrorMessage(msg);
    } finally {
      setLoading(false);
    }
  }, [priorityFilter, statusFilter, authToken]);

  useEffect(() => {
    loadQueue();
  }, [loadQueue]);

  // Real metric counters calculated directly from live backend data
  const metrics = useMemo(() => {
    const total = queue.length;
    const urgent = queue.filter((item) => item.triagePriority === 'URGENT').length;
    const readyForReview = queue.filter((item) => item.reviewStatus === 'READY_FOR_REVIEW').length;
    const verified = queue.filter((item) => item.reviewStatus === 'VERIFIED').length;
    return { total, urgent, readyForReview, verified };
  }, [queue]);

  // Client-side search query filtering
  const filteredQueue = useMemo(() => {
    if (!searchQuery.trim()) return queue;
    const q = searchQuery.toLowerCase();
    return queue.filter(
      (item) =>
        item.patientName.toLowerCase().includes(q) ||
        item.patientIdentifier.toLowerCase().includes(q) ||
        (item.chiefComplaint && item.chiefComplaint.toLowerCase().includes(q))
    );
  }, [queue, searchQuery]);

  const getPriorityBadgeClass = (priority?: TriagePriority) => {
    switch (priority) {
      case 'URGENT':
        return 'bg-red-100 text-red-800 border-red-200 font-bold';
      case 'PRIORITY':
        return 'bg-amber-100 text-amber-800 border-amber-200 font-semibold';
      case 'STABLE':
        return 'bg-emerald-100 text-emerald-800 border-emerald-200';
      default:
        return 'bg-slate-100 text-slate-700 border-slate-200';
    }
  };

  const getStatusBadgeClass = (status: string) => {
    switch (status) {
      case 'VERIFIED':
        return 'bg-emerald-50 text-emerald-700 border-emerald-200 font-medium';
      case 'IN_REVIEW':
        return 'bg-blue-50 text-blue-700 border-blue-200 font-medium';
      case 'READY_FOR_REVIEW':
        return 'bg-indigo-50 text-indigo-700 border-indigo-200 font-medium';
      case 'PENDING_INTAKE':
      default:
        return 'bg-slate-100 text-slate-600 border-slate-200';
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      {/* Console Header */}
      <div className="flex flex-wrap items-center justify-between gap-4 mb-8 pb-6 border-b border-slate-200">
        <div>
          <div className="flex items-center space-x-3">
            <span className="text-2xl">👨‍⚕️</span>
            <h1 className="text-2xl font-bold text-slate-900 tracking-tight">
              Clinician Intake Workstation
            </h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-indigo-50 text-indigo-700 border border-indigo-200">
              Doctor Console
            </span>
          </div>
          <p className="text-sm text-slate-500 mt-1">
            Deterministic triage prioritization & pre-consult clinical intake review queue
          </p>
        </div>

        <button
          onClick={loadQueue}
          disabled={loading}
          className="px-3.5 py-2 bg-white hover:bg-slate-50 border border-slate-300 text-slate-700 text-sm font-medium rounded-lg shadow-sm transition-colors flex items-center space-x-2"
        >
          <span>🔄 Refresh Queue</span>
        </button>
      </div>

      {/* Metrics Banner */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
          <div className="text-xs uppercase font-bold text-slate-500 tracking-wider">Total Queue</div>
          <div className="text-2xl font-extrabold text-slate-900 mt-1">{metrics.total}</div>
          <div className="text-xs text-slate-400 mt-1">Active intake sessions</div>
        </div>

        <div className="bg-red-50/50 p-4 rounded-xl border border-red-200 shadow-sm">
          <div className="text-xs uppercase font-bold text-red-700 tracking-wider">Urgent Triage</div>
          <div className="text-2xl font-extrabold text-red-800 mt-1">{metrics.urgent}</div>
          <div className="text-xs text-red-600 mt-1">Immediate doctor attention</div>
        </div>

        <div className="bg-indigo-50/50 p-4 rounded-xl border border-indigo-200 shadow-sm">
          <div className="text-xs uppercase font-bold text-indigo-700 tracking-wider">Ready For Review</div>
          <div className="text-2xl font-extrabold text-indigo-800 mt-1">{metrics.readyForReview}</div>
          <div className="text-xs text-indigo-600 mt-1">Pre-consult summary ready</div>
        </div>

        <div className="bg-emerald-50/50 p-4 rounded-xl border border-emerald-200 shadow-sm">
          <div className="text-xs uppercase font-bold text-emerald-700 tracking-wider">Verified Cases</div>
          <div className="text-2xl font-extrabold text-emerald-800 mt-1">{metrics.verified}</div>
          <div className="text-xs text-emerald-600 mt-1">Clinician verified</div>
        </div>
      </div>

      {/* Error Alert */}
      {errorMessage && (
        <div className="mb-6 p-4 rounded-xl bg-red-50 border border-red-200 text-red-800 text-sm flex items-center justify-between">
          <span>{errorMessage}</span>
          <button onClick={() => setErrorMessage(null)} className="text-red-500 hover:text-red-800 font-bold">
            ✕
          </button>
        </div>
      )}

      {/* Filters and Search Bar */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm mb-6 flex flex-wrap items-center justify-between gap-4">
        <div className="flex-1 min-w-[240px]">
          <input
            type="text"
            placeholder="Search by patient name, identifier, or chief complaint..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full px-3.5 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
          />
        </div>

        <div className="flex flex-wrap items-center gap-3">
          {/* Priority filter */}
          <div className="flex items-center space-x-2 text-xs">
            <label htmlFor="priority-filter" className="font-semibold text-slate-600">
              Triage:
            </label>
            <select
              id="priority-filter"
              value={priorityFilter}
              onChange={(e) => setPriorityFilter(e.target.value)}
              className="px-2.5 py-1.5 text-xs bg-slate-50 border border-slate-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500"
            >
              <option value="ALL">All Priorities</option>
              <option value="URGENT">Urgent Cases</option>
              <option value="PRIORITY">Priority Cases</option>
              <option value="STABLE">Stable Cases</option>
            </select>
          </div>

          {/* Status filter */}
          <div className="flex items-center space-x-2 text-xs">
            <label htmlFor="status-filter" className="font-semibold text-slate-600">
              Review Status:
            </label>
            <select
              id="status-filter"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="px-2.5 py-1.5 text-xs bg-slate-50 border border-slate-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500"
            >
              <option value="ALL">All Statuses</option>
              <option value="AI_GENERATED">Ready for Review</option>
              <option value="DOCTOR_REVIEWED">In Review</option>
              <option value="VERIFIED">Verified</option>
            </select>
          </div>
        </div>
      </div>

      {/* Queue Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        {loading ? (
          <div className="py-16 text-center text-slate-500">
            <div className="w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin mx-auto mb-3" />
            <p className="text-sm font-medium">Loading consultation queue...</p>
          </div>
        ) : filteredQueue.length === 0 ? (
          <div className="py-16 text-center text-slate-500">
            <div className="text-4xl mb-3">📭</div>
            <h3 className="text-base font-semibold text-slate-800">No Patients in Queue</h3>
            <p className="text-sm text-slate-500 max-w-sm mx-auto mt-1">
              There are currently no patients matching the selected filter criteria.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50/80 border-b border-slate-200 text-[11px] font-bold text-slate-500 uppercase tracking-wider">
                  <th className="py-3.5 px-4">Triage Priority</th>
                  <th className="py-3.5 px-4">Patient</th>
                  <th className="py-3.5 px-4">Chief Complaint</th>
                  <th className="py-3.5 px-4">Review Status</th>
                  <th className="py-3.5 px-4">Session Time</th>
                  <th className="py-3.5 px-4 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-sm">
                {filteredQueue.map((item) => (
                  <tr
                    key={item.sessionId}
                    className={`hover:bg-slate-50/60 transition-colors ${
                      item.urgent ? 'bg-red-50/20' : ''
                    }`}
                  >
                    {/* Triage Priority */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      <div className="flex flex-col items-start">
                        <span
                          className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs border ${getPriorityBadgeClass(
                            item.triagePriority
                          )}`}
                        >
                          {item.triagePriority || 'NOT_TRIAGED'}
                        </span>
                        {item.triageReason && (
                          <span
                            className="text-[11px] text-slate-500 mt-1 max-w-[180px] truncate"
                            title={item.triageReason}
                          >
                            {item.triageReason}
                          </span>
                        )}
                      </div>
                    </td>

                    {/* Patient Name & Demographics */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      <div className="font-semibold text-slate-900">{item.patientName}</div>
                      <div className="text-xs text-slate-500 flex items-center space-x-2 mt-0.5">
                        <span className="font-mono text-[11px] bg-slate-100 px-1 rounded">
                          {item.patientIdentifier}
                        </span>
                        {item.patientAge && <span>{item.patientAge}y</span>}
                        {item.patientGender && <span>• {item.patientGender}</span>}
                      </div>
                    </td>

                    {/* Chief Complaint */}
                    <td className="py-4 px-4">
                      <p className="text-slate-800 line-clamp-2 text-xs md:text-sm">
                        {item.chiefComplaint || 'Not stated'}
                      </p>
                    </td>

                    {/* Review Status */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      <span
                        className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs border ${getStatusBadgeClass(
                          item.reviewStatus
                        )}`}
                      >
                        {item.reviewStatus.replace(/_/g, ' ')}
                      </span>
                    </td>

                    {/* Time */}
                    <td className="py-4 px-4 whitespace-nowrap text-xs text-slate-500">
                      {item.sessionCreatedAt
                        ? new Date(item.sessionCreatedAt).toLocaleTimeString([], {
                            hour: '2-digit',
                            minute: '2-digit',
                          })
                        : '—'}
                    </td>

                    {/* Action Button */}
                    <td className="py-4 px-4 whitespace-nowrap text-right">
                      <button
                        id={`open-review-${item.sessionId}`}
                        onClick={() => onSelectCase(item.sessionId)}
                        className={`px-3 py-1.5 text-xs font-semibold rounded-lg shadow-sm transition-all ${
                          item.urgent
                            ? 'bg-red-600 hover:bg-red-700 text-white'
                            : 'bg-indigo-600 hover:bg-indigo-700 text-white'
                        }`}
                      >
                        Review Case →
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
export default DoctorDashboard;
