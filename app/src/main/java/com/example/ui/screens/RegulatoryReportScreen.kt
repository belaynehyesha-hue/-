package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Artisan
import com.example.data.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ConstructionViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RegulatoryReportScreen(
    viewModel: ConstructionViewModel,
    onConfigureInstitution: () -> Unit
) {
    val context = LocalContext.current
    val institutionProfile by viewModel.institutionProfile.collectAsState()
    val professionals by viewModel.professionals.collectAsState()
    val artisans by viewModel.artisans.collectAsState()
    val projects by viewModel.projects.collectAsState()

    var selectedSection by remember { mutableStateOf(0) } // 0: Executive Audit, 1: Audit Dashboard, 2: Personnel Matrix, 3: Contractor Quorum, 4: Artisans CoC, 5: Laws & 1356 Hub
    val sections = listOf(
        "የቁጥጥር ማጠቃለያ (Summary)",
        "የሰነድ ኦዲት ዳሽቦርድ (Audit Dashboard)",
        "የባለሙያዎች ማህደር (Matrix)",
        "የተቋራጭ የሰው ኃይል ፍተሻ (Quorum)",
        "የCoC ቴክኒክ ሰራተኞች (Artisans)",
        "የሕግና ደንብ ማዕከል (Laws 1356/FIDIC)"
    )

    val totalProfs = professionals.size
    val compliantProfs = professionals.count { it.licenseStatus.contains("COMPLIANT", ignoreCase = true) || it.licenseStatus.contains("ተገዢ") }
    val expiredProfs = totalProfs - compliantProfs
    val totalArtisans = artisans.size
    val safetyCertifiedArtisans = artisans.count { it.safetyTrainingCompleted }
    val internationalCertifiedProfs = professionals.count { it.internationalCertifications.isNotBlank() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Official Institution Header Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, AmharicYellow.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(AmharicYellow.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = AmharicYellow,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = institutionProfile.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${institutionProfile.department} • ${institutionProfile.regionOrCity}",
                                fontSize = 11.sp,
                                color = AmharicYellow,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = onConfigureInstitution,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "የተቋም መለያ ቀይር",
                            tint = AmharicYellow,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ሕጋዊ መሠረት፡ አዋጅ ቁ. 1356/2017 ዓ.ም (አንቀጽ 32/33) & FIDIC Clause 6",
                        fontSize = 9.sp,
                        color = Color.LightGray
                    )
                    Text(
                        text = "ኮድ፡ ${institutionProfile.licenseOrCode}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmharicGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Export Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.shareRegulatoryComplianceReport(context, isExcel = false) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = ConstructionTeal),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ኦፊሴላዊ PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { viewModel.shareRegulatoryComplianceReport(context, isExcel = true) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Excel/CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    val reportText = buildString {
                        appendLine("==================================================")
                        appendLine(institutionProfile.name)
                        appendLine(institutionProfile.department)
                        appendLine("የኮንስትራክሽን ባለሙያዎችና የሰው ኃይል ኦዲት ሪፖርት")
                        appendLine("በአዲሱ የኢትዮጵያ ሕንፃ አዋጅ 1356/2017 ዓ.ም እና በFIDIC/ISO መመሪያዎች መሠረት")
                        appendLine("==================================================")
                        appendLine("• የተመዘገቡ ባለሙያዎች ድምር: $totalProfs")
                        appendLine("• ፈቃዳቸው የታደሰና ተገዢ: $compliantProfs (${if (totalProfs > 0) compliantProfs * 100 / totalProfs else 100}%)")
                        appendLine("• ፈቃዳቸው ያበቃ/እርምጃ የሚሹ: $expiredProfs")
                        appendLine("• የCoC የቴክኒክ ሰራተኞች: $totalArtisans (የደህንነት ስልጠና: $safetyCertifiedArtisans)")
                        appendLine("• ዓለም አቀፍ እውቅና ያላቸው (FIDIC/ISO): $internationalCertifiedProfs")
                        appendLine("\n--- ቁልፍ ባለሙያዎች ---")
                        professionals.forEachIndexed { i, p ->
                            appendLine("${i + 1}. ${p.name} | ${p.role} | ${p.grade} | ${p.licenseNumber} | ${p.licenseStatus} | CPD: ${p.cpdPoints}/30")
                        }
                        appendLine("\nያረጋገጠው ኃላፊ: ${institutionProfile.officerInCharge}")
                    }
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "${institutionProfile.name} - የባለሙያዎች ኦዲት ሪፖርት")
                        putExtra(Intent.EXTRA_TEXT, reportText)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "ሪፖርት አጋራ"))
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, AmharicYellow),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = AmharicYellow, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ፅሁፍ አጋራ", fontSize = 11.sp, color = AmharicYellow, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Operational Manual SOP Action Banner
        Surface(
            color = SteelGray.copy(alpha = 0.5f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, AmharicGreen.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.openOrShareOperationalManual(context) }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = AmharicYellow, modifier = Modifier.size(20.dp))
                    Column {
                        Text(
                            text = "📘 የተሟላ የሲስተም አሰራር መመሪያ (Operational Manual PDF)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "ለባለሙያዎች፣ ለተቋራጮችና ለተቆጣጣሪዎች የተዘጋጀ ኦፊሴላዊ መመሪያ ሰነድ (SOP) በPDF አውርድ ወይም አጋራ",
                            fontSize = 9.sp,
                            color = Color.LightGray
                        )
                    }
                }
                Icon(Icons.Default.Download, contentDescription = "መመሪያ አውርድ", tint = AmharicYellow, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Section Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedSection,
            containerColor = Color.Transparent,
            edgePadding = 0.dp,
            divider = {},
            indicator = {}
        ) {
            sections.forEachIndexed { index, title ->
                val isSelected = selectedSection == index
                Tab(
                    selected = isSelected,
                    onClick = { selectedSection = index },
                    text = {
                        Text(
                            text = title,
                            color = if (isSelected) AmharicYellow else Color.Gray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Section Contents
        when (selectedSection) {
            0 -> ExecutiveAuditSummaryView(
                totalProfs = totalProfs,
                compliantProfs = compliantProfs,
                expiredProfs = expiredProfs,
                totalArtisans = totalArtisans,
                safetyArtisans = safetyCertifiedArtisans,
                intlCertified = internationalCertifiedProfs,
                officerInCharge = institutionProfile.officerInCharge
            )
            1 -> StatutoryVerificationAuditDashboardView(
                viewModel = viewModel,
                professionals = professionals,
                institutionProfile = institutionProfile
            )
            2 -> PersonnelMatrixView(professionals = professionals)
            3 -> ContractorStaffingQuorumView(projects = projects, professionals = professionals)
            4 -> ArtisansCocAuditView(artisans = artisans)
            5 -> RegulatoryStandardsHub(
                institutionProfile = institutionProfile,
                onOpenManualPdf = { viewModel.openOrShareOperationalManual(context) }
            )
        }
    }
}

@Composable
fun ExecutiveAuditSummaryView(
    totalProfs: Int,
    compliantProfs: Int,
    expiredProfs: Int,
    totalArtisans: Int,
    safetyArtisans: Int,
    intlCertified: Int,
    officerInCharge: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // KPI Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatAuditBox(
                title = "ጠቅላላ ባለሙያዎች",
                value = "$totalProfs",
                subtitle = "መሃንዲሶችና ተቋራጮች",
                icon = Icons.Default.Groups,
                color = ConstructionTeal,
                modifier = Modifier.weight(1f)
            )
            StatAuditBox(
                title = "ተገዢና የታደሱ",
                value = "$compliantProfs",
                subtitle = "${if (totalProfs > 0) compliantProfs * 100 / totalProfs else 100}% ብቁ ፈቃድ",
                icon = Icons.Default.VerifiedUser,
                color = AmharicGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatAuditBox(
                title = "ፈቃዳቸው ያበቃ",
                value = "$expiredProfs",
                subtitle = if (expiredProfs > 0) "እድሳት የሚሹ!" else "ምንም የለም (Clean)",
                icon = Icons.Default.WarningAmber,
                color = if (expiredProfs > 0) SafetyOrange else AmharicGreen,
                modifier = Modifier.weight(1f)
            )
            StatAuditBox(
                title = "የCoC ሰራተኞች",
                value = "$totalArtisans",
                subtitle = "$safetyArtisans የደህንነት ሰርተፊኬት",
                icon = Icons.Default.Engineering,
                color = AmharicYellow,
                modifier = Modifier.weight(1f)
            )
        }

        // Regulatory Compliance Verdict Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, if (expiredProfs == 0) AmharicGreen.copy(alpha = 0.5f) else SafetyOrange.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (expiredProfs == 0) Icons.Default.CheckCircle else Icons.Default.Gavel,
                        contentDescription = null,
                        tint = if (expiredProfs == 0) AmharicGreen else SafetyOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "የቁጥጥር ውሳኔና ህጋዊ ማጠቃለያ (Regulatory Verdict)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }

                Text(
                    text = if (expiredProfs == 0) {
                        "የተመዘገቡት መሃንዲሶች፣ ተቆጣጣሪዎች፣ አማካሪዎችና ተቋራጮች በሙሉ በአዲሱ የኢትዮጵያ ሕንፃ አዋጅ ቁጥር 1356/2017 ዓ.ም አንቀጽ 32 እና 33 መሠረት የታደሰ ህጋዊ የሙያ ፈቃድ ያላቸው ሲሆን፣ የሰው ኃይል ምጣኔያቸውም የFIDIC ውል አንቀጽ 6.9 መስፈርቶችን ያሟላል።"
                    } else {
                        "በኦዲቱ መሠረት $expiredProfs ባለሙያዎች የሙያ ፈቃዳቸው ያበቃ በመሆኑ በአዲሱ የኢትዮጵያ ህንፃ አዋጅ 1356/2017 ዓ.ም አንቀጽ 32 ንዑስ አንቀጽ 1 መሠረት በግንባታ ሳይት ላይ ፍተሻና ስራ እንዳያከናውኑ የ14 ቀናት የፈቃድ እድሳት ማስጠንቀቂያ እንዲሰጣቸው ተወስኗል።"
                    },
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    lineHeight = 17.sp
                )

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ያረጋገጠው መሪ መሃንዲስ፡ $officerInCharge",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmharicYellow
                    )
                    Text(
                        text = "ተረጋግጧል ✓",
                        fontSize = 10.sp,
                        color = AmharicGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Citations Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SteelGray.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "የተጠቃቀሱ የሕግ ማዕቀፎች (Statutory References):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = AmharicYellow
                )
                Text(
                    text = "1. አዲሱ የኢትዮጵያ ሕንፃ አዋጅ ቁጥር 1356/2017 ዓ.ም (የቀድሞው 624/2001 ተሻሽሎ የወጣ)\n" +
                            "2. የህንፃ ደንብ ቁጥር 243/2003 ዓ.ም እና ማሻሻያ ደንቦች\n" +
                            "3. የከተማና መሠረተ ልማት ሚኒስቴር የሙያተኞችና ተቋራጮች ምዝገባ መመሪያ\n" +
                            "4. FIDIC Red/Yellow/Silver/Emerald Conditions of Contract (Key Personnel)\n" +
                            "5. IBC 2024 (International Building Code) & Eurocodes (EN 1990-1999)\n" +
                            "6. ISO 45001:2018 (HSE), OSHA 1926 እና ISO 9001:2015 (QA/QC)",
                    fontSize = 10.sp,
                    color = Color.LightGray,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun StatAuditBox(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 11.sp, color = Color.LightGray)
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = subtitle, fontSize = 10.sp, color = color)
        }
    }
}

