package com.example.bookstore.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import coil.compose.AsyncImage
import com.example.bookstore.model.Book
import com.example.bookstore.ui.components.BookCard
import com.example.bookstore.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onBookClick: (String) -> Unit = {},
    onAiRecommendClick: () -> Unit = {},
    onAiChatClick: () -> Unit = {}
) {
    val books by viewModel.books.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val selectedCategory by viewModel.selectedCategoryId.collectAsState()
    var selectedMood by remember { mutableStateOf<String?>(null) }
    var showMoodQuiz by remember { mutableStateOf(false) }
    var showSurpriseAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(showSurpriseAnimation) {
        if (showSurpriseAnimation && books.isNotEmpty()) {
            delay(1500)
            val randomBook = books.random()
            showSurpriseAnimation = false
            onBookClick(randomBook.id)
        }
    }

    val filteredBooks = books.filter {
        (searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true) || it.author.contains(searchQuery, ignoreCase = true)) &&
        (selectedCategory == null || it.categoryId == selectedCategory) &&
        (selectedMood == null || it.moodTags.any { tag -> tag.equals(selectedMood, ignoreCase = true) })
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "BOOKSTORE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Discover",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    // Surprise Me button
                    IconButton(
                        onClick = { if (books.isNotEmpty()) showSurpriseAnimation = true },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Surprise Me",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search bar — flat with border
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search titles, authors…", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // ── Mood Match + AI Feature Row ────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mood Match
                Surface(
                    modifier = Modifier.weight(1f).clickable { showMoodQuiz = true },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("😌", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (selectedMood != null) selectedMood!! else "Mood Match",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = if (selectedMood != null) "Tap to change" else "Find by feeling",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // AI Recommend
                Surface(
                    modifier = Modifier.weight(1f).clickable { onAiRecommendClick() },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "AI Picks",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = "Curated for you",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // BookBot Chat banner — full width
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clickable { onAiChatClick() },
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                )
                            ),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 13.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("BookBot AI Chat", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                            Text("Ask me anything about books →", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Book of the Day ───────────────────────────────────────
            val bookOfDay = books.find { it.isBookOfDay }
            if (bookOfDay != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clickable { onBookClick(bookOfDay.id) },
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                            AsyncImage(
                                model = bookOfDay.imageUrl,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp, 100.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Text(
                                    "🏆  Book of the Day",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Text(bookOfDay.title, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface)
                            Text("by ${bookOfDay.author}", fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("$${bookOfDay.price}", fontWeight = FontWeight.Black, fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // ── Flash Sale Banner ─────────────────────────────────────
            val activeFlashSaleBook = books.find {
                it.flashSalePrice != null && it.flashSaleExpiry != null && it.flashSaleExpiry > System.currentTimeMillis()
            }
            var timeRemainingText by remember(activeFlashSaleBook) { mutableStateOf("") }
            if (activeFlashSaleBook != null) {
                LaunchedEffect(activeFlashSaleBook) {
                    while (true) {
                        val diff = activeFlashSaleBook.flashSaleExpiry!! - System.currentTimeMillis()
                        if (diff <= 0) { timeRemainingText = "Expired"; break }
                        val h = diff / (1000 * 60 * 60)
                        val m = (diff % (1000 * 60 * 60)) / (1000 * 60)
                        val s = (diff % (1000 * 60)) / 1000
                        timeRemainingText = "%02d:%02d:%02d".format(h, m, s)
                        delay(1000)
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clickable { onBookClick(activeFlashSaleBook.id) },
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = activeFlashSaleBook.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.size(52.dp, 70.dp).clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("⚡  FLASH SALE", fontWeight = FontWeight.Black, fontSize = 10.sp,
                                letterSpacing = 2.sp, color = MaterialTheme.colorScheme.error)
                            Text(activeFlashSaleBook.title, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                                maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onErrorContainer)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("$${activeFlashSaleBook.flashSalePrice}", fontWeight = FontWeight.Black,
                                    fontSize = 16.sp, color = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("$${activeFlashSaleBook.price}", fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textDecoration = TextDecoration.LineThrough)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = timeRemainingText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // ── Categories ────────────────────────────────────────────
            Row(
                modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 4.dp, end = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Categories",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)
            ) {
                item {
                    CategoryChip(
                        categoryName = "All",
                        isSelected = selectedCategory == null,
                        onClick = { viewModel.selectCategory(null) }
                    )
                }
                items(categories) { category ->
                    CategoryChip(
                        categoryName = category.name,
                        isSelected = selectedCategory == category.id,
                        onClick = { viewModel.selectCategory(category.id) }
                    )
                }
            }

            // ── Book Grid ─────────────────────────────────────────────
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 2.5.dp)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredBooks) { book ->
                        BookCard(book = book, onClick = { onBookClick(book.id) })
                    }
                }
            }
        }
    }

    // ── Mood Quiz Dialog ──────────────────────────────────────────
    if (showMoodQuiz) {
        val moods = listOf("Happy", "Sad", "Adventurous", "Romantic", "Mystery", "Sci-Fi", "Relaxing", "Dark", "Motivational")
        AlertDialog(
            onDismissRequest = { showMoodQuiz = false },
            title = { Text("How are you feeling?", fontWeight = FontWeight.Bold) },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(moods) { mood ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedMood == mood) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                selectedMood = mood
                                showMoodQuiz = false
                            }
                        ) {
                            Text(
                                text = mood,
                                fontWeight = if (selectedMood == mood) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                color = if (selectedMood == mood) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (selectedMood != null) {
                    TextButton(onClick = { selectedMood = null; showMoodQuiz = false }) {
                        Text("Clear Filter", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showMoodQuiz = false }) { Text("Close") }
            }
        )
    }

    // ── Surprise animation dialog ─────────────────────────────────
    if (showSurpriseAnimation) {
        Dialog(onDismissRequest = {}) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(200.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(56.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Finding magic…", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun CategoryChip(categoryName: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(20.dp),
        border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null
    ) {
        Text(
            text = categoryName,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}
