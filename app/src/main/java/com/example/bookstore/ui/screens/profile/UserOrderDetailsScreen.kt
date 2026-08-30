package com.example.bookstore.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookstore.viewmodel.OrderViewModel
import com.example.bookstore.ui.components.StatusBadge
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserOrderDetailsScreen(
    orderId: String,
    viewModel: OrderViewModel,
    onBack: () -> Unit
) {
    val orders by viewModel.userOrders.collectAsState()
    val returns by viewModel.returnRequests.collectAsState()
    val order = orders.find { it.id == orderId }

    var showReturnDialog by remember { mutableStateOf(false) }
    var returnBookId by remember { mutableStateOf("") }
    var returnBookTitle by remember { mutableStateOf("") }
    var returnReason by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Details", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        if (order == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Order not found.")
            }
            return@Scaffold
        }

        val dateFormat = SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        val isDelivered = order.status.equals("Delivered", ignoreCase = true)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Order Info",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            StatusBadge(status = order.status)
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Order ID: ${order.id}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Placed on: ${dateFormat.format(Date(order.date))}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Items",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(order.items) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.bookTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Qty: ${item.quantity}",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "$${String.format("%.2f", item.price * item.quantity)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (isDelivered) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val existingReturn = returns.find { it.orderId == order.id && it.bookId == item.bookId }
                            if (existingReturn != null) {
                                Surface(
                                    color = when (existingReturn.status.lowercase(Locale.ROOT)) {
                                        "approved" -> MaterialTheme.colorScheme.primaryContainer
                                        "rejected" -> MaterialTheme.colorScheme.errorContainer
                                        else -> MaterialTheme.colorScheme.secondaryContainer
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text(
                                        text = "Return Request: ${existingReturn.status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }}",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (existingReturn.status.lowercase(Locale.ROOT)) {
                                            "approved" -> MaterialTheme.colorScheme.onPrimaryContainer
                                            "rejected" -> MaterialTheme.colorScheme.onErrorContainer
                                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                                        }
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        returnBookId = item.bookId
                                        returnBookTitle = item.bookTitle
                                        showReturnDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Return Item", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$${String.format("%.2f", order.totalAmount)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showReturnDialog) {
        AlertDialog(
            onDismissRequest = { showReturnDialog = false },
            title = { Text("Request Return", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Book: \"$returnBookTitle\"", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = returnReason,
                        onValueChange = { returnReason = it },
                        label = { Text("Reason for Return") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (returnReason.isNotBlank()) {
                            viewModel.submitReturnRequest(
                                orderId = orderId,
                                bookId = returnBookId,
                                bookTitle = returnBookTitle,
                                reason = returnReason
                            )
                            showReturnDialog = false
                            returnReason = ""
                        }
                    }
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReturnDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
