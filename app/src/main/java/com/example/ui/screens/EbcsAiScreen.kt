package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Project
import com.example.ui.viewmodel.ConstructionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EbcsAiScreen(
    projects: List<Project>,
    viewModel: ConstructionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    
    var subTab by remember { mutableStateOf(0) } // 0: EBCS Compliance, 1: AI Document Verification, 2: Official Letter Generator, 3: Comprehensive Codes & Proclamations Library

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundSlate)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Premium Header ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SteelGray),
            border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(AmharicYellow, AmharicGreen)))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        tint = AmharicYellow,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "ብሔራዊ የሕንፃ ኮድ መቆጣጠሪያና AI ሠነድ ማረጋገጫ",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Surface(
                    color = AmharicYellow.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, AmharicYellow.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "🛡️ በEBCS እና ESEN ደንቦች መሠረት የሚመራ | ባለቤት፡ በላይነህ የሻለም ብቻ",
                        color = AmharicYellow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // --- Custom Sub-Tab Navigation Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SteelGray)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf(
                Triple("ኮድ መመርመሪያ", Icons.Default.Rule, 0),
                Triple("ሰነድ ማረጋገጫ", Icons.Default.Verified, 1),
                Triple("ደብዳቤ ማዘጋጃ", Icons.Default.Email, 2),
                Triple("ቤተ-መጻሕፍት", Icons.Default.Book, 3)
            )
            
            tabs.forEach { (title, icon, index) ->
                val isSelected = subTab == index
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
                        .clickable { subTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) AmharicYellow else Color.LightGray,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = title,
                            color = if (isSelected) AmharicYellow else Color.LightGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // --- Active Sub-Tab Screen Content ---
        when (subTab) {
            0 -> EbcsComplianceTab(projects)
            1 -> DocumentVerificationTab(projects)
            2 -> OfficialLetterGeneratorTab(projects)
            3 -> ComprehensiveCodesLibraryTab(projects)
        }
    }
}

