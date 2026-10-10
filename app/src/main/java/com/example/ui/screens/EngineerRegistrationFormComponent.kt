package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InstitutionProfile
import com.example.data.Professional
import com.example.data.Project
import com.example.ui.theme.*

/**
 * Standard Industry Formatting Validation Rules and Models
 * Aligned with:
 * - Ethiopian Building Proclamation No. 1356/2017 (Repealing 624/2001)
 * - Ministry of Urban and Infrastructure (MUI) Directives
 * - Ethiopian Construction Authority (ECA) Registration Guidelines
 * - FIDIC & ISO 9001/45001 Competency Frameworks
 */
object IndustryFormatValidators {

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null,
        val helperText: String? = null
    )

    // Full name: Must contain at least first and father name (2 words)
    fun validateFullName(name: String): ValidationResult {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            return ValidationResult(false, "ሙሉ ስም ማስገባት ግዴታ ነው (Full name is required)")
        }
        val words = trimmed.split("\\s+".toRegex())
        if (words.size < 2) {
            return ValidationResult(false, "ቢያንስ የስራተኛውንና የአባቱን ስም ያካቱ (Enter at least First & Father Name)")
        }
        return ValidationResult(true, helperText = "ትክክለኛ የሙሉ ስም ቅርጸት")
    }

    // Phone: Ethiopian format (+2519..., +2517..., 09..., 07...)
    fun validatePhone(phone: String): ValidationResult {
        val cleaned = phone.replace("\\s+".toRegex(), "").replace("-", "")
        if (cleaned.isBlank()) {
            return ValidationResult(false, "ስልክ ቁጥር ማስገባት ግዴታ ነው (Phone number required)")
        }
        val ethPhoneRegex = "^(\\+251|251|0)(9|7)[0-9]{8}$".toRegex()
        if (!cleaned.matches(ethPhoneRegex)) {
            return ValidationResult(
                false,
                "የተሳሳተ የስልክ ቁጥር ቅርጸት! ምሳሌ፡ 0911234567 ወይም +251911234567",
                "ቅርጸት፡ +251 9XX XXX XXX ወይም 09XXXXXXXX"
            )
        }
        return ValidationResult(true, helperText = "ተቀባይነት ያለው የኢትዮጵያ ስልክ ቁጥር")
    }

    // Email: Standard RFC email format
    fun validateEmail(email: String): ValidationResult {
        val trimmed = email.trim()
        if (trimmed.isBlank()) {
            return ValidationResult(true, helperText = "አማራጭ የመገናኛ ኢሜይል (Optional)")
        }
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}$".toRegex()
        if (!trimmed.matches(emailRegex)) {
            return ValidationResult(false, "የተሳሳተ የኢሜይል አድራሻ (Invalid email: user@domain.com)")
        }
        return ValidationResult(true, helperText = "ትክክለኛ የኢሜይል ቅርጸት")
    }

    // National Fayda ID: Standard Ethiopian Digital ID format: ET-FAN-XXXXXXXX or FAN-XXXXXXXXXXXX
    fun validateNationalId(faydaId: String): ValidationResult {
        val trimmed = faydaId.trim().uppercase()
        if (trimmed.isBlank()) {
            return ValidationResult(false, "የብሔራዊ ፋይዳ መታወቂያ ማስገባት ያስፈልጋል (Fayda ID required)")
        }
        // Accepts: ET-FAN-12345678, FAN-1234-5678-9012, or alphanumeric 8-16 chars
        val faydaRegex = "^(ET-FAN-[0-9]{6,12}|FAN-[0-9]{4}-[0-9]{4}-[0-9]{4}|ET-[0-9A-Z]{8,14})$".toRegex()
        if (!trimmed.matches(faydaRegex)) {
            return ValidationResult(
                false,
                "የተሳሳተ የፋይዳ መታወቂያ ቅርጸት! ምሳሌ፡ ET-FAN-98421048 ወይም FAN-1029-3847-5610",
                "ደረጃውን የጠበቀ የብሔራዊ ዲጂታል ፋይዳ መለያ"
            )
        }
        return ValidationResult(true, helperText = "ህጋዊ የብሔራዊ ፋይዳ ዲጂታል መለያ")
    }

    // Statutory Construction License Number
    // Standards:
    // MUI/[ROLE/GRADE]/[YEAR]/[NUM] e.g. MUI/PPE/2026/04821 or ECA/PE/2025/11904 or ETH-ENG-78291
    fun validateLicenseNumber(license: String): ValidationResult {
        val trimmed = license.trim().uppercase()
        if (trimmed.isBlank()) {
            return ValidationResult(false, "የሙያ ፈቃድ ቁጥር ማስገባት ግዴታ ነው (License number is required)")
        }
        val muiPattern = "^(MUI|ECA|MoID|MUDC)/[A-Z0-9-]{2,6}/[0-9]{4}/[0-9]{3,7}$".toRegex()
        val ethEngPattern = "^ETH-ENG-[0-9]{4,8}$".toRegex()
        val generalLicPattern = "^[A-Z]{2,4}/[0-9]{3,6}/[0-9]{2,4}$".toRegex()

        if (!trimmed.matches(muiPattern) && !trimmed.matches(ethEngPattern) && !trimmed.matches(generalLicPattern)) {
            return ValidationResult(
                false,
                "የተሳሳተ የፈቃድ ቁጥር ቅርጸት! ምሳሌ፡ MUI/PPE/2026/04821 ወይም ETH-ENG-84920",
                "ቅርጸት፡ MUI/[GRADE]/[YEAR]/[SEQUENCE]"
            )
        }
        return ValidationResult(true, helperText = "በሚኒስቴር መስሪያ ቤቱ ተቀባይነት ያለው የፈቃድ ቁጥር")
    }

    // MUI Central Registration Number: MUI-REG-XXXXXX
    fun validateMuiRegNumber(regNo: String): ValidationResult {
        val trimmed = regNo.trim().uppercase()
        if (trimmed.isBlank()) {
            return ValidationResult(true, helperText = "አማራጭ የማዕከላዊ መዝገብ ቁጥር (Optional MUI Reg No)")
        }
        val muiRegRegex = "^(MUI-REG-[0-9]{4,8}|ECA-REG-[0-9]{4,8})$".toRegex()
        if (!trimmed.matches(muiRegRegex)) {
            return ValidationResult(
                false,
                "የተሳሳተ ቅርጸት! ምሳሌ፡ MUI-REG-482910 ወይም ECA-REG-10492",
                "ቅርጸት፡ MUI-REG-XXXXXX"
            )
        }
        return ValidationResult(true, helperText = "ትክክለኛ የማዕከላዊ ምዝገባ ቁጥር")
    }

    // Professional Indemnity Insurance Policy: [COMPANY]-PI-[YEAR]-[NUM]
    fun validateInsurancePolicy(policy: String): ValidationResult {
        val trimmed = policy.trim().uppercase()
        if (trimmed.isBlank()) {
            return ValidationResult(false, "የሙያ ዋስትና መድን ፖሊሲ ማስገባት ግዴታ ነው (Insurance Policy required)")
        }
        val piRegex = "^[A-Z]{2,8}-PI-[0-9]{4}-[0-9]{4,8}$".toRegex()
        val genericPolicyRegex = "^INS-[A-Z0-9-]{6,16}$".toRegex()

        if (!trimmed.matches(piRegex) && !trimmed.matches(genericPolicyRegex)) {
            return ValidationResult(
                false,
                "የተሳሳተ የፖሊሲ ቁጥር ቅርጸት! ምሳሌ፡ NYALA-PI-2026-10492 ወይም INS-ETH-PI-9902",
                "ቅርጸት፡ [INSURER]-PI-[YEAR]-[POLICY_NO]"
            )
        }
        return ValidationResult(true, helperText = "የተረጋገጠ የሙያ ኃላፊነት ዋስትና መድን")
    }

    // Continuing Professional Development (CPD) Credits: Statutory 30 hrs/year
    fun validateCpdPoints(pointsText: String): ValidationResult {
        val points = pointsText.trim().toIntOrNull()
        if (points == null) {
            return ValidationResult(false, "የCPD ነጥብ በቁጥር ብቻ ያስገቡ (Enter numeric credit hours)")
        }
        if (points < 0 || points > 100) {
            return ValidationResult(false, "የCPD ነጥብ ከ0 እስከ 100 ሰዓት መሆን አለበት (Must be 0-100 credits)")
        }
        if (points < 30) {
            return ValidationResult(
                true,
                helperText = "ማሳሰቢያ፡ በኢትዮጵያ ሕግ ለፈቃድ እድሳት ቢያንስ 30 ሰዓት ያስፈልጋል (Min 30 credits recommended)"
            )
        }
        return ValidationResult(true, helperText = "ህጋዊውን የ30 ሰዓት የCPD መስፈርት ያሟላል (Fully compliant)")
    }

    // Cross-validation: Experience Years vs Grade Requirement
    fun validateExperienceVsGrade(grade: String, experienceYears: Int): ValidationResult {
        val minRequired = when {
            grade.contains("PPE") || grade.contains("Grade 1") -> 8
            grade.contains("PE") || grade.contains("Grade 2") -> 4
            grade.contains("CA-1") || grade.contains("አማካሪ") -> 6
            grade.contains("GC-1") || grade.contains("BC-1") -> 10
            else -> 0
        }

        if (experienceYears < minRequired) {
            return ValidationResult(
                false,
                "ለ$grade ቢያንስ $minRequired ዓመት የስራ ልምድ ያስፈልጋል! አሁን ያለው፡ $experienceYears ዓመት",
                "ዝቅተኛው የተደነገገው የስራ ልምድ፡ $minRequired ዓመት"
            )
        }
        return ValidationResult(true, helperText = "የደረጃው የልምድ መስፈርት ሙሉ በሙሉ ተሟልቷል ($minRequired+ ዓመት)")
    }

    // Specialized skills validation: At least 2 specialized skills recommended
    fun validateSkills(skills: List<String>): ValidationResult {
        if (skills.isEmpty()) {
            return ValidationResult(false, "ቢያንስ 1 ወይም 2 ልዩ የሙያ ክህሎቶች ይምረጡ (Select at least 1-2 skills)")
        }
        return ValidationResult(true, helperText = "${skills.size} ልዩ ክህሎቶች ተመርጠዋል")
    }
}

