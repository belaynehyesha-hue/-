package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Project
import com.example.data.RiskAssessment
import com.example.ui.viewmodel.ConstructionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskAssessmentScreen(
    projects: List<Project>,
    viewModel: ConstructionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val riskAssessments by viewModel.riskAssessments.collectAsState()

    var selectedProjectFilter by remember { mutableStateOf<Int?>(null) }
    var selectedRiskFilter by remember { mutableStateOf<String?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<String?>(null) }

    // Form inputs state
    var selectedProjectId by remember { mutableStateOf<Int?>(null) }
    var hazardTitle by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var riskLevel by remember { mutableStateOf("HIGH") } // LOW, MEDIUM, HIGH, CRITICAL
    var selectedStandardId by remember { mutableStateOf("ebcs-1") }
    var selectedSafetyClause by remember { mutableStateOf("") }
    var mitigationPlan by remember { mutableStateOf("") }

    var expandedProjectDropdown by remember { mutableStateOf(false) }
    var expandedStandardDropdown by remember { mutableStateOf(false) }
    var expandedClauseDropdown by remember { mutableStateOf(false) }

    // Selected standard object
    val currentStandard = remember(selectedStandardId) {
        allLibraryItems.find { it.id == selectedStandardId }
    }

    // Set default clause when standard changes
    LaunchedEffect(currentStandard) {
        currentStandard?.let {
            val clausesAndCheckpoints = it.keyClauses + it.complianceCheckpoints
            if (clausesAndCheckpoints.isNotEmpty()) {
                selectedSafetyClause = clausesAndCheckpoints.first()
            } else {
                selectedSafetyClause = "ጠቅላላ የደህንነት መመሪያ"
            }
        }
    }

    // Set default project ID if available
    LaunchedEffect(projects) {
        if (selectedProjectId == null && projects.isNotEmpty()) {
            selectedProjectId = projects.first().id
        }
    }

    // Filter assessments
    val filteredAssessments = remember(riskAssessments, selectedProjectFilter, selectedRiskFilter, selectedStatusFilter) {
        riskAssessments.filter { assessment ->
            val matchesProject = selectedProjectFilter == null || assessment.projectId == selectedProjectFilter
            val matchesRisk = selectedRiskFilter == null || assessment.riskLevel == selectedRiskFilter
            val matchesStatus = selectedStatusFilter == null || assessment.status == selectedStatusFilter
            matchesProject && matchesRisk && matchesStatus
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(BackgroundSlate)) {
        val isWideScreen = maxWidth > 850.dp

        Row(modifier = Modifier.fillMaxSize()) {
            // Dual Pane Layout on Wide Screens
            if (isWideScreen) {
                // Left Column: Hazard Input Form
                Card(
                    modifier = Modifier
                        .width(380.dp)
                        .fillMaxHeight()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "አዲስ አደጋ መመዝገቢያ (Log Hazard)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AmharicYellow
                            )
                        )
                        Divider(color = Color.Gray.copy(alpha = 0.2f))

                        HazardLoggingForm(
                            projects = projects,
                            selectedProjectId = selectedProjectId,
                            hazardTitle = hazardTitle,
                            description = description,
                            riskLevel = riskLevel,
                            selectedStandardId = selectedStandardId,
                            selectedSafetyClause = selectedSafetyClause,
                            mitigationPlan = mitigationPlan,
                            onProjectIdChange = { selectedProjectId = it },
                            onHazardTitleChange = { hazardTitle = it },
                            onDescriptionChange = { description = it },
                            onRiskLevelChange = { riskLevel = it },
                            onStandardIdChange = { selectedStandardId = it },
                            onSafetyClauseChange = { selectedSafetyClause = it },
                            onMitigationPlanChange = { mitigationPlan = it },
                            expandedProjectDropdown = expandedProjectDropdown,
                            onExpandedProjectDropdownChange = { expandedProjectDropdown = it },
                            expandedStandardDropdown = expandedStandardDropdown,
                            onExpandedStandardDropdownChange = { expandedStandardDropdown = it },
                            expandedClauseDropdown = expandedClauseDropdown,
                            onExpandedClauseDropdownChange = { expandedClauseDropdown = it },
                            currentStandard = currentStandard,
                            onLogClick = {
                                if (selectedProjectId != null && hazardTitle.isNotBlank() && description.isNotBlank() && mitigationPlan.isNotBlank()) {
                                    viewModel.addRiskAssessment(
                                        projectId = selectedProjectId!!,
                                        hazardTitle = hazardTitle,
                                        description = description,
                                        riskLevel = riskLevel,
                                        mitigationPlan = mitigationPlan,
                                        ebcsStandardRefId = selectedStandardId,
                                        safetyClause = selectedSafetyClause
                                    )
                                    hazardTitle = ""
                                    description = ""
                                    mitigationPlan = ""
                                    Toast.makeText(context, "የግንባታ ደህንነት አደጋው በስኬት ተመዝግቧል! 🛡", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "እባክዎን ሁሉንም መስኮች ያሟሉ!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }

            // Right Column / Main Content: Logged Hazards & Filters
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title and Header block
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = AmharicRed, modifier = Modifier.size(28.dp))
                            Text(
                                text = "የግንባታ ደህንነት አደጋዎች ፍተሻ",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                        Text(
                            text = "Site Risk Assessment & Hazard Mapping (EBCS Standards)",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var showReportMenu by remember { mutableStateOf(false) }
                        Box {
                            Button(
                                onClick = { showReportMenu = true },
                                colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("safety_report_download_btn")
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ማጠቃለያ ሪፖርት", fontSize = 11.sp)
                            }
                            DropdownMenu(
                                expanded = showReportMenu,
                                onDismissRequest = { showReportMenu = false },
                                modifier = Modifier.background(CardBackground)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("በExcel (CSV) አውርድ", color = Color.White, fontSize = 11.sp) },
                                    leadingIcon = { Icon(Icons.Default.GridOn, contentDescription = null, tint = AmharicGreen, modifier = Modifier.size(16.dp)) },
                                    onClick = {
                                        showReportMenu = false
                                        viewModel.shareSummaryReport(context, isExcel = true)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("በPDF (TXT) አውርድ", color = Color.White, fontSize = 11.sp) },
                                    leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = AmharicRed, modifier = Modifier.size(16.dp)) },
                                    onClick = {
                                        showReportMenu = false
                                        viewModel.shareSummaryReport(context, isExcel = false)
                                    }
                                )
                            }
                        }

                        // Mobile log button (Triggers logging dialog on small screens)
                        if (!isWideScreen) {
                            var showMobileFormDialog by remember { mutableStateOf(false) }
                            Button(
                                onClick = { showMobileFormDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = AmharicRed),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("አደጋ መዝግብ", fontSize = 11.sp)
                            }

                            if (showMobileFormDialog) {
                                Dialog(
                                    onDismissRequest = { showMobileFormDialog = false }
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight()
                                            .padding(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                                .verticalScroll(rememberScrollState()),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "አዲስ አደጋ መመዝገቢያ (Log Hazard)",
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = AmharicYellow
                                                    )
                                                )
                                                IconButton(onClick = { showMobileFormDialog = false }) {
                                                    Icon(Icons.Default.Close, contentDescription = "ዝጋ", tint = Color.LightGray)
                                                }
                                            }
                                            Divider(color = Color.Gray.copy(alpha = 0.2f))

                                            HazardLoggingForm(
                                                projects = projects,
                                                selectedProjectId = selectedProjectId,
                                                hazardTitle = hazardTitle,
                                                description = description,
                                                riskLevel = riskLevel,
                                                selectedStandardId = selectedStandardId,
                                                selectedSafetyClause = selectedSafetyClause,
                                                mitigationPlan = mitigationPlan,
                                                onProjectIdChange = { selectedProjectId = it },
                                                onHazardTitleChange = { hazardTitle = it },
                                                onDescriptionChange = { description = it },
                                                onRiskLevelChange = { riskLevel = it },
                                                onStandardIdChange = { selectedStandardId = it },
                                                onSafetyClauseChange = { selectedSafetyClause = it },
                                                onMitigationPlanChange = { mitigationPlan = it },
                                                expandedProjectDropdown = expandedProjectDropdown,
                                                onExpandedProjectDropdownChange = { expandedProjectDropdown = it },
                                                expandedStandardDropdown = expandedStandardDropdown,
                                                onExpandedStandardDropdownChange = { expandedStandardDropdown = it },
                                                expandedClauseDropdown = expandedClauseDropdown,
                                                onExpandedClauseDropdownChange = { expandedClauseDropdown = it },
                                                currentStandard = currentStandard,
                                                onLogClick = {
                                                    if (selectedProjectId != null && hazardTitle.isNotBlank() && description.isNotBlank() && mitigationPlan.isNotBlank()) {
                                                        viewModel.addRiskAssessment(
                                                            projectId = selectedProjectId!!,
                                                            hazardTitle = hazardTitle,
                                                            description = description,
                                                            riskLevel = riskLevel,
                                                            mitigationPlan = mitigationPlan,
                                                            ebcsStandardRefId = selectedStandardId,
                                                            safetyClause = selectedSafetyClause
                                                        )
                                                        hazardTitle = ""
                                                        description = ""
                                                        mitigationPlan = ""
                                                        showMobileFormDialog = false
                                                        Toast.makeText(context, "የግንባታ ደህንነት አደጋው በስኬት ተመዝግቧል! 🛡", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "እባክዎን ሁሉንም መስኮች ያሟሉ!", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Filter controls Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "አደጋዎችን ማጣሪያ (Filter Site Hazards)",
                            color = AmharicYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Project Filter Chips
                            FilterChip(
                                selected = selectedProjectFilter == null,
                                onClick = { selectedProjectFilter = null },
                                label = { Text("ሁሉም ግንባታዎች (All)", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AmharicGreen,
                                    selectedLabelColor = Color.White,
                                    containerColor = SteelGray,
                                    labelColor = Color.LightGray
                                )
                            )

                            projects.forEach { proj ->
                                FilterChip(
                                    selected = selectedProjectFilter == proj.id,
                                    onClick = { selectedProjectFilter = proj.id },
                                    label = { Text(proj.name, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AmharicGreen,
                                        selectedLabelColor = Color.White,
                                        containerColor = SteelGray,
                                        labelColor = Color.LightGray
                                    )
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Risk Level Filter Chips
                            Text("አደጋ ደረጃ፡", color = Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            listOf("ሁሉም" to null, "CRITICAL" to "CRITICAL", "HIGH" to "HIGH", "MEDIUM" to "MEDIUM", "LOW" to "LOW").forEach { (title, key) ->
                                FilterChip(
                                    selected = selectedRiskFilter == key,
                                    onClick = { selectedRiskFilter = key },
                                    label = { Text(title, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ConstructionTeal,
                                        selectedLabelColor = Color.White,
                                        containerColor = SteelGray,
                                        labelColor = Color.LightGray
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Status Filter Chips
                            Text("ሁኔታ፡", color = Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            listOf("ሁሉም" to null, "OPEN" to "OPEN", "RESOLVED" to "RESOLVED").forEach { (title, key) ->
                                FilterChip(
                                    selected = selectedStatusFilter == key,
                                    onClick = { selectedStatusFilter = key },
                                    label = { Text(if (title == "OPEN") "ክትትል ላይ" else if (title == "RESOLVED") "መፍትሄ ያገኘ" else title, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AmharicYellow,
                                        selectedLabelColor = SteelGray,
                                        containerColor = SteelGray,
                                        labelColor = Color.LightGray
                                    )
                                )
                            }
                        }
                    }
                }

                // Hazards List Column
                if (filteredAssessments.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color.LightGray.copy(alpha = 0.5f),
                                modifier = Modifier.size(50.dp)
                            )
                            Text(
                                text = "ምንም የተመዘገበ የደህንነት አደጋ የለም!",
                                color = Color.LightGray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ሁሉም የደህንነት ፍተሻዎች ንፁህ ናቸው ወይም ማጣሪያውን የሚዛመድ የለም።",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f).testTag("hazards_lazy_list"),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredAssessments, key = { it.id }) { assessment ->
                            val assocProject = projects.find { it.id == assessment.projectId }
                            HazardCard(
                                assessment = assessment,
                                project = assocProject,
                                onToggleStatus = { viewModel.toggleRiskAssessmentStatus(assessment) },
                                onDelete = { viewModel.deleteRiskAssessment(assessment) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HazardLoggingForm(
    projects: List<Project>,
    selectedProjectId: Int?,
    hazardTitle: String,
    description: String,
    riskLevel: String,
    selectedStandardId: String,
    selectedSafetyClause: String,
    mitigationPlan: String,
    onProjectIdChange: (Int) -> Unit,
    onHazardTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onRiskLevelChange: (String) -> Unit,
    onStandardIdChange: (String) -> Unit,
    onSafetyClauseChange: (String) -> Unit,
    onMitigationPlanChange: (String) -> Unit,
    expandedProjectDropdown: Boolean,
    onExpandedProjectDropdownChange: (Boolean) -> Unit,
    expandedStandardDropdown: Boolean,
    onExpandedStandardDropdownChange: (Boolean) -> Unit,
    expandedClauseDropdown: Boolean,
    onExpandedClauseDropdownChange: (Boolean) -> Unit,
    currentStandard: CodeLibraryItem?,
    onLogClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Project Selection Dropdown
        Text("የግንባታ ፕሮጀክት (Project)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Box(modifier = Modifier.fillMaxWidth()) {
            val selectedProject = projects.find { it.id == selectedProjectId }
            OutlinedButton(
                onClick = { onExpandedProjectDropdownChange(true) },
                modifier = Modifier.fillMaxWidth().testTag("hazard_form_project_dropdown"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedProject?.name ?: "ለመመዝገብ መጀመሪያ ግንባታ ይምረጡ",
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
            DropdownMenu(
                expanded = expandedProjectDropdown,
                onDismissRequest = { onExpandedProjectDropdownChange(false) },
                modifier = Modifier.background(CardBackground)
            ) {
                projects.forEach { proj ->
                    DropdownMenuItem(
                        text = { Text(proj.name, color = Color.White, fontSize = 11.sp) },
                        onClick = {
                            onProjectIdChange(proj.id)
                            onExpandedProjectDropdownChange(false)
                        }
                    )
                }
            }
        }

        // Hazard Title
        Text("የአደጋው ርዕስ (Hazard Title)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = hazardTitle,
            onValueChange = onHazardTitleChange,
            placeholder = { Text("ምሳሌ፡ ያልተከለለ የቁፋሮ ጠርዝ", fontSize = 11.sp, color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().testTag("hazard_form_title_input"),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontSize = 11.sp),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.LightGray,
                focusedBorderColor = AmharicYellow,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
            ),
            singleLine = true
        )

        // Description
        Text("ዝርዝር መግለጫ (Hazard Description)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            placeholder = { Text("አደጋው እንዴት ሊፈጠር እንደሚችልና የሚያስከትለውን ጉዳት ይግለጹ...", fontSize = 11.sp, color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().testTag("hazard_form_desc_input"),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontSize = 11.sp),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.LightGray,
                focusedBorderColor = AmharicYellow,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
            ),
            maxLines = 3
        )

        // Risk Level Buttons
        Text("የአደጋ ደረጃ (Risk Level)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("LOW" to "ዝቅተኛ", "MEDIUM" to "መካከለኛ", "HIGH" to "ከፍተኛ", "CRITICAL" to "ወሳኝ").forEach { (key, name) ->
                val isSel = riskLevel == key
                val btnColor = when (key) {
                    "LOW" -> if (isSel) Color(0xFF2E7D32) else SteelGray
                    "MEDIUM" -> if (isSel) Color(0xFFF9A825) else SteelGray
                    "HIGH" -> if (isSel) Color(0xFFEF6C00) else SteelGray
                    else -> if (isSel) AmharicRed else SteelGray
                }
                val textColor = if (isSel) Color.White else Color.LightGray

                Button(
                    onClick = { onRiskLevelChange(key) },
                    colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f).height(32.dp).testTag("hazard_form_risk_btn_$key")
                ) {
                    Text(name, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textColor)
                }
            }
        }

        // Mapping to EBCS building standards selection
        Text("ተዛማጅ EBCS/የህንጻ ኮድ (EBCS Code Mapping)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Box(modifier = Modifier.fillMaxWidth()) {
            val selectedStandard = allLibraryItems.find { it.id == selectedStandardId }
            OutlinedButton(
                onClick = { onExpandedStandardDropdownChange(true) },
                modifier = Modifier.fillMaxWidth().testTag("hazard_form_standard_dropdown"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedStandard?.titleEn?.substringBefore(":") ?: "ብሔራዊ የሕንፃ ኮድ ይምረጡ",
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
            DropdownMenu(
                expanded = expandedStandardDropdown,
                onDismissRequest = { onExpandedStandardDropdownChange(false) },
                modifier = Modifier.background(CardBackground)
            ) {
                allLibraryItems.forEach { standard ->
                    DropdownMenuItem(
                        text = { Text(standard.titleEn, color = Color.White, fontSize = 11.sp) },
                        onClick = {
                            onStandardIdChange(standard.id)
                            onExpandedStandardDropdownChange(false)
                        }
                    )
                }
            }
        }

        // Map Safety Clause/Checkpoint Selection
        Text("የደህንነት አንቀጽ / ቼክፖይንት (Safety Clause / Checkpoint)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { onExpandedClauseDropdownChange(true) },
                modifier = Modifier.fillMaxWidth().testTag("hazard_form_clause_dropdown"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedSafetyClause.ifBlank { "አንቀጽ / ቼክፖይንት ይምረጡ" },
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
            DropdownMenu(
                expanded = expandedClauseDropdown,
                onDismissRequest = { onExpandedClauseDropdownChange(false) },
                modifier = Modifier.background(CardBackground)
            ) {
                currentStandard?.let { standard ->
                    val clausesAndCheckpoints = standard.keyClauses + standard.complianceCheckpoints
                    clausesAndCheckpoints.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item, color = Color.White, fontSize = 11.sp) },
                            onClick = {
                                onSafetyClauseChange(item)
                                onExpandedClauseDropdownChange(false)
                            }
                        )
                    }
                }
            }
        }

        // Mitigation Plan
        Text("የመፍትሄ/አደጋ መቀነሻ ዕቅድ (Mitigation Plan)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = mitigationPlan,
            onValueChange = onMitigationPlanChange,
            placeholder = { Text("ምሳሌ፡ የአደጋ ማስጠንቀቂያ ምልክቶችን መትከል፣ ዙሪያውን በባሪኬድ ማጠር", fontSize = 11.sp, color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().testTag("hazard_form_mitigation_input"),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontSize = 11.sp),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.LightGray,
                focusedBorderColor = AmharicYellow,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
            ),
            maxLines = 2
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Save Button
        Button(
            onClick = onLogClick,
            colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen),
            modifier = Modifier.fillMaxWidth().testTag("hazard_form_submit_btn"),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("አደጋውን መዝግብ (Log Safety Hazard)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HazardCard(
    assessment: RiskAssessment,
    project: Project?,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    val riskColor = when (assessment.riskLevel) {
        "LOW" -> Color(0xFF4CAF50)
        "MEDIUM" -> Color(0xFFFFEB3B)
        "HIGH" -> Color(0xFFFF9800)
        else -> AmharicRed
    }

    val riskText = when (assessment.riskLevel) {
        "LOW" -> "ዝቅተኛ (Low Risk)"
        "MEDIUM" -> "መካከለኛ (Medium Risk)"
        "HIGH" -> "ከፍተኛ (High Risk)"
        else -> "ወሳኝ (Critical Risk)"
    }

    val statusBgColor = if (assessment.status == "OPEN") AmharicRed.copy(alpha = 0.15f) else AmharicGreen.copy(alpha = 0.15f)
    val statusColor = if (assessment.status == "OPEN") AmharicRed else AmharicGreen
    val statusLabel = if (assessment.status == "OPEN") "በክትትል ላይ (OPEN)" else "ተፈቷል / መፍትሄ አግኝቷል (RESOLVED)"

    val sdf = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val formattedDate = remember(assessment.dateLogged) { sdf.format(Date(assessment.dateLogged)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hazard_card_${assessment.id}"),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, riskColor.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Risk Level & Status badges + delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Risk level indicator circle
                    Box(modifier = Modifier.size(10.dp).background(riskColor, CircleShape))
                    Surface(
                        color = riskColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = riskText,
                            color = if (assessment.riskLevel == "MEDIUM") Color(0xFFFBC02D) else riskColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        color = statusBgColor,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            color = statusColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp).testTag("hazard_card_delete_${assessment.id}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "ሰርዝ", tint = AmharicRed.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                }
            }

            // Hazard Title
            Text(
                text = assessment.hazardTitle,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            // Description
            Text(
                text = assessment.description,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.LightGray, fontSize = 12.sp)
            )

            // Associated Project Detail
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Apartment, contentDescription = null, tint = AmharicYellow, modifier = Modifier.size(14.dp))
                Text(
                    text = "ፕሮጀክት፡ ${project?.name ?: "ያልታወቀ ግንባታ"} (${project?.location ?: "-"})",
                    color = AmharicYellow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Divider(color = Color.Gray.copy(alpha = 0.1f))

            // Mapped safety standard details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Gavel, contentDescription = null, tint = ConstructionTeal, modifier = Modifier.size(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "ተዛማጅ የህንፃ ኮድ / ስታንዳርድ (Mapped EBCS Code):",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${assessment.ebcsStandardRefId.uppercase()} -> ${assessment.safetyClause}",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }

            // Mitigation Plan details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = AmharicGreen, modifier = Modifier.size(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "የአደጋ መቀነሻ ዕቅድ (Safety Mitigation Plan):",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = assessment.mitigationPlan,
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }

            Divider(color = Color.Gray.copy(alpha = 0.1f))

            // Footer row with Logger + date + toggle status button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "የመዘገበው፡ ${assessment.loggedBy}",
                        color = Color.Gray,
                        fontSize = 9.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (assessment.isSynced) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "በዳታቤዝ ላይ ተቀምጧል",
                                tint = AmharicGreen,
                                modifier = Modifier.size(11.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "በመሳሪያው ላይ ተቀምጧል (Unsynced)",
                                tint = AmharicYellow,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                        Text(
                            text = "የተመዘገበበት፡ $formattedDate",
                            color = Color.Gray,
                            fontSize = 9.sp
                        )
                    }
                }

                // Status toggle button
                Button(
                    onClick = onToggleStatus,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (assessment.status == "OPEN") AmharicGreen else AmharicRed
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(28.dp).testTag("hazard_card_toggle_${assessment.id}")
                ) {
                    val icon = if (assessment.status == "OPEN") Icons.Default.CheckCircle else Icons.Default.Cancel
                    val text = if (assessment.status == "OPEN") "ፈታሁት (Resolve)" else "እንደገና ክፈት (Reopen)"
                    Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
