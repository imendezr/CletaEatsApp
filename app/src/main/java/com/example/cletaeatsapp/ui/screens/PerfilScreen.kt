package com.example.cletaeatsapp.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.cletaeatsapp.data.model.UserType
import com.example.cletaeatsapp.viewmodel.LoginViewModel
import com.example.cletaeatsapp.viewmodel.PerfilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    loginViewModel: LoginViewModel,
    navController: NavController,
    perfilViewModel: PerfilViewModel = hiltViewModel()
) {
    val cedulaState by loginViewModel.cedulaFlow.collectAsStateWithLifecycle()
    val isLoadingState by loginViewModel.isLoading.collectAsStateWithLifecycle()
    val userTypeState by loginViewModel.userType.collectAsStateWithLifecycle()
    var isEditing by remember { mutableStateOf(false) }
    var showPasswordChange by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Lift all editable fields to outer scope
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var distancia by remember {
        mutableStateOf((userTypeState as? UserType.RepartidorUser)?.repartidor?.distancia?.toString() ?: "")
    }
    var costoPorKm by remember {
        mutableStateOf((userTypeState as? UserType.RepartidorUser)?.repartidor?.costoPorKm?.toString() ?: "")
    }
    var nombre by remember {
        mutableStateOf(
            (userTypeState as? UserType.ClientUser)?.cliente?.nombre
                ?: (userTypeState as? UserType.RepartidorUser)?.repartidor?.nombre
                ?: (userTypeState as? UserType.RestauranteUser)?.restaurante?.nombre ?: ""
        )
    }
    var direccion by remember {
        mutableStateOf(
            (userTypeState as? UserType.ClientUser)?.cliente?.direccion
                ?: (userTypeState as? UserType.RepartidorUser)?.repartidor?.direccion
                ?: (userTypeState as? UserType.RestauranteUser)?.restaurante?.direccion ?: ""
        )
    }
    var telefono by remember {
        mutableStateOf(
            (userTypeState as? UserType.ClientUser)?.cliente?.telefono
                ?: (userTypeState as? UserType.RepartidorUser)?.repartidor?.telefono ?: ""
        )
    }
    var correo by remember {
        mutableStateOf(
            (userTypeState as? UserType.ClientUser)?.cliente?.correo
                ?: (userTypeState as? UserType.RepartidorUser)?.repartidor?.correo ?: ""
        )
    }
    var tipoComida by remember {
        mutableStateOf((userTypeState as? UserType.RestauranteUser)?.restaurante?.tipoComida ?: "")
    }

    LaunchedEffect(userTypeState, cedulaState) {
        if (cedulaState.isBlank() || userTypeState == null) {
            navController.navigate("login") {
                popUpTo("login") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
                    }
                },
                actions = {
                    IconButton(onClick = loginViewModel::logout) {
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
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = when (userTypeState) {
                    is UserType.ClientUser -> "Perfil del Cliente"
                    is UserType.RepartidorUser -> "Perfil del Repartidor"
                    is UserType.RestauranteUser -> "Perfil del Restaurante"
                    else -> "Perfil"
                },
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
            )
            AnimatedContent(
                targetState = isLoadingState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "loading_transition"
            ) { loading ->
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp)
                    )
                } else if (cedulaState.isBlank() || userTypeState == null) {
                    Text(
                        text = "No hay datos de usuario disponibles. Inicie sesión.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                } else {
                    val userData = userTypeState!!
                    AnimatedContent(
                        targetState = isEditing,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "profile_edit_transition"
                    ) { editing ->
                        if (editing) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(scrollState)
                            ) {
                                ProfileField(label = "Cédula", value = cedulaState, enabled = false)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = nombre,
                                    onValueChange = { nombre = it },
                                    label = { Text("Nombre") },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = true,
                                    isError = false,
                                    supportingText = { }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = direccion,
                                    onValueChange = { direccion = it },
                                    label = { Text("Dirección") },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = true,
                                    isError = false,
                                    supportingText = { }
                                )
                                if (userData is UserType.RestauranteUser) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = tipoComida,
                                        onValueChange = { tipoComida = it },
                                        label = { Text("Tipo de Comida") },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = true,
                                        isError = false,
                                        supportingText = { }
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = telefono,
                                    onValueChange = { telefono = it },
                                    label = { Text("Teléfono") },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = true,
                                    isError = false,
                                    supportingText = { }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = correo,
                                    onValueChange = { correo = it },
                                    label = { Text("Correo") },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = true,
                                    isError = false,
                                    supportingText = { }
                                )
                                if (userData is UserType.RepartidorUser) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = distancia,
                                        onValueChange = { distancia = it },
                                        label = { Text("Distancia (km)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = true,
                                        isError = false,
                                        supportingText = { }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = costoPorKm,
                                        onValueChange = { costoPorKm = it },
                                        label = { Text("Costo por km") },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = true,
                                        isError = false,
                                        supportingText = { }
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                TextButton(onClick = { showPasswordChange = !showPasswordChange }) {
                                    Text(if (showPasswordChange) "Ocultar cambio de contraseña" else "Cambiar contraseña")
                                }
                                if (showPasswordChange) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = currentPassword,
                                        onValueChange = { currentPassword = it },
                                        label = { Text("Contraseña actual") },
                                        modifier = Modifier.fillMaxWidth(),
                                        visualTransformation = PasswordVisualTransformation()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = newPassword,
                                        onValueChange = { newPassword = it },
                                        label = { Text("Nueva contraseña") },
                                        modifier = Modifier.fillMaxWidth(),
                                        visualTransformation = PasswordVisualTransformation()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = confirmPassword,
                                        onValueChange = { confirmPassword = it },
                                        label = { Text("Confirmar contraseña") },
                                        modifier = Modifier.fillMaxWidth(),
                                        visualTransformation = PasswordVisualTransformation()
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        perfilViewModel.updateProfile(
                                            cedula = cedulaState,
                                            nombre = nombre,
                                            direccion = direccion,
                                            telefono = telefono,
                                            correo = correo,
                                            distancia = distancia.toDoubleOrNull(),
                                            costoPorKm = costoPorKm.toDoubleOrNull(),
                                            currentPassword = currentPassword,
                                            newPassword = newPassword,
                                            confirmPassword = confirmPassword
                                        ) {
                                            isEditing = false
                                            showPasswordChange = false
                                            loginViewModel.loadUserData() // Reload user data
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Guardar") }
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = {
                                    isEditing = false
                                    showPasswordChange = false
                                }) {
                                    Text("Cancelar")
                                }
                            }
                        } else {
                            Column {
                                ProfileField(label = "Cédula", value = cedulaState, enabled = false)
                                ProfileField(
                                    label = "Nombre",
                                    value = (userData as? UserType.ClientUser)?.cliente?.nombre
                                        ?: (userData as? UserType.RepartidorUser)?.repartidor?.nombre
                                        ?: (userData as? UserType.RestauranteUser)?.restaurante?.nombre
                                        ?: ""
                                )
                                ProfileField(
                                    label = "Dirección",
                                    value = (userData as? UserType.ClientUser)?.cliente?.direccion
                                        ?: (userData as? UserType.RepartidorUser)?.repartidor?.direccion
                                        ?: (userData as? UserType.RestauranteUser)?.restaurante?.direccion
                                        ?: ""
                                )
                                if (userData is UserType.RestauranteUser) {
                                    ProfileField(
                                        label = "Tipo de Comida",
                                        value = userData.restaurante.tipoComida
                                    )
                                }
                                ProfileField(
                                    label = "Teléfono",
                                    value = (userData as? UserType.ClientUser)?.cliente?.telefono
                                        ?: (userData as? UserType.RepartidorUser)?.repartidor?.telefono
                                        ?: ""
                                )
                                ProfileField(
                                    label = "Correo",
                                    value = (userData as? UserType.ClientUser)?.cliente?.correo
                                        ?: (userData as? UserType.RepartidorUser)?.repartidor?.correo
                                        ?: ""
                                )
                                if (userData is UserType.RepartidorUser) {
                                    ProfileField(
                                        label = "Distancia (km)",
                                        value = userData.repartidor.distancia.toString()
                                    )
                                    ProfileField(
                                        label = "Costo por km",
                                        value = userData.repartidor.costoPorKm.toString()
                                    )
                                    ProfileField(
                                        label = "Estado",
                                        value = userData.repartidor.estado
                                    )
                                    ProfileField(
                                        label = "Amonestaciones",
                                        value = userData.repartidor.amonestaciones.toString()
                                    )
                                    ProfileField(
                                        label = "Quejas",
                                        value = userData.repartidor.quejas.joinToString(", ")
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { isEditing = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Editar") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileField(label: String, value: String, enabled: Boolean = true) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value.ifEmpty { "No disponible" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
