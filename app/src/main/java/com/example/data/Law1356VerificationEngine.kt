package com.example.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Severity level of statutory document flags under Ethiopian Building Law No. 1356/2017.
 */
enum class DocVerificationSeverity {
    CRITICAL, // Legal violation causing immediate suspension or work stoppage (e.g., expired/missing license, disciplinary suspension)
    HIGH,     // High non-compliance preventing project approval or signing (e.g., missing PII insurance, severe CPD deficit, missing Fayda ID)
    MEDIUM,   // Medium severity flag
    WARNING,  // Approaching expiry or partial compliance (e.g., expiring within 60 days, CPD 20-29 points, unverified digital signature)
    INFO      // Advisory informational notice
}

/**
 * Classification category of the flag.
 */
enum class DocFlagCategory {
    EXPIRED_DOCUMENT,          // ሰነዱ ጊዜው ያለፈበት
    MISSING_DOCUMENT,          // ሰነዱ ፈጽሞ ያልቀረበ/የጎደለ
    INADEQUATE_CPD,            // የሙያ ማሻሻያ ነጥብ ያልተሟላ
    REGULATORY_RESTRICTION,    // የቁጥጥር ወይም የዲስፕሊን ገደብ
    UNVERIFIED_DIGITAL_SEAL,   // ያልተረጋገጠ ዲጂታል ፊርማና ማህተም
    LICENSE,                   // የሙያ ፈቃድ
    INSURANCE,                 // ኢንሹራንስ
    CPD,                       // ሲፒዲ
    IDENTIFICATION             // ፋይዳ መታወቂያ
}

/**
 * Specific statutory flag type under Proclamation 1356/2017 and MUI Directives.
 */
enum class DocFlagType {
    EXPIRED_PRACTICING_LICENSE,
    MISSING_PRACTICING_LICENSE,
    EXPIRING_PRACTICING_LICENSE,
    EXPIRED_INDEMNITY_INSURANCE,
    MISSING_INDEMNITY_INSURANCE,
    EXPIRING_INDEMNITY_INSURANCE,
    MISSING_FAYDA_NATIONAL_ID,
    CRITICAL_CPD_SHORTFALL,
    INSUFFICIENT_CPD_POINTS,
    MISSING_ACADEMIC_CREDENTIALS,
    MISSING_ATTACHED_DEGREE_FILE,
    MISSING_ATTACHED_LICENSE_FILE,
    MISSING_ATTACHED_INSURANCE_FILE,
    UNVERIFIED_DIGITAL_SIGNATURE,
    DISCIPLINARY_SANCTION,
    BUILDING_CATEGORY_MISMATCH,
    MISSING_DEGREE_DOCUMENT,
    MISSING_ATTACHED_DOSSIER
}

/**
 * Detailed verification flag record.
 */
data class DocumentVerificationFlag(
    val flagType: DocFlagType,
    val category: DocFlagCategory,
    val severity: DocVerificationSeverity,
    val documentName: String,
    val statutoryClause: String, // e.g. "አዋጅ 1356/2017 አንቀጽ 28(1)"
    val title: String,
    val descriptionAmharic: String,
    val remedialActionAmharic: String,
    val isExpired: Boolean = false,
    val isMissing: Boolean = false,
    val daysRemainingOrOverdue: Long? = null
) {
    val statutoryCitation: String
        get() = statutoryClause

    val description: String
        get() = descriptionAmharic
}

/**
 * Status of individual statutory document.
 */
enum class DocumentHealthStatus {
    COMPLIANT,  // ሙሉ በሙሉ ተገዢና ህጋዊ
    EXPIRED,    // ጊዜው ያለፈበት
    MISSING,    // ያልቀረበ / የጎደለ
    EXPIRING,   // በቅርቡ የሚያበቃ (Warning)
    DEFICIENT,  // ጉድለት ያለበት (e.g. CPD)
    UNVERIFIED, // ያልተረጋገጠ
    VALID,      // የተሟላ ህጋዊ
    RENEWAL_DUE // ማደስ ያለበት
}

/**
 * Itemized health report for each core statutory document required by Law 1356/2017.
 */
data class StatutoryDocumentCheckItem(
    val documentTitle: String,
    val statutoryRequirement: String,
    val documentValue: String,
    val status: DocumentHealthStatus,
    val statusLabel: String,
    val expiryOrDetail: String,
    val statutoryArticle: String
) {
    val docName: String
        get() = documentTitle

    val legalArticle: String
        get() = statutoryArticle

    val detailAmharic: String
        get() = expiryOrDetail
}

/**
 * Comprehensive verification output for a professional.
 */
data class ProfessionalVerificationReport(
    val professionalId: Int,
    val professionalName: String,
    val role: String,
    val grade: String,
    val complianceScore: Int, // 0 to 100%
    val statutoryStatus: String, // "ተገዢ (COMPLIANT)", "ጊዜው ያለፈበት ሰነድ (EXPIRED DOCS)", "ያልተሟላ ሰነድ (MISSING DOCS)", "ማደስ ያለበት (RENEWAL DUE)", "የታገደ (SUSPENDED)"
    val isFullyCompliant: Boolean,
    val hasExpiredDocs: Boolean,
    val hasMissingDocs: Boolean,
    val hasCriticalFlags: Boolean,
    val expiredDocsCount: Int,
    val missingDocsCount: Int,
    val warningCount: Int,
    val flags: List<DocumentVerificationFlag>,
    val documentChecks: List<StatutoryDocumentCheckItem>,
    val summaryAmharic: String,
    val verificationTimestamp: Long = System.currentTimeMillis(),
    val professional: Professional
)

/**
 * High level audit summary across all professionals in the bureau/organization.
 */
