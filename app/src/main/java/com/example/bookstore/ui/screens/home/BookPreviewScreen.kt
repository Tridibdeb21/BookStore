package com.example.bookstore.ui.screens.home

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.bookstore.model.Book
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookPreviewScreen(bookId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    var book by remember { mutableStateOf<Book?>(null) }
    var pdfFile by remember { mutableStateOf<File?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(bookId) {
        try {
            val doc = FirebaseFirestore.getInstance().collection("books").document(bookId).get().await()
            book = doc.toObject(Book::class.java)
            
            val pdfUrl = book?.pdfUrl
            if (!pdfUrl.isNullOrBlank()) {
                withContext(Dispatchers.IO) {
                    val file = File(context.cacheDir, "preview_$bookId.pdf")
                    if (!file.exists()) {
                        val url = URL(pdfUrl)
                        val connection = url.openConnection()
                        connection.connect()
                        val input = connection.getInputStream()
                        val output = FileOutputStream(file)
                        input.use { i -> output.use { o -> i.copyTo(o) } }
                    }
                    pdfFile = file
                }
            } else if (book?.previewImages?.isEmpty() == true) {
                error = "No preview available for this book."
            }
        } catch (e: Exception) {
            error = "Failed to load preview: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book?.title ?: "Book Preview", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(MaterialTheme.colorScheme.surfaceVariant)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null) {
                Text(error!!, modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
            } else if (pdfFile != null) {
                PdfViewer(pdfFile!!)
            } else if (book != null && book!!.previewImages.isNotEmpty()) {
                ImagePreviewList(book!!.previewImages)
            }
        }
    }
}

@Composable
fun PdfViewer(file: File) {
    val pages = remember(file) {
        try {
            val renderer = PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY))
            val pageCount = renderer.pageCount
            val bitmaps = mutableListOf<Bitmap>()
            for (i in 0 until pageCount) {
                val page = renderer.openPage(i)
                val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmaps.add(bitmap)
                page.close()
            }
            renderer.close()
            bitmaps
        } catch (e: Exception) {
            emptyList()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(pages) { bitmap ->
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Page",
                    modifier = Modifier.fillMaxWidth().aspectRatio(bitmap.width.toFloat() / bitmap.height.toFloat())
                )
            }
        }
    }
}

@Composable
fun ImagePreviewList(images: List<String>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "First ${images.size} Pages Preview",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        items(images) { imageUrl ->
            Card(
                modifier = Modifier.padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Preview",
                    modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp)
                )
            }
        }
        
        item {
            Text(
                text = "--- End of Preview ---",
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
