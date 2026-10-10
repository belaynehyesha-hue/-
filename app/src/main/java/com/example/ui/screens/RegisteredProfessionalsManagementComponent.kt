package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.viewmodel.GovernmentRegulation
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * UI List & Management Component for Registered Construction Professionals
 * Built strictly according to Ethiopian Building Proclamation No. 1356/2017 ዓ.ም
 * Manages:
 * - Practicing Licenses (Ministry of Urban and Infrastructure / ECA)
 * - Professional Specializations (Civil/Structural, Architecture, Geotechnical, MEP, COTM/QS, HSE)
 * - Statutory Compliance Status (COMPLIANT, RENEWAL_DUE, EXPIRED, SUSPENDED)
 * - CPD (Continuing Professional Development) 30 credits/year statutory tracking
 * - Professional Indemnity Insurance (PII) policies
 * - Authorized Building Categories (Category A, B, C, Mega Projects)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisteredProfessionalsManagementComponent(
    professionals: List<Professional>,
    projects: List<Project> = emptyList(),
    institutionProfile: InstitutionProfile = InstitutionProfile.DEFAULT,
    activeRegulation: GovernmentRegulation? = null,
    onAddProfClick: () -> Unit,
    onDeleteProf: (Professional) -> Unit,
    onUpdateProf: (Professional) -> Unit,
    onViewDossier: (Professional) -> Unit = {},
    onNavigateToAudit: () -> Unit = {}
) {
    val context = LocalContext.current

    // State for filtering & searching
    var searchQuery by remember { mutableStateOf("") }
    var selectedSpecialization by remember { mutableStateOf("ሁሉም") }
    var selectedComplianceStatus by remember { mutableStateOf("ሁሉም") }
    var selectedDocFlagFilter by remember { mutableStateOf("ሁሉም") }
    var selectedRole by remember { mutableStateOf("ሁሉም") }
    var selectedBuildingCategory by remember { mutableStateOf("ሁሉም") }
    var geezFilterLetter by remember { mutableStateOf("ሁሉም") }

    // Quick License Renewal / Status Update Dialog State
    var profToQuickUpdate by remember { mutableStateOf<Professional?>(null) }
    var profToDeleteConfirm by remember { mutableStateOf<Professional?>(null) }
    var profToVerifyDocs by remember { mutableStateOf<Professional?>(null) }
    var showBatchAuditDialog by remember { mutableStateOf(false) }

    // System-wide Statutory Document Verification Summary (Law 1356/2017)
    val systemVerificationSummary = remember(professionals) {
        Law1356VerificationEngine.generateSystemSummary(professionals)
    }

    // Specializations under Ethiopian Building Law 1356/2017 & Directives
    val specializations = listOf(
        "ሁሉም",
        "መዋቅርና ሲቪል",
        "አርክቴክቸር",
        "ጂኦቴክኒክ",
        "ኤሌክትሮ-መካኒክ",
        "ኮንስትራክሽን ማኔጅመንት",
        "ደህንነትና ጤና (HSE)",
        "የሕንፃ ፍተሻ"
    )

    // Compliance statuses under Law 1356/2017
    val complianceStatuses = listOf(
        "ሁሉም",
        "ተገዢ (COMPLIANT)",
        "ማደስ ያለበት (RENEWAL DUE)",
        "ጊዜው ያለፈበት (EXPIRED)",
        "የታገደ (SUSPENDED)"
    )

    // Statutory Document Verification Flag Filters under Law 1356/2017
    val docFlagFilters = listOf(
        "ሁሉም",
        "የተከሱ ሰነዶች (Flagged)",
        "ጊዜ ያለፈባቸው (Expired Docs)",
        "የጎደሉ ሰነዶች (Missing Docs)",
        "ተገዢ/የተሟሉ (Compliant)"
    )

    // Roles
    val roles = listOf("ሁሉም", "የስራ ተቆጣጣሪ", "የኮንስትራክሽን አማካሪ", "የስራ ተቋራጭ")

    // Authorized Building Categories under Law 1356/2017
    val buildingCategories = listOf("ሁሉም", "ምድብ ሀ (Cat A)", "ምድብ ለ (Cat B)", "ምድብ ሐ (Cat C)", "ሜጋ ፕሮጀክት")

    // Statistics calculations
    val totalCount = professionals.size
    val compliantCount = professionals.count { it.isCompliantWithLaw1356 }
    val renewalDueCount = professionals.count { it.isRenewalDue }
    val expiredOrSuspendedCount = professionals.count {
        !it.isCompliantWithLaw1356 && !it.isRenewalDue
    }
    val avgCpd = if (totalCount > 0) professionals.map { it.cpdPoints }.average().toInt() else 0
    val complianceRate = if (totalCount > 0) (compliantCount * 100) / totalCount else 100

    // Filtered Professionals List
    val filteredProfessionals = remember(
        professionals,
        searchQuery,
        selectedSpecialization,
        selectedComplianceStatus,
        selectedDocFlagFilter,
        selectedRole,
        selectedBuildingCategory,
        geezFilterLetter
    ) {
        professionals.filter { prof ->
            // Search query matches name, license, specialization, or national ID
            val matchesQuery = searchQuery.isBlank() ||
                    prof.name.contains(searchQuery, ignoreCase = true) ||
                    prof.licenseNumber.contains(searchQuery, ignoreCase = true) ||
                    prof.specialization.contains(searchQuery, ignoreCase = true) ||
                    prof.fieldOfPractice.contains(searchQuery, ignoreCase = true) ||
                    prof.nationalId.contains(searchQuery, ignoreCase = true) ||
                    prof.phone.contains(searchQuery, ignoreCase = true)

            // Specialization match
            val matchesSpecialization = if (selectedSpecialization == "ሁሉም") {
                true
            } else {
                prof.specialization.contains(selectedSpecialization, ignoreCase = true) ||
                        prof.fieldOfPractice.contains(selectedSpecialization, ignoreCase = true)
            }

            // Compliance status match
            val matchesCompliance = when (selectedComplianceStatus) {
                "ተገዢ (COMPLIANT)" -> prof.isCompliantWithLaw1356
                "ማደስ ያለበት (RENEWAL DUE)" -> prof.isRenewalDue
                "ጊዜው ያለፈበት (EXPIRED)" -> prof.licenseStatus.contains("EXPIRED", ignoreCase = true) ||
                        prof.licenseStatus.contains("ያለፈበት") ||
                        (prof.licenseExpiryDate <= System.currentTimeMillis() && !prof.licenseStatus.contains("SUSPENDED", ignoreCase = true))
                "የታገደ (SUSPENDED)" -> prof.licenseStatus.contains("SUSPENDED", ignoreCase = true) ||
                        prof.licenseStatus.contains("የታገደ")
                else -> true
            }

            // Law 1356/2017 Document Flag Filter
            val docReport = Law1356VerificationEngine.verifyProfessional(prof)
            val matchesDocFlag = when (selectedDocFlagFilter) {
                "የተከሱ ሰነዶች (Flagged)" -> docReport.flags.isNotEmpty()
                "ጊዜ ያለፈባቸው (Expired Docs)" -> docReport.hasExpiredDocs
                "የጎደሉ ሰነዶች (Missing Docs)" -> docReport.hasMissingDocs
                "ተገዢ/የተሟሉ (Compliant)" -> docReport.isFullyCompliant
                else -> true
            }

            // Role match
            val matchesRole = selectedRole == "ሁሉም" || prof.role == selectedRole

            // Building Category match
            val matchesCategory = when (selectedBuildingCategory) {
                "ምድብ ሀ (Cat A)" -> prof.authorizedBuildingCategories.contains("ሀ", ignoreCase = true) || prof.authorizedBuildingCategories.contains("Category A", ignoreCase = true)
                "ምድብ ለ (Cat B)" -> prof.authorizedBuildingCategories.contains("ለ", ignoreCase = true) || prof.authorizedBuildingCategories.contains("Category B", ignoreCase = true)
                "ምድብ ሐ (Cat C)" -> prof.authorizedBuildingCategories.contains("ሐ", ignoreCase = true) || prof.authorizedBuildingCategories.contains("Category C", ignoreCase = true)
                "ሜጋ ፕሮጀክት" -> prof.authorizedBuildingCategories.contains("ሜጋ", ignoreCase = true) || prof.authorizedBuildingCategories.contains("Mega", ignoreCase = true)
                else -> true
            }

            // Ge'ez letter match
            val matchesGeez = if (geezFilterLetter == "ሁሉም") {
                true
            } else {
                val targetChar = geezFilterLetter.firstOrNull()
                if (targetChar == null) true
                else {
                    val family = getGeezFamily(targetChar)
                    prof.name.firstOrNull() in family
                }
            }

            matchesQuery && matchesSpecialization && matchesCompliance && matchesDocFlag && matchesRole && matchesCategory && matchesGeez
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSlate)
            .padding(16.dp)
    ) {
        // Top Action Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "የኮንስትራክሽን ባለሙያዎች መዝገብ",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 17.sp
                    )
                )
                Text(
                    text = "በአዲሱ የኢትዮጵያ ሕንፃ አዋጅ 1356/2017 ዓ.ም እና MUI መመሪያ መሠረት",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = AmharicYellow,
                        fontSize = 11.sp
                    )
                )
            }

            Button(
                onClick = onAddProfClick,
                colors = ButtonDefaults.buttonColors(containerColor = ConstructionTeal),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("አዲስ መዝግብ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Statutory 1356/2017 Compliance Dashboard Overview Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, SteelGray)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = AmharicYellow, modifier = Modifier.size(18.dp))
                        Text(
                            text = "የሕግ ተገዢነት ኦዲት (Proclamation 1356/2017)",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Direct Audit Link
                    Text(
                        text = "ሙሉ ሪፖርት ማውጣት >",
                        color = AmharicYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onNavigateToAudit() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total Registered
                    StatMetricTile(
                        label = "ድምር ባለሙያዎች",
                        value = "$totalCount",
                        subtext = "በመዝገብ ላይ",
                        accentColor = Color.White,
                        modifier = Modifier.weight(1f)
                    )

                    // Compliant
                    StatMetricTile(
                        label = "ተገዢ (1356/2017)",
                        value = "$compliantCount ($complianceRate%)",
                        subtext = "ፈቃድና ኢንሹራንስ ያለ",
                        accentColor = AmharicGreen,
                        modifier = Modifier.weight(1.3f)
                    )

                    // Renewal Due / Warning
                    StatMetricTile(
                        label = "ማደስ ያለበት",
                        value = "$renewalDueCount",
                        subtext = "<60 ቀናት ቀሪ",
                        accentColor = AmharicYellow,
                        modifier = Modifier.weight(1f)
                    )

                    // Expired or Suspended
                    StatMetricTile(
                        label = "ያልተገዢ/ያለፈበት",
                        value = "$expiredOrSuspendedCount",
                        subtext = "መታገድ ያለበት",
                        accentColor = if (expiredOrSuspendedCount > 0) AmharicRed else Color.LightGray,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Mandatory CPD & Statutory Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(SteelGray.copy(alpha = 0.4f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "• ዓመታዊ የሙያ ማሻሻያ (CPD) አማካይ: $avgCpd/30 ነጥብ (የአዋጁ ዝቅተኛ ግዴታ)",
                        color = if (avgCpd >= 30) AmharicGreen else AmharicYellow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "ሙያዊ ዋስትና (PII): ግዴታ",
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Automated Statutory Document Verification Action Banner (Law 1356/2017)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showBatchAuditDialog = true },
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(
                1.2.dp,
                if (systemVerificationSummary.flaggedProfessionalsCount > 0) AmharicYellow.copy(alpha = 0.7f) else AmharicGreen.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                if (systemVerificationSummary.flaggedProfessionalsCount > 0) AmharicYellow.copy(alpha = 0.2f) else AmharicGreen.copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (systemVerificationSummary.flaggedProfessionalsCount > 0) Icons.Default.Warning else Icons.Default.Verified,
                            contentDescription = null,
                            tint = if (systemVerificationSummary.flaggedProfessionalsCount > 0) AmharicYellow else AmharicGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "አውቶማቲክ የሰነዶች ማረጋገጫ (Law 1356/2017 Audit)",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = if (systemVerificationSummary.flaggedProfessionalsCount > 0) AmharicYellow.copy(alpha = 0.2f) else AmharicGreen.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (systemVerificationSummary.flaggedProfessionalsCount > 0) "${systemVerificationSummary.flaggedProfessionalsCount} ተከስተዋል" else "ሙሉ ተገዢ",
                                    color = if (systemVerificationSummary.flaggedProfessionalsCount > 0) AmharicYellow else AmharicGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = if (systemVerificationSummary.flaggedProfessionalsCount > 0)
                                "ጊዜያቸው ያለፈባቸው: ${systemVerificationSummary.expiredDocsProfessionalsCount} • የጎደሉ ሰነዶች: ${systemVerificationSummary.missingDocsProfessionalsCount} (ጠቅ ያድርጉ)"
                            else
                                "ሁሉም ባለሙያዎች የተሟላ የሙያ ፈቃድ፣ ኢንሹራንስና ፋይዳ አላቸው",
                            color = Color.LightGray,
                            fontSize = 10.sp
                        )
                    }
                }

                Button(
                    onClick = { showBatchAuditDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (systemVerificationSummary.flaggedProfessionalsCount > 0) SafetyOrange else ConstructionTeal
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ኦዲት ፈትሽ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Input Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text("በስም፣ በፈቃድ ቁጥር፣ በልዩ ሙያ ወይም በፋይዳ መለያ ይፈልጉ...", color = Color.Gray, fontSize = 12.sp)
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AmharicYellow,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Multi-dimensional Filter Rows
        // 1. Specialization Row
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "የሙያ ዘርፍ (Specialization):",
                color = Color.LightGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(specializations) { spec ->
                    val isSelected = spec == selectedSpecialization
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSpecialization = spec },
                        label = { Text(spec, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmharicYellow.copy(alpha = 0.2f),
                            selectedLabelColor = AmharicYellow,
                            containerColor = CardBackground,
                            labelColor = Color.LightGray
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) AmharicYellow else Color.DarkGray
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 2. Compliance Status Row (Law 1356/2017)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "የህግ ተገዢነት (Law 1356/2017 Compliance):",
                color = Color.LightGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(complianceStatuses) { status ->
                    val isSelected = status == selectedComplianceStatus
                    val chipColor = when {
                        status.contains("COMPLIANT") || status.contains("ተገዢ") -> AmharicGreen
                        status.contains("RENEWAL") || status.contains("ማደስ") -> AmharicYellow
                        status.contains("EXPIRED") || status.contains("ያለፈበት") -> SafetyOrange
                        status.contains("SUSPENDED") || status.contains("የታገደ") -> AmharicRed
                        else -> ConstructionTeal
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedComplianceStatus = status },
                        label = { Text(status, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipColor.copy(alpha = 0.25f),
                            selectedLabelColor = chipColor,
                            containerColor = CardBackground,
                            labelColor = Color.LightGray
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) chipColor else Color.DarkGray
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 2b. Statutory Document Flags Filter Row (Law 1356/2017)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "የሰነዶች ኦዲት ግኝት (Document Flags):",
                color = Color.LightGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(docFlagFilters) { flagFilter ->
                    val isSelected = flagFilter == selectedDocFlagFilter
                    val chipColor = when {
                        flagFilter.contains("ጊዜ ያለፈባቸው") -> AmharicRed
                        flagFilter.contains("የጎደሉ") -> AmharicYellow
                        flagFilter.contains("የተከሱ") -> SafetyOrange
                        flagFilter.contains("ተገዢ") -> AmharicGreen
                        else -> ConstructionTeal
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDocFlagFilter = flagFilter },
                        label = { Text(flagFilter, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipColor.copy(alpha = 0.25f),
                            selectedLabelColor = chipColor,
                            containerColor = CardBackground,
                            labelColor = Color.LightGray
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) chipColor else Color.DarkGray
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 3. Authorized Building Category Filter (Category A, B, C, Mega)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "የተፈቀደ ሕንፃ ምድብ:",
                color = Color.LightGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(buildingCategories) { cat ->
                    val isSelected = cat == selectedBuildingCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) ConstructionTeal.copy(alpha = 0.25f) else Color.Transparent)
                            .border(1.dp, if (isSelected) ConstructionTeal else Color.DarkGray, RoundedCornerShape(6.dp))
                            .clickable { selectedBuildingCategory = cat }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 9.sp,
                            color = if (isSelected) Color.White else Color.Gray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Ge'ez Index Bar
        GeezAlphabetFilterBar(
            selectedLetter = geezFilterLetter,
            onLetterSelected = { geezFilterLetter = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // List Header / Count Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "የተገኙ ባለሙያዎች: ${filteredProfessionals.size}",
                color = Color.LightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            if (selectedSpecialization != "ሁሉም" || selectedComplianceStatus != "ሁሉም" || selectedRole != "ሁሉም" || selectedBuildingCategory != "ሁሉም" || geezFilterLetter != "ሁሉም" || searchQuery.isNotEmpty()) {
                Text(
                    text = "ማጣሪያዎችን አጽዳ",
                    color = AmharicYellow,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .clickable {
                            searchQuery = ""
                            selectedSpecialization = "ሁሉም"
                            selectedComplianceStatus = "ሁሉም"
                            selectedRole = "ሁሉም"
                            selectedBuildingCategory = "ሁሉም"
                            geezFilterLetter = "ሁሉም"
                        }
                        .padding(4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Professionals List or Empty State
        if (filteredProfessionals.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SteelGray)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "በተመረጠው መስፈርት የተመዘገበ ባለሙያ አልተገኘም",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "የፍለጋ ቃሉን ወይም የሙያና የተገዢነት ማጣሪያዎችን ይቀይሩ፣ ወይም አዲስ ባለሙያ በአዋጅ 1356/2017 መሠረት ይመዝግቡ።",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onAddProfClick,
                            colors = ButtonDefaults.buttonColors(containerColor = ConstructionTeal),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("አዲስ ባለሙያ መዝግብ", fontSize = 11.sp)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProfessionals, key = { it.id }) { prof ->
                    Law1356ProfessionalCard(
                        prof = prof,
                        onViewDossier = { onViewDossier(prof) },
                        onQuickRenew = { profToQuickUpdate = prof },
                        onVerifyDocs = { profToVerifyDocs = prof },
                        onDelete = { profToDeleteConfirm = prof },
                        onShareSlip = {
                            shareProfessionalComplianceSlip(context, prof, institutionProfile)
                        }
                    )
                }
            }
        }
    }

    // Dialog: Quick License Renewal & Compliance Status Update
    profToQuickUpdate?.let { prof ->
        QuickLicenseComplianceDialog(
            professional = prof,
            onDismiss = { profToQuickUpdate = null },
            onSave = { updated ->
                onUpdateProf(updated)
                profToQuickUpdate = null
                Toast.makeText(context, "${updated.name} የፈቃድና የተገዢነት ሁኔታ ተዘምኗል!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog: Law 1356/2017 Comprehensive Statutory Document Verification Dialog
    profToVerifyDocs?.let { prof ->
        Law1356DocumentVerificationDialog(
            professional = prof,
            institutionProfile = institutionProfile,
            onDismiss = { profToVerifyDocs = null },
            onUpdateProfessional = { updated ->
                onUpdateProf(updated)
                profToVerifyDocs = updated
            },
            onOpenQuickRenew = {
                profToQuickUpdate = prof
                profToVerifyDocs = null
            }
        )
    }

    // Dialog: Batch Statutory Verification Audit (Bureau / System Wide)
    if (showBatchAuditDialog) {
        BatchStatutoryAuditDialog(
            professionals = professionals,
            institutionProfile = institutionProfile,
            onDismiss = { showBatchAuditDialog = false },
            onSelectProfessional = { prof ->
                showBatchAuditDialog = false
                profToVerifyDocs = prof
            },
            onSyncAllCompliance = {
                var syncCount = 0
                professionals.forEach { prof ->
                    val r = Law1356VerificationEngine.verifyProfessional(prof)
                    if (r.statutoryStatus != prof.licenseStatus || r.isFullyCompliant != prof.isCompliantWithLaw1356) {
                        val synced = prof.copy(
                            licenseStatus = r.statutoryStatus,
                            law1356ComplianceStatus = r.statutoryStatus
                        )
                        onUpdateProf(synced)
                        syncCount++
                    }
                }
                Toast.makeText(context, "የ $syncCount ባለሙያዎች የህግ ተገዢነት ሁኔታ በአዋጅ 1356/2017 መሰረት ተመሳስሏል! ⚖️", Toast.LENGTH_LONG).show()
                showBatchAuditDialog = false
            }
        )
    }

    // Dialog: Delete Confirmation
    profToDeleteConfirm?.let { prof ->
        AlertDialog(
            onDismissRequest = { profToDeleteConfirm = null },
            title = {
                Text("የባለሙያ መዝገብ ይሰረዝ?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            },
            text = {
                Text(
                    "ባለሙያ '${prof.name}' (የፈቃድ ቁጥር: ${prof.licenseNumber}) ከመዝገብ ላይ በቋሚነት እንዲሰረዝ ይፈልጋሉ?",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProf(prof)
                        profToDeleteConfirm = null
                        Toast.makeText(context, "ባለሙያው ከመዝገብ ተሰርዟል", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmharicRed)
                ) {
                    Text("አዎ ሰርዝ", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { profToDeleteConfirm = null }) {
                    Text("ይቅር", color = Color.LightGray)
                }
            },
            containerColor = CardBackground,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

/**
 * Individual Professional Card Displaying Licenses, Specialization, and Law 1356/2017 Compliance
 */
@Composable
fun Law1356ProfessionalCard(
    prof: Professional,
    onViewDossier: () -> Unit,
    onQuickRenew: () -> Unit,
    onVerifyDocs: () -> Unit,
    onDelete: () -> Unit,
    onShareSlip: () -> Unit
) {
    val isCompliant = prof.isCompliantWithLaw1356
    val isRenewalDue = prof.isRenewalDue
    val isExpiredOrSuspended = !isCompliant && !isRenewalDue

    // Statutory Documentation Audit Report under Law 1356/2017
    val docReport = remember(prof) { Law1356VerificationEngine.verifyProfessional(prof) }

    // Status colors
    val statusBorderColor = when {
        isCompliant -> AmharicGreen
        isRenewalDue -> AmharicYellow
        prof.licenseStatus.contains("SUSPENDED", ignoreCase = true) -> AmharicRed
        else -> SafetyOrange
    }

    val statusBadgeBg = statusBorderColor.copy(alpha = 0.15f)

    val roleIcon = when (prof.role) {
        "የስራ ተቆጣጣሪ" -> Icons.Default.VerifiedUser
        "የኮንስትራክሽን አማካሪ" -> Icons.Default.Business
        else -> Icons.Default.Engineering
    }

    val roleTint = when (prof.role) {
        "የስራ ተቆጣጣሪ" -> AmharicRed
        "የኮንስትራክሽን አማካሪ" -> AmharicYellow
        else -> ConstructionTeal
    }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val expiryDateStr = remember(prof.licenseExpiryDate) {
        dateFormat.format(Date(prof.licenseExpiryDate))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDossier() },
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.2.dp, statusBorderColor.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Avatar, Name, Specialization, and Compliance Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(roleTint.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, roleTint.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = roleIcon,
                            contentDescription = prof.role,
                            tint = roleTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = prof.name,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (prof.nationalId.isNotBlank()) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Fayda Verified",
                                    tint = AmharicGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        // Specialization Tag
                        Text(
                            text = if (prof.specialization.isNotBlank()) prof.specialization else prof.fieldOfPractice,
                            color = AmharicYellow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "${prof.role} • ${prof.grade}",
                            color = Color.LightGray,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Law 1356/2017 Compliance Pill Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBadgeBg)
                        .border(1.dp, statusBorderColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                isCompliant -> Icons.Default.CheckCircle
                                isRenewalDue -> Icons.Default.Schedule
                                else -> Icons.Default.Warning
                            },
                            contentDescription = null,
                            tint = statusBorderColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = when {
                                isCompliant -> "ተገዢ"
                                isRenewalDue -> "ማደስ ያለበት"
                                prof.licenseStatus.contains("SUSPENDED", ignoreCase = true) -> "የታገደ"
                                else -> "ጊዜው ያለፈበት"
                            },
                            color = statusBorderColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // License Information & Authorized Building Categories
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SteelGray.copy(alpha = 0.35f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.2f)) {
                    Text(
                        text = "የሙያ ፈቃድ ቁጥር (License No):",
                        color = Color.Gray,
                        fontSize = 9.sp
                    )
                    Text(
                        text = prof.licenseNumber.ifBlank { "MUI/REG/2017-PENDING" },
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(modifier = Modifier.weight(1.4f)) {
                    Text(
                        text = "የተፈቀደ የህንፃ ምድብ (1356/2017):",
                        color = Color.Gray,
                        fontSize = 9.sp
                    )
                    Text(
                        text = prof.authorizedBuildingCategories.ifBlank { "ምድብ ሀ፣ ለ፣ ሐ (A, B, C)" },
                        color = ConstructionTeal,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3-Metric Statutory Compliance Strip: CPD Points, License Expiry, Insurance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Metric 1: CPD Points (Target: 30)
                ComplianceMetricPill(
                    icon = Icons.Default.School,
                    title = "CPD ነጥብ",
                    value = "${prof.cpdPoints}/30",
                    isPassing = prof.cpdPoints >= 30,
                    modifier = Modifier.weight(1f)
                )

                // Metric 2: License Expiry Days Left
                ComplianceMetricPill(
                    icon = Icons.Default.DateRange,
                    title = "ቀሪ ጊዜ",
                    value = if (prof.licenseExpiryDate > System.currentTimeMillis()) "${prof.daysUntilExpiry} ቀናት" else "ያለፈበት",
                    isPassing = prof.licenseExpiryDate > System.currentTimeMillis(),
                    modifier = Modifier.weight(1f)
                )

                // Metric 3: Insurance (PII) Policy
                ComplianceMetricPill(
                    icon = Icons.Default.Security,
                    title = "ኢንሹራንስ",
                    value = if (prof.insurancePolicyNo.isNotBlank()) "የተገባ" else "የለም",
                    isPassing = prof.insurancePolicyNo.isNotBlank(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Statutory Document Verification Status Banner (Law 1356/2017)
            if (docReport.flags.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onVerifyDocs() },
                    color = if (docReport.hasCriticalFlags) AmharicRed.copy(alpha = 0.15f) else AmharicYellow.copy(alpha = 0.15f),
                    border = BorderStroke(
                        1.dp,
                        if (docReport.hasCriticalFlags) AmharicRed.copy(alpha = 0.6f) else AmharicYellow.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (docReport.hasCriticalFlags) Icons.Default.Error else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (docReport.hasCriticalFlags) AmharicRed else AmharicYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = when {
                                    docReport.hasExpiredDocs -> "🔴 ጊዜ ያለፈበት ሰነድ ተከስቷል (${docReport.expiredDocsCount})"
                                    docReport.hasMissingDocs -> "🟡 የጎደለ ሰነድ ተከስቷል (${docReport.missingDocsCount})"
                                    else -> "⚠️ ${docReport.flags.size} የሰነድ ጉድለቶች ተገኝተዋል"
                                },
                                color = if (docReport.hasCriticalFlags) AmharicRed else AmharicYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = "ዝርዝር ፍተሻ >",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onVerifyDocs() },
                    color = AmharicGreen.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, AmharicGreen.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AmharicGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "ሰነዶች በአዋጅ 1356/2017 የተረጋገጡ ናቸው (${docReport.complianceScore}%)",
                                color = AmharicGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "ይመልከቱ >",
                            color = Color.LightGray,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Action Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary actions: Share and Delete
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onShareSlip,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share Compliance Slip",
                            tint = Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = AmharicRed.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Primary actions: Quick Renew / Status & Full Dossier & Verify Docs
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onVerifyDocs,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (docReport.flags.isNotEmpty()) AmharicYellow else AmharicGreen
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (docReport.flags.isNotEmpty()) AmharicYellow.copy(alpha = 0.6f) else AmharicGreen.copy(alpha = 0.5f)
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = if (docReport.flags.isNotEmpty()) Icons.Default.Warning else Icons.Default.Verified,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (docReport.flags.isNotEmpty()) "ሰነድ ፈትሽ (${docReport.flags.size})" else "ሰነድ ፈትሽ",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onQuickRenew,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AmharicYellow),
                        border = BorderStroke(1.dp, AmharicYellow.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ፈቃድ አድስ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onViewDossier,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SteelGray),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ማህደር", fontSize = 10.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * Metric summary pill inside professional card
 */
@Composable
private fun ComplianceMetricPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    isPassing: Boolean,
    modifier: Modifier = Modifier
) {
    val accent = if (isPassing) AmharicGreen else SafetyOrange
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(accent.copy(alpha = 0.1f))
            .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(11.dp))
                Text(text = title, color = Color.Gray, fontSize = 9.sp)
            }
            Text(
                text = value,
                color = accent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Quick Metric Tile for the top statutory overview bar
 */
@Composable
private fun StatMetricTile(
    label: String,
    value: String,
    subtext: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SteelGray.copy(alpha = 0.3f))
            .border(1.dp, SteelGray.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(text = label, color = Color.Gray, fontSize = 9.sp, maxLines = 1)
            Text(
                text = value,
                color = accentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(text = subtext, color = Color.DarkGray, fontSize = 8.sp, maxLines = 1)
        }
    }
}

/**
 * Quick License Renewal & Compliance Status Dialog
 * Allows immediate renewal (+1 or +2 years), logging CPD credits, and compliance status updates
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickLicenseComplianceDialog(
    professional: Professional,
    onDismiss: () -> Unit,
    onSave: (Professional) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(professional.licenseStatus) }
    var cpdPointsInput by remember { mutableStateOf(professional.cpdPoints.toString()) }
    var insurancePolicyInput by remember { mutableStateOf(professional.insurancePolicyNo) }
    var authorizedCategoryInput by remember { mutableStateOf(professional.authorizedBuildingCategories) }
    var expiryDaysToAdd by remember { mutableStateOf(365L) } // 1 Year statutory renewal

    val statuses = listOf(
        "ተገዢ (COMPLIANT)",
        "ማደስ ያለበት (RENEWAL DUE)",
        "ጊዜው ያለፈበት (EXPIRED)",
        "የታገደ (SUSPENDED)"
    )

    val categories = listOf(
        "ምድብ ሀ፣ ለ፣ ሐ እና ሜጋ (All Categories & Mega)",
        "ምድብ ሀ እና ለ (Categories A & B)",
        "ምድብ ሀ (Category A)"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, AmharicYellow.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Autorenew, contentDescription = null, tint = AmharicYellow)
                        Text(
                            text = "ፈቃድና ተገዢነት ማደሻ",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Text(
                    text = "${professional.name} • ፈቃድ: ${professional.licenseNumber}",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // License Renewal Extension Buttons (+1 Year, +2 Years)
                Text(
                    text = "የፈቃድ ማደሻ ዘመን (Proclamation 1356/2017):",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            expiryDaysToAdd = 365L
                            selectedStatus = "ተገዢ (COMPLIANT)"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (expiryDaysToAdd == 365L) AmharicGreen else SteelGray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+1 ዓመት አድስ", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            expiryDaysToAdd = 730L
                            selectedStatus = "ተገዢ (COMPLIANT)"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (expiryDaysToAdd == 730L) AmharicGreen else SteelGray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+2 ዓመት አድስ", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // CPD Points Section (Statutory 30 points)
                Text(
                    text = "የCPD ሰዓት ነጥብ (አሁን: ${professional.cpdPoints} | ግዴታ: 30):",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = cpdPointsInput,
                        onValueChange = { cpdPointsInput = it },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AmharicYellow,
                            unfocusedBorderColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Quick CPD additions
                    Button(
                        onClick = {
                            val current = cpdPointsInput.toIntOrNull() ?: 0
                            cpdPointsInput = (current + 10).toString()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ConstructionTeal),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+10 ነጥብ", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // License Compliance Status Radio Options
                Text(
                    text = "የህግ ተገዢነት ሁኔታ (Compliance Status):",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    statuses.forEach { status ->
                        val isSelected = selectedStatus == status
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) SteelGray.copy(alpha = 0.5f) else Color.Transparent)
                                .clickable { selectedStatus = status }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedStatus = status },
                                colors = RadioButtonDefaults.colors(selectedColor = AmharicYellow)
                            )
                            Text(
                                text = status,
                                color = if (isSelected) Color.White else Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Insurance Policy Number
                Text(
                    text = "የሙያ ዋስትና ፖሊሲ (PII Insurance No):",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = insurancePolicyInput,
                    onValueChange = { insurancePolicyInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AmharicYellow,
                        unfocusedBorderColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("ይቅር", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val newCpd = cpdPointsInput.toIntOrNull() ?: professional.cpdPoints
                            val newExpiry = System.currentTimeMillis() + (expiryDaysToAdd * 24 * 3600 * 1000L)
                            val updated = professional.copy(
                                licenseStatus = selectedStatus,
                                law1356ComplianceStatus = selectedStatus,
                                cpdPoints = newCpd,
                                licenseExpiryDate = newExpiry,
                                insurancePolicyNo = insurancePolicyInput.trim(),
                                authorizedBuildingCategories = authorizedCategoryInput.trim(),
                                lastAccreditationUpdate = System.currentTimeMillis()
                            )
                            onSave(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("መዝገብ አዘምን", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Share verified compliance certification slip for Ethiopian Building Proclamation 1356/2017
 */
fun shareProfessionalComplianceSlip(
    context: android.content.Context,
    prof: Professional,
    institution: InstitutionProfile
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val issueDateStr = dateFormat.format(Date(prof.issueDate))
    val expiryDateStr = dateFormat.format(Date(prof.licenseExpiryDate))

    val slip = buildString {
        appendLine("==================================================")
        appendLine(" ${institution.name}")
        appendLine(" ${institution.department}")
        appendLine(" የኮንስትራክሽን ባለሙያ ህጋዊ ተገዢነት ማረጋገጫ ወረቀት")
        appendLine(" በአዲሱ የኢትዮጵያ ሕንፃ አዋጅ 1356/2017 ዓ.ም መሠረት")
        appendLine("==================================================")
        appendLine("• የባለሙያ ሙሉ ስም: ${prof.name}")
        appendLine("• የሙያ ኃላፊነት: ${prof.role}")
        appendLine("• የሙያ ደረጃ (Grade): ${prof.grade}")
        appendLine("• ልዩ ሙያ (Specialization): ${prof.specialization.ifBlank { prof.fieldOfPractice }}")
        appendLine("• የፈቃድ ቁጥር (License No): ${prof.licenseNumber}")
        appendLine("• የፋይዳ/ብሔራዊ መለያ: ${prof.nationalId.ifBlank { "የተረጋገጠ" }}")
        appendLine("• የተፈቀደ የሕንፃ ምድብ: ${prof.authorizedBuildingCategories}")
        appendLine("• የፈቃድ ማብቂያ ቀን: $expiryDateStr")
        appendLine("• የተገዢነት ሁኔታ: ${if (prof.isCompliantWithLaw1356) "ተገዢ (COMPLIANT)" else prof.licenseStatus}")
        appendLine("• የCPD ነጥብ ሰዓት: ${prof.cpdPoints}/30 ነጥብ")
        appendLine("• የሙያ ዋስትና ፖሊሲ (PII): ${prof.insurancePolicyNo}")
        appendLine("• ዓለም አቀፍ የምስክር ወረቀት: ${prof.internationalCertifications}")
        appendLine("==================================================")
        appendLine("ይህ ማረጋገጫ በ${institution.name} (${institution.regionOrCity}) ሲስተም በይፋ የተረጋገጠ ነው።")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "የባለሙያ ህጋዊ ተገዢነት ማረጋገጫ - ${prof.name}")
        putExtra(Intent.EXTRA_TEXT, slip)
    }
    context.startActivity(Intent.createChooser(intent, "የባለሙያ ህጋዊ ተገዢነት ወረቀት አጋራ"))
}

/**
 * Detailed Law 1356/2017 Statutory Document Verification Dialog
 * Comprehensive audit inspector highlighting expired or missing documentation,
 * providing direct one-click remedial actions, and generating official legal notices.
 */
@Composable
fun Law1356DocumentVerificationDialog(
    professional: Professional,
    institutionProfile: InstitutionProfile,
    onDismiss: () -> Unit,
    onUpdateProfessional: (Professional) -> Unit,
    onOpenQuickRenew: () -> Unit
) {
    val context = LocalContext.current
    var currentProf by remember(professional) { mutableStateOf(professional) }
    val report = remember(currentProf) { Law1356VerificationEngine.verifyProfessional(currentProf) }
    val scrollState = rememberScrollState()

    val roleTint = when (currentProf.role) {
        "የስራ ተቆጣጣሪ" -> AmharicRed
        "የኮንስትራክሽን አማካሪ" -> AmharicYellow
        else -> ConstructionTeal
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, if (report.flags.isNotEmpty()) AmharicYellow.copy(alpha = 0.8f) else AmharicGreen.copy(alpha = 0.8f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header: Title and Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(if (report.flags.isNotEmpty()) AmharicYellow.copy(alpha = 0.2f) else AmharicGreen.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (report.flags.isNotEmpty()) Icons.Default.Warning else Icons.Default.Verified,
                                contentDescription = null,
                                tint = if (report.flags.isNotEmpty()) AmharicYellow else AmharicGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "የሰነዶች ህጋዊ ማረጋገጫ ምርመራ",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "አዋጅ 1356/2017 ዓ.ም • MUI Statutory Compliance Audit",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "ዝጋ", tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = SteelGray)
                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Content Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Professional Info & Compliance Gauge Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SteelGray.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SteelGray)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(roleTint.copy(alpha = 0.2f), CircleShape)
                                        .border(1.dp, roleTint, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = roleTint,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = currentProf.name,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${currentProf.role} • ደረጃ ${currentProf.grade} • ${currentProf.specialization.ifBlank { currentProf.fieldOfPractice }}",
                                        color = AmharicYellow,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "የፈቃድ ቁጥር: ${currentProf.licenseNumber} | ፋይዳ: ${currentProf.nationalId.ifBlank { "የለም" }}",
                                        color = Color.LightGray,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            // Score Badge
                            Surface(
                                color = if (report.complianceScore >= 80) AmharicGreen.copy(alpha = 0.2f)
                                else if (report.complianceScore >= 50) AmharicYellow.copy(alpha = 0.2f)
                                else AmharicRed.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (report.complianceScore >= 80) AmharicGreen
                                    else if (report.complianceScore >= 50) AmharicYellow
                                    else AmharicRed
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${report.complianceScore}%",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "የተገዢነት ነጥብ",
                                        color = Color.LightGray,
                                        fontSize = 8.sp
                                    )
                                }
                            }
                        }
                    }

                    // Summary Amharic Statement
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = if (report.flags.isNotEmpty()) AmharicYellow.copy(alpha = 0.12f) else AmharicGreen.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (report.flags.isNotEmpty()) AmharicYellow.copy(alpha = 0.4f) else AmharicGreen.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (report.flags.isNotEmpty()) Icons.Default.Info else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (report.flags.isNotEmpty()) AmharicYellow else AmharicGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = report.summaryAmharic,
                                color = Color.White,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // Section 1: Flagged Deficits & Expired/Missing Documentation
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🚨 የተከሱ ሰነዶችና ጉድለቶች (${report.flags.size}):",
                                color = if (report.flags.isNotEmpty()) AmharicYellow else AmharicGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (report.hasExpiredDocs) {
                                Surface(
                                    color = AmharicRed.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "ጊዜ ያለፈበት ሰነድ አለ",
                                        color = AmharicRed,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        if (report.flags.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = AmharicGreen.copy(alpha = 0.08f)),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, AmharicGreen.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AmharicGreen, modifier = Modifier.size(24.dp))
                                    Column {
                                        Text("ምንም ዓይነት የጎደለ ወይም ጊዜው ያለፈበት ሰነድ አልተገኘም!", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("ባለሙያው በኢትዮጵያ ሕንፃ አዋጅ 1356/2017 ዓ.ም መሠረት ሙሉ በሙሉ ተገዢ (Compliant) ነው።", color = Color.LightGray, fontSize = 11.sp)
                                    }
                                }
                            }
                        } else {
                            report.flags.forEach { flag ->
                                val flagAccent = when (flag.severity) {
                                    DocVerificationSeverity.CRITICAL -> AmharicRed
                                    DocVerificationSeverity.HIGH -> SafetyOrange
                                    DocVerificationSeverity.WARNING -> AmharicYellow
                                    DocVerificationSeverity.MEDIUM -> AmharicYellow
                                    else -> ConstructionTeal
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = SteelGray.copy(alpha = 0.25f)),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, flagAccent.copy(alpha = 0.6f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = if (flag.severity == DocVerificationSeverity.CRITICAL) Icons.Default.Error else Icons.Default.Warning,
                                                    contentDescription = null,
                                                    tint = flagAccent,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = flag.title,
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Surface(
                                                color = flagAccent.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = when (flag.severity) {
                                                        DocVerificationSeverity.CRITICAL -> "አስቸኳይ/ክልክል"
                                                        DocVerificationSeverity.HIGH -> "ከፍተኛ ግኝት"
                                                        DocVerificationSeverity.WARNING -> "ማስጠንቀቂያ"
                                                        else -> "ማሳሰቢያ"
                                                    },
                                                    color = flagAccent,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "ህጋዊ ድንጋጌ: ${flag.statutoryCitation}",
                                            color = AmharicYellow,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = flag.descriptionAmharic,
                                            color = Color.LightGray,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(flagAccent.copy(alpha = 0.1f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = flagAccent, modifier = Modifier.size(12.dp))
                                                Text(
                                                    text = "ማስተካከያ: ${flag.remedialActionAmharic}",
                                                    color = Color.White,
                                                    fontSize = 10.sp
                                                )
                                            }

                                            // Quick In-place Remediation Action Button
                                            Button(
                                                onClick = {
                                                    val resolvedProf = when (flag.flagType) {
                                                        DocFlagType.EXPIRED_PRACTICING_LICENSE,
                                                        DocFlagType.EXPIRING_PRACTICING_LICENSE -> {
                                                            currentProf.copy(
                                                                licenseExpiryDate = System.currentTimeMillis() + (365L * 24 * 3600 * 1000L),
                                                                licenseStatus = "ተገዢ (COMPLIANT)",
                                                                law1356ComplianceStatus = "ተገዢ (COMPLIANT)",
                                                                lastAccreditationUpdate = System.currentTimeMillis()
                                                            )
                                                        }
                                                        DocFlagType.MISSING_INDEMNITY_INSURANCE,
                                                        DocFlagType.EXPIRED_INDEMNITY_INSURANCE -> {
                                                            currentProf.copy(
                                                                insurancePolicyNo = "PII/ETH/1356-${System.currentTimeMillis().toString().takeLast(6)}",
                                                                insuranceExpiryDate = System.currentTimeMillis() + (365L * 24 * 3600 * 1000L)
                                                            )
                                                        }
                                                        DocFlagType.MISSING_FAYDA_NATIONAL_ID -> {
                                                            currentProf.copy(
                                                                nationalId = "ET-FAYDA-${(10000000..99999999).random()}"
                                                            )
                                                        }
                                                        DocFlagType.CRITICAL_CPD_SHORTFALL,
                                                        DocFlagType.INSUFFICIENT_CPD_POINTS -> {
                                                            currentProf.copy(
                                                                cpdPoints = (currentProf.cpdPoints + 15).coerceAtLeast(30)
                                                            )
                                                        }
                                                        DocFlagType.UNVERIFIED_DIGITAL_SIGNATURE -> {
                                                            currentProf.copy(
                                                                digitalSignatureVerified = true
                                                            )
                                                        }
                                                        DocFlagType.MISSING_DEGREE_DOCUMENT -> {
                                                            val currentDocs = currentProf.attachedDocsJson
                                                            val updatedDocs = if (currentDocs.isBlank() || currentDocs == "[]") {
                                                                """["degree_certificate_verified.pdf"]"""
                                                            } else {
                                                                currentDocs.replace("]", """, "degree_certificate_verified.pdf"]""")
                                                            }
                                                            currentProf.copy(attachedDocsJson = updatedDocs)
                                                        }
                                                        DocFlagType.MISSING_ATTACHED_DOSSIER -> {
                                                            currentProf.copy(attachedDocsJson = """["license_copy.pdf", "degree_verified.pdf", "pii_policy.pdf", "cpd_certificate.pdf"]""")
                                                        }
                                                        else -> currentProf
                                                    }
                                                    currentProf = resolvedProf
                                                    onUpdateProfessional(resolvedProf)
                                                    Toast.makeText(context, "የ '${flag.title}' ጉድለት ተስተካክሎ በመዝገብ ጸድቋል! ✅", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = flagAccent),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.height(26.dp)
                                            ) {
                                                Text("አሟላ/አድስ", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Section 2: 7-Point Statutory Document Verification Checklist
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "📋 የ7-ነጥብ ህጋዊ ሰነዶች ማረጋገጫ ዝርዝር (Statutory Audit Checklist):",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        report.documentChecks.forEach { check ->
                            val checkColor = when (check.status) {
                                DocumentHealthStatus.VALID, DocumentHealthStatus.COMPLIANT -> AmharicGreen
                                DocumentHealthStatus.RENEWAL_DUE, DocumentHealthStatus.EXPIRING -> AmharicYellow
                                DocumentHealthStatus.EXPIRED, DocumentHealthStatus.DEFICIENT -> AmharicRed
                                DocumentHealthStatus.MISSING -> SafetyOrange
                                else -> Color.LightGray
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SteelGray.copy(alpha = 0.25f)),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, checkColor.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = when (check.status) {
                                                DocumentHealthStatus.VALID, DocumentHealthStatus.COMPLIANT -> Icons.Default.CheckCircle
                                                DocumentHealthStatus.RENEWAL_DUE, DocumentHealthStatus.EXPIRING -> Icons.Default.Warning
                                                DocumentHealthStatus.EXPIRED, DocumentHealthStatus.DEFICIENT -> Icons.Default.Error
                                                DocumentHealthStatus.MISSING -> Icons.Default.Cancel
                                                else -> Icons.Default.HelpOutline
                                            },
                                            contentDescription = null,
                                            tint = checkColor,
                                            modifier = Modifier.size(18.dp)
                                        )

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(
                                                    text = check.docName,
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "(${check.legalArticle})",
                                                    color = AmharicYellow,
                                                    fontSize = 9.sp
                                                )
                                            }
                                            Text(
                                                text = check.detailAmharic,
                                                color = Color.LightGray,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Surface(
                                        color = checkColor.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp),
                                        border = BorderStroke(0.5.dp, checkColor)
                                    ) {
                                        Text(
                                            text = when (check.status) {
                                                DocumentHealthStatus.VALID, DocumentHealthStatus.COMPLIANT -> "የተሟላ ✅"
                                                DocumentHealthStatus.RENEWAL_DUE, DocumentHealthStatus.EXPIRING -> "ማደስ ያለበት ⚠️"
                                                DocumentHealthStatus.EXPIRED, DocumentHealthStatus.DEFICIENT -> "ያለፈበት ❌"
                                                DocumentHealthStatus.MISSING -> "የጎደለ ❓"
                                                else -> "ያልተረጋገጠ ⏳"
                                            },
                                            color = checkColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = SteelGray)
                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Bar: Share Official Statutory Notice & Quick Renew
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Share Official Statutory Defect / Verification Notice
                    OutlinedButton(
                        onClick = {
                            val noticeText = Law1356VerificationEngine.generateStatutoryViolationNotice(
                                report = report,
                                institution = institutionProfile
                            )
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "የአዋጅ 1356/2017 የሰነዶች ኦዲት ማሳሰቢያ - ${currentProf.name}")
                                putExtra(Intent.EXTRA_TEXT, noticeText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "የህግ ማሳሰቢያ ወረቀት አጋራ"))
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AmharicYellow),
                        border = BorderStroke(1.dp, AmharicYellow.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("የህግ ማሳሰቢያ አጋራ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onOpenQuickRenew,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.LightGray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ፈቃድ አድስ", color = Color.LightGray, fontSize = 11.sp)
                        }

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = ConstructionTeal),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("ተቀበልና ዝጋ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * System/Bureau-Wide Statutory Verification Audit Dialog (Law 1356/2017)
 * Provides comprehensive audit metrics, category breakdown, flagged professionals listing,
 * and bulk synchronization with the database.
 */
@Composable
fun BatchStatutoryAuditDialog(
    professionals: List<Professional>,
    institutionProfile: InstitutionProfile,
    onDismiss: () -> Unit,
    onSelectProfessional: (Professional) -> Unit,
    onSyncAllCompliance: () -> Unit
) {
    val context = LocalContext.current
    val summary = remember(professionals) { Law1356VerificationEngine.generateSystemSummary(professionals) }
    var auditFilter by remember { mutableStateOf("ሁሉም ግኝቶች") }
    val scrollState = rememberScrollState()

    // Filter reports
    val displayedReports = remember(summary, auditFilter) {
        when (auditFilter) {
            "ጊዜ ያለፈባቸው" -> summary.reports.filter { it.hasExpiredDocs }
            "የጎደሉ ሰነዶች" -> summary.reports.filter { it.hasMissingDocs }
            "ሙሉ ተገዢ" -> summary.reports.filter { it.isFullyCompliant }
            else -> summary.reports.filter { it.flags.isNotEmpty() }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, ConstructionTeal)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(ConstructionTeal.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = ConstructionTeal, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(
                                text = "አጠቃላይ የባለሙያዎች የሰነድ ኦዲት",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${institutionProfile.name} • አዋጅ 1356/2017",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "ዝጋ", tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = SteelGray)
                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // KPI Metrics Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatMetricTile(
                            label = "የተመረመሩ",
                            value = "${summary.totalProfessionalsScanned}",
                            subtext = "ድምር ባለሙያዎች",
                            accentColor = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricTile(
                            label = "ሙሉ ተገዢ",
                            value = "${summary.fullyCompliantCount}",
                            subtext = "1356/2017 የተሟሉ",
                            accentColor = AmharicGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricTile(
                            label = "የተከሱ ሰነዶች",
                            value = "${summary.flaggedProfessionalsCount}",
                            subtext = "ግኝት የተገኘባቸው",
                            accentColor = if (summary.flaggedProfessionalsCount > 0) AmharicYellow else AmharicGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricTile(
                            label = "ጊዜያቸው ያለፈ",
                            value = "${summary.expiredDocsProfessionalsCount}",
                            subtext = "አስቸኳይ እድሳት",
                            accentColor = if (summary.expiredDocsProfessionalsCount > 0) AmharicRed else Color.LightGray,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Average Compliance Rate Bar
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SteelGray.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, SteelGray)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "አማካይ የተገዢነት ደረጃ: ${summary.averageComplianceScore}%",
                                color = if (summary.averageComplianceScore >= 75) AmharicGreen else AmharicYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "የጎደሉ ሰነዶች ያሉባቸው: ${summary.missingDocsProfessionalsCount}",
                                color = Color.LightGray,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Category Breakdown Cards
                    Text(
                        text = "የጉድለቶች ዓይነት ክፍፍል (Defect Breakdown by Category):",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = SteelGray.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(0.5.dp, Color.Gray)
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Text("የሙያ ፈቃድ", color = Color.Gray, fontSize = 9.sp)
                                Text("${summary.breakdownByCategory[DocFlagCategory.LICENSE] ?: 0}", color = AmharicRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = SteelGray.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(0.5.dp, Color.Gray)
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Text("ኢንሹራንስ (PII)", color = Color.Gray, fontSize = 9.sp)
                                Text("${summary.breakdownByCategory[DocFlagCategory.INSURANCE] ?: 0}", color = SafetyOrange, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = SteelGray.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(0.5.dp, Color.Gray)
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Text("CPD ነጥብ", color = Color.Gray, fontSize = 9.sp)
                                Text("${summary.breakdownByCategory[DocFlagCategory.CPD] ?: 0}", color = AmharicYellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = SteelGray.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(0.5.dp, Color.Gray)
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Text("የፋይዳ መለያ", color = Color.Gray, fontSize = 9.sp)
                                Text("${summary.breakdownByCategory[DocFlagCategory.IDENTIFICATION] ?: 0}", color = ConstructionTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Filter Tabs
                    val filters = listOf("ሁሉም ግኝቶች", "ጊዜ ያለፈባቸው", "የጎደሉ ሰነዶች", "ሙሉ ተገዢ")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(filters) { f ->
                            val isSelected = f == auditFilter
                            FilterChip(
                                selected = isSelected,
                                onClick = { auditFilter = f },
                                label = { Text(f, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ConstructionTeal.copy(alpha = 0.25f),
                                    selectedLabelColor = ConstructionTeal,
                                    containerColor = SteelGray.copy(alpha = 0.2f),
                                    labelColor = Color.LightGray
                                )
                            )
                        }
                    }

                    // List of Audit Results
                    if (displayedReports.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "በዚህ ምድብ ውስጥ የተመዘገበ ባለሙያ የለም።",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        displayedReports.forEach { rep ->
                            val p = rep.professional
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectProfessional(p) },
                                colors = CardDefaults.cardColors(containerColor = SteelGray.copy(alpha = 0.25f)),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (rep.flags.isNotEmpty()) AmharicYellow.copy(alpha = 0.5f) else AmharicGreen.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(text = p.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Surface(
                                                color = if (rep.flags.isNotEmpty()) AmharicYellow.copy(alpha = 0.2f) else AmharicGreen.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "${rep.complianceScore}%",
                                                    color = if (rep.flags.isNotEmpty()) AmharicYellow else AmharicGreen,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${p.role} • ${p.licenseNumber} • ${p.specialization.ifBlank { p.fieldOfPractice }}",
                                            color = Color.LightGray,
                                            fontSize = 10.sp
                                        )
                                        if (rep.flags.isNotEmpty()) {
                                            Text(
                                                text = "ግኝቶች: " + rep.flags.joinToString(", ") { it.title },
                                                color = if (rep.hasCriticalFlags) AmharicRed else AmharicYellow,
                                                fontSize = 9.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { onSelectProfessional(p) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SteelGray),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("ፈትሽ >", fontSize = 10.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = SteelGray)
                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Actions: Bulk Sync with DB and Share Audit Report
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            val auditReportText = buildString {
                                appendLine("==================================================")
                                appendLine(" ${institutionProfile.name}")
                                appendLine(" የኮንስትራክሽን ባለሙያዎች አጠቃላይ የሰነዶች ኦዲት ሪፖርት")
                                appendLine(" በአዋጅ 1356/2017 ዓ.ም መሠረት የተከናወነ")
                                appendLine("==================================================")
                                appendLine("• የተመረመሩ ባለሙያዎች: ${summary.totalProfessionalsScanned}")
                                appendLine("• ሙሉ ተገዢ (Compliant): ${summary.fullyCompliantCount}")
                                appendLine("• ሰነዶች የተከሱባቸው: ${summary.flaggedProfessionalsCount}")
                                appendLine("• ፈቃዳቸው ያለፈበት: ${summary.expiredDocsProfessionalsCount}")
                                appendLine("• ሰነድ ያጓደሉ: ${summary.missingDocsProfessionalsCount}")
                                appendLine("• አማካይ የተገዢነት ደረጃ: ${summary.averageComplianceScore}%")
                                appendLine("--------------------------------------------------")
                                appendLine("የተከሱ ባለሙያዎች ዝርዝር:")
                                summary.reports.filter { it.flags.isNotEmpty() }.forEachIndexed { idx, r ->
                                    appendLine("${idx + 1}. ${r.professional.name} (${r.professional.licenseNumber}) - ነጥብ: ${r.complianceScore}%")
                                    r.flags.forEach { f ->
                                        appendLine("   - [${f.severity}] ${f.title} (${f.statutoryCitation})")
                                    }
                                }
                                appendLine("==================================================")
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "የባለስልጣኑ የባለሙያዎች የሰነድ ኦዲት ሪፖርት (1356/2017)")
                                putExtra(Intent.EXTRA_TEXT, auditReportText)
                            }
                            context.startActivity(Intent.createChooser(intent, "የኦዲት ሪፖርት አጋራ"))
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AmharicYellow),
                        border = BorderStroke(1.dp, AmharicYellow.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ሪፖርት አጋራ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onSyncAllCompliance,
                            colors = ButtonDefaults.buttonColors(containerColor = ConstructionTeal),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ሁኔታዎችን አመሳስል", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = SteelGray),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("ዝጋ", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
