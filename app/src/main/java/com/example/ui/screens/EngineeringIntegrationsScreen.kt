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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Project
import com.example.ui.viewmodel.ConstructionViewModel
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

// Custom color palette for Engineering Softwares
val AutoCADColor = Color(0xFFE53935)
val RevitColor = Color(0xFF1E88E5)
val PrimaveraColor = Color(0xFFFB8C00)
val GISColor = Color(0xFF43A047)
val ETABSColor = Color(0xFF8E24AA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EngineeringIntegrationsScreen(
    projects: List<Project>,
    viewModel: ConstructionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedProject by remember { mutableStateOf<Project?>(null) }
    
    // Auto-select the first project if nothing is selected yet
    LaunchedEffect(projects) {
        if (selectedProject == null && projects.isNotEmpty()) {
            selectedProject = projects.first()
        }
    }

    // Tab state: 0: AutoCAD, 1: Revit BIM, 2: Primavera P6, 3: GIS Location, 4: ETABS Safety
    var activeSubTab by remember { mutableStateOf(0) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 680.dp

        if (isWideScreen) {
            // Tablet / Computer Landscape Layout (Dual-Pane)
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Pane: Selector & Software Tabs
                Card(
                    modifier = Modifier
                        .width(320.dp)
                        .fillMaxHeight()
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF242629)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "የምህንድስና ሲስተሞች መቆጣጠሪያ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Divider(color = Color.Gray.copy(alpha = 0.3f))
                        
                        // Project Dropdown/Selector Card
                        ProjectSelectorBlock(
                            projects = projects,
                            selectedProject = selectedProject,
                            onProjectSelected = { selectedProject = it }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "ለመገናኘት ሶፍትዌር ይምረጡ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.LightGray
                        )

                        // Vertical Sidebar Buttons for Softwares
                        SoftwareSidebarButton(
                            title = "AutoCAD / Civil 3D (DXF)",
                            icon = Icons.Default.GridOn,
                            color = AutoCADColor,
                            selected = activeSubTab == 0,
                            onClick = { activeSubTab = 0 }
                        )
                        SoftwareSidebarButton(
                            title = "Revit BIM (IFC)",
                            icon = Icons.Default.Layers,
                            color = RevitColor,
                            selected = activeSubTab == 1,
                            onClick = { activeSubTab = 1 }
                        )
                        SoftwareSidebarButton(
                            title = "Primavera P6 / MS Project",
                            icon = Icons.Default.Timeline,
                            color = PrimaveraColor,
                            selected = activeSubTab == 2,
                            onClick = { activeSubTab = 2 }
                        )
                        SoftwareSidebarButton(
                            title = "QGIS / ArcGIS (Geospatial)",
                            icon = Icons.Default.Map,
                            color = GISColor,
                            selected = activeSubTab == 3,
                            onClick = { activeSubTab = 3 }
                        )
                        SoftwareSidebarButton(
                            title = "ETABS / SAP2000 (Safety)",
                            icon = Icons.Default.Shield,
                            color = ETABSColor,
                            selected = activeSubTab == 4,
                            onClick = { activeSubTab = 4 }
                        )
                        SoftwareSidebarButton(
                            title = "የህንጻ ክትትልና ሰነዶች (Tracking)",
                            icon = Icons.Default.Assessment,
                            color = AmharicYellow,
                            selected = activeSubTab == 5,
                            onClick = { activeSubTab = 5 }
                        )
                    }
                }

                // Right Pane: Active Workspace Detail Display
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(vertical = 12.dp, horizontal = 4.dp)
                ) {
                    if (selectedProject == null) {
                        EmptyProjectPlaceholder()
                    } else {
                        ActiveIntegrationWorkspace(
                            tab = activeSubTab,
                            project = selectedProject!!,
                            viewModel = viewModel,
                            onDataSynced = { updatedProj ->
                                selectedProject = updatedProj
                            }
                        )
                    }
                }
            }
        } else {
            // Mobile Layout (Single-Pane Scrollable Layout with top tabs)
            Column(modifier = Modifier.fillMaxSize()) {
                // Dropdown Selector for Mobile
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF242629)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        ProjectSelectorBlock(
                            projects = projects,
                            selectedProject = selectedProject,
                            onProjectSelected = { selectedProject = it }
                        )
                    }
                }

                // Horizontal Scrollable Tabs for Softwares
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MobileSoftwareTab(
                        title = "AutoCAD",
                        icon = Icons.Default.GridOn,
                        color = AutoCADColor,
                        selected = activeSubTab == 0,
                        onClick = { activeSubTab = 0 }
                    )
                    MobileSoftwareTab(
                        title = "Revit BIM",
                        icon = Icons.Default.Layers,
                        color = RevitColor,
                        selected = activeSubTab == 1,
                        onClick = { activeSubTab = 1 }
                    )
                    MobileSoftwareTab(
                        title = "Primavera",
                        icon = Icons.Default.Timeline,
                        color = PrimaveraColor,
                        selected = activeSubTab == 2,
                        onClick = { activeSubTab = 2 }
                    )
                    MobileSoftwareTab(
                        title = "GIS Map",
                        icon = Icons.Default.Map,
                        color = GISColor,
                        selected = activeSubTab == 3,
                        onClick = { activeSubTab = 3 }
                    )
                    MobileSoftwareTab(
                        title = "ETABS Check",
                        icon = Icons.Default.Shield,
                        color = ETABSColor,
                        selected = activeSubTab == 4,
                        onClick = { activeSubTab = 4 }
                    )
                    MobileSoftwareTab(
                        title = "የህንጻ ክትትል",
                        icon = Icons.Default.Assessment,
                        color = AmharicYellow,
                        selected = activeSubTab == 5,
                        onClick = { activeSubTab = 5 }
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    if (selectedProject == null) {
                        EmptyProjectPlaceholder()
                    } else {
                        ActiveIntegrationWorkspace(
                            tab = activeSubTab,
                            project = selectedProject!!,
                            viewModel = viewModel,
                            onDataSynced = { updatedProj ->
                                selectedProject = updatedProj
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectSelectorBlock(
    projects: List<Project>,
    selectedProject: Project?,
    onProjectSelected: (Project) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "ክትትል የሚደረግበት ግንባታ",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.LightGray
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E2022), RoundedCornerShape(8.dp))
                .border(1.dp, Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .clickable { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = selectedProject?.name ?: "ምንም ፕሮጀክት አልተመረጠም",
                    color = Color.White,
                    fontSize = 13.sp,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = "ዝርዝር",
                    tint = Color.White
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(Color(0xFF242629))
            ) {
                projects.forEach { proj ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = proj.name,
                                color = Color.White,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        },
                        onClick = {
                            onProjectSelected(proj)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SoftwareSidebarButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) color.copy(alpha = 0.15f) else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) color else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) Color.White else Color.LightGray
            )
        }
    }
}

