import React, { useState, useRef, useEffect } from 'react';
import { 
  Sparkles, 
  Send, 
  Globe, 
  Paperclip, 
  Trash2, 
  Copy, 
  Check, 
  Share2, 
  FileText, 
  ExternalLink,
  Bot,
  User,
  Search,
  AlertCircle,
  AlertTriangle,
  Clock,
  ShieldCheck
} from 'lucide-react';
import { ChatMessage, GroundingSource } from '../types';

interface GeminiAiChatProps {
  lang: 'am' | 'en';
}

export const GeminiAiChat: React.FC<GeminiAiChatProps> = ({ lang }) => {
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: 'welcome-1',
      sender: 'gemini',
      responseType: 'gemini',
      statusMessage: 'Ready',
      text: lang === 'am'
        ? `ሰላም መሐንዲስ በላይነህ! እኔ የቲሊሊ ከተማ ኮንስትራክሽን የ Gemini AI ረዳት ነኝ።\n\nስለ **EBCS (የኢትዮጵያ ህንፃ ኮዶች)**፣ **አዋጅ ቁጥር 721/2004 (የከተማ መሬት ሊዝ)**፣ **FIDIC ውሎች**፣ ወይም ወቅታዊ የግንባታ እቃዎች ዋጋዎችን ከድረ-ገጽ (Google Search) ጠይቀው በእውነተኛ ምንጮች የተረጋገጠ ምላሽ ማግኘት ይችላሉ።\n\nፋይሎችን (PDF, DOC, ምስሎች) አያይዘው እንዲተነተኑ መጠየቅም ይችላሉ።`
        : `Welcome Engineer Belayneh! I am your Gemini AI Civil Engineering & Regulatory Assistant for Tilli Municipality.\n\nYou can ask about Ethiopian Building Code Standards (EBCS), Lease Proclamation No. 721/2004, FIDIC contracts, or search live web sources for current construction materials prices.\n\nYou can also attach documents (PDF, DOCX, images) for instant structural & compliance audit.`,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    }
  ]);

  const [input, setInput] = useState('');
  const [enableWebSearch, setEnableWebSearch] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lastApiStatus, setLastApiStatus] = useState<string | null>(null);
  const [geminiStatus, setGeminiStatus] = useState<'LIVE' | 'QUOTA_LIMITED' | 'CHECKING'>('CHECKING');
  const [searchStatus, setSearchStatus] = useState<'LIVE' | 'UNAVAILABLE' | 'CHECKING'>('CHECKING');
  const [copiedId, setCopiedId] = useState<string | null>(null);
  const [attachedFile, setAttachedFile] = useState<{ name: string; content: string; type: string; size: number } | null>(null);
  
  const fileInputRef = useRef<HTMLInputElement>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, isLoading]);

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      const result = event.target?.result as string;
      setAttachedFile({
        name: file.name,
        content: result.slice(0, 10000), // excerpt for prompt context
        type: file.type || 'text/plain',
        size: file.size
      });
    };

    if (file.type.includes('image')) {
      reader.readAsDataURL(file);
    } else {
      reader.readAsText(file);
    }
  };

  const handleSendMessage = async () => {
    if ((!input.trim() && !attachedFile) || isLoading) return;

    const userText = input.trim();
    const currentAttachment = attachedFile;
    setInput('');
    setAttachedFile(null);
    setError(null);

    const userMsg: ChatMessage = {
      id: Date.now().toString(),
      sender: 'user',
      text: userText,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      fileAttachment: currentAttachment ? {
        name: currentAttachment.name,
        type: currentAttachment.type,
        size: currentAttachment.size
      } : undefined
    };

    setMessages(prev => [...prev, userMsg]);
    setIsLoading(true);

    try {
      const response = await fetch('/api/gemini', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          prompt: userText,
          history: messages,
          enableWebSearch,
          documentContext: currentAttachment ? `የፋይል ስም፡ ${currentAttachment.name}\nይዘት፡ ${currentAttachment.content}` : '',
          systemInstruction: "You are an authoritative civil engineering regulatory AI for Tilli City Administration, Ethiopia. Answer professionally in Amharic with appropriate English technical terms. When Google Search Grounding is enabled, base your findings on the web search results and cite the sources."
        })
      });

      if (!response.ok) {
        throw new Error(`Server returned HTTP ${response.status}`);
      }

      const data = await response.json();

      if (data.statusMessage) {
        setLastApiStatus(data.statusMessage);
      }

      const aiMsg: ChatMessage = {
        id: (Date.now() + 1).toString(),
        sender: 'gemini',
        text: data.text || 'ምላሽ አልተገኘም።',
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        responseType: data.responseType || (data.sources?.length > 0 ? 'gemini_grounded' : (data.isFallback ? 'fallback' : 'gemini')),
        statusMessage: data.statusMessage,
        isFallback: Boolean(data.isFallback),
        retryAfter: data.retryAfter,
        groundingSources: data.sources || [],
        webQueries: data.queries || [],
        offlineReferences: data.offlineReferences || [],
        isSearchingWeb: Boolean(enableWebSearch && (data.sources?.length > 0 || data.queries?.length > 0))
      };

      // Update active status badges based on server result
      if (data.isFallback) {
        setGeminiStatus('QUOTA_LIMITED');
        setSearchStatus('UNAVAILABLE');
      } else {
        setGeminiStatus('LIVE');
        if (data.grounded && data.sources?.length > 0) {
          setSearchStatus('LIVE');
        } else {
          setSearchStatus(enableWebSearch ? 'LIVE' : 'UNAVAILABLE');
        }
      }

      setMessages(prev => [...prev, aiMsg]);
    } catch (err: any) {
      console.error('Gemini chat error:', err);
      setGeminiStatus('QUOTA_LIMITED');
      setSearchStatus('UNAVAILABLE');
      setError(err.message || 'ከGemini ሰርቨር ጋር መገናኘት አልተቻለም');
    } finally {
      setIsLoading(false);
    }
  };

  const copyToClipboard = (text: string, id: string) => {
    navigator.clipboard.writeText(text);
    setCopiedId(id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  const shareMessage = async (text: string) => {
    if (navigator.share) {
      try {
        await navigator.share({
          title: 'የቲሊሊ ኮንስትራክሽን Gemini AI ምክረ-ሃሳብ',
          text: text
        });
      } catch (err) {
        copyToClipboard(text, 'share');
      }
    } else {
      copyToClipboard(text, 'share');
      alert(lang === 'am' ? 'ጽሑፉ ወደ ቅንጥብ ሰሌዳ ተቀድቷል!' : 'Copied to clipboard!');
    }
  };

  const clearChat = () => {
    if (confirm(lang === 'am' ? 'የውይይት ታሪኩን ማጽዳት ይፈልጋሉ?' : 'Clear conversation history?')) {
      setMessages([]);
    }
  };

  return (
    <div className="flex flex-col h-[calc(100vh-130px)] max-w-5xl mx-auto bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-2xl">
      {/* Top Bar / Status */}
      <div className="bg-slate-850 border-b border-slate-800 p-3.5 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-amber-500 to-ethiopia-yellow flex items-center justify-center text-slate-950 font-bold shadow-md shadow-amber-500/20">
            <Sparkles className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="font-bold text-sm text-white">
                {lang === 'am' ? 'Gemini 3 Flash • የሕንፃ ኮዶችና የድረ-ገጽ ፍለጋ' : 'Gemini 3 Flash • EBCS & Web Grounding'}
              </h3>
            </div>
            <p className="text-[11px] text-slate-400">
              {lang === 'am' ? 'የኢትዮጵያ ህንፃ ኮዶች (EBCS 1-14) እና ይፋዊ የድረ-ገጽ ምንጮች' : 'Ethiopian Building Standards & Grounded Web Search'}
            </p>
          </div>
        </div>

        {/* Status Indicators & Controls */}
        <div className="flex flex-wrap items-center gap-2">
          {/* Status Indicator Badges explicitly showing LIVE or QUOTA LIMITED / UNAVAILABLE */}
          <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-slate-900 border border-slate-800 text-[10px] font-mono">
            {geminiStatus === 'LIVE' ? (
              <span className="flex items-center gap-1 text-emerald-400 font-bold">
                <Sparkles className="w-3 h-3 text-emerald-400" />
                Gemini AI: LIVE
              </span>
            ) : geminiStatus === 'QUOTA_LIMITED' ? (
              <span className="flex items-center gap-1 text-amber-400 font-bold">
                <AlertTriangle className="w-3 h-3 text-amber-400" />
                Gemini AI: QUOTA LIMITED
              </span>
            ) : (
              <span className="flex items-center gap-1 text-purple-300 font-bold">
                <Sparkles className="w-3 h-3 text-purple-300" />
                Gemini AI: READY
              </span>
            )}

            <span className="text-slate-600">|</span>

            {searchStatus === 'LIVE' ? (
              <span className="flex items-center gap-1 text-blue-400 font-bold">
                <Globe className="w-3 h-3 text-blue-400" />
                Web Search: LIVE
              </span>
            ) : searchStatus === 'UNAVAILABLE' ? (
              <span className="flex items-center gap-1 text-slate-400 font-bold">
                <Globe className="w-3 h-3 text-slate-500" />
                Web Search: UNAVAILABLE
              </span>
            ) : (
              <span className="flex items-center gap-1 text-blue-300 font-bold">
                <Globe className="w-3 h-3 text-blue-300" />
                Web Search: {enableWebSearch ? 'READY' : 'OFF'}
              </span>
            )}
          </div>

          {/* Real Google Search Grounding Toggle */}
          <button
            onClick={() => setEnableWebSearch(!enableWebSearch)}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold border transition-all ${
              enableWebSearch
                ? 'bg-blue-600/20 text-blue-300 border-blue-500/50 shadow-sm'
                : 'bg-slate-800 text-slate-400 border-slate-700 hover:text-slate-200'
            }`}
            title="Toggle Google Search Grounding"
          >
            <Globe className={`w-3.5 h-3.5 ${enableWebSearch ? 'text-blue-400 animate-spin-slow' : ''}`} />
            <span className="hidden sm:inline">{lang === 'am' ? 'የድረ-ገጽ ፍለጋ' : 'Web Grounding'}</span>
            <span className={`w-2 h-2 rounded-full ${enableWebSearch ? 'bg-blue-400' : 'bg-slate-600'}`}></span>
          </button>

          {/* Clear Chat */}
          <button
            onClick={clearChat}
            className="p-1.5 bg-slate-800 hover:bg-slate-700 text-slate-400 hover:text-rose-400 rounded-lg border border-slate-700 transition-colors"
            title="Clear Chat History"
          >
            <Trash2 className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Quota / Status Notice Banner (If Last Response was Fallback) */}
      {lastApiStatus && (
        <div className="bg-amber-950/40 border-b border-amber-800/40 px-4 py-2 flex items-center justify-between text-xs text-amber-300">
          <div className="flex items-center gap-2">
            <AlertTriangle className="w-3.5 h-3.5 text-amber-400 shrink-0" />
            <span>{lastApiStatus}</span>
          </div>
          <button
            onClick={() => setLastApiStatus(null)}
            className="text-amber-400/70 hover:text-amber-200 text-[11px]"
          >
            ✕
          </button>
        </div>
      )}

      {/* Messages Scroll Area */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4">
        {messages.map((msg) => {
          const isUser = msg.sender === 'user';
          return (
            <div
              key={msg.id}
              className={`flex items-start gap-3 ${isUser ? 'flex-row-reverse' : 'flex-row'}`}
            >
              {/* Avatar */}
              <div className={`w-8 h-8 rounded-xl flex items-center justify-center shrink-0 ${
                isUser ? 'bg-ethiopia-green text-white' : 'bg-gradient-to-br from-slate-800 to-slate-700 text-amber-400 border border-slate-700'
              }`}>
                {isUser ? <User className="w-4 h-4" /> : <Bot className="w-4 h-4" />}
              </div>

              {/* Message Bubble */}
              <div className={`max-w-[85%] md:max-w-[75%] rounded-2xl p-4 text-sm leading-relaxed shadow-md ${
                isUser 
                  ? 'bg-ethiopia-green text-white rounded-tr-none' 
                  : 'bg-slate-850 text-slate-100 border border-slate-800 rounded-tl-none'
              }`}>
                {/* AI Origin Badge (Never disguise fallback as Gemini or Google Search) */}
                {!isUser && (
                  <div className="mb-2 pb-2 border-b border-slate-800/80 flex flex-wrap items-center justify-between gap-2">
                    {msg.responseType === 'gemini_grounded' ? (
                      <div className="flex items-center gap-1.5">
                        <span className="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-md bg-emerald-950/80 border border-emerald-600/50 text-emerald-300 text-[10px] font-bold font-mono">
                          <Sparkles className="w-3 h-3 text-emerald-400" />
                          Gemini AI — Live
                        </span>
                        <span className="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-md bg-blue-950/80 border border-blue-600/50 text-blue-300 text-[10px] font-bold font-mono">
                          <Globe className="w-3 h-3 text-blue-400" />
                          Web Search — Live
                        </span>
                      </div>
                    ) : msg.responseType === 'gemini' ? (
                      <div className="flex items-center gap-1.5">
                        <span className="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-md bg-emerald-950/80 border border-emerald-600/50 text-emerald-300 text-[10px] font-bold font-mono">
                          <Sparkles className="w-3 h-3 text-emerald-400" />
                          Gemini AI — Live
                        </span>
                        {msg.isSearchingWeb && (
                          <span className="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-md bg-slate-900 border border-slate-700 text-slate-400 text-[10px] font-bold font-mono">
                            <Globe className="w-3 h-3 text-slate-400" />
                            Web Search — Unavailable
                          </span>
                        )}
                      </div>
                    ) : (
                      <div className="flex items-center gap-1.5">
                        <span className="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-md bg-amber-950/80 border border-amber-600/50 text-amber-300 text-[10px] font-bold font-mono">
                          <AlertTriangle className="w-3 h-3 text-amber-400" />
                          Gemini AI — Quota Limited
                        </span>
                        <span className="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-md bg-slate-900 border border-amber-800/40 text-amber-200/90 text-[10px] font-bold font-mono">
                          <ShieldCheck className="w-3 h-3 text-amber-400" />
                          FALLBACK / OFFLINE KNOWLEDGE
                        </span>
                      </div>
                    )}

                    {msg.isFallback && msg.retryAfter && (
                      <span className="inline-flex items-center gap-1 text-[10px] text-amber-400/80 font-mono">
                        <Clock className="w-3 h-3" />
                        Retry in ~{msg.retryAfter}s
                      </span>
                    )}
                  </div>
                )}

                {/* File Attachment Pill */}
                {msg.fileAttachment && (
                  <div className="mb-2 p-2 rounded-lg bg-black/20 border border-white/10 flex items-center gap-2 text-xs">
                    <FileText className="w-4 h-4 text-amber-300" />
                    <span className="font-semibold">{msg.fileAttachment.name}</span>
                    <span className="text-[10px] text-white/70">({(msg.fileAttachment.size / 1024).toFixed(1)} KB)</span>
                  </div>
                )}

                {/* Fallback Explanation Alert if Quota Exceeded */}
                {!isUser && msg.isFallback && (
                  <div className="mb-3 p-2.5 rounded-lg bg-amber-900/30 border border-amber-700/50 text-amber-200 text-xs flex items-start gap-2">
                    <AlertCircle className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
                    <div>
                      <p className="font-semibold">
                        {lang === 'am' ? 'የGemini API የጥያቄ ገደብ (Quota Limit) ተደርሷል' : 'Gemini API quota/rate limit reached'}
                      </p>
                      <p className="text-[11px] text-amber-300/80 mt-0.5">
                        {lang === 'am'
                          ? 'መተግበሪያው እንዳይቋረጥ ከመስመር ውጭ ካለው የቲሊሊ EBCS ደንቦች ዳታቤዝ የተዘጋጀ ምላሽ እየቀረበ ነው።'
                          : 'Serving authoritative offline municipal EBCS regulatory database until quota resets.'}
                      </p>
                    </div>
                  </div>
                )}

                {/* Main Text */}
                <div className="whitespace-pre-wrap font-sans text-xs md:text-sm">
                  {msg.text}
                </div>

                {/* Genuine Grounding Sources & Search Queries (ONLY for Real Search Grounding) */}
                {msg.groundingSources && msg.groundingSources.length > 0 && (
                  <div className="mt-4 pt-3 border-t border-slate-800 text-xs">
                    <div className="flex items-center gap-1.5 text-blue-400 font-bold mb-2">
                      <Search className="w-3.5 h-3.5" />
                      <span>{lang === 'am' ? 'የተረጋገጡ የቀጥታ ድረ-ገጽ ምንጮች (Google Search Grounding):' : 'Verified Live Web Sources:'}</span>
                    </div>

                    {msg.webQueries && msg.webQueries.length > 0 && (
                      <p className="text-[11px] text-slate-400 mb-2">
                        <span className="font-semibold">{lang === 'am' ? 'የፍለጋ ቃላት፡' : 'Searches:'}</span> {msg.webQueries.map(q => `"${q}"`).join(', ')}
                      </p>
                    )}

                    <div className="space-y-1.5">
                      {msg.groundingSources.map((source, idx) => (
                        <a
                          key={idx}
                          href={source.url}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="flex items-center justify-between p-2 rounded-lg bg-slate-900/90 hover:bg-slate-800 border border-slate-800 text-blue-300 hover:text-blue-200 transition-colors text-xs group"
                        >
                          <span className="line-clamp-1 font-medium">{source.title || source.url}</span>
                          <ExternalLink className="w-3.5 h-3.5 shrink-0 ml-2 text-slate-500 group-hover:text-blue-300" />
                        </a>
                      ))}
                    </div>
                  </div>
                )}

                {/* Separate Municipal Offline Reference Links (Explicitly NOT Live Search) */}
                {msg.offlineReferences && msg.offlineReferences.length > 0 && (
                  <div className="mt-4 pt-3 border-t border-slate-800 text-xs">
                    <div className="flex items-center gap-1.5 text-amber-400 font-bold mb-1.5">
                      <ShieldCheck className="w-3.5 h-3.5" />
                      <span>{lang === 'am' ? 'የከተማው ይፋዊ የህግ ማጣቀሻዎች (Municipal Offline References):' : 'Municipal Offline References:'}</span>
                    </div>
                    <p className="text-[10px] text-slate-400 mb-2">
                      {lang === 'am' ? 'እነዚህ ከመስመር ውጭ የተመዘገቡ ይፋዊ ማጣቀሻዎች ናቸው፤ የቀጥታ ፍለጋ ውጤቶች አይደሉም።' : 'Authoritative municipal regulatory references (Not live search results).'}
                    </p>
                    <div className="space-y-1.5">
                      {msg.offlineReferences.map((ref, idx) => (
                        <a
                          key={idx}
                          href={ref.url}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="flex items-center justify-between p-2 rounded-lg bg-slate-900/90 hover:bg-slate-800 border border-slate-800 text-amber-300 hover:text-amber-200 transition-colors text-xs group"
                        >
                          <span className="line-clamp-1 font-medium">{ref.title}</span>
                          <ExternalLink className="w-3.5 h-3.5 shrink-0 ml-2 text-slate-500 group-hover:text-amber-300" />
                        </a>
                      ))}
                    </div>
                  </div>
                )}

                {/* Message Footer: Timestamp and actions */}
                <div className="flex items-center justify-between mt-2 pt-1 text-[10px] opacity-70">
                  <span>{msg.timestamp}</span>
                  {!isUser && (
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => copyToClipboard(msg.text, msg.id)}
                        className="hover:text-amber-300 transition-colors"
                        title="Copy message"
                      >
                        {copiedId === msg.id ? <Check className="w-3 h-3 text-emerald-400" /> : <Copy className="w-3 h-3" />}
                      </button>
                      <button
                        onClick={() => shareMessage(msg.text)}
                        className="hover:text-amber-300 transition-colors"
                        title="Share message"
                      >
                        <Share2 className="w-3 h-3" />
                      </button>
                    </div>
                  )}
                </div>
              </div>
            </div>
          );
        })}

        {/* Loading Indicator */}
        {isLoading && (
          <div className="flex items-start gap-3">
            <div className="w-8 h-8 rounded-xl bg-slate-800 text-amber-400 border border-slate-700 flex items-center justify-center animate-pulse">
              <Sparkles className="w-4 h-4 animate-spin-slow" />
            </div>
            <div className="bg-slate-850 border border-slate-800 rounded-2xl rounded-tl-none p-4 text-xs text-slate-300 flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-amber-400 animate-ping"></span>
              <span>
                {enableWebSearch 
                  ? (lang === 'am' ? 'በGoogle Search ድረ-ገጾችና በEBCS ኮዶች እየፈለገ ነው...' : 'Querying Google Search grounding & EBCS...')
                  : (lang === 'am' ? 'Gemini AI መልስ በማዘጋጀት ላይ ነው...' : 'Gemini AI is drafting response...')}
              </span>
            </div>
          </div>
        )}

        {/* Error Alert */}
        {error && (
          <div className="bg-rose-950/60 border border-rose-800 text-rose-200 p-3 rounded-xl text-xs flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0 text-rose-400" />
            <span>{error}</span>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* Input Area */}
      <div className="bg-slate-850 border-t border-slate-800 p-3">
        {/* Attached File Preview */}
        {attachedFile && (
          <div className="mb-2 p-2 bg-slate-800 rounded-lg border border-slate-700 flex items-center justify-between text-xs">
            <div className="flex items-center gap-2 text-amber-300">
              <FileText className="w-4 h-4" />
              <span className="font-semibold">{attachedFile.name}</span>
            </div>
            <button
              onClick={() => setAttachedFile(null)}
              className="text-slate-400 hover:text-rose-400"
            >
              ✕
            </button>
          </div>
        )}

        <div className="flex items-center gap-2">
          {/* Attachment Button */}
          <input
            type="file"
            ref={fileInputRef}
            onChange={handleFileUpload}
            className="hidden"
            accept=".pdf,.doc,.docx,.txt,.csv,.xlsx,image/*"
          />
          <button
            onClick={() => fileInputRef.current?.click()}
            className="p-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-amber-400 rounded-xl border border-slate-700 transition-colors"
            title="Attach File (PDF, DOCX, TXT, Image)"
          >
            <Paperclip className="w-4 h-4" />
          </button>

          {/* Prompt Text Input */}
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && !e.shiftKey && handleSendMessage()}
            placeholder={
              lang === 'am' 
                ? 'የግንባታ ኮድ፣ አዋጅ 721/2004፣ ወይም ወቅታዊ የሲሚንቶ ዋጋ በድረ-ገጽ ፈልግ...' 
                : 'Ask about EBCS codes, lease proclamation, or search live web for cement prices...'
            }
            className="flex-1 bg-slate-900 border border-slate-700 rounded-xl px-4 py-2.5 text-xs md:text-sm text-white placeholder-slate-500 focus:outline-none focus:border-amber-400 focus:ring-1 focus:ring-amber-400"
          />

          {/* Send Button */}
          <button
            onClick={handleSendMessage}
            disabled={isLoading || (!input.trim() && !attachedFile)}
            className="p-2.5 bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 disabled:opacity-40 disabled:hover:from-amber-500 text-slate-950 font-bold rounded-xl shadow-md shadow-amber-500/20 transition-all"
            title="Send Query"
          >
            <Send className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  );
};
