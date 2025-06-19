package com.example.cletaeatsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.cletaeatsapp.data.model.UserType
import com.example.cletaeatsapp.ui.navigation.NavGraph
import com.example.cletaeatsapp.ui.theme.CletaEatsAppTheme
import com.example.cletaeatsapp.viewmodel.LoginViewModel
import com.example.cletaeatsapp.viewmodel.NavigationEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CletaEatsAppTheme {
                MainNavDrawer()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavDrawer() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val loginViewModel: LoginViewModel = hiltViewModel()
    val cedula by loginViewModel.cedulaFlow.collectAsStateWithLifecycle(initialValue = "")
    val userType by loginViewModel.userType.collectAsStateWithLifecycle(initialValue = null)
    val navigationEvent by loginViewModel.navigationEvent.collectAsStateWithLifecycle(initialValue = null)
    val windowInfo = LocalWindowInfo.current
    val context = LocalContext.current
    val drawerWidth = with(LocalDensity.current) {
        minOf(windowInfo.containerSize.width.toDp() * 0.5f, 320.dp)
    }

    // Handle back press
    BackHandler(enabled = true) {
        val currentDestination = navController.currentBackStackEntry?.destination?.route
        if (currentDestination == "login") {
            (context as? ComponentActivity)?.finish()
        } else {
            navController.popBackStack()
        }
    }

    // Handle navigation events
    LaunchedEffect(navigationEvent) {
        when (navigationEvent) {
            is NavigationEvent.NavigateToLogin -> {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
                loginViewModel.clearNavigationEvent()
            }
            is NavigationEvent.NavigateToClientHome -> {
                navController.navigate("restaurants/${(navigationEvent as NavigationEvent.NavigateToClientHome).cedula}") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
                loginViewModel.clearNavigationEvent()
            }
            is NavigationEvent.NavigateToRepartidorOrders -> {
                navController.navigate("repartidor_orders/${(navigationEvent as NavigationEvent.NavigateToRepartidorOrders).cedula}") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
                loginViewModel.clearNavigationEvent()
            }
            is NavigationEvent.NavigateToAdminReports -> {
                navController.navigate("reports") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
                loginViewModel.clearNavigationEvent()
            }
            else -> Unit
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // Role validation
            LaunchedEffect(userType) {
                if (userType == null) {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            if (userType == null) return@ModalNavigationDrawer

            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .fillMaxHeight()
                    .width(drawerWidth)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Menú",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.close() } },
                            modifier = Modifier.testTag("drawer_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Cerrar menú",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
                when (userType) {
                    is UserType.ClientUser -> {
                        DrawerItem(
                            text = "Perfil",
                            icon = Icons.Default.Person,
                            contentDescription = "Perfil de usuario",
                            tag = "drawer_profile",
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate("profile")
                                }
                            }
                        )
                        DrawerItem(
                            text = "Restaurantes",
                            icon = Icons.Default.Restaurant,
                            contentDescription = "Lista de restaurantes",
                            tag = "drawer_restaurants",
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    if (cedula.isBlank()) {
                                        navController.navigate("login")
                                    } else {
                                        navController.navigate("restaurants/$cedula")
                                    }
                                }
                            }
                        )
                        DrawerItem(
                            text = "Pedidos",
                            icon = Icons.Default.ShoppingCart,
                            contentDescription = "Lista de pedidos",
                            tag = "drawer_orders",
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate("orders")
                                }
                            }
                        )
                    }
                    is UserType.RepartidorUser -> {
                        DrawerItem(
                            text = "Perfil",
                            icon = Icons.Default.Person,
                            contentDescription = "Perfil de usuario",
                            tag = "drawer_profile",
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate("profile")
                                }
                            }
                        )
                        DrawerItem(
                            text = "Mis Pedidos",
                            icon = Icons.Default.DeliveryDining,
                            contentDescription = "Pedidos asignados",
                            tag = "drawer_repartidor_orders",
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    if (cedula.isBlank()) {
                                        navController.navigate("login")
                                    } else {
                                        navController.navigate("repartidor_orders/$cedula")
                                    }
                                }
                            }
                        )
                        DrawerItem(
                            text = "Quejas",
                            icon = Icons.Default.Warning,
                            contentDescription = "Quejas de repartidores",
                            tag = "drawer_repartidor_quejas",
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate("repartidor_quejas")
                                }
                            }
                        )
                    }
                    is UserType.AdminUser -> {
                        DrawerItem(
                            text = "Reportes",
                            icon = Icons.Default.BarChart,
                            contentDescription = "Reportes administrativos",
                            tag = "drawer_reports",
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate("reports")
                                }
                            }
                        )
                        DrawerItem(
                            text = "Quejas",
                            icon = Icons.Default.Warning,
                            contentDescription = "Gestión de quejas",
                            tag = "drawer_quejas",
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate("reports")
                                }
                            }
                        )
                        DrawerItem(
                            text = "Registrar Restaurante",
                            icon = Icons.Default.Restaurant,
                            contentDescription = "Registrar nuevo restaurante",
                            tag = "drawer_register_restaurant",
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate("register_restaurant")
                                }
                            }
                        )
                    }
                    is UserType.RestauranteUser -> {
                        DrawerItem(
                            text = "Perfil",
                            icon = Icons.Default.Person,
                            contentDescription = "Perfil del restaurante",
                            tag = "drawer_restaurante_profile",
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate("profile")
                                }
                            }
                        )
                        DrawerItem(
                            text = "Mis Pedidos",
                            icon = Icons.Default.ShoppingCart,
                            contentDescription = "Pedidos del restaurante",
                            tag = "drawer_restaurante_orders",
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    val restaurante = loginViewModel.repository.getRestaurantes()
                                        .find { it.cedulaJuridica == cedula }
                                    if (restaurante != null && cedula.isNotBlank()) {
                                        navController.navigate("restaurante_orders/${restaurante.id}")
                                    } else {
                                        navController.navigate("login")
                                    }
                                }
                            }
                        )
                    }
                    else -> Unit
                }
            }
        }
    ) {
        NavGraph(
            navController = navController,
            drawerState = drawerState,
            scope = scope,
            loginViewModel = loginViewModel
        )
    }
}

@Composable
fun DrawerItem(
    text: String,
    icon: ImageVector,
    contentDescription: String,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .testTag(tag)
            .clickable { onClick() }
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
