package com.example.bookstore.ui.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.bookstore.viewmodel.AdminViewModel
import com.example.bookstore.model.Book
import com.example.bookstore.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageBooksScreen(bookId: String?, onBack: () -> Unit, viewModel: AdminViewModel = viewModel()) {
    val books by viewModel.books.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val status by viewModel.addBookStatus.collectAsState()

    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf("") }
    var coverUrl by remember { mutableStateOf("") }
    var previewUrls by remember { mutableStateOf<List<String>>(List(5) { "" }) }
    var pdfUrl by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(bookId, books, categories) {
        if (bookId != null && books.isNotEmpty()) {
            val bookToEdit = books.find { it.id == bookId }
            if (bookToEdit != null && title.isEmpty()) { 
                title = bookToEdit.title
                author = bookToEdit.author
                price = bookToEdit.price.toString()
                description = bookToEdit.description
                categoryId = bookToEdit.categoryId
                coverUrl = bookToEdit.imageUrl
                val currentPreviews = bookToEdit.previewImages
                previewUrls = List(5) { index -> if (index < currentPreviews.size) currentPreviews[index] else "" }
                pdfUrl = bookToEdit.pdfUrl
            }
        } else if (bookId == null && categoryId.isEmpty() && categories.isNotEmpty()) {
            categoryId = categories.first().id
        }
    }

    // Remove launchers as we are using URLs now

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (bookId == null) "Add New Book" else "Edit Book") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Book Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = author,
                onValueChange = { author = it },
                label = { Text("Author") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price (e.g. 19.99)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                val selectedCat = categories.find { it.id == categoryId }?.name ?: "Select Category"
                OutlinedTextField(
                    value = selectedCat,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = {
                                categoryId = cat.id
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("Book Assets (External URLs)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = coverUrl,
                onValueChange = { coverUrl = it },
                label = { Text("Cover Image URL (Direct link required)") },
                placeholder = { Text("https://example.com/image.jpg") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = { Text("Make sure the link ends with .jpg, .png, etc.") }
            )
            if (coverUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = coverUrl, 
                    contentDescription = "Cover Preview", 
                    modifier = Modifier.size(100.dp, 140.dp).align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Crop,
                    error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Book)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Preview Image URLs (Up to 5)", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            previewUrls.forEachIndexed { index, url ->
                OutlinedTextField(
                    value = url,
                    onValueChange = { newUrl ->
                        val newList = previewUrls.toMutableList()
                        newList[index] = newUrl
                        previewUrls = newList
                    },
                    label = { Text("Preview Image ${index + 1}") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    singleLine = true
                )
            }
            
            if (previewUrls.any { it.isNotEmpty() }) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(previewUrls.filter { it.isNotEmpty() }) { url ->
                        AsyncImage(model = url, contentDescription = "Preview", modifier = Modifier.size(80.dp, 120.dp), contentScale = ContentScale.Crop)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = pdfUrl,
                onValueChange = { pdfUrl = it },
                label = { Text("Preview PDF URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    viewModel.saveBook(
                        bookId, title, author, price, description, 
                        coverUrl, previewUrls.filter { it.isNotBlank() }, pdfUrl, categoryId
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(if (bookId == null) "Save Book" else "Save Changes", fontWeight = FontWeight.Bold)
            }
            
            if (status != null) {
                Spacer(modifier = Modifier.height(16.dp))
                if (status!!.startsWith("Error")) {
                    AlertDialog(
                        onDismissRequest = { /* Handle dismiss */ },
                        title = { Text("Save Error") },
                        text = { Text(status!!) },
                        confirmButton = {
                            Button(onClick = { viewModel.clearAddBookStatus() }) {
                                Text("Dismiss")
                            }
                        }
                    )
                } else {
                    Text(
                        text = status!!,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}
