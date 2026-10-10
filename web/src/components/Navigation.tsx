import React from 'react';
import { 
  LayoutDashboard, 
  Building2, 
  HardHat, 
  GraduationCap, 
  Sparkles, 
  FileSpreadsheet, 
  AlertTriangle,
  Settings
} from 'lucide-react';

interface NavigationProps {
  currentTab: string;
  onSelectTab: (tab: string) => void;
  lang: 'am' | 'en';
  warningCount: number;
}

export const Navigation: React.FC<NavigationProps> = ({
  currentTab,
  onSelectTab,
  lang,
  warningCount
}) => {
  const navItems = [
    {
      id: 'dashboard',
      labelAm: 'ዋና ዳሽቦርድ',
      labelEn: 'Dashboard',
      icon: LayoutDashboard,
    },
    {
      id: 'projects',
      labelAm: 'ግንባታዎች',
      labelEn: 'Projects',
      icon: Building2,
    },
    {
      id: 'artisans',
      labelAm: 'የልምድ ሰራተኞች',
      labelEn: 'Artisans',
      icon: HardHat,
    },
    {
      id: 'professionals',
      labelAm: 'ባለሙያዎች',
      labelEn: 'Professionals',
      icon: GraduationCap,
    },
    {
      id: 'ai',
      labelAm: 'Gemini AI & ድረ-ገጽ',
      labelEn: 'Gemini AI & Search',
      icon: Sparkles,
      highlight: true,
    },
    {
      id: 'reports',
      labelAm: 'ሪፖርቶችና ኤክስፖርት',
      labelEn: 'Reports & Export',
      icon: FileSpreadsheet,
    },
    {
      id: 'warnings',
      labelAm: 'ማስጠንቀቂያዎች',
      labelEn: 'Warnings',
      icon: AlertTriangle,
      badge: warningCount > 0 ? warningCount : undefined,
    }
  ];

  return (
    <>
      {/* DESKTOP & TABLET SIDEBAR (Hidden on small mobile screens) */}
      <aside className="hidden md:flex flex-col w-64 bg-slate-900 border-r border-slate-800 p-4 shrink-0 h-screen sticky top-0 overflow-y-auto">
        {/* Brand Header */}
        <div className="flex items-center gap-3 px-2 py-4 mb-4 border-b border-slate-800">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-ethiopia-green via-ethiopia-yellow to-accent-teal flex items-center justify-center shadow-lg shadow-ethiopia-green/20">
            <Building2 className="w-6 h-6 text-slate-950" />
          </div>
          <div>
            <h1 className="font-bold text-sm text-slate-100 leading-tight">
              {lang === 'am' ? 'የቲሊሊ ከተማ አስተዳደር' : 'Tilli Municipality'}
            </h1>
            <p className="text-xs text-ethiopia-yellow font-medium">
              {lang === 'am' ? 'ኮንስትራክሽን ዲጂታል መድረክ' : 'Construction Platform'}
            </p>
          </div>
        </div>

        {/* Navigation Items */}
        <nav className="flex-1 space-y-1">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = currentTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => onSelectTab(item.id)}
                className={`w-full flex items-center justify-between px-3 py-2.5 rounded-xl text-sm font-medium transition-all ${
                  isActive
                    ? 'bg-ethiopia-green text-white shadow-md shadow-ethiopia-green/25 font-semibold'
                    : item.highlight
                    ? 'text-ethiopia-yellow hover:bg-slate-800/80 bg-ethiopia-yellow/10 border border-ethiopia-yellow/30'
                    : 'text-slate-300 hover:bg-slate-800 hover:text-white'
                }`}
              >
                <div className="flex items-center gap-3">
                  <Icon className={`w-5 h-5 ${isActive ? 'text-white' : item.highlight ? 'text-ethiopia-yellow' : 'text-slate-400'}`} />
                  <span>{lang === 'am' ? item.labelAm : item.labelEn}</span>
                </div>
                {item.badge && (
                  <span className="bg-ethiopia-red text-white text-xs px-2 py-0.5 rounded-full font-bold">
                    {item.badge}
                  </span>
                )}
              </button>
            );
          })}
        </nav>

        {/* Sidebar Footer Status */}
        <div className="pt-4 border-t border-slate-800 mt-auto">
          <div className="bg-slate-850 p-3 rounded-xl border border-slate-800 text-xs">
            <div className="flex items-center gap-2 text-slate-300 font-medium mb-1">
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
              <span>{lang === 'am' ? 'የቁጥጥር ሲስተም ንቁ ነው' : 'System Active'}</span>
            </div>
            <p className="text-slate-400 text-[11px] leading-relaxed">
              {lang === 'am' ? 'EBCS-1 እስከ EBCS-14 እና አዋጅ 721/2004' : 'EBCS 1-14 & Proclamation 721'}
            </p>
          </div>
        </div>
      </aside>

      {/* MOBILE BOTTOM NAVIGATION BAR (Visible only on mobile screens < 768px) */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 z-50 bg-slate-900/95 backdrop-blur-md border-t border-slate-800 px-1 py-1 flex items-center justify-between overflow-x-auto shadow-2xl no-scrollbar">
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive = currentTab === item.id;
          return (
            <button
              key={item.id}
              onClick={() => onSelectTab(item.id)}
              className={`flex flex-col items-center justify-center py-1.5 px-2 rounded-lg transition-colors relative shrink-0 min-w-[50px] ${
                isActive ? 'text-ethiopia-yellow font-bold bg-slate-800/60' : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              <Icon className={`w-4 h-4 ${isActive ? 'scale-110 text-ethiopia-yellow' : ''}`} />
              <span className="text-[9px] mt-1 leading-none whitespace-nowrap">
                {lang === 'am' ? item.labelAm.split(' ')[0] : item.labelEn.split(' ')[0]}
              </span>
              {item.badge && (
                <span className="absolute top-1 right-1 px-1 min-w-[14px] h-3.5 flex items-center justify-center text-[8px] font-bold rounded-full bg-ethiopia-red text-white">
                  {item.badge}
                </span>
              )}
            </button>
          );
        })}
      </nav>
    </>
  );
};
