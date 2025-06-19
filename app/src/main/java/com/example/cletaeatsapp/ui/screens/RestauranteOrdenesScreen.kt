package com.example.cletaeatsapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.cletaeatsapp.data.model.UserType
import com.example.cletaeatsapp.ui.components.OrdenCard
import com.example.cletaeatsapp.viewmodel.LoginViewModel
import com.example.cletaeatsapp.viewmodel.RestauranteOrdenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestauranteOrdenesScreen(
    restauranteId: String,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RestauranteOrdenViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel,
    navController: NavController
) {
    val userType by loginViewModel.userType.collectAsStateWithLifecycle()
    if (userType !is UserType.RestauranteUser) {
        LaunchedEffect(Unit) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
        return
    }

    val pedidos by viewModel.getPedidosForRestaurante(restauranteId).collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Mis Pedidos", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
                    }
                },
                actions = {
                    IconButton(onClick = { loginViewModel.logout() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                errorMessage != null -> {
                    Text(
                        text = errorMessage ?: "Error desconocido",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                pedidos.isEmpty() -> {
                    Text(
                        text = "No hay pedidos asignados",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 16.dp),
                        state = rememberLazyListState()
                    ) {
                        itemsIndexed(pedidos) { index, pedido ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically(),
                                exit = fadeOut() + slideOutVertically()
                            ) {
                                OrdenCard(
                                    pedido = pedido,
                                    onMarkDelivered = {
                                        if (pedido.estado != "entregado") {
                                            viewModel.updateOrderStatus(pedido.id, "entregado")
                                        }
                                    },
                                    onMarkInTransit = {
                                        if (pedido.estado != "en camino") {
                                            viewModel.updateOrderStatus(pedido.id, "en camino")
                                        }
                                    },
                                    onMarkSuspended = {
                                        if (pedido.estado != "suspendido") {
                                            viewModel.updateOrderStatus(pedido.id, "suspendido")
                                        }
                                    },
                                    isRepartidor = false,
                                    isRestaurant = true
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
