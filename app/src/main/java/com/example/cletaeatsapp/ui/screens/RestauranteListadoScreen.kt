package com.example.cletaeatsapp.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.cletaeatsapp.data.repository.UserType
import com.example.cletaeatsapp.ui.components.RequireRole
import com.example.cletaeatsapp.ui.components.RestauranteCard
import com.example.cletaeatsapp.viewmodel.LoginViewModel
import com.example.cletaeatsapp.viewmodel.RestauranteViewModel
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun RestauranteListadoScreen(
    navController: NavController,
    clienteId: String,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RestauranteViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel
) {
    RequireRole(
        allowedRoles = setOf(UserType.ClientUser::class),
        navController = navController,
        loginViewModel = loginViewModel
    ) {
        val context = LocalContext.current
        val activity =
            context as? Activity ?: throw IllegalStateException("Context is not an Activity")
        val windowSizeClass = calculateWindowSizeClass(activity)
        val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
        val restaurantes by viewModel.restaurantes.collectAsStateWithLifecycle()
        val uiState = UiState(
            isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
            errorMessage = viewModel.errorMessage.collectAsStateWithLifecycle().value
        )

        // Trigger data loading on composition
        LaunchedEffect(Unit) { viewModel.loadRestaurantData() }

        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Restaurantes",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
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
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    uiState.isLoading -> CircularProgressIndicator(
                        modifier = Modifier.align(
                            Alignment.CenterHorizontally
                        )
                    )

                    uiState.errorMessage != null -> Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )

                    restaurantes.isEmpty() -> Text(
                        text = "No hay restaurantes disponibles",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )

                    else -> Row(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(
                                items = restaurantes,
                                key = { it.id }
                            ) { restaurante ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn() + slideInVertically(),
                                    exit = fadeOut() + slideOutVertically(),
                                    label = "restaurant_item_transition"
                                ) {
                                    RestauranteCard(
                                        restaurante = restaurante,
                                        onClick = {
                                            val restaurantJson = Gson().toJson(restaurante)
                                            navController.navigate("restaurant_details/$clienteId/$restaurantJson")
                                        }
                                    )
                                }
                            }
                        }
                        if (isExpanded) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Seleccione un restaurante para ver detalles",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class UiState(
    val isLoading: Boolean,
    val errorMessage: String?
)
