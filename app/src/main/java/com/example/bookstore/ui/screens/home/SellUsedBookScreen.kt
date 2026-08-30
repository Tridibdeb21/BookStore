package com.example.bookstore.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.bookstore.viewmodel.UsedListingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellUsedBookScreen(
    bookId: String,
    bookTitle: String,
    bookCoverUrl: String,
    onBack: () -> Unit,
    viewModel: UsedListingViewModel = viewModel()
) {
    var askingPrice by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("Good") }
    var expandedConditionDropdown by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val conditions = listOf("Like New", "Good", "Acceptable")
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sell Your Copy", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Book Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = bookCoverUrl,
                        contentDescription = "Cover Image",
                        modifier = Modifier
                            .size(72.dp)
                            .padding(end = 16.dp)
                    )
                    Column {
                        Text(
                            text = bookTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Listing for secondhand sale",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Condition Field
            ExposedDropdownMenuBox(
                expanded = expandedConditionDropdown,
                onExpandedChange = { expandedConditionDropdown = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = condition,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Book Condition") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedConditionDropdown) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedConditionDropdown,
                    onDismissRequest = { expandedConditionDropdown = false }
                ) {
                    conditions.forEach { cond ->
                        DropdownMenuItem(
                            text = { Text(cond) },
                            onClick = {
                                condition = cond
                                expandedConditionDropdown = false
                            }
                        )
                    }
                }
            }

            // Price Field
            OutlinedTextField(
                value = askingPrice,
                onValueChange = { askingPrice = it },
                label = { Text("Asking Price ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Description Field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description / Notes (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val priceDouble = askingPrice.toDoubleOrNull()
                    if (priceDouble == null || priceDouble <= 0.0) {
                        Toast.makeText(context, "Please enter a valid asking price.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSubmitting = true
                    viewModel.createListing(
                        bookId = bookId,
                        bookTitle = bookTitle,
                        bookCoverUrl = bookCoverUrl,
                        askingPrice = priceDouble,
                        condition = condition,
                        description = description,
                        onSuccess = {
                            isSubmitting = false
                            Toast.makeText(context, "Listing created successfully!", Toast.LENGTH_SHORT).show()
                            onBack()
                        },
                        onFailure = { error ->
                            isSubmitting = false
                            Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSubmitting && askingPrice.isNotBlank()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Submit Listing", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