/**
 * Standard Catalog of Engineering Specialized Skills
 */
data class SkillDomain(
    val domainName: String,
    val amharicTitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val skills: List<String>
)

val STANDARD_ENGINEERING_SKILL_DOMAINS = listOf(
    SkillDomain(
        domainName = "Structural",
        amharicTitle = "መዋቅርና ዲዛይን (Structural Engineering)",
        icon = Icons.Default.Domain,
        skills = listOf(
            "ETABS 3D Seismic Modeling",
            "SAP2000 Structural Analysis",
            "SAFE Foundation Design",
            "EBCS 2 Reinforced Concrete",
            "EBCS 3 Structural Steel Design",
            "Eurocode 2 / 8 Seismic Analysis",
            "Bridge Structural Design",
            "Post-Tensioned Concrete (PT)"
        )
    ),
    SkillDomain(
        domainName = "BIM & Digital",
        amharicTitle = "ቢም እና ዲጂታል ቴክኖሎጂ (BIM & Digital)",
        icon = Icons.Default.Architecture,
        skills = listOf(
            "Autodesk Revit 3D/BIM",
            "AutoCAD 2D/3D Drafting",
            "Navisworks 4D Clash Detection",
            "Civil 3D Road Geometric Design",
            "GIS Spatial Mapping & Survey",
            "InfraWorks Infrastructure Model",
            "Dynamo BIM Automation"
        )
    ),
    SkillDomain(
        domainName = "Contract & FIDIC",
        amharicTitle = "ኮንትራትና ፕሮጀክት ማኔጅመንት (FIDIC & Contracts)",
        icon = Icons.Default.Gavel,
        skills = listOf(
            "FIDIC Red Book (1999/2017)",
            "FIDIC Yellow Book Design-Build",
            "PPA 2011 Standard Bidding",
            "Primavera P6 Professional",
            "MS Project Scheduling & CPM",
            "Claims Management & Dispute Boards",
            "Earned Value Management (EVM)"
        )
    ),
    SkillDomain(
        domainName = "Cost & QS",
        amharicTitle = "ዋጋ ዝርዝርና ግመታ (Quantity Surveying & Cost)",
        icon = Icons.Default.Calculate,
        skills = listOf(
            "Detailed Takeoff & BOQ Preparation",
            "Unit Rate Analysis & Pricing",
            "Price Escalation (FIDIC Cl. 13.8)",
            "Valuation of Variation Orders",
            "Subcontractor Interim Payments",
            "Value Engineering & Cost Optimization"
        )
    ),
    SkillDomain(
        domainName = "Geotech & Materials",
        amharicTitle = "ጂኦቴክኒካልና ቁሳቁስ (Geotechnical & Testing)",
        icon = Icons.Default.Layers,
        skills = listOf(
            "Soil Mechanics & Core Drilling",
            "Plate Load & SPT Borehole Testing",
            "Concrete Mix Design & Slump/Cube Test",
            "Asphalt Mix Design & Quality Control",
            "Slope Stability & Retaining Walls",
            "Pile Foundation Integrity Testing"
        )
    ),
    SkillDomain(
        domainName = "Safety & Quality",
        amharicTitle = "ሳይት ደህንነትና ጥራት (HSE & ISO Quality)",
        icon = Icons.Default.HealthAndSafety,
        skills = listOf(
            "OSHA 30 Construction Safety",
            "ISO 45001 Occupational Health & Safety",
            "ISO 9001 Quality Management System",
            "Site Safety Risk Assessment (JHA)",
            "Environmental Impact Assessment (EIA)",
            "Scaffolding & Fall Protection Audit"
        )
    )
)