data class SystemVerificationSummary(
    val totalProfessionals: Int,
    val fullyCompliantCount: Int,
    val flaggedProfessionalsCount: Int,
    val expiredDocsProfessionalsCount: Int,
    val missingDocsProfessionalsCount: Int,
    val renewalDueCount: Int,
    val suspendedCount: Int,
    val averageComplianceScore: Int,
    val criticalFlagsTotal: Int,
    val highFlagsTotal: Int,
    val warningFlagsTotal: Int,
    val reports: List<ProfessionalVerificationReport> = emptyList(),
    val totalProfessionalsScanned: Int = totalProfessionals,
    val breakdownByCategory: Map<DocFlagCategory, Int> = emptyMap()
)

/**
 * Core Statutory Verification Engine for Ethiopian Building Law No. 1356/2017 (አዋጅ ቁጥር ፩ሺ፫፻፶፮/፪ሺ፲፯ ዓ.ም)
 * and Ministry of Urban & Infrastructure (MUI) Implementation Directives.
 */
object Law1356VerificationEngine {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    /**
     * Primary verification function that audits all documents, credentials, and compliance parameters
     * for a registered professional and flags any expired or missing statutory documentation.
     */
    fun verifyProfessional(
        professional: Professional,
        now: Long = System.currentTimeMillis()
    ): ProfessionalVerificationReport {
        val flags = mutableListOf<DocumentVerificationFlag>()
        val checks = mutableListOf<StatutoryDocumentCheckItem>()

        var scorePoints = 100

        val attachedDocsLower = professional.attachedDocsJson.lowercase(Locale.getDefault())

        // -------------------------------------------------------------
        // 1. Practicing License (የሙያ ፈቃድ) Verification - Law 1356/2017 Art. 28 & 29
        // -------------------------------------------------------------
        val licenseNum = professional.licenseNumber.trim()
        val isLicenseBlank = licenseNum.isEmpty() || licenseNum.equals("PENDING", ignoreCase = true) || licenseNum.contains("የለም")
        val licenseExpiry = professional.licenseExpiryDate
        val isLicenseSuspended = professional.licenseStatus.contains("SUSPENDED", ignoreCase = true) || professional.licenseStatus.contains("የታገደ")
        val daysUntilLicenseExpiry = (licenseExpiry - now) / (24 * 3600 * 1000L)

        when {
            isLicenseSuspended -> {
                flags.add(
                    DocumentVerificationFlag(
                        flagType = DocFlagType.DISCIPLINARY_SANCTION,
                        category = DocFlagCategory.REGULATORY_RESTRICTION,
                        severity = DocVerificationSeverity.CRITICAL,
                        documentName = "የሙያ ፈቃድ (Practicing License)",
                        statutoryClause = "አዋጅ 1356/2017 አንቀጽ 45",
                        title = "የታገደ የሙያ ፈቃድ (License Suspended)",
                        descriptionAmharic = "የባለሙያው የሙያ ፈቃድ በፍተሻ ግኝት ወይም በስነ-ምግባር ጉድለት ምክንያት በMUI/በከተማ አስተዳደሩ ታግዷል።",
                        remedialActionAmharic = "በተቆጣጣሪ ባለስልጣን የቀረቡ የዕግድ ቅድመ-ሁኔታዎችን አሟልተው እግዱ እስኪነሳ ምንም ዓይነት የሳይት ስራ ወይም ፊርማ አይፈቀድም።"
                    )
                )
                checks.add(
                    StatutoryDocumentCheckItem(
                        documentTitle = "የሙያ ፈቃድ (Practicing License)",
                        statutoryRequirement = "በስራ ላይ ያለ ህጋዊ ፈቃድ",
                        documentValue = licenseNum.ifEmpty { "ያልተመዘገበ" },
                        status = DocumentHealthStatus.DEFICIENT,
                        statusLabel = "የታገደ (SUSPENDED)",
                        expiryOrDetail = "የእግድ ውሳኔ",
                        statutoryArticle = "አዋጅ 1356/2017 አንቀጽ 45"
                    )
                )
                scorePoints -= 40
            }
            isLicenseBlank -> {
                flags.add(
                    DocumentVerificationFlag(
                        flagType = DocFlagType.MISSING_PRACTICING_LICENSE,
                        category = DocFlagCategory.MISSING_DOCUMENT,
                        severity = DocVerificationSeverity.CRITICAL,
                        documentName = "የሙያ ፈቃድ (Practicing License)",
                        statutoryClause = "አዋጅ 1356/2017 አንቀጽ 28(1)",
                        title = "የጎደለ የሙያ ፈቃድ ቁጥር (Missing License)",
                        descriptionAmharic = "በአዋጅ 1356/2017 መሠረት ህጋዊ የሙያ ፈቃድ ሳይመዘገብ ማንኛውንም የግንባታ ዲዛይን፣ ቁጥጥር ወይም ስራ ማከናወን በህግ የተከለከለ ነው።",
                        remedialActionAmharic = "የከተማና መሠረተ ልማት ሚኒስቴር (MUI) የሙያ ምዝገባ ፈቃድ ቁጥር ያስገቡና ሰነዱን ያያይዙ።",
                        isMissing = true
                    )
                )
                checks.add(
                    StatutoryDocumentCheckItem(
                        documentTitle = "የሙያ ፈቃድ (Practicing License)",
                        statutoryRequirement = "ህጋዊ የሙያ ፈቃድ ቁጥር",
                        documentValue = "ያልተመዘገበ (Missing)",
                        status = DocumentHealthStatus.MISSING,
                        statusLabel = "የጎደለ (MISSING)",
                        expiryOrDetail = "ሰነድ አልተያያዘም",
                        statutoryArticle = "አዋጅ 1356/2017 አንቀጽ 28(1)"
                    )
                )
                scorePoints -= 35
            }
            licenseExpiry <= now -> {
                val overdueDays = Math.abs(daysUntilLicenseExpiry)
                flags.add(
                    DocumentVerificationFlag(
                        flagType = DocFlagType.EXPIRED_PRACTICING_LICENSE,
                        category = DocFlagCategory.EXPIRED_DOCUMENT,
                        severity = DocVerificationSeverity.CRITICAL,
                        documentName = "የሙያ ፈቃድ (Practicing License)",
                        statutoryClause = "አዋጅ 1356/2017 አንቀጽ 29",
                        title = "ጊዜው ያለፈበት የሙያ ፈቃድ (License Expired)",
                        descriptionAmharic = "የሙያ ፈቃዱ ህጋዊ አገልግሎት ጊዜ ከ${overdueDays} ቀናት በፊት አብቅቷል። በአዋጁ መሠረት ጊዜው ባለፈበት ፈቃድ የሚሰራ ስራ ተቀባይነት የለውም።",
                        remedialActionAmharic = "በአስቸኳይ አመታዊ እድሳት በMUI e-Service ፖርታል ያከናውኑና አዲሱን የአገልግሎት ዘመን ያዘምኑ።",
                        isExpired = true,
                        daysRemainingOrOverdue = overdueDays
                    )
                )
                checks.add(
                    StatutoryDocumentCheckItem(
                        documentTitle = "የሙያ ፈቃድ (Practicing License)",
                        statutoryRequirement = "በአገልግሎት ላይ ያለ",
                        documentValue = licenseNum,
                        status = DocumentHealthStatus.EXPIRED,
                        statusLabel = "ጊዜው ያለፈበት (EXPIRED)",
                        expiryOrDetail = "ያበቃበት ቀን: ${dateFormat.format(Date(licenseExpiry))} (${overdueDays} ቀናት አልፈዋል)",
                        statutoryArticle = "አዋጅ 1356/2017 አንቀጽ 29"
                    )
                )
                scorePoints -= 30
            }
            daysUntilLicenseExpiry in 1..60 -> {
                flags.add(
                    DocumentVerificationFlag(
                        flagType = DocFlagType.EXPIRING_PRACTICING_LICENSE,
                        category = DocFlagCategory.EXPIRED_DOCUMENT,
                        severity = DocVerificationSeverity.WARNING,
                        documentName = "የሙያ ፈቃድ (Practicing License)",
                        statutoryClause = "አዋጅ 1356/2017 መመሪያ ቁጥር 2/2017",
                        title = "የፈቃድ ማደሻ ጊዜ ደርሷል (License Expiring Soon)",
                        descriptionAmharic = "የሙያ ፈቃዱ በአገልግሎት ላይ የሚቆየው ለቀጣይ ${daysUntilLicenseExpiry} ቀናት ብቻ ነው። የአገልግሎት መቋረጥ እንዳያጋጥም የእድሳት ሂደት ይጀምሩ።",
                        remedialActionAmharic = "የቀጣይ ዓመት ፈቃድ እድሳት ጥያቄ ከCPD ነጥብ ማረጋገጫ ጋር ያቅርቡ።",
                        daysRemainingOrOverdue = daysUntilLicenseExpiry
                    )
                )
                checks.add(
                    StatutoryDocumentCheckItem(
                        documentTitle = "የሙያ ፈቃድ (Practicing License)",
                        statutoryRequirement = "የእድሳት ቅድመ-ማሳወቂያ",
                        documentValue = licenseNum,
                        status = DocumentHealthStatus.EXPIRING,
                        statusLabel = "ማደስ ያለበት (EXPIRING)",
                        expiryOrDetail = "ቀሪ: ${daysUntilLicenseExpiry} ቀናት (${dateFormat.format(Date(licenseExpiry))})",
                        statutoryArticle = "አዋጅ 1356/2017 መመሪያ"
                    )
                )
                scorePoints -= 10
            }
            else -> {
                checks.add(
                    StatutoryDocumentCheckItem(
                        documentTitle = "የሙያ ፈቃድ (Practicing License)",
                        statutoryRequirement = "ህጋዊና በስራ ላይ ያለ",
                        documentValue = licenseNum,
                        status = DocumentHealthStatus.COMPLIANT,
                        statusLabel = "ተገዢ (VALID)",
                        expiryOrDetail = "ያበቃል: ${dateFormat.format(Date(licenseExpiry))} (${daysUntilLicenseExpiry} ቀናት ቀሪ)",
                        statutoryArticle = "አዋጅ 1356/2017 አንቀጽ 28"
                    )
                )
            }
        }

        // -------------------------------------------------------------
        // 2. Professional Indemnity Insurance - PII (የሙያ ኃላፊነት ዋስትና) - Art. 31
        // -------------------------------------------------------------
        val policyNo = professional.insurancePolicyNo.trim()
        val isPolicyBlank = policyNo.isEmpty() || policyNo.equals("የለም", ignoreCase = true) || policyNo.equals("NONE", ignoreCase = true)
        val insuranceExpiry = professional.insuranceExpiryDate
        val daysUntilInsuranceExpiry = (insuranceExpiry - now) / (24 * 3600 * 1000L)

        when {
            isPolicyBlank -> {
                flags.add(
                    DocumentVerificationFlag(
                        flagType = DocFlagType.MISSING_INDEMNITY_INSURANCE,
                        category = DocFlagCategory.MISSING_DOCUMENT,
                        severity = DocVerificationSeverity.HIGH,
                        documentName = "የሙያ ኃላፊነት ዋስትና (PII Insurance)",
                        statutoryClause = "አዋጅ 1356/2017 አንቀጽ 31",
                        title = "የጎደለ የሙያ ኃላፊነት ዋስትና (Missing PII Policy)",
                        descriptionAmharic = "በአዲሱ ሕንፃ አዋጅ 1356/2017 መሠረት አማካሪዎች፣ ተቆጣጣሪዎችና ተቋራጮች የሙያ ኃላፊነት መድን (Professional Indemnity Insurance) መያዝ ግዴታ ነው።",
                        remedialActionAmharic = "ከተፈቀደ የኢንሹራንስ ኩባንያ የሙያ መድን ዋስትና ፖሊሲ ሰርተፊኬት አስገብተው ያያይዙ።",
                        isMissing = true
                    )
                )
                checks.add(
                    StatutoryDocumentCheckItem(
                        documentTitle = "የሙያ መድን ዋስትና (PII Policy)",
                        statutoryRequirement = "አንቀጽ 31 - ግዴታ የሙያ ዋስትና",
                        documentValue = "ያልቀረበ (Missing)",
                        status = DocumentHealthStatus.MISSING,
                        statusLabel = "የጎደለ (MISSING)",
                        expiryOrDetail = "የኢንሹራንስ ፖሊሲ አልተመዘገበም",
                        statutoryArticle = "አዋጅ 1356/2017 አንቀጽ 31"
                    )
                )
                scorePoints -= 25
            }
            insuranceExpiry <= now -> {
                val overdueInsDays = Math.abs(daysUntilInsuranceExpiry)
                flags.add(
                    DocumentVerificationFlag(
                        flagType = DocFlagType.EXPIRED_INDEMNITY_INSURANCE,
                        category = DocFlagCategory.EXPIRED_DOCUMENT,
                        severity = DocVerificationSeverity.HIGH,
                        documentName = "የሙያ ኃላፊነት ዋስትና (PII Insurance)",
                        statutoryClause = "አዋጅ 1356/2017 አንቀጽ 31",
                        title = "ጊዜው ያለፈበት የሙያ ዋስትና ፖሊሲ (PII Expired)",
                        descriptionAmharic = "የሙያ ኃላፊነት ዋስትና ኢንሹራንስ ፖሊሲው ከ${overdueInsDays} ቀናት በፊት ጊዜው አልፏል። የዋስትና ሽፋን በሌለበት ሁኔታ ስራ መስራት ተጠያቂነት ያስከትላል።",
                        remedialActionAmharic = "የመድን ፖሊሲውን በኢንሹራንስ ኩባንያዎ ያድሱና አዲሱን የፖሊሲ ቁጥር ያስመዝግቡ።",
                        isExpired = true,
                        daysRemainingOrOverdue = overdueInsDays
                    )
                )
                checks.add(
                    StatutoryDocumentCheckItem(
                        documentTitle = "የሙያ መድን ዋስትና (PII Policy)",
                        statutoryRequirement = "ንቁ የመድን ሽፋን",
                        documentValue = policyNo,
                        status = DocumentHealthStatus.EXPIRED,
                        statusLabel = "ጊዜው ያለፈበት (EXPIRED)",
                        expiryOrDetail = "ያበቃበት: ${dateFormat.format(Date(insuranceExpiry))}",
                        statutoryArticle = "አዋጅ 1356/2017 አንቀጽ 31"
                    )
                )
                scorePoints -= 20
            }
            daysUntilInsuranceExpiry in 1..30 -> {
                flags.add(
                    DocumentVerificationFlag(
                        flagType = DocFlagType.EXPIRING_INDEMNITY_INSURANCE,
                        category = DocFlagCategory.EXPIRED_DOCUMENT,
                        severity = DocVerificationSeverity.WARNING,
                        documentName = "የሙያ ኃላፊነት ዋስትና (PII Insurance)",
                        statutoryClause = "አዋጅ 1356/2017 አንቀጽ 31",
                        title = "የኢንሹራንስ ፖሊሲ በቅርቡ ያበቃል (PII Expiring Soon)",
                        descriptionAmharic = "የመድን ዋስትና ፖሊሲው የሚያበቃው በ${daysUntilInsuranceExpiry} ቀናት ውስጥ ነው። ሽፋን እንዳይቋረጥ ቅድመ ዝግጅት ያድርጉ።",
                        remedialActionAmharic = "ከመድን ሰጪ ኩባንያው የእድሳት ስምምነት ያድርጉ።",
                        daysRemainingOrOverdue = daysUntilInsuranceExpiry
                    )
                )
                checks.add(
                    StatutoryDocumentCheckItem(
                        documentTitle = "የሙያ መድን ዋስትና (PII Policy)",
                        statutoryRequirement = "የእድሳት ቅድመ-ማሳሰቢያ",
                        documentValue = policyNo,
                        status = DocumentHealthStatus.EXPIRING,
                        statusLabel = "ማደስ ያለበት (EXPIRING)",
                        expiryOrDetail = "ቀሪ: ${daysUntilInsuranceExpiry} ቀናት",
                        statutoryArticle = "አዋጅ 1356/2017 አንቀጽ 31"
                    )
                )
                scorePoints -= 5
            }
            else -> {
                checks.add(
                    StatutoryDocumentCheckItem(
                        documentTitle = "የሙያ መድን ዋስትና (PII Policy)",
                        statutoryRequirement = "ንቁ የሙያ ሽፋን",
                        documentValue = policyNo,
                        status = DocumentHealthStatus.COMPLIANT,
                        statusLabel = "ተገዢ (ACTIVE)",
                        expiryOrDetail = "ያበቃል: ${dateFormat.format(Date(insuranceExpiry))} (${daysUntilInsuranceExpiry} ቀናት ቀሪ)",
                        statutoryArticle = "አዋጅ 1356/2017 አንቀጽ 31"
                    )
                )
            }
        }

        // -------------------------------------------------------------
        // 3. National Fayda Digital ID Verification
        // -------------------------------------------------------------
        val nationalIdTrimmed = professional.nationalId.trim()
        val isFaydaMissing = nationalIdTrimmed.isEmpty() || nationalIdTrimmed.equals("የለም", ignoreCase = true)

        if (isFaydaMissing) {
            flags.add(
                DocumentVerificationFlag(
                    flagType = DocFlagType.MISSING_FAYDA_NATIONAL_ID,
                    category = DocFlagCategory.MISSING_DOCUMENT,
                    severity = DocVerificationSeverity.HIGH,
                    documentName = "ብሔራዊ የፋይዳ መለያ (Fayda Digital ID)",
                    statutoryClause = "የኢትዮጵያ ዲጂታል መታወቂያ አዋጅና 1356/2017",
                    title = "የጎደለ የፋይዳ መለያ (Missing Fayda ID)",
                    descriptionAmharic = "በባለሙያዎች ዲጂታል ምዝገባና ፍተሻ ሲስተም ውስጥ ህጋዊ የፋይዳ (Fayda National ID) መለያ አልተረጋገጠም።",
                    remedialActionAmharic = "የባለሙያውን ህጋዊ የፋይዳ ቁጥር (ET-FAYDA-XXXX) ወደ ማህደሩ ያስገቡ።",
                    isMissing = true
                )
            )
            checks.add(
                StatutoryDocumentCheckItem(
                    documentTitle = "የፋይዳ ዲጂታል መለያ (Fayda ID)",
                    statutoryRequirement = "ብሔራዊ ዲጂታል መታወቂያ",
                    documentValue = "ያልተመዘገበ (Missing)",
                    status = DocumentHealthStatus.MISSING,
                    statusLabel = "የጎደለ (MISSING)",
                    expiryOrDetail = "ዲጂታል መለያ አልተረጋገጠም",
                    statutoryArticle = "አዋጅ 1356/2017 ዲጂታል መመሪያ"
                )
            )
            scorePoints -= 15
        } else {
            checks.add(
                StatutoryDocumentCheckItem(
                    documentTitle = "የፋይዳ ዲጂታል መለያ (Fayda ID)",
                    statutoryRequirement = "ብሔራዊ ዲጂታል መለያ",
                    documentValue = nationalIdTrimmed,
                    status = DocumentHealthStatus.COMPLIANT,
                    statusLabel = "ተረጋግጧል (VERIFIED)",
                    expiryOrDetail = "በፋይዳ ሲስተም የፀና",
                    statutoryArticle = "አዋጅ 1356/2017"
                )
            )
        }

        // -------------------------------------------------------------
        // 4. Continuing Professional Development - CPD (30 Credits Min) - Art. 34
        // -------------------------------------------------------------
        val cpd = professional.cpdPoints
        when {
            cpd < 20 -> {
                val deficit = 30 - cpd
                flags.add(
                    DocumentVerificationFlag(
                        flagType = DocFlagType.CRITICAL_CPD_SHORTFALL,
                        category = DocFlagCategory.INADEQUATE_CPD,
                        severity = DocVerificationSeverity.HIGH,
                        documentName = "የቀጣይ ሙያ ማሻሻያ (CPD Credits)",
                        statutoryClause = "አዋጅ 1356/2017 አንቀጽ 34",
                        title = "ከፍተኛ የCPD ነጥብ ጉድለት (Critical CPD Deficit)",
                        descriptionAmharic = "የተመዘገበው የCPD ነጥብ ${cpd}/30 ብቻ ነው። በአዋጅ 1356/2017 መሠረት ፈቃድ ለማደስ ቢያንስ 30 ነጥብ ማሟላት ግዴታ ነው።",
                        remedialActionAmharic = "በእውቅና በተሰጣቸው ማህበራት (EEA/AEA) ቀሪ ${deficit} ነጥብ ስልጠናዎችን ወስደው ሰርተፊኬት ያቅርቡ።"
                    )
                )
                checks.add(
                    StatutoryDocumentCheckItem(
                        documentTitle = "የCPD ስልጠና ነጥብ (CPD Hours)",
                        statutoryRequirement = "ዝቅተኛው 30 ነጥብ በዓመት",
                        documentValue = "$cpd / 30 ነጥብ",
                        status = DocumentHealthStatus.DEFICIENT,
                        statusLabel = "ጉድለት (${deficit} ነጥብ)",
                        expiryOrDetail = "ከመመሪያው መስፈርት በታች",
                        statutoryArticle = "አዋጅ 1356/2017 አንቀጽ 34"
                    )
                )
                scorePoints -= 20
            }
            cpd in 20..29 -> {
                val deficit = 30 - cpd
                flags.add(
                    DocumentVerificationFlag(
                        flagType = DocFlagType.INSUFFICIENT_CPD_POINTS,
                        category = DocFlagCategory.INADEQUATE_CPD,
                        severity = DocVerificationSeverity.WARNING,
                        documentName = "የቀጣይ ሙያ ማሻሻያ (CPD Credits)",
                        statutoryClause = "አዋጅ 1356/2017 አንቀጽ 34",
                        title = "ያልተሟላ የCPD ነጥብ (Incomplete CPD)",
                        descriptionAmharic = "የCPD ነጥብ ${cpd}/30 ደርሷል። ለፈቃድ እድሳት የተሟላ ተገዢ ለመሆን ቀሪ ${deficit} ነጥብ ያስፈልጋል።",
                        remedialActionAmharic = "ቀሪውን ${deficit} ነጥብ ስልጠና በቅርቡ በማጠናቀቅ ማህደርዎን ያሟሉ።"
                    )
                )
                checks.add(
                    StatutoryDocumentCheckItem(
                        documentTitle = "የCPD ስልጠና ነጥብ (CPD Hours)",
                        statutoryRequirement = "30 ነጥብ ማሟላት",
                        documentValue = "$cpd / 30 ነጥብ",
                        status = DocumentHealthStatus.EXPIRING,
                        statusLabel = "ያልተሟላ (ቀሪ ${deficit})",
                        expiryOrDetail = "የእድሳት ቅድመ-ሁኔታ",
                        statutoryArticle = "አዋጅ 1356/2017 አንቀጽ 34"
                    )
                )
                scorePoints -= 8
            }
            else -> {
                checks.add(
                    StatutoryDocumentCheckItem(
                        documentTitle = "የCPD ስልጠና ነጥብ (CPD Hours)",
                        statutoryRequirement = "ዝቅተኛው 30 ነጥብ",
                        documentValue = "$cpd / 30 ነጥብ",
                        status = DocumentHealthStatus.COMPLIANT,
                        statusLabel = "ተገዢ (FULFILLED)",
                        expiryOrDetail = "የህግ መስፈርቱን አሟልቷል",
                        statutoryArticle = "አዋጅ 1356/2017 አንቀጽ 34"
                    )
                )
            }
        }

        // -------------------------------------------------------------
        // 5. Educational Qualifications & Graduation Verification - Art. 28(2)
        // -------------------------------------------------------------
        val qual = professional.qualification.trim()
        val inst = professional.institutionGraduated.trim()
        val isQualMissing = qual.isEmpty() || inst.isEmpty()

        if (isQualMissing) {
            flags.add(
                DocumentVerificationFlag(
                    flagType = DocFlagType.MISSING_ACADEMIC_CREDENTIALS,
                    category = DocFlagCategory.MISSING_DOCUMENT,
                    severity = DocVerificationSeverity.HIGH,
                    documentName = "የትምህርት ማስረጃ (Degree Credentials)",
                    statutoryClause = "አዋጅ 1356/2017 አንቀጽ 28(2)",
                    title = "የጎደለ የትምህርት ዝግጅት መረጃ (Missing Academic Credentials)",
                    descriptionAmharic = "የባለሙያው የመጀመሪያ ወይም ሁለተኛ ዲግሪ የትምህርት ዝግጅት ወይም የተመረቀበት የከፍተኛ ትምህርት ተቋም አልተመዘገበም።",
                    remedialActionAmharic = "የትምህርት ደረጃውን (BSc/MSc/PhD) እና ተቋሙን በማህደሩ ላይ ይሙሉ፤ የትምህርት ማስረጃ ያያይዙ።",
                    isMissing = true
                )
            )
            checks.add(
                StatutoryDocumentCheckItem(
                    documentTitle = "የትምህርት ማስረጃ (Academic Degree)",
                    statutoryRequirement = "የከፍተኛ ትምህርት ሰነድ",
                    documentValue = "ያልተሟላ (Incomplete)",
                    status = DocumentHealthStatus.MISSING,
                    statusLabel = "የጎደለ (MISSING)",
                    expiryOrDetail = "ተቋም ወይም ዲግሪ አልተመዘገበም",
                    statutoryArticle = "አዋጅ 1356/2017 አንቀጽ 28(2)"
                )
            )
            scorePoints -= 15
        } else {
            checks.add(
                StatutoryDocumentCheckItem(
                    documentTitle = "የትምህርት ማስረጃ (Academic Degree)",
                    statutoryRequirement = "እውቅና ያለው የትምህርት ማስረጃ",
                    documentValue = "$qual (${professional.graduationYear})",
                    status = DocumentHealthStatus.COMPLIANT,
                    statusLabel = "ተረጋግጧል (VERIFIED)",
                    expiryOrDetail = inst,
                    statutoryArticle = "አዋጅ 1356/2017 አንቀጽ 28(2)"
                )
            )
        }

        // -------------------------------------------------------------
        // 6. Digital Stamp & Signature Verification - Art. 42
        // -------------------------------------------------------------
        if (!professional.digitalSignatureVerified) {
            flags.add(
                DocumentVerificationFlag(
                    flagType = DocFlagType.UNVERIFIED_DIGITAL_SIGNATURE,
                    category = DocFlagCategory.UNVERIFIED_DIGITAL_SEAL,
                    severity = DocVerificationSeverity.WARNING,
                    documentName = "ዲጂታል ፊርማና ማህተም (Digital Seal)",
                    statutoryClause = "አዋጅ 1356/2017 አንቀጽ 42",
                    title = "ያልተረጋገጠ ዲጂታል ማህተም (Unverified Digital Signature)",
                    descriptionAmharic = "በኤሌክትሮኒክ የግንባታ ፈቃድና የሳይት ፍተሻ ሲስተም (e-Permit) የሚሰራ ዲጂታል ፊርማና ማህተም ማረጋገጫ አልተከናወነም።",
                    remedialActionAmharic = "የባለሙያውን ዲጂታል ፊርማና ማህተም በሲስተሙ ያረጋግጡ።"
                )
            )
            checks.add(
                StatutoryDocumentCheckItem(
                    documentTitle = "ዲጂታል ፊርማና ማህተም (Digital Seal)",
                    statutoryRequirement = "አንቀጽ 42 - ዲጂታል ፊርማ",
                    documentValue = "ያልተረጋገጠ (Unverified)",
                    status = DocumentHealthStatus.UNVERIFIED,
                    statusLabel = "ያልተረጋገጠ",
                    expiryOrDetail = "በe-Permit ማረጋገጫ ይፈልጋል",
                    statutoryArticle = "አዋጅ 1356/2017 አንቀጽ 42"
                )
            )
            scorePoints -= 5
        } else {
            checks.add(
                StatutoryDocumentCheckItem(
                    documentTitle = "ዲጂታል ፊርማና ማህተም (Digital Seal)",
                    statutoryRequirement = "ህጋዊ ዲጂታል ፊርማ",
                    documentValue = "የተረጋገጠ ዲጂታል ማህተም",
                    status = DocumentHealthStatus.COMPLIANT,
                    statusLabel = "የተረጋገጠ (VERIFIED)",
                    expiryOrDetail = "በሲስተሙ የፀና",
                    statutoryArticle = "አዋጅ 1356/2017 አንቀጽ 42"
                )
            )
        }

        // -------------------------------------------------------------
        // 7. Attached Documentation Dossier Check (degree, license, insurance files)
        // -------------------------------------------------------------
        val hasAttachedDegree = attachedDocsLower.contains("degree") || attachedDocsLower.contains("የትምህርት") || attachedDocsLower.contains("diploma")
        val hasAttachedLicense = attachedDocsLower.contains("license") || attachedDocsLower.contains("ፈቃድ") || attachedDocsLower.contains("mui")
        val hasAttachedInsurance = attachedDocsLower.contains("insurance") || attachedDocsLower.contains("ዋስትና") || attachedDocsLower.contains("policy")

        if (!hasAttachedLicense && !isLicenseBlank) {
            flags.add(
                DocumentVerificationFlag(
                    flagType = DocFlagType.MISSING_ATTACHED_LICENSE_FILE,
                    category = DocFlagCategory.MISSING_DOCUMENT,
                    severity = DocVerificationSeverity.WARNING,
                    documentName = "የፈቃድ ሰነድ ቅጂ (License Copy)",
                    statutoryClause = "አዋጅ 1356/2017 ማህደር መመሪያ",
                    title = "የፈቃድ ሰነድ ቅጂ አልተያያዘም (License File Missing)",
                    descriptionAmharic = "የሙያ ፈቃድ ቁጥር ቢመዘገብም የኦሪጅናል ሰርተፊኬቱ ዲጂታል ቅጂ (Scanned PDF) ወደ ማህደሩ አልተጫነም።",
                    remedialActionAmharic = "የሙያ ፈቃድ ምስክር ወረቀቱን ስካን አድርገው በማህደሩ ያያይዙ።",
                    isMissing = true
                )
            )
        }

        if (!hasAttachedInsurance && !isPolicyBlank) {
            flags.add(
                DocumentVerificationFlag(
                    flagType = DocFlagType.MISSING_ATTACHED_INSURANCE_FILE,
                    category = DocFlagCategory.MISSING_DOCUMENT,
                    severity = DocVerificationSeverity.WARNING,
                    documentName = "የኢንሹራንስ ፖሊሲ ሰነድ (PII Certificate)",
                    statutoryClause = "አዋጅ 1356/2017 ማህደር መመሪያ",
                    title = "የኢንሹራንስ ሰነድ ቅጂ አልተያያዘም (PII File Missing)",
                    descriptionAmharic = "የሙያ መድን ዋስትና ፖሊሲ ቁጥር ቢኖርም የመድን ሰጪው ማረጋገጫ ሰርተፊኬት ቅጂ አልተጫነም።",
                    remedialActionAmharic = "የመድን ፖሊሲውን ሰነድ ያያይዙ።",
                    isMissing = true
                )
            )
        }

        // -------------------------------------------------------------
        // Final Score & Status Computation
        // -------------------------------------------------------------
        val finalScore = scorePoints.coerceIn(0, 100)

        val hasExpiredDocs = flags.any { it.isExpired }
        val hasMissingDocs = flags.any { it.isMissing }
        val hasCriticalFlags = flags.any { it.severity == DocVerificationSeverity.CRITICAL }

        val expiredCount = flags.count { it.isExpired }
        val missingCount = flags.count { it.isMissing }
        val warningCount = flags.count { it.severity == DocVerificationSeverity.WARNING }

        val overallStatus = when {
            isLicenseSuspended -> "የታገደ (SUSPENDED)"
            hasExpiredDocs -> "ጊዜው ያለፈበት ሰነድ (EXPIRED DOCS)"
            hasMissingDocs -> "ያልተሟላ ሰነድ (MISSING DOCS)"
            daysUntilLicenseExpiry in 1..60 || daysUntilInsuranceExpiry in 1..30 || cpd in 20..29 -> "ማደስ ያለበት (RENEWAL DUE)"
            finalScore >= 80 -> "ተገዢ (COMPLIANT)"
            else -> "ማስተካከያ የሚሻ (FLAGGED)"
        }

        val summaryAmharic = buildString {
            if (flags.isEmpty()) {
                append("ሁሉም ህጋዊ ሰነዶች በአዋጅ 1356/2017 መሠረት የተሟሉና ተገዢ ናቸው።")
            } else {
                val parts = mutableListOf<String>()
                if (expiredCount > 0) parts.add("$expiredCount ጊዜው ያለፈበት ሰነድ")
                if (missingCount > 0) parts.add("$missingCount የጎደለ ሰነድ")
                if (warningCount > 0) parts.add("$warningCount የማስጠንቀቂያ ግኝት")
                append("በአዋጅ 1356/2017 ማጣሪያ መሠረት: ")
                append(parts.joinToString("፣ "))
                append(" ተከስቷል።")
            }
        }

        return ProfessionalVerificationReport(
            professionalId = professional.id,
            professionalName = professional.name,
            role = professional.role,
            grade = professional.grade,
            complianceScore = finalScore,
            statutoryStatus = overallStatus,
            isFullyCompliant = flags.isEmpty() && finalScore >= 90,
            hasExpiredDocs = hasExpiredDocs,
            hasMissingDocs = hasMissingDocs,
            hasCriticalFlags = hasCriticalFlags,
            expiredDocsCount = expiredCount,
            missingDocsCount = missingCount,
            warningCount = warningCount,
            flags = flags,
            documentChecks = checks,
            summaryAmharic = summaryAmharic,
            verificationTimestamp = now,
            professional = professional
        )
    }