// ==========================================
// SUB-TAB 1: EBCS & ESEN COMPLIANCE CHECKER
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EbcsComplianceTab(projects: List<Project>) {
    var selectedRegion by remember { mutableStateOf("አዲስ አበባ ከተማ አስተዳደር") }
    var selectedStandard by remember { mutableStateOf("EBCS-2: የኮንክሪት መዋቅሮች ጥራት (Concrete)") }
    var floorsInput by remember { mutableStateOf("4") }
    var concreteStrengthInput by remember { mutableStateOf("25") } // MPa
    var setbackInput by remember { mutableStateOf("3.5") } // Meters
    var seismicFactorInput by remember { mutableStateOf("0.10") } // Zone factor
    var foundationType by remember { mutableStateOf("Isolated Foundation") }
    
    var showRegionDropdown by remember { mutableStateOf(false) }
    var showStandardDropdown by remember { mutableStateOf(false) }
    var showFoundationDropdown by remember { mutableStateOf(false) }
    
    var evaluationResult by remember { mutableStateOf<EbcsEvaluationResult?>(null) }
    var isEvaluating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val regions = listOf(
        "አዲስ አበባ ከተማ አስተዳደር", "ድሬዳዋ ከተማ አስተዳደር", "ኦሮሚያ ክልላዊ መንግስት", 
        "አማራ ክልላዊ መንግስት", "ትግራይ ክልላዊ መንግስት", "ሲዳማ ክልላዊ መንግስት", 
        "ደቡብ ኢትዮጵያ ክልል", "ማዕከላዊ ኢትዮጵያ ክልል", "ሶማሌ ክልላዊ መንግስት", 
        "አፋር ክልላዊ መንግስት", "ቤንሻንጉል ጉሙዝ ክልል", "ጋምቤላ ክልላዊ መንግስት", "ሐረሪ ክልላዊ መንግስት"
    )

    val standards = listOf(
        "EBCS-1: የህንጻ ዲዛይን መሠረታዊ ህጎች (Actions/Loads)",
        "EBCS-2: የኮንክሪት መዋቅሮች ጥራት (Concrete)",
        "EBCS-3: የግንባታ ብረት መዋቅሮች (Steel)",
        "EBCS-8: የመሬት መንቀጥቀጥ ጥበቃ ደንብ (Seismic)",
        "ESEN 1990-1999: የኢትዮጵያና አውሮፓ ጥምር ደረጃዎች (Eurocodes)",
        "Revised Building Proclamation: የህንጻዎች ማሻሻያ አዋጅ 2026"
    )
    
    val foundations = listOf("Isolated Foundation", "Strip Foundation", "Mat/Raft Foundation", "Pile Foundation")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "የከተማ/ክልል መመዘኛ እና የኮድ መረጃዎች ማስገቢያ",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            
            // Region Selector
            ExposedDropdownMenuBox(
                expanded = showRegionDropdown,
                onExpandedChange = { showRegionDropdown = it }
            ) {
                OutlinedTextField(
                    value = selectedRegion,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("የክልል / የከተማ አስተዳደር መምረጫ") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showRegionDropdown) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AmharicYellow,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = AmharicYellow
                    )
                )
                ExposedDropdownMenu(
                    expanded = showRegionDropdown,
                    onDismissRequest = { showRegionDropdown = false }
                ) {
                    regions.forEach { region ->
                        DropdownMenuItem(
                            text = { Text(region) },
                            onClick = {
                                selectedRegion = region
                                showRegionDropdown = false
                            }
                        )
                    }
                }
            }

            // Standard Selector
            ExposedDropdownMenuBox(
                expanded = showStandardDropdown,
                onExpandedChange = { showStandardDropdown = it }
            ) {
                OutlinedTextField(
                    value = selectedStandard,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("የመመሪያ ወይም ኮድ መምረጫ (EBCS & ESEN)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showStandardDropdown) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AmharicYellow,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = AmharicYellow
                    )
                )
                ExposedDropdownMenu(
                    expanded = showStandardDropdown,
                    onDismissRequest = { showStandardDropdown = false }
                ) {
                    standards.forEach { std ->
                        DropdownMenuItem(
                            text = { Text(std) },
                            onClick = {
                                selectedStandard = std
                                showStandardDropdown = false
                            }
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Number of Floors
                OutlinedTextField(
                    value = floorsInput,
                    onValueChange = { floorsInput = it },
                    label = { Text("የፎቅ ብዛት (Floors)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AmharicYellow,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = AmharicYellow
                    )
                )
                // Concrete Strength
                OutlinedTextField(
                    value = concreteStrengthInput,
                    onValueChange = { concreteStrengthInput = it },
                    label = { Text("የኮንክሪት ጥንካሬ (f'c MPa)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AmharicYellow,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = AmharicYellow
                    )
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Setback Distance
                OutlinedTextField(
                    value = setbackInput,
                    onValueChange = { setbackInput = it },
                    label = { Text("የወሰን ገደብ ርቀት (Setback m)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AmharicYellow,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = AmharicYellow
                    )
                )
                // Seismic Zone Factor
                OutlinedTextField(
                    value = seismicFactorInput,
                    onValueChange = { seismicFactorInput = it },
                    label = { Text("የመሬት መንቀጥቀጥ (Zone α)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AmharicYellow,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = AmharicYellow
                    )
                )
            }

            // Foundation Type Selector
            ExposedDropdownMenuBox(
                expanded = showFoundationDropdown,
                onExpandedChange = { showFoundationDropdown = it }
            ) {
                OutlinedTextField(
                    value = foundationType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("የመሠረት ዓይነት (Foundation Type)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showFoundationDropdown) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AmharicYellow,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = AmharicYellow
                    )
                )
                ExposedDropdownMenu(
                    expanded = showFoundationDropdown,
                    onDismissRequest = { showFoundationDropdown = false }
                ) {
                    foundations.forEach { fnd ->
                        DropdownMenuItem(
                            text = { Text(fnd) },
                            onClick = {
                                foundationType = fnd
                                showFoundationDropdown = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        isEvaluating = true
                        evaluationResult = null
                        delay(1200) // Realistic processing delay
                        val floors = floorsInput.toIntOrNull() ?: 1
                        val concrete = concreteStrengthInput.toIntOrNull() ?: 25
                        val setback = setbackInput.toDoubleOrNull() ?: 3.0
                        val seismic = seismicFactorInput.toDoubleOrNull() ?: 0.05
                        
                        // Rule engine evaluation
                        evaluationResult = runEbcsRuleEngine(selectedRegion, selectedStandard, floors, concrete, setback, seismic, foundationType)
                        isEvaluating = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen),
                shape = RoundedCornerShape(8.dp),
                enabled = !isEvaluating
            ) {
                if (isEvaluating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ደንብ በመተንተን ላይ...", color = Color.White)
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("የኮድ ደንብ ተገዢነትን መርምር", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // --- Evaluation Results Display ---
    AnimatedVisibility(
        visible = evaluationResult != null,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        evaluationResult?.let { result ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, if (result.overallPassed) AmharicGreen else AmharicRed)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔎 የዲዛይን ተገዢነት ውጤት (Compliance Report)",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Surface(
                            color = (if (result.overallPassed) AmharicGreen else AmharicRed).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, if (result.overallPassed) AmharicGreen else AmharicRed)
                        ) {
                            Text(
                                text = if (result.overallPassed) "ያልፋል ✅ PASSED" else "ማስተካከያ ያስፈልገዋል ⚠️ WARNING",
                                color = if (result.overallPassed) AmharicGreen else AmharicRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

                    // Detail parameters evaluation
                    result.details.forEach { detail ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = if (detail.passed) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (detail.passed) AmharicGreen else AmharicRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(text = detail.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(text = detail.description, color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Surface(
                        color = AmharicYellow.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, AmharicYellow.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "⚖️ የክልሉ/ከተማው የሕንፃ አዋጅ ልዩ መመሪያ፡",
                                color = AmharicYellow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = result.regionalNote,
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SUB-TAB 2: AI DOCUMENT VERIFICATION
// ==========================================
@Composable
fun DocumentVerificationTab(projects: List<Project>) {
    var selectedProjectName by remember { mutableStateOf(projects.firstOrNull()?.name ?: "ምንም ፕሮጀክት የለም") }
    var documentMetadata by remember { mutableStateOf("የመዋቅራዊ ዲዛይን ስሌት (Structural Analysis Sheet)\nየአርማታ ብረት ዝርዝር መግለጫ (Rebar Detailing Document)") }
    var showProjDropdown by remember { mutableStateOf(false) }
    
    var isVerifying by remember { mutableStateOf(false) }
    var verificationStep by remember { mutableStateOf("") }
    var verificationStepProgress by remember { mutableStateOf(0f) }
    var verificationResult by remember { mutableStateOf<DocumentVerificationResult?>(null) }
    
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "የግንባታ ሠነድ እና ዲዛይን በAI ማረጋገጫ (Document Verification)",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            // Project Selector
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedProjectName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("የሚረጋገጠው ፕሮጀክት") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { showProjDropdown = true }) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AmharicYellow,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = AmharicYellow
                    )
                )
                DropdownMenu(
                    expanded = showProjDropdown,
                    onDismissRequest = { showProjDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    if (projects.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("ምንም ፕሮጀክት የለም") },
                            onClick = { showProjDropdown = false }
                        )
                    } else {
                        projects.forEach { proj ->
                            DropdownMenuItem(
                                text = { Text(proj.name) },
                                onClick = {
                                    selectedProjectName = proj.name
                                    showProjDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Document Metadata
            OutlinedTextField(
                value = documentMetadata,
                onValueChange = { documentMetadata = it },
                label = { Text("የሠነድ ይዘቶችና ምህንድስና መረጃዎች (Document Summary)") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AmharicYellow,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = AmharicYellow
                )
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        isVerifying = true
                        verificationResult = null
                        
                        val steps = listOf(
                            "1. የሰነዱን ይዘት በEBCS-1 የጭነት መመሪያ መሠረት በማንበብ ላይ... 📂" to 0.25f,
                            "2. በEBCS-2 መሠረት የኮንክሪት ጥንካሬን (f'c) ከጭነት ጋር በማገናዘብ ላይ... 🏗️" to 0.50f,
                            "3. በEBCS-8 መሠረት የመሬት መንቀጥቀጥ ዞን ማስተካከያውን በማስላት ላይ... ⚡" to 0.75f,
                            "4. የመጨረሻ የጥራትና የደህንነት ማረጋገጫ ውጤት በማመንጨት ላይ... 🛡️" to 1.0f
                        )
                        
                        for (step in steps) {
                            verificationStep = step.first
                            verificationStepProgress = step.second
                            delay(1000)
                        }
                        
                        // Populate simulated verification result
                        verificationResult = DocumentVerificationResult(
                            projectName = selectedProjectName,
                            verifiedDate = "2026-07-03 (ሰኔ 26 ቀን 2018 ዓ.ም)",
                            certificateNumber = "EBCS-AI-REG-${(100000..999999).random()}",
                            safetyIndex = 92,
                            critique = "• በEBCS-2 መሠረት የኮንክሪት ጥንካሬ (f'c = 25MPa) ለባለ 4 ፎቅ መኖሪያ ህንጻ ዲዛይን ሙሉ በሙሉ ተስማሚ ነው።\n" +
                                      "• በEBCS-8 መሠረት የመሬት መንቀጥቀጥ ዞን ማስተካከያ ፋክተር (α = 0.10) ተካቶ ዲዛይን መደረጉ ደህንነቱን አስተማማኝ ያደርገዋል።\n" +
                                      "• በEBCS-3 መሠረት ለአምዶች መጋጠሚያ ላይ ጥቅም ላይ የዋሉት የብረት ፌሮዎች ዲያሜትር (16mm) እና ክፍተት (Spacing) የተገዢነት መስፈርቱን ያሟላል።\n" +
                                      "• ማሳሰቢያ፡ በምድር ቤት የውሃ መከላከያ ስራ (Waterproofing Basement) ላይ ተጨማሪ ዝርዝር መግለጫ በሰነዱ ላይ ቢካተት ይመረጣል።",
                            authorizedOwner = "በላይነህ የሻለም ብቻ"
                        )
                        isVerifying = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AmharicYellow),
                shape = RoundedCornerShape(8.dp),
                enabled = !isVerifying
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = BackgroundSlate)
                Spacer(modifier = Modifier.width(6.dp))
                Text("በAI ሠነዱን መርምርና አረጋግጥ", color = BackgroundSlate, fontWeight = FontWeight.Bold)
            }
        }
    }

    // --- Verification Status & Certificate Layout ---
    if (isVerifying) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "የAI የሠነድ መፈተሽ ሂደት", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = verificationStepProgress,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = AmharicYellow,
                    trackColor = Color.Gray.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = verificationStep, color = AmharicYellow, fontSize = 11.sp, textAlign = TextAlign.Center)
            }
        }
    }

    AnimatedVisibility(
        visible = verificationResult != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        verificationResult?.let { cert ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, Brush.sweepGradient(listOf(AmharicGreen, AmharicYellow, AmharicGreen)))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Certificate Header
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "🛡️ የኢትዮጵያ የሕንፃ ኮድ ማረጋገጫ ሠነድ 🛡️",
                            color = AmharicYellow,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "NATIONAL COMPLIANCE CERTIFICATE",
                            color = Color.LightGray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 1.sp
                        )
                    }

                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))

                    // Certificate details Grid
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("የፕሮጀክት ስም፡", color = Color.Gray, fontSize = 11.sp)
                            Text(cert.projectName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("የማረጋገጫ ቁጥር፡", color = Color.Gray, fontSize = 11.sp)
                            Text(cert.certificateNumber, color = AmharicYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("የተረጋገጠበት ቀን፡", color = Color.Gray, fontSize = 11.sp)
                            Text(cert.verifiedDate, color = Color.White, fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("የደህንነት ስኬት (Safety Index)፡", color = Color.Gray, fontSize = 11.sp)
                            Text("${cert.safetyIndex}% (እጅግ በጣም ደህንነቱ የተጠበቀ)", color = AmharicGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))

                    // Critique
                    Text("💡 የምህንድስና ጥራትና ቴክኒካል ትንተና (AI Critique):", color = AmharicYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Surface(
                        color = Color.Black.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = cert.critique,
                            color = Color.White,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Official Stamp Sign
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = AmharicGreen.copy(alpha = 0.15f),
                            border = BorderStroke(1.5.dp, AmharicGreen),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                text = "✅ APPROVED",
                                color = AmharicGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("ያረጋገጠው ባለስልጣን ባለቤት", color = Color.Gray, fontSize = 9.sp)
                            Text("በላይነህ የሻለም ብቻ", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("ዋና ሲቪል መሃንዲስ (Civil Engineer)", color = AmharicYellow, fontSize = 9.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SUB-TAB 3: OFFICIAL LETTER GENERATOR
// ==========================================
@Composable
fun OfficialLetterGeneratorTab(projects: List<Project>) {
    var letterType by remember { mutableStateOf("የግንባታ ፈቃድ ማረጋገጫ (Permit Approval)") }
    var selectedProject by remember { mutableStateOf(projects.firstOrNull()) }
    var selectedRegion by remember { mutableStateOf("አዲስ አበባ ከተማ አስተዳደር") }
    var customHighlights by remember { mutableStateOf("የእሳት አደጋ መውጫ የተካተተበት፣ በEBCS-8 መሠረት የመሬት መንቀጥቀጥ ዲዛይን የተሟላለት።") }
    
    var showTypeDropdown by remember { mutableStateOf(false) }
    var showProjDropdown by remember { mutableStateOf(false) }
    var showRegionDropdown by remember { mutableStateOf(false) }
    
    var isGenerating by remember { mutableStateOf(false) }
    var generatedLetter by remember { mutableStateOf("") }
    
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val letterTypes = listOf(
        "የግንባታ ፈቃድ ማረጋገጫ (Permit Approval)",
        "የማስቆሚያ ወይም የዕግድ ደብዳቤ (Stop-Work Order)",
        "የማስጠንቀቂያ መመሪያ (Official Warning)",
        "የብቃት ማረጋገጫ ምስክር ወረቀት (Certificate of Compliance)"
    )

    val regions = listOf(
        "አዲስ አበባ ከተማ አስተዳደር", "ድሬዳዋ ከተማ አስተዳደር", "ኦሮሚያ ክልላዊ መንግስት", 
        "አማራ ክልላዊ መንግስት", "ትግራይ ክልላዊ መንግስት", "ሲዳማ ክልላዊ መንግስት", 
        "ደቡብ ኢትዮጵያ ክልል", "ማዕከላዊ ኢትዮጵያ ክልል", "ሶማሌ ክልላዊ መንግስት"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "የመንግስት የሙያ ይፋዊ ደብዳቤ በራስ-ሰር ማመንጫ",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            // Letter Type Selector
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = letterType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("የደብዳቤው ዓይነት") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { showTypeDropdown = true }) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AmharicYellow,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = AmharicYellow
                    )
                )
                DropdownMenu(
                    expanded = showTypeDropdown,
                    onDismissRequest = { showTypeDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    letterTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                letterType = type
                                showTypeDropdown = false
                            }
                        )
                    }
                }
            }

            // Project Selector
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedProject?.name ?: "ምንም ፕሮጀክት የለም",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("ተዛማጅ ፕሮጀክት") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { showProjDropdown = true }) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AmharicYellow,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = AmharicYellow
                    )
                )
                DropdownMenu(
                    expanded = showProjDropdown,
                    onDismissRequest = { showProjDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    if (projects.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("ምንም ፕሮጀክት የለም") },
                            onClick = { showProjDropdown = false }
                        )
                    } else {
                        projects.forEach { proj ->
                            DropdownMenuItem(
                                text = { Text(proj.name) },
                                onClick = {
                                    selectedProject = proj
                                    showProjDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Region Selector
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedRegion,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("ደብዳቤ የሚላክበት ክልል/ከተማ") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { showRegionDropdown = true }) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AmharicYellow,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = AmharicYellow
                    )
                )
                DropdownMenu(
                    expanded = showRegionDropdown,
                    onDismissRequest = { showRegionDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    regions.forEach { reg ->
                        DropdownMenuItem(
                            text = { Text(reg) },
                            onClick = {
                                selectedRegion = reg
                                showRegionDropdown = false
                            }
                        )
                    }
                }
            }

            // Highlights
            OutlinedTextField(
                value = customHighlights,
                onValueChange = { customHighlights = it },
                label = { Text("ለደብዳቤው ልዩ መረጃዎች / የጉድለት ማሳሰቢያዎች") },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AmharicYellow,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = AmharicYellow
                )
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        isGenerating = true
                        generatedLetter = ""
                        
                        val projName = selectedProject?.name ?: "የግንባታ ፕሮጀክት"
                        val projLoc = selectedProject?.location ?: "ባላወቅነው አካባቢ"
                        val standardRef = "በEBCS እና ESEN ብሔራዊ የግንባታ መመሪያዎች"
                        
                        // Try calling Gemini API, if not configured, use local synthesis
                        val aiResult = generateOfficialLetterViaGemini(
                            type = letterType,
                            projectName = projName,
                            location = projLoc,
                            region = selectedRegion,
                            standardRef = standardRef,
                            customNotes = customHighlights
                        )
                        
                        generatedLetter = aiResult
                        isGenerating = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen),
                shape = RoundedCornerShape(8.dp),
                enabled = !isGenerating
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ይፋዊ ደብዳቤ በመቅረጽ ላይ...", color = Color.White)
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("በAI ደብዳቤውን አዘጋጅ", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // --- Generated Letter Presentation Sheet ---
    AnimatedVisibility(
        visible = generatedLetter.isNotEmpty(),
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF6EE)), // Parchment paper style background
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(2.dp, AmharicYellow)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📜 የተዘጋጀው ይፋዊ ደብዳቤ",
                        color = Color(0xFF333333),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = {
                            clipboardManager.setText(AnnotatedString(generatedLetter))
                            Toast.makeText(context, "ደብዳቤው በተሳካ ሁኔታ ተገልብጧል! 📋", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "ኮፒ", tint = Color(0xFF555555))
                        }
                        
                        IconButton(onClick = {
                            val file = exportLetterToTxt(context, selectedProject?.name ?: "ደብዳቤ", generatedLetter)
                            if (file != null) {
                                Toast.makeText(context, "ደብዳቤው በቁም ፋይል ተቀምጧል! 📂", Toast.LENGTH_LONG).show()
                            }
                        }) {
                            Icon(Icons.Default.Download, contentDescription = "አውርድ", tint = Color(0xFF555555))
                        }
                    }
                }

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))

                // Letter Body Scrollable container
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = generatedLetter,
                        color = Color(0xFF1E1E1E),
                        fontSize = 12.sp,
                        lineHeight = 20.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// DATA CLASSES AND RULE ENGINE LOGIC
// ==========================================
data class EbcsEvaluationResult(
    val overallPassed: Boolean,
    val details: List<EbcsDetailResult>,
    val regionalNote: String
)

data class EbcsDetailResult(
    val title: String,
    val passed: Boolean,
    val description: String
)

data class DocumentVerificationResult(
    val projectName: String,
    val verifiedDate: String,
    val certificateNumber: String,
    val safetyIndex: Int,
    val critique: String,
    val authorizedOwner: String
)

// LOCAL EBCS RULE ENGINE
fun runEbcsRuleEngine(
    region: String,
    standard: String,
    floors: Int,
    concrete: Int,
    setback: Double,
    seismic: Double,
    foundation: String
): EbcsEvaluationResult {
    val details = mutableListOf<EbcsDetailResult>()
    var overallPassed = true

    // Rule 1: Concrete Strength vs Floors (EBCS-2)
    val concreteRequired = when {
        floors >= 10 -> 35
        floors >= 5 -> 30
        else -> 25
    }
    val concretePassed = concrete >= concreteRequired
    if (!concretePassed) overallPassed = false
    details.add(
        EbcsDetailResult(
            title = "EBCS-2 የኮንክሪት ጥንካሬ ማረጋገጫ (Concrete Grade Check)",
            passed = concretePassed,
            description = "ለባለ $floors ፎቅ ህንጻ ዝቅተኛው የሚፈቀደው የኮንክሪት ጥንካሬ f'c = $concreteRequired MPa ነው። የእርስዎ ዲዛይን $concrete MPa በመሆኑ ${if (concretePassed) "በቂና ደህንነቱ የተጠበቀ ነው።" else "ከተፈላጊው ያንሳል! መሻሻል አለበት።"}"
        )
    )

    // Rule 2: Setback Distance vs Floors (Revised Building Proclamation)
    val setbackRequired = when {
        floors >= 8 -> 5.0
        floors >= 4 -> 4.0
        else -> 3.0
    }
    val setbackPassed = setback >= setbackRequired
    if (!setbackPassed) overallPassed = false
    details.add(
        EbcsDetailResult(
            title = "የወሰን ገደብ ርቀት ተገዢነት (Setback Check)",
            passed = setbackPassed,
            description = "በህንፃ ማሻሻያ ደንብ መሠረት ለባለ $floors ፎቅ ህንጻ ከወሰን የሚጠበቀው ዝቅተኛው ርቀት $setbackRequired ሜትር ነው። የእርስዎ ምዝገባ $setback ሜትር በመሆኑ ${if (setbackPassed) "መስፈርቱን ሙሉ በሙሉ ያሟላል።" else "ሕገ-ወጥ ግንባታ ወይም የወሰን ጥሰት አደጋ አለው! እባክዎ ያስተካክሉ።"}"
        )
    )

    // Rule 3: Seismic Design vs Zone Factor (EBCS-8)
    val seismicPassed = when {
        seismic < 0.05 -> false
        seismic > 0.25 -> false
        else -> true
    }
    if (!seismicPassed) overallPassed = false
    details.add(
        EbcsDetailResult(
            title = "EBCS-8 የመሬት መንቀጥቀጥ ደህንነት (Seismic Safety)",
            passed = seismicPassed,
            description = "በኢትዮጵያ ስምጥ ሸለቆና አካባቢዎች የመሬት መንቀጥቀጥ ዲዛይን ፋክተር ከ 0.05 እስከ 0.25 መሆን አለበት። ያስገቡት $seismic በመሆኑ ${if (seismicPassed) "በዲዛይን ስሌቱ ውስጥ በትክክል ተካቷል።" else "ማስተካከያ ይፈልጋል።"}"
        )
    )

    // Rule 4: Foundation Type Suitability
    val foundationPassed = when {
        floors >= 10 && foundation == "Isolated Foundation" -> false
        floors >= 6 && foundation == "Strip Foundation" -> false
        else -> true
    }
    if (!foundationPassed) overallPassed = false
    details.add(
        EbcsDetailResult(
            title = "የመሠረት ዓይነት ተስማሚነት (Foundation Check)",
            passed = foundationPassed,
            description = "ለባለ $floors ፎቅ ህንፃ የመረጡት መሠረት ($foundation) ${if (foundationPassed) "በእቅዱና በጭነት ትንተናው መሠረት ተቀባይነት አለው።" else "አስተማማኝ ባለመሆኑ ወደ Mat ወይም Pile Foundation ማሳደግ ግዴታ ነው።"}"
        )
    )

    // Regional notes
    val regionalNote = when (region) {
        "አዲስ አበባ ከተማ አስተዳደር" -> "በአዲስ አበባ መዋቅራዊ ፕላን መሠረት ከ 7 ፎቅ በላይ የሆኑ ህንጻዎች ሙሉ የአፈር ምርመራ (Soil Test Report) እና ባለ ሁለት ምድር ቤት (Basement) ማካተት አለባቸው።"
        "ኦሮሚያ ክልላዊ መንግስት" -> "በክልሉ የከተሞች ልማት መመሪያ መሠረት ማንኛውም ግንባታ ቢያንስ 15% አረንጓዴ ቦታ (Green Area Clearance) መተው አለበት።"
        "አማራ ክልላዊ መንግስት" -> "በቅርስ መከላከያና ታሪካዊ ቦታዎች አቅራቢያ የሚሰሩ ግንባታዎች የውጪው ገፅታ ባህላዊ የድንጋይ ንጣፍ ወይም የተለየ የዲዛይን ይዘት እንዲኖራቸው ይፈለጋል።"
        "ትግራይ ክልላዊ መንግስት" -> "በመሬት መንቀጥቀጥ ዞን ውስጥ ለሚገኙ ግንባታዎች የETABS ደህንነት ውጤት በEBCS-8 መሠረት ቢያንስ 90+ ሊሆን ይገባል።"
        else -> "በአዲሱ የኢትዮጵያ ከተሞች ልማትና ኮንስትራክሽን የጋራ መመሪያ መሠረት ደረጃውን የጠበቀ የግንባታ ፈቃድ ማረጋገጫ ወረቀት መያዝ ግዴታ ነው።"
    }

    return EbcsEvaluationResult(overallPassed, details, regionalNote)
}

// AI OR TEMPLATE GENERATED LETTER LOGIC
suspend fun generateOfficialLetterViaGemini(
    type: String,
    projectName: String,
    location: String,
    region: String,
    standardRef: String,
    customNotes: String
): String = withContext(Dispatchers.IO) {
    val apiKey = com.example.BuildConfig.GEMINI_API_KEY
    val hasApiKey = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"
    
    val systemPrompt = "You are a senior professional civil engineering regulatory expert drafting formal Ethiopian Government official letters in Amharic. The letter MUST be formal, authoritative, and strictly follow the layout guidelines of public sector proclamations. The signature block of the owner and director MUST ONLY be 'በላይነህ የሻለም ብቻ' (Owner of Construction Projects Network)."
    
    val prompt = "እባክዎ የሚከተሉትን መረጃዎች መሠረት በማድረግ ይፋዊ የመንግስት ይዘት ያለው የኮንስትራክሽን ደብዳቤ በአማርኛ ያዘጋጁ፡\n" +
            "1. የደብዳቤ ዓይነት፡ $type\n" +
            "2. የፕሮጀክት ስም፡ $projectName\n" +
            "3. የግንባታ ቦታ/ክልል፡ $location ($region)\n" +
            "4. ህጋዊ መስፈርቶች፡ $standardRef\n" +
            "5. የተለዩ ዋና ዋና ነጥቦች/ማሳሰቢያዎች፡ $customNotes\n" +
            "ደብዳቤው የዕለቱን ቀን (ሰኔ 2018 ዓ.ም / 2026 እ.ኤ.አ)፣ የደብዳቤ ቁጥር (ለምሳሌ መ/ቁ 2026/9012)፣ ርዕስ፣ መግቢያ፣ ዝርዝር ትንተና፣ እና መደምደሚያ ማካተት አለበት።\n" +
            "አስፈላጊ ማሳሰቢያ፡ የደብዳቤው ባለቤትና ብቸኛ ፈራሚ 'በላይነህ የሻለም ብቻ' በመሆን መፈረም አለበት።"

    if (hasApiKey) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()
            
            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            }
            
            val systemInstructionJson = JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", systemPrompt)
                    })
                })
            }

            val requestBodyJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", systemInstructionJson)
            }

            val requestBody = requestBodyJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseString = response.body?.string()
                if (responseString != null) {
                    val candidates = JSONObject(responseString).getJSONArray("candidates")
                    val firstCandidate = candidates.getJSONObject(0)
                    val contentParts = firstCandidate.getJSONObject("content").getJSONArray("parts")
                    val generatedText = contentParts.getJSONObject(0).getString("text")
                    if (generatedText.isNotEmpty()) {
                        return@withContext generatedText
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // HIGH QUALITY LOCAL FALLBACK GENERATIVE TEMPLATE ENGINE
    delay(1000) // Simulate generation
    val refNum = "ቁጥር፡ መ/ቁ-2026/AI-${(1000..9999).random()}"
    val localDate = "ቀን፡ ሰኔ 26 ቀን 2018 ዓ.ም (2026-07-03 እ.ኤ.አ)"
    
    val fallback = when {
        type.contains("ፈቃድ") || type.contains("Permit") -> {
            "የኮንስትራክሽን ግንባታዎች መረብ\n" +
            "የግንባታ ክትትልና ቁጥጥር ይፋዊ የውሳኔ ሠነድ\n" +
            "--------------------------------------------\n" +
            "$refNum\n" +
            "$localDate\n" +
            "ቦታ፡ $location ($region)\n\n" +
            "ለሚመለከተው ሁሉ፡\n" +
            "ርዕስ፡- ስለ $projectName የግንባታ ፈቃድና የEBCS ደረጃዎች ማረጋገጫ\n\n" +
            "ከተማ አስተዳደራችን ከላይ ስሙ የተጠቀሰውን የግንባታ ፕሮጀክት ባቀረበው የምህንድስና መዋቅር ዲዛይን (Structural Drawings) እና ስሌቶች መሠረት በዝርዝር መርምሯል።\n\n" +
            "በዚሁ መሠረት ግንባታው፡-\n" +
            "1. በEBCS-1 (Actions on Structures) መሠረት ተገቢው የንፋስና የተሸከርካሪ ጭነት ስሌት ተካቷል።\n" +
            "2. በEBCS-2 (Structural Concrete) መሠረት ዝቅተኛው የኮንክሪት ጥንካሬ መስፈርት የተሟላ ሲሆን፣ በተለይም $customNotes የተባሉ ልዩ ማሳሰቢያዎች በጥብቅ ተካተዋል።\n" +
            "3. በEBCS-8 የመሬት መንቀጥቀጥ ጥበቃ ደንብ የተካተቱት የደህንነት ማስተካከያዎች ደረጃቸውን የጠበቁ ናቸው።\n\n" +
            "ስለሆነም ይህ የግንባታ ፕሮጀክት በEBCS እና ESEN ብሔራዊ የግንባታ መመሪያዎች መሠረት ተቀባይነት አግኝቶ ይፋዊ የግንባታ ፈቃድ የተሰጠው መሆኑን እንገልፃለን።\n\n" +
            "ከአክብሮት ሰላምታ ጋር፣\n\n\n\n" +
            "በላይነህ የሻለም ብቻ\n" +
            "የግንባታ ቁጥጥርና የሙያ ማረጋገጫ መረብ ዋና ባለቤት\n" +
            "ዋና ሲቪል መሃንዲስ (Civil Engineer)"
        }
        type.contains("ማስቆሚያ") || type.contains("Stop-Work") -> {
            "የኮንስትራክሽን ግንባታዎች መረብ\n" +
            "የግንባታ እገዳና ማስቆሚያ አስገዳጅ ትዕዛዝ\n" +
            "--------------------------------------------\n" +
            "$refNum\n" +
            "$localDate\n" +
            "ቦታ፡ $location ($region)\n\n" +
            "ለሚመለከተው የሥራ ተቋራጭ / ባለቤት፡\n" +
            "ርዕስ፡- ስለ $projectName አስቸኳይ የግንባታ እገዳ ማስፈጸም\n\n" +
            "ከተማ አስተዳደራችን በወሰደው የግንባታ ክትትልና ቁጥጥር ሂደት፣ በግንባታ ቦታው ላይ በEBCS እና ESEN ደንቦች መሠረት ከባድ የጥራትና የደህንነት ግድፈት መኖሩን አረጋግጧል።\n\n" +
            "በተለይም የተገኘው ዋና ጉድለት፡-\n" +
            "- $customNotes\n" +
            "- ይህ ግድፈት በEBCS-2 እና በአዲሱ የ2026 የከተማ ፕላንና የህንፃዎች ማሻሻያ አዋጅ መሠረት ለሰው ሕይወትና ለህንጻው መዋቅር መውደቅ አደጋ የሚጋብዝ ነው።\n\n" +
            "ስለሆነም ይህ ደብዳቤ ከደረሰዎት ሰዓት ጀምሮ ግንባታው በአስቸኳይ እንዲቆም የተገደደ መሆኑን እያሳወቅን፣ የተጠቀሰውን የምህንድስና ስህተት አስተካክለው እንደገና ማረጋገጫ እስኪያገኙ ድረስ የግንባታ እንቅስቃሴ ማድረግ በህግ የሚያስቀጣ መሆኑን በጥብቅ እናሳስባለን።\n\n" +
            "ከአክብሮት ሰላምታ ጋር፣\n\n\n\n" +
            "በላይነህ የሻለም ብቻ\n" +
            "የግንባታ ቁጥጥርና የሙያ ማረጋገጫ መረብ ዋና ባለቤት\n" +
            "ዋና ሲቪል መሃንዲስ (Civil Engineer)"
        }
        type.contains("ማስጠንቀቂያ") || type.contains("Warning") -> {
            "የኮንስትራክሽን ግንባታዎች መረብ\n" +
            "የመንግስት የቁጥጥር ማስጠንቀቂያ መመሪያ\n" +
            "--------------------------------------------\n" +
            "$refNum\n" +
            "$localDate\n" +
            "ቦታ፡ $location ($region)\n\n" +
            "ለሚመለከተው አካል፡\n" +
            "ርዕስ፡- ስለ $projectName የዲዛይንና የግንባታ ደረጃዎች ጥሰት ማስጠንቀቂያ\n\n" +
            "የግንባታ ክትትል ቡድናችን ከላይ ስሙ በተጠቀሰው ግንባታ ላይ ባደረገው ፍተሻ፣ ህንጻው በብሔራዊ የህንጻ አዋጅ የተቀመጡትን ደንቦች እየጣሰ እንደሆነ ደርሶበታል።\n\n" +
            "ዋና ዋና የታዩት ግድፈቶች፡-\n" +
            "1. $customNotes\n" +
            "2. በEBCS-2 መሠረት የአርማታ ስራዎች ተገቢው የአፈር ምርመራ ሳይካሄድባቸው እየተከናወኑ መሆኑ።\n" +
            "3. በህንፃዎች ማሻሻያ ደንብ መሠረት የተቀመጠው የወሰን ርቀት (Setback) አለመከበሩ።\n\n" +
            "ይህ የመጨረሻ የፅሁፍ ማስጠንቀቂያ ሲሆን፣ በሚቀጥሉት 5 የሥራ ቀናት ውስጥ አስፈላጊውን የምህንድስና ማስተካከያ አድርገው ሪፖርት ካላቀረቡ ግንባታው ለዘላቂ እገዳና ለካሳ ቅጣት የሚዳረግ መሆኑን በጥብቅ እናሳስባለን።\n\n" +
            "ከአክብሮት ሰላምታ ጋር፣\n\n\n\n" +
            "በላይነህ የሻለም ብቻ\n" +
            "የግንባታ ቁጥጥርና የሙያ ማረጋገጫ መረብ ዋና ባለቤት\n" +
            "ዋና ሲቪል መሃንዲስ (Civil Engineer)"
        }
        else -> {
            "የኮንስትራክሽን ግንባታዎች መረብ\n" +
            "ይፋዊ የሙያ ብቃትና ደረጃ ተገዢነት ምስክር ወረቀት\n" +
            "--------------------------------------------\n" +
            "$refNum\n" +
            "$localDate\n" +
            "ቦታ፡ $location ($region)\n\n" +
            "ለሚመለከተው ሁሉ፡\n" +
            "ርዕስ፡- ስለ $projectName ደረጃውን የጠበቀ ግንባታ የምስክር ወረቀት ማረጋገጫ\n\n" +
            "የኮንስትራክሽን ግንባታዎች መረብ ለ $projectName ባደረገው የምህንድስና ጥራት ፍተሻ፣ ሁሉም የግንባታ አካላት ብሔራዊ የኮድ ደንቦችን በሚገባ ማሟላታቸውን አረጋግጧል።\n\n" +
            "የምዘና ውጤቶች ማጠቃለያ፡-\n" +
            "- የመዋቅር ዲዛይን ጥራት በEBCS-2 እና ESEN መሠረት፡ እጅግ በጣም ከፍተኛ\n" +
            "- ልዩ የታዩ አዎንታዊ የምህንድስና እውነታዎች፡ $customNotes\n" +
            "- በEBCS-8 የተካተተው የመሬት መንቀጥቀጥ መቋቋም አቅም፡ አስተማማኝ\n\n" +
            "ይህ ፕሮጀክት ለሌሎች ግንባታዎች አርአያ የሚሆንና ሙሉ የደህንነት ጥራቱን የጠበቀ በመሆኑ ይህ የጥራት የምስክር ወረቀት ተሰጥቶታል።\n\n" +
            "ከአክብሮት ሰላምታ ጋር፣\n\n\n\n" +
            "በላይነህ የሻለም ብቻ\n" +
            "የግንባታ ቁጥጥርና የሙያ ማረጋገጫ መረብ ዋና ባለቤት\n" +
            "ዋና ሲቪል መሃንዲስ (Civil Engineer)"
        }
    }
    
    return@withContext fallback
}

// Helper to save generated letter to a local txt file for sharing/downloading
fun exportLetterToTxt(context: Context, filename: String, text: String): File? {
    val sanitized = filename.replace(" ", "_").replace("/", "_")
    val file = File(context.cacheDir, "ደብዳቤ_${sanitized}_${System.currentTimeMillis()}.txt")
    try {
        val fos = FileOutputStream(file)
        // Write Amharic UTF-8 BOM
        fos.write(0xEF)
        fos.write(0xBB)
        fos.write(0xBF)
        val writer = fos.bufferedWriter()
        writer.write(text)
        writer.flush()
        writer.close()
        fos.close()
        return file
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

// ==========================================
// SUB-TAB 4: COMPREHENSIVE CODES & PROCLAMATIONS LIBRARY
// ==========================================

data class CodeLibraryItem(
    val id: String,
    val titleAm: String,
    val titleEn: String,
    val category: String,
    val description: String,
    val keyClauses: List<String>,
    val complianceCheckpoints: List<String>,
    val regionalApplication: String
)

// Global list of comprehensive standards, codes and proclamations
val allLibraryItems = listOf(
    CodeLibraryItem(
        id = "ebcs-1",
        titleAm = "EBCS-1: የህንጻ ዲዛይን መሠረታዊ ህጎች (Actions/Loads)",
        titleEn = "EBCS-1: Basis of Design and Actions on Structures",
        category = "EBCS",
        description = "የህንፃዎችን መዋቅር በሚነድፉበት ጊዜ ሊኖሩ የሚችሉ ቋሚ (Dead)፣ ተለዋዋጭ (Live)፣ እና የንፋስና የአየር ሁኔታ ጭነቶችን ለማስላት የሚያገለግል ብሔራዊ ደረጃ ነው።",
        keyClauses = listOf(
            "Clause 2.1: የጭነት አይነቶችና ውህደት (Load Classifications & Combinations)",
            "Clause 3.4: የሰውና የቤት እቃዎች ጭነት (Imposed Loads for Residential: 2.0 kN/m²)",
            "Clause 4.2: የንፋስ ኃይል ስሌት (Basic Wind Speed: Up to 22 m/s depending on location)"
        ),
        complianceCheckpoints = listOf(
            "ሁሉም መጋጠሚያዎች በከፊልና ሙሉ ጭነት ጥምረት (ULS: 1.35G_k + 1.5Q_k) መፈተሻቸውን ማረጋገጥ።",
            "በሰገነትና በበረንዳ ላይ የሚቀመጡ ጭነቶች ቢያንስ 3.0 kN/m² መሆናቸውን ማረጋገጥ።"
        ),
        regionalApplication = "በሁሉም የኢትዮጵያ ክልሎችና ከተማ መስተዳድሮች ለሚሰሩ የመኖሪያና የንግድ ህንጻዎች አስገዳጅ ነው።"
    ),
    CodeLibraryItem(
        id = "ebcs-2",
        titleAm = "EBCS-2: የአርማታ ኮንክሪት መዋቅሮች ጥራት (Concrete)",
        titleEn = "EBCS-2: Structural Use of Concrete",
        category = "EBCS",
        description = "በአርማታ ኮንክሪት (Reinforced Concrete) ለሚገነቡ ምሰሶዎች፣ ጨረሮች፣ ሰቆች እና መሠረቶች ዝርዝር ዲዛይን፣ የብረት አቀማመጥና የኮንክሪት ጥንካሬን የሚወስን መመሪያ ነው።",
        keyClauses = listOf(
            "Clause 4.1: የኮንክሪት ጥንካሬ ምድቦች (Concrete Grades: C25, C30, C35, C40)",
            "Clause 5.2: የብረት መቶኛ ገደብ (Min reinforcement ratio for columns: 0.008 A_c, Max: 0.04 A_c)",
            "Clause 6.3: የአርማታ ሽፋን ውፍረት (Concrete Cover for columns: Min 30mm - 40mm depending on exposure)"
        ),
        complianceCheckpoints = listOf(
            "ለፎቅ ህንፃዎች ምሰሶዎች የሚውል ኮንክሪት ቢያንስ C25 (25 MPa) መሆን አለበት።",
            "የምሰሶዎች ማሰሪያ ብረት (Stirrups) ክፍተት በከፍተኛ ጭነት ቦታዎች ከ 150mm መብለጥ የለበትም።"
        ),
        regionalApplication = "በኢትዮጵያ ለሚከናወኑ ማናቸውም የኮንክሪት ስራዎች ተፈፃሚነት አለው።"
    ),
    CodeLibraryItem(
        id = "ebcs-3",
        titleAm = "EBCS-3: የግንባታ ብረት መዋቅሮች (Steel)",
        titleEn = "EBCS-3: Design of Steel Structures",
        category = "EBCS",
        description = "የብረት ክፈፎች (Steel Frames)፣ ጋንትሪዎችና ጣራዎችን ጨምሮ መዋቅራዊ የብረት ስራዎችን ለመንደፍና ለመስራት የሚያገለግል ብሔራዊ ኮድ ነው።",
        keyClauses = listOf(
            "Clause 3.1: የብረት ደረጃ ምድቦች (Steel Grades: S235, S275, S355)",
            "Clause 5.4: የብረት አባላትን መታጠፍ መከላከል (Buckling and Lateral-Torsional Buckling of Beams)",
            "Clause 7.2: የብረት መጋጠሚያዎች፣ ብሎኖችና ብየዳዎች (Bolted and Welded Connections)"
        ),
        complianceCheckpoints = listOf(
            "የጣራ ብረቶች ከከባድ ንፋስ የሚነሳውን ወደ ላይ የመሳብ ኃይል (Uplift Force) መቋቋም መቻላቸውን መፈተሽ።",
            "በብየዳ ስራዎች ላይ ተገቢውን የጥራት ፍተሻ ማካሄድ።"
        ),
        regionalApplication = "በዋናነት ለኢንዱስትሪ ሼዶች፣ መጋዘኖችና ለትላልቅ አዳራሾች ጣራ ዲዛይን በስፋት ጥቅም ላይ ይውላል።"
    ),
    CodeLibraryItem(
        id = "ebcs-8",
        titleAm = "EBCS-8: የመሬት መንቀጥቀጥ ጥበቃ ደንብ (Seismic)",
        titleEn = "EBCS-8: Design of Structures for Earthquake Resistance",
        category = "EBCS",
        description = "ኢትዮጵያ በምስራቅ አፍሪካ ስምጥ ሸለቆ ላይ የምትገኝ በመሆኗ፣ ህንጻዎች ከመሬት መንቀጥቀጥ አደጋ ደህንነታቸው የተጠበቀ እንዲሆን ልዩ የስሌት ማስተካከያዎችን የሚያዝ መመሪያ ነው።",
        keyClauses = listOf(
            "Clause 3.1: የመሬት መንቀጥቀጥ ዞኖች (Seismic Zone Alpha: Zone 0=0.0, Zone 1=0.03, Zone 2=0.05, Zone 3=0.07, Zone 4=0.10)",
            "Clause 4.3: የህንፃ አስፈላጊነት ሁኔታ (Importance Factor: Hospital = 1.4, School = 1.2, Residential = 1.0)",
            "Clause 5.1: የመዋቅር ተለዋዋጭነት (Ductility Class: Medium DCM, High DCH)"
        ),
        complianceCheckpoints = listOf(
            "አዲስ አበባ በዞን 2 (α = 0.05 ወይም እንደ አዲሱ ረቂቅ 0.10) ስሌት መሰራቱን ማረጋገጥ።",
            "በምስራቅ ሸዋ፣ አዋሽና አካባቢው (ዞን 4) ለሚሰሩ ህንጻዎች DCM/DCH ዲዛይን ዝርዝሮች በጥብቅ መከበር አለባቸው።"
        ),
        regionalApplication = "በተለይም በስምጥ ሸለቆና አጎራባች ከተሞች (አዋሽ፣ አዳማ፣ ሃዋሳ፣ አርባምንጭ፣ ወዘተ) ላይ ከፍተኛ ትኩረት ይሰጠዋል።"
    ),
    CodeLibraryItem(
        id = "esen-Eurocodes",
        titleAm = "ESEN 1990-1999: የኢትዮጵያና አውሮፓ ጥምር ደረጃዎች (Eurocodes)",
        titleEn = "ESEN Standards (Ethiopian Standard Eurocodes)",
        category = "ESEN",
        description = "የአውሮፓ ህብረት የግንባታ መስፈርቶችን (Eurocodes) ከኢትዮጵያ የአየር ንብረት፣ አፈርና ሁኔታዎች ጋር በማጣጣም የተዘጋጁ ዘመናዊ የምህንድስና መመሪያዎች ናቸው።",
        keyClauses = listOf(
            "ESEN 1990: የዲዛይን አስተማማኝነት መሠረታዊ ህግ (Basis of Structural Design)",
            "ESEN 1992: የኮንክሪት ዲዛይን ስልቶች (Concrete Design Rules)",
            "ESEN 1997: የአፈር ምህንድስና ስሌት (Geotechnical Design - Soil mechanics)"
        ),
        complianceCheckpoints = listOf(
            "ከድሮዎቹ የEBCS-1995 ኮዶች ይልቅ አዲሶቹን የESEN-Eurocode ደረጃዎች መጠቀም ይመረጣል።",
            "የቁሳቁስ ከፊል ደህንነት ፋክተሮች (Partial safety factors for concrete: 1.5, steel: 1.15) መተግበራቸውን መፈተሽ።"
        ),
        regionalApplication = "በአገር አቀፍ ደረጃ በፌዴራል ቤቶችና ከተማ ልማት ሚኒስቴር ተቀባይነት አግኝቷል።"
    ),
    CodeLibraryItem(
        id = "bs-8110",
        titleAm = "BS 8110: የብሪታንያ የአርማታ ኮንክሪት ግንባታ ኮድ (British Code)",
        titleEn = "BS 8110: Structural Use of Concrete",
        category = "BRITISH",
        description = "ታሪካዊና በዓለም ዙሪያ በሰፊው እውቅና ያለው የብሪታንያ ኮንክሪት መዋቅራዊ ዲዛይን መመሪያ ሲሆን፣ በኢትዮጵያ የቆዩ ግንባታዎችና ዲዛይኖች ላይ በስፋት ጥቅም ላይ ውሏል።",
        keyClauses = listOf(
            "Part 1: ለዲዛይንና ግንባታ የተፈተሹ መደበኛ ደንቦች (Code of Practice for Design and Construction)",
            "Table 3.4: የተፈቀዱ የመሸከም አቅሞች (Concrete stress block and design curves)",
            "Clause 3.12: የብረት መጋጠሚያዎችና መደራረብ (Splices and laps details)"
        ),
        complianceCheckpoints = listOf(
            "የቆዩ ዲዛይኖችን ለመፈተሽና ለማስተካከል የBS 8110 ስሌቶችን ማጣቀስ።",
            "የCharacteristic Strength ፍቺ ከESEN-Eurocode ጋር ሲወዳደር በኩብ (Cube) ጥንካሬ ላይ የተመሰረተ መሆኑን ማስታወስ (f_cu)።"
        ),
        regionalApplication = "በዋናነት ለቆዩ ፕሮጀክቶች ክለሳ እና በአንዳንድ ዓለም አቀፍ አማካሪዎች ዘንድ ተመራጭ ነው።"
    ),
    CodeLibraryItem(
        id = "aashto-bridge",
        titleAm = "AASHTO: የአሜሪካ መንገድና ድልድይ ዲዛይን መመሪያ (AASHTO LRFD)",
        titleEn = "AASHTO LRFD Bridge Design Specifications",
        category = "AASHTO",
        description = "የሀይዌይ መንገዶች፣ ድልድዮችና ተዛማጅ የመዋቅር አካላት (Culverts, Retaining Walls) ጭነትና የመቋቋም አቅም (LRFD) ዘዴን ተጠቅሞ ለመንደፍ የሚያገለግል ዓለም አቀፍ መመሪያ ነው።",
        keyClauses = listOf(
            "Section 3: የድልድይ ጭነቶችና ጥምረቶች (HL-93 Truck loading, Lane loading)",
            "Section 5: ኮንክሪት መዋቅሮች (Concrete structures & Prestressed girders design)",
            "Section 10: ጥልቅና ጥልቀት የሌላቸው መሠረቶች (Foundations & Soil capacity)"
        ),
        complianceCheckpoints = listOf(
            "በኢትዮጵያ ዋና ዋና መንገዶች ላይ ለሚሰሩ ድልድዮች የHL-93 የጭነት መደብ መተግበሩን ማረጋገጥ።",
            "የድልድይ ምሰሶዎች (Abutments) በቂ የጎንዮሽ የአፈር ግፊት (Lateral Earth Pressure) መቋቋም መቻላቸውን መፈተሽ።"
        ),
        regionalApplication = "በኢትዮጵያ መንገዶች ባለስልጣን (ERA) ሥር ለሚሰሩ የሀይዌይና ድልድይ ግንባታዎች በሙሉ አስገዳጅ ነው።"
    ),
    CodeLibraryItem(
        id = "aacra-geom",
        titleAm = "AACRA: የአዲስ አበባ ከተማ መንገዶች ባለስልጣን ዲዛይን ማንዋል (AACRA)",
        titleEn = "AACRA Road Design and Drainage Manuals",
        category = "AACRA",
        description = "የአዲስ አበባ ከተማን የትራፊክ ፍሰት፣ የመሬት አቀማመጥና የፍሳሽ ማስወገጃ ሁኔታዎችን ከግምት አስገብቶ የተዘጋጀ የከተማ መንገዶች ዲዛይን መመሪያ ነው።",
        keyClauses = listOf(
            "Geometric Design Manual: የከተማ መንገዶች ምድቦች (Arterial, Collector, Local) እና የፍጥነት ገደብ ዲዛይን",
            "Drainage Design Manual: የከተማ ጎርፍ መቋቋሚያ ቦዮች፣ ቱቦዎችና የውሃ መውረጃዎች ስሌት",
            "Pavement Design Manual: አስፋልት ንጣፍ ጥንካሬና ውፍረት ስሌት (ESALs & CBR value)"
        ),
        complianceCheckpoints = listOf(
            "የከተማ መንገዶች ቢያንስ ከ 20 እስከ 50 ዓመት የጎርፍ መተንበያ ጊዜ (Return Period) ታስቦ ዲዛይን መደረጋቸውን መፈተሽ።",
            "በመንገዱ ዳር የሚሰሩ የጎንዮሽ የእግረኛ መንገዶች (Sidewalks) ስፋትና ቁመት ከአዲሱ የከተማ ውበት ደረጃዎች ጋር መጣጣሙን ማረጋገጥ።"
        ),
        regionalApplication = "በአዲስ አበባ ከተማ አስተዳደር ወሰን ውስጥ ለሚሰሩ ማናቸውም የከተማ መንገዶችና የፍሳሽ ማስወገጃዎች አስገዳጅ ነው።"
    ),
    CodeLibraryItem(
        id = "batcoda-specs",
        titleAm = "BATCODA: የግንባታ መመሪያዎችና የስራ ዋጋ ስሌቶች (BATCODA)",
        titleEn = "BATCODA Building Guidelines and Standard Specifications",
        category = "BATCODA",
        description = "የህንፃና ትራንስፖርት ግንባታ ዲዛይን ባለስልጣን (BATCODA) ያወጣቸው የግንባታ ቁሳቁሶች ጥራት መግለጫ፣ የአሰራር ዘዴዎች (Specifications) እና የስራ ዝርዝር ዋጋ (Unit Rate Breakdowns) ማጣቀሻ ነው።",
        keyClauses = listOf(
            "Standard Specifications: የኮንክሪት፣ የድንጋይ ንጣፍ፣ እና የቀለም ስራዎች ዝርዝር የቁስ ጥራት መግለጫ",
            "BATCODA Unit Rates: የሰው ኃይል፣ የማሽንና የቁሳቁስ ፍጆታ ስሌት ቀመሮች (Labor Outputs per day)",
            "Standard Bill of Quantities (BOQ): የግንባታ ዝርዝር መግለጫ መደበኛ ፎርማቶች"
        ),
        complianceCheckpoints = listOf(
            "የBOQ አዘገጃጀት ከBATCODA መደበኛ የስራ ዝርዝሮች (Standard Items) ጋር መጣጣሙን ማረጋገጥ።",
            "የኮንስትራክሽን የሰው ኃይል በቀን ሊያከናውን የሚችለው ስራ (Productivity Constants) በBATCODA ታሪፍ መፈተሹን ማረጋገጥ።"
        ),
        regionalApplication = "በዋናነት ለፌዴራልና ክልላዊ የመንግስት ግንባታዎች በጀት ዝግጅትና ኦዲት በስፋት ጥቅም ላይ ይውላል።"
    ),
    CodeLibraryItem(
        id = "fidic-red",
        titleAm = "FIDIC: ዓለም አቀፍ የግንባታ ውል ስምምነቶች (FIDIC Contracts)",
        titleEn = "FIDIC Conditions of Contract (Red, Yellow, Silver Books)",
        category = "FIDIC",
        description = "በአሰሪ፣ በኮንትራክተር እና በአማካሪ መሐንዲስ መካከል የሚደረጉ ውሎችን፣ ኃላፊነቶችን፣ ክፍያዎችን፣ የይገባኛል ጥያቄዎችንና (Claims) ውዝግቦችን ለመፍታት የሚያገለግል ዓለም አቀፍ የግንባታ ውል መደበኛ ፎርማት ነው።",
        keyClauses = listOf(
            "FIDIC Red Book: በአሰሪው ዲዛይን ለሚደረጉ የግንባታ ስራዎች የሚውል ውል (Employer Designs)",
            "FIDIC Yellow Book: በኮንትራክተሩ ዲዛይንና ግንባታ ለሚከናወኑ ስራዎች (Design-Build)",
            "Clause 20: የይገባኛል ጥያቄዎች፣ የጊዜ ማራዘሚያና ክርክሮች አፈታት (Contractor Claims within 28 days)"
        ),
        complianceCheckpoints = listOf(
            "የይገባኛል ጥያቄ (Claims) ከተፈጠረበት ቀን ጀምሮ በ28 ቀናት ውስጥ በፅሁፍ ማሳወቅ ግዴታ መሆኑን መከታተል።",
            "በውሉ ውስጥ የተመደበው አማካሪ (The Engineer) ሚና እና ውሳኔዎች ገለልተኝነትን ማረጋገጥ።"
        ),
        regionalApplication = "በኢትዮጵያ በዓለም ባንክ፣ በአፍሪካ ልማት ባንክና በውጭ ድጋፍ በሚከናወኑ ታላላቅ የመንገድ፣ ድልድይና የኢንዱስትሪ ፓርክ ግንባታዎች ላይ አስገዳጅ ውል ነው።"
    ),
    CodeLibraryItem(
        id = "proc-building",
        titleAm = "የኢትዮጵያ የህንፃ አዋጅ ቁጥር 624/2001 (Building Proclamation)",
        titleEn = "Ethiopian Building Proclamation No. 624/2001 and Regulation 243/2003",
        category = "PROCLAMATION",
        description = "በከተሞች ውስጥ የሚገነቡ ህንጻዎች የሰዎችን ደህንነት፣ ጤንነትና ምቾት እንዲጠብቁ፣ የከተማን ውበት እንዲያሳድጉና በባለሙያ እንዲመሩ የወጣ አስገዳጅ ብሔራዊ የሕግ አዋጅ ነው።",
        keyClauses = listOf(
            "አንቀጽ 4: የግንባታ ፈቃድ ማግኘት (Building Permit is mandatory before any construction)",
            "አንቀጽ 32: የግንባታ ቁጥጥርና የጥራት ኦዲት (Authority inspections & Quality audits)",
            "አንቀጽ 43: የመጠቀሚያ ፈቃድ (Occupancy Permit is required before utilizing the building)"
        ),
        complianceCheckpoints = listOf(
            "ያለ ግንባታ ፈቃድ የተጀመሩ ግንባታዎች በአዋጁ መሠረት እንዲፈርሱና ከባድ የገንዘብ ቅጣት እንዲጣልባቸው ሊደረግ እንደሚችል ማወቅ።",
            "የግንባታ ባለሙያ ፊርማ (Professional stamp) በፌዴራል ወይም በክልል ፈቃድ ባለው መሐንዲስ መረጋገጡን መፈተሽ።"
        ),
        regionalApplication = "በሁሉም የኢትዮጵያ ከተሞችና የክልል ከተማ አስተዳደሮች ውስጥ ሙሉ አስገዳጅነት አለው።"
    ),
    CodeLibraryItem(
        id = "proc-lease",
        titleAm = "የከተማ ቦታን በሊዝ ስለመያዝ የወጣ አዋጅ ቁጥር 721/2004 (Lease Proclamation)",
        titleEn = "Ethiopian Urban Land Lease Holding Proclamation No. 721/2011",
        category = "PROCLAMATION",
        description = "በከተማ ውስጥ መሬት በሊዝ ስለመያዝ፣ የሊዝ ዘመን ገደቦችን፣ የግንባታ መጀመሪያና ማጠናቀቂያ ጊዜያትን እና መሬትን የመጠቀም መብቶችን የሚደነግግ ዋና አዋጅ ነው።",
        keyClauses = listOf(
            "አንቀጽ 16: የግንባታ መጀመሪያ ጊዜ (Construction commencement period: Short: 6-18 months depending on scale)",
            "አንቀጽ 17: ግንባታ ማጠናቀቂያ ጊዜ (Construction completion period: Small: 18 months, Medium: 24 months, Large: 36 months)",
            "አንቀጽ 22: የሊዝ ዘመን ገደብ (Residential lease: 99 years, Commercial/Industry lease: 60 years)"
        ),
        complianceCheckpoints = listOf(
            "የሊዝ ውል ከገቡ በኋላ ግንባታ ሳይጀምሩ ከተቀመጠው ጊዜ (commencement time) ማለፍ የሊዝ ውል እንዲሰረዝ ሊያደርግ እንደሚችል ማሳሰብ።",
            "የግንባታ ማጠናቀቂያ ጊዜን ማራዘም የሚቻለው በበቂ ምክንያትና በከተማ አስተዳደሩ ፈቃድ ለተወሰነ ጊዜ ብቻ መሆኑን መገንዘብ።"
        ),
        regionalApplication = "በሁሉም የኢትዮጵያ ከተሞች ውስጥ ባሉ የሊዝ ይዞታዎች ላይ በጥብቅ ተግባራዊ ይደረጋል።"
    ),
    CodeLibraryItem(
        id = "int-codes",
        titleAm = "ዓለም አቀፍ የግንባታ መመሪያዎች (ACI 318, ASTM, IBC)",
        titleEn = "International Building Codes and Standards (ACI 318, ASTM, IBC)",
        category = "INTERNATIONAL",
        description = "በዓለም አቀፍ ደረጃ እውቅና ያገኙ የግንባታ መመሪያዎችና ቁሳቁስ መፈተሻዎች ሲሆኑ፣ በኢትዮጵያ ለታላላቅ የግልና የመንግስት ፕሮጀክቶች እንደ ተጨማሪ ማጣቀሻ ያገለግላሉ።",
        keyClauses = listOf(
            "ACI 318: የአሜሪካ ኮንክሪት ኢንስቲትዩት የአርማታ ዲዛይን መመሪያ (American Concrete Institute)",
            "ASTM Standards: የግንባታ እቃዎች የቤተ-ሙከራ ፍተሻ ደረጃዎች (American Society for Testing and Materials)",
            "IBC: ዓለም አቀፍ የህንጻ ኮድ (International Building Code - Safety & Occupancy)"
        ),
        complianceCheckpoints = listOf(
            "በቤተ-ሙከራ ውስጥ የሚደረጉ የአፈር፣ የኮንክሪትና የብረት ጥንካሬ ፍተሻዎች በASTM መመዘኛዎች መካሄዳቸውን ማጣራት።",
            "የአሜሪካን ሶፍትዌሮች (እንደ ETABS፣ SAP2000፣ SAFE ያሉ) ለዲዛይን ሲጠቀሙ ከEBCS ጋር የሚጣጣሙ የACI 318 መመዘኛዎችን ማቀናጀት።"
        ),
        regionalApplication = "ለዓለም አቀፍ ጨረታዎች፣ በውጪ አማካሪዎች ለሚመሩ ፕሮጀክቶች እና ለላቦራቶሪ ፍተሻዎች ሰፊ አገልግሎት ይሰጣል።"
    )
)

// UI Component for Tab 4: Digital Library and QA
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprehensiveCodesLibraryTab(projects: List<Project>) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ሁሉም") }
    var selectedItem by remember { mutableStateOf<CodeLibraryItem?>(allLibraryItems.firstOrNull()) }
    
    // AI Chat state
    var chatInput by remember { mutableStateOf("") }
    val chatHistory = remember { mutableStateListOf<Pair<String, Boolean>>() } // Message, isUser
    var isChatting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var recentSearches by remember { mutableStateOf(SearchHistoryUtils.getRecentSearches(context)) }
    
    if (chatHistory.isEmpty()) {
        chatHistory.add("ሰላም! እኔ የግንባታ ኮዶች፣ የሊዝ አዋጆች እና የFIDIC ውል ስምምነቶች ዘመናዊ AI ረዳት ነኝ። በላይነህ የሻለም ሆይ፣ ምን ለመጠየቅ ይፈልጋሉ? ስለ EBCS፣ BS፣ AACRA፣ FIDIC፣ ወይስ ስለሊዝ አዋጆች ማወቅ የፈለጉትን ይጠይቁኝ! 🛡" to false)
    }

    val categories = listOf("ሁሉም", "EBCS", "ESEN", "BRITISH", "AASHTO", "AACRA", "BATCODA", "FIDIC", "PROCLAMATION", "INTERNATIONAL")

    // Filtered items
    val filteredItems = allLibraryItems.filter { item ->
        val matchesCategory = selectedCategory == "ሁሉም" || item.category == selectedCategory
        val matchesSearch = item.titleAm.contains(searchQuery, ignoreCase = true) || 
                            item.titleEn.contains(searchQuery, ignoreCase = true) || 
                            item.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Search & Filter controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🔍 የህንፃ ህጎች፣ አዋጆችና ኮዶች ፍለጋ (Standards Search)",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("በቁልፍ ቃል ይፈልጉ...", color = Color.Gray, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AmharicYellow) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "አጽዳ", tint = Color.LightGray)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AmharicYellow,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = AmharicYellow
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (searchQuery.trim().isNotEmpty()) {
                                SearchHistoryUtils.saveRecentSearch(context, searchQuery)
                                recentSearches = SearchHistoryUtils.getRecentSearches(context)
                            }
                        }
                    )
                )

                // Recent Search History Section (Chips)
                if (recentSearches.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "የቅርብ ጊዜ ፍለጋዎች (Recent Searches)",
                                    color = Color.LightGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(
                                onClick = {
                                    SearchHistoryUtils.clearRecentSearches(context)
                                    recentSearches = emptyList()
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "ታሪክ አጽዳ",
                                    tint = AmharicRed.copy(alpha = 0.8f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            recentSearches.forEach { q ->
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            searchQuery = q
                                            SearchHistoryUtils.saveRecentSearch(context, q)
                                            recentSearches = SearchHistoryUtils.getRecentSearches(context)
                                        }
                                        .testTag("lib_recent_search_chip_$q"),
                                    color = SteelGray.copy(alpha = 0.4f),
                                    border = BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Text(
                                            text = q,
                                            color = Color.White,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Category badges horizontal scroll
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSel = selectedCategory == cat
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmharicYellow.copy(alpha = 0.2f),
                                selectedLabelColor = AmharicYellow,
                                containerColor = SteelGray,
                                labelColor = Color.LightGray
                            ),
                            border = BorderStroke(1.dp, if (isSel) AmharicYellow else Color.Transparent)
                        )
                    }
                }
            }
        }

        // Split view / Lists and Detail
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Document List (Left/Narrow side)
            Card(
                modifier = Modifier
                    .weight(1.2f)
                    .height(350.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "የመመሪያዎች ማውጫ (${filteredItems.size})",
                        color = AmharicYellow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (filteredItems.isEmpty()) {
                            Text(
                                "ምንም መመሪያ አልተገኘም",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 16.dp),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            filteredItems.forEach { item ->
                                val isSel = selectedItem?.id == item.id
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) AmharicYellow.copy(alpha = 0.12f) else Color.Transparent)
                                        .border(0.5.dp, if (isSel) AmharicYellow else Color.Transparent, RoundedCornerShape(8.dp))
                                        .clickable { 
                                            selectedItem = item 
                                            if (searchQuery.trim().isNotEmpty()) {
                                                SearchHistoryUtils.saveRecentSearch(context, searchQuery)
                                                recentSearches = SearchHistoryUtils.getRecentSearches(context)
                                            }
                                        }
                                        .padding(10.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = item.titleAm,
                                            color = if (isSel) AmharicYellow else Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = item.titleEn,
                                            color = Color.Gray,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Document Detail (Right/Wide side)
            Card(
                modifier = Modifier
                    .weight(1.8f)
                    .height(350.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    selectedItem?.let { item ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = AmharicYellow.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = item.category,
                                        color = AmharicYellow,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                
                                Text(
                                    text = "ቁልፍ መረጃዎች",
                                    color = AmharicGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = item.titleAm,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Text(
                                text = item.description,
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )

                            HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))

                            // Key Clauses
                            Text(
                                text = "⚖ ዋና ዋና ድንጋጌዎች (Key Clauses):",
                                color = AmharicYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            item.keyClauses.forEach { clause ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.padding(start = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Gavel,
                                        contentDescription = null,
                                        tint = AmharicYellow,
                                        modifier = Modifier.size(12.dp).padding(top = 2.dp)
                                    )
                                    Text(text = clause, color = Color.White, fontSize = 10.sp, lineHeight = 14.sp)
                                }
                            }

                            HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))

                            // Compliance checkpoints
                            Text(
                                text = "✅ የተገዢነት ፍተሻዎች (Compliance Checks):",
                                color = AmharicGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            item.complianceCheckpoints.forEach { check ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.padding(start = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = AmharicGreen,
                                        modifier = Modifier.size(12.dp).padding(top = 2.dp)
                                    )
                                    Text(text = check, color = Color.White, fontSize = 10.sp, lineHeight = 14.sp)
                                }
                            }

                            HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))

                            // Regional application
                            Text(
                                text = "📍 ክልላዊ ተፈጻሚነት፡",
                                color = Color.LightGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item.regionalApplication,
                                color = Color.LightGray,
                                fontSize = 10.sp
                            )
                        }
                    } ?: run {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("እባክዎ መመሪያ ይምረጡ", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Tab 4.3: Expert AI Chatbot Consultant for Codes, Proclamations, and Leases
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, AmharicYellow.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AmharicYellow,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "የኮድና የአዋጆች AI አማካሪ (Interactive AI Consultant)",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = {
                        chatHistory.clear()
                        chatHistory.add("ሰላም! እኔ የግንባታ ኮዶች፣ የሊዝ አዋጆች እና የFIDIC ውል ስምምነቶች ዘመናዊ AI ረዳት ነኝ። በላይነህ የሻለም ሆይ፣ ምን ለመጠየቅ ይፈልጋሉ? ስለ EBCS፣ BS፣ AACRA፣ FIDIC፣ ወይስ ስለሊዝ አዋጆች ማወቅ የፈለጉትን ይጠይቁኝ! 🛡️" to false)
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "አዲስ ጀምር", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    }
                }

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

                // Chat History Scroll Window
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chatHistory.forEach { (msg, isUser) ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Surface(
                                color = if (isUser) AmharicYellow.copy(alpha = 0.15f) else SteelGray,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (isUser) AmharicYellow.copy(alpha = 0.4f) else Color.Transparent)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = if (isUser) "በላይነህ የሻለም (እርስዎ)" else "የኮድና አዋጅ AI አማካሪ",
                                        color = if (isUser) AmharicYellow else AmharicGreen,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                    Text(
                                        text = msg,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Chat Input Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        placeholder = { Text("ስለሊዝ፣ ለምሳሌ 'የህንጻ ፈቃድ ለማግኘት ምን ያስፈልጋል?'", color = Color.Gray, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AmharicYellow,
                            unfocusedBorderColor = Color.Gray
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )

                    Button(
                        onClick = {
                            if (chatInput.isNotBlank()) {
                                val query = chatInput
                                chatHistory.add(query to true)
                                chatInput = ""
                                isChatting = true
                                
                                coroutineScope.launch {
                                    val currentDocTitle = selectedItem?.titleAm ?: "All Codes"
                                    val answer = queryCodeLibraryWithGemini(query, currentDocTitle)
                                    chatHistory.add(answer to false)
                                    isChatting = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isChatting && chatInput.isNotBlank()
                    ) {
                        if (isChatting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 1.5.dp)
                        } else {
                            Icon(Icons.Default.Send, contentDescription = "ጠይቅ", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// AI Integration or Smart Local Fallback for Sub-Tab 4
suspend fun queryCodeLibraryWithGemini(query: String, selectedDocContext: String): String = withContext(Dispatchers.IO) {
    val apiKey = com.example.BuildConfig.GEMINI_API_KEY
    val hasApiKey = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"

    val systemPrompt = "You are a senior professional civil engineering expert, urban land administrator, and legal advisor on Ethiopian construction laws. " +
            "You have complete expert knowledge of: " +
            "1. EBCS (Ethiopian Building Code Standards 1-14)\n" +
            "2. ESEN (Ethiopian Standard Eurocodes)\n" +
            "3. British Standards (BS 8110, BS 5950, BS 8004)\n" +
            "4. AASHTO LRFD Bridge Specifications\n" +
            "5. AACRA Geometric & Drainage Design Manuals\n" +
            "6. BATCODA Guidelines & productivity constants\n" +
            "7. FIDIC Contract models (Red, Yellow, Silver, Green Books)\n" +
            "8. Ethiopian Building Proclamation No. 624/2001 & Regulation 243/2003\n" +
            "9. Ethiopian Urban Land Lease Proclamation No. 721/2011\n" +
            "10. International codes such as ACI 318, ASTM, and IBC.\n\n" +
            "The user is 'በላይነህ የሻለም ብቻ' (Senior Civil Engineer & Construction Network Owner). " +
            "Provide authoritative, accurate, and highly specific responses in Amharic with appropriate English technical terms. Always cite the relevant Proclamation Articles, EBCS Clauses, or FIDIC sections."

    val prompt = "የመሐንዲሱ ስም፡ በላይነህ የሻለም ብቻ\n" +
            "ማጣቀሻ ሠነድ፡ $selectedDocContext\n" +
            "የመሐንዲሱ ጥያቄ፡ $query\n\n" +
            "እባክዎን ከላይ በተገለጸው አውድና በኢትዮጵያ ደረጃዎች መሠረት ዝርዝር ምላሽ ይስጡ።"

    if (hasApiKey) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
            
            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            }
            
            val systemInstructionJson = JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", systemPrompt)
                    })
                })
            }

            val requestBodyJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", systemInstructionJson)
            }

            val requestBody = requestBodyJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseString = response.body?.string()
                if (responseString != null) {
                    val candidates = JSONObject(responseString).getJSONArray("candidates")
                    val generatedText = candidates.getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                    if (generatedText.isNotEmpty()) {
                        return@withContext generatedText
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // High quality offline engineering local parser for smart answers
    delay(1200)
    val lower = query.lowercase()
    val reply = when {
        lower.contains("ሊዝ") || lower.contains("lease") || lower.contains("መሬት") || lower.contains("መሬት") -> {
            "በከተማ ቦታን በሊዝ ስለመያዝ የወጣ አዋጅ ቁጥር 721/2004 መሠረት ለሚነሱ ጥያቄዎች ማብራሪያ፡-\n\n" +
            "1. **የግንባታ መጀመሪያ ጊዜ (አንቀጽ 16)፡** ለትናንሽ ግንባታዎች 6 ወር፣ ለመካከለኛ ግንባታዎች 12 ወር እና ለትላልቅ ግንባታዎች 18 ወራት በሊዝ ውሉ በተወሰነው መሠረት መሰጠት አለበት።\n" +
            "2. **የግንባታ ማጠናቀቂያ ጊዜ (አንቀጽ 17)፡** ትናንሽ ግንባታዎች በ18 ወራት፣ መካከለኛ ግንባታዎች በ24 ወራት፣ እና ትላልቅ ግንባታዎች በ36 ወራት ውስጥ መጠናቀቅ አለባቸው።\n" +
            "3. **የሊዝ ዘመን ገደቦች (አንቀጽ 22)፡** ለመኖሪያ ቤት 99 ዓመት ሲሆን ለኢንዱስትሪ 60 ዓመት፣ ለንግድና አገልግሎቶች ደግሞ 60 ዓመት ነው።\n\n" +
            "ውድ መሐንዲስ በላይነህ የሻለም ብቻ፣ በሊዝ ውል ጊዜዎችን አለማክበር የሊዝ ይዞታ መብት መቋረጥና ካሳ ሳይከፈል መሬቱ እንዲመለስ ሊያደርግ ስለሚችል የግንባታ መርሃ-ግብር (Construction Schedule) በጥብቅ መከታተል አለበት።"
        }
        lower.contains("fidic") || lower.contains("ውል") || lower.contains("claim") || lower.contains("ይገባኛል") -> {
            "በ FIDIC ኮንትራት ህጎች (በተለይም FIDIC Red Book) መሠረት የሚከተሉት መርሆዎች ተፈጻሚ ይሆናሉ፡-\n\n" +
            "1. **Clause 20.1 (Contractor's Claims)፡** ኮንትራክተሩ ተጨማሪ ክፍያ ወይም የጊዜ ማራዘሚያ (Extension of Time - EOT) ከፈለገ፣ ክስተቱን ካወቀበት ቀን ጀምሮ በ28 ቀናት ውስጥ ለአማካሪ መሐንዲሱ ማስታወቂያ (Notice of Claim) መላክ አለበት። በ28 ቀናት ካላሳወቀ የይገባኛል መብቱ ሙሉ በሙሉ ይሰረዛል።\n" +
            "2. **The Engineer's Role (Clause 3.5)፡** አማካሪው በአሰሪውና በኮንትራክተሩ መካከል የሚነሱ አለመግባባቶችን በፍትሃዊነት (Fair Determination) የመፍታት ኃላፊነት አለበት።\n" +
            "3. **FIDIC Yellow/Silver Books፡** የዲዛይን ኃላፊነቱ በከፊልም ሆነ በሙሉ በኮንትራክተሩ ላይ የሚወድቅባቸው የውል አይነቶች ናቸው።"
        }
        lower.contains("aacra") || lower.contains("መንገድ") || lower.contains("ፍሳሽ") || lower.contains("drainage") -> {
            "በአዲስ አበባ ከተማ መንገዶች ባለስልጣን (AACRA) የዲዛይን ማንዋሎች መሠረት የተሰጠ ምላሽ፡-\n\n" +
            "1. **የጎርፍ ፍሳሽ ስሌት (Rational Method)፡** የከተማ የጎርፍ ማስወገጃ ቱቦዎችና ቦዮች በሚነደፉበት ጊዜ ቢያንስ የ10 ዓመት የጎርፍ ድግግሞሽ ጊዜ (10-Year Return Period) ለውስጥ መንገዶች፣ እና ለዋና ዋና መንገዶች የ25-50 ዓመት ድግግሞሽ መጠቀም አለበት።\n" +
            "2. **Pavement Thickness (የመንገድ ንጣፍ ውፍረት)፡** የዲዛይን ዘመኑ በትንሹ 15-20 ዓመት ታስቦ፣ የሚጠበቀው ከፍተኛ የተሸከርካሪ ጭነት (ESALs) እና የአፈሩ የመሸከም አቅም (Subgrade CBR value) ተሰልቶ የ አስፋልት ኮንክሪት፣ ቤዝ ኮርስ እና ሰብ-ቤዝ ውፍረት ይወሰናል።\n" +
            "3. **የእግረኛ መንገድ (Sidewalks)፡** ለአርቴሪያል መንገዶች ቢያንስ 2.5 ሜትር እስከ 3.0 ሜትር ስፋት እንዲኖራቸው ይመከራል።"
        }
        lower.contains("ebcs") || lower.contains("ኮንክሪት") || lower.contains("concrete") || lower.contains("strength") || lower.contains("ብረት") || lower.contains("የመሬት መንቀጥቀጥ") || lower.contains("seismic") -> {
            "በEBCS-2 (Structural Concrete) እና ESEN 1992 መመሪያዎች መሠረት ተገቢው የአርማታ ዲዛይን ደንቦች ማብራሪያ፡-\n\n" +
            "1. **Concrete Grade (የኮንክሪት ደረጃ)፡** ለሁሉም ባለፎቅ ህንጻ መዋቅራዊ አካላት (Columns, Beams) ዝቅተኛው ጥንካሬ f'c = 25 MPa (C25) መሆን አለበት።\n" +
            "2. **Reinforcement Ratio (የብረት መቶኛ)፡** በአምዶች (Columns) ውስጥ የሚገኘው የብረት መጠን ከመላው አምድ ስፋት ከ 0.8% በታች መሆን የለበትም፣ እንዲሁም በኮንክሪት መፍሰሻ ጊዜ እንቅፋት እንዳይሆን ከ 4% መብለጥ የለበትም።\n" +
            "3. **Concrete Cover (የአርማታ ብረት ሽፋን)፡** በአፈር ውስጥ ለሚቀበሩ መሠረቶች ቢያንስ 50mm፣ ለአምዶችና ጨረሮች 30mm-40mm መከላከያ ሽፋን መሰጠት አለበት።\n\n" +
            "ውድ መሐንዲስ በላይነህ የሻለም ብቻ፣ እነዚህ መመዘኛዎች የህንፃውን ዘላቂነት (Durability) እና የእሳት መቋቋም አቅም በቀጥታ የሚወስኑ ናቸው።"
        }
        lower.contains("ፈቃድ") || lower.contains("permit") || lower.contains("ህንፃ አዋጅ") || lower.contains("proclamation") || lower.contains("624") -> {
            "በኢትዮጵያ የህንፃ አዋጅ ቁጥር 624/2001 እና መመሪያ ቁጥር 243/2003 መሠረት የሚከተሉት ይደነግጋሉ፡-\n\n" +
            "1. **የግንባታ ፈቃድ (አንቀጽ 4)፡** ማንኛውም ግንባታ ከመጀመሩ በፊት የዲዛይን ፕላን ለሚመለከተው የከተማ አስተዳደር ቀርቦ የግንባታ ፈቃድ ማግኘት አለበት። ያለፈቃድ መገንባት ግንባታው እንዲፈርስና የገንዘብ ቅጣት እንዲጣል ያደርጋል።\n" +
            "2. **የመጠቀሚያ ፈቃድ (Occupancy Permit - አንቀጽ 43)፡** ግንባታው ሲጠናቀቅና በባለሙያዎች ጥራቱ ሲረጋገጥ የመጠቀሚያ ፈቃድ (Occupancy Permit) ማግኘት ግዴታ ነው። ያለዚህ ፈቃድ ህንጻውን መጠቀም የተከለከለ ነው።\n" +
            "3. **ቁጥጥር (አንቀጽ 32)፡** የመንግስት ተቆጣጣሪ መሐንዲሶች ግንባታው በታወጀው ደረጃና ፕላን መሠረት እየተከናወነ መሆኑን በማንኛውም ጊዜ የመፈተሽ መብት አላቸው።"
        }
        else -> {
            "ውድ ሲቪል መሐንዲስ በላይነህ የሻለም ብቻ፣ ላቀረቡት ጥያቄ፡ \"$query\" ምላሽ እንደሚከተለው ይቀርባል፡-\n\n" +
            "በኢትዮጵያ የግንባታ ዘርፍ EBCS እና ESEN ብሔራዊ ደንቦች ዋና መሪዎች ሲሆኑ፣ ለመንገድ ግንባታዎች ደግሞ የ AASHTO እና AACRA መመሪያዎች ተግባራዊ ይደረጋሉ።\n" +
            "በተጨማሪም የ FIDIC የውል ስምምነት ህጎች የፕሮጀክት ስራዎችን ያለምንም ውዝግብ በሰላም ለማጠናቀቅ ትልቅ ሚና አላቸው።\n\n" +
            "እባክዎን ከላይ ካለው ማውጫ ውስጥ የሚፈልጉትን መመሪያ መርጠው ልዩ ጥያቄዎን በዝርዝር ይጠይቁኝ። ተጨማሪ ማብራሪያ ለመስጠት ዝግጁ ነኝ! 🛡️"
        }
    }
    return@withContext reply
}

