package com.example.bookstore.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import coil.compose.AsyncImage
import com.example.bookstore.model.Book

@Composable
fun BookCard(book: Book, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isFlashSale = book.flashSalePrice != null &&
            book.flashSaleExpiry != null &&
            book.flashSaleExpiry > System.currentTimeMillis()
    val isOutOfStock = book.stockQuantity <= 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isOutOfStock) { onClick() }
            .height(260.dp),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Cover image area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (book.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = book.imageUrl,
                        contentDescription = book.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Book,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp).align(Alignment.Center),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                    )
                }

                // Gradient scrim for text readability
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                            )
                        )
                )

                // Flash sale badge
                if (isFlashSale) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFEF4444)
                    ) {
                        Text(
                            text = "⚡ SALE",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }

                // Out of stock overlay
                if (isOutOfStock) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.65f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SOLD OUT",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }

            // Book info
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = book.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = book.author,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Price row
                if (isFlashSale) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$${book.flashSalePrice}",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "$${book.price}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                } else {
                    Text(
                        text = "$${book.price}",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