/**
 * Modern, Industry-Compliant Engineer Registration Form Component
 */
@Composable
fun EngineerRegistrationFormComponent(
    projects: List<Project> = emptyList(),
    institutionProfile: InstitutionProfile? = null,
    initialProfessional: Professional? = null,
    onDismiss: () -> Unit,
    onSaveSuccess: (Professional) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Stepper / Wizard Tabs
    var currentStep by remember { mutableIntStateOf(0) }
    val stepTitles = listOf(
        "፩. መለያ መረጃ",
        "፪. የትምህርት ዝግጅት",
        "፫. ፈቃድና ኢንሹራንስ",
        "፬. ልዩ ክህሎቶች"
    )

    // Form Field States
    var fullName by remember { mutableStateOf(initialProfessional?.name ?: "") }
    var phone by remember { mutableStateOf(initialProfessional?.phone ?: "") }
    var email by remember { mutableStateOf(initialProfessional?.email ?: "") }
    var nationalId by remember { mutableStateOf(initialProfessional?.nationalId ?: "") }

    val roles = listOf(
        "የስራ ተቆጣጣሪ (Resident Engineer)",
        "የኮንስትራክሽን አማካሪ (Consultant)",
        "የስራ ተቋራጭ (Contractor Representative)",
        "ሳይት መሃንዲስ (Site Engineer)",
        "የደህንነትና ጤና መኮንን (HSE Officer)",
        "የስራ ዝርዝርና ዋጋ መሃንዲስ (Quantity Surveyor)",
        "የመዋቅር መሃንዲስ (Structural Engineer)",
        "የጂኦቴክኒክ መሃንዲስ (Geotechnical Engineer)"
    )
    var selectedRole by remember { mutableStateOf(initialProfessional?.role ?: roles.first()) }
    var expandedRole by remember { mutableStateOf(false) }

    val engineeringGrades = listOf(
        "ልምድ ያለው መሪ መሃንዲስ (PPE Grade 1)",
        "ተግባራዊ መሃንዲስ (PE Grade 2)",
        "ጀማሪ ምሩቅ መሃንዲስ (GE Grade 3)",
        "ረዳት ተቆጣጣሪ መሃንዲስ (AE Grade 4)",
        "አማካሪ መሃንዲስ ደረጃ ፩ (CA-1)",
        "ጠቅላላ ተቋራጭ ደረጃ ፩ (GC-1)",
        "የህንፃ ተቋራጭ ደረጃ ፩-፫ (BC 1-3)",
        "ልዩ ሙያተኛ (Specialist Consultant)"
    )
    var selectedGrade by remember { mutableStateOf(initialProfessional?.grade ?: engineeringGrades.first()) }
    var expandedGrade by remember { mutableStateOf(false) }

    var qualification by remember { mutableStateOf(initialProfessional?.qualification ?: "BSc in Civil Engineering") }
    var institutionGraduated by remember { mutableStateOf(initialProfessional?.institutionGraduated ?: "Addis Ababa University (AAiT)") }
    var graduationYearText by remember { mutableStateOf(initialProfessional?.graduationYear?.toString() ?: "2018") }
    var experienceYearsText by remember { mutableStateOf(initialProfessional?.experienceYears?.toString() ?: "6") }
    var fieldOfPractice by remember { mutableStateOf(initialProfessional?.fieldOfPractice ?: "ሲቪልና መዋቅር ምህንድስና (Civil & Structural)") }

    var licenseNumber by remember { mutableStateOf(initialProfessional?.licenseNumber ?: "") }
    var muiRegistrationNo by remember { mutableStateOf(initialProfessional?.muiRegistrationNo ?: "") }
    var insurancePolicyNo by remember { mutableStateOf(initialProfessional?.insurancePolicyNo ?: "") }
    var cpdPointsText by remember { mutableStateOf(initialProfessional?.cpdPoints?.toString() ?: "30") }

    val licenseStatuses = listOf("ተገዢ (COMPLIANT)", "እድሳት የሚሻ (PENDING)", "ጊዜው ያለፈበት (EXPIRED)", "የታገደ (SUSPENDED)")
    var selectedLicenseStatus by remember { mutableStateOf(initialProfessional?.licenseStatus ?: licenseStatuses.first()) }
    var expandedStatus by remember { mutableStateOf(false) }

    var assignedProjectRole by remember { mutableStateOf(initialProfessional?.assignedProjectRole ?: "ዋና የሳይት ተቆጣጣሪ መሃንዲስ") }
    var selectedProjectId by remember { mutableStateOf<Int?>(initialProfessional?.assignedProjectId) }
    var expandedProjectSelector by remember { mutableStateOf(false) }

    // Specialized Skills (Parsed from initial or empty)
    val initialSkillsList = remember {
        initialProfessional?.internationalCertifications?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: listOf("ETABS 3D Seismic Modeling", "FIDIC Red Book (1999/2017)", "OSHA 30 Construction Safety")
    }
    val selectedSkills = remember { mutableStateListOf<String>().apply { addAll(initialSkillsList) } }
    var customSkillInput by remember { mutableStateOf("") }

    // Validation Results Calculation
    val nameValidation = IndustryFormatValidators.validateFullName(fullName)
    val phoneValidation = IndustryFormatValidators.validatePhone(phone)
    val emailValidation = IndustryFormatValidators.validateEmail(email)
    val nationalIdValidation = IndustryFormatValidators.validateNationalId(nationalId)
    val licenseValidation = IndustryFormatValidators.validateLicenseNumber(licenseNumber)
    val muiRegValidation = IndustryFormatValidators.validateMuiRegNumber(muiRegistrationNo)
    val insuranceValidation = IndustryFormatValidators.validateInsurancePolicy(insurancePolicyNo)
    val cpdValidation = IndustryFormatValidators.validateCpdPoints(cpdPointsText)
    val expYears = experienceYearsText.toIntOrNull() ?: 0
    val expGradeValidation = IndustryFormatValidators.validateExperienceVsGrade(selectedGrade, expYears)
    val skillsValidation = IndustryFormatValidators.validateSkills(selectedSkills)

    // Calculate Overall Compliance Checklist Score
    val validationChecks = listOf(
        nameValidation.isValid,
        phoneValidation.isValid,
        emailValidation.isValid,
        nationalIdValidation.isValid,
        licenseValidation.isValid,
        insuranceValidation.isValid,
        cpdValidation.isValid,
        expGradeValidation.isValid,
        skillsValidation.isValid
    )
    val passedChecksCount = validationChecks.count { it }
    val totalChecksCount = validationChecks.size
    val compliancePercentage = (passedChecksCount.toFloat() / totalChecksCount.toFloat() * 100).toInt()
    val isFormReadyToSubmit = nameValidation.isValid && phoneValidation.isValid && nationalIdValidation.isValid && licenseValidation.isValid

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, AmharicYellow.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(ConstructionTeal.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = "Engineer Badge",
                            tint = AmharicYellow,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "የመሃንዲሶች የሙያ ፈቃድና ምዝገባ ሰነድ",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        )
                        Text(
                            text = "በአዲሱ አዋጅ 1356/2017 እና FIDIC/ISO ስታንዳርዶች መሠረት",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = AmharicYellow,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Sample Autofill Button to test formatting quickly
                IconButton(
                    onClick = {
                        fullName = "ኢንጂነር ዳዊት ሃይሉ ወልደማሪያም"
                        phone = "0911482910"
                        email = "dawit.hailu@construction.et"
                        nationalId = "ET-FAN-98410294"
                        selectedRole = "የስራ ተቆጣጣሪ (Resident Engineer)"
                        selectedGrade = "ልምድ ያለው መሪ መሃንዲስ (PPE Grade 1)"
                        qualification = "MSc in Structural Engineering"
                        institutionGraduated = "Addis Ababa University (AAiT)"
                        graduationYearText = "2015"
                        experienceYearsText = "11"
                        licenseNumber = "MUI/PPE/2026/04821"
                        muiRegistrationNo = "MUI-REG-849201"
                        insurancePolicyNo = "NYALA-PI-2026-10492"
                        cpdPointsText = "35"
                        selectedLicenseStatus = "ተገዢ (COMPLIANT)"
                        if (!selectedSkills.contains("ETABS 3D Seismic Modeling")) selectedSkills.add("ETABS 3D Seismic Modeling")
                        if (!selectedSkills.contains("FIDIC Red Book (1999/2017)")) selectedSkills.add("FIDIC Red Book (1999/2017)")
                        if (!selectedSkills.contains("OSHA 30 Construction Safety")) selectedSkills.add("OSHA 30 Construction Safety")
                        Toast.makeText(context, "ደረጃውን የጠበቀ የናሙና መረጃ ተሞልቷል!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = "Autofill Industry Sample",
                        tint = AmharicYellow
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Overall Compliance Progress Bar & Status Badge
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = SteelGray.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, if (compliancePercentage >= 80) AmharicGreen.copy(alpha = 0.5f) else SafetyOrange.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = if (compliancePercentage >= 80) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = if (compliancePercentage >= 80) AmharicGreen else SafetyOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "የህግና የፈቃድ ተገዢነት ምጣኔ፡ $compliancePercentage%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "$passedChecksCount/$totalChecksCount መመዘኛዎች",
                            fontSize = 11.sp,
                            color = if (compliancePercentage >= 80) AmharicGreen else AmharicYellow,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { compliancePercentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (compliancePercentage >= 80) AmharicGreen else if (compliancePercentage >= 50) AmharicYellow else SafetyOrange,
                        trackColor = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Wizard Step Navigation Tabs
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(stepTitles.indices.toList()) { index ->
                    val isSelected = currentStep == index
                    Surface(
                        modifier = Modifier
                            .clickable { currentStep = index }
                            .testTag("step_tab_$index"),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) AmharicYellow else SteelGray,
                        border = BorderStroke(1.dp, if (isSelected) AmharicYellow else Color.Gray.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = stepTitles[index],
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) SteelGray else Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(10.dp))

            // Step Content inside scrollable body
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
            ) {
                when (currentStep) {
                    0 -> Step1PersonalIdentitySection(
                        fullName = fullName,
                        onFullNameChange = { fullName = it },
                        nameValidation = nameValidation,
                        phone = phone,
                        onPhoneChange = { phone = it },
                        phoneValidation = phoneValidation,
                        email = email,
                        onEmailChange = { email = it },
                        emailValidation = emailValidation,
                        nationalId = nationalId,
                        onNationalIdChange = { nationalId = it },
                        nationalIdValidation = nationalIdValidation,
                        focusManager = focusManager
                    )

                    1 -> Step2CredentialsAndGradeSection(
                        qualification = qualification,
                        onQualificationChange = { qualification = it },
                        institutionGraduated = institutionGraduated,
                        onInstitutionChange = { institutionGraduated = it },
                        graduationYearText = graduationYearText,
                        onGraduationYearChange = { graduationYearText = it },
                        experienceYearsText = experienceYearsText,
                        onExperienceYearsChange = { experienceYearsText = it },
                        expGradeValidation = expGradeValidation,
                        fieldOfPractice = fieldOfPractice,
                        onFieldChange = { fieldOfPractice = it },
                        selectedRole = selectedRole,
                        onRoleChange = { selectedRole = it },
                        roles = roles,
                        expandedRole = expandedRole,
                        onExpandedRoleChange = { expandedRole = it },
                        selectedGrade = selectedGrade,
                        onGradeChange = { selectedGrade = it },
                        grades = engineeringGrades,
                        expandedGrade = expandedGrade,
                        onExpandedGradeChange = { expandedGrade = it }
                    )

                    2 -> Step3LicensesAndInsuranceSection(
                        licenseNumber = licenseNumber,
                        onLicenseNumberChange = { licenseNumber = it },
                        licenseValidation = licenseValidation,
                        muiRegistrationNo = muiRegistrationNo,
                        onMuiRegChange = { muiRegistrationNo = it },
                        muiRegValidation = muiRegValidation,
                        insurancePolicyNo = insurancePolicyNo,
                        onInsuranceChange = { insurancePolicyNo = it },
                        insuranceValidation = insuranceValidation,
                        cpdPointsText = cpdPointsText,
                        onCpdPointsChange = { cpdPointsText = it },
                        cpdValidation = cpdValidation,
                        selectedLicenseStatus = selectedLicenseStatus,
                        onStatusChange = { selectedLicenseStatus = it },
                        licenseStatuses = licenseStatuses,
                        expandedStatus = expandedStatus,
                        onExpandedStatusChange = { expandedStatus = it },
                        projects = projects,
                        selectedProjectId = selectedProjectId,
                        onProjectIdChange = { selectedProjectId = it },
                        assignedProjectRole = assignedProjectRole,
                        onAssignedRoleChange = { assignedProjectRole = it },
                        expandedProjectSelector = expandedProjectSelector,
                        onExpandedProjectSelectorChange = { expandedProjectSelector = it }
                    )

                    3 -> Step4SpecializedSkillsSection(
                        selectedSkills = selectedSkills,
                        onToggleSkill = { skill ->
                            if (selectedSkills.contains(skill)) {
                                selectedSkills.remove(skill)
                            } else {
                                selectedSkills.add(skill)
                            }
                        },
                        customSkillInput = customSkillInput,
                        onCustomSkillInputChange = { customSkillInput = it },
                        onAddCustomSkill = {
                            val trimmed = customSkillInput.trim()
                            if (trimmed.isNotBlank() && !selectedSkills.contains(trimmed)) {
                                selectedSkills.add(trimmed)
                                customSkillInput = ""
                            }
                        },
                        skillsValidation = skillsValidation
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(12.dp))

            // Footer Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back / Cancel Button
                if (currentStep > 0) {
                    OutlinedButton(
                        onClick = { currentStep -= 1 },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.Gray),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ወደ ኋላ", fontSize = 12.sp, color = Color.White)
                    }
                } else {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.Gray),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("ይቅር", fontSize = 12.sp, color = Color.White)
                    }
                }

                // Next / Submit Button
                if (currentStep < 3) {
                    Button(
                        onClick = { currentStep += 1 },
                        colors = ButtonDefaults.buttonColors(containerColor = ConstructionTeal),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("ቀጣይ ደረጃ", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                } else {
                    Button(
                        onClick = {
                            if (!isFormReadyToSubmit) {
                                Toast.makeText(
                                    context,
                                    "እባክዎ አስፈላጊ የሆኑትን የፈቃድና መለያ መረጃዎች በትክክለኛ ቅርጸት ያሟሉ!",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@Button
                            }

                            val gradYear = graduationYearText.toIntOrNull() ?: 2018
                            val cpdPts = cpdPointsText.toIntOrNull() ?: 30
                            val skillsJoined = selectedSkills.joinToString(", ")

                            val registeredProf = (initialProfessional ?: Professional(
                                name = fullName.trim(),
                                phone = phone.trim(),
                                role = selectedRole,
                                grade = selectedGrade,
                                licenseNumber = licenseNumber.trim()
                            )).copy(
                                name = fullName.trim(),
                                phone = phone.trim(),
                                email = email.trim(),
                                nationalId = nationalId.trim(),
                                role = selectedRole,
                                grade = selectedGrade,
                                licenseNumber = licenseNumber.trim(),
                                qualification = qualification.trim(),
                                institutionGraduated = institutionGraduated.trim(),
                                graduationYear = gradYear,
                                experienceYears = expYears,
                                fieldOfPractice = fieldOfPractice.trim(),
                                specialization = fieldOfPractice.trim(),
                                law1356ComplianceStatus = selectedLicenseStatus,
                                authorizedBuildingCategories = when {
                                    selectedGrade.contains("Grade 1", ignoreCase = true) || selectedGrade.contains("ደረጃ ፩") || selectedGrade.contains("CA-1") || selectedGrade.contains("PPE") -> "ምድብ ሀ፣ ለ፣ ሐ እና ሜጋ (All Categories & Mega)"
                                    selectedGrade.contains("Grade 2") || selectedGrade.contains("ደረጃ ፪") || selectedGrade.contains("Grade 3") || selectedGrade.contains("ደረጃ ፫") -> "ምድብ ሀ እና ለ (Categories A & B)"
                                    else -> "ምድብ ሀ (Category A)"
                                },
                                muiRegistrationNo = muiRegistrationNo.trim(),
                                licenseStatus = selectedLicenseStatus,
                                cpdPoints = cpdPts,
                                insurancePolicyNo = insurancePolicyNo.trim(),
                                internationalCertifications = skillsJoined,
                                assignedProjectId = selectedProjectId,
                                assignedProjectRole = assignedProjectRole.trim(),
                                complianceRating = (compliancePercentage / 20f).coerceIn(3.0f, 5.0f),
                                lastAccreditationUpdate = System.currentTimeMillis()
                            )

                            onSaveSuccess(registeredProf)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFormReadyToSubmit) AmharicGreen else Color.Gray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (initialProfessional != null) "ማህደር አዘምን" else "በህግ መሠረት መዝግብ",
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Section 1: Personal & National Identity
 */
@Composable
private fun Step1PersonalIdentitySection(
    fullName: String,
    onFullNameChange: (String) -> Unit,
    nameValidation: IndustryFormatValidators.ValidationResult,
    phone: String,
    onPhoneChange: (String) -> Unit,
    phoneValidation: IndustryFormatValidators.ValidationResult,
    email: String,
    onEmailChange: (String) -> Unit,
    emailValidation: IndustryFormatValidators.ValidationResult,
    nationalId: String,
    onNationalIdChange: (String) -> Unit,
    nationalIdValidation: IndustryFormatValidators.ValidationResult,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "፩. የግልና ብሔራዊ መለያ መረጃ (Personal & National Identity)",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = AmharicYellow
        )

        // Full Name Field
        ValidatedInputField(
            value = fullName,
            onValueChange = onFullNameChange,
            label = "ሙሉ ስም (የአባትና የአያት ስም)",
            placeholder = "ምሳሌ፡ ኢንጂነር ታሪኩ በቀለ ገብሬ",
            validation = nameValidation,
            leadingIcon = Icons.Default.Person,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
        )

        // Phone Number Field
        ValidatedInputField(
            value = phone,
            onValueChange = onPhoneChange,
            label = "ስልክ ቁጥር (የኢትዮጵያ ቅርጸት)",
            placeholder = "0911234567 ወይም +251911234567",
            validation = phoneValidation,
            leadingIcon = Icons.Default.Phone,
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
        )

        // National Digital Fayda ID
        ValidatedInputField(
            value = nationalId,
            onValueChange = onNationalIdChange,
            label = "የብሔራዊ ፋይዳ መታወቂያ (Fayda Digital ID)",
            placeholder = "ET-FAN-98410294 ወይም FAN-1029-3847-5610",
            validation = nationalIdValidation,
            leadingIcon = Icons.Default.CreditCard,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
        )

        // Email Address (Optional)
        ValidatedInputField(
            value = email,
            onValueChange = onEmailChange,
            label = "ኦፊሴላዊ የኢሜይል አድራሻ (Official Email)",
            placeholder = "engineer.name@domain.com",
            validation = emailValidation,
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
            onImeAction = { focusManager.clearFocus() }
        )
    }
}

/**
 * Section 2: Academic Credentials, Field & Statutory Grade
 */
@Composable
private fun Step2CredentialsAndGradeSection(
    qualification: String,
    onQualificationChange: (String) -> Unit,
    institutionGraduated: String,
    onInstitutionChange: (String) -> Unit,
    graduationYearText: String,
    onGraduationYearChange: (String) -> Unit,
    experienceYearsText: String,
    onExperienceYearsChange: (String) -> Unit,
    expGradeValidation: IndustryFormatValidators.ValidationResult,
    fieldOfPractice: String,
    onFieldChange: (String) -> Unit,
    selectedRole: String,
    onRoleChange: (String) -> Unit,
    roles: List<String>,
    expandedRole: Boolean,
    onExpandedRoleChange: (Boolean) -> Unit,
    selectedGrade: String,
    onGradeChange: (String) -> Unit,
    grades: List<String>,
    expandedGrade: Boolean,
    onExpandedGradeChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "፪. የትምህርት ዝግጅትና የሙያ ማዕረግ (Academic & Credentials)",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = AmharicYellow
        )

        // Role Dropdown
        Text("የሙያ ኃላፊነት ምድብ (Professional Function):", fontSize = 11.sp, color = Color.LightGray)
        Box {
            OutlinedButton(
                onClick = { onExpandedRoleChange(true) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.Gray)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedRole, color = Color.White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AmharicYellow)
                }
            }
            DropdownMenu(expanded = expandedRole, onDismissRequest = { onExpandedRoleChange(false) }) {
                roles.forEach { r ->
                    DropdownMenuItem(
                        text = { Text(r, fontSize = 12.sp) },
                        onClick = {
                            onRoleChange(r)
                            onExpandedRoleChange(false)
                        }
                    )
                }
            }
        }

        // Statutory Grade Dropdown
        Text("የመንግስት የሙያ ደረጃ (Statutory Engineering Grade):", fontSize = 11.sp, color = Color.LightGray)
        Box {
            OutlinedButton(
                onClick = { onExpandedGradeChange(true) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (!expGradeValidation.isValid) SafetyOrange else AmharicGreen)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedGrade, color = Color.White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AmharicYellow)
                }
            }
            DropdownMenu(expanded = expandedGrade, onDismissRequest = { onExpandedGradeChange(false) }) {
                grades.forEach { g ->
                    DropdownMenuItem(
                        text = { Text(g, fontSize = 12.sp) },
                        onClick = {
                            onGradeChange(g)
                            onExpandedGradeChange(false)
                        }
                    )
                }
            }
        }

        // Experience Years vs Grade Warning Display
        if (!expGradeValidation.isValid) {
            Surface(
                color = SafetyOrange.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, SafetyOrange),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = SafetyOrange, modifier = Modifier.size(16.dp))
                    Text(
                        text = expGradeValidation.errorMessage ?: "",
                        color = SafetyOrange,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = experienceYearsText,
                onValueChange = onExperienceYearsChange,
                label = { Text("የስራ ልምድ (ዓመት)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AmharicYellow,
                    unfocusedBorderColor = Color.Gray
                ),
                shape = RoundedCornerShape(8.dp)
            )

            OutlinedTextField(
                value = graduationYearText,
                onValueChange = onGraduationYearChange,
                label = { Text("የምረቃ ዓመተ ምህረት") },
                placeholder = { Text("2018") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AmharicYellow,
                    unfocusedBorderColor = Color.Gray
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }

        OutlinedTextField(
            value = qualification,
            onValueChange = onQualificationChange,
            label = { Text("የትምህርት ደረጃና ዲግሪ (BSc / MSc / PhD)") },
            placeholder = { Text("BSc in Civil Engineering") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AmharicYellow,
                unfocusedBorderColor = Color.Gray
            ),
            shape = RoundedCornerShape(8.dp)
        )

        OutlinedTextField(
            value = institutionGraduated,
            onValueChange = onInstitutionChange,
            label = { Text("የተመረቀበት ዩኒቨርሲቲ / ተቋም") },
            placeholder = { Text("Addis Ababa University (AAiT)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AmharicYellow,
                unfocusedBorderColor = Color.Gray
            ),
            shape = RoundedCornerShape(8.dp)
        )

        OutlinedTextField(
            value = fieldOfPractice,
            onValueChange = onFieldChange,
            label = { Text("የምህንድስና የስራ መስክ (Discipline)") },
            placeholder = { Text("ሲቪልና መዋቅር ምህንድስና (Civil & Structural)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AmharicYellow,
                unfocusedBorderColor = Color.Gray
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

/**
 * Section 3: Construction Licenses, MUI Registration, Insurance & CPD
 */
@Composable
private fun Step3LicensesAndInsuranceSection(
    licenseNumber: String,
    onLicenseNumberChange: (String) -> Unit,
    licenseValidation: IndustryFormatValidators.ValidationResult,
    muiRegistrationNo: String,
    onMuiRegChange: (String) -> Unit,
    muiRegValidation: IndustryFormatValidators.ValidationResult,
    insurancePolicyNo: String,
    onInsuranceChange: (String) -> Unit,
    insuranceValidation: IndustryFormatValidators.ValidationResult,
    cpdPointsText: String,
    onCpdPointsChange: (String) -> Unit,
    cpdValidation: IndustryFormatValidators.ValidationResult,
    selectedLicenseStatus: String,
    onStatusChange: (String) -> Unit,
    licenseStatuses: List<String>,
    expandedStatus: Boolean,
    onExpandedStatusChange: (Boolean) -> Unit,
    projects: List<Project>,
    selectedProjectId: Int?,
    onProjectIdChange: (Int?) -> Unit,
    assignedProjectRole: String,
    onAssignedRoleChange: (String) -> Unit,
    expandedProjectSelector: Boolean,
    onExpandedProjectSelectorChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "፫. የግንባታ ፈቃድና ኢንሹራንስ (Licenses & Statutory Compliance)",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = AmharicYellow
        )

        // License Number Field
        ValidatedInputField(
            value = licenseNumber,
            onValueChange = onLicenseNumberChange,
            label = "የኮንስትራክሽን ፈቃድ ቁጥር (Practicing License)",
            placeholder = "MUI/PPE/2026/04821 ወይም ETH-ENG-78291",
            validation = licenseValidation,
            leadingIcon = Icons.Default.Verified
        )

        // MUI Central Registration
        ValidatedInputField(
            value = muiRegistrationNo,
            onValueChange = onMuiRegChange,
            label = "የከተማና መሠረተ ልማት ማዕከላዊ ምዝገባ ቁጥር (MUI Reg No)",
            placeholder = "MUI-REG-849201",
            validation = muiRegValidation,
            leadingIcon = Icons.Default.AccountBalance
        )

        // Professional Indemnity Insurance Policy
        ValidatedInputField(
            value = insurancePolicyNo,
            onValueChange = onInsuranceChange,
            label = "የሙያ ኃላፊነት ዋስትና መድን ፖሊሲ (PI Insurance Policy)",
            placeholder = "NYALA-PI-2026-10492 ወይም INS-ETH-PI-9902",
            validation = insuranceValidation,
            leadingIcon = Icons.Default.Security
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // CPD Points
            Column(modifier = Modifier.weight(1f)) {
                ValidatedInputField(
                    value = cpdPointsText,
                    onValueChange = onCpdPointsChange,
                    label = "የCPD ነጥብ (በዓመት)",
                    placeholder = "30",
                    validation = cpdValidation,
                    leadingIcon = Icons.Default.MilitaryTech,
                    keyboardType = KeyboardType.Number
                )
            }

            // License Status Dropdown
            Column(modifier = Modifier.weight(1f)) {
                Text("የፈቃድ ሁኔታ፡", fontSize = 11.sp, color = Color.LightGray)
                Spacer(modifier = Modifier.height(4.dp))
                Box {
                    OutlinedButton(
                        onClick = { onExpandedStatusChange(true) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.Gray)
                    ) {
                        Text(
                            text = selectedLicenseStatus,
                            color = if (selectedLicenseStatus.contains("COMPLIANT")) AmharicGreen else SafetyOrange,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    DropdownMenu(expanded = expandedStatus, onDismissRequest = { onExpandedStatusChange(false) }) {
                        licenseStatuses.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s, fontSize = 12.sp) },
                                onClick = {
                                    onStatusChange(s)
                                    onExpandedStatusChange(false)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Assigned Project
        if (projects.isNotEmpty()) {
            Text("የተመደበበት የኮንስትራክሽን ፕሮጀክት (Assigned Project):", fontSize = 11.sp, color = Color.LightGray)
            val currentProjectName = projects.find { it.id == selectedProjectId }?.name ?: "ምንም ፕሮጀክት አልተመረጠም"

            Box {
                OutlinedButton(
                    onClick = { onExpandedProjectSelectorChange(true) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.Gray)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(currentProjectName, color = Color.White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AmharicYellow)
                    }
                }
                DropdownMenu(
                    expanded = expandedProjectSelector,
                    onDismissRequest = { onExpandedProjectSelectorChange(false) }
                ) {
                    DropdownMenuItem(
                        text = { Text("ምንም ፕሮጀክት አልተመረጠም (Unassigned)", fontSize = 12.sp) },
                        onClick = {
                            onProjectIdChange(null)
                            onExpandedProjectSelectorChange(false)
                        }
                    )
                    projects.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p.name, fontSize = 12.sp) },
                            onClick = {
                                onProjectIdChange(p.id)
                                onExpandedProjectSelectorChange(false)
                            }
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = assignedProjectRole,
            onValueChange = onAssignedRoleChange,
            label = { Text("የሳይት የኃላፊነት ቦታ (Assigned Site Designation)") },
            placeholder = { Text("ዋና የሳይት ተቆጣጣሪ መሃንዲስ (Lead Resident Engineer)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AmharicYellow,
                unfocusedBorderColor = Color.Gray
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

/**
 * Section 4: Specialized Engineering Skills & Tools (ETABS, Revit, FIDIC, OSHA)
 */
@Composable
private fun Step4SpecializedSkillsSection(
    selectedSkills: List<String>,
    onToggleSkill: (String) -> Unit,
    customSkillInput: String,
    onCustomSkillInputChange: (String) -> Unit,
    onAddCustomSkill: () -> Unit,
    skillsValidation: IndustryFormatValidators.ValidationResult
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "፬. ልዩ የምህንድስና ክህሎቶች (Specialized Skills)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = AmharicYellow
            )
            Surface(
                color = if (skillsValidation.isValid) AmharicGreen.copy(alpha = 0.2f) else SafetyOrange.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, if (skillsValidation.isValid) AmharicGreen else SafetyOrange),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "${selectedSkills.size} ክህሎቶች ተመርጠዋል",
                    color = if (skillsValidation.isValid) AmharicGreen else SafetyOrange,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Text(
            text = "በኢትዮጵያና ዓለም አቀፍ የኮንስትራክሽን ፕሮጀክቶች ተፈላጊ የሆኑ የሶፍትዌር፣ የኮንትራትና የሳይት ክህሎቶችን ይምረጡ፡",
            fontSize = 11.sp,
            color = Color.LightGray
        )

        // Custom Skill Addition Field
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = customSkillInput,
                onValueChange = onCustomSkillInputChange,
                label = { Text("ተጨማሪ ልዩ ክህሎት ያክሉ (Custom Skill)") },
                placeholder = { Text("ምሳሌ፡ Plaxis 2D, Lean Construction") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AmharicYellow,
                    unfocusedBorderColor = Color.Gray
                ),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onAddCustomSkill() })
            )

            Button(
                onClick = onAddCustomSkill,
                colors = ButtonDefaults.buttonColors(containerColor = ConstructionTeal),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Skill", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("አክል", fontSize = 11.sp)
            }
        }

        // Categorized Skill Domains
        STANDARD_ENGINEERING_SKILL_DOMAINS.forEach { domain ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SteelGray.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(domain.icon, contentDescription = null, tint = AmharicYellow, modifier = Modifier.size(16.dp))
                        Text(
                            text = domain.amharicTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Flow of Skill Chips
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        domain.skills.chunked(2).forEach { skillRow ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                skillRow.forEach { skill ->
                                    val isSelected = selectedSkills.contains(skill)
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onToggleSkill(skill) },
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) AmharicGreen.copy(alpha = 0.25f) else Color.DarkGray.copy(alpha = 0.5f),
                                        border = BorderStroke(1.dp, if (isSelected) AmharicGreen else Color.Gray.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = skill,
                                                fontSize = 10.sp,
                                                color = if (isSelected) Color.White else Color.LightGray,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = AmharicGreen,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                if (skillRow.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Reusable Validated Input Field Component with Live Visual Status Indicator
 */
@Composable
private fun ValidatedInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    validation: IndustryFormatValidators.ValidationResult,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    onImeAction: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isError = !validation.isValid && value.isNotBlank()

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, fontSize = 12.sp) },
            placeholder = { Text(placeholder, fontSize = 11.sp, color = Color.Gray) },
            leadingIcon = {
                Icon(
                    leadingIcon,
                    contentDescription = null,
                    tint = if (isError) SafetyOrange else if (validation.isValid && value.isNotBlank()) AmharicGreen else Color.LightGray,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (value.isNotBlank()) {
                    if (validation.isValid) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Valid Format",
                            tint = AmharicGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = "Format Error",
                            tint = SafetyOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(onAny = { onImeAction() }),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = if (isError) SafetyOrange else AmharicYellow,
                unfocusedBorderColor = if (isError) SafetyOrange else if (validation.isValid && value.isNotBlank()) AmharicGreen else Color.Gray,
                errorBorderColor = SafetyOrange
            ),
            shape = RoundedCornerShape(8.dp)
        )

        // Helper / Error message text
        if (isError && validation.errorMessage != null) {
            Text(
                text = validation.errorMessage,
                color = SafetyOrange,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 6.dp, top = 2.dp)
            )
        } else if (validation.helperText != null && value.isNotBlank()) {
            Text(
                text = validation.helperText,
                color = if (validation.isValid) AmharicGreen else Color.Gray,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 6.dp, top = 2.dp)
            )
        }
    }
}
