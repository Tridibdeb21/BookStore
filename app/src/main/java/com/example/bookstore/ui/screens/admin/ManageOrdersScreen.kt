package com.example.bookstore.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookstore.model.Order
import com.example.bookstore.viewmodel.AdminViewModel
import com.example.bookstore.ui.components.StatusBadge
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageOrdersScreen(viewModel: AdminViewModel, onBack: () -> Unit) {
    val orders by viewModel.allOrders.collectAsState()
    val updateStatus by viewModel.orderUpdateStatus.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(updateStatus) {
        updateStatus?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearOrderUpdateStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Fulfillment", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(orders) { order ->
                OrderAdminCard(order = order, onStatusChange = { newStatus ->
                    viewModel.updateOrderStatus(order.id, newStatus)
                })
            }
        }
    }
}

@Composable
fun OrderAdminCard(order: Order, onStatusChange: (String) -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    val statuses = listOf("Pending", "Processing", "Shipped", "Delivered", "Cancelled")

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
                Column {
                    Text(
                        text = "Order #${order.id.takeLast(6)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = order.userEmail,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                StatusBadge(status = order.status)
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            
            order.items.forEach { item ->
                Text(
                    text = "${item.quantity}x ${item.bookTitle}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: $${String.format("%.2f", order.totalAmount)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (order.status != "Delivered") {
                    Box {
                        Button(
                            onClick = { showMenu = true },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Update Status", fontSize = 12.sp)
                        }
                        
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            statuses.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status) },
                                    onClick = {
                                        if (status == "Delivered") {
                                            showConfirmDialog = true
                                        } else {
                                            onStatusChange(status)
                                        }
                                        showMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Delivery") },
            text = { Text("Are you sure you want to mark this order as Delivered? Once marked as Delivered, the status cannot be changed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onStatusChange("Delivered")
                        showConfirmDialog = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
