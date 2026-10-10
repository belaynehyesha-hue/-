import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import path from 'path';
import { fileURLToPath } from 'url';

dotenv.config();

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT = process.env.PORT || process.env.WEB_PORT || 5050;

app.use(cors());
app.use(express.json({ limit: '25mb' }));

// Health Check
app.get('/api/health', (req, res) => {
  res.json({
    status: 'ok',
    service: 'construction-platform-api',
    environment: process.env.NODE_ENV || 'production',
    platform: 'Tilli Construction Digital Regulatory & Engineering Web/PWA',
    timestamp: new Date().toISOString(),
    geminiConfigured: Boolean(process.env.GEMINI_API_KEY)
  });
});

// Mock persistent in-memory construction store for unified REST endpoints
let storedProjects = [
  {
    id: 'PRJ-001',
    name: 'የቲሊሊ ከተማ አስተዳደር ዋና አዳራሽ ግንባታ',
    contractor: 'ብርሃን ኮንስትራክሽን ኃ.የተ.የግ.ማ',
    consultant: 'አባይ ዲዛይንና ቁጥጥር ድርጅት',
    residentEngineer: 'ኢንጅነር በላይነህ የሻለም',
    location: 'ቀበሌ 01 (ማዘጋጃ ቤት ግቢ)',
    type: 'የመንግስት አስተዳደር ህንፃ',
    status: 'active',
    progress: 42,
    startDate: '2024-01-15',
    budgetETB: 35000000,
    ebcsStatus: 'EBCS-1 & 2 የተረጋገጠ'
  },
  {
    id: 'PRJ-002',
    name: 'የቲሊሊ ጤና ጣቢያ ማስፋፊያ (ክሊኒካል ዋርድ)',
    contractor: 'ጎጃም አጠቃላይ ህንፃ ተቋራጭ',
    consultant: 'አማራ ዲዛይንና ቁጥጥር ተቋም',
    residentEngineer: 'ኢንጅነር ዮናስ ተፈራ',
    location: 'ቀበሌ 02',
    type: 'የጤና ተቋም',
    status: 'active',
    progress: 78,
    startDate: '2023-11-10',
    budgetETB: 18500000,
    ebcsStatus: 'EBCS-2, 3 & 4 የተረጋገጠ'
  },
  {
    id: 'PRJ-003',
    name: 'የቀበሌ 03 የመጀመሪያ ደረጃ ትምህርት ቤት 4 ተጨማሪ የመማሪያ ክፍሎች',
    contractor: 'ፋሲል ህብረት ኮንስትራክሽን',
    consultant: 'ቲሊሊ ከተማ መሠረተ ልማት መምሪያ',
    residentEngineer: 'ኢንጅነር ሰለሞን ዳኘው',
    location: 'ቀበሌ 03',
    type: 'የትምህርት ተቋም',
    status: 'completed',
    progress: 100,
    startDate: '2023-08-01',
    budgetETB: 9200000,
    ebcsStatus: 'EBCS የተረጋገጠ'
  }
];

// REST Projects API
app.get('/api/projects', (req, res) => {
  res.json({ success: true, count: storedProjects.length, data: storedProjects });
});

app.post('/api/projects', (req, res) => {
  const newPrj = req.body;
  if (!newPrj.name || !newPrj.contractor) {
    return res.status(400).json({ error: 'Name and contractor are required' });
  }
  const id = newPrj.id || `PRJ-${String(storedProjects.length + 1).padStart(3, '0')}`;
  const fullPrj = { ...newPrj, id, progress: Number(newPrj.progress) || 0 };
  storedProjects.unshift(fullPrj);
  res.status(201).json({ success: true, data: fullPrj });
});

app.get('/api/projects/:id', (req, res) => {
  const prj = storedProjects.find(p => p.id === req.params.id);
  if (!prj) return res.status(404).json({ error: 'Project not found' });
  res.json({ success: true, data: prj });
});

app.put('/api/projects/:id', (req, res) => {
  const index = storedProjects.findIndex(p => p.id === req.params.id);
  if (index === -1) return res.status(404).json({ error: 'Project not found' });
  storedProjects[index] = { ...storedProjects[index], ...req.body };
  res.json({ success: true, data: storedProjects[index] });
});

