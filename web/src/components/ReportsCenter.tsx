import React, { useState } from 'react';
import { 
  FileSpreadsheet, 
  FileText, 
  Download, 
  Share2, 
  Printer, 
  Check, 
  Loader2, 
  Building2, 
  ShieldCheck,
  Send,
  AlertCircle
} from 'lucide-react';
import { Project, Artisan, Professional, Warning, InstitutionProfile } from '../types';

interface ReportsCenterProps {
  projects: Project[];
  artisans: Artisan[];
  professionals: Professional[];
  warnings: Warning[];
  institution: InstitutionProfile;
  lang: 'am' | 'en';
}

export const ReportsCenter: React.FC<ReportsCenterProps> = ({
  projects,
  artisans,
  professionals,
  warnings,
  institution,
  lang
}) => {
  const [exportType, setExportType] = useState<'all' | 'projects' | 'artisans' | 'compliance'>('all');
  const [format, setFormat] = useState<'csv' | 'pdf' | 'json'>('csv');
  const [isProcessing, setIsProcessing] = useState(false);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  // Sharing Confirmation Dialog State
  const [showShareDialog, setShowShareDialog] = useState(false);
  const [shareFormat, setShareFormat] = useState<'csv' | 'json' | 'summary'>('summary');
  const [shareProgress, setShareProgress] = useState<number>(0);
  const [isSharing, setIsSharing] = useState(false);

  // Generate CSV Data String
  const generateCSV = (): string => {
    let csv = '';
    
    if (exportType === 'all' || exportType === 'projects') {
      csv += '--- PROJECTS SUMMARY (የግንባታ ፕሮጀክቶች መዝገብ) ---\n';
      csv += 'Project ID,Name,Contractor,Consultant,Resident Engineer,Location,Budget ETB,Progress %,EBCS Status\n';
      projects.forEach(p => {
        csv += `"${p.id}","${p.name}","${p.contractor}","${p.consultant}","${p.residentEngineer}","${p.location}",${p.budgetETB},${p.progress}%,"${p.ebcsStatus}"\n`;
      });
      csv += '\n';
    }

    if (exportType === 'all' || exportType === 'artisans') {
      csv += '--- ARTISANS & TRADE WORKERS (የተመሰከረላቸው የልምድ ሰራተኞች) ---\n';
      csv += 'Artisan ID,Full Name,Phone,Trade Sector,COC Level,Experience Years,Assigned Project,Warnings\n';
      artisans.forEach(a => {
        csv += `"${a.id}","${a.fullName}","${a.phoneNumber}","${a.tradeSector}","${a.cocLevel}",${a.experienceYears},"${a.projectAssigned}",${a.warningCount}\n`;
      });
      csv += '\n';
    }

    if (exportType === 'all' || exportType === 'compliance') {
      csv += '--- REGULATORY WARNINGS & NOTICES (የቁጥጥርና ማስጠንቀቂያ መዝገብ) ---\n';
      csv += 'Warning ID,Recipient,Phone,Sector,Reason,Severity,Issued Date\n';
      warnings.forEach(w => {
        csv += `"${w.id}","${w.recipientName}","${w.phoneNumber}","${w.sector}","${w.reason}","${w.severity}","${w.issuedDate}"\n`;
      });
    }

    return csv;
  };

  // Download Action
  const handleDownload = () => {
    setIsProcessing(true);
    setSuccessMsg(null);

    setTimeout(() => {
      try {
        let blob: Blob;
        let filename: string;

        if (format === 'csv') {
          const csvContent = "\uFEFF" + generateCSV(); // Add BOM for Excel UTF-8
          blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
          filename = `Tilli_Construction_Report_${new Date().toISOString().split('T')[0]}.csv`;
        } else if (format === 'json') {
          const data = {
            institution,
            exportedAt: new Date().toISOString(),
            projects: exportType === 'all' || exportType === 'projects' ? projects : undefined,
            artisans: exportType === 'all' || exportType === 'artisans' ? artisans : undefined,
            professionals: exportType === 'all' || exportType === 'compliance' ? professionals : undefined,
            warnings: exportType === 'all' || exportType === 'compliance' ? warnings : undefined,
          };
          blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
          filename = `Tilli_Construction_Export_${new Date().toISOString().split('T')[0]}.json`;
        } else {
          // Trigger Print Window as PDF
          window.print();
          setIsProcessing(false);
          setSuccessMsg(lang === 'am' ? 'የሕትመት መስኮት ተከፍቷል (Print to PDF)' : 'Print dialog opened');
          return;
        }

        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);

        setSuccessMsg(
          lang === 'am'
            ? `ፋይል "${filename}" በተሳካ ሁኔታ ወርዷል!`
            : `File "${filename}" downloaded successfully!`
        );
      } catch (err: any) {
        console.error('Download error:', err);
      } finally {
        setIsProcessing(false);
      }
    }, 600);
  };

  // Share via External App with Progress Confirmation
  const executeShare = () => {
    setIsSharing(true);
    setShareProgress(20);

    const interval = setInterval(() => {
      setShareProgress((prev) => {
        if (prev >= 90) {
          clearInterval(interval);
          return 90;
        }
        return prev + 25;
      });
    }, 200);

    setTimeout(async () => {
      clearInterval(interval);
      setShareProgress(100);

      const summaryText = `[የቲሊሊ ከተማ ኮንስትራክሽንና ከተማ ልማት ጽሕፈት ቤት ይፋዊ ሪፖርት]\n\n• ንቁ ፕሮጀክቶች፡ ${projects.length}\n• የተመዘገቡ የልምድ ሰራተኞች፡ ${artisans.length}\n• ፈቃድ ያላቸው መሐንዲሶች፡ ${professionals.length}\n• የቁጥጥር ማስጠንቀቂያዎች፡ ${warnings.length}\n\nኃላፊ፡ ${institution.officerName}\nቀን፡ ${new Date().toLocaleDateString('am-ET')}`;

      if (navigator.share) {
        try {
          await navigator.share({
            title: 'የቲሊሊ ከተማ ኮንስትራክሽን ሪፖርት',
            text: summaryText
          });
        } catch (err) {
          console.log('Share canceled or not allowed');
        }
      } else {
        navigator.clipboard.writeText(summaryText);
        alert(lang === 'am' ? 'የሪፖርቱ ማጠቃለያ ወደ ቅንጥብ ሰሌዳ ተቀድቷል (Clipboard)' : 'Report summary copied to clipboard');
      }

      setIsSharing(false);
      setShowShareDialog(false);
      setShareProgress(0);
      setSuccessMsg(lang === 'am' ? 'ሪፖርቱ በተሳካ ሁኔታ ተጋርቷል!' : 'Report shared successfully!');
    }, 900);
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto pb-12">
      {/* Header bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-slate-900 border border-slate-800 p-5 rounded-2xl">
        <div>
          <h1 className="text-xl font-bold text-white flex items-center gap-2">
            <FileSpreadsheet className="w-5 h-5 text-emerald-400" />
            <span>{lang === 'am' ? 'የሪፖርቶችና ዳታ ማውረጃ ማዕከል' : 'Reports & Export Center'}</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            {lang === 'am' ? 'የግንባታ ፕሮጀክቶች፣ የባለሙያዎችና የቁጥጥር መረጃዎችን በ Excel (CSV)፣ PDF እና JSON ማውረጃ' : 'Export official municipal logs to Excel/CSV, PDF or share with regulatory authorities'}
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => window.print()}
            className="flex items-center gap-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 px-3 py-2 rounded-xl text-xs font-semibold border border-slate-700 transition-colors"
          >
            <Printer className="w-4 h-4" />
            <span>{lang === 'am' ? 'አትም (Print)' : 'Print'}</span>
          </button>

          <button
            onClick={() => setShowShareDialog(true)}
            className="flex items-center gap-1.5 bg-blue-600 hover:bg-blue-500 text-white px-3.5 py-2 rounded-xl text-xs font-semibold shadow-md shadow-blue-600/20 transition-all"
          >
            <Share2 className="w-4 h-4" />
            <span>{lang === 'am' ? 'ሪፖርት አጋራ' : 'Share Report'}</span>
          </button>
        </div>
      </div>

      {/* Configuration & Export Box */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Left 2 Cols: Exporter Configuration */}
        <div className="md:col-span-2 bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-sm space-y-5">
          <h2 className="text-base font-bold text-white flex items-center gap-2">
            <Download className="w-4 h-4 text-ethiopia-yellow" />
            <span>{lang === 'am' ? 'የሪፖርት ማውጫ ምርጫዎች' : 'Export Parameters'}</span>
          </h2>

          {/* Data Category Selection */}
          <div>
            <label className="block text-xs font-medium text-slate-300 mb-2">
              {lang === 'am' ? 'የሪፖርት አይነት ምረጥ' : 'Select Data Scope'}
            </label>
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
              {[
                { id: 'all', labelAm: 'የተሟላ ሪፖርት', labelEn: 'All Data' },
                { id: 'projects', labelAm: 'ግንባታዎች ብቻ', labelEn: 'Projects' },
                { id: 'artisans', labelAm: 'የልምድ ሰራተኞች', labelEn: 'Artisans' },
                { id: 'compliance', labelAm: 'ማስጠንቀቂያዎች', labelEn: 'Compliance' },
              ].map((cat) => (
                <button
                  key={cat.id}
                  onClick={() => setExportType(cat.id as any)}
                  className={`p-3 rounded-xl border text-xs font-semibold text-center transition-all ${
                    exportType === cat.id
                      ? 'bg-ethiopia-green/20 text-ethiopia-yellow border-ethiopia-green'
                      : 'bg-slate-850 text-slate-400 border-slate-800 hover:bg-slate-800'
                  }`}
                >
                  {lang === 'am' ? cat.labelAm : cat.labelEn}
                </button>
              ))}
            </div>
          </div>

          {/* File Format Selection */}
          <div>
            <label className="block text-xs font-medium text-slate-300 mb-2">
              {lang === 'am' ? 'የፋይል ፎርማት (File Format)' : 'Select Format'}
            </label>
            <div className="grid grid-cols-3 gap-3">
              {[
                { id: 'csv', name: 'Excel / CSV (.csv)', icon: FileSpreadsheet, desc: 'UTF-8 with Excel BOM' },
                { id: 'pdf', name: 'Official PDF Document', icon: FileText, desc: 'Printable with letterhead' },
                { id: 'json', name: 'JSON Data (.json)', icon: Download, desc: 'System interoperability' },
              ].map((fmt) => {
                const Icon = fmt.icon;
                return (
                  <button
                    key={fmt.id}
                    onClick={() => setFormat(fmt.id as any)}
                    className={`p-3.5 rounded-xl border text-left transition-all ${
                      format === fmt.id
                        ? 'bg-blue-600/10 text-white border-blue-500 shadow-sm'
                        : 'bg-slate-850 text-slate-400 border-slate-800 hover:bg-slate-800'
                    }`}
                  >
                    <Icon className={`w-5 h-5 mb-1.5 ${format === fmt.id ? 'text-blue-400' : 'text-slate-500'}`} />
                    <span className="text-xs font-bold block text-slate-200">{fmt.name}</span>
                    <span className="text-[10px] text-slate-500 mt-0.5 block">{fmt.desc}</span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Download Execution Button */}
          <div className="pt-2">
            <button
              onClick={handleDownload}
              disabled={isProcessing}
              className="w-full py-3 bg-gradient-to-r from-ethiopia-green to-emerald-600 hover:from-emerald-600 hover:to-emerald-500 text-white font-bold rounded-xl shadow-lg shadow-ethiopia-green/20 flex items-center justify-center gap-2 text-sm transition-all"
            >
              {isProcessing ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  <span>{lang === 'am' ? 'ፋይሉ እየተዘጋጀ ነው...' : 'Generating Report...'}</span>
                </>
              ) : (
                <>
                  <Download className="w-4 h-4" />
                  <span>
                    {lang === 'am' 
                      ? `${format.toUpperCase()} ሪፖርት አውርድ` 
                      : `Download ${format.toUpperCase()} Report`}
                  </span>
                </>
              )}
            </button>
          </div>

          {/* Success Banner */}
          {successMsg && (
            <div className="bg-emerald-950/60 border border-emerald-800 text-emerald-300 p-3 rounded-xl text-xs flex items-center gap-2">
              <Check className="w-4 h-4 text-emerald-400 shrink-0" />
              <span>{successMsg}</span>
            </div>
          )}
        </div>

        {/* Right 1 Col: Live Municipal Letterhead Summary Preview */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-sm flex flex-col justify-between">
          <div>
            <div className="border-b border-slate-800 pb-3 mb-3 text-center">
              <div className="w-8 h-8 mx-auto rounded-lg bg-ethiopia-green flex items-center justify-center font-bold text-white text-xs mb-1">
                🇪🇹
              </div>
              <h3 className="text-xs font-bold text-white leading-tight">
                {institution.name}
              </h3>
              <p className="text-[10px] text-slate-400 mt-0.5">
                {institution.department}
              </p>
            </div>

            <div className="space-y-2 text-xs text-slate-300">
              <div className="flex justify-between py-1 border-b border-slate-850">
                <span className="text-slate-500">{lang === 'am' ? 'የፕሮጀክቶች ቁጥር' : 'Total Projects'}</span>
                <span className="font-bold text-white">{projects.length}</span>
              </div>
              <div className="flex justify-between py-1 border-b border-slate-850">
                <span className="text-slate-500">{lang === 'am' ? 'የልምድ ሰራተኞች' : 'Artisans'}</span>
                <span className="font-bold text-white">{artisans.length}</span>
              </div>
              <div className="flex justify-between py-1 border-b border-slate-850">
                <span className="text-slate-500">{lang === 'am' ? 'መሐንዲሶች' : 'Engineers'}</span>
                <span className="font-bold text-white">{professionals.length}</span>
              </div>
              <div className="flex justify-between py-1 border-b border-slate-850">
                <span className="text-slate-500">{lang === 'am' ? 'ማስጠንቀቂያዎች' : 'Warnings'}</span>
                <span className="font-bold text-rose-400">{warnings.length}</span>
              </div>
              <div className="flex justify-between py-1">
                <span className="text-slate-500">{lang === 'am' ? 'ኃላፊ' : 'Officer'}</span>
                <span className="font-medium text-slate-200">{institution.officerName}</span>
              </div>
            </div>
          </div>

          <div className="mt-6 p-3 rounded-xl bg-slate-850 border border-slate-850 text-[11px] text-slate-400 text-center">
            <ShieldCheck className="w-4 h-4 text-ethiopia-green mx-auto mb-1" />
            <span>{lang === 'am' ? 'ይፋዊ ማህተም እና ዲጂታል ፊርማ ያለው' : 'Official certified municipal register'}</span>
          </div>
        </div>
      </div>

      {/* External Share Confirmation Dialog with Progress Indicator */}
      {showShareDialog && (
        <div className="fixed inset-0 z-50 bg-black/75 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-2xl max-w-md w-full p-6 shadow-2xl space-y-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-blue-600/20 border border-blue-500/30 flex items-center justify-center text-blue-400">
                <Share2 className="w-5 h-5" />
              </div>
              <div>
                <h3 className="font-bold text-base text-white">
                  {lang === 'am' ? 'ሪፖርት ወደ ውጪ መተግበሪያ ማጋሪያ' : 'Share Report Confirmation'}
                </h3>
                <p className="text-xs text-slate-400">
                  {lang === 'am' ? 'የሚላከውን የፋይል ቅርጽና አድራሻ ያረጋግጡ' : 'Confirm format before sending via external apps'}
                </p>
              </div>
            </div>

            <div className="p-3 bg-slate-850 rounded-xl border border-slate-800 text-xs space-y-2">
              <div className="flex justify-between">
                <span className="text-slate-400">{lang === 'am' ? 'የሚላክበት ፎርማት፡' : 'Format:'}</span>
                <span className="font-bold text-ethiopia-yellow">
                  {shareFormat === 'summary' ? 'Official Municipal Summary (Text)' : `${shareFormat.toUpperCase()} File`}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">{lang === 'am' ? 'የፕሮጀክቶች ብዛት፡' : 'Projects Included:'}</span>
                <span className="font-semibold text-white">{projects.length} ፕሮጀክቶች</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">{lang === 'am' ? 'የተረጋገጡ ምንጮች፡' : 'Standards:'}</span>
                <span className="text-emerald-400 font-semibold">EBCS-1 to 14 & Proclamation 721</span>
              </div>
            </div>

            {/* Format choice in dialog */}
            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1.5">
                {lang === 'am' ? 'የማጋሪያ ቅርፅ ይምረጡ' : 'Choose Share Payload'}
              </label>
              <div className="grid grid-cols-3 gap-2">
                {[
                  { id: 'summary', name: 'Text Summary' },
                  { id: 'csv', name: 'CSV File' },
                  { id: 'json', name: 'JSON Export' },
                ].map((s) => (
                  <button
                    key={s.id}
                    onClick={() => setShareFormat(s.id as any)}
                    className={`py-2 px-2 rounded-lg text-xs font-medium border text-center transition-all ${
                      shareFormat === s.id
                        ? 'bg-blue-600 text-white border-blue-500'
                        : 'bg-slate-800 text-slate-400 border-slate-700'
                    }`}
                  >
                    {s.name}
                  </button>
                ))}
              </div>
            </div>

            {/* Progress Indicator when Sharing */}
            {isSharing && (
              <div className="space-y-1.5 pt-2">
                <div className="flex justify-between text-xs text-slate-400">
                  <span>{lang === 'am' ? 'ፋይሉ እየተዘጋጀ ነው...' : 'Preparing payload...'}</span>
                  <span className="font-bold text-white">{shareProgress}%</span>
                </div>
                <div className="w-full bg-slate-950 h-2 rounded-full overflow-hidden">
                  <div 
                    className="bg-blue-500 h-full rounded-full transition-all duration-200"
                    style={{ width: `${shareProgress}%` }}
                  ></div>
                </div>
              </div>
            )}

            {/* Dialog Footer Actions */}
            <div className="flex items-center justify-end gap-2 pt-3 border-t border-slate-800">
              <button
                type="button"
                disabled={isSharing}
                onClick={() => setShowShareDialog(false)}
                className="px-4 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-medium"
              >
                {lang === 'am' ? 'ሰርዝ' : 'Cancel'}
              </button>
              <button
                type="button"
                disabled={isSharing}
                onClick={executeShare}
                className="flex items-center gap-1.5 px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-500 text-white text-xs font-semibold shadow-md shadow-blue-600/20"
              >
                {isSharing ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <Send className="w-3.5 h-3.5" />}
                <span>{lang === 'am' ? 'አረጋግጥና ላክ' : 'Confirm & Share'}</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