    /**
     * Batch verification of an entire registry of professionals.
     */
    fun verifyAll(
        professionals: List<Professional>,
        now: Long = System.currentTimeMillis()
    ): List<ProfessionalVerificationReport> {
        return professionals.map { verifyProfessional(it, now) }
    }

    /**
     * Calculates system-wide verification indicators.
     */
    fun generateSystemSummary(
        professionals: List<Professional>,
        now: Long = System.currentTimeMillis()
    ): SystemVerificationSummary {
        val reports = verifyAll(professionals, now)
        val total = reports.size
        val fullyCompliant = reports.count { it.isFullyCompliant }
        val flagged = reports.count { it.flags.isNotEmpty() }
        val expiredDocs = reports.count { it.hasExpiredDocs }
        val missingDocs = reports.count { it.hasMissingDocs }
        val renewalDue = reports.count { it.statutoryStatus.contains("RENEWAL", ignoreCase = true) || it.statutoryStatus.contains("ማደስ") }
        val suspended = reports.count { it.statutoryStatus.contains("SUSPENDED", ignoreCase = true) || it.statutoryStatus.contains("የታገደ") }
        val avgScore = if (total > 0) reports.map { it.complianceScore }.average().toInt() else 100

        val criticalTotal = reports.sumOf { r -> r.flags.count { it.severity == DocVerificationSeverity.CRITICAL } }
        val highTotal = reports.sumOf { r -> r.flags.count { it.severity == DocVerificationSeverity.HIGH } }
        val warningTotal = reports.sumOf { r -> r.flags.count { it.severity == DocVerificationSeverity.WARNING } }

        val breakdown = mutableMapOf<DocFlagCategory, Int>()
        reports.flatMap { it.flags }.forEach { flag ->
            val cat = when (flag.category) {
                DocFlagCategory.LICENSE, DocFlagCategory.EXPIRED_DOCUMENT -> DocFlagCategory.LICENSE
                DocFlagCategory.INSURANCE -> DocFlagCategory.INSURANCE
                DocFlagCategory.CPD, DocFlagCategory.INADEQUATE_CPD -> DocFlagCategory.CPD
                DocFlagCategory.IDENTIFICATION -> DocFlagCategory.IDENTIFICATION
                else -> flag.category
            }
            breakdown[cat] = (breakdown[cat] ?: 0) + 1
        }

        return SystemVerificationSummary(
            totalProfessionals = total,
            fullyCompliantCount = fullyCompliant,
            flaggedProfessionalsCount = flagged,
            expiredDocsProfessionalsCount = expiredDocs,
            missingDocsProfessionalsCount = missingDocs,
            renewalDueCount = renewalDue,
            suspendedCount = suspended,
            averageComplianceScore = avgScore,
            criticalFlagsTotal = criticalTotal,
            highFlagsTotal = highTotal,
            warningFlagsTotal = warningTotal,
            reports = reports,
            totalProfessionalsScanned = total,
            breakdownByCategory = breakdown
        )
    }

