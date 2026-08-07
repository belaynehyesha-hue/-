package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Project
import com.example.ui.viewmodel.ConstructionViewModel

// Data model representing a task in a construction phase
data class PhaseTask(
    val id: String,
    val phaseId: String, // "foundation", "structural", "finishing"
    val nameAm: String,
    val nameEn: String,
    val weight: Int, // percentage contribution (summing to 100 overall)
    val description: String,
    val standardRef: String // EBCS/ESEN reference
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhaseTrackingDashboardComponent(
    modifier: Modifier = Modifier,
    viewModel: ConstructionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val projects by viewModel.projects.collectAsState()
    
    // Track selected project
    var selectedProjectIndex by remember { mutableStateOf(0) }
    val selectedProject = if (projects.isNotEmpty() && selectedProjectIndex < projects.size) {
        projects[selectedProjectIndex]
    } else null

    // Definitions of all tasks across the 3 main phases
    val allTasks = remember {
        listOf(
            // 1. Foundation Phase (35% total)
            PhaseTask(
                id = "found-1",
                phaseId = "foundation",
                nameAm = "የመሬት ቁፋሮና ድጋፍ ስራ",
                nameEn = "Excavation & Shoring Work",
                weight = 8,
                description = "ጥልቀት፣ የአፈር መሸከም አቅም ፍተሻ እና የአፈር መደርመስ መከላከያ (EBCS-1)",
                standardRef = "ESEN 1997 / EBCS-1"
            ),
            PhaseTask(
                id = "found-2",
                phaseId = "foundation",
                nameAm = "የንፁህ ኮንክሪት ንጣፍ (ብሎንኬት)",
                nameEn = "Lean Concrete Casting",
                weight = 7,
                description = "ለመሠረት ድጋፍ የሚውል ዝቅተኛ ጥንካሬ ያለው የንፁህ ኮንክሪት ንጣፍ ውፍረት ፍተሻ",
                standardRef = "EBCS-2"
            ),
            PhaseTask(
                id = "found-3",
                phaseId = "foundation",
                nameAm = "የመሠረት ብረትና ፎርምወርክ",
                nameEn = "Footing Reinforcement & Formwork",
                weight = 10,
                description = "የብረት ርዝመት፣ ውፍረትና ክፍተት እንዲሁም የኮንክሪት ሽፋን (ክሊራንስ ከ50ሚሜ በላይ)",
                standardRef = "EBCS-2"
            ),
            PhaseTask(
                id = "found-4",
                phaseId = "foundation",
                nameAm = "የመሠረት ኮንክሪት ማፍሰስና ውሃ ማጠጣት",
                nameEn = "Concrete Pouring & Curing",
                weight = 10,
                description = "የኮንክሪት ጥንካሬ C25/30 ፍተሻና ለ7 ቀናት በተከታታይ ውሃ ማጠጣት",
                standardRef = "EBCS-2"
            ),

            // 2. Structural Phase (40% total)
            PhaseTask(
                id = "struct-1",
                phaseId = "structural",
                nameAm = "የምሰሶዎች (ኮለምን) አርማታ ስራ",
                nameEn = "Column Casting",
                weight = 10,
                description = "የአቀባዊነት (Plumbness) ትክክለኛነት፣ የምሰሶዎች ብረት ስራና የኮንክሪት ጥራት ፍተሻ",
                standardRef = "EBCS-2 / EBCS-1"
            ),
            PhaseTask(
                id = "struct-2",
                phaseId = "structural",
                nameAm = "የቢም እና ስላብ ፎርምወርክ (እርግብ ስራ)",
                nameEn = "Beams & Slab Shuttering",
                weight = 10,
                description = "የፎርምወርክ ደረጃ፣ ድጋፎች (Props) እና የቢም ቅርጽ ልኬት ትክክለኛነት ፍተሻ",
                standardRef = "EBCS-2"
            ),
            PhaseTask(
                id = "struct-3",
                phaseId = "structural",
                nameAm = "የስላብ ብረት ስራ እና ኮንክሪት ማፍሰስ",
                nameEn = "Slab Reinforcement & Casting",
                weight = 12,
                description = "ባለ ሁለት ረድፍ ብረት ዝግጅት፣ የስፔሰር ዝግጅት እና የኮንክሪት ርግብግቢት (Vibrator) አጠቃቀም",
                standardRef = "EBCS-2"
            ),
            PhaseTask(
                id = "struct-4",
                phaseId = "structural",
                nameAm = "የግድግዳ ጡብ/ብሎኬት መደርደር",
                nameEn = "Block Work & Brick Laying",
                weight = 8,
                description = "ከምሰሶዎች ጋር ያለው ትስስር (Anchoring bars) እና የሞርታር ውህድ ጥምርታ ፍተሻ",
                standardRef = "EBCS-2 / BATCODA"
            ),

            // 3. Finishing Phase (25% total)
            PhaseTask(
                id = "finish-1",
                phaseId = "finishing",
                nameAm = "የግድግዳ ፕላስተር/ለሰና ስራ",
                nameEn = "Wall Plastering",
                weight = 7,
                description = "የግድግዳ ልስን አቀባዊነት ትክክለኛነት፣ ውፍረት ፍተሻ እና ለ3 ቀናት ውሃ ማጠጣት",
                standardRef = "EBCS-2 / BATCODA"
            ),
            PhaseTask(
                id = "finish-2",
                phaseId = "finishing",
                nameAm = "የቧንቧና መብራት መስመር ዝርጋታ",
                nameEn = "Plumbing & Electrical Roughing",
                weight = 6,
                description = "የቧንቧዎች የውሃ ግፊት ሙከራ (Pressure Test) እና የኤሌክትሪክ ኮንዱይቶች ዝርጋታ ፍተሻ",
                standardRef = "EBCS-10 / ESEN 1996"
            ),
            PhaseTask(
                id = "finish-3",
                phaseId = "finishing",
                nameAm = "የወለልና ግድግዳ ሸክላ/ኮንክሪት ንጣፍ",
                nameEn = "Tiling & Flooring",
                weight = 6,
                description = "የወለል ተዳፋት (Slope)፣ የሸክላዎች ትስስር እና የቁመታዊነት አሰላለፍ ፍተሻ",
                standardRef = "BATCODA Standard"
            ),
            PhaseTask(
                id = "finish-4",
                phaseId = "finishing",
                nameAm = "የቀለምና የመጨረሻ ማጠናቀቂያ እቃዎች መግጠም",
                nameEn = "Painting & Final Fixtures",
                weight = 6,
                description = "የመጨረሻ ቀለም ቅባት ጥራት፣ የበርና መስኮት እቃዎች መግጠም እና የፅዳት ስራዎች",
                standardRef = "BATCODA Standard"
            )
        )
    }

    // Load completed tasks state
    val sharedPrefs = remember(context) { context.getSharedPreferences("project_phase_tracking_v1", Context.MODE_PRIVATE) }
    
    // Local completed tasks set for the selected project
    var completedTaskIds by remember { mutableStateOf(setOf<String>()) }
    
    // Dropdown expansion state
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Synchronize tasks when selected project changes
    LaunchedEffect(selectedProject?.id, selectedProject?.scheduleProgressPercent) {
        if (selectedProject != null) {
            val savedSet = sharedPrefs.getStringSet("project_tasks_${selectedProject.id}", null)
            if (savedSet != null) {
                completedTaskIds = savedSet
            } else {
                // If no saved task list, seed tasks based on the current scheduleProgressPercent
                val seeded = mutableSetOf<String>()
                var currentWeightSum = 0
                for (task in allTasks) {
                    if (currentWeightSum + task.weight <= selectedProject.scheduleProgressPercent + 1) {
                        seeded.add(task.id)
                        currentWeightSum += task.weight
                    }
                }
                completedTaskIds = seeded
                sharedPrefs.edit().putStringSet("project_tasks_${selectedProject.id}", seeded).apply()
            }
        }
    }

    // Individual progress calculation
    val foundationTasks = allTasks.filter { it.phaseId == "foundation" }
    val structuralTasks = allTasks.filter { it.phaseId == "structural" }
    val finishingTasks = allTasks.filter { it.phaseId == "finishing" }

    val foundationWeight = foundationTasks.sumOf { it.weight } // 35
    val structuralWeight = structuralTasks.sumOf { it.weight } // 40
    val finishingWeight = finishingTasks.sumOf { it.weight } // 25

    val completedFoundationWeight = foundationTasks.filter { it.id in completedTaskIds }.sumOf { it.weight }
    val completedStructuralWeight = structuralTasks.filter { it.id in completedTaskIds }.sumOf { it.weight }
    val completedFinishingWeight = finishingTasks.filter { it.id in completedTaskIds }.sumOf { it.weight }

    val foundationProgress = if (foundationWeight > 0) completedFoundationWeight.toFloat() / foundationWeight else 0f
    val structuralProgress = if (structuralWeight > 0) completedStructuralWeight.toFloat() / structuralWeight else 0f
    val finishingProgress = if (finishingWeight > 0) completedFinishingWeight.toFloat() / finishingWeight else 0f

    // Expand/Collapse states for the three phases
    var foundationExpanded by remember { mutableStateOf(true) }
    var structuralExpanded by remember { mutableStateOf(false) }
    var finishingExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("phase_tracking_dashboard_component"),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(AmharicGreen.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TrackChanges,
                        contentDescription = null,
                        tint = AmharicGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "የግንባታ ደረጃዎች መከታተያ ሰሌዳ",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Phase-Based Progress Tracking (EBCS Compliance)",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
            }

            Text(
                text = "የተመዘገቡ ግንባታዎችን የመሠረት፣ የመዋቅር እና የማጠናቀቂያ ምዕራፎች አፈጻጸም እዚህ ጋር በዝርዝር መከታተልና ማዘመን ይችላሉ።",
                color = Color.Gray,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))

            if (selectedProject == null) {
                // Empty state if no projects
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "ምንም ግንባታ አልተመዘገበም።",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "እባክዎ መጀመሪያ ከላይ 'ግንባታ መዝግብ' በሚለው ቁልፍ ፕሮጀክት ይመዝግቡ።",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Dropdown Selector for active project
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "ለመከታተል የተመረጠው ግንባታ (Selected Project):",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SteelGray.copy(alpha = 0.4f))
                            .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                            .clickable { dropdownExpanded = !dropdownExpanded }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
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
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = AmharicYellow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Column {
                                    Text(
                                        text = selectedProject.name,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${selectedProject.location} • ምድብ ${selectedProject.buildingCategory}",
                                        color = Color.LightGray,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    color = if (selectedProject.status == "የተጠናቀቀ") AmharicGreen.copy(alpha = 0.15f) else ConstructionTeal.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, if (selectedProject.status == "የተጠናቀቀ") AmharicGreen else ConstructionTeal)
                                ) {
                                    Text(
                                        text = "${selectedProject.scheduleProgressPercent}%",
                                        color = if (selectedProject.status == "የተጠናቀቀ") AmharicGreen else AmharicYellow,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Icon(
                                    imageVector = if (dropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(CardBackground)
                                .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        ) {
                            projects.forEachIndexed { index, project ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(project.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                Text(project.location, color = Color.Gray, fontSize = 10.sp)
                                            }
                                            Text(
                                                text = "${project.scheduleProgressPercent}%",
                                                color = AmharicYellow,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedProjectIndex = index
                                        dropdownExpanded = false
                                    },
                                    modifier = Modifier.background(
                                        if (index == selectedProjectIndex) SteelGray.copy(alpha = 0.3f) else Color.Transparent
                                    )
                                )
                            }
                        }
                    }
                }

                // Helper local function to update project status in ViewModel/DB
                val onTaskToggle: (String, Boolean) -> Unit = { taskId, isChecked ->
                    val newCompleted = if (isChecked) {
                        completedTaskIds + taskId
                    } else {
                        completedTaskIds - taskId
                    }
                    completedTaskIds = newCompleted
                    
                    // Save to SharedPreferences
                    sharedPrefs.edit().putStringSet("project_tasks_${selectedProject.id}", newCompleted).apply()

                    // Compute overall percentage
                    val overallProgress = allTasks.filter { it.id in newCompleted }.sumOf { it.weight }
                    
                    // Automatically adjust project status
                    val status = when {
                        overallProgress == 100 -> "የተጠናቀቀ"
                        overallProgress > 0 -> "በግንባታ ላይ"
                        else -> "በዝግጅት ላይ"
                    }

                    // Toast message when milestones are reached
                    if (overallProgress == 100 && selectedProject.scheduleProgressPercent < 100) {
                        Toast.makeText(context, "እንኳን ደስ አላችሁ! 🥳 '${selectedProject.name}' ግንባታ 100% ተጠናቋል።", Toast.LENGTH_LONG).show()
                    } else if (taskId == "found-4" && isChecked && "found-4" !in completedTaskIds) {
                        Toast.makeText(context, "የመሠረት ግንባታ ምዕራፍ በተሳካ ሁኔታ ተጠናቋል! 🧱", Toast.LENGTH_SHORT).show()
                    } else if (taskId == "struct-3" && isChecked && "struct-3" !in completedTaskIds) {
                        Toast.makeText(context, "የስላብ አርማታ ስራ ተጠናቋል! የህንፃው መዋቅር ወደ ማጠናቀቂያ ተሸጋግሯል። 🏢", Toast.LENGTH_SHORT).show()
                    }

                    // Write back to DB
                    viewModel.updateProject(selectedProject.copy(
                        scheduleProgressPercent = overallProgress,
                        status = status
                    ))
                }

                // Cumulative statistics summary
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SteelGray.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PhaseMiniStat(
                            title = "መሠረት (35%)",
                            progress = foundationProgress,
                            color = AmharicYellow,
                            completedWeight = completedFoundationWeight,
                            totalWeight = foundationWeight
                        )
                        Box(modifier = Modifier.size(1.dp, 30.dp).background(Color.Gray.copy(alpha = 0.2f)))
                        PhaseMiniStat(
                            title = "መዋቅር (40%)",
                            progress = structuralProgress,
                            color = ConstructionTeal,
                            completedWeight = completedStructuralWeight,
                            totalWeight = structuralWeight
                        )
                        Box(modifier = Modifier.size(1.dp, 30.dp).background(Color.Gray.copy(alpha = 0.2f)))
                        PhaseMiniStat(
                            title = "ማጠናቀቂያ (25%)",
                            progress = finishingProgress,
                            color = AmharicGreen,
                            completedWeight = completedFinishingWeight,
                            totalWeight = finishingWeight
                        )
                    }
                }

                // Render Phase 1: Foundation
                PhaseTrackingCard(
                    titleAm = "1. የመሠረት ሥራ ምዕራፍ",
                    titleEn = "Foundation Stage (35%)",
                    progress = foundationProgress,
                    isExpanded = foundationExpanded,
                    onToggleExpand = { foundationExpanded = !foundationExpanded },
                    tasks = foundationTasks,
                    completedTaskIds = completedTaskIds,
                    onTaskToggle = onTaskToggle,
                    color = AmharicYellow,
                    icon = Icons.Default.Layers
                )

                // Render Phase 2: Structural
                PhaseTrackingCard(
                    titleAm = "2. የመዋቅር ሥራ ምዕራፍ",
                    titleEn = "Structural Stage (40%)",
                    progress = structuralProgress,
                    isExpanded = structuralExpanded,
                    onToggleExpand = { structuralExpanded = !structuralExpanded },
                    tasks = structuralTasks,
                    completedTaskIds = completedTaskIds,
                    onTaskToggle = onTaskToggle,
                    color = ConstructionTeal,
                    icon = Icons.Default.HomeWork
                )

                // Render Phase 3: Finishing
                PhaseTrackingCard(
                    titleAm = "3. የማጠናቀቂያ ሥራ ምዕራፍ",
                    titleEn = "Finishing Stage (25%)",
                    progress = finishingProgress,
                    isExpanded = finishingExpanded,
                    onToggleExpand = { finishingExpanded = !finishingExpanded },
                    tasks = finishingTasks,
                    completedTaskIds = completedTaskIds,
                    onTaskToggle = onTaskToggle,
                    color = AmharicGreen,
                    icon = Icons.Default.Brush
                )

                // Quick actions for the phases
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            completedTaskIds = emptySet()
                            sharedPrefs.edit().putStringSet("project_tasks_${selectedProject.id}", emptySet()).apply()
                            viewModel.updateProject(selectedProject.copy(
                                scheduleProgressPercent = 0,
                                status = "በዝግጅት ላይ"
                            ))
                            Toast.makeText(context, "የግንባታ ምዕራፎች በሙሉ ተጀምረዋል (0%)", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AmharicRed),
                        border = BorderStroke(1.dp, AmharicRed.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ሁሉንም አፅዳ (Reset)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val allIds = allTasks.map { it.id }.toSet()
                            completedTaskIds = allIds
                            sharedPrefs.edit().putStringSet("project_tasks_${selectedProject.id}", allIds).apply()
                            viewModel.updateProject(selectedProject.copy(
                                scheduleProgressPercent = 100,
                                status = "የተጠናቀቀ"
                            ))
                            Toast.makeText(context, "ግንባታው ሙሉ በሙሉ ተጠናቋል! 💯", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ሁሉንም አጠናቅ (All Done)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                // Official Government Inspection Standard Notice
                Surface(
                    color = GoldAccent.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.5.dp, GoldAccent.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "🛡️ ይህ መከታተያ በEBCS የኢትዮጵያ የህንጻ ኮዶችና አዋጆች (Ministry of Construction) መሠረት የተቀረጸ የደረጃዎች ማረጋገጫ ሰሌዳ ነው።",
                            color = Color.LightGray,
                            fontSize = 9.sp,
                            lineHeight = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PhaseMiniStat(
    title: String,
    progress: Float,
    color: Color,
    completedWeight: Int,
    totalWeight: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = title,
            color = Color.Gray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "${(progress * 100).toInt()}%",
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "${completedWeight}/${totalWeight} pt",
            color = Color.LightGray.copy(alpha = 0.8f),
            fontSize = 9.sp
        )
    }
}

@Composable
fun PhaseTrackingCard(
    titleAm: String,
    titleEn: String,
    progress: Float,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    tasks: List<PhaseTask>,
    completedTaskIds: Set<String>,
    onTaskToggle: (String, Boolean) -> Unit,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val progressAnimate by animateFloatAsState(targetValue = progress, label = "progress")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SteelGray.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.1f))
    ) {
        Column {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = titleAm,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = titleEn,
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        color = color,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Sleek Phase Progress Bar
            LinearProgressIndicator(
                progress = { progressAnimate },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = color,
                trackColor = Color.Transparent
            )

            // Tasks Checklist Section (Expanded)
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tasks.forEach { task ->
                        val isChecked = task.id in completedTaskIds
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isChecked) color.copy(alpha = 0.05f) else Color.Transparent)
                                .border(
                                    0.5.dp,
                                    if (isChecked) color.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onTaskToggle(task.id, !isChecked) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Custom circular Material Checkbox block
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (isChecked) color else Color.Transparent)
                                    .border(1.5.dp, if (isChecked) color else Color.Gray.copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isChecked) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "ተጠናቋል",
                                        tint = if (color == AmharicYellow) SteelGray else Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = task.nameAm,
                                        color = if (isChecked) Color.White else Color.LightGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    // Weight bubble
                                    Surface(
                                        color = color.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.padding(start = 6.dp)
                                    ) {
                                        Text(
                                            text = "+${task.weight}%",
                                            color = color,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = task.nameEn,
                                    color = Color.Gray,
                                    fontSize = 9.sp
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = task.description,
                                    color = Color.LightGray.copy(alpha = 0.6f),
                                    fontSize = 9.sp,
                                    lineHeight = 13.sp
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                // Standard reference badge
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = GoldAccent.copy(alpha = 0.7f),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = "የሕግ ማጣቀሻ፡ ${task.standardRef}",
                                        color = GoldAccent.copy(alpha = 0.8f),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
