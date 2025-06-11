package com.example.cletaeatsapp.ui.navigation

import android.util.Log
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.cletaeatsapp.data.model.Pedido
import com.example.cletaeatsapp.data.model.Restaurante
import com.example.cletaeatsapp.ui.screens.FeedbackScreen
import com.example.cletaeatsapp.ui.screens.LoginScreen
import com.example.cletaeatsapp.ui.screens.OrdersScreen
import com.example.cletaeatsapp.ui.screens.ProfileScreen
import com.example.cletaeatsapp.ui.screens.RepartidorQuejasScreen
import com.example.cletaeatsapp.ui.screens.RepartidorRegistrationScreen
import com.example.cletaeatsapp.ui.screens.ReportsScreen
import com.example.cletaeatsapp.ui.screens.RestaurantDetailsScreen
import com.example.cletaeatsapp.ui.screens.RestaurantListScreen
import com.example.cletaeatsapp.ui.screens.RestaurantRegistrationScreen
import com.example.cletaeatsapp.viewmodel.LoginViewModel
import com.example.cletaeatsapp.viewmodel.OrderViewModel
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun NavGraph(
    navController: NavHostController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    loginViewModel: LoginViewModel
) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { cedula ->
                    Log.d("NavGraph", "Navigating to restaurants with cedula: $cedula")
                    if (cedula.isNotBlank()) {
                        scope.launch {
                            loginViewModel.saveCedula(cedula)
                            navController.navigate("restaurants/$cedula") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    } else {
                        Log.w("NavGraph", "Cedula is blank, navigation aborted")
                    }
                }
            )
        }
        composable("restaurants/{clienteId}") { backStackEntry ->
            val clienteId = backStackEntry.arguments?.getString("clienteId") ?: ""
            Log.d("NavGraph", "Entered restaurants route with clienteId: $clienteId")
            if (clienteId.isBlank() && clienteId != "0") {
                Log.w("NavGraph", "Blank clienteId, redirecting to login")
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
                return@composable
            }
            RestaurantListScreen(
                navController = navController,
                clienteId = clienteId,
                onOpenDrawer = { scope.launch { drawerState.open() } },
                loginViewModel = loginViewModel
            )
        }
        composable("restaurant_details/{clienteId}/{restauranteJson}") { backStackEntry ->
            val clienteId = backStackEntry.arguments?.getString("clienteId") ?: ""
            val restauranteJson = backStackEntry.arguments?.getString("restauranteJson") ?: ""
            Log.d("NavGraph", "Entered restaurant_details with clienteId: $clienteId")
            if (clienteId.isBlank() || restauranteJson.isBlank()) {
                Log.w("NavGraph", "Invalid clienteId or restauranteJson, redirecting to login")
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
                return@composable
            }
            val restaurante = parseRestaurante(restauranteJson)
            if (restaurante == null) {
                Log.w("NavGraph", "Failed to parse restaurante, redirecting to restaurants")
                navController.navigate("restaurants/$clienteId")
                return@composable
            }
            RestaurantDetailsScreen(
                restaurante = restaurante,
                clienteId = clienteId,
                onOpenDrawer = { scope.launch { drawerState.open() } },
                onOrderCreated = { navController.navigate("orders") },
                loginViewModel = loginViewModel,
                navController = navController
            )
        }
        composable("profile") {
            ProfileScreen(
                onOpenDrawer = { scope.launch { drawerState.open() } },
                loginViewModel = loginViewModel,
                navController = navController
            )
        }
        composable("orders") {
            OrdersScreen(
                onOpenDrawer = { scope.launch { drawerState.open() } },
                loginViewModel = loginViewModel,
                navController = navController
            )
        }
        composable("reports") {
            ReportsScreen(
                onOpenDrawer = { scope.launch { drawerState.open() } },
                loginViewModel = loginViewModel,
                navController = navController
            )
        }
        composable("register_restaurant") {
            RestaurantRegistrationScreen(
                navController = navController
            )
        }
        composable("register_repartidor") {
            RepartidorRegistrationScreen(
                navController = navController
            )
        }
        composable("feedback/{pedidoId}") { backStackEntry ->
            val pedidoId = backStackEntry.arguments?.getString("pedidoId") ?: ""
            if (pedidoId.isBlank()) {
                Log.w("NavGraph", "Invalid pedidoId, redirecting to orders")
                navController.navigate("orders")
                return@composable
            }
            val orderViewModel: OrderViewModel = hiltViewModel()
            val pedido: Pedido? = orderViewModel.getPedidoById(pedidoId)
            if (pedido == null) {
                Log.w("NavGraph", "Pedido not found, redirecting to orders")
                navController.navigate("orders")
                return@composable
            }
            FeedbackScreen(
                pedido = pedido,
                onFeedbackSubmitted = { navController.navigate("orders") },
                navController = navController
            )
        }
        composable("repartidor_quejas") {
            RepartidorQuejasScreen(
                onOpenDrawer = { scope.launch { drawerState.open() } },
                loginViewModel = loginViewModel,
                navController = navController
            )
        }
    }
}

private fun parseRestaurante(json: String): Restaurante? {
    return try {
        Gson().fromJson(json, Restaurante::class.java)
    } catch (e: Exception) {
        Log.e("NavGraph", "Failed to parse restaurante JSON: $json", e)
        null
    }
}
