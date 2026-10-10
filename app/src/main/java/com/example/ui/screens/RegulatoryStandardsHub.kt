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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegulatoryStandardsHub(
    institutionProfile: InstitutionProfile,
    onNavigateBack: () -> Unit = {},
    onOpenManualPdf: () -> Unit = {}
) {
    val context = LocalContext.current

    var selectedJurisdiction by remember { mutableStateOf<JurisdictionScope?>(null) }
    var selectedWorkDomain by remember { mutableStateOf(WorkDomain.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var currentHubTab by remember { mutableStateOf(0) } // 0: Browse Laws & Codes, 1: Office Works Audit, 2: Site Works Inspection, 3: Proclamation 1356/2017 Special Focus

    val hubTabs = listOf(
        Triple("ሕጎችና ኮዶች (Laws)", Icons.Default.Gavel, 0),
        Triple("የቢሮ ስራዎች (Office)", Icons.Default.Apartment, 1),
        Triple("የሳይት ስራዎች (Site)", Icons.Default.Construction, 2),
        Triple("አዋጅ 1356/2017 (New Law)", Icons.Default.VerifiedUser, 3)
    )

    // State for interactive checklists
    val officeChecklist = remember { mutableStateListOf(*RegulatoryLawRepository.OFFICE_WORKS_CHECKLIST.toTypedArray()) }
    val siteChecklist = remember { mutableStateListOf(*RegulatoryLawRepository.SITE_WORKS_CHECKLIST.toTypedArray()) }

    val filteredLaws = remember(selectedJurisdiction, selectedWorkDomain, searchQuery) {
        RegulatoryLawRepository.ALL_LAWS_AND_REGULATIONS.filter { item ->
            val matchJurisdiction = selectedJurisdiction == null || item.jurisdiction == selectedJurisdiction
            val matchDomain = selectedWorkDomain == WorkDomain.ALL || item.workDomain == selectedWorkDomain || item.workDomain == WorkDomain.ALL
            val matchQuery = searchQuery.isBlank() ||
                    item.titleAm.contains(searchQuery, ignoreCase = true) ||
                    item.titleEn.contains(searchQuery, ignoreCase = true) ||
                    item.codeOrRef.contains(searchQuery, ignoreCase = true) ||
                    item.summaryAm.contains(searchQuery, ignoreCase = true)
            matchJurisdiction && matchDomain && matchQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSlate)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- Header Banner with Proclamation 1356/2017 and International Scope ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SteelGray),
            border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(AmharicYellow, ConstructionTeal, AmharicGreen)))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(AmharicYellow.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = AmharicYellow,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "ዓለም አቀፍና የኢትዮጵያ ኮንስትራክሽን ሕግጋት ማዕከል",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Ethiopian & Worldwide Building Codes & Statutory Laws (Office & Site)",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                            )
                        }
                    }

                    Surface(
                        color = AmharicGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, AmharicGreen)
                    ) {
                        Text(
                            text = "አዋጅ 1356/2017",
                            color = AmharicGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "በአዲሱ የኢትዮጵያ ሕንፃ አዋጅ ቁጥር 1356/2017 ዓ.ም፣ በEBCS/Eurocodes፣ FIDIC 2017፣ IBC 2024 እና OSHA/ISO 45001 የተደገፈ የተሟላ የቢሮና የሳይት ስራዎች መመሪያ።",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 16.sp
                )
            }
        }

        // --- Hub Tab Navigation Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SteelGray)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            hubTabs.forEach { (title, icon, idx) ->
                val isSelected = currentHubTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) AmharicYellow.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) AmharicYellow else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { currentHubTab = idx }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) AmharicYellow else Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = title,
                            fontSize = 9.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) AmharicYellow else Color.LightGray,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        when (currentHubTab) {
            0 -> BrowseLawsSection(
                filteredLaws = filteredLaws,
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                selectedJurisdiction = selectedJurisdiction,
                onJurisdictionChange = { selectedJurisdiction = it },
                selectedWorkDomain = selectedWorkDomain,
                onDomainChange = { selectedWorkDomain = it }
            )
            1 -> OfficeWorksAuditSection(
                institutionProfile = institutionProfile,
                checklist = officeChecklist,
                onToggleCheck = { idx, checked ->
                    officeChecklist[idx] = officeChecklist[idx].copy(isCompliant = checked)
                }
            )
            2 -> SiteWorksInspectionSection(
                institutionProfile = institutionProfile,
                checklist = siteChecklist,
                onToggleCheck = { idx, checked ->
                    siteChecklist[idx] = siteChecklist[idx].copy(isCompliant = checked)
                }
            )
            3 -> Proclamation1356SpecialFocusSection(
                institutionProfile = institutionProfile,
                onOpenManualPdf = onOpenManualPdf
            )
        }
    }
}

