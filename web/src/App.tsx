import React, { useState, useEffect } from 'react';
import { Navigation } from './components/Navigation';
import { Header } from './components/Header';
import { Dashboard } from './components/Dashboard';
import { ProjectsView } from './components/ProjectsView';
import { ArtisansView } from './components/ArtisansView';
import { ProfessionalsView } from './components/ProfessionalsView';
import { GeminiAiChat } from './components/GeminiAiChat';
import { ReportsCenter } from './components/ReportsCenter';
import { WarningsView } from './components/WarningsView';
import { SettingsModal } from './components/SettingsModal';

import { 
  INITIAL_PROJECTS, 
  INITIAL_ARTISANS, 
  INITIAL_PROFESSIONALS, 
  INITIAL_WARNINGS, 
  INITIAL_INSTITUTION 
} from './data/initialData';

import { Project, Artisan, Professional, Warning, InstitutionProfile } from './types';

export const App: React.FC = () => {
  // LocalStorage-backed state with fallbacks
  const [projects, setProjects] = useState<Project[]>(() => {
    const saved = localStorage.getItem('tilli_projects');
    return saved ? JSON.parse(saved) : INITIAL_PROJECTS;
  });

  const [artisans, setArtisans] = useState<Artisan[]>(() => {
    const saved = localStorage.getItem('tilli_artisans');
    return saved ? JSON.parse(saved) : INITIAL_ARTISANS;
  });

  const [professionals, setProfessionals] = useState<Professional[]>(() => {
    const saved = localStorage.getItem('tilli_professionals');
    return saved ? JSON.parse(saved) : INITIAL_PROFESSIONALS;
  });

  const [warnings, setWarnings] = useState<Warning[]>(() => {
    const saved = localStorage.getItem('tilli_warnings');
    return saved ? JSON.parse(saved) : INITIAL_WARNINGS;
  });

  const [institution, setInstitution] = useState<InstitutionProfile>(() => {
    const saved = localStorage.getItem('tilli_institution');
    return saved ? JSON.parse(saved) : INITIAL_INSTITUTION;
  });

  // UI state
  const [currentTab, setCurrentTab] = useState('dashboard');
  const [lang, setLang] = useState<'am' | 'en'>('am');
  const [isDark, setIsDark] = useState(true);
  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);
  const [installPrompt, setInstallPrompt] = useState<any>(null);

  // Sync to localStorage
  useEffect(() => {
    localStorage.setItem('tilli_projects', JSON.stringify(projects));
  }, [projects]);

  useEffect(() => {
    localStorage.setItem('tilli_artisans', JSON.stringify(artisans));
  }, [artisans]);

  useEffect(() => {
    localStorage.setItem('tilli_professionals', JSON.stringify(professionals));
  }, [professionals]);

  useEffect(() => {
    localStorage.setItem('tilli_warnings', JSON.stringify(warnings));
  }, [warnings]);

  useEffect(() => {
    localStorage.setItem('tilli_institution', JSON.stringify(institution));
  }, [institution]);

  // Online / Offline Detection
  useEffect(() => {
    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  // PWA Install Prompt Listener
  useEffect(() => {
    const handleBeforeInstall = (e: Event) => {
      e.preventDefault();
      setInstallPrompt(e);
    };

    window.addEventListener('beforeinstallprompt', handleBeforeInstall);
    return () => window.removeEventListener('beforeinstallprompt', handleBeforeInstall);
  }, []);

  const handleInstallApp = async () => {
    if (!installPrompt) return;
    installPrompt.prompt();
    const { outcome } = await installPrompt.userChoice;
    if (outcome === 'accepted') {
      setInstallPrompt(null);
    }
  };

  // State mutation handlers
  const handleAddProject = (p: Project) => setProjects(prev => [p, ...prev]);
  const handleUpdateProject = (updated: Project) => setProjects(prev => prev.map(p => p.id === updated.id ? updated : p));
  const handleDeleteProject = (id: string) => setProjects(prev => prev.filter(p => p.id !== id));

  const handleAddArtisan = (a: Artisan) => setArtisans(prev => [a, ...prev]);
  const handleDeleteArtisan = (id: string) => setArtisans(prev => prev.filter(a => a.id !== id));

  const handleAddProfessional = (p: Professional) => setProfessionals(prev => [p, ...prev]);
  const handleDeleteProfessional = (id: string) => setProfessionals(prev => prev.filter(p => p.id !== id));

  const handleAddWarning = (w: Warning) => setWarnings(prev => [w, ...prev]);
  const handleDeleteWarning = (id: string) => setWarnings(prev => prev.filter(w => w.id !== id));

  const handleResetData = () => {
    if (confirm(lang === 'am' ? 'ሁሉንም መረጃዎች ወደ መጀመሪያው ይዘት መመለስ ይፈልጋሉ?' : 'Reset all data to default municipal state?')) {
      setProjects(INITIAL_PROJECTS);
      setArtisans(INITIAL_ARTISANS);
      setProfessionals(INITIAL_PROFESSIONALS);
      setWarnings(INITIAL_WARNINGS);
      setInstitution(INITIAL_INSTITUTION);
      localStorage.clear();
      setIsSettingsOpen(false);
    }
  };

  return (
    <div className={`min-h-screen flex bg-slate-950 text-slate-100 ${isDark ? 'dark' : ''}`}>
      {/* Responsive Navigation Sidebar / Bottom Bar */}
      <Navigation
        currentTab={currentTab}
        onSelectTab={setCurrentTab}
        lang={lang}
        warningCount={warnings.length}
      />

      {/* Main Content Viewport */}
      <div className="flex-1 flex flex-col min-w-0 min-h-screen overflow-x-hidden">
        {/* Responsive Header */}
        <Header
          institution={institution}
          lang={lang}
          onToggleLang={() => setLang(prev => prev === 'am' ? 'en' : 'am')}
          isDark={isDark}
          onToggleTheme={() => setIsDark(prev => !prev)}
          isOnline={isOnline}
          onOpenSettings={() => setIsSettingsOpen(true)}
          installPrompt={installPrompt}
          onInstallApp={handleInstallApp}
        />

        {/* Dynamic View Panel with padding for bottom nav on mobile */}
        <main className="flex-1 p-4 md:p-6 pb-20 md:pb-6 overflow-y-auto">
          {currentTab === 'dashboard' && (
            <Dashboard
              projects={projects}
              artisans={artisans}
              professionals={professionals}
              warnings={warnings}
              institution={institution}
              lang={lang}
              onNavigate={setCurrentTab}
              onOpenAddProject={() => setCurrentTab('projects')}
              onOpenAddArtisan={() => setCurrentTab('artisans')}
              onOpenAddWarning={() => setCurrentTab('warnings')}
            />
          )}

          {currentTab === 'projects' && (
            <ProjectsView
              projects={projects}
              lang={lang}
              onAddProject={handleAddProject}
              onUpdateProject={handleUpdateProject}
              onDeleteProject={handleDeleteProject}
            />
          )}

          {currentTab === 'artisans' && (
            <ArtisansView
              artisans={artisans}
              lang={lang}
              onAddArtisan={handleAddArtisan}
              onDeleteArtisan={handleDeleteArtisan}
              onSendWarning={(a) => {
                setCurrentTab('warnings');
              }}
            />
          )}

          {currentTab === 'professionals' && (
            <ProfessionalsView
              professionals={professionals}
              lang={lang}
              onAddProfessional={handleAddProfessional}
              onDeleteProfessional={handleDeleteProfessional}
            />
          )}

          {currentTab === 'ai' && (
            <GeminiAiChat
              lang={lang}
            />
          )}

          {currentTab === 'reports' && (
            <ReportsCenter
              projects={projects}
              artisans={artisans}
              professionals={professionals}
              warnings={warnings}
              institution={institution}
              lang={lang}
            />
          )}

          {currentTab === 'warnings' && (
            <WarningsView
              warnings={warnings}
              lang={lang}
              onAddWarning={handleAddWarning}
              onDeleteWarning={handleDeleteWarning}
            />
          )}
        </main>
      </div>

      {/* Global Settings Modal */}
      <SettingsModal
        isOpen={isSettingsOpen}
        onClose={() => setIsSettingsOpen(false)}
        institution={institution}
        onUpdateInstitution={setInstitution}
        lang={lang}
        onToggleLang={() => setLang(prev => prev === 'am' ? 'en' : 'am')}
        isDark={isDark}
        onToggleTheme={() => setIsDark(prev => !prev)}
        onResetData={handleResetData}
      />
    </div>
  );
};
export default App;
