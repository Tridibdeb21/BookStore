package com.example.bookstore.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.bookstore.model.Book
import com.example.bookstore.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFlashSaleScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val books by viewModel.books.collectAsState()
    val context = LocalContext.current

    var selectedBook by remember { mutableStateOf<Book?>(null) }
    var flashPrice by remember { mutableStateOf("") }
    var durationHours by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flash Sales & Featured Books", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(books, key = { it.id }) { book ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = book.imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .padding(end = 12.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(book.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Author: ${book.author}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (book.isBookOfDay) {
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text("Book of the Day", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                    )
                                }
                                if (book.flashSalePrice != null && book.flashSaleExpiry != null && book.flashSaleExpiry > System.currentTimeMillis()) {
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text("Flash Sale: $${book.flashSalePrice}", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                selectedBook = book
                                flashPrice = book.flashSalePrice?.toString() ?: ""
                                durationHours = ""
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Manage", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    if (selectedBook != null) {
        val book = selectedBook!!
        AlertDialog(
            onDismissRequest = { selectedBook = null },
            title = { Text("Feature/Flash Sale Panel", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Configuring: \"${book.title}\"", fontWeight = FontWeight.SemiBold)
                    
                    // Book of the Day Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Set as Book of the Day", fontWeight = FontWeight.Medium)
                        Switch(
                            checked = book.isBookOfDay,
                            onCheckedChange = { isChecked ->
                                viewModel.setBookOfDay(book.id, isChecked)
                                selectedBook = book.copy(isBookOfDay = isChecked)
                                Toast.makeText(context, "Updated Book of the Day Status", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    HorizontalDivider()

                    Text("Flash Sale Configuration", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    OutlinedTextField(
                        value = flashPrice,
                        onValueChange = { flashPrice = it },
                        label = { Text("Flash Sale Price ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = durationHours,
                        onValueChange = { durationHours = it },
                        label = { Text("Duration (Hours)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Save Button
                    Button(
                        onClick = {
                            val priceDouble = flashPrice.toDoubleOrNull()
                            val hoursInt = durationHours.toIntOrNull()
                            if (priceDouble == null || priceDouble <= 0.0 || hoursInt == null || hoursInt <= 0) {
                                Toast.makeText(context, "Invalid input values.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val expiryTimestamp = System.currentTimeMillis() + (hoursInt * 60 * 60 * 1000L)
                            viewModel.setFlashSale(book.id, priceDouble, expiryTimestamp)
                            Toast.makeText(context, "Flash sale set successfully!", Toast.LENGTH_SHORT).show()
                            selectedBook = null
                        }
                    ) {
                        Text("Save")
                    }

                    // Clear Button (if active)
                    if (book.flashSalePrice != null) {
                        OutlinedButton(
                            onClick = {
                                viewModel.setFlashSale(book.id, null, null)
                                Toast.makeText(context, "Flash sale cleared.", Toast.LENGTH_SHORT).show()
                                selectedBook = null
                            }
                        ) {
                            Text("Clear Sale")
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedBook = null }) {
                    Text("Close")
                }
            }
        )
    }
}
