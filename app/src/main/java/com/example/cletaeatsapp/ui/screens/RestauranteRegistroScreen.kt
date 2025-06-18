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
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.cletaeatsapp.data.repository.UserType
import com.example.cletaeatsapp.viewmodel.LoginViewModel
import com.example.cletaeatsapp.viewmodel.RestauranteRegistroViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun RestauranteRegistroScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: RestauranteRegistroViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val userType by loginViewModel.userType.collectAsStateWithLifecycle()
    val cedulaJuridica by viewModel.cedulaJuridica
    val nombre by viewModel.nombre
    val direccion by viewModel.direccion
    val tipoComida by viewModel.tipoComida
    val errorMessage by viewModel.errorMessage
    val isLoading by viewModel.isLoading
    val context = LocalContext.current
    val windowSizeClass = calculateWindowSizeClass(context as androidx.activity.ComponentActivity)
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    val padding = if (isExpanded) 32.dp else 16.dp

    // Restrict access to admins only
    LaunchedEffect(userType) {
        if (userType !is UserType.AdminUser) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    if (userType !is UserType.AdminUser) return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Restaurante") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Registro de Restaurante",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedContent(
                targetState = isLoading,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "restaurant_form_transition"
            ) { loading ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    OutlinedTextField(
                        value = cedulaJuridica,
                        onValueChange = { viewModel.cedulaJuridica.value = it },
                        label = { Text("Cédula Jurídica") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !loading,
                        isError = errorMessage != null && cedulaJuridica.isBlank()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { viewModel.nombre.value = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !loading,
                        isError = errorMessage != null && nombre.isBlank()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = direccion,
                        onValueChange = { viewModel.direccion.value = it },
                        label = { Text("Dirección") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !loading,
                        isError = errorMessage != null && direccion.isBlank()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tipoComida,
                        onValueChange = { viewModel.tipoComida.value = it },
                        label = { Text("Tipo de Comida") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !loading,
                        isError = errorMessage != null && tipoComida.isBlank()
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.register {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Registrar")
                }
            }
            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
