package com.example.cletaeatsapp.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.cletaeatsapp.viewmodel.LoginViewModel
import com.example.cletaeatsapp.viewmodel.NavigationEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val cedula by loginViewModel.cedulaFlow.collectAsStateWithLifecycle()
    val contrasena by loginViewModel.contrasena.collectAsStateWithLifecycle()
    val isLoading by loginViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by loginViewModel.errorMessage.collectAsStateWithLifecycle()
    val fieldErrors by loginViewModel.fieldErrors.collectAsStateWithLifecycle()
    val navigationEvent by loginViewModel.navigationEvent.collectAsStateWithLifecycle()

    LaunchedEffect(navigationEvent) {
        when (navigationEvent) {
            is NavigationEvent.NavigateToClienteHome -> {
                val id = (navigationEvent as NavigationEvent.NavigateToClienteHome).id
                navController.navigate("restaurants/$id") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
                loginViewModel.clearNavigationEvent()
            }

            is NavigationEvent.NavigateToRepartidorHome -> {
                val id = (navigationEvent as NavigationEvent.NavigateToRepartidorHome).id
                navController.navigate("repartidor_orders/$id") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
                loginViewModel.clearNavigationEvent()
            }

            is NavigationEvent.NavigateToRestauranteOrders -> {
                val id = (navigationEvent as NavigationEvent.NavigateToRestauranteOrders).id
                navController.navigate("restaurante_orders/$id") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
                loginViewModel.clearNavigationEvent()
            }

            is NavigationEvent.NavigateToAdminHome -> {
                navController.navigate("reports") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
                loginViewModel.clearNavigationEvent()
            }

            is NavigationEvent.NavigateToLogin -> {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
                loginViewModel.clearNavigationEvent()
            }

            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CletaEats", style = MaterialTheme.typography.headlineSmall) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Iniciar Sesión",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            AnimatedContent(
                targetState = isLoading,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "loading_transition"
            ) { loading ->
                if (loading) {
                    CircularProgressIndicator()
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            value = cedula,
                            onValueChange = loginViewModel::updateCedula,
                            label = { Text("Cédula o usuario") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = fieldErrors.containsKey("cedula") || fieldErrors.containsKey("general"),
                            supportingText = {
                                fieldErrors["cedula"]?.let {
                                    Text(
                                        text = it,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } ?: fieldErrors["general"]?.let {
                                    Text(
                                        text = it,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = contrasena,
                            onValueChange = loginViewModel::updateContrasena,
                            label = { Text("Contraseña") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            isError = fieldErrors.containsKey("contrasena") || fieldErrors.containsKey(
                                "general"
                            ),
                            supportingText = {
                                fieldErrors["contrasena"]?.let {
                                    Text(
                                        text = it,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } ?: fieldErrors["general"]?.let {
                                    Text(
                                        text = it,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        errorMessage.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        Button(
                            onClick = { loginViewModel.login { _, _ -> } },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            Text("Iniciar Sesión")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { navController.navigate("register") },
                            enabled = !isLoading
                        ) {
                            Text("Registrarse")
                        }
                    }
                }
            }
        }
    }
}