// -------------------------------------------------------------
// SECTION 0: Browse Laws, Codes & Regulations (Searchable & Filterable)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrowseLawsSection(
    filteredLaws: List<LawRegulationItem>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedJurisdiction: JurisdictionScope?,
    onJurisdictionChange: (JurisdictionScope?) -> Unit,
    selectedWorkDomain: WorkDomain,
    onDomainChange: (WorkDomain) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("በሕግ፣ አዋጅ፣ ኮድ (1356፣ FIDIC፣ IBC፣ EBCS...) ፈልግ...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AmharicYellow) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.LightGray)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AmharicYellow,
                unfocusedBorderColor = Color.Gray,
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground
            ),
            shape = RoundedCornerShape(10.dp)
        )

        // Jurisdiction Chips
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("የሕግ ስልጣን ክልል (Jurisdiction Scope):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmharicYellow)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(
                        selected = selectedJurisdiction == null,
                        onClick = { onJurisdictionChange(null) },
                        label = { Text("ሁሉም ክልሎች (All Worldwide)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmharicYellow,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
                items(JurisdictionScope.values()) { scope ->
                    FilterChip(
                        selected = selectedJurisdiction == scope,
                        onClick = { onJurisdictionChange(scope) },
                        label = { Text(scope.labelAm, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmharicYellow,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }
        }

        // Domain Filters (Office vs Site)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            WorkDomain.values().forEach { domain ->
                val isSelected = selectedWorkDomain == domain
                Surface(
                    color = if (isSelected) ConstructionTeal else SteelGray,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isSelected) ConstructionTeal else Color.Gray.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onDomainChange(domain) }
                ) {
                    Text(
                        text = domain.labelAm.substringBefore(" "),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 6.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Text(
            text = "የተገኙ ህጎችና ደንቦች (${filteredLaws.size})",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        filteredLaws.forEach { item ->
            LawRegulationCard(item = item)
        }
    }
}

@Composable
fun LawRegulationCard(item: LawRegulationItem) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, if (isExpanded) AmharicYellow else Color.Gray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = when (item.category) {
                        "PROCLAMATION" -> AmharicGreen.copy(alpha = 0.2f)
                        "CONTRACT" -> ConstructionTeal.copy(alpha = 0.2f)
                        "HSE" -> SafetyOrange.copy(alpha = 0.2f)
                        else -> AmharicYellow.copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, when (item.category) {
                        "PROCLAMATION" -> AmharicGreen
                        "CONTRACT" -> ConstructionTeal
                        "HSE" -> SafetyOrange
                        else -> AmharicYellow
                    })
                ) {
                    Text(
                        text = item.codeOrRef,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    color = SteelGray,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = item.workDomain.labelAm.substringBefore(" "),
                        fontSize = 9.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = item.titleAm,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
            )
            Text(
                text = item.titleEn,
                fontSize = 11.sp,
                color = AmharicYellow
            )

            Text(
                text = item.summaryAm,
                fontSize = 11.5.sp,
                color = Color.White.copy(alpha = 0.85f),
                lineHeight = 16.sp
            )

            // Expandable details
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))

                    // Mandatory Requirements
                    Text("• አስገዳጅ መስፈርቶች (Mandatory Requirements):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmharicYellow)
                    item.mandatoryRequirements.forEach { req ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("✓", color = AmharicGreen, fontSize = 11.sp)
                            Text(req, fontSize = 11.sp, color = Color.White)
                        }
                    }

                    // Office Works Guidelines
                    if (item.officeWorksGuidelines.isNotEmpty()) {
                        Text("• የቢሮ ስራዎች መመሪያ (Office Works SOP):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ConstructionTeal)
                        item.officeWorksGuidelines.forEach { sop ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("🏢", fontSize = 10.sp)
                                Text(sop, fontSize = 11.sp, color = Color.LightGray)
                            }
                        }
                    }

                    // Site Works Guidelines
                    if (item.siteWorksGuidelines.isNotEmpty()) {
                        Text("• የሳይት ስራዎች መመሪያ (Site Works SOP):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SafetyOrange)
                        item.siteWorksGuidelines.forEach { sop ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("👷", fontSize = 10.sp)
                                Text(sop, fontSize = 11.sp, color = Color.LightGray)
                            }
                        }
                    }

                    // Penalties and Equivalents
                    Surface(
                        color = Color.Red.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("የሕግ ተጠያቂነትና ቅጣት (Legal Sanctions):", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                            Text(item.penaltyOrRisk, fontSize = 10.sp, color = Color.White)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ዓለም አቀፍ ተዛማጅ፡", fontSize = 10.sp, color = Color.Gray)
                        Text(item.internationalEquivalent, fontSize = 10.sp, color = AmharicYellow, fontWeight = FontWeight.Bold)
                    }
                }
            }

            TextButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = if (isExpanded) "ዝርዝር ደብቅ ▲" else "ሙሉ የሕግ ዝርዝርና መመሪያዎች ይመልከቱ ▼",
                    fontSize = 11.sp,
                    color = AmharicYellow
                )
            }
        }
    }
}

