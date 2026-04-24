package com.example.bookstore.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bookstore.ui.screens.cart.CartScreen
import com.example.bookstore.ui.screens.home.CategoriesScreen
import com.example.bookstore.ui.screens.home.HomeScreen
import com.example.bookstore.ui.screens.profile.ProfileScreen

sealed class BottomNavItem(val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Home")
    object Categories : BottomNavItem("categories", Icons.Default.List, "Categories")
    object Orders : BottomNavItem("orders", Icons.Default.List, "Orders")
    object Cart : BottomNavItem("cart", Icons.Default.ShoppingCart, "Cart")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Profile")
}

@Composable
fun MainScreen(
    onBookClick: (String) -> Unit,
    onLogout: () -> Unit,
    onAdminClick: () -> Unit,
    onWishlistClick: () -> Unit,
    onOrdersClick: () -> Unit,
    cartViewModel: com.example.bookstore.viewmodel.CartViewModel,
    orderViewModel: com.example.bookstore.viewmodel.OrderViewModel,
    homeViewModel: com.example.bookstore.viewmodel.HomeViewModel,
    onOrderClick: (String) -> Unit
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
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            if (item == BottomNavItem.Cart && cartCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge {
                                            Text(cartCount.toString())
                                        }
                                    }
                                ) {
                                    Icon(item.icon, contentDescription = item.label)
                                }
                            } else {
                                Icon(item.icon, contentDescription = item.label)
                            }
                        },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                navController.graph.startDestinationRoute?.let { route ->
                                    popUpTo(route) { saveState = true }
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = BottomNavItem.Home.route, Modifier.padding(innerPadding)) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(viewModel = homeViewModel, onBookClick = onBookClick)
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
                ProfileScreen(onLogout = onLogout, onAdminClick = onAdminClick, onWishlistClick = onWishlistClick, onOrdersClick = onOrdersClick)
            }
        }
    }
}
