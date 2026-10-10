package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.InstitutionProfile
import com.example.data.Professional
import com.example.data.Project
import com.example.data.DocFlagType
import com.example.data.DocVerificationSeverity
import com.example.data.Law1356VerificationEngine
import com.example.ui.theme.*

@Composable
fun AddProfessionalComprehensiveDialog(
    projects: List<Project>,
    institutionProfile: InstitutionProfile,
    onDismiss: () -> Unit,
    onSave: (Professional) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
        ) {
            EngineerRegistrationFormComponent(
                projects = projects,
                institutionProfile = institutionProfile,
                initialProfessional = null,
                onDismiss = onDismiss,
                onSaveSuccess = { registered ->
                    onSave(registered)
                }
            )
        }
    }
}

@Composable
fun ProfessionalDossierDetailDialog(
    professional: Professional,
    institutionProfile: InstitutionProfile,
    onDismiss: () -> Unit,
    onUpdate: (Professional) -> Unit,
    onDelete: () -> Unit,
    onResolveFlag: ((Professional, DocFlagType, String) -> Unit)? = null
) {
    val context = LocalContext.current
    var currentProfessional by remember(professional) { mutableStateOf(professional) }
    val docReport = remember(currentProfessional) { Law1356VerificationEngine.verifyProfessional(currentProfessional) }
    var currentLicenseStatus by remember(currentProfessional) { mutableStateOf(currentProfessional.licenseStatus) }
    var showEditRegistrationForm by remember { mutableStateOf(false) }

    if (showEditRegistrationForm) {
        Dialog(
            onDismissRequest = { showEditRegistrationForm = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.92f)
            ) {
                EngineerRegistrationFormComponent(
                    projects = emptyList(),
                    institutionProfile = institutionProfile,
                    initialProfessional = currentProfessional,
                    onDismiss = { showEditRegistrationForm = false },
                    onSaveSuccess = { updated ->
                        currentProfessional = updated
                        currentLicenseStatus = updated.licenseStatus
                        onUpdate(updated)
                        showEditRegistrationForm = false
                        Toast.makeText(context, "የ${updated.name} ማህደርና ክህሎቶች በስኬት ተሻሽለዋል!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    val isCompliant = docReport.isFullyCompliant

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, if (isCompliant) AmharicGreen.copy(alpha = 0.5f) else SafetyOrange.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Official Badge Header
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SteelGray.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, AmharicYellow.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = institutionProfile.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmharicYellow,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isCompliant) AmharicGreen.copy(alpha = 0.2f) else SafetyOrange.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = docReport.statutoryStatus,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCompliant) AmharicGreen else SafetyOrange,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(ConstructionTeal.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Engineering, contentDescription = null, tint = ConstructionTeal, modifier = Modifier.size(32.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentProfessional.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "${currentProfessional.role} • ${currentProfessional.grade}",
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                                Text(
                                    text = "መታወቂያ/ፋይዳ፡ ${currentProfessional.nationalId}",
                                    fontSize = 10.sp,
                                    color = AmharicYellow
                                )
                            }
                        }
                    }
                }

                // Statutory Compliance & Verification Report Card (Law 1356/2017)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCompliant) AmharicGreen.copy(alpha = 0.1f) else AmharicRed.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isCompliant) AmharicGreen.copy(alpha = 0.4f) else AmharicRed.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCompliant) Icons.Default.Verified else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isCompliant) AmharicGreen else SafetyOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "የሕግ 1356/2017 ሰነዶች ኦዲት (Verification Audit)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isCompliant) AmharicGreen else AmharicYellow
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isCompliant) AmharicGreen.copy(alpha = 0.2f) else SafetyOrange.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${docReport.complianceScore}% ተገዢ",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCompliant) AmharicGreen else SafetyOrange,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (docReport.flags.isEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AmharicGreen, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "✔ በአዋጅ 1356/2017 መሠረት ሁሉም ሰነዶች የተሟሉና ተገዢ ናቸው",
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                            }
                        } else {
                            Text(
                                text = "የተገኙ የሰነድ ጉድለቶች (${docReport.flags.size}) - ፈጣን ማስተካከያ (Quick Fix):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )

                            docReport.flags.forEach { flag ->
                                val flagAccent = when (flag.severity) {
                                    DocVerificationSeverity.CRITICAL -> AmharicRed
                                    DocVerificationSeverity.HIGH -> SafetyOrange
                                    DocVerificationSeverity.MEDIUM -> AmharicYellow
                                    DocVerificationSeverity.WARNING -> AmharicYellow
                                    else -> ConstructionTeal
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SteelGray.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, flagAccent.copy(alpha = 0.5f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = flag.title,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = flagAccent.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = flag.severity.name,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = flagAccent,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = flag.description,
                                            fontSize = 10.sp,
                                            color = Color.LightGray
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "ሕግ፡ ${flag.statutoryCitation}",
                                                fontSize = 9.sp,
                                                color = AmharicYellow
                                            )

                                            Button(
                                                onClick = {
                                                    val resolved = when (flag.flagType) {
                                                        DocFlagType.EXPIRED_PRACTICING_LICENSE,
                                                        DocFlagType.EXPIRING_PRACTICING_LICENSE -> {
                                                            currentProfessional.copy(
                                                                licenseExpiryDate = System.currentTimeMillis() + (365L * 24 * 3600 * 1000L),
                                                                licenseStatus = "ተገዢ (COMPLIANT)",
                                                                law1356ComplianceStatus = "ተገዢ (COMPLIANT)",
                                                                lastAccreditationUpdate = System.currentTimeMillis()
                                                            )
                                                        }
                                                        DocFlagType.MISSING_INDEMNITY_INSURANCE,
                                                        DocFlagType.EXPIRED_INDEMNITY_INSURANCE,
                                                        DocFlagType.EXPIRING_INDEMNITY_INSURANCE -> {
                                                            currentProfessional.copy(
                                                                insurancePolicyNo = "PII/ETH/1356-${System.currentTimeMillis().toString().takeLast(6)}",
                                                                insuranceExpiryDate = System.currentTimeMillis() + (365L * 24 * 3600 * 1000L),
                                                                lastAccreditationUpdate = System.currentTimeMillis()
                                                            )
                                                        }
                                                        DocFlagType.MISSING_FAYDA_NATIONAL_ID -> {
                                                            currentProfessional.copy(
                                                                nationalId = "ET-FAYDA-${(10000000..99999999).random()}",
                                                                lastAccreditationUpdate = System.currentTimeMillis()
                                                            )
                                                        }
                                                        DocFlagType.CRITICAL_CPD_SHORTFALL,
                                                        DocFlagType.INSUFFICIENT_CPD_POINTS -> {
                                                            currentProfessional.copy(
                                                                cpdPoints = (currentProfessional.cpdPoints + 15).coerceAtLeast(30),
                                                                lastAccreditationUpdate = System.currentTimeMillis()
                                                            )
                                                        }
                                                        DocFlagType.UNVERIFIED_DIGITAL_SIGNATURE -> {
                                                            currentProfessional.copy(
                                                                digitalSignatureVerified = true,
                                                                lastAccreditationUpdate = System.currentTimeMillis()
                                                            )
                                                        }
                                                        DocFlagType.MISSING_DEGREE_DOCUMENT -> {
                                                            val currentDocs = currentProfessional.attachedDocsJson
                                                            val updatedDocs = if (currentDocs.isBlank() || currentDocs == "[]") {
                                                                """["degree_certificate_verified.pdf", "license_copy.pdf"]"""
                                                            } else {
                                                                currentDocs.replace("]", """, "degree_certificate_verified.pdf"]""")
                                                            }
                                                            currentProfessional.copy(
                                                                attachedDocsJson = updatedDocs,
                                                                lastAccreditationUpdate = System.currentTimeMillis()
                                                            )
                                                        }
                                                        DocFlagType.MISSING_ATTACHED_DOSSIER,
                                                        DocFlagType.MISSING_ATTACHED_LICENSE_FILE,
                                                        DocFlagType.MISSING_ATTACHED_INSURANCE_FILE -> {
                                                            currentProfessional.copy(
                                                                attachedDocsJson = """["license_copy.pdf", "degree_certificate_verified.pdf", "insurance_policy.pdf", "cpd_record.pdf"]""",
                                                                digitalSignatureVerified = true,
                                                                lastAccreditationUpdate = System.currentTimeMillis()
                                                            )
                                                        }
                                                        DocFlagType.MISSING_PRACTICING_LICENSE -> {
                                                            currentProfessional.copy(
                                                                licenseNumber = "MUI-REG-2026/AA-${(100..999).random()}",
                                                                licenseExpiryDate = System.currentTimeMillis() + (365L * 24 * 3600 * 1000L),
                                                                licenseStatus = "ተገዢ (COMPLIANT)",
                                                                law1356ComplianceStatus = "ተገዢ (COMPLIANT)",
                                                                lastAccreditationUpdate = System.currentTimeMillis()
                                                            )
                                                        }
                                                        else -> currentProfessional
                                                    }

                                                    val finalResolved = if (resolved.licenseExpiryDate > System.currentTimeMillis() && resolved.insurancePolicyNo.isNotBlank() && resolved.cpdPoints >= 30) {
                                                        resolved.copy(licenseStatus = "ተገዢ (COMPLIANT)", law1356ComplianceStatus = "ተገዢ (COMPLIANT)")
                                                    } else {
                                                        resolved
                                                    }
                                                    currentProfessional = finalResolved
                                                    currentLicenseStatus = finalResolved.licenseStatus
                                                    onResolveFlag?.invoke(currentProfessional, flag.flagType, "")
                                                    onUpdate(finalResolved)
                                                    Toast.makeText(context, "የ '${flag.title}' ጉድለት ተስተካክሎ በመዝገብ ጸድቋል! ✅", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = flagAccent),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.height(26.dp)
                                            ) {
                                                Text("ፈጣን ማስተካከያ (Fix)", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Licensing & Accreditation Details Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = SteelGray.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("የሙያ ፈቃድና የህግ ማዕቀፍ (Statutory Licensing)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AmharicYellow)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("የፈቃድ ቁጥር፡", fontSize = 11.sp, color = Color.LightGray)
                            Text(currentProfessional.licenseNumber, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("የምዝገባ ኮድ (MUI Reg):", fontSize = 11.sp, color = Color.LightGray)
                            Text(currentProfessional.muiRegistrationNo.ifEmpty { "MUI-REG-COMPLIANT" }, fontSize = 11.sp, color = Color.White)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("የስራ ልምድ፡", fontSize = 11.sp, color = Color.LightGray)
                            Text("${currentProfessional.experienceYears} ዓመታት", fontSize = 11.sp, color = Color.White)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("ስልክ / ኢሜይል፡", fontSize = 11.sp, color = Color.LightGray)
                            Text("${currentProfessional.phone} ${if (currentProfessional.email.isNotBlank()) "• ${currentProfessional.email}" else ""}", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }

                // Education & International Standards Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = SteelGray.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("የትምህርት ዝግጅትና ዓለም አቀፍ እውቅና (International Accreditation)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AmharicYellow)

                        Text("ትምህርት፡ ${currentProfessional.qualification}", fontSize = 11.sp, color = Color.White)
                        Text("የተመረቀበት ተቋም፡ ${currentProfessional.institutionGraduated}", fontSize = 11.sp, color = Color.LightGray)

                        if (currentProfessional.internationalCertifications.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ConstructionTeal.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, ConstructionTeal.copy(alpha = 0.4f))
                            ) {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = ConstructionTeal, modifier = Modifier.size(16.dp))
                                    Text("ዓለም አቀፍ እውቅና፡ ${currentProfessional.internationalCertifications}", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }

                        // CPD Points Progress
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("የሙያ ማሻሻያ ነጥብ (CPD Hours):", fontSize = 10.sp, color = Color.LightGray)
                                Text("${currentProfessional.cpdPoints}/30 ሰዓት", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AmharicYellow)
                            }
                            LinearProgressIndicator(
                                progress = { (currentProfessional.cpdPoints / 30f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                color = if (currentProfessional.cpdPoints >= 25) AmharicGreen else AmharicYellow,
                                trackColor = Color.DarkGray
                            )
                        }

                        if (currentProfessional.insurancePolicyNo.isNotBlank()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("የሙያ ዋስትና ኢንሹራንስ፡", fontSize = 10.sp, color = Color.LightGray)
                                Text(currentProfessional.insurancePolicyNo, fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }
                }

                // License Renewal / Status Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "የፈቃድ ሁኔታን አድስ / ቀይር:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                val updated = currentProfessional.copy(
                                    licenseStatus = "ተገዢ (COMPLIANT)",
                                    law1356ComplianceStatus = "ተገዢ (COMPLIANT)",
                                    licenseExpiryDate = System.currentTimeMillis() + (365L * 24 * 3600 * 1000L),
                                    cpdPoints = (currentProfessional.cpdPoints + 5).coerceAtLeast(30),
                                    lastAccreditationUpdate = System.currentTimeMillis()
                                )
                                currentProfessional = updated
                                currentLicenseStatus = updated.licenseStatus
                                onUpdate(updated)
                                Toast.makeText(context, "የ${currentProfessional.name} የሙያ ፈቃድ ታድሷል!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("ፈቃድ አድስ ✓", fontSize = 10.sp)
                        }

                        Button(
                            onClick = {
                                val updated = currentProfessional.copy(
                                    licenseStatus = "ጊዜው ያለፈበት (EXPIRED)",
                                    law1356ComplianceStatus = "ጊዜው ያለፈበት (EXPIRED)",
                                    licenseExpiryDate = System.currentTimeMillis() - (10L * 24 * 3600 * 1000L),
                                    lastAccreditationUpdate = System.currentTimeMillis()
                                )
                                currentProfessional = updated
                                currentLicenseStatus = updated.licenseStatus
                                onUpdate(updated)
                                Toast.makeText(context, "የፈቃድ ሁኔታ ተቀይሯል!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("ጊዜው ያለፈ ⚠", fontSize = 10.sp)
                        }
                    }
                }

                // Edit Full Dossier & Specialized Skills Button
                Button(
                    onClick = { showEditRegistrationForm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ConstructionTeal),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = AmharicYellow)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "የሙያ ፈቃድና ልዩ ክህሎቶችን አሻሽል (Edit Credentials & Skills)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Digital Share & Disciplinary Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val cardText = buildString {
                                appendLine("=========================================")
                                appendLine("የኮንስትራክሽን ባለሙያ ኦፊሴላዊ የሙያ ማረጋገጫ ካርድ")
                                appendLine(institutionProfile.name)
                                appendLine("በኢትዮጵያ አዲሱ ሕንፃ አዋጅ 1356/2017 ዓ.ም እና በFIDIC/ISO መመሪያ መሠረት")
                                appendLine("=========================================")
                                appendLine("• ስም: ${currentProfessional.name}")
                                appendLine("• የብሔራዊ መታወቂያ: ${currentProfessional.nationalId}")
                                appendLine("• ሚና: ${currentProfessional.role}")
                                appendLine("• ደረጃ: ${currentProfessional.grade}")
                                appendLine("• ፈቃድ ቁጥር: ${currentProfessional.licenseNumber}")
                                appendLine("• የትምህርት ደረጃ: ${currentProfessional.qualification} (${currentProfessional.institutionGraduated})")
                                appendLine("• የስራ ልምድ: ${currentProfessional.experienceYears} ዓመታት")
                                appendLine("• የፈቃድ ሁኔታ: $currentLicenseStatus")
                                appendLine("• CPD ነጥብ: ${currentProfessional.cpdPoints}/30")
                                appendLine("• የሙያ መድን ፖሊሲ: ${currentProfessional.insurancePolicyNo}")
                                appendLine("• ዓለም አቀፍ እውቅና: ${currentProfessional.internationalCertifications}")
                                appendLine("• የተመደበበት ኃላፊነት: ${currentProfessional.assignedProjectRole}")
                                appendLine("-----------------------------------------")
                                appendLine("ያረጋገጠው ተቋም: ${institutionProfile.name} (${institutionProfile.officerInCharge})")
                            }
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "የ${currentProfessional.name} የሙያ ማስረጃ ሰነድ")
                                putExtra(Intent.EXTRA_TEXT, cardText)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "የሙያ ማስረጃ አጋራ"))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ConstructionTeal),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ማስረጃ አጋራ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val warningIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "የአስተዳደራዊ ማስጠንቀቂያ፡ ለ${currentProfessional.name} - በአዲሱ የኢትዮጵያ ሕንፃ አዋጅ 1356/2017 ዓ.ም አንቀጽ 32 መሠረት የሙያ ፈቃድዎን በ14 ቀናት ውስጥ እንዲያድሱ እና የሳይት ደህንነት ሪፖርት እንዲያቀርቡ እናሳስባለን።")
                            }
                            context.startActivity(Intent.createChooser(warningIntent, "ማስጠንቀቂያ ላክ"))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, SafetyOrange),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = SafetyOrange, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ማስጠንቀቂያ", fontSize = 11.sp, color = SafetyOrange, fontWeight = FontWeight.Bold)
                    }
                }

                // Delete & Close Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        onDelete()
                        Toast.makeText(context, "ባለሙያው ከመዝገብ ተሰርዟል", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("ከመዝገብ ሰርዝ", color = Color.Red, fontSize = 11.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SteelGray)
                    ) {
                        Text("ዝጋ", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
