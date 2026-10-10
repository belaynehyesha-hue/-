import React, { useState } from 'react';
import { 
  GraduationCap, 
  Search, 
  PlusCircle, 
  CheckCircle2, 
  AlertCircle, 
  ShieldCheck, 
  Trash2,
  FileCheck
} from 'lucide-react';
import { Professional } from '../types';

interface ProfessionalsViewProps {
  professionals: Professional[];
  lang: 'am' | 'en';
  onAddProfessional: (pro: Professional) => void;
  onDeleteProfessional: (id: string) => void;
}

export const ProfessionalsView: React.FC<ProfessionalsViewProps> = ({
  professionals,
  lang,
  onAddProfessional,
  onDeleteProfessional
}) => {
  const [search, setSearch] = useState('');
  const [showModal, setShowModal] = useState(false);

  // Form State
  const [fullName, setFullName] = useState('');
  const [profession, setProfession] = useState('ዋና ሲቪል መሐንዲስ (Civil Engineer)');
  const [licenseNumber, setLicenseNumber] = useState('');
  const [licenseLevel, setLicenseLevel] = useState('ከፍተኛ አማካሪ መሐንዲስ (PE)');
  const [assignedProject, setAssignedProject] = useState('የማዘጋጃ ቤት አዲሱ ህንፃ');
  const [expiryDate, setExpiryDate] = useState('2027-12-31');

  const filtered = professionals.filter(p =>
    p.fullName.toLowerCase().includes(search.toLowerCase()) ||
    p.profession.toLowerCase().includes(search.toLowerCase()) ||
    p.licenseNumber.toLowerCase().includes(search.toLowerCase())
  );

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!fullName || !licenseNumber) return;

    const newPro: Professional = {
      id: `ENG-0${professionals.length + 1}`,
      fullName,
      profession,
      licenseNumber,
      licenseLevel,
      assignedProject,
      expiryDate,
      status: 'valid'
    };

    onAddProfessional(newPro);
    setShowModal(false);
    setFullName('');
    setLicenseNumber('');
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto pb-12">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-slate-900 border border-slate-800 p-5 rounded-2xl">
        <div>
          <h1 className="text-xl font-bold text-white flex items-center gap-2">
            <GraduationCap className="w-5 h-5 text-emerald-400" />
            <span>{lang === 'am' ? 'የመሐንዲሶችና አርክቴክቶች የሙያ ፈቃድ መዝገብ' : 'Professional Engineers & Architects'}</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            {lang === 'am' ? 'በከተማና መሠረተ ልማት ሚኒስቴር የታደሰ የሙያ ፈቃድ ያላቸው ተቆጣጣሪዎችና ዲዛይነሮች' : 'Licensed professionals with verified Ministry of Urban Development accreditation'}
          </p>
        </div>

        <button
          onClick={() => setShowModal(true)}
          className="flex items-center gap-2 bg-ethiopia-green hover:bg-emerald-600 text-white px-4 py-2.5 rounded-xl text-sm font-semibold shadow-md shadow-ethiopia-green/20 transition-all shrink-0"
        >
          <PlusCircle className="w-4 h-4" />
          <span>{lang === 'am' ? 'አዲስ ባለሙያ መዝግብ' : 'Add Professional'}</span>
        </button>
      </div>

      {/* Search */}
      <div className="relative bg-slate-900 border border-slate-800 p-3 rounded-xl">
        <Search className="w-4 h-4 absolute left-6 top-5 text-slate-400" />
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder={lang === 'am' ? 'በስም፣ በሙያ ወይም በፈቃድ ቁጥር ፈልግ...' : 'Search by name, profession, license...'}
          className="w-full bg-slate-850 border border-slate-700 rounded-lg pl-9 pr-4 py-2 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-emerald-400"
        />
      </div>

      {/* Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {filtered.map((p) => (
          <div
            key={p.id}
            className="bg-slate-900 border border-slate-800 rounded-2xl p-5 hover:border-slate-700 transition-all flex flex-col justify-between shadow-sm"
          >
            <div>
              <div className="flex items-center justify-between gap-2 mb-2">
                <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-slate-800 text-emerald-400 border border-slate-700">
                  {p.id}
                </span>
                <span className={`text-[10px] font-semibold px-2.5 py-0.5 rounded-full flex items-center gap-1 ${
                  p.status === 'valid'
                    ? 'bg-emerald-950 text-emerald-400 border border-emerald-800'
                    : 'bg-amber-950 text-amber-400 border border-amber-800'
                }`}>
                  {p.status === 'valid' ? <CheckCircle2 className="w-3 h-3" /> : <AlertCircle className="w-3 h-3" />}
                  <span>{p.status === 'valid' ? (lang === 'am' ? 'ሕጋዊ ፈቃድ' : 'Valid') : (lang === 'am' ? 'በኦዲት ላይ' : 'Under Audit')}</span>
                </span>
              </div>

              <h3 className="font-bold text-base text-white">{p.fullName}</h3>
              <p className="text-xs text-emerald-400 font-medium mt-0.5">{p.profession}</p>

              <div className="space-y-1.5 mt-4 pt-3 border-t border-slate-800 text-xs">
                <div className="flex items-center justify-between text-slate-300">
                  <span className="text-slate-500 flex items-center gap-1">
                    <FileCheck className="w-3 h-3" />
                    <span>{lang === 'am' ? 'የፈቃድ ቁጥር' : 'License No'}</span>
                  </span>
                  <span className="font-mono text-slate-200 font-semibold">{p.licenseNumber}</span>
                </div>

                <div className="flex items-center justify-between text-slate-300">
                  <span className="text-slate-500 flex items-center gap-1">
                    <ShieldCheck className="w-3 h-3" />
                    <span>{lang === 'am' ? 'የሙያ ደረጃ' : 'Grade'}</span>
                  </span>
                  <span className="text-white font-medium">{p.licenseLevel}</span>
                </div>

                <div className="flex items-center justify-between text-slate-300">
                  <span className="text-slate-500">{lang === 'am' ? 'የሚያበቃበት ቀን' : 'Expires'}</span>
                  <span className="text-slate-300 font-mono">{p.expiryDate}</span>
                </div>

                <div className="text-slate-300 pt-1">
                  <span className="text-slate-500 text-[11px] block">{lang === 'am' ? 'የተመደበበት ኃላፊነት' : 'Assignment'}</span>
                  <span className="text-slate-200 font-medium line-clamp-1">{p.assignedProject}</span>
                </div>
              </div>
            </div>

            <div className="flex items-center justify-end mt-4 pt-3 border-t border-slate-800">
              <button
                onClick={() => onDeleteProfessional(p.id)}
                className="p-1.5 text-slate-500 hover:text-rose-400 rounded-lg transition-colors"
                title="Delete Professional"
              >
                <Trash2 className="w-4 h-4" />
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* Add Modal */}
      {showModal && (
        <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-2xl max-w-md w-full p-6 shadow-2xl space-y-4">
            <h2 className="text-lg font-bold text-white flex items-center gap-2">
              <GraduationCap className="w-5 h-5 text-emerald-400" />
              <span>{lang === 'am' ? 'አዲስ መሐንዲስ / አርክቴክት መመዝገቢያ' : 'Register New Professional'}</span>
            </h2>

            <form onSubmit={handleSubmit} className="space-y-3 text-xs">
              <div>
                <label className="block text-slate-300 mb-1 font-medium">{lang === 'am' ? 'ሙሉ ስም' : 'Full Name'}</label>
                <input
                  type="text"
                  required
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  placeholder="ለምሳሌ፡ ኢንጅነር ዳዊት መኮንን"
                  className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-emerald-400"
                />
              </div>

              <div>
                <label className="block text-slate-300 mb-1 font-medium">{lang === 'am' ? 'የሙያ መስክ' : 'Profession'}</label>
                <input
                  type="text"
                  required
                  value={profession}
                  onChange={(e) => setProfession(e.target.value)}
                  className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-emerald-400"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-300 mb-1 font-medium">{lang === 'am' ? 'የፈቃድ ቁጥር' : 'License Number'}</label>
                  <input
                    type="text"
                    required
                    value={licenseNumber}
                    onChange={(e) => setLicenseNumber(e.target.value)}
                    placeholder="MOTI-SE-12345/16"
                    className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-emerald-400"
                  />
                </div>
                <div>
                  <label className="block text-slate-300 mb-1 font-medium">{lang === 'am' ? 'የሚያበቃበት ቀን' : 'Expiry Date'}</label>
                  <input
                    type="date"
                    value={expiryDate}
                    onChange={(e) => setExpiryDate(e.target.value)}
                    className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-emerald-400"
                  />
                </div>
              </div>

              <div>
                <label className="block text-slate-300 mb-1 font-medium">{lang === 'am' ? 'የተመደበበት ኃላፊነት' : 'Project Assignment'}</label>
                <input
                  type="text"
                  value={assignedProject}
                  onChange={(e) => setAssignedProject(e.target.value)}
                  className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-emerald-400"
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
                  {lang === 'am' ? 'መዝግብ' : 'Save'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
