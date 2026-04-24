package com.example.bookstore.ui.screens.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.bookstore.model.Coupon
import com.example.bookstore.viewmodel.CartViewModel
import com.example.bookstore.viewmodel.CartItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(onBack: () -> Unit, onCheckoutSuccess: () -> Unit, cartViewModel: CartViewModel) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val subtotal by cartViewModel.subtotal.collectAsState()
    val total by cartViewModel.total.collectAsState()
    val appliedCoupon by cartViewModel.appliedCoupon.collectAsState()
    val couponError by cartViewModel.couponError.collectAsState()
    val checkoutStatus by cartViewModel.checkoutStatus.collectAsState()
    val selectedBookIds by cartViewModel.selectedBookIds.collectAsState()
    val allSelected = cartItems.isNotEmpty() && cartItems.all { it.book.id in selectedBookIds }
    val lastOrder by cartViewModel.lastOrder.collectAsState()
    
    var cartSearchQuery by remember { mutableStateOf("") }
    val displayedCartItems = cartItems.filter {
        cartSearchQuery.isBlank() ||
        it.book.title.contains(cartSearchQuery, ignoreCase = true) ||
        it.book.author.contains(cartSearchQuery, ignoreCase = true)
    }

    val context = LocalContext.current

    var promoCodeText by remember { mutableStateOf("") }
    var showRemoveDialog by remember { mutableStateOf(false) }
    var itemToRemove by remember { mutableStateOf<CartItem?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(checkoutStatus) {
        if (checkoutStatus != null) {
            snackbarHostState.showSnackbar(checkoutStatus!!)
            cartViewModel.resetCheckoutStatus()
        }
    }

    LaunchedEffect(lastOrder) {
        if (lastOrder != null) {
            onCheckoutSuccess()
        }
    }

    if (showRemoveDialog && itemToRemove != null) {
        AlertDialog(
            onDismissRequest = { 
                showRemoveDialog = false
                itemToRemove = null
            },
            title = { Text("Remove item?", fontWeight = FontWeight.Bold) },
            text = { Text("Do you want to remove '${itemToRemove?.book?.title}' from your shopping bag?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemToRemove?.let { cartViewModel.removeFromCart(it.book.id) }
                        showRemoveDialog = false
                        itemToRemove = null
                    }
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showRemoveDialog = false 
                        itemToRemove = null
                    }
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Shopping Bag", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost = { 
            SnackbarHost(snackbarHostState) { data ->
                val isError = data.visuals.message.startsWith("Error")
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (isError) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(data.visuals.message, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (cartItems.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Your bag is empty",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Looks like you haven't added anything yet. Discover your next favorite read!",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = onBack, shape = RoundedCornerShape(12.dp)) {
                        Text("Start Browsing")
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Search Bar
                    OutlinedTextField(
                        value = cartSearchQuery,
                        onValueChange = { cartSearchQuery = it },
                        placeholder = { Text("Search by title or author...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(28.dp),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Select All / None header
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = allSelected,
                                        onCheckedChange = {
                                            if (allSelected) cartViewModel.clearSelection()
                                            else cartViewModel.selectAll()
                                        }
                                    )
                                    Text(
                                        if (allSelected) "Deselect All" else "Select All",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                                }
                                Text(
                                    "${selectedBookIds.size} of ${cartItems.size} selected",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        items(displayedCartItems) { item ->
                            CartCard(
                                item = item,
                                isSelected = item.book.id in selectedBookIds,
                                onToggle = { cartViewModel.toggleSelection(item.book.id) },
                                onAdd = { cartViewModel.addToCart(item.book) },
                                onRemove = { 
                                    if (item.quantity > 1) {
                                        cartViewModel.decreaseQuantity(item.book.id)
                                    } else {
                                        itemToRemove = item
                                        showRemoveDialog = true
                                    }
                                },
                                onDelete = { cartViewModel.removeFromCart(item.book.id) }
                            )
                        }
                    }

                    // Checkout Summary Card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        shadowElevation = 16.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .navigationBarsPadding()
                        ) {
                            // Status text was removed as it's now handled by Toast

                            // Promo Code Section
                            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = promoCodeText,
                                        onValueChange = { promoCodeText = it.uppercase() },
                                        label = { Text("Promo Code") },
                                        placeholder = { Text("e.g. READ20") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        isError = couponError != null,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Button(
                                        onClick = { cartViewModel.applyCoupon(promoCodeText) },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.height(56.dp)
                                    ) {
                                        Text("Apply")
                                    }
                                }
                                
                                if (couponError != null) {
                                    Text(
                                        text = couponError!!,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                                    )
                                }
                                
                                if (appliedCoupon != null) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Applied: ${appliedCoupon!!.code} (${appliedCoupon!!.discountPercent}% OFF)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Remove",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.clickable { cartViewModel.removeCoupon() }
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Subtotal", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$${String.format("%.2f", subtotal)}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            
                            if (appliedCoupon != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Discount", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                    Text(
                                        "-$${String.format("%.2f", subtotal - total)}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Amount", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "$${String.format("%.2f", total)}",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Button(
                                onClick = { cartViewModel.checkout() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Proceed to Checkout", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartCard(
    item: CartItem,
    isSelected: Boolean,
    onToggle: () -> Unit,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection Checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                modifier = Modifier.padding(end = 4.dp)
            )

            if (item.book.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = item.book.imageUrl,
                    contentDescription = "Cover",
                    modifier = Modifier
                        .size(80.dp, 110.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier.size(80.dp, 110.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Img", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.book.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    maxLines = 1
                )
                Text(
                    item.book.author,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Quantity Selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                        Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Text("${item.quantity}", fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 12.dp))
                    IconButton(onClick = onAdd, modifier = Modifier.size(32.dp)) {
                        Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.5f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "$${String.format("%.2f", item.book.price * item.quantity)}",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
