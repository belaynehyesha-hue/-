package com.example.ui.screens

import android.widget.Toast
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Artisan
import com.example.data.Professional
import com.example.data.Project
import com.example.data.Warning
import com.example.data.RiskAssessment
import com.example.ui.viewmodel.ConstructionViewModel
import com.example.ui.viewmodel.GovernmentRegulation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Color scheme for the Tilili Construction app matching the Ethiopian / Earthy vibe
val AmharicGreen = Color(0xFF007A33)
val AmharicYellow = Color(0xFFFCD116)
val AmharicRed = Color(0xFFC8102E)
val ConstructionTeal = Color(0xFF005F73)
val SteelGray = Color(0xFF2B2D42)
val BackgroundSlate = Color(0xFF1E2022)
val GoldAccent = Color(0xFFE9C46A)
val CardBackground = Color(0xFF242629)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    modifier: Modifier = Modifier,
    viewModel: ConstructionViewModel = viewModel()
) {
    val vm = viewModel
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val projects by viewModel.projects.collectAsState()
    val artisans by viewModel.artisans.collectAsState()
    val professionals by viewModel.professionals.collectAsState()
    val warnings by viewModel.warnings.collectAsState()
    val activeRegulation by viewModel.activeRegulation.collectAsState()

    var currentTab by remember { mutableStateOf(0) } // 0: Dashboard, 1: Projects, 2: Artisans, 3: Professionals, 4: Integrations, 5: Reports & Alerts

    // Dialog state for adding items
    var showAddProjectDialog by remember { mutableStateOf(false) }
    var showAddArtisanDialog by remember { mutableStateOf(false) }
    var showAddProfDialog by remember { mutableStateOf(false) }
    var showWarningDialog by remember { mutableStateOf(false) }
    var selectedProjectForDetails by remember { mutableStateOf<Project?>(null) }
    var showShareInstallDialog by remember { mutableStateOf(false) }

    // Scanner simulated state
    var showScannerView by remember { mutableStateOf(false) }
    var showQrScanner by remember { mutableStateOf(false) }
    var scannedCocResult by remember { mutableStateOf("") }
    var scannedSectorResult by remember { mutableStateOf("") }
    var scannedNameResult by remember { mutableStateOf("") }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 680.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "የቲሊሊ ከተማ ኮንስትራክሽን ጽሕፈት ቤት",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "ግንባታ ክትትልና ቁጥጥር ቡድን",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AmharicYellow,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        showQrScanner = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "የግንባታ QR መለያ መፈለጊያ (ስካነር)",
                            tint = AmharicYellow
                        )
                    }
                    IconButton(onClick = {
                        showShareInstallDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "መተግበሪያውን ጫን እና አጋራ",
                            tint = AmharicYellow
                        )
                    }
                    IconButton(onClick = {
                        Toast.makeText(context, "ባለቤትነት፡ በላይነህ የሻለም (Civil Engineer)", Toast.LENGTH_LONG).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "ስለ መድረኩ",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SteelGray
                )
            )
        },
        bottomBar = {
            if (!isWideScreen) {
                NavigationBar(
                    containerColor = SteelGray,
                    tonalElevation = 8.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = "ዋና ገጽ") },
                        label = { Text("ዋና ገጽ", fontSize = 9.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AmharicYellow,
                            selectedTextColor = AmharicYellow,
                            unselectedIconColor = Color.LightGray,
                            unselectedTextColor = Color.LightGray,
                            indicatorColor = SteelGray.copy(alpha = 0.5f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        icon = { Icon(Icons.Default.Build, contentDescription = "ግንባታዎች") },
                        label = { Text("ግንባታ", fontSize = 9.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AmharicYellow,
                            selectedTextColor = AmharicYellow,
                            unselectedIconColor = Color.LightGray,
                            unselectedTextColor = Color.LightGray,
                            indicatorColor = SteelGray.copy(alpha = 0.5f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { currentTab = 2 },
                        icon = { Icon(Icons.Default.Person, contentDescription = "የልምድ ሰራተኞች") },
                        label = { Text("ልምድ", fontSize = 9.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AmharicYellow,
                            selectedTextColor = AmharicYellow,
                            unselectedIconColor = Color.LightGray,
                            unselectedTextColor = Color.LightGray,
                            indicatorColor = SteelGray.copy(alpha = 0.5f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == 3,
                        onClick = { currentTab = 3 },
                        icon = { Icon(Icons.Default.SupervisorAccount, contentDescription = "ባለሙያዎች") },
                        label = { Text("ባለሙያ", fontSize = 9.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AmharicYellow,
                            selectedTextColor = AmharicYellow,
                            unselectedIconColor = Color.LightGray,
                            unselectedTextColor = Color.LightGray,
                            indicatorColor = SteelGray.copy(alpha = 0.5f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == 4,
                        onClick = { currentTab = 4 },
                        icon = { Icon(Icons.Default.SettingsInputComponent, contentDescription = "የምህንድስና ትስስር") },
                        label = { Text("ትስስር", fontSize = 9.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AmharicYellow,
                            selectedTextColor = AmharicYellow,
                            unselectedIconColor = Color.LightGray,
                            unselectedTextColor = Color.LightGray,
                            indicatorColor = SteelGray.copy(alpha = 0.5f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == 5,
                        onClick = { currentTab = 5 },
                        icon = { Icon(Icons.Default.Warning, contentDescription = "ቁጥጥርና ሪፖርት") },
                        label = { Text("ሪፖርት", fontSize = 9.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AmharicYellow,
                            selectedTextColor = AmharicYellow,
                            unselectedIconColor = Color.LightGray,
                            unselectedTextColor = Color.LightGray,
                            indicatorColor = SteelGray.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        },
        containerColor = BackgroundSlate
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isWideScreen) {
                NavigationRail(
                    containerColor = SteelGray,
                    header = {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(AmharicGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationCity,
                                contentDescription = "አርማ",
                                tint = AmharicYellow,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    NavigationRailItem(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = "ዋና ገጽ") },
                        label = { Text("ዋና ገጽ", fontSize = 10.sp) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = AmharicYellow,
                            selectedTextColor = AmharicYellow,
                            unselectedIconColor = Color.LightGray,
                            unselectedTextColor = Color.LightGray
                        )
                    )
                    NavigationRailItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        icon = { Icon(Icons.Default.Build, contentDescription = "ግንባታዎች") },
                        label = { Text("ግንባታዎች", fontSize = 10.sp) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = AmharicYellow,
                            selectedTextColor = AmharicYellow,
                            unselectedIconColor = Color.LightGray,
                            unselectedTextColor = Color.LightGray
                        )
                    )
                    NavigationRailItem(
                        selected = currentTab == 2,
                        onClick = { currentTab = 2 },
                        icon = { Icon(Icons.Default.Person, contentDescription = "የልምድ ሰራተኞች") },
                        label = { Text("ልምድ", fontSize = 10.sp) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = AmharicYellow,
                            selectedTextColor = AmharicYellow,
                            unselectedIconColor = Color.LightGray,
                            unselectedTextColor = Color.LightGray
                        )
                    )
                    NavigationRailItem(
                        selected = currentTab == 3,
                        onClick = { currentTab = 3 },
                        icon = { Icon(Icons.Default.SupervisorAccount, contentDescription = "ባለሙያዎች") },
                        label = { Text("ባለሙያ", fontSize = 10.sp) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = AmharicYellow,
                            selectedTextColor = AmharicYellow,
                            unselectedIconColor = Color.LightGray,
                            unselectedTextColor = Color.LightGray
                        )
                    )
                    NavigationRailItem(
                        selected = currentTab == 4,
                        onClick = { currentTab = 4 },
                        icon = { Icon(Icons.Default.SettingsInputComponent, contentDescription = "ትስስር") },
                        label = { Text("ትስስር", fontSize = 10.sp) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = AmharicYellow,
                            selectedTextColor = AmharicYellow,
                            unselectedIconColor = Color.LightGray,
                            unselectedTextColor = Color.LightGray
                        )
                    )
                    NavigationRailItem(
                        selected = currentTab == 5,
                        onClick = { currentTab = 5 },
                        icon = { Icon(Icons.Default.Warning, contentDescription = "ቁጥጥርና ሪፖርት") },
                        label = { Text("ሪፖርት", fontSize = 10.sp) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = AmharicYellow,
                            selectedTextColor = AmharicYellow,
                            unselectedIconColor = Color.LightGray,
                            unselectedTextColor = Color.LightGray
                        )
                    )
                    NavigationRailItem(
                        selected = currentTab == 6,
                        onClick = { currentTab = 6 },
                        icon = { Icon(Icons.Default.Gavel, contentDescription = "የሕንፃ ኮድ እና AI ደብዳቤ") },
                        label = { Text("የሕንፃ ኮድ", fontSize = 10.sp) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = AmharicYellow,
                            selectedTextColor = AmharicYellow,
                            unselectedIconColor = Color.LightGray,
                            unselectedTextColor = Color.LightGray
                        )
                    )
                    NavigationRailItem(
                        selected = currentTab == 7,
                        onClick = { currentTab = 7 },
                        icon = { Icon(Icons.Default.FactCheck, contentDescription = "ዕለታዊ ክትትል") },
                        label = { Text("ዕለታዊ ክትትል", fontSize = 10.sp) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = AmharicYellow,
                            selectedTextColor = AmharicYellow,
                            unselectedIconColor = Color.LightGray,
                            unselectedTextColor = Color.LightGray
                        )
                    )
                    NavigationRailItem(
                        selected = currentTab == 8,
                        onClick = { currentTab = 8 },
                        icon = { Icon(Icons.Default.HealthAndSafety, contentDescription = "ደህንነት አደጋ") },
                        label = { Text("ደህንነት አደጋ", fontSize = 10.sp) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = AmharicYellow,
                            selectedTextColor = AmharicYellow,
                            unselectedIconColor = Color.LightGray,
                            unselectedTextColor = Color.LightGray
                        )
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Crossfade(targetState = currentTab, label = "tabTransition") { tab ->
                    when (tab) {
                    0 -> DashboardScreen(
                        projects = projects,
                        artisans = artisans,
                        professionals = professionals,
                        warnings = warnings,
                        onAddProject = { showAddProjectDialog = true },
                        onSendWarning = { showWarningDialog = true },
                        onNavigate = { currentTab = it },
                        onProjectSelect = { selectedProjectForDetails = it },
                        onAddArtisanClick = { showAddArtisanDialog = true },
                        onAddProfessionalClick = { showAddProfDialog = true },
                        onShareAppClick = { showShareInstallDialog = true },
                        viewModel = vm
                    )
                    1 -> ProjectsScreen(
                        projects = projects,
                        professionals = professionals,
                        onAddProjectClick = { showAddProjectDialog = true },
                        onProjectSelect = { selectedProjectForDetails = it },
                        onDeleteProject = { viewModel.deleteProject(it) }
                    )
                    2 -> ArtisansScreen(
                        artisans = artisans,
                        warnings = warnings,
                        activeRegulation = activeRegulation,
                        onAddArtisanClick = { showAddArtisanDialog = true },
                        onDeleteArtisan = { viewModel.deleteArtisan(it) },
                        onUpdateArtisan = { viewModel.updateArtisan(it) },
                        onAddWarning = { name, phone, sector, reason, severity ->
                            viewModel.sendWarning(name, phone, sector, reason, severity)
                        },
                        onScanClick = {
                            showScannerView = true
                        }
                    )
                    3 -> ProfessionalsScreen(
                        professionals = professionals,
                        activeRegulation = activeRegulation,
                        onAddProfClick = { showAddProfDialog = true },
                        onDeleteProf = { viewModel.deleteProfessional(it) },
                        onUpdateProf = { viewModel.updateProfessional(it) }
                    )
                    4 -> EngineeringIntegrationsScreen(
                        projects = projects,
                        viewModel = viewModel
                    )
                    5 -> ReportsAndAlertsScreen(
                        warnings = warnings,
                        professionals = professionals,
                        artisans = artisans,
                        viewModel = viewModel,
                        onSendWarningClick = { showWarningDialog = true }
                    )
                    6 -> EbcsAiScreen(
                        projects = projects,
                        viewModel = viewModel
                    )
                    7 -> DailyProgressScreen(
                        projects = projects,
                        viewModel = viewModel
                    )
                    8 -> RiskAssessmentScreen(
                        projects = projects,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

            if (showQrScanner) {
                val dailyProgressState by viewModel.dailyProgress.collectAsState()
                val riskAssessmentsState by viewModel.riskAssessments.collectAsState()
                QrCodeScannerView(
                    projects = projects,
                    dailyProgress = dailyProgressState,
                    riskAssessments = riskAssessmentsState,
                    onClose = { showQrScanner = false }
                )
            }

            // Interactive Certificate Scanner Overlay Overlay
            if (showScannerView) {
                CertificateScannerView(
                    onScanComplete = { name, trade, coc ->
                        scannedNameResult = name
                        scannedSectorResult = trade
                        scannedCocResult = coc
                        showScannerView = false
                        showAddArtisanDialog = true
                    },
                    onClose = {
                        showScannerView = false
                    }
                )
            }
        }
    }

    // Dialog: Add Project
    if (showAddProjectDialog) {
        AddProjectDialog(
            professionals = professionals,
            onDismiss = { showAddProjectDialog = false },
            onConfirm = { name, location, category, type, materials, consultantId, contractorId, inspectorId, status ->
                viewModel.addProject(name, location, category, type, materials, consultantId, contractorId, inspectorId, status)
                showAddProjectDialog = false
                Toast.makeText(context, "ግንባታው በስኬት ተመዝግቧል!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog: Add Artisan (Skilled Labor)
    if (showAddArtisanDialog) {
        AddArtisanDialog(
            prefilledName = scannedNameResult,
            prefilledTrade = scannedSectorResult,
            prefilledCoc = scannedCocResult,
            onDismiss = {
                showAddArtisanDialog = false
                scannedNameResult = ""
                scannedSectorResult = ""
                scannedCocResult = ""
            },
            onConfirm = { name, phone, trade, coc, exp ->
                viewModel.addArtisan(name, phone, trade, coc, "scanned_coc_photo.jpg", exp)
                showAddArtisanDialog = false
                scannedNameResult = ""
                scannedSectorResult = ""
                scannedCocResult = ""
                Toast.makeText(context, "የልምድ ሰራተኛው በስኬት ተመዝግቧል!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog: Add Professional
    if (showAddProfDialog) {
        AddProfessionalDialog(
            onDismiss = { showAddProfDialog = false },
            onConfirm = { name, phone, role, grade, license ->
                viewModel.addProfessional(name, phone, role, grade, license)
                showAddProfDialog = false
                Toast.makeText(context, "ባለሙያው በስኬት ተመዝግቧል!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog: Dispatch Alert
    if (showWarningDialog) {
        SendWarningDialog(
            professionals = professionals,
            artisans = artisans,
            onDismiss = { showWarningDialog = false },
            onConfirm = { name, phone, role, message, severity ->
                viewModel.sendWarning(name, phone, role, message, severity)
                showWarningDialog = false
                coroutineScope.launch {
                    // Simulated visual feedback delay for SMS dispatch
                    Toast.makeText(context, "መልዕክቱ በስልክ ቁጥር $phone እየተላከ ነው...", Toast.LENGTH_LONG).show()
                    delay(1500)
                    Toast.makeText(context, "ማስጠንቀቂያ ለ $name በስልካቸው በተሳካ ሁኔታ ተልኳል! 📡", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Dialog: Project Detail
    selectedProjectForDetails?.let { project ->
        ProjectDetailDialog(
            project = project,
            professionals = professionals,
            onDismiss = { selectedProjectForDetails = null }
        )
    }

    if (showShareInstallDialog) {
        ShareInstallDialog(
            onDismiss = { showShareInstallDialog = false },
            context = context
        )
    }
}

// ==================== DASHBOARD SCREEN ====================
@Composable
fun DashboardScreen(
    projects: List<Project>,
    artisans: List<Artisan>,
    professionals: List<Professional>,
    warnings: List<Warning>,
    onAddProject: () -> Unit,
    onSendWarning: () -> Unit,
    onNavigate: (Int) -> Unit,
    onProjectSelect: (Project) -> Unit,
    onAddArtisanClick: () -> Unit = {},
    onAddProfessionalClick: () -> Unit = {},
    onShareAppClick: () -> Unit = {},
    viewModel: ConstructionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero / Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_hero_card"),
                colors = CardDefaults.cardColors(containerColor = SteelGray),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(SteelGray, BackgroundSlate)
                            )
                        )
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(AmharicGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationCity,
                            contentDescription = "አርማ",
                            tint = AmharicYellow,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "የቲሊሊ ከተማ አስተዳደር",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "ግንባታ ክትትልና ቁጥጥር ቡድን ዲጂታል መድረክ",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = AmharicRed.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, AmharicYellow.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "ባለቤትነት፡ በላይነህ የሻለም (Civil Engineer)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AmharicYellow,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Stats Grid
        item {
            Text(
                text = "የከተማው አጠቃላይ መረጃ (City Registry Stats)",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "ግንባታዎች",
                    value = projects.size.toString(),
                    icon = Icons.Default.Apartment,
                    color = ConstructionTeal,
                    onClick = { onNavigate(1) }
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "ልምድ ሰራተኞች",
                    value = artisans.size.toString(),
                    icon = Icons.Default.Construction,
                    color = AmharicGreen,
                    onClick = { onNavigate(2) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "አማካሪ/ተቋራጮች",
                    value = professionals.size.toString(),
                    icon = Icons.Default.People,
                    color = SteelGray,
                    onClick = { onNavigate(3) }
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "ማስጠንቀቂያዎች",
                    value = warnings.size.toString(),
                    icon = Icons.Default.NotificationsActive,
                    color = AmharicRed,
                    onClick = { onNavigate(5) }
                )
            }
        }

        // Self-Registration and App Download Center (ባለሙያ/ሰራተኛ ራስ-ምዝገባ)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("self_registration_card"),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AmharicYellow.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = AmharicYellow,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "የባለሙያዎችና የልምድ ሰራተኞች ራስ-ምዝገባ ማዕከል",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "በቲሊሊ ከተማ የምትገኙ መሃንዲሶች፣ ግንበኞች፣ ባለሙያዎች እና የልምድ ሰራተኞች (Artisans/Professionals) በሙሉ! ይህንን መተግበሪያ በራሳችሁ ስልክ ላይ ጭናችሁ በቀጥታ ራሳችሁን መመዝገብ ትችላላችሁ። መተግበሪያውን ለባልደረባዎችዎ ለማጋራትም 'ሊንክ አጋራ/አውርድ' የሚለውን ይጠቀሙ።",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.LightGray,
                            lineHeight = 16.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onAddArtisanClick,
                            colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen),
                            modifier = Modifier.weight(1f).testTag("self_register_artisan"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Construction, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("የልምድ ሰራተኛ ምዝገባ", fontSize = 10.sp, maxLines = 1)
                        }
                        
                        Button(
                            onClick = onAddProfessionalClick,
                            colors = ButtonDefaults.buttonColors(containerColor = ConstructionTeal),
                            modifier = Modifier.weight(1f).testTag("self_register_professional"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("የባለሙያ/ተቋራጭ ምዝገባ", fontSize = 10.sp, maxLines = 1)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onShareAppClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AmharicYellow),
                        border = BorderStroke(1.dp, AmharicYellow),
                        modifier = Modifier.fillMaxWidth().testTag("self_register_download"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("መተግበሪያውን አውርድ & ለሌሎች አጋራ (Download/Share App)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Central Dashboard Component: Project Status, Progress, and Milestones
        item {
            ProjectProgressDashboardSection(
                projects = projects,
                onNavigateToProjects = { onNavigate(1) },
                onProjectSelect = onProjectSelect
            )
        }

        // New Interactive Phase-based Progress Tracking Dashboard Component
        item {
            PhaseTrackingDashboardComponent(viewModel = viewModel)
        }

        // Codes and Proclamations Search Dashboard Component
        item {
            CodesDashboardComponent()
        }

        // Quick Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ፈጣን ተግባራት (Quick Actions)",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onAddProject,
                            colors = ButtonDefaults.buttonColors(containerColor = ConstructionTeal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_add_project"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ግንባታ መዝግብ", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Button(
                            onClick = onSendWarning,
                            colors = ButtonDefaults.buttonColors(containerColor = AmharicRed),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_send_warning"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ማስጠንቀቂያ ላክ", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onNavigate(4) }, // Open Engineering Integrations (tab index 4)
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        modifier = Modifier.fillMaxWidth().testTag("quick_engineering_hub"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.SettingsInputComponent, contentDescription = null, modifier = Modifier.size(16.dp), tint = SteelGray)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("የምህንድስና ሶፍትዌሮች ትስስር ማዕከል (CAD/BIM/GIS Sync)", fontSize = 11.sp, color = SteelGray, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onNavigate(6) }, // Open EBCS & AI Screen (tab index 6)
                        colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen),
                        modifier = Modifier.fillMaxWidth().testTag("quick_ebcs_ai"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ብሔራዊ የሕንፃ ኮድ መቆጣጠሪያና AI ሠነድ ማረጋገጫ (EBCS & AI)", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onNavigate(7) }, // Open Daily Progress (tab index 7)
                        colors = ButtonDefaults.buttonColors(containerColor = AmharicYellow),
                        modifier = Modifier.fillMaxWidth().testTag("quick_daily_progress"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(16.dp), tint = SteelGray)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ዕለታዊ የግንባታ መከታተያ እና ተገዢነት መቆጣጠሪያ (Daily Progress Logs)", fontSize = 11.sp, color = SteelGray, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onNavigate(8) }, // Open Risk Assessment (tab index 8)
                        colors = ButtonDefaults.buttonColors(containerColor = AmharicRed),
                        modifier = Modifier.fillMaxWidth().testTag("quick_risk_assessment"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.HealthAndSafety, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("የግንባታ ደህንነት አደጋዎች ፍተሻ እና መቆጣጠሪያ (Site Risk Assessment)", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Recent Warnings Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "የቅርብ ጊዜ ማስጠንቀቂያዎች (Recent Alerts)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                TextButton(onClick = { onNavigate(5) }) {
                    Text("ሁሉንም እይ", color = AmharicYellow)
                }
            }
        }

        if (warnings.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("እስካሁን የተላከ ማስጠንቀቂያ የለም።", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(warnings.take(3)) { warning ->
                WarningItem(warning = warning, onDelete = {})
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun ProjectProgressDashboardSection(
    projects: List<Project>,
    onNavigateToProjects: () -> Unit,
    onProjectSelect: (Project) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "የፕሮጀክቶች ዕቅድ አፈጻጸምና ምዕራፎች (Project Progress & Milestones Dashboard)",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            TextButton(onClick = onNavigateToProjects) {
                Text("ሁሉንም እይ", color = AmharicYellow, fontSize = 12.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (projects.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ምንም ግንባታ አልተመዘገበም።",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "በመሆኑም እዚህ ጋር የሚታይ የአፈጻጸም መረጃ የለም።",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Display top 3 active projects or all projects up to 3 for summary
            val activeProjects = projects.take(3)
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                activeProjects.forEach { project ->
                    ProjectDashboardProgressCard(
                        project = project,
                        onClick = { onProjectSelect(project) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectDashboardProgressCard(
    project: Project,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("dashboard_project_card_${project.id}"),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row: Name & Category and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.name,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = AmharicYellow,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${project.location} • ምድብ ${project.buildingCategory}",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }
                
                // Status Badge (Tailwind style)
                val badgeColors = when (project.status) {
                    "የተጠናቀቀ" -> Triple(Color(0xFFDCFCE7), Color(0xFF16A34A), BorderStroke(1.dp, Color(0xFF86EFAC)))
                    "በግንባታ ላይ" -> Triple(Color(0xFFE0F2FE), Color(0xFF0284C7), BorderStroke(1.dp, Color(0xFF7DD3FC)))
                    else -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), BorderStroke(1.dp, Color(0xFFFCD34D)))
                }
                
                Surface(
                    color = badgeColors.first,
                    shape = RoundedCornerShape(20.dp),
                    border = badgeColors.third,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = project.status,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = badgeColors.second,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Bar & Percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "የእቅድ አፈጻጸም ሂደት (Overall Progress)",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${project.scheduleProgressPercent}%",
                    color = when {
                        project.scheduleProgressPercent >= 90 -> AmharicGreen
                        project.scheduleProgressPercent >= 50 -> AmharicYellow
                        else -> AmharicRed
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Styled Progress Bar
            LinearProgressIndicator(
                progress = project.scheduleProgressPercent / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    project.scheduleProgressPercent >= 90 -> AmharicGreen
                    project.scheduleProgressPercent >= 50 -> ConstructionTeal
                    else -> AmharicRed
                },
                trackColor = Color.White.copy(alpha = 0.08f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Key Milestones Row
            Text(
                text = "የግንባታ ምዕራፎች (Milestones):",
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val milestones = listOf(
                    "ፕላን ዝግጅት" to 10,
                    "የመሠረት ሥራ" to 35,
                    "መዋቅራዊ" to 65,
                    "ማጠናቀቂያ" to 95
                )
                
                milestones.forEachIndexed { index, (name, threshold) ->
                    val isCompleted = project.scheduleProgressPercent >= threshold
                    val isCurrent = project.scheduleProgressPercent in (if (index == 0) 0 else milestones[index-1].second) until threshold
                    
                    val bgTint = when {
                        isCompleted -> AmharicGreen.copy(alpha = 0.15f)
                        isCurrent -> AmharicYellow.copy(alpha = 0.15f)
                        else -> Color.White.copy(alpha = 0.05f)
                    }
                    val borderStroke = when {
                        isCompleted -> BorderStroke(1.dp, AmharicGreen)
                        isCurrent -> BorderStroke(1.dp, AmharicYellow)
                        else -> BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
                    }
                    val textColor = when {
                        isCompleted -> AmharicGreen
                        isCurrent -> AmharicYellow
                        else -> Color.Gray
                    }
                    
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(bgTint, RoundedCornerShape(8.dp))
                            .border(borderStroke, RoundedCornerShape(8.dp))
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Completed",
                                    tint = AmharicGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(
                                            if (isCurrent) AmharicYellow else Color.White.copy(alpha = 0.1f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (index + 1).toString(),
                                        color = if (isCurrent) SteelGray else Color.Gray,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = name,
                            color = textColor,
                            fontSize = 10.sp,
                            fontWeight = if (isCurrent || isCompleted) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ==================== PROJECTS SCREEN ====================
@Composable
fun ProjectsScreen(
    projects: List<Project>,
    professionals: List<Professional>,
    onAddProjectClick: () -> Unit,
    onProjectSelect: (Project) -> Unit,
    onDeleteProject: (Project) -> Unit
) {
    var subTabState by remember { mutableStateOf(0) } // 0: Projects List, 1: Ethiopian Calendar

    var searchQuery by remember { mutableStateOf("") }
    var categoryFilter by remember { mutableStateOf("ሁሉም") }

    val categories = listOf("ሁሉም", "ምድብ 'ሀ'", "ምድብ 'ለ'", "ምድብ 'ሐ'", "ምድብ 'መ'", "የጭቃ ቤት", "ፎቅ የሌላቸው / Basement", "ጊዜያዊ ግንባታ")

    val filteredProjects = projects.filter {
        (categoryFilter == "ሁሉም" || it.buildingCategory.contains(categoryFilter.substringBefore(" "))) &&
                (it.name.contains(searchQuery, ignoreCase = true) || it.location.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sub-Tab Switcher
        TabRow(
            selectedTabIndex = subTabState,
            containerColor = Color.Transparent,
            divider = { HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f)) }
        ) {
            Tab(
                selected = subTabState == 0,
                onClick = { subTabState = 0 },
                text = { Text("የግግንባታ መዝገብ (Projects)", fontSize = 12.sp, fontWeight = if (subTabState == 0) FontWeight.Bold else FontWeight.Normal, color = if (subTabState == 0) AmharicYellow else Color.LightGray) }
            )
            Tab(
                selected = subTabState == 1,
                onClick = { subTabState = 1 },
                text = { Text("የኢትዮጵያ መቁጠሪያ (Ethiopian Calendar)", fontSize = 12.sp, fontWeight = if (subTabState == 1) FontWeight.Bold else FontWeight.Normal, color = if (subTabState == 1) AmharicYellow else Color.LightGray) }
            )
        }

        if (subTabState == 0) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "የግንባታ ፕሮጀክቶች ምዝገባ",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Button(
                        onClick = onAddProjectClick,
                        colors = ButtonDefaults.buttonColors(containerColor = ConstructionTeal),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("መዝግብ", fontSize = 12.sp)
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("በስም ወይም በቀበሌ ፈልግ...", color = Color.Gray, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AmharicYellow,
                        unfocusedBorderColor = Color.Gray,
                        focusedContainerColor = CardBackground,
                        unfocusedContainerColor = CardBackground
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                // Horizontal Category Filter List
                ScrollableTabRow(
                    selectedTabIndex = categories.indexOf(categoryFilter).coerceAtLeast(0),
                    containerColor = Color.Transparent,
                    edgePadding = 0.dp,
                    divider = {},
                    indicator = {}
                ) {
                    categories.forEach { cat ->
                        val isSelected = cat == categoryFilter
                        Tab(
                            selected = isSelected,
                            onClick = { categoryFilter = cat },
                            text = {
                                Text(
                                    text = cat,
                                    color = if (isSelected) AmharicYellow else Color.Gray,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            }
                        )
                    }
                }

                if (filteredProjects.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.HourglassEmpty, contentDescription = null, modifier = Modifier.size(60.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("ምንም የተመዘገበ ግንባታ አልተገኘም።", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredProjects) { project ->
                            ProjectCard(
                                project = project,
                                onSelect = { onProjectSelect(project) },
                                onDelete = { onDeleteProject(project) }
                            )
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.weight(1f)) {
                EthiopianCalendarTab(projects = projects)
            }
        }
    }
}

@Composable
fun ProjectCard(
    project: Project,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.name,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = AmharicYellow, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = project.location, color = Color.LightGray, fontSize = 12.sp)
                    }
                }

                Surface(
                    color = when (project.status) {
                        "በግንባታ ላይ" -> AmharicGreen.copy(alpha = 0.2f)
                        "የተጠናቀቀ" -> ConstructionTeal.copy(alpha = 0.2f)
                        else -> AmharicYellow.copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, when (project.status) {
                        "በግንባታ ላይ" -> AmharicGreen
                        "የተጠናቀቀ" -> ConstructionTeal
                        else -> AmharicYellow
                    })
                ) {
                    Text(
                        text = project.status,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = when (project.status) {
                                "በግንባታ ላይ" -> AmharicGreen
                                "የተጠናቀቀ" -> ConstructionTeal
                                else -> AmharicYellow
                            },
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Building category tag & materials info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Layers, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Text(
                    text = "ምድብ፡ ${project.buildingCategory}",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.GridOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Text(
                    text = "ግብዓት፡ ${project.materials}",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ዝርዝር መረጃ እይ / ባለሙያ አገናኝ 🔗",
                    color = AmharicYellow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "ሰርዝ", tint = AmharicRed.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ==================== ARTISANS SCREEN (የልምድ ሰራተኞች) ====================
@Composable
fun ArtisansScreen(
    artisans: List<Artisan>,
    warnings: List<Warning>,
    activeRegulation: GovernmentRegulation,
    onAddArtisanClick: () -> Unit,
    onDeleteArtisan: (Artisan) -> Unit,
    onUpdateArtisan: (Artisan) -> Unit,
    onAddWarning: (name: String, phone: String, sector: String, reason: String, severity: String) -> Unit,
    onScanClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSector by remember { mutableStateOf("ሁሉም") }
    var geezFilterLetter by remember { mutableStateOf("ሁሉም") }

    val sectors = listOf("ሁሉም", "ግንበኛ", "አናፂ", "ፌሮዮ (የግንባታ ብረት ሰራተኛ)", "ኤሌክትሪሺያን", "ቧንቧ ሰራተኛ")

    val filteredArtisans = artisans.filter { artisan ->
        val matchesSector = selectedSector == "ሁሉም" || artisan.tradeSector == selectedSector
        val matchesQuery = artisan.name.contains(searchQuery, ignoreCase = true) || artisan.cocNumber.contains(searchQuery, ignoreCase = true)
        val matchesGeez = if (geezFilterLetter == "ሁሉም") {
            true
        } else {
            val targetChar = geezFilterLetter.firstOrNull()
            if (targetChar == null) true
            else {
                val family = getGeezFamily(targetChar)
                artisan.name.firstOrNull() in family
            }
        }
        matchesSector && matchesQuery && matchesGeez
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "የልምድ ሰራተኞች ምዝገባ (COC)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onScanClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AmharicYellow),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ሰነድ ስካን", fontSize = 11.sp, color = Color.Black)
                }
                Button(
                    onClick = onAddArtisanClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("መዝግብ", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("በስም ወይም በCOC ቁጥር ፈልግ...", color = Color.Gray, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.LightGray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AmharicYellow,
                unfocusedBorderColor = Color.Gray,
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Sector Filter List
        ScrollableTabRow(
            selectedTabIndex = sectors.indexOf(selectedSector).coerceAtLeast(0),
            containerColor = Color.Transparent,
            edgePadding = 0.dp,
            divider = {},
            indicator = {}
        ) {
            sectors.forEach { sec ->
                val isSelected = sec == selectedSector
                Tab(
                    selected = isSelected,
                    onClick = { selectedSector = sec },
                    text = {
                        Text(
                            text = sec,
                            color = if (isSelected) AmharicYellow else Color.Gray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        GeezAlphabetFilterBar(
            selectedLetter = geezFilterLetter,
            onLetterSelected = { geezFilterLetter = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredArtisans.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(60.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("ምንም የልምድ ሰራተኛ አልተገኘም።", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredArtisans) { artisan ->
                    val artisanWarnings = warnings.filter { it.recipientName == artisan.name }
                    ArtisanCard(
                        artisan = artisan,
                        warningCount = artisanWarnings.size,
                        activeRegulation = activeRegulation,
                        onDelete = { onDeleteArtisan(artisan) },
                        onUpdate = onUpdateArtisan,
                        onAddWarningClick = { reason, severity ->
                            onAddWarning(artisan.name, artisan.phone, artisan.tradeSector, reason, severity)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ArtisanCard(
    artisan: Artisan,
    warningCount: Int,
    activeRegulation: GovernmentRegulation,
    onDelete: () -> Unit,
    onUpdate: (Artisan) -> Unit,
    onAddWarningClick: (reason: String, severity: String) -> Unit
) {
    val context = LocalContext.current
    var showWarningInput by remember { mutableStateOf(false) }
    var warningReason by remember { mutableStateOf("") }
    var warningSeverity by remember { mutableStateOf("ከፍተኛ") }
    var showAccreditationDialog by remember { mutableStateOf(false) }
    var inputCode by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf(false) }

    val isSuspended = warningCount >= activeRegulation.warningThreshold
    val isPermitMissing = !artisan.hasPermit

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSuspended) Color(0xFF351216) else CardBackground
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSuspended) BorderStroke(1.5.dp, AmharicRed) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Suspension Notification Header
            if (isSuspended) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AmharicRed.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = AmharicRed)
                    Column {
                        Text(
                            text = "⛔ የታገደ የልምድ ሰራተኛ (SUSPENDED)",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isPermitMissing && activeRegulation.isPermitMandatory) "ምክንያት፡ ፈቃድ ሳይኖረው በመስራቱና $warningCount / ${activeRegulation.warningThreshold} ማስጠንቀቂያዎች ስለተሰጡት" 
                                   else "ምክንያት፡ $warningCount / ${activeRegulation.warningThreshold} ማስጠንቀቂያዎች ስለተሰጡት",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (isSuspended) AmharicRed.copy(alpha = 0.2f) else AmharicGreen.copy(alpha = 0.15f), 
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = artisan.name.take(1),
                            color = if (isSuspended) AmharicRed else AmharicGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                    Column {
                        Text(text = artisan.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(text = "የስራ ዘርፍ፡ ${artisan.tradeSector}", color = AmharicYellow, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(
                                color = if (artisan.isAccredited) AmharicGreen.copy(alpha = 0.15f) else AmharicRed.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, if (artisan.isAccredited) AmharicGreen else AmharicRed),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (artisan.isAccredited) "🏅 እውቅና አለው" else "⚠️ እውቅና ማሻሻያ ያስፈልገዋል",
                                    color = if (artisan.isAccredited) AmharicGreen else AmharicRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "ሰርዝ", tint = AmharicRed.copy(alpha = 0.8f))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "ስልክ ቁጥር", color = Color.Gray, fontSize = 11.sp)
                    Text(text = artisan.phone, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "የብቃት ወረቀት (COC)", color = Color.Gray, fontSize = 11.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AmharicGreen, modifier = Modifier.size(14.dp))
                        Text(text = artisan.cocNumber, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Permit and Warnings section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Construction permit status
                Column {
                    Text(text = "የግንባታ ፈቃድ (Permit)", color = Color.Gray, fontSize = 11.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = if (artisan.hasPermit) AmharicGreen.copy(alpha = 0.15f) else AmharicRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, if (artisan.hasPermit) AmharicGreen else AmharicRed)
                        ) {
                            Text(
                                text = if (artisan.hasPermit) "ፈቃድ አለው ✅" else "ፈቃድ የለውም ❌",
                                color = if (artisan.hasPermit) AmharicGreen else AmharicRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        
                        // Small toggle switch for admin to revoke/grant permit
                        Switch(
                            checked = artisan.hasPermit,
                            onCheckedChange = { onUpdate(artisan.copy(hasPermit = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AmharicGreen,
                                checkedTrackColor = AmharicGreen.copy(alpha = 0.3f),
                                uncheckedThumbColor = AmharicRed,
                                uncheckedTrackColor = AmharicRed.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.scale(0.7f)
                        )
                    }
                }

                // Warnings counter
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "የተሰጡ ማስጠንቀቂያዎች", color = Color.Gray, fontSize = 11.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (warningCount >= activeRegulation.warningThreshold) AmharicRed else if (warningCount >= activeRegulation.warningThreshold / 2) AmharicYellow else AmharicGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$warningCount / ${activeRegulation.warningThreshold}",
                            color = if (warningCount >= activeRegulation.warningThreshold) AmharicRed else if (warningCount >= activeRegulation.warningThreshold / 2) AmharicYellow else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    Text(text = "ልምድ፡ ${artisan.experienceYears} አመታት", color = Color.LightGray, fontSize = 12.sp)
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showWarningInput = !showWarningInput },
                        colors = ButtonDefaults.buttonColors(containerColor = AmharicRed.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, AmharicRed),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(12.dp), tint = AmharicRed)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ማስጠንቀቂያ ጨምር", fontSize = 10.sp, color = AmharicRed)
                    }

                    Button(
                        onClick = { showAccreditationDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, AmharicGreen),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(12.dp), tint = AmharicGreen)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("እውቅና አሻሽል", fontSize = 10.sp, color = AmharicGreen)
                    }

                    TextButton(
                        onClick = {
                            Toast.makeText(context, "ስካን የተደረገው የ ${artisan.name} የብቃት ማረጋገጫ በሲስተም ውስጥ ተቀምጧል 📁", Toast.LENGTH_SHORT).show()
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Attachment, contentDescription = null, modifier = Modifier.size(16.dp), tint = AmharicYellow)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ሰነድ ክፈት", color = AmharicYellow, fontSize = 12.sp)
                    }
                }
            }

            // Expandable Inline Quick Warning Input Panel
            if (showWarningInput) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "🚨 አዲስ ማስጠንቀቂያ መመዝገቢያ",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = warningReason,
                    onValueChange = { warningReason = it },
                    placeholder = { Text("የማስጠንቀቂያ ምክንያት ይጻፉ...", color = Color.Gray, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AmharicYellow,
                        unfocusedBorderColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Severity selection
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("ዝቅተኛ", "ከፍተኛ", "ወሳኝ").forEach { sev ->
                            val isSevSelected = warningSeverity == sev
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSevSelected) AmharicRed else Color.White.copy(alpha = 0.05f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .clickable { warningSeverity = sev }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = sev, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { showWarningInput = false }) {
                            Text("ሰርዝ", color = Color.Gray, fontSize = 11.sp)
                        }
                        
                        Button(
                            onClick = {
                                if (warningReason.isNotBlank()) {
                                    onAddWarningClick(warningReason, warningSeverity)
                                    warningReason = ""
                                    showWarningInput = false
                                    Toast.makeText(context, "ማስጠንቀቂያ በተሳካ ሁኔታ ተመዝግቧል።", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "እባክዎ ምክንያት ያስገቡ!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AmharicRed),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("ማስጠንቀቂያ ስጥ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showAccreditationDialog) {
        val expMet = artisan.experienceYears >= activeRegulation.minExperienceYears
        val permitMet = artisan.hasPermit || !activeRegulation.isPermitMandatory
        val codeRequired = activeRegulation.requiresVerificationCode

        AlertDialog(
            onDismissRequest = { showAccreditationDialog = false },
            title = {
                Text(
                    text = "የመንግስት የሙያ እውቅና ማሻሻያ ማረጋገጫ (Accreditation Update)",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            containerColor = CardBackground,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "በአሁኑ ሰዓት የሚተገበረው አዲሱ የመንግስት አሰራር ፖሊሲ፡",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Surface(
                        color = AmharicYellow.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, AmharicYellow.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp).fillMaxWidth()) {
                            Text(text = activeRegulation.name, color = AmharicYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "• ዝቅተኛ የስራ ልምድ፡ ${activeRegulation.minExperienceYears} አመት", color = Color.White, fontSize = 11.sp)
                            Text(text = "• አስገዳጅ ፈቃድ፡ ${if (activeRegulation.isPermitMandatory) "አዎ (ግዴታ)" else "አይ (ተፈላጊ)"}", color = Color.White, fontSize = 11.sp)
                        }
                    }

                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))

                    Text(text = "የልምድ ሰራተኛው መመዘኛ ዝርዝር (Artisan Status):", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    // Experience check Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("1. የሥራ ልምድ (${artisan.experienceYears} አመት)", color = Color.White, fontSize = 12.sp)
                        if (expMet) {
                            Text("ያሟላል ✅", color = AmharicGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("አያሟላም ⚠️ (ያንስበታል)", color = AmharicRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Permit check Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("2. የግንባታ ፈቃድ (Permit)", color = Color.White, fontSize = 12.sp)
                        if (artisan.hasPermit) {
                            Text("አለው ✅", color = AmharicGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("የለውም ❌ (ፈቃድ ያስፈልገዋል)", color = AmharicRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (codeRequired) {
                        OutlinedTextField(
                            value = inputCode,
                            onValueChange = { 
                                inputCode = it
                                codeError = false
                            },
                            label = { Text("የመንግስት ማረጋገጫ የይለፍ ኮድ", fontSize = 11.sp) },
                            placeholder = { Text("ኮዱን '2026' ያስገቡ", fontSize = 11.sp) },
                            singleLine = true,
                            isError = codeError,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AmharicYellow,
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        if (codeError) {
                            Text("ማሳሰቢያ፡ ያስገቡት የመንግስት ኮድ ትክክል አይደለም! እባክዎ '2026' ያስገቡ።", color = AmharicRed, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (codeRequired && inputCode != "2026") {
                            codeError = true
                        } else {
                            onUpdate(
                                artisan.copy(
                                    isAccredited = true,
                                    accreditationNote = "የ${activeRegulation.name} እውቅና ማሻሻያ የተሰጠው",
                                    lastAccreditationUpdate = System.currentTimeMillis()
                                )
                            )
                            showAccreditationDialog = false
                            Toast.makeText(context, "የ ${artisan.name} የሙያ እውቅና በአዲሱ ደንብ መሰረት በተሳካ ሁኔታ ተሻሽሏል! 🏅", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen)
                ) {
                    Text("እውቅናውን አሻሽል")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccreditationDialog = false }) {
                    Text("ሰርዝ", color = Color.Gray)
                }
            }
        )
    }
}

// ==================== PROFESSIONALS SCREEN (አማካሪ/ተቋራጭ/ተቆጣጣሪ) ====================
@Composable
fun ProfessionalsScreen(
    professionals: List<Professional>,
    activeRegulation: GovernmentRegulation,
    onAddProfClick: () -> Unit,
    onDeleteProf: (Professional) -> Unit,
    onUpdateProf: (Professional) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("ሁሉም") }
    var geezFilterLetter by remember { mutableStateOf("ሁሉም") }

    val roles = listOf("ሁሉም", "የስራ ተቆጣጣሪ", "የኮንስትራክሽን አማካሪ", "የስራ ተቋራጭ")

    val filteredProfs = professionals.filter { prof ->
        val matchesRole = selectedRole == "ሁሉም" || prof.role == selectedRole
        val matchesQuery = prof.name.contains(searchQuery, ignoreCase = true)
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
        matchesRole && matchesQuery && matchesGeez
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "የኮንስትራክሽን ባለሙያዎችና ተቋራጮች",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Button(
                onClick = onAddProfClick,
                colors = ButtonDefaults.buttonColors(containerColor = ConstructionTeal),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("መዝግብ", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("በስም ፈልግ...", color = Color.Gray, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.LightGray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AmharicYellow,
                unfocusedBorderColor = Color.Gray,
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Role Tab Selectors
        ScrollableTabRow(
            selectedTabIndex = roles.indexOf(selectedRole).coerceAtLeast(0),
            containerColor = Color.Transparent,
            edgePadding = 0.dp,
            divider = {},
            indicator = {}
        ) {
            roles.forEach { role ->
                val isSelected = role == selectedRole
                Tab(
                    selected = isSelected,
                    onClick = { selectedRole = role },
                    text = {
                        Text(
                            text = role,
                            color = if (isSelected) AmharicYellow else Color.Gray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        GeezAlphabetFilterBar(
            selectedLetter = geezFilterLetter,
            onLetterSelected = { geezFilterLetter = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredProfs.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(60.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("ምንም የተመዘገበ ባለሙያ አልተገኘም።", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredProfs) { prof ->
                    ProfessionalCard(
                        prof = prof,
                        activeRegulation = activeRegulation,
                        onDelete = { onDeleteProf(prof) },
                        onUpdate = onUpdateProf
                    )
                }
            }
        }
    }
}

@Composable
fun ProfessionalCard(
    prof: Professional,
    activeRegulation: GovernmentRegulation,
    onDelete: () -> Unit,
    onUpdate: (Professional) -> Unit
) {
    val context = LocalContext.current
    var showAccreditationDialog by remember { mutableStateOf(false) }
    var inputCode by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                when (prof.role) {
                                    "የስራ ተቆጣጣሪ" -> AmharicRed.copy(alpha = 0.15f)
                                    "የኮንስትራክሽን አማካሪ" -> AmharicYellow.copy(alpha = 0.15f)
                                    else -> ConstructionTeal.copy(alpha = 0.15f)
                                },
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (prof.role) {
                                "የስራ ተቆጣጣሪ" -> Icons.Default.VerifiedUser
                                "የኮንስትራክሽን አማካሪ" -> Icons.Default.Business
                                else -> Icons.Default.Engineering
                            },
                            contentDescription = null,
                            tint = when (prof.role) {
                                "የስራ ተቆጣጣሪ" -> AmharicRed
                                "የኮንስትራክሽን አማካሪ" -> AmharicYellow
                                else -> ConstructionTeal
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(text = prof.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(text = prof.role, color = Color.LightGray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(
                                color = if (prof.isAccredited) AmharicGreen.copy(alpha = 0.15f) else AmharicRed.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, if (prof.isAccredited) AmharicGreen else AmharicRed),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (prof.isAccredited) "🏅 እውቅና አለው" else "⚠️ እውቅና ማሻሻያ ያስፈልገዋል",
                                    color = if (prof.isAccredited) AmharicGreen else AmharicRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "ሰርዝ", tint = AmharicRed.copy(alpha = 0.8f))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "ስልክ ቁጥር", color = Color.Gray, fontSize = 11.sp)
                    Text(text = prof.phone, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "ደረጃ & የፈቃድ ቁጥር", color = Color.Gray, fontSize = 11.sp)
                    Text(text = "${prof.grade} | ${prof.licenseNumber}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    Text(text = "ደረጃ፡ ${prof.grade}", color = Color.LightGray, fontSize = 12.sp)
                }

                Button(
                    onClick = { showAccreditationDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, AmharicGreen),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(12.dp), tint = AmharicGreen)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("እውቅና አሻሽል", fontSize = 11.sp, color = AmharicGreen)
                }
            }
        }
    }

    if (showAccreditationDialog) {
        val expMet = true
        val codeRequired = activeRegulation.requiresVerificationCode

        AlertDialog(
            onDismissRequest = { showAccreditationDialog = false },
            title = {
                Text(
                    text = "የመንግስት የሙያ እውቅና ማሻሻያ ማረጋገጫ (Accreditation Update)",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            containerColor = CardBackground,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "በአሁኑ ሰዓት የሚተገበረው አዲሱ የመንግስት አሰራር ፖሊሲ፡",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Surface(
                        color = AmharicYellow.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, AmharicYellow.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp).fillMaxWidth()) {
                            Text(text = activeRegulation.name, color = AmharicYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "• ዝቅተኛ የስራ ልምድ፡ ${activeRegulation.minExperienceYears} አመት", color = Color.White, fontSize = 11.sp)
                        }
                    }

                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))

                    Text(text = "የባለሙያው መመዘኛ ዝርዝር (Professional Status):", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    // Experience check Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("1. የፈቃድ ምዝገባ ደረጃ (${prof.grade})", color = Color.White, fontSize = 12.sp)
                        Text("ያሟላል ✅", color = AmharicGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // License check Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("2. የፈቃድ ቁጥር (License)", color = Color.White, fontSize = 12.sp)
                        Text("${prof.licenseNumber} ✅", color = AmharicGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (codeRequired) {
                        OutlinedTextField(
                            value = inputCode,
                            onValueChange = { 
                                inputCode = it
                                codeError = false
                            },
                            label = { Text("የመንግስት ማረጋገጫ የይለፍ ኮድ", fontSize = 11.sp) },
                            placeholder = { Text("ኮዱን '2026' ያስገቡ", fontSize = 11.sp) },
                            singleLine = true,
                            isError = codeError,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AmharicYellow,
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        if (codeError) {
                            Text("ማሳሰቢያ፡ ያስገቡት የመንግስት ኮድ ትክክል አይደለም! እባክዎ '2026' ያስገቡ።", color = AmharicRed, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (codeRequired && inputCode != "2026") {
                            codeError = true
                        } else {
                            onUpdate(
                                prof.copy(
                                    isAccredited = true,
                                    accreditationNote = "የ${activeRegulation.name} እውቅና ማሻሻያ የተሰጠው",
                                    lastAccreditationUpdate = System.currentTimeMillis()
                                )
                            )
                            showAccreditationDialog = false
                            Toast.makeText(context, "የ ${prof.name} የሙያ እውቅና በአዲሱ ደንብ መሰረት በተሳካ ሁኔታ ተሻሽሏል! 🏅", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen)
                ) {
                    Text("እውቅናውን አሻሽል")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccreditationDialog = false }) {
                    Text("ሰርዝ", color = Color.Gray)
                }
            }
        )
    }
}

// ==================== REPORTS AND ALERTS SCREEN ====================
@Composable
fun ReportsAndAlertsScreen(
    warnings: List<Warning>,
    professionals: List<Professional>,
    artisans: List<Artisan>,
    viewModel: ConstructionViewModel,
    onSendWarningClick: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "ክትትል፡ ቁጥጥርና ሪፖርቶች",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        var reportSubTab by remember { mutableStateOf(0) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SteelGray)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf(
                Pair("ደንብና ሪፖርቶች", Icons.Default.Assessment),
                Pair("የኢሜይል ማስጠንቀቂያ (Email Alerts)", Icons.Default.Email)
            )
            tabs.forEachIndexed { index, (title, icon) ->
                val isSelected = reportSubTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) AmharicYellow.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) AmharicYellow else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { reportSubTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) AmharicYellow else Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = title,
                            color = if (isSelected) AmharicYellow else Color.LightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (reportSubTab == 0) {
            // 1. Government Policy and Regulation Selector
        var showRegulationDropdown by remember { mutableStateOf(false) }
        val activeReg by viewModel.activeRegulation.collectAsState()

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, AmharicYellow.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Gavel, contentDescription = null, tint = AmharicYellow)
                        Text(
                            text = "የመንግስት ደንብና አሰራር ቁጥጥር",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                    
                    Surface(
                        color = AmharicYellow.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.clickable { showRegulationDropdown = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("ደንብ ቀይር ⚙️", color = AmharicYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "የነቃው መመሪያ፡ ${activeReg.name}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = activeReg.description,
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ማስጠንቀቂያ ገደብ", color = Color.Gray, fontSize = 11.sp)
                        Text("${activeReg.warningThreshold} ማስጠንቀቂያ", color = AmharicRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("የግንባታ ፈቃድ", color = Color.Gray, fontSize = 11.sp)
                        Text(if (activeReg.isPermitMandatory) "ግዴታ ነው ⚠️" else "አያስገድድም ✅", color = if (activeReg.isPermitMandatory) AmharicYellow else AmharicGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("አነስተኛ ልምድ", color = Color.Gray, fontSize = 11.sp)
                        Text("${activeReg.minExperienceYears} ዓመት", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Regulation picker dropdown / dialog
        if (showRegulationDropdown) {
            AlertDialog(
                onDismissRequest = { showRegulationDropdown = false },
                title = { Text("አዲስ የመንግስት አሰራር/መመሪያ ይምረጡ", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                containerColor = CardBackground,
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        viewModel.regulations.forEach { reg ->
                            val isSelected = reg.id == activeReg.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectRegulation(reg.id)
                                        showRegulationDropdown = false
                                        Toast.makeText(context, "የመንግስት አሰራር ወደ '${reg.name}' ተቀይሯል!", Toast.LENGTH_SHORT).show()
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) AmharicYellow.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.03f)
                                ),
                                border = if (isSelected) BorderStroke(1.5.dp, AmharicYellow) else null,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = reg.name, color = if (isSelected) AmharicYellow else Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = reg.description, color = Color.LightGray, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showRegulationDropdown = false }) {
                        Text("ዝጋ", color = AmharicYellow)
                    }
                }
            )
        }

        // Export Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ኦፊሴላዊ የከተማ ሪፖርቶች ማውረጃ (Export Center)",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "የቲሊሊ ከተማ የግንባታ ፕሮጀክቶች፣ የልምድ ሰራተኞች እና የባለሙያዎች የተሟላ መዝገብ ማውጫ።",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.LightGray
                    ),
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.shareReport(context, isExcel = true) },
                        colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_excel_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.GridOn, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Excel ሪፖርት (CSV)", fontSize = 11.sp, maxLines = 1)
                    }

                    Button(
                        onClick = { viewModel.shareReport(context, isExcel = false) },
                        colors = ButtonDefaults.buttonColors(containerColor = ConstructionTeal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_pdf_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PDF ሪፖርት (PDF)", fontSize = 11.sp, maxLines = 1)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "የግንባታ ክትትልና ደህንነት ማጠቃለያ ሪፖርት (EBCS Compliance Summary)",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AmharicYellow
                    )
                )
                Text(
                    text = "በብሔራዊ የህንጻ ኮዶች (EBCS Standards) መሠረት የተመዘገቡ ዕለታዊ ግንባታ መከታተያዎችን (Compliance Progress) እና የተገመገሙ አደጋዎችን (Site Hazards) ያካተተ የተሟላ ማጠቃለያ ሪፖርት።",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.LightGray
                    ),
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.shareSummaryReport(context, isExcel = true) },
                        colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_summary_excel_button"),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, AmharicYellow.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ማጠቃለያ Excel", fontSize = 11.sp, maxLines = 1)
                    }

                    Button(
                        onClick = { viewModel.shareSummaryReport(context, isExcel = false) },
                        colors = ButtonDefaults.buttonColors(containerColor = AmharicRed),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_summary_pdf_button"),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, AmharicYellow.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.DownloadForOffline, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ማጠቃለያ PDF (PDF)", fontSize = 11.sp, maxLines = 1)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "የተላኩ ማስጠንቀቂያዎች ታሪክ",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Button(
                onClick = onSendWarningClick,
                colors = ButtonDefaults.buttonColors(containerColor = AmharicRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("ማስጠንቀቂያ ላክ", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (warnings.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NotificationsOff, contentDescription = null, modifier = Modifier.size(60.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("ማስጠንቀቂያ ታሪክ ባዶ ነው።", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(warnings) { warning ->
                    WarningItem(warning = warning, onDelete = { viewModel.deleteWarning(warning) })
                }
            }
        }
    } else {
        // ==================== AUTOMATED NOTIFICATION & EMAIL ALERT DASHBOARD ====================
            val siteEngEmail by viewModel.siteEngineerEmail.collectAsState()
            val supervisorEmail by viewModel.supervisorEmail.collectAsState()
            val thresholdDays by viewModel.deadlineThresholdDays.collectAsState()
            val alerts by viewModel.automatedAlerts.collectAsState()

            var inputEngEmail by remember { mutableStateOf(siteEngEmail) }
            var inputSupEmail by remember { mutableStateOf(supervisorEmail) }
            var inputThreshold by remember { mutableStateOf(thresholdDays.toString()) }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Settings Configuration Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, tint = AmharicYellow)
                                Text(
                                    text = "የኢሜይልና ማስጠንቀቂያ መቆጣጠሪያ (Settings)",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = inputEngEmail,
                                onValueChange = { inputEngEmail = it },
                                label = { Text("የሳይት መሃንዲስ ኢሜይል (Site Engineer)", color = Color.LightGray) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AmharicYellow,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f),
                                    focusedLabelColor = AmharicYellow,
                                    unfocusedLabelColor = Color.LightGray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = inputSupEmail,
                                onValueChange = { inputSupEmail = it },
                                label = { Text("ተቆጣጣሪ/ሱፐርቫይዘር ኢሜይል (Supervisor)", color = Color.LightGray) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AmharicYellow,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f),
                                    focusedLabelColor = AmharicYellow,
                                    unfocusedLabelColor = Color.LightGray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = inputThreshold,
                                onValueChange = { inputThreshold = it.filter { char -> char.isDigit() } },
                                label = { Text("የቀነ-ገደብ ማሳወቂያ ቀኖች (Threshold Days)", color = Color.LightGray) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AmharicYellow,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f),
                                    focusedLabelColor = AmharicYellow,
                                    unfocusedLabelColor = Color.LightGray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    val thresholdVal = inputThreshold.toIntOrNull() ?: 30
                                    if (inputEngEmail.contains("@") && inputSupEmail.contains("@")) {
                                        viewModel.updateNotificationSettings(inputEngEmail, inputSupEmail, thresholdVal)
                                        Toast.makeText(context, "የኢሜይል ማስጠንቀቂያ መቆጣጠሪያ ቅንጅቶች ተቀምጠዋል! 💾", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "እባክዎን ትክክለኛ ኢሜይል አድራሻ ያስገቡ!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ቅንጅቶችን አስቀምጥ (Save Settings)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Alerts Title Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "አውቶማቲክ ማስጠንቀቂያዎችና ኢሜይሎች (${alerts.size})",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        if (alerts.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    viewModel.clearSentAlertHistory()
                                    Toast.makeText(context, "የኢሜይል ማስጠንቀቂያ ታሪክ ተጠርጓል!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text("ታሪክ አጽዳ 🔄", color = AmharicYellow, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Alerts Items
                if (alerts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = AmharicGreen, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "ምንም ማስጠንቀቂያ ወይም የቀነ-ገደብ ስጋት የለም!",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "ሁሉም ግንባታዎች በሊዝ ቀነ-ገደብ መሠረት በመልካም ሁኔታ ላይ ናቸው።",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(alerts) { alert ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (alert.type == "HIGH_PRIORITY_RISK") AmharicRed.copy(alpha = 0.5f) else AmharicYellow.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
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
                                            imageVector = if (alert.type == "HIGH_PRIORITY_RISK") Icons.Default.Warning else Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = if (alert.type == "HIGH_PRIORITY_RISK") AmharicRed else AmharicYellow,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = alert.title,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }

                                    Surface(
                                        color = if (alert.isSent) AmharicGreen.copy(alpha = 0.15f) else AmharicYellow.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (alert.isSent) "ተልኳል ✅" else "አልተላከም ⚠️",
                                            color = if (alert.isSent) AmharicGreen else AmharicYellow,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = alert.description,
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "ተቀባዮች (Recipients):",
                                            color = Color.Gray,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = alert.recipientEmails.joinToString(", "),
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }

                                    Button(
                                        onClick = { viewModel.triggerEmailIntent(context, alert) },
                                        colors = ButtonDefaults.buttonColors(containerColor = ConstructionTeal),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("send_email_button_" + alert.id)
                                    ) {
                                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ኢሜይል ላክ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WarningItem(
    warning: Warning,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                when (warning.severity) {
                                    "ወሳኝ" -> AmharicRed
                                    "ከፍተኛ" -> AmharicYellow
                                    else -> AmharicGreen
                                },
                                CircleShape
                            )
                    )
                    Text(
                        text = "ለ ${warning.recipientName} (${warning.recipientRole})",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "አጥፋ", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = warning.message, color = Color.LightGray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = warning.recipientPhone, color = Color.Gray, fontSize = 11.sp)
                }

                Surface(
                    color = AmharicGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = AmharicGreen, modifier = Modifier.size(10.dp))
                        Text(text = "በስልክ ተልኳል", color = AmharicGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==================== DIALOGS ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectDialog(
    professionals: List<Professional>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, location: String, category: String, type: String, materials: String, consultantId: Int?, contractorId: Int?, inspectorId: Int?, status: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("ቀበሌ ") }
    var selectedCategory by remember { mutableStateOf("ምድብ 'ሀ' (ከሁለት ፎቅ በታች የሆኑ ወይም አነስተኛ ህንጻዎች)") }
    var type by remember { mutableStateOf("የመኖሪያ ቤት ግንባታ") }
    var selectedMaterials = remember { mutableStateListOf<String>() }
    var status by remember { mutableStateOf("በግንባታ ላይ") }

    val categories = listOf(
        "ምድብ 'ሀ' (ከሁለት ፎቅ በታች የሆኑ ወይም አነስተኛ ህንጻዎች)",
        "ምድብ 'ለ' (ባለብዙ ፎቅ ህንጻዎች)",
        "ምድብ 'ሐ' (ከፍተኛ ህንጻዎች / ፋብሪካዎች)",
        "ምድብ 'መ' (ልዩ ግንባታዎች)",
        "የጭቃ ቤት",
        "ፎቅ የሌላቸው / Basement",
        "ጊዜያዊ ግንባታ"
    )

    val materialOptions = listOf("ኮንክሪት እና ብረት", "የሲሚንቶ ብሎኬት (HCB)", "ድንጋይ", "ጡብ", "እንጨትና ጭቃ", "ቆርቆሮ", "ጂፕሰም")

    // Link Professionals
    val inspectors = professionals.filter { it.role == "የስራ ተቆጣጣሪ" }
    val consultants = professionals.filter { it.role == "የኮንስትራክሽን አማካሪ" }
    val contractors = professionals.filter { it.role == "የስራ ተቋራጭ" }

    var selectedInspectorId by remember { mutableStateOf<Int?>(null) }
    var selectedConsultantId by remember { mutableStateOf<Int?>(null) }
    var selectedContractorId by remember { mutableStateOf<Int?>(null) }

    var expandedCat by remember { mutableStateOf(false) }
    var expandedInsp by remember { mutableStateOf(false) }
    var expandedCons by remember { mutableStateOf(false) }
    var expandedCont by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            colors = CardDefaults.cardColors(containerColor = SteelGray),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "አዲስ ግንባታ መመዝገቢያ",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("የግንባታው ስም / ባለቤት") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("ቦታ / ቀበሌ") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Dropdown
                Box {
                    OutlinedButton(
                        onClick = { expandedCat = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "ምድብ፡ $selectedCategory", maxLines = 1, overflow = TextOverflow.Ellipsis, color = AmharicYellow)
                    }
                    DropdownMenu(expanded = expandedCat, onDismissRequest = { expandedCat = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    expandedCat = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("የግንባታው አይነት (ለምሳሌ፡ የመኖሪያ ቤት፣ መጋዘን...)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                // Materials Selection
                Text("ጥቅም ላይ የሚውሉ የኢትዮጵያ ግንባታ ቁሳቁሶች፡", color = Color.LightGray, fontSize = 12.sp)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    materialOptions.forEach { mat ->
                        val isChecked = selectedMaterials.contains(mat)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) selectedMaterials.remove(mat) else selectedMaterials.add(mat)
                                }
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    if (isChecked) selectedMaterials.remove(mat) else selectedMaterials.add(mat)
                                }
                            )
                            Text(text = mat, color = Color.White, fontSize = 13.sp)
                        }
                    }
                }

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                Text("ባለሙያዎችን አገናኝ (Link Authorities):", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                // Inspectors
                Box {
                    OutlinedButton(onClick = { expandedInsp = true }, modifier = Modifier.fillMaxWidth()) {
                        val inspector = inspectors.find { it.id == selectedInspectorId }
                        Text(text = inspector?.let { "ተቆጣጣሪ፡ ${it.name}" } ?: "የስራ ተቆጣጣሪ ምረጥ", color = Color.White)
                    }
                    DropdownMenu(expanded = expandedInsp, onDismissRequest = { expandedInsp = false }) {
                        inspectors.forEach { insp ->
                            DropdownMenuItem(text = { Text(insp.name) }, onClick = { selectedInspectorId = insp.id; expandedInsp = false })
                        }
                    }
                }

                // Consultants
                Box {
                    OutlinedButton(onClick = { expandedCons = true }, modifier = Modifier.fillMaxWidth()) {
                        val consultant = consultants.find { it.id == selectedConsultantId }
                        Text(text = consultant?.let { "አማካሪ፡ ${it.name}" } ?: "የኮንስትራክሽን አማካሪ ምረጥ", color = Color.White)
                    }
                    DropdownMenu(expanded = expandedCons, onDismissRequest = { expandedCons = false }) {
                        consultants.forEach { cons ->
                            DropdownMenuItem(text = { Text(cons.name) }, onClick = { selectedConsultantId = cons.id; expandedCons = false })
                        }
                    }
                }

                // Contractors
                Box {
                    OutlinedButton(onClick = { expandedCont = true }, modifier = Modifier.fillMaxWidth()) {
                        val contractor = contractors.find { it.id == selectedContractorId }
                        Text(text = contractor?.let { "ተቋራጭ፡ ${it.name}" } ?: "የስራ ተቋራጭ ምረጥ", color = Color.White)
                    }
                    DropdownMenu(expanded = expandedCont, onDismissRequest = { expandedCont = false }) {
                        contractors.forEach { cont ->
                            DropdownMenuItem(text = { Text(cont.name) }, onClick = { selectedContractorId = cont.id; expandedCont = false })
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("የግንባታ ሁኔታ፡", color = Color.White, fontSize = 13.sp)
                    listOf("በዝግጅት ላይ", "በግንባታ ላይ", "የተጠናቀቀ").forEach { st ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { status = st }) {
                            RadioButton(selected = status == st, onClick = { status = st })
                            Text(st, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("ተመለስ", color = Color.White)
                    }
                    Button(
                        onClick = {
                            if (name.isNotEmpty() && selectedMaterials.isNotEmpty()) {
                                onConfirm(
                                    name,
                                    location,
                                    selectedCategory.substringBefore(" ("),
                                    type,
                                    selectedMaterials.joinToString(", "),
                                    selectedConsultantId,
                                    selectedContractorId,
                                    selectedInspectorId,
                                    status
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("መዝግብ")
                    }
                }
            }
        }
    }
}

@Composable
fun AddArtisanDialog(
    prefilledName: String,
    prefilledTrade: String,
    prefilledCoc: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, tradeSector: String, cocNumber: String, experience: Int) -> Unit
) {
    var name by remember { mutableStateOf(prefilledName) }
    var phone by remember { mutableStateOf("+2519") }
    var selectedSector by remember { mutableStateOf(if (prefilledTrade.isNotEmpty()) prefilledTrade else "ግንበኛ") }
    var cocNumber by remember { mutableStateOf(if (prefilledCoc.isNotEmpty()) prefilledCoc else "COC-") }
    var experience by remember { mutableStateOf("3") }

    val sectors = listOf("ግንበኛ", "አናፂ", "ፌሮዮ (የግንባታ ብረት ሰራተኛ)", "ኤሌክትሪሺያን", "ቧንቧ ሰራተኛ")
    var expandedSector by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SteelGray),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "አዲስ የልምድ ሰራተኛ ምዝገባ (Skilled Labor)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )

                if (prefilledCoc.isNotEmpty()) {
                    Surface(
                        color = AmharicGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, AmharicGreen)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AmharicGreen)
                            Text("ሰነድ በተሳካ ሁኔታ ስካን ተደርጓል! መረጃዎች በራስ-ሰር ተሞልተዋል።", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ሙሉ ስም") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("ስልክ ቁጥር") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                // Trade Sector Dropdown
                Box {
                    OutlinedButton(onClick = { expandedSector = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "የስራ ዘርፍ፡ $selectedSector", color = Color.White)
                    }
                    DropdownMenu(expanded = expandedSector, onDismissRequest = { expandedSector = false }) {
                        sectors.forEach { sec ->
                            DropdownMenuItem(text = { Text(sec) }, onClick = { selectedSector = sec; expandedSector = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = cocNumber,
                    onValueChange = { cocNumber = it },
                    label = { Text("የብቃት ወረቀት (COC) ቁጥር") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = experience,
                    onValueChange = { experience = it },
                    label = { Text("የስራ ዘርፍ ልምድ (በአመታት)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("ተመለስ", color = Color.White)
                    }
                    Button(
                        onClick = {
                            if (name.isNotEmpty() && phone.isNotEmpty() && cocNumber.isNotEmpty()) {
                                onConfirm(name, phone, selectedSector, cocNumber, experience.toIntOrNull() ?: 1)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("መዝግብ")
                    }
                }
            }
        }
    }
}

@Composable
fun AddProfessionalDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, role: String, grade: String, license: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+2519") }
    var selectedRole by remember { mutableStateOf("የስራ ተቆጣጣሪ") }
    var grade by remember { mutableStateOf("Grade ") }
    var license by remember { mutableStateOf("ETH-") }

    val roles = listOf("የስራ ተቆጣጣሪ", "የኮንስትራክሽን አማካሪ", "የስራ ተቋራጭ")
    var expandedRole by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SteelGray),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "አዲስ ባለሙያ / ድርጅት ምዝገባ",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ሙሉ ስም / የድርጅት ስም") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("ስልክ ቁጥር") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                // Role Dropdown
                Box {
                    OutlinedButton(onClick = { expandedRole = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "የሙያ ሚና፡ $selectedRole", color = Color.White)
                    }
                    DropdownMenu(expanded = expandedRole, onDismissRequest = { expandedRole = false }) {
                        roles.forEach { role ->
                            DropdownMenuItem(text = { Text(role) }, onClick = { selectedRole = role; expandedRole = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = grade,
                    onValueChange = { grade = it },
                    label = { Text("የሙያ ደረጃ (ለምሳሌ፡ Grade 1, GC-4)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = license,
                    onValueChange = { license = it },
                    label = { Text("የሙያ ወይም የንግድ ፈቃድ ቁጥር") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("ተመለስ", color = Color.White)
                    }
                    Button(
                        onClick = {
                            if (name.isNotEmpty() && phone.isNotEmpty()) {
                                onConfirm(name, phone, selectedRole, grade, license)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("መዝግብ")
                    }
                }
            }
        }
    }
}

@Composable
fun SendWarningDialog(
    professionals: List<Professional>,
    artisans: List<Artisan>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, role: String, message: String, severity: String) -> Unit
) {
    var selectedName by remember { mutableStateOf("") }
    var selectedPhone by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf("ከፍተኛ") }

    var expandedSelect by remember { mutableStateOf(false) }

    // Combined list for targets
    val targets = professionals.map { Triple(it.name, it.phone, it.role) } +
            artisans.map { Triple(it.name, it.phone, it.tradeSector) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SteelGray),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "ማስጠንቀቂያ መላኪያ (SMS Dispatcher)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )

                // Select Target Dropdown
                Box {
                    OutlinedButton(onClick = { expandedSelect = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (selectedName.isNotEmpty()) "ተቀባይ፡ $selectedName ($selectedRole)" else "ማስጠንቀቂያ የሚላክለትን ባለሙያ ምረጥ",
                            color = AmharicYellow,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    DropdownMenu(expanded = expandedSelect, onDismissRequest = { expandedSelect = false }) {
                        targets.forEach { target ->
                            DropdownMenuItem(
                                text = { Text("${target.first} (${target.third})") },
                                onClick = {
                                    selectedName = target.first
                                    selectedPhone = target.second
                                    selectedRole = target.third
                                    expandedSelect = false
                                }
                            )
                        }
                    }
                }

                if (selectedPhone.isNotEmpty()) {
                    Text("ስልክ ቁጥር፡ $selectedPhone", color = Color.LightGray, fontSize = 12.sp)
                }

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("የማስጠንቀቂያ መልዕክት") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )

                Text("የማስጠንቀቂያ ክብደት ደረጃ፡", color = Color.White, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ዝቅተኛ", "ከፍተኛ", "ወሳኝ").forEach { sev ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { severity = sev }) {
                            RadioButton(selected = severity == sev, onClick = { severity = sev })
                            Text(sev, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("ተመለስ", color = Color.White)
                    }
                    Button(
                        onClick = {
                            if (selectedName.isNotEmpty() && message.isNotEmpty()) {
                                onConfirm(selectedName, selectedPhone, selectedRole, message, severity)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmharicRed),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("በስልክ ላክ")
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectDetailDialog(
    project: Project,
    professionals: List<Professional>,
    onDismiss: () -> Unit
) {
    val inspector = professionals.find { it.id == project.inspectorId }
    val consultant = professionals.find { it.id == project.consultantId }
    val contractor = professionals.find { it.id == project.contractorId }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SteelGray),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "የግንባታ ዝርዝር መረጃ",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "ዝጋ", tint = Color.LightGray)
                    }
                }

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))

                Text(text = project.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = AmharicYellow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "ቦታ፡ ${project.location}", color = Color.LightGray, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                DetailItemRow(label = "የሕንፃ ምድብ", value = project.buildingCategory, color = AmharicYellow)
                DetailItemRow(label = "የግንባታ አይነት", value = project.constructionType, color = Color.White)
                DetailItemRow(label = "ጥቅም ላይ የዋሉ ቁሳቁሶች", value = project.materials, color = Color.White)
                DetailItemRow(label = "የግንባታ ሁኔታ", value = project.status, color = AmharicGreen)

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                Text("የተመደቡ ባለሙያዎችና ድርጅቶች፡", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                ProfessionalLinkRow(roleLabel = "የስራ ተቆጣጣሪ (Inspector)", professional = inspector)
                ProfessionalLinkRow(roleLabel = "የኮንስትራክሽን አማካሪ (Consultant)", professional = consultant)
                ProfessionalLinkRow(roleLabel = "የስራ ተቋራጭ (Contractor)", professional = contractor)

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                Text("የምህንድስና ሶፍትዌሮች ትስስር መረጃ (CAD/BIM/GIS):", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                DetailItemRow(
                    label = "የ CAD ወሰን መጋጠሚያዎች",
                    value = if (!project.cadCoordinates.isNullOrBlank()) "ተገናኝቷል (የነጥቦች ብዛት: ${project.cadCoordinates?.split(";")?.size ?: 0})" else "ያልተገናኘ",
                    color = if (!project.cadCoordinates.isNullOrBlank()) AutoCADColor else Color.Gray
                )
                DetailItemRow(
                    label = "የ BIM የተገመተ ወጪ (Revit)",
                    value = if (project.bimEstimatedCost != null) {
                        val format = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("en", "US")).apply {
                            maximumFractionDigits = 0
                        }
                        format.format(project.bimEstimatedCost).replace("$", "ETB ")
                    } else "ያልተገናኘ",
                    color = if (project.bimEstimatedCost != null) RevitColor else Color.Gray
                )
                DetailItemRow(
                    label = "የእቅድ አፈጻጸም ሂደት (Primavera)",
                    value = "${project.scheduleProgressPercent}%",
                    color = PrimaveraColor
                )
                DetailItemRow(
                    label = "የካርታ መገኛ (QGIS Latitude/Longitude)",
                    value = if (project.gisLatitude != null && project.gisLongitude != null) {
                        "${project.gisLatitude}, ${project.gisLongitude}"
                    } else "ያልተገናኘ",
                    color = if (project.gisLatitude != null) GISColor else Color.Gray
                )
                DetailItemRow(
                    label = "የመዋቅር ደህንነት ደረጃ (ETABS)",
                    value = if (project.etabsSafetyScore != null) "${project.etabsSafetyScore}%" else "ያልተገናኘ",
                    color = if (project.etabsSafetyScore != null) {
                        if (project.etabsSafetyScore!! >= 85) AmharicGreen else if (project.etabsSafetyScore!! >= 60) AmharicYellow else AmharicRed
                    } else Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ConstructionTeal),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("እሺ")
                }
            }
        }
    }
}

@Composable
fun DetailItemRow(label: String, value: String, color: Color) {
    Column {
        Text(text = label, color = Color.Gray, fontSize = 11.sp)
        Text(text = value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
fun ProfessionalLinkRow(roleLabel: String, professional: Professional?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BackgroundSlate.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = roleLabel, color = AmharicYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            if (professional != null) {
                Text(text = professional.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "ስልክ፡ ${professional.phone}", color = Color.LightGray, fontSize = 11.sp)
                    Text(text = "ደረጃ፡ ${professional.grade}", color = Color.LightGray, fontSize = 11.sp)
                }
            } else {
                Text(text = "የተመደበ አካል የለም", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Light)
            }
        }
    }
}

// ==================== INTERACTIVE SIMULATED SCANNER VIEW ====================
@Composable
fun CertificateScannerView(
    onScanComplete: (name: String, trade: String, coc: String) -> Unit,
    onClose: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isScanning by remember { mutableStateOf(true) }
    var progress by remember { mutableStateOf(0f) }
    var scanMessage by remember { mutableStateOf("የብቃት ማረጋገጫ (COC) ሰነድ ይፈልጉ...") }

    val scanAnimation = rememberInfiniteTransition(label = "scan")
    val laserOffset by scanAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser"
    )

    // Simulate OCR reading of Ethiopian artisans certifications
    val simulatedNames = listOf("ሙሉጌታ ፈይሳ", "ደመቀ አስራት", "አሸናፊ ዘነበ", "ዘነበች አበራ", "ታሪኩ ታደሰ")
    val simulatedTrades = listOf("ግንበኛ", "አናፂ", "ቧንቧ ሰራተኛ", "ኤሌክትሪሺያን", "ፌሮዮ (የግንባታ ብረት ሰራተኛ)")

    LaunchedEffect(Unit) {
        delay(1000)
        scanMessage = "የሰነድ ምስል ተገኝቷል! OCR በመተንተን ላይ..."
        while (progress < 1.0f) {
            delay(150)
            progress += 0.1f
        }
        scanMessage = "ተንትኖ ተጠናቋል። መረጃዎች እየተጫኑ ነው..."
        delay(800)
        isScanning = false
        val randomName = simulatedNames.random()
        val randomTrade = simulatedTrades.random()
        val randomCoc = "COC-${randomTrade.take(3).uppercase()}-${(1000..9999).random()}-26"
        onScanComplete(randomName, randomTrade, randomCoc)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("የብቃት ማረጋገጫ ስካነር", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "ዝጋ", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Simulated Camera Frame
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .border(BorderStroke(3.dp, AmharicYellow), RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.DarkGray)
            ) {
                // Background simulated document preview
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = Color.LightGray.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "የኢትዮጵያ ፌዴራላዊ ዲሞክራሲያዊ ሪፐብሊክ\nየብቃት ማረጋገጫ ወረቀት (COC)",
                        color = Color.LightGray.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Sweeping Laser Beam
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = (280 * laserOffset).dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, AmharicRed, Color.Transparent)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            CircularProgressIndicator(progress = { progress }, color = AmharicYellow)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = scanMessage,
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "እባክዎ የምስክር ወረቀቱን በካሜራው ሳጥን ውስጥ ያሳዩ",
                color = Color.Gray,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==================== GE'EZ ALPHABET SEARCH INDEX HELPERS ====================
fun getGeezFamily(char: Char): List<Char> {
    val families = listOf(
        listOf('ሀ', 'ሁ', 'ሂ', 'ሃ', 'ሄ', 'ህ', 'ሆ'),
        listOf('ለ', 'ሉ', 'ሊ', 'ላ', 'ሌ', 'ል', 'ሎ', 'ሏ'),
        listOf('ሐ', 'ሑ', 'ሒ', 'ሓ', 'ሔ', 'ሕ', 'ሖ', 'ሗ'),
        listOf('መ', 'ሙ', 'ሚ', 'ማ', 'ሜ', 'ም', 'ሞ', 'ሟ'),
        listOf('ሠ', 'ሡ', 'ሢ', 'ሣ', 'ሤ', 'ሥ', 'ሦ', 'ሧ'),
        listOf('ረ', 'ሩ', 'ሪ', 'ራ', 'ሬ', 'ር', 'ሮ', 'ሯ'),
        listOf('ሰ', 'ሱ', 'ሲ', 'ሳ', 'ሴ', 'ስ', 'ሶ', 'ሷ'),
        listOf('ሸ', 'ሹ', 'ሺ', 'ሻ', 'ሼ', 'ሽ', 'ሾ', 'ሿ'),
        listOf('ቀ', 'ቁ', 'ቂ', 'ቃ', 'ቄ', 'ቅ', 'ቆ', 'ቋ'),
        listOf('በ', 'ቡ', 'ቢ', 'ባ', 'ቤ', 'ብ', 'ቦ', 'ቧ'),
        listOf('ተ', 'ቱ', 'ቲ', 'ታ', 'ቴ', 'ት', 'ቶ', 'ቷ'),
        listOf('ቸ', 'ቹ', 'ቺ', 'ቻ', 'ቼ', 'ች', 'ቾ', 'ቿ'),
        listOf('ነ', 'ኑ', 'ኒ', 'ና', 'ኔ', 'ን', 'ኖ', 'ኗ'),
        listOf('ኘ', 'ኙ', 'ኚ', 'ኛ', 'ኜ', 'ኝ', 'ኞ', 'ኟ'),
        listOf('አ', 'ኡ', 'ኢ', 'ኣ', 'ኤ', 'እ', 'ኦ', 'ኧ'),
        listOf('ከ', 'ኩ', 'ኪ', 'ካ', 'ኬ', 'ክ', 'ኮ', 'ኳ'),
        listOf('ወ', 'ዉ', 'ዊ', 'ዋ', 'ዌ', 'ው', 'ዎ'),
        listOf('ዐ', 'ዑ', 'ዒ', 'ዓ', 'ዔ', 'ዕ', 'ዖ'),
        listOf('ዘ', 'ዙ', 'ዚ', 'ዛ', 'ዜ', 'ዝ', 'ዞ', 'ዟ'),
        listOf('ዠ', 'ዡ', 'ዢ', 'ዣ', 'ዤ', 'ዥ', 'ዦ', 'ዧ'),
        listOf('የ', 'ዩ', 'ዪ', 'ያ', 'ዬ', 'ይ', 'ዮ'),
        listOf('ደ', 'ዱ', 'ዲ', 'ዳ', 'ዴ', 'ድ', 'ዶ', 'ዷ'),
        listOf('ጀ', 'ጁ', 'ጂ', 'ጃ', 'ጄ', 'ጅ', 'ጆ', 'ጇ'),
        listOf('ገ', 'ጉ', 'ጊ', 'ጋ', 'ጌ', 'ግ', 'ጎ', 'ጓ'),
        listOf('ጠ', 'ጡ', 'ጢ', 'ጣ', 'ጤ', 'ጥ', 'ጦ', 'ጧ'),
        listOf('ጨ', 'ጩ', 'ጪ', 'ጫ', 'ጬ', 'ጭ', 'ጮ', 'ጯ'),
        listOf('ጰ', 'ጱ', 'ጲ', 'ጳ', 'ጴ', 'ጵ', 'ጶ', 'ጷ'),
        listOf('ጸ', 'ጹ', 'ጺ', 'ጻ', 'ጼ', 'ጽ', 'ጾ', 'ጿ'),
        listOf('ፀ', 'ፁ', 'ፂ', 'ፃ', 'ፄ', 'ፅ', 'ፆ'),
        listOf('ፈ', 'ፉ', 'ፊ', 'ፋ', 'ፌ', 'ፍ', 'ፎ', 'ፏ'),
        listOf('ፐ', 'ፑ', 'ፒ', 'ፓ', 'ፔ', 'ፕ', 'ፖ', 'ፗ')
    )
    return families.find { char in it } ?: listOf(char)
}

@Composable
fun GeezAlphabetFilterBar(
    selectedLetter: String,
    onLetterSelected: (String) -> Unit
) {
    val geezLetters = listOf(
        "ሁሉም", "ሀ", "ለ", "ሐ", "መ", "ሠ", "ረ", "ሰ", "ሸ", "ቀ", 
        "በ", "ተ", "ቸ", "ነ", "ኘ", "አ", "ከ", "ወ", "ዐ", "ዘ", 
        "ዠ", "የ", "ደ", "ጀ", "ገ", "ጠ", "ጨ", "ጰ", "ጸ", "ፀ", 
        "ፈ", "ፐ"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = AmharicYellow,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "በአማርኛ ግዕዝ ፊደላት ፈልግ (Ge'ez Alphabet Search):",
                color = Color.LightGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            geezLetters.forEach { letter ->
                val isSelected = letter == selectedLetter
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) AmharicYellow else Color.White.copy(alpha = 0.05f),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) AmharicYellow else Color.Gray.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onLetterSelected(letter) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter,
                        color = if (isSelected) Color.Black else Color.White,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ShareInstallDialog(
    onDismiss: () -> Unit,
    context: android.content.Context
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, AmharicYellow.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon Header
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(AmharicYellow.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = AmharicYellow,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "መተግበሪያውን በቀጥታ ስልክ ላይ መጫኛ መመሪያ",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "ይህንን መተግበሪያ በስልክዎ ላይ በቀጥታ እንደማንኛውም app ለመጫን እና ለሌሎች መሃንዲሶች ለማጋራት የሚከተሉትን ቀላል ደረጃዎች ይከተሉ፡",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))

                // Steps
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    InstallStepRow(
                        stepNumber = "1",
                        title = "APK ፋይል ማዘጋጀት",
                        description = "ከላይኛው ቀኝ የቅንጅቶች (Settings) ምልክት በመጫን 'Generate APK' የሚለውን ይጫኑ። መድረኩ መተግበሪያውን ሰብስቦ የ .apk ፋይል ያዘጋጅልዎታል።"
                    )

                    InstallStepRow(
                        stepNumber = "2",
                        title = "ፋይሉን ማውረድ",
                        description = "ግንባታው ሲጠናቀቅ የሚመጣውን 'Download APK' ሊንክ በመጫን በቀጥታ ስልክዎ ላይ ያውርዱት።"
                    )

                    InstallStepRow(
                        stepNumber = "3",
                        title = "ለሌሎች ማጋራት (Share)",
                        description = "የወረደውን የ .apk ፋይል በቴሌግራም (Telegram)፣ በሼርኢት (ShareIt) ወይም በብሉቱዝ ለባልደረባዎችዎ በሴኮንዶች ውስጥ መላክ ይችላሉ።"
                    )

                    InstallStepRow(
                        stepNumber = "4",
                        title = "ስልክ ላይ መጫን (Install)",
                        description = "በስልክዎ የወረደውን ፋይል ሲከፍቱ 'Unknown Sources' ፈቃድ በመስጠት መተግበሪያውን ሙሉ በሙሉ ይጫኑት። ከመስመር ውጭ (Offline) ይሰራል!"
                    )
                }

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Construction BYA")
                                putExtra(Intent.EXTRA_TEXT, """
                                    Construction BYA - የኮንስትራክሽን ግንባታዎች መረብ መተግበሪያ
                                    
                                    በስልክዎ ላይ በቀጥታ ለመጫን ይህንን ሊንክ ይክፈቱ፡
                                    https://ais-pre-gkbshauubl4rx4uq45c3no-180667634259.europe-west1.run.app
                                """.trimIndent())
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "ለማጋራት ይምረጡ"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ሊንክ አጋራ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, Color.Gray),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Text("ዝጋ", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun InstallStepRow(
    stepNumber: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(AmharicYellow, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                color = SteelGray,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = Color.LightGray,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
