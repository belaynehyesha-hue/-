import React, { useState } from 'react';
import { 
  AlertTriangle, 
  Search, 
  PlusCircle, 
  Send, 
  Trash2, 
  CheckCircle2, 
  PhoneCall,
  Calendar
} from 'lucide-react';
import { Warning } from '../types';

interface WarningsViewProps {
  warnings: Warning[];
  lang: 'am' | 'en';
  onAddWarning: (warning: Warning) => void;
  onDeleteWarning: (id: string) => void;
}

export const WarningsView: React.FC<WarningsViewProps> = ({
  warnings,
  lang,
  onAddWarning,
  onDeleteWarning
}) => {
  const [search, setSearch] = useState('');
  const [showModal, setShowModal] = useState(false);

  // Form State
  const [recipientName, setRecipientName] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [sector, setSector] = useState('ኮንትራክተር (Contractor)');
  const [reason, setReason] = useState('');
  const [severity, setSeverity] = useState<'low' | 'medium' | 'high' | 'critical'>('medium');

  const filtered = warnings.filter(w =>
    w.recipientName.toLowerCase().includes(search.toLowerCase()) ||
    w.sector.toLowerCase().includes(search.toLowerCase()) ||
    w.phoneNumber.includes(search) ||
    w.reason.toLowerCase().includes(search.toLowerCase())
  );

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!recipientName || !reason) return;

    const newWarning: Warning = {
      id: `WRN-${500 + warnings.length + 1}`,
      recipientName,
      phoneNumber: phoneNumber || '0918000000',
      sector,
      reason,
      severity,
      issuedDate: new Date().toISOString().split('T')[0]
    };

    onAddWarning(newWarning);
    setShowModal(false);
    setRecipientName('');
    setReason('');
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto pb-12">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-slate-900 border border-slate-800 p-5 rounded-2xl">
        <div>
          <h1 className="text-xl font-bold text-white flex items-center gap-2">
            <AlertTriangle className="w-5 h-5 text-rose-400" />
            <span>{lang === 'am' ? 'የግንባታ ሕግና ደንብ ማስጠንቀቂያዎች መዝገብ' : 'Regulatory Violations & Notices'}</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            {lang === 'am' ? 'በግንባታ ጥራት፣ በደህንነት ጉድለት እና በሊዝ ህግ ጥሰት የተሰጡ ይፋዊ የጽሁፍና የኤስኤምኤስ ማስጠንቀቂያዎች' : 'Formal compliance warnings issued under EBCS safety and municipal regulations'}
          </p>
        </div>

        <button
          onClick={() => setShowModal(true)}
          className="flex items-center gap-2 bg-rose-600 hover:bg-rose-500 text-white px-4 py-2.5 rounded-xl text-sm font-semibold shadow-md shadow-rose-600/20 transition-all shrink-0"
        >
          <PlusCircle className="w-4 h-4" />
          <span>{lang === 'am' ? 'አዲስ ማስጠንቀቂያ ስጥ' : 'Issue Notice'}</span>
        </button>
      </div>

      {/* Search */}
      <div className="relative bg-slate-900 border border-slate-800 p-3 rounded-xl">
        <Search className="w-4 h-4 absolute left-6 top-5 text-slate-400" />
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder={lang === 'am' ? 'በተቀባይ ስም፣ በዘርፍ ወይም በምክንያት ፈልግ...' : 'Search by recipient, sector, reason...'}
          className="w-full bg-slate-850 border border-slate-700 rounded-lg pl-9 pr-4 py-2 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-rose-400"
        />
      </div>

      {/* Warnings List */}
      <div className="space-y-3">
        {filtered.map((w) => (
          <div
            key={w.id}
            className="bg-slate-900 border border-slate-800 hover:border-slate-700 rounded-2xl p-5 shadow-sm transition-all"
          >
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-2">
              <div className="flex items-center gap-2">
                <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-slate-800 text-rose-400 border border-slate-700">
                  {w.id}
                </span>
                <span className="font-bold text-base text-white">{w.recipientName}</span>
                <span className="text-xs text-slate-400">({w.sector})</span>
              </div>

              <div className="flex items-center gap-3">
                <span className={`text-[10px] font-bold px-2.5 py-0.5 rounded-full ${
                  w.severity === 'critical'
                    ? 'bg-rose-950 text-rose-300 border border-rose-800 animate-pulse'
                    : w.severity === 'high'
                    ? 'bg-rose-950 text-rose-400 border border-rose-800'
                    : w.severity === 'medium'
                    ? 'bg-amber-950 text-amber-400 border border-amber-800'
                    : 'bg-slate-800 text-slate-300'
                }`}>
                  {w.severity.toUpperCase()}
                </span>
                <span className="text-xs text-slate-400 flex items-center gap-1">
                  <Calendar className="w-3.5 h-3.5" />
                  <span>{w.issuedDate}</span>
                </span>
              </div>
            </div>

            <p className="text-xs text-slate-300 bg-slate-850 p-3 rounded-xl border border-slate-800 mt-2 leading-relaxed">
              {w.reason}
            </p>

            <div className="flex items-center justify-between mt-3 pt-3 border-t border-slate-800 text-xs">
              <div className="flex items-center gap-2 text-slate-400">
                <PhoneCall className="w-3.5 h-3.5 text-slate-500" />
                <span className="font-mono">{w.phoneNumber}</span>
              </div>

              <div className="flex items-center gap-2">
                <button
                  onClick={() => alert(lang === 'am' ? `ማስጠንቀቂያው ለ ${w.recipientName} በኤስኤምኤስ ተልኳል!` : `SMS notice re-sent to ${w.recipientName}`)}
                  className="flex items-center gap-1 text-xs text-blue-400 hover:text-blue-300 font-semibold"
                >
                  <Send className="w-3.5 h-3.5" />
                  <span>{lang === 'am' ? 'በSMS ድገም' : 'Resend SMS'}</span>
                </button>
                <button
                  onClick={() => onDeleteWarning(w.id)}
                  className="p-1.5 text-slate-500 hover:text-rose-400 transition-colors"
                  title="Delete Notice"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Add Warning Modal */}
      {showModal && (
        <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-2xl max-w-md w-full p-6 shadow-2xl space-y-4">
            <h2 className="text-lg font-bold text-white flex items-center gap-2">
              <AlertTriangle className="w-5 h-5 text-rose-400" />
              <span>{lang === 'am' ? 'አዲስ ይፋዊ ማስጠንቀቂያ መዝግብ' : 'Issue Regulatory Notice'}</span>
            </h2>

            <form onSubmit={handleSubmit} className="space-y-3 text-xs">
              <div>
                <label className="block text-slate-300 mb-1 font-medium">{lang === 'am' ? 'የተቀባይ ስም / ድርጅት' : 'Recipient / Contractor'}</label>
                <input
                  type="text"
                  required
                  value={recipientName}
                  onChange={(e) => setRecipientName(e.target.value)}
                  placeholder="ለምሳሌ፡ ብርሃን ኮንስትራክሽን"
                  className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-rose-400"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-300 mb-1 font-medium">{lang === 'am' ? 'ስልክ ቁጥር' : 'Phone'}</label>
                  <input
                    type="tel"
                    value={phoneNumber}
                    onChange={(e) => setPhoneNumber(e.target.value)}
                    placeholder="0918000000"
                    className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-rose-400"
                  />
                </div>
                <div>
                  <label className="block text-slate-300 mb-1 font-medium">{lang === 'am' ? 'የክብደት ደረጃ' : 'Severity'}</label>
                  <select
                    value={severity}
                    onChange={(e) => setSeverity(e.target.value as any)}
                    className="w-full bg-slate-850 border border-slate-700 rounded-lg px-2 py-2 text-white focus:outline-none focus:border-rose-400"
                  >
                    <option value="low">መካከለኛ (Low)</option>
                    <option value="medium">አስፈላጊ (Medium)</option>
                    <option value="high">ከፍተኛ (High)</option>
                    <option value="critical">አስቸኳይ (Critical)</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-slate-300 mb-1 font-medium">{lang === 'am' ? 'የጥሰቱ ምክንያትና ዝርዝር' : 'Reason / Violation Details'}</label>
                <textarea
                  required
                  rows={3}
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  placeholder="የተፈፀመውን የኮድ ወይም የደህንነት ጥሰት ይግለጹ..."
                  className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-rose-400"
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
                  className="px-4 py-2 rounded-lg bg-rose-600 hover:bg-rose-500 text-white font-semibold shadow-md"
                >
                  {lang === 'am' ? 'ማስጠንቀቂያ ስጥ' : 'Issue Notice'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
