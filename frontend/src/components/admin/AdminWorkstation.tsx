import React, { useState, useEffect, useCallback } from 'react';
import { adminService } from '../../services/adminService';
import {
  SystemAnalytics,
  AdminUser,
  AuditLogDetail,
  SystemConfigSummary,
} from '../../types/admin';
import {
  Users,
  Activity,
  ShieldCheck,
  FileText,
  Server,
  UserPlus,
  RefreshCw,
  Search,
  CheckCircle2,
  XCircle,
  Clock,
  Layers,
  BarChart3,
  Database,
  Lock,
} from 'lucide-react';

interface AdminWorkstationProps {
  authToken?: string | null;
}

export const AdminWorkstation: React.FC<AdminWorkstationProps> = ({ authToken }) => {
  const [activeTab, setActiveTab] = useState<'analytics' | 'users' | 'audit' | 'config'>('analytics');
  const [analytics, setAnalytics] = useState<SystemAnalytics | null>(null);
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [auditLogs, setAuditLogs] = useState<AuditLogDetail[]>([]);
  const [config, setConfig] = useState<SystemConfigSummary | null>(null);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  // Filters & Search
  const [userRoleFilter, setUserRoleFilter] = useState<string>('');
  const [userSearch, setUserSearch] = useState<string>('');
  const [auditActionFilter, setAuditActionFilter] = useState<string>('');
  const [selectedAuditLog, setSelectedAuditLog] = useState<AuditLogDetail | null>(null);

  // Add User Modal State
  const [isAddUserOpen, setIsAddUserOpen] = useState(false);
  const [newEmail, setNewEmail] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [newFirstName, setNewFirstName] = useState('');
  const [newLastName, setNewLastName] = useState('');
  const [newRole, setNewRole] = useState<'DOCTOR' | 'ADMIN'>('DOCTOR');

  const loadData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      if (activeTab === 'analytics') {
        const res = await adminService.getAnalytics(authToken);
        if (res.data) setAnalytics(res.data);
      } else if (activeTab === 'users') {
        const res = await adminService.getUsers(authToken, userRoleFilter || undefined, userSearch || undefined);
        if (res.data) setUsers(res.data);
      } else if (activeTab === 'audit') {
        const res = await adminService.getAuditLogs(authToken, auditActionFilter || undefined);
        if (res.data) setAuditLogs(res.data);
      } else if (activeTab === 'config') {
        const res = await adminService.getConfigSummary(authToken);
        if (res.data) setConfig(res.data);
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to load administrative data';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, [activeTab, authToken, userRoleFilter, userSearch, auditActionFilter]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleCreateUser = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newEmail || !newPassword || !newFirstName || !newLastName) {
      setError('All fields are required');
      return;
    }
    try {
      await adminService.createUser(
        {
          email: newEmail.trim(),
          password: newPassword,
          firstName: newFirstName.trim(),
          lastName: newLastName.trim(),
          role: newRole,
        },
        authToken
      );
      setSuccessMsg(`Successfully registered new ${newRole.toLowerCase()}: ${newEmail}`);
      setIsAddUserOpen(false);
      setNewEmail('');
      setNewPassword('');
      setNewFirstName('');
      setNewLastName('');
      loadData();
      setTimeout(() => setSuccessMsg(null), 4000);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to create user account';
      setError(msg);
    }
  };

  const handleToggleUserStatus = async (user: AdminUser) => {
    const nextStatus = user.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    try {
      await adminService.updateUserStatus(user.id, nextStatus, authToken);
      setSuccessMsg(`Updated user ${user.email} status to ${nextStatus}`);
      loadData();
      setTimeout(() => setSuccessMsg(null), 3000);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to update user status';
      setError(msg);
    }
  };

  return (
    <div className="min-h-screen bg-slate-900 text-slate-100 flex flex-col font-sans">
      {/* Header Banner */}
      <header className="bg-slate-800/90 backdrop-blur-md border-b border-slate-700/80 px-6 py-4 sticky top-0 z-40">
        <div className="max-w-7xl mx-auto flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center space-x-3">
            <div className="p-2.5 bg-indigo-600/20 text-indigo-400 border border-indigo-500/30 rounded-xl">
              <ShieldCheck className="w-6 h-6" />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <h1 className="text-xl font-extrabold tracking-tight text-white">Hospital Administration Workstation</h1>
                <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                  STAGE 6
                </span>
              </div>
              <p className="text-xs text-slate-400">System Analytics, Clinician Directory, Security Audit Trail & Config Governance</p>
            </div>
          </div>

          <div className="flex items-center space-x-3">
            <button
              onClick={loadData}
              disabled={loading}
              className="p-2 text-slate-300 hover:text-white bg-slate-700/60 hover:bg-slate-700 rounded-lg transition-colors flex items-center space-x-1.5 text-xs font-semibold border border-slate-600"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
              <span>Refresh Data</span>
            </button>
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="max-w-7xl mx-auto px-6 py-6 flex-1 w-full space-y-6">
        {/* Banner Alert Messages */}
        {error && (
          <div className="p-4 bg-red-950/80 border border-red-500/50 rounded-xl text-red-200 text-sm flex items-center justify-between shadow-lg">
            <div className="flex items-center space-x-2">
              <XCircle className="w-5 h-5 text-red-400 shrink-0" />
              <span>{error}</span>
            </div>
            <button onClick={() => setError(null)} className="text-red-400 hover:text-red-200 text-xs font-bold">Dismiss</button>
          </div>
        )}

        {successMsg && (
          <div className="p-4 bg-emerald-950/80 border border-emerald-500/50 rounded-xl text-emerald-200 text-sm flex items-center space-x-2 shadow-lg">
            <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0" />
            <span>{successMsg}</span>
          </div>
        )}

        {/* Navigation Tabs */}
        <div className="flex flex-wrap border-b border-slate-700/80 space-x-1">
          <button
            onClick={() => setActiveTab('analytics')}
            className={`px-5 py-3 text-sm font-semibold border-b-2 flex items-center space-x-2 transition-colors ${
              activeTab === 'analytics'
                ? 'border-indigo-500 text-indigo-400 bg-indigo-500/10'
                : 'border-transparent text-slate-400 hover:text-slate-200 hover:bg-slate-800/40'
            } rounded-t-xl`}
          >
            <BarChart3 className="w-4 h-4" />
            <span>System Analytics</span>
          </button>

          <button
            onClick={() => setActiveTab('users')}
            className={`px-5 py-3 text-sm font-semibold border-b-2 flex items-center space-x-2 transition-colors ${
              activeTab === 'users'
                ? 'border-indigo-500 text-indigo-400 bg-indigo-500/10'
                : 'border-transparent text-slate-400 hover:text-slate-200 hover:bg-slate-800/40'
            } rounded-t-xl`}
          >
            <Users className="w-4 h-4" />
            <span>Clinicians & Staff</span>
          </button>

          <button
            onClick={() => setActiveTab('audit')}
            className={`px-5 py-3 text-sm font-semibold border-b-2 flex items-center space-x-2 transition-colors ${
              activeTab === 'audit'
                ? 'border-indigo-500 text-indigo-400 bg-indigo-500/10'
                : 'border-transparent text-slate-400 hover:text-slate-200 hover:bg-slate-800/40'
            } rounded-t-xl`}
          >
            <ShieldCheck className="w-4 h-4" />
            <span>Security Audit Log</span>
          </button>

          <button
            onClick={() => setActiveTab('config')}
            className={`px-5 py-3 text-sm font-semibold border-b-2 flex items-center space-x-2 transition-colors ${
              activeTab === 'config'
                ? 'border-indigo-500 text-indigo-400 bg-indigo-500/10'
                : 'border-transparent text-slate-400 hover:text-slate-200 hover:bg-slate-800/40'
            } rounded-t-xl`}
          >
            <Server className="w-4 h-4" />
            <span>System Config & Status</span>
          </button>
        </div>

        {/* TAB 1: SYSTEM ANALYTICS */}
        {activeTab === 'analytics' && (
          <div className="space-y-6">
            {/* Metric KPI Cards Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              <div className="bg-slate-800/80 border border-slate-700 p-5 rounded-2xl shadow-md flex items-center space-x-4">
                <div className="p-3 bg-blue-500/20 text-blue-400 rounded-xl border border-blue-500/30">
                  <Activity className="w-6 h-6" />
                </div>
                <div>
                  <p className="text-xs font-medium text-slate-400 uppercase tracking-wider">Total Intakes</p>
                  <p className="text-2xl font-black text-white">{analytics?.totalIntakes || 0}</p>
                  <p className="text-[11px] text-slate-400 mt-0.5">Completed: {analytics?.completedIntakes || 0}</p>
                </div>
              </div>

              <div className="bg-slate-800/80 border border-slate-700 p-5 rounded-2xl shadow-md flex items-center space-x-4">
                <div className="p-3 bg-emerald-500/20 text-emerald-400 rounded-xl border border-emerald-500/30">
                  <Users className="w-6 h-6" />
                </div>
                <div>
                  <p className="text-xs font-medium text-slate-400 uppercase tracking-wider">Patient Profiles</p>
                  <p className="text-2xl font-black text-white">{analytics?.totalPatients || 0}</p>
                  <p className="text-[11px] text-slate-400 mt-0.5">Registered Clinicians: {analytics?.totalDoctors || 0}</p>
                </div>
              </div>

              <div className="bg-slate-800/80 border border-slate-700 p-5 rounded-2xl shadow-md flex items-center space-x-4">
                <div className="p-3 bg-indigo-500/20 text-indigo-400 rounded-xl border border-indigo-500/30">
                  <FileText className="w-6 h-6" />
                </div>
                <div>
                  <p className="text-xs font-medium text-slate-400 uppercase tracking-wider">Clinical Summaries</p>
                  <p className="text-2xl font-black text-white">{analytics?.totalSummariesGenerated || 0}</p>
                  <p className="text-[11px] text-emerald-400 mt-0.5">Verified: {analytics?.totalSummariesVerified || 0}</p>
                </div>
              </div>

              <div className="bg-slate-800/80 border border-slate-700 p-5 rounded-2xl shadow-md flex items-center space-x-4">
                <div className="p-3 bg-purple-500/20 text-purple-400 rounded-xl border border-purple-500/30">
                  <Clock className="w-6 h-6" />
                </div>
                <div>
                  <p className="text-xs font-medium text-slate-400 uppercase tracking-wider">Avg Intake Time</p>
                  <p className="text-2xl font-black text-white">{analytics?.avgIntakeDurationSeconds || 0}s</p>
                  <p className="text-[11px] text-slate-400 mt-0.5">OCR Volume: {analytics?.totalDocumentsProcessed || 0} docs</p>
                </div>
              </div>
            </div>

            {/* Triage Priority & Interoperability Breakdowns */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {/* Triage Priority Distribution Card */}
              <div className="bg-slate-800/80 border border-slate-700 rounded-2xl p-6 shadow-md space-y-4">
                <div className="flex items-center justify-between border-b border-slate-700 pb-3">
                  <h2 className="text-base font-bold text-white flex items-center space-x-2">
                    <Layers className="w-5 h-5 text-indigo-400" />
                    <span>Triage Priority Distribution</span>
                  </h2>
                  <span className="text-xs text-slate-400">Deterministic Rule Engine</span>
                </div>

                <div className="space-y-3">
                  <div className="flex items-center justify-between text-sm p-3 bg-red-950/40 border border-red-800/50 rounded-xl">
                    <span className="font-semibold text-red-300">EMERGENCY</span>
                    <span className="px-3 py-1 bg-red-500/20 text-red-300 font-bold rounded-lg border border-red-500/30">
                      {analytics?.triageDistribution?.EMERGENCY || 0}
                    </span>
                  </div>

                  <div className="flex items-center justify-between text-sm p-3 bg-amber-950/40 border border-amber-800/50 rounded-xl">
                    <span className="font-semibold text-amber-300">URGENT</span>
                    <span className="px-3 py-1 bg-amber-500/20 text-amber-300 font-bold rounded-lg border border-amber-500/30">
                      {analytics?.triageDistribution?.URGENT || 0}
                    </span>
                  </div>

                  <div className="flex items-center justify-between text-sm p-3 bg-blue-950/40 border border-blue-800/50 rounded-xl">
                    <span className="font-semibold text-blue-300">PRIORITY</span>
                    <span className="px-3 py-1 bg-blue-500/20 text-blue-300 font-bold rounded-lg border border-blue-500/30">
                      {analytics?.triageDistribution?.PRIORITY || 0}
                    </span>
                  </div>

                  <div className="flex items-center justify-between text-sm p-3 bg-slate-900 border border-slate-700 rounded-xl">
                    <span className="font-semibold text-slate-300">ROUTINE</span>
                    <span className="px-3 py-1 bg-slate-700 text-slate-300 font-bold rounded-lg">
                      {analytics?.triageDistribution?.ROUTINE || 0}
                    </span>
                  </div>
                </div>
              </div>

              {/* Multi-System Integration Activity Card */}
              <div className="bg-slate-800/80 border border-slate-700 rounded-2xl p-6 shadow-md space-y-4">
                <div className="flex items-center justify-between border-b border-slate-700 pb-3">
                  <h2 className="text-base font-bold text-white flex items-center space-x-2">
                    <Database className="w-5 h-5 text-indigo-400" />
                    <span>Healthcare Interoperability Activity</span>
                  </h2>
                  <span className="text-xs text-slate-400">FHIR / ABDM / HIS / HealthLake</span>
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div className="p-4 bg-slate-900/90 border border-slate-700/80 rounded-xl">
                    <p className="text-xs font-semibold text-slate-400">FHIR R4 Bundles</p>
                    <p className="text-xl font-bold text-indigo-400 mt-1">{analytics?.fhirBundlesGenerated || 0}</p>
                    <p className="text-[10px] text-slate-500 mt-1">Generated & Validated</p>
                  </div>

                  <div className="p-4 bg-slate-900/90 border border-slate-700/80 rounded-xl">
                    <p className="text-xs font-semibold text-slate-400">ABDM Exchanges</p>
                    <p className="text-xl font-bold text-emerald-400 mt-1">{analytics?.abdmExchangesCount || 0}</p>
                    <p className="text-[10px] text-slate-500 mt-1">Consent-Gated Transmissions</p>
                  </div>

                  <div className="p-4 bg-slate-900/90 border border-slate-700/80 rounded-xl">
                    <p className="text-xs font-semibold text-slate-400">HIS Exports</p>
                    <p className="text-xl font-bold text-blue-400 mt-1">{analytics?.hisExportsCount || 0}</p>
                    <p className="text-[10px] text-slate-500 mt-1">Hospital Queues Linked</p>
                  </div>

                  <div className="p-4 bg-slate-900/90 border border-slate-700/80 rounded-xl">
                    <p className="text-xs font-semibold text-slate-400">AWS HealthLake</p>
                    <p className="text-xl font-bold text-purple-400 mt-1">{analytics?.healthLakeSyncsCount || 0}</p>
                    <p className="text-[10px] text-slate-500 mt-1">Cloud Datastore Syncs</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB 2: CLINICIANS & STAFF */}
        {activeTab === 'users' && (
          <div className="space-y-4">
            {/* Toolbar: Search, Role Filter, Add Button */}
            <div className="flex flex-wrap items-center justify-between gap-3 bg-slate-800/80 border border-slate-700 p-4 rounded-2xl">
              <div className="flex flex-wrap items-center gap-3 flex-1">
                <div className="relative min-w-[240px] flex-1">
                  <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                  <input
                    type="text"
                    placeholder="Search clinicians by name or email..."
                    value={userSearch}
                    onChange={(e) => setUserSearch(e.target.value)}
                    className="w-full bg-slate-900 border border-slate-700 rounded-xl pl-9 pr-4 py-2 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-indigo-500"
                  />
                </div>

                <select
                  value={userRoleFilter}
                  onChange={(e) => setUserRoleFilter(e.target.value)}
                  className="bg-slate-900 border border-slate-700 text-slate-300 text-xs rounded-xl px-3 py-2 focus:outline-none focus:border-indigo-500"
                >
                  <option value="">All Roles</option>
                  <option value="DOCTOR">Doctors</option>
                  <option value="ADMIN">Administrators</option>
                  <option value="PATIENT">Patients</option>
                </select>
              </div>

              <button
                onClick={() => setIsAddUserOpen(true)}
                className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl text-xs font-bold transition-colors flex items-center space-x-1.5 shadow-md"
              >
                <UserPlus className="w-4 h-4" />
                <span>Add New Clinician / Staff</span>
              </button>
            </div>

            {/* Users Table */}
            <div className="bg-slate-800/80 border border-slate-700 rounded-2xl overflow-hidden shadow-md">
              <table className="w-full text-left border-collapse text-xs">
                <thead>
                  <tr className="bg-slate-900/80 border-b border-slate-700 text-slate-400 uppercase tracking-wider font-semibold">
                    <th className="p-4">Name</th>
                    <th className="p-4">Email</th>
                    <th className="p-4">Role</th>
                    <th className="p-4">Status</th>
                    <th className="p-4">Created Date</th>
                    <th className="p-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-700/60 text-slate-200">
                  {users.length === 0 ? (
                    <tr>
                      <td colSpan={6} className="p-8 text-center text-slate-500">
                        No user accounts match current search criteria.
                      </td>
                    </tr>
                  ) : (
                    users.map((u) => (
                      <tr key={u.id} className="hover:bg-slate-700/30 transition-colors">
                        <td className="p-4 font-bold text-white">
                          {u.firstName} {u.lastName}
                        </td>
                        <td className="p-4 text-slate-300 font-mono">{u.email}</td>
                        <td className="p-4">
                          <span
                            className={`px-2.5 py-1 rounded-full font-bold text-[10px] ${
                              u.role === 'ADMIN'
                                ? 'bg-purple-500/20 text-purple-300 border border-purple-500/30'
                                : u.role === 'DOCTOR'
                                ? 'bg-indigo-500/20 text-indigo-300 border border-indigo-500/30'
                                : 'bg-slate-700 text-slate-300'
                            }`}
                          >
                            {u.role}
                          </span>
                        </td>
                        <td className="p-4">
                          <span
                            className={`px-2.5 py-1 rounded-full font-bold text-[10px] ${
                              u.status === 'ACTIVE'
                                ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30'
                                : 'bg-red-500/20 text-red-300 border border-red-500/30'
                            }`}
                          >
                            {u.status}
                          </span>
                        </td>
                        <td className="p-4 text-slate-400">{new Date(u.createdAt).toLocaleDateString()}</td>
                        <td className="p-4 text-right">
                          <button
                            onClick={() => handleToggleUserStatus(u)}
                            className={`px-3 py-1.5 rounded-lg font-semibold text-xs transition-colors ${
                              u.status === 'ACTIVE'
                                ? 'bg-red-950/60 hover:bg-red-900 text-red-300 border border-red-800'
                                : 'bg-emerald-950/60 hover:bg-emerald-900 text-emerald-300 border border-emerald-800'
                            }`}
                          >
                            {u.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
                          </button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* TAB 3: SECURITY AUDIT LOG */}
        {activeTab === 'audit' && (
          <div className="space-y-4">
            {/* Filter Bar */}
            <div className="bg-slate-800/80 border border-slate-700 p-4 rounded-2xl flex flex-wrap items-center justify-between gap-3">
              <div className="flex items-center space-x-3 flex-1">
                <ShieldCheck className="w-5 h-5 text-indigo-400" />
                <h2 className="text-sm font-bold text-white">Immutable Compliance Audit Logs</h2>
              </div>

              <div className="flex items-center space-x-2">
                <label className="text-xs text-slate-400 font-semibold">Action Filter:</label>
                <select
                  value={auditActionFilter}
                  onChange={(e) => setAuditActionFilter(e.target.value)}
                  className="bg-slate-900 border border-slate-700 text-slate-300 text-xs rounded-xl px-3 py-2 focus:outline-none focus:border-indigo-500"
                >
                  <option value="">All Actions</option>
                  <option value="DOCTOR_SUMMARY_VERIFIED">DOCTOR_SUMMARY_VERIFIED</option>
                  <option value="ABDM_REQUESTED">ABDM_REQUESTED</option>
                  <option value="HIS_CLINICAL_EXPORT">HIS_CLINICAL_EXPORT</option>
                  <option value="ADMIN_USER_CREATED">ADMIN_USER_CREATED</option>
                  <option value="CLINICAL_SUMMARY_GENERATED">CLINICAL_SUMMARY_GENERATED</option>
                </select>
              </div>
            </div>

            {/* Audit Log Table */}
            <div className="bg-slate-800/80 border border-slate-700 rounded-2xl overflow-hidden shadow-md">
              <table className="w-full text-left border-collapse text-xs">
                <thead>
                  <tr className="bg-slate-900/80 border-b border-slate-700 text-slate-400 uppercase tracking-wider font-semibold">
                    <th className="p-4">Timestamp</th>
                    <th className="p-4">Action</th>
                    <th className="p-4">Actor Email</th>
                    <th className="p-4">Resource Type</th>
                    <th className="p-4">Resource ID</th>
                    <th className="p-4 text-right">Metadata Details</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-700/60 text-slate-200 font-mono">
                  {auditLogs.length === 0 ? (
                    <tr>
                      <td colSpan={6} className="p-8 text-center text-slate-500 font-sans">
                        No audit events recorded for current filter.
                      </td>
                    </tr>
                  ) : (
                    auditLogs.map((log) => (
                      <tr key={log.id} className="hover:bg-slate-700/30 transition-colors">
                        <td className="p-4 text-slate-400">{new Date(log.createdAt).toLocaleString()}</td>
                        <td className="p-4 font-bold text-indigo-300">{log.action}</td>
                        <td className="p-4 text-slate-300">{log.userEmail || 'System / Kiosk'}</td>
                        <td className="p-4 text-slate-400">{log.entityType || '—'}</td>
                        <td className="p-4 text-slate-400 truncate max-w-[140px]">{log.entityId || '—'}</td>
                        <td className="p-4 text-right font-sans">
                          <button
                            onClick={() => setSelectedAuditLog(log)}
                            className="px-2.5 py-1 bg-slate-700 hover:bg-slate-600 text-slate-200 rounded text-[11px] transition-colors"
                          >
                            Inspect JSON
                          </button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* TAB 4: SYSTEM CONFIG & STATUS */}
        {activeTab === 'config' && (
          <div className="space-y-6">
            <div className="bg-slate-800/80 border border-slate-700 rounded-2xl p-6 shadow-md space-y-4">
              <div className="flex items-center justify-between border-b border-slate-700 pb-3">
                <h2 className="text-base font-bold text-white flex items-center space-x-2">
                  <Server className="w-5 h-5 text-indigo-400" />
                  <span>Platform System Governance & Adapter Modes</span>
                </h2>
                <span className="px-3 py-1 bg-emerald-500/20 text-emerald-300 font-bold rounded-full text-xs border border-emerald-500/30">
                  {config?.environment || 'PRODUCTION_READY'}
                </span>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                <div className="p-4 bg-slate-900 border border-slate-700 rounded-xl space-y-1">
                  <p className="text-xs text-slate-400 font-medium">AI Structuring Gateway</p>
                  <p className="text-sm font-bold text-white">{config?.aiProviderMode}</p>
                  <p className="text-[11px] text-emerald-400">Offline Fallback Guarded</p>
                </div>

                <div className="p-4 bg-slate-900 border border-slate-700 rounded-xl space-y-1">
                  <p className="text-xs text-slate-400 font-medium">ABDM Adapter Boundary</p>
                  <p className="text-sm font-bold text-white">{config?.abdmOperatingMode} MODE</p>
                  <p className="text-[11px] text-indigo-400">Deterministic Mock Active</p>
                </div>

                <div className="p-4 bg-slate-900 border border-slate-700 rounded-xl space-y-1">
                  <p className="text-xs text-slate-400 font-medium">HIS Adapter Boundary</p>
                  <p className="text-sm font-bold text-white">{config?.hisOperatingMode} MODE</p>
                  <p className="text-[11px] text-indigo-400">MPI & Queues Connected</p>
                </div>

                <div className="p-4 bg-slate-900 border border-slate-700 rounded-xl space-y-1">
                  <p className="text-xs text-slate-400 font-medium">AWS HealthLake Cloud</p>
                  <p className="text-sm font-bold text-white">{config?.healthLakeOperatingMode} MODE</p>
                  <p className="text-[11px] text-indigo-400">Cloud FHIR Datastore Guarded</p>
                </div>
              </div>
            </div>

            {/* Feature Toggles Overview */}
            <div className="bg-slate-800/80 border border-slate-700 rounded-2xl p-6 shadow-md space-y-4">
              <h3 className="text-sm font-bold text-white flex items-center space-x-2 border-b border-slate-700 pb-3">
                <Lock className="w-4 h-4 text-indigo-400" />
                <span>Active Safety & Feature Invariants</span>
              </h3>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-3 text-xs">
                {config?.featureToggles &&
                  Object.entries(config.featureToggles).map(([toggle, enabled]) => (
                    <div key={toggle} className="p-3 bg-slate-900 border border-slate-700/70 rounded-xl flex items-center justify-between">
                      <span className="font-semibold text-slate-300 font-mono">{toggle}</span>
                      <span className="px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-300 font-bold text-[10px]">
                        {enabled ? 'ACTIVE' : 'DISABLED'}
                      </span>
                    </div>
                  ))}
              </div>
            </div>
          </div>
        )}
      </main>

      {/* MODAL 1: ADD CLINICIAN / STAFF */}
      {isAddUserOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-800 border border-slate-700 rounded-2xl max-w-md w-full p-6 shadow-2xl space-y-4">
            <h3 className="text-base font-bold text-white border-b border-slate-700 pb-3">
              Register New Clinician or Administrator
            </h3>

            <form onSubmit={handleCreateUser} className="space-y-4 text-xs">
              <div>
                <label className="block text-slate-300 font-semibold mb-1">Role Type</label>
                <select
                  value={newRole}
                  onChange={(e) => setNewRole(e.target.value as 'DOCTOR' | 'ADMIN')}
                  className="w-full bg-slate-900 border border-slate-700 rounded-xl px-3 py-2 text-white focus:outline-none focus:border-indigo-500"
                >
                  <option value="DOCTOR">Doctor / Clinician (ROLE_DOCTOR)</option>
                  <option value="ADMIN">System Administrator (ROLE_ADMIN)</option>
                </select>
              </div>

              <div>
                <label className="block text-slate-300 font-semibold mb-1">Email Address</label>
                <input
                  type="email"
                  required
                  value={newEmail}
                  onChange={(e) => setNewEmail(e.target.value)}
                  placeholder="doctor@hospital.org"
                  className="w-full bg-slate-900 border border-slate-700 rounded-xl px-3 py-2 text-white focus:outline-none focus:border-indigo-500"
                />
              </div>

              <div>
                <label className="block text-slate-300 font-semibold mb-1">Initial Password</label>
                <input
                  type="password"
                  required
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="Minimum 8 characters"
                  className="w-full bg-slate-900 border border-slate-700 rounded-xl px-3 py-2 text-white focus:outline-none focus:border-indigo-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-300 font-semibold mb-1">First Name</label>
                  <input
                    type="text"
                    required
                    value={newFirstName}
                    onChange={(e) => setNewFirstName(e.target.value)}
                    placeholder="Arun"
                    className="w-full bg-slate-900 border border-slate-700 rounded-xl px-3 py-2 text-white focus:outline-none focus:border-indigo-500"
                  />
                </div>

                <div>
                  <label className="block text-slate-300 font-semibold mb-1">Last Name</label>
                  <input
                    type="text"
                    required
                    value={newLastName}
                    onChange={(e) => setNewLastName(e.target.value)}
                    placeholder="Sharma"
                    className="w-full bg-slate-900 border border-slate-700 rounded-xl px-3 py-2 text-white focus:outline-none focus:border-indigo-500"
                  />
                </div>
              </div>

              <div className="flex items-center justify-end space-x-3 pt-2">
                <button
                  type="button"
                  onClick={() => setIsAddUserOpen(false)}
                  className="px-4 py-2 bg-slate-700 hover:bg-slate-600 text-slate-300 rounded-xl font-semibold transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl font-bold transition-colors shadow-md"
                >
                  Create Account
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL 2: AUDIT LOG METADATA INSPECTOR */}
      {selectedAuditLog && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-800 border border-slate-700 rounded-2xl max-w-xl w-full p-6 shadow-2xl space-y-4">
            <div className="flex items-center justify-between border-b border-slate-700 pb-3">
              <h3 className="text-base font-bold text-white">Security Audit Log Metadata</h3>
              <button onClick={() => setSelectedAuditLog(null)} className="text-slate-400 hover:text-white font-bold">
                ✕
              </button>
            </div>

            <div className="space-y-2 text-xs">
              <p className="text-slate-300">
                <strong>Action:</strong> <span className="font-mono text-indigo-400">{selectedAuditLog.action}</span>
              </p>
              <p className="text-slate-300">
                <strong>User Email:</strong> <span className="font-mono">{selectedAuditLog.userEmail || 'System'}</span>
              </p>
              <p className="text-slate-300">
                <strong>Timestamp:</strong> {new Date(selectedAuditLog.createdAt).toLocaleString()}
              </p>
            </div>

            <div className="p-4 bg-slate-900 rounded-xl border border-slate-700/80 font-mono text-xs text-slate-200 overflow-x-auto max-h-60">
              <pre>{JSON.stringify(JSON.parse(selectedAuditLog.metadata || '{}'), null, 2)}</pre>
            </div>

            <div className="text-right">
              <button
                onClick={() => setSelectedAuditLog(null)}
                className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl font-semibold text-xs transition-colors"
              >
                Close Inspector
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
