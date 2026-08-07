package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.screens.AmharicGreen
import com.example.ui.screens.AmharicYellow
import com.example.ui.screens.AmharicRed
import com.example.ui.screens.CardBackground
import com.example.ui.screens.SteelGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EthiopianCalendarTab(projects: List<Project>) {
    val context = LocalContext.current
    
    // Calendar view state
    var selectedYear by remember { mutableStateOf(EthiopianCalendar.getCurrentEthiopianDate().year) }
    var selectedMonth by remember { mutableStateOf(EthiopianCalendar.getCurrentEthiopianDate().month) }
    var selectedDay by remember { mutableStateOf(EthiopianCalendar.getCurrentEthiopianDate().day) }
    
    val currentEthDate = remember { EthiopianCalendar.getCurrentEthiopianDate() }
    
    // Lease calculator state
    var selectedProjectForCalc by remember { mutableStateOf<Project?>(projects.firstOrNull()) }
    var inputLeaseDay by remember { mutableStateOf("1") }
    var inputLeaseMonth by remember { mutableStateOf("1") }
    var inputLeaseYear by remember { mutableStateOf("2017") }
    var selectedScale by remember { mutableStateOf("Small") } // Small, Medium, Large
    var computedLeaseMilestones by remember { mutableStateOf<List<LeaseMilestone>>(emptyList()) }
    
    // Auto-calculate lease milestones for the selected project if exists
    LaunchedEffect(selectedProjectForCalc, selectedScale) {
        selectedProjectForCalc?.let { proj ->
            // Use project registration timestamp as start date
            val ethRegDate = EthiopianCalendar.gregorianToEthiopian(
                java.util.Calendar.getInstance().apply { timeInMillis = proj.timestamp }.get(java.util.Calendar.YEAR),
                java.util.Calendar.getInstance().apply { timeInMillis = proj.timestamp }.get(java.util.Calendar.MONTH) + 1,
                java.util.Calendar.getInstance().apply { timeInMillis = proj.timestamp }.get(java.util.Calendar.DAY_OF_MONTH)
            )
            inputLeaseDay = ethRegDate.day.toString()
            inputLeaseMonth = ethRegDate.month.toString()
            inputLeaseYear = ethRegDate.year.toString()
            
            // Determine scale based on buildingCategory
            selectedScale = when {
                proj.buildingCategory.contains("ሀ") || proj.buildingCategory.contains("የጭቃ") -> "Small"
                proj.buildingCategory.contains("ለ") -> "Medium"
                else -> "Large"
            }
            
            computedLeaseMilestones = EthiopianCalendar.calculateLeaseMilestones(
                ethRegDate.year, ethRegDate.month, ethRegDate.day, selectedScale
            )
        }
    }

    // List of all active project standard milestones based on start date
    val projectMilestones = remember(projects) {
        projects.flatMap { proj ->
            val startEth = EthiopianCalendar.gregorianToEthiopian(
                java.util.Calendar.getInstance().apply { timeInMillis = proj.timestamp }.get(java.util.Calendar.YEAR),
                java.util.Calendar.getInstance().apply { timeInMillis = proj.timestamp }.get(java.util.Calendar.MONTH) + 1,
                java.util.Calendar.getInstance().apply { timeInMillis = proj.timestamp }.get(java.util.Calendar.DAY_OF_MONTH)
            )
            listOf(
                ProjectMilestone(
                    projectName = proj.name,
                    name = "የመሠረት ግንባታ ማጠናቀቂያ (Foundation Finish)",
                    date = startEth.addDays(60),
                    status = if (proj.scheduleProgressPercent >= 25) "ተጠናቋል" else "በሂደት ላይ",
                    color = AmharicGreen
                ),
                ProjectMilestone(
                    projectName = proj.name,
                    name = "የምሶሶና ጨረሮች ግንባታ (Structural Frame)",
                    date = startEth.addDays(120),
                    status = if (proj.scheduleProgressPercent >= 60) "ተጠናቋል" else "በሂደት ላይ",
                    color = AmharicYellow
                ),
                ProjectMilestone(
                    projectName = proj.name,
                    name = "የግድግዳ ግንባታ (Walling & Blockwork)",
                    date = startEth.addDays(180),
                    status = if (proj.scheduleProgressPercent >= 80) "ተጠናቋል" else "በሂደት ላይ",
                    color = AmharicRed
                ),
                ProjectMilestone(
                    projectName = proj.name,
                    name = "ሙሉ የህንፃ ርክክብ (Final Handover Milestone)",
                    date = startEth.addDays(270),
                    status = if (proj.scheduleProgressPercent == 100) "ተጠናቋል" else "በሂደት ላይ",
                    color = Color(0xFF00A896)
                )
            )
        }
    }

    // Combine all milestones and lease deadlines falling on the currently visible month
    val visibleMilestones = projectMilestones.filter { it.date.year == selectedYear && it.date.month == selectedMonth }
    val visibleLeaseDeadlines = computedLeaseMilestones.filter { it.date.year == selectedYear && it.date.month == selectedMonth }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SteelGray),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(AmharicGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = AmharicYellow)
                }
                Column {
                    Text(
                        text = "የኢትዮጵያ መቁጠሪያና የግንባታ ቀናት መቆጣጠሪያ",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "የፕሮጀክት ማይልስቶኖች እና የሊዝ አዋጅ 721/2011 አስገዳጅ ግንባታ ማጠናቀቂያ ቀኖች መከታተያ ሞዱል",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Split Layout: Calendar grid (Left/Wide) and Details Panel (Right/Narrow)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Calendar Grid Container (Left Side)
            Card(
                modifier = Modifier
                    .weight(1.8f)
                    .heightIn(min = 450.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Month & Year Selector Navigation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (selectedMonth == 1) {
                                selectedMonth = 13
                                selectedYear -= 1
                            } else {
                                selectedMonth -= 1
                            }
                        }) {
                            Icon(Icons.Default.ArrowBackIosNew, contentDescription = "ያለፈው ወር", tint = AmharicYellow, modifier = Modifier.size(16.dp))
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${EthiopianCalendar.MONTH_NAMES[selectedMonth - 1]} $selectedYear ዓ.ም",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val gregRange = getGregorianDateRange(selectedYear, selectedMonth)
                            Text(
                                text = gregRange,
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }

                        IconButton(onClick = {
                            if (selectedMonth == 13) {
                                selectedMonth = 1
                                selectedYear += 1
                            } else {
                                selectedMonth += 1
                            }
                        }) {
                            Icon(Icons.Default.ArrowForwardIos, contentDescription = "የሚቀጥለው ወር", tint = AmharicYellow, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Days of Week Header
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        EthiopianCalendar.WEEKDAYS.forEach { day ->
                            Text(
                                text = day,
                                color = AmharicYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Days Grid
                    val daysInMonth = EthiopianCalendar.getDaysInMonth(selectedYear, selectedMonth)
                    val firstDayWeekday = EthiopianCalendar.getFirstDayWeekday(selectedYear, selectedMonth)
                    
                    // We render rows. Up to 6 rows.
                    var dayCounter = 1
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (row in 0 until 6) {
                            if (dayCounter > daysInMonth) break
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (col in 0 until 7) {
                                    val cellIndex = row * 7 + col
                                    if (cellIndex < firstDayWeekday || dayCounter > daysInMonth) {
                                        // Empty spacer cell
                                        Spacer(modifier = Modifier.weight(1f))
                                    } else {
                                        val currentDayNum = dayCounter
                                        val isSelected = selectedDay == currentDayNum
                                        val isCurrentToday = currentEthDate.year == selectedYear && 
                                                             currentEthDate.month == selectedMonth && 
                                                             currentEthDate.day == currentDayNum
                                        
                                        // Check if this day has any construction milestones or lease deadlines
                                        val dayMilestones = projectMilestones.filter { it.date.year == selectedYear && it.date.month == selectedMonth && it.date.day == currentDayNum }
                                        val dayLease = computedLeaseMilestones.filter { it.date.year == selectedYear && it.date.month == selectedMonth && it.date.day == currentDayNum }
                                        val hasEvents = dayMilestones.isNotEmpty() || dayLease.isNotEmpty()

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .padding(2.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    when {
                                                        isSelected -> AmharicYellow.copy(alpha = 0.25f)
                                                        isCurrentToday -> AmharicGreen.copy(alpha = 0.2f)
                                                        else -> Color.Transparent
                                                    }
                                                )
                                                .border(
                                                    1.dp,
                                                    when {
                                                        isSelected -> AmharicYellow
                                                        isCurrentToday -> AmharicGreen
                                                        else -> Color.Transparent
                                                    },
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable { selectedDay = currentDayNum }
                                                .padding(4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = currentDayNum.toString(),
                                                    color = when {
                                                        isSelected -> AmharicYellow
                                                        isCurrentToday -> AmharicGreen
                                                        else -> Color.White
                                                    },
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected || isCurrentToday) FontWeight.Bold else FontWeight.Normal
                                                )
                                                
                                                if (hasEvents) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    ) {
                                                        if (dayLease.isNotEmpty()) {
                                                            Box(modifier = Modifier.size(4.dp).background(AmharicRed, CircleShape))
                                                        }
                                                        if (dayMilestones.isNotEmpty()) {
                                                            Box(modifier = Modifier.size(4.dp).background(AmharicGreen, CircleShape))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        dayCounter++
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Events List Panel (Right Side)
            Card(
                modifier = Modifier
                    .weight(1.2f)
                    .heightIn(min = 450.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "የዕለቱ የክትትል መረጃ",
                        color = AmharicYellow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "${EthiopianCalendar.MONTH_NAMES[selectedMonth - 1]} $selectedDay, $selectedYear ዓ.ም",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )

                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))

                    val selectedDayMilestones = projectMilestones.filter { it.date.year == selectedYear && it.date.month == selectedMonth && it.date.day == selectedDay }
                    val selectedDayLease = computedLeaseMilestones.filter { it.date.year == selectedYear && it.date.month == selectedMonth && it.date.day == selectedDay }

                    if (selectedDayMilestones.isEmpty() && selectedDayLease.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "በዚህ ቀን የተመዘገበ የጊዜ ገደብ ወይም ማይልስቶን የለም።",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Render Lease Deadlines
                            selectedDayLease.forEach { dead ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = SteelGray.copy(alpha = 0.5f)),
                                    border = BorderStroke(1.dp, AmharicRed.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Gavel, contentDescription = null, tint = AmharicRed, modifier = Modifier.size(12.dp))
                                            Text(dead.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Text(dead.description, color = Color.LightGray, fontSize = 9.sp)
                                        Text("አስገዳጅ ህግ፡ ${dead.lawArticle}", color = AmharicYellow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text("ያለማክበር ቅጣት፡ ${dead.penalty}", color = AmharicRed, fontSize = 9.sp)
                                    }
                                }
                            }

                            // Render Project Milestones
                            selectedDayMilestones.forEach { mile ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = SteelGray.copy(alpha = 0.5f)),
                                    border = BorderStroke(1.dp, AmharicGreen.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Build, contentDescription = null, tint = AmharicGreen, modifier = Modifier.size(12.dp))
                                            Text(mile.projectName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Text(mile.name, color = Color.LightGray, fontSize = 10.sp)
                                        Surface(
                                            color = mile.color.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "ሁኔታ፡ ${mile.status}",
                                                color = mile.color,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
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

        // Section 2: Ethiopian Lease Proclamation No. 721/2011 Automated Deadline Calculator
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = AmharicYellow)
                    Text(
                        text = "የሊዝ አዋጅ 721/2011 የግንባታ ቀነ-ገደብ ማስያ (Lease Deadline Calculator)",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))

                if (projects.isEmpty()) {
                    Text(
                        "ቀነ-ገደብ ለማስላት አስቀድመው ቢያንስ አንድ የግንባታ ፕሮጀክት መመዝገብ ይኖርብዎታል።",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Project Selector dropdown
                        Column(modifier = Modifier.weight(1.5f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("ፕሮጀክት ይምረጡ፡", color = Color.LightGray, fontSize = 11.sp)
                            var expandedProj by remember { mutableStateOf(false) }
                            Box {
                                OutlinedButton(
                                    onClick = { expandedProj = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = selectedProjectForCalc?.name ?: "ፕሮጀክት ምረጥ",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = AmharicYellow,
                                        fontSize = 12.sp
                                    )
                                }
                                DropdownMenu(expanded = expandedProj, onDismissRequest = { expandedProj = false }) {
                                    projects.forEach { proj ->
                                        DropdownMenuItem(
                                            text = { Text(proj.name, fontSize = 12.sp) },
                                            onClick = {
                                                selectedProjectForCalc = proj
                                                expandedProj = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Project scale displaying automatically
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("የግንባታ ደረጃ (ስፋት)፡", color = Color.LightGray, fontSize = 11.sp)
                            val scaleAm = when (selectedScale) {
                                "Small" -> "አነስተኛ (ከ6-18 ወር)"
                                "Medium" -> "መካከለኛ (ከ12-24 ወር)"
                                else -> "ከፍተኛ (ከ18-36 ወር)"
                            }
                            Surface(
                                color = SteelGray,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                                border = BorderStroke(0.5.dp, Color.Gray)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(scaleAm, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Result milestones list
                    Text(
                        "የተሰላ በአዋጅ ቁጥር 721/2011 መሠረት አስገዳጅ ቀናት፡",
                        color = AmharicYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        computedLeaseMilestones.forEach { milestone ->
                            val isLeapMonth = milestone.date.month == 13
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SteelGray),
                                border = BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1.8f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(milestone.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(milestone.description, color = Color.LightGray, fontSize = 10.sp)
                                        Text("አንቀጽ፡ ${milestone.lawArticle}", color = AmharicGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                    
                                    Column(modifier = Modifier.weight(1.2f), horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = milestone.date.format(),
                                            color = AmharicYellow,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.End
                                        )
                                        Text(
                                            text = "Gregorian: ${ethiopianToGregorianFormatted(milestone.date)}",
                                            color = Color.Gray,
                                            fontSize = 9.sp,
                                            textAlign = TextAlign.End
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Button(
                                            onClick = {
                                                selectedYear = milestone.date.year
                                                selectedMonth = milestone.date.month
                                                selectedDay = milestone.date.day
                                                Toast.makeText(context, "${milestone.name} በመቁጠሪያው ላይ ተመርጧል!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = AmharicGreen),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            modifier = Modifier.height(24.dp)
                                        ) {
                                            Text("መቁጠሪያው ላይ አሳይ", fontSize = 8.sp)
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
}

// Data models for the Calendar events
data class ProjectMilestone(
    val projectName: String,
    val name: String,
    val date: EthiopianDate,
    val status: String,
    val color: Color
)

// Helper function to format Ethiopian Date to Gregorian date string
fun ethiopianToGregorianFormatted(date: EthiopianDate): String {
    val (y, m, d) = EthiopianCalendar.ethiopianToGregorianDate(date.year, date.month, date.day)
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    return "$months[m - 1]} $d, $y"
}

// Helper function to get Gregorian range for an Ethiopian month
fun getGregorianDateRange(ethYear: Int, ethMonth: Int): String {
    val (yStart, mStart, dStart) = EthiopianCalendar.ethiopianToGregorianDate(ethYear, ethMonth, 1)
    val daysInMonth = EthiopianCalendar.getDaysInMonth(ethYear, ethMonth)
    val (yEnd, mEnd, dEnd) = EthiopianCalendar.ethiopianToGregorianDate(ethYear, ethMonth, daysInMonth)
    
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    return if (mStart == mEnd) {
        "${months[mStart - 1]} $dStart - $dEnd, $yStart"
    } else {
        "${months[mStart - 1]} $dStart, $yStart - ${months[mEnd - 1]} $dEnd, $yEnd"
    }
}
