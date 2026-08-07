@file:kotlin.OptIn(
    com.google.accompanist.permissions.ExperimentalPermissionsApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.example.ui.screens

import android.widget.Toast
import com.example.util.AudioRecorderHelper
import java.io.File
import com.google.accompanist.permissions.*
import kotlinx.coroutines.launch
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.data.DailyProgress
import com.example.data.Project
import com.example.ui.viewmodel.ConstructionViewModel
import java.text.SimpleDateFormat
import java.util.*

// Data models for the dynamic checklist
data class PhaseChecklistItem(
    val id: String,
    val taskAm: String,
    val taskEn: String,
    val standardRefId: String,
    val standardRefName: String,
    val clauseOrCheckpoint: String,
    val recommendedValue: String,
    val category: String // "EBCS" or "FIDIC"
)

data class InspectionPhase(
    val nameAm: String,
    val nameEn: String,
    val iconName: String,
    val description: String,
    val items: List<PhaseChecklistItem>
)

// Global list of phase-based checklists mapped against EBCS & FIDIC standards
val allInspectionPhases = listOf(
    InspectionPhase(
        nameAm = "የቁፋሮና የመሬት ስራ",
        nameEn = "Earthworks & Excavation",
        iconName = "landscape",
        description = "የህንፃ ቁፋሮ ጥልቀት፣ የአፈር መሸከም አቅም (Bearing Capacity) እና የጎንዮሽ አፈር መደርመስ መከላከያ ፍተሻዎች (EBCS-1 እና ESEN 1997)",
        items = listOf(
            PhaseChecklistItem(
                id = "earth-1",
                taskAm = "የአፈር ዓይነትና የመሸከም አቅም (Soil Bearing Capacity)",
                taskEn = "Verify soil type and bearing capacity against geotechnical design assumptions",
                standardRefId = "esen-Eurocodes",
                standardRefName = "ESEN 1997: Geotechnical Design",
                clauseOrCheckpoint = "የአፈር የመሸከም አቅም (Soil Bearing Capacity: Min 150 kN/m² for stable foundation as per ESEN 1997)",
                recommendedValue = "Soil load test or dynamic penetrometer values match report",
                category = "EBCS"
            ),
            PhaseChecklistItem(
                id = "earth-2",
                taskAm = "የቁፋሮ ጥልቀትና ደህንነት (Excavation Depth & Safety)",
                taskEn = "Check depth of excavation and stability of side slopes/shoring",
                standardRefId = "ebcs-1",
                standardRefName = "EBCS-1: Basis of Design",
                clauseOrCheckpoint = "የጎንዮሽ አፈር መደርመስ መከላከያ ዝግጅት (Shoring/sloping required for excavations deeper than 1.5m)",
                recommendedValue = "Excavation depth matches structural drawings and safety criteria met",
                category = "EBCS"
            )
        )
    ),
    InspectionPhase(
        nameAm = "የመሰረት ግንባታ",
        nameEn = "Substructure & Foundation",
        iconName = "layers",
        description = "የመሠረት ድንጋይ ንጣፍ፣ የንፁህ ኮንክሪት (Lean Concrete) እና ለመሠረትና ከምድር በታች ላሉ አርማታዎች የሚውል የኮንክሪት ሽፋን ውፍረት ፍተሻ (EBCS-2)",
        items = listOf(
            PhaseChecklistItem(
                id = "found-1",
                taskAm = "የመሠረት አርማታ ውፍረት (Concrete Cover in Footing)",
                taskEn = "Ensure minimum concrete cover for foundation elements (footing/ground beam)",
                standardRefId = "ebcs-2",
                standardRefName = "EBCS-2: Structural Concrete",
                clauseOrCheckpoint = "ለመሠረትና ከምድር በታች ላሉ አርማታዎች አነስተኛው የኮንክሪት ሽፋን (Concrete Cover for foundation: Min 50mm due to soil contact)",
                recommendedValue = "Spacer blocks thickness: Min 50mm installed",
                category = "EBCS"
            ),
            PhaseChecklistItem(
                id = "found-2",
                taskAm = "የመሠረት ድንጋይ ንጣፍና መስመር (Lean Concrete)",
                taskEn = "Verify thickness and compaction of hardcore bed and lean concrete thickness",
                standardRefId = "esen-Eurocodes",
                standardRefName = "ESEN 1992: Concrete Design",
                clauseOrCheckpoint = "ለመሠረት የሚውል የንፁህ ኮንክሪት ውፍረት (Lean Concrete thickness: Min 50mm - 100mm, Grade C15)",
                recommendedValue = "Lean concrete poured with thickness >= 50mm, compacted hardcore layer >= 250mm",
                category = "EBCS"
            )
        )
    ),
    InspectionPhase(
        nameAm = "የአርማታ መዋቅር",
        nameEn = "Superstructure & Concrete",
        iconName = "apartment",
        description = "የምሰሶዎችና ጨረሮች የአርማታ ሽፋን ውፍረት፣ የኮንክሪት ጥንካሬ ደረጃ (C25+) እና የማሰሪያ ብረቶች ክፍተት ፍተሻ (EBCS-2)",
        items = listOf(
            PhaseChecklistItem(
                id = "concrete-1",
                taskAm = "የምሰሶዎችና ጨረሮች የአርማታ ሽፋን (Concrete Cover for Columns)",
                taskEn = "Verify concrete cover thickness for superstructure elements",
                standardRefId = "ebcs-2",
                standardRefName = "EBCS-2: Structural Concrete",
                clauseOrCheckpoint = "የምሰሶዎችና የጨረሮች የአርማታ ሽፋን ውፍረት (Concrete Cover for columns/beams: Min 30mm - 40mm)",
                recommendedValue = "Spacer blocks verified at 35mm on all column faces",
                category = "EBCS"
            ),
            PhaseChecklistItem(
                id = "concrete-2",
                taskAm = "የኮንክሪት ጥንካሬ ማረጋገጫ (Concrete Grade & Slump)",
                taskEn = "Perform slump test and prepare cubes for compressive strength verification",
                standardRefId = "ebcs-2",
                standardRefName = "EBCS-2: Structural Concrete",
                clauseOrCheckpoint = "ለፎቅ ህንፃዎች ምሰሶዎች የሚውል ኮንክሪት ቢያንስ C25 (25 MPa) መሆኑን ማረጋገጥ",
                recommendedValue = "Concrete grade C25 or higher, slump: 80mm - 100mm, 6 cube samples cast",
                category = "EBCS"
            ),
            PhaseChecklistItem(
                id = "concrete-3",
                taskAm = "የማሰሪያ ብረት (Stirrups) ክፍተት ፍተሻ",
                taskEn = "Check spacing of stirrups/ties in high stress zones near joints",
                standardRefId = "ebcs-2",
                standardRefName = "EBCS-2: Structural Concrete",
                clauseOrCheckpoint = "የምሰሶዎች ማሰሪያ ብረት (Stirrups) ክፍተት በከፍተኛ ጭነት ቦታዎች ከ 150mm መብለጥ የለበትም",
                recommendedValue = "Stirrup spacing in critical zones: 100mm - 120mm, non-critical zones: 150mm",
                category = "EBCS"
            )
        )
    ),
    InspectionPhase(
        nameAm = "የጣራና የብረት መዋቅር",
        nameEn = "Roofing & Steel Structures",
        iconName = "build",
        description = "የጣራ መዋቅር ብረቶች ግንኙነት፣ የብሎንና የብየዳ ጥራት እንዲሁም የንፋስ ኃይል መቋቋሚያ ማሰሪያዎች ፍተሻ (EBCS-1 እና EBCS-3)",
        items = listOf(
            PhaseChecklistItem(
                id = "steel-1",
                taskAm = "የብረት ግንኙነቶችና ብየዳ ጥራት (Bolted/Welded)",
                taskEn = "Check bolt grades and weld sizes of structural steel joints",
                standardRefId = "ebcs-3",
                standardRefName = "EBCS-3: Steel Structures",
                clauseOrCheckpoint = "የብረት መጋጠሚያዎች፣ ብሎኖችና ብየዳዎች (Bolted/Welded Connections, Weld throat thickness: Min 4mm - 6mm)",
                recommendedValue = "Full penetration weld, weld fillet thickness >= 5mm, bolt grade 8.8 tightened",
                category = "EBCS"
            ),
            PhaseChecklistItem(
                id = "steel-2",
                taskAm = "የንፋስ መቋቋም አቅም ማሰሪያ (Wind Uplift Bracing)",
                taskEn = "Verify anchor bolts and wind bracing elements are in place",
                standardRefId = "ebcs-1",
                standardRefName = "EBCS-1: Basis of Design",
                clauseOrCheckpoint = "የጣራ ብረቶች ከከባድ ንፋስ የሚነሳውን ወደ ላይ የመሳብ ኃይል (Uplift Force) መቋቋም መቻላቸውን መፈተሽ",
                recommendedValue = "Wind bracing diagonals in place; J-bolts embedded min 150mm in ring beam",
                category = "EBCS"
            )
        )
    ),
    InspectionPhase(
        nameAm = "የመሬት መንቀጥቀጥ ጥበቃ",
        nameEn = "Seismic Resistance & Joints",
        iconName = "shield",
        description = "የህንፃ መገጣጠሚያ ክፍተቶች፣ በምሰሶዎች ውስጥ የብረት ማሰሪያዎች በ 135 ዲግሪ መታጠፍና የመሬት መንቀጥቀጥ መቋቋሚያ ዝርዝር ዲዛይን ፍተሻ (EBCS-8)",
        items = listOf(
            PhaseChecklistItem(
                id = "seismic-1",
                taskAm = "የህንፃ መገጣጠሚያ ክፍተት (Expansion / Seismic Joint)",
                taskEn = "Verify separation distance between adjacent blocks to prevent pounding",
                standardRefId = "ebcs-8",
                standardRefName = "EBCS-8: Earthquake Resistance",
                clauseOrCheckpoint = "የመሬት መንቀጥቀጥ መገጣጠሚያ ክፍተት (Seismic Joint spacing: Min 50mm - 100mm depending on building height)",
                recommendedValue = "Gap width verified: 50mm, clear of any debris or mortar spills",
                category = "EBCS"
            ),
            PhaseChecklistItem(
                id = "seismic-2",
                taskAm = "የምሰሶዎች ልዩ ማጠናከሪያ (Ductile Detailing)",
                taskEn = "Inspect 135-degree hooks in stirrups for seismic confinement",
                standardRefId = "ebcs-8",
                standardRefName = "EBCS-8: Earthquake Resistance",
                clauseOrCheckpoint = "የምሰሶ ማሰሪያዎች በ135 ዲግሪ መታጠፍና ቢያንስ 10d (ወይም 100mm) መዘርጋት አለባቸው (Seismic hook 135-deg)",
                recommendedValue = "Stirrup ends bent at 135-degree angle with 100mm anchorage length",
                category = "EBCS"
            )
        )
    ),
    InspectionPhase(
        nameAm = "የውል አስተዳደር (FIDIC)",
        nameEn = "Contract & FIDIC Clauses",
        iconName = "assignment",
        description = "የስራ ቦታ ይዞታ ርክክብ፣ ስራዎች ከመሸፈናቸው በፊት የፍተሻ ማሳወቂያዎች እና የጊዜ ማራዘሚያ (EoT) ጥያቄዎች ውል-ተኮር ፍተሻዎች (FIDIC Red Book)",
        items = listOf(
            PhaseChecklistItem(
                id = "fidic-1",
                taskAm = "የመግቢያና የመስራት መብት (Right of Access)",
                taskEn = "Confirm employer has given contractor access to site as per contract timeline",
                standardRefId = "fidic-red",
                standardRefName = "FIDIC Red Book: Conditions of Contract",
                clauseOrCheckpoint = "Clause 2.1: ለተቋራጩ የሳይት መግቢያና ይዞታ መስጠት (Right of Access to the Site - Employer's obligation)",
                recommendedValue = "Site handed over within 14 days of Letter of Acceptance; protocol signed",
                category = "FIDIC"
            ),
            PhaseChecklistItem(
                id = "fidic-2",
                taskAm = "ስራዎች ከመሸፈናቸው በፊት ፍተሻ (Inspection before Cover)",
                taskEn = "Ensure Engineer is given notice to inspect any foundation or reinforcement before concrete is poured",
                standardRefId = "fidic-red",
                standardRefName = "FIDIC Red Book: Conditions of Contract",
                clauseOrCheckpoint = "Clause 7.3 & 7.4: ስራዎችን ከመሸፈን በፊት ለኢንጂነሩ ፍተሻ ማሳወቅ (Notice to inspect before covering up work)",
                recommendedValue = "Minimum 24 hours written RFI (Request for Inspection) submitted and signed by Consultant",
                category = "FIDIC"
            ),
            PhaseChecklistItem(
                id = "fidic-3",
                taskAm = "የጊዜ ማራዘሚያ ማሳወቂያ (Claim Notification Period)",
                taskEn = "Verify contractor has notified of any delay event within specified contract days",
                standardRefId = "fidic-red",
                standardRefName = "FIDIC Red Book: Conditions of Contract",
                clauseOrCheckpoint = "Clause 20.1 / 8.4: የጊዜ ማራዘሚያ ጥያቄ በ28 ቀናት ውስጥ ማሳወቅ (Contractor Claims: Notice of claim within 28 days of event)",
                recommendedValue = "Notice of claim submitted within 28 days of delay occurrence; daily records maintained",
                category = "FIDIC"
            )
        )
    )
)

// Helper to get vector icon for phase
@Composable
fun getPhaseIcon(name: String) = when (name) {
    "landscape" -> Icons.Default.Landscape
    "layers" -> Icons.Default.Layers
    "apartment" -> Icons.Default.Apartment
    "build" -> Icons.Default.Build
    "shield" -> Icons.Default.Shield
    "assignment" -> Icons.Default.Assignment
    else -> Icons.Default.List
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyProgressScreen(
    projects: List<Project>,
    viewModel: ConstructionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dailyLogs by viewModel.dailyProgress.collectAsState()
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()
    val isNetworkConnected by viewModel.isNetworkConnected.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncProgress by viewModel.syncProgress.collectAsState()

    var selectedProjectIdFilter by remember { mutableStateOf<Int?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var logToDelete by remember { mutableStateOf<DailyProgress?>(null) }

    var selectedTab by remember { mutableStateOf(0) }
    var activePhaseIndex by remember { mutableStateOf(0) }

    // Map to keep track of checked states for each checklist item ID ("PENDING", "PASSED", "FAILED")
    val checklistItemStatuses = remember { mutableStateMapOf<String, String>() }

    // Prefill states for the AddDailyLogDialog
    var prefillTaskDescription by remember { mutableStateOf("") }
    var prefillStandardId by remember { mutableStateOf("") }
    var prefillCheckpoint by remember { mutableStateOf("") }
    var prefillMeasuredValue by remember { mutableStateOf("") }
    var prefillStatus by remember { mutableStateOf("ተገዢ (COMPLIANT)") }

    // Filtered logs
    val filteredLogs = remember(dailyLogs, selectedProjectIdFilter, selectedStatusFilter, searchQuery) {
        dailyLogs.filter { log ->
            val matchesProject = selectedProjectIdFilter == null || log.projectId == selectedProjectIdFilter
            val matchesStatus = selectedStatusFilter == null || log.status.contains(selectedStatusFilter!!, ignoreCase = true)
            val matchesSearch = searchQuery.trim().isEmpty() ||
                    log.taskDescription.contains(searchQuery, ignoreCase = true) ||
                    log.notes.contains(searchQuery, ignoreCase = true) ||
                    log.measuredValue.contains(searchQuery, ignoreCase = true) ||
                    log.checkpointPassed.contains(searchQuery, ignoreCase = true)
            matchesProject && matchesStatus && matchesSearch
        }
    }

    // Statistical calculations
    val totalLogsCount = filteredLogs.size
    val compliantLogsCount = filteredLogs.count { it.status.contains("COMPLIANT") || it.status.contains("ተገዢ") }
    val nonCompliantLogsCount = filteredLogs.count { it.status.contains("NON-COMPLIANT") || it.status.contains("ያልተገዢ") }
    val warningLogsCount = filteredLogs.count { it.status.contains("WARNING") || it.status.contains("ማስጠንቀቂያ") }
    
    val complianceRate = if (totalLogsCount > 0) {
        compliantLogsCount.toFloat() / totalLogsCount.toFloat()
    } else {
        1f
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("daily_progress_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    prefillTaskDescription = ""
                    prefillStandardId = ""
                    prefillCheckpoint = ""
                    prefillMeasuredValue = ""
                    prefillStatus = "ተገዢ (COMPLIANT)"
                    showAddDialog = true 
                },
                containerColor = AmharicYellow,
                contentColor = SteelGray,
                modifier = Modifier.testTag("add_daily_log_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "አዲስ መከታተያ መዝግብ")
            }
        },
        containerColor = BackgroundSlate
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "የዕለት ተዕለት ግንባታ ቁጥጥርና የተገዢነት መዝገብ",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ግንባታዎችን ከሀገራዊ (EBCS) እና ዓለም አቀፍ ደረጃዎች ጋር የማወዳደሪያ መከታተያ",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            }

            // Network & Sync status indicator card
            val unsyncedCount = dailyLogs.count { !it.isSynced }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOfflineMode) AmharicRed.copy(alpha = 0.1f) else AmharicGreen.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (isOfflineMode) AmharicRed.copy(alpha = 0.3f) else AmharicGreen.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                                    .size(10.dp)
                                    .background(if (isOfflineMode) AmharicRed else AmharicGreen, CircleShape)
                            )
                            Column {
                                Text(
                                    text = if (isOfflineMode) "ከመስመር ውጭ የአገልግሎት ሁነታ (Offline Mode)" else "ከመስመር በላይ የአገልግሎት ሁነታ (Online)",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (!isNetworkConnected) "የሞባይል ኢንተርኔት ግንኙነት የለም 📶 (Offline)" else "ግንኙነቱ የተረጋጋ ነው - መረጃ በቀጥታ ይጫናል 🌐",
                                    color = Color.LightGray,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Toggle offline manually
                        val isManualOffline by viewModel.isManualOfflineMode.collectAsState()
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isManualOffline) AmharicRed.copy(alpha = 0.15f) else SteelGray.copy(alpha = 0.5f))
                                .border(0.5.dp, if (isManualOffline) AmharicRed else Color.Gray, RoundedCornerShape(20.dp))
                                .clickable {
                                    viewModel.toggleOfflineMode(!isManualOffline)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isManualOffline) Icons.Default.WifiOff else Icons.Default.Wifi,
                                contentDescription = null,
                                tint = if (isManualOffline) AmharicRed else Color.LightGray,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (isManualOffline) "ከመስመር ውጭ" else "መስመር ላይ",
                                color = if (isManualOffline) AmharicRed else Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (unsyncedCount > 0) {
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
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
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = AmharicYellow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "ያልተመሳሰሉ ${unsyncedCount} መዝገቦች በስልክዎ ላይ ተቀምጠዋል 💾",
                                    color = AmharicYellow,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.syncOfflineData { count ->
                                        if (count > 0) {
                                            Toast.makeText(context, "${count} የዕለት ሪፖርቶች ከማዕከላዊ ዳታቤዝ ጋር ተመሳስለዋል! 🚀", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "ምንም ያልተመሳሰለ መረጃ የለም።", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                enabled = !isOfflineMode && !isSyncing,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AmharicYellow,
                                    contentColor = SteelGray,
                                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f),
                                    disabledContentColor = Color.LightGray
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Text("አሁን አመሳስል (Sync)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (isSyncing) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "መረጃዎች ከከተማ አስተዳደር ኮንስትራክሽን ጽ/ቤት ሰርቨር ጋር እየተመሳሰሉ ነው...",
                                    color = Color.LightGray,
                                    fontSize = 9.sp
                                )
                                Text(
                                    text = "${(syncProgress * 100).toInt()}%",
                                    color = AmharicYellow,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            LinearProgressIndicator(
                                progress = { syncProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = AmharicYellow,
                                trackColor = Color.Gray.copy(alpha = 0.2f),
                            )
                        }
                    }
                }
            }

            // Tab row to toggle between history logs and phase checklists
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SteelGray,
                contentColor = AmharicYellow,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = AmharicYellow
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { 
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("የክትትል መዝገብ ታሪክ (History Logs)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { 
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("ደረጃ-ተኮር ቼክሊስት (Phase Checklist)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                // Quick Stats & Compliance Gauge Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Gauge Canvas
                        Box(
                            modifier = Modifier.size(90.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Track
                                drawArc(
                                    color = Color.Gray.copy(alpha = 0.2f),
                                    startAngle = -220f,
                                    sweepAngle = 260f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                                // Filled Arc
                                drawArc(
                                    color = when {
                                        complianceRate >= 0.8f -> AmharicGreen
                                        complianceRate >= 0.5f -> AmharicYellow
                                        else -> AmharicRed
                                    },
                                    startAngle = -220f,
                                    sweepAngle = 260f * complianceRate,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${(complianceRate * 100).toInt()}%",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "የተገዢነት ደረጃ",
                                    color = Color.LightGray,
                                    fontSize = 9.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Stat Fields
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "ቁጥጥር የተደረገባቸው ሁኔታዎች",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatItem(title = "ጠቅላላ መዝገቦች", value = totalLogsCount.toString(), color = Color.LightGray)
                                StatItem(title = "ተገዢ (Compliant)", value = compliantLogsCount.toString(), color = AmharicGreen)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatItem(title = "ማስጠንቀቂያ", value = warningLogsCount.toString(), color = AmharicYellow)
                                StatItem(title = "ያልተገዢ (Non)", value = nonCompliantLogsCount.toString(), color = AmharicRed)
                            }
                        }
                    }
                }

                // Filters Section (Horizontal Scroll Row)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Search field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("በቁልፍ ቃል ፈልግ...", color = Color.Gray, fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("progress_search_input"),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AmharicYellow, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "አጽዳ", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AmharicYellow,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            focusedContainerColor = SteelGray.copy(alpha = 0.4f),
                            unfocusedContainerColor = SteelGray.copy(alpha = 0.2f)
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Dropdown and Selectors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Project Selector Dropdown
                        var projectDropdownExpanded by remember { mutableStateOf(false) }
                        val currentProjectName = selectedProjectIdFilter?.let { id ->
                            projects.find { it.id == id }?.name ?: "ሁሉም ፕሮጀክቶች"
                        } ?: "ሁሉም ፕሮጀክቶች"

                        Box(modifier = Modifier.weight(1f)) {
                            Button(
                                onClick = { projectDropdownExpanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SteelGray),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = currentProjectName,
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AmharicYellow, modifier = Modifier.size(16.dp))
                                }
                            }
                            DropdownMenu(
                                expanded = projectDropdownExpanded,
                                onDismissRequest = { projectDropdownExpanded = false },
                                modifier = Modifier.background(CardBackground)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("ሁሉም ፕሮጀክቶች", color = Color.White, fontSize = 12.sp) },
                                    onClick = {
                                        selectedProjectIdFilter = null
                                        projectDropdownExpanded = false
                                    }
                                )
                                projects.forEach { proj ->
                                    DropdownMenuItem(
                                        text = { Text(proj.name, color = Color.White, fontSize = 12.sp) },
                                        onClick = {
                                            selectedProjectIdFilter = proj.id
                                            projectDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Status Filter
                        val statusFilters = listOf(
                            "ሁሉም" to null,
                            "ተገዢ" to "COMPLIANT",
                            "ያልተገዢ" to "NON-COMPLIANT",
                            "ማስጠንቀቂያ" to "WARNING"
                        )

                        var statusDropdownExpanded by remember { mutableStateOf(false) }
                        val currentStatusLabel = statusFilters.find { it.second == selectedStatusFilter }?.first ?: "ሁሉም ሁኔታዎች"

                        Box(modifier = Modifier.weight(1f)) {
                            Button(
                                onClick = { statusDropdownExpanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SteelGray),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = currentStatusLabel,
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AmharicYellow, modifier = Modifier.size(16.dp))
                                }
                            }
                            DropdownMenu(
                                expanded = statusDropdownExpanded,
                                onDismissRequest = { statusDropdownExpanded = false },
                                modifier = Modifier.background(CardBackground)
                            ) {
                                statusFilters.forEach { (label, value) ->
                                    DropdownMenuItem(
                                        text = { Text(label, color = Color.White, fontSize = 12.sp) },
                                        onClick = {
                                            selectedStatusFilter = value
                                            statusDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Results summary or empty message
                if (filteredLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(CardBackground, RoundedCornerShape(12.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.EventNote, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Text(
                                text = "ምንም የዕለት ቁጥጥር መከታተያ አልተመዘገበም ወይም ከፍለጋ ማጣሪያዎች ጋር የሚዛመድ የለም።",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { 
                                    prefillTaskDescription = ""
                                    prefillStandardId = ""
                                    prefillCheckpoint = ""
                                    prefillMeasuredValue = ""
                                    prefillStatus = "ተገዢ (COMPLIANT)"
                                    showAddDialog = true 
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AmharicYellow),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("አዲስ መከታተያ መዝግብ", color = SteelGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Timeline List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredLogs) { log ->
                            val matchedProject = projects.find { it.id == log.projectId }
                            val matchingStandard = allLibraryItems.find { it.id == log.standardRefId }

                            DailyProgressLogCard(
                                log = log,
                                projectName = matchedProject?.name ?: "ያልታወቀ ፕሮጀክት",
                                standardName = matchingStandard?.titleAm ?: log.standardRefId,
                                onDeleteClick = { logToDelete = log }
                            )
                        }
                    }
                }
            } else {
                // Dynamic phase-based checklist
                // 1. Horizontal Phase Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allInspectionPhases.forEachIndexed { index, phase ->
                        val isSelected = activePhaseIndex == index
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { activePhaseIndex = index }
                                .testTag("phase_chip_${index}"),
                            color = if (isSelected) AmharicYellow else SteelGray.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, if (isSelected) AmharicYellow else Color.Transparent)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = getPhaseIcon(phase.iconName),
                                    contentDescription = null,
                                    tint = if (isSelected) SteelGray else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = phase.nameAm,
                                    color = if (isSelected) SteelGray else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Selected Phase Progress Card
                val activePhase = allInspectionPhases[activePhaseIndex]
                val phaseItems = activePhase.items
                val completedCount = phaseItems.count { checklistItemStatuses[it.id] != null && checklistItemStatuses[it.id] != "PENDING" }
                val passedCount = phaseItems.count { checklistItemStatuses[it.id] == "PASSED" }
                val progressVal = if (phaseItems.isNotEmpty()) completedCount.toFloat() / phaseItems.size.toFloat() else 0f
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${activePhase.nameAm} (${activePhase.nameEn})",
                            color = AmharicYellow,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = activePhase.description,
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "የፍተሻ ማጠናቀቂያ መጠን (Completed): $completedCount/${phaseItems.size} ($passedCount ያለፉ)",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${(progressVal * 100).toInt()}%",
                                color = if (progressVal == 1f) AmharicGreen else AmharicYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        LinearProgressIndicator(
                            progress = progressVal,
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = if (progressVal == 1f) AmharicGreen else AmharicYellow,
                            trackColor = SteelGray
                        )
                    }
                }

                // Checklist Grid/List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(phaseItems) { item ->
                        val status = checklistItemStatuses[item.id] ?: "PENDING"
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = when (status) {
                                    "PASSED" -> AmharicGreen.copy(alpha = 0.4f)
                                    "FAILED" -> AmharicRed.copy(alpha = 0.4f)
                                    else -> Color.Gray.copy(alpha = 0.1f)
                                }
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("checklist_item_${item.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Header (Title & Category)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.taskAm,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (item.category == "EBCS") AmharicGreen.copy(alpha = 0.2f) else AmharicYellow.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = item.category,
                                            color = if (item.category == "EBCS") AmharicGreen else AmharicYellow,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                
                                // Reference Clause
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SteelGray.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.Bookmark, contentDescription = null, tint = AmharicYellow, modifier = Modifier.size(12.dp))
                                        Text(
                                            text = item.standardRefName,
                                            color = AmharicYellow,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = item.clauseOrCheckpoint,
                                        color = Color.LightGray,
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                                
                                // Recommended Target Criteria
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(12.dp).padding(top = 1.dp))
                                    Text(
                                        text = "የሚጠበቅ መመዘኛ (Target): ${item.recommendedValue}",
                                        color = Color.LightGray,
                                        fontSize = 9.sp,
                                        lineHeight = 13.sp
                                    )
                                }
                                
                                HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                                
                                // Verification state buttons & Logging CTA
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Compliance selector chips
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val states = listOf(
                                            "PENDING" to Pair("ያልተገመገመ", Color.Gray),
                                            "PASSED" to Pair("ያለፈ (PASSED)", AmharicGreen),
                                            "FAILED" to Pair("የወደቀ (FAILED)", AmharicRed)
                                        )
                                        states.forEach { (stateId, info) ->
                                            val (labelText, colorVal) = info
                                            val isCurrent = status == stateId
                                            Surface(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable { checklistItemStatuses[item.id] = stateId },
                                                color = if (isCurrent) colorVal.copy(alpha = 0.2f) else SteelGray.copy(alpha = 0.3f),
                                                border = BorderStroke(1.dp, if (isCurrent) colorVal else Color.Transparent)
                                            ) {
                                                Text(
                                                    text = labelText.split(" ")[0],
                                                    color = if (isCurrent) colorVal else Color.LightGray,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                    
                                    // Create Daily Log Pre-filled Action Button
                                    Button(
                                        onClick = {
                                            prefillTaskDescription = item.taskAm
                                            prefillStandardId = item.standardRefId
                                            prefillCheckpoint = item.clauseOrCheckpoint
                                            prefillMeasuredValue = item.recommendedValue
                                            prefillStatus = when (status) {
                                                "PASSED" -> "ተገዢ (COMPLIANT)"
                                                "FAILED" -> "ያልተገዢ (NON-COMPLIANT)"
                                                else -> "ተገዢ (COMPLIANT)"
                                            }
                                            showAddDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AmharicYellow),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(28.dp).testTag("checklist_log_btn_${item.id}")
                                    ) {
                                        Icon(Icons.Default.FactCheck, contentDescription = null, tint = SteelGray, modifier = Modifier.size(11.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ሪፖርት መዝግብ", color = SteelGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Log Dialog
    if (showAddDialog) {
        AddDailyLogDialog(
            projects = projects,
            onDismiss = { showAddDialog = false },
            initialTaskDescription = prefillTaskDescription,
            initialStandardId = prefillStandardId,
            initialCheckpointPassed = prefillCheckpoint,
            initialMeasuredValue = prefillMeasuredValue,
            initialComplianceStatus = prefillStatus,
            onSave = { projectId, description, quantity, standardId, checkpoint, measured, status, notes, audioPath ->
                viewModel.addDailyProgress(
                    projectId = projectId,
                    taskDescription = description,
                    workQuantity = quantity,
                    standardRefId = standardId,
                    checkpointPassed = checkpoint,
                    measuredValue = measured,
                    status = status,
                    notes = notes,
                    audioPath = audioPath
                )
                showAddDialog = false
                Toast.makeText(context, "ዕለታዊ ግንባታ መረጃ በተገቢው ሁኔታ ተመዝግቧል!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete confirmation Dialog
    if (logToDelete != null) {
        AlertDialog(
            onDismissRequest = { logToDelete = null },
            title = { Text("መዝገብ መሰረዝ", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = { Text("የተመረጠውን የዕለት ግንባታ መከታተያ መዝገብ በእርግጥ መሰረዝ ይፈልጋሉ?", color = Color.LightGray, fontSize = 13.sp) },
            confirmButton = {
                TextButton(
                    onClick = {
                        logToDelete?.let { viewModel.deleteDailyProgress(it) }
                        logToDelete = null
                        Toast.makeText(context, "መዝገቡ ተሰርዟል", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("አዎ ሰርዝ", color = AmharicRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { logToDelete = null }) {
                    Text("ተው", color = Color.LightGray)
                }
            },
            containerColor = CardBackground
        )
    }
}

@Composable
fun StatItem(title: String, value: String, color: Color) {
    Column {
        Text(text = title, color = Color.LightGray, fontSize = 10.sp)
        Text(text = value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DailyProgressLogCard(
    log: DailyProgress,
    projectName: String,
    standardName: String,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val formattedDate = remember(log.date) { dateFormatter.format(Date(log.date)) }

    val statusTheme = when {
        log.status.contains("COMPLIANT") || log.status.contains("ተገዢ") -> Triple(AmharicGreen, Icons.Default.CheckCircle, "ተገዢ (Compliant)")
        log.status.contains("WARNING") || log.status.contains("ማስጠንቀቂያ") -> Triple(AmharicYellow, Icons.Default.Warning, "ማስጠንቀቂያ (Warning)")
        else -> Triple(AmharicRed, Icons.Default.Error, "ያልተገዢ (Non-Compliant)")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_log_card_${log.id}"),
        colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Status & Project Name, Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = statusTheme.first.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = statusTheme.second,
                                    contentDescription = null,
                                    tint = statusTheme.first,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = statusTheme.third,
                                    color = statusTheme.first,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = projectName,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Date and trash action
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (log.isSynced) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "በዳታቤዝ ላይ ተቀምጧል",
                            tint = AmharicGreen,
                            modifier = Modifier.size(13.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "በመሳሪያው ላይ ተቀምጧል (Unsynced)",
                            tint = AmharicYellow,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Text(
                        text = formattedDate,
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(24.dp).testTag("delete_log_btn_${log.id}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "ሰርዝ", tint = Color.Gray.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                    }
                }
            }

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))

            // Body Row: Task & Quantity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = log.taskDescription,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = SteelGray,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = log.workQuantity,
                        color = AmharicYellow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Compliance details mapping
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SteelGray.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Code Category
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Bookmark, contentDescription = null, tint = AmharicYellow, modifier = Modifier.size(11.dp))
                    Text(
                        text = buildString {
                            append("የማመሳከሪያ ደረጃ፡ ")
                            append(standardName)
                        },
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Checkpoint
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = AmharicGreen, modifier = Modifier.size(11.dp).padding(top = 1.dp))
                    Column {
                        Text(
                            text = "የተገመገመው ድንጋጌ፡",
                            color = Color.Gray,
                            fontSize = 9.sp
                        )
                        Text(
                            text = log.checkpointPassed,
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                }

                // Measured Value
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Straighten, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(11.dp))
                    Text(
                        text = buildString {
                            append("የተለካው እሴት/መለኪያ፡ ")
                            append(log.measuredValue)
                        },
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )
                }
            }

            // Extra notes
            if (log.notes.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = AmharicYellow,
                        modifier = Modifier.size(12.dp).padding(top = 2.dp)
                    )
                    Text(
                        text = "ተጨማሪ ማስታወሻ፡ ${log.notes}",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            // Audio Note Player inside Card
            if (!log.audioPath.isNullOrEmpty()) {
                var isLogPlaying by remember { mutableStateOf(false) }
                val audioHelper = remember { AudioRecorderHelper(context) }
                val scope = rememberCoroutineScope()

                DisposableEffect(Unit) {
                    onDispose {
                        audioHelper.stopPlayback()
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(AmharicYellow.copy(alpha = 0.1f))
                        .border(BorderStroke(1.dp, AmharicYellow.copy(alpha = 0.2f)), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "የድምጽ ማስታወሻ",
                            tint = AmharicYellow,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isLogPlaying) "የድምጽ ማስታወሻውን በማጫወት ላይ..." else "የድምጽ ማስታወሻ (Audio Dictation)",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(
                        onClick = {
                            if (isLogPlaying) {
                                audioHelper.stopPlayback()
                                isLogPlaying = false
                            } else {
                                val file = File(log.audioPath)
                                if (file.exists() && file.length() > 25) { // Real recorded file
                                    val played = audioHelper.startPlayback(log.audioPath) {
                                        isLogPlaying = false
                                    }
                                    if (played) {
                                        isLogPlaying = true
                                    }
                                } else {
                                    // Fallback / Simulation playback for mock recorded files
                                    isLogPlaying = true
                                    Toast.makeText(context, "የድምፅ ማስታወሻውን ማጫወት ተጀምሯል (የተመሰለ)", Toast.LENGTH_SHORT).show()
                                    scope.launch {
                                        kotlinx.coroutines.delay(4000)
                                        isLogPlaying = false
                                        Toast.makeText(context, "የድምፅ ማጫወት ተጠናቋል", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.size(28.dp).testTag("play_audio_btn_${log.id}")
                    ) {
                        Icon(
                            imageVector = if (isLogPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = if (isLogPlaying) "አቁም" else "አጫውት",
                            tint = AmharicYellow,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Inspector Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(10.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "ተቆጣጣሪ፡ ${log.inspectorName}",
                    color = Color.Gray,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDailyLogDialog(
    projects: List<Project>,
    onDismiss: () -> Unit,
    initialTaskDescription: String = "",
    initialStandardId: String = "",
    initialCheckpointPassed: String = "",
    initialMeasuredValue: String = "",
    initialComplianceStatus: String = "ተገዢ (COMPLIANT)",
    onSave: (
        projectId: Int,
        description: String,
        quantity: String,
        standardId: String,
        checkpoint: String,
        measured: String,
        status: String,
        notes: String,
        audioPath: String?
    ) -> Unit
) {
    val context = LocalContext.current
    if (projects.isEmpty()) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("ምንም ፕሮጀክት የለም", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("እባክዎ መጀመሪያ ግንባታ/ፕሮጀክት ይመዝግቡ።", color = Color.LightGray, fontSize = 12.sp, textAlign = TextAlign.Center)
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = AmharicYellow)) {
                        Text("እሺ", color = SteelGray)
                    }
                }
            }
        }
        return
    }

    var selectedProject by remember { mutableStateOf(projects.first()) }
    var taskDescription by remember { mutableStateOf(initialTaskDescription) }
    var workQuantity by remember { mutableStateOf("") }
    
    // Standards binding
    val initialStdItem = allLibraryItems.find { it.id == initialStandardId } ?: allLibraryItems.first()
    var selectedStandard by remember { mutableStateOf(initialStdItem) }
    var selectedCheckpoint by remember { mutableStateOf(if (initialCheckpointPassed.isNotEmpty()) initialCheckpointPassed else (selectedStandard.complianceCheckpoints.firstOrNull() ?: "")) }

    var measuredValue by remember { mutableStateOf(initialMeasuredValue) }
    var complianceStatus by remember { mutableStateOf(initialComplianceStatus) } // Default status
    var observationNotes by remember { mutableStateOf("") }

    val audioRecorderHelper = remember { AudioRecorderHelper(context) }
    val audioPermissionState = rememberPermissionState(android.Manifest.permission.RECORD_AUDIO)
    val recordedFile = remember { mutableStateOf<File?>(null) }
    val isRecording = remember { mutableStateOf(false) }
    val isPlaying = remember { mutableStateOf(false) }
    val recordingDuration = remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isRecording.value) {
        if (isRecording.value) {
            while (isRecording.value && recordingDuration.value < 30) {
                kotlinx.coroutines.delay(1000)
                recordingDuration.value += 1
            }
            if (recordingDuration.value >= 30) {
                audioRecorderHelper.stopRecording()
                isRecording.value = false
                Toast.makeText(context, "ከፍተኛ የድምፅ ቀረጻ ርዝመት (30 ሰከንዶች) ደርሷል!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioRecorderHelper.stopRecording()
            audioRecorderHelper.stopPlayback()
        }
    }

    // Quick suggestions for tasks
    val taskSuggestions = listOf("የኮንክሪት ስራ (Concrete pouring)", "የብረት ስራ (Reinforcement)", "የግድግዳ ግንባታ (Masonry blockwork)", "የቁፋሮ ስራ (Excavation)", "የጣሪያ ስራ (Roofing truss)")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .padding(vertical = 16.dp)
                .testTag("add_daily_log_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "አዲስ የዕለት ግንባታ ቁጥጥር መመዝገቢያ",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "ዝጋ", tint = Color.LightGray)
                    }
                }

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

                // Form Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Project Selector
                    var projectExpanded by remember { mutableStateOf(false) }
                    Text("ፕሮጀክት / ግንባታ ምርጣ", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedProject.name,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().testTag("form_project_selector"),
                            trailingIcon = {
                                IconButton(onClick = { projectExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AmharicYellow)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AmharicYellow,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )
                        DropdownMenu(
                            expanded = projectExpanded,
                            onDismissRequest = { projectExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f).background(CardBackground)
                        ) {
                            projects.forEach { proj ->
                                DropdownMenuItem(
                                    text = { Text(proj.name, color = Color.White, fontSize = 11.sp) },
                                    onClick = {
                                        selectedProject = proj
                                        projectExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // 2. Task Description & Quick chips
                    Text("የተከናወነ ግንባታ/ስራ (Task Description)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = taskDescription,
                        onValueChange = { taskDescription = it },
                        placeholder = { Text("ለምሳሌ፡ ምሰሶዎች ላይ ኮንክሪት መሙላት...", color = Color.Gray, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("form_task_description"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AmharicYellow,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        taskSuggestions.forEach { suggestion ->
                            Surface(
                                modifier = Modifier.clickable { taskDescription = suggestion },
                                color = SteelGray,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = suggestion,
                                    color = Color.LightGray,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // 3. Work Quantity
                    Text("የስራ መጠን / ልኬት (Work Quantity)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = workQuantity,
                        onValueChange = { workQuantity = it },
                        placeholder = { Text("ለምሳሌ፡ 24 m³, 180 m², 1.5 Tons", color = Color.Gray, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("form_work_quantity"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AmharicYellow,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )

                    // 4. Standard Reference Selector
                    var standardExpanded by remember { mutableStateOf(false) }
                    Text("የመመዘኛ ኮድ / አዋጅ (Standard Reference)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedStandard.titleAm,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().testTag("form_standard_selector"),
                            trailingIcon = {
                                IconButton(onClick = { standardExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AmharicYellow)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AmharicYellow,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )
                        DropdownMenu(
                            expanded = standardExpanded,
                            onDismissRequest = { standardExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f).background(CardBackground)
                        ) {
                            allLibraryItems.forEach { standard ->
                                DropdownMenuItem(
                                    text = { Text(standard.titleAm, color = Color.White, fontSize = 11.sp) },
                                    onClick = {
                                        selectedStandard = standard
                                        selectedCheckpoint = standard.complianceCheckpoints.firstOrNull() ?: ""
                                        standardExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // 5. Checkpoint Selector
                    var checkpointExpanded by remember { mutableStateOf(false) }
                    Text("ተገዢ መሆን ያለበት ድንጋጌ (Standard Checkpoint)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedCheckpoint,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().testTag("form_checkpoint_selector"),
                            trailingIcon = {
                                IconButton(onClick = { checkpointExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AmharicYellow)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AmharicYellow,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )
                        DropdownMenu(
                            expanded = checkpointExpanded,
                            onDismissRequest = { checkpointExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f).background(CardBackground)
                        ) {
                            selectedStandard.complianceCheckpoints.forEach { check ->
                                DropdownMenuItem(
                                    text = { Text(check, color = Color.White, fontSize = 11.sp) },
                                    onClick = {
                                        selectedCheckpoint = check
                                        checkpointExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // 6. Measured value
                    Text("በግንባታ ቦታ የተለካው እሴት (Measured Value / Field Observation)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = measuredValue,
                        onValueChange = { measuredValue = it },
                        placeholder = { Text("ለምሳሌ፡ Slump: 85mm, cover: 35mm, C25 Cube", color = Color.Gray, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("form_measured_value"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AmharicYellow,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )

                    // 7. Status Radio buttons
                    Text("የተገዢነት ሁኔታ (Compliance Status)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val statusOptions = listOf(
                            "ተገዢ (COMPLIANT)" to AmharicGreen,
                            "ማስጠንቀቂያ (WARNING)" to AmharicYellow,
                            "ያልተገዢ (NON-COMPLIANT)" to AmharicRed
                        )

                        statusOptions.forEach { (label, color) ->
                            val isSelected = complianceStatus == label
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { complianceStatus = label },
                                color = if (isSelected) color.copy(alpha = 0.2f) else SteelGray.copy(alpha = 0.5f),
                                border = BorderStroke(1.5.dp, if (isSelected) color else Color.Transparent)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label.split(" ")[0],
                                        color = if (isSelected) color else Color.LightGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Audio Dictation Note Section
                    Text("የድምጽ ማስታወሻ መቅረጫ (Audio Dictation Note)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("audio_dictation_card"),
                        colors = CardDefaults.cardColors(containerColor = SteelGray.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (recordedFile.value == null) {
                                // Not recorded yet
                                if (isRecording.value) {
                                    // Recording state
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Red pulsing recording dot
                                        val pulseTransition = rememberInfiniteTransition(label = "pulse")
                                        val pulseAlpha by pulseTransition.animateFloat(
                                            initialValue = 0.3f,
                                            targetValue = 1.0f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(800, easing = LinearEasing),
                                                repeatMode = RepeatMode.Reverse
                                            ),
                                            label = "alpha"
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(AmharicRed.copy(alpha = pulseAlpha), CircleShape)
                                        )
                                        Text(
                                            "ድምጽ እየተቀረጸ ነው... ${recordingDuration.value}s / 30s",
                                            color = AmharicRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    // Animated audio wave
                                    Row(
                                        modifier = Modifier.height(30.dp),
                                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        repeat(10) { index ->
                                            val waveHeight = remember { mutableStateOf(10f) }
                                            LaunchedEffect(isRecording.value) {
                                                while (isRecording.value) {
                                                    waveHeight.value = (10..30).random().toFloat()
                                                    kotlinx.coroutines.delay(120)
                                                }
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .width(3.dp)
                                                    .height(waveHeight.value.dp)
                                                    .background(AmharicRed, RoundedCornerShape(1.dp))
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            audioRecorderHelper.stopRecording()
                                            isRecording.value = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AmharicRed),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Stop, contentDescription = "አቁም")
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("ቀረጻ አቁም (Stop)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    // Idle state: Record buttons
                                    Text(
                                        "በግንባታ ሜዳ ላይ መተየብ ሲከብድዎት የቁጥጥር መረጃውን በድምጽ መቅረጽ ይችላሉ።",
                                        color = Color.LightGray,
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                if (audioPermissionState.status.isGranted) {
                                                    val tempFile = File(context.cacheDir, "audio_progress_${System.currentTimeMillis()}.mp4")
                                                    val started = audioRecorderHelper.startRecording(tempFile)
                                                    if (started) {
                                                        recordedFile.value = tempFile
                                                        isRecording.value = true
                                                        recordingDuration.value = 0
                                                    } else {
                                                        // Failed starting (usually mic missing in emulator). Auto fallback to mock recording
                                                        val mockFile = File(context.cacheDir, "audio_progress_mock.mp4")
                                                        mockFile.writeText("MOCK AUDIO DATA")
                                                        recordedFile.value = mockFile
                                                        isRecording.value = true
                                                        recordingDuration.value = 0
                                                        Toast.makeText(context, "ማይክራፎን ባለመኖሩ አስመሳይ ቀረጻ (Simulation) ተጀምሯል", Toast.LENGTH_SHORT).show()
                                                    }
                                                } else {
                                                    audioPermissionState.launchPermissionRequest()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = AmharicYellow),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Mic, contentDescription = "ቅረጽ", tint = SteelGray)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("ቅረጽ (Record)", color = SteelGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        // Fallback dictation simulation
                                        OutlinedButton(
                                            onClick = {
                                                val mockFile = File(context.cacheDir, "audio_progress_simulated_${System.currentTimeMillis()}.mp4")
                                                mockFile.writeText("SIMULATED DICTATION NOTE")
                                                recordedFile.value = mockFile
                                                recordingDuration.value = 15
                                                Toast.makeText(context, "የድምጽ ማስታወሻ መዝገብ አስመሳይ በተሳካ ሁኔታ ተፈጥሯል (Simulated Dictation)", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AmharicYellow),
                                            border = BorderStroke(1.dp, AmharicYellow)
                                        ) {
                                            Icon(Icons.Default.VolumeUp, contentDescription = "አስመስል")
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("የድምጽ አስመሳይ (Simulate)", fontSize = 11.sp)
                                        }
                                    }
                                }
                            } else {
                                // Recorded successfully
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Audiotrack,
                                            contentDescription = null,
                                            tint = AmharicGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Column {
                                            Text(
                                                "የተቀረጸ የድምጽ ማስታወሻ",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "የቀረጻ ርዝመት፡ ${recordingDuration.value} ሰከንዶች",
                                                color = Color.LightGray,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Play / Stop Playback Button
                                        IconButton(
                                            onClick = {
                                                if (isPlaying.value) {
                                                    audioRecorderHelper.stopPlayback()
                                                    isPlaying.value = false
                                                } else {
                                                    val played = audioRecorderHelper.startPlayback(recordedFile.value!!.absolutePath) {
                                                        isPlaying.value = false
                                                    }
                                                    if (played) {
                                                        isPlaying.value = true
                                                    } else {
                                                        // Fallback simulated playback toast
                                                        isPlaying.value = true
                                                        Toast.makeText(context, "የድምጽ መዝገቡን በማጫወት ላይ...", Toast.LENGTH_SHORT).show()
                                                        coroutineScope.launch {
                                                            kotlinx.coroutines.delay(3000)
                                                            isPlaying.value = false
                                                            Toast.makeText(context, "የድምጽ ማጫወት ተጠናቋል", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (isPlaying.value) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                                contentDescription = "አጫውት",
                                                tint = AmharicYellow,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }

                                        // Delete/Trash button
                                        IconButton(
                                            onClick = {
                                                audioRecorderHelper.stopPlayback()
                                                isPlaying.value = false
                                                recordedFile.value?.delete()
                                                recordedFile.value = null
                                                recordingDuration.value = 0
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "አጥፋ",
                                                tint = AmharicRed,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 8. Observation notes
                    Text("ተጨማሪ ማስታወሻዎች / የመፍትሄ ሃሳቦች (Notes / Observations)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = observationNotes,
                        onValueChange = { observationNotes = it },
                        placeholder = { Text("ለምሳሌ፡ የተለካው ውፍረት ተቀባይነት አለው፤ ስራው እንዲቀጥል ተፈቅዷል...", color = Color.Gray, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().height(80.dp).testTag("form_notes"),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AmharicYellow,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )
                }

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("ተው", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            if (taskDescription.trim().isEmpty() || workQuantity.trim().isEmpty() || measuredValue.trim().isEmpty()) {
                                Toast.makeText(context, "እባክዎ ሁሉንም አስፈላጊ መረጃዎችን ያስገቡ!", Toast.LENGTH_SHORT).show()
                            } else {
                                onSave(
                                    selectedProject.id,
                                    taskDescription,
                                    workQuantity,
                                    selectedStandard.id,
                                    selectedCheckpoint,
                                    measuredValue,
                                    complianceStatus,
                                    observationNotes,
                                    recordedFile.value?.absolutePath
                                )
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.5f).testTag("submit_daily_log_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = AmharicYellow, contentColor = SteelGray)
                    ) {
                        Text("መዝግብ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
