package com.example.bookstore.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookstore.model.Book
import com.example.bookstore.viewmodel.GeminiUiState
import com.example.bookstore.viewmodel.GeminiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiRecommendScreen(
    viewModel: GeminiViewModel,
    books: List<Book>,
    onBack: () -> Unit
) {
    val recommendState by viewModel.recommendState.collectAsState()
    var userPrompt by remember { mutableStateOf("") }
    
    val quickPrompts = listOf(
        "A mind-bending mystery",
        "An epic sci-fi adventure",
        "Heartwarming romance",
        "A fascinating historical tale",
        "Self-improvement & mindset"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI icon",
                            tint = Color(0xFF8E24AA),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            "AI Book Recommendations",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Intro Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Describe your mood or what you want to read",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Our custom AI will search our catalog to recommend the best matches for you.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            // Quick prompts row
            Text(
                "Quick Ideas:",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickPrompts) { prompt ->
                    SuggestionChip(
                        onClick = { userPrompt = prompt },
                        label = { Text(prompt) }
                    )
                }
            }

            // Text Input
            OutlinedTextField(
                value = userPrompt,
                onValueChange = { userPrompt = it },
                label = { Text("What kind of book are you looking for?") },
                placeholder = { Text("e.g. a thrilling mystery with unexpected twists") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                maxLines = 3,
                trailingIcon = {
                    if (userPrompt.isNotEmpty()) {
                        IconButton(onClick = { userPrompt = "" }) {
                            Text("Clear")
                        }
                    }
                }
            )

            // Submit Button
            Button(
                onClick = { viewModel.getBookRecommendations(userPrompt, books) },
                enabled = userPrompt.isNotBlank() && recommendState !is GeminiUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search icon")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Get AI Recommendations", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // State Display
            when (val state = recommendState) {
                is GeminiUiState.Idle -> {
                    // Show prompt suggestion placeholder
                }
                is GeminiUiState.Loading -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Gemini is searching catalog...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                is GeminiUiState.Success -> {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI",
                                        tint = Color(0xFF8E24AA)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Recommended For You",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = state.text,
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                is GeminiUiState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "AI Recommendation Failed",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}
