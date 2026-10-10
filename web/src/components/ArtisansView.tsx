import React, { useState } from 'react';
import { 
  HardHat, 
  Search, 
  PlusCircle, 
  Phone, 
  Award, 
  AlertTriangle, 
  CheckCircle2, 
  Trash2,
  Briefcase
} from 'lucide-react';
import { Artisan } from '../types';

interface ArtisansViewProps {
  artisans: Artisan[];
  lang: 'am' | 'en';
  onAddArtisan: (artisan: Artisan) => void;
  onDeleteArtisan: (id: string) => void;
  onSendWarning: (artisan: Artisan) => void;
}

export const ArtisansView: React.FC<ArtisansViewProps> = ({
  artisans,
  lang,
  onAddArtisan,
  onDeleteArtisan,
  onSendWarning
}) => {
  const [search, setSearch] = useState('');
  const [showModal, setShowModal] = useState(false);

  // Form State
  const [fullName, setFullName] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [tradeSector, setTradeSector] = useState('የግንበኝነት ስራ (Masonry)');
  const [cocLevel, setCocLevel] = useState('ደረጃ 3 (Level III COC)');
  const [experienceYears, setExperienceYears] = useState(5);
  const [projectAssigned, setProjectAssigned] = useState('PRJ-001 (የማዘጋጃ ቤት ህንፃ)');

  const filtered = artisans.filter(a => 
    a.fullName.toLowerCase().includes(search.toLowerCase()) ||
    a.tradeSector.toLowerCase().includes(search.toLowerCase()) ||
    a.phoneNumber.includes(search)
  );

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!fullName || !phoneNumber) return;

    const newArtisan: Artisan = {
      id: `ART-${100 + artisans.length + 1}`,
      fullName,
      phoneNumber,
      tradeSector,
      cocLevel,
      experienceYears: Number(experienceYears) || 1,
      projectAssigned,
      warningCount: 0,
      isActive: true
    };

    onAddArtisan(newArtisan);
    setShowModal(false);
    setFullName('');
    setPhoneNumber('');
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto pb-12">
      {/* Header bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-slate-900 border border-slate-800 p-5 rounded-2xl">
        <div>
          <h1 className="text-xl font-bold text-white flex items-center gap-2">
            <HardHat className="w-5 h-5 text-amber-400" />
            <span>{lang === 'am' ? 'የተመሰከረላቸው የልምድ ሰራተኞች መዝገብ (Artisans)' : 'Certified Artisans & Trade Workers'}</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            {lang === 'am' ? 'የCOC የሙያ ብቃት ማረጋገጫ ያላቸው የግንበኞች፣ የብረት አሰሪዎች እና የኤሌክትሪክ ባለሙያዎች' : 'Certified skilled workforce with validated COC standards and project assignments'}
          </p>
        </div>

        <button
          onClick={() => setShowModal(true)}
          className="flex items-center gap-2 bg-ethiopia-green hover:bg-emerald-600 text-white px-4 py-2.5 rounded-xl text-sm font-semibold shadow-md shadow-ethiopia-green/20 transition-all shrink-0"
        >
          <PlusCircle className="w-4 h-4" />
          <span>{lang === 'am' ? 'አዲስ ሰራተኛ መዝግብ' : 'Add Artisan'}</span>
        </button>
      </div>

      {/* Search Input */}
      <div className="relative bg-slate-900 border border-slate-800 p-3 rounded-xl">
        <Search className="w-4 h-4 absolute left-6 top-5 text-slate-400" />
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder={lang === 'am' ? 'በስም፣ በስልክ ቁጥር ወይም በሙያ ዘርፍ ፈልግ...' : 'Search by name, phone, trade...'}
          className="w-full bg-slate-850 border border-slate-700 rounded-lg pl-9 pr-4 py-2 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-amber-400"
        />
      </div>

      {/* Artisans Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {filtered.map((a) => (
          <div
            key={a.id}
            className="bg-slate-900 border border-slate-800 rounded-2xl p-5 hover:border-slate-700 transition-all flex flex-col justify-between shadow-sm"
          >
            <div>
              <div className="flex items-center justify-between gap-2 mb-2">
                <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-slate-800 text-amber-400 border border-slate-700">
                  {a.id}
                </span>
                <span className="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-emerald-950 text-emerald-400 border border-emerald-800 flex items-center gap-1">
                  <CheckCircle2 className="w-3 h-3" />
                  <span>{a.cocLevel.split(' ')[0]}</span>
                </span>
              </div>

              <h3 className="font-bold text-base text-white">{a.fullName}</h3>
              <p className="text-xs text-amber-400 font-medium mt-0.5 flex items-center gap-1">
                <Briefcase className="w-3.5 h-3.5" />
                <span>{a.tradeSector}</span>
              </p>

              <div className="space-y-1.5 mt-4 pt-3 border-t border-slate-800 text-xs">
                <div className="flex items-center justify-between text-slate-300">
                  <span className="text-slate-500 flex items-center gap-1">
                    <Phone className="w-3 h-3" />
                    <span>{lang === 'am' ? 'ስልክ ቁጥር' : 'Phone'}</span>
                  </span>
                  <a href={`tel:${a.phoneNumber}`} className="text-blue-400 hover:underline font-mono">
                    {a.phoneNumber}
                  </a>
                </div>

                <div className="flex items-center justify-between text-slate-300">
                  <span className="text-slate-500 flex items-center gap-1">
                    <Award className="w-3 h-3" />
                    <span>{lang === 'am' ? 'የስራ ልምድ' : 'Experience'}</span>
                  </span>
                  <span className="font-bold text-white">{a.experienceYears} {lang === 'am' ? 'ዓመታት' : 'Years'}</span>
                </div>

                <div className="text-slate-300 pt-1">
                  <span className="text-slate-500 text-[11px] block">{lang === 'am' ? 'የተመደበበት ፕሮጀክት' : 'Assigned Project'}</span>
                  <span className="text-slate-200 font-medium line-clamp-1">{a.projectAssigned}</span>
                </div>
              </div>
            </div>

            {/* Bottom Actions */}
            <div className="flex items-center justify-between mt-4 pt-3 border-t border-slate-800 text-xs">
              <button
                onClick={() => onSendWarning(a)}
                className="flex items-center gap-1 text-rose-400 hover:text-rose-300 font-medium"
              >
                <AlertTriangle className="w-3.5 h-3.5" />
                <span>{lang === 'am' ? 'ማስጠንቀቂያ ላክ' : 'Issue Notice'}</span>
              </button>

              <button
                onClick={() => onDeleteArtisan(a.id)}
                className="p-1.5 text-slate-500 hover:text-rose-400 rounded-lg transition-colors"
                title="Remove Artisan"
              >
                <Trash2 className="w-4 h-4" />
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* Add Artisan Modal */}
      {showModal && (
        <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-2xl max-w-md w-full p-6 shadow-2xl space-y-4">
            <h2 className="text-lg font-bold text-white flex items-center gap-2">
              <HardHat className="w-5 h-5 text-amber-400" />
              <span>{lang === 'am' ? 'አዲስ የልምድ ሰራተኛ መመዝገቢያ' : 'Register New Artisan'}</span>
            </h2>

            <form onSubmit={handleSubmit} className="space-y-3 text-xs">
              <div>
                <label className="block text-slate-300 mb-1 font-medium">{lang === 'am' ? 'ሙሉ ስም' : 'Full Name'}</label>
                <input
                  type="text"
                  required
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  placeholder="ለምሳሌ፡ ካሳሁን አያሌው ተሰማ"
                  className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-amber-400"
                />
              </div>

              <div>
                <label className="block text-slate-300 mb-1 font-medium">{lang === 'am' ? 'ስልክ ቁጥር' : 'Phone Number'}</label>
                <input
                  type="tel"
                  required
                  value={phoneNumber}
                  onChange={(e) => setPhoneNumber(e.target.value)}
                  placeholder="0918112233"
                  className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-amber-400"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-300 mb-1 font-medium">{lang === 'am' ? 'የሙያ ዘርፍ' : 'Trade Sector'}</label>
                  <select
                    value={tradeSector}
                    onChange={(e) => setTradeSector(e.target.value)}
                    className="w-full bg-slate-850 border border-slate-700 rounded-lg px-2 py-2 text-white focus:outline-none focus:border-amber-400"
                  >
                    <option value="የግንበኝነት ስራ (Masonry)">የግንበኝነት ስራ (Masonry)</option>
                    <option value="የብረት ማሰርና ኮንክሪት (Rebar)">የብረት ማሰርና ኮንክሪት (Rebar)</option>
                    <option value="የኤሌክትሪክ ዝርጋታ (Electrical)">የኤሌክትሪክ ዝርጋታ (Electrical)</option>
                    <option value="የቧንቧና ፍሳሽ (Plumbing)">የቧንቧና ፍሳሽ (Plumbing)</option>
                    <option value="የአናፂነትና ጣሪያ (Carpentry)">የአናፂነትና ጣሪያ (Carpentry)</option>
                  </select>
                </div>
                <div>
                  <label className="block text-slate-300 mb-1 font-medium">{lang === 'am' ? 'የCOC ደረጃ' : 'COC Level'}</label>
                  <select
                    value={cocLevel}
                    onChange={(e) => setCocLevel(e.target.value)}
                    className="w-full bg-slate-850 border border-slate-700 rounded-lg px-2 py-2 text-white focus:outline-none focus:border-amber-400"
                  >
                    <option value="ደረጃ 1 (Level I COC)">ደረጃ 1 (Level I)</option>
                    <option value="ደረጃ 2 (Level II COC)">ደረጃ 2 (Level II)</option>
                    <option value="ደረጃ 3 (Level III COC)">ደረጃ 3 (Level III)</option>
                    <option value="ደረጃ 4 (Level IV COC)">ደረጃ 4 (Level IV)</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-slate-300 mb-1 font-medium">{lang === 'am' ? 'የተመደበበት ፕሮጀክት' : 'Project Assignment'}</label>
                <input
                  type="text"
                  value={projectAssigned}
                  onChange={(e) => setProjectAssigned(e.target.value)}
                  className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-amber-400"
                />
              </div>

              <div className="flex items-center justify-end gap-2 pt-4 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 font-medium"
                >
                  {lang === 'am' ? 'ሰርዝ' : 'Cancel'}
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-lg bg-ethiopia-green hover:bg-emerald-600 text-white font-semibold shadow-md"
                >
                  {lang === 'am' ? 'መዝግብ' : 'Save Artisan'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
