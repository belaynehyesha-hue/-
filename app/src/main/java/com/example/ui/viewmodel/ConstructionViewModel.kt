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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

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
        // Seed Professionals
        val prof1 = Professional(name = "በላይነህ የሻለም (Civil Engineer)", phone = "+251911223344", role = "የስራ ተቆጣጣሪ", grade = "ሲቪል መሃንዲስ (Grade 1)", licenseNumber = "ETH-INSP-2026-089")
        val prof2 = Professional(name = "ሳሙኤል ግርማ", phone = "+251912445566", role = "የኮንስትራክሽን አማካሪ", grade = "አማካሪ መሃንዲስ (Grade 2)", licenseNumber = "ETH-CONS-2024-541")
        val prof3 = Professional(name = "ቁጥር ፩ ግንባታ ተቋራጭ (Number 1 Contractor)", phone = "+251915998877", role = "የስራ ተቋራጭ", grade = "ጠቅላላ ተቋራጭ (GC-4)", licenseNumber = "ETH-CONT-2025-102")
        val prof4 = Professional(name = "አሰፋ ከበደ", phone = "+251920112233", role = "የስራ ተቆጣጣሪ", grade = "ረዳት ተቆጣጣሪ (Grade 4)", licenseNumber = "ETH-INSP-2026-112")

        repository.insertProfessional(prof1)
        repository.insertProfessional(prof2)
        repository.insertProfessional(prof3)
        repository.insertProfessional(prof4)

        // Seed Artisans (Skilled Labor)
        val artisan1 = Artisan(name = "አስቻለው ከበደ", phone = "+251944112233", tradeSector = "ግንበኛ", cocNumber = "COC-MAS-1092-26", certificatePhotoPath = "simulated_coc_mason.jpg", experienceYears = 8)
        val artisan2 = Artisan(name = "ዮናስ ታደሰ", phone = "+251940998877", tradeSector = "አናፂ", cocNumber = "COC-CAR-2281-25", certificatePhotoPath = "simulated_coc_carpenter.jpg", experienceYears = 5)
        val artisan3 = Artisan(name = "ትርፌ በቀለ", phone = "+251930224455", tradeSector = "ቧንቧ ሰራተኛ", cocNumber = "COC-PLU-9021-26", certificatePhotoPath = "simulated_coc_plumber.jpg", experienceYears = 6)
        val artisan4 = Artisan(name = "ግሩም ተስፋዬ", phone = "+251915334455", tradeSector = "ኤሌክትሪሺያን", cocNumber = "COC-ELE-4412-24", certificatePhotoPath = "simulated_coc_electrician.jpg", experienceYears = 4)
        val artisan5 = Artisan(name = "በላይነህ ደረጄ", phone = "+251965889900", tradeSector = "ፌሮዮ (የግንባታ ብረት ሰራተኛ)", cocNumber = "COC-REB-8712-25", certificatePhotoPath = "simulated_coc_rebar.jpg", experienceYears = 10)

        repository.insertArtisan(artisan1)
        repository.insertArtisan(artisan2)
        repository.insertArtisan(artisan3)
        repository.insertArtisan(artisan4)
        repository.insertArtisan(artisan5)

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

    class PdfCreator(private val file: File) {
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
            canvas?.drawText("የቲሊሊ ከተማ ኮንስትራክሽን ጽ/ቤት - Construction BYA", 330f, 810f, paint)
            
            // Header line
            val paintLine = Paint().apply {
                color = android.graphics.Color.LTGRAY
                strokeWidth = 0.5f
            }
            canvas?.drawLine(leftMargin, 800f, rightMargin, 800f, paintLine)
        }

        // Helper to draw text with word wrap
        fun drawText(text: String, size: Float = 10f, isBold: Boolean = false, color: Int = android.graphics.Color.BLACK, spaceAfter: Float = 4f) {
            val paint = Paint().apply {
                this.color = color
                this.textSize = size
                this.isAntiAlias = true
                if (isBold) {
                    this.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
            }

            val maxTextWidth = rightMargin - leftMargin
            val words = text.split(" ")
            var currentLine = StringBuilder()

            for (word in words) {
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

    fun shareReport(context: Context, isExcel: Boolean) {
        val file = if (isExcel) generateExcelReport(context) else generatePdfTextReport(context)
        if (file != null && file.exists()) {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = if (isExcel) "text/comma-separated-values" else "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, if (isExcel) "የቲሊሊ ከተማ ኮንስትራክሽን Excel ሪፖርት" else "የቲሊሊ ከተማ ኮንስትራክሽን PDF ሪፖርት")
                putExtra(Intent.EXTRA_TEXT, "የቲሊሊ ከተማ ኮንስትራክሽን ጽሕፈት ቤት ግንባታ ክትትልና ቁጥጥር ቡድን የተዘጋጀ ሪፖርት")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "ሪፖርት አጋራ"))
        } else {
            Toast.makeText(context, "ሪፖርት ማዘጋጀት አልተቻለም።", Toast.LENGTH_SHORT).show()
        }
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

    fun shareSummaryReport(context: Context, isExcel: Boolean) {
        val file = if (isExcel) generateSummaryReportExcel(context) else generateSummaryReportPdfText(context)
        if (file != null && file.exists()) {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = if (isExcel) "text/comma-separated-values" else "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, if (isExcel) "የግንባታ ክትትልና ደህንነት ማጠቃለያ Excel ሪፖርት" else "የግንባታ ክትትልና ደህንነት ማጠቃለያ PDF ሪፖርት")
                putExtra(Intent.EXTRA_TEXT, "የቲሊሊ ከተማ ኮንስትራክሽን ጽሕፈት ቤት ግንባታ ክትትልና የደህንነት አደጋዎች ማጠቃለያ ሪፖርት")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "ማጠቃለያ ሪፖርት አጋራ"))
        } else {
            Toast.makeText(context, "ማጠቃለያ ሪፖርት ማዘጋጀት አልተቻለም።", Toast.LENGTH_SHORT).show()
        }
    }

    // ==================== AUTOMATED NOTIFICATION & EMAIL ALERT SYSTEM ====================

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
