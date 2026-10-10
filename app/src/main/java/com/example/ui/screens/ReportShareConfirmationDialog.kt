package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import com.example.ui.viewmodel.ReportFormat
import com.example.ui.viewmodel.ShareReportState

// Destination app branding colors
private val WhatsAppColor = Color(0xFF25D366)
private val TelegramColor = Color(0xFF2AABEE)
private val GmailColor = Color(0xFFEA4335)
private val DriveColor = Color(0xFFFBBC05)

@Composable
fun ReportShareConfirmationDialog(
    state: ShareReportState,
    onDismiss: () -> Unit,
    onConfirmShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prompt = state.prompt
    val isExcel = prompt.format == ReportFormat.EXCEL
    val formatColor = if (isExcel) AmharicGreen else ConstructionTeal
    val formatBadgeBg = if (isExcel) AmharicGreen.copy(alpha = 0.2f) else AmharicRed.copy(alpha = 0.2f)
    val formatBadgeBorder = if (isExcel) AmharicGreen else AmharicRed

    Dialog(
        onDismissRequest = {
            if (!state.isGenerating) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = !state.isGenerating,
            dismissOnClickOutside = !state.isGenerating,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 520.dp)
                .padding(vertical = 16.dp)
                .testTag("report_share_confirmation_dialog"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2028)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(
                1.5.dp,
                Brush.verticalGradient(
                    listOf(
                        formatColor.copy(alpha = 0.8f),
                        AmharicYellow.copy(alpha = 0.4f),
                        Color(0xFF2D323E)
                    )
                )
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Format Icon and Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(
                                    if (isExcel) AmharicGreen.copy(alpha = 0.15f) else AmharicRed.copy(alpha = 0.15f),
                                    CircleShape
                                )
                                .border(
                                    1.5.dp,
                                    if (isExcel) AmharicGreen else AmharicRed,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isExcel) Icons.Default.GridOn else Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = if (isExcel) AmharicGreen else AmharicRed,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "የሪፖርት ማጋሪያ ማረጋገጫ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Share Report Confirmation",
                                fontSize = 10.sp,
                                color = Color.LightGray
                            )
                        }
                    }

                    if (!state.isGenerating) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("close_share_dialog_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "ዝጋ",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(14.dp))

                // Report Title Box
                Text(
                    text = prompt.titleAm,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmharicYellow,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = prompt.titleEn,
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // File Format & Specs Card
                Surface(
                    color = Color(0xFF14171E),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, formatColor.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // File format badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "የፋይል ቅርጸት (File Format):",
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                fontWeight = FontWeight.Medium
                            )
                            Surface(
                                color = formatBadgeBg,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, formatBadgeBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isExcel) Icons.Default.TableChart else Icons.Default.Description,
                                        contentDescription = null,
                                        tint = if (isExcel) AmharicGreen else AmharicRed,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = prompt.format.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isExcel) AmharicGreen else Color(0xFFFF6B6B)
                                    )
                                }
                            }
                        }

                        // File name row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = null,
                                tint = AmharicYellow,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "የፋይሉ ስም (File Name):",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = prompt.fileName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }

                        // Format description row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = ConstructionTeal,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "የፋይሉ አይነትና አጠቃቀም:",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = prompt.format.fileTypeNameAm,
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                            }
                        }

                        // Summary description
                        if (prompt.descriptionAm.isNotBlank()) {
                            Text(
                                text = prompt.descriptionAm,
                                fontSize = 11.sp,
                                color = Color.LightGray.copy(alpha = 0.9f),
                                lineHeight = 16.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1B1F27), RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Target Apps Row (WhatsApp, Telegram, Email, More)
                Text(
                    text = "በቀጥታ ለማጋራት የሚደገፉ መተግበሪያዎች (Target Apps):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TargetAppChip(
                        name = "WhatsApp",
                        color = WhatsAppColor,
                        icon = Icons.Default.Chat,
                        modifier = Modifier.weight(1f)
                    )
                    TargetAppChip(
                        name = "Telegram",
                        color = TelegramColor,
                        icon = Icons.Default.Send,
                        modifier = Modifier.weight(1f)
                    )
                    TargetAppChip(
                        name = "Gmail/Email",
                        color = GmailColor,
                        icon = Icons.Default.Email,
                        modifier = Modifier.weight(1f)
                    )
                    TargetAppChip(
                        name = "Drive/Files",
                        color = DriveColor,
                        icon = Icons.Default.CloudUpload,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "✓ ፋይሉ በአንድሮይድ Native Share Sheet አማካኝነት ከላይ በተጠቀሱት እና በስልክዎ ባሉ ሌሎች መተግበሪያዎች በሙሉ ጥራት ይላካል።",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                )

                // Progress Indicator (Active while generating)
                AnimatedVisibility(
                    visible = state.isGenerating,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                            .background(Color(0xFF222834), RoundedCornerShape(12.dp))
                            .border(1.dp, AmharicYellow.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = AmharicYellow,
                            strokeWidth = 3.dp,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("share_generating_progress_indicator")
                        )

                        Text(
                            text = if (state.progressStatusText.isNotBlank()) state.progressStatusText else "ሰነዱን በማዘጋጀት ላይ ነው... እባክዎ ይጠብቁ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        LinearProgressIndicator(
                            color = formatColor,
                            trackColor = Color.DarkGray,
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(4.dp)
                        )

                        Text(
                            text = "የደህንነት ፈቃድ (FileProvider) እና የማጋሪያ መስኮት (Share Sheet) በመክፈት ላይ...",
                            fontSize = 10.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Error Message if failed
                if (state.errorMessage != null && !state.isGenerating) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = AmharicRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, AmharicRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = AmharicRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = state.errorMessage,
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !state.isGenerating,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("cancel_share_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text(
                            text = "ሰርዝ (Cancel)",
                            fontSize = 12.sp,
                            color = if (state.isGenerating) Color.DarkGray else Color.White
                        )
                    }

                    Button(
                        onClick = onConfirmShare,
                        enabled = !state.isGenerating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = formatColor,
                            disabledContainerColor = formatColor.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("confirm_share_button"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.isGenerating) "እያዘጋጀ ነው..." else "በመተግበሪያዎች አጋራ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TargetAppChip(
    name: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF14171E),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = name,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = name,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}