    /**
     * Generates a formal, printable statutory defect/violation notice in Amharic under Law 1356/2017.
     */
    fun generateStatutoryViolationNotice(
        report: ProfessionalVerificationReport,
        institution: InstitutionProfile
    ): String {
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(report.verificationTimestamp))
        return buildString {
            appendLine("==================================================")
            appendLine("     ${institution.name.uppercase(Locale.getDefault())}")
            appendLine("  ${institution.department}")
            appendLine("  የአዋጅ ቁጥር 1356/2017 ዓ.ም የባለሙያዎች ሰነድ ማረጋገጫ ማስታወቂያ")
            appendLine("==================================================")
            appendLine("የተረጋገጠበት ቀን: $dateStr")
            appendLine("የባለሙያ ስም: ${report.professionalName}")
            appendLine("የስራ ድርሻ / ደረጃ: ${report.role} (${report.grade})")
            appendLine("የህግ ተገዢነት ነጥብ: ${report.complianceScore}%")
            appendLine("የምዝገባ ሁኔታ: ${report.statutoryStatus}")
            appendLine("--------------------------------------------------")
            appendLine("የተገኙ የህግና የሰነድ ጉድለቶች ማጠቃለያ:")
            if (report.flags.isEmpty()) {
                appendLine("• ምንም ዓይነት የጎደለ ወይም ጊዜው ያለፈበት ሰነድ አልተገኘም። ሙሉ በሙሉ በአዋጅ 1356/2017 መሠረት ተገዢ ነው።")
            } else {
                report.flags.forEachIndexed { idx, flag ->
                    val sevMarker = when (flag.severity) {
                        DocVerificationSeverity.CRITICAL -> "[አስቸኳይ/ክልክል]"
                        DocVerificationSeverity.HIGH -> "[ከፍተኛ ጉድለት]"
                        DocVerificationSeverity.MEDIUM -> "[መካከለኛ ጉድለት]"
                        DocVerificationSeverity.WARNING -> "[ማስጠንቀቂያ]"
                        else -> "[መረጃ]"
                    }
                    appendLine("${idx + 1}. $sevMarker ${flag.title}")
                    appendLine("   - ህጋዊ አንቀጽ: ${flag.statutoryClause}")
                    appendLine("   - የጉድለት ዝርዝር: ${flag.descriptionAmharic}")
                    appendLine("   - የማስተካከያ እርምጃ: ${flag.remedialActionAmharic}")
                }
            }
            appendLine("--------------------------------------------------")
            appendLine("የህግ ማሳሰቢያ:")
            appendLine("በአዋጅ 1356/2017 መሠረት የጎደሉ ወይም ጊዜያቸው ያለፈባቸውን ሰነዶች ሳያሟሉ በማንኛውም የግንባታ ፕሮጀክት ላይ መሳተፍ፣ ዲዛይን ማጽደቅ ወይም ፍተሻ ማከናወን የተከለከለና ቅጣት የሚያስከትል ነው።")
            appendLine("==================================================")
            appendLine("አረጋጋጭ መኮንን: ${institution.officerInCharge}")
            appendLine("ስልክ: ${institution.contactPhone} | መለያ: ${institution.licenseOrCode}")
        }
    }
}
