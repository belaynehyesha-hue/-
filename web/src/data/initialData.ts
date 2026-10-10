import { Project, Artisan, Professional, Warning, InstitutionProfile } from '../types';

export const INITIAL_INSTITUTION: InstitutionProfile = {
  name: "የቲሊሊ ከተማ አስተዳደር ኮንስትራክሽንና ከተማ ልማት ጽሕፈት ቤት",
  region: "አማራ ክልል • ምዕራብ ጎጃም",
  department: "የግንባታ ፈቃድና ቁጥጥር ዋና የሥራ ሂደት",
  officerName: "በላይነህ የሻለም ብቻ",
  officialEmail: "belaynehyesha@gmail.com",
  officialPhone: "+251 91 876 5432",
  officeAddress: "ቲሊሊ ከተማ አስተዳደር ህንፃ፣ 3ኛ ፎቅ ቢሮ ቁጥር 304"
};

export const INITIAL_PROJECTS: Project[] = [
  {
    id: "PRJ-001",
    name: "የቲሊሊ ከተማ ማዘጋጃ ቤት አዲሱ አስተዳደር ህንፃ (G+4)",
    contractor: "ጎጃም ጠቅላይ ኮንትራክተር BC-3",
    consultant: "አባይ ዲዛይንና ቁጥጥር አማካሪ ድርጅት",
    residentEngineer: "ኢንጅነር በላይነህ የሻለም ብቻ",
    location: "ቀበሌ 01 ማዕከላዊ አደባባይ",
    type: "የመንግስት አስተዳደር ህንፃ",
    status: "active",
    progress: 68,
    startDate: "2024-01-15",
    budgetETB: 48500000,
    ebcsStatus: "EBCS-1 & EBCS-2 የተረጋገጠ"
  },
  {
    id: "PRJ-002",
    name: "የቲሊሊ የመጀመሪያ ደረጃ ሆስፒታል ማስፋፊያ (G+2)",
    contractor: "ብርሃን ኮንስትራክሽን GC-4",
    consultant: "ሰላም ኢንጂነሪንግ አሶሼትስ",
    residentEngineer: "ኢንጅነር ዳዊት መኮንን",
    location: "ቀበሌ 02 ጤና ጥበቃ ዞን",
    type: "የጤና ተቋም ህንፃ",
    status: "active",
    progress: 42,
    startDate: "2024-04-10",
    budgetETB: 34200000,
    ebcsStatus: "EBCS-8 የመሬት መንቀጥቀጥ ቁጥጥር"
  },
  {
    id: "PRJ-003",
    name: "የቲሊሊ ከተማ ውስጣዊ የአስፓልትና ጠጠር መንገድ ኔትወርክ (4.2 ኪ.ሜ)",
    contractor: "ጣና መንገድ ስራዎች RC-2",
    consultant: "አማራ መንገድ ዲዛይን ኢንተርፕራይዝ",
    residentEngineer: "ኢንጅነር ሰለሞን አለሙ",
    location: "ቀበሌ 01 እና 03 ዋና ዋና መተላለፊያዎች",
    type: "የከተማ የመንገድና ፍሳሽ ፕሮጀክት",
    status: "active",
    progress: 81,
    startDate: "2023-11-01",
    budgetETB: 62000000,
    ebcsStatus: "AACRA/ERA የዲዛይን ማንዋል የተከተለ"
  },
  {
    id: "PRJ-004",
    name: "የቲሊሊ ዘመናዊ የገበያ አዳራሽና የንግድ ማዕከል (G+3)",
    contractor: "አንድነት ህብረት ስራ ማህበር BC-4",
    consultant: "ቪዥን ኢንጂነሪንግ አማካሪዎች",
    residentEngineer: "ኢንጅነር ትዕግስት ወርቁ",
    location: "ቀበሌ 03 አዲሱ ገበያ",
    type: "የንግድና ማህበራዊ አገልግሎት",
    status: "pending",
    progress: 15,
    startDate: "2024-06-20",
    budgetETB: 29800000,
    ebcsStatus: "የእሳት አደጋ ደህንነት ፍተሻ ላይ"
  }
];