app.patch('/api/projects/:id/progress', (req, res) => {
  const index = storedProjects.findIndex(p => p.id === req.params.id);
  if (index === -1) return res.status(404).json({ error: 'Project not found' });
  const { progress } = req.body;
  const numProgress = Math.min(100, Math.max(0, Number(progress) || 0));
  storedProjects[index].progress = numProgress;
  if (numProgress >= 100) storedProjects[index].status = 'completed';
  res.json({ success: true, data: storedProjects[index] });
});

app.delete('/api/projects/:id', (req, res) => {
  storedProjects = storedProjects.filter(p => p.id !== req.params.id);
  res.json({ success: true, message: 'Project deleted' });
});

// BOQ & Takeoff endpoints
app.get('/api/projects/:id/boq', (req, res) => {
  res.json({
    projectId: req.params.id,
    currency: 'ETB',
    items: [
      { itemNo: '1.01', description: 'Site clearance and top soil stripping (depth 200mm)', unit: 'm2', quantity: 850, rate: 65, amount: 55250 },
      { itemNo: '1.02', description: 'Bulk excavation in ordinary soil to reach formation level', unit: 'm3', quantity: 420, rate: 210, amount: 88200 },
      { itemNo: '2.01', description: 'C-25 Reinforced Concrete for isolated footing pads', unit: 'm3', quantity: 95, rate: 11500, amount: 1092500 },
      { itemNo: '2.02', description: 'High yield deformed reinforcement bars (dia 12-20mm)', unit: 'kg', quantity: 14200, rate: 175, amount: 2485000 }
    ],
    totalBOQ: 3720950
  });
});

app.post('/api/projects/:id/boq', (req, res) => {
  res.status(201).json({ success: true, message: 'BOQ updated successfully', data: req.body });
});

// Reports endpoint
app.get('/api/projects/:id/reports', (req, res) => {
  res.json({
    projectId: req.params.id,
    reports: [
      { id: 'REP-001', type: 'Daily Site Report', date: new Date().toISOString().split('T')[0], status: 'approved' },
      { id: 'REP-002', type: 'EBCS Structural Concrete Test', date: '2024-02-18', status: 'compliant' }
    ]
  });
});

// Document upload endpoint (Stateless)
app.post('/api/documents/upload', (req, res) => {
  const { filename, fileType, projectId } = req.body;
  res.status(201).json({
    success: true,
    fileId: `DOC-${Date.now()}`,
    filename: filename || 'document.pdf',
    projectId: projectId || 'PRJ-001',
    uploadTime: new Date().toISOString(),
    status: 'uploaded'
  });
});

// User profile endpoint
app.get('/api/users/me', (req, res) => {
  res.json({
    id: 'USR-MGR-01',
    name: 'ኢንጅነር በላይነህ የሻለም',
    role: 'Lead Municipal Civil Engineer & Regulator',
    department: 'ቲሊሊ ከተማ አስተዳደር መሠረተ ልማት መምሪያ',
    permissions: ['all_projects_read', 'all_projects_write', 'regulatory_audit', 'safety_ncr_issue']
  });
});

