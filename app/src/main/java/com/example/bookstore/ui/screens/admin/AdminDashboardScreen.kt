package com.example.bookstore.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onBack: () -> Unit, 
    onManageBooksClick: () -> Unit, 
    onManageCategoriesClick: () -> Unit, 
    onSalesDashboardClick: () -> Unit,
    onManageOrdersClick: () -> Unit,
    onManageCouponsClick: () -> Unit,
    onManageReturnsClick: () -> Unit,
    onManageFlashSalesClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Control Center", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Portal Overview",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            AdminActionCard(
                title = "Books & Inventory",
                subtitle = "Manage your catalog and stock",
                onClick = onManageBooksClick
            )
            Spacer(modifier = Modifier.height(16.dp))
            AdminActionCard(
                title = "Categories",
                subtitle = "Organize books by genre",
                onClick = onManageCategoriesClick
            )
            Spacer(modifier = Modifier.height(16.dp))
            AdminActionCard(
                title = "Order Fulfillment",
                subtitle = "Update shipping and delivery",
                onClick = onManageOrdersClick
            )
            Spacer(modifier = Modifier.height(16.dp))
            AdminActionCard(
                title = "Promo Codes",
                subtitle = "Manage discounts and coupons",
                onClick = onManageCouponsClick
            )
            Spacer(modifier = Modifier.height(16.dp))
            AdminActionCard(
                title = "Sales Analytics",
                subtitle = "View orders and revenue",
                onClick = onSalesDashboardClick
            )
            Spacer(modifier = Modifier.height(16.dp))
            AdminActionCard(
                title = "Returns & Refunds",
                subtitle = "Manage user return requests",
                onClick = onManageReturnsClick
            )
            Spacer(modifier = Modifier.height(16.dp))
            AdminActionCard(
                title = "Flash Sales & Feature",
                subtitle = "Set flash sales and Book of the Day",
                onClick = onManageFlashSalesClick
            )
        }
    }
}

@Composable
fun AdminActionCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                Text(subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        }
    }
}