@Composable
fun PersonnelMatrixView(professionals: List<Professional>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(professionals) { prof ->
            val isCompliant = prof.licenseStatus.contains("COMPLIANT", ignoreCase = true) || prof.licenseStatus.contains("ተገዢ")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (isCompliant) AmharicGreen.copy(alpha = 0.3f) else SafetyOrange.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = prof.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Text(
                                text = "${prof.role} • ${prof.grade}",
                                fontSize = 11.sp,
                                color = AmharicYellow
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isCompliant) AmharicGreen.copy(alpha = 0.2f) else SafetyOrange.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, if (isCompliant) AmharicGreen else SafetyOrange)
                        ) {
                            Text(
                                text = prof.licenseStatus,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCompliant) AmharicGreen else SafetyOrange,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "ፈቃድ ቁጥር፡ ${prof.licenseNumber}", fontSize = 10.sp, color = Color.LightGray)
                        Text(text = "የስራ ልምድ፡ ${prof.experienceYears} ዓመት", fontSize = 10.sp, color = Color.LightGray)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "ትምህርት፡ ${prof.qualification}", fontSize = 10.sp, color = Color.LightGray)
                        Text(text = "CPD: ${prof.cpdPoints}/30 ሰዓት", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AmharicYellow)
                    }

                    if (prof.insurancePolicyNo.isNotBlank()) {
                        Text(text = "የሙያ መድን ዋስትና ፖሊሲ፡ ${prof.insurancePolicyNo}", fontSize = 9.sp, color = Color.Gray)
                    }

                    if (prof.internationalCertifications.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = ConstructionTeal.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "ዓለም አቀፍ እውቅና፡ ${prof.internationalCertifications}",
                                fontSize = 9.sp,
                                color = ConstructionTeal,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (prof.assignedProjectRole.isNotBlank()) {
                        Text(
                            text = "ኃላፊነት፡ ${prof.assignedProjectRole}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContractorStaffingQuorumView(projects: List<Project>, professionals: List<Professional>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SteelGray.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.FactCheck, contentDescription = null, tint = AmharicYellow, modifier = Modifier.size(20.dp))
                    Text(
                        text = "በኢትዮጵያ የኮንስትራክሽን ግዥ (PPA) እና በFIDIC ውል አንቀጽ 6.9 መሠረት በግንባታ ላይ ያሉ ፕሮጀክቶች አነስተኛ የሰው ኃይል ፍተሻ፡",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }
            }
        }

        items(projects) { project ->
            val contractor = professionals.find { it.id == project.contractorId }
            val consultant = professionals.find { it.id == project.consultantId }
            val inspector = professionals.find { it.id == project.inspectorId }

            val hasFullQuorum = contractor != null && inspector != null

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, if (hasFullQuorum) AmharicGreen.copy(alpha = 0.4f) else SafetyOrange.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = project.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (hasFullQuorum) AmharicGreen.copy(alpha = 0.2f) else SafetyOrange.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (hasFullQuorum) "የሰው ኃይል ተሟልቷል ✓" else "ያልተሟላ ኃይል ⚠",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (hasFullQuorum) AmharicGreen else SafetyOrange,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(text = "ቦታ፡ ${project.location} • ምድብ፡ ${project.buildingCategory}", fontSize = 10.sp, color = Color.Gray)

                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

                    // Staff check rows
                    StaffRequirementRow(
                        roleTitle = "ዋና ስራ ተቋራጭ (Contractor)",
                        assignedName = contractor?.name ?: "አልተመደበም",
                        isAssigned = contractor != null,
                        grade = contractor?.grade ?: "ደረጃ አልተገለጸም"
                    )

                    StaffRequirementRow(
                        roleTitle = "አማካሪ መሃንዲስ (Consultant)",
                        assignedName = consultant?.name ?: "አልተመደበም",
                        isAssigned = consultant != null,
                        grade = consultant?.grade ?: "ደረጃ አልተገለጸም"
                    )

                    StaffRequirementRow(
                        roleTitle = "የስራ ተቆጣጣሪ (Inspector / Clerk)",
                        assignedName = inspector?.name ?: "አልተመደበም",
                        isAssigned = inspector != null,
                        grade = inspector?.grade ?: "ደረጃ አልተገለጸም"
                    )
                }
            }
        }
    }
}

