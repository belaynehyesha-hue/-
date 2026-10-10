import React, { useState } from 'react';
import { 
  Settings, 
  Globe, 
  Moon, 
  Sun, 
  Building2, 
  Check, 
  Sparkles, 
  RefreshCw,
  Info
} from 'lucide-react';
import { InstitutionProfile } from '../types';

interface SettingsModalProps {
  isOpen: boolean;
  onClose: () => void;
  institution: InstitutionProfile;
  onUpdateInstitution: (profile: InstitutionProfile) => void;
  lang: 'am' | 'en';
  onToggleLang: () => void;
  isDark: boolean;
  onToggleTheme: () => void;
  onResetData: () => void;
}

export const SettingsModal: React.FC<SettingsModalProps> = ({
  isOpen,
  onClose,
  institution,
  onUpdateInstitution,
  lang,
  onToggleLang,
  isDark,
  onToggleTheme,
  onResetData
}) => {
  if (!isOpen) return null;

  const [officerName, setOfficerName] = useState(institution.officerName);
  const [department, setDepartment] = useState(institution.department);
  const [phone, setPhone] = useState(institution.officialPhone);
  const [saved, setSaved] = useState(false);

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    onUpdateInstitution({
      ...institution,
      officerName,
      department,
      officialPhone: phone
    });
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-slate-900 border border-slate-700 rounded-2xl max-w-lg w-full p-6 shadow-2xl space-y-5 max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between border-b border-slate-800 pb-3">
          <h2 className="text-base font-bold text-white flex items-center gap-2">
            <Settings className="w-5 h-5 text-ethiopia-yellow" />
            <span>{lang === 'am' ? 'የሲስተም ቅንብሮች (Settings)' : 'Platform Settings'}</span>
          </h2>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-white text-lg font-bold"
          >
            ✕
          </button>
        </div>

        {/* Display & Language Controls */}
        <div className="space-y-3">
          <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider">
            {lang === 'am' ? 'ቋንቋና ገጽታ' : 'Language & Display'}
          </h3>

          <div className="grid grid-cols-2 gap-3">
            <button
              onClick={onToggleLang}
              className="p-3 rounded-xl bg-slate-850 hover:bg-slate-800 border border-slate-700 flex items-center gap-2 text-xs text-white"
            >
              <Globe className="w-4 h-4 text-ethiopia-yellow" />
              <span>{lang === 'am' ? 'ቋንቋ፡ አማርኛ (ቀይር)' : 'Language: English (Switch)'}</span>
            </button>

            <button
              onClick={onToggleTheme}
              className="p-3 rounded-xl bg-slate-850 hover:bg-slate-800 border border-slate-700 flex items-center gap-2 text-xs text-white"
            >
              {isDark ? <Moon className="w-4 h-4 text-blue-400" /> : <Sun className="w-4 h-4 text-amber-400" />}
              <span>{isDark ? 'ገጽታ፡ Dark Slate' : 'Theme: Light Clean'}</span>
            </button>
          </div>
        </div>

        {/* Municipal Profile Form */}
        <form onSubmit={handleSave} className="space-y-3 pt-2">
          <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider">
            {lang === 'am' ? 'የተቆጣጣሪ ተቋም መረጃዎች' : 'Regulatory Office Credentials'}
          </h3>

          <div>
            <label className="block text-slate-300 text-xs mb-1 font-medium">
              {lang === 'am' ? 'የኃላፊው ሙሉ ስም' : 'Officer Name'}
            </label>
            <input
              type="text"
              value={officerName}
              onChange={(e) => setOfficerName(e.target.value)}
              className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-xs text-white focus:outline-none focus:border-ethiopia-yellow"
            />
          </div>

          <div>
            <label className="block text-slate-300 text-xs mb-1 font-medium">
              {lang === 'am' ? 'የስራ ሂደት / ክፍል' : 'Department'}
            </label>
            <input
              type="text"
              value={department}
              onChange={(e) => setDepartment(e.target.value)}
              className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-xs text-white focus:outline-none focus:border-ethiopia-yellow"
            />
          </div>

          <div>
            <label className="block text-slate-300 text-xs mb-1 font-medium">
              {lang === 'am' ? 'ይፋዊ ስልክ' : 'Official Phone'}
            </label>
            <input
              type="text"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-xs text-white focus:outline-none focus:border-ethiopia-yellow"
            />
          </div>

          <div className="flex items-center justify-between pt-2">
            <button
              type="submit"
              className="flex items-center gap-1.5 px-4 py-2 bg-ethiopia-green hover:bg-emerald-600 text-white rounded-lg text-xs font-semibold shadow-md"
            >
              {saved ? <Check className="w-3.5 h-3.5" /> : null}
              <span>{saved ? (lang === 'am' ? 'ተቀምጧል!' : 'Saved!') : (lang === 'am' ? 'መረጃዎችን አስቀምጥ' : 'Save Details')}</span>
            </button>

            <button
              type="button"
              onClick={onResetData}
              className="flex items-center gap-1.5 text-xs text-slate-400 hover:text-amber-400 p-2 rounded-lg"
              title="Reset to Municipal Defaults"
            >
              <RefreshCw className="w-3.5 h-3.5" />
              <span>{lang === 'am' ? 'ወደ መጀመሪያው መልስ' : 'Reset Defaults'}</span>
            </button>
          </div>
        </form>

        {/* PWA & System Info Box */}
        <div className="p-3 bg-slate-850 rounded-xl border border-slate-800 text-[11px] text-slate-400 space-y-1">
          <div className="flex items-center gap-1.5 font-bold text-slate-300">
            <Info className="w-3.5 h-3.5 text-blue-400" />
            <span>{lang === 'am' ? 'ስለ ሲስተሙ (PWA Platform Info)' : 'System Architecture'}</span>
          </div>
          <p>
            {lang === 'am' 
              ? 'ይህ መድረክ በAndroid፣ በኮምፒውተር Chrome/Edge፣ በMac Safari እና በLinux ብሮውዘሮች እንደ PWA መተግበሪያ ይሰራል፤ ከመስመር ውጭ (Offline) የመረጃ ምዝገባን ይደግፋል።' 
              : 'Cross-platform PWA running simultaneously on Android, Windows, Mac, and Linux browsers with offline service worker support.'}
          </p>
        </div>
      </div>
    </div>
  );
};
