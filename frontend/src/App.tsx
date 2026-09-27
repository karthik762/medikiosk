import React, { useState, useEffect } from 'react';
import { FoundationPage } from './pages/FoundationPage';
import { DoctorDashboard } from './components/doctor/DoctorDashboard';
import { ClinicalReviewWorkstation } from './components/doctor/ClinicalReviewWorkstation';
import { AdminWorkstation } from './components/admin/AdminWorkstation';
import { PatientKiosk } from './components/patient/PatientKiosk';
import { API_BASE_URL } from './constants';
import { Shield, Key, LogOut, AlertCircle, Sparkles } from 'lucide-react';

interface AuthDoctor {
  email: string;
  firstName: string;
  lastName: string;
  role: string;
}

import ConstellationField from './components/ui/constellation-field';

export const App: React.FC = () => {
  const [activePortal, setActivePortal] = useState<'PATIENT' | 'DOCTOR' | 'ADMIN' | 'FOUNDATION'>('PATIENT');
  const [selectedSessionId, setSelectedSessionId] = useState<string | null>(null);

  // Clinician Authentication State
  const [authToken, setAuthToken] = useState<string | null>(() => {
    const saved = sessionStorage.getItem('medikiosk_doctor_token');
    return saved && saved !== 'undefined' && saved !== 'null' ? saved : null;
  });
  const [doctorUser, setDoctorUser] = useState<AuthDoctor | null>(() => {
    const saved = sessionStorage.getItem('medikiosk_doctor_user');
    try {
      return saved && saved !== 'undefined' && saved !== 'null' ? JSON.parse(saved) : null;
    } catch {
      return null;
    }
  });
  const [loginEmail, setLoginEmail] = useState('');
  const [loginPassword, setLoginPassword] = useState('');
  const [isLoggingIn, setIsLoggingIn] = useState(false);
  const [loginError, setLoginError] = useState<string | null>(null);
  const [isDemoEnabled, setIsDemoEnabled] = useState(false);

  // Probe backend demo status safely on mount
  useEffect(() => {
    fetch(`${API_BASE_URL}/api/demo/status`)
      .then((res) => (res.ok ? res.json() : null))
      .then((data) => {
        if (data?.success && data?.data?.demoEnabled === true) {
          setIsDemoEnabled(true);
        } else {
          setIsDemoEnabled(false);
        }
      })
      .catch(() => setIsDemoEnabled(false));
  }, []);

  const handleLogin = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!loginEmail.trim() || !loginPassword) {
      setLoginError('Please enter both clinician email and password');
      return;
    }

    setIsLoggingIn(true);
    setLoginError(null);

    try {
      const res = await fetch(`${API_BASE_URL}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: loginEmail.trim(), password: loginPassword }),
      });

      let data: any;
      try {
        data = await res.json();
      } catch {
        const text = await res.text().catch(() => '');
        throw new Error(text || `Authentication failed (HTTP ${res.status})`);
      }

      if (!res.ok || !data.success) {
        throw new Error(data.message || `Authentication failed (HTTP ${res.status})`);
      }

      const token = data.data.accessToken || data.data.token;
      if (!token) {
        throw new Error('Authentication token not received');
      }

      const user: AuthDoctor = {
        email: data.data.email,
        firstName: data.data.firstName,
        lastName: data.data.lastName,
        role: data.data.role,
      };

      setAuthToken(token);
      setDoctorUser(user);
      setActivePortal('DOCTOR');
      sessionStorage.setItem('medikiosk_doctor_token', token);
      sessionStorage.setItem('medikiosk_doctor_user', JSON.stringify(user));
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Login failed';
      setLoginError(msg);
    } finally {
      setIsLoggingIn(false);
    }
  };

  const handleDemoLogin = async () => {
    setIsLoggingIn(true);
    setLoginError(null);

    try {
      const res = await fetch(`${API_BASE_URL}/api/demo/doctor-login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
      });

      if (res.status === 403) {
        throw new Error('Demo Mode is disabled in backend. Enable with DEMO_ENABLED=true or profile local.');
      }

      let data: any;
      try {
        data = await res.json();
      } catch {
        const text = await res.text().catch(() => '');
        throw new Error(text || `Demo login failed (HTTP ${res.status})`);
      }

      if (!res.ok || !data.success) {
        throw new Error(data.message || `Demo authentication failed (HTTP ${res.status})`);
      }

      const token = data.data.accessToken || data.data.token;
      if (!token) {
        throw new Error('Authentication token not received');
      }

      const user: AuthDoctor = {
        email: data.data.email,
        firstName: data.data.firstName,
        lastName: data.data.lastName,
        role: data.data.role,
      };

      setAuthToken(token);
      setDoctorUser(user);
      setActivePortal('DOCTOR');
      sessionStorage.setItem('medikiosk_doctor_token', token);
      sessionStorage.setItem('medikiosk_doctor_user', JSON.stringify(user));
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Demo login failed';
      setLoginError(msg);
    } finally {
      setIsLoggingIn(false);
    }
  };

  const handleLogout = () => {
    setAuthToken(null);
    setDoctorUser(null);
    setSelectedSessionId(null);
    sessionStorage.removeItem('medikiosk_doctor_token');
    sessionStorage.removeItem('medikiosk_doctor_user');
  };

  return (
    <div className="relative min-h-screen bg-transparent flex flex-col text-slate-100 overflow-hidden font-sans">
      {/* Global Animated Constellation Canvas Background */}
      <ConstellationField className="fixed inset-0 w-full h-full pointer-events-none z-0" />

      {/* Main Application Container */}
      <div className="relative z-10 flex flex-col flex-1">
        {/* Global Portal Switcher Navigation */}
        <nav className="bg-slate-900/80 backdrop-blur-md border-b border-slate-800/80 px-4 py-2 flex flex-wrap items-center justify-between text-xs text-slate-300 gap-2">
        <div className="flex items-center space-x-2">
          <span className="font-bold text-slate-100 flex items-center gap-1.5 mr-1">
            <span className="w-2 h-2 rounded-full bg-indigo-500 animate-pulse"></span>
            MediKiosk:
          </span>
          {isDemoEnabled && (
            <span className="px-2 py-0.5 rounded text-[10px] font-mono font-bold bg-amber-500/20 text-amber-300 border border-amber-500/40">
              DEMO_MODE=true
            </span>
          )}
          <button
            onClick={() => {
              setActivePortal('PATIENT');
              setSelectedSessionId(null);
            }}
            className={`px-3 py-1.5 rounded-lg font-medium transition-colors ${
              activePortal === 'PATIENT'
                ? 'bg-indigo-600 text-white shadow-sm font-semibold'
                : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
            }`}
          >
            🏥 Patient Kiosk
          </button>
          <button
            onClick={() => setActivePortal('DOCTOR')}
            className={`px-3 py-1.5 rounded-lg font-medium transition-colors ${
              activePortal === 'DOCTOR'
                ? 'bg-indigo-600 text-white shadow-sm font-semibold'
                : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
            }`}
          >
            👨‍⚕️ Clinician Review Console
          </button>
          <button
            onClick={() => setActivePortal('ADMIN')}
            className={`px-3 py-1.5 rounded-lg font-medium transition-colors ${
              activePortal === 'ADMIN'
                ? 'bg-purple-600 text-white shadow-sm font-semibold'
                : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
            }`}
          >
            🛡️ Admin Workstation
          </button>
          <button
            onClick={() => {
              setActivePortal('FOUNDATION');
              setSelectedSessionId(null);
            }}
            className={`px-3 py-1.5 rounded-lg font-medium transition-colors ${
              activePortal === 'FOUNDATION'
                ? 'bg-cyan-700 text-white shadow-sm font-semibold'
                : 'bg-slate-800 text-slate-400 hover:bg-slate-700'
            }`}
          >
            ⚙️ Dev Sandbox
          </button>
          <button
            onClick={() => {
              sessionStorage.clear();
              setSelectedSessionId(null);
              setAuthToken(null);
              setDoctorUser(null);
              setActivePortal('PATIENT');
              window.location.reload();
            }}
            className="px-2.5 py-1 rounded-lg bg-rose-900/60 hover:bg-rose-800 text-rose-200 border border-rose-700/60 font-semibold text-xs flex items-center gap-1 transition ml-2"
            title="Developer/Demo-only: Reset demo state for presentation replay"
          >
            <span>🔄</span>
            <span>RESET DEMO</span>
          </button>
        </div>

        {/* Clinician Authentication Status & Actions */}
        <div className="flex items-center space-x-3">
          {activePortal === 'DOCTOR' && (
            authToken && doctorUser ? (
              <div className="flex items-center space-x-2 bg-slate-800/80 px-3 py-1 rounded-lg border border-slate-700">
                <span className="w-2 h-2 rounded-full bg-emerald-400"></span>
                <span className="text-slate-200 font-medium">
                  Dr. {doctorUser.firstName} {doctorUser.lastName} ({doctorUser.role})
                </span>
                <button
                  onClick={handleLogout}
                  className="ml-2 text-slate-400 hover:text-rose-400 flex items-center gap-1 transition"
                  title="Sign Out"
                >
                  <LogOut className="w-3.5 h-3.5" />
                  <span>Log out</span>
                </button>
              </div>
            ) : (
              <div className="flex items-center space-x-2">
                <span className="text-amber-400 text-[11px] font-mono">Clinician Auth Required</span>
                {isDemoEnabled && (
                  <button
                    onClick={handleDemoLogin}
                    disabled={isLoggingIn}
                    className="px-2.5 py-1 rounded bg-indigo-600 hover:bg-indigo-500 text-white font-medium text-[11px] flex items-center gap-1 transition disabled:opacity-50"
                    title="Quick login with demonstration doctor account"
                  >
                    <Sparkles className="w-3 h-3" />
                    <span>{isLoggingIn ? 'Authenticating...' : '1-Click Demo Doctor Login'}</span>
                  </button>
                )}
              </div>
            )
          )}

          <div className="text-slate-400 font-mono text-[11px] hidden sm:block">
            {activePortal === 'DOCTOR'
              ? selectedSessionId
                ? 'Review Workstation'
                : 'Consultation Queue'
              : activePortal === 'PATIENT'
              ? 'Patient Intake Kiosk'
              : 'Integration Architecture Sandbox'}
          </div>
        </div>
      </nav>

      {/* Main Portal View */}
      <div className="flex-1">
        {activePortal === 'PATIENT' ? (
          <PatientKiosk onSwitchToClinician={() => setActivePortal('DOCTOR')} />
        ) : activePortal === 'FOUNDATION' ? (
          <FoundationPage />
        ) : !authToken ? (
          /* Clinician Login Workstation Banner */
          <div className="max-w-md mx-auto my-12 p-6 bg-slate-900 border border-slate-800 rounded-2xl text-slate-100 shadow-2xl">
            <div className="flex items-center gap-3 mb-4">
              <div className="w-10 h-10 rounded-xl bg-indigo-600/20 text-indigo-400 flex items-center justify-center border border-indigo-500/30">
                <Shield className="w-5 h-5" />
              </div>
              <div>
                <h2 className="text-lg font-bold">Clinician Authentication Gate</h2>
                <p className="text-xs text-slate-400">Doctor & Admin credentials required for clinical sign-off</p>
              </div>
            </div>

            <div className="p-3 bg-slate-950 border border-slate-800 rounded-lg text-xs text-slate-300 mb-5 leading-relaxed">
              In accordance with MediKiosk clinical governance rules, consultation queues, patient review details, amendments, and integration exports (ABDM, HIS, HealthLake) require a verified physician identity.
            </div>

            {loginError && (
              <div className="p-3 bg-rose-950/60 border border-rose-800 rounded-lg text-xs text-rose-300 flex items-start gap-2 mb-4">
                <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
                <span>{loginError}</span>
              </div>
            )}

            <form onSubmit={handleLogin} className="space-y-3.5">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Clinician Email</label>
                <input
                  type="email"
                  value={loginEmail}
                  onChange={(e) => setLoginEmail(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-lg text-xs text-white focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  placeholder="doctor@medikiosk.org"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Password</label>
                <input
                  type="password"
                  value={loginPassword}
                  onChange={(e) => setLoginPassword(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-lg text-xs text-white focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  placeholder="••••••••"
                  required
                />
              </div>

              <div className="pt-2 flex flex-col gap-2">
                <button
                  type="submit"
                  disabled={isLoggingIn}
                  className="w-full py-2 px-4 bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg text-xs font-semibold transition flex items-center justify-center gap-2 disabled:opacity-50"
                >
                  <Key className="w-3.5 h-3.5" />
                  <span>{isLoggingIn ? 'Verifying Credentials...' : 'Sign In as Clinician'}</span>
                </button>

                {isDemoEnabled && (
                  <button
                    type="button"
                    onClick={handleDemoLogin}
                    disabled={isLoggingIn}
                    className="w-full py-2 px-4 bg-slate-800 hover:bg-slate-700 text-cyan-300 border border-slate-700 rounded-lg text-xs font-medium transition flex items-center justify-center gap-2"
                  >
                    <Sparkles className="w-3.5 h-3.5 text-cyan-400" />
                    <span>1-Click Demo Doctor Login (Demo Mode Active)</span>
                  </button>
                )}
              </div>
            </form>
          </div>
        ) : activePortal === 'ADMIN' ? (
          <AdminWorkstation authToken={authToken} />
        ) : selectedSessionId ? (
          <div className="bg-transparent min-h-full py-4">
            <ClinicalReviewWorkstation
              sessionId={selectedSessionId}
              authToken={authToken}
              onBackToQueue={() => setSelectedSessionId(null)}
            />
          </div>
        ) : (
          <div className="bg-transparent min-h-full py-4">
            <DoctorDashboard
              authToken={authToken}
              onSelectCase={(id) => setSelectedSessionId(id)}
            />
          </div>
        )}
      </div>
    </div>
  </div>
);
};

export default App;