@Composable
fun StaffRequirementRow(
    roleTitle: String,
    assignedName: String,
    isAssigned: Boolean,
    grade: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                imageVector = if (isAssigned) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (isAssigned) AmharicGreen else Color.Red,
                modifier = Modifier.size(14.dp)
            )
            Text(text = roleTitle, fontSize = 10.sp, color = Color.LightGray)
        }
        Text(
            text = "$assignedName ($grade)",
            fontSize = 10.sp,
            fontWeight = if (isAssigned) FontWeight.Bold else FontWeight.Normal,
            color = if (isAssigned) Color.White else Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ArtisansCocAuditView(artisans: List<Artisan>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SteelGray.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = AmharicYellow, modifier = Modifier.size(20.dp))
                    Text(
                        text = "በብሔራዊ የቴክኒክና ሙያ ብቃት ማዕቀፍ (TVET NQF) መሠረት የCoC ደረጃ 1 እስከ 5 እና የሳይት ደህንነት (Safety Induction) ኦዲት፡",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }
            }
        }

        items(artisans) { artisan ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (artisan.safetyTrainingCompleted) AmharicGreen.copy(alpha = 0.3f) else SafetyOrange.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = artisan.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AmharicYellow.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = artisan.cocLevel,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmharicYellow,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "የስራ ዘርፍ፡ ${artisan.tradeSector}", fontSize = 11.sp, color = Color.LightGray)
                        Text(text = "ስልክ፡ ${artisan.phone}", fontSize = 11.sp, color = Color.LightGray)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "CoC ቁጥር፡ ${artisan.cocNumber}", fontSize = 10.sp, color = Color.Gray)
                        Text(text = "ልምድ፡ ${artisan.experienceYears} ዓመት", fontSize = 10.sp, color = Color.Gray)
                    }

                    Text(text = "የሰጠው ማዕከል፡ ${artisan.issuingCenter}", fontSize = 9.sp, color = Color.Gray)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                imageVector = if (artisan.safetyTrainingCompleted) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (artisan.safetyTrainingCompleted) AmharicGreen else SafetyOrange,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (artisan.safetyTrainingCompleted) "የደህንነት ስልጠና ተጠናቋል (OSHA Induction ✓)" else "የደህንነት ስልጠና አልተጠናቀቀም",
                                fontSize = 9.sp,
                                color = if (artisan.safetyTrainingCompleted) AmharicGreen else SafetyOrange
                            )
                        }

                        Text(
                            text = if (artisan.hasPermit) "ህጋዊ ፈቃድ አለው" else "ፈቃድ የለውም",
                            fontSize = 9.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatutoryVerificationAuditDashboardView(
    viewModel: ConstructionViewModel,
    professionals: List<Professional>,
    institutionProfile: InstitutionProfile
) {
    val context = LocalContext.current
    val summary = remember(professionals) { viewModel.getSystemVerificationAudit(professionals) }
    var selectedFilter by remember { mutableStateOf(0) } // 0: All, 1: Flagged, 2: Expired, 3: Missing Docs, 4: Compliant
    var profToDetailDossier by remember { mutableStateOf<Professional?>(null) }

    profToDetailDossier?.let { prof ->
        ProfessionalDossierDetailDialog(
            professional = prof,
            institutionProfile = institutionProfile,
            onDismiss = { profToDetailDossier = null },
            onUpdate = { updated ->
                viewModel.updateProfessional(updated)
                profToDetailDossier = updated
            },
            onDelete = {
                viewModel.deleteProfessional(prof)
                profToDetailDossier = null
            },
            onResolveFlag = { p, flagType, value ->
                viewModel.resolveFlag(p, flagType, value)
            }
        )
    }

    val filteredReports = remember(summary, selectedFilter) {
        when (selectedFilter) {
            1 -> summary.reports.filter { it.flags.isNotEmpty() }
            2 -> summary.reports.filter { it.hasExpiredDocs }
            3 -> summary.reports.filter { it.hasMissingDocs }
            4 -> summary.reports.filter { it.isFullyCompliant }
            else -> summary.reports
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Header: Bureau Statutory Audit
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, ConstructionTeal.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Policy, contentDescription = null, tint = AmharicYellow, modifier = Modifier.size(20.dp))
                            Text(
                                text = "የሕግ 1356/2017 አጠቃላይ የሰነዶች ኦዲት ዳሽቦርድ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "በኢትዮጵያ ሕንፃ አዋጅ 1356/2017 መሠረት የባለሙያዎች ፈቃድ፣ መድንና CPD ትክክለኛነት ፍተሻ",
                            fontSize = 10.sp,
                            color = Color.LightGray
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (summary.averageComplianceScore >= 80) AmharicGreen.copy(alpha = 0.2f) else SafetyOrange.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, if (summary.averageComplianceScore >= 80) AmharicGreen else SafetyOrange)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("አጠቃላይ ነጥብ", fontSize = 9.sp, color = Color.LightGray)
                            Text("${summary.averageComplianceScore}%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (summary.averageComplianceScore >= 80) AmharicGreen else SafetyOrange)
                        }
                    }
                }

                HorizontalDivider(color = SteelGray)

                // Quick Bulk Operations Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.runAutomatedStatutoryAudit(professionals) { flagged, updated ->
                                Toast.makeText(
                                    context,
                                    "የ $updated ባለሙያዎች ሰነዶች ከመረጃ ቋት ጋር ተመሳስለዋል! $flagged የሰነድ ጉድለቶች ተገኝተዋል። ⚖️",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ConstructionTeal),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("አጠቃላይ ህጋዊ ኦዲት አስጀምር", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

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
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ሪፖርት አጋራ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // KPI Stat Cards Grid (4 key metrics)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatAuditBox(
                title = "የተመረመሩ ባለሙያዎች",
                value = "${summary.totalProfessionalsScanned}",
                subtitle = "ጠቅላላ በመዝገብ ላይ ያሉ",
                icon = Icons.Default.Groups,
                color = ConstructionTeal,
                modifier = Modifier.weight(1f)
            )
            StatAuditBox(
                title = "ሙሉ ተገዢ (Compliant)",
                value = "${summary.fullyCompliantCount}",
                subtitle = "${if (summary.totalProfessionalsScanned > 0) summary.fullyCompliantCount * 100 / summary.totalProfessionalsScanned else 100}% ተሟልተዋል",
                icon = Icons.Default.VerifiedUser,
                color = AmharicGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatAuditBox(
                title = "የተከሱ ሰነዶች (Flagged)",
                value = "${summary.flaggedProfessionalsCount}",
                subtitle = "ማስተካከያ የሚሹ",
                icon = Icons.Default.Warning,
                color = if (summary.flaggedProfessionalsCount > 0) AmharicRed else AmharicGreen,
                modifier = Modifier.weight(1f)
            )
            StatAuditBox(
                title = "የፈቃድ/መድን ማለፍ (Expired)",
                value = "${summary.expiredDocsProfessionalsCount}",
                subtitle = "አስቸኳይ እድሳት",
                icon = Icons.Default.TimerOff,
                color = if (summary.expiredDocsProfessionalsCount > 0) SafetyOrange else AmharicGreen,
                modifier = Modifier.weight(1f)
            )
        }

        // Filter Chips Row
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("የባለሙያዎች ማጣሪያ (Audit Filter):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "ሁሉም (${summary.totalProfessionalsScanned})" to 0,
                    "የተከሱ (${summary.flaggedProfessionalsCount})" to 1,
                    "ያለፈባቸው (${summary.expiredDocsProfessionalsCount})" to 2,
                    "ያጎደሉ (${summary.missingDocsProfessionalsCount})" to 3,
                    "ተገዢ (${summary.fullyCompliantCount})" to 4
                ).forEach { (label, idx) ->
                    val isSelected = selectedFilter == idx
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = idx },
                        label = { Text(label, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmharicYellow.copy(alpha = 0.2f),
                            selectedLabelColor = AmharicYellow,
                            containerColor = SteelGray.copy(alpha = 0.3f),
                            labelColor = Color.LightGray
                        ),
                        border = BorderStroke(1.dp, if (isSelected) AmharicYellow else Color.Transparent)
                    )
                }
            }
        }

        // List of Filtered Professionals and Quick Fix Actions
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "የባለሙያዎች ዝርዝር ምርመራ (${filteredReports.size}):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AmharicYellow
            )

            if (filteredReports.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AmharicGreen, modifier = Modifier.size(36.dp))
                        Text("በዚህ መስፈርት የተገኘ ባለሙያ የለም ወይም ሁሉም ሙሉ ተገዢ ናቸው!", fontSize = 12.sp, color = Color.LightGray, textAlign = TextAlign.Center)
                    }
                }
            } else {
                filteredReports.forEach { report ->
                    val prof = report.professional
                    val isRepCompliant = report.isFullyCompliant
                    val statusColor = if (isRepCompliant) AmharicGreen else if (report.hasExpiredDocs) AmharicRed else SafetyOrange

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                            .background(statusColor.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isRepCompliant) Icons.Default.Verified else Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = statusColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = prof.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "${prof.role} • ${prof.licenseNumber} • ${prof.grade}",
                                            fontSize = 10.sp,
                                            color = Color.LightGray
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = statusColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${report.complianceScore}% ተገዢ",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // Flags List with Quick Fix Button
                            if (report.flags.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    report.flags.forEach { flag ->
                                        val flagAccent = when (flag.severity) {
                                            DocVerificationSeverity.CRITICAL -> AmharicRed
                                            DocVerificationSeverity.HIGH -> SafetyOrange
                                            DocVerificationSeverity.MEDIUM -> AmharicYellow
                                            DocVerificationSeverity.WARNING -> AmharicYellow
                                            else -> ConstructionTeal
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = SteelGray.copy(alpha = 0.35f),
                                            border = BorderStroke(1.dp, flagAccent.copy(alpha = 0.4f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Text("• ${flag.title}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                        Text("(${flag.statutoryCitation})", fontSize = 8.sp, color = AmharicYellow)
                                                    }
                                                    Text(flag.description, fontSize = 9.sp, color = Color.LightGray)
                                                }

                                                Spacer(modifier = Modifier.width(6.dp))

                                                Button(
                                                    onClick = {
                                                        viewModel.resolveFlag(prof, flag.flagType, "")
                                                        Toast.makeText(context, "የ '${flag.title}' ጉድለት ተስተካክሏል! ✅", Toast.LENGTH_SHORT).show()
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
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AmharicGreen, modifier = Modifier.size(14.dp))
                                    Text("ሁሉም ሰነዶች የተሟሉና ተገዢ ናቸው (100% Compliant)", fontSize = 10.sp, color = Color.LightGray)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { profToDetailDossier = prof },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp), tint = ConstructionTeal)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ሙሉ ማህደርና ሰነዶች ተመልከት >", fontSize = 10.sp, color = ConstructionTeal)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

