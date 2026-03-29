package com.example.bookstore.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.bookstore.viewmodel.BookDetailsViewModel
import com.example.bookstore.viewmodel.CartViewModel
import com.example.bookstore.viewmodel.WishlistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsScreen(
    bookId: String, 
    onBack: () -> Unit,
    onReadPreviewClick: () -> Unit = {},
    cartViewModel: CartViewModel,
    wishlistViewModel: WishlistViewModel,
    detailsViewModel: BookDetailsViewModel = viewModel(),
    authViewModel: com.example.bookstore.viewmodel.AuthViewModel = viewModel()
) {
    val book by detailsViewModel.book.collectAsState()
    val reviews by detailsViewModel.reviews.collectAsState()
    val isLoading by detailsViewModel.isLoading.collectAsState()
    val wishlistIds by wishlistViewModel.wishlistIds.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val cartItems by cartViewModel.cartItems.collectAsState()
    
    val context = LocalContext.current
    
    var userRating by remember { mutableStateOf(0) }
    var userComment by remember { mutableStateOf("") }
    
    LaunchedEffect(bookId) {
        detailsViewModel.loadBook(bookId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val isWishlisted = book?.let { wishlistIds.contains(it.id) } ?: false
                    IconButton(onClick = { book?.let { wishlistViewModel.toggleWishlist(it.id) } }) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (isWishlisted) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        if (isLoading || book == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            val safeBook = book!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Hero Image Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .padding(paddingValues)
                        .padding(top = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (safeBook.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = safeBook.imageUrl,
                            contentDescription = "Cover",
                            modifier = Modifier
                                .fillMaxHeight(0.9f)
                                .aspectRatio(0.7f)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Info Card Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = safeBook.title,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "by ${safeBook.author}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        
                        // Rating Display
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB400),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = String.format("%.1f", safeBook.rating),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            Text(
                                text = " (${safeBook.reviewsCount} reviews)",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }

                        Text(
                            text = "$${safeBook.price}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Text(
                            text = if(safeBook.description.isNotBlank()) safeBook.description else "This classic masterpiece awaits its discovery in your library.",
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            textAlign = TextAlign.Justify,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (safeBook.previewImages.isNotEmpty() || safeBook.pdfUrl.isNotBlank()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = onReadPreviewClick,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Read Sample Preview", fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { 
                                val isAlreadyInCart = cartItems.any { it.book.id == safeBook.id }
                                if (isAlreadyInCart) {
                                    Toast.makeText(context, "This book is already in your shopping bag", Toast.LENGTH_SHORT).show()
                                } else {
                                    cartViewModel.addToCart(safeBook) 
                                    Toast.makeText(context, "${safeBook.title} added to cart", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Text("Add to Cart • $${String.format("%.2f", safeBook.price)}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }

                // Reviews Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        "Rate this Book",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    
                    RatingBar(
                        rating = userRating,
                        onRatingSelected = { userRating = it },
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = userComment,
                        onValueChange = { userComment = it },
                        placeholder = { Text("Write your thoughts about this book...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(100.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Button(
                        onClick = {
                            val user = (authState as? com.example.bookstore.viewmodel.AuthState.Success)?.user
                            if (user != null && userRating > 0) {
                                detailsViewModel.submitReview(
                                    safeBook.id,
                                    com.example.bookstore.model.Review(
                                        userId = user.uid,
                                        userName = user.email.substringBefore("@"),
                                        rating = userRating.toDouble(),
                                        comment = userComment
                                    )
                                )
                                userRating = 0
                                userComment = ""
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                            .align(Alignment.End),
                        enabled = userRating > 0 && authState is com.example.bookstore.viewmodel.AuthState.Success
                    ) {
                        Text(if (authState is com.example.bookstore.viewmodel.AuthState.Success) "Post Review" else "Login to Review")
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        "Community Reviews",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    
                    if (reviews.isEmpty()) {
                        Text(
                            "No reviews yet. Be the first to share your thoughts!",
                            modifier = Modifier.padding(24.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        reviews.forEach { review ->
                            ReviewItem(review)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RatingBar(rating: Int, onRatingSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        (1..5).forEach { index ->
            IconButton(
                onClick = { onRatingSelected(index) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (index <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = null,
                    tint = if (index <= rating) Color(0xFFFFB400) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun ReviewItem(review: com.example.bookstore.model.Review) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(review.userName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row {
                    (1..5).forEach { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index <= review.rating) Color(0xFFFFB400) else Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            if (review.comment.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(review.comment, fontSize = 14.sp)
            }
        }
    }
}
