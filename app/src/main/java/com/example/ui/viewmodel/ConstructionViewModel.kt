package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class ReportFormat(
    val extension: String,
    val mimeType: String,
    val displayName: String,
    val formatBadgeAm: String,
    val fileTypeNameAm: String
) {
    PDF(
        extension = "pdf",
        mimeType = "application/pdf",
        displayName = "Adobe PDF (.pdf)",
        formatBadgeAm = "PDF ሰነድ",
        fileTypeNameAm = "Adobe Acrobat PDF Document (ለንባብና ለህትመት)"
    ),
    EXCEL(
        extension = "csv",
        mimeType = "text/comma-separated-values",
        displayName = "Microsoft Excel / CSV (.csv)",
        formatBadgeAm = "Excel ሰንጠረዥ",
        fileTypeNameAm = "Microsoft Excel / CSV Spreadsheet (ለውሂብ ትንተና)"
    )
}

data class ShareReportPrompt(
    val id: String,
    val titleAm: String,
    val titleEn: String,
    val descriptionAm: String,
    val fileName: String,
    val format: ReportFormat,
    val subject: String,
    val shareBodyText: String,
    val generateAction: suspend (Context) -> File?
)

data class ShareReportState(
    val prompt: ShareReportPrompt,
    val isGenerating: Boolean = false,
    val progressStatusText: String = "",
    val errorMessage: String? = null
)

data class GovernmentRegulation(
    val id: String,
    val name: String,
    val description: String,
    val warningThreshold: Int,
    val isPermitMandatory: Boolean,
    val minExperienceYears: Int,
    val requiresVerificationCode: Boolean
)

data class AutomatedAlert(
    val id: String,
    val type: String, // "DEADLINE_APPROACHING" or "HIGH_PRIORITY_RISK"
    val title: String,
    val description: String,
    val associatedName: String,
    val detailValue: String,
    val timestamp: Long,
    val recipientEmails: List<String>,
    val emailSubject: String,
    val emailBody: String,
    val isSent: Boolean = false
)

class ConstructionViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPrefs = application.getSharedPreferences("tilli_notifications", Context.MODE_PRIVATE)
    private val repository = DatabaseProvider.getRepository(application)

    private val _isNetworkConnected = MutableStateFlow(true)
    val isNetworkConnected = _isNetworkConnected.asStateFlow()

    private val _isManualOfflineMode = MutableStateFlow(sharedPrefs.getBoolean("offline_mode", false))
    val isManualOfflineMode = _isManualOfflineMode.asStateFlow()

    // Combining manual setting and actual network status
    val isOfflineMode = combine(_isManualOfflineMode, _isNetworkConnected) { manualOffline, networkConnected ->
        manualOffline || !networkConnected
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = sharedPrefs.getBoolean("offline_mode", false)
    )

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _syncProgress = MutableStateFlow(0f)
    val syncProgress = _syncProgress.asStateFlow()

    private val _institutionProfile = MutableStateFlow(InstitutionProfile.load(application))
    val institutionProfile: StateFlow<InstitutionProfile> = _institutionProfile.asStateFlow()

    // Interactive Report Share Confirmation & Progress State
    private val _shareReportState = MutableStateFlow<ShareReportState?>(null)
    val shareReportState: StateFlow<ShareReportState?> = _shareReportState.asStateFlow()

    fun dismissShareReportDialog() {
        if (_shareReportState.value?.isGenerating != true) {
            _shareReportState.value = null
        }
    }

    fun confirmAndExecuteShare(context: Context) {
        val currentState = _shareReportState.value ?: return
        if (currentState.isGenerating) return

        viewModelScope.launch {
            _shareReportState.value = currentState.copy(
                isGenerating = true,
                progressStatusText = "ሰነዱን በ ${currentState.prompt.format.displayName} ቅርጸት በማዘጋጀት ላይ ነው...",
                errorMessage = null
            )

            val file: File? = withContext(Dispatchers.IO) {
                try {
                    delay(350)
                    currentState.prompt.generateAction(context)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            if (file != null && file.exists()) {
                _shareReportState.value = currentState.copy(
                    isGenerating = true,
                    progressStatusText = "የደህንነት ፈቃድና ማጋሪያ መስኮት (Share Sheet) በመክፈት ላይ..."
                )
                delay(250)

                try {
                    val uri: Uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = currentState.prompt.format.mimeType
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, currentState.prompt.subject)
                        putExtra(Intent.EXTRA_TEXT, currentState.prompt.shareBodyText)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val chooserTitle = "ሪፖርት አጋራ (WhatsApp, Telegram, Email...)"
                    context.startActivity(Intent.createChooser(intent, chooserTitle))
                    _shareReportState.value = null
                } catch (e: Exception) {
                    _shareReportState.value = currentState.copy(
                        isGenerating = false,
                        errorMessage = "ማጋራት አልተቻለም: ${e.localizedMessage ?: "ያልታወቀ ስህተት"}"
                    )
                }
            } else {
                _shareReportState.value = currentState.copy(
                    isGenerating = false,
                    errorMessage = "ፋይሉን ማዘጋጀት አልተቻለም። እባክዎ እንደገና ይሞክሩ።"
                )
            }
        }
    }

    fun updateInstitutionProfile(profile: InstitutionProfile) {
        _institutionProfile.value = profile
        InstitutionProfile.save(getApplication(), profile)
    }

    fun toggleOfflineMode(enabled: Boolean) {
        _isManualOfflineMode.value = enabled
        sharedPrefs.edit().putBoolean("offline_mode", enabled).apply()
        if (!enabled && _isNetworkConnected.value) {
            triggerAutoSync()
        }
    }

    private val _bookmarkedStandardIds = MutableStateFlow(
        sharedPrefs.getStringSet("bookmarked_standard_ids", emptySet()) ?: emptySet()
    )
    val bookmarkedStandardIds = _bookmarkedStandardIds.asStateFlow()

    fun toggleBookmarkStandard(id: String) {
        val current = _bookmarkedStandardIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _bookmarkedStandardIds.value = current
        sharedPrefs.edit().putStringSet("bookmarked_standard_ids", current).apply()
    }

    private val _standardNotes = MutableStateFlow<Map<String, String>>(
        sharedPrefs.all.filterKeys { it.startsWith("standard_note_") }
            .map { (key, value) -> key.removePrefix("standard_note_") to (value as? String ?: "") }
            .toMap()
    )
    val standardNotes = _standardNotes.asStateFlow()

    fun saveStandardNote(id: String, note: String) {
        val current = _standardNotes.value.toMutableMap()
        current[id] = note
        _standardNotes.value = current
        sharedPrefs.edit().putString("standard_note_$id", note).apply()
    }

    val regulations = listOf(
        GovernmentRegulation(
            id = "reg_traditional",
            name = "የቀድሞው መደበኛ የከተማ ቁጥጥር መመሪያ v1",
            description = "ባህላዊ/ቀድሞ የነበረ አሰራር፡ ማስጠንቀቂያ ገደብ 10፣ ፈቃድ ያለው ወይም የሌለው፣ አነስተኛ ልምድ 1 ዓመት።",
            warningThreshold = 10,
            isPermitMandatory = false,
            minExperienceYears = 1,
            requiresVerificationCode = false
        ),
        GovernmentRegulation(
            id = "reg_2026_new",
            name = "አዲሱ የከተማ ፕላንና ኮንስትራክሽን መመሪያ 2026 (አዲስ)",
            description = "ወቅታዊ ጥብቅ አሰራር፡ ማስጠንቀቂያ ገደብ 7፣ የግንባታ ፈቃድ ግዴታ ነው፣ አነስተኛ የሥራ ልምድ 2 ዓመት፣ ጥብቅ የእውቅና ማሻሻያ።",
            warningThreshold = 7,
            isPermitMandatory = true,
            minExperienceYears = 2,
            requiresVerificationCode = true
        ),
        GovernmentRegulation(
            id = "reg_emergency_strict",
            name = "ልዩ የአስቸኳይ ጊዜ የግንባታ መዋቅር ደንብ (ጥብቅ)",
            description = "ለደህንነት የተደነገገ ጥብቅ አሰራር፡ ማስጠንቀቂያ ገደብ 5፣ የግንባታ ፈቃድ ግዴታ ነው፣ አነስተኛ የሥራ ልምድ 3 ዓመት፣ አስገዳጅ እውቅና ማሻሻያ።",
            warningThreshold = 5,
            isPermitMandatory = true,
            minExperienceYears = 3,
            requiresVerificationCode = true
        )
    )

    private val _activeRegulation = MutableStateFlow(regulations[1]) // Default to 2026 New
    val activeRegulation: StateFlow<GovernmentRegulation> = _activeRegulation.asStateFlow()

    fun selectRegulation(regId: String) {
        val found = regulations.find { it.id == regId }
        if (found != null) {
            _activeRegulation.value = found
        }
    }

    val artisans = repository.allArtisans.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val professionals = repository.allProfessionals.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val projects = repository.allProjects.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val warnings = repository.allWarnings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val dailyProgress = repository.allDailyProgress.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val riskAssessments = repository.allRiskAssessments.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Pre-populate database with realistic Ethiopian construction data if empty
        viewModelScope.launch {
            combine(
                repository.allArtisans.take(1),
                repository.allProfessionals.take(1),
                repository.allProjects.take(1)
            ) { a, p, pr ->
                Triple(a.isEmpty(), p.isEmpty(), pr.isEmpty())
            }.collect { (artisanEmpty, profEmpty, projEmpty) ->
                if (artisanEmpty && profEmpty && projEmpty) {
                    seedInitialData()
                }
            }
        }
        observeNetworkConnection(application)
    }

    private fun observeNetworkConnection(context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
        if (cm != null) {
            // Check initial state
            val activeNetwork = cm.activeNetwork
            val capabilities = cm.getNetworkCapabilities(activeNetwork)
            val isConnected = capabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            _isNetworkConnected.value = isConnected

            // Register dynamic callback
            try {
                val builder = android.net.NetworkRequest.Builder()
                cm.registerNetworkCallback(builder.build(), object : android.net.ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: android.net.Network) {
                        _isNetworkConnected.value = true
                        triggerAutoSync()
                    }

                    override fun onLost(network: android.net.Network) {
                        _isNetworkConnected.value = false
                    }
                })
            } catch (e: Exception) {
                _isNetworkConnected.value = true
            }
        } else {
            _isNetworkConnected.value = true
        }
    }

    fun syncOfflineData(onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch {
            if (_isSyncing.value) return@launch
            _isSyncing.value = true
            _syncProgress.value = 0f

            val unsyncedProgress = dailyProgress.value.filter { !it.isSynced }
            val unsyncedRisks = riskAssessments.value.filter { !it.isSynced }
            val totalUnsynced = unsyncedProgress.size + unsyncedRisks.size

            if (totalUnsynced == 0) {
                _isSyncing.value = false
                onComplete(0)
                return@launch
            }

            // Simulated sync progress for cloud servers
            for (step in 1..10) {
                kotlinx.coroutines.delay(150)
                _syncProgress.value = step / 10f
            }

            unsyncedProgress.forEach { log ->
                repository.insertDailyProgress(log.copy(isSynced = true))
            }

            unsyncedRisks.forEach { risk ->
                repository.insertRiskAssessment(risk.copy(isSynced = true))
            }

            _isSyncing.value = false
            onComplete(totalUnsynced)
        }
    }

    private fun triggerAutoSync() {
        val unsyncedProgressCount = dailyProgress.value.count { !it.isSynced }
        val unsyncedRiskCount = riskAssessments.value.count { !it.isSynced }
        if (unsyncedProgressCount > 0 || unsyncedRiskCount > 0) {
            syncOfflineData()
        }
    }

    private suspend fun seedInitialData() {
        // Seed Professionals complying with Ethiopian Building Proclamation 1356/2017 & International Standards
        val prof1 = Professional(
            name = "በላይነህ የሻለም (Lead Civil Engineer)",
            phone = "+251911223344",
            email = "belayneh.yeshalem@construction.gov.et",
            nationalId = "ET-FAYDA-98124018",
            role = "የስራ ተቆጣጣሪ",
            grade = "ልምድ ያለው መሪ መሃንዲስ (PPE Grade 1)",
            licenseNumber = "MUI/PPE/2021/AA-089",
            qualification = "MSc in Structural Engineering, AAiT",
            institutionGraduated = "Addis Ababa University (AAiT)",
            graduationYear = 2014,
            experienceYears = 12,
            fieldOfPractice = "ሲቪልና መዋቅር ምህንድስና (Civil & Structural)",
            specialization = "መዋቅርና ሲቪል (Structural & Civil)",
            authorizedBuildingCategories = "ምድብ ሀ፣ ለ፣ ሐ እና ሜጋ (All Categories & Mega)",
            digitalSignatureVerified = true,
            law1356ComplianceStatus = "ተገዢ (COMPLIANT)",
            muiRegistrationNo = "MUI-REG-10928/ETH",
            licenseStatus = "ተገዢ (COMPLIANT)",
            cpdPoints = 35,
            insurancePolicyNo = "NYALA-PI-2026-9018",
            internationalCertifications = "FIDIC Certified Engineer, ISO 45001 Lead Auditor",
            assignedProjectRole = "ዋና የቁጥጥር መሀንዲስ (Lead Supervising Engineer)"
        )
        val prof2 = Professional(
            name = "ዶ/ር አክሊሉ ወርቁ (MH Engineering CA-1)",
            phone = "+251912445566",
            email = "akliluw@mhengineering.et",
            nationalId = "ET-FAYDA-10928374",
            role = "የኮንስትራክሽን አማካሪ",
            grade = "አማካሪ መሃንዲስ (CA-1)",
            licenseNumber = "MUI/CONS/2019/CA-0012",
            qualification = "PhD in Structural Dynamics, TU Delft",
            institutionGraduated = "TU Delft / AAU",
            graduationYear = 2008,
            experienceYears = 18,
            fieldOfPractice = "የህንፃ ዲዛይንና መዋቅር አማካሪ (Principal Structural Consultant)",
            specialization = "መዋቅርና ሲቪል (Structural & Civil)",
            authorizedBuildingCategories = "ምድብ ሀ፣ ለ፣ ሐ እና ሜጋ (All Categories & Mega)",
            digitalSignatureVerified = true,
            law1356ComplianceStatus = "ተገዢ (COMPLIANT)",
            muiRegistrationNo = "MUI-CONS-0018/ETH",
            licenseStatus = "ተገዢ (COMPLIANT)",
            cpdPoints = 40,
            insurancePolicyNo = "UNITED-PI-2025-4412",
            internationalCertifications = "FIDIC Adjudicator (President's List), Eurocode & EBCS Specialist",
            assignedProjectRole = "ዋና አማካሪ መሀንዲስ (Principal Design Consultant)"
        )
        val prof3 = Professional(
            name = "ሱር ኮንስትራክሽን ጠቅላላ ተቋራጭ (Sur Construction)",
            phone = "+251915998877",
            email = "projects@surconstruction.et",
            nationalId = "TIN-0018274619",
            role = "የስራ ተቋራጭ",
            grade = "ጠቅላላ ተቋራጭ ደረጃ ፩ (GC-1)",
            licenseNumber = "MUI/GC1/2018/0042",
            qualification = "Corporate Engineering Contractor Class 1",
            institutionGraduated = "National Construction Registry",
            graduationYear = 1999,
            experienceYears = 25,
            fieldOfPractice = "ጠቅላላ የህንፃና የመሰረተ ልማት ግንባታ (General Construction)",
            specialization = "ኮንስትራክሽን ማኔጅመንት (General Construction)",
            authorizedBuildingCategories = "ምድብ ሀ፣ ለ፣ ሐ እና ሜጋ (All Categories & Mega)",
            digitalSignatureVerified = true,
            law1356ComplianceStatus = "ተገዢ (COMPLIANT)",
            muiRegistrationNo = "MUI-GC1-0042/ETH",
            licenseStatus = "ተገዢ (COMPLIANT)",
            cpdPoints = 30,
            insurancePolicyNo = "AWASH-CAR-2026-7819",
            internationalCertifications = "ISO 9001:2015, ISO 14001, ISO 45001:2018, FIDIC Red Book",
            assignedProjectRole = "ዋና ስራ ተቋራጭ (Main EPC Contractor)"
        )
        val prof4 = Professional(
            name = "ሳራ ተስፋዬ (HSE Safety Manager)",
            phone = "+251920112233",
            email = "sara.tesfaye@safety.et",
            nationalId = "ET-FAYDA-77182934",
            role = "የስራ ተቆጣጣሪ",
            grade = "የደህንነትና ጤና መሃንዲስ (HSE Officer Grade 2)",
            licenseNumber = "ETH-HSE-2024-331",
            qualification = "BSc in Environmental Health & Safety",
            institutionGraduated = "Gondar University",
            graduationYear = 2019,
            experienceYears = 7,
            fieldOfPractice = "የግንባታ ደህንነትና አካባቢ ጥበቃ (Occupational HSE)",
            specialization = "ደህንነትና ጤና (HSE)",
            authorizedBuildingCategories = "ምድብ ሀ፣ ለ፣ ሐ እና ሜጋ (All Categories & Mega)",
            digitalSignatureVerified = true,
            law1356ComplianceStatus = "ተገዢ (COMPLIANT)",
            muiRegistrationNo = "MUI-HSE-0812/ETH",
            licenseStatus = "ተገዢ (COMPLIANT)",
            cpdPoints = 28,
            insurancePolicyNo = "AFRICA-INS-HSE-1102",
            internationalCertifications = "NEBOSH International Diploma, OSHA 30-Hour Construction",
            assignedProjectRole = "የሳይት ደህንነትና ጤና ኃላፊ (Site HSE Lead)"
        )
        val prof5 = Professional(
            name = "ዮሐንስ ገብሬ (Senior Quantity Surveyor)",
            phone = "+251930114455",
            email = "yohannes.qs@costeng.et",
            nationalId = "ET-FAYDA-44820192",
            role = "የኮንስትራክሽን አማካሪ",
            grade = "የስራ ዝርዝርና ዋጋ መሃንዲስ (QS Grade 1)",
            licenseNumber = "MUI/QS/2022/AA-198",
            qualification = "BSc in Construction Technology & Management (COTM)",
            institutionGraduated = "Addis Ababa University (AAiT)",
            graduationYear = 2017,
            experienceYears = 9,
            fieldOfPractice = "የስራ ዝርዝር፣ ዋጋና ክፍያ ቁጥጥር (Quantity Surveying & Take-off)",
            specialization = "ኮንስትራክሽን ማኔጅመንት (COTM & QS)",
            authorizedBuildingCategories = "ምድብ ሀ፣ ለ፣ ሐ እና ሜጋ (All Categories & Mega)",
            digitalSignatureVerified = true,
            law1356ComplianceStatus = "ተገዢ (COMPLIANT)",
            muiRegistrationNo = "MUI-QS-0921/ETH",
            licenseStatus = "ተገዢ (COMPLIANT)",
            cpdPoints = 32,
            insurancePolicyNo = "GLOBAL-PI-2026-0811",
            internationalCertifications = "RICS Accredited, FIDIC Claims Specialist",
            assignedProjectRole = "የኮንትራትና ክፍያ አስተዳዳሪ (Contracts & Valuation Engineer)"
        )
        val prof6 = Professional(
            name = "አሰፋ ከበደ (Resident Site Engineer)",
            phone = "+251940223344",
            email = "assefa.kebede@site.et",
            nationalId = "ET-FAYDA-22019384",
            role = "የስራ ተቆጣጣሪ",
            grade = "ተባባሪ መሃንዲስ (AE Grade 3)",
            licenseNumber = "ETH-INSP-2023-112",
            qualification = "BSc in Civil Engineering",
            institutionGraduated = "Bahir Dar University",
            graduationYear = 2022,
            experienceYears = 4,
            fieldOfPractice = "የሳይት ቁጥጥርና ፍተሻ (Site Supervision)",
            specialization = "የሕንፃ ፍተሻ (Site Supervision)",
            authorizedBuildingCategories = "ምድብ ሀ እና ለ (Categories A & B)",
            digitalSignatureVerified = false,
            law1356ComplianceStatus = "ጊዜው ያለፈበት (EXPIRED)",
            muiRegistrationNo = "MUI-AE-2023-991",
            licenseStatus = "ጊዜው ያለፈበት (EXPIRED)",
            licenseExpiryDate = System.currentTimeMillis() - (15L * 24 * 3600 * 1000), // expired 15 days ago
            cpdPoints = 14,
            insurancePolicyNo = "LION-PI-2024-9912",
            internationalCertifications = "Site Safety Induction",
            assignedProjectRole = "የሳይት ተቆጣጣሪ (Resident Site Engineer)"
        )
        val prof7 = Professional(
            name = "አርክቴክት ሰላማዊት አበበ (Principal Architect)",
            phone = "+251911778899",
            email = "selamawit.arch@studio.et",
            nationalId = "ET-FAYDA-55019283",
            role = "የኮንስትራክሽን አማካሪ",
            grade = "ዋና አርክቴክት (PA-1)",
            licenseNumber = "MUI/ARCH/2020/AA-045",
            qualification = "MArch in Architectural Design, EiABC",
            institutionGraduated = "EiABC, Addis Ababa University",
            graduationYear = 2015,
            experienceYears = 11,
            fieldOfPractice = "አርክቴክቸራል ዲዛይንና የከተማ ፕላን (Architectural Design)",
            specialization = "አርክቴክቸር (Architectural Design)",
            authorizedBuildingCategories = "ምድብ ሀ፣ ለ፣ ሐ እና ሜጋ (All Categories & Mega)",
            digitalSignatureVerified = true,
            law1356ComplianceStatus = "ተገዢ (COMPLIANT)",
            muiRegistrationNo = "MUI-ARCH-0045/ETH",
            licenseStatus = "ተገዢ (COMPLIANT)",
            cpdPoints = 36,
            insurancePolicyNo = "AWASH-PI-2026-1189",
            internationalCertifications = "LEED Accredited Professional, UIA Member",
            assignedProjectRole = "ዋና የዲዛይን አርክቴክት (Lead Design Architect)"
        )
        val prof8 = Professional(
            name = "ዳዊት ታደሰ (Senior MEP Engineer)",
            phone = "+251913887766",
            email = "dawit.mep@consult.et",
            nationalId = "ET-FAYDA-88192031",
            role = "የኮንስትራክሽን አማካሪ",
            grade = "ኤሌክትሮ-መካኒካል መሃንዲስ (MEP Grade 1)",
            licenseNumber = "MUI/MEP/2021/AA-210",
            qualification = "BSc in Electrical & Mechanical Engineering",
            institutionGraduated = "Hawassa University",
            graduationYear = 2016,
            experienceYears = 10,
            fieldOfPractice = "ሳኒታሪ፣ ኤሌክትሪካልና HVAC ስርዓቶች (Electro-Mechanical)",
            specialization = "ኤሌክትሮ-መካኒክ (MEP Services)",
            authorizedBuildingCategories = "ምድብ ሀ፣ ለ፣ ሐ እና ሜጋ (All Categories & Mega)",
            digitalSignatureVerified = true,
            law1356ComplianceStatus = "ማደስ ያለበት (RENEWAL DUE)",
            muiRegistrationNo = "MUI-MEP-0210/ETH",
            licenseStatus = "ማደስ ያለበት (RENEWAL DUE)",
            licenseExpiryDate = System.currentTimeMillis() + (25L * 24 * 3600 * 1000), // expires in 25 days
            cpdPoints = 26,
            insurancePolicyNo = "ETHIO-INS-PI-4490",
            internationalCertifications = "NFPA Fire Protection Certified, ASHRAE Member",
            assignedProjectRole = "የኤሌክትሮ-መካኒካል አማካሪ (MEP Lead Consultant)"
        )

        val prof9 = Professional(
            name = "ተስፋዬ ገብረመስቀል (Geotechnical Engineer)",
            phone = "+251912003344",
            email = "tesfaye.geo@soil.et",
            nationalId = "", // Missing Fayda ID!
            role = "የኮንስትራክሽን አማካሪ",
            grade = "ጂኦቴክኒካል መሃንዲስ (Geo-Eng 2)",
            licenseNumber = "MUI/GEO/2019/AA-881",
            qualification = "BSc in Geological Engineering",
            institutionGraduated = "Arba Minch University",
            graduationYear = 2018,
            experienceYears = 8,
            fieldOfPractice = "የአፈርና መሰረት ፍተሻ (Soil & Foundation Testing)",
            specialization = "ጂኦቴክኒክ (Geotechnical)",
            authorizedBuildingCategories = "ምድብ ሀ እና ለ (Categories A & B)",
            digitalSignatureVerified = false,
            law1356ComplianceStatus = "ያልተሟላ ሰነድ (MISSING DOCS)",
            muiRegistrationNo = "MUI-GEO-0881/ETH",
            licenseStatus = "ያልተሟላ ሰነድ (MISSING DOCS)",
            licenseExpiryDate = System.currentTimeMillis() + (120L * 24 * 3600 * 1000),
            cpdPoints = 18, // Inadequate CPD (< 20)
            insurancePolicyNo = "", // Missing Professional Indemnity Insurance!
            internationalCertifications = "",
            assignedProjectRole = "የጂኦቴክኒክ አማካሪ (Geotechnical Consultant)"
        )

        repository.insertProfessional(prof1)
        repository.insertProfessional(prof2)
        repository.insertProfessional(prof3)
        repository.insertProfessional(prof4)
        repository.insertProfessional(prof5)
        repository.insertProfessional(prof6)
        repository.insertProfessional(prof7)
        repository.insertProfessional(prof8)
        repository.insertProfessional(prof9)

        // Seed Artisans (CoC Skilled Technical Craftsmen)
        val artisan1 = Artisan(name = "አስቻለው ከበደ", phone = "+251944112233", tradeSector = "ግንበኛ", cocNumber = "COC-MAS-1092-26", certificatePhotoPath = "simulated_coc_mason.jpg", experienceYears = 8, cocLevel = "ደረጃ ፬ (Level IV)", issuingCenter = "አዲስ አበባ ቴግበራእድ ቴክኒክና ሙያ ኮሌጅ", safetyTrainingCompleted = true)
        val artisan2 = Artisan(name = "ዮናስ ታደሰ", phone = "+251940998877", tradeSector = "አናፂና ፎርምወርክ", cocNumber = "COC-CAR-2281-25", certificatePhotoPath = "simulated_coc_carpenter.jpg", experienceYears = 5, cocLevel = "ደረጃ ፫ (Level III)", issuingCenter = "የባህር ዳር ፖሊቴክኒክ ኮሌጅ", safetyTrainingCompleted = true)
        val artisan3 = Artisan(name = "ትርፌ በቀለ", phone = "+251930224455", tradeSector = "ቧንቧና ሳኒተሪ", cocNumber = "COC-PLU-9021-26", certificatePhotoPath = "simulated_coc_plumber.jpg", experienceYears = 6, cocLevel = "ደረጃ ፫ (Level III)", issuingCenter = "የፌዴራል የሙያ ብቃትና ምዘና ኤጀንሲ", safetyTrainingCompleted = true)
        val artisan4 = Artisan(name = "ግሩም ተስፋዬ", phone = "+251915334455", tradeSector = "ኤሌክትሪሺያን", cocNumber = "COC-ELE-4412-24", certificatePhotoPath = "simulated_coc_electrician.jpg", experienceYears = 4, cocLevel = "ደረጃ ፬ (Level IV)", issuingCenter = "የድሬዳዋ ፖሊቴክኒክ ኮሌጅ", safetyTrainingCompleted = true)
        val artisan5 = Artisan(name = "በላይነህ ደረጄ", phone = "+251965889900", tradeSector = "ፌሮዮ (የግንባታ ብረት ሰራተኛ)", cocNumber = "COC-REB-8712-25", certificatePhotoPath = "simulated_coc_rebar.jpg", experienceYears = 10, cocLevel = "ደረጃ ፫ (Level III)", issuingCenter = "የሐዋሳ ቴክኒክና ሙያ ኮሌጅ", safetyTrainingCompleted = true)
        val artisan6 = Artisan(name = "ታምራት ሞላ", phone = "+251977112233", tradeSector = "የተረጋገጠ ብየዳ ሰራተኛ (AWS Welder)", cocNumber = "COC-WLD-5501-26", certificatePhotoPath = "simulated_coc_welder.jpg", experienceYears = 7, cocLevel = "ደረጃ ፬ (Level IV)", issuingCenter = "ኢትዮ-ጣሊያን የቴክኖሎጂ ኢንስቲትዩት", safetyTrainingCompleted = true)

        repository.insertArtisan(artisan1)
        repository.insertArtisan(artisan2)
        repository.insertArtisan(artisan3)
        repository.insertArtisan(artisan4)
        repository.insertArtisan(artisan5)
        repository.insertArtisan(artisan6)

        // Seed Projects
        val proj1 = Project(
            name = "የቲሊሊ ከተማ ማዘጋጃ ቤት ህንጻ ግንባታ",
            location = "ቀበሌ 01 - መካከለኛ አደባባይ",
            buildingCategory = "ምድብ 'ለ' (ባለብዙ ፎቅ ህንጻዎች)",
            constructionType = "የመንግስት አገልግሎት ህንጻ",
            materials = "ኮንክሪት እና ብረት, የሲሚንቶ ብሎኬት (HCB), ድንጋይ, ጂፕሰም",
            consultantId = 2,
            contractorId = 3,
            inspectorId = 1,
            status = "በግንባታ ላይ",
            cadCoordinates = "45.2,38.1; 60.5,38.1; 60.5,55.4; 45.2,55.4",
            bimEstimatedCost = 45000000.0,
            scheduleProgressPercent = 65,
            gisLatitude = 10.5833,
            gisLongitude = 37.1667,
            etabsSafetyScore = 94
        )
        val proj2 = Project(
            name = "የአቶ በላይነህ ሻለም ባለ 3 ፎቅ መኖሪያ ቤት",
            location = "ቀበሌ 02 - ወደ መናኸሪያው መንገድ",
            buildingCategory = "ምድብ 'ሀ' (ከሁለት ፎቅ በታች የሆኑ ወይም አነስተኛ ህንጻዎች)",
            constructionType = "የመኖሪያ ቤት ግንባታ",
            materials = "ኮንክሪት እና ብረት, ጡብ, የሲሚንቶ ብሎኬት (HCB)",
            consultantId = 2,
            contractorId = 3,
            inspectorId = 1,
            status = "በግንባታ ላይ",
            cadCoordinates = "12.0,15.5; 32.0,15.5; 32.0,35.5; 12.0,35.5",
            bimEstimatedCost = 18500000.0,
            scheduleProgressPercent = 40,
            gisLatitude = 10.5845,
            gisLongitude = 37.1682,
            etabsSafetyScore = 88
        )
        val proj3 = Project(
            name = "የቲሊሊ ከተማ ጊዜያዊ የገበያ ሼዶች",
            location = "ቀበሌ 03 - አሮጌው ገበያ",
            buildingCategory = "ጊዜያዊ ግንባታ",
            constructionType = "የገበያ ሼድ",
            materials = "እንጨትና ጭቃ, ቆርቆሮ, ድንጋይ",
            consultantId = null,
            contractorId = 3,
            inspectorId = 4,
            status = "በዝግጅት ላይ",
            cadCoordinates = "5.0,5.0; 25.0,5.0; 25.0,15.0; 5.0,15.0",
            bimEstimatedCost = 1200000.0,
            scheduleProgressPercent = 15,
            gisLatitude = 10.5812,
            gisLongitude = 37.1645,
            etabsSafetyScore = 75
        )
        val proj4 = Project(
            name = "ባለ 1 ፎቅ መጋዘን ግንባታ (Basement ያለው)",
            location = "ቀበሌ 01 - የኢንዱስትሪ ቀጠና",
            buildingCategory = "ፎቅ የሌላቸው / Basement",
            constructionType = "መጋዘን እና ቢሮ",
            materials = "ኮንክሪት እና ብረት, ድንጋይ, ቆርቆሮ",
            consultantId = 2,
            contractorId = 3,
            inspectorId = 1,
            status = "በግንባታ ላይ",
            cadCoordinates = "10.0,10.0; 40.0,10.0; 40.0,30.0; 10.0,30.0",
            bimEstimatedCost = 28000000.0,
            scheduleProgressPercent = 85,
            gisLatitude = 10.5861,
            gisLongitude = 37.1695,
            etabsSafetyScore = 91
        )

        repository.insertProject(proj1)
        repository.insertProject(proj2)
        repository.insertProject(proj3)
        repository.insertProject(proj4)

        // Seed Warnings
        repository.insertWarning(Warning(
            recipientPhone = "+251915998877",
            recipientName = "ቁጥር ፩ ግንባታ ተቋራጭ",
            recipientRole = "የስራ ተቋራጭ",
            message = "በማዘጋጃ ቤት ህንጻ ላይ የግድግዳ ስራ ጥራት ጉድለት ታይቷል። እባክዎ በአስቸኳይ ያስተካክሉ!",
            severity = "ከፍተኛ",
            isSent = true
        ))
        repository.insertWarning(Warning(
            recipientPhone = "+251944112233",
            recipientName = "አስቻለው ከበደ",
            recipientRole = "ግንበኛ",
            message = "የግንባታ ደህንነት ባርኔጣ (Helmet) እና ጓንት ባለመጠቀምዎት ማስጠንቀቂያ ተሰጥቶዎታል።",
            severity = "ዝቅተኛ",
            isSent = true
        ))

        // Seed Daily Progress logs mapped against EBCS standards
        repository.insertDailyProgress(DailyProgress(
            projectId = 1,
            date = System.currentTimeMillis() - 86400000, // Yesterday
            taskDescription = "በማዘጋጃ ቤት ህንጻ 1ኛ ፎቅ ምሰሶዎች ላይ የኮንክሪት ስራ ማከናወን",
            workQuantity = "35 m³",
            standardRefId = "ebcs-2",
            checkpointPassed = "ለፎቅ ህንፃዎች ምሰሶዎች የሚውል ኮንክሪት ቢያንስ C25 (25 MPa) መሆኑን ማረጋገጥ",
            measuredValue = "C25, Slump test: 80mm",
            status = "ተገዢ (COMPLIANT)",
            notes = "የኮንክሪት ድብልቁ ጥራትና ጥንካሬ ደረጃውን የጠበቀ ነው።"
        ))

        repository.insertDailyProgress(DailyProgress(
            projectId = 1,
            date = System.currentTimeMillis() - 172800000, // 2 days ago
            taskDescription = "የምሰሶዎች ማሰሪያ ብረት (Stirrups) ዝግጅትና ማሰር",
            workQuantity = "1.2 Tons",
            standardRefId = "ebcs-2",
            checkpointPassed = "የምሰሶዎች ማሰሪያ ብረት (Stirrups) ክፍተት በከፍተኛ ጭነት ቦታዎች ከ 150mm መብለጥ የለበትም።",
            measuredValue = "የክፍተት ልኬት: 120mm - 140mm",
            status = "ተገዢ (COMPLIANT)",
            notes = "ክፍተቶች በEBCS-2 መሰረት በአግባቡ ተለክተዋል።"
        ))

        repository.insertDailyProgress(DailyProgress(
            projectId = 2,
            date = System.currentTimeMillis() - 86400000, // Yesterday
            taskDescription = "የመጀመሪያ ፎቅ ወለል (Slab) ጭነትና ዝግጅት ስሌት",
            workQuantity = "150 m²",
            standardRefId = "ebcs-1",
            checkpointPassed = "በሰገነትና በበረንዳ ላይ የሚቀመጡ ጭነቶች ቢያንስ 3.0 kN/m² መሆናቸውን ማረጋገጥ",
            measuredValue = "የታሰበው ጭነት: 3.2 kN/m²",
            status = "ተገዢ (COMPLIANT)",
            notes = "በዲዛይኑ መሰረት ተረጋግጧል።"
        ))

        repository.insertDailyProgress(DailyProgress(
            projectId = 1,
            date = System.currentTimeMillis(), // Today
            taskDescription = "የማዘጋጃ ቤት ሁለተኛ ፎቅ ምሰሶዎች የአርማታ ሽፋን (Concrete Cover)",
            workQuantity = "8 Columns",
            standardRefId = "ebcs-2",
            checkpointPassed = "የምሰሶዎች የአርማታ ሽፋን ውፍረት (Concrete Cover for columns: Min 30mm - 40mm)",
            measuredValue = "የሽፋን ውፍረት ልኬት: 20mm",
            status = "ያልተገዢ (NON-COMPLIANT)",
            notes = "የሽፋን ውፍረት ከአነስተኛው 30mm በታች ሆኖ ተገኝቷል። አስቸኳይ ማሻሻያ እንዲደረግ ተቋራጩ ታዟል።"
        ))
    }

    fun addArtisan(name: String, phone: String, tradeSector: String, cocNumber: String, path: String, exp: Int, hasPermit: Boolean = true) {
        viewModelScope.launch {
            repository.insertArtisan(Artisan(
                name = name,
                phone = phone,
                tradeSector = tradeSector,
                cocNumber = cocNumber,
                certificatePhotoPath = path,
                experienceYears = exp,
                hasPermit = hasPermit
            ))
        }
    }

    fun updateArtisan(artisan: Artisan) {
        viewModelScope.launch {
            repository.insertArtisan(artisan)
        }
    }

    fun deleteArtisan(artisan: Artisan) {
        viewModelScope.launch {
            repository.deleteArtisan(artisan)
        }
    }

    fun addProfessional(name: String, phone: String, role: String, grade: String, license: String) {
        viewModelScope.launch {
            repository.insertProfessional(Professional(
                name = name,
                phone = phone,
                role = role,
                grade = grade,
                licenseNumber = license
            ))
        }
    }

    fun addProfessionalComprehensive(professional: Professional) {
        viewModelScope.launch {
            repository.insertProfessional(professional)
        }
    }

    fun deleteProfessional(professional: Professional) {
        viewModelScope.launch {
            repository.deleteProfessional(professional)
        }
    }

    fun updateProfessional(professional: Professional) {
        viewModelScope.launch {
            repository.insertProfessional(professional)
        }
    }

    /**
     * Statutory License Renewal under Ethiopian Building Law 1356/2017 & Directives
     * Extends license validity and ensures compliance with CPD and Insurance requirements
     */
    fun renewProfessionalLicense(professional: Professional, yearsToAdd: Int = 1, cpdBoost: Int = 30) {
        viewModelScope.launch {
            val newExpiry = System.currentTimeMillis() + (yearsToAdd.toLong() * 365L * 24 * 3600 * 1000L)
            val updated = professional.copy(
                licenseStatus = "ተገዢ (COMPLIANT)",
                law1356ComplianceStatus = "ተገዢ (COMPLIANT)",
                licenseExpiryDate = newExpiry,
                cpdPoints = (professional.cpdPoints + cpdBoost).coerceAtLeast(30),
                lastAccreditationUpdate = System.currentTimeMillis()
            )
            repository.insertProfessional(updated)
        }
    }

    fun updateProfessionalComplianceStatus(professional: Professional, newStatus: String) {
        viewModelScope.launch {
            val updated = professional.copy(
                licenseStatus = newStatus,
                law1356ComplianceStatus = newStatus,
                lastAccreditationUpdate = System.currentTimeMillis()
            )
            repository.insertProfessional(updated)
        }
    }

    /**
     * Law 1356/2017 Automatic Document Verification Engine
     */
    fun verifyProfessionalDocumentation(professional: Professional): ProfessionalVerificationReport {
        return Law1356VerificationEngine.verifyProfessional(professional)
    }

    fun verifyAllProfessionals(professionals: List<Professional>): List<ProfessionalVerificationReport> {
        return Law1356VerificationEngine.verifyAll(professionals)
    }

    fun getSystemVerificationAudit(professionals: List<Professional>): SystemVerificationSummary {
        return Law1356VerificationEngine.generateSystemSummary(professionals)
    }

    /**
     * Automatically scans all registered professionals and flags any non-compliant, expired,
     * or missing documentation according to statutory rules of Law 1356/2017.
     * Updates database records so compliance status is synchronized with reality.
     */
    fun runAutomatedStatutoryAudit(professionals: List<Professional>, onComplete: (Int, Int) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            var updatedCount = 0
            var flaggedCount = 0
            professionals.forEach { prof ->
                val report = Law1356VerificationEngine.verifyProfessional(prof)
                if (!report.isFullyCompliant || report.hasExpiredDocs || report.hasMissingDocs) {
                    flaggedCount++
                    if (prof.law1356ComplianceStatus != report.statutoryStatus || prof.licenseStatus != report.statutoryStatus) {
                        val newStatus = report.statutoryStatus
                        val updated = prof.copy(
                            law1356ComplianceStatus = newStatus,
                            licenseStatus = newStatus,
                            lastAccreditationUpdate = System.currentTimeMillis()
                        )
                        repository.insertProfessional(updated)
                        updatedCount++
                    }
                }
            }
            onComplete(flaggedCount, updatedCount)
        }
    }

    /**
     * Resolves an individual statutory flag (e.g. updating insurance policy, national Fayda ID, CPD credits, digital seal)
     */
    fun resolveFlag(
        professional: Professional,
        flagType: DocFlagType,
        newValue: String
    ) {
        viewModelScope.launch {
            val updated = when (flagType) {
                DocFlagType.MISSING_INDEMNITY_INSURANCE, DocFlagType.EXPIRED_INDEMNITY_INSURANCE, DocFlagType.EXPIRING_INDEMNITY_INSURANCE -> {
                    professional.copy(
                        insurancePolicyNo = newValue.ifBlank { "ETH-INS-PII-${(1000..9999).random()}" },
                        insuranceExpiryDate = System.currentTimeMillis() + (365L * 24 * 3600 * 1000L),
                        law1356ComplianceStatus = if (professional.licenseExpiryDate > System.currentTimeMillis() && professional.cpdPoints >= 30) "ተገዢ (COMPLIANT)" else professional.law1356ComplianceStatus,
                        lastAccreditationUpdate = System.currentTimeMillis()
                    )
                }
                DocFlagType.MISSING_FAYDA_NATIONAL_ID -> {
                    professional.copy(
                        nationalId = newValue.ifBlank { "ET-FAYDA-${(10000000..99999999).random()}" },
                        lastAccreditationUpdate = System.currentTimeMillis()
                    )
                }
                DocFlagType.CRITICAL_CPD_SHORTFALL, DocFlagType.INSUFFICIENT_CPD_POINTS -> {
                    val addPoints = newValue.toIntOrNull() ?: 15
                    val newCpd = (professional.cpdPoints + addPoints).coerceAtLeast(30)
                    professional.copy(
                        cpdPoints = newCpd,
                        law1356ComplianceStatus = if (professional.licenseExpiryDate > System.currentTimeMillis() && professional.insurancePolicyNo.isNotBlank()) "ተገዢ (COMPLIANT)" else professional.law1356ComplianceStatus,
                        lastAccreditationUpdate = System.currentTimeMillis()
                    )
                }
                DocFlagType.UNVERIFIED_DIGITAL_SIGNATURE -> {
                    professional.copy(
                        digitalSignatureVerified = true,
                        lastAccreditationUpdate = System.currentTimeMillis()
                    )
                }
                DocFlagType.EXPIRED_PRACTICING_LICENSE, DocFlagType.EXPIRING_PRACTICING_LICENSE -> {
                    professional.copy(
                        licenseStatus = "ተገዢ (COMPLIANT)",
                        law1356ComplianceStatus = "ተገዢ (COMPLIANT)",
                        licenseExpiryDate = System.currentTimeMillis() + (365L * 24 * 3600 * 1000L),
                        cpdPoints = (professional.cpdPoints + 10).coerceAtLeast(30),
                        lastAccreditationUpdate = System.currentTimeMillis()
                    )
                }
                DocFlagType.MISSING_PRACTICING_LICENSE -> {
                    professional.copy(
                        licenseNumber = newValue.ifBlank { "MUI-REG-2026/AA-${(100..999).random()}" },
                        licenseStatus = "ተገዢ (COMPLIANT)",
                        law1356ComplianceStatus = "ተገዢ (COMPLIANT)",
                        licenseExpiryDate = System.currentTimeMillis() + (365L * 24 * 3600 * 1000L),
                        lastAccreditationUpdate = System.currentTimeMillis()
                    )
                }
                DocFlagType.MISSING_ATTACHED_LICENSE_FILE -> {
                    val newDocs = if (professional.attachedDocsJson.contains("MUI_Practicing_License")) professional.attachedDocsJson else "[\"MUI_Practicing_License.pdf\", \"Degree_Certificate.pdf\", \"Insurance_Policy.pdf\"]"
                    professional.copy(attachedDocsJson = newDocs, lastAccreditationUpdate = System.currentTimeMillis())
                }
                DocFlagType.MISSING_ATTACHED_INSURANCE_FILE -> {
                    val newDocs = if (professional.attachedDocsJson.contains("Insurance_Policy")) professional.attachedDocsJson else "[\"Degree_Certificate.pdf\", \"MUI_Practicing_License.pdf\", \"Insurance_Policy.pdf\"]"
                    professional.copy(attachedDocsJson = newDocs, lastAccreditationUpdate = System.currentTimeMillis())
                }
                DocFlagType.MISSING_DEGREE_DOCUMENT -> {
                    val currentDocs = professional.attachedDocsJson
                    val updatedDocs = if (currentDocs.isBlank() || currentDocs == "[]") {
                        "[\"Degree_Certificate.pdf\", \"MUI_Practicing_License.pdf\"]"
                    } else {
                        currentDocs.replace("]", ", \"Degree_Certificate.pdf\"]")
                    }
                    professional.copy(attachedDocsJson = updatedDocs, lastAccreditationUpdate = System.currentTimeMillis())
                }
                DocFlagType.MISSING_ATTACHED_DOSSIER -> {
                    professional.copy(
                        attachedDocsJson = "[\"MUI_Practicing_License.pdf\", \"Degree_Certificate.pdf\", \"Insurance_Policy.pdf\", \"CPD_Certificate.pdf\"]",
                        digitalSignatureVerified = true,
                        lastAccreditationUpdate = System.currentTimeMillis()
                    )
                }
                else -> professional
            }
            val finalUpdated = if (updated.licenseExpiryDate > System.currentTimeMillis() && updated.insurancePolicyNo.isNotBlank() && updated.cpdPoints >= 30) {
                updated.copy(licenseStatus = "ተገዢ (COMPLIANT)", law1356ComplianceStatus = "ተገዢ (COMPLIANT)")
            } else {
                updated
            }
            repository.insertProfessional(finalUpdated)
        }
    }

    fun addProject(name: String, location: String, category: String, type: String, materials: String, consultantId: Int?, contractorId: Int?, inspectorId: Int?, status: String) {
        viewModelScope.launch {
            repository.insertProject(Project(
                name = name,
                location = location,
                buildingCategory = category,
                constructionType = type,
                materials = materials,
                consultantId = consultantId,
                contractorId = contractorId,
                inspectorId = inspectorId,
                status = status
            ))
        }
    }

    fun addProjectExtended(name: String, location: String, category: String, type: String, materials: String, consultantId: Int?, contractorId: Int?, inspectorId: Int?, status: String, cad: String?, bim: Double?, progress: Int, lat: Double?, lon: Double?, safety: Int?) {
        viewModelScope.launch {
            repository.insertProject(Project(
                name = name,
                location = location,
                buildingCategory = category,
                constructionType = type,
                materials = materials,
                consultantId = consultantId,
                contractorId = contractorId,
                inspectorId = inspectorId,
                status = status,
                cadCoordinates = cad,
                bimEstimatedCost = bim,
                scheduleProgressPercent = progress,
                gisLatitude = lat,
                gisLongitude = lon,
                etabsSafetyScore = safety
            ))
        }
    }

    fun updateProjectEngineeringData(projectId: Int, cad: String?, bim: Double?, schedule: Int, lat: Double?, lon: Double?, etabs: Int?) {
        viewModelScope.launch {
            val project = projects.value.find { it.id == projectId }
            if (project != null) {
                repository.insertProject(project.copy(
                    cadCoordinates = cad ?: project.cadCoordinates,
                    bimEstimatedCost = bim ?: project.bimEstimatedCost,
                    scheduleProgressPercent = if (schedule >= 0) schedule else project.scheduleProgressPercent,
                    gisLatitude = lat ?: project.gisLatitude,
                    gisLongitude = lon ?: project.gisLongitude,
                    etabsSafetyScore = etabs ?: project.etabsSafetyScore
                ))
            }
        }
    }

    fun updateProject(project: Project) {
        viewModelScope.launch {
            repository.insertProject(project)
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            repository.deleteProject(project)
        }
    }

    fun sendWarning(name: String, phone: String, role: String, message: String, severity: String) {
        viewModelScope.launch {
            repository.insertWarning(Warning(
                recipientPhone = phone,
                recipientName = name,
                recipientRole = role,
                message = message,
                severity = severity,
                isSent = true
            ))
        }
    }

    fun deleteWarning(warning: Warning) {
        viewModelScope.launch {
            repository.deleteWarning(warning)
        }
    }

    fun addDailyProgress(
        projectId: Int,
        taskDescription: String,
        workQuantity: String,
        standardRefId: String,
        checkpointPassed: String,
        measuredValue: String,
        status: String,
        notes: String = "",
        inspectorName: String = "በላይነህ የሻለም (Civil Engineer)",
        audioPath: String? = null
    ) {
        viewModelScope.launch {
            repository.insertDailyProgress(DailyProgress(
                projectId = projectId,
                taskDescription = taskDescription,
                workQuantity = workQuantity,
                standardRefId = standardRefId,
                checkpointPassed = checkpointPassed,
                measuredValue = measuredValue,
                status = status,
                notes = notes,
                inspectorName = inspectorName,
                isSynced = !isOfflineMode.value,
                audioPath = audioPath
            ))
        }
    }

    fun deleteDailyProgress(progress: DailyProgress) {
        viewModelScope.launch {
            repository.deleteDailyProgress(progress)
        }
    }

    fun addRiskAssessment(
        projectId: Int,
        hazardTitle: String,
        description: String,
        riskLevel: String,
        mitigationPlan: String,
        ebcsStandardRefId: String,
        safetyClause: String,
        status: String = "OPEN"
    ) {
        viewModelScope.launch {
            repository.insertRiskAssessment(
                RiskAssessment(
                    projectId = projectId,
                    hazardTitle = hazardTitle,
                    description = description,
                    riskLevel = riskLevel,
                    mitigationPlan = mitigationPlan,
                    ebcsStandardRefId = ebcsStandardRefId,
                    safetyClause = safetyClause,
                    status = status,
                    isSynced = !isOfflineMode.value
                )
            )
            if (riskLevel == "HIGH" || riskLevel == "CRITICAL") {
                triggerLocalNotification(
                    getApplication(),
                    "⚠️ ከፍተኛ ግንባታ አደጋ ተመዝግቧል (${riskLevel})",
                    "በግንባታ ላይ ከፍተኛ ስጋት ተለይቷል፡ $hazardTitle"
                )
            }
        }
    }

    fun toggleRiskAssessmentStatus(assessment: RiskAssessment) {
        viewModelScope.launch {
            val updated = assessment.copy(status = if (assessment.status == "OPEN") "RESOLVED" else "OPEN")
            repository.insertRiskAssessment(updated)
        }
    }

    fun deleteRiskAssessment(assessment: RiskAssessment) {
        viewModelScope.launch {
            repository.deleteRiskAssessment(assessment)
        }
    }

    // Reports Generation
    fun generateExcelReport(context: Context): File? {
        val file = File(context.cacheDir, "የቲሊሊ_ከተማ_ግንባታ_ቁጥጥር_ሪፖርት.csv")
        try {
            val fos = FileOutputStream(file)
            // Add UTF-8 BOM for Excel Amharic compatibility
            fos.write(0xEF)
            fos.write(0xBB)
            fos.write(0xBF)

            val writer = fos.bufferedWriter()
            writer.write("የቲሊሊ ከተማ ኮንስትራክሽን ጽሕፈት ቤት ግንባታ ክትትልና ቁጥጥር ቡድን\n")
            writer.write("ባለቤትነት፡ በላይነህ የሻለም (Civil Engineer)\n\n")

            // Projects Sheet / Section
            writer.write("--- ግንባታዎች / ፕሮጀክቶች ---\n")
            writer.write("የፕሮጀክት ስም,ቦታ,የህንጻ ምድብ,የግንባታ አይነት,ቁሳቁሶች,ሁኔታ\n")
            projects.value.forEach {
                writer.write("\"${it.name}\",\"${it.location}\",\"${it.buildingCategory}\",\"${it.constructionType}\",\"${it.materials}\",\"${it.status}\"\n")
            }

            writer.write("\n--- የልምድ ሰራተኞች (Artisans) ---\n")
            writer.write("ስም,ስልክ,የስራ ዘርፍ,የብቃት ወረቀት (COC),ልምድ (አመት)\n")
            artisans.value.forEach {
                writer.write("\"${it.name}\",\"${it.phone}\",\"${it.tradeSector}\",\"${it.cocNumber}\",\"${it.experienceYears}\"\n")
            }

            writer.write("\n--- አማካሪዎች፡ ተቋራጮች እና ተቆጣጣሪዎች ---\n")
            writer.write("ስም,ስልክ,ሚና/ድርሻ,ደረጃ,የፈቃድ ቁጥር\n")
            professionals.value.forEach {
                writer.write("\"${it.name}\",\"${it.phone}\",\"${it.role}\",\"${it.grade}\",\"${it.licenseNumber}\"\n")
            }

            writer.write("\n--- የተላኩ ማስጠንቀቂያዎች ---\n")
            writer.write("ተቀባይ,ስልክ,ሚና,ማስጠንቀቂያ,ክብደት,ሁኔታ\n")
            warnings.value.forEach {
                writer.write("\"${it.recipientName}\",\"${it.recipientPhone}\",\"${it.recipientRole}\",\"${it.message}\",\"${it.severity}\",\"ተልኳል\"\n")
            }

            writer.flush()
            writer.close()
            fos.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    class PdfCreator(private val file: File, private val institutionName: String = "የአማራ ክልል ኮንስትራክሽን ባለስልጣን") {
        private val document = PdfDocument()
        private val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        private var currentPage: PdfDocument.Page? = null
        private var canvas: Canvas? = null
        private var currentY = 50f
        private val leftMargin = 40f
        private val rightMargin = 555f
        private val bottomLimit = 780f // 842 - 62
        private var pageNumber = 0

        init {
            addNewPage()
        }

        private fun addNewPage() {
            if (currentPage != null) {
                document.finishPage(currentPage)
            }
            pageNumber++
            currentPage = document.startPage(pageInfo)
            canvas = currentPage?.canvas
            currentY = 50f
            
            // Draw page number in the footer of every page
            val paint = Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 8f
                isAntiAlias = true
            }
            canvas?.drawText("ገጽ $pageNumber / Page $pageNumber", leftMargin, 810f, paint)
            canvas?.drawText("$institutionName - Digital Regulatory System", 270f, 810f, paint)
            
            // Header line
            val paintLine = Paint().apply {
                color = android.graphics.Color.LTGRAY
                strokeWidth = 0.5f
            }
            canvas?.drawLine(leftMargin, 800f, rightMargin, 800f, paintLine)
        }

        // Helper to draw text with word wrap & newline support
        fun drawText(text: String, size: Float = 10f, isBold: Boolean = false, color: Int = android.graphics.Color.BLACK, spaceAfter: Float = 4f) {
            val paragraphs = text.split("\n")
            val paint = Paint().apply {
                this.color = color
                this.textSize = size
                this.isAntiAlias = true
                if (isBold) {
                    this.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
            }
            val maxTextWidth = rightMargin - leftMargin

            for (paragraph in paragraphs) {
                if (paragraph.isBlank()) {
                    currentY += spaceAfter + 2f
                    continue
                }
                val words = paragraph.split(" ")
                var currentLine = StringBuilder()

                for (word in words) {
                    if (word.isEmpty()) continue
                    val testLine = if (currentLine.isEmpty()) word else "${currentLine} $word"
                    val width = paint.measureText(testLine)
                    if (width < maxTextWidth) {
                        currentLine.append(if (currentLine.isEmpty()) word else " $word")
                    } else {
                        if (currentLine.isEmpty()) {
                            drawSingleLine(word, paint, size, spaceAfter)
                        } else {
                            drawSingleLine(currentLine.toString(), paint, size, spaceAfter)
                            currentLine = StringBuilder(word)
                        }
                    }
                }

                if (currentLine.isNotEmpty()) {
                    drawSingleLine(currentLine.toString(), paint, size, spaceAfter)
                }
            }
        }

        fun drawSubSectionTitle(title: String, color: Int = android.graphics.Color.rgb(20, 80, 120)) {
            val size = 10f
            val paint = Paint().apply {
                this.color = color
                this.textSize = size
                this.isAntiAlias = true
                this.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            if (currentY + size + 12f > bottomLimit) {
                addNewPage()
            }

            canvas?.drawText(title, leftMargin + 4f, currentY + size, paint)
            currentY += size + 6f
        }

        fun drawCalloutBox(title: String, points: List<String>, accentColor: Int = android.graphics.Color.rgb(0, 130, 120)) {
            val approxHeight = 22f + (points.size * 18f)
            if (currentY + approxHeight > bottomLimit) {
                addNewPage()
            }
            val startY = currentY
            val paintTitle = Paint().apply {
                this.color = accentColor
                this.textSize = 9.5f
                this.isAntiAlias = true
                this.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            currentY += 4f
            canvas?.drawText("📌 $title", leftMargin + 10f, currentY + 8f, paintTitle)
            currentY += 15f

            points.forEach { pt ->
                drawBulletPoint(pt, bulletColor = accentColor)
            }

            val endY = currentY + 4f
            val paintStripe = Paint().apply {
                this.color = accentColor
                this.style = Paint.Style.FILL
            }
            canvas?.drawRect(leftMargin, startY, leftMargin + 3.5f, endY, paintStripe)
            val paintBorder = Paint().apply {
                this.color = android.graphics.Color.rgb(220, 225, 230)
                this.style = Paint.Style.STROKE
                this.strokeWidth = 0.8f
            }
            canvas?.drawRect(leftMargin, startY, rightMargin, endY, paintBorder)
            currentY = endY + 8f
        }

        private fun drawSingleLine(line: String, paint: Paint, size: Float, spaceAfter: Float) {
            val lineHeight = size + 3f
            if (currentY + lineHeight > bottomLimit) {
                addNewPage()
            }
            
            canvas?.drawText(line, leftMargin, currentY + size, paint)
            currentY += lineHeight + spaceAfter
        }

        fun drawHeaderBanner(titleAm: String, titleEn: String, subtitle: String? = null) {
            val bannerHeight = if (subtitle != null) 75f else 60f
            if (currentY + bannerHeight > bottomLimit) {
                addNewPage()
            }

            // Draw background banner rect with a dark professional look
            val paintBg = Paint().apply {
                color = android.graphics.Color.rgb(20, 25, 30) // Dark Slate color
                style = Paint.Style.FILL
            }
            canvas?.drawRect(leftMargin, currentY, rightMargin, currentY + bannerHeight, paintBg)

            // Title Amharic
            val paintTitleAm = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 13f
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas?.drawText(titleAm, leftMargin + 15f, currentY + 22f, paintTitleAm)

            // Title English
            val paintTitleEn = Paint().apply {
                color = android.graphics.Color.rgb(0, 150, 136) // ConstructionTeal like
                textSize = 10f
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas?.drawText(titleEn, leftMargin + 15f, currentY + 38f, paintTitleEn)

            if (subtitle != null) {
                val paintSub = Paint().apply {
                    color = android.graphics.Color.LTGRAY
                    textSize = 9f
                    isAntiAlias = true
                }
                canvas?.drawText(subtitle, leftMargin + 15f, currentY + 55f, paintSub)
            }

            currentY += bannerHeight + 15f
        }

        fun drawSectionTitle(title: String) {
            val size = 11f
            val paint = Paint().apply {
                color = android.graphics.Color.rgb(0, 120, 110) // Dark Teal
                textSize = size
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            if (currentY + size + 15f > bottomLimit) {
                addNewPage()
            }

            // Left sidebar indicator for Section
            val paintBar = Paint().apply {
                color = android.graphics.Color.rgb(0, 150, 136) // ConstructionTeal
                style = Paint.Style.FILL
            }
            canvas?.drawRect(leftMargin, currentY, leftMargin + 4f, currentY + size + 4f, paintBar)

            canvas?.drawText(title, leftMargin + 10f, currentY + size + 1f, paint)
            currentY += size + 10f
        }

        fun drawLabelAndValue(label: String, value: String) {
            val size = 9f
            val paintLabel = Paint().apply {
                color = android.graphics.Color.rgb(80, 80, 80) // Dark Gray
                textSize = size
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val paintValue = Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = size
                isAntiAlias = true
            }

            val labelWidth = paintLabel.measureText(label) + 4f
            val maxTextWidth = rightMargin - leftMargin - labelWidth
            
            // Wrap value if needed
            val words = value.split(" ")
            var currentLine = StringBuilder()
            var isFirstLine = true

            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "${currentLine} $word"
                val width = paintValue.measureText(testLine)
                if (width < maxTextWidth) {
                    currentLine.append(if (currentLine.isEmpty()) word else " $word")
                } else {
                    if (isFirstLine) {
                        drawLabelLine(label, currentLine.toString(), paintLabel, paintValue, size)
                        isFirstLine = false
                    } else {
                        drawLabelLine("", currentLine.toString(), paintLabel, paintValue, size)
                    }
                    currentLine = StringBuilder(word)
                }
            }

            if (currentLine.isNotEmpty() || isFirstLine) {
                if (isFirstLine) {
                    drawLabelLine(label, currentLine.toString(), paintLabel, paintValue, size)
                } else {
                    drawLabelLine("", currentLine.toString(), paintLabel, paintValue, size)
                }
            }
        }

        private fun drawLabelLine(label: String, valueLine: String, paintLabel: Paint, paintValue: Paint, size: Float) {
            val lineHeight = size + 3f
            if (currentY + lineHeight > bottomLimit) {
                addNewPage()
            }
            
            if (label.isNotEmpty()) {
                canvas?.drawText(label, leftMargin + 12f, currentY + size, paintLabel)
                val labelWidth = paintLabel.measureText(label)
                canvas?.drawText(valueLine, leftMargin + 12f + labelWidth, currentY + size, paintValue)
            } else {
                canvas?.drawText(valueLine, leftMargin + 40f, currentY + size, paintValue)
            }
            currentY += lineHeight + 2f
        }

        fun drawSeparator() {
            if (currentY + 10f > bottomLimit) {
                addNewPage()
            }
            val paintLine = Paint().apply {
                color = android.graphics.Color.rgb(230, 230, 230) // Very Light Gray
                strokeWidth = 1f
            }
            canvas?.drawLine(leftMargin, currentY + 4f, rightMargin, currentY + 4f, paintLine)
            currentY += 10f
        }

        fun drawBulletPoint(text: String, bulletColor: Int = android.graphics.Color.rgb(0, 150, 136)) {
            val size = 9.5f
            val paintBullet = Paint().apply {
                color = bulletColor
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val paintText = Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = size
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            if (currentY + size + 10f > bottomLimit) {
                addNewPage()
            }

            canvas?.drawCircle(leftMargin + 6f, currentY + size / 2f + 2f, 2.5f, paintBullet)
            canvas?.drawText(text, leftMargin + 15f, currentY + size, paintText)
            currentY += size + 4f
        }

        fun drawCodeBlock(code: String) {
            val lines = code.trimIndent().split("\n")
            val paintText = Paint().apply {
                color = android.graphics.Color.rgb(35, 45, 55)
                textSize = 8f
                typeface = Typeface.MONOSPACE
                isAntiAlias = true
            }
            val lineHeight = 12f
            val totalHeight = (lines.size * lineHeight) + 12f

            if (currentY + totalHeight > bottomLimit) {
                addNewPage()
            }

            val startY = currentY
            val endY = startY + totalHeight

            val paintBg = Paint().apply {
                color = android.graphics.Color.rgb(243, 246, 249)
                style = Paint.Style.FILL
            }
            canvas?.drawRect(leftMargin, startY, rightMargin, endY, paintBg)

            val paintBorder = Paint().apply {
                color = android.graphics.Color.rgb(210, 220, 230)
                style = Paint.Style.STROKE
                strokeWidth = 0.8f
            }
            canvas?.drawRect(leftMargin, startY, rightMargin, endY, paintBorder)

            val paintLeftAccent = Paint().apply {
                color = android.graphics.Color.rgb(0, 150, 136)
                style = Paint.Style.FILL
            }
            canvas?.drawRect(leftMargin, startY, leftMargin + 3.5f, endY, paintLeftAccent)

            var lineY = startY + 10f
            for (line in lines) {
                if (lineY > bottomLimit) {
                    addNewPage()
                    lineY = currentY + 10f
                }
                canvas?.drawText(line, leftMargin + 8f, lineY, paintText)
                lineY += lineHeight
            }
            currentY = endY + 8f
        }

        fun finish() {
            if (currentPage != null) {
                document.finishPage(currentPage)
            }
            val fos = FileOutputStream(file)
            document.writeTo(fos)
            document.close()
            fos.close()
        }
    }

    fun generatePdfTextReport(context: Context): File? {
        val file = File(context.cacheDir, "የቲሊሊ_ከተማ_ግንባታ_ቁጥጥር_ሪፖርት.pdf")
        try {
            val pdfCreator = PdfCreator(file)
            
            pdfCreator.drawHeaderBanner(
                titleAm = "የቲሊሊ ከተማ ኮንስትራክሽን ጽሕፈት ቤት ግንባታ ክትትልና ቁጥጥር ቡድን",
                titleEn = "TILILI CITY CONSTRUCTION REGULATION & INSPECTION REPORT",
                subtitle = "ባለቤትነት፡ በላይነህ የሻለም (Civil Engineer) | ቀን፡ ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}"
            )

            // 1. Projects Section
            pdfCreator.drawSectionTitle("1. በስራ ላይ ያሉ ግንባታዎች (PROJECTS REGISTER)")
            projects.value.forEachIndexed { i, p ->
                pdfCreator.drawBulletPoint("${i + 1}. ${p.name}")
                pdfCreator.drawLabelAndValue("  • ቦታ / Location: ", p.location)
                pdfCreator.drawLabelAndValue("  • ምድብ / Category: ", p.buildingCategory)
                pdfCreator.drawLabelAndValue("  • አይነት / Type: ", p.constructionType)
                pdfCreator.drawLabelAndValue("  • ቁሳቁስ / Materials: ", p.materials)
                pdfCreator.drawLabelAndValue("  • ሁኔታ / Status: ", p.status)
                pdfCreator.drawSeparator()
            }

            // 2. Artisans Section
            pdfCreator.drawSectionTitle("2. የልምድ ሰራተኞች ምዝገባ (SKILLED ARTISANS REGISTER)")
            artisans.value.forEachIndexed { i, a ->
                pdfCreator.drawBulletPoint("${i + 1}. ${a.name} | ${a.tradeSector}")
                pdfCreator.drawLabelAndValue("  • ስልክ / Phone: ", a.phone)
                pdfCreator.drawLabelAndValue("  • የብቃት ማረጋገጫ (COC): ", a.cocNumber)
                pdfCreator.drawLabelAndValue("  • የስራ ልምድ / Experience: ", "${a.experienceYears} አመታት / Years")
                pdfCreator.drawSeparator()
            }

            // 3. Professionals Section
            pdfCreator.drawSectionTitle("3. የባለሙያዎች፡ አማካሪዎችና ተቋራጮች ዝርዝ (PROFESSIONALS REGISTER)")
            professionals.value.forEachIndexed { i, pr ->
                pdfCreator.drawBulletPoint("${i + 1}. ${pr.name} (${pr.role})")
                pdfCreator.drawLabelAndValue("  • ስልክ / Phone: ", pr.phone)
                pdfCreator.drawLabelAndValue("  • ደረጃ / Grade: ", pr.grade)
                pdfCreator.drawLabelAndValue("  • የሙያ ፈቃድ ቁጥር: ", pr.licenseNumber)
                pdfCreator.drawSeparator()
            }

            // 4. Warnings Section
            pdfCreator.drawSectionTitle("4. የተላኩ ማስጠንቀቂያዎች (DISPATCHED ALERTS)")
            warnings.value.forEachIndexed { i, w ->
                pdfCreator.drawBulletPoint("${i + 1}. ለ ${w.recipientName} (${w.recipientRole})")
                pdfCreator.drawLabelAndValue("  • ስልክ / Phone: ", w.recipientPhone)
                pdfCreator.drawLabelAndValue("  • ደረጃ / Level: ", "${w.severity} ማስጠንቀቂያ / Warning")
                pdfCreator.drawLabelAndValue("  • መልዕክት / Message: ", w.message)
                pdfCreator.drawLabelAndValue("  • ሁኔታ / Dispatch Status: ", "በስልክ ተልኳል (SMS Dispatch Completed)")
                pdfCreator.drawSeparator()
            }

            pdfCreator.finish()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun promptShareReport(isExcel: Boolean) {
        _shareReportState.value = ShareReportState(
            prompt = ShareReportPrompt(
                id = "project_registry_report",
                titleAm = "የቲሊሊ ከተማ ኮንስትራክሽን ሪፖርት",
                titleEn = "Construction Registry Report",
                descriptionAm = "የግንባታ ፕሮጀክቶች፣ የቴክኒክ ባለሙያዎች እና የሙያ ፈቃድ ማረጋገጫ ዝርዝር የያዘ የተሟላ ሪፖርት።",
                fileName = if (isExcel) "የቲሊሊ_ከተማ_የግንባታ_ፕሮጀክቶች_ሪፖርት.csv" else "የቲሊሊ_ከተማ_የግንባታ_ፕሮጀክቶች_ሪፖርት.pdf",
                format = if (isExcel) ReportFormat.EXCEL else ReportFormat.PDF,
                subject = if (isExcel) "የቲሊሊ ከተማ ኮንስትራክሽን Excel ሪፖርት" else "የቲሊሊ ከተማ ኮንስትራክሽን PDF ሪፖርት",
                shareBodyText = "የቲሊሊ ከተማ ኮንስትራክሽን ጽሕፈት ቤት ግንባታ ክትትልና ቁጥጥር ቡድን የተዘጋጀ ሪፖርት",
                generateAction = { ctx -> if (isExcel) generateExcelReport(ctx) else generatePdfTextReport(ctx) }
            )
        )
    }

    fun shareReport(context: Context, isExcel: Boolean) {
        promptShareReport(isExcel)
    }

    fun generateSummaryReportExcel(context: Context): File? {
        val file = File(context.cacheDir, "የግንባታ_ክትትልና_የደህንነት_አደጋዎች_ማጠቃለያ_ሪፖርት.csv")
        try {
            val fos = FileOutputStream(file)
            // Add UTF-8 BOM for Excel Amharic compatibility
            fos.write(0xEF)
            fos.write(0xBB)
            fos.write(0xBF)

            val writer = fos.bufferedWriter()
            writer.write("የግንባታ ክትትልና የደህንነት አደጋዎች ማጠቃለያ ሪፖርት (Construction Progress & Risk Assessments Summary Report)\n")
            writer.write("የቲሊሊ ከተማ ኮንስትራክሽን ጽሕፈት ቤት ግንባታ ክትትልና ቁጥጥር ቡድን\n")
            writer.write("ሪፖርት ያወጣው ባለሙያ፡ በላይነህ የሻለም (Civil Engineer)\n\n")

            // 1. Projects Section
            writer.write("--- 1. በክትትል ላይ ያሉ ግንባታዎች / ፕሮጀክቶች ---\n")
            writer.write("የፕሮጀክት ስም,ቦታ,የህንጻ ምድብ,የግግንባታ አይነት,ቁሳቁሶች,ሁኔታ\n")
            projects.value.forEach {
                writer.write("\"${it.name}\",\"${it.location}\",\"${it.buildingCategory}\",\"${it.constructionType}\",\"${it.materials}\",\"${it.status}\"\n")
            }

            // 2. Risk Assessments Section
            writer.write("\n--- 2. የተመዘገቡ የደህንነት አደጋዎችና ተዛማጅ EBCS ስታንዳርዶች ---\n")
            writer.write("ፕሮጀክት,የአደጋው ርዕስ,ዝርዝር መግለጫ,የአደጋ ደረጃ,የመፍትሄ ዕቅድ,የታገደበት EBCS ኮድ,የደህንነት አንቀጽ,ሁኔታ,የተመዘገበበት ቀን\n")
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            riskAssessments.value.forEach { ra ->
                val projName = projects.value.find { it.id == ra.projectId }?.name ?: "ያልታወቀ"
                val dateStr = sdf.format(java.util.Date(ra.dateLogged))
                writer.write("\"$projName\",\"${ra.hazardTitle}\",\"${ra.description}\",\"${ra.riskLevel}\",\"${ra.mitigationPlan}\",\"${ra.ebcsStandardRefId.uppercase(java.util.Locale.ROOT)}\",\"${ra.safetyClause}\",\"${ra.status}\",\"$dateStr\"\n")
            }

            // 3. Daily Progress Logs Section
            writer.write("\n--- 3. ዕለታዊ የግንባታ መከታተያና ተገዢነት ምዝግብ ---\n")
            writer.write("ፕሮጀክት,የተግባር ዝርዝር,የስራ መጠን,የተጣቀሰ EBCS,የተገመገመ ቼክፖይንት,የተለካ እሴት,የተገዢነት ሁኔታ,ማስታወሻ,ቀን\n")
            dailyProgress.value.forEach { dp ->
                val projName = projects.value.find { it.id == dp.projectId }?.name ?: "ያልታወቀ"
                val dateStr = sdf.format(java.util.Date(dp.date))
                writer.write("\"$projName\",\"${dp.taskDescription}\",\"${dp.workQuantity}\",\"${dp.standardRefId.uppercase(java.util.Locale.ROOT)}\",\"${dp.checkpointPassed}\",\"${dp.measuredValue}\",\"${dp.status}\",\"${dp.notes}\",\"$dateStr\"\n")
            }

            writer.flush()
            writer.close()
            fos.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun generateSummaryReportPdfText(context: Context): File? {
        val file = File(context.cacheDir, "የግንባታ_ክትትልና_የደህንነት_አደጋዎች_ማጠቃለያ_ሪፖርት.pdf")
        try {
            val pdfCreator = PdfCreator(file)
            
            pdfCreator.drawHeaderBanner(
                titleAm = "የግንባታ ክትትልና የደህንነት አደጋዎች ማጠቃለያ ሪፖርት",
                titleEn = "CONSTRUCTION MONITORING & SITE SAFETY SUMMARY REPORT",
                subtitle = "የቲሊሊ ከተማ ኮንስትራክሽን ጽሕፈት ቤት | ባለሙያ፡ በላይነህ የሻለም (Civil Engineer) | ቀን፡ ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}"
            )

            // 1. Projects Section
            pdfCreator.drawSectionTitle("1. በስራ ላይ ያሉ ግንባታዎች (PROJECTS REGISTER)")
            projects.value.forEachIndexed { i, p ->
                pdfCreator.drawBulletPoint("${i + 1}. ${p.name}")
                pdfCreator.drawLabelAndValue("  • ቦታ / Location: ", p.location)
                pdfCreator.drawLabelAndValue("  • ምድብ / Category: ", p.buildingCategory)
                pdfCreator.drawLabelAndValue("  • አይነት / Type: ", p.constructionType)
                pdfCreator.drawLabelAndValue("  • ቁሳቁሶች / Materials: ", p.materials)
                pdfCreator.drawLabelAndValue("  • የግንባታ ሁኔታ / Status: ", p.status)
                pdfCreator.drawSeparator()
            }

            // 2. Risk Assessments Section
            pdfCreator.drawSectionTitle("2. የተመዘገቡ የደህንነት አደጋዎችና ተዛማጅ EBCS ስታንዳርዶች (SITE HAZARDS & MAPPED EBCS)")
            if (riskAssessments.value.isEmpty()) {
                pdfCreator.drawText("   ምንም የተመዘገበ የደህንነት አደጋ የለም / No site hazards registered.")
            } else {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                riskAssessments.value.forEachIndexed { i, ra ->
                    val projName = projects.value.find { it.id == ra.projectId }?.name ?: "ያልታወቀ"
                    val dateStr = sdf.format(java.util.Date(ra.dateLogged))
                    pdfCreator.drawBulletPoint("${i + 1}. [${ra.riskLevel}] ${ra.hazardTitle}")
                    pdfCreator.drawLabelAndValue("  • ግንባታ / Project: ", projName)
                    pdfCreator.drawLabelAndValue("  • ዝርዝር መግለጫ / Description: ", ra.description)
                    pdfCreator.drawLabelAndValue("  • የአደጋ መቀነሻ ዕቅድ / Mitigation: ", ra.mitigationPlan)
                    pdfCreator.drawLabelAndValue("  • EBCS ኮድ / Standard Clause: ", "${ra.ebcsStandardRefId.uppercase(java.util.Locale.ROOT)} -> ${ra.safetyClause}")
                    pdfCreator.drawLabelAndValue("  • የክትትል ሁኔታ / Status: ", ra.status)
                    pdfCreator.drawLabelAndValue("  • ቀን / Date Logged: ", dateStr)
                    pdfCreator.drawLabelAndValue("  • የመዘገበው ባለሙያ / Logger: ", ra.loggedBy)
                    pdfCreator.drawSeparator()
                }
            }

            // 3. Daily Progress Section
            pdfCreator.drawSectionTitle("3. ዕለታዊ የግንባታ መከታተያና ተገዢነት ምዝግብ (CONSTRUCTION PROGRESS & COMPLIANCE LOGS)")
            if (dailyProgress.value.isEmpty()) {
                pdfCreator.drawText("   ምንም ዕለታዊ የግንባታ ምዝግብ አልተገኘም / No compliance progress logged.")
            } else {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                dailyProgress.value.forEachIndexed { i, dp ->
                    val projName = projects.value.find { it.id == dp.projectId }?.name ?: "ያልታወቀ"
                    val dateStr = sdf.format(java.util.Date(dp.date))
                    pdfCreator.drawBulletPoint("${i + 1}. ተግባር / Task: ${dp.taskDescription} (${dp.workQuantity})")
                    pdfCreator.drawLabelAndValue("  • ግንባታ / Project: ", projName)
                    pdfCreator.drawLabelAndValue("  • የተጣቀሰ EBCS / Ref Standard: ", dp.standardRefId.uppercase(java.util.Locale.ROOT))
                    pdfCreator.drawLabelAndValue("  • የተገመገመ ቼክፖይንት / Checkpoint: ", dp.checkpointPassed)
                    pdfCreator.drawLabelAndValue("  • የተለካ እሴት / Measured Value: ", dp.measuredValue)
                    pdfCreator.drawLabelAndValue("  • ሁኔታ / Compliance Status: ", dp.status)
                    pdfCreator.drawLabelAndValue("  • ቀን / Date: ", dateStr)
                    if (dp.notes.isNotBlank()) {
                        pdfCreator.drawLabelAndValue("  • ማስታወሻ / Notes: ", dp.notes)
                    }
                    pdfCreator.drawLabelAndValue("  • ተቆጣጣሪ / Inspector: ", dp.inspectorName)
                    pdfCreator.drawSeparator()
                }
            }

            pdfCreator.finish()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun promptShareSummaryReport(isExcel: Boolean) {
        _shareReportState.value = ShareReportState(
            prompt = ShareReportPrompt(
                id = "summary_ebcs_report",
                titleAm = "የግንባታ ክትትልና ደህንነት ማጠቃለያ ሪፖርት",
                titleEn = "EBCS Compliance & Safety Summary Report",
                descriptionAm = "በብሔራዊ የህንጻ ኮዶች (EBCS) መሠረት የተመዘገቡ ዕለታዊ ግንባታ መከታተያዎችን እና የተገመገሙ አደጋዎችን ያካተተ የተሟላ ማጠቃለያ ሪፖርት።",
                fileName = if (isExcel) "የግንባታ_ክትትልና_የደህንነት_አደጋዎች_ማጠቃለያ_ሪፖርት.csv" else "የግንባታ_ክትትልና_የደህንነት_አደጋዎች_ማጠቃለያ_ሪፖርት.pdf",
                format = if (isExcel) ReportFormat.EXCEL else ReportFormat.PDF,
                subject = if (isExcel) "የግንባታ ክትትልና ደህንነት ማጠቃለያ Excel ሪፖርት" else "የግንባታ ክትትልና ደህንነት ማጠቃለያ PDF ሪፖርት",
                shareBodyText = "የቲሊሊ ከተማ ኮንስትራክሽን ጽሕፈት ቤት ግንባታ ክትትልና የደህንነት አደጋዎች ማጠቃለያ ሪፖርት",
                generateAction = { ctx -> if (isExcel) generateSummaryReportExcel(ctx) else generateSummaryReportPdfText(ctx) }
            )
        )
    }

    fun shareSummaryReport(context: Context, isExcel: Boolean) {
        promptShareSummaryReport(isExcel)
    }

    // ==================== ETHIOPIAN BUILDING PROCLAMATION & INTERNATIONAL STANDARDS REGULATORY REPORTS ====================

    fun generateRegulatoryComplianceExcelReport(context: Context): File? {
        val file = File(context.cacheDir, "የኮንስትራክሽን_ባለሙያዎች_የሕግ_ተገዢነትና_የሰው_ኃይል_ኦዲት_ሪፖርት.csv")
        try {
            val fos = FileOutputStream(file)
            fos.write(0xEF)
            fos.write(0xBB)
            fos.write(0xBF) // UTF-8 BOM

            val writer = fos.bufferedWriter()
            val inst = _institutionProfile.value

            writer.write("${inst.name}\n")
            writer.write("${inst.department} | ${inst.regionOrCity}\n")
            writer.write("የተቋም ኮድ፡ ${inst.licenseOrCode} | ኃላፊ፡ ${inst.officerInCharge}\n")
            writer.write("ሕጋዊ መሠረት፡ በአዲሱ የኢትዮጵያ ሕንፃ አዋጅ ቁጥር 1356/2017 ዓ.ም (አንቀጽ 32 እና 33)፣ ደንብ 243/2003 እና በFIDIC/ISO ዓለም አቀፍ መመሪያዎች መሠረት\n")
            writer.write("ቀን፡ ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}\n\n")

            // Executive Summary Stats
            val totalProfs = professionals.value.size
            val compliantProfs = professionals.value.count { it.licenseStatus.contains("COMPLIANT", ignoreCase = true) || it.licenseStatus.contains("ተገዢ") }
            val expiredProfs = totalProfs - compliantProfs
            val totalArtisans = artisans.value.size

            writer.write("=== ማጠቃለያ የሕግና የሙያ ብቃት ኦዲት (EXECUTIVE REGULATORY AUDIT KPI) ===\n")
            writer.write("ጠቅላላ የተመዘገቡ ባለሙያዎች,${totalProfs}\n")
            writer.write("የሙያ ፈቃዳቸው የታደሰና ተገዢ (Compliant),${compliantProfs}\n")
            writer.write("ፈቃዳቸው ያበቃ / እርምጃ የሚሹ (Expired/Pending),${expiredProfs}\n")
            writer.write("የተመዘገቡ የ CoC የቴክኒክ ሰራተኞች (Artisans),${totalArtisans}\n")
            writer.write("የሕግ ተገዢነት ምጣኔ (Compliance Rate),${if (totalProfs > 0) (compliantProfs * 100 / totalProfs) else 100}%\n\n")

            // Section 1: Construction Professionals & Consultants & Contractors
            writer.write("=== ክፍል 1፡ የኮንስትራክሽን መሃንዲሶች፣ አማካሪዎችና ተቋራጮች ዝርዝር ማህደር (PROFESSIONALS REGISTER) ===\n")
            writer.write("ተ.ቁ,ስም,ስልክ,ኢሜይል,የዲጂታል መታወቂያ (Fayda ID),የሙያ ሚና,የብቃት ደረጃ,የፈቃድ ቁጥር,የትምህርት ደረጃ,ዩኒቨርሲቲ/ተቋም,የስራ ልምድ (ዓመት),የሙያ ፈቃድ ሁኔታ,የCPD ነጥብ,የሙያ ዋስትና መድን ፖሊሲ,ዓለም አቀፍ እውቅናዎች (FIDIC/ISO),የተመደበበት ፕሮጀክትና ኃላፊነት,የዲሲፕሊን መዝገብ\n")
            professionals.value.forEachIndexed { i, p ->
                writer.write("\"${i + 1}\",\"${p.name}\",\"${p.phone}\",\"${p.email}\",\"${p.nationalId}\",\"${p.role}\",\"${p.grade}\",\"${p.licenseNumber}\",\"${p.qualification}\",\"${p.institutionGraduated}\",\"${p.experienceYears}\",\"${p.licenseStatus}\",\"${p.cpdPoints}/30\",\"${p.insurancePolicyNo}\",\"${p.internationalCertifications}\",\"${p.assignedProjectRole}\",\"${p.disciplinaryRecord}\"\n")
            }

            // Section 2: Technical Artisans
            writer.write("\n=== ክፍል 2፡ የቴክኒክና የልምድ ሰራተኞች የብቃት ማረጋገጫ (CoC ARTISANS REGISTER) ===\n")
            writer.write("ተ.ቁ,ስም,ስልክ,የሙያ ዘርፍ,የCoC የብቃት ደረጃ,የCoC ምስክር ወረቀት ቁጥር,የሰጠው ተቋም,የስራ ልምድ (ዓመት),የደህንነት ስልጠና (Safety Induction),ፈቃድ አለው?,ሁኔታ\n")
            artisans.value.forEachIndexed { i, a ->
                writer.write("\"${i + 1}\",\"${a.name}\",\"${a.phone}\",\"${a.tradeSector}\",\"${a.cocLevel}\",\"${a.cocNumber}\",\"${a.issuingCenter}\",\"${a.experienceYears}\",\"${if (a.safetyTrainingCompleted) "የተጠናቀቀ (Completed)" else "ያልተጠናቀቀ"}\",\"${if (a.hasPermit) "አለው" else "የለውም"}\",\"${if (a.isAccredited) "እውቅና ያለው" else "እውቅና የሌለው"}\"\n")
            }

            // Section 3: Legal Verdict & Regulatory Findings
            writer.write("\n=== ክፍል 3፡ የቁጥጥር ግኝትና አስተዳደራዊ ውሳኔ (REGULATORY VERDICT) ===\n")
            writer.write("በአዲሱ የኢትዮጵያ ሕንፃ አዋጅ 1356/2017 ዓ.ም አንቀጽ 32 እና 33 መሠረት ማንኛውም ዲዛይን አዘጋጅ እና ተቆጣጣሪ ህጋዊ የሙያ ፈቃድ ሊኖረው ይገባል።\n")
            writer.write("በFIDIC ውል አንቀጽ 6.9 (Key Personnel) መሠረት ተቋራጮችና አማካሪዎች ብቁ ቁልፍ ሰራተኞችን ማሰማራት ግዴታቸው ነው።\n")
            writer.write("ያረጋገጠው ኃላፊ፡ ${inst.officerInCharge}\n")
            writer.write("የተቋም ማህተም፡ [ህጋዊ ማህተም እዚህ ጋር ያርፋል]\n")

            writer.flush()
            writer.close()
            fos.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun generateRegulatoryCompliancePdfReport(context: Context): File? {
        val file = File(context.cacheDir, "የኮንስትራክሽን_ባለሙያዎች_የሕግ_ተገዢነትና_የሰው_ኃይል_ኦዲት_ሪፖርት.pdf")
        try {
            val pdfCreator = PdfCreator(file)
            val inst = _institutionProfile.value

            pdfCreator.drawHeaderBanner(
                titleAm = inst.name,
                titleEn = "OFFICIAL REGULATORY PERSONNEL & STAFFING COMPLIANCE AUDIT",
                subtitle = "${inst.department} | ${inst.regionOrCity} | ኃላፊ፡ ${inst.officerInCharge}"
            )

            // Legal Baseline Citation
            pdfCreator.drawSectionTitle("1. ሕጋዊ መሠረትና ደንቦች (REGULATORY STATUTORY BASELINE)")
            pdfCreator.drawLabelAndValue("• የኢትዮጵያ ሕንፃ አዋጅ፡ ", "አዲሱ የሕንፃ አዋጅ ቁጥር 1356/2017 ዓ.ም (የቀድሞው 624/2001 ተሻሽሎ የወጣ)")
            pdfCreator.drawLabelAndValue("• የሚኒስትሮች ም/ቤት ደንብ፡ ", "የህንፃ ደንብ ቁጥር 243/2003 እና ማሻሻያዎች")
            pdfCreator.drawLabelAndValue("• የከተማና መሠረተ ልማት ሚኒስቴር፡ ", "የኮንስትራክሽን ኢንዱስትሪ ሙያተኞችና ተቋራጮች ምዝገባና የሙያ ብቃት መመሪያ")
            pdfCreator.drawLabelAndValue("• FIDIC ዓለም አቀፍ ውል፡ ", "FIDIC Conditions of Contract (Clause 6 - Staff and Labour, Key Personnel)")
            pdfCreator.drawLabelAndValue("• የደህንነትና ጥራት ስታንዳርድ፡ ", "ISO 45001:2018 (Occupational HSE), ISO 9001:2015, OSHA Standards")
            pdfCreator.drawSeparator()

            // Executive Summary
            val totalProfs = professionals.value.size
            val compliantProfs = professionals.value.count { it.licenseStatus.contains("COMPLIANT", ignoreCase = true) || it.licenseStatus.contains("ተገዢ") }
            val expiredProfs = totalProfs - compliantProfs
            val totalArtisans = artisans.value.size

            pdfCreator.drawSectionTitle("2. ማጠቃለያ የሕግና የሰው ኃይል ኦዲት (EXECUTIVE AUDIT SUMMARY)")
            pdfCreator.drawLabelAndValue("• ጠቅላላ የተመዘገቡ ባለሙያዎች፡ ", "$totalProfs መሃንዲሶች፣ አማካሪዎችና ተቋራጮች")
            pdfCreator.drawLabelAndValue("• ሙሉ የሙያ ፈቃዳቸው የታደሰ (Compliant): ", "$compliantProfs ባለሙያዎች (${if (totalProfs > 0) (compliantProfs * 100 / totalProfs) else 100}%)")
            pdfCreator.drawLabelAndValue("• ፈቃዳቸው ያበቃ / እርምጃ የሚሹ (Expired/Pending): ", "$expiredProfs ባለሙያዎች")
            pdfCreator.drawLabelAndValue("• የተመዘገቡ የ CoC የቴክኒክ ሰራተኞች (Artisans): ", "$totalArtisans ሰራተኞች")
            pdfCreator.drawLabelAndValue("• የሕግ ተገዢነት ምጣኔ (Regulatory Status): ", if (expiredProfs == 0) "100% ሙሉ ተገዢ (FULLY COMPLIANT)" else "ከፊል ተገዢ - የፈቃድ እድሳት ያስፈልጋል")
            pdfCreator.drawSeparator()

            // Key Personnel Matrix
            pdfCreator.drawSectionTitle("3. የቁልፍ ባለሙያዎች ዝርዝር ማህደር (KEY PERSONNEL AUDIT MATRIX)")
            professionals.value.forEachIndexed { i, p ->
                pdfCreator.drawBulletPoint("${i + 1}. ${p.name} — ${p.role}")
                pdfCreator.drawLabelAndValue("  • የብቃት ደረጃና ፈቃድ ቁጥር: ", "${p.grade} | ${p.licenseNumber}")
                pdfCreator.drawLabelAndValue("  • ትምህርትና ተቋም: ", "${p.qualification} (${p.institutionGraduated})")
                pdfCreator.drawLabelAndValue("  • የልምድ ዘመንና የፈቃድ ሁኔታ: ", "${p.experienceYears} ዓመታት | ${p.licenseStatus}")
                pdfCreator.drawLabelAndValue("  • የCPD ነጥብና የሙያ መድን: ", "${p.cpdPoints}/30 Hours | ፖሊሲ፡ ${p.insurancePolicyNo}")
                pdfCreator.drawLabelAndValue("  • ዓለም አቀፍ እውቅና: ", p.internationalCertifications)
                pdfCreator.drawLabelAndValue("  • የተመደበበት ኃላፊነት: ", p.assignedProjectRole)
                pdfCreator.drawSeparator()
            }

            // Technical Artisans Matrix
            pdfCreator.drawSectionTitle("4. የቴክኒክና የልምድ ሰራተኞች ብቃት (CoC TECHNICAL ARTISANS)")
            artisans.value.forEachIndexed { i, a ->
                pdfCreator.drawBulletPoint("${i + 1}. ${a.name} — ${a.tradeSector}")
                pdfCreator.drawLabelAndValue("  • የCoC ደረጃና ቁጥር: ", "${a.cocLevel} | ${a.cocNumber}")
                pdfCreator.drawLabelAndValue("  • የሰጠው የምዘና ማዕከል: ", a.issuingCenter)
                pdfCreator.drawLabelAndValue("  • የደህንነት ስልጠና (HSE Induction): ", if (a.safetyTrainingCompleted) "ተጠናቋል (Certified)" else "አልተጠናቀቀም")
                pdfCreator.drawSeparator()
            }

            // Legal Conclusion and Endorsement
            pdfCreator.drawSectionTitle("5. የቁጥጥር ውሳኔና ይሁንታ (REGULATORY ENDORSEMENT & SIGN-OFF)")
            pdfCreator.drawLabelAndValue("• የኦዲት መደምደሚያ: ", "ይህ የሰው ኃይል ኦዲት ሰነድ በአዲሱ የኢትዮጵያ ሕንፃ አዋጅ 1356/2017 ዓ.ም እና በሚመለከታቸው ዓለም አቀፍ የኮንስትራክሽን ህግጋት መሠረት የተረጋገጠ ኦፊሴላዊ ሰነድ ነው።")
            pdfCreator.drawLabelAndValue("• ያረጋገጠው መሪ መሃንዲስ/ኃላፊ: ", inst.officerInCharge)
            pdfCreator.drawLabelAndValue("• የተቋሙ ስም: ", inst.name)
            pdfCreator.drawLabelAndValue("• የሪፖርት ቁጥር: ", "AUDIT-${System.currentTimeMillis() % 100000}/2026")

            pdfCreator.finish()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun promptShareRegulatoryComplianceReport(isExcel: Boolean) {
        _shareReportState.value = ShareReportState(
            prompt = ShareReportPrompt(
                id = "regulatory_compliance_report",
                titleAm = "የኮንስትራክሽን ባለሙያዎች የሕግ ተገዢነት ኦዲት ሪፖርት",
                titleEn = "Regulatory Compliance & Workforce Audit Report",
                descriptionAm = "${_institutionProfile.value.name} - በአዲሱ የኢትዮጵያ ሕንፃ አዋጅ 1356/2017 ዓ.ም እና በFIDIC/ISO ዓለም አቀፍ መመሪያዎች መሠረት የተዘጋጀ የባለሙያዎችና የሰው ኃይል ኦዲት ሪፖርት።",
                fileName = if (isExcel) "የኮንስትራክሽን_ባለሙያዎች_የሕግ_ተገዢነትና_የሰው_ኃይል_ኦዲት_ሪፖርት.csv" else "የኮንስትራክሽን_ባለሙያዎች_የሕግ_ተገዢነትና_የሰው_ኃይል_ኦዲት_ሪፖርት.pdf",
                format = if (isExcel) ReportFormat.EXCEL else ReportFormat.PDF,
                subject = if (isExcel) "የኮንስትራክሽን ባለሙያዎች የሕግ ተገዢነት Excel ሪፖርት" else "የኮንስትራክሽን ባለሙያዎች የሕግ ተገዢነት PDF ሪፖርት",
                shareBodyText = "${_institutionProfile.value.name} - በአዲሱ የኢትዮጵያ ሕንፃ አዋጅ 1356/2017 ዓ.ም እና በFIDIC/ISO ዓለም አቀፍ መመሪያዎች መሠረት የተዘጋጀ የባለሙያዎችና የሰው ኃይል ኦዲት ሪፖርት",
                generateAction = { ctx -> if (isExcel) generateRegulatoryComplianceExcelReport(ctx) else generateRegulatoryCompliancePdfReport(ctx) }
            )
        )
    }

    fun shareRegulatoryComplianceReport(context: Context, isExcel: Boolean) {
        promptShareRegulatoryComplianceReport(isExcel)
    }

    // ==================== COMPREHENSIVE SYSTEM OPERATIONAL MANUAL (PDF) ====================

    fun generateOperationalManualPdf(context: Context): File? {
        val file = File(context.cacheDir, "የኮንስትራክሽን_ዲጂታል_መድረክ_የተሟላ_የአሰራር_መመሪያ_SOP.pdf")
        try {
            val inst = _institutionProfile.value
            val pdfCreator = PdfCreator(file, inst.name)

            // 1. Cover & Header Banner
            pdfCreator.drawHeaderBanner(
                titleAm = "የኮንስትራክሽን ዲጂታል መድረክ የተሟላ የአሰራር መመሪያ (SOP)",
                titleEn = "COMPREHENSIVE OPERATIONAL MANUAL: DIGITAL REGULATORY WORKSPACE",
                subtitle = "${inst.name} | ${inst.department} | ህጋዊ ማዕቀፍ፡ አዋጅ ቁጥር 1356/2017 & EBCS/FIDIC"
            )

            // Document Identification Matrix
            pdfCreator.drawLabelAndValue("• የሰነድ መለያ ቁጥር (Document Ref): ", "SOP-ETH-AMHARA-2026-V2")
            pdfCreator.drawLabelAndValue("• የተዘጋጀበት ቀንና ሰዓት: ", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date()))
            pdfCreator.drawLabelAndValue("• የሚመለከተው ተቋም / ክልል: ", "${inst.name} (${inst.regionOrCity})")
            pdfCreator.drawLabelAndValue("• ኃላፊ መሃንዲስ / ኦፊሰር: ", inst.officerInCharge)
            pdfCreator.drawLabelAndValue("• የሕግ ማዕቀፍ መሰረት: ", "የኢትዮጵያ ሕንፃ አዋጅ ቁጥር 1356/2017 ዓ.ም፣ ደንብ 243/2003፣ EBCS 1-11፣ FIDIC 2017 & ISO 45001")
            pdfCreator.drawSeparator()

            // SECTION 1: ARCHITECTURE & MULTI-TENANT ISOLATION
            pdfCreator.drawSectionTitle("1. የመድረኩ አጠቃላይ መዋቅርና የተከለሉ የተቋም መረጃ ቋቶች (ARCHITECTURE & ISOLATION)")
            pdfCreator.drawText(
                "ይህ የኮንስትራክሽን ዲጂታል መድረክ በአማራ ክልል ኮንስትራክሽን ባለስልጣን፣ በዞን መምሪያዎች፣ በማዘጋጃ ቤቶች እንዲሁም በግል አማካሪዎችና ተቋራጮች መካከል የተቀናጀ፣ ግልጽና በህግ የተደገፈ የቁጥጥር ስራ ለመስራት የተዘጋጀ ዘመናዊ የዲጂታል ስርዓት ነው።",
                size = 9.5f
            )
            pdfCreator.drawSubSectionTitle("1.1 የተከለለ የስራ ክፍል (Tenant Partitioned Architecture):")
            pdfCreator.drawBulletPoint("ሙሉ የውሂብ ደህንነት (Zero Cross-Tenant Contamination)፡ እያንዳንዱ ክልላዊ መምሪያ (ባህር ዳር፣ ጎንደር፣ ደሴ፣ ደብረ ማርቆስ፣ ደብረ ብርሃን፣ ወልዲያ፣ ደብረ ታቦር፣ ወዘተ) የራሱ የተከለለ መረጃ ቋት (Isolated Workspace) አለው።")
            pdfCreator.drawBulletPoint("የተቋም መቀየሪያ (Workspace Switcher)፡ ተጠቃሚዎች በዋናው አሞሌ ላይ ያለውን የተቋም ባጅ በመጫን ወደሚሰሩበት ክልላዊ መምሪያ መግባት፣ ማስተካከል ወይም አዲስ መምሪያ በቅጽበት መክፈት ይችላሉ።")
            pdfCreator.drawBulletPoint("ከመስመር ውጭ (Offline-First) አሰራር፡ ያለ ኢንተርኔት በሳይት ላይ መረጃ መመዝገብ፣ መፈተሽና ማቆየት ይቻላል፤ ኢንተርኔት ሲገኝ መረጃው በራሱ ይመሳሰላል።")

            pdfCreator.drawSubSectionTitle("1.2 ቀጥታ ያለ APK በስልክና በኮምፒውተር መጫን (PWA Zero-Install):")
            pdfCreator.drawBulletPoint("በስማርት ስልክ (Chrome/Safari)፡ አድራሻውን ከፍተው በብሮውዘሩ ሜኑ 'Install app' ወይም 'Add to Home Screen' ሲሉ ያለ APK ቀጥታ በስክሪኑ ላይ ይቀመጣል።")
            pdfCreator.drawBulletPoint("በኮምፒውተር (Chrome/Edge)፡ በአድራሻው አሞሌ ቀኝ በኩል ያለውን 'Install app' ምልክት በመጫን እንደ ዴስክቶፕ ሶፍትዌር ተጭኖ ፈጣን ስራ ይሰጣል።")
            pdfCreator.drawSeparator()

            // SECTION 2: GUIDE FOR ENGINEERING PROFESSIONALS
            pdfCreator.drawSectionTitle("2. ለምህንድስናና ቴክኒክ ባለሙያዎች የተጠቃሚ መመሪያ (GUIDE FOR PROFESSIONALS)")
            pdfCreator.drawText(
                "የምህንድስና፣ አርክቴክቸር፣ ቅየሳና የዋጋ ግመታ (QS) ባለሙያዎች በአዲሱ የኢትዮጵያ ሕንፃ አዋጅ ቁጥር 1356/2017 ዓ.ም እና በሚኒስቴሩ መመሪያ መሠረት የሚከተሉትን ስራዎች ያከናውናሉ፡",
                size = 9.5f
            )
            pdfCreator.drawSubSectionTitle("2.1 የሙያ ፈቃድ ምዝገባና ማህደር (Licensing & Registration):")
            pdfCreator.drawBulletPoint("በ'ባለሙያዎች' ትር ስር 'አዲስ ባለሙያ መዝግብ' የሚለውን ይጫኑ፤ ሙሉ ስም፣ ስልክ፣ የተመረቁበት ዩኒቨርሲቲ እና የትምህርት ደረጃ ያስገቡ።")
            pdfCreator.drawBulletPoint("የፈቃድ ደረጃ መምረጥ፡ ምሩቅ መሃንዲስ (Graduate)، ተለማማጅ (Junior)، ባለሙያ (Practicing Engineer Grade 1-8)፣ ወይም አማካሪ (Consulting Engineer)።")
            
            pdfCreator.drawSubSectionTitle("2.2 የአዋጅ 1356/2017 ተገዢነትና CPD ነጥቦች (CPD & Statutory Compliance):")
            pdfCreator.drawBulletPoint("የCPD ክትትል፡ በአዲሱ አዋጅ መሰረት እያንዳንዱ ባለሙያ በየዓመቱ ቢያንስ 30 ሰዓት/ነጥብ የሙያ ማሻሻያ (CPD) ማጠናቀቅ ግዴታ አለበት። ሲስተሙ የCPD ነጥቦችን ይመዝግባል እንዲሁም ያሰላል።")
            pdfCreator.drawBulletPoint("የሙያ ተጠያቂነት መድን (Professional Indemnity Insurance)፡ የኢንሹራንስ ፖሊሲ ቁጥርና የሚያበቃበት ቀን ይመዝገቡ፤ ሲስተሙ የፈቃድ እድሳት ጊዜ ሲቃረብ አስቀድሞ የማንቂያ መልዕክት ያሳያል።")
            pdfCreator.drawBulletPoint("ዲጂታል ማህተም እና የQR ማረጋገጫ፡ በዲዛይኖችና በሳይት ማረጋገጫ ሰነዶች ላይ የሚያርፍ ህጋዊ ኤሌክትሮኒክ መለያና ዲጂታል ማህተም በሲስተሙ ይመነጫል።")
            
            pdfCreator.drawSubSectionTitle("2.3 የልምድ ሰራተኞች ማህደር (CoC Skilled Artisans):")
            pdfCreator.drawBulletPoint("የግንበኞች፣ አናጢዎች፣ ኤሌክትሪሻኖች፣ ብየዳዎችና የቧንቧ ባለሙያዎች የደረጃ ፩ እስከ ፭ የብቃት ማረጋገጫ (CoC)፣ የምዘና ማዕከልና የስራ ልምድ በ'ልምድ ባለሙያ' ክፍል ይመዘገባል።")
            pdfCreator.drawSeparator()

            // SECTION 3: GUIDE FOR CONTRACTORS
            pdfCreator.drawSectionTitle("3. ለኮንትራክተሮችና ግንባታ ተቋራጮች የተጠቃሚ መመሪያ (GUIDE FOR CONTRACTORS)")
            pdfCreator.drawText(
                "ተቋራጮች (ጠቅላላ ተቋራጭ GC፣ የህንፃ BC፣ የመንገድ RC ደረጃ 1 እስከ 10) በሳይት ላይ ያሉ ፕሮጀክቶችን፣ የሰው ኃይልን፣ የደህንነት መሳሪያዎችንና ዕለታዊ ግንባታን በሲስተሙ እንዲህ ይመራሉ፡",
                size = 9.5f
            )
            pdfCreator.drawSubSectionTitle("3.1 አዲስ ግንባታ ሳይት መመዝገብ (Site Project Intake):")
            pdfCreator.drawBulletPoint("በ'ፕሮጀክቶች' ትር ውስጥ 'አዲስ ፕሮጀክት መዝግብ' በማለት የግንባታውን ስም፣ ባለቤት፣ ቦታና የህንፃ ምድብ ያስመዝግቡ።")
            pdfCreator.drawBulletPoint("የህንፃ ምድብ መምረጥ፡ ምድብ 1 (እስከ G+2)፣ ምድብ 2 (እስከ G+4)፣ ምድብ 3 (ከ G+4 በላይ እና የህዝብ መገልገያዎች)።")
            pdfCreator.drawBulletPoint("የግንባታ መዋቅር አይነት (አርማታ/RC Frame፣ ብረት/Steel Structure፣ ወዘተ) እና ግብዓቶችን መመዝገብ።")

            pdfCreator.drawSubSectionTitle("3.2 የቁልፍ ባለሙያዎች ስምሪት በFIDIC 2017 Clause 6.9 መስፈርት:")
            pdfCreator.drawBulletPoint("ለእያንዳንዱ ሳይት ፕሮጀክት ማናጀር፣ ሳይት መሃንዲስ፣ ፎርማንና የደህንነት ኦፊሰር ይመድቡ።")
            pdfCreator.drawBulletPoint("ብቃት የሌለው ወይም ፈቃዱ ያበቃ ባለሙያ በሳይት ማሰማራት በአዋጅ 1356/2017 መሰረት ህጋዊ ተጠያቂነትና የስራ ማቆም ያስከትላል።")

            pdfCreator.drawSubSectionTitle("3.3 የሳይት ደህንነትና ጤንነት (HSE - OSHA & ISO 45001 Standards):")
            pdfCreator.drawBulletPoint("በ'ደህንነት' ትር ስር በየቀኑ የሳይት ፍተሻ ያካሂዱ፡ የራስ ቁር (Helmets)፣ የደህንነት ጫማ (Boots)፣ የከፍታ ቀበቶ (Safety Harness)፣ የስካፎልዲንግ ጥንካሬ እና የመጀመሪያ እርዳታ ሳጥን።")
            pdfCreator.drawBulletPoint("የሳይት ደህንነት ውጤት (Safety Score) ማስላትና አደጋዎችን አስቀድሞ መከላከል፤ ማንኛውም አደጋ ሲያጋጥም ወዲያውኑ በሲስተሙ ሪፖርት ያድርጉ።")

            pdfCreator.drawSubSectionTitle("3.4 ዕለታዊ የግንባታ ሪፖርትና የጥራት ቁጥጥር (Daily QC Logs):")
            pdfCreator.drawBulletPoint("የሲሚንቶ፣ አሸዋ፣ ጠጠርና ብረት የላብራቶሪ ምርመራ ውጤቶችን (Slump test, Concrete 28-day strength) በሲስተሙ ይመዝግቡ።")
            pdfCreator.drawSeparator()

            // SECTION 4: GUIDE FOR REGULATORY INSPECTORS
            pdfCreator.drawSectionTitle("4. ለተቆጣጣሪዎችና ለባለስልጣኑ ኢንስፔክተሮች የተጠቃሚ መመሪያ (GUIDE FOR INSPECTORS)")
            pdfCreator.drawText(
                "የአማራ ክልል ኮንስትራክሽን ባለስልጣን ኢንስፔክተሮችና የከተማ አስተዳደር ተቆጣጣሪዎች በህግ የተሰጣቸውን የቁጥጥር ኃላፊነት በሲስተሙ የሚከተሉትን ደረጃዎች ተከትለው ይፈጽማሉ፡",
                size = 9.5f
            )
            pdfCreator.drawSubSectionTitle("4.1 በደረጃ የተከፋፈለ የሳይት ኢንስፔክሽን (Staged Site Inspection):")
            pdfCreator.drawBulletPoint("ደረጃ 1 - የመሰረት ቁፋሮና የበረዶ ኮንክሪት ፍተሻ (Excavation & Blinding Inspection)")
            pdfCreator.drawBulletPoint("ደረጃ 2 - የመሰረት አርማታና የኮንክሪት ሙሌት ይሁንታ (Footing & Column Starter Clearance)")
            pdfCreator.drawBulletPoint("ደረጃ 3 - የወለልና የጣሪያ አርማታ ፍተሻ (Floor Slabs & Beams Reinforcement Check)")
            pdfCreator.drawBulletPoint("ደረጃ 4 - የሳኒተሪ፣ ኤሌክትሪክና የግንብ ስራ ፍተሻ (MEP & Blockwork Rough-in)")
            pdfCreator.drawBulletPoint("ደረጃ 5 - የማጠናቀቂያ፣ የተደራሽነትና የእሳት ደህንነት ፍተሻ (Occupancy Clearance)")

            pdfCreator.drawSubSectionTitle("4.2 በአዋጅ 1356/2017 መሰረት የኦዲትና የእርምጃ ስርዓት (Statutory Enforcement):")
            pdfCreator.drawBulletPoint("የኦዲት ውጤት ማመንጨት፡ በ'ኦዲት' ክፍል በመግባት የባለሙያዎችና የሳይት ተገዢነት ምጣኔ (Compliance Rate %) በቅጽበት ይገምግሙ።")
            pdfCreator.drawBulletPoint("ማስጠንቀቂያ መላክ፡ ጉድለት ላለባቸው ተቋራጮች ደረጃ 1 (ቃል)፣ ደረጃ 2 (የጽሁፍ ማስጠንቀቂያ)፣ ወይም ደረጃ 3 (የስራ ማቆም ትዕዛዝ / Stop-Work Order) በSMS እና በደብዳቤ ይላኩ።")
            pdfCreator.drawBulletPoint("የቅጣት ስሌትና ህጋዊ ክስ፡ በአዋጅ 1356/2017 አንቀጽ 40 እና 41 መሰረት ያለፈቃድ ለተጀመረ ወይም የጥራት ጉድለት ላለበት ግንባታ የገንዘብ ቅጣትና የፈቃድ እገዳ ውሳኔ ያሳልፉ።")

            pdfCreator.drawSubSectionTitle("4.3 ኦፊሴላዊ የሕግ ተገዢነት ሪፖርት (Official PDF/Excel Reports):")
            pdfCreator.drawBulletPoint("በአንድ ጠቅታ የተሟላ የኦዲት ሪፖርት በPDF ወይም Excel ያመንጩ፤ ሪፖርቱ የተቋሙን ማህተም፣ የኃላፊውን ስም እና ህጋዊ ድንጋጌዎችን በሙሉ አካቶ ይወጣል።")
            pdfCreator.drawSeparator()

            // SECTION 5: QUICK OPERATIONAL CHECKLIST
            pdfCreator.drawSectionTitle("5. ፈጣን የስራ ሂደት ቼክሊስት ለሁሉም አካላት (SOP QUICK REFERENCE CHECKLIST)")
            pdfCreator.drawCalloutBox(
                title = "ለባለሙያዎች (5 ወሳኝ እርምጃዎች / 5 Steps for Professionals):",
                points = listOf(
                    "1. የራስዎን ማህደር፣ የትምህርት ማስረጃና የሙያ ፈቃድ ቁጥር በሲስተሙ ያስመዝግቡ።",
                    "2. በየዓመቱ ቢያንስ 30 የCPD ነጥቦችን በስልጠናና በምርምር አሟልተው ይመዝግቡ።",
                    "3. የሙያ ተጠያቂነት ኢንሹራንስ (Professional Indemnity) ፖሊሲዎን ያድሱ።",
                    "4. በዲዛይኖችና ሪፖርቶች ላይ የእርስዎን ዲጂታል ማህተምና የQR ኮድ ያኑሩ።",
                    "5. በሳይት ለሚሰሩ የCoC ባለሙያዎች ተገቢውን የሙያ ክትትልና ድጋፍ ያድርጉ።"
                ),
                accentColor = android.graphics.Color.rgb(0, 150, 136)
            )

            pdfCreator.drawCalloutBox(
                title = "ለተቋራጮች (5 ወሳኝ እርምጃዎች / 5 Steps for Contractors):",
                points = listOf(
                    "1. የሳይቱን ፕሮጀክት ስም፣ ምድብ፣ ቦታና መዋቅራዊ ንድፍ መዝግበው ያጽድቁ።",
                    "2. በFIDIC Clause 6.9 መስፈርት መሰረት ህጋዊ የሙያ ፈቃድ ያላቸው መሃንዲሶችን ይመድቡ።",
                    "3. የደረጃ ፩ እስከ ፭ የCoC ሰርተፍኬት ያላቸውን የግንባታ ባለሙያዎች ብቻ ያሰማሩ።",
                    "4. ዕለታዊ የHSE ደህንነት ፍተሻ (Helmets, Boots, Harnesses) በማድረግ ነጥብ ያስመዝግቡ።",
                    "5. የኮንክሪት ስለምፕና የ28 ቀን ሲሊንደር ጥንካሬ የላብራቶሪ ሰነዶችን በወቅቱ ያስገቡ።"
                ),
                accentColor = android.graphics.Color.rgb(230, 120, 20)
            )

            pdfCreator.drawCalloutBox(
                title = "ለተቆጣጣሪዎች (5 ወሳኝ እርምጃዎች / 5 Steps for Inspectors):",
                points = listOf(
                    "1. በእያንዳንዱ የሳይት ምዕራፍ (መሰረት፣ አርማታ፣ ማጠናቀቂያ) የሳይት ኢንስፔክሽን ያካሂዱ።",
                    "2. የባለሙያዎችን የፈቃድ ሁኔታና የCPD ተገዢነት በሲስተሙ በቅጽበት ይፈትሹ።",
                    "3. ጉድለት ሲገኝ ደረጃውን የጠበቀ ማስጠንቀቂያና የስራ ማቆም ትዕዛዝ በSMS ይላኩ።",
                    "4. በአዋጅ 1356/2017 መሰረት የቅጣትና የአስተዳደራዊ እርምጃ ውሳኔዎችን ያሳልፉ።",
                    "5. ኦፊሴላዊ የሕግ ተገዢነት ሪፖርት በPDF አዘጋጅተው ለሚመለከተው የበላይ አካል ያቅርቡ።"
                ),
                accentColor = android.graphics.Color.rgb(180, 40, 40)
            )
            pdfCreator.drawSeparator()

            // SECTION 6: SYSTEM SPECIFICATIONS & HELPDESK
            pdfCreator.drawSectionTitle("6. ቴክኒካዊ መረጃና የድጋፍ መስመሮች (SYSTEM SPECIFICATIONS & HELPDESK)")
            pdfCreator.drawLabelAndValue("• የሲስተሙ ስም፡ ", "የኮንስትራክሽን ግንባታዎችና ባለሙያዎች ዲጂታል ቁጥጥር ስርዓት")
            pdfCreator.drawLabelAndValue("• የስሪት ቁጥር (Version): ", "2.6.0-ENTERPRISE (Multi-Tenant Offline PWA)")
            pdfCreator.drawLabelAndValue("• ዋና አስተዳዳሪ ተቋም፡ ", "${inst.name} - ${inst.department}")
            pdfCreator.drawLabelAndValue("• ስልክና እውቂያ፡ ", inst.contactPhone)
            pdfCreator.drawLabelAndValue("• የድጋፍ ማዕከል፡ ", "የአማራ ክልል ኮንስትራክሽን ባለስልጣን የዲጂታል ቴክኖሎጂና ቁጥጥር መምሪያ")
            pdfCreator.drawLabelAndValue("• ማረጋገጫ፡ ", "ይህ ሰነድ በአዲሱ የኢትዮጵያ ሕንፃ አዋጅ 1356/2017 ዓ.ም፣ በEBCS ብሔራዊ ህጎች እና በFIDIC/ISO ዓለም አቀፍ ስታንዳርዶች መሠረት የተዘጋጀ ይፋዊ የስራ መመሪያ ነው።")

            pdfCreator.finish()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun promptShareOperationalManual() {
        _shareReportState.value = ShareReportState(
            prompt = ShareReportPrompt(
                id = "operational_manual_sop",
                titleAm = "የኮንስትራክሽን ሲስተም አሰራር መመሪያ (SOP)",
                titleEn = "Operational Manual & Standard Operating Procedure (PDF)",
                descriptionAm = "${_institutionProfile.value.name} - የኮንስትራክሽን ባለሙያዎች፣ ተቋራጮችና ተቆጣጣሪዎች የተሟላ የሲስተም አሰራር መመሪያ (Operational Manual PDF)። በአዲሱ የኢትዮጵያ ሕንፃ አዋጅ 1356/2017 ዓ.ም እና በEBCS/FIDIC ስታንዳርዶች መሠረት የተዘጋጀ።",
                fileName = "የኮንስትራክሽን_ዲጂታል_መድረክ_የተሟላ_የአሰራር_መመሪያ_SOP.pdf",
                format = ReportFormat.PDF,
                subject = "የኮንስትራክሽን ዲጂታል መድረክ የተጠቃሚዎች መመሪያ (Operational Manual PDF)",
                shareBodyText = "${_institutionProfile.value.name} - የኮንስትራክሽን ባለሙያዎች፣ ተቋራጮችና ተቆጣጣሪዎች የተሟላ የሲስተም አሰራር መመሪያ (Operational Manual PDF)። በአዲሱ የኢትዮጵያ ሕንፃ አዋጅ 1356/2017 ዓ.ም እና በEBCS/FIDIC ስታንዳርዶች መሠረት የተዘጋጀ።",
                generateAction = { ctx -> generateOperationalManualPdf(ctx) }
            )
        )
    }

    fun openOrShareOperationalManual(context: Context) {
        promptShareOperationalManual()
    }

    // ==================== KOTLIN ZERO-TO-APK MASTER GUIDE (PDF) ====================

    fun generateKotlinToApkMasterManualPdf(context: Context): File? {
        val file = File(context.cacheDir, "የኮትሊን_አንድሮይድ_ግንባታ_ከዜሮ_እስከ_APK_የተሟላ_መመሪያ.pdf")
        try {
            val inst = _institutionProfile.value
            val pdfCreator = PdfCreator(file, "የኮንስትራክሽን ሶፍትዌር ምህንድስና መምሪያ")

            // 1. Header & Cover
            pdfCreator.drawHeaderBanner(
                titleAm = "የኮትሊን አንድሮይድ ሶፍትዌር ግንባታ ከዜሮ እስከ APK ማመንጨት",
                titleEn = "KOTLIN ANDROID FULL LIFECYCLE: FROM ZERO TO SIGNED APK",
                subtitle = "የተሟላ ተግባራዊ የማስተማሪያ መመሪያ (Complete Developer Manual & Build Guide) | ነጻ ማውረጃ"
            )

            // Metadata Box
            pdfCreator.drawLabelAndValue("• የሰነድ ርዕስ (Document Title): ", "Kotlin Android Zero-to-APK Production Manual")
            pdfCreator.drawLabelAndValue("• የተዘጋጀበት ቀንና ሰዓት: ", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date()))
            pdfCreator.drawLabelAndValue("• ዋና ባለቤትና መሃንዲስ: ", "በላይነህ የሻለም (Civil Engineer & Tech Developer)")
            pdfCreator.drawLabelAndValue("• የተጠቃሚው ኢሜይል: ", "belaynehyesha@gmail.com")
            pdfCreator.drawLabelAndValue("• የቴክኖሎጂ ስብስብ (Tech Stack): ", "Kotlin 2.0+, Jetpack Compose, Material 3, Coroutines, Room DB, Gradle DSL & Android SDK 35")
            pdfCreator.drawSeparator()

            // SECTION 1: ENVIRONMENT & PREREQUISITES
            pdfCreator.drawSectionTitle("ምዕራፍ 1፡ መግቢያና አስፈላጊ የስራ መሳሪያዎች ዝግጅት (ENVIRONMENT SETUP)")
            pdfCreator.drawText(
                "አንድሮይድ አፕሊኬሽንን በዘመናዊው ኮትሊን (Kotlin) ቋንቋ ከዜሮ ለመጀመር የሚከተሉትን መሰረታዊ የሶፍትዌር መሳሪያዎች በኮምፒውተርዎ ላይ ማዘጋጀት ያስፈልጋል፡",
                size = 9.5f
            )
            pdfCreator.drawBulletPoint("1. Java Development Kit (JDK 17 ወይም JDK 21)፡ ዘመናዊ አንድሮይድ Gradle 8+ እና Kotlin 2.0 የሚደግፈው JDK 17/21 ነው።")
            pdfCreator.drawBulletPoint("2. Android Studio (Ladybug / Koala / Hedgehog)፡ ይፋዊው የአንድሮይድ ልማት መድረክ (IDE) ሲሆን የኮድ አርታዒ፣ Emulator እና Build tools በአንድ ላይ አሉት።")
            pdfCreator.drawBulletPoint("3. Android SDK (Software Development Kit)፡ SDK Platform 35 (Android 15) እና Android SDK Build-Tools ማውረድ።")
            pdfCreator.drawBulletPoint("4. Git Version Control፡ የኮድዎን ለውጥ ለመከታተልና ወደ GitHub/GitLab ለመስቀል።")
            pdfCreator.drawSeparator()

            // SECTION 2: PROJECT ARCHITECTURE
            pdfCreator.drawSectionTitle("ምዕራፍ 2፡ የአንድሮይድ ፕሮጀክት መዋቅርና ፋይሎች (PROJECT STRUCTURE)")
            pdfCreator.drawText(
                "በአንድሮይድ ስቱዲዮ 'New Project -> Empty Activity (Compose)' ሲመርጡ የሚፈጠሩት ወሳኝ ፋይሎች የሚከተሉት ናቸው፡",
                size = 9.5f
            )
            pdfCreator.drawSubSectionTitle("2.1 የፋይል ማውጫ አወቃቀር (File Directory Structure):")
            pdfCreator.drawBulletPoint("• settings.gradle.kts ፡ የፕሮጀክቱን ስም እና ላይብረሪዎች የሚገኙባቸውን ማዕከላት (google(), mavenCentral()) ይይዛል።")
            pdfCreator.drawBulletPoint("• gradle/libs.versions.toml ፡ የላይብረሪዎችን ስሪት (Dependency Version Catalog) በአንድ ቦታ የሚያስተዳድር ዘመናዊ ፋይል ነው።")
            pdfCreator.drawBulletPoint("• app/build.gradle.kts ፡ የአፑን መለያ (ApplicationId)، የSDK ቁጥሮች (compileSdk, minSdk, targetSdk) እና የሚገነባበትን ውቅር ይወስናል።")
            pdfCreator.drawBulletPoint("• app/src/main/AndroidManifest.xml ፡ የአፑ ፓስፖርት ሲሆን ፈቃዶችን (Permissions)፣ Activities እና አገልግሎቶችን ያሳውቃል።")
            pdfCreator.drawBulletPoint("• app/src/main/java/com/example/ ፡ የKotlin ኮዶች (MainActivity, ViewModels, UI, Data) የሚቀመጡበት ነው።")
            pdfCreator.drawBulletPoint("• app/src/main/res/ ፡ ምስሎች (drawable)፣ ጽሁፎች (values/strings.xml) እና አዶዎች (mipmap) የሚቀመጡበት ነው።")
            pdfCreator.drawSeparator()

            // SECTION 3: KOTLIN CORE FOR ANDROID
            pdfCreator.drawSectionTitle("ምዕራፍ 3፡ የኮትሊን ወሳኝ መሰረታዊያን (KOTLIN ESSENTIALS FOR ANDROID)")
            pdfCreator.drawText(
                "በአንድሮይድ ላይ ጥራት ያለው ኮድ ለመጻፍ የሚከተሉትን የኮትሊን ፅንሰ ሀሳቦች መጠቀም ወሳኝ ነው፡",
                size = 9.5f
            )
            pdfCreator.drawSubSectionTitle("3.1 ዳታ ክላስና የUI ሁኔታ መግለጫ (Data Classes & Sealed Interface):")
            pdfCreator.drawCodeBlock(
                "// ዳታ ክላስ (Immutable Data Model)\n" +
                "data class Project(val id: String, val name: String, val progress: Float)\n\n" +
                "// የUI ገጽ ሁኔታዎችን በግልጽ ማስተዳደሪያ\n" +
                "sealed interface UiState {\n" +
                "    object Loading : UiState\n" +
                "    data class Success(val projects: List<Project>) : UiState\n" +
                "    data class Error(val message: String) : UiState\n" +
                "}"
            )

            pdfCreator.drawSubSectionTitle("3.2 ኮሩቲኖችና StateFlow (Coroutines & Reactive State):")
            pdfCreator.drawBulletPoint("• Coroutines (viewModelScope.launch)፡ ከበስተጀርባ (Background IO) የሚሰሩ ስራዎችን ያለምንም ስክሪን መቆራረጥ (No ANR) ያከናውናል።")
            pdfCreator.drawBulletPoint("• StateFlow / MutableStateFlow፡ ዳታ ሲቀየር ስክሪኑ በራሱ በቅጽበት እንዲቀየር የሚያደርግ አስተማማኝ የመልዕክት ቱቦ ነው።")
            pdfCreator.drawSeparator()

            // SECTION 4: JETPACK COMPOSE UI
            pdfCreator.drawSectionTitle("ምዕራፍ 4፡ በJetpack Compose ዘመናዊ ገጽ መገንባት (JETPACK COMPOSE UI)")
            pdfCreator.drawText(
                "Jetpack Compose በXML ምትክ በKotlin ኮድ ብቻ እጅግ ዘመናዊ፣ ፈጣንና ማራኪ UI ለመገንባት የሚያስችል አዲሱ የአንድሮይድ ስታንዳርድ ነው።",
                size = 9.5f
            )
            pdfCreator.drawCodeBlock(
                "@Composable\n" +
                "fun ConstructionDashboard(viewModel: ConstructionViewModel) {\n" +
                "    val state by viewModel.uiState.collectAsStateWithLifecycle()\n" +
                "    Scaffold(\n" +
                "        topBar = { TopAppBar(title = { Text(\"የኮንስትራክሽን መድረክ\") }) }\n" +
                "    ) { padding ->\n" +
                "        LazyColumn(modifier = Modifier.padding(padding)) {\n" +
                "            items(state.projects) { project ->\n" +
                "                ProjectCard(project = project)\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}"
            )
            pdfCreator.drawBulletPoint("• Scaffold፡ ርዕስ (TopAppBar)፣ የታች አሞሌ (BottomBar) እና የድርጊት ቁልፍ (FAB) ያቀርባል።")
            pdfCreator.drawBulletPoint("• LazyColumn፡ እጅግ ብዙ ዝርዝሮችን ያለስልኩ ማህደረ-ትውስታ መጫን (Recycling) የሚያሳይ ፈጣን ዝርዝር ነው።")
            pdfCreator.drawBulletPoint("• Material 3 (M3) Theming፡ በTheme.kt ውስጥ የቀለማትና የፊደላት ስታንዳርድን በአንድ ወጥ መንገድ ያስተዳድራል።")
            pdfCreator.drawSeparator()

            // SECTION 5: DATA PERSISTENCE - ROOM DB
            pdfCreator.drawSectionTitle("ምዕራፍ 5፡ ዳታ ማከማቸትና የመረጃ ቋት (ROOM LOCAL DATABASE & MVVM)")
            pdfCreator.drawText(
                "ያለ ኢንተርኔት (Offline-First) መረጃዎችን በስልኩ SQLite ውስጥ በሰላም ለማስቀመጥ የRoom ላይብረሪን እንጠቀማለን፡",
                size = 9.5f
            )
            pdfCreator.drawCodeBlock(
                "@Entity(tableName = \"projects_table\")\n" +
                "data class ProjectEntity(@PrimaryKey val id: String, val name: String)\n\n" +
                "@Dao\n" +
                "interface ProjectDao {\n" +
                "    @Query(\"SELECT * FROM projects_table\")\n" +
                "    fun getAllProjects(): Flow<List<ProjectEntity>>\n\n" +
                "    @Insert(onConflict = OnConflictStrategy.REPLACE)\n" +
                "    suspend fun insertProject(project: ProjectEntity)\n" +
                "}\n\n" +
                "@Database(entities = [ProjectEntity::class], version = 1)\n" +
                "abstract class AppDatabase : RoomDatabase() {\n" +
                "    abstract fun projectDao(): ProjectDao\n" +
                "}"
            )
            pdfCreator.drawSeparator()

            // SECTION 6: GENERATING APK - STEP BY STEP
            pdfCreator.drawSectionTitle("ምዕራፍ 6፡ ከኮድ ወደ APK ማመንጨት ደረጃ በደረጃ (GENERATING THE APK)")
            pdfCreator.drawText(
                "APK (Android Package Kit) ማለት አንድሮይድ ስልክ ላይ የሚጫነው የተጠናቀረ የሶፍትዌር ጥቅል ፋይል ነው። ሁለት አይነት APK ማመንጨት ይቻላል፡",
                size = 9.5f
            )
            pdfCreator.drawSubSectionTitle("6.1 አማራጭ ፩፡ Debug APK ማመንጨት (ለሙከራና ለሳይት ላይ ፍተሻ):")
            pdfCreator.drawBulletPoint("ይህ APK ያለ ዲጂታል ፊርማ ለፈጣን ሙከራ የሚዘጋጅ ነው።")
            pdfCreator.drawBulletPoint("የትዕዛዝ መስመር (Terminal command):")
            pdfCreator.drawCodeBlock("gradle assembleDebug")
            pdfCreator.drawBulletPoint("የፋይል መገኛ (Output Location)፡")
            pdfCreator.drawText("app/build/outputs/apk/debug/app-debug.apk", size = 8.5f, isBold = true, color = android.graphics.Color.rgb(0, 100, 150))
            pdfCreator.drawBulletPoint("በAndroid Studio ውስጥ፡ Menu -> Build -> Build Bundle(s) / APK(s) -> Build APK(s) የሚለውን ጠቅ ያድርጉ።")

            pdfCreator.drawSubSectionTitle("6.2 አማራጭ ፪፡ ህጋዊ የተፈረመበት Release APK ማመንጨት (Production & Play Store):")
            pdfCreator.drawText("ለተጠቃሚዎች ወይም ለPlay Store የሚሰጥ APK ደህንነቱ በተጠበቀ Keystore መፈረም አለበት። ደረጃዎቹ፡")
            pdfCreator.drawBulletPoint("ደረጃ 1 - የምስጠራ ቁልፍ (Keystore) በTerminal መፍጠር፡")
            pdfCreator.drawCodeBlock(
                "keytool -genkey -v -keystore my-release-key.jks -keyalg RSA \\\n" +
                "  -keysize 2048 -validity 10000 -alias my-key-alias"
            )
            pdfCreator.drawBulletPoint("ደረጃ 2 - በ app/build.gradle.kts ውስጥ ፊርማውን ማዋቀር፡")
            pdfCreator.drawCodeBlock(
                "android {\n" +
                "    signingConfigs {\n" +
                "        create(\"release\") {\n" +
                "            storeFile = file(\"my-release-key.jks\")\n" +
                "            storePassword = \"YourPassword\"\n" +
                "            keyAlias = \"my-key-alias\"\n" +
                "            keyPassword = \"YourPassword\"\n" +
                "        }\n" +
                "    }\n" +
                "    buildTypes {\n" +
                "        release {\n" +
                "            isMinifyEnabled = true\n" +
                "            isShrinkResources = true\n" +
                "            proguardFiles(getDefaultProguardFile(\"proguard-android-optimize.txt\"), \"proguard-rules.pro\")\n" +
                "            signingConfig = signingConfigs.getByName(\"release\")\n" +
                "        }\n" +
                "    }\n" +
                "}"
            )
            pdfCreator.drawBulletPoint("ደረጃ 3 - Release APK ማመንጨት፡")
            pdfCreator.drawCodeBlock("gradle assembleRelease")
            pdfCreator.drawBulletPoint("የፋይሉ መገኛ (Output Location)፡")
            pdfCreator.drawText("app/build/outputs/apk/release/app-release.apk", size = 8.5f, isBold = true, color = android.graphics.Color.rgb(0, 120, 70))
            pdfCreator.drawSeparator()

            // SECTION 7: INSTALLING APK ON DEVICE
            pdfCreator.drawSectionTitle("ምዕራፍ 7፡ የተመረተውን APK በስልክ ላይ መጫን (INSTALLING APK ON DEVICE)")
            pdfCreator.drawBulletPoint("1. በUSB ገመድ (ADB Command)፡")
            pdfCreator.drawCodeBlock("adb install -r app/build/outputs/apk/debug/app-debug.apk")
            pdfCreator.drawBulletPoint("2. ያለ ኮምፒውተር በቀጥታ በስልክ ላይ፡ የተዘጋጀውን app-debug.apk ወይም app-release.apk በTelegram፣ WhatsApp ወይም Google Drive ወደ ስልክዎ ይላኩ፤ ፋይሉን ሲነኩት 'Install Unknown Apps' ፈቃድ ጠይቆ ወዲያውኑ በስልክዎ ይጫናል።")
            pdfCreator.drawSeparator()

            // SECTION 8: PWA ZERO-INSTALL ALTERNATIVE
            pdfCreator.drawSectionTitle("ምዕራፍ 8፡ ዘመናዊው ያለ APK የመጫን አማራጭ (PWA ZERO-INSTALL METHOD)")
            pdfCreator.drawText(
                "ይህ የኮንስትራክሽን ሲስተም ያለ ምንም APK ፋይል ማውረድና መጫን ሳያስፈልግ በቀጥታ በማንኛውም ስልክና ኮምፒውተር ላይ ልክ እንደ መደበኛ አፕሊኬሽን የመጫን (PWA) አቅም አለው፡",
                size = 9.5f
            )
            pdfCreator.drawBulletPoint("• በሞባይል Chrome/Safari፡ የሲስተሙን ሊንክ ከፍተው በብሮውዘሩ ሜኑ 'Install app' ወይም 'Add to Home Screen' ሲሉ ወዲያውኑ አዶው በስልኩ ላይ ይቀመጣል።")
            pdfCreator.drawBulletPoint("• በኮምፒውተር Chrome/Edge፡ በአድራሻ አሞሌው ቀኝ ያለውን የ'Install' ምልክት በመጫን እንደ ዴስክቶፕ ሶፍትዌር ይጫናል።")
            pdfCreator.drawBulletPoint("• ሙሉ Offline አሰራር፡ ያለ ኢንተርኔት በሳይት ላይ መስራትና ኢንተርኔት ሲገኝ መረጃዎችን በቅጽበት ማመሳሰል ያስችላል።")
            pdfCreator.drawSeparator()

            // SECTION 9: QUICK 5-STEP SUMMARY
            pdfCreator.drawSectionTitle("ምዕራፍ 9፡ ፈጣን የ5-ደረጃ ማጠቃለያ (5-STEP QUICK ROADMAP)")
            pdfCreator.drawCalloutBox(
                title = "ከዜሮ እስከ APK ማጠቃለያ ቼክሊስት (Zero-to-APK Checklist):",
                points = listOf(
                    "ደረጃ 1፡ JDK 17 እና Android Studio ጫን፤ New Project (Compose) ክፈት።",
                    "ደረጃ 2፡ Data Models (Kotlin) እና Room Database አዋቅር።",
                    "ደረጃ 3፡ በJetpack Compose እና Material 3 የተጠቃሚ ገጾችን (UI) ገንባ።",
                    "ደረጃ 4፡ 'gradle assembleDebug' በማለት Debug APK አመንጭተህ በስልክህ ሞክር።",
                    "ደረጃ 5፡ Keystore በማዘጋጀት 'gradle assembleRelease' በማለት ይፋዊ Signed APK አውጣ።"
                ),
                accentColor = android.graphics.Color.rgb(0, 120, 110)
            )
            pdfCreator.drawSeparator()

            // SECTION 10: SIGN-OFF & HELP
            pdfCreator.drawSectionTitle("ምዕራፍ 10፡ ቴክኒካዊ እገዛና የመረጃ ምንጮች (RESOURCES & ASSISTANCE)")
            pdfCreator.drawLabelAndValue("• ይፋዊ የኮትሊን ገጽ፡ ", "https://kotlinlang.org/docs/android-overview.html")
            pdfCreator.drawLabelAndValue("• የአንድሮይድ መመሪያ፡ ", "https://developer.android.com")
            pdfCreator.drawLabelAndValue("• ያዘጋጀው መሃንዲስ፡ ", "በላይነህ የሻለም (Civil Engineer)")
            pdfCreator.drawLabelAndValue("• ኢሜይል እውቂያ፡ ", "belaynehyesha@gmail.com")
            pdfCreator.drawLabelAndValue("• ማረጋገጫ፡ ", "ይህ መመሪያ በነጻ እንዲወርድና ለማንኛውም ኢትዮጵያዊ መሃንዲስና የሶፍትዌር አልሚ ጠቃሚ እውቀት እንዲሆን የተዘጋጀ ነው።")

            pdfCreator.finish()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun promptShareKotlinToApkPdf() {
        _shareReportState.value = ShareReportState(
            prompt = ShareReportPrompt(
                id = "kotlin_zero_to_apk_guide",
                titleAm = "የኮትሊን አንድሮይድ ግንባታ ከዜሮ እስከ APK መመሪያ",
                titleEn = "Kotlin Android: Zero to APK Master Guide (PDF)",
                descriptionAm = "የኮትሊን አንድሮይድ ሶፍትዌር ግንባታ ከዜሮ እስከ APK ማመንጨት የተሟላ ተግባራዊ መመሪያ (PDF)። በነጻ ያውርዱ ወይም በቴሌግራም/ዋትስአፕ ያጋሩ!",
                fileName = "የኮትሊን_አንድሮይድ_ግንባታ_ከዜሮ_እስከ_APK_የተሟላ_መመሪያ.pdf",
                format = ReportFormat.PDF,
                subject = "የኮትሊን አንድሮይድ ግንባታ ከዜሮ እስከ APK የተሟላ መመሪያ (Zero to APK Guide PDF)",
                shareBodyText = "የኮትሊን አንድሮይድ ሶፍትዌር ግንባታ ከዜሮ እስከ APK ማመንጨት የተሟላ ተግባራዊ መመሪያ (PDF)። በነጻ ያውርዱ ወይም ያጋሩ! ያዘጋጀው፡ በላይነህ የሻለም (Civil Engineer)።",
                generateAction = { ctx -> generateKotlinToApkMasterManualPdf(ctx) }
            )
        )
    }

    fun openOrShareKotlinToApkPdf(context: Context) {
        promptShareKotlinToApkPdf()
    }

    private val _siteEngineerEmail = MutableStateFlow(sharedPrefs.getString("site_engineer_email", "site.engineer@tilli.gov.et") ?: "site.engineer@tilli.gov.et")
    val siteEngineerEmail = _siteEngineerEmail.asStateFlow()

    private val _supervisorEmail = MutableStateFlow(sharedPrefs.getString("supervisor_email", "supervisor@tilli.gov.et") ?: "supervisor@tilli.gov.et")
    val supervisorEmail = _supervisorEmail.asStateFlow()

    private val _deadlineThresholdDays = MutableStateFlow(sharedPrefs.getInt("deadline_threshold_days", 30))
    val deadlineThresholdDays = _deadlineThresholdDays.asStateFlow()

    private val _sentAlertIds = MutableStateFlow(
        sharedPrefs.getStringSet("sent_alert_ids", emptySet()) ?: emptySet()
    )
    val sentAlertIds = _sentAlertIds.asStateFlow()

    fun updateNotificationSettings(engineer: String, supervisor: String, threshold: Int) {
        _siteEngineerEmail.value = engineer
        _supervisorEmail.value = supervisor
        _deadlineThresholdDays.value = threshold
        sharedPrefs.edit().apply {
            putString("site_engineer_email", engineer)
            putString("supervisor_email", supervisor)
            putInt("deadline_threshold_days", threshold)
            apply()
        }
    }

    fun markAlertAsSent(alertId: String) {
        val updated = _sentAlertIds.value.toMutableSet().apply { add(alertId) }
        _sentAlertIds.value = updated
        sharedPrefs.edit().putStringSet("sent_alert_ids", updated).apply()
    }

    fun clearSentAlertHistory() {
        _sentAlertIds.value = emptySet()
        sharedPrefs.edit().remove("sent_alert_ids").apply()
    }

    private fun toGregorianMillis(year: Int, month: Int, day: Int): Long {
        return try {
            val greg = EthiopianCalendar.ethiopianToGregorianDate(year, month, day)
            val cal = java.util.Calendar.getInstance()
            cal.set(greg.first, greg.second - 1, greg.third, 0, 0, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            cal.timeInMillis
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    val automatedAlerts: StateFlow<List<AutomatedAlert>> = combine(
        projects,
        riskAssessments,
        siteEngineerEmail,
        supervisorEmail,
        deadlineThresholdDays,
        sentAlertIds
    ) { array ->
        val projs = array[0] as List<Project>
        val risks = array[1] as List<RiskAssessment>
        val engEmail = array[2] as String
        val supEmail = array[3] as String
        val threshold = array[4] as Int
        val sentIds = array[5] as Set<String>

        val alerts = mutableListOf<AutomatedAlert>()

        // 1. Check for High-priority and Critical Risk Assessments
        risks.forEach { risk ->
            if (risk.riskLevel == "HIGH" || risk.riskLevel == "CRITICAL") {
                val projName = projs.find { it.id == risk.projectId }?.name ?: "ያልታወቀ ግንባታ"
                val subject = "⚠️ የአስቸኳይ ጊዜ ደህንነት ማስጠንቀቂያ፡ [${risk.riskLevel}] ${risk.hazardTitle}"
                val body = """
                    ሰላም የሳይት መሃንዲስ እና የክትትል ባለሙያ፣
                    
                    በግንባታ ክትትል ቁጥጥር መድረክ ላይ ከፍተኛ የአደጋ ስጋት ተመዝግቧል፡
                    
                    • ግንባታ/ፕሮጀክት፡ $projName
                    • የአደጋው ርዕስ፡ ${risk.hazardTitle}
                    • የአደጋ ደረጃ፡ ${risk.riskLevel}
                    • መግለጫ፡ ${risk.description}
                    • የተጣቀሰ EBCS ሕንፃ ኮድ፡ ${risk.ebcsStandardRefId.uppercase()} (አንቀጽ ${risk.safetyClause})
                    • የተቀመጠ መፍትሄ/ዕቅድ፡ ${risk.mitigationPlan}
                    • መዝጋቢ ባለሙያ፡ በላይነህ የሻለም (Civil Engineer)
                    
                    እባክዎን በአስቸኳይ ተገቢውን የእርምት እርምጃ ይውሰዱ።
                    
                    ከሰላምታ ጋር፣
                    የቲሊሊ ከተማ ኮንስትራክሽን ጽሕፈት ቤት
                """.trimIndent()
                
                val alertId = "risk_${risk.id}"
                alerts.add(
                    AutomatedAlert(
                        id = alertId,
                        type = "HIGH_PRIORITY_RISK",
                        title = "ከፍተኛ የደህንነት ስጋት፡ ${risk.hazardTitle}",
                        description = "በ'$projName' ግንባታ ላይ [${risk.riskLevel}] የሆነ የደህንነት ስጋት ተለይቷል። የተጣቀሰ ሕንፃ ኮድ፡ ${risk.ebcsStandardRefId.uppercase()}",
                        associatedName = projName,
                        detailValue = risk.riskLevel,
                        timestamp = risk.dateLogged,
                        recipientEmails = listOf(engEmail, supEmail),
                        emailSubject = subject,
                        emailBody = body,
                        isSent = sentIds.contains(alertId)
                    )
                )
            }
        }

        // 2. Check for Approaching Project Deadlines
        projs.forEach { proj ->
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = proj.timestamp }
            val startEth = EthiopianCalendar.gregorianToEthiopian(
                cal.get(java.util.Calendar.YEAR),
                cal.get(java.util.Calendar.MONTH) + 1,
                cal.get(java.util.Calendar.DAY_OF_MONTH)
            )

            // Determine scale based on buildingCategory
            val scale = when {
                proj.buildingCategory.contains("ሀ") || proj.buildingCategory.contains("የጭቃ") -> "Small"
                proj.buildingCategory.contains("ለ") -> "Medium"
                else -> "Large"
            }

            // Milestone 1: Commencement (6, 12, or 18 months depending on scale)
            val commencementMonths = when (scale) {
                "Small" -> 6
                "Medium" -> 12
                else -> 18
            }
            val commencementDays = commencementMonths * 30
            val commencementEth = startEth.addDays(commencementDays)
            val commencementMillis = toGregorianMillis(commencementEth.year, commencementEth.month, commencementEth.day)

            // Milestone 2: Completion (18, 24, or 36 months depending on scale)
            val completionMonths = when {
                scale == "Small" -> 18
                scale == "Medium" -> 24
                else -> 36
            }
            val completionDays = completionMonths * 30
            val completionEth = startEth.addDays(completionDays)
            val completionMillis = toGregorianMillis(completionEth.year, completionEth.month, completionEth.day)

            // Milestone 3: Foundation Finish (60 days)
            val foundationEth = startEth.addDays(60)
            val foundationMillis = toGregorianMillis(foundationEth.year, foundationEth.month, foundationEth.day)

            val now = System.currentTimeMillis()

            val milestonesToCheck = listOf(
                Triple("የግንባታ መጀመሪያ ቀነ-ገደብ (Commencement)", commencementMillis, "${commencementEth.day}/${commencementEth.month}/${commencementEth.year}"),
                Triple("የግንባታ ማጠናቀቂያ ቀነ-ገደብ (Completion)", completionMillis, "${completionEth.day}/${completionEth.month}/${completionEth.year}"),
                Triple("የመሠረት ግንባታ ማጠናቀቂያ (Foundation Finish)", foundationMillis, "${foundationEth.day}/${foundationEth.month}/${foundationEth.year}")
            )

            milestonesToCheck.forEach { (milestoneName, milMillis, ethDateStr) ->
                val diffMillis = milMillis - now
                val daysRemaining = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

                // If deadline is approaching (positive and within threshold days, or slightly overdue within 7 days)
                if (daysRemaining in -7..threshold) {
                    val label = if (daysRemaining < 0) "ቀነ-ገደብ አልፏል (${-daysRemaining} ቀን በፊት)" else "ቀሪ ቀናት፡ $daysRemaining ቀን"
                    val subject = "📅 የኮንስትራክሽን ግንባታ ቀነ-ገደብ ማስጠንቀቂያ፡ $milestoneName"
                    val body = """
                        ሰላም የሳይት መሃንዲስ እና የክትትል ባለሙያ፣
                        
                        በግንባታ ክትትል ቁጥጥር መድረክ ላይ የሚከተለው የሊዝ / የግንባታ ቀነ-ገደብ እየቀረበ መሆኑ ተለይቷል፡
                        
                        • ግንባታ/ፕሮጀክት፡ ${proj.name}
                        • ቀነ-ገደብ: $milestoneName
                        • የኢትዮጵያ ቀን: $ethDateStr ዓ.ም
                        • የሁኔታ ማብራሪያ: $label
                        • የግንባታ ሁኔታ: ${proj.status} (የጊዜ ሰሌዳ ፍጥነት: ${proj.scheduleProgressPercent}%)
                        
                        በአዋጅ ቁጥር 721/2011 እና ተያያዥ መመሪያዎች መሠረት ተገቢው ክትትል እንዲደረግ እናሳስባለን።
                        
                        ከሰላምታ ጋር፣
                        የቲሊሊ ከተማ ኮንስትራክሽን ጽሕፈት ቤት
                    """.trimIndent()

                    val alertId = "deadline_${proj.id}_${milestoneName.hashCode()}"
                    alerts.add(
                        AutomatedAlert(
                            id = alertId,
                            type = "DEADLINE_APPROACHING",
                            title = "የቀነ-ገደብ ማስጠንቀቂያ፡ $milestoneName",
                            description = "በ'${proj.name}' ግንባታ ላይ ያለው $milestoneName እየቀረበ ነው። የኢትዮጵያ ቀን፡ $ethDateStr ዓ.ም ($label)።",
                            associatedName = proj.name,
                            detailValue = label,
                            timestamp = milMillis,
                            recipientEmails = listOf(engEmail, supEmail),
                            emailSubject = subject,
                            emailBody = body,
                            isSent = sentIds.contains(alertId)
                        )
                    )
                }
            }
        }

        alerts.sortedBy { it.timestamp }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun triggerEmailIntent(context: Context, alert: AutomatedAlert) {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, alert.recipientEmails.toTypedArray())
            putExtra(Intent.EXTRA_SUBJECT, alert.emailSubject)
            putExtra(Intent.EXTRA_TEXT, alert.emailBody)
        }
        try {
            context.startActivity(Intent.createChooser(emailIntent, "ማስጠንቀቂያ በኢሜይል ላክ..."))
            markAlertAsSent(alert.id)
            Toast.makeText(context, "ኢሜይል መላኪያ ተከፍቷል! 📧", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "ኢሜይል መላኪያ መተግበሪያ አልተገኘም።", Toast.LENGTH_SHORT).show()
        }
    }

    fun triggerLocalNotification(context: Context, title: String, message: String) {
        val channelId = "tilli_construction_alerts"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "የግንባታ ደህንነትና ቀነ-ገደብ ማስጠንቀቂያዎች",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "ስለ ግንባታ ቀነ-ገደብ እና ከፍተኛ ስጋቶች አውቶማቲክ ማስጠንቀቂያዎች"
                enableLights(true)
                lightColor = android.graphics.Color.RED
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(message))
            .build()

        notificationManager.notify(java.util.Random().nextInt(100000), notification)
    }
}
