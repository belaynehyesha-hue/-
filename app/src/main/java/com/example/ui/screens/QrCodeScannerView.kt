@file:kotlin.OptIn(
    com.google.accompanist.permissions.ExperimentalPermissionsApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)

package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.DailyProgress
import com.example.data.Project
import com.example.data.RiskAssessment
import com.google.accompanist.permissions.*
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

// Dark canvas color specifically for scanner backdrop
private val DarkCanvasBackdrop = Color(0xFF1E2022)

// Analyzer implementation using ML Kit Barcode Scanning
class QrCodeAnalyzer(
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue
                        if (rawValue != null) {
                            onQrCodeScanned(rawValue)
                            break
                        }
                    }
                }
                .addOnFailureListener {
                    // Silent fail for frame processing
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}

@SuppressLint("ModifierParameter")
@Composable
fun QrCodeScannerView(
    projects: List<Project>,
    dailyProgress: List<DailyProgress>,
    riskAssessments: List<RiskAssessment>,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Scanner states
    var scannedRawValue by remember { mutableStateOf<String?>(null) }
    var parsedTitle by remember { mutableStateOf("") }
    var parsedType by remember { mutableStateOf("") } // "Project", "ProgressLog", "Risk"
    var matchedProject by remember { mutableStateOf<Project?>(null) }
    var matchedProgressLog by remember { mutableStateOf<DailyProgress?>(null) }
    var matchedRiskAssessment by remember { mutableStateOf<RiskAssessment?>(null) }

    // Simulation tab state
    var showSimulatorOnly by remember { mutableStateOf(false) }

    // Permission state
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    // Laser scanning bar animation
    val scanAnimation = rememberInfiniteTransition(label = "qr_scan")
    val laserOffset by scanAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "qr_laser"
    )

    // Parse the QR code value
    fun parseScannedValue(value: String) {
        scannedRawValue = value
        matchedProject = null
        matchedProgressLog = null
        matchedRiskAssessment = null

        try {
            when {
                // e.g. tilili://project/3
                value.startsWith("tilili://project/") -> {
                    val idStr = value.substringAfter("tilili://project/").trim()
                    val id = idStr.toIntOrNull()
                    matchedProject = projects.find { it.id == id }
                    parsedType = "Project"
                    parsedTitle = matchedProject?.name ?: "የማይታወቅ ግንባታ (ID: $idStr)"
                }
                // e.g. tilili://progress/4
                value.startsWith("tilili://progress/") -> {
                    val idStr = value.substringAfter("tilili://progress/").trim()
                    val id = idStr.toIntOrNull()
                    matchedProgressLog = dailyProgress.find { it.id == id }
                    parsedType = "ProgressLog"
                    parsedTitle = matchedProgressLog?.taskDescription ?: "የማይታወቅ ዕለታዊ መዝግብ (ID: $idStr)"
                    
                    // Match project name as well if possible
                    matchedProgressLog?.let { log ->
                        matchedProject = projects.find { it.id == log.projectId }
                    }
                }
                // e.g. tilili://risk/5
                value.startsWith("tilili://risk/") -> {
                    val idStr = value.substringAfter("tilili://risk/").trim()
                    val id = idStr.toIntOrNull()
                    matchedRiskAssessment = riskAssessments.find { it.id == id }
                    parsedType = "Risk"
                    parsedTitle = matchedRiskAssessment?.hazardTitle ?: "የማይታወቅ የስራ ላይ አደጋ (ID: $idStr)"
                    
                    // Match project name
                    matchedRiskAssessment?.let { risk ->
                        matchedProject = projects.find { it.id == risk.projectId }
                    }
                }
                else -> {
                    parsedType = "Unknown"
                    parsedTitle = "የማይታወቅ የQR ኮድ ምልክት"
                }
            }
        } catch (e: Exception) {
            parsedType = "Unknown"
            parsedTitle = "የQR ኮድ ትንተና ስህተት"
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "የኮንስትራክሽን QR ኮድ መፈለጊያ (ስካነር)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "ሙከራ (Simulation)",
                                fontSize = 11.sp,
                                color = if (showSimulatorOnly) AmharicYellow else Color.LightGray,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Switch(
                                checked = showSimulatorOnly,
                                onCheckedChange = { showSimulatorOnly = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AmharicYellow,
                                    checkedTrackColor = AmharicYellow.copy(alpha = 0.5f)
                                )
                            )
                        }
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "ዝጋ", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = SteelGray)
                )
            },
            containerColor = DarkCanvasBackdrop
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!showSimulatorOnly) {
                        Text(
                            text = "በግንባታ ቦታዎች ላይ የተለጠፉ የ QR ኮዶችን ለመቃኘት ካሜራውን ይጠቀሙ።",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Camera Scan Frame OR Request Permission Card
                        if (cameraPermissionState.status.isGranted) {
                            Box(
                                modifier = Modifier
                                    .size(260.dp)
                                    .border(BorderStroke(3.dp, AmharicYellow), RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                // CameraX Preview Interop
                                AndroidView(
                                    factory = { ctx ->
                                        val previewView = PreviewView(ctx)
                                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                                        cameraProviderFuture.addListener({
                                            val cameraProvider = cameraProviderFuture.get()
                                            val preview = Preview.Builder().build().also {
                                                it.setSurfaceProvider(previewView.surfaceProvider)
                                            }

                                            val imageAnalysis = ImageAnalysis.Builder()
                                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                                .build()

                                            imageAnalysis.setAnalyzer(
                                                cameraExecutor,
                                                QrCodeAnalyzer { qrText ->
                                                    // Scanned successfully!
                                                    parseScannedValue(qrText)
                                                }
                                            )

                                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                                            try {
                                                cameraProvider.unbindAll()
                                                cameraProvider.bindToLifecycle(
                                                    lifecycleOwner,
                                                    cameraSelector,
                                                    preview,
                                                    imageAnalysis
                                                )
                                            } catch (exc: Exception) {
                                                Toast.makeText(ctx, "ካሜራ ለመክፈት አልተቻለም፡ ${exc.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }, ContextCompat.getMainExecutor(ctx))

                                        previewView
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Animated Scanning Laser Line Overlay
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(0.02f)
                                        .align(Alignment.TopCenter)
                                        .offset(y = (260.dp * laserOffset))
                                        .background(AmharicYellow.copy(alpha = 0.8f))
                                )

                                // Frame corner indicators
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.3f),
                                        modifier = Modifier
                                            .size(48.dp)
                                            .align(Alignment.Center)
                                    )
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                                    .testTag("camera_permission_card"),
                                colors = CardDefaults.cardColors(containerColor = CardBackground),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = null,
                                        tint = AmharicYellow,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "የካሜራ መዳረሻ ፈቃድ ያስፈልጋል",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "የQR ኮዶችን በካሜራ ለመቃኘት መተግበሪያው የካሜራ ፈቃድ እንዲያገኝ መፍቀድ አለብዎት።",
                                        color = Color.LightGray,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { cameraPermissionState.launchPermissionRequest() },
                                        colors = ButtonDefaults.buttonColors(containerColor = AmharicYellow),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("ፈቃድ ስጥ / Request Permission", color = SteelGray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // A prominent Testing / Simulation Panel matching runtime constraints
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("qr_simulator_panel"),
                        colors = CardDefaults.cardColors(containerColor = SteelGray.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, AmharicYellow.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LogoDev,
                                    contentDescription = null,
                                    tint = AmharicYellow,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "የQR መለያዎች የሙከራ አስመሳይ (SITE TAG SIMULATOR)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmharicYellow
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "በ emulator ወይም headless container ውስጥ ሲሞክሩ አካላዊ ካሜራ ስለማይኖር የሚከተሉትን የሙከራ QR መለያዎች በመንካት በቀጥታ የQR ስካን ተፅዕኖን ማየት ይችላሉ፡",
                                fontSize = 10.sp,
                                color = Color.LightGray,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // 1. Project tags
                            Text("1. የፕሮጀክቶች QR መለያዎች (Project Tags):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                projects.take(3).forEach { proj ->
                                    Button(
                                        onClick = { parseScannedValue("tilili://project/${proj.id}") },
                                        colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(12.dp), tint = AmharicYellow)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${proj.name.take(15)}...", fontSize = 9.sp, color = Color.White)
                                    }
                                }
                            }

                            // 2. Daily progress tags
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("2. የዕለታዊ መከታተያ መዛግብት QR መለያዎች (Progress Logs):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            if (dailyProgress.isEmpty()) {
                                Text("   ምንም ዕለታዊ መዝግብ የለም / No progress logs yet.", fontSize = 9.sp, color = Color.Gray)
                            } else {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    dailyProgress.take(3).forEach { log ->
                                        Button(
                                            onClick = { parseScannedValue("tilili://progress/${log.id}") },
                                            colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(12.dp), tint = AmharicGreen)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("${log.taskDescription.take(15)}...", fontSize = 9.sp, color = Color.White)
                                        }
                                    }
                                }
                            }

                            // 3. Risk tags
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("3. የደህንነት ስጋቶች QR መለያዎች (Safety Hazard Tags):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            if (riskAssessments.isEmpty()) {
                                Text("   ምንም የተመዘገበ የደህንነት አደጋ የለም / No hazards yet.", fontSize = 9.sp, color = Color.Gray)
                            } else {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    riskAssessments.take(3).forEach { ra ->
                                        Button(
                                            onClick = { parseScannedValue("tilili://risk/${ra.id}") },
                                            colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(12.dp), tint = AmharicRed)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("${ra.hazardTitle.take(15)}...", fontSize = 9.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Scanned results container area
                    Spacer(modifier = Modifier.height(16.dp))
                    AnimatedVisibility(
                        visible = scannedRawValue != null,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("scanned_result_details_card"),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(2.dp, if (parsedType != "Unknown") AmharicGreen else AmharicRed)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val (statusColor, statusIcon) = when (parsedType) {
                                            "Project" -> Pair(ConstructionTeal, Icons.Default.Domain)
                                            "ProgressLog" -> Pair(AmharicGreen, Icons.Default.FactCheck)
                                            "Risk" -> Pair(AmharicRed, Icons.Default.Warning)
                                            else -> Pair(Color.Gray, Icons.Default.Error)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(statusColor.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(16.dp))
                                        }
                                        Text(
                                            text = when (parsedType) {
                                                "Project" -> "የግንባታ ፕሮጀክት መለያ (Project Tag)"
                                                "ProgressLog" -> "የእለት ተገዢነት መከታተያ መዝገብ (Compliance Log)"
                                                "Risk" -> "የስራ ላይ ደህንነት አደጋ (Site Safety Hazard)"
                                                else -> "ያልታወቀ የQR መለያ"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            color = statusColor,
                                            fontSize = 13.sp
                                        )
                                    }
                                    
                                    IconButton(
                                        onClick = {
                                            scannedRawValue = null
                                            matchedProject = null
                                            matchedProgressLog = null
                                            matchedRiskAssessment = null
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "ውጤት አጽዳ", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = parsedTitle,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "መለያ ኮድ (Raw Tag): ${scannedRawValue ?: ""}",
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 2.dp)
                                )

                                Divider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

                                // Render detailed properties based on what was matched
                                when {
                                    matchedProject != null -> {
                                        val p = matchedProject!!
                                        RenderDetailRow("ቦታ (Location):", p.location)
                                        RenderDetailRow("ምድብ (Category):", p.buildingCategory)
                                        RenderDetailRow("ሁኔታ (Status):", p.status)
                                        RenderDetailRow("የእድገት መቶኛ (Progress):", "${p.scheduleProgressPercent}%")
                                        RenderDetailRow("ጥንካሬ ውጤት (ETABS):", p.etabsSafetyScore?.let { "$it/100" } ?: "ያልተሰላ")
                                        if (!p.cadCoordinates.isNullOrBlank()) {
                                            RenderDetailRow("CAD ኮኦርዲኔትስ:", p.cadCoordinates)
                                        }
                                    }
                                    matchedProgressLog != null -> {
                                        val log = matchedProgressLog!!
                                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                        RenderDetailRow("ግንባታ (Project):", matchedProject?.name ?: "ያልታወቀ ፕሮጀክት")
                                        RenderDetailRow("የስራ መጠን (Quantity):", log.workQuantity)
                                        RenderDetailRow("ተገዢነት (Compliance):", log.status)
                                        RenderDetailRow("የተጣቀሰ EBCS (Standard):", log.standardRefId.uppercase())
                                        RenderDetailRow("የተገመገመ ቼክፖይንት (Checkpoint):", log.checkpointPassed)
                                        RenderDetailRow("የተለካ እሴት (Measured Value):", log.measuredValue)
                                        RenderDetailRow("ቀን (Date Logs):", sdf.format(Date(log.date)))
                                        RenderDetailRow("ተቆጣጣሪ (Inspector):", log.inspectorName)
                                        if (log.notes.isNotBlank()) {
                                            RenderDetailRow("ማስታወሻ (Notes):", log.notes)
                                        }
                                    }
                                    matchedRiskAssessment != null -> {
                                        val risk = matchedRiskAssessment!!
                                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                        RenderDetailRow("ግንባታ (Project):", matchedProject?.name ?: "ያልታወቀ ፕሮጀክት")
                                        RenderDetailRow("አደጋ ደረጃ (Risk Level):", risk.riskLevel)
                                        RenderDetailRow("መግለጫ (Description):", risk.description)
                                        RenderDetailRow("የመፍትሄ እቅድ (Mitigation):", risk.mitigationPlan)
                                        RenderDetailRow("የታገደበት EBCS (Standard):", risk.ebcsStandardRefId.uppercase())
                                        RenderDetailRow("የደህንነት አንቀጽ (Clause):", risk.safetyClause)
                                        RenderDetailRow("ሁኔታ (Status):", risk.status)
                                        RenderDetailRow("የተመዘገበበት ቀን:", sdf.format(Date(risk.dateLogged)))
                                        RenderDetailRow("ባለሙያ (Logger):", risk.loggedBy)
                                    }
                                    else -> {
                                        // Tag exists but no matching record found in local Room Database
                                        Text(
                                            text = "ይህ መለያ በቲሊሊ ከተማ ኮንስትራክሽን የውሂብ ጎታ (Database) ውስጥ አልተመዘገበም ወይም ተሰርዟል። እባክዎን ትክክለኛነቱን ያረጋግጡ።",
                                            color = AmharicYellow,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }

                                if (parsedType != "Unknown" && (matchedProject != null || matchedProgressLog != null || matchedRiskAssessment != null)) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = {
                                            Toast.makeText(context, "ይህ መዝገብ በተሳካ ሁኔታ ተረጋግጧል! (Verified via Tilili Site Tag Scanner)", Toast.LENGTH_LONG).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("መዝገቡን አረጋግጥ (Approve/Verify)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
private fun RenderDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.LightGray,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(0.6f),
            textAlign = TextAlign.End
        )
    }
}

// FlowRow standard implementation for earlier SDK APIs compatibility
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}
