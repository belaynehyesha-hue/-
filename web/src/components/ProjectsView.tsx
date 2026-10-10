import React, { useState } from 'react';
import { 
  Building2, 
  Search, 
  PlusCircle, 
  CheckCircle2, 
  UserCheck, 
  MapPin, 
  ShieldCheck, 
  Trash2,
  Filter,
  TrendingUp,
  Sliders,
  Check,
  RotateCcw,
  Sparkles,
  BarChart3
} from 'lucide-react';
import { Project } from '../types';

interface ProjectsViewProps {
  projects: Project[];
  lang: 'am' | 'en';
  onAddProject: (project: Project) => void;
  onUpdateProject?: (project: Project) => void;
  onDeleteProject: (id: string) => void;
}

export const ProjectsView: React.FC<ProjectsViewProps> = ({
  projects,
  lang,
  onAddProject,
  onUpdateProject,
  onDeleteProject
}) => {
  const [search, setSearch] = useState('');
  const [filterStatus, setFilterStatus] = useState<'all' | 'active' | 'pending' | 'completed'>('all');
  const [filterProgress, setFilterProgress] = useState<'all' | '0-25' | '26-50' | '51-75' | '76-100'>('all');
  const [showModal, setShowModal] = useState(false);
  const [editingProgressId, setEditingProgressId] = useState<string | null>(null);
  const [tempProgress, setTempProgress] = useState<number>(0);

  // New Project Form State
  const [name, setName] = useState('');
  const [contractor, setContractor] = useState('');
  const [consultant, setConsultant] = useState('');
  const [residentEngineer, setResidentEngineer] = useState('ኢንጅነር በላይነህ የሻለም');
  const [location, setLocation] = useState('ቀበሌ 01');
  const [type, setType] = useState('የመንግስት አስተዳደር ህንፃ');
  const [budgetETB, setBudgetETB] = useState('25000000');
  const [progress, setProgress] = useState(15);
  const [ebcsStatus, setEbcsStatus] = useState('EBCS-1 & 2 የተረጋገጠ');

  // Statistics
  const totalBudget = projects.reduce((acc, p) => acc + (p.budgetETB || 0), 0);
  const avgProgress = projects.length > 0 
    ? Math.round(projects.reduce((acc, p) => acc + (p.progress || 0), 0) / projects.length) 
    : 0;
  const completedCount = projects.filter(p => p.progress >= 100 || p.status === 'completed').length;
  const activeCount = projects.filter(p => p.status === 'active' && p.progress < 100).length;

  const filtered = projects.filter(p => {
    const matchesSearch = p.name.toLowerCase().includes(search.toLowerCase()) ||
      p.contractor.toLowerCase().includes(search.toLowerCase()) ||
      p.location.toLowerCase().includes(search.toLowerCase());
    const matchesStatus = filterStatus === 'all' || p.status === filterStatus;
    
    let matchesProgress = true;
    if (filterProgress === '0-25') matchesProgress = p.progress <= 25;
    else if (filterProgress === '26-50') matchesProgress = p.progress > 25 && p.progress <= 50;
    else if (filterProgress === '51-75') matchesProgress = p.progress > 50 && p.progress <= 75;
    else if (filterProgress === '76-100') matchesProgress = p.progress > 75;

    return matchesSearch && matchesStatus && matchesProgress;
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name || !contractor) return;

    const finalProgress = Math.min(100, Math.max(0, Number(progress) || 0));
    const finalStatus = finalProgress >= 100 ? 'completed' : 'active';

    const newPrj: Project = {
      id: `PRJ-${String(projects.length + 1).padStart(3, '0')}`,
      name,
      contractor,
      consultant: consultant || 'አባይ ዲዛይን አማካሪ ድርጅት',
      residentEngineer: residentEngineer || 'ኢንጅነር በላይነህ የሻለም',
      location,
      type,
      status: finalStatus,
      progress: finalProgress,
      startDate: new Date().toISOString().split('T')[0],
      budgetETB: Number(budgetETB) || 10000000,
      ebcsStatus: ebcsStatus || 'EBCS-1 የተረጋገጠ'
    };

    onAddProject(newPrj);
    setShowModal(false);
    setName('');
    setContractor('');
    setProgress(15);
  };

  const handleStartEditProgress = (p: Project) => {
    setEditingProgressId(p.id);
    setTempProgress(p.progress || 0);
  };

  const handleSaveProgress = (project: Project) => {
    if (!onUpdateProject) return;
    const clampedProgress = Math.min(100, Math.max(0, tempProgress));
    const updatedStatus = clampedProgress >= 100 ? 'completed' : (project.status === 'completed' && clampedProgress < 100 ? 'active' : project.status);
    
    onUpdateProject({
      ...project,
      progress: clampedProgress,
      status: updatedStatus
    });
    setEditingProgressId(null);
  };

  const handleQuickProgressStep = (project: Project, delta: number) => {
    if (!onUpdateProject) return;
    const nextProgress = Math.min(100, Math.max(0, (project.progress || 0) + delta));
    const nextStatus = nextProgress >= 100 ? 'completed' : (project.status === 'completed' && nextProgress < 100 ? 'active' : project.status);
    onUpdateProject({
      ...project,
      progress: nextProgress,
      status: nextStatus
    });
  };

  const getProgressColor = (val: number) => {
    if (val >= 100) return 'from-emerald-500 to-green-400';
    if (val >= 75) return 'from-teal-500 to-emerald-400';
    if (val >= 50) return 'from-blue-500 to-cyan-400';
    if (val >= 25) return 'from-amber-500 to-yellow-400';
    return 'from-rose-500 to-orange-400';
  };

  const getProgressTextColor = (val: number) => {
    if (val >= 100) return 'text-emerald-400';
    if (val >= 75) return 'text-teal-400';
    if (val >= 50) return 'text-blue-400';
    if (val >= 25) return 'text-amber-400';
    return 'text-rose-400';
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto pb-12">
      {/* Header bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-slate-900 border border-slate-800 p-5 rounded-2xl shadow-sm">
        <div>
          <h1 className="text-xl font-bold text-white flex items-center gap-2">
            <Building2 className="w-5 h-5 text-ethiopia-yellow" />
            <span>{lang === 'am' ? 'የቲሊሊ ከተማ የግንባታ ፕሮጀክቶች መዝገብ' : 'Municipal Construction Projects'}</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            {lang === 'am' ? 'በከተማው ውስጥ ያሉ የመንግስትና የህዝብ ግንባታዎች ዝርዝርና የጥራት ክትትል' : 'Detailed oversight of ongoing public works, infrastructure & progress tracking'}
          </p>
        </div>

        <button
          onClick={() => setShowModal(true)}
          className="flex items-center gap-2 bg-ethiopia-green hover:bg-emerald-600 text-white px-4 py-2.5 rounded-xl text-sm font-semibold shadow-md shadow-ethiopia-green/20 transition-all shrink-0"
        >
          <PlusCircle className="w-4 h-4" />
          <span>{lang === 'am' ? 'አዲስ ፕሮጀክት መዝግብ' : 'Add Project'}</span>
        </button>
      </div>

      {/* Progress & Project Key Metrics */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div className="bg-slate-900 border border-slate-800 p-4 rounded-xl">
          <div className="flex items-center justify-between text-xs text-slate-400 mb-1">
            <span>{lang === 'am' ? 'አማካይ አፈጻጸም' : 'Average Progress'}</span>
            <TrendingUp className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="text-2xl font-bold text-white mb-2">{avgProgress}%</div>
          <div className="w-full bg-slate-950 h-2 rounded-full overflow-hidden">
            <div 
              className={`h-full rounded-full transition-all duration-500 bg-gradient-to-r ${getProgressColor(avgProgress)}`}
              style={{ width: `${avgProgress}%` }}
            />
          </div>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-4 rounded-xl">
          <div className="flex items-center justify-between text-xs text-slate-400 mb-1">
            <span>{lang === 'am' ? 'ጠቅላላ ፕሮጀክቶች' : 'Total Projects'}</span>
            <Building2 className="w-4 h-4 text-blue-400" />
          </div>
          <div className="text-2xl font-bold text-white mb-1">{projects.length}</div>
          <span className="text-[11px] text-slate-500">{activeCount} {lang === 'am' ? 'በስራ ላይ' : 'Active'}</span>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-4 rounded-xl">
          <div className="flex items-center justify-between text-xs text-slate-400 mb-1">
            <span>{lang === 'am' ? 'የተጠናቀቁ' : 'Completed (100%)'}</span>
            <CheckCircle2 className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="text-2xl font-bold text-emerald-400 mb-1">{completedCount}</div>
          <span className="text-[11px] text-slate-500">{projects.length > 0 ? Math.round((completedCount / projects.length) * 100) : 0}% {lang === 'am' ? 'ተጠናቋል' : 'done'}</span>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-4 rounded-xl">
          <div className="flex items-center justify-between text-xs text-slate-400 mb-1">
            <span>{lang === 'am' ? 'ጠቅላላ በጀት' : 'Total Budget'}</span>
            <BarChart3 className="w-4 h-4 text-ethiopia-yellow" />
          </div>
          <div className="text-2xl font-bold text-ethiopia-yellow mb-1">{(totalBudget / 1000000).toFixed(1)}M</div>
          <span className="text-[11px] text-slate-500">ETB ({lang === 'am' ? 'የተመደበ በጀት' : 'Allocated'})</span>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="flex flex-col gap-3 bg-slate-900 border border-slate-800 p-3.5 rounded-xl">
        <div className="flex flex-col sm:flex-row items-center gap-3">
          <div className="relative flex-1 w-full">
            <Search className="w-4 h-4 absolute left-3 top-3 text-slate-400" />
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder={lang === 'am' ? 'በፕሮጀክት ስም፣ በኮንትራክተር ወይም በቦታ ፈልግ...' : 'Search by name, contractor, location...'}
              className="w-full bg-slate-850 border border-slate-700 rounded-lg pl-9 pr-4 py-2 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-ethiopia-yellow"
            />
          </div>

          <div className="flex items-center gap-1.5 w-full sm:w-auto overflow-x-auto pb-1 sm:pb-0">
            <Filter className="w-3.5 h-3.5 text-slate-400 ml-1 hidden sm:inline" />
            {(['all', 'active', 'pending', 'completed'] as const).map((status) => (
              <button
                key={status}
                onClick={() => setFilterStatus(status)}
                className={`px-3 py-1.5 rounded-lg text-xs font-medium whitespace-nowrap transition-colors ${
                  filterStatus === status
                    ? 'bg-ethiopia-yellow text-slate-950 font-bold'
                    : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
                }`}
              >
                {status === 'all' && (lang === 'am' ? 'ሁሉንም' : 'All Status')}
                {status === 'active' && (lang === 'am' ? 'በስራ ላይ' : 'Active')}
                {status === 'pending' && (lang === 'am' ? 'በመጠባበቅ ላይ' : 'Pending')}
                {status === 'completed' && (lang === 'am' ? 'የተጠናቀቁ' : 'Completed')}
              </button>
            ))}
          </div>
        </div>

        {/* Progress Range Filter Buttons */}
        <div className="flex items-center gap-2 pt-2 border-t border-slate-800/80 overflow-x-auto text-[11px]">
          <span className="text-slate-400 font-medium whitespace-nowrap">{lang === 'am' ? 'የአፈጻጸም ምድብ:' : 'Progress Range:'}</span>
          {[
            { id: 'all', label: lang === 'am' ? 'ሁሉም (0-100%)' : 'All (0-100%)' },
            { id: '0-25', label: '0 - 25% (መሰረት/ማስጀመሪያ)' },
            { id: '26-50', label: '26 - 50% (ዋና ህንፃ/አጽም)' },
            { id: '51-75', label: '51 - 75% (ማጠናቀቂያ/ኤሌክትሪክ)' },
            { id: '76-100', label: '76 - 100% (ማስረከቢያ ደረጃ)' }
          ].map(pFilter => (
            <button
              key={pFilter.id}
              onClick={() => setFilterProgress(pFilter.id as any)}
              className={`px-2.5 py-1 rounded-md whitespace-nowrap transition-colors ${
                filterProgress === pFilter.id
                  ? 'bg-emerald-950 border border-emerald-500 text-emerald-300 font-semibold'
                  : 'bg-slate-850 text-slate-400 hover:text-white border border-slate-800'
              }`}
            >
              {pFilter.label}
            </button>
          ))}
        </div>
      </div>

      {/* Projects Grid (Responsive: 1 col mobile, 2 col tablet/desktop) */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {filtered.map((p) => {
          const isEditing = editingProgressId === p.id;
          const isDone = p.progress >= 100;

          return (
            <div
              key={p.id}
              className={`bg-slate-900 border rounded-2xl p-5 transition-all flex flex-col justify-between shadow-sm relative ${
                isDone ? 'border-emerald-800/60 bg-gradient-to-br from-slate-900 to-emerald-950/20' : 'border-slate-800 hover:border-slate-700'
              }`}
            >
              <div>
                <div className="flex items-center justify-between gap-2 mb-2">
                  <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-slate-800 text-ethiopia-yellow border border-slate-700">
                    {p.id}
                  </span>
                  <div className="flex items-center gap-1.5">
                    {isDone && (
                      <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-emerald-950 text-emerald-400 border border-emerald-600 flex items-center gap-1">
                        <CheckCircle2 className="w-3 h-3" />
                        {lang === 'am' ? 'የተጠናቀቀ' : 'DONE'}
                      </span>
                    )}
                    <span className={`text-[10px] font-bold px-2.5 py-0.5 rounded-full ${
                      p.status === 'active' 
                        ? 'bg-blue-950 text-blue-400 border border-blue-800' 
                        : p.status === 'pending'
                        ? 'bg-amber-950 text-amber-400 border border-amber-800'
                        : 'bg-emerald-950 text-emerald-400 border border-emerald-800'
                    }`}>
                      {p.status.toUpperCase()}
                    </span>
                  </div>
                </div>

                <h3 className="font-bold text-base text-white leading-snug">{p.name}</h3>
                <p className="text-xs text-slate-400 mt-1 flex items-center gap-1.5">
                  <MapPin className="w-3.5 h-3.5 text-ethiopia-red shrink-0" />
                  <span>{p.location}</span>
                  <span>•</span>
                  <span>{p.type}</span>
                </p>

                <div className="grid grid-cols-2 gap-2 mt-4 pt-3 border-t border-slate-800 text-xs">
                  <div>
                    <span className="text-slate-500 text-[11px] block">{lang === 'am' ? 'ዋና ኮንትራክተር' : 'Contractor'}</span>
                    <span className="text-slate-200 font-medium line-clamp-1">{p.contractor}</span>
                  </div>
                  <div>
                    <span className="text-slate-500 text-[11px] block">{lang === 'am' ? 'አማካሪ መሐንዲስ' : 'Consultant'}</span>
                    <span className="text-slate-200 font-medium line-clamp-1">{p.consultant}</span>
                  </div>
                  <div>
                    <span className="text-slate-500 text-[11px] block">{lang === 'am' ? 'የሳይት መሐንዲስ' : 'Resident Engineer'}</span>
                    <span className="text-slate-200 font-medium flex items-center gap-1 line-clamp-1">
                      <UserCheck className="w-3 h-3 text-emerald-400 shrink-0" />
                      <span>{p.residentEngineer}</span>
                    </span>
                  </div>
                  <div>
                    <span className="text-slate-500 text-[11px] block">{lang === 'am' ? 'በጀት' : 'Budget'}</span>
                    <span className="text-amber-400 font-bold">{(p.budgetETB / 1000000).toFixed(2)}M ETB</span>
                  </div>
                </div>

                {/* EBCS compliance note */}
                <div className="mt-3 p-2 rounded-lg bg-slate-850 border border-slate-800 flex items-center gap-2 text-[11px] text-slate-300">
                  <ShieldCheck className="w-4 h-4 text-ethiopia-green shrink-0" />
                  <span className="line-clamp-1">{p.ebcsStatus}</span>
                </div>
              </div>

              {/* Progress Bar & Interactive Percentage Controls */}
              <div className="mt-4 pt-3 border-t border-slate-800">
                <div className="flex items-center justify-between text-xs mb-1.5">
                  <span className="text-slate-300 font-medium flex items-center gap-1.5">
                    <TrendingUp className="w-3.5 h-3.5 text-slate-400" />
                    <span>{lang === 'am' ? 'የግንባታ አፈጻጸም ደረጃ' : 'Construction Progress'}</span>
                  </span>
                  <div className="flex items-center gap-2">
                    <span className={`font-mono text-sm font-bold ${getProgressTextColor(p.progress)}`}>
                      {p.progress}%
                    </span>
                    {!isEditing && onUpdateProject && (
                      <button
                        onClick={() => handleStartEditProgress(p)}
                        className="px-2 py-0.5 text-[10px] bg-slate-800 hover:bg-slate-700 text-slate-300 rounded border border-slate-700 flex items-center gap-1 transition-colors"
                        title="Edit progress percentage"
                      >
                        <Sliders className="w-3 h-3" />
                        <span>{lang === 'am' ? 'ቀይር' : 'Adjust'}</span>
                      </button>
                    )}
                  </div>
                </div>

                {/* Main Visual Progress Bar */}
                <div className="w-full bg-slate-950 h-3 rounded-full overflow-hidden p-0.5 border border-slate-800 shadow-inner">
                  <div 
                    className={`h-full rounded-full transition-all duration-300 bg-gradient-to-r ${getProgressColor(p.progress)}`}
                    style={{ width: `${p.progress}%` }}
                  />
                </div>

                {/* Inline Editing Controls (Slider, number input, presets, and quick buttons) */}
                {isEditing ? (
                  <div className="mt-3 p-3 rounded-xl bg-slate-950 border border-slate-700/80 space-y-2.5 animate-fadeIn">
                    <div className="flex items-center justify-between text-xs text-slate-300">
                      <span className="font-semibold text-ethiopia-yellow">
                        {lang === 'am' ? 'አዲስ አፈጻጸም በመቶኛ ያስገቡ:' : 'Update Progress Percentage:'}
                      </span>
                      <span className="font-mono font-bold text-white text-sm bg-slate-800 px-2 py-0.5 rounded border border-slate-700">
                        {tempProgress}%
                      </span>
                    </div>

                    {/* Range slider for smooth progress dragging */}
                    <input
                      type="range"
                      min={0}
                      max={100}
                      value={tempProgress}
                      onChange={(e) => setTempProgress(Number(e.target.value))}
                      className="w-full h-2 bg-slate-800 rounded-lg appearance-none cursor-pointer accent-ethiopia-green"
                    />

                    {/* Quick percentage preset buttons */}
                    <div className="flex items-center justify-between gap-1 text-[10px]">
                      {[10, 25, 50, 75, 100].map(val => (
                        <button
                          key={val}
                          type="button"
                          onClick={() => setTempProgress(val)}
                          className={`px-2 py-1 rounded transition-colors ${
                            tempProgress === val 
                              ? 'bg-ethiopia-yellow text-slate-950 font-bold' 
                              : 'bg-slate-850 text-slate-400 hover:text-white border border-slate-800'
                          }`}
                        >
                          {val}%
                        </button>
                      ))}
                    </div>

                    {/* Action buttons to Save or Cancel */}
                    <div className="flex items-center justify-end gap-2 pt-1 border-t border-slate-800">
                      <button
                        type="button"
                        onClick={() => setEditingProgressId(null)}
                        className="px-2.5 py-1 rounded text-xs bg-slate-800 hover:bg-slate-700 text-slate-300"
                      >
                        {lang === 'am' ? 'ተው' : 'Cancel'}
                      </button>
                      <button
                        type="button"
                        onClick={() => handleSaveProgress(p)}
                        className="px-3 py-1 rounded text-xs bg-ethiopia-green hover:bg-emerald-600 text-white font-bold flex items-center gap-1 shadow-sm"
                      >
                        <Check className="w-3.5 h-3.5" />
                        <span>{lang === 'am' ? 'አስቀምጥ' : 'Save'}</span>
                      </button>
                    </div>
                  </div>
                ) : (
                  /* Quick step buttons for instant 1-click updates */
                  onUpdateProject && (
                    <div className="flex items-center justify-between gap-1.5 mt-2.5 pt-2 border-t border-slate-800/60">
                      <div className="flex items-center gap-1 text-[10px]">
                        <span className="text-slate-500 mr-1">{lang === 'am' ? 'ፈጣን:' : 'Quick:'}</span>
                        <button
                          onClick={() => handleQuickProgressStep(p, -5)}
                          disabled={p.progress <= 0}
                          className="px-1.5 py-0.5 rounded bg-slate-800 hover:bg-slate-700 disabled:opacity-30 text-slate-300 transition-colors"
                          title="Decrease by 5%"
                        >
                          -5%
                        </button>
                        <button
                          onClick={() => handleQuickProgressStep(p, +5)}
                          disabled={p.progress >= 100}
                          className="px-1.5 py-0.5 rounded bg-slate-800 hover:bg-slate-700 disabled:opacity-30 text-slate-300 transition-colors"
                          title="Increase by 5%"
                        >
                          +5%
                        </button>
                        <button
                          onClick={() => handleQuickProgressStep(p, +10)}
                          disabled={p.progress >= 100}
                          className="px-1.5 py-0.5 rounded bg-slate-800 hover:bg-slate-700 disabled:opacity-30 text-slate-300 transition-colors"
                          title="Increase by 10%"
                        >
                          +10%
                        </button>
                      </div>

                      <button
                        onClick={() => onDeleteProject(p.id)}
                        className="p-1.5 text-slate-500 hover:text-rose-400 hover:bg-rose-950/30 rounded-lg transition-colors"
                        title="Delete Project"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  )
                )}
              </div>
            </div>
          );
        })}
      </div>

      {filtered.length === 0 && (
        <div className="text-center py-12 bg-slate-900 border border-slate-800 rounded-2xl p-6">
          <Building2 className="w-12 h-12 text-slate-600 mx-auto mb-3" />
          <h3 className="text-base font-bold text-slate-300">
            {lang === 'am' ? 'ምንም የተገኘ ፕሮጀክት የለም' : 'No Projects Found'}
          </h3>
          <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto">
            {lang === 'am' ? 'የተሰጠውን ፍለጋ ወይም ማጣሪያ የሚያሟላ የግንባታ ፕሮጀክት አልተገኘም።' : 'No projects match your current filter or search criteria.'}
          </p>
        </div>
      )}

      {/* Add Project Modal with Live Progress Bar & Percentage Input */}
      {showModal && (
        <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-2xl max-w-lg w-full p-6 shadow-2xl space-y-4 max-h-[90vh] overflow-y-auto">
            <h2 className="text-lg font-bold text-white flex items-center gap-2">
              <Building2 className="w-5 h-5 text-ethiopia-yellow" />
              <span>{lang === 'am' ? 'አዲስ የግንባታ ፕሮጀክት መመዝገቢያ' : 'Register New Project'}</span>
            </h2>

            <form onSubmit={handleSubmit} className="space-y-3.5 text-xs">
              <div>
                <label className="block text-slate-300 mb-1 font-medium">{lang === 'am' ? 'የፕሮጀክት ስም' : 'Project Name'}</label>
                <input
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="ለምሳሌ፡ የቲሊሊ ሁለተኛ ደረጃ ትምህርት ቤት አዳራሽ"
                  className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-ethiopia-yellow"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-300 mb-1 font-medium">{lang === 'am' ? 'ዋና ኮንትራክተር' : 'Contractor'}</label>
                  <input
                    type="text"
                    required
                    value={contractor}
                    onChange={(e) => setContractor(e.target.value)}
                    placeholder="ብርሃን ኮንስትራክሽን"
                    className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-ethiopia-yellow"
                  />
                </div>
                <div>
                  <label className="block text-slate-300 mb-1 font-medium">{lang === 'am' ? 'አማካሪ ድርጅት' : 'Consultant'}</label>
                  <input
                    type="text"
                    value={consultant}
                    onChange={(e) => setConsultant(e.target.value)}
                    placeholder="አባይ ዲዛይንና ቁጥጥር"
                    className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-ethiopia-yellow"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-300 mb-1 font-medium">{lang === 'am' ? 'ቦታ (ቀበሌ)' : 'Location'}</label>
                  <input
                    type="text"
                    value={location}
                    onChange={(e) => setLocation(e.target.value)}
                    className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-ethiopia-yellow"
                  />
                </div>
                <div>
                  <label className="block text-slate-300 mb-1 font-medium">{lang === 'am' ? 'በጀት (በብር)' : 'Budget (ETB)'}</label>
                  <input
                    type="number"
                    value={budgetETB}
                    onChange={(e) => setBudgetETB(e.target.value)}
                    className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-ethiopia-yellow"
                  />
                </div>
              </div>

              {/* Progress Percentage Control with Live Preview Bar */}
              <div className="p-3.5 bg-slate-850 border border-slate-700 rounded-xl space-y-2">
                <div className="flex items-center justify-between">
                  <label className="text-slate-200 font-semibold flex items-center gap-1.5">
                    <TrendingUp className="w-3.5 h-3.5 text-ethiopia-yellow" />
                    <span>{lang === 'am' ? 'የመጀመሪያ አፈጻጸም ደረጃ (%)' : 'Initial Progress Percentage (%)'}</span>
                  </label>
                  <div className="flex items-center gap-1">
                    <input
                      type="number"
                      min={0}
                      max={100}
                      value={progress}
                      onChange={(e) => setProgress(Math.min(100, Math.max(0, Number(e.target.value))))}
                      className="w-16 bg-slate-800 border border-slate-600 rounded px-2 py-0.5 text-right font-mono font-bold text-white focus:outline-none focus:border-ethiopia-yellow text-xs"
                    />
                    <span className="text-slate-400 font-mono">%</span>
                  </div>
                </div>

                {/* Range Slider */}
                <input
                  type="range"
                  min={0}
                  max={100}
                  value={progress}
                  onChange={(e) => setProgress(Number(e.target.value))}
                  className="w-full h-2 bg-slate-800 rounded-lg appearance-none cursor-pointer accent-ethiopia-green"
                />

                {/* Live Progress Bar Preview */}
                <div className="w-full bg-slate-950 h-2.5 rounded-full overflow-hidden p-0.5 border border-slate-800">
                  <div
                    className={`h-full rounded-full transition-all duration-200 bg-gradient-to-r ${getProgressColor(progress)}`}
                    style={{ width: `${progress}%` }}
                  />
                </div>

                <div className="flex items-center justify-between text-[10px] text-slate-500">
                  <span>0% (ጀምሯል)</span>
                  <span>50% (ግማሽ)</span>
                  <span>100% (ተጠናቋል)</span>
                </div>
              </div>

              <div>
                <label className="block text-slate-300 mb-1 font-medium">{lang === 'am' ? 'የመሐንዲስ ኃላፊ' : 'Resident Engineer'}</label>
                <input
                  type="text"
                  value={residentEngineer}
                  onChange={(e) => setResidentEngineer(e.target.value)}
                  className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-ethiopia-yellow"
                />
              </div>

              <div className="flex items-center justify-end gap-2 pt-4 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 font-medium"
                >
                  {lang === 'am' ? 'ተው' : 'Cancel'}
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-lg bg-ethiopia-green hover:bg-emerald-600 text-white font-semibold shadow-md"
                >
                  {lang === 'am' ? 'መዝግብ' : 'Save Project'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
