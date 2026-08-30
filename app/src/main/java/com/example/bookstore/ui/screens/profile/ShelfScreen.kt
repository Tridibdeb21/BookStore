package com.example.bookstore.ui.screens.profile

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.bookstore.model.ShelfItem
import com.example.bookstore.viewmodel.ShelfViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelfScreen(
    onBack: () -> Unit,
    onBookClick: (String) -> Unit = {},
    viewModel: ShelfViewModel = viewModel()
) {
    val shelfItems by viewModel.shelfItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val categories = listOf("All", "To Read", "Reading", "Finished")

    val filteredItems = remember(shelfItems, selectedTab) {
        when (selectedTab) {
            1 -> shelfItems.filter { it.status == "To Read" }
            2 -> shelfItems.filter { it.status == "Reading" }
            3 -> shelfItems.filter { it.status == "Finished" }
            else -> shelfItems
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "My Personal Library 📚",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${shelfItems.size} Total Books trackable",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Modern Pill-style Filter Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                categories.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    val count = when (index) {
                        1 -> shelfItems.count { it.status == "To Read" }
                        2 -> shelfItems.count { it.status == "Reading" }
                        3 -> shelfItems.count { it.status == "Finished" }
                        else -> shelfItems.size
                    }

                    Tab(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                    ) {
                        Text(
                            text = "$title ($count)",
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 3.dp)
                }
            } else if (filteredItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkBorder,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Books Here Yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Add books from the detail screen or cart to organize your shelf!",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        ShelfCard(
                            item = item,
                            onStatusChange = { newStatus ->
                                viewModel.updateShelfStatus(item.bookId, newStatus)
                            },
                            onDecrypt = { viewModel.decryptNote(item.noteEncrypted) },
                            onBookClick = { onBookClick(item.bookId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShelfCard(
    item: ShelfItem,
    onStatusChange: (String) -> Unit,
    onDecrypt: () -> String,
    onBookClick: () -> Unit
) {
    var expandedStatusMenu by remember { mutableStateOf(false) }
    var revealedNote by remember { mutableStateOf<String?>(null) }

    val statusColor = when (item.status) {
        "Finished" -> Color(0xFF10B981) // Emerald Green
        "Reading" -> Color(0xFF8B5CF6)  // Electric Purple
        else -> Color(0xFFF59E0B)       // Warm Amber
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBookClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Book Cover with Soft Shadow Accent
                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    AsyncImage(
                        model = item.coverUrl,
                        contentDescription = item.title,
                        modifier = Modifier
                            .size(75.dp, 110.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "by ${item.author.ifBlank { "Unknown Author" }}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Dynamic Status Selector Chip
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = statusColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f)),
                        modifier = Modifier.clickable { expandedStatusMenu = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${item.status} ▾",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = statusColor
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expandedStatusMenu,
                        onDismissRequest = { expandedStatusMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("To Read 📌", fontWeight = FontWeight.Medium) },
                            onClick = {
                                onStatusChange("To Read")
                                expandedStatusMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Reading 📖", fontWeight = FontWeight.Medium) },
                            onClick = {
                                onStatusChange("Reading")
                                expandedStatusMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Finished 🎉", fontWeight = FontWeight.Medium) },
                            onClick = {
                                onStatusChange("Finished")
                                expandedStatusMenu = false
                            }
                        )
                    }
                }
            }

            // Encrypted Time Capsule Note Card Section
            if (item.noteEncrypted.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(14.dp))

                if (item.status == "Finished") {
                    if (revealedNote == null) {
                        Button(
                            onClick = { revealedNote = onDecrypt() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reveal Time Capsule Note 🔓", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.LockOpen, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        "Time Capsule Note Unlocked",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "\"${revealedNote}\"",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Time Capsule Note Encrypted 🔒 — Mark as Finished to decrypt!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