// Helper to call Gemini API with timeout, retry & exponential backoff
async function callGeminiWithTimeoutAndRetry(endpoint, requestBody, maxRetries = 1, timeoutMs = 15000) {
  let attempt = 0;
  while (attempt <= maxRetries) {
    attempt++;
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), timeoutMs);
    const startTime = Date.now();

    try {
      console.log(`[Server] [Gemini Call] Attempt ${attempt}/${maxRetries + 1} starting...`);
      const response = await fetch(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestBody),
        signal: controller.signal
      });
      clearTimeout(timer);
      const elapsed = Date.now() - startTime;

      if (response.ok) {
        console.log(`[Server] [Gemini Success] Request succeeded in ${elapsed}ms (attempt ${attempt})`);
        return { ok: true, status: response.status, response };
      }

      const rawError = await response.text();
      // Sanitize any accidental API key leaks in error text
      const sanitizedError = rawError.replace(/key=[a-zA-Z0-9_\-]+/g, 'key=REDACTED');

      if (response.status === 429) {
        console.warn(`[Server] [HTTP 429 Quota/Rate Limit] (attempt ${attempt}): Gemini API quota reached.`);
        if (attempt <= maxRetries) {
          const backoffDelay = attempt * 2000;
          console.log(`[Server] [Backoff Retry] Waiting ${backoffDelay}ms before retry attempt ${attempt + 1}...`);
          await new Promise(resolve => setTimeout(resolve, backoffDelay));
          continue;
        }
        return { ok: false, status: 429, errorText: 'Gemini API quota/rate limit reached.', retryAfter: 30 };
      }

      if (response.status === 503) {
        console.warn(`[Server] [HTTP 503 Service Overloaded] (attempt ${attempt})`);
        if (attempt <= maxRetries) {
          const backoffDelay = attempt * 1500;
          console.log(`[Server] [Backoff Retry] Waiting ${backoffDelay}ms before retry...`);
          await new Promise(resolve => setTimeout(resolve, backoffDelay));
          continue;
        }
        return { ok: false, status: 503, errorText: 'Gemini service temporarily overloaded.', retryAfter: 15 };
      }

      console.error(`[Server] [API Error HTTP ${response.status}]:`, sanitizedError.slice(0, 200));
      return { ok: false, status: response.status, errorText: sanitizedError };

    } catch (err) {
      clearTimeout(timer);
      if (err.name === 'AbortError') {
        console.error(`[Server] [Request Timeout] Call timed out after ${timeoutMs}ms (attempt ${attempt})`);
        if (attempt <= maxRetries) {
          continue;
        }
        return { ok: false, status: 504, errorText: 'Request timeout to Gemini API' };
      }
      console.error(`[Server] [Fetch Exception] (attempt ${attempt}):`, err.message);
      if (attempt <= maxRetries) {
        await new Promise(resolve => setTimeout(resolve, 1000));
        continue;
      }
      return { ok: false, status: 500, errorText: err.message };
    }
  }
}