export const INITIAL_ARTISANS: Artisan[] = [
  {
    id: "ART-101",
    fullName: "ካሳሁን አያሌው ተሰማ",
    phoneNumber: "0918112233",
    tradeSector: "የግንበኝነት ስራ (Masonry)",
    cocLevel: "ደረጃ 3 (Level III COC)",
    experienceYears: 7,
    projectAssigned: "PRJ-001 (የማዘጋጃ ቤት ህንፃ)",
    warningCount: 0,
    isActive: true
  },
  {
    id: "ART-102",
    fullName: "ሙሉጌታ ደምሴ ዘውዴ",
    phoneNumber: "0912445566",
    tradeSector: "የብረት ማሰርና ኮንክሪት (Rebar & Concrete)",
    cocLevel: "ደረጃ 4 (Level IV COC)",
    experienceYears: 10,
    projectAssigned: "PRJ-001 (የማዘጋጃ ቤት ህንፃ)",
    warningCount: 0,
    isActive: true
  },
  {
    id: "ART-103",
    fullName: "ግርማ ወንድሙ ከበደ",
    phoneNumber: "0923778899",
    tradeSector: "የኤሌክትሪክ ዝርጋታ (Electrical)",
    cocLevel: "ደረጃ 3 (Level III COC)",
    experienceYears: 5,
    projectAssigned: "PRJ-002 (የሆስፒታል ማስፋፊያ)",
    warningCount: 1,
    isActive: true
  },
  {
    id: "ART-104",
    fullName: "አበራ ጥላሁን አየለ",
    phoneNumber: "0934001122",
    tradeSector: "የቧንቧና ፍሳሽ መስመር (Plumbing)",
    cocLevel: "ደረጃ 2 (Level II COC)",
    experienceYears: 4,
    projectAssigned: "PRJ-002 (የሆስፒታል ማስፋፊያ)",
    warningCount: 0,
    isActive: true
  }
];

export const INITIAL_PROFESSIONALS: Professional[] = [
  {
    id: "ENG-01",
    fullName: "ኢንጅነር በላይነህ የሻለም ብቻ",
    profession: "ዋና ሲቪል መሐንዲስና የቁጥጥር ባለሙያ",
    licenseNumber: "MOTI-CE-09412/14",
    licenseLevel: "ከፍተኛ አማካሪ መሐንዲስ (PE)",
    assignedProject: "የሁሉም ፕሮጀክቶች ዋና ተቆጣጣሪ",
    expiryDate: "2027-12-31",
    status: "valid"
  },
  {
    id: "ENG-02",
    fullName: "አርክቴክት ዮናስ ብርሃኑ",
    profession: "ዋና አርክቴክትና የከተማ ፕላነር",
    licenseNumber: "MUDC-AR-05183/15",
    licenseLevel: "ከፍተኛ አርክቴክት",
    assignedProject: "PRJ-001 (የማዘጋጃ ቤት ህንፃ)",
    expiryDate: "2026-08-30",
    status: "valid"
  },
  {
    id: "ENG-03",
    fullName: "ኢንጅነር ዳዊት መኮንን",
    profession: "መዋቅራዊ መሐንዲስ (Structural Engineer)",
    licenseNumber: "MOTI-SE-03911/16",
    licenseLevel: "ተባባሪ መሐንዲስ",
    assignedProject: "PRJ-002 (የሆስፒታል ማስፋፊያ)",
    expiryDate: "2025-11-20",
    status: "under_audit"
  }
];

export const INITIAL_WARNINGS: Warning[] = [
  {
    id: "WRN-501",
    recipientName: "ግርማ ወንድሙ ከበደ",
    phoneNumber: "0923778899",
    sector: "የኤሌክትሪክ ዝርጋታ",
    reason: "በሆስፒታል ፕሮጀክት ላይ የተዘረጉ ኬብሎች ያለ ኮንዱዊት ፓይፕ መሬት ላይ መጣላቸው ተገኝቷል።",
    severity: "medium",
    issuedDate: "2024-05-12"
  },
  {
    id: "WRN-502",
    recipientName: "ጎጃም ጠቅላይ ኮንትራክተር BC-3",
    phoneNumber: "058 220 1144",
    sector: "ዋና ኮንትራክተር",
    reason: "በማዘጋጃ ቤቱ 2ኛ ፎቅ ስላብ ላይ የኮንክሪት ኪዩብ የላቦራቶሪ ውጤት ሳይረጋገጥ ድጋፍ ፎርምወርክ መፍታት በመሞከሩ።",
    severity: "high",
    issuedDate: "2024-05-28"
  }
];
