import React from 'react';
import { Layers, ShieldCheck, Database, Cpu, Network } from 'lucide-react';

export const ArchitectureCard: React.FC = () => {
  const tiers = [
    {
      title: 'Frontend Tier',
      desc: 'Touch & Voice Kiosk UI (React 18 + TypeScript + Tailwind)',
      icon: Layers,
      color: 'text-cyan-400',
      badge: 'Client',
    },
    {
      title: 'API & Security Gateway',
      desc: 'Stateless REST endpoints & Spring Security foundation',
      icon: ShieldCheck,
      color: 'text-emerald-400',
      badge: 'Spring Boot',
    },
    {
      title: 'Clinical Services',
      desc: 'Rule-Based Triage, Ayurveda Intake & AI Structuring boundaries',
      icon: Cpu,
      color: 'text-indigo-400',
      badge: 'Core Services',
    },
    {
      title: 'Persistence Layer',
      desc: 'MySQL 8.x Relational Storage & Flyway Database Migrations',
      icon: Database,
      color: 'text-blue-400',
      badge: 'MySQL 8.x',
    },
    {
      title: 'Interoperability & Adapters',
      desc: 'FHIR R4 Mapper & external boundaries: ABDM, HIS, AWS HealthLake',
      icon: Network,
      color: 'text-purple-400',
      badge: 'FHIR R4',
    },
  ];

  return (
    <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-6 shadow-xl backdrop-blur-sm">
      <div className="flex items-center justify-between pb-4 border-b border-slate-800">
        <div>
          <h3 className="text-base font-bold text-slate-100 flex items-center gap-2">
            <Network className="w-5 h-5 text-cyan-400" />
            System Architecture & Module Boundaries
          </h3>
          <p className="text-xs text-slate-400 mt-0.5">
            Strict separation of concerns — No clinical bypasses or direct client-to-DB calls
          </p>
        </div>
        <span className="text-xs font-mono px-2.5 py-1 bg-cyan-950/60 border border-cyan-800/40 text-cyan-300 rounded-md">
          Phase 1 Foundation
        </span>
      </div>

      <div className="mt-4 grid grid-cols-1 md:grid-cols-5 gap-3">
        {tiers.map((tier, idx) => {
          const Icon = tier.icon;
          return (
            <div
              key={idx}
              className="flex flex-col justify-between p-3.5 bg-slate-950/60 border border-slate-800/80 rounded-xl hover:border-slate-700 transition-all"
            >
              <div>
                <div className="flex items-center justify-between mb-2">
                  <Icon className={`w-5 h-5 ${tier.color}`} />
                  <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-slate-800/80 text-slate-300">
                    {tier.badge}
                  </span>
                </div>
                <h4 className="text-xs font-bold text-slate-200">{tier.title}</h4>
                <p className="text-[11px] text-slate-400 mt-1 leading-relaxed">{tier.desc}</p>
              </div>
              <div className="mt-3 text-[10px] text-slate-500 font-mono">
                Step 0{idx + 1}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
