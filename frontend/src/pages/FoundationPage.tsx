import React from 'react';
import { useBackendHealth } from '../hooks/useBackendHealth';
import { StatusBadge } from '../components/StatusBadge';
import { ArchitectureCard } from '../components/ArchitectureCard';
import { APP_NAME, APP_TAGLINE, APP_PHASE } from '../constants';
import { Activity, RefreshCw, Server, Terminal, FileText, CheckCircle } from 'lucide-react';

export const FoundationPage: React.FC = () => {
  const {
    connectionState,
    healthData,
    errorMessage,
    lastChecked,
    isRefreshing,
    refresh
  } = useBackendHealth();

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col justify-between">
      {/* Top Navigation / Brand Header */}
      <header className="border-b border-slate-800/80 bg-slate-900/50 backdrop-blur-md sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-cyan-600 to-emerald-500 flex items-center justify-center shadow-lg shadow-cyan-500/20">
              <Activity className="w-5 h-5 text-white" />
            </div>
            <div>
              <span className="text-lg font-extrabold tracking-tight bg-gradient-to-r from-white via-slate-100 to-slate-400 bg-clip-text text-transparent">
                {APP_NAME}
              </span>
              <span className="hidden sm:inline-block ml-2 text-xs text-slate-400 font-medium">
                • {APP_TAGLINE}
              </span>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="hidden md:flex items-center gap-2 text-xs font-mono text-slate-400 bg-slate-900 px-3 py-1.5 rounded-lg border border-slate-800">
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
              {APP_PHASE}
            </div>
            <StatusBadge state={connectionState} />
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 flex-1 w-full space-y-8">
        {/* Hero Section */}
        <div className="relative rounded-3xl bg-gradient-to-b from-slate-900 to-slate-950 border border-slate-800/80 p-6 sm:p-10 overflow-hidden shadow-2xl">
          <div className="absolute top-0 right-0 -mt-8 -mr-8 w-80 h-80 bg-cyan-500/10 rounded-full blur-3xl pointer-events-none" />
          <div className="absolute bottom-0 left-0 -mb-8 -ml-8 w-80 h-80 bg-emerald-500/10 rounded-full blur-3xl pointer-events-none" />

          <div className="relative z-10 max-w-3xl">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 mb-4">
              <Terminal className="w-3.5 h-3.5" />
              Production Ready: Full End-to-End Clinical Workflow Complete
            </div>
            <h1 className="text-3xl sm:text-4xl font-black text-white tracking-tight">
              AI-Assisted Intake, Clinician-Verified Care
            </h1>
            <p className="mt-3 text-sm sm:text-base text-slate-300 leading-relaxed">
              MediKiosk is an intelligent clinical pre-consultation intake kiosk. It collects, structures, and triages patient symptoms and history prior to doctor consultation. All clinical outputs require final doctor verification.
            </p>
          </div>
        </div>

        {/* Live Connectivity Card */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 bg-slate-900/80 border border-slate-800 rounded-2xl p-6 shadow-xl backdrop-blur-sm flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between pb-4 border-b border-slate-800">
                <div className="flex items-center gap-2.5">
                  <Server className="w-5 h-5 text-cyan-400" />
                  <h2 className="text-base font-bold text-white">Backend Connectivity Status</h2>
                </div>
                <button
                  onClick={refresh}
                  disabled={isRefreshing}
                  className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition disabled:opacity-50"
                  title="Ping /api/health endpoint"
                >
                  <RefreshCw className={`w-3.5 h-3.5 ${isRefreshing ? 'animate-spin' : ''}`} />
                  Ping Backend
                </button>
              </div>

              <div className="mt-6 grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div className="p-4 rounded-xl bg-slate-950 border border-slate-800/80">
                  <span className="text-xs text-slate-400 font-medium">Backend Status</span>
                  <div className="mt-2">
                    <StatusBadge state={connectionState} />
                  </div>
                </div>

                <div className="p-4 rounded-xl bg-slate-950 border border-slate-800/80">
                  <span className="text-xs text-slate-400 font-medium">Verified Endpoint</span>
                  <div className="mt-2 font-mono text-sm text-cyan-400 font-semibold truncate">
                    GET /api/health
                  </div>
                </div>

                <div className="p-4 rounded-xl bg-slate-950 border border-slate-800/80">
                  <span className="text-xs text-slate-400 font-medium">Last Checked</span>
                  <div className="mt-2 font-mono text-xs text-slate-300">
                    {lastChecked ? lastChecked.toLocaleTimeString() : 'Pending'}
                  </div>
                </div>
              </div>

              {/* Endpoint response details */}
              <div className="mt-6">
                <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
                  Live Response Payload
                </span>
                <div className="mt-2 p-4 rounded-xl bg-slate-950 border border-slate-800/80 font-mono text-xs">
                  {connectionState === 'connected' && healthData ? (
                    <pre className="text-emerald-400 overflow-x-auto">
                      {JSON.stringify(healthData, null, 2)}
                    </pre>
                  ) : connectionState === 'checking' ? (
                    <p className="text-amber-400 animate-pulse">Requesting /api/health from Spring Boot...</p>
                  ) : (
                    <div className="text-rose-400 space-y-1">
                      <p>Backend is currently unreachable at http://localhost:8080</p>
                      {errorMessage && <p className="text-slate-500 text-[11px]">{errorMessage}</p>}
                    </div>
                  )}
                </div>
              </div>
            </div>

            <div className="mt-4 pt-4 border-t border-slate-800/60 flex items-center justify-between text-xs text-slate-500">
              <span>CORS: Enabled for http://localhost:5173</span>
              <span>Stateless REST &bull; No Credentials in React</span>
            </div>
          </div>

          {/* Foundation Scope Card */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-6 shadow-xl backdrop-blur-sm flex flex-col justify-between">
            <div>
              <h3 className="text-base font-bold text-white flex items-center gap-2">
                <CheckCircle className="w-5 h-5 text-emerald-400" />
                Phase 1 Scope & Guardrails
              </h3>
              <p className="text-xs text-slate-400 mt-1">
                Strict adherence to architectural requirements
              </p>

              <ul className="mt-4 space-y-3 text-xs text-slate-300">
                <li className="flex items-start gap-2">
                  <span className="text-emerald-400 font-bold">&#10003;</span>
                  <span><strong>Spring Boot 3.4 + Java 21</strong> with modular package layout</span>
                </li>
                <li className="flex items-start gap-2">
                  <span className="text-emerald-400 font-bold">&#10003;</span>
                  <span><strong>MySQL 8.x + Flyway</strong> migrations (V1 initial setup verified)</span>
                </li>
                <li className="flex items-start gap-2">
                  <span className="text-emerald-400 font-bold">&#10003;</span>
                  <span><strong>Spring Security foundation</strong> with public /api/health</span>
                </li>
                <li className="flex items-start gap-2">
                  <span className="text-emerald-400 font-bold">&#10003;</span>
                  <span><strong>FHIR R4 & Adapter interfaces</strong> (ABDM, HIS, HealthLake)</span>
                </li>
                <li className="flex items-start gap-2">
                  <span className="text-emerald-400 font-bold">&#10003;</span>
                  <span><strong>Zero Fake Data:</strong> No placeholder patients or synthetic records</span>
                </li>
                <li className="flex items-start gap-2">
                  <span className="text-emerald-400 font-bold">&#10003;</span>
                  <span><strong>Safe Secret Handling:</strong> .gitignore & .env.example template</span>
                </li>
              </ul>
            </div>

            <div className="mt-6 pt-4 border-t border-slate-800/80 flex items-center gap-2 text-[11px] text-emerald-300/90 bg-emerald-500/10 px-3 py-2 rounded-lg border border-emerald-500/20">
              <CheckCircle className="w-4 h-4 text-emerald-400 shrink-0" />
              <span>Full patient intake, triage, document intelligence, and doctor verification modules are active and production-ready.</span>
            </div>
          </div>
        </div>

        {/* System Architecture Diagram Card */}
        <ArchitectureCard />
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-800/80 bg-slate-950 py-4 text-center text-xs text-slate-500">
        <div className="max-w-7xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-2">
          <span>MediKiosk &bull; Clinical Intake & Triage Foundation</span>
          <div className="flex items-center gap-4 text-slate-400">
            <span className="flex items-center gap-1">
              <FileText className="w-3.5 h-3.5" /> docs/
            </span>
            <span>MySQL 8.x</span>
            <span>React + Vite</span>
          </div>
        </div>
      </footer>
    </div>
  );
};