// -------------------------------------------------------------
// SECTION 1: Office Works Audit & Permitting Engine
// -------------------------------------------------------------
@Composable
private fun OfficeWorksAuditSection(
    institutionProfile: InstitutionProfile,
    checklist: List<ComplianceCheckItem>,
    onToggleCheck: (Int, Boolean) -> Unit
) {
    val context = LocalContext.current
    val totalCount = checklist.size
    val compliantCount = checklist.count { it.isCompliant }
    val scorePercentage = if (totalCount > 0) (compliantCount * 100 / totalCount) else 0

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Audit KPI Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, ConstructionTeal.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("የቢሮ ስራዎች የሕግ ተገዢነት ኦዲት", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        Text("Design, Permitting, Insurance & Contract Compliance", fontSize = 11.sp, color = Color.LightGray)
                    }

                    Surface(
                        color = if (scorePercentage >= 80) AmharicGreen else SafetyOrange,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "$scorePercentage% የተሟላ",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { scorePercentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (scorePercentage >= 80) AmharicGreen else SafetyOrange,
                    trackColor = SteelGray
                )
                Text(
                    text = "የተረጋገጡ መስፈርቶች፡ $compliantCount ከ $totalCount",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
            }
        }

        // Checklist Items
        checklist.forEachIndexed { idx, item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, if (item.isCompliant) AmharicGreen.copy(alpha = 0.6f) else Color.Gray.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = item.isCompliant,
                        onCheckedChange = { onToggleCheck(idx, it) },
                        colors = CheckboxDefaults.colors(checkedColor = AmharicGreen, uncheckedColor = Color.LightGray)
                    )

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item.taskTitleAm,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isCompliant) AmharicGreen else Color.White
                            )
                        }
                        Text(
                            text = item.governingLawRef,
                            fontSize = 10.sp,
                            color = AmharicYellow,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.descriptionAm,
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                        Text(
                            text = "ካልተሟላ አደጋው፡ ${item.penaltyIfIgnored}",
                            fontSize = 10.sp,
                            color = SafetyOrange
                        )
                    }
                }
            }
        }

        // Export / Share Official Audit Certificate
        Button(
            onClick = {
                val reportText = buildString {
                    appendLine("=====================================================")
                    appendLine("የቢሮ ስራዎች፣ ፕላንና ውል አስተዳደር የሕግ ተገዢነት ማረጋገጫ ሰርተፍኬት")
                    appendLine(institutionProfile.name)
                    appendLine("በአዲሱ የኢትዮጵያ ሕንፃ አዋጅ 1356/2017 ዓ.ም እና በFIDIC/EBCS መሠረት")
                    appendLine("=====================================================")
                    appendLine("የተገዢነት ምጣኔ: $scorePercentage% ($compliantCount / $totalCount መስፈርቶች)")
                    appendLine("ያረጋገጠው ተቋም: ${institutionProfile.name}")
                    appendLine("ኃላፊ: ${institutionProfile.officerInCharge}")
                    appendLine("-----------------------------------------------------")
                    checklist.forEachIndexed { i, item ->
                        val status = if (item.isCompliant) "✓ ተሟልቷል (COMPLIANT)" else "✗ አልተሟላም (NON-COMPLIANT)"
                        appendLine("${i + 1}. ${item.taskTitleAm} [${item.governingLawRef}] -> $status")
                    }
                    appendLine("=====================================================")
                }

                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "የቢሮ ስራዎችና ፕላን ማጽደቂያ የሕግ ኦዲት")
                    putExtra(Intent.EXTRA_TEXT, reportText)
                }
                context.startActivity(Intent.createChooser(sendIntent, "የቢሮ ኦዲት ሪፖርት አጋራ"))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ConstructionTeal)
        ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("የቢሮ ስራዎች የሕግ ኦዲት ሪፖርት አጋራ (Share Audit)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

// -------------------------------------------------------------
// SECTION 2: Site Works Inspection & Casting Permit Engine
// -------------------------------------------------------------
@Composable
private fun SiteWorksInspectionSection(
    institutionProfile: InstitutionProfile,
    checklist: List<ComplianceCheckItem>,
    onToggleCheck: (Int, Boolean) -> Unit
) {
    val context = LocalContext.current
    val totalCount = checklist.size
    val compliantCount = checklist.count { it.isCompliant }
    val scorePercentage = if (totalCount > 0) (compliantCount * 100 / totalCount) else 0

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Site Inspection KPI Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, SafetyOrange.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("የሳይት ስራዎችና የኮንክሪት ፈቃድ ቁጥጥር", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        Text("Hoarding, PPE, Shoring, Rebar, Pour Permit & Cube Tests", fontSize = 11.sp, color = Color.LightGray)
                    }

                    Surface(
                        color = if (scorePercentage >= 80) AmharicGreen else SafetyOrange,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "$scorePercentage% የሳይት ዝግጁነት",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { scorePercentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (scorePercentage >= 80) AmharicGreen else SafetyOrange,
                    trackColor = SteelGray
                )
                Text(
                    text = "የተረጋገጡ የሳይት መስፈርቶች፡ $compliantCount ከ $totalCount",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
            }
        }

        // Checklist Items
        checklist.forEachIndexed { idx, item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, if (item.isCompliant) AmharicGreen.copy(alpha = 0.6f) else Color.Gray.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = item.isCompliant,
                        onCheckedChange = { onToggleCheck(idx, it) },
                        colors = CheckboxDefaults.colors(checkedColor = AmharicGreen, uncheckedColor = Color.LightGray)
                    )

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = item.taskTitleAm,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isCompliant) AmharicGreen else Color.White
                        )
                        Text(
                            text = item.governingLawRef,
                            fontSize = 10.sp,
                            color = SafetyOrange,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.descriptionAm,
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                        Text(
                            text = "ጥሰት ቢፈጠር፡ ${item.penaltyIfIgnored}",
                            fontSize = 10.sp,
                            color = Color.Red.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Export / Share Official Pre-Pour & Site Inspection Permit
        Button(
            onClick = {
                val siteReport = buildString {
                    appendLine("=====================================================")
                    appendLine("የኮንስትራክሽን ሳይት ፍተሻና የኮንክሪት መሙላት ፈቃድ (POUR PERMIT)")
                    appendLine(institutionProfile.name)
                    appendLine("በአዲሱ የኢትዮጵያ ሕንፃ አዋጅ 1356/2017 ዓ.ም፣ EBCS-2 እና OSHA 1926 መሠረት")
                    appendLine("=====================================================")
                    appendLine("የሳይት ዝግጁነት ውጤት: $scorePercentage% ($compliantCount / $totalCount መስፈርቶች)")
                    appendLine("የሳይት መሪ መሃንዲስ / ተቆጣጣሪ: ${institutionProfile.officerInCharge}")
                    appendLine("የፍተሻ ውሳኔ: ${if (scorePercentage >= 80) "ኮንክሪት ለመሙላትና ስራ ለመቀጠል ተፈቅዷል (APPROVED TO PROCEED)" else "ያልተሟሉ መስፈርቶች ስላሉ ኮንክሪት መሙላት በጥብቅ ተከልክሏል (HOLD ORDER)"}")
                    appendLine("-----------------------------------------------------")
                    checklist.forEachIndexed { i, item ->
                        val status = if (item.isCompliant) "✓ ተፈትሾ ጸድቋል" else "✗ አልጸደቀም (የሚስተካከል)"
                        appendLine("${i + 1}. ${item.taskTitleAm} -> $status")
                    }
                    appendLine("=====================================================")
                }

                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "የሳይት ፍተሻና የኮንክሪት ፈቃድ ሰነድ")
                    putExtra(Intent.EXTRA_TEXT, siteReport)
                }
                context.startActivity(Intent.createChooser(sendIntent, "የሳይት ፍተሻ ሪፖርት አጋራ"))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange)
        ) {
            Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("የሳይት ፍተሻና የኮንክሪት ፈቃድ አጋራ (Share Pour Permit)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
        }
    }
}