// Real Gemini AI Chat with Google Search Grounding & Offline Knowledge Fallback
const handleAiChat = async (req, res) => {
  try {
    const {
      prompt,
      history = [],
      enableWebSearch = true,
      systemInstruction = "You are a senior professional civil engineering regulatory expert advising on Ethiopian Building Codes (EBCS), ESEN, BATCODA constants, FIDIC contracts, and urban construction laws in Amharic and English for the Tilli City Administration.",
      documentContext = ""
    } = req.body;

    console.log(`[Server] [Request Start] /api/gemini received prompt length: ${prompt?.length || 0}, webSearch: ${Boolean(enableWebSearch)}`);

    if (!prompt || typeof prompt !== 'string') {
      return res.status(400).json({ error: 'Prompt is required' });
    }

    const apiKey = process.env.GEMINI_API_KEY;

    // Fallback if no API key is set on the server
    if (!apiKey || apiKey === 'MY_GEMINI_API_KEY') {
      console.log('[Server] [Fallback Activation] No valid GEMINI_API_KEY found; activating offline knowledge base.');
      return res.json({
        text: `[የከመስመር ውጭ ማብራሪያ (Fallback / Offline Knowledge Base)]\n\nማስታወሻ፡ በሰርቨሩ ላይ የGemini API ቁልፍ ስላልተዋቀረ ይህ ምላሽ ከመስመር ውጭ ካለው የቲሊሊ ከተማ አስተዳደር ደንቦች ዳታቤዝ የተዘጋጀ ነው።\n\nውድ መሐንዲስ፣ በቲሊሊ ከተማ ኮንስትራክሽንና ከተማ ልማት ጽሕፈት ቤት ደንቦች መሠረት ለቀረበው ጥያቄ («${prompt}») ምላሽ፡\n\n1. **የኢትዮጵያ ህንፃ ኮዶች (EBCS-1 & 2):** መዋቅራዊ ንድፎች በቂ የደህንነት ፋክተር (Safety Factor C-25: 1.5 ለኮንክሪት፣ 1.15 ለአርማታ ብረት) ማሟላት አለባቸው።\n2. **የሊዝ አዋጅ ቁጥር 721/2004:** ግንባታዎች በተፈቀደላቸው የጊዜ ሰሌዳ ውስጥ ካልተጠናቀቁ ቅጣትና የይዞታ መብት መቋረጥ ያስከትላሉ።\n3. **የሰነድ ማጣቀሻ:** ${documentContext || 'የአጠቃላይ ኮድ መመሪያዎች'}`,
        responseType: 'fallback',
        statusMessage: 'Gemini API key not configured. Using offline knowledge base.',
        isFallback: true,
        sources: [],
        queries: [],
        offlineReferences: [
          { title: "የኢትዮጵያ ኮንስትራክሽን ባለስልጣን ደንቦች", url: "https://www.moti.gov.et" },
          { title: "የከተማ ቦታን በሊዝ ስለመያዝ የወጣ አዋጅ 721/2004", url: "https://www.moj.gov.et" }
        ],
        model: "offline-regulatory-knowledge-base",
        grounded: false
      });
    }

    // Build Gemini contents array from history + new prompt
    const contents = [];

    // Append context if provided
    if (documentContext) {
      contents.push({
        role: 'user',
        parts: [{ text: `[የተያያዘ ሰነድና አውድ]:\n${documentContext}` }]
      });
      contents.push({
        role: 'model',
        parts: [{ text: "የተያያዘው ሰነድ ተመዝግቧል። በዚህ መሠረት ጥያቄዎችዎን እመልሳለሁ።" }]
      });
    }

    // Previous chat turns
    for (const msg of history.slice(-6)) {
      if (msg.sender === 'user') {
        contents.push({ role: 'user', parts: [{ text: msg.text }] });
      } else if (msg.sender === 'gemini') {
        contents.push({ role: 'model', parts: [{ text: msg.text }] });
      }
    }

    // Current turn
    contents.push({
      role: 'user',
      parts: [{ text: prompt }]
    });

    // Request payload
    const requestBody = {
      contents,
      systemInstruction: {
        parts: [{ text: systemInstruction }]
      }
    };

    // Attach Google Search Tool for Grounding when enabled
    if (enableWebSearch) {
      requestBody.tools = [
        { google_search: {} }
      ];
    }

    const endpoint = `https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=${apiKey}`;

    const callResult = await callGeminiWithTimeoutAndRetry(endpoint, requestBody, 1, 15000);

    if (!callResult.ok) {
      console.log(`[Server] [Fallback Activation] Fallback response activated (reason: HTTP ${callResult.status} - ${callResult.errorText})`);

      const statusMsg = callResult.status === 429
        ? 'Gemini API quota/rate limit reached. Showing offline knowledge base.'
        : `Gemini API returned status ${callResult.status}. Showing offline knowledge base.`;

      // Return CLEAR fallback response with NO fake search citations
      return res.json({
        text: `[የከመስመር ውጭ ማብራሪያ (Fallback / Offline Knowledge Base)]\n\nማስታወሻ፡ የGoogle Gemini API የጥያቄ ገደብ (Quota/Rate Limit) ላይ በመድረሱ ይህ ምላሽ ከመስመር ውጭ ካለው የቲሊሊ ከተማ EBCS ደንቦች ዳታቤዝ የተዘጋጀ ነው።\n\nለቀረበው ጥያቄ («${prompt}») በቲሊሊ ከተማ አስተዳደር ኮንስትራክሽን መመሪያዎችና EBCS ስታንዳርዶች መሠረት የሚከተሉት ነጥቦች ተለይተዋል፡\n\n1. **የኮንክሪትና ስትራክቸር ህጎች (EBCS-1 & 2):** መዋቅራዊ ንድፎች C-25 ወይም ከዚያ በላይ የሆነ የኮንክሪት ጥንካሬ (Characteristic compressive strength) እና የ 1.5 Safety factor ለኮንክሪት፣ 1.15 ለአርማታ ብረት ማሟላት አለባቸው።\n2. **የሊዝ አዋጅ ቁጥር 721/2004:** የግንባታ ፈቃድ የተሰጣቸው ፕሮጀክቶች በውሉ የጊዜ ገደብ መሠረት መጠናቀቅ ይኖርባቸዋል፤ አለበለዚያ የቅጣት እና የቦታ ውል ማቋረጥ እርምጃ ይወሰዳል።\n3. **የወቅታዊ እቃዎች ግብይት:** የሲሚንቶ፣ የብረትና የአሸዋ ግብይት በፋብሪካና በንግድ ሚኒስቴር የወጡ የጣሪያ ዋጋዎችን (Ceiling prices) መከተል እንዳለበት በከተማው የቁጥጥር መመሪያ ተቀምጧል።`,
        responseType: 'fallback',
        statusMessage: statusMsg,
        isFallback: true,
        retryAfter: callResult.retryAfter || 30,
        sources: [], // NEVER present fake Google Search sources!
        queries: [],
        offlineReferences: [
          { title: "የኢትዮጵያ ኮንስትራክሽን ባለስልጣን ደንቦች (EBCS 1-14)", url: "https://www.moti.gov.et" },
          { title: "የከተማ ቦታን በሊዝ ስለመያዝ የወጣ አዋጅ 721/2004", url: "https://www.moj.gov.et" },
          { title: "የፌዴራል ቤቶችና ከተማ ልማት ሚኒስቴር የግንባታ መመሪያ", url: "https://www.mudc.gov.et" }
        ],
        model: 'offline-regulatory-knowledge-base',
        grounded: false
      });
    }

    const data = await callResult.response.json();
    const candidate = data.candidates?.[0];
    
    // Extract text from parts (filtering out non-text parts if any)
    let generatedText = '';
    if (candidate?.content?.parts) {
      generatedText = candidate.content.parts
        .filter(p => typeof p.text === 'string')
        .map(p => p.text)
        .join('\n\n');
    }
    if (!generatedText) {
      generatedText = 'ምንም ምላሽ አልተገኘም።';
    }

    // Parse Grounding Metadata
    const groundingMetadata = candidate?.groundingMetadata;
    const sources = [];
    const queries = [];

    if (groundingMetadata) {
      if (Array.isArray(groundingMetadata.groundingChunks)) {
        for (const chunk of groundingMetadata.groundingChunks) {
          if (chunk.web?.uri && chunk.web?.title) {
            sources.push({
              title: chunk.web.title,
              url: chunk.web.uri
            });
          }
        }
      }

      if (Array.isArray(groundingMetadata.webSearchQueries)) {
        queries.push(...groundingMetadata.webSearchQueries);
      }
    }

    const isGrounded = sources.length > 0;
    if (isGrounded) {
      console.log(`[Server] [Google Search Grounding Success] Sources: ${sources.length}, Queries: ${queries.length}`);
    } else {
      console.log(`[Server] [Gemini Direct Response] Success without search grounding chunks.`);
    }

    return res.json({
      text: generatedText,
      responseType: isGrounded ? 'gemini_grounded' : 'gemini',
      statusMessage: isGrounded ? 'Real Google Search Grounded Response' : 'Real Gemini AI Response',
      isFallback: false,
      sources, // Genuine sources only!
      queries, // Genuine queries only!
      model: 'gemini-3.6-flash',
      grounded: isGrounded
    });

  } catch (error) {
    console.error('[Server] [Exception in /api/gemini]:', error);
    res.status(500).json({ error: error.message || 'Internal Server Error' });
  }
};

app.post('/api/gemini', handleAiChat);
app.post('/api/ai/chat', handleAiChat);

// Serve Vite Static Production Build if exists
const distPath = path.join(__dirname, '../dist');
app.use(express.static(distPath));

app.get('*', (req, res) => {
  if (!req.path.startsWith('/api/')) {
    res.sendFile(path.join(distPath, 'index.html'));
  }
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`[Tilli Construction Web Server] Running on http://localhost:${PORT}`);
});
