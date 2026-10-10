import React from 'react';
import { Globe, Download, Settings as SettingsIcon, Wifi, WifiOff, Moon, Sun } from 'lucide-react';
import { InstitutionProfile } from '../types';

interface HeaderProps {
  institution: InstitutionProfile;
  lang: 'am' | 'en';
  onToggleLang: () => void;
  isDark: boolean;
  onToggleTheme: () => void;
  isOnline: boolean;
  onOpenSettings: () => void;
  installPrompt: any;
  onInstallApp: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  institution,
  lang,
  onToggleLang,
  isDark,
  onToggleTheme,
  isOnline,
  onOpenSettings,
  installPrompt,
  onInstallApp
}) => {
  return (
    <header className="bg-slate-900/90 backdrop-blur-md border-b border-slate-800 sticky top-0 z-40 px-4 py-3">
      <div className="max-w-7xl mx-auto flex items-center justify-between gap-4">
        {/* Mobile Header Title / Desktop Department info */}
        <div className="flex items-center gap-3">
          <div className="md:hidden w-8 h-8 rounded-lg bg-ethiopia-green flex items-center justify-center font-bold text-white text-xs">
            ቲሊ
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-sm md:text-base font-bold text-slate-100 line-clamp-1">
                {institution.name}
              </h2>
              <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                isOnline ? 'bg-emerald-950 text-emerald-400 border border-emerald-800' : 'bg-rose-950 text-rose-400 border border-rose-800'
              }`}>
                {isOnline ? (
                  <>
                    <Wifi className="w-3 h-3 mr-1" />
                    <span>{lang === 'am' ? 'ኦንላይን' : 'Online'}</span>
                  </>
                ) : (
                  <>
                    <WifiOff className="w-3 h-3 mr-1" />
                    <span>{lang === 'am' ? 'ከመስመር ውጭ' : 'Offline'}</span>
                  </>
                )}
              </span>
            </div>
            <p className="text-xs text-slate-400 line-clamp-1 hidden sm:block">
              {institution.department} • {institution.region}
            </p>
          </div>
        </div>

        {/* Action Controls */}
        <div className="flex items-center gap-2">
          {/* PWA Install Button (If browser triggered beforeinstallprompt) */}
          {installPrompt && (
            <button
              onClick={onInstallApp}
              className="flex items-center gap-1.5 bg-ethiopia-yellow text-slate-950 px-3 py-1.5 rounded-lg text-xs font-bold shadow-md hover:bg-yellow-400 transition-all animate-pulse"
              title="Install Web App / PWA"
            >
              <Download className="w-3.5 h-3.5" />
              <span className="hidden sm:inline">{lang === 'am' ? 'መተግበሪያውን ጫን' : 'Install PWA'}</span>
            </button>
          )}

          {/* Language Switcher */}
          <button
            onClick={onToggleLang}
            className="flex items-center gap-1 bg-slate-800 hover:bg-slate-700 text-slate-200 px-2.5 py-1.5 rounded-lg text-xs font-medium border border-slate-700 transition-colors"
            title="Toggle Language"
          >
            <Globe className="w-3.5 h-3.5 text-ethiopia-yellow" />
            <span className="font-semibold">{lang === 'am' ? 'English' : 'አማርኛ'}</span>
          </button>

          {/* Dark / Light Toggle */}
          <button
            onClick={onToggleTheme}
            className="p-2 bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white rounded-lg border border-slate-700 transition-colors"
            title="Toggle Theme"
          >
            {isDark ? <Sun className="w-4 h-4 text-amber-400" /> : <Moon className="w-4 h-4 text-blue-400" />}
          </button>

          {/* Settings Trigger */}
          <button
            onClick={onOpenSettings}
            className="p-2 bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white rounded-lg border border-slate-700 transition-colors"
            title="System Settings"
          >
            <SettingsIcon className="w-4 h-4" />
          </button>
        </div>
      </div>
    </header>
  );
};
