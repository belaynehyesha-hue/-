package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.InstitutionProfile
import com.example.ui.theme.AmharicGreen
import com.example.ui.theme.AmharicYellow
import com.example.ui.theme.CardBackground
import com.example.ui.theme.ConstructionTeal
import com.example.ui.theme.SteelGray

@Composable
fun InstitutionSettingsDialog(
    currentProfile: InstitutionProfile,
    onDismiss: () -> Unit,
    onSave: (InstitutionProfile) -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(currentProfile.name) }
    var department by remember { mutableStateOf(currentProfile.department) }
    var type by remember { mutableStateOf(currentProfile.type) }
    var regionOrCity by remember { mutableStateOf(currentProfile.regionOrCity) }
    var licenseOrCode by remember { mutableStateOf(currentProfile.licenseOrCode) }
    var officerInCharge by remember { mutableStateOf(currentProfile.officerInCharge) }
    var contactPhone by remember { mutableStateOf(currentProfile.contactPhone) }
    var contactEmail by remember { mutableStateOf(currentProfile.contactEmail) }
    var officialMotto by remember { mutableStateOf(currentProfile.officialMotto) }

    val institutionTypes = listOf(
        "የቁጥጥርና ፈቃድ ሰጪ ባለስልጣን (Regulatory Authority)",
        "የከተማ አስተዳደር ጽሕፈት ቤት (Municipal Office)",
        "ጠቅላላ ተቋራጭ (General Contractor - GC)",
        "የህንፃ ተቋራጭ (Building Contractor - BC)",
        "አማካሪ መሀንዲሶች ድርጅት (Engineering Consultancy)",
        "አሰሪ / የሪል እስቴት አልሚ (Developer / Client)",
        "የመሰረተ ልማት ኤጀንሲ (Infrastructure Agency)",
        "ዓለም አቀፍ የኮንስትራክሽን ጥምረት (International JV)"
    )
    var expandedType by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, AmharicYellow.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(AmharicYellow.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = AmharicYellow,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "የተቋም መለያና ስም ማስተካከያ",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "በየትኛውም ቦታ ያለ ተቋም/ድርጅት መጠቀም የሚያስችል",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                        )
                    }
                }

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))

                // Presets
                Text(
                    text = "ከተዘጋጁ የተቋም አይነቶች ምረጥ (Quick Presets):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmharicYellow
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(InstitutionProfile.PRESET_ORGANIZATIONS) { preset ->
                        val isSelected = preset.name == name
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) AmharicYellow else Color.White.copy(alpha = 0.08f),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) AmharicYellow else Color.Gray.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.clickable {
                                name = preset.name
                                department = preset.department
                                type = preset.type
                                regionOrCity = preset.regionOrCity
                                licenseOrCode = preset.licenseOrCode
                                officerInCharge = preset.officerInCharge
                            }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(
                                    text = preset.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else Color.White
                                )
                                Text(
                                    text = preset.type,
                                    fontSize = 9.sp,
                                    color = if (isSelected) Color.DarkGray else Color.LightGray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Input fields
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("የተቋሙ / የድርጅቱ ሙሉ ስም") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = AmharicYellow) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AmharicYellow,
                        unfocusedBorderColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("የመምሪያ / የስራ ክፍል ስም") },
                    leadingIcon = { Icon(Icons.Default.Domain, contentDescription = null, tint = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AmharicYellow,
                        unfocusedBorderColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Institution Type Dropdown
                Box {
                    OutlinedButton(
                        onClick = { expandedType = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.Gray)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("የተቋሙ አይነት (Institution Type):", fontSize = 10.sp, color = Color.Gray)
                                Text(type, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AmharicYellow)
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AmharicYellow)
                        }
                    }
                    DropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false }
                    ) {
                        institutionTypes.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item, fontSize = 12.sp) },
                                onClick = {
                                    type = item
                                    expandedType = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = regionOrCity,
                        onValueChange = { regionOrCity = it },
                        label = { Text("ከተማ / ክልል") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AmharicYellow,
                            unfocusedBorderColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = licenseOrCode,
                        onValueChange = { licenseOrCode = it },
                        label = { Text("የምዝገባ/ፈቃድ ኮድ") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AmharicYellow,
                            unfocusedBorderColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                OutlinedTextField(
                    value = officerInCharge,
                    onValueChange = { officerInCharge = it },
                    label = { Text("ኃላፊ / መሪ መሃንዲስ (Lead Engineer / Officer)") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = AmharicGreen) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AmharicYellow,
                        unfocusedBorderColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = { Text("የተቋሙ ስልክ") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AmharicYellow,
                            unfocusedBorderColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = contactEmail,
                        onValueChange = { contactEmail = it },
                        label = { Text("ኢሜይል") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AmharicYellow,
                            unfocusedBorderColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                OutlinedTextField(
                    value = officialMotto,
                    onValueChange = { officialMotto = it },
                    label = { Text("የሕግና ስታንዳርድ መመሪያ ርዕስ (Legal Baseline)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AmharicYellow,
                        unfocusedBorderColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Info banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = SteelGray.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = AmharicYellow, modifier = Modifier.size(20.dp))
                        Text(
                            text = "እነዚህ መረጃዎች በሁሉም ኦፊሴላዊ የኦዲት ሪፖርቶች፣ በፒዲኤፍ እና በኤክሴል ሰነዶች አናት ላይ በህጋዊ ማህተምነት ይቀመጣሉ።",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color.Gray)
                    ) {
                        Text("ይቅር", color = Color.White)
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val updated = InstitutionProfile(
                                    name = name.trim(),
                                    department = department.trim(),
                                    type = type.trim(),
                                    regionOrCity = regionOrCity.trim(),
                                    licenseOrCode = licenseOrCode.trim(),
                                    officerInCharge = officerInCharge.trim(),
                                    contactPhone = contactPhone.trim(),
                                    contactEmail = contactEmail.trim(),
                                    officialMotto = officialMotto.trim()
                                )
                                onSave(updated)
                                Toast.makeText(context, "የተቋሙ መረጃ በተሳካ ሁኔታ ተሻሽሏል!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("አስቀምጥ", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