// -------------------------------------------------------------
// SECTION 3: Proclamation 1356/2017 Special Deep-Dive Focus
// -------------------------------------------------------------
@Composable
private fun Proclamation1356SpecialFocusSection(
    institutionProfile: InstitutionProfile,
    onOpenManualPdf: () -> Unit = {}
) {
    val context = LocalContext.current

    val keyArticles = listOf(
        Pair("ክፍል 2፡ የህንፃዎች ምድብ (Building Categorization)", "ምድብ ሀ (Category A: ≤2 ፎቅ፣ ስፓን ≤7m)፣ ምድብ ለ (Category B: 3-8 ፎቅ፣ ስፓን ≤12m፣ ስብሰባ ≤500 ሰው)፣ ምድብ ሐ (Category C: >8 ፎቅ፣ ጥልቅ ቤዝመንት፣ ፋብሪካዎች፣ ስብሰባ >500 ሰው) እና ሜጋ ፕሮጀክቶች ተለይተዋል። ለእያንዳንዱ ምድብ የተለየ የዲዛይንና ፈቃድ መስፈርት ተቀምጧል።"),
        Pair("ክፍል 3፡ ዲጂታል ፕላንና የግንባታ ፈቃድ (Permitting)", "ማንኛውም ግንባታ ያለ ዲጂታል ፈቃድ መጀመር የተከለከለ ነው። የአርክቴክቸር፣ ስትራክቸር፣ ሳኒታሪ፣ ኤሌክትሪካልና የእሳት ደህንነት ፕላኖች በአንድነት ተገምግመው በ21 ቀናት ውስጥ ፈቃድ ይሰጣል።"),
        Pair("ክፍል 4፡ 7ቱ የሳይት ፍተሻ ደረጃዎች (Statutory Inspections)", "1. ቁፋሮና የመሬት ዝግጅት፣ 2. መሰረትና የበለበሎ፣ 3. ምሶሶና ሸክም ተሸካሚ ግድግዳ፣ 4. የኮንክሪት ወለል (ስላብ)፣ 5. ጠቅላላ መዋቅር፣ 6. የሳኒታሪና ኤሌክትሪክ መስመሮች፣ 7. የመጨረሻ ማጠናቀቂያና የመጠቀሚያ ፈቃድ (Occupancy Permit)።"),
        Pair("ክፍል 5፡ የአካል ጉዳተኞች ተደራሽነት (Accessibility)", "በሁሉም የህዝብና የንግድ ህንፃዎች ላይ ተዳፋት (1:12 Ramp)፣ ሊፍት፣ ሰፊ በሮች (≥90cm)፣ የሚዳሰሱ ንጣፎች (Tactile paving) እና ልዩ መጸዳጃ ቤቶች ማካተት አስገዳጅ ነው።"),
        Pair("ክፍል 6፡ የጋራ የሕግ ተጠያቂነት (Joint Liability)", "ህንፃው ቢፈርስ ወይም ጉዳት ቢደርስበት ባለቤቱ፣ ዲዛይን አዘጋጁ፣ አማካሪው መሃንዲስና ተቋራጩ በጋራና በግል (Joint & Several Liability) በወንጀልና በፍትሐብሔር ህግ ተጠያቂ ይሆናሉ።"),
        Pair("ክፍል 7፡ አስተዳደራዊና የወንጀል ቅጣቶች (Sanctions)", "ያለፈቃድ የተጀመረ ግንባታ ወዲያውኑ ይታገዳል፣ እንዲፈርስ ይደረጋል። ጥፋተኛውም እስከ 1,000,000 ብር የገንዘብ መቀጮ እና ከ5 እስከ 20 ዓመት ፅኑ እስራት ይጠብቀዋል።")
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.5.dp, AmharicYellow)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = AmharicYellow, modifier = Modifier.size(24.dp))
                    Text(
                        text = "አዲሱ የኢትዮጵያ ሕንፃ አዋጅ ቁጥር 1356/2017 ዓ.ም",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
                Text(
                    text = "ይህ አዋጅ የቀድሞውን አዋጅ ቁጥር 624/2001 በመተካት በ2017 ዓ.ም የፀደቀ ብሔራዊ አስገዳጅ ሕግ ነው።",
                    fontSize = 11.5.sp,
                    color = AmharicYellow
                )
            }
        }

        keyArticles.forEach { (articleTitle, articleContent) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = articleTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AmharicGreen)
                    Text(text = articleContent, fontSize = 11.sp, color = Color.White, lineHeight = 16.sp)
                }
            }
        }

        // Share Proclamation 1356 Guide
        Button(
            onClick = {
                val fullSummary = buildString {
                    appendLine("=========================================")
                    appendLine("የኢትዮጵያ አዲሱ ሕንፃ አዋጅ ቁጥር 1356/2017 ዓ.ም")
                    appendLine("ኦፊሴላዊ መመሪያ ሰነድ - ${institutionProfile.name}")
                    appendLine("=========================================")
                    keyArticles.forEach { (t, c) ->
                        appendLine("• $t\n  $c\n")
                    }
                    appendLine("ያዘጋጀው፡ ${institutionProfile.officerInCharge}")
                }
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "አዋጅ ቁጥር 1356/2017 ዓ.ም መመሪያ")
                    putExtra(Intent.EXTRA_TEXT, fullSummary)
                }
                context.startActivity(Intent.createChooser(shareIntent, "የአዋጅ 1356 ማጠቃለያ አጋራ"))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen)
        ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("የአዋጅ 1356/2017 ዝርዝር መመሪያ አጋራ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                onOpenManualPdf()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ConstructionTeal)
        ) {
            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("📘 የተሟላ የሲስተም አሰራር መመሪያ (Operational Manual PDF)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
        }
    }
}
