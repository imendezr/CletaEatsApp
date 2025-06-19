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
import com.example.cletaeatsapp.data.model.UserType
import com.example.cletaeatsapp.ui.screens.ClienteOrdenesScreen
import com.example.cletaeatsapp.ui.screens.FeedbackScreen
import com.example.cletaeatsapp.ui.screens.LoginScreen
import com.example.cletaeatsapp.ui.screens.PerfilScreen
import com.example.cletaeatsapp.ui.screens.RegisterScreen
import com.example.cletaeatsapp.ui.screens.RepartidorOrdenesScreen
import com.example.cletaeatsapp.ui.screens.RepartidorQuejasScreen
import com.example.cletaeatsapp.ui.screens.ReportesScreen
import com.example.cletaeatsapp.ui.screens.RestauranteDetallesScreen
import com.example.cletaeatsapp.ui.screens.RestauranteListadoScreen
import com.example.cletaeatsapp.ui.screens.RestauranteOrdenesScreen
import com.example.cletaeatsapp.ui.screens.RestauranteRegistroScreen
import com.example.cletaeatsapp.viewmodel.ClienteOrdenViewModel
import com.example.cletaeatsapp.viewmodel.LoginViewModel
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
                navController = navController,
                onLoginSuccess = { cedula: String, userType: UserType ->
                    scope.launch {
                        loginViewModel.saveCedula(cedula)
                        when (userType) {
                            is UserType.ClienteUser -> navController.navigate("restaurants/$cedula") {
                                popUpTo("login") { inclusive = true }
                                launchSingleTop = true
                            }

                            is UserType.RepartidorUser -> navController.navigate("repartidor_orders/$cedula") {
                                popUpTo("login") { inclusive = true }
                                launchSingleTop = true
                            }

                            is UserType.AdminUser -> navController.navigate("reports") {
                                popUpTo("login") { inclusive = true }
                                launchSingleTop = true
                            }

                            is UserType.RestauranteUser -> {
                                // Obtener restauranteId desde la cédula jurídica
                                val restaurante = loginViewModel.repository.getRestaurantes()
                                    .find { it.cedulaJuridica == cedula }
                                if (restaurante != null) {
                                    navController.navigate("restaurante_orders/${restaurante.id}") {
                                        popUpTo("login") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                } else {
                                    navController.navigate("login") {
                                        popUpTo("login") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        }
                    }
                },
                loginViewModel = loginViewModel
            )
        }
        composable("register") {
            RegisterScreen(
                navController = navController,
                onRegisterSuccess = { cedula: String, userType: UserType ->
                    scope.launch {
                        loginViewModel.saveCedula(cedula)
                        when (userType) {
                            is UserType.ClienteUser -> navController.navigate("restaurants/$cedula") {
                                popUpTo("register") { inclusive = true }
                                launchSingleTop = true
                            }

                            is UserType.RepartidorUser -> navController.navigate("repartidor_orders/$cedula") {
                                popUpTo("register") { inclusive = true }
                                launchSingleTop = true
                            }

                            else -> navController.navigate("login") {
                                popUpTo("register") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                }
            )
        }
        composable("restaurants/{clienteId}") { backStackEntry ->
            val clienteId = backStackEntry.arguments?.getString("clienteId") ?: ""
            if (clienteId.isBlank()) {
                navController.navigate("login") {
                    popUpTo("login") { inclusive = true }
                    launchSingleTop = true
                }
                return@composable
            }
            RestauranteListadoScreen(
                navController = navController,
                clienteId = clienteId,
                onOpenDrawer = { scope.launch { drawerState.open() } },
                loginViewModel = loginViewModel
            )
        }
        composable("restaurant_details/{clienteId}/{restauranteJson}") { backStackEntry ->
            val clienteId = backStackEntry.arguments?.getString("clienteId") ?: ""
            val restauranteJson = backStackEntry.arguments?.getString("restauranteJson") ?: ""
            if (clienteId.isBlank() || restauranteJson.isBlank()) {
                navController.navigate("login") {
                    popUpTo("login") { inclusive = true }
                    launchSingleTop = true
                }
                return@composable
            }
            val restaurante = parseRestaurante(restauranteJson)
            if (restaurante == null) {
                navController.navigate("restaurants/$clienteId")
                return@composable
            }
            RestauranteDetallesScreen(
                restaurante = restaurante,
                clienteId = clienteId,
                onOpenDrawer = { scope.launch { drawerState.open() } },
                onOrderCreated = { navController.navigate("orders") },
                loginViewModel = loginViewModel,
                navController = navController
            )
        }
        composable("profile") {
            PerfilScreen(
                onOpenDrawer = { scope.launch { drawerState.open() } },
                loginViewModel = loginViewModel,
                navController = navController
            )
        }
        composable("orders") {
            ClienteOrdenesScreen(
                onOpenDrawer = { scope.launch { drawerState.open() } },
                loginViewModel = loginViewModel,
                navController = navController
            )
        }
        composable("feedback/{pedidoId}") { backStackEntry ->
            val pedidoId = backStackEntry.arguments?.getString("pedidoId") ?: ""
            if (pedidoId.isBlank()) {
                navController.navigate("orders")
                return@composable
            }
            val clienteOrdenViewModel: ClienteOrdenViewModel = hiltViewModel()
            val pedido: Pedido? = clienteOrdenViewModel.getPedidoById(pedidoId)
            if (pedido == null) {
                navController.navigate("orders")
                return@composable
            }
            FeedbackScreen(
                pedido = pedido,
                onFeedbackSubmitted = { navController.navigate("orders") },
                navController = navController,
                loginViewModel = loginViewModel
            )
        }
        composable("repartidor_quejas") {
            RepartidorQuejasScreen(
                onOpenDrawer = { scope.launch { drawerState.open() } },
                loginViewModel = loginViewModel,
                navController = navController
            )
        }
        composable("repartidor_orders/{repartidorId}") { backStackEntry ->
            val repartidorId = backStackEntry.arguments?.getString("repartidorId") ?: ""
            if (repartidorId.isBlank()) {
                navController.navigate("login") {
                    popUpTo("login") { inclusive = true }
                    launchSingleTop = true
                }
                return@composable
            }
            RepartidorOrdenesScreen(
                repartidorId = repartidorId,
                onOpenDrawer = { scope.launch { drawerState.open() } },
                loginViewModel = loginViewModel,
                navController = navController
            )
        }
        composable("reports") {
            ReportesScreen(
                onOpenDrawer = { scope.launch { drawerState.open() } },
                loginViewModel = loginViewModel,
                navController = navController
            )
        }
        composable("register_restaurant") {
            RestauranteRegistroScreen(
                navController = navController,
                loginViewModel = loginViewModel
            )
        }
        composable("restaurante_orders/{restauranteId}") { backStackEntry ->
            val restauranteId = backStackEntry.arguments?.getString("restauranteId") ?: ""
            if (restauranteId.isBlank()) {
                navController.navigate("login") {
                    popUpTo("login") { inclusive = true }
                    launchSingleTop = true
                }
                return@composable
            }
            RestauranteOrdenesScreen(
                restauranteId = restauranteId,
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
