package com.example.bookstore.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bookstore.ui.screens.cart.CartScreen
import com.example.bookstore.ui.screens.home.CategoriesScreen
import com.example.bookstore.ui.screens.home.HomeScreen
import com.example.bookstore.ui.screens.profile.ProfileScreen

sealed class BottomNavItem(val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String) {
    object Home       : BottomNavItem("home",       Icons.Default.Home,           "Home")
    object Categories : BottomNavItem("categories", Icons.AutoMirrored.Filled.List, "Browse")
    object Orders     : BottomNavItem("orders",     Icons.Default.ReceiptLong,    "Orders")
    object Cart       : BottomNavItem("cart",       Icons.Default.ShoppingCart,   "Cart")
    object Profile    : BottomNavItem("profile",    Icons.Default.Person,         "Profile")
}

@Composable
fun MainScreen(
    onBookClick: (String) -> Unit,
    onLogout: () -> Unit,
    onAdminClick: () -> Unit,
    onWishlistClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onShelfClick: () -> Unit = {},
    cartViewModel: com.example.bookstore.viewmodel.CartViewModel,
    orderViewModel: com.example.bookstore.viewmodel.OrderViewModel,
    homeViewModel: com.example.bookstore.viewmodel.HomeViewModel,
    onOrderClick: (String) -> Unit,
    onAiRecommendClick: () -> Unit,
    onAiChatClick: () -> Unit
) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val cartCount = cartItems.sumOf { it.quantity }

    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Categories,
        BottomNavItem.Orders,
        BottomNavItem.Cart,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 24.dp,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    items.forEach { item ->
                        val isSelected = currentRoute == item.route
                        val iconColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            label = "nav_icon_color"
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Transparent),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .padding(bottom = 2.dp)
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            } else {
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            IconButton(
                                onClick = {
                                    navController.navigate(item.route) {
                                        navController.graph.startDestinationRoute?.let { route ->
                                            popUpTo(route) { saveState = true }
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                modifier = Modifier.size(44.dp)
                            ) {
                                if (item == BottomNavItem.Cart && cartCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            ) {
                                                Text(cartCount.toString(), fontSize = 9.sp, fontWeight = FontWeight.Black)
                                            }
                                        }
                                    ) {
                                        Icon(item.icon, contentDescription = item.label, tint = iconColor, modifier = Modifier.size(24.dp))
                                    }
                                } else {
                                    Icon(item.icon, contentDescription = item.label, tint = iconColor, modifier = Modifier.size(24.dp))
                                }
                            }

                            Text(
                                text = item.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = iconColor
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = BottomNavItem.Home.route, Modifier.padding(innerPadding)) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onBookClick = onBookClick,
                    onAiRecommendClick = onAiRecommendClick,
                    onAiChatClick = onAiChatClick
                )
            }
            composable(BottomNavItem.Categories.route) {
                CategoriesScreen(
                    viewModel = homeViewModel,
                    onCategoryClick = {
                        navController.navigate(BottomNavItem.Home.route) {
                            navController.graph.startDestinationRoute?.let { route ->
                                popUpTo(route) { saveState = true }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(BottomNavItem.Orders.route) {
                com.example.bookstore.ui.screens.profile.OrderHistoryScreen(
                    viewModel = orderViewModel,
                    onBack = { navController.popBackStack() },
                    onOrderClick = onOrderClick
                )
            }
            composable(BottomNavItem.Cart.route) {
                CartScreen(
                    onBack = { navController.popBackStack() },
                    onCheckoutSuccess = { navController.navigate("order_success") },
                    cartViewModel = cartViewModel
                )
            }
            composable("order_success") {
                com.example.bookstore.ui.screens.cart.OrderSuccessScreen(
                    cartViewModel = cartViewModel,
                    onContinueShopping = {
                        cartViewModel.resetLastOrder()
                        navController.navigate(BottomNavItem.Home.route) {
                            popUpTo(BottomNavItem.Home.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    onLogout = onLogout,
                    onAdminClick = onAdminClick,
                    onWishlistClick = onWishlistClick,
                    onOrdersClick = onOrdersClick,
                    onShelfClick = onShelfClick
                )
            }
        }
    }
}
