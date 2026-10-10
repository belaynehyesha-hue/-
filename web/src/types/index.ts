export interface Project {
  id: string;
  name: string;
  contractor: string;
  consultant: string;
  residentEngineer: string;
  location: string;
  type: string;
  status: 'active' | 'pending' | 'completed' | 'suspended';
  progress: number;
  startDate: string;
  budgetETB: number;
  ebcsStatus: string;
}

export interface Artisan {
  id: string;
  fullName: string;
  phoneNumber: string;
  tradeSector: string;
  cocLevel: string;
  experienceYears: number;
  projectAssigned: string;
  warningCount: number;
  isActive: boolean;
}

export interface Professional {
  id: string;
  fullName: string;
  profession: string;
  licenseNumber: string;
  licenseLevel: string;
  assignedProject: string;
  expiryDate: string;
  status: 'valid' | 'expired' | 'under_audit';
}

export interface Warning {
  id: string;
  recipientName: string;
  phoneNumber: string;
  sector: string;
  reason: string;
  severity: 'low' | 'medium' | 'high' | 'critical';
  issuedDate: string;
}

export interface InstitutionProfile {
  name: string;
  region: string;
  department: string;
  officerName: string;
  officialEmail: string;
  officialPhone: string;
  officeAddress: string;
}

export interface GroundingSource {
  title: string;
  url: string;
  snippet?: string;
}

export interface ChatMessage {
  id: string;
  sender: 'user' | 'gemini';
  text: string;
  timestamp: string;
  isSearchingWeb?: boolean;
  groundingSources?: GroundingSource[];
  webQueries?: string[];
  responseType?: 'gemini' | 'gemini_grounded' | 'fallback';
  statusMessage?: string;
  isFallback?: boolean;
  retryAfter?: number;
  offlineReferences?: GroundingSource[];
  fileAttachment?: {
    name: string;
    type: string;
    size: number;
  };
}
