package com.example.bookstore.ui.screens.admin

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
import com.example.bookstore.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReturnsScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val returnsList by viewModel.returnsList.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchReturns()
    }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val pending = returnsList.filter { it.status.lowercase(Locale.ROOT) == "pending" }
    val resolved = returnsList.filter { it.status.lowercase(Locale.ROOT) != "pending" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Returns & Refunds Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (returnsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No return/refund requests yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (pending.isNotEmpty()) {
                item {
                    Text(
                        text = "Pending Requests (${pending.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                items(pending, key = { it.id }) { req ->
                    ReturnRequestCard(
                        bookTitle = req.bookTitle,
                        reason = req.reason,
                        date = dateFormat.format(Date(req.timestamp)),
                        status = req.status,
                        onApprove = { viewModel.updateReturnStatus(req.id, "approved") },
                        onReject = { viewModel.updateReturnStatus(req.id, "rejected") }
                    )
                }
            }

            if (resolved.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Resolved Requests (${resolved.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                items(resolved, key = { it.id }) { req ->
                    ReturnRequestCard(
                        bookTitle = req.bookTitle,
                        reason = req.reason,
                        date = dateFormat.format(Date(req.timestamp)),
                        status = req.status,
                        onApprove = null,
                        onReject = null
                    )
                }
            }
        }
    }
}

@Composable
private fun ReturnRequestCard(
    bookTitle: String,
    reason: String,
    date: String,
    status: String,
    onApprove: (() -> Unit)?,
    onReject: (() -> Unit)?
) {
    val statusColor = when (status.lowercase(Locale.ROOT)) {
        "approved" -> MaterialTheme.colorScheme.primary
        "rejected" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.secondary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
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
                    text = bookTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Reason: $reason",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Requested on: $date",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            if (onApprove != null && onReject != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Approve")
                    }
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reject", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
