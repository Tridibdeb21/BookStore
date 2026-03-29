package com.example.bookstore.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bookstore.model.Order
import com.example.bookstore.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSalesScreen(onBack: () -> Unit, viewModel: AdminViewModel = viewModel()) {
    val allOrders by viewModel.allOrders.collectAsState()
    
    val totalRevenue = allOrders.sumOf { it.totalAmount }
    val totalOrdersCount = allOrders.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Gross Revenue", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("$$totalRevenue", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Total Orders Processed: $totalOrdersCount", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }

            Text("All Customer Orders", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            if (allOrders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No orders have been placed yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(allOrders) { order ->
                        AdminOrderCard(order = order, onStatusChange = { newStatus -> 
                            viewModel.updateOrderStatus(order.id, newStatus) 
                        })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderCard(order: Order, onStatusChange: (String) -> Unit) {
    val formatter = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
    val statuses = listOf("Pending", "Processing", "Shipped", "Delivered")
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(order.userEmail, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("$${order.totalAmount}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.tertiary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Date: ${formatter.format(Date(order.date))}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            
            order.items.forEach { item ->
                Text("- ${item.quantity}x ${item.bookTitle}", fontSize = 14.sp)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = "Status: ${order.status}",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    statuses.forEach { statusOption ->
                        DropdownMenuItem(
                            text = { Text(statusOption) },
                            onClick = {
                                onStatusChange(statusOption)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