@Composable
fun MobileSoftwareTab(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))
            .background(if (selected) color else Color(0xFF242629))
            .border(
                width = 1.dp,
                color = if (selected) color else Color.LightGray.copy(alpha = 0.1f),
                shape = RoundedCornerShape(30.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Color.White else color,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else Color.LightGray
            )
        }
    }
}

@Composable
fun EmptyProjectPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Business,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "እባክዎን ከላይ ፕሮጀክት ይምረጡ",
                color = Color.LightGray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ActiveIntegrationWorkspace(
    tab: Int,
    project: Project,
    viewModel: ConstructionViewModel,
    onDataSynced: (Project) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF242629)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        when (tab) {
            0 -> AutoCADWorkspace(project, viewModel, onDataSynced)
            1 -> RevitBIMWorkspace(project, viewModel, onDataSynced)
            2 -> PrimaveraScheduleWorkspace(project, viewModel, onDataSynced)
            3 -> GISGeospatialWorkspace(project, viewModel, onDataSynced)
            4 -> ETABSStructuralWorkspace(project, viewModel, onDataSynced)
            5 -> BuildingTrackingWorkspace(project, viewModel, onDataSynced)
        }
    }
}

// ==================== 1. AUTOCAD WORKSPACE ====================
@Composable
fun AutoCADWorkspace(
    project: Project,
    viewModel: ConstructionViewModel,
    onDataSynced: (Project) -> Unit
) {
    val context = LocalContext.current
    var inputCoordinates by remember { mutableStateOf(project.cadCoordinates ?: "10.0,10.0; 50.0,10.0; 50.0,40.0; 10.0,40.0") }
    var parsedCoordinates by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var plotArea by remember { mutableStateOf(0.0) }

    // Parse G-code/DXF coordinates
    fun parseLayout() {
        try {
            val points = inputCoordinates.split(";").map {
                val coords = it.trim().split(",")
                Offset(coords[0].trim().toFloat(), coords[1].trim().toFloat())
            }
            if (points.size >= 3) {
                parsedCoordinates = points
                
                // Calculate Area using Shoelace algorithm
                var areaSum = 0.0
                val n = points.size
                for (i in 0 until n) {
                    val j = (i + 1) % n
                    areaSum += points[i].x * points[j].y
                    areaSum -= points[j].x * points[i].y
                }
                plotArea = Math.abs(areaSum) / 2.0
                Toast.makeText(context, "የ AutoCAD DXF ፋይል በተሳካ ሁኔታ ተተንትኗል! ስፋት፡ ${"%.2f".format(plotArea)} ካሬ ሜትር", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "ቢያንስ 3 መጋጠሚያ ነጥቦች (Vertices) ማስገባት አለብዎት!", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "የመጋጠሚያ ፎርማት ስህተት አለ። እባክዎ X,Y; X,Y ቅርፅ ይጠቀሙ።", Toast.LENGTH_LONG).show()
        }
    }

    // Auto-parse on load if exists
    LaunchedEffect(project.id) {
        if (!project.cadCoordinates.isNullOrBlank()) {
            inputCoordinates = project.cadCoordinates
            parseLayout()
        } else {
            parsedCoordinates = emptyList()
            plotArea = 0.0
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.GridOn, "AutoCAD", tint = AutoCADColor, modifier = Modifier.size(24.dp))
                Text("AutoCAD / Civil 3D DXF የዲዛይን ትስስር", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        item {
            Text(
                text = "የፕሎት መሬት ይዞታዎችንና የህንጻውን ወሰን የ CAD DXF ኮርዲኔት በቀጥታ እዚህ በማስገባት መጋጠሚያዎችን መሳል እና አጠቃላይ የፕሎት ስፋት ማግኘት ይችላሉ።",
                fontSize = 12.sp,
                color = Color.LightGray
            )
        }

        item {
            OutlinedTextField(
                value = inputCoordinates,
                onValueChange = { inputCoordinates = it },
                label = { Text("የ AutoCAD ወሰን መጋጠሚያዎች (X,Y; X,Y; ...)", color = AutoCADColor, fontSize = 12.sp) },
                placeholder = { Text("e.g., 10,10; 50,10; 50,40; 10,40") },
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontFamily = FontFamily.Monospace),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AutoCADColor,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                    focusedLabelColor = AutoCADColor
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { parseLayout() },
                    colors = ButtonDefaults.buttonColors(containerColor = AutoCADColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ዲዛይኑን ተንትን", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        viewModel.updateProjectEngineeringData(
                            projectId = project.id,
                            cad = inputCoordinates,
                            bim = null,
                            schedule = -1,
                            lat = null,
                            lon = null,
                            etabs = null
                        )
                        onDataSynced(project.copy(cadCoordinates = inputCoordinates))
                        Toast.makeText(context, "የ CAD ዲዛይን መረጃ ወደ ማህደሩ ገብቷል! 💾", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ወሰን ወደ ማህደር አስገባ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // CAD Drawing Canvas Grid
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2022)),
                border = BorderStroke(1.dp, AutoCADColor.copy(alpha = 0.3f))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        // Draw grid lines
                        val gridSpacing = 30f
                        for (x in 0..size.width.toInt() step gridSpacing.toInt()) {
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.1f),
                                start = Offset(x.toFloat(), 0f),
                                end = Offset(x.toFloat(), size.height),
                                strokeWidth = 1f
                            )
                        }
                        for (y in 0..size.height.toInt() step gridSpacing.toInt()) {
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.1f),
                                start = Offset(0f, y.toFloat()),
                                end = Offset(size.width, y.toFloat()),
                                strokeWidth = 1f
                            )
                        }

                        // Draw parsed CAD shapes
                        if (parsedCoordinates.isNotEmpty()) {
                            val scaledPoints = parsedCoordinates.map {
                                // Scale points to fit canvas bounding box comfortably
                                Offset(
                                    x = 40f + (it.x * 4.5f),
                                    y = 40f + (it.y * 3f)
                                )
                            }
                            
                            // Draw boundaries
                            for (i in scaledPoints.indices) {
                                val nextIndex = (i + 1) % scaledPoints.size
                                drawLine(
                                    color = AutoCADColor,
                                    start = scaledPoints[i],
                                    end = scaledPoints[nextIndex],
                                    strokeWidth = 4f
                                )
                                drawCircle(
                                    color = AmharicYellow,
                                    center = scaledPoints[i],
                                    radius = 6f
                                )
                            }
                        }
                    }

                    if (parsedCoordinates.isEmpty()) {
                        Text(
                            text = "ወሰኖችን ለመሳል ከላይ 'ዲዛይኑን ተንትን' ይጫኑ",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .padding(6.dp)
                        ) {
                            Text("የወሰን ነጥቦች: ${parsedCoordinates.size}", color = Color.White, fontSize = 10.sp)
                            Text("አጠቃላይ የቦታ ስፋት: ${"%.1f".format(plotArea)} m²", color = AutoCADColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==================== 2. REVIT BIM WORKSPACE ====================
@Composable
fun RevitBIMWorkspace(
    project: Project,
    viewModel: ConstructionViewModel,
    onDataSynced: (Project) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var activeModelIndex by remember { mutableStateOf(0) }
    
    val bimModels = listOf(
        BimModelTemplate("የመንግስት ህንጻ ዲዛይን v3", 45000000.0, "HCB (60,000 blocks), Concrete (1,200 m³), Rebar (45 tons)"),
        BimModelTemplate("መካከለኛ መኖሪያ ቤት v1.2", 18500000.0, "Red Brick (25,000 blocks), Concrete (450 m³), Steel (15 tons)"),
        BimModelTemplate("የንግድ ማእከል ፕላን v5", 95000000.0, "Glass Curtain, Concrete (3,500 m³), Steel Frame (120 tons)"),
        BimModelTemplate("ቀላል የገበያ ሼድ ፕላን", 1200000.0, "Corrugated Iron (350 sheets), Timber frames, Stone foundation")
    )

    val format = NumberFormat.getCurrencyInstance(Locale("en", "US")).apply {
        maximumFractionDigits = 0
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Layers, "Revit", tint = RevitColor, modifier = Modifier.size(24.dp))
                Text("Revit BIM (IFC) የግንባታ ማቴሪያልና ወጪ ትስስር", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        item {
            Text(
                text = "የህንጻውን የ Autodesk Revit BIM ሞዴል በቀጥታ በማገናኘት ዝርዝር ማቴሪያሎችንና የተገመተውን አጠቃላይ የግንባታ ወጪ መረጃ መተንተን ይችላሉ።",
                fontSize = 12.sp,
                color = Color.LightGray
            )
        }

        item {
            Text("ዲጂታል BIM ሞዴል ይምረጡ:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                bimModels.forEachIndexed { idx, model ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activeModelIndex == idx) RevitColor.copy(alpha = 0.15f) else Color(0xFF1E2022))
                            .border(1.dp, if (activeModelIndex == idx) RevitColor else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { activeModelIndex = idx }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(model.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("ማቴሪያል፡ ${model.materials}", color = Color.LightGray, fontSize = 11.sp, maxLines = 1)
                        }
                        Text(
                            text = "${format.format(model.cost).replace("$", "ETB ")}",
                            color = RevitColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    val model = bimModels[activeModelIndex]
                    viewModel.updateProjectEngineeringData(
                        projectId = project.id,
                        cad = null,
                        bim = model.cost,
                        schedule = -1,
                        lat = null,
                        lon = null,
                        etabs = null
                    )
                    
                    // Also update project's materials field in ViewModel if needed
                    coroutineScope.launch {
                        viewModel.updateProject(project.copy(
                            materials = model.materials,
                            bimEstimatedCost = model.cost
                        ))
                        onDataSynced(project.copy(materials = model.materials, bimEstimatedCost = model.cost))
                        Toast.makeText(context, "የ BIM ሞዴል ማቴሪያሎችና ወጪዎች በስኬት ተመሳስለዋል! 🔄", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RevitColor),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Sync, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("የማቴሪያልና ወጪ መረጃ አመሳስል (Sync BIM)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2022)),
                border = BorderStroke(1.dp, RevitColor.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("በመዝገብ ላይ ያለው የ Revit BIM መረጃ:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("የተገመተ ወጪ (BIM Cost Estimate):", fontSize = 11.sp, color = Color.LightGray)
                        Text(
                            text = if (project.bimEstimatedCost != null) format.format(project.bimEstimatedCost).replace("$", "ETB ") else "ያልተገናኘ",
                            fontSize = 12.sp,
                            color = RevitColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("የተመሳሰሉ ማቴሪያሎች:", fontSize = 11.sp, color = Color.LightGray)
                        Text(project.materials, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium, maxLines = 1)
                    }
                }
            }
        }
    }
}

data class BimModelTemplate(val name: String, val cost: Double, val materials: String)

// ==================== 3. PRIMAVERA SCHEDULE WORKSPACE ====================
@Composable
fun PrimaveraScheduleWorkspace(
    project: Project,
    viewModel: ConstructionViewModel,
    onDataSynced: (Project) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var scheduleProgress by remember { mutableStateOf(project.scheduleProgressPercent.toFloat()) }

    val scheduleMilestones = listOf(
        Milestone("የሳይት ዝግጅትና ቁፋሮ (Excavation)", 100, "100% ተጠናቋል"),
        Milestone("የመሠረት ግንባታ (Sub-Structure)", if (scheduleProgress >= 30) 100 else ((scheduleProgress / 30f) * 100).toInt(), if (scheduleProgress >= 30) "100% ተጠናቋል" else "${((scheduleProgress / 30f) * 100).toInt()}% በስራ ላይ"),
        Milestone("አምዶችና የግንባታ ፍሬም (Super-Structure)", if (scheduleProgress >= 65) 100 else if (scheduleProgress < 30) 0 else (((scheduleProgress - 30) / 35f) * 100).toInt(), if (scheduleProgress >= 65) "100% ተጠናቋል" else if (scheduleProgress < 30) "ያልተጀመረ" else "${(((scheduleProgress - 30) / 35f) * 100).toInt()}% በስራ ላይ"),
        Milestone("የውስጥና ውጪ ግድግዳ ስራ (Masonry & Finishes)", if (scheduleProgress >= 90) 100 else if (scheduleProgress < 65) 0 else (((scheduleProgress - 65) / 25f) * 100).toInt(), if (scheduleProgress >= 90) "100% ተጠናቋል" else if (scheduleProgress < 65) "ያልተጀመረ" else "${(((scheduleProgress - 65) / 25f) * 100).toInt()}% በስራ ላይ"),
        Milestone("ኤሌክትሪክና ቧንቧ (MEP Installations)", if (scheduleProgress >= 100) 100 else if (scheduleProgress < 90) 0 else (((scheduleProgress - 90) / 10f) * 100).toInt(), if (scheduleProgress >= 100) "100% ተጠናቋል" else if (scheduleProgress < 90) "ያልተጀመረ" else "${(((scheduleProgress - 90) / 10f) * 100).toInt()}% በስራ ላይ")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Timeline, "Primavera", tint = PrimaveraColor, modifier = Modifier.size(24.dp))
                Text("Primavera P6 / MS Project የስራ እቅድ ትስስር", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        item {
            Text(
                text = "የግንባታ ፕሮጀክት የ Primavera P6 እቅድ የ Gantt Chart ዝርዝሮችን በቀጥታ በማመሳሰል የደረጃ በደረጃ የስራ ክንውን ሁኔታን መከታተልና መመዝገብ ይችላሉ።",
                fontSize = 12.sp,
                color = Color.LightGray
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2022)),
                border = BorderStroke(1.dp, PrimaveraColor.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("አጠቃላይ የስራ እቅድ ክንውን (Project Completion):", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("${scheduleProgress.toInt()}%", fontSize = 14.sp, color = PrimaveraColor, fontWeight = FontWeight.Bold)
                    }

                    Slider(
                        value = scheduleProgress,
                        onValueChange = { scheduleProgress = it },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaveraColor,
                            activeTrackColor = PrimaveraColor,
                            inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val newProgress = scheduleProgress.toInt()
                            val status = when {
                                newProgress >= 100 -> "የተጠናቀቀ"
                                newProgress > 0 -> "በግንባታ ላይ"
                                else -> "በዝግጅት ላይ"
                            }
                            
                            viewModel.updateProjectEngineeringData(
                                projectId = project.id,
                                cad = null,
                                bim = null,
                                schedule = newProgress,
                                lat = null,
                                lon = null,
                                etabs = null
                            )

                            // Update project state in DB
                            coroutineScope.launch {
                                viewModel.updateProject(project.copy(
                                    scheduleProgressPercent = newProgress,
                                    status = status
                                ))
                                onDataSynced(project.copy(scheduleProgressPercent = newProgress, status = status))
                                Toast.makeText(context, "የግንባታ እቅድ ሂደት በስኬት ተመሳስሏል! ሁኔታ፡ $status (${newProgress}%)", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaveraColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Sync, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("እቅድ አመሳስል (Sync Primavera)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text("የእቅድ ማይልስቶኖች (Primavera Gantt Milestones):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
        }

        items(scheduleMilestones) { milestone ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(milestone.name, fontSize = 11.sp, color = Color.White)
                    Text(milestone.statusText, fontSize = 10.sp, color = if (milestone.progress == 100) AmharicGreen else if (milestone.progress > 0) PrimaveraColor else Color.Gray)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = milestone.progress / 100f,
                    color = if (milestone.progress == 100) AmharicGreen else PrimaveraColor,
                    trackColor = Color.Gray.copy(alpha = 0.2f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

data class Milestone(val name: String, val progress: Int, val statusText: String)

// ==================== 4. GIS LOCATION WORKSPACE ====================
@Composable
fun GISGeospatialWorkspace(
    project: Project,
    viewModel: ConstructionViewModel,
    onDataSynced: (Project) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Default coordinates in Tilili Town (approximate area coords)
    var latValue by remember { mutableStateOf(project.gisLatitude?.toString() ?: "10.5833") }
    var lonValue by remember { mutableStateOf(project.gisLongitude?.toString() ?: "37.1667") }

    // Simulation mapping
    var mapCenterLat by remember { mutableStateOf(project.gisLatitude ?: 10.5833) }
    var mapCenterLon by remember { mutableStateOf(project.gisLongitude ?: 37.1667) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Map, "GIS", tint = GISColor, modifier = Modifier.size(24.dp))
                Text("QGIS / ArcGIS የጂኦስፓሻል (GIS) ካርታ ትስስር", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        item {
            Text(
                text = "የግንባታው የሚገኝበትን ትክክለኛ የ GIS መጋጠሚያዎች (GPS Coordinates) በቀጥታ ከ QGIS / ArcGIS በማገናኘት በካርታው ላይ መገኛቸውን ምልክት ማድረግ ይቻላል።",
                fontSize = 12.sp,
                color = Color.LightGray
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = latValue,
                    onValueChange = { latValue = it },
                    label = { Text("ኬክሮስ (Latitude)", color = GISColor, fontSize = 11.sp) },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GISColor, focusedLabelColor = GISColor),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = lonValue,
                    onValueChange = { lonValue = it },
                    label = { Text("ኬንትሮስ (Longitude)", color = GISColor, fontSize = 11.sp) },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GISColor, focusedLabelColor = GISColor),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Button(
                onClick = {
                    val lat = latValue.toDoubleOrNull()
                    val lon = lonValue.toDoubleOrNull()
                    if (lat != null && lon != null) {
                        mapCenterLat = lat
                        mapCenterLon = lon
                        viewModel.updateProjectEngineeringData(
                            projectId = project.id,
                            cad = null,
                            bim = null,
                            schedule = -1,
                            lat = lat,
                            lon = lon,
                            etabs = null
                        )
                        coroutineScope.launch {
                            viewModel.updateProject(project.copy(
                                gisLatitude = lat,
                                gisLongitude = lon
                            ))
                            onDataSynced(project.copy(gisLatitude = lat, gisLongitude = lon))
                            Toast.makeText(context, "የካርታ መገኛ መረጃ በ QGIS በተሳካ ሁኔታ ተመሳስሏል! 🗺️", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "ልክ ያልሆነ የካርታ መጋጠሚያ ነጥብ!", Toast.LENGTH_LONG).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GISColor),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PinDrop, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ወሰን ካርታ አመሳስል (GIS Locate)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Custom Simulated GIS Map Canvas
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2022)),
                border = BorderStroke(1.dp, GISColor.copy(alpha = 0.3f))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize().clickable {
                        // Simulating interactive click on map to set coordinates
                        latValue = "10.58" + (20..90).random().toString()
                        lonValue = "37.16" + (20..90).random().toString()
                        Toast.makeText(context, "በካርታው ላይ አዲስ ነጥብ ተመረጠ! ከላይ 'ካርታ አመሳስል' ይጫኑ", Toast.LENGTH_SHORT).show()
                    }) {
                        // Drawing simulated satellite view (grid layers)
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF0F3D0F), Color(0xFF1E2022)),
                                center = Offset(size.width / 2, size.height / 2),
                                radius = size.width
                            )
                        )

                        // Draw roads/lines on map
                        drawLine(
                            color = Color.DarkGray,
                            start = Offset(0f, size.height / 3),
                            end = Offset(size.width, size.height / 2),
                            strokeWidth = 12f
                        )
                        drawLine(
                            color = Color.LightGray,
                            start = Offset(0f, size.height / 3),
                            end = Offset(size.width, size.height / 2),
                            strokeWidth = 2f,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f))
                        )

                        drawLine(
                            color = Color.DarkGray,
                            start = Offset(size.width / 2, 0f),
                            end = Offset(size.width / 3, size.height),
                            strokeWidth = 8f
                        )

                        // Draw river
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, size.height * 0.8f)
                            quadraticTo(size.width / 2, size.height * 0.9f, size.width, size.height * 0.65f)
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFF0288D1),
                            style = Stroke(width = 6f)
                        )

                        // Draw GPS Pin Marker (Project Center)
                        drawCircle(
                            color = GISColor,
                            center = Offset(size.width / 2f, size.height / 2.2f),
                            radius = 25f,
                            style = Stroke(width = 3f)
                        )
                        drawCircle(
                            color = AutoCADColor,
                            center = Offset(size.width / 2f, size.height / 2.2f),
                            radius = 8f
                        )
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(6.dp)
                    ) {
                        Text("የቲሊሊ ከተማ GIS ካርታ", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("Latitude: $mapCenterLat", color = Color.LightGray, fontSize = 8.sp)
                        Text("Longitude: $mapCenterLon", color = Color.LightGray, fontSize = 8.sp)
                    }

                    Text(
                        text = "በቀጥታ መጋጠሚያ ለመምረጥ ካርታውን ይንኩት",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

// ==================== 5. ETABS STRUCTURAL WORKSPACE ====================
@Composable
fun ETABSStructuralWorkspace(
    project: Project,
    viewModel: ConstructionViewModel,
    onDataSynced: (Project) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var safetyScore by remember { mutableStateOf(project.etabsSafetyScore ?: 90) }
    var isSimulating by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Shield, "ETABS", tint = ETABSColor, modifier = Modifier.size(24.dp))
                Text("ETABS / SAP2000 የህንጻ ንድፍ ደህንነት ትስስር", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        item {
            Text(
                text = "የህንጻውን የስበትና የመሬት መንቀጥቀጥ ጫና መቋቋም አቅም የ ETABS ንድፍ ሪፖርት በቀጥታ በመተንተን የደህንነት ደረጃ ማረጋገጫ ማግኘት ይቻላል።",
                fontSize = 12.sp,
                color = Color.LightGray
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2022)),
                border = BorderStroke(1.dp, ETABSColor.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "የህንጻው የደህንነት መጠን (Structural Safety Score):",
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isSimulating) "በማስላት ላይ..." else "$safetyScore%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (safetyScore >= 85) AmharicGreen else if (safetyScore >= 60) AmharicYellow else AmharicRed
                        )
                        Text(
                            text = if (safetyScore >= 85) "ፍጹም አስተማማኝ (Excellent)" else if (safetyScore >= 60) "መካከለኛ (Warning)" else "ወሳኝ የጥራት ስጋት (Critical)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (safetyScore >= 85) AmharicGreen else if (safetyScore >= 60) AmharicYellow else AmharicRed
                        )
                    }

                    LinearProgressIndicator(
                        progress = safetyScore / 100f,
                        color = if (safetyScore >= 85) AmharicGreen else if (safetyScore >= 60) AmharicYellow else AmharicRed,
                        trackColor = Color.Gray.copy(alpha = 0.2f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                isSimulating = true
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(1200)
                                    safetyScore = (50..100).random()
                                    isSimulating = false
                                    Toast.makeText(context, "የ ETABS መዋቅር ጫና ትንተና ተጠናቋል!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ETABSColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PlayArrow, null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ጭና አስላ (Analyze)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.updateProjectEngineeringData(
                                    projectId = project.id,
                                    cad = null,
                                    bim = null,
                                    schedule = -1,
                                    lat = null,
                                    lon = null,
                                    etabs = safetyScore
                                )
                                coroutineScope.launch {
                                    viewModel.updateProject(project.copy(
                                        etabsSafetyScore = safetyScore
                                    ))
                                    onDataSynced(project.copy(etabsSafetyScore = safetyScore))
                                    Toast.makeText(context, "የ ETABS ደህንነት ውጤት በማህደሩ ውስጥ ተመዝግቧል! 🔒", Toast.LENGTH_SHORT).show()
                                    
                                    // If safety is critical, auto-prompt to send warning
                                    if (safetyScore < 60) {
                                        Toast.makeText(context, "⚠️ ማስጠንቀቂያ፡ የህንጻው ደህንነት ስጋት ስላለው እባክዎ ለተቋራጩ ማስጠንቀቂያ ይላኩ!", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            enabled = !isSimulating,
                            colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Save, null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ውጤት መዝግብ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Structural Stress Canvas drawing (Bending moment diagram simulation)
        item {
            Text("የጭነትና የኩርባ ውጥረት ስዕላዊ መግለጫ (Shear & Moment Diagram):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2022)),
                border = BorderStroke(1.dp, ETABSColor.copy(alpha = 0.3f))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        val width = size.width
                        val height = size.height
                        
                        // Draw Beam Line
                        drawLine(
                            color = Color.LightGray,
                            start = Offset(40f, height / 2f),
                            end = Offset(width - 40f, height / 2f),
                            strokeWidth = 8f
                        )

                        // Draw Supports (Triangles)
                        val pathSupport1 = androidx.compose.ui.graphics.Path().apply {
                            moveTo(60f, height / 2f)
                            lineTo(45f, height / 2f + 25f)
                            lineTo(75f, height / 2f + 25f)
                            close()
                        }
                        drawPath(path = pathSupport1, color = AmharicYellow)

                        val pathSupport2 = androidx.compose.ui.graphics.Path().apply {
                            moveTo(width - 60f, height / 2f)
                            lineTo(width - 75f, height / 2f + 25f)
                            lineTo(width - 45f, height / 2f + 25f)
                            close()
                        }
                        drawPath(path = pathSupport2, color = AmharicYellow)

                        // Draw Stress Curve (Moment Curve)
                        val curvePath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(60f, height / 2f)
                            // Scale curve based on safety score (lower score = higher stress deflection)
                            val deflectionFactor = if (safetyScore < 60) 80f else 40f
                            quadraticBezierTo(
                                width / 2f,
                                height / 2f - deflectionFactor,
                                width - 60f,
                                height / 2f
                            )
                        }
                        drawPath(
                            path = curvePath,
                            color = ETABSColor,
                            style = Stroke(width = 4f)
                        )

                        // Draw Load Arrows
                        val arrowY = height / 2f - 40f
                        for (i in 1..4) {
                            val arrowX = 60f + (width - 120f) * (i / 5f)
                            drawLine(
                                color = AmharicRed,
                                start = Offset(arrowX, arrowY),
                                end = Offset(arrowX, height / 2f - 10f),
                                strokeWidth = 3f
                            )
                            // Arrowhead
                            drawLine(
                                color = AmharicRed,
                                start = Offset(arrowX - 6f, height / 2f - 18f),
                                end = Offset(arrowX, height / 2f - 10f),
                                strokeWidth = 3f
                            )
                            drawLine(
                                color = AmharicRed,
                                start = Offset(arrowX + 6f, height / 2f - 18f),
                                end = Offset(arrowX, height / 2f - 10f),
                                strokeWidth = 3f
                            )
                        }
                    }

                    Text(
                        text = "Beam Load Bending Diagram (Simulated ETABS Report)",
                        color = Color.Gray,
                        fontSize = 9.sp,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BuildingTrackingWorkspace(
    project: Project,
    viewModel: ConstructionViewModel,
    onDataSynced: (Project) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Telemetry state
    var concreteMoisture by remember { mutableStateOf(45f) }
    var buildingTilt by remember { mutableStateOf(0.12f) }
    var steelStress by remember { mutableStateOf(240f) }

    // List of simulated engineering documents for this project
    val documents = remember(project.id) {
        listOf(
            Triple("የመሠረት ሥራ ፕላን (Foundation Plan)", "መስተካከል ያለበት", "የመሠረት ኮንክሪት ጥልቀት ቢያንስ በ0.5 ሜትር መጨመር አለበት።"),
            Triple("የአርማታ ብረት አሰራር ዲዛይን (Structural Rebar)", "የጸደቀ", "የአርማታ ብረት አሰራር ዲዛይን ከከተማው ስታንዳርድ ጋር ይጣጣማል።"),
            Triple("የአፈር ምርመራ ሪፖርት (Geotechnical Soil Report)", "መስተካከል ያለበት", "የአፈር ጥናት ናሙናው በቂ ባለመሆኑ ድጋሚ ምርመራ ያስፈልገዋል።"),
            Triple("የህንጻው የፍሳሽ ማስወገጃ ፕላን (Sewerage & Plumbing)", "የጸደቀ", "የፍሳሽ ማስወገጃ መስመር ዲዛይን ሙሉ በሙሉ ተቀባይነት አግኝቷል።")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Workspace Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "የህንጻ ክትትልና የሰነዶች ግምገማ (Building Tracking & Docs)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "ለፕሮጀክት፡ ${project.name}",
                    fontSize = 12.sp,
                    color = AmharicYellow
                )
            }
            Icon(
                imageVector = Icons.Default.Assessment,
                contentDescription = null,
                tint = AmharicYellow,
                modifier = Modifier.size(24.dp)
            )
        }

        Divider(color = Color.Gray.copy(alpha = 0.2f))

        // PART 1: Building Tracking Telemetry Data Receiver
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2022)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Sensors, contentDescription = null, tint = ConstructionTeal)
                    Text("የሕንፃ ክትትል የቴሌሜትሪ መረጃዎች መቀበያ (Telemetry Receiver)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                
                Text(
                    text = "ከግንባታው ቦታ የተገጠሙ ሴንሰሮች መረጃዎችን እዚህ ይቀበሉ እና ያገናኙ።",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )

                // Concrete moisture
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("የኮንክሪት እርጥበት መጠን (Concrete Moisture):", color = Color.LightGray, fontSize = 12.sp)
                        Text("${concreteMoisture.toInt()}%", color = if (concreteMoisture < 30f) AmharicRed else AmharicGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Slider(
                        value = concreteMoisture,
                        onValueChange = { concreteMoisture = it },
                        valueRange = 10f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = ConstructionTeal,
                            activeTrackColor = ConstructionTeal
                        )
                    )
                }

                // Building tilt
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("የህንጻ ዝንባሌ መለኪያ (Tilt Sensor Angle):", color = Color.LightGray, fontSize = 12.sp)
                        Text("${"%.2f".format(buildingTilt)}°", color = if (buildingTilt > 0.4f) AmharicRed else AmharicGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Slider(
                        value = buildingTilt,
                        onValueChange = { buildingTilt = it },
                        valueRange = 0.0f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = AmharicYellow,
                            activeTrackColor = AmharicYellow
                        )
                    )
                    if (buildingTilt > 0.4f) {
                        Text(
                            text = "⚠ ማስጠንቀቂያ፡ የህንጻው ዝንባሌ ከተፈቀደው ደህንነት ወሰን በላይ ነው!",
                            color = AmharicRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Steel stress
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("የአርማታ ብረት ጥንካሬ/ውጥረት (Rebar Tensile Stress):", color = Color.LightGray, fontSize = 12.sp)
                        Text("${steelStress.toInt()} MPa", color = if (steelStress > 350f) AmharicRed else AmharicGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Slider(
                        value = steelStress,
                        onValueChange = { steelStress = it },
                        valueRange = 100f..500f,
                        colors = SliderDefaults.colors(
                            thumbColor = AmharicGreen,
                            activeTrackColor = AmharicGreen
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            val safetyPenalty = if (buildingTilt > 0.4f) 20 else 0
                            val moisturePenalty = if (concreteMoisture < 30f) 10 else 0
                            val newSafetyScore = (100 - safetyPenalty - moisturePenalty).coerceIn(10, 100)
                            
                            viewModel.updateProjectEngineeringData(
                                projectId = project.id,
                                cad = null,
                                bim = null,
                                schedule = -1,
                                lat = null,
                                lon = null,
                                etabs = newSafetyScore
                            )
                            Toast.makeText(context, "የክትትል ሴንሰር መረጃዎች በተሳካ ሁኔታ ተገናኝተዋል! የህንጻ ደህንነት ነጥብ ወደ $newSafetyScore% ተቀይሯል።", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ConstructionTeal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("የክትትል መረጃዎችን አመሳስል (Sync)", fontSize = 11.sp)
                    }

                    if (buildingTilt > 0.4f || concreteMoisture < 30f || steelStress > 350f) {
                        Button(
                            onClick = {
                                val problem = when {
                                    buildingTilt > 0.4f -> "የህንጻው ዝንባሌ ከተፈቀደው በላይ (${"%.2f".format(buildingTilt)}°) በመሆኑ አደገኛ ነው!"
                                    concreteMoisture < 30f -> "የኮንክሪት እርጥበት መጠን አነስተኛ (${concreteMoisture.toInt()}%) በመሆኑ ስንጥቅ ሊያስከትል ይችላል።"
                                    else -> "የአርማታ ብረት ጥንካሬ/ውጥረት ከመጠን በላይ (${steelStress.toInt()} MPa) ሆኗል።"
                                }
                                viewModel.sendWarning(
                                    name = project.name,
                                    phone = "+251911223344",
                                    role = "የስራ ተቆጣጣሪ",
                                    message = "የህንጻ ክትትል ሴንሰር ማስጠንቀቂያ፡ $problem እባክዎ በአስቸኳይ ይፈትሹ!",
                                    severity = "ወሳኝ"
                                )
                                Toast.makeText(context, "የደህንነት ማስጠንቀቂያ በሲስተም ውስጥ ተመዝግቧል! 🚨", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AmharicRed),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("የደህንነት ማስጠንቀቂያ ላክ", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // PART 2: Document Reviews with Category Engineer Comments
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2022)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Assignment, contentDescription = null, tint = AmharicYellow)
                    Text("የሰነዶችና የምድብ መሃንዲስ አስተያየት ግምገማ (Document Comments)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Text(
                    text = "በከተማው ምድብ መሀንዲሶች የተሰጡ የሰነድ ግምገማዎች እና የማስተካከያ ትዕዛዞች ዝርዝር።",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )

                documents.forEach { doc ->
                    val isCorrectionNeeded = doc.second == "መስተካከል ያለበት"
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isCorrectionNeeded) AmharicRed.copy(alpha = 0.5f) else AmharicGreen.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = doc.first,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Surface(
                                    color = if (isCorrectionNeeded) AmharicRed.copy(alpha = 0.15f) else AmharicGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(1.dp, if (isCorrectionNeeded) AmharicRed else AmharicGreen)
                                ) {
                                    Text(
                                        text = doc.second,
                                        color = if (isCorrectionNeeded) AmharicRed else AmharicGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "የምድብ መሃንዲሱ አስተያየት፡",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = doc.third,
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )

                            if (isCorrectionNeeded) {
                                Button(
                                    onClick = {
                                        viewModel.sendWarning(
                                            name = project.name,
                                            phone = "+251915998877",
                                            role = "የስራ ተቋራጭ",
                                            message = "የምድብ መሃንዲስ የሰነድ ማስተካከያ ትዕዛዝ [${doc.first}]፡ ${doc.third}",
                                            severity = "ከፍተኛ"
                                        )
                                        Toast.makeText(context, "ማስጠንቀቂያ በተሳካ ሁኔታ ተልኳል! ✉", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AmharicRed),
                                    modifier = Modifier.align(Alignment.End),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("የማስተካከያ ማስጠንቀቂያ አካትት", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

