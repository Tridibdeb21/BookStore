package com.example.bookstore.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bookstore.ui.screens.login.LoginScreen
import com.example.bookstore.ui.screens.register.RegisterScreen
import com.example.bookstore.ui.screens.home.BookDetailsScreen
import com.example.bookstore.ui.screens.MainScreen
import com.example.bookstore.ui.screens.admin.AdminDashboardScreen
import com.example.bookstore.ui.screens.admin.ManageBooksScreen

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object AdminDashboard : Screen("admin_dashboard")
    object AdminBookList : Screen("admin_book_list")
    object ManageCategories : Screen("manage_categories")
    object AdminSalesDashboard : Screen("admin_sales_dashboard")
    object ManageOrders : Screen("manage_orders")
    object ManageCoupons : Screen("manage_coupons")
    object ManageBooks : Screen("manage_books?bookId={bookId}") {
        fun createRoute(bookId: String?) = if(bookId != null) "manage_books?bookId=$bookId" else "manage_books"
    }
    object Wishlist : Screen("wishlist")
    object BookDetails : Screen("book_details/{bookId}") {
        fun createRoute(bookId: String) = "book_details/$bookId"
    }
    object OrderHistory : Screen("order_history")
    object UserOrderDetails : Screen("user_order_details/{orderId}") {
        fun createRoute(orderId: String) = "user_order_details/$orderId"
    }
    object BookPreview : Screen("book_preview/{bookId}") {
        fun createRoute(bookId: String) = "book_preview/$bookId"
    }
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val cartViewModel: com.example.bookstore.viewmodel.CartViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val wishlistViewModel: com.example.bookstore.viewmodel.WishlistViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val orderViewModel: com.example.bookstore.viewmodel.OrderViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val adminViewModel: com.example.bookstore.viewmodel.AdminViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val homeViewModel: com.example.bookstore.viewmodel.HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val authViewModel: com.example.bookstore.viewmodel.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route,
        modifier = modifier
    ) {
        composable(route = Screen.Welcome.route) {
            com.example.bookstore.ui.screens.WelcomeScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        composable(route = Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = { 
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(route = Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(route = Screen.Main.route) {
            MainScreen(
                onBookClick = { bookId -> 
                    navController.navigate(Screen.BookDetails.createRoute(bookId)) 
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onAdminClick = {
                    navController.navigate(Screen.AdminDashboard.route)
                },
                onOrdersClick = {
                    navController.navigate(Screen.OrderHistory.route)
                },
                onWishlistClick = {
                    navController.navigate(Screen.Wishlist.route)
                },
                cartViewModel = cartViewModel,
                orderViewModel = orderViewModel,
                homeViewModel = homeViewModel,
                onOrderClick = { orderId -> 
                    navController.navigate(Screen.UserOrderDetails.createRoute(orderId))
                }
            )
        }
        composable(route = Screen.AdminDashboard.route) {
            com.example.bookstore.ui.screens.admin.AdminDashboardScreen(
                onBack = { navController.popBackStack() },
                onManageBooksClick = { navController.navigate(Screen.AdminBookList.route) },
                onManageCategoriesClick = { navController.navigate(Screen.ManageCategories.route) },
                onSalesDashboardClick = { navController.navigate(Screen.AdminSalesDashboard.route) },
                onManageOrdersClick = { navController.navigate(Screen.ManageOrders.route) },
                onManageCouponsClick = { navController.navigate(Screen.ManageCoupons.route) }
            )
        }
        composable(route = Screen.ManageOrders.route) {
            com.example.bookstore.ui.screens.admin.ManageOrdersScreen(
                viewModel = adminViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(route = Screen.ManageCoupons.route) {
            com.example.bookstore.ui.screens.admin.ManageCouponsScreen(
                viewModel = adminViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(route = Screen.AdminBookList.route) {
            com.example.bookstore.ui.screens.admin.AdminBookListScreen(
                onBack = { navController.popBackStack() },
                onAddBookClick = { navController.navigate(Screen.ManageBooks.createRoute(null)) },
                onEditBookClick = { bookId -> navController.navigate(Screen.ManageBooks.createRoute(bookId)) }
            )
        }
        composable(route = Screen.ManageCategories.route) {
            com.example.bookstore.ui.screens.admin.AdminCategoriesScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(route = Screen.AdminSalesDashboard.route) {
            com.example.bookstore.ui.screens.admin.AdminSalesScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.ManageBooks.route,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId")
            ManageBooksScreen(
                bookId = bookId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(route = Screen.Wishlist.route) {
            com.example.bookstore.ui.screens.profile.WishlistScreen(
                onBack = { navController.popBackStack() },
                wishlistViewModel = wishlistViewModel,
                onBookClick = { bookId -> navController.navigate(Screen.BookDetails.createRoute(bookId)) }
            )
        }
        composable(route = Screen.OrderHistory.route) {
            com.example.bookstore.ui.screens.profile.OrderHistoryScreen(
                viewModel = orderViewModel,
                onBack = { navController.popBackStack() },
                onOrderClick = { orderId -> navController.navigate(Screen.UserOrderDetails.createRoute(orderId)) }
            )
        }
        composable(
            route = Screen.UserOrderDetails.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            com.example.bookstore.ui.screens.profile.UserOrderDetailsScreen(
                orderId = orderId,
                viewModel = orderViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.BookDetails.route,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            BookDetailsScreen(
                bookId = bookId,
                onBack = { navController.popBackStack() },
                onReadPreviewClick = { navController.navigate(Screen.BookPreview.createRoute(bookId)) },
                cartViewModel = cartViewModel,
                wishlistViewModel = wishlistViewModel
            )
        }
        composable(
            route = Screen.BookPreview.route,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            com.example.bookstore.ui.screens.home.BookPreviewScreen(
                bookId = bookId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
