import React from 'react';
import { 
  Building2, 
  HardHat, 
  GraduationCap, 
  AlertTriangle, 
  TrendingUp, 
  PlusCircle, 
  FileDown, 
  Sparkles,
  CheckCircle2,
  Clock,
  ArrowRight
} from 'lucide-react';
import { Project, Artisan, Professional, Warning, InstitutionProfile } from '../types';

interface DashboardProps {
  projects: Project[];
  artisans: Artisan[];
  professionals: Professional[];
  warnings: Warning[];
  institution: InstitutionProfile;
  lang: 'am' | 'en';
  onNavigate: (tab: string) => void;
  onOpenAddProject: () => void;
  onOpenAddArtisan: () => void;
  onOpenAddWarning: () => void;
}

export const Dashboard: React.FC<DashboardProps> = ({
  projects,
  artisans,
  professionals,
  warnings,
  institution,
  lang,
  onNavigate,
  onOpenAddProject,
  onOpenAddArtisan,
  onOpenAddWarning
}) => {
  const activeProjectsCount = projects.filter(p => p.status === 'active').length;
  const totalBudget = projects.reduce((sum, p) => sum + p.budgetETB, 0);
  const avgProgress = Math.round(projects.reduce((sum, p) => sum + p.progress, 0) / (projects.length || 1));

  return (
    <div className="space-y-6 max-w-7xl mx-auto pb-12">
      {/* Hero Header Card */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-slate-900 via-slate-850 to-slate-900 border border-slate-800 p-6 shadow-xl">
        <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-ethiopia-green via-ethiopia-yellow to-ethiopia-red"></div>
        
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="inline-flex items-center gap-2 px-2.5 py-1 rounded-full bg-ethiopia-green/20 border border-ethiopia-green/40 text-ethiopia-yellow text-xs font-semibold mb-2">
              <span>🇪🇹</span>
              <span>{lang === 'am' ? 'የከተማ ልማትና ኮንስትራክሽን ፈቃድና ቁጥጥር' : 'Urban Development & Construction Regulation'}</span>
            </div>
            <h1 className="text-xl md:text-2xl font-bold text-white tracking-tight">
              {lang === 'am' ? 'የቲሊሊ ከተማ የግንባታ ፕሮጀክቶች ዲጂታል ማዕከል' : 'Tilli City Construction Management Portal'}
            </h1>
            <p className="text-sm text-slate-300 mt-1 max-w-2xl leading-relaxed">
              {lang === 'am' 
                ? `በኢትዮጵያ ህንፃ ኮዶች (EBCS 1-14) እና የከተማ ቦታን በሊዝ ስለመያዝ የወጣ አዋጅ ቁጥር 721/2004 መሠረት የሚመራ የቁጥጥር ስርዓት። ኃላፊ፡ ${institution.officerName}`
                : `Regulatory control under EBCS standards and Ethiopian Urban Lease Proclamation. Officer in charge: ${institution.officerName}`
              }
            </p>
          </div>

          {/* Primary Action Buttons */}
          <div className="flex flex-wrap items-center gap-2.5 shrink-0">
            <button
              onClick={onOpenAddProject}
              className="flex items-center gap-2 bg-ethiopia-green hover:bg-emerald-600 text-white px-4 py-2.5 rounded-xl text-sm font-semibold shadow-lg shadow-ethiopia-green/20 transition-all"
            >
              <PlusCircle className="w-4 h-4" />
              <span>{lang === 'am' ? 'አዲስ ፕሮጀክት' : 'Add Project'}</span>
            </button>
            <button
              onClick={() => onNavigate('ai')}
              className="flex items-center gap-2 bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-slate-950 px-4 py-2.5 rounded-xl text-sm font-bold shadow-lg shadow-amber-500/20 transition-all"
            >
              <Sparkles className="w-4 h-4" />
              <span>{lang === 'am' ? 'Gemini AI ረዳት' : 'Gemini AI Search'}</span>
            </button>
          </div>
        </div>
      </div>

      {/* KPI METRIC CARDS (Adaptive Grid: 1 col mobile, 2 col tablet, 4 col desktop) */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Metric 1: Active Projects */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 flex items-center justify-between shadow-sm">
          <div>
            <p className="text-xs font-medium text-slate-400">
              {lang === 'am' ? 'ንቁ ግንባታዎች' : 'Active Projects'}
            </p>
            <h3 className="text-2xl font-bold text-white mt-1">
              {activeProjectsCount} <span className="text-xs font-normal text-slate-400">/ {projects.length}</span>
            </h3>
            <p className="text-xs text-emerald-400 flex items-center gap-1 mt-1 font-medium">
              <TrendingUp className="w-3 h-3" />
              <span>{avgProgress}% {lang === 'am' ? 'አማካይ አፈጻጸም' : 'Avg Progress'}</span>
            </p>
          </div>
          <div className="w-12 h-12 rounded-xl bg-blue-500/10 border border-blue-500/20 flex items-center justify-center text-blue-400">
            <Building2 className="w-6 h-6" />
          </div>
        </div>

        {/* Metric 2: Artisans */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 flex items-center justify-between shadow-sm">
          <div>
            <p className="text-xs font-medium text-slate-400">
              {lang === 'am' ? 'የልምድ ሰራተኞች' : 'Certified Artisans'}
            </p>
            <h3 className="text-2xl font-bold text-white mt-1">
              {artisans.length}
            </h3>
            <p className="text-xs text-slate-400 mt-1">
              {lang === 'am' ? '100% የCOC ደረጃ የተረጋገጠ' : '100% COC Certified'}
            </p>
          </div>
          <div className="w-12 h-12 rounded-xl bg-amber-500/10 border border-amber-500/20 flex items-center justify-center text-amber-400">
            <HardHat className="w-6 h-6" />
          </div>
        </div>

        {/* Metric 3: Professionals */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 flex items-center justify-between shadow-sm">
          <div>
            <p className="text-xs font-medium text-slate-400">
              {lang === 'am' ? 'መሐንዲሶችና አርክቴክቶች' : 'Engineers & Architects'}
            </p>
            <h3 className="text-2xl font-bold text-white mt-1">
              {professionals.length}
            </h3>
            <p className="text-xs text-emerald-400 flex items-center gap-1 mt-1 font-medium">
              <CheckCircle2 className="w-3 h-3" />
              <span>{lang === 'am' ? 'ሕጋዊ የሙያ ፈቃድ' : 'Licensed'}</span>
            </p>
          </div>
          <div className="w-12 h-12 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400">
            <GraduationCap className="w-6 h-6" />
          </div>
        </div>

        {/* Metric 4: Total Project Budget */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 flex items-center justify-between shadow-sm">
          <div>
            <p className="text-xs font-medium text-slate-400">
              {lang === 'am' ? 'አጠቃላይ የፕሮጀክት በጀት' : 'Total Project Value'}
            </p>
            <h3 className="text-xl font-bold text-white mt-1">
              {(totalBudget / 1000000).toFixed(1)} <span className="text-xs text-ethiopia-yellow">ሚሊዮን ብር</span>
            </h3>
            <p className="text-xs text-slate-400 mt-1">
              {lang === 'am' ? 'በመንግስትና ማህበረሰብ የተመደበ' : 'Allocated Budget'}
            </p>
          </div>
          <div className="w-12 h-12 rounded-xl bg-purple-500/10 border border-purple-500/20 flex items-center justify-center text-purple-400">
            <FileDown className="w-6 h-6" />
          </div>
        </div>
      </div>

      {/* WARNINGS & REGULATORY ALERTS (If any) */}
      {warnings.length > 0 && (
        <div className="bg-rose-950/40 border border-rose-800/60 rounded-xl p-4">
          <div className="flex items-center justify-between mb-2">
            <div className="flex items-center gap-2 text-rose-400 font-semibold text-sm">
              <AlertTriangle className="w-4 h-4 text-rose-400" />
              <span>{lang === 'am' ? 'አስቸኳይ የቁጥጥርና ማስጠንቀቂያ ማስታወሻዎች' : 'Urgent Regulatory Alerts'}</span>
            </div>
            <button
              onClick={() => onNavigate('warnings')}
              className="text-xs text-rose-300 hover:text-white flex items-center gap-1 font-medium"
            >
              <span>{lang === 'am' ? 'ሁሉንም ተመልከት' : 'View All'}</span>
              <ArrowRight className="w-3 h-3" />
            </button>
          </div>
          <div className="space-y-2">
            {warnings.slice(0, 2).map((w) => (
              <div key={w.id} className="bg-slate-900/80 p-2.5 rounded-lg border border-slate-800 flex items-start justify-between gap-3 text-xs">
                <div>
                  <span className="font-bold text-white">{w.recipientName}</span>
                  <span className="text-slate-400 ml-2">({w.sector})</span>
                  <p className="text-slate-300 mt-0.5">{w.reason}</p>
                </div>
                <span className="bg-rose-900/80 text-rose-200 px-2 py-0.5 rounded text-[10px] font-bold shrink-0">
                  {w.severity.toUpperCase()}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* TWO COLUMN SECTION: PROJECTS OVERVIEW & FAST ACTIONS */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2 Cols: Recent Projects Table/Cards */}
        <div className="lg:col-span-2 bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-base font-bold text-white flex items-center gap-2">
              <Building2 className="w-4 h-4 text-ethiopia-yellow" />
              <span>{lang === 'am' ? 'የግንባታ ፕሮጀክቶች ወቅታዊ ሁኔታ' : 'Current Construction Projects'}</span>
            </h2>
            <button
              onClick={() => onNavigate('projects')}
              className="text-xs text-ethiopia-yellow hover:underline flex items-center gap-1 font-medium"
            >
              <span>{lang === 'am' ? 'ሁሉንም ፕሮጀክቶች' : 'All Projects'}</span>
              <ArrowRight className="w-3 h-3" />
            </button>
          </div>

          <div className="space-y-3">
            {projects.slice(0, 4).map((p) => (
              <div
                key={p.id}
                className="bg-slate-850 hover:bg-slate-800 p-4 rounded-xl border border-slate-800/80 transition-colors"
              >
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                  <div>
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-slate-800 text-ethiopia-yellow border border-slate-700">
                      {p.id}
                    </span>
                    <h3 className="text-sm font-bold text-white mt-1">{p.name}</h3>
                    <p className="text-xs text-slate-400 mt-0.5">
                      {p.contractor} • {p.location}
                    </p>
                  </div>
                  <div className="text-left sm:text-right shrink-0">
                    <span className="text-xs font-bold text-slate-200">
                      {(p.budgetETB / 1000000).toFixed(1)}M ETB
                    </span>
                    <div className="text-[11px] text-emerald-400 font-semibold flex items-center sm:justify-end gap-1 mt-0.5">
                      <Clock className="w-3 h-3" />
                      <span>{p.progress}% {lang === 'am' ? 'ተጠናቋል' : 'Done'}</span>
                    </div>
                  </div>
                </div>

                {/* Progress Bar */}
                <div className="w-full bg-slate-950 h-2 rounded-full mt-3 overflow-hidden">
                  <div 
                    className="bg-gradient-to-r from-ethiopia-green to-ethiopia-yellow h-full rounded-full transition-all duration-500"
                    style={{ width: `${p.progress}%` }}
                  ></div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Right 1 Col: Quick Administrative Shortcuts */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-sm space-y-4">
          <h2 className="text-base font-bold text-white flex items-center gap-2">
            <Sparkles className="w-4 h-4 text-ethiopia-yellow" />
            <span>{lang === 'am' ? 'ፈጣን የአሰራር ተግባራት' : 'Quick Operations'}</span>
          </h2>

          <div className="space-y-2.5">
            <button
              onClick={onOpenAddArtisan}
              className="w-full text-left p-3 rounded-xl bg-slate-850 hover:bg-slate-800 border border-slate-800 transition-colors flex items-center justify-between"
            >
              <div>
                <h4 className="text-xs font-bold text-white">
                  {lang === 'am' ? 'አዲስ የልምድ ሰራተኛ መመዝገቢያ' : 'Register New Artisan'}
                </h4>
                <p className="text-[11px] text-slate-400">
                  {lang === 'am' ? 'የCOC ሰነድ ማረጋገጫና ሙያ መዝገብ' : 'COC level and project assignment'}
                </p>
              </div>
              <PlusCircle className="w-4 h-4 text-amber-400" />
            </button>

            <button
              onClick={onOpenAddWarning}
              className="w-full text-left p-3 rounded-xl bg-slate-850 hover:bg-slate-800 border border-slate-800 transition-colors flex items-center justify-between"
            >
              <div>
                <h4 className="text-xs font-bold text-white">
                  {lang === 'am' ? 'የሕግና ደንብ ማስጠንቀቂያ መላኪያ' : 'Issue Regulatory Warning'}
                </h4>
                <p className="text-[11px] text-slate-400">
                  {lang === 'am' ? 'ለኮንትራክተሮችና ሰራተኞች ማስጠንቀቂያ' : 'Send SMS/formal notice'}
                </p>
              </div>
              <AlertTriangle className="w-4 h-4 text-rose-400" />
            </button>

            <button
              onClick={() => onNavigate('reports')}
              className="w-full text-left p-3 rounded-xl bg-slate-850 hover:bg-slate-800 border border-slate-800 transition-colors flex items-center justify-between"
            >
              <div>
                <h4 className="text-xs font-bold text-white">
                  {lang === 'am' ? 'ሪፖርቶችን ማውረድና ማጋራት' : 'Export Official Reports'}
                </h4>
                <p className="text-[11px] text-slate-400">
                  {lang === 'am' ? 'Excel, PDF እና WhatsApp/Telegram ማጋሪያ' : 'PDF, Excel, WhatsApp & Email'}
                </p>
              </div>
              <FileDown className="w-4 h-4 text-emerald-400" />
            </button>

            <button
              onClick={() => onNavigate('ai')}
              className="w-full text-left p-3 rounded-xl bg-slate-850 hover:bg-slate-800 border border-slate-800 transition-colors flex items-center justify-between"
            >
              <div>
                <h4 className="text-xs font-bold text-white">
                  {lang === 'am' ? 'በድረ-ገጽ የተደገፈ Gemini AI ፍለጋ' : 'Google Grounded Web Search'}
                </h4>
                <p className="text-[11px] text-slate-400">
                  {lang === 'am' ? 'የወቅታዊ ዋጋዎችና EBCS ኮዶች ፍለጋ' : 'Current prices & building standards'}
                </p>
              </div>
              <Sparkles className="w-4 h-4 text-amber-400" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
