package pl.pysznabasia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import pl.pysznabasia.ui.screens.*
import pl.pysznabasia.ui.theme.PysznaBasiaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PysznaBasiaTheme {
                PysznaBasiaApp()
            }
        }
    }
}

private object Routes {
    const val MENU = "menu"
    const val CART = "cart"
    const val ORDERS = "orders"
    const val PRODUCT = "product/{id}"
    const val CHECKOUT = "checkout"
    const val CONFIRMATION = "confirmation/{orderId}"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.MENU, "Menu", Icons.Default.RestaurantMenu),
    Tab(Routes.CART, "Koszyk", Icons.Default.ShoppingCart),
    Tab(Routes.ORDERS, "Zamówienia", Icons.AutoMirrored.Filled.ReceiptLong),
)

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun PysznaBasiaApp() {
    val vm: AppViewModel = viewModel()
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val cart by vm.cart.collectAsStateWithLifecycle()
    val cartCount = cart.size

    Scaffold(
        bottomBar = {
            if (tabs.any { it.route == currentRoute }) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = { nav.navigateToTab(tab.route) },
                            label = { Text(tab.label) },
                            icon = {
                                if (tab.route == Routes.CART && cartCount > 0) {
                                    BadgedBox(badge = { Badge { Text("$cartCount") } }) {
                                        Icon(tab.icon, contentDescription = tab.label)
                                    }
                                } else {
                                    Icon(tab.icon, contentDescription = tab.label)
                                }
                            },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = nav,
            startDestination = Routes.MENU,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            composable(Routes.MENU) {
                MenuScreen(vm, onProductClick = { nav.navigate("product/$it") })
            }
            composable(Routes.PRODUCT) { entry ->
                ProductDetailScreen(
                    productId = entry.arguments?.getString("id").orEmpty(),
                    vm = vm,
                    onBack = { nav.popBackStack() },
                )
            }
            composable(Routes.CART) {
                CartScreen(
                    vm,
                    onBrowseMenu = { nav.navigateToTab(Routes.MENU) },
                    onCheckout = { nav.navigate(Routes.CHECKOUT) },
                )
            }
            composable(Routes.CHECKOUT) {
                CheckoutScreen(
                    vm,
                    onBack = { nav.popBackStack() },
                    onOrderPlaced = { order ->
                        nav.navigate("confirmation/${order.id}") {
                            popUpTo(Routes.MENU)
                        }
                    },
                )
            }
            composable(Routes.CONFIRMATION) { entry ->
                ConfirmationScreen(
                    vm,
                    orderId = entry.arguments?.getString("orderId").orEmpty(),
                    onBackToMenu = { nav.popBackStack(Routes.MENU, inclusive = false) },
                    onShowOrders = { nav.navigateToTab(Routes.ORDERS) },
                )
            }
            composable(Routes.ORDERS) {
                OrdersScreen(vm, onReordered = { nav.navigateToTab(Routes.CART) })
            }
        }
    }
}
