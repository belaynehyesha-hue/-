package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodesDashboardComponent(
    modifier: Modifier = Modifier,
    viewModel: com.example.ui.viewmodel.ConstructionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Observe offline mode, bookmarked standard IDs and standard notes persistently from ViewModel
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()
    val bookmarkedIdsSet by viewModel.bookmarkedStandardIds.collectAsState()
    val standardNotes by viewModel.standardNotes.collectAsState()

    // Search and filter state
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ሁሉም") }
    var recentSearches by remember { mutableStateOf(SearchHistoryUtils.getRecentSearches(context)) }
    
    // UI Expand/Collapse states for documents
    val expandedItemIds = remember { mutableStateMapOf<String, Boolean>() }

    val categories = listOf("ሁሉም", "EBCS", "ESEN", "BRITISH", "AASHTO", "AACRA", "BATCODA", "FIDIC", "PROCLAMATION", "INTERNATIONAL")

    // Filtered items based on search and category
    val filteredItems = remember(searchQuery, selectedCategory) {
        allLibraryItems.filter { item ->
            val matchesCategory = selectedCategory == "ሁሉም" || item.category == selectedCategory
            val matchesSearch = if (searchQuery.trim().isEmpty()) {
                true
            } else {
                item.titleAm.contains(searchQuery, ignoreCase = true) ||
                item.titleEn.contains(searchQuery, ignoreCase = true) ||
                item.description.contains(searchQuery, ignoreCase = true) ||
                item.regionalApplication.contains(searchQuery, ignoreCase = true) ||
                item.keyClauses.any { it.contains(searchQuery, ignoreCase = true) } ||
                item.complianceCheckpoints.any { it.contains(searchQuery, ignoreCase = true) }
            }
            matchesCategory && matchesSearch
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("codes_dashboard_component"),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(AmharicYellow.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        tint = AmharicYellow,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "የህንፃ ኮዶችና አዋጆች ማጣቀሻ ማዕከል",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "በኢትዮጵያ ተፈጻሚ የሆኑ መመሪያዎችና የተገዢነት ድንጋጌዎች መፈለጊያ",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
            }

            // Offline Status & Toggle Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SteelGray.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (isOfflineMode) AmharicRed.copy(alpha = 0.3f) else AmharicGreen.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (isOfflineMode) AmharicRed else AmharicGreen, CircleShape)
                        )
                        Column {
                            Text(
                                text = if (isOfflineMode) "ከመስመር ውጭ የአገልግሎት ሁነታ (Offline Mode)" else "ከመስመር በላይ የአገልግሎት ሁነታ (Online Mode)",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ሁሉም የEBCS መመሪያዎችና ቼክሊስቶች ከመስመር ውጭ ዝግጁ ናቸው ✅",
                                color = Color.LightGray,
                                fontSize = 9.sp
                            )
                        }
                    }
                    
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isOfflineMode) AmharicRed.copy(alpha = 0.2f) else AmharicGreen.copy(alpha = 0.2f))
                            .border(1.dp, if (isOfflineMode) AmharicRed else AmharicGreen, RoundedCornerShape(20.dp))
                            .clickable {
                                val newVal = !isOfflineMode
                                viewModel.toggleOfflineMode(newVal)
                                if (newVal) {
                                    Toast.makeText(context, "ከመስመር ውጭ የአገልግሎት ሁነታ ገባሪ ሆኗል! 💾", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "የመስመር ላይ ሁነታ ተመልሷል! 🌐", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isOfflineMode) Icons.Default.WifiOff else Icons.Default.Wifi,
                            contentDescription = null,
                            tint = if (isOfflineMode) AmharicRed else AmharicGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (isOfflineMode) "ከመስመር ውጭ" else "መስመር ላይ",
                            color = if (isOfflineMode) AmharicRed else AmharicGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Search input field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("በቁልፍ ቃል (ለምሳሌ፡ C25, Clause 20, አዋጅ, ኮንክሪት)...", color = Color.Gray, fontSize = 12.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("codes_search_input"),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AmharicYellow, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "አጽዳ", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AmharicYellow,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                    focusedContainerColor = SteelGray.copy(alpha = 0.4f),
                    unfocusedContainerColor = SteelGray.copy(alpha = 0.2f)
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
                ),
                shape = RoundedCornerShape(10.dp)
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
                                    .testTag("recent_search_chip_$q"),
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

            // Category Horizontal Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedCategory = cat },
                        color = if (isSelected) AmharicYellow else SteelGray.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, if (isSelected) AmharicYellow else Color.Transparent)
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) SteelGray else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Search Results Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (searchQuery.trim().isEmpty()) "ሁሉም መመሪያዎች (${filteredItems.size})" else "የፍለጋ ውጤቶች (${filteredItems.size} ተገኝተዋል)",
                    color = AmharicYellow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                if (bookmarkedIdsSet.isNotEmpty()) {
                    Surface(
                        color = AmharicGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.clickable {
                            // Quick action to show only bookmarked or reset search
                            Toast.makeText(context, "የተመረጡ ማስታወሻዎች ምልክት ተደርጎባቸዋል", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = AmharicYellow, modifier = Modifier.size(10.dp))
                            Text("ማስታወሻዎች", color = AmharicYellow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Document Cards List (Scrollable layout or list items)
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.animateContentSize()
            ) {
                if (filteredItems.isEmpty()) {
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
                                imageVector = Icons.Default.FindInPage,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "ምንም የሚዛመድ የግንባታ ኮድ ወይም አንቀጽ አልተገኘም።",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    filteredItems.take(5).forEach { item ->
                        val isExpanded = expandedItemIds[item.id] == true
                        val isBookmarked = bookmarkedIdsSet.contains(item.id)

                        CodeLibraryItemRow(
                            item = item,
                            searchQuery = searchQuery,
                            isExpanded = isExpanded,
                            isBookmarked = isBookmarked,
                            savedNote = standardNotes[item.id] ?: "",
                            onSaveNote = { note -> viewModel.saveStandardNote(item.id, note) },
                            onToggleExpand = { 
                                expandedItemIds[item.id] = !isExpanded 
                                if (!isExpanded && searchQuery.trim().isNotEmpty()) {
                                    SearchHistoryUtils.saveRecentSearch(context, searchQuery)
                                    recentSearches = SearchHistoryUtils.getRecentSearches(context)
                                }
                            },
                            onToggleBookmark = {
                                viewModel.toggleBookmarkStandard(item.id)
                                if (!isBookmarked) {
                                    Toast.makeText(context, "አንቀጹ ወደ ተመረጡ መመሪያዎች ተጨምሯል!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onCopyText = { text ->
                                clipboardManager.setText(AnnotatedString(text))
                                Toast.makeText(context, "ጽሑፉ በስኬት ኮፒ ተደርጓል!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    if (filteredItems.size > 5) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "ሁሉንም መመሪያዎች ለማየት ወደ 'የሕንፃ ኮድ' ታብ ይሂዱ", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SteelGray),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text("ቀሪዎቹን ${filteredItems.size - 5} መመሪያዎች በሙሉ እይ", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CodeLibraryItemRow(
    item: CodeLibraryItem,
    searchQuery: String,
    isExpanded: Boolean,
    isBookmarked: Boolean,
    savedNote: String,
    onSaveNote: (String) -> Unit,
    onToggleExpand: () -> Unit,
    onToggleBookmark: () -> Unit,
    onCopyText: (String) -> Unit
) {
    // Determine category theme color
    val badgeColor = when (item.category) {
        "EBCS" -> AmharicGreen
        "ESEN" -> Color(0xFF0077B6)
        "FIDIC" -> AmharicRed
        "PROCLAMATION" -> GoldAccent
        "AACRA", "AASHTO" -> ConstructionTeal
        else -> SteelGray
    }

    val badgeTextColor = if (item.category == "PROCLAMATION") SteelGray else Color.White

    // Check if search query specifically matches any key clauses or checkpoints
    val matchingClauses = remember(searchQuery, item.keyClauses) {
        if (searchQuery.trim().isEmpty()) emptyList() else {
            item.keyClauses.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    val matchingCheckpoints = remember(searchQuery, item.complianceCheckpoints) {
        if (searchQuery.trim().isEmpty()) emptyList() else {
            item.complianceCheckpoints.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() }
            .testTag("code_row_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = SteelGray.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.5.dp, if (isExpanded) AmharicYellow.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Badge, Title, Bookmark Star, Expand Arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category Badge
                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = item.category,
                        color = badgeTextColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Title
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = highlightSearchQuery(item.titleAm, searchQuery),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = if (isExpanded) 3 else 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = highlightSearchQuery(item.titleEn, searchQuery),
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        maxLines = if (isExpanded) 2 else 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Bookmark Icon Button
                IconButton(
                    onClick = {
                        onToggleBookmark()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "ምልክት አድርግ",
                        tint = if (isBookmarked) AmharicYellow else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Expand/Collapse Icon
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = AmharicYellow,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Quick Highlight for Match
            if (!isExpanded && (matchingClauses.isNotEmpty() || matchingCheckpoints.isNotEmpty())) {
                Surface(
                    color = AmharicYellow.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(0.5.dp, AmharicYellow.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = AmharicYellow,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = buildAnnotatedString {
                                append("ተዛማጅ አንቀጽ፡ ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    val matchedText = matchingClauses.firstOrNull() ?: matchingCheckpoints.first()
                                    append(matchedText.take(60) + if (matchedText.length > 60) "..." else "")
                                }
                            },
                            color = Color.White,
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Expanded Content Details
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))

                    // Description with Quote Accent Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .heightIn(min = 30.dp)
                                .background(badgeColor)
                        )
                        Text(
                            text = highlightSearchQuery(item.description, searchQuery),
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }

                    // Key Clauses List
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = AmharicYellow, modifier = Modifier.size(12.dp))
                            Text(
                                text = "ቁልፍ አንቀጾችና ውሳኔዎች (Key Clauses)",
                                color = AmharicYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        item.keyClauses.forEach { clause ->
                            val isMatch = matchingClauses.contains(clause)
                            Surface(
                                color = if (isMatch) AmharicYellow.copy(alpha = 0.15f) else Color.Transparent,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = highlightSearchQuery("• $clause", searchQuery),
                                        color = if (isMatch) Color.White else Color.LightGray,
                                        fontSize = 10.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { onCopyText(clause) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "ቅዳ",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Compliance Checkpoints
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = AmharicGreen, modifier = Modifier.size(12.dp))
                            Text(
                                text = "የተገዢነትና የጥራት ነጥቦች (Compliance Audit Checkpoints)",
                                color = AmharicGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        item.complianceCheckpoints.forEach { checkpoint ->
                            val isMatch = matchingCheckpoints.contains(checkpoint)
                            Surface(
                                color = if (isMatch) AmharicGreen.copy(alpha = 0.15f) else Color.Transparent,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = AmharicGreen,
                                            modifier = Modifier.size(12.dp).padding(top = 2.dp)
                                        )
                                        Text(
                                            text = highlightSearchQuery(checkpoint, searchQuery),
                                            color = if (isMatch) Color.White else Color.LightGray,
                                            fontSize = 10.sp
                                        )
                                    }
                                    IconButton(
                                        onClick = { onCopyText(checkpoint) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "ቅዳ",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Regional Application Box
                    Surface(
                        color = SteelGray,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                tint = AmharicYellow,
                                modifier = Modifier.size(14.dp)
                            )
                            Column {
                                Text(
                                    text = "ተፈጻሚነት ክልል (Regional Scope)",
                                    color = AmharicYellow,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = highlightSearchQuery(item.regionalApplication, searchQuery),
                                    color = Color.LightGray,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Offline Personal Notes Box
                    var noteText by remember(savedNote) { mutableStateOf(savedNote) }
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SteelGray),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
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
                                        imageVector = Icons.Default.EditNote,
                                        contentDescription = null,
                                        tint = AmharicYellow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "የኢንጂነር ከመስመር ውጭ ማስታወሻ (Engineer Offline Note)",
                                        color = AmharicYellow,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (savedNote.isNotEmpty()) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = "በመሳሪያው ላይ ተቀምጧል",
                                        tint = AmharicGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                placeholder = { 
                                    Text(
                                        text = "በዚህ መመሪያ ወይም በግንባታ ቦታው ላይ ያዩትን ከመስመር ውጭ ማስታወሻ እዚህ ይጻፉ...", 
                                        color = Color.Gray, 
                                        fontSize = 10.sp
                                    ) 
                                },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3,
                                textStyle = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontSize = 11.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AmharicYellow,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                    cursorColor = AmharicYellow
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        onSaveNote(noteText)
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    colors = ButtonDefaults.textButtonColors(contentColor = AmharicYellow)
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ማስታወሻ አስቀምጥ (Save)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Utility function to build annotated strings that highlight matching search queries
 */
@Composable
fun highlightSearchQuery(text: String, query: String): AnnotatedString {
    if (query.trim().isEmpty() || !text.contains(query, ignoreCase = true)) {
        return AnnotatedString(text)
    }

    return remember(text, query) {
        buildAnnotatedString {
            var startIdx = 0
            while (startIdx < text.length) {
                val matchIdx = text.indexOf(query, startIdx, ignoreCase = true)
                if (matchIdx == -1) {
                    append(text.substring(startIdx))
                    break
                } else {
                    append(text.substring(startIdx, matchIdx))
                    withStyle(
                        style = SpanStyle(
                            background = AmharicYellow.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    ) {
                        append(text.substring(matchIdx, matchIdx + query.length))
                    }
                    startIdx = matchIdx + query.length
                }
            }
        }
    }
}
